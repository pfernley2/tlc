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
import org.grails.tlc.sys.CacheService
import org.grails.tlc.sys.SystemDocumentType
import doc.Line
import doc.Tax
import doc.Total
import org.apache.commons.collections.set.ListOrderedSet

class PostingService {

    private static systemDocumentTypes = [:]

    static transactional = false

    def bookService
    def utilService

    // Posts a new document. All the values are assumed to be 'positive' and are adjusted to be debit or credit
    // according to the SystemDocumentType.analysisIsDebit flag as the values are posted. The values are returned to
    // the caller in their original 'positive' state if an error occurs (so that the document can be redisplayed to the
    // user) but in their positive/negatve for if the document was posted ok. Returns true if the posting succeeded, false
    // if it failed. In the case of a failure the document object will have an error message attached to it. The
    // optional transaction parameter, if not null, will be taken to mean that there is already an active transaction
    // and that we should not start our own. In such cases the caller is responsible for rolling back the transaction
    // if we return false. Will throw an exception if the document would put the books out of balance.
    def post(document, transaction = null) {
        def company = document.type.company
        def analysisIsDebit = document.type.type.analysisIsDebit
        def retained, valid

        def lock = bookService.getCompanyLock(company)
        lock.lock()
        try {

            // Get a list of the active periods
            def periods = bookService.getActivePeriods(company)

            // Find out the first period we are to post to
            def index = -1
            for (int i = 0; i < periods.size(); i++) {
                if (periods[i].id == document.period.id) {
                    index = i
                    break
                }
            }

            // Check we found an open period
            if (index < 0) {
                document.errorMessage(field: 'period', code: 'document.bad.period', default: 'Invalid document period')
                return false
            }

            // If we will be rolling over a year end, get the retained profit account just in case we need it
            if (periods[index].year.id != periods[-1].year.id) {
                def txt = 'retained'
                retained = bookService.getControlAccount(company, txt)
                if (!retained) {
                    document.errorMessage(code: 'document.no.control', args: [txt], default: "Could not find the ${txt} control account in the General Ledger")
                    return false
                }
            }

            // Set/refresh the balance record for each line in the document (including any total and taxes) so we can save it.
            for (line in document.lines) {
                if (line.balance) {
                    line.balance.refresh()
                    if (line.balance.period.id != document.period.id) {
                        document.errorMessage(code: 'not.found', domain: 'generalBalance', value: line.balance.id)
                        return false
                    }

                    line.account = line.balance.account
                } else {
                    line.balance = GeneralBalance.findByAccountAndPeriod(line.account, document.period)
                }
            }
			
            for (line in document.taxes) {
                if (line.balance) {
                    line.balance.refresh()
                    if (line.balance.period.id != document.period.id) {
                        document.errorMessage(code: 'not.found', domain: 'generalBalance', value: line.balance.id)
                        return false
                    }

                    line.account = line.balance.account
                } else {
                    line.balance = GeneralBalance.findByAccountAndPeriod(line.account, document.period)
                }
            }
			
            for (line in document.total) {
                if (line.balance) {
                    line.balance.refresh()
                    if (line.balance.period.id != document.period.id) {
                        document.errorMessage(code: 'not.found', domain: 'generalBalance', value: line.balance.id)
                        return false
                    }

                    line.account = line.balance.account
                } else {
                    line.balance = GeneralBalance.findByAccountAndPeriod(line.account, document.period)
                }
            }

            // We need to get the various values in their correct debit (positive) and
            // credit (negative) state before we actually save it.
            adjustDocumentValues(document, analysisIsDebit)

            // Within a database transaction if required
            if (transaction == null) {
                Document.withTransaction {status ->
                    valid = postDocument(document, periods, index, retained)
                    if (!valid) status.setRollbackOnly()
                }
            } else {        // Part of an existing transaction
                valid = postDocument(document, periods, index, retained)
            }

            // If the posting failed
            if (!valid) {

                // We need to reverse the adjustment we made just before we tried to update the database since,
                // in the case of an error, we want to pass the document back to the caller in its original
                // 'data entry' format whereas with a successful save we pass it back in its debit/credit form
                adjustDocumentValues(document, analysisIsDebit)
            }
        } finally {
            lock.unlock()
        }

        return valid
    }

    // Creates the model for data entry of a new customer or supplier invoice or credit note. The ledger parameter
    // should be a string of either 'customer' or 'supplier'
    def createSubLedgerInvoice(ledger) {
        def documentInstance = new Document()
        for (int i = 0; i < 10; i++) documentInstance.addToLines(new Line())
        return getSubLedgerInvoiceModel(utilService.currentCompany(), ledger, documentInstance)
    }

    // Adds more lines to a customer or supplier invoice or credit note that is being entered and returns the
    // updated model. The ledger parameter should be a string of either 'customer' or 'supplier' and the params
    // should be the request parameters.
    def addSubLedgerInvoiceLines(ledger, params) {
        def documentInstance = new Document()
        if (!params.code && params.sourceNumber) params.code = params.sourceNumber  // A disabled field would not be in the params, so we keep a copy in a hidden field
        documentInstance.properties['type', 'period', 'currency', 'code', 'description', 'documentDate', 'dueDate', 'reference', 'sourceCode', 'sourceHold', 'sourceGoods', 'sourceTax', 'sourceTotal'] = params

        // Load the document lines from the request parameters
        refreshDocumentLines(documentInstance, params)

        // Add the lines
        for (int i = 0; i < 10; i++) documentInstance.addToLines(new Line())

        return getSubLedgerInvoiceModel(utilService.currentCompany(), ledger, documentInstance)
    }

