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
package org.grails.tlc.rest

import org.grails.tlc.corp.CompanyUser
import org.grails.tlc.corp.ExchangeCurrency
import org.grails.tlc.corp.TaxCode
import org.grails.tlc.corp.TaxRate
import doc.Line
import doc.Tax
import doc.Total
import grails.converters.JSON
import java.security.SecureRandom
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.collections.set.ListOrderedSet
import sun.misc.BASE64Encoder
import org.grails.tlc.books.*

class RestService {

    static transactional = false

    def utilService
    def bookService
    def postingService
    def sessionFactory

    private static final TOLERANCE_MILLIS = 15L * 60L * 1000L       // 15 minutes timestamp tolerance
    private static final HEARTBEAT_MILLIS = 15L * 60L * 1000L       // 15 minutes between updates of a company user's lastUsed field
    private static final THROTTLE_MILLIS = 60L * 1000L              // Sixty seconds between REST interface uses per demo company
    private static final THROTTLE_MAP = [:]                         // The map for checking demo company REST interface usage
    private static final HMAC_SHA1_ALGORITHM = 'HmacSHA1'
    private static final REST_CHARS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789abcdefghijklmnopqrstuvwxyz+-'
    private static final REST_ID_LENGTH = 20
    private static final REST_SECRET_LENGTH = 40

    // The activity codes required for permissions to post the given document types
    private static final postingActivities = [SI: 'arinvoice', SC: 'arinvoice', PI: 'apinvoice', PC: 'apinvoice',
        BP: 'bankentry', BR: 'bankentry', CP: 'cashentry', CR: 'cashentry', GLJ: 'gljournal', FJ: 'finjournal',
        ARJ: 'arjournal', APJ: 'apjournal', SOJ: 'sojournal', AC: 'provision', PR: 'provision']

    // Checks whether a request is a RESTful request and, if not, returns false.
    // Returns true if the request is a valid RESTful request (in which case the
    // session will have been set up and the JSON data placed in the params map)
    // or an error message if the request was invalid.
    def isRestful(session, request, params) {

        // Check if there is any attempt to sign this request as that
        // will be construed as this being a RESTful request.
        def signature = request.getHeader('tlc-signature')
        if (signature == null) return false

        // Verify the request timestamp is in date
        def val = request.getHeader('tlc-timestamp')
        def now = System.currentTimeMillis()
        if (!val || !val.isLong()) return message(code: 'rest.timestamp.invalid', default: 'Invalid REST timestamp')
        val = val.toLong()
        if (val < now - TOLERANCE_MILLIS || val > now + TOLERANCE_MILLIS) return message(code: 'rest.timestamp.range', default: 'REST timestamp is out of permitted range')

        // Get the user agent credentials
        def agent = AgentCredential.findByCode(request.getHeader('tlc-agent'), [cache: true])
        if (!agent?.active) return message(code: 'rest.agent.invalid', default: 'Invalid REST agent')

        // Update the company user lastUsed value if required
        def companyUser = agent.companyUser
        if (companyUser.lastUsed.getTime() < now - HEARTBEAT_MILLIS) {
            sessionFactory.currentSession.evict(companyUser)
            CompanyUser.withTransaction {status ->
                companyUser = CompanyUser.lock(companyUser.id)
                companyUser.lastUsed = new Date()
                if (!companyUser.saveThis()) {
                    status.setRollbackOnly()
                    companyUser = null
                }
            }

            // Make sure any update worked ok
            if (!companyUser) return message(code: 'rest.user.bad.update', default: 'Unable to update the companyUser lastUsed value for a REST request')
        }

        // Set up the session with the company and user data
        def company = companyUser.company
        def user = companyUser.user
        session.userdata = [companyId: company.id, userId: user.id, logoName: null, menuId: null]
        session.logindata = null
        session.filterdata = null
        session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' = new Locale(user.language.code, user.country.code)

        // Also set up the request details
        request.userrec = user
        request.companyrec = company
        request.currencyrec = null
        request.menurec = null

        // See if we need to throttle the demo system
        if (utilService.systemSetting('isDemoSystem', false)) {
            synchronized (THROTTLE_MAP) {
                val = THROTTLE_MAP.put(company.id.toString(), now) ?: 0L
            }

            if (now - val < THROTTLE_MILLIS) return message(code: 'rest.throttled', default: 'Request exceeds the demo system REST interface usage frequency')
        }

        // Build up the tlc headers (lower case keys) in alphabetic order
        def headers = new TreeMap()
        for (it in request.headerNames) {
            val = it.toLowerCase(Locale.US)	// Need to use US locale to avoid things like the Turkish undotted i
            if (val.startsWith('tlc-') && val.length() > 4 && val != 'tlc-signature') headers.put(val, request.getHeader(it))
        }

        // Build up the plain text starting with the tlc headers
        def sb = new StringBuilder()
        for (it in headers) {
            if (it.value != null && it.value != '') {
                sb.append(it.key)
                sb.append(it.value)
            }
        }

        // Add in any request body (also parsing it in to a JSON object)
        def body
        if (request.contentLength) {
            val = new byte[request.contentLength]
            request.inputStream.read(val)
            val = new String(val, 'UTF-8')
            sb.append(val)
            body = JSON.parse(val)
        }

        // Create a new signature (SHA1 HMAC, Base64 encoded) and see that it matches
        Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
        mac.init(new SecretKeySpec(agent.secret.getBytes(), HMAC_SHA1_ALGORITHM))
        val = (new BASE64Encoder()).encode(mac.doFinal(sb.toString().getBytes()))
        if (val != signature) return message(code: 'rest.signature.invalid', default: 'Invalid REST signature')

        // Store the data in the parameters map
        params.data = body
        return true
    }

