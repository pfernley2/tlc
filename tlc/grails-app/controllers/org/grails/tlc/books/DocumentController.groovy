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

import org.grails.tlc.obj.DocumentSearch
import java.text.DateFormat
import java.text.NumberFormat
import java.text.ParseException

class DocumentController {

    // Injected services
    def utilService
    def bookService
    def postingService

    // Security settings
    def activities = [default: 'attached', search: 'enquire', list: 'enquire']

    // List of actions with specific request types
    static allowedMethods = [customer: 'POST', supplier: 'POST', period: 'POST', code: 'POST', account: 'POST', accounts: 'POST', hold: 'POST']

    // Ajax call to get customer details from a customer code
    def customer() {
        def customerInstance = Customer.findByCompanyAndCode(utilService.currentCompany(), bookService.fixCustomerCase(params.sourceCode))
        if (customerInstance?.active && bookService.hasCustomerAccess(customerInstance)) {
            render(contentType: 'text/json') {
                sourceCode = customerInstance.code
                sourceName = customerInstance.name
                currencyId = customerInstance.currency.id
            }
        } else {
            render(contentType: 'text/json') {
                errorMessage = message(code: 'document.customer.invalid', default: 'Invalid customer')
            }
        }
    }

    // Ajax call to get supplier details from a supplier code
    def supplier() {
        def supplierInstance = Supplier.findByCompanyAndCode(utilService.currentCompany(), bookService.fixSupplierCase(params.sourceCode))
        if (supplierInstance?.active && bookService.hasSupplierAccess(supplierInstance)) {
            render(contentType: 'text/json') {
                sourceCode = supplierInstance.code
                sourceName = supplierInstance.name
                currencyId = supplierInstance.currency.id
            }
        } else {
            render(contentType: 'text/json') {
                errorMessage = message(code: 'document.supplier.invalid', default: 'Invalid supplier')
            }
        }
    }

    // Ajax call to get the id of a period based on a documentDate parameter. Returns zero if no period can be found.
    // If a supplier or customer code is passed to this method then a dueDate will be returned. If no supplier
    // or customer code is passed in, then the dueDate result will be an empty string.
    def period() {
        def pdId = 0L
        def due = ''
        def period
        def docDate
        def days
        if (params.documentDate) {
            try {
                docDate = DateFormat.getDateInstance(DateFormat.SHORT, utilService.currentLocale()).parse(params.documentDate)
                def now = new Date()
                if (docDate >= now - 365 && docDate <= now + 365) {
                    if (params.adjustment == 'true') {
                        period = bookService.selectPeriod(bookService.getUsedPeriods(utilService.currentCompany()), docDate)
                    } else {
                        period = bookService.selectPeriod(bookService.getOpenPeriods(utilService.currentCompany()), docDate)
                    }
                }
            } catch (ParseException pe) {}
        }

        if (period) {
            pdId = period.id
            def source
            if (params.customer) {
                if (params.type && params.type != 'null' && DocumentType.get(params.type)?.type?.code == 'SC') {
                    due = docDate
                } else {
                    days = utilService.setting('customer.settlement.days', 30)
                    source = Customer.findByCompanyAndCode(utilService.currentCompany(), params.customer)
                }
            } else if (params.supplier) {
                if (params.type && params.type != 'null' && DocumentType.get(params.type)?.type?.code == 'PC') {
                    due = docDate
                } else {
                    days = utilService.setting('supplier.settlement.days', 30)
                    source = Supplier.findByCompanyAndCode(utilService.currentCompany(), params.supplier)
                }
            }

            if (source) {
                if (source.periodicSettlement) docDate = period.validTo
                if (source.settlementDays) days = source.settlementDays
                due = docDate + days
            } else if (params.common == 'true') {   // If due date should be set to doc date
                due = docDate
            }

            if (due) due = utilService.format(due, 1)
        }

        render(contentType: 'text/json') {
            periodId = pdId
            dueDate = due
        }
    }

