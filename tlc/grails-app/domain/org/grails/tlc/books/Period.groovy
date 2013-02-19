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

class Period {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [year: Year]
    static hasMany = [balances: GeneralBalance, documents: Document, allocations: Allocation,
            customerTurnovers: CustomerTurnover, supplierTurnovers: SupplierTurnover, revaluations: PeriodRevaluation]

    String code
    Date validFrom
    Date validTo
    String status = 'new'
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        table 'accounting_period'
        cache true
        columns {
            year lazy: true
            balances cascade: 'all'
            documents cascade: 'all'
            allocations cascade: 'all'
            customerTurnovers cascade: 'all'
            supplierTurnovers cascade: 'all'
            revaluations cascade: 'all'
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, unique: 'year')
        validFrom(validator: {val, obj ->
            if (val) {
                if (val != UtilService.fixDate(val)) return 'bad'
                if (val < obj?.year?.validFrom) return 'low'
                if (obj.validTo && val > obj.validTo) return 'mismatch'
            }

            return true
        })
        validTo(validator: {val, obj ->
            if (val) {
                if (val != UtilService.fixDate(val)) return 'bad'
                if (val > obj?.year?.validTo) return 'high'
            }

            return true
        })
        status(inList: ['new', 'open', 'adjust', 'closed'])
        securityCode(unique: ['validFrom', 'validTo'], validator: {val, obj ->
            obj.securityCode = obj.year.securityCode
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
        return code
    }
}
