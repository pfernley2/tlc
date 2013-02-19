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

class BookTagLib {

    def utilService
    def bookService

    // Output a monetary value from a financial record if greater than or equal to zero, or nothing if negative
    def debit = {attrs, body ->
        def value = attrs.field ? bookService.getBookValue(attrs) : getSimpleValue(attrs)
        if (value != null) {
            if (value instanceof String) {
                out << value
            } else if (value >= 0.0) {
                if (attrs.grouped != null && attrs.grouped instanceof String) attrs.grouped = attrs.grouped.equalsIgnoreCase('true')
                out << utilService.format(value, attrs.scale, attrs.grouped).encodeAsHTML()
            }
        }
    }

    // Output a monetary value from a financial record if less than zero, or nothing if >= zero
    def credit = {attrs, body ->
        def value = attrs.field ? bookService.getBookValue(attrs) : getSimpleValue(attrs)
        if (value != null) {
            if (value instanceof String) {
                out << value
            } else if (value < 0.0) {
                if (attrs.grouped != null && attrs.grouped instanceof String) attrs.grouped = attrs.grouped.equalsIgnoreCase('true')
                out << utilService.format(-value, attrs.scale, attrs.grouped).encodeAsHTML()
            }
        }
    }

    // Output a monetary value from a financial record but displaying a negative value as positive and a positive value with a trailing 'Dr' marker
    def dr = {attrs, body ->
        def value = attrs.field ? bookService.getBookValue(attrs) : getSimpleValue(attrs)
        if (value != null) {
            if (attrs.grouped != null && attrs.grouped instanceof String) attrs.grouped = attrs.grouped.equalsIgnoreCase('true')
            if (value instanceof String) {
                out << value
            } else if (value > 0.0) {
                out << utilService.format(value, attrs.scale, attrs.grouped).encodeAsHTML()
                out << '&nbsp;'
                out << g.msg(code: 'generic.dr', default: 'Dr')
            } else {
                out << utilService.format(-value, attrs.scale, attrs.grouped).encodeAsHTML()
            }
        }
    }

    // Output a monetary value from a financial record but displaying a negative value as positive with a trailing 'Cr' marker
    def cr = {attrs, body ->
        def value = attrs.field ? bookService.getBookValue(attrs) : getSimpleValue(attrs)
        if (value != null) {
            if (attrs.grouped != null && attrs.grouped instanceof String) attrs.grouped = attrs.grouped.equalsIgnoreCase('true')
            if (value instanceof String) {
                out << value
            } else if (value < 0.0) {
                out << utilService.format(-value, attrs.scale, attrs.grouped).encodeAsHTML()
                out << '&nbsp;'
                out << g.msg(code: 'generic.cr', default: 'Cr')
            } else {
                out << utilService.format(value, attrs.scale, attrs.grouped).encodeAsHTML()
            }
        }
    }

    // Output a monetary value from a financial record displaying a trailing Dr/Cr indicator rather than +/-
    def drcr = {attrs, body ->
        def value = attrs.field ? bookService.getBookValue(attrs) : getSimpleValue(attrs)
        if (value != null) {
            if (attrs.grouped != null && attrs.grouped instanceof String) attrs.grouped = attrs.grouped.equalsIgnoreCase('true')
            if (attrs.zeroIsUnmarked != null && attrs.zeroIsUnmarked instanceof String) attrs.zeroIsUnmarked = attrs.zeroIsUnmarked.equalsIgnoreCase('true')
            if (value instanceof String) {
                out << value
            } else if (value < 0.0) {
                out << utilService.format(-value, attrs.scale, attrs.grouped).encodeAsHTML()
                out << '&nbsp;'
                out << g.msg(code: 'generic.cr', default: 'Cr')
            } else {
                out << utilService.format(value, attrs.scale, attrs.grouped).encodeAsHTML()
                if (value != 0.0 || !attrs.zeroIsUnmarked) {
                    out << '&nbsp;'
                    out << g.msg(code: 'generic.dr', default: 'Dr')
                }
            }
        }
    }

