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

class Remittance {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [supplier: Supplier]
    static hasMany = [lines: RemittanceLine]
    static transients = ['accountValue', 'sourceLine', 'sourceDocument']

    BigDecimal accountValue             // Transient used for display. Note that this is the negative of the line values (which use negative for credit items)
    GeneralTransaction sourceLine       // The General transaction line created from this remittance advice when it is posted
    Document sourceDocument             // Transient of the document used to pay this remittance advice used for an enquiry link
    Date adviceDate
    Date authorizedDate
    Date paymentDate
    Long paymentDocumentId
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            supplier lazy: true
            lines cascade: 'all'
        }
    }

    static constraints = {
        adviceDate(unique: 'supplier', validator: {val, obj ->
            return val == UtilService.fixDate(val)
        })
        authorizedDate(nullable: true, validator: {val, obj ->
            if (val && (val != UtilService.fixDate(val) || val < obj.adviceDate)) return false

            return true
        })
        paymentDate(nullable: true, validator: {val, obj ->
            if (val && (val != UtilService.fixDate(val) || !obj.authorizedDate || val < obj.authorizedDate)) return false

            return true
        })
        paymentDocumentId(nullable: true)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.supplier.securityCode
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
