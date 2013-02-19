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

import org.grails.tlc.sys.UtilService
import java.text.SimpleDateFormat
import java.util.concurrent.atomic.AtomicLong

class ExchangeRate {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [currency: ExchangeCurrency]

    Date validFrom
    BigDecimal rate
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            currency lazy: true
        }
    }

    static constraints = {
        validFrom(unique: 'currency', range: UtilService.validDateRange(), validator: {val, obj ->
            return (val == UtilService.fixDate(val))
        })
        rate(scale: 6, min: 0.000001)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.currency.securityCode
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
        return new SimpleDateFormat('yyyy-MM-dd', Locale.US).format(validFrom)
    }
}
