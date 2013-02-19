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

import org.grails.tlc.corp.Company
import org.grails.tlc.corp.ExchangeCurrency
import org.grails.tlc.corp.TaxCode
import org.grails.tlc.sys.SystemCountry
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class Customer {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company, taxCode: TaxCode, country: SystemCountry, currency: ExchangeCurrency, accessCode: AccessCode]
    static hasMany = [transactions: GeneralTransaction, turnovers: CustomerTurnover, templates: TemplateDocument, templateLines: TemplateLine,
		recurringLines: RecurringLine, addresses: CustomerAddress, addressUsages: CustomerAddressUsage, statements: Statement]

    String code
    String name
    String taxId
    Integer settlementDays = 0
    Boolean periodicSettlement = false
    Boolean active = true
    BigDecimal accountCreditLimit = 0
    BigDecimal accountCurrentBalance = 0.0
    BigDecimal companyCurrentBalance = 0.0
    String revaluationMethod
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache true
        columns {
            company lazy: true
            taxCode lazy: true
            country lazy: true
            currency lazy: true
            accessCode lazy: true
            transactions cascade: 'save-update'
            turnovers cascade: 'all'
            templates cascade: 'save-update'
            templateLines cascade: 'save-update'
            recurringLines cascade: 'save-update'
            addresses cascade: 'all'
            addressUsages cascade: 'all'
            statements cascade: 'all'
        }
    }

    static constraints = {
        taxCode(nullable: true)
        code(blank: false, size: 1..20, unique: 'company', validator: {val, obj ->
            obj.code = BookService.fixCustomerCase(val)
            return true
        })
        name(blank: false, size: 1..50)
        taxId(nullable: true, size: 1..20)
        settlementDays(range: 0..250)
        accountCreditLimit(scale: 0, min: 0.0)
        accountCurrentBalance(scale: 3)
        companyCurrentBalance(scale: 3)
        revaluationMethod(nullable: true, inList: ['standard'], validator: {val, obj ->
            return (val && obj.currency?.companyCurrency) ? 'none' : true
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

    def hasTransactions() {
        return (GeneralTransaction.countByCustomer(this) > 0)
    }

    public String toString() {
        return code
    }
}
