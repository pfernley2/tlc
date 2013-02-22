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
package org.grails.tlc.books

class ReconciliationLineDetailController {

    // Injected services
    def utilService
    def bookService

    // Security settings
    def activities = [default: 'bankrec']

    // List of actions with specific request types
    static allowedMethods = [reconcileDetail: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        def reconciliationLineInstance = utilService.source('reconciliation.edit')
        if (reconciliationLineInstance?.securityCode == utilService.currentCompany().securityCode &&
        bookService.hasAccountAccess(reconciliationLineInstance.reconciliation.bankAccount) &&
        !reconciliationLineInstance.reconciliation.finalizedDate) {
            def reconciliationInstance = reconciliationLineInstance.reconciliation
            params.max = utilService.max
            params.offset = utilService.offset
            def unrec = ReconciliationLine.executeQuery('select sum(bankAccountValue - reconciledValue) from ReconciliationLine where reconciliation = ?', [reconciliationInstance])[0] ?: 0.0
            def subtot = reconciliationInstance.statementBalance + unrec
            def diff = subtot - reconciliationInstance.bankAccountBalance
            def decimals = reconciliationInstance.bankAccount.currency.decimals
            def reconciliationLineDetailInstanceList = ReconciliationLineDetail.findAll('from ReconciliationLineDetail where line = ? order by type, ledgerCode, id', [reconciliationLineInstance])
            def reconciliationLineDetailInstanceTotal = ReconciliationLineDetail.countByLine(reconciliationLineInstance)

            // Set any detail line account name
            def account
            for (detail in reconciliationLineDetailInstanceList) {
                if (detail.type == 'ar') {
                    account = Customer.get(detail.ledgerId)
                } else if (detail.type == 'ap') {
                    account = Supplier.get(detail.ledgerId)
                } else {
                    account = Account.get(detail.ledgerId)
                }

                detail.ledgerName = account ? account.name : message(code: 'generic.not.applicable', default: 'n/a')
            }

            [reconciliationInstance: reconciliationInstance, reconciliationLineInstance: reconciliationLineInstance, reconciliationLineDetailInstanceList: reconciliationLineDetailInstanceList,
                        reconciliationLineDetailInstanceTotal: reconciliationLineDetailInstanceTotal, unreconciled: utilService.format(unrec, decimals),
                        subtotal: utilService.format(subtot, decimals), difference: utilService.format(diff, decimals), decimals: decimals, canFinalize: (diff == 0.0)]
        } else {
            flash.message = message(code: 'reconciliation.line.invalid', default: 'Line not found')
            redirect(controller: 'reconciliation', action: 'list')
        }
    }

    // Ajax call to set/clear the reconciled flag on a detail line
    def reconcileDetail() {
        def errMessage, reconciliationInstance
        def reconciliationLineInstance = utilService.reSource('reconciliation.edit')
        if (reconciliationLineInstance?.securityCode == utilService.currentCompany().securityCode &&
        bookService.hasAccountAccess(reconciliationLineInstance.reconciliation.bankAccount) &&
        !reconciliationLineInstance.reconciliation.finalizedDate) {
            def reconciliationLineDetailInstance = ReconciliationLineDetail.get(params.lineId)
            if (reconciliationLineDetailInstance?.line?.id == reconciliationLineInstance.id) {
                reconciliationInstance = reconciliationLineInstance.reconciliation
                def newState = (params.newState == 'true')
                if (reconciliationLineDetailInstance.reconciled != newState) {
                    reconciliationLineDetailInstance.reconciled = newState
                    if (newState) {
                        reconciliationLineInstance.reconciledValue += reconciliationLineDetailInstance.bankAccountValue
                    } else {
                        reconciliationLineInstance.reconciledValue -= reconciliationLineDetailInstance.bankAccountValue
                    }

                    ReconciliationLine.withTransaction {status ->
                        if (reconciliationLineInstance.saveThis()) {
                            if (!reconciliationLineDetailInstance.saveThis()) {
                                errMessage = message(code: 'reconciliationLineDetail.line.bad', default: 'Unable to update the detailed line')
                                status.setRollbackOnly()
                            }
                        } else {
                            errMessage = message(code: 'reconciliation.line.bad', default: 'Unable to update the line')
                            status.setRollbackOnly()
                        }
                    }
                }
            } else {
                errMessage = message(code: 'reconciliationLineDetail.line.invalid', default: 'Detailed line not found')
            }
        } else {
            errMessage = message(code: 'reconciliation.line.invalid', default: 'Line not found')
        }

        if (errMessage) {
            render(contentType: 'text/json') {
                errorMessage = errMessage
            }
        } else {
            def unrec = ReconciliationLine.executeQuery('select sum(bankAccountValue - reconciledValue) from ReconciliationLine where reconciliation = ?', [reconciliationInstance])[0] ?: 0.0
            def subtot = reconciliationInstance.statementBalance + unrec
            def diff = subtot - reconciliationInstance.bankAccountBalance
            def decimals = reconciliationInstance.bankAccount.currency.decimals
            render(contentType: 'text/json') {
                unreconciledValue = utilService.format(unrec, decimals)
                subtotalValue = utilService.format(subtot, decimals)
                differenceValue = utilService.format(diff, decimals)
                canFinalize = (diff == 0.0)
            }
        }
    }

    def display() {
        def reconciliationLineInstance = utilService.source('reconciliation.display')
        if (reconciliationLineInstance?.securityCode == utilService.currentCompany().securityCode &&
        bookService.hasAccountAccess(reconciliationLineInstance.reconciliation.bankAccount) &&
        reconciliationLineInstance.reconciliation.finalizedDate) {
            def reconciliationInstance = reconciliationLineInstance.reconciliation
            params.max = utilService.max
            params.offset = utilService.offset
            def unrec = ReconciliationLine.executeQuery('select sum(bankAccountValue - reconciledValue) from ReconciliationLine where reconciliation = ?', [reconciliationInstance])[0] ?: 0.0
            def subtot = reconciliationInstance.statementBalance + unrec
            def diff = subtot - reconciliationInstance.bankAccountBalance
            def decimals = reconciliationInstance.bankAccount.currency.decimals
            def reconciliationLineDetailInstanceList = ReconciliationLineDetail.findAll('from ReconciliationLineDetail where line = ? order by type, ledgerCode, id', [reconciliationLineInstance])
            def reconciliationLineDetailInstanceTotal = ReconciliationLineDetail.countByLine(reconciliationLineInstance)

            // Set any detail line account name
            def account
            for (detail in reconciliationLineDetailInstanceList) {
                if (detail.type == 'ar') {
                    account = Customer.get(detail.ledgerId)
                } else if (detail.type == 'ap') {
                    account = Supplier.get(detail.ledgerId)
                } else {
                    account = Account.get(detail.ledgerId)
                }

                detail.ledgerName = account ? account.name : message(code: 'generic.not.applicable', default: 'n/a')
            }

            [reconciliationInstance: reconciliationInstance, reconciliationLineInstance: reconciliationLineInstance, reconciliationLineDetailInstanceList: reconciliationLineDetailInstanceList,
                        reconciliationLineDetailInstanceTotal: reconciliationLineDetailInstanceTotal, unreconciled: utilService.format(unrec, decimals),
                        subtotal: utilService.format(subtot, decimals), difference: utilService.format(diff, decimals), decimals: decimals]
        } else {
            flash.message = message(code: 'reconciliation.line.invalid', default: 'Line not found')
            redirect(controller: 'reconciliation', action: 'list')
        }
    }
}
