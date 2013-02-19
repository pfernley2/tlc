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

import org.grails.tlc.sys.SystemActivity
import org.grails.tlc.sys.SystemUser
import org.grails.tlc.sys.TaskScanner
import org.grails.tlc.sys.UtilService
import it.sauronsoftware.cron4j.SchedulingPattern
import java.util.concurrent.atomic.AtomicLong

class Task {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company, user: SystemUser, activity: SystemActivity]
    static hasMany = [parameters: TaskParam, results: TaskResult, queued: QueuedTask]

    String code
    String name
    String executable
    Boolean allowOnDemand
    String schedule
    Date nextScheduledRun
    Integer retentionDays = 7
    Boolean systemOnly = false
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            company lazy: true
            user lazy: true
            activity lazy: true
            parameters cascade: 'all'
            results cascade: 'all'
            queued cascade: 'save-update'
            nextScheduledRun index: 'next_run_idx'
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, matches: '[a-zA-Z][a-zA-Z_0-9]*', unique: 'company')
        name(blank: false, size: 1..50)
        executable(blank: false, size: 1..50, validator: {val, obj ->
            if (val) {
                try {
                    Class.forName("org.grails.tlc.tasks.${val}", true, TaskScanner.getClassLoader()).newInstance()
                } catch (Throwable t) {
                    return 'no.instance'
                }
            }

            return true
        })
        schedule(nullable: true, size: 1..200, validator: {val, obj ->
            return !(val && !SchedulingPattern.validate(val))
        })
        nextScheduledRun(nullable: true, min: new Date(), max: UtilService.maxDate(), validator: {val, obj ->
            return !(val && !obj.schedule)
        })
        retentionDays(range: 1..1000)
        systemOnly(validator: {val, obj ->
            return (!val || obj.company.systemOnly)
        })
        activity(nullable: true, validator: {val, obj ->
            return (obj.allowOnDemand && !val) ? 'no.activity' : true
        })
        user(nullable: true, validator: {val, obj ->
            return (obj.schedule && !val) ? 'no.user' : true
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
