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
import doc.Line
import java.text.NumberFormat
import org.apache.commons.collections.set.ListOrderedSet

class PurchaseController {

    // Injected services
    def utilService
    def bookService
    def postingService

    // Security settings
    def activities = [default: 'aptemplate', template: 'apinvoice', invoice: 'apinvoice', lines: 'apinvoice', invoicing: 'apinvoice',
            auto: 'apinvoice', manual: 'apinvoice', allocate: 'apinvoice', allocating: 'apinvoice', enquire: 'enquire']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', templateLines: 'POST', lines: 'POST', invoicing: 'POST',
            auto: 'POST', manual: 'POST', allocating: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        def max = utilService.max
        def offset = utilService.offset
        def listing = TemplateDocument.findAll("from TemplateDocument as x where x.type.company = ? and x.type.type.code in ('PI', 'PC') order by x.type.code, x.description",
                [utilService.currentCompany()], [max: max, offset: offset])
        def total = TemplateDocument.executeQuery("select count(*) from TemplateDocument as x where x.type.company = ? and x.type.type.code in ('PI', 'PC')", [utilService.currentCompany()])[0]
        [templateDocumentInstanceList: listing, templateDocumentInstanceTotal: total]
    }

    def show() {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!templateDocumentInstance || !['PI', 'PC'].contains(templateDocumentInstance.type.type.code)) {
            flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
            redirect(action: 'list')
        } else {
            return [templateDocumentInstance: templateDocumentInstance]
        }
    }

    def delete() {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (templateDocumentInstance && ['PI', 'PC'].contains(templateDocumentInstance.type.type.code)) {
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
        if (!templateDocumentInstance || !['PI', 'PC'].contains(templateDocumentInstance.type.type.code)) {
            flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
            redirect(action: 'list')
        } else {
            for (line in templateDocumentInstance.lines) {
                if (line.account && bookService.hasAccountAccess(line.account)) {
                    line.accountCode = line.account.code
                    line.accountName = line.account.name
                }
            }

            return getTemplateModel(utilService.currentCompany(), templateDocumentInstance)
        }
    }

    def update(Long version) {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (templateDocumentInstance && ['PI', 'PC'].contains(templateDocumentInstance.type.type.code)) {
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
            if (!templateDocumentInstance || !['PI', 'PC'].contains(templateDocumentInstance.type.type.code)) {
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
        if (templateDocumentInstance && ['PI', 'PC'].contains(templateDocumentInstance.type.type.code)) {
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
            if (templateDocumentInstance.supplier && bookService.hasSupplierAccess(templateDocumentInstance.supplier)) {
                documentInstance.sourceCode = templateDocumentInstance.supplier.code
                documentInstance.sourceName = templateDocumentInstance.supplier.name
            }

            if (templateDocumentInstance.lines) {
                documentInstance.lines = new ListOrderedSet()
                def docLine
                for (line in templateDocumentInstance.lines) {
                    docLine = new Line()
                    if (line.account && bookService.hasAccountAccess(line.account)) {
                        docLine.accountCode = line.account.code
                        docLine.accountName = line.account.name
                    }

                    docLine.description = line.description
                    docLine.taxCode = line.taxCode
                    docLine.documentValue = line.documentValue
                    docLine.documentTax = line.documentTax
                    docLine.documentTotal = line.documentTotal

                    documentInstance.addToLines(docLine)
                }
            } else {

                // Add some lines
                for (int i = 0; i < 10; i++) documentInstance.addToLines(new Line())
            }

            render(view: 'invoice', model: postingService.getSubLedgerInvoiceModel(utilService.currentCompany(), 'supplier', documentInstance))
        } else {
            flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
            redirect(action: 'invoice')
        }
    }

    def invoice() {
        postingService.createSubLedgerInvoice('supplier')
    }

    def lines() {
        render(view: 'invoice', model: postingService.addSubLedgerInvoiceLines('supplier', params))
    }

    def invoicing() {
        def result = postingService.postSubLedgerInvoice('supplier', params)
        if (result instanceof Document) {           // Document comes back in its debit/credit form
            flash.message = message(code: 'document.created', args: [result.type.code, result.code], default: "Document ${result.type.code}${result.code} created")
            redirect(action: 'invoice')
        } else {                                    // Document comes back in its data entry form
            render(view: 'invoice', model: result)
        }
    }

    def auto() {
        def result = postingService.postSubLedgerInvoice('supplier', params)
        if (result instanceof Document) {           // Document comes back in its debit/credit form
            def account = bookService.getTotalLine(result).supplier

            if (postingService.autoAllocate(account)) {
                flash.message = message(code: 'document.created', args: [result.type.code, result.code], default: "Document ${result.type.code}${result.code} created")
            } else {
                flash.message = message(code: 'document.not.allocated', args: [result.type.code, result.code], default: "Document ${result.type.code}${result.code} created but could not be allocated")
            }

            redirect(action: 'invoice')
        } else {                                    // Document comes back in its data entry form
            render(view: 'invoice', model: result)
        }
    }

    def manual() {
        def result = postingService.postSubLedgerInvoice('supplier', params)
        if (result instanceof Document) {
            redirect(action: 'allocate', id: bookService.getTotalLine(result).id)
        } else {
            render(view: 'invoice', model: result)
        }
    }

    def allocate() {
        def lineInstance = GeneralTransaction.get(params.id)
        if (!lineInstance || lineInstance.securityCode != utilService.currentCompany().securityCode || !bookService.hasSupplierAccess(lineInstance.supplier)) {
            flash.message = message(code: 'document.no.manual', default: 'Unable to perform manual allocation')
            redirect(action: 'invoice')
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
        def allowDifference = utilService.setting('supplier.dataEntry.fxDiff.allowed', false)

        [supplierInstance: supplierInstance, lineInstance: lineInstance, allocationInstance: allocationInstance, transactionInstanceList: transactionInstanceList,
                documentTypeList: documentTypeList, transactionInstanceTotal: transactionInstanceTotal, periodList: periodList, displayPeriod: displayPeriod,
                allowDifference: allowDifference]
    }

    def allocating() {
        def lineInstance = GeneralTransaction.get(params.id)
        if (!lineInstance || lineInstance.securityCode != utilService.currentCompany().securityCode || !bookService.hasSupplierAccess(lineInstance.supplier)) {
            flash.message = message(code: 'document.no.manual', default: 'Unable to perform manual allocation')
            redirect(action: 'invoice')
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
            } else if (allocationInstance.accountValue && allocationInstance.accountDifference) {
                if (!utilService.setting('supplier.dataEntry.fxDiff.allowed', false)) {
                    allocationInstance.errorMessage(field: 'accountDifference', code: 'document.bad.difference', default: 'Invalid exchange difference')
                    valid = false
                } else {
                    def temp = ((allocationInstance.accountValue.abs() * utilService.setting('supplier.dataEntry.fxDiff.percent', 5)) / 100.0).max(1.0)
                    if (allocationInstance.accountDifference.abs() > temp) {
                        temp = "${utilService.setting('supplier.dataEntry.fxDiff.percent', 5)}"
                        allocationInstance.errorMessage(field: 'accountDifference', code: 'document.allocation.excess', args: [temp],
                                default: "The exchange difference amount exceeds the maximum data entry write-off of ${temp} percent")
                        valid = false
                    }
                }
            }
        }

        if (valid && postingService.allocateLine(lineInstance, allocationInstance)) {
            if (lineInstance.accountUnallocated) {
                redirect(action: 'allocate', id: lineInstance.id)
            } else {
                flash.message = message(code: 'document.created', args: [lineInstance.document.type.code, lineInstance.document.code], default: "Document ${lineInstance.document.type.code}${lineInstance.document.code} created")
                redirect(action: 'invoice')
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
            def allowDifference = utilService.setting('supplier.dataEntry.fxDiff.allowed', false)

            render(view: 'allocate', model: [supplierInstance: supplierInstance, lineInstance: lineInstance, allocationInstance: allocationInstance,
                    transactionInstanceList: transactionInstanceList, documentTypeList: documentTypeList, transactionInstanceTotal: transactionInstanceTotal,
                    periodList: periodList, displayPeriod: displayPeriod, allowDifference: allowDifference])
        }
    }

    def enquire() {
        def model = bookService.loadDocumentModel(params, ['PI', 'PC'])
        def documentInstance = model.documentInstance
        if (documentInstance.id) {
            model.totalInstance = bookService.getTotalLine(documentInstance)
            model.supplierInstance = model.totalInstance.supplier
            if (documentInstance.taxes) {
                def taxAnalysisList = []
                def count = 1
                def attrs = [context: documentInstance, currency: model.displayCurrency, negate: !model.analysisIsDebit]
                def format = NumberFormat.getPercentInstance(utilService.currentLocale())
                format.setMinimumIntegerDigits(1)
                format.setMinimumFractionDigits(3)
                def goods, tax
                for (line in documentInstance.taxes) {
                    attrs.line = line
                    attrs.field = 'value'
                    tax = bookService.getBookValue(attrs)
                    attrs.field = 'tax'
                    goods = bookService.getBookValue(attrs)
                    taxAnalysisList << [id: count++, data: "${line.taxCode.code}: ${utilService.format(goods, attrs.scale)} @ ${format.format(line.taxPercentage / 100.0)} = ${utilService.format(tax, attrs.scale)}"]
                }

                model.taxAnalysisList = taxAnalysisList
            }
        }

        model
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private getTemplateModel(company, templateDocumentInstance) {
        def documentTypeList = DocumentType.findAll("from DocumentType as dt where dt.company = ? and dt.type.code in ('PI', 'PC')", [company])
        def taxCodeList = TaxCode.findAllByCompany(company, [sort: 'code', cache: true])
        def currencyList = ExchangeCurrency.findAllByCompany(company, [cache: true])
        if (!templateDocumentInstance.currency) templateDocumentInstance.currency = utilService.companyCurrency()
        def settings = [:]
        settings.decimals = templateDocumentInstance.currency.decimals
        if (templateDocumentInstance.sourceCode) {
            if (!templateDocumentInstance.sourceName) {
                def subAccount = Supplier.findByCompanyAndCode(company, bookService.fixSupplierCase(templateDocumentInstance.sourceCode))
                if (subAccount && bookService.hasSupplierAccess(subAccount)) templateDocumentInstance.sourceName = subAccount.name
            }
        } else if (templateDocumentInstance.supplier && bookService.hasSupplierAccess(templateDocumentInstance.supplier)) {
            templateDocumentInstance.sourceCode = templateDocumentInstance.supplier.code
            templateDocumentInstance.sourceName = templateDocumentInstance.supplier.name
        }

        return [templateDocumentInstance: templateDocumentInstance, documentTypeList: documentTypeList, taxCodeList: taxCodeList, currencyList: currencyList, settings: settings]
    }

    private saveTemplate(templateDocumentInstance, params) {
        templateDocumentInstance.properties['type', 'currency', 'description', 'reference', 'sourceCode'] = params
        utilService.verify(templateDocumentInstance, ['type', 'currency'])             // Ensure correct references
        def valid = !templateDocumentInstance.hasErrors()
        def removables = []
        def documentDecs = templateDocumentInstance.currency?.decimals
        def subAccount, account, temp

        // Load the template lines from the request parameters and check for data binding errors
		// in the line at the same time. We do this whether the header had a fault or not
        def num = postingService.refreshTemplateLines(templateDocumentInstance, params)
        if (num) {
            templateDocumentInstance.errorMessage(code: 'document.line.data', args: [num], default: "Line ${num} has a 'data type' error")
            valid = false
        }

        if (valid) {
            if (templateDocumentInstance.sourceCode) {
                subAccount = Supplier.findByCompanyAndCode(utilService.currentCompany(), bookService.fixSupplierCase(templateDocumentInstance.sourceCode))
                if (subAccount && bookService.hasSupplierAccess(subAccount)) {
                    templateDocumentInstance.supplier = subAccount
                } else {
                    templateDocumentInstance.errorMessage(field: 'sourceCode', code: 'document.supplier.invalid', default: 'Invalid sub-ledger account')
                    templateDocumentInstance.sourceName = null
                    valid = false
                }
            } else {
                templateDocumentInstance.supplier = null
            }
        }

        if (valid) {
            num = 0
            for (line in templateDocumentInstance.lines) {
                num++
                if (line.accountCode) {

                    // Make sure the GL account code is expanded for mnemonics and case is correct
                    temp = bookService.expandAccountCode(utilService.currentUser(), line.accountCode)
                    if (!temp) {
                        temp = message(code: 'account.not.exists', default: 'Invalid GL account')
                        templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('accountCode', null)
                        valid = false
                        break
                    } else {
                        line.accountCode = temp
                    }

                    // See if the GL account actually exists
                    account = bookService.getAccount(utilService.currentCompany(), line.accountCode)
                    if (account instanceof String) {
                        templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, account], default: "Line ${num}: ${account}")
                        line.errors.rejectValue('accountCode', null)
                        valid = false
                        break
                    } else {

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

                        // Check that any tax code is compatible with any sub-ledger account
                        if (subAccount?.taxCode && line.taxCode && subAccount.taxCode.authority.id != line.taxCode.authority.id) {
                            temp = message(code: 'document.bad.taxCode', default: 'The tax code is inconsistent with the tax status of the account that the document total is being posted to')
                            templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('taxCode', null)
                            valid = false
                            break
                        }

                        // Round any entered values
                        if (line.documentTax != null) line.documentTax = utilService.round(line.documentTax, documentDecs)
                        if (line.documentValue != null) line.documentValue = utilService.round(line.documentValue, documentDecs)
                        if (line.documentTotal != null) line.documentTotal = utilService.round(line.documentTotal, documentDecs)
                        if (line.documentTax != null && line.documentValue != null && line.documentTotal != null && line.documentTax + line.documentValue != line.documentTotal) {
                            temp = message(code: 'document.bad.total', default: 'The Goods and Tax values do not add up to the Total value')
                            documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('documentTotal', null)
                            valid = false
                            break
                        }

                        line.account = account
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
