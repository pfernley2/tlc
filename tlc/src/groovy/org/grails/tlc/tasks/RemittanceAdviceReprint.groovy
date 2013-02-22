/*
 *  Copyright 2010-2013 Paul Fernley.
 *
 *  This file is part of the Three Ledger Core (TLC) software
 *  from Paul Fernley.
 *
 *  TLC is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TLC is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TLC. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grails.tlc.tasks

import org.grails.tlc.books.Remittance
import org.grails.tlc.books.Supplier
import org.grails.tlc.books.SupplierAddress
import org.grails.tlc.sys.SystemSupplierAddressType
import org.grails.tlc.sys.SystemSupplierContactType
import org.grails.tlc.sys.SystemWorkarea
import org.grails.tlc.sys.TaskExecutable

public class RemittanceAdviceReprint extends TaskExecutable {

    def execute() {
        def count = 0
        def batch = 1
        def batchCount = 0
        def pid = utilService.getNextProcessId()
        def session = runSessionFactory.currentSession
        def reportParams = [:]
        def countryNamesMap = [:]
        def countries = SupplierAddress.executeQuery('select distinct x.country from SupplierAddress as x where x.supplier.company = ? and x.country != ?', [company, company.country])
        for (country in countries) countryNamesMap.put(country.id.toString(), message(code: 'country.name.' + country.code, default: country.name))
        def remittanceAddressType = SystemSupplierAddressType.findByCode('remittance')
        def remittanceContactType = SystemSupplierContactType.findByCode('remittance')
        def title = message(code: 'remittance.reprint.title', default: 'Remittance Advice Reprint')
        def specificSupplier, selectedCodes
        if (params.supplier) {
            specificSupplier = Supplier.get(params.supplier)
            if (bookService.hasSupplierAccess(specificSupplier, company, user)) {
                reportParams.specificSupplier = specificSupplier.code
                reportParams.codes = message(code: 'generic.not.applicable', default: 'n/a')
                reportParams.noSummary = true
            } else {
                completionMessage = utilService.standardMessage('not.found', 'supplier', params.supplier)
                return false
            }
        } else {
            def accessCodeList = bookService.supplierAccessCodes(company, user)
            def codes = params.codes?.split(',') as List
            if (codes) {
                selectedCodes = accessCodeList.findAll {codes.contains(it.code)}
                reportParams.codes = params.codes
            } else {
                selectedCodes = accessCodeList
                reportParams.codes = message(code: 'generic.all.selection', default: '-- all --')
            }

            if (!selectedCodes) {
                completionMessage = message(code: 'report.no.access', default: 'You do not have permission to access any accounts and therefore cannot run this report.')
                return false
            }

            reportParams.specificSupplier = message(code: 'generic.not.applicable', default: 'n/a')
        }

        def batchSize = params.batchSize ?: 0
        if (batchSize < 0) {
            completionMessage = message(code: 'generic.batch.size', default: 'Invalid batch size')
            return false
        }

        reportParams.pid = pid
        reportParams.reportTitle = title
        reportParams.countryNamesMap = countryNamesMap
        reportParams.companyCountryId = company.country.id
        reportParams.addressTypeId = remittanceAddressType?.id
        reportParams.contactTypeId = remittanceContactType?.id

        reportParams.txtTitle = message(code: 'remittance.title', default: 'Remittance Advice')
        reportParams.txtAccount = message(code: 'remittance.account', default: 'Account')
        reportParams.txtDate = message(code: 'remittance.date', default: 'Date')
        reportParams.txtCurrency = message(code: 'remittance.currency', default: 'Currency')
        reportParams.txtPage = message(code: 'remittance.page', default: 'Page')
        reportParams.batchPrompt = message(code: 'generic.batch', default: 'Batch')
        reportParams.txtPart = message(code: 'remittance.part', default: 'Part')
        reportParams.txtDocument = message(code: 'remittance.doc', default: 'Document')
        reportParams.txtDocDate = message(code: 'remittance.docdate', default: 'Date')
        reportParams.txtReference = message(code: 'remittance.ref', default: 'Reference')
        reportParams.txtDebit = message(code: 'generic.debit', default: 'Debit')
        reportParams.txtCredit = message(code: 'generic.credit', default: 'Credit')
        reportParams.txtBF = message(code: 'generic.bf', default: 'b/f')
        reportParams.txtCF = message(code: 'generic.cf', default: 'c/f')
        reportParams.txtTotal = message(code: 'remittance.total', default: 'Payment')
        reportParams.txtReprint = message(code: 'remittance.reprint', default: 'REPRINT')
        reportParams.txtSupplier = message(code: 'remittance.supplier.label', default: 'Specific Supplier')
        reportParams.codesPrompt = message(code: 'report.accessCode.label', default: 'Access Code(s)')

        def adviceDate = params.adviceDate ?: utilService.fixDate()
        def remittances
        if (specificSupplier) {
            remittances = Remittance.findAll('from Remittance where supplier = ? and adviceDate = ? and paymentDate is not null', [specificSupplier, adviceDate])
        } else {
            def listClause = ''
            for (int i = 0; i < selectedCodes.size(); i++) {
                if (i) listClause += ','
                listClause += selectedCodes[i].id.toString()
            }

            remittances = Remittance.findAll('from Remittance as x where x.adviceDate = ? and x.paymentDate is not null and x.supplier.accessCode in (' + listClause + ')', [adviceDate])
        }

        def val
        def valid = true
        Remittance.withTransaction {status ->
            for (remittance in remittances) {
                val = new SystemWorkarea(process: pid, identifier: remittance.id, integer1: batch, string1: remittance.supplier.code)
                if (val.saveThis()) {
                    count++
                    batchCount++
                    if (batchSize && batchCount == batchSize) {
                        batch++
                        batchCount = 0
                    }

                    session.evict(remittance)
                } else {
                    status.setRollbackOnly()
                    completionMessage = message(code: 'generic.workarea', default: 'Unable to update the work table')
                    valid = false
                }
            }
        }

        yield()
        if (count) {
            if (!batchCount) batch--
            reportParams.batches = batch
            for (int rpt = 1; rpt <= batch; rpt++) {

                reportParams.batch = rpt
                def pdfFile = createReportPDF('Remittances', reportParams)
                yield()
                mailService.sendMail {
                    multipart true
                    to user.email
                    subject "${title} (${rpt}/${batch})"
                    body(view: '/emails/genericReport', model: [companyInstance: company, systemUserInstance: user, title: title])
                    attach pdfFile
                }

                yield()
                pdfFile.delete()
            }

            SystemWorkarea.withTransaction {status ->
                SystemWorkarea.executeUpdate('delete from SystemWorkarea where process = ?', [pid])
            }
        } else {
            batch = 0
        }

        results.advices = count
        results.batches = batch
        return valid
    }
}