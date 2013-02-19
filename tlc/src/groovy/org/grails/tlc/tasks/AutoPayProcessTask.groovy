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

import org.grails.tlc.corp.ExchangeCurrency
import org.grails.tlc.sys.SystemSupplierAddressType
import org.grails.tlc.sys.SystemSupplierContactType
import org.grails.tlc.sys.SystemWorkarea
import org.grails.tlc.sys.TaskExecutable
import doc.Line
import doc.Total
import org.grails.tlc.books.*

class AutoPayProcessTask extends TaskExecutable {

    def execute() {
        if (company.systemOnly) return true     // Don't do this for the System company

        def documentTypeId, currencyId, remittanceId, payeeCount, maxPayees, documentCode, result
        def documentType, documentCurrency, bankAccount, bankCurrency, bankRate, companyRate
        def pid = utilService.getNextProcessId()
        def valid = true
        def printAdvices = true
        def batchSize = params.batchSize ?: 0
        def reference = message(code: 'remittance.reference', default: 'auto')
        def description = message(code: 'remittance.description', default: 'Automatic Payment')
        def documentsCreated = 0
        def remittancesPaid = 0
        def today = utilService.fixDate()
        def session = runSessionFactory.currentSession
        def remittanceIds = []
        def remittances = []
        def rejections = []
        def rates = [:]
        def customizationMap = bookService.paymentService ? [pid: utilService.getNextProcessId(), company: company, user: user, locale: locale, today: today] : null
        def apControlAccount = bookService.getControlAccount(company, 'ap')
        if (!apControlAccount) {
            completionMessage = message(code: 'document.no.control', args: ['ap'], default: 'Could not find the ap control account in the General Ledger')
            return false
        }

        def documentPeriod = bookService.selectPeriod(bookService.getOpenPeriods(company), today)
        if (!documentPeriod) {
            completionMessage = message(code: 'document.no.period', default: 'Could not find an open period to post the document to')
            return false
        }

        def sql = 'select x.supplier.documentType.id, x.supplier.currency.id, x.id' +
                ' from Remittance as x' +
                ' where x.authorizedDate is not null and x.paymentDate is null and x.supplier.company = ?' +
                ' order by x.supplier.documentType.id, x.supplier.currency.id, x.id'
        def records = Remittance.executeQuery(sql, [company])
        yield()
        if (records && customizationMap) {
            completionMessage = bookService.paymentService.initializePaymentRun(customizationMap)
            if (completionMessage) return false
        }

        for (record in records) {
            documentTypeId = record[0]
            currencyId = record[1]
            remittanceId = record[2]

            // If we need to start a new payment document
            if (documentTypeId != documentType?.id || currencyId != documentCurrency?.id || payeeCount == maxPayees) {

                // If there is  payment document to process
                if (payeeCount) {
                    result = processPayment(documentType, documentCode, documentPeriod, documentCurrency, bankAccount, bankCurrency, bankRate, companyRate,
                            remittanceIds, remittances, apControlAccount, today, reference, description, rejections, customizationMap, pid)
                    if (result) {
                        if (result instanceof String) {
                            completionMessage = result
                            valid = false
                            break
                        }

                        for (remittance in remittances) {
                            remittancesPaid++
                            processAllocation(remittance, result, session)
                            session.evict(remittance)
                        }

                        session.evict(result)
                        documentsCreated++
                    }
                }

                // Load document specific data and get next number
                if (documentType) documentType.discard()
                DocumentType.withTransaction {status ->
                    documentType = DocumentType.lock(documentTypeId)
                    documentCode = documentType.nextSequenceNumber.toString()
                    documentType.nextSequenceNumber += 1
                    if (!documentType.saveThis()) {
                        completionMessage = message(code: 'document.bad.next', default: 'Unable to update the next document sequence number')
                        valid = false
                        status.setRollbackOnly()
                    }
                }

                if (!valid) break

                bankAccount = documentType.autoBankAccount
                bankCurrency = bankAccount.currency
                maxPayees = documentType.autoMaxPayees

                // Handle change of supplier currency if needed
                if (currencyId != documentCurrency?.id) documentCurrency = ExchangeCurrency.get(currencyId)

                // Set the exchange rates
                bankRate = getRate(documentCurrency, bankCurrency, rates, today)
                if (!bankRate) {
                    completionMessage = message(code: 'document.bad.exchangeRate', args: [documentCurrency.code, bankCurrency.code],
                            default: "No exchange rate available from ${documentCurrency.code} to ${bankCurrency.code}")
                    valid = false
                    break
                }

                companyRate = getRate(documentCurrency, currency, rates, today)
                if (!companyRate) {
                    completionMessage = message(code: 'document.bad.exchangeRate', args: [documentCurrency.code, currency.code],
                            default: "No exchange rate available from ${documentCurrency.code} to ${currency.code}")
                    valid = false
                    break
                }

                // Clear the list of remittance ids potentially included in this document
                remittanceIds.clear()

                // Clear the remittances actually paid by this document
                remittances.clear()

                // Reset the payees counter
                payeeCount = 0
            }

            // Add the new remittance id to the list of those potentially included in the current document
            remittanceIds << remittanceId

            // Bump the count of potential payees
            payeeCount++
        }

        // Finish off any last document
        if (valid && payeeCount) {
            result = processPayment(documentType, documentCode, documentPeriod, documentCurrency, bankAccount, bankCurrency, bankRate, companyRate,
                    remittanceIds, remittances, apControlAccount, today, reference, description, rejections, customizationMap, pid)
            if (result) {
                if (result instanceof String) {
                    completionMessage = result
                    valid = false
                } else {
                    for (remittance in remittances) {
                        remittancesPaid++
                        processAllocation(remittance, result, session)
                        session.evict(remittance)
                    }

                    session.evict(result)
                    documentsCreated++
                }
            }
        }

        if (records && customizationMap) {
            yield()
            result = bookService.paymentService.finalizePaymentRun(customizationMap)
            if (result) {

                // If there is not already an error message and if we do
                // intend to print remittance advices, set our completion
                // message to their reason for not printing advices
                if (valid && remittancesPaid) completionMessage = result
                printAdvices = false
            }
        }

        if (rejections) {
            yield()
            def title = message(code: 'remittance.email.title', default: 'Automatic Payment Rejections')
            mailService.sendMail {
                to user.email
                subject title
                body(view: '/emails/paymentRejections', model: [companyInstance: company, systemUserInstance: user, title: title, rejections: rejections])
            }
        }

        if (remittancesPaid) {
            if (printAdvices) {
                def lastBatch = 1
                if (batchSize && remittancesPaid > batchSize) {
                    def counter = batchSize
                    yield()
                    def workers = SystemWorkarea.findAllByProcess(pid, [sort: 'string1', offset: batchSize as Long])
                    SystemWorkarea.withTransaction {status ->
                        for (worker in workers) {
                            if (counter == batchSize) {
                                counter = 0
                                lastBatch++
                            }

                            counter++
                            worker.integer1 = lastBatch
                            worker.saveThis()
                            session.evict(worker)
                        }
                    }
                }

                def reportParams = [:]
                def countryNamesMap = [:]
                def countries = SupplierAddress.executeQuery('select distinct x.country from SupplierAddress as x where x.supplier.company = ? and x.country != ?', [company, company.country])
                for (country in countries) countryNamesMap.put(country.id.toString(), message(code: 'country.name.' + country.code, default: country.name))
                def remittanceAddressType = SystemSupplierAddressType.findByCode('remittance')
                def remittanceContactType = SystemSupplierContactType.findByCode('remittance')
                def title = message(code: 'remittance.report', default: 'Remittance Advices')
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
                reportParams.batches = lastBatch
                for (reportNum in 1..lastBatch) {
                    yield()
                    reportParams.batch = reportNum
                    def pdfFile = createReportPDF('Remittances', reportParams)
                    yield()
                    mailService.sendMail {
						multipart true
                        to user.email
                        subject "${title} (${reportNum}/${lastBatch})"
                        body(view: '/emails/genericReport', model: [companyInstance: company, systemUserInstance: user, title: title])
                        attach pdfFile
                    }

                    pdfFile.delete()
                }
            }

            yield()
            SystemWorkarea.withTransaction {status ->
                SystemWorkarea.executeUpdate('delete from SystemWorkarea where process = ?', [pid])
            }
        }

        results.paid = remittancesPaid
        results.rejected = rejections.size()
        results.posted = documentsCreated

        return valid
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private getRate(fromCurrency, toCurrency, map, date) {
        if (fromCurrency.code == toCurrency.code) return 1.0
        def rate = map.get(fromCurrency.code + toCurrency.code)
        if (!rate) {
            rate = utilService.getExchangeRate(fromCurrency, toCurrency, date)
            map.put(fromCurrency.code + toCurrency.code, rate)
        }

        return rate
    }

    private processPayment(documentType, documentCode, documentPeriod, documentCurrency, bankAccount, bankCurrency, bankRate, companyRate, remittanceIds,
                           remittances, apControlAccount, today, reference, description, rejections, customizationMap, pid) {
        def remittance, included, accountValue, bankValue, companyValue, document, line, objection, errorMessage, worker
        def accountTotal = 0.0
        def bankTotal = 0.0
        def companyTotal = 0.0
        def lineNumber = 0
        def rejectAll = false
        Remittance.withTransaction {status ->
            for (rid in remittanceIds) {
                included = true
                remittance = Remittance.lock(rid)

                // Make sure it's still available for payment
                if (remittance && remittance.authorizedDate && !remittance.paymentDate) {
                    accountValue = RemittanceLine.executeQuery('select sum(accountUnallocated) from RemittanceLine where remittance = ?', [remittance])[0]
                    if (accountValue < 0.0) {
                        accountValue = -accountValue
                        if (customizationMap) {
                            objection = bookService.paymentService.verifyPayment(customizationMap, remittance.supplier, documentType, bankAccount, documentCurrency, accountValue)
                            if (objection) {
                                reject(remittance, objection, rejections)
                                included = false
                            }
                        }
                    } else {
                        objection = message(code: 'remittance.invalid.total', args: [utilService.format(-accountValue, documentCurrency.decimals, true, locale)],
                                default: "Total is ${utilService.format(-accountValue, documentCurrency.decimals, true, locale)}")
                        reject(remittance, objection, rejections)
                        included = false
                    }
                } else {
                    included = false
                }

                if (included) {
                    bankValue = (bankRate == 1.0) ? accountValue : utilService.round(accountValue * bankRate, bankCurrency.decimals)
                    companyValue = (companyRate == 1.0) ? accountValue : utilService.round(accountValue * companyRate, currency.decimals)
                    accountTotal += accountValue
                    bankTotal += bankValue
                    companyTotal += companyValue
                    remittances << remittance
                    if (!document) {
                        document = new Document(type: documentType, currency: documentCurrency, period: documentPeriod, documentDate: today,
                                code: documentCode, description: description, reference: reference)
                    }

                    lineNumber++
                    line = new Line(supplier: remittance.supplier, documentValue: accountValue, accountValue: accountValue,
                            accountUnallocated: accountValue, generalValue: companyValue, companyValue: companyValue, companyUnallocated: companyValue,
                            description: documentType.code + documentCode + '/' + lineNumber.toString(), reconciled: today)
					line.account = apControlAccount
                    document.addToLines(line)
                    remittance.paymentDate = today
                    remittance.sourceLine = line    // We will need to know the general transaction line created from the remittance for allocation purposes
                    if (remittance.saveThis()) {
                        worker = new SystemWorkarea(process: pid, identifier: remittance.id, integer1: 1, string1: remittance.supplier.code)
                        if (!worker.saveThis()) {
                            errorMessage = message(code: 'generic.workarea', default: 'Unable to update the work table')
                            status.setRollbackOnly()
                            break
                        }
                    } else {
                        errorMessage = message(code: 'remittance.bad.payment', default: 'Unable to update the remittance advice payment date')
                        status.setRollbackOnly()
                        break
                    }
                }
            }

            if (document && !errorMessage) {
                line = new Total(description: description, documentValue: accountTotal, generalValue: bankTotal, companyValue: companyTotal)
				line.account = bankAccount
                document.addToTotal(line)
                if (customizationMap) {
                    errorMessage = bookService.paymentService.preProcessDocument(customizationMap, document)
                    if (errorMessage) {
                        rejectAll = true
                        status.setRollbackOnly()
                    }
                }

                if (!rejectAll) {
                    if (postingService.post(document, status)) {
                        if (customizationMap) {
                            errorMessage = bookService.paymentService.postProcessDocument(customizationMap, document, true)
                            if (errorMessage) {
                                rejectAll = true
                                status.setRollbackOnly()
                            }
                        }
                    } else {
                        if (customizationMap) bookService.paymentService.postProcessDocument(customizationMap, document, false)
                        errorMessage = message(code: 'recurring.bad.document', args: [bankAccount.code, documentType.code + documentCode],
                                default: "Bank account ${bankAccount.code}, reference ${documentType.code + documentCode}: Unable to post the document")
                        status.setRollbackOnly()
                    }
                }
            }
        }

        if (rejectAll) {
            yield()
            Remittance.withTransaction {
                for (ra in remittances) reject(ra, errorMessage, rejections)
            }

            // This is a programmatic cancellation of the document rather than an error, so clean up
            remittances.clear()
            errorMessage = null
            document = null
        }

        yield()
        return errorMessage ?: document
    }

    private reject(remittance, objection, rejections) {
        def supplier = remittance.supplier.code
        def created = utilService.format(remittance.adviceDate, 1, null, locale)
        def authorized = utilService.format(remittance.authorizedDate, 1, null, locale)
        def msg = message(code: 'remittance.rejection', args: [supplier, created, authorized, objection],
                default: "Remittance for supplier ${supplier} created on ${created} and authorised on ${authorized} rejected: ${objection}")
        rejections << msg
        remittance.refresh()
        remittance.delete(flush: true)
    }

    private processAllocation(remittance, document, session) {
        def sourceLine = remittance.sourceLine.refresh()
        def targetLine, sourceAllocation, targetAllocation
        for (remittanceLine in remittance.lines) {
            targetLine = GeneralTransaction.get(remittanceLine.sequencer)
            sourceAllocation = new Allocation(targetType: targetLine.document.type, period: document.period, targetCode: targetLine.document.code,
                    targetId: targetLine.id, accountValue: remittanceLine.accountUnallocated)
            targetAllocation = new Allocation(targetType: document.type, period: targetLine.document.period, targetCode: document.code,
                    targetId: sourceLine.id, accountValue: -remittanceLine.accountUnallocated)
            postingService.allocateLine(sourceLine, sourceAllocation, targetAllocation, company, currency)
            session.evict(targetLine)
            session.evict(sourceAllocation)
            session.evict(targetAllocation)
        }

        session.evict(sourceLine)
        yield()
    }
}
