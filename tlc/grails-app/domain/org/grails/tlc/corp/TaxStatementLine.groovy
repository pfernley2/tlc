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

class TaxStatementLine {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [statement: TaxStatement, taxCode: TaxCode]

    Boolean currentStatement                // If false, means that this line represents a prior period adjustment
    Boolean expenditure                     // If true then this line represents input tax on purchases, else represents output tax on sales
    BigDecimal taxPercentage                // The actual percentage used
    BigDecimal companyGoodsValue            // Signed values with input taxes on purchases normally being positive and out taxes on sales normally being negative. HOWEVER, it is
    BigDecimal companyTaxValue              // possible (if unlikely) in the case of, say, a massive credit note exceeding the value of all invoices that signs could be opposite
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            statement lazy: true
            taxCode lazy: true
        }
    }

    static constraints = {
        taxPercentage(scale: 3, min: 0.0)
        companyGoodsValue(scale: 3)
        companyTaxValue(scale: 3)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.taxCode.securityCode
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
