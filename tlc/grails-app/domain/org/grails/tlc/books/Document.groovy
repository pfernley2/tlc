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

import org.grails.tlc.corp.ExchangeCurrency
import org.grails.tlc.corp.TaxStatement
import org.grails.tlc.sys.UtilService
import doc.Line
import doc.Tax
import doc.Total
import java.util.concurrent.atomic.AtomicLong

class Document {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [currency: ExchangeCurrency, type: DocumentType, period: Period]
    static hasMany = [lines: Line, taxes: Tax, total: Total, taxStatements: TaxStatement, revaluations: PeriodRevaluation]
    static transients = ['sourceCode', 'sourceName', 'sourceHold', 'sourceAdjustment', 'sourceAffectsTurnover', 'sourceGoods', 'sourceTax', 'sourceTotal']

    String sourceCode                               // Used for ajax/dataEntry
    String sourceName                               // Used for ajax fields on the GSPs
    Boolean sourceHold = false                      // Used for data entry
    Boolean sourceAdjustment = false                // Used for data entry
    Boolean sourceAffectsTurnover = false           // Used for data entry
    BigDecimal sourceGoods                          // Used for data entry
    BigDecimal sourceTax                            // Used for data entry
    BigDecimal sourceTotal                          // Used for data entry
    String code
    String description
    Date documentDate
    Date dueDate
    String reference
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            currency lazy: true
            type lazy: true, index: 'doc_created_idx'
            dateCreated index: 'doc_created_idx'
            period lazy: true
            lines cascade: 'all', sort: 'id'
            taxes cascade: 'all', sort: 'id'
            total cascade: 'all'
            taxStatements cascade: 'all'
            revaluations: cascade: 'save-update'
        }
    }

    static constraints = {
        lines(minsize: 1)
        total(maxSize: 1)
        code(blank: false, size: 1..10, unique: 'type')
        description(nullable: true, size: 1..50)
        documentDate(range: (new Date() - 365)..(new Date() + 365), validator: {val, obj ->
            if (val && val != UtilService.fixDate(val)) return 'invalid'

            return true
        })
        dueDate(nullable: true, validator: {val, obj ->
            if (val) {
                if (obj.documentDate && (val < obj.documentDate || val > obj.documentDate + 250 || val != UtilService.fixDate(val))) return 'invalid'
            } else {
                obj.dueDate = obj.documentDate
            }

            return true
        })
        reference(nullable: true, size: 1..30)
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
        return code
    }
}
