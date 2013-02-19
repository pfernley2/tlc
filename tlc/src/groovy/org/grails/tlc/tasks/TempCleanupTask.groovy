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

import org.grails.tlc.sys.TaskExecutable

class TempCleanupTask extends TaskExecutable {

    def count = 0

    def execute() {
        def retentionDays = params.days
        if (!retentionDays || retentionDays < 0) retentionDays = 1  // Default to one day of history
        deleteFiles(utilService.realFile('/temp'), new Date() - retentionDays)
        results.count = count
    }

    def deleteFiles(dir, date) {
        def list = dir.listFiles()
        for (file in list) {
            yield()
            if (file.isDirectory()) {
                if (file.name != 'swap') deleteFiles(file, date)    // Don't delete the report virtualizer swap file
            } else if (file.lastModified() <= date.getTime()) {
                file.delete()
                count++
            }
        }
    }
}
