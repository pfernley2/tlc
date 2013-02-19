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

class Reconciliation {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [bankAccount: Account]
    static hasMany = [lines: ReconciliationLine]

    Date statementDate
    BigDecimal statementBalance
    BigDecimal bankAccountBalance
    Date finalizedDate
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            account lazy: true
            lines cascade: 'all'
        }
    }

    static constraints = {
        statementDate(unique: 'bankAccount', validator: {val, obj ->
            if (val) {
                if (val != UtilService.fixDate(val)) return false
                if (!obj.id) {
                    def today = UtilService.fixDate()
                    if (val < today - 365 || val > today) return false
                }
            }

            return true
        })
        statementBalance(scale: 3)
        bankAccountBalance(scale: 3)
        finalizedDate(nullable: true, validator: {val, obj ->
            if (val && val != UtilService.fixDate(val)) return false

            return true
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.bankAccount.securityCode
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
        return "${id}"
    }
}
