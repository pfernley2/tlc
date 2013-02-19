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
import org.grails.tlc.sys.SystemDocumentType
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class DocumentType {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company, type: SystemDocumentType]
    static hasMany = [documents: Document, templates: TemplateDocument, recurrences: Recurring, suppliers: Supplier]

    String code
    String name
    Integer nextSequenceNumber = 100000
    Boolean autoGenerate = true
    Boolean allowEdit = false
    Account autoBankAccount
    Boolean autoForeignCurrency
    Integer autoMaxPayees
    Boolean autoBankDetails
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache true
        columns {
            company lazy: true
            systemDocumentType lazy: true
            documents cascade: 'save-update'
            templates cascade: 'save-update'
            recurrences cascade: 'save-update'
            suppliers cascade: 'save-update'
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, matches: '[^:\\= \\.]+', unique: 'company')
        name(blank: false, size: 1..30)
        nextSequenceNumber(min: 1)
        allowEdit(validator: {val, obj ->
            if (!val && !obj.autoGenerate) obj.allowEdit = true
            return true
        })
        autoBankAccount(nullable: true, validator: {val, obj ->
            if (val) {
                if (obj.type.code != 'BP') return 'not.payment'
                if (val.type.code != 'bank') return 'not.bank'
            }

            return true
        })
        autoForeignCurrency(nullable: true, validator: {val, obj ->
            return (val && !obj.autoBankAccount) ? 'not.auto' : true
        })
        autoMaxPayees(nullable: true, min: 1, validator: {val, obj ->
            if (val) {
                if (!obj.autoBankAccount) return 'not.auto'
            } else {
                if (obj.autoBankAccount) return 'is.auto'
            }

            return true
        })
        autoBankDetails(nullable: true, validator: {val, obj ->
            if (obj.autoBankAccount) {
                if (val == null) obj.autoBankDetails = false
            } else {
                if (val != null) obj.autoBankDetails = null
            }

            return true
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
