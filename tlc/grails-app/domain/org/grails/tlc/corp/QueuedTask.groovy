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

import org.grails.tlc.sys.SystemUser
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class QueuedTask {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [task: Task, user: SystemUser]
    static hasMany = [parameters: QueuedTaskParam, results: QueuedTaskResult]

    Boolean scheduled
    String currentStatus = 'waiting'
    Date preferredStart
    Date startedAt
    Date completedAt
    String completionMessage
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            task lazy: true
            user lazy: true
            parameters cascade: 'all'
            results cascade: 'all'
            currentStatus index: 'waiting_idx'
            preferredStart index: 'waiting_idx'
        }
    }

    static constraints = {
        currentStatus(inList: ['waiting', 'running', 'failed', 'cancelled', 'abandoned', 'completed'])
        preferredStart(range: UtilService.validDateRange())
        startedAt(nullable: true, range: UtilService.validDateRange())
        completedAt(nullable: true, range: UtilService.validDateRange())
        completionMessage(nullable: true, size: 1..200)
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
        return "${id}"
    }
}