    // Ajax call to get the next document number for a particular document type
    def code() {
        def typeId = params.typeId
        def codeVal = ''
        def codeEdit = true
        def errMessage
        if (typeId != 'null') {
            def documentTypeInstance = DocumentType.lock(typeId.toLong())
            if (documentTypeInstance?.securityCode == utilService.currentCompany().securityCode) {
                codeEdit = documentTypeInstance.allowEdit
                if (documentTypeInstance.autoGenerate) {
                    codeVal = documentTypeInstance.nextSequenceNumber
                    documentTypeInstance.nextSequenceNumber += 1
                    if (!documentTypeInstance.saveThis()) errMessage = message(code: 'document.bad.next', default: 'Unable to update the next document sequence number')
                }
            } else {
                errMessage = message(code: 'document.bad.find', default: 'Document type not found')
            }
        }

        if (errMessage) {
            render(contentType: 'text/json') {
                errorMessage = errMessage
            }
        } else {
            render(contentType: 'text/json') {
                sourceNumber = codeVal
                allowEdit = codeEdit
                nextField = params.nextField
            }
        }
    }

    // Ajax call to get GL account details from an account code
    def account() {
        def code = params.accountCode
        def errMessage, accountInstance
        if (code) {
            code = bookService.expandAccountCode(utilService.currentUser(), code)
            if (code) {
                accountInstance = bookService.getAccount(utilService.currentCompany(), code)
                errMessage = (accountInstance instanceof String) ? accountInstance : checkPostability(accountInstance, params.metaType)
            } else {
                errMessage = message(code: 'account.not.exists', default: 'Invalid GL account')
            }
        }

        if (errMessage) {
            render(contentType: 'text/json') {
                errorMessage = errMessage
            }
        } else {
            render(contentType: 'text/json') {
                accountCode = accountInstance?.code
                accountName = accountInstance?.name
            }
        }
    }

    // Ajax call to return the details of a GL, AR or AP account based on an accountType parameter
    def accounts() {
        def code = params.accountCode
        def type = params.accountType
        def errMessage
        def accountInstance
        if (code && type) {
            switch (type) {
                case 'gl':
                    code = bookService.expandAccountCode(utilService.currentUser(), code)
                    if (code) {
                        accountInstance = bookService.getAccount(utilService.currentCompany(), code)
                        errMessage = (accountInstance instanceof String) ? accountInstance : checkPostability(accountInstance, params.metaType)
                    } else {
                        errMessage = message(code: 'account.not.exists', default: 'Invalid GL account')
                    }
                    break

                case 'ar':
                    accountInstance = Customer.findByCompanyAndCode(utilService.currentCompany(), bookService.fixCustomerCase(code))
                    if (!accountInstance?.active || !bookService.hasCustomerAccess(accountInstance)) errMessage = message(code: 'document.customer.invalid', default: 'Invalid customer')
                    break

                case 'ap':
                    accountInstance = Supplier.findByCompanyAndCode(utilService.currentCompany(), bookService.fixSupplierCase(code))
                    if (!accountInstance?.active || !bookService.hasSupplierAccess(accountInstance)) errMessage = message(code: 'document.supplier.invalid', default: 'Invalid supplier')
                    break
            }
        }

        if (errMessage) {
            render(contentType: 'text/json') {
                errorMessage = errMessage
            }
        } else {
            render(contentType: 'text/json') {
                accountCode = accountInstance?.code
                accountName = accountInstance?.name
            }
        }
    }