    // Output a monetary value from a financial record
    def amount = {attrs, body ->
        def value = attrs.field ? bookService.getBookValue(attrs) : getSimpleValue(attrs)
        if (value != null) {
            if (value instanceof String) {
                out << value
            } else {
                if (attrs.grouped != null && attrs.grouped instanceof String) attrs.grouped = attrs.grouped.equalsIgnoreCase('true')
                out << utilService.format(value, attrs.scale, attrs.grouped).encodeAsHTML()
            }
        }
    }

    // For an enquiry screen, output a link to an account (gl, ar or ap), document or allocation history if the user has permission, else no link
    def enquiryLink = {attrs, body ->
        def controller
        def action = 'enquire'
        def parameters = [:]
        def target = attrs.remove('target')
        def id
        if (target) {
            id = target.id
            parameters.displayPeriod = attrs.displayPeriod?.id
            parameters.displayCurrency = attrs.displayCurrency?.id
            if (target instanceof Customer) {                                           // Link to a customer account
                if (bookService.hasCustomerAccess(target)) controller = 'customer'
            } else if (target instanceof Supplier) {                                    // Link to a supplier account
                if (bookService.hasSupplierAccess(target)) controller = 'supplier'
            } else if (target instanceof Account) {                                     // Link to a general ledger account
                if (bookService.hasAccountAccess(target)) controller = 'account'
            } else if (target instanceof Document) {                                    // Link to a document
                def sysType = target.type.type
                if (utilService.permitted(sysType.activity.code)) controller = getDocumentController(sysType.code)
            } else if (target instanceof GeneralTransaction) {                          // Link from a document line to its allocations
                action = 'allocations'
                if (target.customer) {
                    controller = 'customer'
                } else if (target.supplier) {
                    controller = 'supplier'
                }
            } else if (target instanceof Allocation) {                                  // Link from an allocation to a document
                def sysType = target.targetType.type
                if (utilService.permitted(sysType.activity.code)) {
                    def doc = Document.findByTypeAndCode(target.targetType, target.targetCode)
                    if (doc) {
                        controller = getDocumentController(sysType.code)
                        id = doc.id
                    }
                }
            }
        }

        if (controller) {
            out << g.link(controller: controller, action: action, id: id, params: parameters) { body() }
        } else {
            out << body()
        }
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private getDocumentController(code) {
        switch (code) {
            case ['PI', 'PC']:
                return 'purchase'

            case ['SI', 'SC']:
                return 'sales'

            case 'FXD':
                return 'difference'

            case 'FXR':
                return 'revaluation'

            case 'GLJ':
                return 'general'

            case 'FJ':
                return 'financial'

            case 'APJ':
                return 'payable'

            case 'ARJ':
                return 'receivable'

            case 'SOJ':
                return 'setoff'

            case ['AC', 'PR', 'ACR', 'PRR']:
                return 'provision'

            case ['CP', 'CR']:
                return 'cash'

            case ['BP', 'BR']:
                return 'bank'

            default:
                return null
        }
    }

    private getSimpleValue(attrs) {
        def value
        if (attrs.value != null && attrs.scale != null) {
            if (attrs.scale instanceof String) attrs.scale = attrs.scale.toInteger()
            value = utilService.round(attrs.value, attrs.scale)
            if (attrs.value) {
                def negate = attrs.negate
                if (negate != null && negate instanceof String) negate = negate.equalsIgnoreCase('true')
                if (negate) value = -value
            } else {
                def zeroIsNull = attrs.zeroIsNull
                if (zeroIsNull != null && zeroIsNull instanceof String) zeroIsNull = zeroIsNull.equalsIgnoreCase('true')
                if (zeroIsNull) {
                    value = null
                } else if (attrs.zeroIsText != null) {
                    value = attrs.zeroIsText.encodeAsHTML()
                } else if (attrs.zeroIsHTML != null) {
                    value = attrs.zeroIsHTML
                }
            }
        }

        return value
    }
}
