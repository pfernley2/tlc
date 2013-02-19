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

import org.grails.tlc.corp.ExchangeCurrency
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class Recurring {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [account: Account, currency: ExchangeCurrency, type: DocumentType]
    static hasMany = [lines: RecurringLine]
    static transients = ['sourceCode']

    String sourceCode                               // Used for ajax/dataEntry
    String reference
    String description
    Integer totalTransactions
    Date initialDate
    BigDecimal initialValue
    Date recursFrom
    String recurrenceType = 'monthly'
    Integer recurrenceInterval = 1
    Boolean lastDayOfMonth = false
    BigDecimal recurringValue
    BigDecimal finalValue
    Boolean autoAllocate = true
    Date nextDue
    Integer processedCount = 0
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            account lazy: true
            currency lazy: true
            type lazy: true
            lines cascade: 'all', sort: 'id'
        }
    }

    static constraints = {
        reference(blank: false, size: 1..24, unique: 'account')
        description(nullable: true, size: 1..50)
        totalTransactions(nullable: true, min: 2, validator: {val, obj ->
            if (val > 1 && val < obj.processedCount) return 'low'
            return true
        })
        initialDate(nullable: true, range: UtilService.validDateRange(), validator: {val, obj ->
            if (val) {
                if (val != UtilService.fixDate(val)) return false
                if (obj.recursFrom && val >= obj.recursFrom) return 'less'
            }

            return true
        })
        initialValue(nullable: true, scale: 3, min: 0.001)
        recursFrom(range: UtilService.validDateRange(), validator: {val, obj ->
            if (val) return val == UtilService.fixDate(val)
            return true
        })
        recurrenceType(inList: ['daily', 'weekly', 'monthly'])
        recurrenceInterval(min: 1)
        lastDayOfMonth(validator: {val, obj ->
            if (val) {
                if (obj.recurrenceType != 'monthly') return 'month'
                if (obj.recursFrom && obj.recursFrom != UtilService.endOfMonth(obj.recursFrom)) return 'eom'
            }

            return true
        })
        recurringValue(scale: 3, min: 0.001)
        finalValue(nullable: true, scale: 3, min: 0.001)
        nextDue(nullable: true, validator: {val, obj ->
            if (!obj.processedCount) {
                obj.nextDue = obj.initialDate ?: obj.recursFrom
            } else if (obj.processedCount == 1) {
                if (obj.initialDate) obj.nextDue = obj.recursFrom
            } else if (obj.totalTransactions && obj.processedCount == obj.totalTransactions) {
                obj.nextDue = null
            }

            return true
        })
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
        return reference
    }
}
