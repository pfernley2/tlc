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

import org.grails.tlc.corp.ExchangeCurrency

class RecurringController {

    // Injected services
    def utilService
    def bookService
    def postingService

    // Security settings
    def activities = [default: 'recurring']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', lines: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        def max = utilService.max
        def offset = utilService.offset
        def bankAccountList = []
        def fragment
        def banks = Account.findAll("from Account as x where x.securityCode = ? and x.type.code = 'bank' order by x.name", [utilService.currentCompany().securityCode])
        for (bank in banks) {
            if (bookService.hasAccountAccess(bank)) {
                bankAccountList << bank
                if (fragment) {
                    fragment += ', ?'
                } else {
                    fragment = '?'
                }
            }
        }

        def listing, total
        if (fragment) {
            listing = Recurring.findAll('from Recurring as x where x.account in (' + fragment + ') order by x.account.name, x.reference', bankAccountList, [max: max, offset: offset])
            total = Recurring.countByAccountInList(bankAccountList)
        } else {
            listing = []
            total = 0
        }

        [recurringInstanceList: listing, recurringInstanceTotal: total]
    }

    def show() {
        def recurringInstance = Recurring.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!recurringInstance || !bookService.hasAccountAccess(recurringInstance.account)) {
            flash.message = utilService.standardMessage('not.found', 'recurring', params.id)
            redirect(action: 'list')
        } else {
            def settings = [:]
            settings.decimals = recurringInstance.currency.decimals
            return [recurringInstance: recurringInstance, settings: settings]
        }
    }

    def delete() {
        def recurringInstance = Recurring.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (recurringInstance && bookService.hasAccountAccess(recurringInstance.account)) {
            try {
                recurringInstance.delete(flush: true)
                flash.message = utilService.standardMessage('deleted', recurringInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', recurringInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'recurring', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def recurringInstance = Recurring.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!recurringInstance || !bookService.hasAccountAccess(recurringInstance.account)) {
            flash.message = utilService.standardMessage('not.found', 'recurring', params.id)
            redirect(action: 'list')
        } else {
            def model = getModel(utilService.currentCompany(), recurringInstance)
            for (line in recurringInstance.lines) {
                updateTransientLineData(line, model.settings, recurringInstance.initialValue)
            }

            return model
        }
    }

    def update(Long version) {
        def recurringInstance = Recurring.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (recurringInstance && bookService.hasAccountAccess(recurringInstance.account)) {

            if (!recurringInstance.sourceCode && recurringInstance.account) recurringInstance.sourceCode = recurringInstance.account.code       // May be disabled

            // Load the document lines from the request parameters
            postingService.refreshRecurringLines(recurringInstance, params)

            // Some transient line data may be missing if the line is disabled
            reconstructUsedLines(recurringInstance)

            if (version != null && recurringInstance.version > version) {
                recurringInstance.errorMessage(code: 'locking.failure', domain: 'recurring')
                render(view: 'edit', model: getModel(utilService.currentCompany(), recurringInstance))
                return
            }

            if (saveInstance(recurringInstance, params)) {
                flash.message = utilService.standardMessage('updated', recurringInstance)
                redirect(action: 'show', id: recurringInstance.id)
            } else {
                render(view: 'edit', model: getModel(utilService.currentCompany(), recurringInstance))
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'recurring', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def recurringInstance = new Recurring()
        for (int i = 0; i < 10; i++) recurringInstance.addToLines(new RecurringLine())
        getModel(utilService.currentCompany(), recurringInstance)
    }

    def save() {
        def recurringInstance = new Recurring()
        if (saveInstance(recurringInstance, params)) {
            flash.message = utilService.standardMessage('created', recurringInstance)
            redirect(action: 'show', id: recurringInstance.id)
        } else {
            render(view: 'create', model: getModel(utilService.currentCompany(), recurringInstance))
        }
    }

    def lines() {
        def recurringInstance
        if (params.id) {
            recurringInstance = Recurring.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
            if (!recurringInstance || !bookService.hasAccountAccess(recurringInstance.account)) {
                flash.message = utilService.standardMessage('not.found', 'recurring', params.id)
                redirect(action: 'list')
                return
            }

            if (!recurringInstance.sourceCode && recurringInstance.account) recurringInstance.sourceCode = recurringInstance.account.code       // May be disabled
        } else {
            recurringInstance = new Recurring()
        }

        recurringInstance.properties['account', 'currency', 'type', 'sourceCode', 'reference', 'description', 'totalTransactions',
            'initialDate', 'initialValue', 'recursFrom', 'recurrenceType', 'recurrenceInterval', 'lastDayOfMonth', 'recurringValue',
            'finalValue', 'autoAllocate', 'nextDue'] = params

        // Load the document lines from the request parameters
        postingService.refreshRecurringLines(recurringInstance, params)

        // Some transient line data may be missing if the line is disabled
        reconstructUsedLines(recurringInstance)

        // Add the lines
        for (int i = 0; i < 10; i++) recurringInstance.addToLines(new RecurringLine())

        // Grails would automatically save an existing record that was modified if we didn't discard it
        if (recurringInstance.id) recurringInstance.discard()

        render(view: params.view, model: getModel(utilService.currentCompany(), recurringInstance))
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private updateTransientLineData(line, settings, initialValue) {
        def dataFound = false
        if (line.customer) {
            line.accountType = 'ar'
            if (bookService.hasCustomerAccess(line.customer)) {
                line.accountCode = line.customer.code
                line.accountName = line.customer.name
                dataFound = true
            }
        } else if (line.supplier) {
            line.accountType = 'ap'
            if (bookService.hasSupplierAccess(line.supplier)) {
                line.accountCode = line.supplier.code
                line.accountName = line.supplier.name
                dataFound = true
            }
        } else {
            line.accountType = 'gl'
            if (line.account && bookService.hasAccountAccess(line.account)) {
                line.accountCode = line.account.code
                line.accountName = line.account.name
                dataFound = true
            }
        }

        if (dataFound) {
            if (settings.isComplete ||
            (settings.disableInitial && (line.initialValue || (line.recurringValue && !initialValue))) ||
            (settings.disableRecurring && line.recurringValue)) {
                line.used = true
            }
        }
    }

    private reconstructUsedLines(recurringInstance) {
        for (line in recurringInstance.lines) {
            if (line.used) {
                if (line.customer) {
                    line.accountType = 'ar'
                    if (bookService.hasCustomerAccess(line.customer)) {
                        line.accountCode = line.customer.code
                        line.accountName = line.customer.name
                    }
                } else if (line.supplier) {
                    line.accountType = 'ap'
                    if (bookService.hasSupplierAccess(line.supplier)) {
                        line.accountCode = line.supplier.code
                        line.accountName = line.supplier.name
                    }
                } else {
                    line.accountType = 'gl'
                    if (line.account && bookService.hasAccountAccess(line.account)) {
                        line.accountCode = line.account.code
                        line.accountName = line.account.name
                    }
                }
            }
        }
    }

    private getModel(company, recurringInstance) {
        def documentTypeList = DocumentType.findAll("from DocumentType as dt where dt.company = ? and dt.type.code in ('BP', 'BR')", [company])
        def currencyList = ExchangeCurrency.findAllByCompany(company, [cache: true])
        if (!recurringInstance.sourceCode) recurringInstance.sourceCode = recurringInstance.account?.code
        def bankAccountList = []
        def bankAccount
        def banks = Account.findAll("from Account as x where x.securityCode = ? and x.type.code = 'bank' order by x.name", [company.securityCode])
        for (bank in banks) {
            if (bookService.hasAccountAccess(bank)) {
                bankAccountList << bank
                if (bank.code == recurringInstance.sourceCode) bankAccount = bank
            }
        }

        if (!bankAccount) recurringInstance.sourceCode = null
        if (!recurringInstance.currency) recurringInstance.currency = bankAccount?.currency ?: utilService.companyCurrency()
        def settings = [:]
        settings.decimals = recurringInstance.currency.decimals
        settings.enableInitial = (recurringInstance.processedCount == 0)
        settings.enableRecurring = (recurringInstance.processedCount == 0 || (recurringInstance.processedCount == 1 && recurringInstance.initialDate))
        settings.isComplete = (recurringInstance.processedCount == recurringInstance.totalTransactions)
        settings.recurringComplete = settings.isComplete ?: (recurringInstance.processedCount + 1 == recurringInstance.totalTransactions && recurringInstance.finalValue)
        settings.disableInitial = !settings.enableInitial
        settings.disableRecurring = !settings.enableRecurring
        settings.isOngoing = !settings.isComplete
        settings.recurringIncomplete = !settings.recurringComplete
        return [recurringInstance: recurringInstance, bankAccountList: bankAccountList, documentTypeList: documentTypeList, currencyList: currencyList, settings: settings]
    }

    private saveInstance(recurringInstance, params) {
        def company = utilService.currentCompany()
        recurringInstance.properties['account', 'currency', 'type', 'sourceCode', 'reference', 'description', 'totalTransactions',
            'initialDate', 'initialValue', 'recursFrom', 'recurrenceType', 'recurrenceInterval', 'lastDayOfMonth', 'recurringValue',
            'finalValue', 'autoAllocate', 'nextDue'] = params
        def valid = !recurringInstance.hasErrors()
        def removables = []
        def documentDecs = recurringInstance.currency?.decimals
        def account, temp
        def customers = [:]
        def suppliers = [:]
        def initialTotal = 0.0
        def recurringTotal = 0.0
        def finalTotal = 0.0

        // Load the document lines from the request parameters and check for data binding errors
        // in the line at the same time. We do this whether the header had a fault or not
        def num = postingService.refreshRecurringLines(recurringInstance, params)
        if (num) {
            recurringInstance.errorMessage(code: 'document.line.data', args: [num], default: "Line ${num} has a 'data type' error")
            valid = false
        }

        // Now get on to standard validation, starting with the header: Make sure references are to the correct company objects
        if (valid) {
            utilService.verify(recurringInstance, ['type', 'currency'])
            if (recurringInstance.type == null || !['BP', 'BR'].contains(recurringInstance.type.type.code)) {
                recurringInstance.errorMessage(field: 'type', code: 'document.bad.type', default: 'Invalid document type')
                valid = false
            }

            if (recurringInstance.currency == null) {
                recurringInstance.errorMessage(field: 'currency', code: 'document.bad.currency', default: 'Invalid document currency')
                valid = false
            }
        }

        // Check out the bank account
        if (valid) {
            if (recurringInstance.sourceCode) {
                account = Account.findBySecurityCodeAndCode(company.securityCode, recurringInstance.sourceCode)
                if (!account || account.type?.code != 'bank' || !bookService.hasAccountAccess(account)) {
                    recurringInstance.errorMessage(field: 'sourceCode', code: 'bank.not.exists', default: 'Invalid Bank account')
                    valid = false
                } else {
                    recurringInstance.account = account
                }
            } else {
                recurringInstance.account = null
            }
        }

        // Do an overall header validation
        if (valid) {
            recurringInstance.initialValue = recurringInstance.initialValue ? utilService.round(recurringInstance.initialValue, documentDecs) : null
            recurringInstance.recurringValue = recurringInstance.recurringValue ? utilService.round(recurringInstance.recurringValue, documentDecs) : null
            recurringInstance.finalValue = recurringInstance.finalValue ? utilService.round(recurringInstance.finalValue, documentDecs) : null
            valid = recurringInstance.validate()
        }

        // Check out that any upcoming start date is acceptable
        if (valid && recurringInstance.processedCount < 2) {
            def testDate = (!recurringInstance.processedCount && recurringInstance.initialDate) ? recurringInstance.initialDate : recurringInstance.recursFrom
            def pds = bookService.getActivePeriods(company)
            if (pds && testDate < pds[0].validFrom) {
                if (testDate == recurringInstance.initialDate) {
                    recurringInstance.errorMessage(field: 'initialDate', code: 'recurring.bad.initial', default: 'The Initial Date is before the first period that can be posted to')
                } else {
                    recurringInstance.errorMessage(field: 'recursFrom', code: 'recurring.bad.from', default: 'The Recurs From date is before the first period that can be posted to')
                }

                valid = false
            }
        }

        // Step through each line checking it in detail
        if (valid) {
            num = 0
            for (line in recurringInstance.lines) {
                num++
                if (line.accountCode) {
                    if (line.accountType == 'gl') {

                        // Make sure the GL account code is expanded for mnemonics and case is correct
                        temp = bookService.expandAccountCode(utilService.currentUser(), line.accountCode)
                        if (!temp) {
                            temp = message(code: 'account.not.exists', default: 'Invalid GL account')
                            recurringInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        line.accountCode = temp

                        // See if the GL account actually exists
                        account = bookService.getAccount(company, line.accountCode)
                        if (account instanceof String) {
                            recurringInstance.errorMessage(code: 'document.line.message', args: [num, account], default: "Line ${num}: ${account}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        // Make sure the GL account is active and that the user is allowed to access this account and
                        // that the account is not restricted as to what sort of documents can be posted to it
                        if (account?.active && bookService.hasAccountAccess(account)) {
                            valid = postingService.canPostDocumentToAccount(recurringInstance, line, num, account)
                            if (!valid) break
                        } else {
                            temp = message(code: 'account.not.exists', default: 'Invalid GL account')
                            recurringInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        // Make sure it's not the Bank Account they're trying to post to
                        if (account.code == recurringInstance.sourceCode) {
                            temp = message(code: 'bank.not.self', default: 'You cannot post a bank transaction to the originating Bank account')
                            recurringInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        line.account = account
                        line.customer = null
                        line.supplier = null
                    } else if (line.accountType == 'ar') {
                        account = Customer.findByCompanyAndCode(company, bookService.fixCustomerCase(line.accountCode))
                        if (!account?.active || !bookService.hasCustomerAccess(account)) {
                            temp = message(code: 'document.customer.invalid', default: 'Invalid customer')
                            recurringInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        if (customers.containsKey(account.code)) {
                            temp = message(code: 'document.customer.duplicate', args: [account.code], default: "Customer ${account.code} is a duplicate. Please combine duplicates in to a single entry.")
                            recurringInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        } else {
                            customers.put(account.code, null)
                        }

                        line.customer = account
                        line.account = null
                        line.supplier = null
                    } else if (line.accountType == 'ap') {
                        account = Supplier.findByCompanyAndCode(company, bookService.fixSupplierCase(line.accountCode))
                        if (!account?.active || !bookService.hasSupplierAccess(account)) {
                            temp = message(code: 'document.supplier.invalid', default: 'Invalid supplier')
                            recurringInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        if (suppliers.containsKey(account.code)) {
                            temp = message(code: 'document.supplier.duplicate', args: [account.code], default: "Supplier ${account.code} is a duplicate. Please combine duplicates in to a single entry.")
                            recurringInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        } else {
                            suppliers.put(account.code, null)
                        }

                        line.supplier = account
                        line.account = null
                        line.customer = null
                    } else {
                        temp = message(code: 'document.bad.ledger', default: 'Invalid ledger')
                        recurringInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('accountType', null)
                        valid = false
                        break
                    }

                    // Round any entered values
                    line.initialValue = line.initialValue ? utilService.round(line.initialValue, documentDecs) : null
                    line.recurringValue = line.recurringValue ? utilService.round(line.recurringValue, documentDecs) : null
                    line.finalValue = line.finalValue ? utilService.round(line.finalValue, documentDecs) : null

                    // Make sure there is a value
                    if (!line.initialValue && !line.recurringValue && !line.finalValue) {
                        temp = message(code: 'recurringLine.no.value', default: 'At least one value is required on a line')
                        recurringInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('recurringValue', null)
                        valid = false
                        break
                    }

                    // Do the totalling
                    if (line.initialValue) initialTotal += line.initialValue
                    if (line.recurringValue) recurringTotal += line.recurringValue
                    if (line.finalValue) finalTotal += line.finalValue
                } else {
                    removables << line
                }
            }
        }

        // Cross check the lines to the header
        if (valid && initialTotal) {
            if (recurringInstance.initialValue) {
                if (recurringInstance.initialValue != initialTotal) {
                    recurringInstance.errorMessage(field: 'initialValue', code: 'recurring.initial.mismatch', default: 'The Initial Value does not match the total of the posting analysis Initial column')
                    valid = false
                }
            } else {
                if (recurringInstance.recurringValue != initialTotal) {
                    recurringInstance.errorMessage(field: 'recurringValue', code: 'recurring.recurring.initial', default: 'The Recurring Value does not match the total of the posting analysis Initial column')
                    valid = false
                }
            }
        }

        if (valid && recurringInstance.recurringValue != recurringTotal) {
            if (recurringInstance.recurringValue != initialTotal) {
                recurringInstance.errorMessage(field: 'recurringValue', code: 'recurring.recurring.mismatch', default: 'The Recurring Value does not match the total of the posting analysis Recurring column')
                valid = false
            }
        }

        if (valid && finalTotal) {
            if (recurringInstance.finalValue) {
                if (recurringInstance.finalValue != finalTotal) {
                    recurringInstance.errorMessage(field: 'finalValue', code: 'recurring.final.mismatch', default: 'The Final Value does not match the total of the posting analysis Final column')
                    valid = false
                }
            } else {
                if (recurringInstance.recurringValue != initialTotal) {
                    recurringInstance.errorMessage(field: 'recurringValue', code: 'recurring.recurring.final', default: 'The Recurring Value does not match the total of the posting analysis Final column')
                    valid = false
                }
            }
        }

        // Remove any 'blank' lines and then save the document
        if (valid) {
            for (line in removables) {
                recurringInstance.removeFromLines(line)

                // Need to delete the items as removing them from the association dosn't do it
                if (line.id) {
                    line.delete(flush: true)
                    line.discard()
                }
            }

            valid = recurringInstance.save(flush: true)  // With deep validation
        }

        return valid
    }
}