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

class RecurringLine {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [recurrence: Recurring, account: Account, customer: Customer, supplier: Supplier]
    static transients = ['accountCode', 'accountName', 'accountType', 'used']

    String accountCode                      // Used as transient GL account code on data input/display
    String accountName                      // Used as transient GL account name on data input/display
    String accountType                      // Used as transient selector between GL, AR and AP ledgers
    Boolean used = false                    // Used as a transient for determining whether to allow edit of the ledger & account details
    String description
    BigDecimal initialValue
    BigDecimal recurringValue
    BigDecimal finalValue
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            recurrence lazy: true
            account lazy: true
            customer lazy: true
            supplier lazy: true
        }
    }

    static constraints = {
        account(nullable: true)
        customer(nullable: true)
        supplier(nullable: true)
        description(nullable: true, size: 1..50)
        initialValue(nullable: true, scale: 3, min: 0.001)
        recurringValue(nullable: true, scale: 3)
        finalValue(nullable: true, scale: 3, min: 0.001)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.recurrence.securityCode
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
