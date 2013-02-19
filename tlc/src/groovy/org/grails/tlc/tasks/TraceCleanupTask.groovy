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
package org.grails.tlc.tasks

import org.grails.tlc.sys.SystemTrace
import org.grails.tlc.sys.SystemTracing
import org.grails.tlc.sys.TaskExecutable

class TraceCleanupTask extends TaskExecutable {

    def execute() {
        def now = new Date()
        def count = 0
        def tracings = SystemTracing.list()
        for (tracing in tracings) {
            yield()
            count += deleteTraces(tracing.domainName, 'insert', now - tracing.insertRetentionDays)
            yield()
            count += deleteTraces(tracing.domainName, 'update', now - tracing.updateRetentionDays)
            yield()
            count += deleteTraces(tracing.domainName, 'delete', now - tracing.deleteRetentionDays)
        }

        results.count = count
    }

    def deleteTraces(domainName, databaseAction, cutoff) {
        return SystemTrace.executeUpdate('delete from SystemTrace where domainName = ? and databaseAction = ? and dateCreated <= ?', [domainName, databaseAction, cutoff])
    }
}

