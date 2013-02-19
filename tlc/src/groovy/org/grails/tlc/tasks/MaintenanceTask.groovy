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
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler

public class MaintenanceTask extends TaskExecutable {

    def execute() {
        def count = 0
        def val = grailsApplication.config.maintenance.commands
        if (val && val instanceof List) {
            def statement, callable, tableNames, parameterized
            try {
                for (command in val) {
                    parameterized = (command.indexOf('?') >= 0)
                    if (parameterized && !tableNames) tableNames = createTableNameList()
                    if (command ==~  /\s*\{\s*[c|C][a|A][l|L][l|L]\s+.*\}\s*/) {
                        callable = runSessionFactory.getCurrentSession().connection().prepareCall(command)
                        if (parameterized) {
                            for (tableName in tableNames) {
                                callable.setString(1, tableName)
                                callable.execute()
                                count++
                            }
                        } else {
                            callable.execute()
                            count++
                        }

                        callable.close()
                        callable = null
                    } else {
                        if (!statement) statement = runSessionFactory.getCurrentSession ().connection ().createStatement()
                        if (parameterized) {
                            for (tableName in tableNames) {
                                statement.execute(command.replace('?', tableName))
                                count++
                            }
                        } else {
                            statement.execute(command)
                            count++
                        }
                    }
                }
            } finally {
                if (statement) {
                    try {
                        statement.close()
                    } catch (Exception ex1) {}
                }

                if (callable) {
                    try {
                        statement.close()
                    } catch (Exception ex2) {}
                }
            }
        }

        results.executed = count

        return true
    }

    private createTableNameList() {

        // Get all the tables known to the Grails application
        def list = []
        def tableName
        for (it in grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE)) {
            tableName = runSessionFactory.getClassMetadata(it.getClazz()).getTableName()
            if (!list.contains(tableName)) list << tableName    // Grails produces duplicate names when inhertitance is involved
        }

        // Add in any extra tables
        def val = grailsApplication.config.maintenance.extra.tables
        if (val && val instanceof List) {
            for (it in val) if (!list.contains(it)) list << it
        }

        return list
    }
}