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
import org.apache.commons.collections.set.ListOrderedSet

class SetoffController {

    // Injected services
    def utilService
    def bookService
    def postingService

    // Security settings
    def activities = [default: 'sotemplate', template: 'sojournal', journal: 'sojournal', lines: 'sojournal',
            auto: 'sojournal', journaling: 'sojournal', enquire: 'enquire']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', templateLines: 'POST', lines: 'POST', auto: 'POST', journaling: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        def max = utilService.max
        def offset = utilService.offset
        def listing = TemplateDocument.findAll("from TemplateDocument as x where x.type.company = ? and x.type.type.code = 'SOJ' order by x.type.code, x.description",
                [utilService.currentCompany()], [max: max, offset: offset])
        def total = TemplateDocument.executeQuery("select count(*) from TemplateDocument as x where x.type.company = ? and x.type.type.code = 'SOJ'", [utilService.currentCompany()])[0]
        [templateDocumentInstanceList: listing, templateDocumentInstanceTotal: total]
    }

    def show() {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!templateDocumentInstance || templateDocumentInstance.type.type.code != 'SOJ') {
            flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
            redirect(action: 'list')
        } else {
            return [templateDocumentInstance: templateDocumentInstance]
        }
    }

    def delete() {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (templateDocumentInstance && templateDocumentInstance.type.type.code == 'SOJ') {
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
        if (!templateDocumentInstance || templateDocumentInstance.type.type.code != 'SOJ') {
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
        if (templateDocumentInstance && templateDocumentInstance.type.type.code == 'SOJ') {
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
            if (!templateDocumentInstance || templateDocumentInstance.type.type.code != 'SOJ') {
                flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
                redirect(action: 'list')
                return
            }
        } else {
            templateDocumentInstance = new TemplateDocument()
        }

        templateDocumentInstance.properties['type', 'currency', 'description', 'reference', 'sourceAffectsTurnover'] = params

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
        if (templateDocumentInstance && templateDocumentInstance.type.type.code == 'SOJ') {
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
            documentInstance.sourceAffectsTurnover = templateDocumentInstance.sourceAffectsTurnover
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
                    if (line.documentValue != null) {
                        if (line.documentValue < 0.0) {
                            docLine.documentCredit = -line.documentValue
                        } else {
                            docLine.documentDebit = line.documentValue
                        }
                    }

                    documentInstance.addToLines(docLine)
                }
            } else {

                // Add some lines
                for (int i = 0; i < 10; i++) documentInstance.addToLines(new Line())
            }

            render(view: 'journal', model: getModel(utilService.currentCompany(), documentInstance))
        } else {
            flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
            redirect(action: 'journal')
        }
    }

    def journal() {
        def documentInstance = new Document()
        for (int i = 0; i < 10; i++) documentInstance.addToLines(new Line())
        getModel(utilService.currentCompany(), documentInstance)
    }

    def lines() {
        def documentInstance = new Document()
        if (!params.code && params.sourceNumber) params.code = params.sourceNumber  // A disabled field would not be in the params, so we keep a copy in a hidden field
        documentInstance.properties['type', 'period', 'currency', 'code', 'description', 'documentDate', 'dueDate', 'reference', 'sourceAffectsTurnover'] = params

        // Load the document lines from the request parameters
        postingService.refreshDocumentLines(documentInstance, params)

        // Add the lines
        for (int i = 0; i < 10; i++) documentInstance.addToLines(new Line())

        render(view: 'journal', model: getModel(utilService.currentCompany(), documentInstance))
    }

    def auto() {
        def documentInstance = new Document()
        if (postDocument(documentInstance, params)) {           // Document comes back from posting in its debit/credit form

            def valid = true
            for (line in documentInstance.lines) {
                if (!postingService.autoAllocate(line.customer ?: line.supplier)) valid = false
            }

            if (valid) {
                flash.message = message(code: 'document.created', args: [documentInstance.type.code, documentInstance.code], default: "Document ${documentInstance.type.code}${documentInstance.code} created")
            } else {
                flash.message = message(code: 'document.not.allocated', args: [documentInstance.type.code, documentInstance.code], default: "Document ${documentInstance.type.code}${documentInstance.code} created but could not be allocated")
            }

            redirect(action: 'journal')
        } else {               // Document comes back in its data entry form if posting failed
            render(view: 'journal', model: getModel(utilService.currentCompany(), documentInstance))
        }
    }

    def journaling() {
        def documentInstance = new Document()
        if (postDocument(documentInstance, params)) {           // Document comes back from posting in its debit/credit form
            flash.message = message(code: 'document.created', args: [documentInstance.type.code, documentInstance.code], default: "Document ${documentInstance.type.code}${documentInstance.code} created")
            redirect(action: 'journal')
        } else {               // Document comes back in its data entry form if posting failed
            render(view: 'journal', model: getModel(utilService.currentCompany(), documentInstance))
        }
    }

    def enquire() {
        def model = bookService.loadDocumentModel(params, ['SOJ'])
        def documentInstance = model.documentInstance
        if (documentInstance.id) {
            def debit = 0.0
            def credit = 0.0
            def val, affectsTurnoverSet
            def parameters = [context: documentInstance, field: 'value', currency: model.displayCurrency]
            for (line in documentInstance.lines) {
                if (!affectsTurnoverSet) {
                    documentInstance.sourceAffectsTurnover = line.affectsTurnover
                    affectsTurnoverSet = true
                }

                parameters.line = line
                val = bookService.getBookValue(parameters)
                if (val != null && !(val instanceof String)) {
                    if (val < 0.0) {
                        credit -= val
                    } else {
                        debit += val
                    }
                }

                updateTransientLineData(line)
            }

            model.totalInstance = [debit: debit, credit: credit, scale: parameters.scale]
        }

        model
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private postDocument(documentInstance, params) {
        def company = utilService.currentCompany()
        def companyCurrency = utilService.companyCurrency()
        if (!params.code && params.sourceNumber) params.code = params.sourceNumber  // A disabled field would not be in the params, so we keep a copy in a hidden field
        documentInstance.properties['type', 'period', 'currency', 'code', 'description', 'documentDate', 'dueDate', 'reference', 'sourceAffectsTurnover'] = params
        def documentDecs, account, temp
        def companyDecs = companyCurrency.decimals
        def now = utilService.fixDate()
        def removables = []                 // 'blank' lines that we can remove from the document just before posting
        def companyRate = 1.0    // The exchange rate we need to multiply document currency values by to get the company currency values
        def otherRates = [:]                // Other exchange rates we may use to convert from document currency values to GL account currency values
        def documentDebitTotal = 0.0
        def documentCreditTotal = 0.0
        def companySignedTotal = 0.0
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
            if (documentInstance.type == null || documentInstance.type.type.code != 'SOJ') {
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

        // Check out the dates
        if (valid) {
            if (documentInstance.documentDate >= now - 365 && documentInstance.documentDate <= now + 365 && documentInstance.documentDate == utilService.fixDate(documentInstance.documentDate)) {
                if (!documentInstance.dueDate || documentInstance.dueDate < documentInstance.documentDate || documentInstance.dueDate > documentInstance.documentDate + 250 || documentInstance.dueDate != utilService.fixDate(documentInstance.dueDate)) {
                    documentInstance.errorMessage(field: 'dueDate', code: 'document.dueDate.invalid', default: 'Due date is invalid')
                    valid = false
                }
            } else {
                documentInstance.errorMessage(field: 'documentDate', code: 'document.documentDate.invalid', default: 'Document date is invalid')
                valid = false
            }
        }

        // Get any document to company exchange rates we may need
        if (valid) {
            documentDecs = documentInstance.currency.decimals
            if (companyCurrency.code != documentInstance.currency.code) {
                companyRate = utilService.getExchangeRate(documentInstance.currency, companyCurrency, now)
                if (!companyRate) {
                    documentInstance.errorMessage(code: 'document.bad.exchangeRate', args: [documentInstance.currency.code, companyCurrency.code],
                            default: "No exchange rate available from ${documentInstance.currency.code} to ${companyCurrency.code}")
                    valid = false
                }
            }
        }

        // Step through each line checking it in detail
        if (valid) {
            num = 0
            for (line in documentInstance.lines) {
                num++

                // If this is intended to be an active line
                if (line.accountCode) {
                    if (line.accountType == 'ar') {
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

                    // Check they have entered either a debit or credit value for the line
                    if (line.documentDebit == null && line.documentCredit == null) {
                        temp = message(code: 'document.no.entry', default: 'Either a Debit or Credit value must be entered')
                        documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('documentDebit', null)
                        valid = false
                        break
                    }

                    // Check they haven't entered BOTH a debit AND a credit value for the line
                    if (line.documentDebit != null && line.documentCredit != null) {
                        temp = message(code: 'document.dup.entry', default: 'You may not enter both a Debit and Credit value')
                        documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('documentDebit', null)
                        valid = false
                        break
                    }

                    // Round any entered values and do the totalling
                    if (line.documentDebit != null) {
                        line.documentDebit = utilService.round(line.documentDebit, documentDecs)
                        documentDebitTotal += line.documentDebit
                        line.documentValue = line.documentDebit
                    } else {
                        line.documentCredit = utilService.round(line.documentCredit, documentDecs)
                        documentCreditTotal += line.documentCredit
                        line.documentValue = -line.documentCredit
                    }

                    if (line.documentValue == 0.0) {
                        temp = message(code: 'document.zero.entry', default: 'The line value cannot be zero')
                        documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue((line.documentDebit != null) ? 'documentDebit' : 'documentCredit', null)
                        valid = false
                        break
                    }

                    // Set up the line ready for posting.
                    line.companyValue = utilService.round(line.documentValue * companyRate, companyDecs)
                    line.generalValue = line.companyValue   // Control account is always in company currency
                    companySignedTotal += line.companyValue
                    line.affectsTurnover = documentInstance.sourceAffectsTurnover
                    if (account.currency.code == companyCurrency.code) {
                        line.accountValue = line.companyValue
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
                } else {

                    // These are non-active lines that, if the document passes all our checks here (other than final
                    // validation on call to the save method), we will remove before acutally saving the document.
                    removables << line
                }
            }
        }

        // Cross check the lines have the same debit and credit totals
        if (valid && documentDebitTotal != documentCreditTotal) {
            def dr = utilService.format(documentDebitTotal, documentDecs)
            def cr = utilService.format(documentCreditTotal, documentDecs)
            def diff
            if (documentDebitTotal > documentCreditTotal) {
                diff = utilService.format(documentDebitTotal - documentCreditTotal, documentDecs)
                temp = message(code: 'generic.credit', default: 'Credit')
            } else {
                diff = utilService.format(documentCreditTotal - documentDebitTotal, documentDecs)
                temp = message(code: 'generic.debit', default: 'Debit')
            }

            documentInstance.errorMessage(code: 'document.not.balancing', args: [dr, cr, diff, temp], default: "The debit total (${dr}) does not agree to the credit total (${cr}). It is short by ${diff} ${temp}")
            valid = false
        }

        // Remove any 'blank' lines and then post the document
        if (valid) {
            for (line in removables) {
                documentInstance.removeFromLines(line)
            }

            // Make sure the document balances from a company currency point of view
            if (companySignedTotal != 0.0) postingService.balanceDocument(documentInstance, companySignedTotal)

            valid = postingService.post(documentInstance)
        }

        return valid
    }

    private getModel(company, documentInstance) {
        def documentTypeList = DocumentType.findAll("from DocumentType as dt where dt.company = ? and dt.type.code = 'SOJ'", [company])
        def periodList = bookService.getOpenPeriods(company)
        def currencyList = ExchangeCurrency.findAllByCompany(company, [cache: true])
        if (!documentInstance.documentDate) {
            documentInstance.documentDate = utilService.fixDate()
            documentInstance.period = bookService.selectPeriod(periodList, documentInstance.documentDate)
        }

        if (!documentInstance.dueDate) documentInstance.dueDate = documentInstance.documentDate
        if (!documentInstance.currency) documentInstance.currency = utilService.companyCurrency()
        periodList = periodList.reverse()
        def settings = [:]
        settings.codeGenerate = documentInstance.type?.autoGenerate
        settings.codeEdit = documentInstance.type?.allowEdit
        settings.decimals = documentInstance.currency.decimals
        return [documentInstance: documentInstance, documentTypeList: documentTypeList, periodList: periodList, currencyList: currencyList, settings: settings]
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
        }
    }

    private getTemplateModel(company, templateDocumentInstance) {
        def documentTypeList = DocumentType.findAll("from DocumentType as dt where dt.company = ? and dt.type.code = 'SOJ'", [company])
        def currencyList = ExchangeCurrency.findAllByCompany(company, [cache: true])
        if (!templateDocumentInstance.currency) templateDocumentInstance.currency = utilService.companyCurrency()
        def settings = [:]
        settings.decimals = templateDocumentInstance.currency.decimals
        return [templateDocumentInstance: templateDocumentInstance, documentTypeList: documentTypeList, currencyList: currencyList, settings: settings]
    }

    private saveTemplate(templateDocumentInstance, params) {
        templateDocumentInstance.properties['type', 'currency', 'description', 'reference', 'sourceAffectsTurnover'] = params
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
        } else {
            num = 0
            for (line in templateDocumentInstance.lines) {
                num++
                if (line.accountCode) {
                    if (line.accountType == 'ar') {
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
                        line.customer = null
                    } else {
                        temp = message(code: 'document.bad.ledger', default: 'Invalid ledger')
                        templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('accountType', null)
                        valid = false
                        break
                    }

                    // Check they haven't entered BOTH a debit AND a credit value for the line
                    if (line.documentDebit != null && line.documentCredit != null) {
                        temp = message(code: 'document.dup.entry', default: 'You may not enter both a Debit and Credit value')
                        templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('documentDebit', null)
                        valid = false
                        break
                    }

                    // Round any entered values
                    if (line.documentDebit != null) {
                        line.documentDebit = utilService.round(line.documentDebit, documentDecs)
                        line.documentValue = line.documentDebit
                    } else if (line.documentCredit != null) {
                        line.documentCredit = utilService.round(line.documentCredit, documentDecs)
                        line.documentValue = -line.documentCredit
                    }
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
