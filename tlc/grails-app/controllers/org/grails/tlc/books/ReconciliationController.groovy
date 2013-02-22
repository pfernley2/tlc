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

class ReconciliationController {

    // Injected services
    def utilService
    def bookService

    // Security settings
    def activities = [default: 'bankrec']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', reconcileLine: 'POST', adding: 'POST', removing: 'POST', finalization: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        def reconciliationInstanceList, reconciliationInstanceTotal, bankAccount, allFinalized
        def bankAccountList = []
        def banks = Account.findAll("from Account as x where x.securityCode = ? and x.type.code = 'bank' order by x.name", [utilService.currentCompany().securityCode])
        for (bank in banks) {
            if (bookService.hasAccountAccess(bank)) {
                bankAccountList << bank
                if (bank.id.toString() == params.bankAccount) bankAccount = bank
            }
        }

        if (!bankAccount && bankAccountList.size() == 1) bankAccount = bankAccountList[0]

        if (bankAccount) {
            params.max = utilService.max
            params.offset = params.changed ? 0 : utilService.offset
            if (!['statementDate', 'statementBalance', 'bankAccountBalance', 'finalizedDate'].contains(params.sort)) {
                params.sort = 'statementDate'
                params.order = 'desc'
            }

            reconciliationInstanceList = Reconciliation.findAllByBankAccount(bankAccount, [sort: params.sort, order: params.order, max: params.max, offset: params.offset])
            reconciliationInstanceTotal = Reconciliation.countByBankAccount(bankAccount)
            allFinalized = (Reconciliation.countByBankAccountAndFinalizedDateIsNull(bankAccount) == 0)
        } else {
            bankAccount = new Account()
            reconciliationInstanceList = []
            reconciliationInstanceTotal = 0
            allFinalized = false
        }

