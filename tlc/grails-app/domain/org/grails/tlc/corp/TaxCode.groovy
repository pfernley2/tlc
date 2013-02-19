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

import org.grails.tlc.books.Customer
import org.grails.tlc.books.GeneralTransaction
import org.grails.tlc.books.Supplier
import org.grails.tlc.books.TemplateLine
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class TaxCode {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company, authority: TaxAuthority]
    static hasMany = [rates: TaxRate, transactions: GeneralTransaction, suppliers: Supplier, customers: Customer, templateLines: TemplateLine, statementLines: TaxStatementLine]

    String code
    String name
    Boolean companyTaxCode = false
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache true
        columns {
            company lazy: true
            rates cascade: 'all'
            transactions cascade: 'save-update'
            customers cascade: 'save-update'
            suppliers cascade: 'save-update'
            templateLines cascade: 'save-update'
            statementLines cascade: 'save-update'
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, matches: '[a-zA-Z0-9][a-zA-Z_0-9]*', unique: 'company')
        name(blank: false, size: 1..50)
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
