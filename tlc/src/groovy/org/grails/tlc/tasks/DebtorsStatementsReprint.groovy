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

import org.grails.tlc.books.Customer
import org.grails.tlc.books.CustomerAddress
import org.grails.tlc.books.Statement
import org.grails.tlc.sys.SystemCustomerAddressType
import org.grails.tlc.sys.SystemCustomerContactType
import org.grails.tlc.sys.SystemWorkarea
import org.grails.tlc.sys.TaskExecutable

public class DebtorsStatementsReprint extends TaskExecutable {

    def execute() {
        def count = 0
        def batch = 1
        def batchCount = 0
        def pid = utilService.getNextProcessId()
        def reportParams = [:]
        def countryNamesMap = [:]
        def countries = CustomerAddress.executeQuery('select distinct x.country from CustomerAddress as x where x.customer.company = ? and x.country != ?', [company, company.country])
        for (country in countries) countryNamesMap.put(country.id.toString(), message(code: 'country.name.' + country.code, default: country.name))
        def statementAddressType = SystemCustomerAddressType.findByCode('statement')
        def statementContactType = SystemCustomerContactType.findByCode('statement')
        def title = message(code: 'customer.statement.reprint', default: 'Customer Statement Reprint')
        def specificCustomer, selectedCodes
        if (params.customer) {
            specificCustomer = Customer.get(params.customer)
            if (bookService.hasCustomerAccess(specificCustomer, company, user)) {
                reportParams.specificCustomer = specificCustomer.code
                reportParams.codes = message(code: 'generic.not.applicable', default: 'n/a')
                reportParams.noSummary = true
            } else {
                completionMessage = utilService.standardMessage('not.found', 'customer', params.customer)
                return false
            }
        } else {
            def accessCodeList = bookService.customerAccessCodes(company, user)
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

            reportParams.specificCustomer = message(code: 'generic.not.applicable', default: 'n/a')
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
        reportParams.addressTypeId = statementAddressType?.id
        reportParams.contactTypeId = statementContactType?.id
        reportParams.txtTitle = message(code: 'customer.statement.title', default: 'Statement of Account')
        reportParams.txtAccount = message(code: 'customer.statement.account', default: 'Account')
        reportParams.txtDate = message(code: 'customer.statement.date', default: 'Date')
        reportParams.txtCurrency = message(code: 'customer.statement.currency', default: 'Currency')
        reportParams.txtPage = message(code: 'customer.statement.page', default: 'Page')
        reportParams.batchPrompt = message(code: 'generic.batch', default: 'Batch')
        reportParams.txtPart = message(code: 'customer.statement.part', default: 'Part')
        reportParams.txtDocument = message(code: 'customer.statement.doc', default: 'Document')
        reportParams.txtDocDate = message(code: 'customer.statement.docdate', default: 'Date')
        reportParams.txtDueDate = message(code: 'customer.statement.duedate', default: 'Due')
        reportParams.txtUnallocated = message(code: 'customer.statement.unallocated', default: 'Unallocated')
        reportParams.txtReference = message(code: 'customer.statement.ref', default: 'Reference')
        reportParams.txtPrevious = message(code: 'customer.statement.previous', default: 'From Previous Statements')
        reportParams.txtCurrent = message(code: 'customer.statement.current', default: 'New Items')
        reportParams.txtOD = message(code: 'customer.statement.od', default: 'O/D')
        reportParams.txtAmount = message(code: 'customer.statement.amount', default: 'Amount')
        reportParams.txtBF = message(code: 'generic.bf', default: 'b/f')
        reportParams.txtCF = message(code: 'generic.cf', default: 'c/f')
        reportParams.txtDue = message(code: 'customer.statement.due', default: 'Due')
        reportParams.txtOverdue = message(code: 'customer.statement.overdue', default: 'Overdue')
        reportParams.txtTotal = message(code: 'customer.statement.total', default: 'Statement Total')
        reportParams.txtReprint = message(code: 'customer.statement.status', default: 'REPRINT')
        reportParams.txtCustomer = message(code: 'customer.statement.customer.label', default: 'Specific Customer')
        reportParams.codesPrompt = message(code: 'report.accessCode.label', default: 'Access Code(s)')

        def statementDate = params.stmtDate ?: utilService.fixDate()
        def statementIds
        if (specificCustomer) {
            statementIds = Statement.executeQuery('select id from Statement where customer = ? and statementDate = ?', [specificCustomer, statementDate])
        } else {
            def listClause = ''
            for (int i = 0; i < selectedCodes.size(); i++) {
                if (i) listClause += ','
                listClause += selectedCodes[i].id.toString()
            }

            statementIds = Statement.executeQuery("select s.id from Statement as s join s.customer as c where c.accessCode.id in (${listClause}) and s.statementDate = ? order by c.code", [statementDate])
        }

        def val
        def valid = true
        Statement.withTransaction {status ->
            for (statementId in statementIds) {
                val = new SystemWorkarea(process: pid, identifier: count, long1: statementId, integer1: batch)
                if (val.saveThis()) {
                    count++
                    batchCount++
                    if (batchSize && batchCount == batchSize) {
                        batch++
                        batchCount = 0
                    }
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
                def pdfFile = createReportPDF('Statements', reportParams)
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

        results.statements = count
        results.batches = batch
        return valid
    }
}