    // Creates a new agent code for the agent credentials
    def createAgentCode() {
        return getRandomString(REST_ID_LENGTH)
    }

    // Creates a new secret for the agent credentials
    def createAgentSecret() {
        return getRandomString(REST_SECRET_LENGTH)
    }

    // Return a String value given a JSON value. May return null. Note that an empty string is assumed to be null.
    def getString(val) {
        return (!val || val.equals(null)) ? null : val.toString()
    }

    // Return an Integer value given a JSON number (or string). May return null.
    def getInteger(val) {
        if (val == null || val.equals(null)) return null
        return (val instanceof CharSequence) ? new Integer(val) : val.intValue()
    }

    // Return a Long value given a JSON number (or string). May return null.
    def getLong(val) {
        if (val == null || val.equals(null)) return null
        return (val instanceof CharSequence) ? new Long(val) : val.longValue()
    }

    // Return a BigDecimal value given a JSON number (or string). May return null. Note that the precision of the returned value is unspecified.
    def getDecimal(val) {
        if (val == null || val.equals(null)) return null
        return new BigDecimal(val)
    }

    // Return a Date value given a JSON string encoded date. May return null. Note that the date may or may not include a time portion.
    def getDate(val) {
        return (!val || val.equals(null)) ? null : Date.parse(val.length() > 10 ? 'yyyy-MM-dd HH:mm' : 'yyyy-MM-dd', val)
    }

    // Return a Date value given a JSON string encoded date. May return null. Note that the date MUST NOT include a time portion.
    def getDateOnly(val) {
        return (!val || val.equals(null)) ? null : Date.parse('yyyy-MM-dd', val)
    }

    // Return a Boolean value given a JSON value. May return null. Note that an empty string or a zero number would return false.
    def getBoolean(val) {
        return (val == null || val.equals(null)) ? null : val ? true : false
    }

    // A convenience method (primarily for the Security Filter) to return a localized message
    def getMessage(map) {
        return message(map)
    }