    // Ajax call to set or clear the hold flag on a posted sub-ledger line
    def hold() {
        def errMessage
        def line = GeneralTransaction.get(params.lineId)
        def state = params.newState
        if (line?.securityCode == utilService.currentCompany().securityCode && (bookService.hasCustomerAccess(line.customer) || bookService.hasSupplierAccess(line.supplier))) {
            line.onHold = state == 'true' ? true : false
            if (!line.saveThis()) errMessage = message(code: 'document.bad.hold', default: 'Unable to update the hold status')
        } else {
            errMessage = message(code: 'document.invalid', default: 'Invalid document')
        }

        if (errMessage) {
            render(contentType: 'text/json') {
                errorMessage = errMessage
            }
        } else {
            render(contentType: 'text/json') {
                newState = state
            }
        }
    }

    // Ajax call to get GL cash or bank account and return the id of the currency it uses
    def bank() {
        def errMessage
        def currencyVal = 0L
        if (params.accountCode) {
            def account = Account.findBySecurityCodeAndCode(utilService.currentCompany().securityCode, params.accountCode)
            if (account && account.type?.code == params.type && bookService.hasAccountAccess(account)) {
                currencyVal = account.currency.id
            } else {
                errMessage = message(code: params.type + '.not.exists', default: 'Invalid Bank/Cash account')
            }
        }

        if (errMessage) {
            render(contentType: 'text/json') {
                errorMessage = errMessage
            }
        } else {
            render(contentType: 'text/json') {
                currencyId = currencyVal
            }
        }
    }

    // Ajax call to get the periods of a year
    def year() {
        def errMessage, pds, pdList
        def yr = Year.findByIdAndCompany(params.yearId, utilService.currentCompany())
        if (yr) {
            if (!params.order?.equalsIgnoreCase('desc')) params.order = 'asc'
            def codes = ''
            if (params.statusCodes) {
                def lst = params.statusCodes.split(',')*.trim()
                if (lst.size() == 1) {
                    codes = " and status = '${lst[0]}'"
                } else {
                    for (c in lst) {
                        if (codes) {
                            codes += ", ${c}"
                        } else {
                            codes = " and status in ('${c}'"
                        }
                    }

                    codes += ')'
                }
            }

            pds = Period.findAll('from Period where year = ?' + codes + ' order by validFrom ' + params.order, [yr])
            if (pds) {
                pdList = []
                for (pd in pds) {
                    pdList << [val: pd.id, txt: pd.code]
                }
            } else {
                errMessage = message(code: 'budget.no.periods', default: 'There are no periods available in the selected year')
            }
        } else {
            errMessage = utilService.standardMessage('not.found', 'year', params.yearId)
        }

        if (errMessage) {
            render(contentType: 'text/json') {
                errorMessage = errMessage
            }
        } else {
            render(contentType: 'text/json') {
                periods = pdList
                targetId = params.targetId
            }
        }
    }

    // Ajax call to set the budget value in a GL balance record
    def budget() {
        def errMessage, generalBudget, companyBudget, rate
        def bal = GeneralBalance.get(params.balanceId)
        if (bal?.securityCode == utilService.currentCompany().securityCode && bookService.hasAccountAccess(bal.account)) {
            if (params.budgetValue) {
                try {
                    def nf = NumberFormat.getInstance(utilService.currentLocale())
                    companyBudget = nf.parse(params.budgetValue)
                    if (!(companyBudget instanceof BigDecimal)) companyBudget = new BigDecimal(companyBudget.toString())
                    if (bal.account.status == 'cr') companyBudget = -companyBudget
                    companyBudget = utilService.round(companyBudget, 0)
                } catch (ParseException bdex) {
                    errMessage = message(code: 'budget.bad.amount', args: [params.budgetValue], default: "Invalid value: ${params.budgetValue}")
                }
            } else {
                companyBudget = 0.0
            }

            if (!errMessage) {
                if (!companyBudget || bal.account.currency.id == utilService.companyCurrency().id) {
                    generalBudget = companyBudget
                } else {
                    rate = utilService.getExchangeRate(utilService.companyCurrency(), bal.account.currency)
                    if (rate) {
                        generalBudget = utilService.round(companyBudget * rate, 0)
                    } else {
                        errMessage = message(code: 'document.bad.exchangeRate', args: [utilService.companyCurrency().code, bal.account.currency.code],
                                default: "No exchange rate available from ${utilService.companyCurrency().code} to ${bal.account.currency.code}")
                    }
                }

                if (!errMessage) {
                    bal.companyBudget = companyBudget
                    bal.generalBudget = generalBudget
                    def saved
                    def lock = bookService.getCompanyLock(utilService.currentCompany())
                    lock.lock()
                    try {
                        saved = bal.saveThis()
                    } finally {
                        lock.unlock()
                    }

                    if (saved) {
                        companyBudget = utilService.format((bal.account.status == 'cr') ? -companyBudget : companyBudget, 0, true)
                    } else {
                        bal.discard()
                        errMessage = message(code: 'budget.bad.balance', default: 'Unable to update the GL balance record')
                    }
                }
            }
        } else {
			errMessage = utilService.standardMessage('not.found', 'generalBalance', params.balanceId)
        }

        if (errMessage) {
            render(contentType: 'text/json') {
                errorMessage = errMessage
            }
        } else {
            render(contentType: 'text/json') {
                balanceId = params.balanceId
                budgetValue = companyBudget
            }
        }
    }

