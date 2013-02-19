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

class SupplierController {

    // Injected services
    def utilService
    def bookService
    def postingService

    // Security settings
    def activities = [default: 'apadmin', enquire: 'enquire', remittanceEnquiry: 'enquire', remittancePrint: 'enquire', allocations: 'enquire',
            print: 'apreport', printing: 'apreport', aged: 'apreport', ageing: 'apreport', reprint: 'apreport', reprinting: 'apreport']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', importing: 'POST', auto: 'POST', allocating: 'POST', printing: 'POST', ageing: 'POST', reprinting: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'name', 'accountCreditLimit', 'accountCurrentBalance', 'settlementDays',
                'periodicSettlement', 'taxId', 'active', 'nextAutoPaymentDate', 'revaluationMethod'].contains(params.sort) ? params.sort : 'code'

        // Note that the selectList and selectCount calls below are inherently limited to the current
        // company since the list of accessCode objects is company (and user) specific
        def supplierInstanceList, supplierInstanceTotal
        def accessList = bookService.supplierAccessCodes()
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
            supplierInstanceList = Supplier.selectList(where: where, params: accessList)
            supplierInstanceTotal = Supplier.selectCount()
        } else {
            supplierInstanceList = []
            supplierInstanceTotal = 0L
        }

        [supplierInstanceList: supplierInstanceList, supplierInstanceTotal: supplierInstanceTotal]
    }

    def show() {
        def supplierInstance = Supplier.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!supplierInstance || !bookService.hasSupplierAccess(supplierInstance)) {
            flash.message = utilService.standardMessage('not.found', 'supplier', params.id)
            redirect(action: 'list')
        } else {
            return [supplierInstance: supplierInstance, hasTransactions: supplierInstance.hasTransactions()]
        }
    }

    def delete() {
        def supplierInstance = Supplier.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (supplierInstance && bookService.hasSupplierAccess(supplierInstance)) {
            try {
                bookService.deleteSupplier(supplierInstance)
                flash.message = utilService.standardMessage('deleted', supplierInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', supplierInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'supplier', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def company = utilService.currentCompany()
        def supplierInstance = Supplier.findByIdAndSecurityCode(params.id, company.securityCode)
        if (!supplierInstance || !bookService.hasSupplierAccess(supplierInstance)) {
            flash.message = utilService.standardMessage('not.found', 'supplier', params.id)
            redirect(action: 'list')
        } else {
            return [supplierInstance: supplierInstance, currencyList: ExchangeCurrency.findAllByCompany(company, [cache: true]),
                    taxList: TaxCode.findAllByCompany(company, [cache: true]), accessList: bookService.supplierAccessCodes(),
                    hasTransactions: supplierInstance.hasTransactions(), scheduleList: PaymentSchedule.findAllByCompany(company),
                    documentTypeList: DocumentType.findAllByCompanyAndAutoBankAccountIsNotNull(company), companyCurrencyId: utilService.companyCurrency().id]
        }
    }

    def update(Long version) {
        def company = utilService.currentCompany()
        def supplierInstance = Supplier.findByIdAndSecurityCode(params.id, company.securityCode)
        if (supplierInstance && bookService.hasSupplierAccess(supplierInstance)) {
            def hasTransactions = supplierInstance.hasTransactions()
            if (version != null && supplierInstance.version > version) {
                supplierInstance.errorMessage(code: 'locking.failure', domain: 'supplier')
                render(view: 'edit', model: [supplierInstance: supplierInstance, currencyList: ExchangeCurrency.findAllByCompany(company, [cache: true]),
                        taxList: TaxCode.findAllByCompany(company, [cache: true]), accessList: bookService.supplierAccessCodes(), hasTransactions: hasTransactions,
                        scheduleList: PaymentSchedule.findAllByCompany(company), documentTypeList: DocumentType.findAllByCompanyAndAutoBankAccountIsNotNull(company),
                        companyCurrencyId: utilService.companyCurrency().id])
                return
            }

            def oldCurrencyId = supplierInstance.currency.id
            def oldDocumentType = supplierInstance.documentType
            supplierInstance.properties['taxCode', 'code', 'name', 'accountCreditLimit', 'accessCode', 'country', 'currency', 'taxId', 'settlementDays', 'periodicSettlement',
                    'active', 'bankSortCode', 'bankAccountName', 'bankAccountNumber', 'schedule', 'documentType', 'revaluationMethod'] = params
            utilService.verify(supplierInstance, ['currency', 'taxCode', 'accessCode', 'schedule', 'documentType'])             // Ensure correct references
            def valid = (!supplierInstance.hasErrors() && bookService.hasSupplierAccess(supplierInstance))
            if (valid && oldCurrencyId != supplierInstance.currency.id && hasTransactions) {
                supplierInstance.errorMessage(field: 'currency', code: 'supplier.currency.change', default: 'You may not change the currency of a supplier once transactions have been posted to the account')
                valid = false
            }

            if (valid) {
                Supplier.withTransaction {status ->
                    if (supplierInstance.saveThis()) {
                        if (oldDocumentType && !supplierInstance.documentType) {
                            def pending = Remittance.findAllBySupplierAndPaymentDateIsNull(supplierInstance)
                            for (ra in pending) ra.delete(flush: true)
                        }
                    } else {
                        status.setRollbackOnly()
                        valid = false
                    }
                }
            }

            if (valid) {
                flash.message = utilService.standardMessage('updated', supplierInstance)
                redirect(action: 'show', id: supplierInstance.id)
            } else {
                render(view: 'edit', model: [supplierInstance: supplierInstance, currencyList: ExchangeCurrency.findAllByCompany(company, [cache: true]),
                        taxList: TaxCode.findAllByCompany(company, [cache: true]), accessList: bookService.supplierAccessCodes(), hasTransactions: hasTransactions,
                        scheduleList: PaymentSchedule.findAllByCompany(company), documentTypeList: DocumentType.findAllByCompanyAndAutoBankAccountIsNotNull(company),
                        companyCurrencyId: utilService.companyCurrency().id])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'supplier', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def supplierInstance = new Supplier()
        def company = utilService.currentCompany()
        supplierInstance.company = company   // Ensure correct company
        supplierInstance.country = company.country
        supplierInstance.currency = utilService.companyCurrency()
        supplierInstance.taxCode = utilService.companyTaxCode()
        supplierInstance.settlementDays = utilService.setting('supplier.settlement.days', 30)
        supplierInstance.periodicSettlement = utilService.setting('supplier.settlement.periodic', false)
        return [supplierInstance: supplierInstance, currencyList: ExchangeCurrency.findAllByCompany(company, [cache: true]),
                taxList: TaxCode.findAllByCompany(company, [cache: true]), accessList: bookService.supplierAccessCodes(),
                scheduleList: PaymentSchedule.findAllByCompany(company), documentTypeList: DocumentType.findAllByCompanyAndAutoBankAccountIsNotNull(company),
                companyCurrencyId: utilService.companyCurrency().id]
    }

    def save() {
        def supplierInstance = new Supplier()
        def company = utilService.currentCompany()
        supplierInstance.properties['taxCode', 'currency', 'country', 'accessCode', 'code', 'name', 'accountCreditLimit', 'taxId', 'settlementDays', 'periodicSettlement',
                'active', 'bankSortCode', 'bankAccountName', 'bankAccountNumber', 'schedule', 'documentType', 'revaluationMethod'] = params
        supplierInstance.company = company   // Ensure correct company
        utilService.verify(supplierInstance, ['currency', 'taxCode', 'accessCode', 'schedule', 'documentType'])             // Ensure correct references
        if (!supplierInstance.hasErrors() && bookService.hasSupplierAccess(supplierInstance) && bookService.insertSupplier(supplierInstance)) {
            flash.message = utilService.standardMessage('created', supplierInstance)
            redirect(controller: 'supplierAddress', action: 'initial', id: supplierInstance.id)
        } else {
            render(view: 'create', model: [supplierInstance: supplierInstance, currencyList: ExchangeCurrency.findAllByCompany(company, [cache: true]),
                    taxList: TaxCode.findAllByCompany(company, [cache: true]), accessList: bookService.supplierAccessCodes(),
                    scheduleList: PaymentSchedule.findAllByCompany(company), documentTypeList: DocumentType.findAllByCompanyAndAutoBankAccountIsNotNull(company),
                    companyCurrencyId: utilService.companyCurrency().id])
        }
    }

    def imports() {
        [supplierInstance: new Supplier(), accessList: bookService.supplierAccessCodes()]
    }

    def importing() {
        def supplierInstance = new Supplier()
        def company = utilService.currentCompany()
        supplierInstance.accessCode = AccessCode.findByIdAndCompany(params.accessCode?.id?.toLong(), company)
        def valid = true
        def added = 0
        def ignored = 0
        def errors = 0
        def uploadFile = request.getFile('file')
        if (!bookService.hasSupplierAccessCode(supplierInstance.accessCode?.code)) {
            supplierInstance.errorMessage(code: 'supplier.import.invalid', default: 'Invalid access code')
            valid = false
        } else {
            if (uploadFile.isEmpty()) {
                supplierInstance.errorMessage(code: 'supplier.empty', default: 'File is empty')
                valid = false
            } else {
                if (uploadFile.getSize() > 1024 * 1024) {
                    supplierInstance.errorMessage(code: 'supplier.size', default: 'File exceeds the 1 MB limit')
                    valid = false
                } else {
                    def sourceFile = utilService.tempFile('AP', 'txt')
                    def currency = utilService.companyCurrency()
                    def taxCode = utilService.companyTaxCode()
                    def fields, code, name, rec
                    try {
                        uploadFile.transferTo(sourceFile)
                        sourceFile.eachLine {
                            if (it.trim()) {
                                fields = it.split('\\t')*.trim()
                                code = bookService.fixSupplierCase(fields[0])
                                if (fields.size() >= 2) {
                                    if (code.length() <= 20) {
                                        rec = Supplier.findByCompanyAndCode(company, code)
                                        if (rec) {
                                            ignored++
                                        } else {
                                            rec = new Supplier()
                                            name = fields[1]
                                            if (name.length() > 50) name = name.substring(0, 50)
                                            rec.company = company
                                            rec.code = code
                                            rec.name = name
                                            rec.accessCode = supplierInstance.accessCode
                                            rec.country = company.country
                                            rec.currency = currency
                                            rec.taxCode = taxCode
                                            rec.settlementDays = utilService.setting('supplier.settlement.days', 30)
                                            rec.periodicSettlement = utilService.setting('supplier.settlement.periodic', false)

                                            if (!rec.hasErrors() && bookService.insertSupplier(rec)) {
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
                        supplierInstance.errorMessage(code: 'supplier.bad.upload', default: 'Unable to upload the file')
                        valid = false
                    }
                }
            }
        }

        if (valid) {
            flash.message = message(code: 'supplier.uploaded', args: [added.toString(), ignored.toString(), errors.toString()], default: "${added} code(s) added, ${ignored} skipped, ${errors} had errors")
            redirect(action: 'list')
        } else {
            render(view: 'imports', model: [supplierInstance: supplierInstance, accessList: bookService.supplierAccessCodes()])
        }
    }

    def enquire() {
        params.max = utilService.max
        params.offset = utilService.offset
        def supplierInstance = new Supplier()
        def transactionInstanceList = []
        def transactionInstanceTotal = 0
        def remittanceCount = 0
        def currencyList = ExchangeCurrency.findAllByCompany(utilService.currentCompany(), [cache: true])
        def periodList = bookService.getUsedPeriods(utilService.currentCompany())
        def displayCurrency = (params.displayCurrency && params.displayCurrency != 'null') ? ExchangeCurrency.get(params.displayCurrency) : null
        def displayCurrencyClass = ''
        def displayPeriod = (params.displayPeriod && params.displayPeriod != 'null') ? Period.get(params.displayPeriod) : null
        def turnoverList = []
        def displayTurnover
        if (params.code) {
            def code = bookService.fixSupplierCase(params.code)
            supplierInstance = Supplier.findByCompanyAndCode(utilService.currentCompany(), code)
            if (!supplierInstance) {
                supplierInstance = new Supplier()
                supplierInstance.code = code
                supplierInstance.errorMessage(field: 'code', code: 'document.supplier.invalid', default: 'Invalid supplier')
            }
        } else if (params.id) {
            supplierInstance = Supplier.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
            if (!supplierInstance) {
                supplierInstance = new Supplier()
                supplierInstance.errorMessage(field: 'code', code: 'document.supplier.invalid', default: 'Invalid supplier')
            }
        }

        if (supplierInstance.id) {
            if (bookService.hasSupplierAccess(supplierInstance)) {
                def parameters = [supplierInstance]
                def sql = 'from GeneralTransaction as x where x.supplier = ? and '
                if (displayPeriod) {
                    sql += 'x.document.period = ?'
                    parameters << displayPeriod
                } else {
                    sql += 'x.accountUnallocated != 0'
                }

                // Don't display documents with a zero account value (e.g. automatic foreign exchange differences) when viewing in account currency
                if (!displayCurrency || displayCurrency.id == supplierInstance.currency.id) sql += ' and x.accountValue != 0'

                transactionInstanceList = GeneralTransaction.findAll(sql + ' order by x.document.documentDate desc, x.document.id desc', parameters, [max: params.max, offset: params.offset])
                transactionInstanceTotal = GeneralTransaction.executeQuery('select count(*) ' + sql, parameters)[0]
                def turnovers = SupplierTurnover.findAll("from SupplierTurnover as t where t.supplier = ? and t.period.status != ? order by t.period.validFrom desc", [supplierInstance, 'new'])
                def val
                parameters = [context: supplierInstance, field: 'turnover', currency: displayCurrency]
                for (turnover in turnovers) {
                    parameters.turnover = turnover
                    val = bookService.getBookValue(parameters)
                    if (val != null && !(val instanceof String)) {
                        if (val >= 0.0) {
                            val = utilService.format(val, parameters.scale)
                        } else {
                            val = utilService.format(-val, parameters.scale) + ' ' + message(code: 'generic.dr', default: 'Dr')
                        }
                    }
                    turnoverList << [id: turnover.period.id, data: turnover.period.code + ': ' + val]
                }
                displayTurnover = displayPeriod ?: bookService.selectPeriod(periodList)
                if (displayCurrency && displayCurrency.id != supplierInstance.currency.id && displayCurrency.id != utilService.companyCurrency().id) displayCurrencyClass = 'conversion'
                remittanceCount = Remittance.countBySupplier(supplierInstance)
            } else {
                def code = supplierInstance.code
                supplierInstance = new Supplier()
                supplierInstance.code = code
                supplierInstance.errorMessage(field: 'code', code: 'document.supplier.invalid', default: 'Invalid supplier')
            }
        }

        periodList = periodList.reverse()   // More intuitive to see it in reverse order

        [supplierInstance: supplierInstance, transactionInstanceList: transactionInstanceList, turnoverList: turnoverList,
                transactionInstanceTotal: transactionInstanceTotal, currencyList: currencyList, periodList: periodList,
                displayCurrency: displayCurrency, displayCurrencyClass: displayCurrencyClass, displayPeriod: displayPeriod,
                displayTurnover: displayTurnover, remittanceCount: remittanceCount]
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
            def account = line.supplier
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
        def supplierInstance = utilService.source('supplier.list')
        if (!supplierInstance || !bookService.hasSupplierAccess(supplierInstance)) {
            flash.message = message(code: 'document.supplier.invalid', default: 'Invalid supplier')
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
        def parameters = [supplierInstance]
        def sql = 'from GeneralTransaction as x where x.supplier = ? and '
        if (displayPeriod) {
            sql += 'x.document.period = ?'
            parameters << displayPeriod
        } else {
            sql += 'x.accountUnallocated != 0'
        }

        transactionInstanceList = GeneralTransaction.findAll(sql + ' order by x.document.documentDate desc, x.document.id desc', parameters, [max: params.max, offset: params.offset])
        transactionInstanceTotal = GeneralTransaction.executeQuery('select count(*) ' + sql, parameters)[0]

        [supplierInstance: supplierInstance, transactionInstanceList: transactionInstanceList, transactionInstanceTotal: transactionInstanceTotal,
                periodList: periodList, displayPeriod: displayPeriod]
    }

    def auto() {
        def supplierInstance = utilService.reSource('supplier.list', [origin: 'transactions'])
        if (!supplierInstance || !bookService.hasSupplierAccess(supplierInstance)) {
            flash.message = message(code: 'document.supplier.invalid', default: 'Invalid supplier')
            redirect(action: 'list')
            return
        }

        if (postingService.autoAllocate(supplierInstance)) {
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
        def lineInstance = utilService.source('supplier.transactions')
        if (!lineInstance || lineInstance.securityCode != utilService.currentCompany().securityCode || !bookService.hasSupplierAccess(lineInstance.supplier)) {
            flash.message = message(code: 'document.no.manual', default: 'Unable to perform manual allocation')
            redirect(action: 'transactions', params: [displayPeriod: transactionPeriod])
            return
        }

        params.max = utilService.max
        params.offset = utilService.offset
        def supplierInstance = lineInstance.supplier
        def allocationInstance = new Allocation()
        allocationInstance.properties['targetType', 'targetCode', 'accountValue', 'accountDifference'] = params
        utilService.verify(allocationInstance, 'targetType')
        if (!allocationInstance.accountValue) allocationInstance.accountValue = (lineInstance.accountValue < 0.0) ? -lineInstance.accountUnallocated : lineInstance.accountUnallocated
        def periodList = bookService.getUsedPeriods(utilService.currentCompany())
        periodList = periodList.reverse()    // More intuitive to see it in reverse order
        def displayPeriod = (params.displayPeriod && params.displayPeriod != 'null') ? Period.get(params.displayPeriod) : null
        def parameters = [supplierInstance, lineInstance.id]
        def sql = 'from GeneralTransaction as x where x.supplier = ? and x.id != ? and x.accountValue != 0 and '
        if (displayPeriod) {
            sql += 'x.document.period = ?'
            parameters << displayPeriod
        } else {
            sql += 'x.accountUnallocated != 0'
        }

        def transactionInstanceList = GeneralTransaction.findAll(sql + ' order by x.document.documentDate desc, x.document.id desc', parameters, [max: params.max, offset: params.offset])
        def transactionInstanceTotal = GeneralTransaction.executeQuery('select count(*) ' + sql, parameters)[0]
        def documentTypeList = DocumentType.findAll("from DocumentType as x where x.company = ? and x.type.supplierAllocate = ?", [utilService.currentCompany(), true])

        [supplierInstance: supplierInstance, lineInstance: lineInstance, allocationInstance: allocationInstance, transactionInstanceList: transactionInstanceList,
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
        def lineInstance = utilService.reSource('supplier.transactions', [origin: 'allocate'])
        if (!lineInstance || lineInstance.securityCode != utilService.currentCompany().securityCode || !bookService.hasSupplierAccess(lineInstance.supplier)) {
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
            def supplierInstance = lineInstance.supplier
            def periodList = bookService.getUsedPeriods(utilService.currentCompany())
            periodList = periodList.reverse()    // More intuitive to see it in reverse order
            def displayPeriod = (params.displayPeriod && params.displayPeriod != 'null') ? Period.get(params.displayPeriod) : null
            def parameters = [supplierInstance, lineInstance.id]
            def sql = 'from GeneralTransaction as x where x.supplier = ? and x.id != ? and x.accountValue != 0 and '
            if (displayPeriod) {
                sql += 'x.document.period = ?'
                parameters << displayPeriod
            } else {
                sql += 'x.accountUnallocated != 0'
            }

            def transactionInstanceList = GeneralTransaction.findAll(sql + ' order by x.document.documentDate desc, x.document.id desc', parameters, [max: params.max, offset: params.offset])
            def transactionInstanceTotal = GeneralTransaction.executeQuery('select count(*) ' + sql, parameters)[0]
            def documentTypeList = DocumentType.findAll("from DocumentType as x where x.company = ? and x.type.supplierAllocate = ?", [utilService.currentCompany(), true])

            render(view: 'allocate', model: [supplierInstance: supplierInstance, lineInstance: lineInstance, allocationInstance: allocationInstance,
                    transactionInstanceList: transactionInstanceList, documentTypeList: documentTypeList, transactionInstanceTotal: transactionInstanceTotal,
                    periodList: periodList, displayPeriod: displayPeriod, allowDifference: true, transactionPeriod: transactionPeriod])
        }
    }

    def print() {
        def supplierInstance = new Supplier()
        def accessCodeList = bookService.supplierAccessCodes()
        [supplierInstance: supplierInstance, accessCodeList: accessCodeList, selectedCodes: []]
    }

    def printing() {
        def supplierInstance = new Supplier(params)
        def accessCodeList = bookService.supplierAccessCodes()
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
            supplierInstance.errorMessage(field: 'accessCode', code: 'report.accessCode.limit', default: 'There is a limit of 18 access codes that can be selected in one report')
            render(view: 'print', model: [supplierInstance: supplierInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes])
            return
        }

        params.p_codes = selectedCodes.join(',')
        params.p_active = supplierInstance.active.toString()
        def result = utilService.demandRunFromParams('suppliers', params)
        if (result instanceof String) {
            supplierInstance.errors.reject(null, result)
            render(view: 'print', model: [supplierInstance: supplierInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes])
        } else {
            flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            redirect(controller: 'systemMenu', action: 'display')
        }
    }

    def aged() {
        def supplierInstance = new Supplier()
        def accessCodeList = bookService.supplierAccessCodes()
        [supplierInstance: supplierInstance, accessCodeList: accessCodeList, selectedCodes: []]
    }

    def ageing() {
        def supplierInstance = new Supplier()
        def accessCodeList = bookService.supplierAccessCodes()
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
            supplierInstance.errorMessage(field: 'accessCode', code: 'report.accessCode.limit', default: 'There is a limit of 18 access codes that can be selected in one report')
            render(view: 'aged', model: [supplierInstance: supplierInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes])
            return
        }

        params.p_codes = selectedCodes.join(',')
        def result = utilService.demandRunFromParams('agedCredit', params)
        if (result instanceof String) {
            supplierInstance.errors.reject(null, result)
            render(view: 'aged', model: [supplierInstance: supplierInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes])
        } else {
            flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            redirect(controller: 'systemMenu', action: 'display')
        }
    }

    def remittanceEnquiry() {
        params.max = utilService.max
        params.offset = utilService.offset
        def displayCurrency = (params.displayCurrency && params.displayCurrency != 'null') ? ExchangeCurrency.get(params.displayCurrency) : null
        def displayPeriod = (params.displayPeriod && params.displayPeriod != 'null') ? Period.get(params.displayPeriod) : null
        def remittanceList, remittanceInstance, remittanceLineInstanceList, locale
        def remittanceLineInstanceTotal = 0
        def debitTotal = 0.0
        def creditTotal = 0.0
        def supplierInstance = Supplier.get(params.supplierId)
        if (bookService.hasSupplierAccess(supplierInstance)) {
            locale = new Locale(supplierInstance.country.language.code, supplierInstance.country.code)
            def remittances = Remittance.findAllBySupplierAndPaymentDateIsNotNull(supplierInstance, [sort: 'paymentDate', order: 'desc'])
            if (remittances) {
                remittanceList = []
                remittanceInstance = Remittance.get(params.remittance) ?: remittances[0]
                if (remittanceInstance) remittanceInstance.sourceDocument = Document.get(remittanceInstance.paymentDocumentId)
                if (!params.priorRemittance || params.priorRemittance.toLong() != remittanceInstance.id) params.offset = 0
                for (ra in remittances) remittanceList << [id: ra.id, adviceDate: utilService.format(ra.adviceDate, 1, null, locale)]
                remittanceLineInstanceList = RemittanceLine.findAll('from RemittanceLine as x where x.remittance = ? order by x.documentDate, x.sequencer',
                        [remittanceInstance], [max: params.max, offset: params.offset])
                for (line in remittanceLineInstanceList) line.source = GeneralTransaction.get(line.sequencer)
                def val = RemittanceLine.executeQuery('select sum(accountUnallocated), count(*) from RemittanceLine where remittance = ? and accountUnallocated >= 0.0', [remittanceInstance])[0]
                if (val[0]) debitTotal = val[0]
                remittanceLineInstanceTotal += val[1]
                val = RemittanceLine.executeQuery('select sum(accountUnallocated), count(*) from RemittanceLine where remittance = ? and accountUnallocated < 0.0', [remittanceInstance])[0]
                if (val[0]) creditTotal = -val[0]
                remittanceLineInstanceTotal += val[1]
            }
        } else {
            supplierInstance = new Supplier()
            supplierInstance.errorMessage(code: 'document.supplier.invalid', default: 'Invalid supplier')
        }

        [supplierInstance: supplierInstance, remittanceList: remittanceList, remittanceInstance: remittanceInstance, remittanceLineInstanceList: remittanceLineInstanceList,
                remittanceLineInstanceTotal: remittanceLineInstanceTotal, displayPeriod: displayPeriod, displayCurrency: displayCurrency,
                debitTotal: debitTotal, creditTotal: creditTotal, locale: locale]
    }

    def remittancePrint() {
        def supplierInstance = Supplier.get(params.supplierId)
        if (bookService.hasSupplierAccess(supplierInstance)) {
            def remittanceInstance = Remittance.get(params.remittance)
            if (remittanceInstance) {
                def fmt = DateFormat.getDateInstance(DateFormat.SHORT, utilService.currentLocale())
                params.p_supplier = supplierInstance.id.toString()
                params.p_adviceDate = fmt.format(remittanceInstance.adviceDate)
                def result = utilService.demandRunFromParams('raReprint', params)
                if (result instanceof String) {
                    flash.message = result
                } else {
                    flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
                }
            } else {
                flash.message = utilService.standardMessage('not.found', 'remittance', params.remittance)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'supplier', params.supplierId)
        }

        redirect(action: 'remittanceEnquiry', params: params)
    }

    def reprint() {
        def supplierInstance = new Supplier()
        def accessCodeList = bookService.supplierAccessCodes()
        [supplierInstance: supplierInstance, accessCodeList: accessCodeList, selectedCodes: [], adviceDate: '']
    }

    def reprinting() {
        def supplierInstance = new Supplier()
        def accessCodeList = bookService.supplierAccessCodes()
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
            supplierInstance.errorMessage(field: 'accessCode', code: 'report.accessCode.limit', default: 'There is a limit of 18 access codes that can be selected in one report')
            valid = false
        }

        def batchSize = params.taxId
        supplierInstance.taxId = batchSize
        if (batchSize != null && batchSize != '' && (!batchSize.isInteger() || batchSize.toInteger() <= 0 || batchSize.toInteger() > 99999)) {
            supplierInstance.errorMessage(field: 'taxId', code: 'remittance.batchSize.bad', default: 'Invalid Batch Size')
            valid = false
        }

        def supplierId
        if (params.code) {
            supplierInstance.code = bookService.fixSupplierCase(params.code)
            def specificSupplier = Supplier.findByCompanyAndCode(utilService.currentCompany(), supplierInstance.code)
            if (bookService.hasSupplierAccess(specificSupplier)) {
                supplierId = specificSupplier.id.toString()
            } else {
                supplierInstance.errorMessage(field: 'code', code: 'document.supplier.invalid', default: 'Invalid supplier')
                valid = false
            }
        }

        def fmt = DateFormat.getDateInstance(DateFormat.SHORT, utilService.currentLocale())
        def adviceDate = params.adviceDate
        if (adviceDate) {
            try {
                adviceDate = fmt.parse(adviceDate)
            } catch (ParseException pe) {
                supplierInstance.errorMessage(field: 'dateCreated', code: 'remittance.date.bad', default: 'Invalid Advice Date')
                valid = false
            }
        } else {
            adviceDate = ''
        }

        if (!valid) {
            render(view: 'reprint', model: [supplierInstance: supplierInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes, adviceDate: adviceDate])
            return
        }

        params.p_codes = selectedCodes.join(',')
        if (batchSize) params.p_batchSize = batchSize
        if (adviceDate) params.p_adviceDate = fmt.format(adviceDate)
        if (supplierId) params.p_supplier = supplierId
        def result = utilService.demandRunFromParams('raReprint', params)
        if (result instanceof String) {
            supplierInstance.errors.reject(null, result)
            render(view: 'reprint', model: [supplierInstance: supplierInstance, accessCodeList: accessCodeList, selectedCodes: selectedCodes])
        } else {
            flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            redirect(controller: 'systemMenu', action: 'display')
        }
    }
}