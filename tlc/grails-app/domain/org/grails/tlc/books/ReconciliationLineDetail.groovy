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

class ReconciliationLineDetail {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [line: ReconciliationLine]
    static transients = ['ledgerName']

    String type                     // Specifies whther the account is a GL account, customer account or supplier account
    Long ledgerId                   // The id of the GL, AR or AP ledger account
    String ledgerCode               // The code of the ledger GL, customer or supplier account
    String ledgerName               // Transient used for diplay of the ledger account, if available, else the ledger code above
    String description              // Copy of the description from the GL transaction
    BigDecimal bankAccountValue     // Receipt positive, Payment negative
    Boolean reconciled = false
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            line lazy: true
        }
    }

    static constraints = {
        type(inList: ['ap', 'ar', 'gl'])
        ledgerCode(size: 1..87)
        description(nullable: true, size: 1..50)
        bankAccountValue(scale: 3)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.line.reconciliation.bankAccount.securityCode
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
