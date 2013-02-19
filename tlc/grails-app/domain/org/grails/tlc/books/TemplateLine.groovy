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

import org.grails.tlc.corp.TaxCode
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class TemplateLine {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [template: TemplateDocument, account: Account, customer: Customer, supplier: Supplier, taxCode: TaxCode]
    static transients = ['documentDebit', 'documentCredit', 'accountCode', 'accountName', 'accountType']

    BigDecimal documentDebit                // Used as transient on data entry of journal style documents.
    BigDecimal documentCredit               // Used as transient on data entry of journal style documents.
    String accountCode                      // Used as transient GL account code on data input/display
    String accountName                      // Used as transient GL account name on data input/display
    String accountType                      // Used as transient selector between GL, AR and AP ledgers
    String description
    BigDecimal documentTax                  // Holds the document (i.e. data entry) currency value of tax on this line (or goods value for a tax line itself)
    BigDecimal documentValue                // Holds the document (i.e. data entry) currency value of goods on this line (or tax value for a tax line itself)
    BigDecimal documentTotal                // Total of good + tax in document currency.
    Boolean adjustment = false      // Whether this line affects the adjustment value of a balance as opposed to the normal value
    Boolean affectsTurnover = false
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            account lazy: true
            template lazy: true
            customer lazy: true
            supplier lazy: true
            taxCode lazy: true
        }
    }

    static constraints = {
        description(nullable: true, size: 1..50)
        account(nullable: true)
        customer(nullable: true)
        supplier(nullable: true)
        taxCode(nullable: true)
        documentTax(nullable: true, scale: 3)
        documentValue(nullable: true, scale: 3)
        documentTotal(nullable: true, scale: 3)
        affectsTurnover(validator: {val, obj ->
            if (val && !obj.customer && !obj.supplier) return false
            return true
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.template.securityCode
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
