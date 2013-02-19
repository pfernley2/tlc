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

class ReconciliationLine {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [reconciliation: Reconciliation]
    static hasMany = [details: ReconciliationLineDetail]
    static transients = ['detailDescription']

    Long documentId                     // The id of the document of which this is a line
    Date documentDate                   // Copy of info from the GL line document
    String documentCode                 // Copy of info from the GL line document
    String documentReference            // Copy of info from the GL line document
    String documentDescription          // Copy of info from the GL line document
    BigDecimal bankAccountValue         // Receipt positive, Payment negative
    BigDecimal reconciledValue = 0.0    // Equal to bankAccountValue when fully reconciled
    Boolean broughtForward = false      // True if an unreconciled item b/f from previous reconciliation
    Boolean part = false                // True if this is a partially reconciled item brought forward
    Integer detailCount = 0             // Number of detail records associated with this line
    Boolean added = false               // If this document was added during the reconciliation process
    String detailDescription            // Transient used for displaying the details of a single detailed line
    Long sequencer                      // GL transaction line id
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            reconciliation lazy: true
            details cascade: 'all'
        }
    }

    static constraints = {
        documentCode(blank: false, size: 1..20)
        documentReference(nullable: true, size: 1..30)
        documentDescription(nullable: true, size: 1..50)
        bankAccountValue(scale: 3)
        reconciledValue(scale: 3)
        part(validator: {val, obj ->
            return !(val && !obj.broughtForward)
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.reconciliation.bankAccount.securityCode
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
