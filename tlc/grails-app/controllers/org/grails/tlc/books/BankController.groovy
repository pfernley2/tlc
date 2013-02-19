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
import doc.Line
import doc.Total
import org.apache.commons.collections.set.ListOrderedSet

class BankController {

    // Injected services
    def utilService
    def bookService
    def postingService

    // Security settings
    def activities = [default: 'banktempl', template: 'bankentry', transact: 'bankentry', lines: 'bankentry',
            auto: 'bankentry', transacting: 'bankentry', enquire: 'enquire']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', templateLines: 'POST', lines: 'POST', auto: 'POST', transacting: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        def max = utilService.max
        def offset = utilService.offset
        def listing = TemplateDocument.findAll("from TemplateDocument as x where x.type.company = ? and x.type.type.code in ('BP', 'BR') order by x.type.code, x.description",
                [utilService.currentCompany()], [max: max, offset: offset])
        def total = TemplateDocument.executeQuery("select count(*) from TemplateDocument as x where x.type.company = ? and x.type.type.code in ('BP', 'BR')", [utilService.currentCompany()])[0]
        [templateDocumentInstanceList: listing, templateDocumentInstanceTotal: total]
    }

    def show() {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!templateDocumentInstance || !['BP', 'BR'].contains(templateDocumentInstance.type.type.code)) {
            flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
            redirect(action: 'list')
        } else {
            return [templateDocumentInstance: templateDocumentInstance]
        }
    }

    def delete() {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (templateDocumentInstance && ['BP', 'BR'].contains(templateDocumentInstance.type.type.code)) {
            try {
                templateDocumentInstance.delete(flush: true)
                flash.message = utilService.standardMessage('deleted', templateDocumentInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', templateDocumentInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!templateDocumentInstance || !['BP', 'BR'].contains(templateDocumentInstance.type.type.code)) {
            flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
            redirect(action: 'list')
        } else {
            for (line in templateDocumentInstance.lines) {
                updateTransientLineData(line, true)

                if (line.documentValue) {
                    if (line.documentValue < 0.0) {
                        line.documentCredit = -line.documentValue
                    } else {
                        line.documentDebit = line.documentValue
                    }
                }
            }

            return getTemplateModel(utilService.currentCompany(), templateDocumentInstance)
        }
    }

    def update(Long version) {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (templateDocumentInstance && ['BP', 'BR'].contains(templateDocumentInstance.type.type.code)) {
            if (version != null && templateDocumentInstance.version > version) {
                templateDocumentInstance.errorMessage(code: 'locking.failure', domain: 'templateDocument')
                render(view: 'edit', model: getTemplateModel(utilService.currentCompany(), templateDocumentInstance))
                return
            }

            if (saveTemplate(templateDocumentInstance, params)) {
                flash.message = utilService.standardMessage('updated', templateDocumentInstance)
                redirect(action: 'show', id: templateDocumentInstance.id)
            } else {
                render(view: 'edit', model: getTemplateModel(utilService.currentCompany(), templateDocumentInstance))
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def templateDocumentInstance = new TemplateDocument()
        for (int i = 0; i < 10; i++) templateDocumentInstance.addToLines(new TemplateLine())
        getTemplateModel(utilService.currentCompany(), templateDocumentInstance)
    }

    def save() {
        def templateDocumentInstance = new TemplateDocument()
        if (saveTemplate(templateDocumentInstance, params)) {
            flash.message = utilService.standardMessage('created', templateDocumentInstance)
            redirect(action: 'show', id: templateDocumentInstance.id)
        } else {
            render(view: 'create', model: getTemplateModel(utilService.currentCompany(), templateDocumentInstance))
        }
    }

    def templateLines() {
        def templateDocumentInstance
        if (params.id) {
            templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
            if (!templateDocumentInstance || !['BP', 'BR'].contains(templateDocumentInstance.type.type.code)) {
                flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
                redirect(action: 'list')
                return
            }
        } else {
            templateDocumentInstance = new TemplateDocument()
        }

        templateDocumentInstance.properties['type', 'currency', 'description', 'reference', 'sourceCode'] = params

        // Load the template lines from the request parameters
        postingService.refreshTemplateLines(templateDocumentInstance, params)

        // Add the lines
        for (int i = 0; i < 10; i++) templateDocumentInstance.addToLines(new TemplateLine())

        // Grails would automatically save an existing record that was modified if we didn't discard it
        if (templateDocumentInstance.id) templateDocumentInstance.discard()

        render(view: params.view, model: getTemplateModel(utilService.currentCompany(), templateDocumentInstance))
    }

    def template() {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (templateDocumentInstance && ['BP', 'BR'].contains(templateDocumentInstance.type.type.code)) {
            def documentInstance = new Document()
            documentInstance.type = templateDocumentInstance.type
            if (documentInstance.type.autoGenerate) {
                documentInstance.type.discard()
                def documentTypeInstance = DocumentType.lock(documentInstance.type.id)
                documentInstance.code = documentTypeInstance.nextSequenceNumber.toString()
                documentTypeInstance.nextSequenceNumber += 1
                documentTypeInstance.saveThis()
            }
            documentInstance.currency = templateDocumentInstance.currency
            documentInstance.reference = templateDocumentInstance.reference
            documentInstance.description = templateDocumentInstance.description
            if (templateDocumentInstance.account && bookService.hasAccountAccess(templateDocumentInstance.account)) documentInstance.sourceCode = templateDocumentInstance.account.code
            if (templateDocumentInstance.lines) {
                documentInstance.lines = new ListOrderedSet()
                def docLine
                for (line in templateDocumentInstance.lines) {
                    updateTransientLineData(line, true)
                    docLine = new Line()
                    docLine.accountCode = line.accountCode
                    docLine.accountName = line.accountName
                    docLine.accountType = line.accountType
                    docLine.description = line.description
                    docLine.documentValue = line.documentValue
                    documentInstance.addToLines(docLine)
                }
            } else {

                // Add some lines
                for (int i = 0; i < 10; i++) documentInstance.addToLines(new Line())
            }

            render(view: 'transact', model: getModel(utilService.currentCompany(), documentInstance))
        } else {
            flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
            redirect(action: 'transact')
        }
    }

    def transact() {
        def documentInstance = new Document()
        for (int i = 0; i < 10; i++) documentInstance.addToLines(new Line())
        getModel(utilService.currentCompany(), documentInstance)
    }

    def lines() {
        def documentInstance = new Document()
        if (!params.code && params.sourceNumber) params.code = params.sourceNumber  // A disabled field would not be in the params, so we keep a copy in a hidden field
        documentInstance.properties['type', 'period', 'currency', 'code', 'description', 'documentDate', 'reference', 'sourceCode', 'sourceTotal'] = params

        // Load the document lines from the request parameters
        postingService.refreshDocumentLines(documentInstance, params)

        // Add the lines
        for (int i = 0; i < 10; i++) documentInstance.addToLines(new Line())

        render(view: 'transact', model: getModel(utilService.currentCompany(), documentInstance))
    }

    def auto() {
        def documentInstance = new Document()
        if (postDocument(documentInstance, params)) {           // Document comes back from posting in its debit/credit form

            def valid = true
            def account
            for (line in documentInstance.lines) {
                account = line.customer ?: line.supplier
                if (account && !postingService.autoAllocate(account)) valid = false
            }

            if (valid) {
                flash.message = message(code: 'document.created', args: [documentInstance.type.code, documentInstance.code], default: "Document ${documentInstance.type.code}${documentInstance.code} created")
            } else {
                flash.message = message(code: 'document.not.allocated', args: [documentInstance.type.code, documentInstance.code], default: "Document ${documentInstance.type.code}${documentInstance.code} created but could not be allocated")
            }

            redirect(action: 'transact')
        } else {               // Document comes back in its data entry form if posting failed
            render(view: 'transact', model: getModel(utilService.currentCompany(), documentInstance))
        }
    }

    def transacting() {
        def documentInstance = new Document()
        if (postDocument(documentInstance, params)) {           // Document comes back from posting in its debit/credit form
            flash.message = message(code: 'document.created', args: [documentInstance.type.code, documentInstance.code], default: "Document ${documentInstance.type.code}${documentInstance.code} created")
            redirect(action: 'transact')
        } else {               // Document comes back in its data entry form if posting failed
            render(view: 'transact', model: getModel(utilService.currentCompany(), documentInstance))
        }
    }

    def enquire() {
        def model = bookService.loadDocumentModel(params, ['BP', 'BR'])
        def documentInstance = model.documentInstance
        if (documentInstance.id) {
            model.totalInstance = bookService.getTotalLine(documentInstance)
            for (line in documentInstance.lines) updateTransientLineData(line)
        }

        model
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private postDocument(documentInstance, params) {
        def company = utilService.currentCompany()
        def companyCurrency = utilService.companyCurrency()
        if (!params.code && params.sourceNumber) params.code = params.sourceNumber  // A disabled field would not be in the params, so we keep a copy in a hidden field
        documentInstance.properties['type', 'period', 'currency', 'code', 'description', 'documentDate', 'reference', 'sourceCode', 'sourceTotal'] = params
        def documentDecs, account, temp, bankAccount, bankDecs, bankValue
        def companyDecs = companyCurrency.decimals
        def now = utilService.fixDate()
        def removables = []                 // 'blank' lines that we can remove from the document just before posting
        def companyRate = 1.0               // The exchange rate we need to multiply document currency values by to get the company currency values
        def bankRate = 1.0                  // The exchange rate we need to multiply document currency values by to get the bank/cash account currency values
        def otherRates = [:]                // Other exchange rates we may use to convert from document currency values to GL account currency values
        def documentTotal = 0.0
        def bankTotal = 0.0
        def companyTotal = 0.0
        def arControl = bookService.getControlAccount(company, 'ar')
        def apControl = bookService.getControlAccount(company, 'ap')
        def customers = [:]
        def suppliers = [:]

        // Process the document header, start by checking for data binding errors
        def valid = !documentInstance.hasErrors()

        // Load the document lines from the request parameters and check for data binding errors
		// in the line at the same time. We do this whether the header had a fault or not
        def num = postingService.refreshDocumentLines(documentInstance, params)
        if (num) {
            documentInstance.errorMessage(code: 'document.line.data', args: [num], default: "Line ${num} has a 'data type' error")
            valid = false
        }

        // Make sure we have the sub-ledger control accounts
        if (!arControl) {
            documentInstance.errorMessage(code: 'document.no.control', args: ['ar'], default: 'Could not find the ar control account in the General Ledger')
            valid = false
        }

        if (!apControl) {
            documentInstance.errorMessage(code: 'document.no.control', args: ['ap'], default: 'Could not find the ap control account in the General Ledger')
            valid = false
        }

        // Now get on to standard validation, starting with the header: Make sure references are to the correct company objects
        if (valid) {
            utilService.verify(documentInstance, ['type', 'period', 'currency'])
            if (documentInstance.type == null || !['BP', 'BR'].contains(documentInstance.type.type.code)) {
                documentInstance.errorMessage(field: 'type', code: 'document.bad.type', default: 'Invalid document type')
                valid = false
            }

            if (documentInstance.period == null || documentInstance.period.status != 'open') {
                documentInstance.errorMessage(field: 'period', code: 'document.bad.period', default: 'Invalid document period')
                valid = false
            }

            if (documentInstance.currency == null) {
                documentInstance.errorMessage(field: 'currency', code: 'document.bad.currency', default: 'Invalid document currency')
                valid = false
            }
        }

        // Check out the bank/cash account and get any exchange rate required
        if (valid) {
            if (documentInstance.sourceCode) {
                bankAccount = Account.findBySecurityCodeAndCode(company.securityCode, documentInstance.sourceCode)
                if (!bankAccount || bankAccount.type?.code != 'bank' || !bankAccount.active || !bookService.hasAccountAccess(bankAccount)) {
                    documentInstance.errorMessage(field: 'sourceCode', code: 'bank.not.exists', default: 'Invalid Bank account')
                    valid = false
                } else {
                    bankDecs = bankAccount.currency.decimals
                    if (bankAccount.currency.code != documentInstance.currency.code) {
                        bankRate = utilService.getExchangeRate(documentInstance.currency, bankAccount.currency, now)
                        if (!bankRate) {
                            documentInstance.errorMessage(code: 'document.bad.exchangeRate', args: [documentInstance.currency.code, bankAccount.currency.code],
                                    default: "No exchange rate available from ${documentInstance.currency.code} to ${bankAccount.currency.code}")
                            valid = false
                        }
                    }
                }
            } else {
                documentInstance.errorMessage(field: 'sourceCode', code: 'bank.not.exists', default: 'Invalid Bank account')
                valid = false
            }
        }

        // Get any document to company exchange rates we may need
        if (valid) {
            documentDecs = documentInstance.currency.decimals
            if (companyCurrency.code != documentInstance.currency.code) {
                if (companyCurrency.code == bankAccount.currency.code) {
                    companyRate = bankRate
                } else {
                    companyRate = utilService.getExchangeRate(documentInstance.currency, companyCurrency, now)
                    if (!companyRate) {
                        documentInstance.errorMessage(code: 'document.bad.exchangeRate', args: [documentInstance.currency.code, companyCurrency.code],
                                default: "No exchange rate available from ${documentInstance.currency.code} to ${companyCurrency.code}")
                        valid = false
                    }
                }
            }
        }

        // Check out the dates
        if (valid) {
            if (documentInstance.documentDate < now - 365 || documentInstance.documentDate > now + 365 || documentInstance.documentDate != utilService.fixDate(documentInstance.documentDate)) {
                documentInstance.errorMessage(field: 'documentDate', code: 'document.documentDate.invalid', default: 'Document date is invalid')
                valid = false
            }
        }

        // Check they've entered a total
        if (valid && documentInstance.sourceTotal == 0.0) {
            documentInstance.errorMessage(field: 'sourceTotal', code: 'document.zero.total', default: 'The Total value cannot be zero')
            valid = false
        }

        // Step through each line checking it in detail
        if (valid) {
            num = 0
            for (line in documentInstance.lines) {
                num++

                // If this is intended to be an active line
                if (line.accountCode) {

                    if (line.accountType == 'gl') {

                        // Make sure the GL account code is expended for mnemonics and case is correct
                        temp = bookService.expandAccountCode(utilService.currentUser(), line.accountCode)
                        if (!temp) {
                            temp = message(code: 'account.not.exists', default: 'Invalid GL account')
                            documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        line.accountCode = temp

                        // See if the GL account actually exists
                        account = bookService.getAccount(company, line.accountCode)
                        if (account instanceof String) {
                            documentInstance.errorMessage(code: 'document.line.message', args: [num, account], default: "Line ${num}: ${account}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        // Make sure the GL account is active and that the user is allowed to access this account and
                        // that the account is not restricted as to what sort of documents can be posted to it
                        if (account?.active && bookService.hasAccountAccess(account)) {
                            valid = postingService.canPostDocumentToAccount(documentInstance, line, num, account)
                            if (!valid) break
                        } else {
                            temp = message(code: 'account.not.exists', default: 'Invalid GL account')
                            documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        // Make sure it's not the Bank Account they're trying to post to
                        if (account.code == bankAccount.code) {
                            temp = message(code: 'bank.not.self', default: 'You cannot post a bank transaction to the originating Bank account')
                            documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        line.account = account
                    } else if (line.accountType == 'ar') {
                        account = Customer.findByCompanyAndCode(company, bookService.fixCustomerCase(line.accountCode))
                        if (!account?.active || !bookService.hasCustomerAccess(account)) {
                            temp = message(code: 'document.customer.invalid', default: 'Invalid customer')
                            documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        if (customers.containsKey(account.code)) {
                            temp = message(code: 'document.customer.duplicate', args: [account.code], default: "Customer ${account.code} is a duplicate. Please combine duplicates in to a single entry.")
                            documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        } else {
                            customers.put(account.code, null)
                        }

                        line.account = arControl
                        line.customer = account
                    } else if (line.accountType == 'ap') {
                        account = Supplier.findByCompanyAndCode(company, bookService.fixSupplierCase(line.accountCode))
                        if (!account?.active || !bookService.hasSupplierAccess(account)) {
                            temp = message(code: 'document.supplier.invalid', default: 'Invalid supplier')
                            documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        if (suppliers.containsKey(account.code)) {
                            temp = message(code: 'document.supplier.duplicate', args: [account.code], default: "Supplier ${account.code} is a duplicate. Please combine duplicates in to a single entry.")
                            documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        } else {
                            suppliers.put(account.code, null)
                        }

                        line.account = apControl
                        line.supplier = account
                    } else {
                        temp = message(code: 'document.bad.ledger', default: 'Invalid ledger')
                        documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('accountType', null)
                        valid = false
                        break
                    }

                    // Round any entered value
                    if (line.documentValue != null) line.documentValue = utilService.round(line.documentValue, documentDecs)

                    // Make sure there is a value
                    if (!line.documentValue) {
                        temp = message(code: 'document.zero.entry', default: 'The line value cannot be zero')
                        documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('documentValue', null)
                        valid = false
                        break
                    }

                    // Do the totalling and set up the line ready for posting.
                    documentTotal += line.documentValue
                    line.companyValue = utilService.round(line.documentValue * companyRate, companyDecs)
                    companyTotal += line.companyValue
                    bankValue = utilService.round(line.documentValue * bankRate, bankDecs)
                    bankTotal += bankValue
                    account = line.account  // Set account to the GL account to be posted to
                    if (account.currency.code == companyCurrency.code) {
                        line.generalValue = line.companyValue
                    } else if (account.currency.code == bankAccount.currency.code) {
                        line.generalValue = bankValue
                    } else if (account.currency.code == documentInstance.currency.code) {
                        line.generalValue = line.documentValue
                    } else {
                        temp = otherRates.get(account.currency.code)
                        if (!temp) {
                            temp = utilService.getExchangeRate(documentInstance.currency, account.currency, now)
                            if (!temp) {
                                documentInstance.errorMessage(code: 'document.bad.exchangeRate', args: [documentInstance.currency.code, account.currency.code],
                                        default: "No exchange rate available from ${documentInstance.currency.code} to ${account.currency.code}")
                                valid = false
                                break
                            }

                            otherRates.put(account.currency.code, temp)
                        }

                        line.generalValue = utilService.round(line.documentValue * temp, account.currency.decimals)
                    }

                    // See if we have a sub-ledger account
                    account = line.customer ?: line.supplier
                    if (account) {
                        if (account.currency.code == companyCurrency.code) {
                            line.accountValue = line.companyValue
                        } else if (account.currency.code == bankAccount.currency.code) {
                            line.accountValue = bankValue
                        } else if (account.currency.code == documentInstance.currency.code) {
                            line.accountValue = line.documentValue
                        } else {
                            temp = otherRates.get(account.currency.code)
                            if (!temp) {
                                temp = utilService.getExchangeRate(documentInstance.currency, account.currency, now)
                                if (!temp) {
                                    documentInstance.errorMessage(code: 'document.bad.exchangeRate', args: [documentInstance.currency.code, account.currency.code],
                                            default: "No exchange rate available from ${documentInstance.currency.code} to ${account.currency.code}")
                                    valid = false
                                    break
                                }

                                otherRates.put(account.currency.code, temp)
                            }

                            line.accountValue = utilService.round(line.documentValue * temp, account.currency.decimals)
                        }

                        line.accountUnallocated = line.accountValue
                        line.companyUnallocated = line.companyValue
                    }
                } else {

                    // These are non-active lines that, if the document passes all our checks here (other than final
                    // validation on call to the save method), we will remove before acutally saving the document.
                    removables << line
                }
            }
        }

        // Cross check the lines to the header
        if (valid && documentTotal != documentInstance.sourceTotal) {
            documentInstance.errorMessage(field: 'sourceTotal', code: 'document.total.mismatch', default: 'The document total value does not agree to the sum of the total values of the lines')
            valid = false
        }

        // Create the total line, remove any 'blank' lines and then post the document
        if (valid) {
			def doc = new Total(description: documentInstance.description, documentValue: documentTotal, generalValue: bankTotal, companyValue: companyTotal)
			doc.account = bankAccount
            documentInstance.addToTotal(doc)

            for (line in removables) {
                documentInstance.removeFromLines(line)
            }

            valid = postingService.post(documentInstance)
        }

        return valid
    }

    private getModel(company, documentInstance) {
        def documentTypeList = DocumentType.findAll("from DocumentType as dt where dt.company = ? and dt.type.code in ('BP', 'BR')", [company])
        def periodList = bookService.getActivePeriods(company)
        def currencyList = ExchangeCurrency.findAllByCompany(company, [cache: true])
        if (!documentInstance.documentDate) documentInstance.documentDate = utilService.fixDate()
        if (!documentInstance.period) documentInstance.period = bookService.selectPeriod(periodList, documentInstance.documentDate)
        periodList = periodList.reverse()
        def bankAccountList = []
        def bankAccount
        def banks = Account.findAll("from Account as x where x.securityCode = ? and x.active = ? and x.type.code = 'bank' order by x.name", [company.securityCode, true])
        for (bank in banks) {
            if (bookService.hasAccountAccess(bank)) {
                bankAccountList << bank
                if (bank.code == documentInstance.sourceCode) bankAccount = bank
            }
        }

        if (!bankAccount) documentInstance.sourceCode = null
        if (!documentInstance.currency) documentInstance.currency = bankAccount?.currency ?: utilService.companyCurrency()
        def settings = [:]
        settings.codeGenerate = documentInstance.type?.autoGenerate
        settings.codeEdit = documentInstance.type?.allowEdit
        settings.decimals = documentInstance.currency.decimals
        return [documentInstance: documentInstance, bankAccountList: bankAccountList, documentTypeList: documentTypeList,
                periodList: periodList, currencyList: currencyList, settings: settings]
    }

    private updateTransientLineData(line, isTemplate = false) {
        if (line.customer) {
            line.accountType = 'ar'
            if (!isTemplate || bookService.hasCustomerAccess(line.customer)) {
                line.accountCode = line.customer.code
                line.accountName = line.customer.name
            }
        } else if (line.supplier) {
            line.accountType = 'ap'
            if (!isTemplate || bookService.hasSupplierAccess(line.supplier)) {
                line.accountCode = line.supplier.code
                line.accountName = line.supplier.name
            }
        } else {
            line.accountType = 'gl'
            if (isTemplate) {
                if (line.account && bookService.hasAccountAccess(line.account)) {
                    line.accountCode = line.account.code
                    line.accountName = line.account.name
                }
            } else {
                line.accountCode = line.balance.account.code
                line.accountName = line.balance.account.name
            }
        }
    }

    private getTemplateModel(company, templateDocumentInstance) {
        def documentTypeList = DocumentType.findAll("from DocumentType as dt where dt.company = ? and dt.type.code in ('BP', 'BR')", [company])
        def currencyList = ExchangeCurrency.findAllByCompany(company, [cache: true])
        if (!templateDocumentInstance.sourceCode) templateDocumentInstance.sourceCode = templateDocumentInstance.account?.code
        def bankAccountList = []
        def bankAccount
        def banks = Account.findAll("from Account as x where x.securityCode = ? and x.type.code = 'bank' order by x.name", [company.securityCode])
        for (bank in banks) {
            if (bookService.hasAccountAccess(bank)) {
                bankAccountList << bank
                if (bank.code == templateDocumentInstance.sourceCode) bankAccount = bank
            }
        }

        if (!bankAccount) templateDocumentInstance.sourceCode = null
        if (!templateDocumentInstance.currency) templateDocumentInstance.currency = bankAccount?.currency ?: utilService.companyCurrency()
        def settings = [:]
        settings.decimals = templateDocumentInstance.currency.decimals
        return [templateDocumentInstance: templateDocumentInstance, bankAccountList: bankAccountList, documentTypeList: documentTypeList,
                currencyList: currencyList, settings: settings]
    }

    private saveTemplate(templateDocumentInstance, params) {
        templateDocumentInstance.properties['type', 'currency', 'description', 'reference', 'sourceCode'] = params
        utilService.verify(templateDocumentInstance, ['type', 'currency'])             // Ensure correct references
        def valid = !templateDocumentInstance.hasErrors()
        def removables = []
        def documentDecs = templateDocumentInstance.currency?.decimals
        def account, temp
        def customers = [:]
        def suppliers = [:]

        // Load the template lines from the request parameters and check for data binding errors
		// in the line at the same time. We do this whether the header had a fault or not
        def num = postingService.refreshTemplateLines(templateDocumentInstance, params)
        if (num) {
            templateDocumentInstance.errorMessage(code: 'document.line.data', args: [num], default: "Line ${num} has a 'data type' error")
            valid = false
        }

        // Check out the bank account
        if (valid) {
            if (templateDocumentInstance.sourceCode) {
                account = Account.findBySecurityCodeAndCode(utilService.currentCompany().securityCode, templateDocumentInstance.sourceCode)
                if (!account || account.type?.code != 'bank' || !bookService.hasAccountAccess(account)) {
                    templateDocumentInstance.errorMessage(field: 'sourceCode', code: 'bank.not.exists', default: 'Invalid Bank account')
                    valid = false
                } else {
                    templateDocumentInstance.account = account
                }
            } else {
                templateDocumentInstance.account = null
            }
        }

        if (valid) {
            num = 0
            for (line in templateDocumentInstance.lines) {
                num++
                if (line.accountCode) {
                    if (line.accountType == 'gl') {

                        // Make sure the GL account code is expanded for mnemonics and case is correct
                        temp = bookService.expandAccountCode(utilService.currentUser(), line.accountCode)
                        if (!temp) {
                            temp = message(code: 'account.not.exists', default: 'Invalid GL account')
                            templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        line.accountCode = temp

                        // See if the GL account actually exists
                        account = bookService.getAccount(utilService.currentCompany(), line.accountCode)
                        if (account instanceof String) {
                            templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, account], default: "Line ${num}: ${account}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        // Make sure the GL account is active and that the user is allowed to access this account and
                        // that the account is not restricted as to what sort of documents can be posted to it
                        if (account?.active && bookService.hasAccountAccess(account)) {
                            valid = postingService.canPostDocumentToAccount(templateDocumentInstance, line, num, account)
                            if (!valid) break
                        } else {
                            temp = message(code: 'account.not.exists', default: 'Invalid GL account')
                            templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        // Make sure it's not the Bank Account they're trying to post to
                        if (account.code == templateDocumentInstance.sourceCode) {
                            temp = message(code: 'bank.not.self', default: 'You cannot post a bank transaction to the originating Bank account')
                            templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        line.account = account
                        line.customer = null
                        line.supplier = null
                    } else if (line.accountType == 'ar') {
                        account = Customer.findByCompanyAndCode(utilService.currentCompany(), bookService.fixCustomerCase(line.accountCode))
                        if (!account?.active || !bookService.hasCustomerAccess(account)) {
                            temp = message(code: 'document.customer.invalid', default: 'Invalid customer')
                            templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        if (customers.containsKey(account.code)) {
                            temp = message(code: 'document.customer.duplicate', args: [account.code], default: "Customer ${account.code} is a duplicate. Please combine duplicates in to a single entry.")
                            templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
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
                        account = Supplier.findByCompanyAndCode(utilService.currentCompany(), bookService.fixSupplierCase(line.accountCode))
                        if (!account?.active || !bookService.hasSupplierAccess(account)) {
                            temp = message(code: 'document.supplier.invalid', default: 'Invalid supplier')
                            templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('accountCode', null)
                            valid = false
                            break
                        }

                        if (suppliers.containsKey(account.code)) {
                            temp = message(code: 'document.supplier.duplicate', args: [account.code], default: "Supplier ${account.code} is a duplicate. Please combine duplicates in to a single entry.")
                            templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
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
                        templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('accountType', null)
                        valid = false
                        break
                    }

                    // Round any entered value
                    if (line.documentValue != null) line.documentValue = utilService.round(line.documentValue, documentDecs)
                } else {
                    removables << line
                }
            }
        }

        // Remove any 'blank' lines and then save the document
        if (valid) {
            for (line in removables) {
                templateDocumentInstance.removeFromLines(line)

                // Need to delete the items as removing them from the association dosn't do it
                if (line.id) {
                    line.delete(flush: true)
                    line.discard()
                }
            }

            valid = templateDocumentInstance.save(flush: true)  // With deep validation
        }

        return valid
    }
}