        [bankAccountList: bankAccountList, bankAccount: bankAccount, allFinalized: allFinalized,
                    reconciliationInstanceList: reconciliationInstanceList, reconciliationInstanceTotal: reconciliationInstanceTotal]
    }

    def show() {
        def reconciliationInstance = Reconciliation.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!reconciliationInstance || !bookService.hasAccountAccess(reconciliationInstance.bankAccount)) {
            flash.message = utilService.standardMessage('not.found', 'reconciliation', params.id)
            redirect(action: 'list')
        } else {
            return [reconciliationInstance: reconciliationInstance]
        }
    }

    def delete() {
        def reconciliationInstance = Reconciliation.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (reconciliationInstance && bookService.hasAccountAccess(reconciliationInstance.bankAccount) && !reconciliationInstance.finalizedDate) {
            try {
                Reconciliation.withTransaction {status ->
                    reconciliationInstance.delete(flush: true)
                    GeneralTransaction.executeUpdate('update GeneralTransaction set reconciled = null where reconciliationKey = ? and reconciled = ?',
                            ['B' + reconciliationInstance.bankAccount.id.toString(), reconciliationInstance.statementDate])
                }

                flash.message = utilService.standardMessage('deleted', reconciliationInstance)
                redirect(action: 'list', params: [bankAccount: reconciliationInstance.bankAccount.id])
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', reconciliationInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'reconciliation', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        if (params.action == 'index') params.action = 'edit'
        def reconciliationInstance = Reconciliation.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!reconciliationInstance || !bookService.hasAccountAccess(reconciliationInstance.bankAccount) || reconciliationInstance.finalizedDate) {
            flash.message = utilService.standardMessage('not.found', 'reconciliation', params.id)
            redirect(action: 'list')
            return
        }

        params.max = utilService.max
        params.offset = utilService.offset
        def reconciliationLineInstanceList = ReconciliationLine.findAll('from ReconciliationLine where reconciliation = ? order by documentDate, documentCode, sequencer',
                [reconciliationInstance], [max: params.max, offset: params.offset])
        def reconciliationLineInstanceTotal = ReconciliationLine.countByReconciliation(reconciliationInstance)
        def hasAdded = (ReconciliationLine.countByReconciliationAndAdded(reconciliationInstance, true) > 0)
        def unrec = ReconciliationLine.executeQuery('select sum(bankAccountValue - reconciledValue) from ReconciliationLine where reconciliation = ?', [reconciliationInstance])[0] ?: 0.0
        def subtot = reconciliationInstance.statementBalance + unrec
        def diff = subtot - reconciliationInstance.bankAccountBalance
        def decimals = reconciliationInstance.bankAccount.currency.decimals

        // Set any single detail line description
        def account
        for (line in reconciliationLineInstanceList) {
            if (line.detailCount == 1) {
                for (detail in line.details) {
                    if (detail.type == 'ar') {
                        account = Customer.get(detail.ledgerId)
                    } else if (detail.type == 'ap') {
                        account = Supplier.get(detail.ledgerId)
                    } else {
                        account = Account.get(detail.ledgerId)
                    }

                    line.detailDescription = account ? account.name : detail.ledgerCode
                    if (detail.description) line.detailDescription = line.detailDescription + ' (' + detail.description + ')'
                }
            }
        }

        [reconciliationInstance: reconciliationInstance, reconciliationLineInstanceList: reconciliationLineInstanceList,
                    reconciliationLineInstanceTotal: reconciliationLineInstanceTotal, bankAccount: reconciliationInstance.bankAccount,
                    unreconciled: utilService.format(unrec, decimals), subtotal: utilService.format(subtot, decimals),
                    difference: utilService.format(diff, decimals), decimals: decimals, canFinalize: (diff == 0.0), hasAdded: hasAdded]
    }

    def create() {
        def bankAccount = Account.get(params.bankAccount)
        if (bankAccount?.securityCode != utilService.currentCompany().securityCode || bankAccount.type.code != 'bank' || !bookService.hasAccountAccess(bankAccount)) {
            flash.message = message(code: 'bank.not.exists', default: 'Invalid Bank account')
            redirect(action: 'list')
            return
        }

        if (Reconciliation.countByBankAccountAndFinalizedDateIsNull(bankAccount)) {
            flash.message = message(code: 'reconciliation.duplicate', default: 'Another user has created a new reconciliation whilst this one was being created')
            redirect(action: 'list', params: params)
            return
        }

        def reconciliationInstance = new Reconciliation(bankAccount: bankAccount)
        return [reconciliationInstance: reconciliationInstance]
    }

    def save() {
        def bankAccount = Account.get(params.bankAccount)
        if (bankAccount?.securityCode != utilService.currentCompany().securityCode || bankAccount.type.code != 'bank' || !bookService.hasAccountAccess(bankAccount)) {
            flash.message = message(code: 'bank.not.exists', default: 'Invalid Bank account')
            redirect(action: 'list')
            return
        }

        if (Reconciliation.countByBankAccountAndFinalizedDateIsNull(bankAccount)) {
            flash.message = message(code: 'reconciliation.duplicate', default: 'Another user has created a new reconciliation whilst this one was being created')
            redirect(action: 'list', params: params)
            return
        }

        def reconciliationInstance = new Reconciliation(bankAccount: bankAccount)
        reconciliationInstance.properties['statementDate', 'statementBalance'] = params
        def valid = !reconciliationInstance.hasErrors()

        if (valid && (!reconciliationInstance.statementDate || reconciliationInstance.statementDate != utilService.fixDate(reconciliationInstance.statementDate) ||
        Reconciliation.countByBankAccountAndStatementDateGreaterThanEquals(bankAccount, reconciliationInstance.statementDate) ||
        reconciliationInstance.statementDate > new Date())) {
            reconciliationInstance.errorMessage(field: 'statementDate', code: 'reconciliation.bad.date', default: 'Invalid Bank Statement Date')
            valid = false
        }

        if (valid && (reconciliationInstance.statementBalance == null ||
        reconciliationInstance.statementBalance != utilService.round(reconciliationInstance.statementBalance, bankAccount.currency.decimals))) {
            reconciliationInstance.errorMessage(field: 'statementBalance', code: 'reconciliation.bad.balance', default: 'Invalid Bank Statement Balance')
            valid = false
        }

        if (valid) {
            params.p_account = bankAccount.id.toString()
            params.p_date = utilService.format(reconciliationInstance.statementDate, 1)
            params.p_balance = utilService.format(reconciliationInstance.statementBalance, bankAccount.currency.decimals, false)
            def result = utilService.demandRunFromParams('reconcile', params)
            if (result instanceof String) {
                reconciliationInstance.errors.reject(null, result)
                valid = false
            } else {
                flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
                redirect(action: 'list', params: [bankAccount: params.bankAccount])
                return
            }
        }

        render(view: 'create', model: [reconciliationInstance: reconciliationInstance])
    }

    // Ajax call to set/clear the reconciled flag on a line
    def reconcileLine() {
        def errMessage, reconciliationInstance
        def reconciliationLineInstance = ReconciliationLine.findByIdAndSecurityCode(params.lineId, utilService.currentCompany().securityCode)
        if (reconciliationLineInstance) {
            reconciliationInstance = reconciliationLineInstance.reconciliation
            if (!reconciliationInstance.finalizedDate && bookService.hasAccountAccess(reconciliationInstance.bankAccount)) {
                def newState = (params.newState == 'true')
                def oldState = (reconciliationLineInstance.bankAccountValue == reconciliationLineInstance.reconciledValue)
                if (oldState != newState) {
                    reconciliationLineInstance.reconciledValue = newState ? reconciliationLineInstance.bankAccountValue : 0.0
                    for (detail in reconciliationLineInstance.details) detail.reconciled = newState
                    if (!reconciliationLineInstance.save(flush: true)) {     // With deep validation
                        errMessage = message(code: 'reconciliation.line.bad', default: 'Unable to update the line')
                    }
                }
            } else {
                errMessage = message(code: 'reconciliation.line.invalid', default: 'Line not found')
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

    def add() {
        def reconciliationInstance = Reconciliation.get(params.reconciliation)
        if (reconciliationInstance?.securityCode != utilService.currentCompany().securityCode || reconciliationInstance.finalizedDate ||
        !bookService.hasAccountAccess(reconciliationInstance.bankAccount)) {
            flash.message = utilService.standardMessage('not.found', 'reconciliation', params.reconciliation)
            redirect(action: 'list')
            return
        }

        params.max = utilService.max
        params.offset = utilService.offset
        def key = 'B' + reconciliationInstance.bankAccount.id.toString()
        def transactionInstanceList = GeneralTransaction.findAll('from GeneralTransaction where reconciliationKey = ? and reconciled is null order by document.documentDate, id',
                [key], [max: params.max, offset: params.offset])
        def transactionInstanceTotal = GeneralTransaction.countByReconciliationKeyAndReconciledIsNull(key)
        return [reconciliationInstance: reconciliationInstance, transactionInstanceList: transactionInstanceList,
            transactionInstanceTotal: transactionInstanceTotal, decimals: reconciliationInstance.bankAccount.currency.decimals]
    }

    def adding() {
        def reconciliationInstance = Reconciliation.get(params.reconciliation)
        if (reconciliationInstance?.securityCode != utilService.currentCompany().securityCode || reconciliationInstance.finalizedDate ||
        !bookService.hasAccountAccess(reconciliationInstance.bankAccount)) {
            flash.message = utilService.standardMessage('not.found', 'reconciliation', params.reconciliation)
            redirect(action: 'list')
            return
        }

        def transactionInstance = GeneralTransaction.get(params.id)
        if (transactionInstance?.securityCode != reconciliationInstance.securityCode || transactionInstance.reconciled ||
        transactionInstance.balance.account.id != reconciliationInstance.bankAccount.id) {
            flash.message = message(code: 'document.invalid', default: 'Invalid document')
        } else {
            def document = transactionInstance.document
            def reconciliationLine = new ReconciliationLine(reconciliation: reconciliationInstance, documentId: document.id, documentDate: document.documentDate,
                    documentCode: document.type.code + document.code, documentReference: document.reference, documentDescription: transactionInstance.description,
                    bankAccountValue: transactionInstance.generalValue, sequencer: transactionInstance.id, added: true)
            if (transactionInstance instanceof doc.Total) {
                def bankCurrency = reconciliationInstance.bankAccount.currency
                def companyCurrency = utilService.companyCurrency()
                def detailTotal = 0.0
                def reconciliationLineDetail
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
                    } else if (bankCurrency.id == companyCurrency.id) {
                        reconciliationLineDetail.bankAccountValue = -line.companyValue
                    } else if (line.documentValue == -transactionInstance.documentValue) {
                        reconciliationLineDetail.bankAccountValue = transactionInstance.generalValue
                    } else if (line.customer && bankCurrency.id == line.customer.currency.id) {
                        reconciliationLineDetail.bankAccountValue = -line.accountValue
                    } else if (line.supplier && bankCurrency.id == line.supplier.currency.id) {
                        reconciliationLineDetail.bankAccountValue = -line.accountValue
                    } else if (!line.customer && !line.supplier && bankCurrency.id == line.balance.account.currency.id) {
                        reconciliationLineDetail.bankAccountValue = -line.generalValue
                    } else {
                        reconciliationLineDetail.bankAccountValue = utilService.round((transactionInstance.generalValue * line.documentValue) / transactionInstance.documentValue, bankCurrency.decimals)
                    }

                    // Keep a running total of the detail records
                    detailTotal += reconciliationLineDetail.bankAccountValue
                }

                // Since we might be handling a foreign currency (from the bank account's
                // point of view, we may have introduced a rounding error that needs correction
                if (detailTotal != transactionInstance.generalValue) {
                    def diff = transactionInstance.generalValue - detailTotal
                    def victimLine
                    for (line in reconciliationLine.details) if (!victimLine || line.bankAccountValue.abs() > victimLine.bankAccountValue.abs()) victimLine = line
                    victimLine.bankAccountValue += diff
                }
            }

            Reconciliation.withTransaction {status ->
                if (reconciliationLine.save(flush: true)) {     // With deep validation
                    transactionInstance.reconciled = reconciliationInstance.statementDate
                    if (transactionInstance.saveThis()) {
                        reconciliationInstance.bankAccountBalance += transactionInstance.generalValue
                        if (reconciliationInstance.saveThis()) {
                            flash.message = message(code: 'reconciliation.added', args: [transactionInstance.document.type.code + transactionInstance.document.code], default: "Document ${transactionInstance.document.type.code + transactionInstance.document.code} added")
                        } else {
                            flash.message = message(code: 'reconciliation.bad.save', default: 'Unable to save the bank reconciliation')
                            status.setRollbackOnly()
                        }
                    } else {
                        flash.message = message(code: 'reconciliation.gl.save', default: 'Unable to update the General Ledger transaction(s)')
                        status.setRollbackOnly()
                    }
                } else {
                    flash.message = message(code: 'reconciliation.line.bad', default: 'Unable to update the line')
                    status.setRollbackOnly()
                }
            }
        }

        redirect(action: 'edit', id: reconciliationInstance.id)
    }

    def remove() {
        def reconciliationInstance = Reconciliation.findByIdAndSecurityCode(params.reconciliation, utilService.currentCompany().securityCode)
        if (!reconciliationInstance || !bookService.hasAccountAccess(reconciliationInstance.bankAccount) || reconciliationInstance.finalizedDate) {
            flash.message = utilService.standardMessage('not.found', 'reconciliation', params.id)
            redirect(action: 'list')
            return
        }

        params.max = utilService.max
        params.offset = utilService.offset
        def reconciliationLineInstanceList = ReconciliationLine.findAll('from ReconciliationLine where reconciliation = ? and added = ? order by documentDate, documentCode, sequencer',
                [reconciliationInstance, true], [max: params.max, offset: params.offset])
        def reconciliationLineInstanceTotal = ReconciliationLine.countByReconciliationAndAdded(reconciliationInstance, true)
        def decimals = reconciliationInstance.bankAccount.currency.decimals

        // Set any single detail line description
        def account
        for (line in reconciliationLineInstanceList) {
            if (line.detailCount == 1) {
                for (detail in line.details) {
                    if (detail.type == 'ar') {
                        account = Customer.get(detail.ledgerId)
                    } else if (detail.type == 'ap') {
                        account = Supplier.get(detail.ledgerId)
                    } else {
                        account = Account.get(detail.ledgerId)
                    }

                    line.detailDescription = account ? account.name : detail.ledgerCode
                    if (detail.description) line.detailDescription = line.detailDescription + ' (' + detail.description + ')'
                }
            }
        }

        [reconciliationInstance: reconciliationInstance, reconciliationLineInstanceList: reconciliationLineInstanceList,
                    reconciliationLineInstanceTotal: reconciliationLineInstanceTotal, decimals: decimals]
    }

    def removing() {
        def reconciliationInstance = Reconciliation.get(params.reconciliation)
        if (reconciliationInstance?.securityCode != utilService.currentCompany().securityCode || reconciliationInstance.finalizedDate ||
        !bookService.hasAccountAccess(reconciliationInstance.bankAccount)) {
            flash.message = utilService.standardMessage('not.found', 'reconciliation', params.reconciliation)
            redirect(action: 'list')
            return
        }

        def reconciliationLineInstance = ReconciliationLine.get(params.id)
        if (reconciliationLineInstance?.reconciliation?.id != reconciliationInstance.id) {
            flash.message = message(code: 'reconciliation.line.invalid', default: 'Line not found')
            redirect(action: 'edit', id: reconciliationInstance.id)
            return
        }

        def transactionInstance = GeneralTransaction.get(reconciliationLineInstance.sequencer)
        if (transactionInstance?.reconciled != reconciliationInstance.statementDate || transactionInstance.balance.account.id != reconciliationInstance.bankAccount.id) {
            flash.message = utilService.standardMessage('not.found', 'generalTransaction', reconciliationLineInstance.sequencer)
            redirect(action: 'edit', id: reconciliationInstance.id)
            return
        }

        Reconciliation.withTransaction {status ->
            try {
                reconciliationLineInstance.delete(flush: true)
                transactionInstance.reconciled = null
                if (transactionInstance.saveThis()) {
                    reconciliationInstance.bankAccountBalance -= transactionInstance.generalValue
                    if (reconciliationInstance.saveThis()) {
                        flash.message = message(code: 'reconciliation.removed', args: [reconciliationLineInstance.documentCode], default: "Document ${reconciliationLineInstance.documentCode} removed")
                    } else {
                        flash.message = message(code: 'reconciliation.bad.save', default: 'Unable to save the bank reconciliation')
                        status.setRollbackOnly()
                    }
                } else {
                    flash.message = message(code: 'reconciliation.gl.save', default: 'Unable to update the General Ledger transaction(s)')
                    status.setRollbackOnly()
                }
            } catch (Exception e) {
                flash.message = message(code: 'reconciliation.line.bad', default: 'Unable to update the line')
                status.setRollbackOnly()
            }
        }

        redirect(action: 'edit', id: reconciliationInstance.id)
    }

    def finalization() {
        def reconciliationInstance = Reconciliation.get(params.id)
        if (reconciliationInstance?.securityCode != utilService.currentCompany().securityCode || reconciliationInstance.finalizedDate ||
        !bookService.hasAccountAccess(reconciliationInstance.bankAccount)) {
            flash.message = utilService.standardMessage('not.found', 'reconciliation', params.reconciliation)
            redirect(action: 'list')
            return
        }

        def unrec = ReconciliationLine.executeQuery('select sum(bankAccountValue - reconciledValue) from ReconciliationLine where reconciliation = ?', [reconciliationInstance])[0] ?: 0.0
        def subtot = reconciliationInstance.statementBalance + unrec
        def diff = subtot - reconciliationInstance.bankAccountBalance
        if (diff != 0.0) {
            flash.message = message(code: 'reconciliation.bad.finalize', default: 'Reconciliation has not been finalized since it still has a difference')
            redirect(action: 'list')
            return
        }

        Reconciliation.withTransaction{status ->
            reconciliationInstance.finalizedDate = utilService.fixDate()
            if (reconciliationInstance.saveThis()) {
                def finalCheck = ReconciliationLine.executeQuery('select sum(bankAccountValue - reconciledValue) from ReconciliationLine where reconciliation = ?', [reconciliationInstance])[0] ?: 0.0
                if (finalCheck != unrec) {
                    flash.message = message(code: 'reconciliation.bad.finalize', default: 'Reconciliation has not been finalized since it still has a difference')
                    status.setRollbackOnly()
                }
            } else {
                flash.message = message(code: 'reconciliation.bad.save', default: 'Unable to save the bank reconciliation')
                status.setRollbackOnly()
            }
        }

        redirect(action: 'list', params: [bankAccount: reconciliationInstance.bankAccount.id])
    }

    def display() {
        if (params.action == 'index') params.action = 'display'
        def reconciliationInstance = Reconciliation.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!reconciliationInstance || !bookService.hasAccountAccess(reconciliationInstance.bankAccount) || !reconciliationInstance.finalizedDate) {
            flash.message = utilService.standardMessage('not.found', 'reconciliation', params.id)
            redirect(action: 'list')
            return
        }

        params.max = utilService.max
        params.offset = utilService.offset
        def reconciliationLineInstanceList = ReconciliationLine.findAll('from ReconciliationLine where reconciliation = ? order by documentDate, documentCode, sequencer',
                [reconciliationInstance], [max: params.max, offset: params.offset])
        def reconciliationLineInstanceTotal = ReconciliationLine.countByReconciliation(reconciliationInstance)
        def unrec = ReconciliationLine.executeQuery('select sum(bankAccountValue - reconciledValue) from ReconciliationLine where reconciliation = ?', [reconciliationInstance])[0] ?: 0.0
        def subtot = reconciliationInstance.statementBalance + unrec
        def diff = subtot - reconciliationInstance.bankAccountBalance
        def decimals = reconciliationInstance.bankAccount.currency.decimals

        // Set any single detail line description
        def account
        for (line in reconciliationLineInstanceList) {
            if (line.detailCount == 1) {
                for (detail in line.details) {
                    if (detail.type == 'ar') {
                        account = Customer.get(detail.ledgerId)
                    } else if (detail.type == 'ap') {
                        account = Supplier.get(detail.ledgerId)
                    } else {
                        account = Account.get(detail.ledgerId)
                    }

                    line.detailDescription = account ? account.name : detail.ledgerCode
                    if (detail.description) line.detailDescription = line.detailDescription + ' (' + detail.description + ')'
                }
            }
        }

        [reconciliationInstance: reconciliationInstance, reconciliationLineInstanceList: reconciliationLineInstanceList,
                    reconciliationLineInstanceTotal: reconciliationLineInstanceTotal, bankAccount: reconciliationInstance.bankAccount,
                    unreconciled: utilService.format(unrec, decimals), subtotal: utilService.format(subtot, decimals),
                    difference: utilService.format(diff, decimals), decimals: decimals]
    }

    def print() {
        def reconciliationInstance = Reconciliation.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!reconciliationInstance || !bookService.hasAccountAccess(reconciliationInstance.bankAccount)) {
            flash.message = utilService.standardMessage('not.found', 'reconciliation', params.id)
        } else {
            params.p_recId = reconciliationInstance.id.toString()
            def result = utilService.demandRunFromParams('recReport', params)
            if (result instanceof String) {
                flash.message = result
            } else {
                flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            }
        }

        redirect(action: params.caller ?: 'list', id: params.id)
    }
}