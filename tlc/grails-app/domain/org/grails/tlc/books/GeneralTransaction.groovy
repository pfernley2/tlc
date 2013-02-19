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

class GeneralTransaction {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [balance: GeneralBalance, document: Document, customer: Customer, supplier: Supplier, taxCode: TaxCode]
    static hasMany = [allocations: Allocation]
    static transients = ['documentTotal', 'documentDebit', 'documentCredit', 'accountCode', 'accountName', 'accountType', 'account']

    BigDecimal documentTotal                // Used as transient total of goods + tax on data input/display.
    BigDecimal documentDebit                // Used as transient on data entry of journal style documents.
    BigDecimal documentCredit               // Used as transient on data entry of journal style documents.
    String accountCode                      // Used as transient GL account code on data input/display
    String accountName                      // Used as transient GL account name on data input/display
    String accountType                      // Used as transient selector between GL, AR and AP ledgers
    Account account                         // Used to avoid reading the balance record before actual posting begins
    String description
    BigDecimal taxPercentage                // Holds any tax percentage used for this line (may not be the *actual* rate applied to the line)
    BigDecimal documentTax                  // Holds the document (i.e. data entry) currency value of tax on this line (or goods value for a tax line itself)
    BigDecimal documentValue                // Holds the document (i.e. data entry) currency value posted to the account (or tax value for a tax line itself)
    BigDecimal accountTax                   // Holds the customer/supplier account currency value of tax on this line (or goods value for a tax line itself)
    BigDecimal accountValue                 // Holds the customer/supplier account currency value posted to the account (or tax value for a tax line itself)
    BigDecimal accountUnallocated           // Holds the customer/supplier account currency value that has not yet been allocated to other transactions
    BigDecimal generalTax                   // Holds the target GL account currency value of tax on this line (or goods value for a tax line itself)
    BigDecimal generalValue                 // Holds the target GL account currency value posted to the account (or tax value for a tax line itself)
    BigDecimal companyTax                   // Holds the company currency value of tax on this line (or goods value for a tax line itself)
    BigDecimal companyValue                 // Holds the company currency value posted to the account (or tax value for a tax line itself)
    BigDecimal companyUnallocated           // Holds the customer/supplier company currency value that has not yet been allocated to other transactions
    Boolean adjustment = false              // Whether this line affects the adjustment value of a balance as opposed to the normal value
    Boolean affectsTurnover = false         // Used by the posting system to determine whether to adjust customer/supplier turnover data
    Date reconciled                         // Statement matching date where applicable (e.g. supplier statements, customer statements, tax returns, bank statements) set to epoch if yet to be reconciled
    String reconciliationKey                // B + bank account id or T + tax authority id or C + customer id or S + supplier id or null
    Boolean onHold = false                  // For a line posted to a sub-ledger, whether the line is on settlement hold or not
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            balance lazy: true
            document lazy: true
            customer lazy: true
            supplier lazy: true
            taxCode lazy: true
            reconciled index: 'reconciliation_idx'
            reconciliationKey index: 'reconciliation_idx'
            allocations sort: 'id', cascade: 'all'
        }
    }

    static constraints = {
        description(nullable: true, size: 1..50)
        customer(nullable: true)
        supplier(nullable: true)
        taxCode(nullable: true)
        taxPercentage(nullable: true, scale: 3, min: 0.0)
        documentTax(nullable: true, scale: 3)
        documentValue(scale: 3)
        accountTax(nullable: true, scale: 3)
        accountValue(nullable: true, scale: 3)
        accountUnallocated(nullable: true, scale: 3)
        generalTax(nullable: true, scale: 3)
        generalValue(scale: 3)
        companyTax(nullable: true, scale: 3)
        companyValue(scale: 3)
        companyUnallocated(nullable: true, scale: 3)
        affectsTurnover(validator: {val, obj ->
            if (val && !obj.customer && !obj.supplier) return false
            return true
        })
        reconciled(nullable: true)
        reconciliationKey(nullable: true, validator: {val, obj ->
            if (!obj.id) {
                if (obj.customer) {
                    obj.reconciliationKey = "C${obj.customer.id}"
                } else if (obj.supplier) {
                    obj.reconciliationKey = "S${obj.supplier.id}"
                } else if (obj.taxCode && obj.balance.account?.type?.code == 'tax') {
                    obj.reconciliationKey = "T${obj.taxCode.authority.id}"
                } else if (obj.balance.account?.type?.code == 'bank') {
                    obj.reconciliationKey = "B${obj.balance.account.id}"
                } else {
                    obj.reconciliationKey = null
                    obj.reconciled = null
                }
            }

            return true
        })
        onHold(validator: {val, obj ->
            if (val && !obj.customer && !obj.supplier) return false
            return true
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.balance.securityCode
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
