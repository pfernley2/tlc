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

import org.grails.tlc.corp.Company
import org.grails.tlc.corp.CompanyUser
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class AccessGroup {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = CompanyUser
    static hasMany = [users: CompanyUser]

    Company company
    String code
    String name
    String element1
    String element2
    String element3
    String element4
    String element5
    String element6
    String element7
    String element8
    String customers
    String suppliers
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            companyUser lazy: true
            users cascade: 'save-update'
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, unique: 'company')
        name(blank: false, size: 1..30)
        element1(nullable: true, size: 1..250, validator: {val, obj ->
            def element
            CodeElement.withNewSession {
                element = CodeElement.findByCompanyAndElementNumber(obj.company, 1)
            }

            if (element) {
                if (val) {
                    if (element.dataType == 'alphabetic') {
                        val = BookService.fixCase(val)
                        obj.element1 = val
                    }

                    if (!verifyAccessData(element, val)) return ['accessGroup.element.bad.data', element.name]
                }
            } else {
                if (val) obj.element1 = null    // Can't have a value if no element
            }

            return true
        })
        element2(nullable: true, size: 1..250, validator: {val, obj ->
            def element
            CodeElement.withNewSession {
                element = CodeElement.findByCompanyAndElementNumber(obj.company, 2)
            }

            if (element) {
                if (val) {
                    if (element.dataType == 'alphabetic') {
                        val = BookService.fixCase(val)
                        obj.element2 = val
                    }

                    if (!verifyAccessData(element, val)) return ['accessGroup.element.bad.data', element.name]
                }
            } else {
                if (val) obj.element2 = null    // Can't have a value if no element
            }

            return true
        })
        element3(nullable: true, size: 1..250, validator: {val, obj ->
            def element
            CodeElement.withNewSession {
                element = CodeElement.findByCompanyAndElementNumber(obj.company, 3)
            }

            if (element) {
                if (val) {
                    if (element.dataType == 'alphabetic') {
                        val = BookService.fixCase(val)
                        obj.element3 = val
                    }

                    if (!verifyAccessData(element, val)) return ['accessGroup.element.bad.data', element.name]
                }
            } else {
                if (val) obj.element3 = null    // Can't have a value if no element
            }

            return true
        })
        element4(nullable: true, size: 1..250, validator: {val, obj ->
            def element
            CodeElement.withNewSession {
                element = CodeElement.findByCompanyAndElementNumber(obj.company, 4)
            }

            if (element) {
                if (val) {
                    if (element.dataType == 'alphabetic') {
                        val = BookService.fixCase(val)
                        obj.element4 = val
                    }

                    if (!verifyAccessData(element, val)) return ['accessGroup.element.bad.data', element.name]
                }
            } else {
                if (val) obj.element4 = null    // Can't have a value if no element
            }

            return true
        })
        element5(nullable: true, size: 1..250, validator: {val, obj ->
            def element
            CodeElement.withNewSession {
                element = CodeElement.findByCompanyAndElementNumber(obj.company, 5)
            }

            if (element) {
                if (val) {
                    if (element.dataType == 'alphabetic') {
                        val = BookService.fixCase(val)
                        obj.element5 = val
                    }

                    if (!verifyAccessData(element, val)) return ['accessGroup.element.bad.data', element.name]
                }
            } else {
                if (val) obj.element5 = null    // Can't have a value if no element
            }

            return true
        })
        element6(nullable: true, size: 1..250, validator: {val, obj ->
            def element
            CodeElement.withNewSession {
                element = CodeElement.findByCompanyAndElementNumber(obj.company, 6)
            }

            if (element) {
                if (val) {
                    if (element.dataType == 'alphabetic') {
                        val = BookService.fixCase(val)
                        obj.element6 = val
                    }

                    if (!verifyAccessData(element, val)) return ['accessGroup.element.bad.data', element.name]
                }
            } else {
                if (val) obj.element6 = null    // Can't have a value if no element
            }

            return true
        })
        element7(nullable: true, size: 1..250, validator: {val, obj ->
            def element
            CodeElement.withNewSession {
                element = CodeElement.findByCompanyAndElementNumber(obj.company, 7)
            }

            if (element) {
                if (val) {
                    if (element.dataType == 'alphabetic') {
                        val = BookService.fixCase(val)
                        obj.element7 = val
                    }

                    if (!verifyAccessData(element, val)) return ['accessGroup.element.bad.data', element.name]
                }
            } else {
                if (val) obj.element7 = null    // Can't have a value if no element
            }

            return true
        })
        element8(nullable: true, size: 1..250, validator: {val, obj ->
            def element
            CodeElement.withNewSession {
                element = CodeElement.findByCompanyAndElementNumber(obj.company, 8)
            }

            if (element) {
                if (val) {
                    if (element.dataType == 'alphabetic') {
                        val = BookService.fixCase(val)
                        obj.element8 = val
                    }

                    if (!verifyAccessData(element, val)) return ['accessGroup.element.bad.data', element.name]
                }
            } else {
                if (val) obj.element8 = null    // Can't have a value if no element
            }

            return true
        })
        customers(nullable: true, size: 1..250, validator: {val, obj ->
            if (val && !verifySubAccessData(val)) return 'bad.data'
            return true
        })
        suppliers(nullable: true, size: 1..250, validator: {val, obj ->
            if (val && !verifySubAccessData(val)) return 'bad.data'
            return true
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.company.securityCode
            return true
        })
    }

    private static verifyAccessData(element, data) {
        def lst = data.replace("\n", "").split(',')*.trim()
        if (lst.size() == 1 && lst[0] == '*') return true
        for (item in lst) {
            if (!item || item.startsWith('-') || item.endsWith('-')) return false
            def pos = item.indexOf('-')
            if (pos > 0) {
                def from = item.substring(0, pos).trim()
                def to = item.substring(pos + 1).trim()
                if (from == to || !isValid(element, from) || !isValid(element, to) || (from != '*' && to != '*' && from > to)) return false
            } else {
                if (item == '*' || !isValid(element, item)) return false
            }
        }

        return true
    }

    private static verifySubAccessData(data) {
        def lst = data.replace("\n", "").split(',')*.trim()
        if (lst.size() == 1 && lst[0] == '*') return true
        for (item in lst) {
            if (!item || item.startsWith('-') || item.endsWith('-')) return false
            def pos = item.indexOf('-')
            if (pos > 0) {
                def from = item.substring(0, pos).trim()
                def to = item.substring(pos + 1).trim()
                if (from == to || !isSubValid(from) || !isSubValid(to) || (from != '*' && to != '*' && from > to)) return false
            } else {
                if (item == '*' || !isSubValid(item)) return false
            }
        }

        return true
    }

    private static isValid(element, value) {
        if (value != '*') {
            if (element.dataLength != value.length()) return false
            if (element.dataType == 'alphabetic') {
                if (!BookService.isAlphabetic(value)) return false
            } else {
                if (!BookService.isNumeric(value)) return false
            }
        }

        return true
    }

    private static isSubValid(value) {
        if (value != '*') {
            if (value.length() > 10 || value.indexOf(' ') != -1 || value.indexOf('-') != -1) return false
        }

        return true
    }

    def afterInsert() {
        UtilService.trace('insert', this)
    }

    def afterUpdate() {
        UtilService.trace('update', this)
    }

    def afterDelete() {
        UtilService.trace('delete', this)
    }

    public String toString() {
        return code
    }
}
