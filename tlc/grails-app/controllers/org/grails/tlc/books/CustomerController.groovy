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
import org.grails.tlc.corp.TaxCode
import java.text.DateFormat
import java.text.ParseException

class CustomerController {

    // Injected services
    def utilService
    def bookService
    def postingService

    // Security settings
    def activities = [default: 'aradmin', enquire: 'enquire', allocations: 'enquire', print: 'arreport', printing: 'arreport', aged: 'arreport', ageing: 'arreport',
            statements: 'arreport', statement: 'arreport', statementEnquiry: 'enquire', statementPrint: 'enquire', reprint: 'arreport', reprinting: 'arreport']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', importing: 'POST', auto: 'POST', allocating: 'POST', printing: 'POST',
            ageing: 'POST', statement: 'POST', reprinting: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'name', 'accountCreditLimit', 'accountCurrentBalance', 'settlementDays',
                'periodicSettlement', 'taxId', 'active', 'revaluationMethod'].contains(params.sort) ? params.sort : 'code'

        // Note that the selectList and selectCount calls below are inherently limited to the current
        // company since the list of accessCode objects is company (and user) specific
        def customerInstanceList, customerInstanceTotal
        def accessList = bookService.customerAccessCodes()
        if (accessList) {
            def where
            for (item in accessList) {
                if (where) {
                    where = where + ',?'
                } else {
                    where = 'x.accessCode in (?'
                }
            }

            where = where + ')'
            customerInstanceList = Customer.selectList(where: where, params: accessList)
            customerInstanceTotal = Customer.selectCount()
        } else {
            customerInstanceList = []
            customerInstanceTotal = 0L
        }

        [customerInstanceList: customerInstanceList, customerInstanceTotal: customerInstanceTotal]
    }

    def show() {
        def customerInstance = Customer.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!customerInstance || !bookService.hasCustomerAccess(customerInstance)) {
            flash.message = utilService.standardMessage('not.found', 'customer', params.id)
            redirect(action: 'list')
        } else {
            return [customerInstance: customerInstance, hasTransactions: customerInstance.hasTransactions()]
        }
    }

    def delete() {
        def customerInstance = Customer.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (customerInstance && bookService.hasCustomerAccess(customerInstance)) {
            try {
                bookService.deleteCustomer(customerInstance)
                flash.message = utilService.standardMessage('deleted', customerInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', customerInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'customer', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def company = utilService.currentCompany()
        def customerInstance = Customer.findByIdAndSecurityCode(params.id, company.securityCode)
        if (!customerInstance || !bookService.hasCustomerAccess(customerInstance)) {
            flash.message = utilService.standardMessage('not.found', 'customer', params.id)
            redirect(action: 'list')
        } else {
            return [customerInstance: customerInstance, currencyList: ExchangeCurrency.findAllByCompany(company, [cache: true]),
                    taxList: TaxCode.findAllByCompany(company, [cache: true]), accessList: bookService.customerAccessCodes(),
                    hasTransactions: customerInstance.hasTransactions(), companyCurrencyId: utilService.companyCurrency().id]
        }
    }

    def update(Long version) {
        def company = utilService.currentCompany()
        def customerInstance = Customer.findByIdAndSecurityCode(params.id, company.securityCode)
        if (customerInstance && bookService.hasCustomerAccess(customerInstance)) {
            def hasTransactions = customerInstance.hasTransactions()
            if (version != null && customerInstance.version > version) {
                customerInstance.errorMessage(code: 'locking.failure', domain: 'customer')
                render(view: 'edit', model: [customerInstance: customerInstance, currencyList: ExchangeCurrency.findAllByCompany(company, [cache: true]),
                        taxList: TaxCode.findAllByCompany(company, [cache: true]), accessList: bookService.customerAccessCodes(),
                        hasTransactions: hasTransactions, companyCurrencyId: utilService.companyCurrency().id])
                return
            }

            def oldCurrencyId = customerInstance.currency.id
            customerInstance.properties['taxCode', 'code', 'name', 'accountCreditLimit', 'accessCode', 'country', 'currency', 'taxId',
                    'settlementDays', 'periodicSettlement', 'active', 'revaluationMethod'] = params
            utilService.verify(customerInstance, ['currency', 'taxCode', 'accessCode'])             // Ensure correct references
            def valid = (!customerInstance.hasErrors() && bookService.hasCustomerAccess(customerInstance))
            if (valid && oldCurrencyId != customerInstance.currency.id && hasTransactions) {
                customerInstance.errorMessage(field: 'currency', code: 'customer.currency.change', default: 'You may not change the currency of a customer once transactions have been posted to the account')
                valid = false
            }

            if (valid) valid = customerInstance.saveThis()
            if (valid) {
                flash.message = utilService.standardMessage('updated', customerInstance)
                redirect(action: 'show', id: customerInstance.id)
            } else {
                render(view: 'edit', model: [customerInstance: customerInstance, currencyList: ExchangeCurrency.findAllByCompany(company, [cache: true]),
                        taxList: TaxCode.findAllByCompany(company, [cache: true]), accessList: bookService.customerAccessCodes(),
                        hasTransactions: hasTransactions, companyCurrencyId: utilService.companyCurrency().id])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'customer', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def customerInstance = new Customer()
        def company = utilService.currentCompany()
        customerInstance.company = company   // Ensure correct company
        customerInstance.country = company.country
        customerInstance.currency = utilService.companyCurrency()
        customerInstance.taxCode = utilService.companyTaxCode()
        customerInstance.settlementDays = utilService.setting('customer.settlement.days', 30)
        customerInstance.periodicSettlement = utilService.setting('customer.settlement.periodic', false)
        return [customerInstance: customerInstance, currencyList: ExchangeCurrency.findAllByCompany(company, [cache: true]),
                taxList: TaxCode.findAllByCompany(company, [cache: true]), accessList: bookService.customerAccessCodes(), companyCurrencyId: utilService.companyCurrency().id]
    }

    def save() {
        def customerInstance = new Customer()
        def company = utilService.currentCompany()
        customerInstance.properties['taxCode', 'code', 'name', 'accountCreditLimit', 'accessCode', 'country', 'currency', 'taxId', 'settlementDays',
                'periodicSettlement', 'active', 'revaluationMethod'] = params
        customerInstance.company = company   // Ensure correct company
        utilService.verify(customerInstance, ['currency', 'taxCode', 'accessCode'])             // Ensure correct references
        if (!customerInstance.hasErrors() && bookService.hasCustomerAccess(customerInstance) && bookService.insertCustomer(customerInstance)) {
            flash.message = utilService.standardMessage('created', customerInstance)
            redirect(controller: 'customerAddress', action: 'initial', id: customerInstance.id)
        } else {
            render(view: 'create', model: [customerInstance: customerInstance, currencyList: ExchangeCurrency.findAllByCompany(company, [cache: true]),
                    taxList: TaxCode.findAllByCompany(company, [cache: true]), accessList: bookService.customerAccessCodes(), companyCurrencyId: utilService.companyCurrency().id])
        }
    }

    def imports() {
        [customerInstance: new Customer(), accessList: bookService.customerAccessCodes()]
    }

    def importing() {
        def customerInstance = new Customer()
        def company = utilService.currentCompany()
        customerInstance.accessCode = AccessCode.findByIdAndCompany(params.accessCode?.id?.toLong(), company)
        def valid = true
        def added = 0
        def ignored = 0
        def errors = 0
        def uploadFile = request.getFile('file')
        if (!bookService.hasCustomerAccessCode(customerInstance.accessCode?.code)) {
            customerInstance.errorMessage(code: 'customer.import.invalid', default: 'Invalid access code')
            valid = false
        } else {
            if (uploadFile.isEmpty()) {
                customerInstance.errorMessage(code: 'customer.empty', default: 'File is empty')
                valid = false
            } else {
                if (uploadFile.getSize() > 1024 * 1024) {
                    customerInstance.errorMessage(code: 'customer.size', default: 'File exceeds the 1 MB limit')
                    valid = false
                } else {
                    def sourceFile = utilService.tempFile('AR', 'txt')
                    def currency = utilService.companyCurrency()
                    def taxCode = utilService.companyTaxCode()
                    def fields, code, name, rec
                    try {
                        uploadFile.transferTo(sourceFile)
                        sourceFile.eachLine {
                            if (it.trim()) {
                                fields = it.split('\\t')*.trim()
                                code = bookService.fixCustomerCase(fields[0])
                                if (fields.size() >= 2) {
                                    if (code.length() <= 20) {
                                        rec = Customer.findByCompanyAndCode(company, code)
                                        if (rec) {
                                            ignored++
                                        } else {
                                            rec = new Customer()
                                            name = fields[1]
                                            if (name.length() > 50) name = name.substring(0, 50)
                                            rec.company = company
                                            rec.code = code
                                            rec.name = name
                                            rec.accessCode = customerInstance.accessCode
                                            rec.country = company.country
                                            rec.currency = currency
                                            rec.taxCode = taxCode
                                            rec.settlementDays = utilService.setting('customer.settlement.days', 30)
                                            rec.periodicSettlement = utilService.setting('customer.settlement.periodic', false)

                                            if (!rec.hasErrors() && bookService.insertCustomer(rec)) {
                                                added++
                                            } else {
                                                errors++
                                            }
                                        }
                                    } else {
                                        errors++
                                    }
                                } else {
                                    errors++
                                }
                            }
                        }

                        try {
                            sourceFile.delete()
                        } catch (Exception e1) {}
                    } catch (Exception ex) {
                        log.error(ex)
                        customerInstance.errorMessage(code: 'customer.bad.upload', default: 'Unable to upload the file')
                        valid = false
                    }
                }
            }
        }

        if (valid) {
            flash.message = message(code: 'customer.uploaded', args: [added.toString(), ignored.toString(), errors.toString()], default: "${added} code(s) added, ${ignored} skipped, ${errors} had errors")
            redirect(action: 'list')
        } else {
            render(view: 'imports', model: [customerInstance: customerInstance, accessList: bookService.customerAccessCodes()])
        }
    }

    def enquire() {
        params.max = utilService.max
        params.offset = utilService.offset
        def customerInstance = new Customer()
        def transactionInstanceList = []
        def transactionInstanceTotal = 0
        def statementCount = 0
        def currencyList = ExchangeCurrency.findAllByCompany(utilService.currentCompany(), [cache: true])
        def periodList = bookService.getUsedPeriods(utilService.currentCompany())
        def displayCurrency = (params.displayCurrency && params.displayCurrency != 'null') ? ExchangeCurrency.get(params.displayCurrency) : null
        def displayCurrencyClass = ''
        def displayPeriod = (params.displayPeriod && params.displayPeriod != 'null') ? Period.get(params.displayPeriod) : null
        def turnoverList = []
        def displayTurnover
        if (params.code) {
            def code = bookService.fixCustomerCase(params.code)
            customerInstance = Customer.findByCompanyAndCode(utilService.currentCompany(), code)
            if (!customerInstance) {
                customerInstance = new Customer()
                customerInstance.code = code
                customerInstance.errorMessage(field: 'code', code: 'document.customer.invalid', default: 'Invalid customer')
            }
        } else if (params.id) {
            customerInstance = Customer.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
            if (!customerInstance) {
                customerInstance = new Customer()
                customerInstance.errorMessage(field: 'code', code: 'document.customer.invalid', default: 'Invalid customer')
            }
        }

        if (customerInstance.id) {
            if (bookService.hasCustomerAccess(customerInstance)) {
                def parameters = [customerInstance]
                def sql = 'from GeneralTransaction as x where x.customer = ? and '
                if (displayPeriod) {
                    sql += 'x.document.period = ?'
                    parameters << displayPeriod
                } else {
                    sql += 'x.accountUnallocated != 0'
                }

                // Don't display documents with a zero account value (e.g. automatic foreign exchange differences) when viewing in account currency
                if (!displayCurrency || displayCurrency.id == customerInstance.currency.id) sql += ' and x.accountValue != 0'

                transactionInstanceList = GeneralTransaction.findAll(sql + ' order by x.document.documentDate desc, x.document.id desc', parameters, [max: params.max, offset: params.offset])
                transactionInstanceTotal = GeneralTransaction.executeQuery('select count(*) ' + sql, parameters)[0]
                def turnovers = CustomerTurnover.findAll("from CustomerTurnover as t where t.customer = ? and t.period.status != ? order by t.period.validFrom desc", [customerInstance, 'new'])
                def val
                parameters = [context: customerInstance, field: 'turnover', currency: displayCurrency]
                for (turnover in turnovers) {
                    parameters.turnover = turnover
                    val = bookService.getBookValue(parameters)
                    if (val != null && !(val instanceof String)) {
                        if (val >= 0.0) {
                            val = utilService.format(val, parameters.scale)
                        } else {
                            val = utilService.format(-val, parameters.scale) + ' ' + message(code: 'generic.cr', default: 'Cr')
                        }
                    }

                    turnoverList << [id: turnover.period.id, data: turnover.period.code + ': ' + val]
                }
                displayTurnover = displayPeriod ?: bookService.selectPeriod(periodList)
                if (displayCurrency && displayCurrency.id != customerInstance.currency.id && displayCurrency.id != utilService.companyCurrency().id) displayCurrencyClass = 'conversion'
                statementCount = Statement.countByCustomer(customerInstance)
            } else {
                def code = customerInstance.code
                customerInstance = new Customer()
                customerInstance.code = code
                customerInstance.errorMessage(field: 'code', code: 'document.customer.invalid', default: 'Invalid customer')
            }
        }

        periodList = periodList.reverse()   // More intuitive to see it in reverse order

        [customerInstance: customerInstance, transactionInstanceList: transactionInstanceList, turnoverList: turnoverList,
                transactionInstanceTotal: transactionInstanceTotal, currencyList: currencyList, periodList: periodList,
                displayCurrency: displayCurrency, displayCurrencyClass: displayCurrencyClass, displayPeriod: displayPeriod,
                displayTurnover: displayTurnover, statementCount: statementCount]
    }

    def allocations() {
        def line = GeneralTransaction.get(params.id)
        def currencyList = ExchangeCurrency.findAllByCompany(utilService.currentCompany(), [cache: true])
        def displayCurrency = (params.displayCurrency && params.displayCurrency != 'null') ? ExchangeCurrency.get(params.displayCurrency) : null
        def displayCurrencyClass = ''
        def allocationInstanceList = []
        def totalInstance
        if (line && line.securityCode == utilService.currentCompany().securityCode && utilService.permitted(line.document.type.type.activity.code)) {
            def decs
            def account = line.customer
            def companyCurrency = utilService.companyCurrency()
            if (displayCurrency && displayCurrency.id != account.currency.id && displayCurrency.id != companyCurrency.id) {
                displayCurrencyClass = 'conversion'
                decs = displayCurrency.decimals
            } else {
                decs = account.currency.decimals
            }

            def value = bookService.getBookValue(context: account, line: line, field: 'value', currency: displayCurrency)
            def debit, credit
            def debitTotal = 0.0
            def creditTotal = 0.0
            if (value == null || value instanceof String) {
                debit = value ?: ''
                credit = ''
            } else if (value < 0.0) {
                debit = ''
                credit = utilService.format(-value, decs)
                creditTotal -= value
            } else {
                debit = utilService.format(value, decs)
                credit = ''
                debitTotal += value
            }

            allocationInstanceList << [target: line.document, code: line.document.type.code + line.document.code, debit: debit, credit: credit]

            for (alloc in line.allocations) {

                // Don't display allocations with a zero account value (e.g. automatic foreign exchange differences) when viewing in account currency
                if (!alloc.accountValue && (!displayCurrency || displayCurrency.id == account.currency.id)) continue
                value = bookService.getBookValue(context: account, allocation: alloc, field: 'value', currency: displayCurrency)
                if (value == null || value instanceof String) {
                    debit = value ?: ''
                    credit = ''
                } else if (value < 0.0) {
                    debit = ''
                    credit = utilService.format(-value, decs)
                    creditTotal -= value
                } else {
                    debit = utilService.format(value, decs)
                    credit = ''
                    debitTotal += value
                }

                allocationInstanceList << [target: alloc, code: alloc.targetType.code + alloc.targetCode, debit: debit, credit: credit]
            }

            if (line.accountUnallocated) {
                value = bookService.getBookValue(context: account, line: line, field: 'unallocated', currency: displayCurrency, negate: true)
                if (value < 0.0) {
                    debit = ''
                    credit = utilService.format(-value, decs)
                    creditTotal -= value
                } else {
                    debit = utilService.format(value, decs)
                    credit = ''
                    debitTotal += value
                }

                allocationInstanceList << [target: null, code: message(code: 'generalTransaction.accountUnallocated.label', default: 'Unallocated'), debit: debit, credit: credit]
            }

            if (debitTotal != creditTotal) {
                value = creditTotal - debitTotal
                if (value < 0.0) {
                    debit = ''
                    credit = utilService.format(-value, decs)
                    creditTotal -= value
                } else {
                    debit = utilService.format(value, decs)
                    credit = ''
                    debitTotal += value
                }

                allocationInstanceList << [target: null, code: message(code: 'generic.difference', default: 'Difference'), debit: debit, credit: credit]
            }

            debit = utilService.format(debitTotal, decs)
            credit = utilService.format(creditTotal, decs)
            totalInstance = [code: message(code: 'document.sourceTotals', default: 'Totals'), debit: debit, credit: credit]
        } else {
            line = new GeneralTransaction()
            line.errorMessage(code: 'document.invalid', default: 'Invalid document')
        }

        [lineInstance: line, allocationInstanceList: allocationInstanceList, totalInstance: totalInstance, currencyList: currencyList,
                displayCurrency: displayCurrency, displayCurrencyClass: displayCurrencyClass]
    }

    def transactions() {

        // The following line is required since there are two buttons on the form in the GSP and Grails currently
        // leaves the 'action' parameter set to index rather than the actual destination action (this). This upsets
        // the reSource method which thinks we're executing the index action.
        if (params.action != 'transactions') params.action = 'transactions'
        def customerInstance = utilService.source('customer.list')
        if (!customerInstance || !bookService.hasCustomerAccess(customerInstance)) {
            flash.message = message(code: 'document.customer.invalid', default: 'Invalid customer')
            redirect(action: 'list')
            return
        }

        params.max = utilService.max
        params.offset = utilService.offset
        def transactionInstanceList = []
        def transactionInstanceTotal = 0
        def periodList = bookService.getUsedPeriods(utilService.currentCompany())
        periodList = periodList.reverse()   // More intuitive to see it in reverse order
        def displayPeriod = (params.displayPeriod && params.displayPeriod != 'null') ? Period.get(params.displayPeriod) : null
        def parameters = [customerInstance]
        def sql = 'from GeneralTransaction as x where x.customer = ? and '
        if (displayPeriod) {
            sql += 'x.document.period = ?'
            parameters << displayPeriod
        } else {
            sql += 'x.accountUnallocated != 0'
        }

        transactionInstanceList = GeneralTransaction.findAll(sql + ' order by x.document.documentDate desc, x.document.id desc', parameters, [max: params.max, offset: params.offset])
        transactionInstanceTotal = GeneralTransaction.executeQuery('select count(*) ' + sql, parameters)[0]

        [customerInstance: customerInstance, transactionInstanceList: transactionInstanceList, transactionInstanceTotal: transactionInstanceTotal,
                periodList: periodList, displayPeriod: displayPeriod]
    }

    def auto() {
        def customerInstance = utilService.reSource('customer.list', [origin: 'transactions'])
        if (!customerInstance || !bookService.hasCustomerAccess(customerInstance)) {
            flash.message = message(code: 'document.customer.invalid', default: 'Invalid customer')
            redirect(action: 'list')
            return
        }

        if (postingService.autoAllocate(customerInstance)) {
            flash.message = message(code: 'document.good.auto', default: 'Auto allocation completed')
        } else {
            flash.message = message(code: 'document.not.allocated', default: 'Auto allocation failed')
        }

        redirect(action: 'transactions')
    }

    def allocate() {

        // The following line is required since there are two buttons on the form in the GSP and Grails currently
        // leaves the 'action' parameter set to index rather than the actual destination action (this). This upsets
        // the reSource method which thinks we're executing the index action.
        if (params.action != 'allocate') params.action = 'allocate'
        def transactionPeriod = params.transactionPeriod    // The caller's display period
        def lineInstance = utilService.source('customer.transactions')
        if (!lineInstance || lineInstance.securityCode != utilService.currentCompany().securityCode || !bookService.hasCustomerAccess(lineInstance.customer)) {
            flash.message = message(code: 'document.no.manual', default: 'Unable to perform manual allocation')
            redirect(action: 'transactions', params: [displayPeriod: transactionPeriod])
            return
        }

        params.max = utilService.max
        params.offset = utilService.offset
        def customerInstance = lineInstance.customer
        def allocationInstance = new Allocation()
        allocationInstance.properties['targetType', 'targetCode', 'accountValue', 'accountDifference'] = params
        utilService.verify(allocationInstance, 'targetType')
        if (!allocationInstance.accountValue) allocationInstance.accountValue = (lineInstance.accountValue < 0.0) ? -lineInstance.accountUnallocated : lineInstance.accountUnallocated
        def periodList = bookService.getUsedPeriods(utilService.currentCompany())
        periodList = periodList.reverse()    // More intuitive to see it in reverse order
        def displayPeriod = (params.displayPeriod && params.displayPeriod != 'null') ? Period.get(params.displayPeriod) : null
        def parameters = [customerInstance, lineInstance.id]
        def sql = 'from GeneralTransaction as x where x.customer = ? and x.id != ? and x.accountValue != 0 and '
        if (displayPeriod) {
            sql += 'x.document.period = ?'
            parameters << displayPeriod
        } else {
            sql += 'x.accountUnallocated != 0'
        }

        def transactionInstanceList = GeneralTransaction.findAll(sql + ' order by x.document.documentDate desc, x.document.id desc', parameters, [max: params.max, offset: params.offset])
        def transactionInstanceTotal = GeneralTransaction.executeQuery('select count(*) ' + sql, parameters)[0]
        def documentTypeList = DocumentType.findAll("from DocumentType as x where x.company = ? and x.type.customerAllocate = ?", [utilService.currentCompany(), true])

        [customerInstance: customerInstance, lineInstance: lineInstance, allocationInstance: allocationInstance, transactionInstanceList: transactionInstanceList,
                documentTypeList: documentTypeList, transactionInstanceTotal: transactionInstanceTotal, periodList: periodList, displayPeriod: displayPeriod,
                allowDifference: true, transactionPeriod: transactionPeriod]
    }

    def allocating() {

        // The following line is required since there are two buttons on the form in the GSP and Grails currently
        // leaves the 'action' parameter set to index rather than the actual destination action (this). This upsets
        // the reSource method which thinks we're executing the index action. We also cheat by saying we are the
        // allocate method so that we get the correct drilldown return values.
        if (params.action != 'allocate') params.action = 'allocate'
        def transactionPeriod = params.transactionPeriod    // The caller's display period
        def lineInstance = utilService.reSource('customer.transactions', [origin: 'allocate'])
        if (!lineInstance || lineInstance.securityCode != utilService.currentCompany().securityCode || !bookService.hasCustomerAccess(lineInstance.customer)) {
            flash.message = message(code: 'document.no.manual', default: 'Unable to perform manual allocation')
            redirect(action: 'transactions', params: [displayPeriod: transactionPeriod])
            return
        }

        def allocationInstance = new Allocation()
        allocationInstance.properties['targetType', 'targetCode', 'accountValue', 'accountDifference'] = params
        def valid = !allocationInstance.hasErrors()
        if (valid) {
            utilService.verify(allocationInstance, 'targetType')
            if (!allocationInstance.targetType) {
                allocationInstance.errorMessage(field: 'targetType', code: 'document.bad.type', default: 'Invalid document type')
                valid = false
            }
        }

        if (valid && postingService.allocateLine(lineInstance, allocationInstance)) {
            if (lineInstance.accountUnallocated) {
                redirect(action: 'allocate', params: [transactionPeriod: transactionPeriod])
            } else {
                redirect(action: 'transactions', params: [displayPeriod: transactionPeriod])
            }
        } else {    // Error message will have been set on the allocation object
            params.max = utilService.max
            params.offset = utilService.offset
            def customerInstance = lineInstance.customer
            def periodList = bookService.getUsedPeriods(utilService.currentCompany())
            periodList = periodList.reverse()    // More intuitive to see it in reverse order
            def displayPeriod = (params.displayPeriod && params.displayPeriod != 'null') ? Period.get(params.displayPeriod) : null
            def parameters = [customerInstance, lineInstance.id]
            def sql = 'from GeneralTransaction as x where x.customer = ? and x.id != ? and x.accountValue != 0 and '
            if (displayPeriod) {
                sql += 'x.document.period = ?'
                parameters << displayPeriod
            } else {
                sql += 'x.accountUnallocated != 0'
            }

            def transactionInstanceList = GeneralTransaction.findAll(sql + ' order by x.document.documentDate desc, x.document.id desc', parameters, [max: params.max, offset: params.offset])
            def transactionInstanceTotal = GeneralTransaction.executeQuery('select count(*) ' + sql, parameters)[0]
            def documentTypeList = DocumentType.findAll("from DocumentType as x where x.company = ? and x.type.customerAllocate = ?", [utilService.currentCompany(), true])

            render(view: 'allocate', model: [customerInstance: customerInstance, lineInstance: lineInstance, allocationInstance: allocationInstance,
                    transactionInstanceList: transactionInstanceList, documentTypeList: documentTypeList, transactionInstanceTotal: transactionInstanceTotal,
                    periodList: periodList, displayPeriod: displayPeriod, allowDifference: true, transactionPeriod: transactionPeriod])
        }
    }

    def print() {
        def customerInstance = new Customer()
        def accessCodeList = bookService.customerAccessCodes()
        [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: []]
    }

    def printing() {
        def customerInstance = new Customer(params)
        def accessCodeList = bookService.customerAccessCodes()
        if (!accessCodeList) {
            redirect(action: 'print')
            return
        }

        def codes = []
        if (params.codes) codes = params.codes instanceof String ? [params.codes.toLong()] : params.codes*.toLong() as List
        def selectedCodes = accessCodeList.findAll {codes.contains(it.id)}
        if (selectedCodes.size() == accessCodeList.size()) {
            selectedCodes.clear()
        } else if (selectedCodes.size() > 18) {
            customerInstance.errorMessage(field: 'accessCode', code: 'report.accessCode.limit', default: 'There is a limit of 18 access codes that can be selected in one report')
            render(view: 'print', model: [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes])
            return
        }

        params.p_codes = selectedCodes.join(',')
        params.p_active = customerInstance.active.toString()
        def result = utilService.demandRunFromParams('customers', params)
        if (result instanceof String) {
            customerInstance.errors.reject(null, result)
            render(view: 'print', model: [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes])
        } else {
            flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            redirect(controller: 'systemMenu', action: 'display')
        }
    }

    def aged() {
        def customerInstance = new Customer()
        def accessCodeList = bookService.customerAccessCodes()
        [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: []]
    }

    def ageing() {
        def customerInstance = new Customer()
        def accessCodeList = bookService.customerAccessCodes()
        if (!accessCodeList) {
            redirect(action: 'aged')
            return
        }

        def codes = []
        if (params.codes) codes = params.codes instanceof String ? [params.codes.toLong()] : params.codes*.toLong() as List
        def selectedCodes = accessCodeList.findAll {codes.contains(it.id)}
        if (selectedCodes.size() == accessCodeList.size()) {
            selectedCodes.clear()
        } else if (selectedCodes.size() > 18) {
            customerInstance.errorMessage(field: 'accessCode', code: 'report.accessCode.limit', default: 'There is a limit of 18 access codes that can be selected in one report')
            render(view: 'aged', model: [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes])
            return
        }

        params.p_codes = selectedCodes.join(',')
        def result = utilService.demandRunFromParams('agedDebt', params)
        if (result instanceof String) {
            customerInstance.errors.reject(null, result)
            render(view: 'aged', model: [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes])
        } else {
            flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            redirect(controller: 'systemMenu', action: 'display')
        }
    }

    def statements() {
        def customerInstance = new Customer()
        def accessCodeList = bookService.customerAccessCodes()
        [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: [], statementDate: '']
    }

    def statement() {
        def customerInstance = new Customer()
        def accessCodeList = bookService.customerAccessCodes()
        if (!accessCodeList) {
            redirect(action: 'statements')
            return
        }

        def valid = true
        def codes = []
        if (params.codes) codes = params.codes instanceof String ? [params.codes.toLong()] : params.codes*.toLong() as List
        def selectedCodes = accessCodeList.findAll {codes.contains(it.id)}
        if (selectedCodes.size() == accessCodeList.size()) {
            selectedCodes.clear()
        } else if (selectedCodes.size() > 18) {
            customerInstance.errorMessage(field: 'accessCode', code: 'report.accessCode.limit', default: 'There is a limit of 18 access codes that can be selected in one report')
            valid = false
        }

        def batchSize = params.taxId
        customerInstance.taxId = batchSize
        if (batchSize != null && batchSize != '' && (!batchSize.isInteger() || batchSize.toInteger() <= 0 || batchSize.toInteger() > 99999)) {
            customerInstance.errorMessage(field: 'taxId', code: 'customer.statement.batchSize.bad', default: 'Invalid Batch Size')
            valid = false
        }

        def customerId
        if (params.code) {
            customerInstance.code = bookService.fixCustomerCase(params.code)
            def specificCustomer = Customer.findByCompanyAndCode(utilService.currentCompany(), customerInstance.code)
            if (bookService.hasCustomerAccess(specificCustomer)) {
                customerId = specificCustomer.id.toString()
            } else {
                customerInstance.errorMessage(field: 'code', code: 'document.customer.invalid', default: 'Invalid customer')
                valid = false
            }
        }

        def fmt = DateFormat.getDateInstance(DateFormat.SHORT, utilService.currentLocale())
        def statementDate = params.statementDate
        if (statementDate) {
            def tmp
            try {
                tmp = fmt.parse(statementDate)
                def today = utilService.fixDate()
                if (tmp < today - 30 || tmp > today + 1) {
                    customerInstance.errorMessage(field: 'dateCreated', code: 'customer.statement.date.range', args: [fmt.format(today - 30), fmt.format(today + 1)],
                            default: "Statement Date must be between ${fmt.format(today - 30)} and ${fmt.format(today + 1)}")
                    valid = false
                } else {
                    statementDate = tmp
                }
            } catch (ParseException pe) {
                customerInstance.errorMessage(field: 'dateCreated', code: 'customer.statement.date.bad', default: 'Invalid Statement Date')
                valid = false
            }
        } else {
            statementDate = ''
        }

        if (!valid) {
            render(view: 'statements', model: [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes, statementDate: statementDate])
            return
        }

        params.p_codes = selectedCodes.join(',')
        if (batchSize) params.p_batchSize = batchSize
        if (statementDate) params.p_stmtDate = fmt.format(statementDate)
        if (customerId) params.p_customer = customerId
        def result = utilService.demandRunFromParams('statements', params)
        if (result instanceof String) {
            customerInstance.errors.reject(null, result)
            render(view: 'statements', model: [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes])
        } else {
            flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            redirect(controller: 'systemMenu', action: 'display')
        }
    }

    def statementEnquiry() {
        params.max = utilService.max
        params.offset = utilService.offset
        def displayCurrency = (params.displayCurrency && params.displayCurrency != 'null') ? ExchangeCurrency.get(params.displayCurrency) : null
        def displayPeriod = (params.displayPeriod && params.displayPeriod != 'null') ? Period.get(params.displayPeriod) : null
        def statementList, statementInstance, statementLineInstanceList, statementLineInstanceTotal, locale
        def due = 0.0
        def overdue = 0.0
        def customerInstance = Customer.get(params.customerId)
        if (bookService.hasCustomerAccess(customerInstance)) {
            locale = new Locale(customerInstance.country.language.code, customerInstance.country.code)
            def statements = Statement.findAllByCustomer(customerInstance, [sort: 'statementDate', order: 'desc'])
            if (statements) {
                statementList = []
                statementInstance = Statement.get(params.statement) ?: statements[0]
                if (!params.priorStatement || params.priorStatement.toLong() != statementInstance.id) params.offset = 0
                for (stmt in statements) statementList << [id: stmt.id, statementDate: utilService.format(stmt.statementDate, 1, null, locale)]
                statementLineInstanceList = StatementLine.findAll('from StatementLine as x where x.statement = ? order by x.currentStatement, x.documentDate, x.sequencer',
                        [statementInstance], [max: params.max, offset: params.offset])
                for (line in statementLineInstanceList) {
                    line.source = GeneralTransaction.get(line.sequencer)
                    if (line.closingUnallocated) {
                        if (line.dueDate < statementInstance.statementDate) {
                            overdue += line.closingUnallocated
                        } else {
                            due += line.closingUnallocated
                        }
                    }
                }

                statementLineInstanceTotal = StatementLine.countByStatement(statementInstance)
            }
        } else {
            customerInstance = new Customer()
            customerInstance.errorMessage(code: 'document.customer.invalid', default: 'Invalid customer')
        }

        [customerInstance: customerInstance, statementList: statementList, statementInstance: statementInstance, statementLineInstanceList: statementLineInstanceList,
                statementLineInstanceTotal: statementLineInstanceTotal, displayPeriod: displayPeriod, displayCurrency: displayCurrency, due: due, overdue: overdue, locale: locale]
    }

    def statementPrint() {
        def customerInstance = Customer.get(params.customerId)
        if (bookService.hasCustomerAccess(customerInstance)) {
            def statementInstance = Statement.get(params.statement)
            if (statementInstance) {
                def fmt = DateFormat.getDateInstance(DateFormat.SHORT, utilService.currentLocale())
                params.p_customer = customerInstance.id.toString()
                params.p_stmtDate = fmt.format(statementInstance.statementDate)
                def result = utilService.demandRunFromParams('csReprint', params)
                if (result instanceof String) {
                    flash.message = result
                } else {
                    flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
                }
            } else {
                flash.message = message(code: 'customer.statement.not.found', args: [params.statement], default: "Statement not found with id ${params.statement}")
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'customer', params.customerId)
        }

        redirect(action: 'statementEnquiry', params: params)
    }

    def reprint() {
        def customerInstance = new Customer()
        def accessCodeList = bookService.customerAccessCodes()
        [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: [], statementDate: '']
    }

    def reprinting() {
        def customerInstance = new Customer()
        def accessCodeList = bookService.customerAccessCodes()
        if (!accessCodeList) {
            redirect(action: 'reprint')
            return
        }

        def valid = true
        def codes = []
        if (params.codes) codes = params.codes instanceof String ? [params.codes.toLong()] : params.codes*.toLong() as List
        def selectedCodes = accessCodeList.findAll {codes.contains(it.id)}
        if (selectedCodes.size() == accessCodeList.size()) {
            selectedCodes.clear()
        } else if (selectedCodes.size() > 18) {
            customerInstance.errorMessage(field: 'accessCode', code: 'report.accessCode.limit', default: 'There is a limit of 18 access codes that can be selected in one report')
            valid = false
        }

        def batchSize = params.taxId
        customerInstance.taxId = batchSize
        if (batchSize != null && batchSize != '' && (!batchSize.isInteger() || batchSize.toInteger() <= 0 || batchSize.toInteger() > 99999)) {
            customerInstance.errorMessage(field: 'taxId', code: 'customer.statement.batchSize.bad', default: 'Invalid Batch Size')
            valid = false
        }

        def customerId
        if (params.code) {
            customerInstance.code = bookService.fixCustomerCase(params.code)
            def specificCustomer = Customer.findByCompanyAndCode(utilService.currentCompany(), customerInstance.code)
            if (bookService.hasCustomerAccess(specificCustomer)) {
                customerId = specificCustomer.id.toString()
            } else {
                customerInstance.errorMessage(field: 'code', code: 'document.customer.invalid', default: 'Invalid customer')
                valid = false
            }
        }

        def fmt = DateFormat.getDateInstance(DateFormat.SHORT, utilService.currentLocale())
        def statementDate = params.statementDate
        if (statementDate) {
            try {
                statementDate = fmt.parse(statementDate)
            } catch (ParseException pe) {
                customerInstance.errorMessage(field: 'dateCreated', code: 'customer.statement.date.bad', default: 'Invalid Statement Date')
                valid = false
            }
        } else {
            statementDate = ''
        }

        if (!valid) {
            render(view: 'reprint', model: [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes, statementDate: statementDate])
            return
        }

        params.p_codes = selectedCodes.join(',')
        if (batchSize) params.p_batchSize = batchSize
        if (statementDate) params.p_stmtDate = fmt.format(statementDate)
        if (customerId) params.p_customer = customerId
        def result = utilService.demandRunFromParams('csReprint', params)
        if (result instanceof String) {
            customerInstance.errors.reject(null, result)
            render(view: 'reprint', model: [customerInstance: customerInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes])
        } else {
            flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            redirect(controller: 'systemMenu', action: 'display')
        }
    }
}