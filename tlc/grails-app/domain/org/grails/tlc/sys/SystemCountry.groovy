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
package org.grails.tlc.sys

import org.grails.tlc.books.Customer
import org.grails.tlc.books.CustomerAddress
import org.grails.tlc.books.Supplier
import org.grails.tlc.books.SupplierAddress
import org.grails.tlc.corp.Company
import java.util.concurrent.atomic.AtomicLong

class SystemCountry {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [region: SystemRegion, language: SystemLanguage, currency: SystemCurrency, addressFormat: SystemAddressFormat]
    static hasMany = [users: SystemUser, companies: Company, customers: Customer, suppliers: Supplier, customerAddresses: CustomerAddress, supplierAddresses: SupplierAddress]

    String code
    String name
    String flag = 'UN'
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            region lazy: true
            language lazy: true
            currency lazy: true
            addressFormat lazy: true
            users cascade: 'save-update'
            companies cascade: 'save-update'
            customers cascade: 'save-update'
            suppliers cascade: 'save-update'
            customerAddresses cascade: 'save-update'
            supplierAddresses cascade: 'save-update'
        }
    }

    static constraints = {
        code(blank: false, unique: true, matches: '[A-Z][A-Z]')
        name(blank: false, size: 1..50)
        flag(blank: false, matches: "[A-Z][A-Z]")
        securityCode(validator: {val, obj ->
            return (val == 0)
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
