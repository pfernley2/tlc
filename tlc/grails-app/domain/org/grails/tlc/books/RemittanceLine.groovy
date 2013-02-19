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

class RemittanceLine {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [remittance: Remittance]
    static transients = ['payment', 'targetType', 'source']

    BigDecimal payment                  // Transient used for data entry. Credit values are held as negatives but are displayed to the user as positives
    DocumentType targetType             // Transient for creating new allocation
    GeneralTransaction source           // Transient for enquiry link to underying source document
    String type
    String code
    Date documentDate
    Date dueDate
    String reference
    BigDecimal originalValue            // Dr is positive cr is negative
    BigDecimal accountUnallocated       // Dr is positive cr is negative
    Long sequencer                      // The GeneralTransaction id of the line being allocated to
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            remittance lazy: true
        }
    }

    static constraints = {
        type(blank: false, size: 1..10, matches: '[^:\\= \\.]+')
        code(blank: false, size: 1..10, unique: ['remittance', 'type'])
        documentDate(validator: {val, obj ->
            return val == UtilService.fixDate(val)
        })
        dueDate(nullable: true, validator: {val, obj ->
            return val == UtilService.fixDate(val)
        })
        reference(nullable: true, size: 1..30)
        originalValue(scale: 3)
        accountUnallocated(scale: 3)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.remittance.securityCode
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
