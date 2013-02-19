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

class TaskParam {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [task: Task]
    static hasMany = [queued: QueuedTaskParam]

    String code
    String name
    Integer sequencer
    String dataType
    Integer dataScale
    String defaultValue
    Boolean required
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            task lazy: true
            queued cascade: 'save-update'
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, matches: '[a-zA-Z][a-zA-Z_0-9]*', unique: 'task')
        name(blank: false, size: 1..30)
        dataType(inList: ['string', 'integer', 'decimal', 'date', 'boolean'])
        dataScale(nullable: true, range: 1..10, validator: {val, obj ->
            switch (obj.dataType) {
                case 'date':
                    if (val != 1 && val != 2) return 'date.error'
                    break

                case 'decimal':
                    if (!val) return 'decimal.error'
                    break

                default:
                    if (val) return 'other.error'
                    break
            }

            return true
        })
        defaultValue(nullable: true, size: 1..100, validator: {val, obj ->
            if (val != null) {
                def result = UtilService.stringOf(obj.dataType, obj.dataScale, UtilService.valueOf(obj.dataType, obj.dataScale, val))
                if (result == null) return false
                obj.defaultValue = result
            }

            return true
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.task.securityCode
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
