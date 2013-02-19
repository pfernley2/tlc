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
import org.grails.tlc.corp.TaxRate
import doc.Line
import doc.Tax
import doc.Total
import java.text.NumberFormat
import org.apache.commons.collections.set.ListOrderedSet

class CashController {

    // Injected services
    def utilService
    def bookService
    def postingService

    // Security settings
    def activities = [default: 'cashtempl', template: 'cashentry', transact: 'cashentry', lines: 'cashentry', transacting: 'cashentry', enquire: 'enquire']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', templateLines: 'POST', lines: 'POST', transacting: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        def max = utilService.max
        def offset = utilService.offset
        def listing = TemplateDocument.findAll("from TemplateDocument as x where x.type.company = ? and x.type.type.code in ('CP', 'CR') order by x.type.code, x.description",
                [utilService.currentCompany()], [max: max, offset: offset])
        def total = TemplateDocument.executeQuery("select count(*) from TemplateDocument as x where x.type.company = ? and x.type.type.code in ('CP', 'CR')", [utilService.currentCompany()])[0]
        [templateDocumentInstanceList: listing, templateDocumentInstanceTotal: total]
    }

    def show() {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!templateDocumentInstance || !['CP', 'CR'].contains(templateDocumentInstance.type.type.code)) {
            flash.message = utilService.standardMessage('not.found', 'templateDocument', params.id)
            redirect(action: 'list')
        } else {
            return [templateDocumentInstance: templateDocumentInstance]
        }
    }

    def delete() {
        def templateDocumentInstance = TemplateDocument.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (templateDocumentInstance && ['CP', 'CR'].contains(templateDocumentInstance.type.type.code)) {
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
        if (!templateDocumentInstance || !['CP', 'CR'].contains(templateDocumentInstance.type.type.code)) {
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
        if (templateDocumentInstance && ['CP', 'CR'].contains(templateDocumentInstance.type.type.code)) {
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
            if (!templateDocumentInstance || !['CP', 'CR'].contains(templateDocumentInstance.type.type.code)) {
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
        if (templateDocumentInstance && ['CP', 'CR'].contains(templateDocumentInstance.type.type.code)) {
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
        documentInstance.properties['type', 'period', 'currency', 'code', 'description', 'documentDate', 'reference', 'sourceCode', 'sourceGoods', 'sourceTax', 'sourceTotal'] = params

        // Load the document lines from the request parameters
        postingService.refreshDocumentLines(documentInstance, params)

        // Add the lines
        for (int i = 0; i < 10; i++) documentInstance.addToLines(new Line())

        render(view: 'transact', model: getModel(utilService.currentCompany(), documentInstance))
    }

    def transacting() {
        def company = utilService.currentCompany()
        def companyCurrency = utilService.companyCurrency()
        def documentInstance = new Document()
        if (!params.code && params.sourceNumber) params.code = params.sourceNumber  // A disabled field would not be in the params, so we keep a copy in a hidden field
        documentInstance.properties['type', 'period', 'currency', 'code', 'description', 'documentDate', 'reference', 'sourceCode', 'sourceGoods', 'sourceTax', 'sourceTotal'] = params
        def documentDecs, account, temp, bankAccount, bankDecs, bankTax, bankValue
        def companyDecs = companyCurrency.decimals
        def now = utilService.fixDate()
        def removables = []                 // 'blank' lines that we can remove from the document just before posting
        def companyRate = 1.0    // The exchange rate we need to multiply document currency values by to get the company currency values
        def bankRate = 1.0       // The exchange rate we need to multiply document currency values by to get the bank/cash account currency values
        def otherRates = [:]                // Other exchange rates we may use to convert from document currency values to GL account currency values
        def lineDocumentGoods = 0.0
        def lineDocumentTaxes = 0.0
        def lineDocumentTotals = 0.0
        def lineBankTaxes = 0.0
        def lineBankGoods = 0.0
        def lineCompanyTaxes = 0.0
        def lineCompanyGoods = 0.0

        // Process the document header, start by checking for data binding errors
        def valid = !documentInstance.hasErrors()

        // Load the document lines from the request parameters and check for data binding errors
		// in the line at the same time. We do this whether the header had a fault or not
        def num = postingService.refreshDocumentLines(documentInstance, params)
        if (num) {
            documentInstance.errorMessage(code: 'document.line.data', args: [num], default: "Line ${num} has a 'data type' error")
            valid = false
        }

        // Now get on to standard validation, starting with the header: Make sure references are to the correct company objects
        if (valid) {
            utilService.verify(documentInstance, ['type', 'period', 'currency'])
            if (documentInstance.type == null || !['CP', 'CR'].contains(documentInstance.type.type.code)) {
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
                if (!bankAccount || bankAccount.type?.code != 'cash' || !bankAccount.active || !bookService.hasAccountAccess(bankAccount)) {
                    documentInstance.errorMessage(field: 'sourceCode', code: 'cash.not.exists', default: 'Invalid Cash account')
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
                documentInstance.errorMessage(field: 'sourceCode', code: 'cash.not.exists', default: 'Invalid Cash account')
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

        // Check that document total cross adds correctly
        if (valid) {
            num = 0
            if (documentInstance.sourceGoods != null) {
                documentInstance.sourceGoods = utilService.round(documentInstance.sourceGoods, documentDecs)
                num++
            }

            if (documentInstance.sourceTax != null) {
                documentInstance.sourceTax = utilService.round(documentInstance.sourceTax, documentDecs)
                num++
            }

            if (documentInstance.sourceTotal != null) {
                documentInstance.sourceTotal = utilService.round(documentInstance.sourceTotal, documentDecs)
                num++
            }

            if (num < 2) {
                temp = (documentInstance.sourceGoods == null) ? 'sourceGoods' : 'sourceTax'
                documentInstance.errorMessage(field: temp, code: 'document.two.values', default: 'At least two out of the Goods, Tax and Total values must be entered')
                valid = false
            } else if (num < 3) {
                if (documentInstance.sourceGoods == null) {
                    documentInstance.sourceGoods = documentInstance.sourceTotal - documentInstance.sourceTax
                } else if (documentInstance.sourceTax == null) {
                    documentInstance.sourceTax = documentInstance.sourceTotal - documentInstance.sourceGoods
                } else {
                    documentInstance.sourceTotal = documentInstance.sourceGoods + documentInstance.sourceTax
                }
            } else if (documentInstance.sourceTotal != documentInstance.sourceGoods + documentInstance.sourceTax) {
                documentInstance.errorMessage(field: 'sourceTotal', code: 'document.bad.total', default: 'The Goods and Tax values do not add up to the Total value')
                valid = false
            }

            if (valid && documentInstance.sourceGoods < 0.0) {
                documentInstance.errorMessage(field: 'sourceGoods', code: 'document.bad.goods', default: 'The Goods value cannot be negative')
                valid = false
            }

            if (valid && documentInstance.sourceTax < 0.0) {
                documentInstance.errorMessage(field: 'sourceTax', code: 'document.bad.tax', default: 'The Tax value cannot be negative')
                valid = false
            }

            if (valid && documentInstance.sourceTotal == 0.0) {
                documentInstance.errorMessage(field: 'sourceTotal', code: 'document.zero.total', default: 'The Total value cannot be zero')
                valid = false
            }
        }

        // Step through each line checking it in detail
        if (valid) {
            num = 0
            for (line in documentInstance.lines) {
                num++

                // If this is intended to be an active line
                if (line.accountCode) {

                    // Make sure the GL account code is expanded for mnemonics and case is correct
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

                    // Make sure it's not the Cash Account they're trying to post to
                    if (account.code == bankAccount.code) {
                        temp = message(code: 'cash.not.self', default: 'You cannot post a cash transaction to the originating Cash account')
                        documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('accountCode', null)
                        valid = false
                        break
                    }

                    // Check they have entered at least the goods value or the total value for the line
                    if (line.documentValue == null && line.documentTotal == null) {
                        temp = message(code: 'document.no.compute', default: 'Either a Goods value or Total value must be entered')
                        documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('documentValue', null)
                        valid = false
                        break
                    }

                    // Round any entered values
                    if (line.documentValue != null) line.documentValue = utilService.round(line.documentValue, documentDecs)
                    if (line.documentTax != null) line.documentTax = utilService.round(line.documentTax, documentDecs)
                    if (line.documentTotal != null) line.documentTotal = utilService.round(line.documentTotal, documentDecs)

                    // If tax is involved, make sure we can find an appropriate rate record. We will record the
                    // 'standard' rate in the line record which may not be the actual rate applied. Most tax systems
                    // require a bookkeeping system to record the ACTUAL tax amount on a source document, even if it
                    // is in error, so that overall tax amounts between customers and suppliers are self balancing.
                    if (line.taxCode) {
                        temp = TaxRate.findAllByTaxCodeAndValidFromLessThanEquals(line.taxCode, documentInstance.documentDate, [sort: 'validFrom', order: 'desc', max: 1, cache: true])
                        if (!temp) {
                            temp = message(code: 'document.no.taxRate', default: 'Unable to find a tax rate for this tax code and document date combination')
                            documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('taxCode', null)
                            valid = false
                            break
                        }

                        line.taxPercentage = temp[0].rate
                        if (line.documentTax == null) {
                            if (line.documentValue != null && line.documentTotal != null) {
                                line.documentTax = utilService.round(line.documentTotal - line.documentValue, documentDecs)
                            } else if (line.documentValue != null) {
                                line.documentTax = utilService.round((line.documentValue * line.taxPercentage) / 100.0, documentDecs)
                            } else {
                                line.documentTax = utilService.round((line.documentTotal * line.taxPercentage) / (100.0 + line.taxPercentage), documentDecs)
                            }
                        }
                    } else {
                        if (line.documentTax) {
                            temp = message(code: 'document.no.tax', default: 'Cannot have a tax value without a tax code')
                            documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                            line.errors.rejectValue('taxCode', null)
                            valid = false
                            break
                        }

                        line.documentTax = null
                    }

                    // Fill in any missing values for the line
                    if (line.documentTotal == null) {
                        line.documentTotal = utilService.round(line.documentValue + (line.documentTax ?: 0.0), documentDecs)
                    } else if (line.documentValue == null) {
                        line.documentValue = utilService.round(line.documentTotal - (line.documentTax ?: 0.0), documentDecs)
                    } else if (line.documentValue + (line.documentTax ?: 0.0) != line.documentTotal) {
                        temp = message(code: 'document.bad.total', default: 'The Goods and Tax values do not add up to the Total value')
                        documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('documentTotal', null)
                        valid = false
                        break
                    }

                    // Set up the line ready for posting. Note that we store the account as a transient in the line
                    // record. This allows us to avoid loading the balance record to which the line will actually
                    // belong but still allows the posting routine fast access to the balance record (via account
                    // and period). We don't want to load the balance record because this can lead to attempts to
                    // update a stale balance since we don't yet have the company locked for posting.
                    line.account = account
                    bankTax = line.taxCode ? utilService.round(line.documentTax * bankRate, bankDecs) : null
                    bankValue = utilService.round(line.documentValue * bankRate, bankDecs)
                    line.companyTax = line.taxCode ? utilService.round(line.documentTax * companyRate, companyDecs) : null
                    line.companyValue = utilService.round(line.documentValue * companyRate, companyDecs)
                    if (account.currency.code == companyCurrency.code) {
                        line.generalTax = line.companyTax
                        line.generalValue = line.companyValue
                    } else if (account.currency.code == bankAccount.currency.code) {
                        line.generalTax = bankTax
                        line.generalValue = bankValue
                    } else if (account.currency.code == documentInstance.currency.code) {
                        line.generalTax = line.documentTax
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

                        line.generalTax = line.taxCode ? utilService.round(line.documentTax * temp, account.currency.decimals) : null
                        line.generalValue = utilService.round(line.documentValue * temp, account.currency.decimals)
                    }

                    // Increment the line totals and, if applicable, the tax analysis totals
                    lineDocumentTotals += line.documentTotal
                    lineDocumentGoods += line.documentValue
                    lineBankGoods += bankValue
                    lineCompanyGoods += line.companyValue
                    if (line.taxCode) {
                        lineDocumentTaxes += line.documentTax
                        lineBankTaxes += bankTax
                        lineCompanyTaxes += line.companyTax
                        temp = false
                        for (tax in documentInstance.taxes) {
                            if (tax.taxCode.id == line.taxCode.id && tax.taxPercentage == line.taxPercentage) {
                                tax.documentValue += line.documentTax
                                tax.generalValue += line.companyTax
                                tax.companyValue += line.companyTax
                                tax.documentTax += line.documentValue
                                tax.generalTax += line.companyValue
                                tax.companyTax += line.companyValue
                                temp = true
                                break
                            }
                        }

                        if (!temp) {
                            temp = new Tax()
                            temp.taxCode = line.taxCode
                            temp.taxPercentage = line.taxPercentage
                            temp.documentValue = line.documentTax
                            temp.generalValue = line.companyTax
                            temp.companyValue = line.companyTax
                            temp.documentTax = line.documentValue
                            temp.generalTax = line.companyValue
                            temp.companyTax = line.companyValue
                            documentInstance.addToTaxes(temp)
                        }
                    }
                } else {

                    // These are non-active lines that, if the document passes all our checks here (other than final
                    // validation on call to the save method), we will remove before acutally saving the document.
                    removables << line
                }
            }
        }

        // Cross check the lines to the header
        if (valid) {
            if (lineDocumentGoods != documentInstance.sourceGoods) {
                documentInstance.errorMessage(field: 'sourceGoods', code: 'document.goods.mismatch', default: 'The document goods value does not agree to the sum of the goods values of the lines')
                valid = false
            } else if (lineDocumentTaxes != documentInstance.sourceTax) {
                documentInstance.errorMessage(field: 'sourceTax', code: 'document.tax.mismatch', default: 'The document tax value does not agree to the sum of the tax values of the lines')
                valid = false
            } else if (lineDocumentTotals != documentInstance.sourceTotal) {
                documentInstance.errorMessage(field: 'sourceTotal', code: 'document.total.mismatch', default: 'The document total value does not agree to the sum of the total values of the lines')
                valid = false
            }
        }

        // Create the total line
        if (valid) {
            def line = new Total(description: documentInstance.description, documentValue: documentInstance.sourceTotal,
                    generalValue: lineBankGoods + lineBankTaxes, companyValue: lineCompanyGoods + lineCompanyTaxes)
			line.account = bankAccount

            // If there are taxes involved
            if (documentInstance.taxes) {
                temp = 'tax'
                account = bookService.getControlAccount(company, temp)
                if (account) {
                    line.documentTax = documentInstance.sourceTax
                    line.generalTax = lineBankTaxes
                    line.companyTax = lineCompanyTaxes

                    // Need to update the tax records with the tax control account
                    for (tax in documentInstance.taxes) {
                        tax.account = account
                    }
                } else {
                    documentInstance.errorMessage(code: 'document.no.control', args: [temp], default: "Could not find the ${temp} control account in the General Ledger")
                    valid = false
                }
            }

            if (valid) documentInstance.addToTotal(line)
        }

        // Remove any 'blank' lines and then post the document
        if (valid) {
            for (line in removables) {
                documentInstance.removeFromLines(line)
            }

            valid = postingService.post(documentInstance)
        }

        if (valid) {           // Document comes back in its debit/credit form
            flash.message = message(code: 'document.created', args: [documentInstance.type.code, documentInstance.code], default: "Document ${documentInstance.type.code}${documentInstance.code} created")
            redirect(action: 'transact')
        } else {                                    // Document comes back in its data entry form
            render(view: 'transact', model: getModel(company, documentInstance))
        }
    }

    def enquire() {
        def model = bookService.loadDocumentModel(params, ['CP', 'CR'])
        def documentInstance = model.documentInstance
        if (documentInstance.id) {
            model.totalInstance = bookService.getTotalLine(documentInstance)
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

    private getModel(company, documentInstance) {
        def documentTypeList = DocumentType.findAll("from DocumentType as dt where dt.company = ? and dt.type.code in ('CP', 'CR')", [company])
        def taxCodeList = TaxCode.findAllByCompany(company, [sort: 'code', cache: true])
        def periodList = bookService.getOpenPeriods(company)
        def currencyList = ExchangeCurrency.findAllByCompany(company, [cache: true])
        if (!documentInstance.documentDate) documentInstance.documentDate = utilService.fixDate()
        if (!documentInstance.period) documentInstance.period = bookService.selectPeriod(periodList, documentInstance.documentDate)
        periodList = periodList.reverse()
        def bankAccountList = []
        def bankAccount
        def banks = Account.findAll("from Account as x where x.securityCode = ? and x.active = ? and x.type.code = 'cash' order by x.name", [company.securityCode, true])
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
                taxCodeList: taxCodeList, periodList: periodList, currencyList: currencyList, settings: settings]
    }

    private getTemplateModel(company, templateDocumentInstance) {
        def documentTypeList = DocumentType.findAll("from DocumentType as dt where dt.company = ? and dt.type.code in ('CP', 'CR')", [company])
        def taxCodeList = TaxCode.findAllByCompany(company, [sort: 'code', cache: true])
        def currencyList = ExchangeCurrency.findAllByCompany(company, [cache: true])
        if (!templateDocumentInstance.sourceCode) templateDocumentInstance.sourceCode = templateDocumentInstance.account?.code
        def bankAccountList = []
        def bankAccount
        def banks = Account.findAll("from Account as x where x.securityCode = ? and x.type.code = 'cash' order by x.name", [company.securityCode])
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
                taxCodeList: taxCodeList, currencyList: currencyList, settings: settings]
    }

    private saveTemplate(templateDocumentInstance, params) {
        templateDocumentInstance.properties['type', 'currency', 'description', 'reference', 'sourceCode'] = params
        utilService.verify(templateDocumentInstance, ['type', 'currency'])             // Ensure correct references
        def valid = !templateDocumentInstance.hasErrors()
        def removables = []
        def documentDecs = templateDocumentInstance.currency?.decimals
        def account, temp

        // Load the document lines from the request parameters and check for data binding errors
		// in the line at the same time. We do this whether the header had a fault or not
        def num = postingService.refreshTemplateLines(templateDocumentInstance, params)
        if (num) {
            templateDocumentInstance.errorMessage(code: 'document.line.data', args: [num], default: "Line ${num} has a 'data type' error")
            valid = false
        }

        // Check out the cash account
        if (valid) {
            if (templateDocumentInstance.sourceCode) {
                account = Account.findBySecurityCodeAndCode(utilService.currentCompany().securityCode, templateDocumentInstance.sourceCode)
                if (!account || account.type?.code != 'cash' || !bookService.hasAccountAccess(account)) {
                    templateDocumentInstance.errorMessage(field: 'sourceCode', code: 'cash.not.exists', default: 'Invalid Cash account')
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

                    // Make sure it's not the Cash Account they're trying to post to
                    if (account.code == templateDocumentInstance.sourceCode) {
                        temp = message(code: 'cash.not.self', default: 'You cannot post a cash transaction to the originating Cash account')
                        templateDocumentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('accountCode', null)
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
