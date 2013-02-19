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
import org.grails.tlc.books.*

public class ReconciliationTask extends TaskExecutable {

    def execute() {
        def statementDate = params.date
        if (!statementDate || statementDate > new Date() || statementDate != utilService.fixDate(statementDate)) {
            completionMessage = message(code: 'reconciliation.bad.date', default: 'Invalid Bank Statement Date')
            return false
        }

        def bankAccount = Account.get(params.account)
        if (!bankAccount || bankAccount.securityCode != company.securityCode || bankAccount.type.code != 'bank') {
            completionMessage = message(code: 'bank.not.exists', default: 'Invalid Bank account')
            return false
        }

        def bankCurrency = bankAccount.currency
        def statementBalance = params.balance
        if (statementBalance != utilService.round(statementBalance, bankCurrency.decimals)) {
            completionMessage = message(code: 'reconciliation.bad.balance', default: 'Invalid Bank Statement Balance')
            return false
        }

        def session = runSessionFactory.currentSession
        def bankAccountBalance = 0.0
        def document, reconciliationLine, reconciliationLineDetail, detailTotal, detailCount
        def reconciliation = new Reconciliation(bankAccount: bankAccount, statementDate: statementDate, statementBalance: statementBalance)
        def priorReconciliations = Reconciliation.findAllByBankAccount(bankAccount, [sort: 'statementDate', order: 'desc', max: 1])
        if (priorReconciliations) {
            def priorReconciliation = priorReconciliations[0]
            if (!priorReconciliation.finalizedDate) {
                completionMessage = message(code: 'reconciliation.prior.final', default: 'The previous bank statement has not been finalized and so no new reconciliation can be created')
                return false
            }

            if (priorReconciliation.statementDate >= statementDate) {
                completionMessage = message(code: 'reconciliation.prior.date', default: 'The Bank Statement Date must be after that of the previous reconciliation')
                return false
            }

            // Grab the previous bank account balance
            bankAccountBalance = priorReconciliation.bankAccountBalance

            // Grab any unreconciled items
            for (line in priorReconciliation.lines) {
                if (line.bankAccountValue != line.reconciledValue) {
                    reconciliationLine = new ReconciliationLine(documentId: line.documentId, documentDate: line.documentDate, documentCode: line.documentCode,
                            documentReference: line.documentReference, documentDescription: line.documentDescription, bankAccountValue: line.bankAccountValue - line.reconciledValue,
                            reconciledValue: 0.0, broughtForward: true, part: (line.reconciledValue != 0.0), sequencer: line.sequencer)
                    reconciliation.addToLines(reconciliationLine)
                    for (detail in line.details) {
                        if (!detail.reconciled) {
                            reconciliationLineDetail = new ReconciliationLineDetail(type: detail.type, ledgerId: detail.ledgerId,
                                    ledgerCode: detail.ledgerCode, description: detail.description, bankAccountValue: detail.bankAccountValue)
                            reconciliationLine.addToDetails(reconciliationLineDetail)
                            reconciliationLine.detailCount++
                        }

                        session.evict(detail)
                    }

                    session.evict(line)
                }
            }

            // Clean up
            session.evict(priorReconciliation)
            priorReconciliation = null
        }

        // Add in all new bank transactions on or before the statement date (note that dateCreated includes a time)
        def transactions = GeneralTransaction.executeQuery('from GeneralTransaction as x where x.reconciliationKey = ? and x.reconciled is null and (x.dateCreated < ? or x.document.documentDate <= ?)',
                ['B' + bankAccount.id.toString(), statementDate + 1, statementDate])
        for (tran in transactions) {
            document = tran.document
            bankAccountBalance += tran.generalValue
            reconciliationLine = new ReconciliationLine(documentId: document.id, documentDate: document.documentDate, documentCode: document.type.code + document.code,
                    documentReference: document.reference, documentDescription: tran.description, bankAccountValue: tran.generalValue, sequencer: tran.id)
            reconciliation.addToLines(reconciliationLine)
            if (tran instanceof doc.Total) {
                detailTotal = 0.0
                for (line in document.lines) {
                    reconciliationLineDetail = new ReconciliationLineDetail(description: line.description)
                    reconciliationLine.addToDetails(reconciliationLineDetail)
                    reconciliationLine.detailCount++

                    // Note what type of detail record this is
                    if (line.customer) {
                        reconciliationLineDetail.type = 'ar'
                        reconciliationLineDetail.ledgerId = line.customer.id
                        reconciliationLineDetail.ledgerCode = line.customer.code
                    } else if (line.supplier) {
                        reconciliationLineDetail.type = 'ap'
                        reconciliationLineDetail.ledgerId = line.supplier.id
                        reconciliationLineDetail.ledgerCode = line.supplier.code
                    } else {
                        reconciliationLineDetail.type = 'gl'
                        reconciliationLineDetail.ledgerId = line.balance.account.id
                        reconciliationLineDetail.ledgerCode = line.balance.account.code
                    }

                    // Get the line value in bank account currency
                    if (bankCurrency.id == document.currency.id) {
                        reconciliationLineDetail.bankAccountValue = -line.documentValue
                    } else if (bankCurrency.id == currency.id) {
                        reconciliationLineDetail.bankAccountValue = -line.companyValue
                    } else if (line.documentValue == -tran.documentValue) {
                        reconciliationLineDetail.bankAccountValue = tran.generalValue
                    } else if (line.customer && bankCurrency.id == line.customer.currency.id) {
                        reconciliationLineDetail.bankAccountValue = -line.accountValue
                    } else if (line.supplier && bankCurrency.id == line.supplier.currency.id) {
                        reconciliationLineDetail.bankAccountValue = -line.accountValue
                    } else if (!line.customer && !line.supplier && bankCurrency.id == line.balance.account.currency.id) {
                        reconciliationLineDetail.bankAccountValue = -line.generalValue
                    } else {
                        reconciliationLineDetail.bankAccountValue = utilService.round((tran.generalValue * line.documentValue) / tran.documentValue, bankCurrency.decimals)
                    }

                    // Keep a running total of the detail records
                    detailTotal += reconciliationLineDetail.bankAccountValue
                }

                // Since we might be handling a foreign currency (from the bank account's
                // point of view, we may have introduced a rounding error that needs correction
                if (detailTotal != tran.generalValue) {
                    def diff = tran.generalValue - detailTotal
                    def victimLine
                    for (line in reconciliationLine.details) if (!victimLine || line.bankAccountValue.abs() > victimLine.bankAccountValue.abs()) victimLine = line
                    victimLine.bankAccountValue += diff
                }
            }
        }

        // Store the bank balance in the reconciliation record
        reconciliation.bankAccountBalance = bankAccountBalance

        // Update the database with the new reconciliation data and update
        // the GL transaction records as being included in a reconciliation
        def valid = true
        Reconciliation.withTransaction {status ->
           if (reconciliation.save(flush: true)) {      // With deep validation
               for (tran in transactions) {
                   tran.reconciled = statementDate
                   if (!tran.saveThis()) {
                       completionMessage = message(code: 'reconciliation.gl.save', default: 'Unable to update the General Ledger transaction(s)')
                       status.setRollbackOnly()
                       valid = false
                       break
                   }
               }

               // Final integrity check
               if (valid && Reconciliation.countByBankAccountAndFinalizedDateIsNull(bankAccount) != 1) {
                   completionMessage = message(code: 'reconciliation.duplicate', default: 'Another user has created a new reconciliation whilst this one was being created')
                   status.setRollbackOnly()
                   valid = false
               }
           } else {
               completionMessage = message(code: 'reconciliation.bad.save', default: 'Unable to save the bank reconciliation')
               status.setRollbackOnly()
               valid = false
           }
        }

        return valid
    }
}