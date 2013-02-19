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

import org.grails.tlc.sys.SystemUser
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class Mnemonic {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [user: SystemUser]

    String code
    String name
    String accountCodeFragment
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            user lazy: true
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, matches: '[^\\-+|/:. ]+', unique: 'user')
        name(blank: false, size: 1..30)
        accountCodeFragment(blank: false, size: 1..87, matches: '[0-9A-Za-z\\' + BookService.SEGMENT_DELIMITER + ']+', validator: {val, obj ->
            if (val) {
                if (val.contains(BookService.SEGMENT_DELIMITER + BookService.SEGMENT_DELIMITER)) return 'double'
                if (val.endsWith(BookService.SEGMENT_DELIMITER)) return 'ending'
                obj.accountCodeFragment = BookService.fixCase(val)
            }

            return true
        })
        securityCode(validator: {val, obj ->
            return (val == 0)
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
        return code
    }
}
