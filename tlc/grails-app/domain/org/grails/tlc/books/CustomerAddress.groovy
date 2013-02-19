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

import org.grails.tlc.sys.SystemAddressFormat
import org.grails.tlc.sys.SystemCountry
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class CustomerAddress {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [customer: Customer, country: SystemCountry, format: SystemAddressFormat]
    static hasMany = [addressUsages: CustomerAddressUsage, contacts: CustomerContact]
    static transients = ['usageTransfers']

    List usageTransfers
    String location1
    String location2
    String location3
    String metro1
    String metro2
    String area1
    String area2
    String encoding
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            customer lazy: true
            country lazy: false
            format lazy: true
            addressUsages cascade: 'all'
            contacts cascade: 'all'
        }
    }

    static constraints = {
        location1(nullable: true, size: 1..50)
        location2(nullable: true, size: 1..50)
        location3(nullable: true, size: 1..50)
        metro1(nullable: true, size: 1..50)
        metro2(nullable: true, size: 1..50)
        area1(nullable: true, size: 1..50)
        area2(nullable: true, size: 1..50)
        encoding(nullable: true, size: 1..50)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.customer.securityCode
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
