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

import org.grails.tlc.sys.TaskExecutable
import doc.Line
import org.apache.commons.collections.set.ListOrderedSet
import org.grails.tlc.books.*

public class RevaluationTask extends TaskExecutable {
    def execute() {
        def activePeriods = bookService.getActivePeriods(company)
        def period = Period.get(params.periodId)
        if (!period || period.securityCode != company.securityCode || !activePeriods.find {it.code == period.code}) {
            completionMessage = utilService.standardMessage('not.found', 'period', params.periodId)
            return false
        }

        def reversalPeriod, reversalDocument
        for (int i = 0; i < activePeriods.size(); i++) {
            if (activePeriods[i].id == period.id) {
                if (i == activePeriods.size() - 1) {
                    completionMessage = message(code: 'document.next.period', args: [period.code], default: "No active period found after ${period.code} to which the reversal could be posted")
                    return false
                } else {
                    reversalPeriod = activePeriods[i + 1]
                }

                break
            }
        }

        def adjustment = (params.adjustment != null) ? params.adjustment : true
        if (!adjustment && period.status == 'adjust') {
            completionMessage = message(code: 'document.adjust.period', args: [period.code],
                    default: "Period ${period.code} is an adjustment period and you may only post this document to it if you set the Adjustment flag")
            return false
        }

        def fxControl = Account.get(params.targetId)
        if (!fxControl || fxControl.securityCode != company.securityCode || !['fxDiff', 'fxRevalue'].contains(fxControl.type?.code)) {
            completionMessage = utilService.standardMessage('not.found', 'account', params.targetId)
            return false
        }

        def temp = 'ar'
        def arControl = bookService.getControlAccount(company, temp)
        if (!arControl) {
            completionMessage = message(code: 'document.no.control', args: [temp], default: "Could not find the ${temp} control account in the General Ledger")
            return false
        }

        temp = 'ap'
        def apControl = bookService.getControlAccount(company, temp)
        if (!apControl) {
            completionMessage = message(code: 'document.no.control', args: [temp], default: "Could not find the ${temp} control account in the General Ledger")
            return false
        }

        temp = 'arRevalue'
        def arRevalue = bookService.getControlAccount(company, temp)
        if (!arRevalue) {
            completionMessage = message(code: 'document.no.control', args: [temp], default: "Could not find the ${temp} control account in the General Ledger")
            return false
        }

        temp = 'apRevalue'
        def apRevalue = bookService.getControlAccount(company, temp)
        if (!apRevalue) {
            completionMessage = message(code: 'document.no.control', args: [temp], default: "Could not find the ${temp} control account in the General Ledger")
            return false
        }

        def arParams, arSQL, apParams, apSQL
        def excludedPeriods = activePeriods.findAll {it.validFrom > period.validTo}
        if (excludedPeriods) {
            arParams = ['dummy']
            arSQL = 'select sum(accountValue), sum(companyValue) from GeneralTransaction where customer = ? and balance in ('
            apParams = ['dummy']
            apSQL = 'select sum(accountValue), sum(companyValue) from GeneralTransaction where supplier = ? and balance in ('
            for (int i = 0; i < excludedPeriods.size(); i++) {
                if (i > 0) {
                    arSQL += ', ?'
                    apSQL += ', ?'
                } else {
                    arSQL += '?'
                    apSQL += '?'
                }

                arParams << GeneralBalance.findByAccountAndPeriod(arControl, excludedPeriods[i])
                apParams << GeneralBalance.findByAccountAndPeriod(apControl, excludedPeriods[i])
            }

            arSQL += ')'
            apSQL += ')'
        }

        def docType = postingService.getAutoDocumentOfType('FXR', company)
        if (!docType) {
            completionMessage = message(code: 'document.auto.type', args: ['FXR'], default: 'Could not find a company document type with a system type of FXR that allows auto-generation')
            return false
        }

        def revaluations = []
        def lock = bookService.getCompanyLock(company)
        def balance, converted, difference, account, exclusions, accountBalance, companyBalance, previousRevaluations
        def count = 0
        def totals = new TreeMap() // A sorted map to accumulate our totals in to
        def controls = [:]
        controls.put(arRevalue.code, arRevalue)
        controls.put(apRevalue.code, apRevalue)
        def session = runSessionFactory.currentSession
        yield()

        def accounts = Account.executeQuery('select id from Account where securityCode = ? and revaluationMethod = ? order by code', [company.securityCode, 'standard'])
        for (id in accounts) {
            yield()
            account = Account.get(id)
            previousRevaluations = PeriodRevaluation.executeQuery('select sum(currentRevaluation) from PeriodRevaluation where period = ? and account = ?', [period, account])[0] ?: 0.0
            balance = GeneralBalance.findByAccountAndPeriod(account, period)
            converted = utilService.convertCurrency(account.currency, currency, balance.generalClosingBalance, period.validTo)
            if (converted == null) {
                completionMessage = message(code: 'document.bad.exchangeRate', args: [account.currency.code, currency.code],
                        default: "No exchange rate available from ${account.currency.code} to ${currency.code}")
                return false
            }

            difference = converted - (balance.companyClosingBalance + previousRevaluations)
            if (difference != 0.0) {
                revaluations << new PeriodRevaluation(period: period, revaluationAccount: account.revaluationAccount, account: account, currentBalance: balance.companyClosingBalance,
                        priorRevaluations: previousRevaluations, currentRevaluation: difference, revaluedBalance: converted)
                count++
                accumulate(totals, account.revaluationAccount.code, difference)
                if (!controls.containsKey(account.revaluationAccount.code)) controls.put(account.revaluationAccount.code, account.revaluationAccount)
            }

            session.evict(account)
            session.evict(balance)
        }

        accounts = Customer.executeQuery('select id from Customer where company = ? and revaluationMethod = ? order by code', [company, 'standard'])
        for (id in accounts) {
            yield()
            if (arSQL) {
                lock.lock()
                try {
                    account = Customer.get(id)
                    arParams[0] = account
                    exclusions = GeneralTransaction.executeQuery(arSQL, arParams)[0]
                } finally {
                    lock.unlock()
                }

                accountBalance = account.accountCurrentBalance
                if (exclusions[0]) accountBalance -= exclusions[0]
                companyBalance = account.companyCurrentBalance
                if (exclusions[1]) companyBalance -= exclusions[1]
            } else {
                account = Customer.get(id)
                accountBalance = account.accountCurrentBalance
                companyBalance = account.companyCurrentBalance
            }

            previousRevaluations = PeriodRevaluation.executeQuery('select sum(currentRevaluation) from PeriodRevaluation where period = ? and customer = ?', [period, account])[0] ?: 0.0
            converted = utilService.convertCurrency(account.currency, currency, accountBalance, period.validTo)
            if (converted == null) {
                completionMessage = message(code: 'document.bad.exchangeRate', args: [account.currency.code, currency.code],
                        default: "No exchange rate available from ${account.currency.code} to ${currency.code}")
                return false
            }

            difference = converted - (companyBalance + previousRevaluations)
            if (difference != 0.0) {
                revaluations << new PeriodRevaluation(period: period, revaluationAccount: arRevalue, customer: account, currentBalance: companyBalance,
                        priorRevaluations: previousRevaluations, currentRevaluation: difference, revaluedBalance: converted)
                count++
                accumulate(totals, arRevalue.code, difference)
            }

            session.evict(account)
        }

        accounts = Supplier.executeQuery('select id from Supplier where company = ? and revaluationMethod = ? order by code', [company, 'standard'])
        for (id in accounts) {
            yield()
            if (apSQL) {
                lock.lock()
                try {
                    account = Supplier.get(id)
                    apParams[0] = account
                    exclusions = GeneralTransaction.executeQuery(apSQL, apParams)[0]
                } finally {
                    lock.unlock()
                }

                accountBalance = -account.accountCurrentBalance
                if (exclusions[0]) accountBalance -= exclusions[0]
                companyBalance = -account.companyCurrentBalance
                if (exclusions[1]) companyBalance -= exclusions[1]
            } else {
                account = Supplier.get(id)
                accountBalance = -account.accountCurrentBalance
                companyBalance = -account.companyCurrentBalance
            }

            previousRevaluations = PeriodRevaluation.executeQuery('select sum(currentRevaluation) from PeriodRevaluation where period = ? and supplier = ?', [period, account])[0] ?: 0.0
            converted = utilService.convertCurrency(account.currency, currency, accountBalance, period.validTo)
            if (converted == null) {
                completionMessage = message(code: 'document.bad.exchangeRate', args: [account.currency.code, currency.code],
                        default: "No exchange rate available from ${account.currency.code} to ${currency.code}")
                return false
            }

            difference = converted - (companyBalance + previousRevaluations)
            if (difference != 0.0) {
                revaluations << new PeriodRevaluation(period: period, revaluationAccount: apRevalue, supplier: account, currentBalance: companyBalance,
                        priorRevaluations: previousRevaluations, currentRevaluation: difference, revaluedBalance: converted)
                count++
                accumulate(totals, apRevalue.code, difference)
            }

            session.evict(account)
        }

        if (count) {
            def docNum = postingService.getNextDocumentNumber(docType, 2)
            if (!docNum) {
                completionMessage = message(code: 'document.bad.next', default: 'Unable to update the next document sequence number')
                return false
            }

            def revNum = docNum + 1
            def now = utilService.fixDate()

            // Create the revaluation document
            def description = message(code: 'document.revaluation.fx', args: [fxControl.type.code], default: "FX revaluation to the ${fxControl.type.code} account")
            if (description.length() > 50) description = description.substring(0, 50)
            def reference = message(code: 'document.revaluation.difference', default: 'FX Revaluation')
            if (reference.length() > 30) reference = reference.substring(0, 30)
            def document = new Document(currency: currency, type: docType, period: period, code: docNum.toString(), description: description, documentDate: now, dueDate: now, reference: reference)
            document.lines = new ListOrderedSet()

            // Create the revaluation reversal document
            description = message(code: 'document.revaluation.fx.reversal', args: [fxControl.type.code], default: "FX reversal to the ${fxControl.type.code} account")
            if (description.length() > 50) description = description.substring(0, 50)
            reference = message(code: 'document.revaluation.diff.reversal', default: 'FX Reversal')
            if (reference.length() > 30) reference = reference.substring(0, 30)
            reversalDocument = new Document(currency: currency, type: docType, period: reversalPeriod, code: revNum.toString(), description: description,
                    documentDate: now, dueDate: now, reference: reference)
            reversalDocument.lines = new ListOrderedSet()

            // Add in the revaluation lines
            def line, reversalLine, control
            def grandTotal = 0.0
            description = message(code: 'document.revaluation.gl', default: 'GL account revaluations')
            if (description.length() > 50) description = description.substring(0, 50)
            def reverseDescription = message(code: 'document.revaluation.gl.reverse', default: 'Reversal of GL account revaluations')
            if (reverseDescription.length() > 50) reverseDescription = reverseDescription.substring(0, 50)
            for (tot in totals) {
                control = controls.get(tot.key)
                line = new Line(documentValue: tot.value, generalValue: tot.value, companyValue: tot.value, adjustment: adjustment)
				line.account = control
                reversalLine = new Line(documentValue: -tot.value, generalValue: -tot.value, companyValue: -tot.value, adjustment: adjustment)
				reversalLine.account = control
                if (control.is(arRevalue)) {
                    temp = message(code: 'document.revaluation.ar', default: 'AR account revaluations')
                    if (temp.length() > 50) temp = temp.substring(0, 50)
                    line.description = temp
                    temp = message(code: 'document.revaluation.ar.reverse', default: 'Reversal of AR account revaluations')
                    if (temp.length() > 50) temp = temp.substring(0, 50)
                    reversalLine.description = temp
                } else if (control.is(apRevalue)) {
                    temp = message(code: 'document.revaluation.ap', default: 'AP account revaluations')
                    if (temp.length() > 50) temp = temp.substring(0, 50)
                    line.description = temp
                    temp = message(code: 'document.revaluation.ap.reverse', default: 'Reversal of AP account revaluations')
                    if (temp.length() > 50) temp = temp.substring(0, 50)
                    reversalLine.description = temp
                } else {
                    line.description = description
                    reversalLine.description = reverseDescription
                }

                document.addToLines(line)
                reversalDocument.addToLines(reversalLine)
                grandTotal += tot.value
            }

            // Add in the total lines
            description = message(code: 'document.revaluation.difference', default: 'FX Revaluation')
            if (description.length() > 50) description = description.substring(0, 50)
            line = new Line(description: description, documentValue: -grandTotal, generalValue: -grandTotal, companyValue: -grandTotal, adjustment: adjustment)
			line.account = fxControl
            document.addToLines(line)
            description = message(code: 'document.revaluation.diff.reversal', default: 'FX Reversal')
            if (description.length() > 50) description = description.substring(0, 50)
            reversalLine = new Line(description: description, documentValue: grandTotal, generalValue: grandTotal, companyValue: grandTotal, adjustment: adjustment)
			reversalLine.account = fxControl
            reversalDocument.addToLines(reversalLine)

            // Perform the updates
            def valid = true
            Document.withTransaction {status ->
                valid = postingService.post(document, status)
                if (valid) {
                    valid = postingService.post(reversalDocument, status)
                    if (valid) {
                        for (reval in revaluations) {
                            reval.document = document
                            if (!reval.saveThis()) {
                                completionMessage = message(code: 'document.revaluation.data', default: 'Unable to save the revaluation data')
                                status.setRollbackOnly()
                                valid = false
                                break
                            }
                        }
                    } else {
                        completionMessage = utilService.getFirstErrorMessage(reversalDocument) ?: message(code: 'document.reversal.error', default: 'Error posting the reversal')
                        status.setRollbackOnly()
                    }
                } else {
                    completionMessage = utilService.getFirstErrorMessage(document) ?: message(code: 'document.invalid', default: 'Invalid document')
                    status.setRollbackOnly()
                }
            }

            if (valid) {
                def title = message(code: 'revaluation.report.title', args: [document.type.code, document.code], default: document.type.code + document.code + ' Revaluation Breakdown')
                results.document = document.type.code + document.code
                results.lineCount = count
                results.diffValue = currency.code + ' ' + makeText(grandTotal, currency.decimals, message(code: 'generic.dr', default: 'Dr'), message(code: 'generic.cr', default: 'Cr'))
                def reportParams = [:]
                reportParams.put('reportTitle', title)
                reportParams.put('documentId', document.id)
                reportParams.put('txtAccount', message(code: 'document.line.accountCode.label', default: 'Account'))
                reportParams.put('txtName', message(code: 'account.name.label', default: 'Name'))
                reportParams.put('txtCurrent', message(code: 'revaluation.report.current', default: 'Account Balance'))
                reportParams.put('txtPrior', message(code: 'revaluation.report.prior', default: 'Prior Revaluations'))
                reportParams.put('txtRevalued', message(code: 'revaluation.report.revalued', default: 'Revalued Balance'))
                reportParams.put('txtAdjustment', message(code: 'revaluation.report.adjustment', default: 'This Revaluation'))
                reportParams.put('txtDifference', fxControl.code + ' ' + fxControl.name)

                yield()
                def pdfFile = createReportPDF('RevaluationBreakdown', reportParams, true)
                yield()
                mailService.sendMail {
					multipart true
                    to user.email
                    subject title
                    body(view: '/emails/genericReport', model: [companyInstance: company, systemUserInstance: user, title: title])
                    attach pdfFile
                }

                yield()
                pdfFile.delete()
            } else {
                return false
            }
        } else {
            completionMessage = message(code: 'revaluation.none', default: 'No revaluation required')
        }

        return true
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private makeText(value, decimals, dr, cr) {
        if (value > 0.0) return utilService.format(value, decimals, null, locale) + ' ' + dr
        if (value < 0.0) return utilService.format(-value, decimals, null, locale) + ' ' + cr
        return utilService.format(value, decimals, null, locale)
    }

    private accumulate(map, key, value) {
        map.put(key, (map.get(key) ?: 0.0) + value)
    }
}