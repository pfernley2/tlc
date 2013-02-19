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
import org.grails.tlc.sys.UtilService
import it.sauronsoftware.cron4j.SchedulingPattern
import java.util.concurrent.atomic.AtomicLong

class PaymentSchedule {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company]
    static hasMany = [suppliers: Supplier]

    String code
    String name
    String monthDayPattern
    String weekDayPattern
    String pattern = '*'
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            company lazy: true
            suppliers cascade: 'save-update'
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, matches: '[a-zA-Z][a-zA-Z_0-9]*', unique: 'company')
        name(blank: false, size: 1..30)
        monthDayPattern(nullable: true, size: 1..30, validator: {val, obj ->
            if (val) {
                def ptn = '0 0 ' + val.replace(' ', '') + ' * '
                if (!SchedulingPattern.validate(ptn + '*')) return 'bad.pattern'
                if (obj.weekDayPattern && SchedulingPattern.validate('0 0 * * ' + obj.weekDayPattern.replace(' ', ''))) {
                    obj.pattern = ptn + obj.weekDayPattern.replace(' ', '')
                } else {
                    obj.pattern = ptn + '*'
                }

                if (!SchedulingPattern.validate(obj.pattern)) return 'bad.combo'
            } else {
                if (!obj.weekDayPattern) return 'no.pattern'
            }

            return true
        })
        weekDayPattern(nullable: true, size: 1..30, validator: {val, obj ->
            if (val) {
                def ptn = '0 0 * * ' + val.replace(' ', '')
                if (!SchedulingPattern.validate(ptn)) return 'bad.pattern'
                if (!obj.monthDayPattern) obj.pattern = ptn
            }

            return true
        })
        pattern(blank: false, size: 1..67)
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
