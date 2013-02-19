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

class PeriodRevaluation {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [period: Period, document: Document]

    Account revaluationAccount
    Account account
    Customer customer
    Supplier supplier
    BigDecimal currentBalance
    BigDecimal priorRevaluations = 0.0
    BigDecimal currentRevaluation
    BigDecimal revaluedBalance
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            period lazy: true
            account lazy: true
            customer lazy: true
            supplier lazy: true
        }
    }

    static constraints = {
        period(validator: {val, obj ->
            return (val?.id == obj.document?.period?.id && (obj.account || obj.customer || obj.supplier))
        })
        account(nullable: true)
        customer(nullable: true)
        supplier(nullable: true)
        currentBalance(scale: 3)
        priorRevaluations(scale: 3)
        currentRevaluation(scale: 3)
        revaluedBalance(scale: 3)
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