    // Creates a customer or supplier invoice or credit notes from the request parameters and posts it. If the posting
    // succeeds, the document is returned otherwise a model (i.e. a map) is returned ready for redisplaying the faulty
    // document to the user.
    def postSubLedgerInvoice(ledger, params) {
        def company = utilService.currentCompany()
        def companyCurrency = utilService.companyCurrency()
        def documentInstance = new Document()
        if (!params.code && params.sourceNumber) params.code = params.sourceNumber  // A disabled field would not be in the params, so we keep a copy in a hidden field
        documentInstance.properties['type', 'period', 'currency', 'code', 'description', 'documentDate', 'dueDate', 'reference', 'sourceCode', 'sourceHold', 'sourceGoods', 'sourceTax', 'sourceTotal'] = params
        def subAccount, documentDecs, accountDecs, account, temp
        def companyDecs = companyCurrency.decimals
        def now = utilService.fixDate()
        def removables = []                 // 'blank' lines that we can remove from the document just before posting
        def companyRate = 1.0               // The exchange rate we need to multiply document currency values by to get the company currency values
        def accountRate = 1.0               // The exchange rate we need to multiply document currency values by to get the customer/supplier account currency values
        def otherRates = [:]                // Other exchange rates we may use to convert from document currency values to GL account currency values
        def lineDocumentGoods = 0.0
        def lineDocumentTaxes = 0.0
        def lineDocumentTotals = 0.0
        def lineAccountTaxes = 0.0
        def lineAccountGoods = 0.0
        def lineCompanyTaxes = 0.0
        def lineCompanyGoods = 0.0

        // Process the document header, start by checking for data binding errors
        def valid = !documentInstance.hasErrors()

        // Load the document lines from the request parameters and check for data binding errors
		// in the line at the same time. We do this whether the header had a fault or not
        def num = refreshDocumentLines(documentInstance, params)
        if (num) {
            documentInstance.errorMessage(code: 'document.line.data', args: [num], default: "Line ${num} has a 'data type' error")
            valid = false
        }

        // Now get on to standard validation, starting with the header: Make sure references are to the correct company objects
        if (valid) {
            temp = (ledger == 'customer') ? ['SI', 'SC'] : ['PI', 'PC']
            utilService.verify(documentInstance, ['type', 'period', 'currency'])
            if (documentInstance.type == null || !temp.contains(documentInstance.type.type.code)) {
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

        // Load up and check out the sub ledger account and any document to company and ledger exchange rates we may need
        if (valid) {
            if (documentInstance.sourceCode) {
                if (ledger == 'customer') {
                    subAccount = Customer.findByCompanyAndCode(company, bookService.fixCustomerCase(documentInstance.sourceCode))
                    temp = (subAccount && bookService.hasCustomerAccess(subAccount))
                } else {
                    subAccount = Supplier.findByCompanyAndCode(company, bookService.fixSupplierCase(documentInstance.sourceCode))
                    temp = (subAccount && bookService.hasSupplierAccess(subAccount))
                }

                if (temp) {
                    documentInstance.sourceCode = subAccount.code
                    documentInstance.sourceName = subAccount.name
                    documentDecs = documentInstance.currency.decimals
                    accountDecs = subAccount.currency.decimals
                    if (subAccount.currency.code != documentInstance.currency.code) {
                        accountRate = utilService.getExchangeRate(documentInstance.currency, subAccount.currency, now)
                        if (!accountRate) {
                            documentInstance.errorMessage(code: 'document.bad.exchangeRate', args: [documentInstance.currency.code, subAccount.currency.code],
                                    default: "No exchange rate available from ${documentInstance.currency.code} to ${subAccount.currency.code}")
                            valid = false
                        }
                    }

                    if (companyCurrency.code != documentInstance.currency.code) {
                        if (companyCurrency.code == subAccount.currency.code) {
                            companyRate = accountRate
                        } else {
                            companyRate = utilService.getExchangeRate(documentInstance.currency, companyCurrency, now)
                            if (!companyRate) {
                                documentInstance.errorMessage(code: 'document.bad.exchangeRate', args: [documentInstance.currency.code, companyCurrency.code],
                                        default: "No exchange rate available from ${documentInstance.currency.code} to ${companyCurrency.code}")
                                valid = false
                            }
                        }
                    }
                } else {
                    documentInstance.errorMessage(field: 'sourceCode', code: 'document.' + ledger + '.invalid', default: 'Invalid sub-ledger account')
                    documentInstance.sourceName = null
                    valid = false
                }
            } else {
                documentInstance.errorMessage(field: 'sourceCode', code: 'document.' + ledger + '.invalid', default: 'Invalid sub-ledger account')
                documentInstance.sourceName = null
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
                        valid = canPostDocumentToAccount(documentInstance, line, num, account)
                        if (!valid) break
                    } else {
                        temp = message(code: 'account.not.exists', default: 'Invalid GL account')
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

                    // Ensure that any line tax code is consistent with the subAccount tax code and that the usage is
                    // consitent with the tax authority usage.
                    if ((line.taxCode && subAccount.taxCode && line.taxCode.authority?.id != subAccount.taxCode.authority?.id) ||
                            (line.taxCode && !subAccount.taxCode && line.taxCode.authority.usage != 'ad-hoc') ||
                            (!line.taxCode && subAccount.taxCode && subAccount.taxCode.authority.usage == 'mandatory')) {
                        temp = message(code: 'document.bad.taxCode', default: 'The tax code is inconsistent with the tax status of the account that the document total is being posted to')
                        documentInstance.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('taxCode', null)
                        valid = false
                        break
                    }

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
                    line.accountTax = line.taxCode ? utilService.round(line.documentTax * accountRate, accountDecs) : null
                    line.accountValue = utilService.round(line.documentValue * accountRate, accountDecs)
                    line.companyTax = line.taxCode ? utilService.round(line.documentTax * companyRate, companyDecs) : null
                    line.companyValue = utilService.round(line.documentValue * companyRate, companyDecs)
                    if (account.currency.code == companyCurrency.code) {
                        line.generalTax = line.companyTax
                        line.generalValue = line.companyValue
                    } else if (account.currency.code == subAccount.currency.code) {
                        line.generalTax = line.accountTax
                        line.generalValue = line.accountValue
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
                    lineAccountGoods += line.accountValue
                    lineCompanyGoods += line.companyValue
                    if (line.taxCode) {
                        lineDocumentTaxes += line.documentTax
                        lineAccountTaxes += line.accountTax
                        lineCompanyTaxes += line.companyTax
                        temp = false
                        for (tax in documentInstance.taxes) {
                            if (tax.taxCode.id == line.taxCode.id && tax.taxPercentage == line.taxPercentage) {
                                tax.documentValue += line.documentTax
                                tax.accountValue += line.accountTax
                                tax.generalValue += line.companyTax
                                tax.companyValue += line.companyTax
                                tax.documentTax += line.documentValue
                                tax.accountTax += line.accountValue
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
                            temp.accountValue = line.accountTax
                            temp.generalValue = line.companyTax
                            temp.companyValue = line.companyTax
                            temp.documentTax = line.documentValue
                            temp.accountTax = line.accountValue
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

            // Will need the sub-ledger control account for the total line
            temp = (ledger == 'customer') ? 'ar' : 'ap'
            account = bookService.getControlAccount(company, temp)
            if (account) {
                def line = new Total(description: documentInstance.description, documentValue: documentInstance.sourceTotal,
                        accountValue: lineAccountGoods + lineAccountTaxes, accountUnallocated: lineAccountGoods + lineAccountTaxes,
                        generalValue: lineCompanyGoods + lineCompanyTaxes, companyValue: lineCompanyGoods + lineCompanyTaxes,
                        companyUnallocated: lineCompanyGoods + lineCompanyTaxes, onHold: documentInstance.sourceHold, affectsTurnover: true)
				line.account = account

                if (ledger == 'customer') {
                    line.customer = subAccount
                } else {
                    line.supplier = subAccount
                }

                // If there are taxes involved
                if (documentInstance.taxes) {
                    temp = 'tax'
                    account = bookService.getControlAccount(company, temp)
                    if (account) {
                        line.documentTax = documentInstance.sourceTax
                        line.accountTax = lineAccountTaxes
                        line.generalTax = lineCompanyTaxes
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
            } else {
                documentInstance.errorMessage(code: 'document.no.control', args: [temp], default: "Could not find the ${temp} control account in the General Ledger")
                valid = false
            }
        }

        // Remove any 'blank' lines and then post the document
        if (valid) {
            for (line in removables) {
                documentInstance.removeFromLines(line)
            }

            valid = post(documentInstance)
        }

        // If successful return the document otherwise return the model (i.e. a map) for re-display to the user
        if (valid) {
            return documentInstance
        } else {
            return getSubLedgerInvoiceModel(company, ledger, documentInstance)
        }
    }

    // Auto allocates either a customer or supplier account returning true if the allocation succeeded or false if not.
    // This method should *NOT* be called within a transaction and need *NOT* be called when the company is locked
    // since it handles its own transactioning and locking (at the customer/supplier database level rather than the
    // company level). It is not an error to call this method when the company is locked but it will inhibit concurrency.
    def autoAllocate(account, company = utilService.currentCompany(), currency = utilService.companyCurrency()) {
        def isCustomer = (account instanceof Customer)
        def lines
        if (isCustomer) {
            Customer.withTransaction {status ->
                account = Customer.lock(account.id)
                lines = GeneralTransaction.findAll('from GeneralTransaction as t where t.customer = ? and t.accountUnallocated != ? and onHold = ? order by t.id', [account, 0.0, false])
            }
        } else {
            Supplier.withTransaction {status ->
                account = Supplier.lock(account.id)
                lines = GeneralTransaction.findAll('from GeneralTransaction as t where t.supplier = ? and t.accountUnallocated != ? and onHold = ? order by t.id', [account, 0.0, false])
            }
        }

        def sourceLine, targetLine, value, srcAllocation, tgtAllocation
        for (int i = 0; i < lines.size() - 1; i++) {
            sourceLine = lines[i]
            if (sourceLine.accountUnallocated) {
                for (int j = i + 1; j < lines.size(); j++) {
                    targetLine = lines[j]
                    if ((sourceLine.accountUnallocated < 0.0 && targetLine.accountUnallocated > 0.0) || (sourceLine.accountUnallocated > 0.0 && targetLine.accountUnallocated < 0.0)) {
                        value = sourceLine.accountUnallocated.abs().min(targetLine.accountUnallocated.abs())
                        if (sourceLine.accountUnallocated < 0.0) value = -value
                        srcAllocation = new Allocation(targetType: targetLine.document.type, period: sourceLine.document.period, targetCode: targetLine.document.code,
                                targetId: targetLine.id, accountValue: -value)
                        tgtAllocation = new Allocation(targetType: sourceLine.document.type, period: targetLine.document.period, targetCode: sourceLine.document.code,
                                targetId: sourceLine.id, accountValue: value)
                        if (!allocateLine(sourceLine, srcAllocation, tgtAllocation, company, currency)) return false
                        if (sourceLine.accountUnallocated == 0.0) break
                    }
                }
            }
        }

        return true
    }

    // This method can work in one of two modes: Programmatic and Data Entry. In programmatic mode, the caller must supply both a
    // the srcAllocation and tgtAllocation parameters fully ready for saving, with the exception of the companyValue fields
    // which will be filled in by this method.
    //
    // In Data Entry mode only the patially completed srcAllocation parameter is supplied and the allocation value is assumed to be the
    // opposite sign of the source line value. For example, if the source line is a purchase invoice total of -100.00 (negative since
    // it is a credit) and the allocation amount is 26.00 then this will reduce the unallocated amount of the invoice to -74.00. However,
    // if the source line was a purchase credit note with a total of 80.00 (positive because it's a debit) and the allocation value
    // was 17.00, then the 17.00 would be negated to be -17.00 thus reducing the unallocated amount of the credit note to 63.00. Things
    // are done this way in Data Entry mode because users are shown the the value of the source line they are allocatin from in it's
    // positive form and, consequently, enter the allocation amount also in positive form.
    //
    // This method will also write off exchange differences if requested by fillling in the accountDifference field of the srcAllocation
    // parameter. It will also create an automatic foreign exchange settlement difference write-off where appropriate.
    //
    // Returns true if the allocation succeeds or false if an error occurs. Note that, in data entry mode, the lone srcAllocation parameter
    // passed in is in no way modified by this method since it is used merely as a source of information with new allocation objects being
    // created from it and the source line data. It will, however, have error messages attached to it where needed.
    def allocateLine(sourceLine, srcAllocation, tgtAllocation = null, company = utilService.currentCompany(), currency = utilService.companyCurrency()) {

        // Find out whether we are dealing with a customer or supplier
        def account, accountIsCustomer, sourceAllocation, targetAllocation, targetLine, targetDocument
        if (sourceLine.customer) {
            account = sourceLine.customer
            accountIsCustomer = true
        } else {
            account = sourceLine.supplier
            accountIsCustomer = false
        }

        def programmatic

        // Programmatic mode, the source and target allocations are aleady created (except companyValue)
        if (tgtAllocation) {
            programmatic = true
            sourceAllocation = srcAllocation
            targetAllocation = tgtAllocation
            targetLine = GeneralTransaction.get(srcAllocation.targetId)
            targetDocument = targetLine.document
        } else {    // Data Entry mode, we need to check and create the allocations ourselves
            programmatic = false

            // Must have a target document type
            if (!srcAllocation.targetType) {
                srcAllocation.errorMessage(field: 'targetType', code: 'document.allocation.targetType', default: 'Invalid target document type')
                return false
            }

            // Must have a target document code
            if (!srcAllocation.targetCode) {
                srcAllocation.errorMessage(field: 'targetCode', code: 'document.allocation.targetCode', default: 'Invalid target document code')
                return false
            }

            // Find the target document
            targetDocument = Document.findByTypeAndCode(srcAllocation.targetType, srcAllocation.targetCode)
            if (!targetDocument) {
                srcAllocation.errorMessage(field: 'targetCode', code: 'document.allocation.target', default: 'Unable to find the document to allocate to')
                return false
            }

            // Can't allocate a document to itself
            if (sourceLine.document.id == targetDocument.id) {
                srcAllocation.errorMessage(field: 'targetCode', code: 'document.allocation.self', default: 'You cannot allocate a document to itself')
                return false
            }

            // Find the target line
            targetLine = accountIsCustomer ? GeneralTransaction.findByDocumentAndCustomer(targetDocument, account) : GeneralTransaction.findByDocumentAndSupplier(targetDocument, account)

            // Must have a target line
            if (!targetLine) {
                srcAllocation.errorMessage(field: 'targetCode', code: 'document.allocation.line', default: 'Unable to find the document line to allocate to')
                return false
            }

            // Cannot allocate to/from lines with an account value of zero (i.e. automatic fx difference documents)
            if (!sourceLine.accountValue || !targetLine.accountValue) {
                srcAllocation.errorMessage(code: 'document.allocation.zero', default: 'Cannot allocate to or from a line with a zero value')
                return false
            }

            // Must have an allocation amount
            if (!srcAllocation.accountValue) {
                srcAllocation.errorMessage(field: 'accountValue', code: 'document.allocation.accountValue', default: 'The allocation amount cannot be zero')
                return false
            }

            // Create the allocation from the source line to th target line
            sourceAllocation = new Allocation(targetType: srcAllocation.targetType, targetCode: srcAllocation.targetCode, targetId: targetLine.id, period: sourceLine.document.period)

            // Create the inverse allocation from the target line to the source line
            targetAllocation = new Allocation(targetType: sourceLine.document.type, targetCode: sourceLine.document.code, targetId: sourceLine.id, period: targetDocument.period)

            // Set the allocation values
            if (sourceLine.accountValue < 0.0) {
                sourceAllocation.accountValue = srcAllocation.accountValue
                targetAllocation.accountValue = -srcAllocation.accountValue
            } else {
                sourceAllocation.accountValue = -srcAllocation.accountValue
                targetAllocation.accountValue = srcAllocation.accountValue
            }
        }

        // If they have entered a manual foreign exchange difference
        def fxDocument, fxTargetAllocation, fxAccountLine, temp
        if (srcAllocation.accountDifference) {

            // Can't have an exchange difference when both source and target documents are in the account currency
            if (account.currency.id == targetDocument.currency.id && account.currency.id == sourceLine.document.currency.id) {
                srcAllocation.errorMessage(field: 'accountDifference', code: 'document.allocation.currency',
                        default: 'You cannot have an exchange difference between documents that are both in the account currency')
                return false
            }

            // Need to find the fxDiff account in the GL
            temp = 'fxDiff'
            def fxAccount = bookService.getControlAccount(company, temp)
            if (!fxAccount) {
                srcAllocation.errorMessage(field: 'accountDifference', code: 'document.no.control', args: [temp],
                        default: "Could not find the ${temp} control account in the General Ledger")
                return false
            }

            // Get the relevant sub-ledger control account
            temp = accountIsCustomer ? 'ar' : 'ap'
            def ctrlAccount = bookService.getControlAccount(company, temp)
            if (!ctrlAccount) {
                srcAllocation.errorMessage(field: 'accountDifference', code: 'document.no.control', args: [temp],
                        default: "Could not find the ${temp} control account in the General Ledger")
                return false
            }

            // Need an open period for the manual fx difference document
            def now = utilService.fixDate()
            def period = bookService.selectPeriod(bookService.getOpenPeriods(company), now)
            if (!period) {
                srcAllocation.errorMessage(field: 'accountDifference', code: 'document.no.period', args: [temp],
                        default: 'Could not find an open period to post the document to')
                return false
            }

            // Will need an exchange difference document with a system type of FXD
            temp = 'FXD'
            def docType = getAutoDocumentOfType(temp, company)
            if (!docType) {
                srcAllocation.errorMessage(field: 'accountDifference', code: 'document.no.type', args: [temp], default: "Could not find a company document type with a system type of ${temp}")
                return false
            }

            def docNum = getNextDocumentNumber(docType)
            if (!docNum) {
                srcAllocation.errorMessage(field: 'accountDifference', code: 'document.bad.next', default: 'Unable to update the next document sequence number')
                return false
            }

            // Create the description and reference for the manual fx document
            def description = message(code: 'document.allocation.fx',
                    args: [sourceLine.document.type.code, sourceLine.document.code, srcAllocation.targetType.code, srcAllocation.targetCode],
                    default: "${sourceLine.document.type.code}${sourceLine.document.code} -> ${srcAllocation.targetType.code}${srcAllocation.targetCode} FX allocation difference")
            if (description.length() > 50) description = description.substring(0, 50)
            def reference = message(code: 'document.allocation.difference.label', default: 'FX Difference')
            if (reference.length() > 30) reference = reference.substring(0, 30)

            // Create the manual fx difference document and ensure we keep its lines in order
            fxDocument = new Document(currency: account.currency, type: docType, period: period, code: docNum.toString(), description: description,
                    documentDate: now, dueDate: now, reference: reference)
            fxDocument.lines = new ListOrderedSet()

            // Create a line in the document for the posting to the Customer/Supplier account
            fxAccountLine = new Line(description: description)
			fxAccountLine.account = ctrlAccount
            if (accountIsCustomer) {
                fxAccountLine.customer = account
            } else {
                fxAccountLine.supplier = account
            }

            // Fill in the basic line values
            fxAccountLine.accountValue = (programmatic || sourceLine.accountValue > 0.0) ? srcAllocation.accountDifference : -srcAllocation.accountDifference
            fxAccountLine.accountUnallocated = 0.0  // Will be fully allocated (the allocation is created below)
            fxAccountLine.companyUnallocated = 0.0
            fxAccountLine.documentValue = fxAccountLine.accountValue    // Must be the same as the account value since we created it that way
            if (account.currency.id == currency.id) {
                fxAccountLine.companyValue = fxAccountLine.accountValue     // No conversion required if the account currency is the company currency
            } else {    // Need to take a proportion of the line company value
                fxAccountLine.companyValue = utilService.round((sourceLine.companyValue * fxAccountLine.accountValue) / sourceLine.accountValue, currency.decimals)
            }

            fxAccountLine.generalValue = fxAccountLine.companyValue // The sub-ledger control account is always in company currency

            // Create the allocation of the manual fx difference document to the target line
            def fxSourceAllocation = new Allocation(targetType: srcAllocation.targetType, targetCode: srcAllocation.targetCode, targetId: targetLine.id,
                    period: period, accountValue: -fxAccountLine.accountValue, companyValue: -fxAccountLine.companyValue)

            // Add the allocation from the manual difference account line to the target line.
            // We can't add the inverse allocation from the target line to our account line
            // because our document hasn't been posted yet and so we don't know the line id.
            fxAccountLine.addToAllocations(fxSourceAllocation)

            // Create the inverse allocation from the target line to the manual fx difference document
            fxTargetAllocation = new Allocation(targetType: docType, targetCode: docNum.toString(), period: targetDocument.period,
                    accountValue: fxAccountLine.accountValue, companyValue: fxAccountLine.companyValue)

            // Create the line in the manual fx difference document to post to the GL fx difference account
            def fxDifferenceLine = new Line(description: description, documentValue: -fxAccountLine.documentValue,
                    companyValue: -fxAccountLine.companyValue, generalValue: -fxAccountLine.generalValue)
			fxDifferenceLine.account = fxAccount

            // Add the lines to the manual fx difference document
            fxDocument.addToLines(fxAccountLine)
            fxDocument.addToLines(fxDifferenceLine)
        }

        // If it's a foreign currency account then we might have fx differences on settlement
        def settled, settlementDocumentType, settlementDocumentNumber, lock
        if (account.currency.id != currency.id) {

            // Assume we wil have settlement differences
            settled = []

            // Since we may be doing a posting of an fx difference document at the end
            // of performing the allocation, we could possibly end up with a deadlock
            // situaton where we have the customer/supplier account locked and are waiting
            // to access the GL fxDiff account while some other user has the fxDiff account
            // locked and is now waiting for the customer/supplier account. So we use a
            // company lock to avoid this.
            lock = bookService.getCompanyLock(company)

            // We need to get the next FXD document number now in order to avoid potential
            // deadlock situations. This is in addition to the potntial deadlock mentioned above.
            temp = 'FXD'
            settlementDocumentType = getAutoDocumentOfType(temp, company)
            if (!settlementDocumentType) {
                srcAllocation.errorMessage(field: 'accountDifference', code: 'document.no.type', args: [temp], default: "Could not find a company document type with a system type of ${temp}")
                return false
            }

            settlementDocumentNumber = getNextDocumentNumber(settlementDocumentType)
            if (!settlementDocumentNumber) {
                srcAllocation.errorMessage(field: 'accountDifference', code: 'document.bad.next', default: 'Unable to update the next document sequence number')
                return false
            }
        }

        // Do the allocation
        def valid = true
        if (lock) lock.lock()
        try {
            Account.withTransaction {status ->

                // Post any manual fx difference document. This can't produce
                // a deadlock since we haven't done anything else yet
                if (fxDocument) valid = post(fxDocument, status)

                if (valid) {

                    // Lock the subsidiary ledger account and make sure we have current versions of the source and target lines
                    account = accountIsCustomer ? Customer.lock(account.id) : Supplier.lock(account.id)
                    sourceLine.refresh()
                    targetLine.refresh()

                    // Need to do any manual fx difference write-off allocation first because
                    // we didn't know the id of the account line until we just posted it above
                    if (fxDocument) {
                        fxTargetAllocation.targetId = fxAccountLine.id
                        targetLine.addToAllocations(fxTargetAllocation)
                        targetLine.accountUnallocated += fxTargetAllocation.accountValue
                        targetLine.companyUnallocated += fxTargetAllocation.companyValue
                    }

                    // Fill in the company currency values for the lines and allocations
                    setCompanyValues(account, accountIsCustomer, sourceLine, sourceAllocation, targetLine, targetAllocation, currency)

                    // Perform the allocation from the source line to the target line for the account currency
                    sourceLine.addToAllocations(sourceAllocation)
                    sourceLine.accountUnallocated += sourceAllocation.accountValue
                    valid = sourceLine.save()   // With deep validation
                    if (valid) {

                        // Perform the inverse allocation from the target line to the source line
                        targetLine.addToAllocations(targetAllocation)
                        targetLine.accountUnallocated += targetAllocation.accountValue
                        valid = targetLine.save()   // With deep validation
                    }

                    // Save the customer/supplier account
                    if (valid) valid = account.saveThis()

                    // See if we have any exchange differences to process
                    if (valid && account.currency.id != currency.id) {
                        if (!sourceLine.accountUnallocated && sourceLine.companyUnallocated) settled << sourceLine
                        if (!targetLine.accountUnallocated && targetLine.companyUnallocated) settled << targetLine
                        if (settled) {
                            temp = settle(account, accountIsCustomer, settled, company, currency, settlementDocumentType, settlementDocumentNumber, status)
                            if (temp) {
                                srcAllocation.errors.reject(null, temp)
                                valid = false
                            }
                        }
                    }
                } else {
                    srcAllocation.errorMessage(code: 'document.allocation.settlement', default: 'Unable to post the settlement exchange difference(s)')
                }

                if (!valid) {
                    status.setRollbackOnly()
                    srcAllocation.errorMessage(code: 'document.no.manual', default: 'Unable to perform manual allocation')
                }

            }
        } finally {
            if (lock) lock.unlock()
        }

        return valid
    }
	
	// Create the lines of an input document being entered by the user. Returns zero if the
	// lines are ok or the line number (one based) of the first line in error if a data binding
	// errors occur on a line. NOTE that this method simply re-creates the lines each time
	// without attempting to 'discard' any existing lines. This works because an accounting
	// document is NEVER editable and so this method would never be called on an existing
	// document (i.e. all the child lines are new and can be simply thrown away).
	def refreshDocumentLines(document, params) {
		document.lines = new ListOrderedSet()	// Throw away any attempt Grails might have made to load the lines - they get it wrong
		def pos = 0
		def err = 0
		def line, map
		while (true) {
			map = params."lines[${pos++}]"	// Note we increment 'pos' at this point so that we can return 'base 1' line numbers
			if (map == null) break	// No more lines[n] parameters, so we've done
				
			// Create a new document line, add in its transients and then add it to the document
			line = new Line(map)
			line.properties['documentTotal', 'documentDebit', 'documentCredit', 'accountCode', 'accountName', 'accountType'] = map
			document.addToLines(line)
			if (line.hasErrors()&& !err) err = pos
		}

		return err
	}

    // Create the lines of a template being entered. Returns zero if the lines are ok or the line
	// number (one based) of the first line in error if a data binding errors occur on a line. We
	// have to be careful with this since there may be existing lines being edited and therefore
	// we can't just throw them away since Grails might try to save them as new lines. We do all
	// this complicated messing around because Grails usually messes up the loading of child lines
	// from the params map and so the only safe way to do it is to do it ourselves.
    def refreshTemplateLines(template, params) {
		
		// Get rid of anything Grails might have tried to do
		for (line in template.lines) if (line) line.discard()
		template.lines = new ListOrderedSet()	// Start afresh with our own child list
        def pos = 0
        def err = 0
        def line, map
        while (true) {
            map = params."lines[${pos++}]"	// Note we increment 'pos' at this point so that we can return 'base 1' line numbers
            if (map == null) break
			
			// If an existing line record try and reload it, otherwise just set the line to null
			line = map.ident?.isLong() ? TemplateLine.findByIdAndSecurityCode(map.ident.toLong(), template.securityCode) : null
			if (!line) line = new TemplateLine(template: template)	// Create a new line if necessary
			line.properties = map	// Set the new property values
			line.properties['documentDebit', 'documentCredit', 'accountCode', 'accountName', 'accountType'] = map	// Transients
			template.lines.add(line)
            if (line.hasErrors()&& !err) err = pos
        }

        return err
    }
	
    // Create the lines of a report format being entered. We have to be careful with this since
	// there may be existing lines being edited and therefore we can't just throw them away since
	// Grails might try to save them as new lines. We do all this complicated messing around
	// because Grails usually messes up the loading of child lines from the params map and so the
	// only safe way to do it is to do it ourselves.
	def refreshReportLines(report, params, lineClass) {
		
		// Get rid of anything Grails might have tried to do
		for (line in report.lines) if (line) line.discard()
		report.lines = new ListOrderedSet()	// Start afresh with our own child list
        def pos = 0
        def err = 0
        def line, map
        while (true) {
            map = params."lines[${pos++}]"
            if (map == null) break
			
			// If an existing line record try and reload it, otherwise just set the line to null
			line = map.ident?.isLong() ? lineClass.findByIdAndSecurityCode(map.ident.toLong(), report.securityCode) : null
			
			// Create a new line if necessary, not forgetting to set its parent report format
			if (!line) {
				line = lineClass.newInstance()
				line.format = report
			}
			
			line.properties = map	// Set the new property values
			report.lines.add(line)
        }
	}
	
	// Create the lines of a recurring bank transaction being entered. Returns zero if the lines
	// are ok or the line number (one based) of the first line in error if a data binding errors
	// occur on a line. We have to be careful with this since there may be existing lines being
	// edited and therefore we can't just throw them away since Grails might try to save them as
	// new lines. We do all this complicated messing around because Grails usually messes up the
	// loading of child lines from the params map and so the only safe way to do it is to do it
	// ourselves.
	def refreshRecurringLines(recurring, params) {
		
		// Get rid of anything Grails might have tried to do
		for (line in recurring.lines) if (line) line.discard()
		recurring.lines = new ListOrderedSet()	// Start afresh with our own child list
		def pos = 0
		def err = 0
		def line, map
		while (true) {
			map = params."lines[${pos++}]"	// Note we increment 'pos' at this point so that we can return 'base 1' line numbers
			if (map == null) break
			
			// If an existing line record try and reload it, otherwise just set the line to null
			line = map.ident?.isLong() ? RecurringLine.findByIdAndSecurityCode(map.ident.toLong(), recurring.securityCode) : null
			if (!line) line = new RecurringLine(recurrence: recurring)	// Create a new line if necessary
			line.properties = map	// Set the new property values
			line.properties['accountCode', 'accountName', 'accountType', 'used'] = map	// Transients
			recurring.lines.add(line)
			if (line.hasErrors()&& !err) err = pos
		}

		return err
	}

    // Checks whether a document line can be posted to the specified account. Returns true if posting is allowed or
    // false if not allowed. If posting is not allowed, the document will have an error message attached and the line
    // will have an error indicator attached to its code field.
    def canPostDocumentToAccount(document, line, num, account) {
        if (account.type) {
            switch (document.type.type.metaType) {
                case 'invoice':
                    if (!account.type.allowInvoices) {
                        def temp = message(code: 'account.no.invoices', args: [account.code], default: "Posting of invoices or credit notes is not allowed to GL account ${account.code}")
                        document.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('accountCode', null)
                        return false
                    }
                    break

                case 'cash':
                    if (!account.type.allowCash) {
                        def temp = message(code: 'account.no.cash', args: [account.code], default: "Posting of payments or receipts is not allowed to GL account ${account.code}")
                        document.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('accountCode', null)
                        return false
                    }
                    break

                case 'provision':
                    if (!account.type.allowProvisions) {
                        def temp = message(code: 'account.no.provisions', args: [account.code], default: "Posting of accruals or prepayments is not allowed to GL account ${account.code}")
                        document.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('accountCode', null)
                        return false
                    }
                    break

                case 'journal':
                    if (!account.type.allowJournals) {
                        def temp = message(code: 'account.no.journals', args: [account.code], default: "Posting of journals is not allowed to GL account ${account.code}")
                        document.errorMessage(code: 'document.line.message', args: [num, temp], default: "Line ${num}: ${temp}")
                        line.errors.rejectValue('accountCode', null)
                        return false
                    }
                    break
            }
        }

        return true
    }

    // Create the model for entry of a customer or supplier invoice or credit note
    def getSubLedgerInvoiceModel(company, ledger, documentInstance) {
        def docTypes = (ledger == 'customer') ? "'SI', 'SC'" : "'PI', 'PC'"
        def documentTypeList = DocumentType.findAll('from DocumentType as dt where dt.company = ? and dt.type.code in (' + docTypes + ')', [company])
        def taxCodeList = TaxCode.findAllByCompany(company, [sort: 'code', cache: true])
        def periodList = bookService.getOpenPeriods(company)
        def currencyList = ExchangeCurrency.findAllByCompany(company, [cache: true])
        if (!documentInstance.period) documentInstance.period = bookService.selectPeriod(periodList)
        if (!documentInstance.currency) documentInstance.currency = utilService.companyCurrency()
        periodList = periodList.reverse()
        def settings = [:]
        settings.codeGenerate = documentInstance.type?.autoGenerate
        settings.codeEdit = documentInstance.type?.allowEdit
        settings.decimals = documentInstance.currency.decimals
        if (documentInstance.sourceCode && !documentInstance.sourceName) {
            def subAccount
            if (ledger == 'customer') {
                subAccount = Customer.findByCompanyAndCode(company, bookService.fixCustomerCase(documentInstance.sourceCode))
                if (subAccount && bookService.hasCustomerAccess(subAccount)) documentInstance.sourceName = subAccount.name
            } else {
                subAccount = Supplier.findByCompanyAndCode(company, bookService.fixSupplierCase(documentInstance.sourceCode))
                if (subAccount && bookService.hasSupplierAccess(subAccount)) documentInstance.sourceName = subAccount.name
            }
        }

        return [documentInstance: documentInstance, documentTypeList: documentTypeList, taxCodeList: taxCodeList, periodList: periodList, currencyList: currencyList, settings: settings]
    }

    // Used only for documents that do not have a total (such as journals) and that are NOT in the company
    // currency. The document should have had any unused/blank lines already removed. The difference must be
    // the value in company currency by which the document is out, NOT the amount needed to correct it. If the
    // document is in the company currency, has a total line, less than three detail lines or the difference is zero
    // an exception will be thrown since these are programmatic errors. This method is intended to be called just
    // before the document is posted. A single line on the document is victimized with the whole adjustment and this
    // will be the line with greatest absolute value.
    def balanceDocument(document, difference) {
        def companyCurrency = utilService.companyCurrency()
        if (document.currency.id == companyCurrency.id) throw new IllegalArgumentException(message(code: 'document.co.currency', default: 'A document in the company currency cannot be subject to balancing'))
        if (document.total) throw new IllegalArgumentException(message(code: 'document.has.total', default: 'A document with a total cannot be subject to balancing'))
        if (document.lines?.size() < 3) throw new IllegalArgumentException(message(code: 'document.few.lines', default: 'A document with less than three lines cannot be subject to balancing'))
        def correction = utilService.round(-difference, companyCurrency.decimals)
        if (!correction) throw new IllegalArgumentException(message(code: 'document.no.difference', args: [difference.toPlainString()], default: "Attempt to balance a document with no difference (${difference.toPlainString()})"))
        def victim, corrected
        for (line in document.lines) {
            if (!victim || line.companyValue.abs() > victim.companyValue.abs()) victim = line
        }

        corrected = victim.companyValue + correction
        if (!corrected) throw new IllegalArgumentException(message(code: 'document.no.correction', args: [correction.toPlainString()], default: "Unable to find a line to balanace ${correction.toPlainString()} off against"))
        def isSubLedger = false
        def account = victim.customer ?: victim.supplier
        if (account) {
            isSubLedger = true
        } else {
            account = victim.account ?: victim.balance.account  // Can be either, depending upon how the caller decided to prepare the line for posting
        }

        victim.companyValue = corrected
        if (isSubLedger) {
            victim.generalValue = corrected   // The ar/ap control account value is in company currency
            victim.companyUnallocated = corrected
            if (account.currency.id == companyCurrency.id) {
                victim.accountValue = corrected
                victim.accountUnallocated = corrected   // Don't forget the allocation amount
            }
        } else {
            if (account.currency.id == companyCurrency.id) victim.generalValue = corrected
        }
    }

    // Find the next company document type, with the given system type code,
    // that allows auto number generation
    def getAutoDocumentOfType(type, company = utilService.currentCompany()) {
        def sysType = systemDocumentTypes.get(type)
        if (sysType == CacheService.IMPOSSIBLE_VALUE) return null
        if (!sysType) {
            sysType = SystemDocumentType.findByCode(type)
            if (!sysType) sysType = CacheService.IMPOSSIBLE_VALUE
            systemDocumentTypes.put(type, sysType)
            if (sysType == CacheService.IMPOSSIBLE_VALUE) return null
        }

        def docTypes = DocumentType.findAll('from DocumentType where company = ? and type = ? and autoGenerate = ? order by id', [company, sysType, true], [max: 1])
        return docTypes ? docTypes[0] : null
    }

    // Get the next document number. If count > 1 then
    // the first of the reserved numbers is returned.
    // Should not be called within a transaction.
    def getNextDocumentNumber(docType, count = 1) {
        def docNum
        DocumentType.withTransaction {status ->
            docType = DocumentType.lock(docType.id)
            if (docType) {
                docNum = docType.nextSequenceNumber
                docType.nextSequenceNumber += count
                if (!docType.saveThis()) {
                    docNum = null
                    status.setRollbackOnly()
                }
            }
        }

        return docNum
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    // Set the company values for an allocation and the line it belongs to
    private setCompanyValues(account, accountIsCustomer, sourceLine, sourceAllocation, targetLine, targetAllocation, currency) {
        def usage
        if (account.currency.id == currency.id) {
            usage = sourceAllocation.accountValue
        } else {
            if (accountIsCustomer) {
                usage = (sourceLine.accountUnallocated < 0.0) ? getCompanyValue(sourceLine, sourceAllocation, currency) : -getCompanyValue(targetLine, targetAllocation, currency)
            } else {
                usage = (sourceLine.accountUnallocated > 0.0) ? getCompanyValue(sourceLine, sourceAllocation, currency) : -getCompanyValue(targetLine, targetAllocation, currency)
            }
        }

        sourceLine.companyUnallocated += usage
        sourceAllocation.companyValue = usage
        targetLine.companyUnallocated -= usage
        targetAllocation.companyValue = -usage
    }

    // Get the company currency equivalent of an account currency value being allocated
    // This method must be called before the line values have had the allocation applied to them
    private getCompanyValue(line, allocation, currency) {

        // If the line is going to end up being zero, use all the remaining company unallocated value
        if (line.accountUnallocated + allocation.accountValue == 0.0) return -line.companyUnallocated

        // If the unallocated amount is not zero, we can take a proportion of the company unallocated amount
        if ((line.accountUnallocated < 0.0 && line.companyUnallocated < 0.0) || (line.accountUnallocated > 0.0 && line.companyUnallocated > 0.0)) {
            return utilService.round((line.companyUnallocated * allocation.accountValue) / line.accountUnallocated, currency.decimals)
        }

        // Use a proportion of the original company value
        return utilService.round((line.companyValue * allocation.accountValue) / line.accountValue, currency.decimals)
    }

    // Handle foreign exchange differences on settlement of documents involving foreign
    // currency. The differences can be caused by rounding errors and/or exchange rate
    // fluctuations. This method is for the use of the AllocateLine method. Returns null
    // if it succeeds or an error message if not.
    private settle(account, accountIsCustomer, lines, company, currency, settlementDocumentType, settlementDocumentNumber, status) {

        // Need to find the fxDiff account in the GL
        def temp = 'fxDiff'
        def fxAccount = bookService.getControlAccount(company, temp)
        if (!fxAccount) return message(code: 'document.no.control', args: [temp], default: "Could not find the ${temp} control account in the General Ledger")

        // Get the relevant sub-ledger control account
        temp = accountIsCustomer ? 'ar' : 'ap'
        def ctrlAccount = bookService.getControlAccount(company, temp)
        if (!ctrlAccount) return message(code: 'document.no.control', args: [temp], default: "Could not find the ${temp} control account in the General Ledger")

        // Need an open period for the manual fx difference document
        def now = utilService.fixDate()
        def period = bookService.selectPeriod(bookService.getOpenPeriods(company), now)
        if (!period) return message(code: 'document.no.period', args: [temp], default: 'Could not find an open period to post the document to')

        // Create the description and document total (company currency)
        def sourceAllocations = []
        def targetAllocations = []
        def allocation
        def difference = 0.0
        temp = ''
        for (line in lines) {
            if (temp) temp += ' & '
            temp += line.document.type.code + line.document.code
            difference -= line.companyUnallocated

            // Create the allocation of the automatic fx difference document to the target line
            allocation = new Allocation(targetType: line.document.type, targetCode: line.document.code, targetId: line.id,
                    period: period, accountValue: 0.0, companyValue: line.companyUnallocated)
			allocation.accountZeroAllowed = true
            sourceAllocations << allocation

            // Create the inverse allocation of the target line to the automatic fx difference document.
            // Note that we can't fill in the targetId at this stage since we haven't posted our document yet
            allocation = new Allocation(targetType: settlementDocumentType, targetCode: settlementDocumentNumber.toString(),
                    period: line.document.period, accountValue: 0.0, companyValue: -line.companyUnallocated)
			allocation.accountZeroAllowed = true
            targetAllocations << allocation

            // Update the line company unallocated amount, which will now be zero
            line.companyUnallocated = 0.0
        }

        def description = message(code: 'document.allocation.settle.desc', args: [temp], default: "${temp} difference(s)")
        if (description.length() > 50) description = description.substring(0, 50)
        def reference = message(code: 'document.allocation.settle.ref', default: 'FX Settlement')
        if (reference.length() > 30) reference = reference.substring(0, 30)

        def fxAccountLine   // Will be needed outside the if/else blocks

        // If the difference is not zero we need to create ax automatic FX document to write it off
        if (difference) {

            // Create the automatic fx difference document and ensure we keep its lines in order
            def fxDocument = new Document(currency: currency, type: settlementDocumentType, period: period, code: settlementDocumentNumber.toString(), description: description,
                    documentDate: now, dueDate: now, reference: reference)
            fxDocument.lines = new ListOrderedSet()

            // Create a line in the document for the posting to the Customer/Supplier account
            fxAccountLine = new Line(description: description)
			fxAccountLine.account = ctrlAccount
            if (accountIsCustomer) {
                fxAccountLine.customer = account
            } else {
                fxAccountLine.supplier = account
            }

            // Fill in the basic line values
            fxAccountLine.accountValue = 0.0
            fxAccountLine.accountUnallocated = 0.0  // Will be fully allocated (the allocation is created above)
            fxAccountLine.companyUnallocated = 0.0
            fxAccountLine.documentValue = difference
            fxAccountLine.companyValue = difference
            fxAccountLine.generalValue = difference // The sub-ledger control account is always in company currency

            // Add in the source allocations
            for (alloc in sourceAllocations) fxAccountLine.addToAllocations(alloc)

            // Create the line in the manual fx difference document to post to the GL fx difference account
            def fxDifferenceLine = new Line(description: description, documentValue: -difference, companyValue: -difference, generalValue: -difference)
			fxDifferenceLine.account = fxAccount

            // Add the lines to the manual fx difference document
            fxDocument.addToLines(fxAccountLine)
            fxDocument.addToLines(fxDifferenceLine)

            // Post our settlement document with deep validation
            if (!post(fxDocument, status)) return message(code: 'document.allocation.settlement', default: 'Unable to post the settlement exchange difference(s)')
        } else {    // A difference of zero is unlikely, but possible, so we just set-off the differences by allocations

            // Need to double check that there is more than one line involved
            if (lines.size() != 2) return message(code: 'document.allocation.no.diff', default: 'A settlement difference cannot be zero')

            // Replace the existing allocations with the set-off ones
            def line0 = lines[0]
            def line1 = lines[1]
            allocation = new Allocation(targetType: line1.document.type, targetCode: line1.document.code, targetId: line1.id,
                    period: line0.document.period, accountValue: 0.0, companyValue: -line0.companyUnallocated)
			allocation.accountZeroAllowed = true
			targetAllocations[0] = allocation
            allocation = new Allocation(targetType: line0.document.type, targetCode: line0.document.code, targetId: line0.id,
                    period: line1.document.period, accountValue: 0.0, companyValue: -line1.companyUnallocated)
			allocation.accountZeroAllowed = true
			targetAllocations[1] = allocation
        }

        // Update the lines
        for (int i = 0; i < lines.size(); i++) {
            temp = lines[i]
            allocation = targetAllocations[i]
            if (fxAccountLine) allocation.targetId = fxAccountLine.id    // Will not have been set if we have created an auto-setllement document
            temp.addToAllocations(allocation)
            if (!temp.save()) return message(code: 'document.allocation.error', default: 'Unable to update the document line with the fx settlement difference')    // With deep validation
        }

        return null
    }

    private postDocument(document, periods, index, retained) {

        def tot = 0.0

        // Save the document
        if (!document.save()) return false   // With deep validation

        // Post the document 'goods' lines and update turnover value if required (Journals may need this)
        for (line in document.lines) {
            tot += line.companyValue
            if (!postDocumentLine(document, line, periods, index, retained)) return false
        }

        // Post any document tax lines (never affects turnover)
        for (line in document.taxes) {
            tot += line.companyValue
            if (!postDocumentLine(document, line, periods, index, retained)) return false
        }

        // Post any total line and update turnover value if required (Invoice etc would use this, but payments etc wouldn't)
        for (line in document.total) {
            tot += line.companyValue
            if (!postDocumentLine(document, line, periods, index, retained)) return false
        }

        if (tot) throw new IllegalArgumentException((String) message(code: 'document.posting.error', args: [tot.toPlainString()],
                default: "Posting failure. Document does not balance by ${tot.toPlainString()}"))

        return true
    }

    // Change a document's posting values to/from their original state to debit/credit values
    private adjustDocumentValues(document, analysisIsDebit) {
        if (analysisIsDebit) {
            for (line in document.total) negate(line)
        } else {
            for (line in document.lines) negate(line)
            for (line in document.taxes) negate(line)
        }
    }

    // Reverse the posting values of a general transaction
    private negate(line) {
        if (line.documentTax) line.documentTax = -line.documentTax
        if (line.documentValue) line.documentValue = -line.documentValue
        if (line.accountTax) line.accountTax = -line.accountTax
        if (line.accountValue) line.accountValue = -line.accountValue
        if (line.accountUnallocated) line.accountUnallocated = -line.accountUnallocated
        if (line.generalTax) line.generalTax = -line.generalTax
        if (line.generalValue) line.generalValue = -line.generalValue
        if (line.companyTax) line.companyTax = -line.companyTax
        if (line.companyValue) line.companyValue = -line.companyValue
        if (line.companyUnallocated) line.companyUnallocated = -line.companyUnallocated
    }

    // Posts a transaction in the general ledger and updates the sub-ledger balance and turnover (if required) if
    // a customer or supplier is included in the line. It is assumed that the company lock has been obtained and
    // that we are in a database transaction. Returns true if the transaction was posted or false if an error
    // occurred (in which case the document will have an error message attached to it).
    private postDocumentLine(document, line, periods, index, revAccount) {
        def balance = line.balance
        if (line.adjustment) {
            balance.generalAdjustmentTotal += line.generalValue
            balance.companyAdjustmentTotal += line.companyValue
        } else {
            balance.generalTransactionTotal += line.generalValue
            balance.companyTransactionTotal += line.companyValue
        }

        balance.generalClosingBalance += line.generalValue
        balance.companyClosingBalance += line.companyValue
        if (!balance.saveThis()) {
            document.errorMessage(code: 'document.bad.balance', args: [line.account.code, document.period.code], default: "Could not update the balance record for GL account ${line.account.code} in period ${document.period.code}")
            return false
        }

        for (int i = index + 1; i < periods.size(); i++) {

            // If we are rolling over a year end for an income and expenditure account
            if (periods[i].year.id != periods[index].year.id && line.account.section.type == 'ie') {
                balance = GeneralBalance.findByAccountAndPeriod(revAccount, periods[i])
                balance.generalOpeningBalance += line.generalValue
                balance.generalClosingBalance += line.generalValue
                balance.companyOpeningBalance += line.companyValue
                balance.companyClosingBalance += line.companyValue
            } else {    // Same accounting year or a balance sheet account
                balance = GeneralBalance.findByAccountAndPeriod(line.account, periods[i])
                balance.generalOpeningBalance += line.generalValue
                balance.generalClosingBalance += line.generalValue
                balance.companyOpeningBalance += line.companyValue
                balance.companyClosingBalance += line.companyValue
            }

            if (!balance.saveThis()) {
                document.errorMessage(code: 'document.bad.balance', args: [line.account.code, period[i].code], default: "Could not update the balance record for GL account ${line.account.code} in period ${period[i].code}")
                return false
            }
        }

        // Update the sub-ledger balance if required and its turnover record if turnover is affected.
        if (line.customer) {
            line.customer.refresh()
            line.customer.accountCurrentBalance += line.accountValue
            line.customer.companyCurrentBalance += line.companyValue
            if (!line.customer.saveThis()) {
                document.errorMessage(code: 'document.customer.error', args: [line.customer.code],
                        default: "Could not update customer account ${line.customer.code}")
                return false
            }

            if (line.affectsTurnover) {
                def rec = CustomerTurnover.findByCustomerAndPeriod(line.customer, document.period)
                rec.accountTurnover += line.accountValue
                rec.companyTurnover += line.companyValue

                if (!rec.saveThis()) {
                    document.errorMessage(code: 'document.ar.turnover', args: [line.customer.code, document.period.code],
                            default: "Unable to update the turnover record for customer ${line.customer.code} in period ${document.period.code}")
                    return false
                }
            }
        } else if (line.supplier) {
            line.supplier.refresh()
            line.supplier.accountCurrentBalance -= line.accountValue
            line.supplier.companyCurrentBalance -= line.companyValue
            if (!line.supplier.saveThis()) {
                document.errorMessage(code: 'document.supplier.error', args: [line.supplier.code],
                        default: "Could not update supplier account ${line.supplier.code}")
                return false
            }

            if (line.affectsTurnover) {
                def rec = SupplierTurnover.findBySupplierAndPeriod(line.supplier, document.period)
                rec.accountTurnover -= line.accountValue
                rec.companyTurnover -= line.companyValue

                if (!rec.saveThis()) {
                    document.errorMessage(code: 'document.ap.turnover', args: [line.supplier.code, document.period.code],
                            default: "Unable to update the turnover record for supplier ${line.supplier.code} in period ${document.period.code}")
                    return false
                }
            }
        }

        return true
    }
}
