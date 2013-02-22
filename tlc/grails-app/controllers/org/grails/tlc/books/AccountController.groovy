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

class AccountController {

    private static final BULK_LIMIT = 100   // Prevent server becoming overloaded by creating too many accounts at once

    // Injected services
    def utilService
    def bookService

    // Security settings
    def activities = [default: 'actadmin', enquire: 'enquire']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', preview: 'POST', confirm: 'POST', imported: 'POST', testing: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'name', 'active', 'status', 'revaluationMethod'].contains(params.sort) ? params.sort : 'code'
        [accountInstanceList: Account.selectList(securityCode: utilService.currentCompany().securityCode), accountInstanceTotal: Account.selectCount()]
    }

    def show() {
        def accountInstance = Account.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!accountInstance) {
            flash.message = utilService.standardMessage('not.found', 'account', params.id)
            redirect(action: 'list')
        } else {
            return [accountInstance: accountInstance, hasTransactions: accountInstance.hasTransactions()]
        }
    }

    def delete() {
        def accountInstance = Account.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (accountInstance) {
            try {
                def lock
                if (accountInstance.type) lock = bookService.getCompanyLock(utilService.currentCompany())
                if (lock) lock.lock()
                try {
                    bookService.deleteAccount(accountInstance)
                } finally {
                    if (lock) {
                        lock.unlock()
                        bookService.resetControlAccounts()
                    }
                }

                utilService.cacheService.resetByValue('account', utilService.currentCompany().securityCode, accountInstance.code)
                utilService.cacheService.resetThis('userAccount', utilService.currentCompany().securityCode, accountInstance.code)
                flash.message = utilService.standardMessage('deleted', accountInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', accountInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'account', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def accountInstance = Account.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!accountInstance) {
            flash.message = utilService.standardMessage('not.found', 'account', params.id)
            redirect(action: 'list')
        } else {
            def hasTransactions = accountInstance.hasTransactions()
            def currencyList = hasTransactions ? null : ExchangeCurrency.findAllByCompany(utilService.currentCompany(), [cache: true])
            def revaluationAccountList = Account.findAll('from Account as x where x.securityCode = ? and x.type.code = ? order by x.code', [utilService.currentCompany().securityCode, 'glRevalue'])
            return [accountInstance: accountInstance, chartSectionList: createSectionList(), currencyList: currencyList,
                hasTransactions: hasTransactions, revaluationAccountList: revaluationAccountList]
        }
    }

    def update(Long version) {
        def accountInstance = Account.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (accountInstance) {
            def hasTransactions = accountInstance.hasTransactions()
            if (version != null && accountInstance.version > version) {
                accountInstance.errorMessage(code: 'locking.failure', domain: 'account')
                def currencyList = hasTransactions ? null : ExchangeCurrency.findAllByCompany(utilService.currentCompany(), [cache: true])
                def revaluationAccountList = Account.findAll('from Account as x where x.securityCode = ? and x.type.code = ? order by x.code', [utilService.currentCompany().securityCode, 'glRevalue'])
                render(view: 'edit', model: [accountInstance: accountInstance, chartSectionList: createSectionList(), currencyList: currencyList,
                            hasTransactions: hasTransactions, revaluationAccountList: revaluationAccountList])
                return
            }

            def oldCode = accountInstance.code
            def oldCurrency = accountInstance.currency
            def oldType = accountInstance.type
            accountInstance.properties['code', 'name', 'currency', 'type', 'active', 'section', 'autoCreateElementValues', 'status', 'revaluationAccount', 'revaluationMethod'] = params
            if (!params.revaluationAccount.id && accountInstance.revaluationAccount) accountInstance.revaluationAccount = null  // Is this a wobbly in Grails 1.2.1 ?
            utilService.verify(accountInstance, ['section', 'revaluationAccount'])             // Ensure correct references
            def valid = !accountInstance.hasErrors()
            if (valid) valid = validateAccountCode(accountInstance)
            if (valid && accountInstance.type?.singleton) {
                if (accountInstance.currency.id != utilService.companyCurrency().id) {
                    accountInstance.errorMessage(field: 'currency', code: 'account.control.currency', default: 'An account of this type must use the company currency')
                    valid = false
                }

                if (!accountInstance.active) {
                    accountInstance.errorMessage(field: 'active', code: 'account.control.active', default: 'An account of this type cannot be disabled')
                    valid = false
                }
            }

            if (valid && oldType?.code == 'bank') {
                if (accountInstance.type?.code != 'bank') {
                    if (Recurring.countByAccount(accountInstance)) {
                        accountInstance.errorMessage(field: 'type', code: 'account.recurring',
                                default: 'You cannot change the type from being a bank account when it has recurring transactions defined')
                        valid = false
                    } else if (DocumentType.countByAutoBankAccount(accountInstance)) {
                        accountInstance.errorMessage(field: 'type', code: 'account.auto.payment',
                                default: 'You cannot change the type from being a bank account when it has Auto-Payment Document Types using it')
                        valid = false
                    }
                } else {
                    def autoPayments = DocumentType.findAllByAutoBankAccount(accountInstance)
                    if (autoPayments) {
                        if (!accountInstance.active) {
                            accountInstance.errorMessage(field: 'active', code: 'account.auto.active',
                                    default: 'You cannot disable a bank account that has Auto-Payment Document Types using it')
                            valid = false
                        } else if (oldCurrency.id != accountInstance.currency.id) {
                            for (ap in autoPayments) {
                                if (!ap.autoForeignCurrency && Supplier.countByDocumentTypeAndCurrencyNotEqual(ap, accountInstance.currency)) {
                                    accountInstance.errorMessage(field: 'currency', code: 'account.currency.auto',
                                            default: 'Changing the currency of this bank account would invalidate suppliers who are auto-paid from this bank account but whose account is held in a different currency')
                                    valid = false
                                    break
                                }
                            }
                        }
                    }
                }
            }

            if (valid && oldType?.code == 'glRevalue' && accountInstance.type?.code != 'glRevalue' && Account.countByRevaluationAccount(accountInstance)) {
                accountInstance.errorMessage(field: 'type', code: 'account.revalue.ref',
                        default: 'You cannot change the account type when other GL accounts are configured to use this account for their revaluations')
                valid = false
            }

            if (valid) {
                def lock
                if (accountInstance.currency.id != oldCurrency.id || accountInstance.type?.id != oldType?.id) lock = bookService.getCompanyLock(utilService.currentCompany())
                if (lock) lock.lock()
                try {
                    if (valid && oldCurrency.id != accountInstance.currency.id && hasTransactions) {
                        accountInstance.errorMessage(field: 'currency', code: 'account.currency.change', default: 'You cannot change the currency of an account after it has had transactions posted to it')
                        valid = false
                    }

                    if (valid && oldType?.id != accountInstance.type?.id) {
                        if (hasTransactions && ((oldType && !oldType.changeable) || (accountInstance.type && !accountInstance.type.changeable))) {
                            accountInstance.errorMessage(field: 'type', code: 'account.type.change', default: 'You cannot change the type of the account after it has had transactions posted to it')
                            valid = false
                        }

                        if (valid && accountInstance.type?.singleton && Account.countBySecurityCodeAndType(utilService.currentCompany().securityCode, accountInstance.type) > 0) {
                            accountInstance.errorMessage(field: 'type', code: 'account.type.dup', default: 'There is already an account of this type defined')
                            valid = false
                        }
                    }

                    if (valid) valid = accountInstance.saveThis()
                    if (valid && accountInstance.type?.id != oldType?.id) bookService.resetControlAccounts()
                } finally {
                    if (lock) lock.unlock()
                }
            }

            if (valid) {
                if (accountInstance.code != oldCode) {
                    utilService.cacheService.resetByValue('account', utilService.currentCompany().securityCode, oldCode)
                    utilService.cacheService.resetThis('userAccount', utilService.currentCompany().securityCode, oldCode)
                }

                flash.message = utilService.standardMessage('updated', accountInstance)
                redirect(action: 'show', id: accountInstance.id)
            } else {
                def currencyList = hasTransactions ? null : ExchangeCurrency.findAllByCompany(utilService.currentCompany(), [cache: true])
                def revaluationAccountList = Account.findAll('from Account as x where x.securityCode = ? and x.type.code = ? order by x.code', [utilService.currentCompany().securityCode, 'glRevalue'])
                render(view: 'edit', model: [accountInstance: accountInstance, chartSectionList: createSectionList(), currencyList: currencyList,
                            hasTransactions: hasTransactions, revaluationAccountList: revaluationAccountList])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'account', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def accountInstance = new Account()
        accountInstance.currency = utilService.companyCurrency()
        def revaluationAccountList = Account.findAll('from Account as x where x.securityCode = ? and x.type.code = ? order by x.code', [utilService.currentCompany().securityCode, 'glRevalue'])
        return [accountInstance: accountInstance, chartSectionList: createSectionList(),
            currencyList: ExchangeCurrency.findAllByCompany(utilService.currentCompany(), [cache: true]), revaluationAccountList: revaluationAccountList]
    }

    def save() {
        def accountInstance = new Account()
        accountInstance.properties['code', 'name', 'currency', 'type', 'active', 'section', 'autoCreateElementValues', 'status', 'revaluationAccount', 'revaluationMethod'] = params
        if (!params.revaluationAccount.id && accountInstance.revaluationAccount) accountInstance.revaluationAccount = null  // Is this a wobbly in Grails 1.2.1 ?
        utilService.verify(accountInstance, ['section', 'revaluationAccount'])             // Ensure correct references
        def valid = !accountInstance.hasErrors()
        if (valid) valid = validateAccountCode(accountInstance)
        if (valid && accountInstance.type?.singleton) {
            if (accountInstance.currency.id != utilService.companyCurrency().id) {
                accountInstance.errorMessage(field: 'currency', code: 'account.control.currency', default: 'An account of this type must use the company currency')
                valid = false
            }

            if (!accountInstance.active) {
                accountInstance.errorMessage(field: 'active', code: 'account.control.active', default: 'An account of this type cannot be disabled')
                valid = false
            }
        }

        def lock
        if (valid && accountInstance.type) lock = bookService.getCompanyLock(utilService.currentCompany())
        if (lock) lock.lock()
        try {
            if (valid && accountInstance.type?.singleton && Account.countBySecurityCodeAndType(utilService.currentCompany().securityCode, accountInstance.type) > 0) {
                accountInstance.errorMessage(field: 'type', code: 'account.type.dup', default: 'There is already an account of this type defined')
                valid = false
            }

            if (valid) valid = bookService.insertAccount(accountInstance)
        } finally {
            if (lock) lock.unlock()
        }

        if (valid) {
            if (accountInstance.type) bookService.resetControlAccounts()
            flash.message = utilService.standardMessage('created', accountInstance)
            redirect(action: 'show', id: accountInstance.id)
        } else {
            def revaluationAccountList = Account.findAll('from Account as x where x.securityCode = ? and x.type.code = ? order by x.code', [utilService.currentCompany().securityCode, 'glRevalue'])
            render(view: 'create', model: [accountInstance: accountInstance, chartSectionList: createSectionList(),
                        currencyList: ExchangeCurrency.findAllByCompany(utilService.currentCompany(), [cache: true]), revaluationAccountList: revaluationAccountList])
        }
    }

    def bulk() {
        [chartSectionRangeInstance: new ChartSectionRange(), chartSectionList: createSectionList(), limit: BULK_LIMIT]
    }

    def preview() {
        def chartSectionRangeInstance = new ChartSectionRange()
        chartSectionRangeInstance.properties['section', 'rangeFrom', 'rangeTo'] = params
        utilService.verify(chartSectionRangeInstance, ['section'])             // Ensure correct references
        if (chartSectionRangeInstance.rangeFrom) chartSectionRangeInstance.rangeFrom = bookService.fixCase(chartSectionRangeInstance.rangeFrom)
        if (chartSectionRangeInstance.rangeTo) chartSectionRangeInstance.rangeTo = bookService.fixCase(chartSectionRangeInstance.rangeTo)
        chartSectionRangeInstance.comment = '\t'    // Disable 'no test' checking
        if (!chartSectionRangeInstance.hasErrors() && chartSectionRangeInstance.validateThis()) {
            def section = chartSectionRangeInstance.section
            def fromSet = chartSectionRangeInstance.rangeFrom.split("\\${BookService.SEGMENT_DELIMITER}")
            def toSet = chartSectionRangeInstance.rangeTo.split("\\${BookService.SEGMENT_DELIMITER}")
            def rangeList = bookService.createAllRanges(section)
            def valueList = []
            for (int i = 0; i < fromSet.size(); i++) {
                def element = section."segment${i + 1}"
                def values = []
                valueList << values
                def ranges = new CodeRange(element.dataType, element.dataLength, fromSet[i], toSet[i]).includeOnly(rangeList[i])
                for (range in ranges) {
                    def codes = CodeElementValue.findAll('from CodeElementValue as x where x.element = ? and x.code >= ? and x.code <= ? order by x.code', [element, range.from, range.to])
                    if (codes) values.addAll(codes)
                }

                if (!values) {
                    chartSectionRangeInstance.errorMessage(code: 'account.bulk.no.values', args: [element.name], default: "There are no suitable values available for code element ${element.name}")
                    render(view: 'bulk', model: [chartSectionRangeInstance: chartSectionRangeInstance, chartSectionList: createSectionList()])
                    return
                }
            }

            def disallowList = (rangeList.size() > fromSet.size()) ? rangeList[fromSet.size()..-1] : []
            def accounts = createValidCombinations(section, valueList, disallowList)

            // Clean up
            valueList = null
            disallowList = null
            rangeList = null
            fromSet = null
            toSet = null

            if (accounts) {
                render(view: 'preview', model: [accountInstanceList: accounts, chartSectionInstance: chartSectionRangeInstance.section])
            } else {
                flash.message = message(code: 'account.none', default: 'No accounts meet the creation criteria')
                render(view: 'bulk', model: [chartSectionRangeInstance: chartSectionRangeInstance, chartSectionList: createSectionList()])
            }
        } else {
            render(view: 'bulk', model: [chartSectionRangeInstance: chartSectionRangeInstance, chartSectionList: createSectionList()])
        }
    }

    def confirm() {
        def chartSectionInstance = ChartSection.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (chartSectionInstance) {

            // The following fixes what appears to be a bug in the new Grails 1.1 facility of auto-loading a list
            // of child domain classes directly from the params. It fails to clear a checkbox.
            def map = [:]
            def pos = 0;
            while (true) {
                if (params."accounts[${pos}].code") {
                    map.put(params."accounts[${pos}].code", (params."accounts[${pos}].active" ? true : false))
                    pos++
                } else {
                    break
                }
            }

            def dummyChartSection = new ChartSection(params)
            def valid = true
            def count = 0
            for (account in dummyChartSection.accounts) {
                account.active = map.get(account.code)    // Implement the fix above
                if (account.active) {
                    account.section = chartSectionInstance
                    account.currency = utilService.companyCurrency()
                    account.status = chartSectionInstance.status
                    if (validateAccountCode(account) && bookService.insertAccount(account)) {
                        count++
                    } else {
                        valid = false
                        break
                    }
                }
            }

            if (valid) {
                flash.message = message(code: 'account.success', args: [count, chartSectionInstance.name], default: "${count} new account(s) created in section ${chartSectionInstance.name}")
            } else {
                flash.message = message(code: 'account.failed', default: 'Unable to create the new accounts')
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'chartSection', params.id)
        }

        redirect(action: 'bulk')
    }

    def imports() {}

    def imported() {
        def valid = true
        def added = 0
        def ignored = 0
        def errors = 0
        def results = []
        def uploadFile = request.getFile('file')
        if (uploadFile.isEmpty()) {
            flash.message = message(code: 'account.empty', default: 'File is empty')
            valid = false
        } else {
            if (uploadFile.getSize() > 1024 * 1024) {
                flash.message = message(code: 'account.size', default: 'File exceeds the 1 MB limit')
                valid = false
            } else {
                def company = utilService.currentCompany()
                def sourceFile = utilService.tempFile('AC', 'txt')
                def fields, code, name, account
                try {
                    uploadFile.transferTo(sourceFile)
                    sourceFile.eachLine {
                        if (it.trim()) {
                            fields = it.split('\\t')*.trim()
                            code = bookService.fixCase(fields[0])
                            if (fields.size() > 1) {
                                name = fields[1]
                                if (name.length() > 87) name = name.substring(0, 87)
                            } else {
                                name = ''
                            }

                            account = bookService.getAccount(company, code, true, true)
                            if (account instanceof String) {
                                errors++
                                results << [code: code, name: name, text: account]
                            } else {
                                if (account.id) {
                                    ignored++
                                    results << [code: account.code, name: account.name, text: message(code: 'account.existing', default: 'Account already exists')]
                                } else {
                                    if (name) account.name = name
                                    if (bookService.insertAccount(account)) {
                                        added++
                                        results << [code: account.code, name: account.name, text: message(code: 'account.added', args: [account.section.name], default: "Account added to section ${account.section.name}")]
                                    } else {
                                        errors++
                                        results << [code: account.code, name: account.name, text: message(code: 'account.bad.save', default: 'Account could not be saved to the database')]
                                    }
                                }
                            }
                        }
                    }

                    try {
                        sourceFile.delete()
                    } catch (Exception e1) {}
                } catch (Exception ex) {
                    log.error(ex)
                    ex.printStackTrace()
                    flash.message = message(code: 'account.bad.upload', default: 'Unable to upload the file')
                    valid = false
                }
            }
        }

        if (valid) {
            flash.message = message(code: 'account.uploaded', args: [added.toString(), ignored.toString(), errors.toString()], default: "${added} account(s) added, ${ignored} skipped, ${errors} had errors")
            render(view: 'imported', model: [results: results])
        } else {
            render(view: 'imports')
        }
    }

    def test() {
        def accountInstance = new Account(params)
        return [accountInstance: accountInstance]
    }

    def testing() {
        def accountInstance
        if (params.code) {
            def expanded = bookService.expandAccountCode(utilService.currentUser(), params.code)
            if (expanded) {
                def result = bookService.getAccount(utilService.currentCompany(), expanded, true)
                if (result instanceof String) {
                    accountInstance = new Account()
                    accountInstance.code = expanded
                    accountInstance.errorMessage(field: 'code', code: '?', default: result)
                } else {
                    accountInstance = result
                    if (accountInstance.id) {
                        accountInstance.errorMessage(field: 'code', code: 'account.exists', args: [accountInstance.toString()], default: "Account ${accountInstance.toString()} already exists")
                    } else {
                        flash.message = message(code: 'account.auto', args: [accountInstance.toString()], default: "Account ${accountInstance.toString()} would have been created as shown below")
                        redirect(action: 'test', params: [code: accountInstance.code, name: accountInstance.name, 'section.id': accountInstance.section.id])
                        return
                    }
                }
            } else {
                accountInstance = new Account()
                accountInstance.code = params.code
            }
        } else {
            accountInstance = new Account()
        }

        render(view: 'test', model: [accountInstance: accountInstance])
    }

    def enquire() {
        params.max = utilService.max
        params.offset = utilService.offset
        def accountInstance = new Account()
        def balanceInstance = new GeneralBalance()
        def transactionInstanceList = []
        def transactionInstanceTotal = 0
        def currencyList = ExchangeCurrency.findAllByCompany(utilService.currentCompany(), [cache: true])
        def periodList = bookService.getUsedPeriods(utilService.currentCompany())
        def displayCurrency = (params.displayCurrency && params.displayCurrency != 'null') ? ExchangeCurrency.get(params.displayCurrency) : null
        def displayCurrencyClass = ''
        def displayPeriod = params.displayPeriod ? Period.get(params.displayPeriod) : bookService.selectPeriod(periodList)
        periodList = periodList.reverse()   // More intuitive to see it in reverse order
        if (params.code) {
            def expanded = bookService.expandAccountCode(utilService.currentUser(), params.code)
            if (expanded) {
                def result = bookService.getAccount(utilService.currentCompany(), expanded)
                if (result instanceof String) {
                    accountInstance = new Account()
                    accountInstance.code = expanded
                    accountInstance.errorMessage(field: 'code', code: '?', default: result)
                } else {
                    accountInstance = result
                }
            } else {
                accountInstance = new Account()
                accountInstance.code = params.code
                accountInstance.errorMessage(field: 'code', code: 'account.not.exists', default: 'Invalid GL account')
            }
        } else if (params.id) {
            accountInstance = Account.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
            if (!accountInstance) {
                accountInstance = new Account()
                accountInstance.errorMessage(field: 'code', code: 'account.not.exists', default: 'Invalid GL account')
            }
        }

        if (accountInstance.id) {
            if (bookService.hasAccountAccess(accountInstance)) {
                if (displayCurrency && displayCurrency.id != accountInstance.currency.id && displayCurrency.id != utilService.companyCurrency().id) displayCurrencyClass = 'conversion'
                balanceInstance = GeneralBalance.findByAccountAndPeriod(accountInstance, displayPeriod)
                transactionInstanceList = GeneralTransaction.findAll('from GeneralTransaction as x where x.balance = ? order by x.document.documentDate desc, x.document.id desc', [balanceInstance], [max: params.max, offset: params.offset])
                transactionInstanceTotal = GeneralTransaction.countByBalance(balanceInstance)
            } else {
                def code = accountInstance.code
                accountInstance = new Account()
                accountInstance.code = code
                accountInstance.errorMessage(field: 'code', code: 'account.not.exists', default: 'Invalid GL account')
            }
        }

        [accountInstance: accountInstance, balanceInstance: balanceInstance, transactionInstanceList: transactionInstanceList,
                    transactionInstanceTotal: transactionInstanceTotal, currencyList: currencyList, periodList: periodList,
                    displayCurrency: displayCurrency, displayCurrencyClass: displayCurrencyClass, displayPeriod: displayPeriod]
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    // Substitutes any mnemonics, adds in any defaults, corrects the case, creates a name if required and fills in the
    // element values. Returns true if the validation succeeds, false otherwise. This is for use when the section of
    // the account is known and it checks that the account code is valid per the section's ranges.
    private validateAccountCode(account) {

        if (!account.code) {
            account.errorMessage(field: 'code', code: 'account.code.missing', default: 'Account code is empty')
            return false
        }

        if (account.code.startsWith(bookService.SEGMENT_DELIMITER) ||
        account.code.endsWith(bookService.SEGMENT_DELIMITER) ||
        account.code.indexOf(bookService.SEGMENT_DELIMITER + bookService.SEGMENT_DELIMITER) >= 0) {
            account.errorMessage(field: 'code', code: 'account.code.malformed', default: 'Account code is malformed')
            return false
        }

        def code = bookService.expandAccountCode(utilService.currentUser(), account.code)
        if (!code) {
            account.errorMessage(field: 'code', code: 'account.code.bad.expand', default: 'Invalid mnemonic in account code')
            return false
        }

        if (code != account.code) account.code = code

        def accountSegments = code.split("\\${BookService.SEGMENT_DELIMITER}")
        def patternSegments = account.section.pattern.split("\\${BookService.SEGMENT_DELIMITER}")
        def effectiveSegments = (accountSegments.size() == patternSegments.size()) ?
                bookService.verifyAccountCode(patternSegments, accountSegments) :
                bookService.completeAccountCode(patternSegments, accountSegments)
        if (!effectiveSegments) {
            account.errorMessage(field: 'code', code: 'account.code.bad.verify', default: 'Unable to verify the account code')
            return false
        }

        def result = bookService.isInRange(account.section, effectiveSegments, false, account.autoCreateElementValues)

        // If we received an error message back
        if (result instanceof String) {
            account.errorMessage(field: 'code', code: '?', default: result)
            return false
        }

        // If the account number does not fit in this section
        if (!result) {
            account.errorMessage(field: 'code', code: 'account.bad.section', default: 'The account code is not valid for this section')
            return false
        }

        code = effectiveSegments.join(BookService.SEGMENT_DELIMITER)
        if (code != account.code) account.code = code

        def name = null
        for (int i = 0; i < effectiveSegments.size(); i++) {
            def element = account.section."segment${i + 1}"

            // Check the segment value actually exists
            def value = CodeElementValue.findByElementAndCode(element, effectiveSegments[i])
            if (!value) {
                if (account.autoCreateElementValues) {
                    value = new CodeElementValue(element: element, code: effectiveSegments[i], shortName: effectiveSegments[i], name: effectiveSegments[i])
                    if (!value.saveThis()) {
                        account.errorMessage(field: 'code', code: 'account.code.autoCreate', args: [i + 1, effectiveSegments[i], element.name],
                        "Code segment ${i + 1} with a value of ${effectiveSegments[i]} could not be created as a value for element ${element.name}")
                        return false
                    }
                } else {
                    account.errorMessage(field: 'code', code: 'account.code.not.exist', args: [account.section.name, i + 1, effectiveSegments[i], element.name],
                    "Error in section ${account.section.name}: Code segment ${i + 1} with a value of ${effectiveSegments[i]} does not exist as a value for element ${element.name}")
                    return false
                }
            }

            // Fill in the element values
            account."element${element.elementNumber}" = value

            // Create a name on the off chance we will need it
            if (name) {
                name = name + ' ' + value.shortName
            } else {
                name = value.shortName
            }
        }

        if (!account.name) account.name = name

        return true
    }

    private createSectionList() {
        return ChartSection.findAll('from ChartSection as x where x.company = ? and x.accountSegment > 0 and exists (select y.id from ChartSectionRange as y where y.section.id = x.id) order by x.name', [utilService.currentCompany()])
    }

    private createValidCombinations(section, valueList, disallowList) {
        def accounts = []
        appendAll(section, accounts, disallowList, 0, valueList)
        return accounts
    }

    private appendAll(section, accounts, disallowList, level, valueList, partialCode = null, partialName = null) {
        def lastLevel = (level == valueList.size() - 1)
        def currentList = valueList[level]
        def code, name
        for (int i = 0; i < currentList.size() && accounts.size() < BULK_LIMIT; i++) {

            if (level == 0) {
                code = currentList[i].code
                name = currentList[i].shortName
            } else {
                code = partialCode + BookService.SEGMENT_DELIMITER + currentList[i].code
                name = partialName + ' ' + currentList[i].shortName
            }

            if (lastLevel) {
                if (!isDisallowed(disallowList, code)) {
                    if (!Account.findBySectionAndCode(section, code)) {
                        accounts << new Account(code: code, name: name)
                    }
                }
            } else {
                appendAll(section, accounts, disallowList, level + 1, valueList, code, name)
            }
        }
    }

    private isDisallowed(disallowList, code) {
        if (disallowList) {
            def codeSet = code.split("\\${BookService.SEGMENT_DELIMITER}")
            for (d in disallowList) {
                def fromSet = d[0].split("\\${BookService.SEGMENT_DELIMITER}")
                def toSet = d[1].split("\\${BookService.SEGMENT_DELIMITER}")
                def disallowed = false
                for (int i = 0; i < codeSet.size(); i++) {
                    if (fromSet[i] == '*' && toSet[i] == '*') continue
                        if ((fromSet[i] == '*' || fromSet[i] <= codeSet[i]) && (toSet[i] == '*' || toSet[i] >= codeSet[i])) {
                            disallowed = true
                        } else {
                            disallowed = false
                            break
                        }
                }

                if (disallowed) return true
            }
        }

        return false
    }
}