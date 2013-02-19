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
package org.grails.tlc.corp

import org.grails.tlc.books.Document
import org.grails.tlc.sys.UtilService
import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicLong

class TaxStatement {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [authority: TaxAuthority, document: Document]
    static hasMany = [lines: TaxStatementLine]

    Date statementDate
    Date priorDate
    String description
    Boolean finalized = false
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            authority lazy: true
            document lazy: true
            lines cascade: 'all'
        }
    }

    static constraints = {
        lines(minsize: 1)
        document(nullable: true)
        statementDate(unique: 'authority', validator: {val, obj ->
            if (val) {
                def today = UtilService.fixDate()
                if (val != UtilService.fixDate(val) || val < today - 365 || val > today) return 'bad'
            }

            return true
        })
        priorDate(validator: {val, obj ->
            if (val && obj.statementDate) return val < obj.statementDate
            return true
        })
        description(blank: false, size: 1..50)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.authority.securityCode
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
        return new SimpleDateFormat('yyyy-MM-dd', Locale.US).format(statementDate)
    }
}
