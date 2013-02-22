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

import org.grails.tlc.sys.SystemCustomerAddressType
import org.grails.tlc.sys.SystemCustomerContactType
import org.grails.tlc.sys.SystemWorkarea
import org.grails.tlc.sys.TaskExecutable
import org.grails.tlc.books.*

public class DebtorsStatementsReport extends TaskExecutable {

    def execute() {
        def postingCutoff = utilService.setting('statements.use.posting.date.cutoff', true, company)
        def documentCutoff = utilService.setting('statements.use.document.date.cutoff', false, company)
        if (!postingCutoff && !documentCutoff) postingCutoff = true
        def count = 0
        def batch = 1
        def batchCount = 0
        def pid = utilService.getNextProcessId()
        def session = runSessionFactory.currentSession
        def reportParams = [:]
        def countryNamesMap = [:]
        def countries = CustomerAddress.executeQuery('select distinct x.country from CustomerAddress as x where x.customer.company = ? and x.country != ?', [company, company.country])
        for (country in countries) countryNamesMap.put(country.id.toString(), message(code: 'country.name.' + country.code, default: country.name))
        def statementAddressType = SystemCustomerAddressType.findByCode('statement')
        def statementContactType = SystemCustomerContactType.findByCode('statement')
        def title = message(code: 'customer.statements.report', default: 'Customer Statements')
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
        reportParams.txtCustomer = message(code: 'customer.statement.customer.label', default: 'Specific Customer')
        reportParams.codesPrompt = message(code: 'report.accessCode.label', default: 'Access Code(s)')

        def statementDate = params.stmtDate ?: utilService.fixDate()
        def cutoffDate = statementDate + 1
        def customerIds
        if (specificCustomer) {
            customerIds = [specificCustomer.id]
        } else {
            def listClause = ''
            for (int i = 0; i < selectedCodes.size(); i++) {
                if (i) listClause += ','
                listClause += selectedCodes[i].id.toString()
            }

            customerIds = Customer.executeQuery("select id from Customer where accessCode.id in (${listClause}) order by code")
        }

        def customer, type, code, val, doc, allocs, src, tgt, iter, transactions, statement
        def lineMap = [:]
        def valid = true
        for (custId in customerIds) {
            yield()
            Statement.withTransaction {status ->
                customer = Customer.lock(custId)
                transactions = null
                statement = null
                if (customer && !Statement.countByCustomerAndStatementDateGreaterThanEquals(customer, statementDate)) {
                    transactions = GeneralTransaction.findAll('from GeneralTransaction as x where x.customer = ? and x.accountValue != 0.0 and (x.reconciled is null or x.accountUnallocated != 0.0)', [customer])
                    if (transactions) {

                        // Create a map of statement lines from the transactions
                        lineMap.clear()
                        for (tran in transactions) {
                            doc = tran.document
                            type = doc.type.code
                            code = doc.code
                            val = new StatementLine(type: type, code: code, documentDate: doc.documentDate,
                                dueDate: doc.dueDate, reference: doc.reference, originalValue: tran.accountValue,
                                openingUnallocated: tran.reconciled ? tran.accountUnallocated : tran.accountValue,
                                closingUnallocated: tran.accountUnallocated, sequencer: tran.id,
                                currentStatement: setStatus(postingCutoff, documentCutoff, cutoffDate, tran.reconciled, tran.dateCreated, doc.documentDate))
                            val.source = tran

                            lineMap.put(type + code, val)
                        }

                        // Work through again rewinding current statement allocations
                        // insofar as they affect prior statment items or, if an after-date
                        // item, an in-date item
                        for (tran in transactions) {

                            // New items only
                            if (!tran.reconciled) {
                                src = lineMap.get(tran.document.type.code + tran.document.code)
                                allocs = tran.allocations
                                for (alloc in allocs) {
                                    tgt = getStatementLine(lineMap, alloc, postingCutoff, documentCutoff, cutoffDate)
                                    if (!isFuture(tgt)) {
                                        if (isHistory(tgt)) tgt.openingUnallocated += alloc.accountValue
                                        if (isFuture(src)) tgt.closingUnallocated += alloc.accountValue
                                    }
                                }
                            }
                        }

                        // Remove any after-date items and historic lines that are no longer applicable
                        iter = lineMap.keySet().iterator()
                        while (iter.hasNext()) {
                            val = lineMap.get(iter.next())
                            if (isFuture(val) || (isHistory(val) && !val.openingUnallocated) && !val.closingUnallocated) iter.remove()
                        }

                        // If there are any statement lines left
                        if (lineMap) {
                            statement = new Statement(customer: customer, statementDate: statementDate)
                            if (statement.saveThis()) {
                                iter = lineMap.keySet().iterator()
                                while (iter.hasNext()) {
                                    val = lineMap.get(iter.next())
                                    val.source.reconciled = statementDate
                                    if (!val.source.saveThis()) {
                                        status.setRollbackOnly()
                                        completionMessage = message(code: 'customer.statement.bad.reconciled', args: [customer.code],
                                            default: "Unable to modify the reconciled dates for customer ${customer.code}")
                                        valid = false
                                        break
                                    }

                                    statement.addToLines(val)
                                }

                                if (valid) {
                                    if (statement.save(flush: true)) {      // With deep validation

                                        // Insert temporary table stuff
                                        val = new SystemWorkarea(process: pid, identifier: count, long1: statement.id, integer1: batch)
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
                                    } else {
                                        status.setRollbackOnly()
                                        completionMessage = message(code: 'customer.statement.bad.lines', args: [customer.code],
                                            default: "Unable to save the statement lines for customer ${customer.code}")
                                        valid = false
                                    }
                                }
                            } else {
                                status.setRollbackOnly()
                                completionMessage = message(code: 'customer.statement.bad.header', args: [customer.code], default: "Unable to save the statement header for customer ${customer.code}")
                                valid = false
                            }
                        }
                    }
                }
            }

            if (valid) {
                if (transactions) {
                    for (tran in transactions) session.evict(tran)
                }

                if (statement) session.evict(statement)
                session.evict(customer)
            } else {
                break
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

    // --------------------------------------------- Support Methods ---------------------------------------------

    private getStatementLine(map, alloc, postingCutoff, documentCutoff, cutoffDate) {
        def type = alloc.targetType.code
        def code = alloc.targetCode
        def val = map.get(type + code)
        if (!val) {
            def tran = GeneralTransaction.get(alloc.targetId)
            def doc = tran.document
            val = new StatementLine(type: type, code: code, documentDate: doc.documentDate, dueDate: doc.dueDate,
                reference: doc.reference, originalValue: tran.accountValue,
                openingUnallocated: tran.reconciled ? tran.accountUnallocated : tran.accountValue,
                closingUnallocated: tran.accountUnallocated, sequencer: tran.id,
                currentStatement: setStatus(postingCutoff, documentCutoff, cutoffDate, tran.reconciled, tran.dateCreated, doc.documentDate))
            val.source = tran

            map.put(type + code, val)
        }

        return val
    }

    private setStatus(postingCutoff, documentCutoff, cutoffDate, reconciled, dateCreated, documentDate) {
        if (reconciled) return 0
        return ((postingCutoff && dateCreated >= cutoffDate) || (documentCutoff && documentDate >= cutoffDate)) ? 2 : 1
    }

    private isHistory(line) {
        return line.currentStatement == 0
    }

    private isFuture(line) {
        return line.currentStatement == 2
    }
}