    // Return a Map containing a valid Document object to be posted and lists of manual
    // and automatic allocations, or, an containing an error message. If the document
    // being returned is a provision, the map will also contain a reversal document.
    def getDocument(data) {
        def company = utilService.currentCompany()
        def companyCurrency = utilService.companyCurrency()

        // Get the basic data from the JSON map
        if (!data || !(data instanceof Map)) return message(code: 'rest.no.data', default: 'No data in the REST request')
        def headerData = data.header
        if (!headerData || !(headerData instanceof Map)) return message(code: 'rest.no.header', default: 'No document header data in the REST request')
        def lineData = data.lines
        if (!lineData || !(lineData instanceof List)) return message(code: 'rest.no.lines', default: 'No document line data in the REST request')
        for (line in lineData) {
            if (!(line instanceof Map)) return message(code: 'rest.bad.lines', default: 'Document line data in the REST request is not a Map')
            def allocs = line.allocations
            if (allocs && !(allocs instanceof List)) return message(code: 'rest.bad.allocs', default: 'Document line allocations is not a List')
            for (alloc in allocs) {
                if (!(alloc instanceof Map)) return message(code: 'rest.bad.alloc', default: 'Document line allocation data in the REST request is not a Map')
            }
        }

        // See if we can find the appropriate document type
        def doctype = DocumentType.findByCompanyAndCode(company, getString(headerData.type))
        if (!doctype) return message(code: 'document.bad.type', default: 'Invalid document type')
        def systype = doctype.type.code
        if (!utilService.permitted(postingActivities.get(systype))) return message(code: 'rest.no.permission', default: 'REST request permission denied')

        // Set some flags to tell us what document type it is etc
        def isJournal = ['GLJ', 'FJ', 'ARJ', 'APJ', 'SOJ'].contains(systype)
        def isInvoice = ['SI', 'SC', 'PI', 'PC'].contains(systype)
        def isBank = ['BP', 'BR'].contains(systype)
        def isCash = ['CP', 'CR'].contains(systype)
        def isProvision = ['AC', 'PR'].contains(systype)
        def canAdjust = ['GLJ', 'FJ', 'AC', 'PR'].contains(systype)

        // Create the new document
        def doc = new Document()
        doc.lines = new ListOrderedSet()
        def total, val, invoiceAccount, accountCurrency, docline, forbiddenAccount, glAllowed, arAllowed, apAllowed, reversal
        def duplicateCustomers = [:]
        def duplicateSuppliers = [:]

        // See if a total seems valid
        def totalData = data.total
        if (totalData) {
            if (!(totalData instanceof Map)) return message(code: 'rest.bad.total', default: 'Document total data in the REST request is not a Map')
            if (isJournal) return message(code: 'rest.has.total', default: 'Superfluous document total data in the REST request')
            total = new Total()
            doc.addToTotal(total)
            if (isProvision) {
                val = (systype == 'AC') ? 'accrue' : 'prepay'
                total.account = bookService.getControlAccount(company, val)
                if (!total.account) return message(code: 'document.no.control', args: [val], default: "Could not find the ${val} control account in the General Ledger")
            } else {
                if (!setAccount(totalData, doc, total, 0)) return message(code: 'rest.total.account', default: 'Account not found in total line of REST request')
                if (isInvoice) {
                    invoiceAccount = total.customer ?: total.supplier
                    if (!invoiceAccount) {
                        return (['SI', 'SC'].contains(systype)) ?
                            message(code: 'rest.bad.customer', default: 'Invalid customer in total line') :
                            message(code: 'rest.bad.supplier', default: 'Invalid supplier in total line')
                    }
                } else if (isBank) {
                    if (total.account.type.code != 'bank') return message(code: 'bank.not.exists', default: 'Invalid Bank account')
                } else if (isCash) {
                    if (total.account.type.code != 'cash') return message(code: 'cash.not.exists', default: 'Invalid Cash account')
                }
            }
        } else {
            if (!isJournal) return message(code: 'rest.no.total', default: 'No document total data in the REST request')
        }

        // Get the document currency
        val = getString(headerData.currency)
        if (val) {
            val = ExchangeCurrency.findByCompanyAndCode(company, val)
            if (val) {
                doc.currency = val
            } else {
                return message(code: 'document.bad.currency', default: 'Invalid document currency')
            }
        } else {
            if (isInvoice) {
                doc.currency = invoiceAccount.currency
            } else if (isBank || isCash) {
                doc.currency = total.account.currency
            } else {
                doc.currency = companyCurrency
            }
        }

        // Prepare for multi currency handling
        def documentCurrency = doc.currency
        def otherCurrencies = [:]
        def today = utilService.fixDate()
        def companyRate = getRate(otherCurrencies, documentCurrency, companyCurrency, today)
        if (companyRate instanceof CharSequence) return companyRate
        def otherCurrency, rate

        // Ensure we have a tax control account
        val = 'tax'
        def taxControl = bookService.getControlAccount(company, val)
        if (!taxControl) return message(code: 'document.no.control', args: [val], default: "Could not find the ${val} control account in the General Ledger")
        def taxCodes = [:]
        def taxRates = [:]

        // Set up the validation details
        switch (systype) {
            case 'SI':
            case 'SC':
            case 'PI':
            case 'PC':
            case 'AC':
            case 'PR':
            case 'GLJ':
                glAllowed = true
                arAllowed = false
                apAllowed = false
                break

            case 'BP':
            case 'BR':
                glAllowed = true
                arAllowed = true
                apAllowed = true
                forbiddenAccount = total.account
                break

            case 'CP':
            case 'CR':
                glAllowed = true
                arAllowed = false
                apAllowed = false
                forbiddenAccount = total.account
                break

            case 'FJ':
                glAllowed = true
                arAllowed = true
                apAllowed = true
                break

            case 'ARJ':
                glAllowed = false
                arAllowed = true
                apAllowed = false
                break

            case 'APJ':
                glAllowed = false
                arAllowed = false
                apAllowed = true
                break

            case 'SOJ':
                glAllowed = false
                arAllowed = true
                apAllowed = true
                break

            default:
                error = message(code: 'document.bad.type', default: 'Invalid document type')
                break
        }

        // Load the header data
        def mapName = 'header'
        def fieldName, activePeriods
        def lineNum = 0
        def hold = false
        def adjustment = false
        def turnover = false
        def manualAllocations = []
        def autoAllocations = []

        // Prepare for totalling
        def documentValue = 0.0
        def documentTax = 0.0
        def companyValue = 0.0
        def companyTax = 0.0
        def accountValue = 0.0
        def accountTax = 0.0
        def generalValue = 0.0  // Only used when a total line exists
        def generalTax = 0.0    // Only used when a total line exists

        try {
            // Load up the header data
            fieldName = 'date'
            doc.documentDate = getDateOnly(headerData.date)
            if (!doc.documentDate || doc.documentDate < today - 365 || doc.documentDate > today + 365) return message(code: 'document.documentDate.invalid', default: 'Document date is invalid')
            fieldName = 'reference'
            doc.reference = getString(headerData.reference)
            fieldName = 'description'
            doc.description = getString(headerData.description)
            fieldName = 'hold'
            if (getBoolean(headerData.hold)) hold = true
            fieldName = 'adjustment'
            if (canAdjust && getBoolean(headerData.adjustment)) adjustment = true
            fieldName = 'turnover'
            if (isInvoice || getBoolean(headerData.turnover)) turnover = true

            // Check we have a period available to post the document to
            activePeriods = bookService.getActivePeriods(company)
            doc.period = bookService.selectPeriod(adjustment ? activePeriods : bookService.getOpenPeriods(company), doc.documentDate)
            if (!doc.period) return message(code: 'document.no.period', default: 'Could not find an open period to post the document to')

            // Work out the due date
            fieldName = 'due'
            doc.dueDate = getDateOnly(headerData.due)
            if (doc.dueDate) {
                if (doc.dueDate < doc.documentDate || doc.dueDate > doc.documentDate + 250) return message(code: 'document.dueDate.invalid', default: 'Due date is invalid')
            } else {
                if (systype == 'SI') {
                    def base = invoiceAccount.periodicSettlement ? doc.period.validTo : doc.documentDate
                    def days = invoiceAccount.settlementDays ?: utilService.setting('customer.settlement.days', 30)
                    doc.dueDate = base + days
                } else if (systype == 'PI') {
                    def base = invoiceAccount.periodicSettlement ? doc.period.validTo : doc.documentDate
                    def days = invoiceAccount.settlementDays ?: utilService.setting('supplier.settlement.days', 30)
                    doc.dueDate = base + days
                } else {
                    doc.dueDate = doc.documentDate
                }
            }

            // Load any total data
            if (total) {
                mapName = 'total'
                fieldName = 'description'
                total.description = getString(totalData.description) ?: doc.description
                fieldName = 'value'
                val = getDecimal(totalData.value) ?: 0.0
                doc.sourceGoods = utilService.round(val, documentCurrency.decimals)
                if (val != doc.sourceGoods) return message(code: 'rest.tot.bad.value', default: 'Total value amount is invalid')
                if (val < 0.0) return message(code: 'rest.tot.value', default: 'Total value cannot be negative')
                fieldName = 'tax'
                val = getDecimal(totalData.tax) ?: 0.0
                doc.sourceTax = utilService.round(val, documentCurrency.decimals)
                if (val != doc.sourceTax) return message(code: 'rest.tot.bad.tax', default: 'Total tax amount is invalid')
                if (val < 0.0) return message(code: 'rest.tot.tax', default: 'Total tax cannot be negative')
                doc.sourceTotal = doc.sourceGoods + doc.sourceTax
                if (doc.sourceTotal == 0.0) return message(code: 'rest.tot.zero', default: 'Document total cannot be zero')
                fieldName = 'auto'
                val = getBoolean(totalData.auto)
                if (val) {
                    if (isInvoice) {
                        autoAllocations << invoiceAccount
                    } else {
                        return message(code: 'rest.total.auto', default: 'The total line specifies an auto allocation but is not being posted to an AR/AP account')
                    }
                }

                if (totalData.allocations) {
                    if (!isInvoice) return message(code: 'rest.total.alloc', default: 'The total line specifies allocations but is not being posted to an AR/AP account')
                    def targetType, targetDoc, targetLine
                    for (alloc in totalData.allocations) {
                        fieldName = 'allocations.type'
                        targetType = DocumentType.findByCompanyAndCode(company, getString(alloc.type))
                        if (!targetType) return message(code: 'rest.total.target', default: 'Invalid document specified in total line allocation')
                        fieldName = 'allocations.code'
                        targetDoc = Document.findByTypeAndCode(targetType, getString(alloc.code))
                        if (!targetDoc) return message(code: 'rest.total.target', default: 'Invalid document specified in total line allocation')
                        targetLine = total.customer ?
                                GeneralTransaction.findByDocumentAndCustomer(targetDoc, total.customer) :
                                GeneralTransaction.findByDocumentAndSupplier(targetDoc, total.supplier)
                        if (!targetLine?.accountValue) return message(code: 'rest.total.target', default: 'Invalid document specified in total line allocation')
                        fieldName = 'allocations.value'
                        val = getDecimal(alloc.value)
                        if (!val || val != utilService.round(val, documentCurrency.decimals)) return message(code: 'rest.total.value', default: 'Invalid value specified in total line allocation')
                        otherCurrency = invoiceAccount.currency
                        if (otherCurrency.code == documentCurrency.code) {
                            rate = 1.0
                        } else if (otherCurrency.code == companyCurrency.code) {
                            rate = companyRate
                        } else {
                            rate = getRate(otherCurrencies, documentCurrency, otherCurrency, today)
                            if (rate instanceof CharSequence) return rate
                        }

                        manualAllocations << new Allocation(transaction: total, targetType: targetType, period: doc.period, targetCode: targetDoc.code,
                                accountValue: utilService.round(val * rate, otherCurrency.decimals),
                                companyValue: utilService.round(val * companyRate, companyCurrency.decimals))
                    }
                }

                // Set the flags from the header
                total.adjustment = adjustment
                if (isInvoice) {
                    total.onHold = hold
                    total.affectsTurnover = turnover
                }
            }

            // Load the line data
            mapName = 'lines'
            for (line in lineData) {
                lineNum++
                docline = new Line(adjustment: adjustment)
                doc.addToLines(docline)
                fieldName = 'account'
                if (!setAccount(line, doc, docline, lineNum)) return message(code: 'rest.line.account', args: [lineNum], default: "Account not found in line ${lineNum} of REST request")

                // Do some basic validation and set hold/turnover flag if required
                if (docline.customer) {
                    if (!arAllowed) return message(code: 'rest.no.customer', args: [lineNum], default: "Line ${lineNum} specifies a customer account which is not allowed at this point")
                    if (duplicateCustomers.put(docline.customer.code, true)) return message(code: 'rest.duplicate.customer', args: [lineNum],
                        default: "Line ${lineNum} contains a duplicate customer account. Please combine in to a single line.")
                    docline.onHold = hold
                    docline.affectsTurnover = turnover
                } else if (docline.supplier) {
                    if (!apAllowed) return message(code: 'rest.no.supplier', args: [lineNum], default: "Line ${lineNum} specifies a supplier account which is not allowed at this point")
                    if (duplicateSuppliers.put(docline.supplier.code, true)) return message(code: 'rest.duplicate.supplier', args: [lineNum],
                        default: "Line ${lineNum} contains a duplicate supplier account. Please combine in to a single line.")
                    docline.onHold = hold
                    docline.affectsTurnover = turnover
                } else {
                    if (!glAllowed) return message(code: 'rest.no.general', args: [lineNum], default: "Line ${lineNum} specifies a GL account which is not allowed at this point")
                    if (docline.account.code == forbiddenAccount?.code) return message(code: 'rest.invalid.general', args: [lineNum],
                        default: "Line ${lineNum} specifies an invalid general ledger account")
                }

                fieldName = 'description'
                docline.description = getString(line.description)
                fieldName = 'value'
                val = getDecimal(line.value) ?: 0.0
                if (!val) return message(code: 'rest.line.zero', args: [lineNum], default: "Line ${lineNum} does not contain a value")
                docline.documentValue = utilService.round(val, documentCurrency.decimals)
                if (val != docline.documentValue) return message(code: 'rest.bad.value', args: [lineNum], default: "Line ${lineNum} contains an invalid monetary value")
                fieldName = 'tax'
                val = getDecimal(line.tax)
                if (val != null) {
                    docline.documentTax = utilService.round(val, documentCurrency.decimals)
                    if (val != docline.documentTax) return message(code: 'rest.bad.tax.value', args: [lineNum], default: "Line ${lineNum} contains an invalid tax value")
                }

                fieldName = 'code'
                val = getString(line.code)
                if (val) {
                    if (docline.documentTax == null) return message(code: 'rest.line.no.tax', args: [lineNum], default: "Line ${lineNum} has a tax code but no tax value")
                    if (!isInvoice && !isCash) return message(code: 'rest.has.tax', args: [lineNum], default: "Line ${lineNum} contains a tax code. Tax is not allowed at this point.")
                    docline.taxCode = taxCodes.get(val)
                    if (docline.taxCode) {
                        docline.taxPercentage = taxRates.get(val)
                    } else {
                        docline.taxCode = TaxCode.findByCompanyAndCode(company, val)
                        if (!docline.taxCode) return message(code: 'rest.line.tax', args: [lineNum], default: "Tax code not found in line ${lineNum} of REST request")
                        taxCodes.put(val, docline.taxCode)
                        def temp = TaxRate.findAllByTaxCodeAndValidFromLessThanEquals(docline.taxCode, doc.documentDate, [sort: 'validFrom', order: 'desc', max: 1, cache: true])
                        if (!temp) return message(code: 'document.no.taxRate', default: 'Unable to find a tax rate for this tax code and document date combination')
                        docline.taxPercentage = temp[0].rate
                        taxRates.put(val, docline.taxPercentage)
                        val = new Tax(taxCode: docline.taxCode, taxPercentage: docline.taxPercentage, companyValue: 0.0, companyTax: 0.0,
                                documentValue: 0.0, documentTax: 0.0, generalValue: 0.0, generalTax: 0.0)
                        val.account = taxControl
                        if (isInvoice) {
                            val.accountValue = 0.0
                            val.accountTax = 0.0
                        }

                        doc.addToTaxes(val)
                    }
                } else {
                    if (docline.documentTax) return message(code: 'rest.line.has.tax', args: [lineNum], default: "Line ${lineNum} has a tax value but no tax code")
                    docline.documentTax = null
                }

                // Check that, for invoices and credit notes, the tax code
                // (if any) is consistent with the customer/supplier account
                if (isInvoice) {
                    if ((docline.taxCode && invoiceAccount.taxCode && docline.taxCode.authority.id != invoiceAccount.taxCode.authority.id) ||
                        (docline.taxCode && !invoiceAccount.taxCode && docline.taxCode.authority.usage != 'ad-hoc') ||
                        (!docline.taxCode && invoiceAccount.taxCode && invoiceAccount.taxCode.authority.usage == 'mandatory')) {
                        return message(code: 'rest.bad.tax', args: [lineNum],
                            default: "The tax code for line ${lineNum} is inconsistent with the tax status of the account that the document total is being posted to")
                    }
                }

                // Set all the currency values
                val = convertValue(otherCurrencies, today, documentCurrency, companyCurrency, companyRate, companyCurrency, docline.documentValue)
                if (val instanceof CharSequence) return val
                docline.companyValue = val
                val = convertValue(otherCurrencies, today, documentCurrency, companyCurrency, companyRate, companyCurrency, docline.documentTax)
                if (val instanceof CharSequence) return val
                docline.companyTax = val
                val = convertValue(otherCurrencies, today, documentCurrency, companyCurrency, companyRate, docline.account.currency, docline.documentValue)
                if (val instanceof CharSequence) return val
                docline.generalValue = val
                val = convertValue(otherCurrencies, today, documentCurrency, companyCurrency, companyRate, docline.account.currency, docline.documentTax)
                if (val instanceof CharSequence) return val
                docline.generalTax = val
                accountCurrency = getAccountCurrency(invoiceAccount, docline)
                if (accountCurrency) {
                    val = convertValue(otherCurrencies, today, documentCurrency, companyCurrency, companyRate, accountCurrency, docline.documentValue)
                    if (val instanceof CharSequence) return val
                    docline.accountValue = val
                    val = convertValue(otherCurrencies, today, documentCurrency, companyCurrency, companyRate, accountCurrency, docline.documentTax)
                    if (val instanceof CharSequence) return val
                    docline.accountTax = val
                }

                // Don't forget any line level unallocated amounts
                if (docline.customer || docline.supplier) {
                    docline.companyUnallocated = docline.companyValue
                    docline.accountUnallocated = docline.accountValue
                }

                // Do totalling
                if (docline.documentValue) documentValue += docline.documentValue
                if (docline.documentTax) documentTax += docline.documentTax
                if (docline.companyValue) companyValue += docline.companyValue
                if (docline.companyTax) companyTax += docline.companyTax
                if (docline.accountValue) accountValue += docline.accountValue
                if (docline.accountTax) accountTax += docline.accountTax

                // If we have a total line, we will need the GL values for that totalling also
                if (total) {
                    val = convertValue(otherCurrencies, today, documentCurrency, companyCurrency, companyRate, total.account.currency, docline.documentValue)
                    if (val instanceof CharSequence) return val
                    if (val) generalValue += val
                    val = convertValue(otherCurrencies, today, documentCurrency, companyCurrency, companyRate, total.account.currency, docline.documentTax)
                    if (val instanceof CharSequence) return val
                    if (val) generalTax += val
                }

                // Increment any tax analysis required
                if (docline.taxCode) {
                    for (tax in doc.taxes) {
                        if (tax.taxCode.id == docline.taxCode.id && tax.taxPercentage == docline.taxPercentage) {
                            tax.documentValue += docline.documentTax
                            tax.documentTax += docline.documentValue
                            tax.companyValue += docline.companyTax
                            tax.companyTax += docline.companyValue
                            tax.generalValue += docline.companyTax
                            tax.generalTax += docline.companyValue
                            if (accountCurrency) {
                                tax.accountValue += docline.accountTax
                                tax.accountTax += docline.accountValue
                            }
                            break
                        }
                    }
                }

                // Handle auto & manual allocations
                fieldName = 'auto'
                val = getBoolean(line.auto)
                if (val) {
                    if (docline.customer || docline.supplier) {
                        autoAllocations << (docline.customer ?: docline.supplier)
                    } else {
                        return message(code: 'rest.line.auto', args: [lineNum], default: "Line ${lineNum} specifies an auto allocation but is not being posted to an AR/AP account")
                    }
                }

                if (line.allocations) {
                    if (!docline.customer && !docline.supplier) return message(code: 'rest.line.alloc', args: [lineNum],
                        default: "Line ${lineNum} specifies allocations but is not being posted to an AR/AP account")
                    def targetType, targetDoc, targetLine
                    for (alloc in line.allocations) {
                        fieldName = 'allocations.type'
                        targetType = DocumentType.findByCompanyAndCode(company, getString(alloc.type))
                        if (!targetType) return message(code: 'rest.line.target', args: [lineNum], default: "Invalid document specified in line ${lineNum} allocation")
                        fieldName = 'allocations.code'
                        targetDoc = Document.findByTypeAndCode(targetType, getString(alloc.code))
                        if (!targetDoc) return message(code: 'rest.line.target', args: [lineNum], default: "Invalid document specified in line ${lineNum} allocation")
                        targetLine = docline.customer ?
                                GeneralTransaction.findByDocumentAndCustomer(targetDoc, docline.customer) :
                                GeneralTransaction.findByDocumentAndSupplier(targetDoc, docline.supplier)
                        if (!targetLine?.accountValue) return message(code: 'rest.line.target', args: [lineNum], default: "Invalid document specified in line ${lineNum} allocation")
                        fieldName = 'allocations.value'
                        val = getDecimal(alloc.value)
                        if (!val || val != utilService.round(val, documentCurrency.decimals)) return message(code: 'rest.line.value', args: [lineNum],
                            default: "Invalid value specified in line ${lineNum} allocation")
                        if (accountCurrency.code == documentCurrency.code) {
                            rate = 1.0
                        } else if (accountCurrency.code == companyCurrency.code) {
                            rate = companyRate
                        } else {
                            rate = getRate(otherCurrencies, documentCurrency, accountCurrency, today)
                            if (rate instanceof CharSequence) return rate
                        }

                        manualAllocations << new Allocation(transaction: docline, targetType: targetType, period: doc.period, targetCode: targetDoc.code,
                                accountValue: utilService.round(val * rate, accountCurrency.decimals),
                                companyValue: utilService.round(val * companyRate, companyCurrency.decimals))
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace()
            if (mapName == 'line') return message(code: 'rest.bind.line', args: [lineNum, fieldName], default: "REST map line ${lineNum}: field ${fieldName} contains invalid data")
            return message(code: 'rest.bind.map', args: [mapName, fieldName], default: "REST map ${mapName}: field ${fieldName} contains invalid data")
        }

        // Cross check (and possibly set) totals
        if (total) {
            if (documentValue != doc.sourceGoods) return message(code: 'document.goods.mismatch', default: 'The document goods value does not agree to the sum of the goods values of the lines')
            if (documentTax != doc.sourceTax) return message(code: 'document.tax.mismatch', default: 'The document tax value does not agree to the sum of the tax values of the lines')
            total.documentValue = documentValue + documentTax
            total.companyValue = companyValue + companyTax
            total.generalValue = generalValue + generalTax
            if (isInvoice) {
                total.accountValue = accountValue + accountTax
                total.companyUnallocated = total.companyValue
                total.accountUnallocated = total.accountValue
            }

            if ((isInvoice || isCash) && doc.taxes) {
                total.documentTax = documentTax
                total.companyTax = companyTax
                total.generalTax = generalTax
                total.accountTax = accountTax
            }
        } else {
            if (documentValue + documentTax != 0.0) return message(code: 'rest.bad.balance', default: 'The debit and credit values do not balance')
            if (companyValue + companyTax != 0.0) {
                try {
                    postingService.balanceDocument(doc, companyValue + companyTax)
                } catch (IllegalArgumentException iae) {
                    return iae.getMessage() ?: message(code: 'rest.bad.victim', default: 'Unable to balance the company currency values')
                }
            }
        }

        // Set the document code number
        sessionFactory.currentSession.evict(doctype)
        DocumentType.withTransaction {status ->
            doctype = DocumentType.lock(doctype.id)
            doc.code = doctype.nextSequenceNumber.toString()
            doctype.nextSequenceNumber += 1
            if (!doctype.saveThis()) {
                status.setRollbackOnly()
                doc.code = null
            }
        }

        // Ensure the document code number was set ok
        if (!doc.code) return message(code: 'document.bad.next', default: 'Unable to update the next document sequence number')
        doc.type = doctype

        // If we are doing a provision, we need to create the reversal
        if (isProvision) {

            // Create the basic reversal document
            reversal = new Document(currency: doc.currency, code: doc.code, description: doc.description, documentDate: doc.documentDate, dueDate: doc.dueDate, reference: doc.reference)
            reversal.lines = new ListOrderedSet()

            // Set the appropriate reversal document type
            val = doctype.code + 'R'
            reversal.type = DocumentType.findByCompanyAndCode(company, val)
            if (reversal.type?.type?.code != (systype == 'AC' ? 'ACR' : 'PRR')) return message(code: 'document.no.reverse', args: [val], default: "No valid ${val} reversal document type found")

            // Set the appropriate reversal period
            for (int i = 0; i < activePeriods.size(); i++) {
                if (activePeriods[i].id == doc.period.id) {
                    if (i == activePeriods.size() - 1) {
                        return message(code: 'document.next.period', args: [doc.period.code], default: "No active period found after ${doc.period.code} to which the reversal could be posted")
                    } else {
                        reversal.period = activePeriods[i + 1]
                    }

                    break
                }
            }

            // Add in the reversal lines
            for (line in doc.lines) {
                val = new Line(description: line.description, documentValue: line.documentValue,
                        generalValue: line.generalValue, companyValue: line.companyValue, adjustment: line.adjustment)
                val.account = line.account
                reversal.addToLines(val)
            }

            // Add in the total line
            val = new Total(description: total.description, documentValue: total.documentValue,
                    generalValue: total.generalValue, companyValue: total.companyValue, adjustment: total.adjustment)
            val.account = total.account
            reversal.addToTotal(val)
        }

        // Pass back the document and all the allocation info as a map
        return [document: doc, manual: manualAllocations, auto: autoAllocations, reversal: reversal]
    }

    // Dump a document and its allocations to stdout. The map parameter
    // is such as is returned from the getDocument method of this service
    def dump(map) {
        def doc = map.document
        if (doc) {
            def manual = map.manual ?: []
            def auto = map.auto ?: []

            println ""
            println "Header"
            println "    Type: ${doc.type?.code}"
            println "    Code: ${doc.code}"
            println "    Date: ${doc.documentDate?.format('yyyy-MM-dd')}"
            println "    Due: ${doc.dueDate?.format('yyyy-MM-dd')}"
            println "    Currency: ${doc.currency?.code}"
            println "    Period: ${doc.period?.code}"
            println "    Reference: ${doc.reference}"
            println "    Description: ${doc.description}"
            println ""
            println "Lines"
            def pos = 0
            def account, isAuto
            for (line in doc.lines) {
                account = line.customer ?: line.supplier
                if (account) {
                    isAuto = auto.any {it.code == account.code}
                } else {
                    isAuto = null
                }

                dumpLine(line, isAuto, manual.findAll {line.is(it.transaction)})
                pos++
            }

            println "Taxes"
            if (doc.taxes) {
                pos = 0
                for (line in doc.taxes) {
                    dumpTax(line)
                    pos++
                }
            } else {
                println "    none"
                println ""
            }

            println "Total"
            if (doc.total) {
                for (line in doc.total) {
                    account = line.customer ?: line.supplier
                    if (account) {
                        isAuto = auto.any {it.code == account.code}
                    } else {
                        isAuto = null
                    }

                    dumpLine(line, isAuto, manual.findAll {line.is(it.transaction)})
                }
            } else {
                println "    none"
                println ""
            }
        }
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private getRandomString(length) {
        def rnd = new SecureRandom()
        def sb = new StringBuilder()
        for (int i = 0; i < length; i++) {
            sb.append(REST_CHARS[rnd.next(6)])
        }

        return sb.toString()
    }

    private setAccount(map, document, line, lineNumber) {
        def account
        switch (getString(map.ledger)) {
            case 'gl':
                account = Account.findBySecurityCodeAndCode(utilService.currentCompany().securityCode, getString(map.account))
                if (!bookService.hasAccountAccess(account) || !account.active || (lineNumber && !postingService.canPostDocumentToAccount(document, line, lineNumber, account))) return false
                line.account = account
                break

            case 'ar':
                account = Customer.findByCompanyAndCode(utilService.currentCompany(), getString(map.account))
                if (!bookService.hasCustomerAccess(account) || !account.active) return false
                line.customer = account
                account = bookService.getControlAccount(utilService.currentCompany(), 'ar')
                if (!account) return false
                line.account = account
                break

            case 'ap':
                account = Supplier.findByCompanyAndCode(utilService.currentCompany(), getString(map.account))
                if (!bookService.hasSupplierAccess(account) || !account.active) return false
                line.supplier = account
                account = bookService.getControlAccount(utilService.currentCompany(), 'ap')
                if (!account) return false
                line.account = account
                break

            default:
                return false
        }

        return true
    }

    private getRate(rates, from, to, today) {
        def rate = rates.get(to.code)
        if (!rate) {
            rate = utilService.getExchangeRate(from, to, today)
            if (!rate) return message(code: 'document.bad.exchangeRate', args: [from.code, to.code],
                default: "No exchange rate available from ${from.code} to ${to.code}")
            rates.put(to.code, rate)
        }

        return rate
    }

    private convertValue(rates, today, documentCurrency, companyCurrency, companyRate, requiredCurrency, documentValue) {
        if (!documentValue || requiredCurrency.code == documentCurrency.code) return utilService.round(documentValue, requiredCurrency.decimals)
        if (requiredCurrency.code == companyCurrency.code) return utilService.round(documentValue * companyRate, requiredCurrency.decimals)
        def rate = getRate(rates, documentCurrency, requiredCurrency, today)
        if (rate instanceof CharSequence) return rate
        return utilService.round(documentValue * rate, requiredCurrency.decimals)
    }

    private getAccountCurrency(invoiceAccount, line) {
        if (invoiceAccount) return invoiceAccount.currency
        if (line.customer) return line.customer.currency
        if (line.supplier) return line.supplier.currency
        return null
    }

    private dumpLine(line, autoAllocate, allocations) {
        println "    GL Account: ${line.account?.code}"
        if (line.customer) println "    AR Account: ${line.customer.code}"
        if (line.supplier) println "    AP Account: ${line.supplier.code}"
        println "    Description: ${line.description}"
        println "    Posting Value: ${line.documentValue?.toPlainString()} (Co: ${line.companyValue?.toPlainString()}, GL: ${line.generalValue?.toPlainString()}, A/C: ${line.accountValue?.toPlainString()})"
        println "    Tax Value: ${line.documentTax?.toPlainString()} (Co: ${line.companyTax?.toPlainString()}, GL: ${line.generalTax?.toPlainString()}, A/C: ${line.accountTax?.toPlainString()})"
        println "    Tax Code: ${line.taxCode?.code}"
        println "    Tax Percentage: ${line.taxPercentage?.toPlainString()}"
        println "    Unallocated: (Co: ${line.companyUnallocated?.toPlainString()}, A/C: ${line.accountUnallocated?.toPlainString()})"
        println "    Hold: ${line.onHold}"
        println "    Adjustment: ${line.adjustment}"
        println "    Turnover: ${line.affectsTurnover}"
        println "    Auto Allocate: ${autoAllocate}"
        if (allocations) {
            println "    Allocations:"
            for (alloc in allocations) {
                println "        ${alloc.targetType?.code} ${alloc.targetCode} = (Co: ${alloc.companyValue?.toPlainString()}, A/C: ${alloc.accountValue?.toPlainString()})"
            }
        }

        println ""
    }

    private dumpTax(line) {
        println "    GL Account: ${line.account?.code}"
        println "    Description: ${line.description}"
        println "    Posting Value: ${line.documentValue?.toPlainString()} (Co: ${line.companyValue?.toPlainString()}, GL: ${line.generalValue?.toPlainString()}, A/C: ${line.accountValue?.toPlainString()})"
        println "    Tax Value: ${line.documentTax?.toPlainString()} (Co: ${line.companyTax?.toPlainString()}, GL: ${line.generalTax?.toPlainString()}, A/C: ${line.accountTax?.toPlainString()})"
        println "    Tax Code: ${line.taxCode?.code}"
        println "    Tax Percentage: ${line.taxPercentage?.toPlainString()}"
        println "    Unallocated: (Co: ${line.companyUnallocated?.toPlainString()}, A/C: ${line.accountUnallocated?.toPlainString()})"
        println "    Hold: ${line.onHold}"
        println "    Adjustment: ${line.adjustment}"
        println "    Turnover: ${line.affectsTurnover}"
        println ""
    }
}