    // Ajax call to get the chart of account sections available on change of section type (ie, bs or both)
    def sectionType() {
        def fragment = ''
        def parameters = [utilService.currentCompany()]
        if (params.sectionType) {
            fragment = ' and x.type = ?'
            parameters << params.sectionType
        }

        def opts = [[val: '', txt: message(code: 'generic.all.selection', default: '-- all --')]]
        def secs = ChartSection.findAll('from ChartSection as x where x.company = ? and x.accountSegment > 0' + fragment +
                ' and exists(select y.id from Account as y where y.section = x) order by x.treeSequence', parameters)
        for (sec in secs) opts << [val: sec.id, txt: sec.name]
        render(contentType: 'text/json') {
            sections = opts
            targetId = params.targetId
        }
    }

    // List the templates available for a given system document type
    def templates() {
        if (!params.act) params.act = 'template'
        def sql = 'from TemplateDocument as x where x.type.company = ? and x.type.type.code in ('
        def parameters = [utilService.currentCompany()]
        def types = params.types.split(',')*.trim()
        for (int i = 0; i < types.size(); i++) {
            if (i > 0) {
                sql += ', ?'
            } else {
                sql += '?'
            }

            parameters << types[i]
        }

        sql += ') order by x.type.code, x.description'
        [templateDocumentInstanceList: TemplateDocument.findAll(sql, parameters), ctrl: params.ctrl, act: params.act]
    }

    def search() {
        def documentTypeList = []
        for (it in DocumentType.findAllByCompany(utilService.currentCompany())) {
            if (utilService.permitted(it.type.activity.code)) documentTypeList << it
        }

        [documentSearchInstance: new DocumentSearch(), documentTypeList: documentTypeList]
    }

