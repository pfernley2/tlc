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

import org.grails.tlc.books.Document
import org.grails.tlc.books.DocumentType
import org.grails.tlc.books.Recurring
import org.grails.tlc.sys.CacheService
import org.grails.tlc.sys.TaskExecutable
import doc.Line
import doc.Total

class BankRecurringTask extends TaskExecutable {

    def execute() {
        if (company.systemOnly) return true     // Don't do this for the System company
        def good = 0
        def bad = 0
        def today = utilService.fixDate()
        def failures = []
        def periods = bookService.getOpenPeriods(company)
        if (!periods) {
            completionMessage = message(code: 'recurring.periods', default: 'No open periods found')
            failures << completionMessage
            sendEmail(failures)
            return false
        }

        def arControl = bookService.getControlAccount(company, 'ar')
        if (!arControl) {
            completionMessage = message(code: 'document.no.control', args: ['ar'], default: 'Could not find the ar control account in the General Ledger')
            failures << completionMessage
            sendEmail(failures)
            return false
        }

        def apControl = bookService.getControlAccount(company, 'ap')
        if (!apControl) {
            completionMessage = message(code: 'document.no.control', args: ['ap'], default: 'Could not find the ap control account in the General Ledger')
            failures << completionMessage
            sendEmail(failures)
            return false
        }

        def code, type, document, docLine, valid, nextDue, useInitial, useFinal, documentTotal, companyTotal, bankTotal
        def lineDocumentValue, lineCompanyValue, lineCompanyUnallocated, lineGeneralValue, lineAccountValue, lineBankValue, lineDocumentTotals
        def allocations = []
        def rates = [:]
        def calendar = Calendar.getInstance()
        def definitions = Recurring.findAllBySecurityCodeAndNextDueLessThanEquals(company.securityCode, today)
        for (recurring in definitions) {
            yield()
            if (recurring.account.type?.code != 'bank') {
                failures << message(code: 'recurring.not.bank', args: [recurring.account.code, recurring.reference],
                        default: "Account ${recurring.account.code} is not a bank account (Reference = ${recurring.reference})")
                bad++
            } else if (!recurring.account.active) {
                failures << message(code: 'recurring.not.active', args: [recurring.account.code, recurring.reference],
                        default: "Bank account ${recurring.account.code} is not active (Reference = ${recurring.reference})")
                bad++
            } else if (!recurring.type.autoGenerate) {
                failures << message(code: 'recurring.no.code', args: [recurring.account.code, recurring.reference, recurring.type.code],
                        default: "Bank account ${recurring.account.code}, reference ${recurring.reference}: Document type ${recurring.type.code} does not allow auto-generation of sequence numbers")
                bad++
            } else {
                while (recurring.nextDue && recurring.nextDue <= today) {
                    code = null
                    DocumentType.withTransaction {status ->
                        recurring.type.discard()
                        type = DocumentType.lock(recurring.type.id)
                        code = type.nextSequenceNumber.toString()
                        type.nextSequenceNumber += 1
                        if (!type.saveThis()) {
                            status.setRollbackOnly()
                            code = null
                        }
                    }

                    if (code) {
                        allocations.clear()
                        useInitial = false
                        useFinal = false
                        if (!recurring.processedCount) {
                            if (recurring.initialDate) {
                                nextDue = recurring.recursFrom
                            } else {
                                nextDue = incrementDate(calendar, recurring.recursFrom, recurring.recurrenceType, recurring.recurrenceInterval, recurring.lastDayOfMonth, 1)
                            }

                            if (recurring.initialValue) {
                                useInitial = true
                                documentTotal = recurring.initialValue
                            } else {
                                documentTotal = recurring.recurringValue
                            }
                        } else if (recurring.processedCount + 1 == recurring.totalTransactions) {
                            nextDue = null
                            if (recurring.finalValue) {
                                useFinal = true
                                documentTotal = recurring.finalValue
                            } else {
                                documentTotal = recurring.recurringValue
                            }
                        } else {
                            nextDue = incrementDate(calendar, recurring.recursFrom, recurring.recurrenceType, recurring.recurrenceInterval, recurring.lastDayOfMonth,
                                    recurring.processedCount + (recurring.initialDate ? 0 : 1))
                            documentTotal = recurring.recurringValue
                        }

                        document = new Document(type: recurring.type, currency: recurring.currency, period: bookService.selectPeriod(periods, recurring.nextDue), documentDate: recurring.nextDue,
                                code: code, description: recurring.description, reference: "${recurring.reference}/${recurring.processedCount + 1}")

                        valid = true
                        lineDocumentTotals = 0.0
                        companyTotal = 0.0
                        bankTotal = 0.0
                        for (line in recurring.lines) {
                            if (useInitial) {
                                lineDocumentValue = line.initialValue
                            } else if (useFinal) {
                                lineDocumentValue = line.finalValue
                            } else {
                                lineDocumentValue = line.recurringValue
                            }

                            if (!lineDocumentValue) continue

                            lineCompanyValue = convert(rates, today, recurring.currency, currency, lineDocumentValue)
                            if (lineCompanyValue == null) {
                                failures << message(code: 'recurring.no.rate', args: [recurring.account.code, recurring.reference, recurring.currency.code, currency.code],
                                        default: "Bank account ${recurring.account.code}, reference ${recurring.reference}: No exchange rate from ${recurring.currency.code} to ${currency.code} available")
                                valid = false
                                break
                            }

                            lineBankValue = convert(rates, today, recurring.currency, recurring.account.currency, lineDocumentValue)
                            if (lineBankValue == null) {
                                failures << message(code: 'recurring.no.rate', args: [recurring.account.code, recurring.reference, recurring.currency.code, recurring.account.currency.code],
                                        default: "Bank account ${recurring.account.code}, reference ${recurring.reference}: No exchange rate from ${recurring.currency.code} to ${recurring.account.currency.code} available")
                                valid = false
                                break
                            }

                            lineDocumentTotals += lineDocumentValue
                            companyTotal += lineCompanyValue
                            bankTotal += lineBankValue

                            docLine = new Line()
                            if (line.customer) {
                                docLine.customer = line.customer
                                docLine.account = arControl
                                if (recurring.autoAllocate) allocations << line.customer
                                lineGeneralValue = lineCompanyValue
                                lineCompanyUnallocated = lineCompanyValue
                                lineAccountValue = convert(rates, today, recurring.currency, line.customer.currency, lineDocumentValue)
                                if (lineAccountValue == null) {
                                    failures << message(code: 'recurring.no.rate', args: [recurring.account.code, recurring.reference, recurring.currency.code, line.customer.currency.code],
                                            default: "Bank account ${recurring.account.code}, reference ${recurring.reference}: No exchange rate from ${recurring.currency.code} to ${line.customer.currency.code} available")
                                    valid = false
                                    break
                                }
                            } else if (line.supplier) {
                                docLine.supplier = line.supplier
                                docLine.account = apControl
                                if (recurring.autoAllocate) allocations << line.supplier
                                lineGeneralValue = lineCompanyValue
                                lineCompanyUnallocated = lineCompanyValue
                                lineAccountValue = convert(rates, today, recurring.currency, line.supplier.currency, lineDocumentValue)
                                if (lineAccountValue == null) {
                                    failures << message(code: 'recurring.no.rate', args: [recurring.account.code, recurring.reference, recurring.currency.code, line.supplier.currency.code],
                                            default: "Bank account ${recurring.account.code}, reference ${recurring.reference}: No exchange rate from ${recurring.currency.code} to ${line.supplier.currency.code} available")
                                    valid = false
                                    break
                                }
                            } else {
                                docLine.account = line.account
                                lineAccountValue = null
                                lineCompanyUnallocated = null
                                lineGeneralValue = convert(rates, today, recurring.currency, line.account.currency, lineDocumentValue)
                                if (lineGeneralValue == null) {
                                    failures << message(code: 'recurring.no.rate', args: [recurring.account.code, recurring.reference, recurring.currency.code, line.account.currency.code],
                                            default: "Bank account ${recurring.account.code}, reference ${recurring.reference}: No exchange rate from ${recurring.currency.code} to ${line.account.currency.code} available")
                                    valid = false
                                    break
                                }
                            }

                            docLine.documentValue = lineDocumentValue
                            docLine.accountValue = lineAccountValue
                            docLine.accountUnallocated = lineAccountValue
                            docLine.generalValue = lineGeneralValue
                            docLine.companyValue = lineCompanyValue
                            docLine.companyUnallocated = lineCompanyUnallocated
                            docLine.description = line.description
                            document.addToLines(docLine)
                        }

                        if (valid) {
                            if (lineDocumentTotals == documentTotal) {
                                docLine = new Total(description: recurring.description, documentValue: documentTotal, generalValue: bankTotal, companyValue: companyTotal)
								docLine.account = recurring.account
                                document.addToTotal(docLine)
                            } else {
                                failures << message(code: 'recurring.bad.total', args: [recurring.account.code, recurring.reference],
                                        default: "Bank account ${recurring.account.code}, reference ${recurring.reference}: The line values do not add up to the document total")
                                valid = false
                            }
                        }

                        if (valid) {
                            yield()
                            Recurring.withTransaction {status ->
                                if (postingService.post(document, status)) {
                                    recurring.processedCount++
                                    recurring.nextDue = nextDue
                                    if (!recurring.saveThis()) {
                                        failures << message(code: 'recurring.bad.save', args: [recurring.account.code, recurring.reference],
                                                default: "Bank account ${recurring.account.code}, reference ${recurring.reference}: Unable to update the recurring transaction definition")
                                        status.setRollbackOnly()
                                        valid = false
                                    }
                                } else {
                                    failures << message(code: 'recurring.bad.document', args: [recurring.account.code, recurring.reference],
                                            default: "Bank account ${recurring.account.code}, reference ${recurring.reference}: Unable to post the document")
                                    status.setRollbackOnly()
                                    valid = false
                                }
                            }

                            if (valid) {
                                for (alloc in allocations) postingService.autoAllocate(alloc, company, currency)
                                good++
                            } else {
                                bad++
                                break
                            }
                        } else {
                            bad++
                            break
                        }
                    } else {
                        failures << message(code: 'recurring.bad.code', args: [recurring.account.code, recurring.reference],
                                default: "Bank account ${recurring.account.code}, reference ${recurring.reference}: Unable to update next sequence number")
                        bad++
                        break
                    }
                }
            }
        }

        if (failures) sendEmail(failures)

        results.success = good
        results.fail = bad
        return true
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private incrementDate(calendar, from, type, interval, lastDayOfMonth, steps) {
        calendar.setTime(from)
        switch (type) {
            case 'daily':
                calendar.add(Calendar.DAY_OF_MONTH, interval * steps)
                break

            case 'weekly':
                calendar.add(Calendar.DAY_OF_MONTH, interval * steps * 7)
                break

            default:
                calendar.add(Calendar.MONTH, interval * steps)
                if (lastDayOfMonth) {
                    calendar.set(Calendar.DAY_OF_MONTH, 1)
                    calendar.add(Calendar.MONTH, 1)
                    calendar.add(Calendar.DAY_OF_MONTH, -1)
                }
                break
        }

        return calendar.getTime()
    }

    private convert(rates, today, fromCurrency, toCurrency, value) {
        if (!value || fromCurrency.id == toCurrency.id) return value
        def rate = rates.get(fromCurrency.code + toCurrency.code)
        if (rate == null) {
            rate = utilService.getExchangeRate(fromCurrency, toCurrency, today)
            if (rate) {
                rates.put(fromCurrency.code + toCurrency.code, rate)
                rates.put(toCurrency.code + fromCurrency.code, utilService.round(1.0 / rate, 6))
            } else {
                rate = CacheService.IMPOSSIBLE_VALUE
                rates.put(fromCurrency.code + toCurrency.code, rate)
                rates.put(toCurrency.code + fromCurrency.code, rate)
            }
        }

        return (rate == CacheService.IMPOSSIBLE_VALUE) ? null : utilService.round(value * rate, toCurrency.decimals)
    }

    private sendEmail(failures) {
        yield()
        def title = message(code: 'recurring.email.title', default: 'Recurring bank transaction error report')
        mailService.sendMail {
            to user.email
            subject title
            body(view: '/emails/recurringErrors', model: [companyInstance: company, systemUserInstance: user, title: title, failures: failures])
        }
    }
}
