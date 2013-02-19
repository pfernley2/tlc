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

class Allocation {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [transaction: GeneralTransaction, targetType: DocumentType, period: Period]
    static transients = ['accountDifference', 'accountZeroAllowed']

    BigDecimal accountDifference    // Transient for entry of manual FX difference write-off
    Boolean accountZeroAllowed      // Transient used to allow an accountValue of zero (e.g. for auto FX difference write-off)
    String targetCode               // The target document code
    Long targetId                   // The target GL transaction line id
    BigDecimal accountValue         // Account currency value being allocated
    BigDecimal companyValue         // Company currency value being allocated
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            transaction lazy: true
            targetType lazy: true
            period lazy: true
        }
    }

    static constraints = {
        targetCode(blank: false, size: 1..10, validator: {val, obj ->
            return (obj.transaction.document.code != val || obj.transaction.document.type.id != obj.targetType.id)
        })
        targetId(min: 1L)
        companyValue(scale: 3)
        accountValue(scale: 3, validator: {val, obj ->
            return (val == 0.0 && !obj.accountZeroAllowed) ? 'zero' : true
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.transaction.securityCode
            return true
        })
    }

    def afterInsert() {
        return UtilService.trace('insert', this)
    }

    def afterUpdate() {
        return UtilService.trace('update', this)
    }

    def afterDelete() {
        return UtilService.trace('delete', this)
    }

    public String toString() {
        return "${id}"
    }
}