    def list(DocumentSearch documentSearchInstance) {
        params.max = utilService.max
        params.offset = utilService.offset
        def valid = (!documentSearchInstance.hasErrors() && documentSearchInstance.validate())
        if (valid) {
            if (documentSearchInstance.type?.securityCode != utilService.currentCompany().securityCode || !utilService.permitted(documentSearchInstance.type.type.activity.code)) {
                documentSearchInstance.rejectValue('type', 'document.bad.type', 'Invalid document type')
                valid = false
            }
        }

        if (!valid) {
            def documentTypeList = []
            for (it in DocumentType.findAllByCompany(utilService.currentCompany())) {
                if (utilService.permitted(it.type.activity.code)) documentTypeList << it
            }

            render(view: 'search', model: [documentSearchInstance: documentSearchInstance, documentTypeList: documentTypeList])
            return
        }

        def sql = 'from Document where type = ?'
        def parameters = [documentSearchInstance.type]
        if (documentSearchInstance.code) {
            sql += ' and code ' + getTest(documentSearchInstance.code) + ' ?'
            parameters << documentSearchInstance.code
        }

        if (documentSearchInstance.reference) {
            sql += ' and reference ' + getTest(documentSearchInstance.reference) + ' ?'
            parameters << documentSearchInstance.reference
        }

        if (documentSearchInstance.description) {
            sql += ' and description ' + getTest(documentSearchInstance.description) + ' ?'
            parameters << documentSearchInstance.description
        }

        if (documentSearchInstance.documentFrom) {
            if (documentSearchInstance.documentTo) {
                sql += ' and documentDate between ? and ?'
                parameters << documentSearchInstance.documentFrom
                parameters << documentSearchInstance.documentTo
            } else {
                sql += ' and documentDate >= ?'
                parameters << documentSearchInstance.documentFrom
            }
        } else if (documentSearchInstance.documentTo) {
            sql += ' and documentDate <= ?'
            parameters << documentSearchInstance.documentTo
        }

        // Note that the dateCreated includes a time and so we check for < postedTo + 1
        if (documentSearchInstance.postedFrom) {
            if (documentSearchInstance.postedTo) {
                sql += ' and dateCreated >= ? and dateCreated < ?'
                parameters << documentSearchInstance.postedFrom
                parameters << documentSearchInstance.postedTo + 1
            } else {
                sql += ' and dateCreated >= ?'
                parameters << documentSearchInstance.postedFrom
            }
        } else if (documentSearchInstance.postedTo) {
            sql += ' and dateCreated < ?'
            parameters << documentSearchInstance.postedTo + 1
        }

        def searchMap = ['type.id': documentSearchInstance.type.id, code: documentSearchInstance.code, reference: documentSearchInstance.reference,
                description: documentSearchInstance.description, documentFrom: utilService.format(documentSearchInstance.documentFrom, 1),
                documentTo: utilService.format(documentSearchInstance.documentTo, 1), postedFrom: utilService.format(documentSearchInstance.postedFrom, 1),
                postedTo: utilService.format(documentSearchInstance.postedTo, 1)]
        def documentInstanceList = Document.findAll(sql + ' order by documentDate desc, id desc', parameters, [max: params.max, offset: params.offset])
        def documentInstanceTotal = Document.executeQuery('select count(*) ' + sql, parameters)[0]
        [documentInstanceList: documentInstanceList, documentInstanceTotal: documentInstanceTotal, searchMap: searchMap]
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    // Return null if a GL account can be posted by the given meta-type or an error message if not
    private checkPostability(account, type) {
        if (account?.active && bookService.hasAccountAccess(account)) {
            if (account.type) {
                switch (type) {
                    case 'invoice':
                        if (!account.type.allowInvoices) return message(code: 'account.no.invoices', args: [account.code], default: "Posting of invoices or credit notes is not allowed to GL account ${account.code}")
                        break

                    case 'cash':
                        if (!account.type.allowCash) return message(code: 'account.no.cash', args: [account.code], default: "Posting of payments or receipts is not allowed to GL account ${account.code}")
                        break

                    case 'provision':
                        if (!account.type.allowProvisions) return message(code: 'account.no.provisions', args: [account.code], default: "Posting of accruals or prepayments is not allowed to GL account ${account.code}")
                        break

                    case 'journal':
                        if (!account.type.allowJournals) return message(code: 'account.no.journals', args: [account.code], default: "Posting of journals is not allowed to GL account ${account.code}")
                        break
                }
            }
        } else {
            return message(code: 'account.not.exists', default: 'Invalid GL account')
        }

        return null // No error so posting is allowed
    }

    private getTest(str) {
        return (str.indexOf('%') >= 0 || str.indexOf('_') >= 0) ? 'like' : '='
    }
}

