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

import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class CodeElementValue {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [element: CodeElement]
    static hasMany = [accounts1: Account, accounts2: Account, accounts3: Account, accounts4: Account,
            accounts5: Account, accounts6: Account, accounts7: Account, accounts8: Account]
    static mappedBy = [accounts1: 'element1', accounts2: 'element2', accounts3: 'element3', accounts4: 'element4',
            accounts5: 'element5', accounts6: 'element6', accounts7: 'element7', accounts8: 'element8']

    String code
    String shortName
    String name
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            element lazy: true
            accounts1 cascade: 'save-update'
            accounts2 cascade: 'save-update'
            accounts3 cascade: 'save-update'
            accounts4 cascade: 'save-update'
            accounts5 cascade: 'save-update'
            accounts6 cascade: 'save-update'
            accounts7 cascade: 'save-update'
            accounts8 cascade: 'save-update'
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, unique: 'element', validator: {val, obj ->
            if (val) {
                if (obj.element.dataLength != val.length()) return 'bad'
                if (obj.element.dataType == 'alphabetic') {
                    if (!BookService.isAlphabetic(val)) return 'bad'
                } else {
                    if (!BookService.isNumeric(val)) return 'bad'
                }
            }

            return true
        })
        shortName(blank: false, size: 1..10)
        name(blank: false, size: 1..30)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.element.securityCode
            return true
        })
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
        return "${code}"
    }
}
