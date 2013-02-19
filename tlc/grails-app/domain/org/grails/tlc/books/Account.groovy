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
import org.grails.tlc.sys.SystemAccountType
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class Account {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [section: ChartSection, type: SystemAccountType, currency: ExchangeCurrency, element1: CodeElementValue, element2: CodeElementValue,
            element3: CodeElementValue, element4: CodeElementValue, element5: CodeElementValue, element6: CodeElementValue, element7: CodeElementValue,
            element8: CodeElementValue]
    static hasMany = [balances: GeneralBalance, templates: TemplateDocument, templateLines: TemplateLine,
            recurrences: Recurring, recurringLines: RecurringLine, reconciliations: Reconciliation]
    static transients = ['autoCreateElementValues']

    String code
    String name
    String status
    Boolean active = true
    Boolean autoCreateElementValues = true
    Account revaluationAccount
    String revaluationMethod
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache true
        columns {
            section lazy: true
            type lazy: true
            currency lazy: true
            element1 lazy: true
            element2 lazy: true
            element3 lazy: true
            element4 lazy: true
            element5 lazy: true
            element6 lazy: true
            element7 lazy: true
            element8 lazy: true
            revaluationAccount lazy: true
            balances cascade: 'all'
            templates cascade: 'save-update'
            templateLines cascade: 'save-update'
            recurrences cascade: 'save-update'
            recurringLines cascade: 'save-update'
            reconciliations cascade: 'all'
        }
    }

    static constraints = {
        code(blank: false, size: 1..87, unique: 'securityCode')
        name(blank: false, size: 1..87)
        status(blank: false, inList: ['dr', 'cr'])
        type(nullable: true, validator: {val, obj ->
            if (val) {
                if (obj.section && val.sectionType != obj.section.type) return 'mismatch'
                if (val.code == 'glRevalue' && !obj.currency?.companyCurrency) return 'currency'
            }

            return true
        })
        element1(nullable: true)
        element2(nullable: true)
        element3(nullable: true)
        element4(nullable: true)
        element5(nullable: true)
        element6(nullable: true)
        element7(nullable: true)
        element8(nullable: true)
        revaluationAccount(nullable: true, validator: {val, obj ->
            if (val) {
                if (obj.currency?.companyCurrency) return 'none'
                if (obj.section?.type == 'ie') return 'section'
                if (val.type.code != 'glRevalue') return 'type'
                if (!obj.revaluationMethod) obj.revaluationMethod = 'standard'
            }

            return true
        })
        revaluationMethod(nullable: true, inList: ['standard'], validator: {val, obj ->
            if (val && !obj.revaluationAccount) obj.revaluationMethod = null
            return true
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.section.securityCode
            return true
        })
    }

    // Check if this account has any transactions
    def hasTransactions() {
        def balances = GeneralBalance.findAllByAccount(this)
        for (bal in balances) {
            if (GeneralTransaction.countByBalance(bal)) return true
        }

        return false
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
