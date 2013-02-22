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
import java.util.concurrent.atomic.AtomicLong
import org.grails.tlc.books.*

class ExchangeCurrency {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company]
    static hasMany = [exchangeRates: ExchangeRate, documents: Document, accounts: Account, suppliers: Supplier,
        customers: Customer, templates: TemplateDocument, recurrences: Recurring]

    static transients = ['currentRateDate', 'currentRateValue', 'currentRateUpdatable']

    String code
    String name
    Byte decimals = 2
    Boolean autoUpdate
    Boolean companyCurrency = false
    Date currentRateDate                // Used for holding today's rate details, if any
    BigDecimal currentRateValue         // as above
    Boolean currentRateUpdatable        // as above
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache true
        columns {
            company lazy: true
            exchangeRates cascade: 'all'
            documents cascade: 'save-update'
            accounts cascade: 'save-update'
            customers cascade: 'save-update'
            suppliers cascade: 'save-update'
            templates cascade: 'save-update'
            recurrences cascade: 'save-update'
        }
    }

    static constraints = {
        code(blank: false, unique: 'company', matches: "[A-Z][A-Z][A-Z]")
        name(blank: false, size: 1..30)
        decimals(range: 0..3)
        autoUpdate(validator: {val, obj ->
            return !(val && obj.code == UtilService.BASE_CURRENCY_CODE)
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.company.securityCode
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
        return code
    }
}
