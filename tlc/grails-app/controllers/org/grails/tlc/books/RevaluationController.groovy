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

import org.grails.tlc.obj.Revaluation

class RevaluationController {

    // Injected services
    def utilService
    def bookService

    // Security settings
    def activities = [default: 'revalue', enquire: 'enquire']

    // List of actions with specific request types
    static allowedMethods = [revaluing: 'POST']

    def index() { }

    def revalue() {
		def revaluationInstance = new Revaluation()
        def periodList = bookService.getActivePeriods(utilService.currentCompany())
        def accountList = []
        if (periodList) revaluationInstance.period = periodList[0]
        def temp = bookService.getControlAccount(utilService.currentCompany(), 'fxDiff')
        if (temp) accountList << temp
        temp = bookService.getControlAccount(utilService.currentCompany(), 'fxRevalue')
        if (temp) accountList << temp
        if (accountList) revaluationInstance.account = accountList[0]
        periodList = periodList.reverse()    // More intuitive to see it in reverse order

        [revaluationInstance: revaluationInstance, periodList: periodList, accountList: accountList, queueNumber: 0L]
    }

    def revaluing(Revaluation revaluationInstance) {
        def result = 0L
        def periodList = bookService.getActivePeriods(utilService.currentCompany())
        def accountList = []
        def temp = bookService.getControlAccount(utilService.currentCompany(), 'fxDiff')
        if (temp) accountList << temp
        temp = bookService.getControlAccount(utilService.currentCompany(), 'fxRevalue')
        if (temp) accountList << temp
        if (revaluationInstance?.period?.status == 'adjust' && !revaluationInstance?.adjustment) {
            revaluationInstance.errorMessage(field: 'adjustment', code: 'document.adjust.period', args: [revaluationInstance.period.code],
				default: "Period ${revaluationInstance.period.code} is an adjustment period and you may only post this document to it if you set the Adjustment flag")
        } else {
            if (!revaluationInstance.hasErrors() && revaluationInstance.validate()) {
                def valid = true
                for (int i = 0; i < periodList.size(); i++) {
                    if (periodList[i].id == revaluationInstance.period.id) {
                        if (i == periodList.size() - 1) {
                            revaluationInstance.errorMessage(field: 'reverse', code: 'document.next.period', args: [revaluationInstance.period.code],
								default: "No active period found after ${revaluationInstance.period.code} to which the reversal could be posted")
                            valid = false
                        }

                        break
                    }
                }

                if (valid) {
                    temp = 'arRevalue'
                    if (!bookService.getControlAccount(utilService.currentCompany(), temp)) {
                        revaluationInstance.errorMessage(code: 'document.no.control', args: [temp],
							default: "Could not find the ${temp} control account in the General Ledger")
                        valid = false
                    }

                    temp = 'apRevalue'
                    if (!bookService.getControlAccount(utilService.currentCompany(), temp)) {
                        revaluationInstance.errorMessage(code: 'document.no.control', args: [temp],
							default: "Could not find the ${temp} control account in the General Ledger")
                        valid = false
                    }
                }

                if (valid) {
                    params.p_periodId = revaluationInstance?.period?.id?.toString()
                    params.p_targetId = revaluationInstance?.account?.id?.toString()
                    params.p_adjustment = revaluationInstance?.adjustment?.toString()
                    result = utilService.demandRunFromParams('fxRevalue', params)
                    if (result instanceof String) {
                        flash.message = result
                        result = 0L
                    }
                }
            }
        }

        if (!result) periodList = periodList.reverse()    // More intuitive to see it in reverse order
        render(view: 'revalue', model: [revaluationInstance: revaluationInstance, periodList: periodList, accountList: accountList, queueNumber: result])
    }

    def enquire() {
        def model = bookService.loadDocumentModel(params, ['FXR'])
        def documentInstance = model.documentInstance
        model.hasBreakdown = false
        model.breakdown = params.breakdown
        if (documentInstance.id) {
            model.hasBreakdown = PeriodRevaluation.countByDocument(documentInstance)
            def debit = 0.0
            def credit = 0.0
            def val

            // Set the document adjustment flag - only need to look at the first line
            for (line in documentInstance.lines) {
                documentInstance.sourceAdjustment = line.adjustment
                break
            }

            // If they're asking to see the breakdown
            if (model.hasBreakdown && model.breakdown) {
                val = 0.0
                def companyCurrency = utilService.companyCurrency()
                def revaluationInstanceList = PeriodRevaluation.findAll('from PeriodRevaluation as x where x.document = ? order by x.revaluationAccount.code, x.id', [documentInstance])
                def isForeignCurrency = (model.displayCurrency && model.displayCurrency.id != companyCurrency.id)
                for (rev in revaluationInstanceList) {
                    if (isForeignCurrency) {
                        rev.currentBalance = utilService.convertCurrency(model.displayCurrency, companyCurrency, rev.currentBalance)
                        rev.priorRevaluations = utilService.convertCurrency(model.displayCurrency, companyCurrency, rev.priorRevaluations)
                        rev.currentRevaluation = utilService.convertCurrency(model.displayCurrency, companyCurrency, rev.currentRevaluation)
                        rev.revaluedBalance = utilService.convertCurrency(model.displayCurrency, companyCurrency, rev.revaluedBalance)
                    }

                    if (rev.currentRevaluation != null) {
                        val -= rev.currentRevaluation
                        if (rev.currentRevaluation < 0.0) {
                            credit -= rev.currentRevaluation
                        } else {
                            debit += rev.currentRevaluation
                        }
                    }

                    rev.discard()   // Don't let grails try and save it!
                }

                // Find the fxDiff (or fxRevalue) account line in the document
                def revAccount, type
                for (line in documentInstance.lines) {
                    type = line.balance.account.type.code
                    if (type == 'fxDiff' || type == 'fxRevalue') {
                        revAccount = line.balance.account
                        break
                    }
                }

                // Put in the revaluation difference line
                revaluationInstanceList << new PeriodRevaluation(revaluationAccount: revAccount, currentRevaluation: val)
                if (val < 0.0) {
                    credit -= val
                } else {
                    debit += val
                }

                model.revaluationInstanceList = revaluationInstanceList
                model.totalInstance = [debit: debit, credit: credit, scale: isForeignCurrency ? model.displayCurrency.decimals : companyCurrency.decimals]
            } else {    // Asking to see the document summary
                def parameters = [context: documentInstance, field: 'value', currency: model.displayCurrency]
                for (line in documentInstance.lines) {
                    parameters.line = line
                    val = bookService.getBookValue(parameters)
                    if (val != null && !(val instanceof String)) {
                        if (val < 0.0) {
                            credit -= val
                        } else {
                            debit += val
                        }
                    }
                }

                model.totalInstance = [debit: debit, credit: credit, scale: parameters.scale]
            }
        }

        model
    }
}
