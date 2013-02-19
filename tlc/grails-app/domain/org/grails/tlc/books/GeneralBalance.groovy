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

class GeneralBalance {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [account: Account, period: Period]
    static hasMany = [generalTransactions: GeneralTransaction]

    BigDecimal generalOpeningBalance = 0.0
    BigDecimal generalTransactionTotal = 0.0
    BigDecimal generalAdjustmentTotal = 0.0
    BigDecimal generalClosingBalance = 0.0
    BigDecimal companyOpeningBalance = 0.0
    BigDecimal companyTransactionTotal = 0.0
    BigDecimal companyAdjustmentTotal = 0.0
    BigDecimal companyClosingBalance = 0.0
    BigDecimal generalBudget = 0
    BigDecimal companyBudget = 0
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            account lazy: true
            period lazy: true
            generalTransactions cascade: 'save-update'
        }
    }

    static constraints = {
        period(unique: 'account')
        companyOpeningBalance(scale: 3)
        companyTransactionTotal(scale: 3)
        companyAdjustmentTotal(scale: 3)
        companyClosingBalance(scale: 3, validator: {val, obj ->
            if (val != obj.companyOpeningBalance + obj.companyTransactionTotal + obj.companyAdjustmentTotal) return 'bad.balance'
            return true
        })
        generalOpeningBalance(scale: 3)
        generalTransactionTotal(scale: 3)
        generalAdjustmentTotal(scale: 3)
        generalClosingBalance(scale: 3, validator: {val, obj ->
            if (val != obj.generalOpeningBalance + obj.generalTransactionTotal + obj.generalAdjustmentTotal) return 'bad.balance'
            return true
        })
        generalBudget(scale: 0)
        companyBudget(scale: 0)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.period.securityCode
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
