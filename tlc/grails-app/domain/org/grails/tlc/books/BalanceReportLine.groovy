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

import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class BalanceReportLine {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [format: BalanceReportFormat, section: ChartSection]
    static transients = ['resequencing']

    Boolean resequencing = false    // Transient used during resequencing operation
    Integer lineNumber
    String text
    String accumulation
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            format lazy: true
            section lazy: true
        }
    }

    static constraints = {
        lineNumber(min: 1, unique: 'format', validator: {val, obj ->
            if (val > 2000000000 && !obj.resequencing) return false

            return true
        })
        text(nullable: true, size: 1..50, validator: {val, obj ->
            if (obj.accumulation && !val) return ['missing', obj.lineNumber]

            return true
        })
        section(nullable: true, validator: {val, obj ->
            if (val) {
                if (val.securityCode != obj.format.securityCode || val.type != 'bs' || val.accountSegment <= 0) return ['invalid', obj.lineNumber, val.code]
            }

            return true
        })
        accumulation(nullable: true, size: 1..200, validator: {val, obj ->
            if (val) {
                if (obj.section) return ['extra', obj.lineNumber]
                if (val != val.trim()) {
                    val = val.trim()
                    obj.accumulation = val
                }

                if (!val.startsWith('+') && !val.startsWith('-')) return ['sign', obj.lineNumber]
                def items = val.substring(1).split(',')*.trim()
                if (!items) return ['empty', obj.lineNumber]
                for (item in items) {
                    if (item.isInteger()) {    // It's a line number
                        if (item.toInteger() >= obj.lineNumber) return ['less', obj.lineNumber, item]
                    } else {    // A section code
                        def sec
                        ChartSection.withNewSession {
                            sec = ChartSection.findByCodeAndSecurityCode(item, obj.format.securityCode)
                        }

                        if (!sec) return ['section', obj.lineNumber, item]
                    }
                }
            }

            return true
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.format.securityCode
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
