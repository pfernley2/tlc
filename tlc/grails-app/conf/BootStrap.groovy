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

import org.grails.tlc.sys.CacheService
import org.grails.tlc.sys.SecurityService
import org.grails.tlc.sys.InitialSystemData
import org.grails.tlc.sys.SystemCurrency
import org.grails.tlc.sys.TaskService
import org.grails.tlc.sys.SystemWorkarea
import org.grails.tlc.sys.ReportService
import org.grails.tlc.sys.SystemSetting
import org.grails.tlc.sys.UpgradeService
import org.grails.tlc.books.BookService

class BootStrap {

    def grailsApplication

    def init = {servletContext ->

        // Graphics on the server may be headless
        System.setProperty('java.awt.headless', 'true')

        // Start caching
        CacheService.createCaches(grailsApplication)

        // Bring the controller/action security info up to date in the database
        SecurityService.syncActionActivities(grailsApplication)

        // Bring the domain tracing info up to date in the database
        SecurityService.syncTracingDomains(grailsApplication)

        // We will need to know the current application version below
        def appVersion = grailsApplication.metadata.'app.version'

        // If a completely new system, load the initial data
        if (SystemCurrency.count() == 0) {
            new InitialSystemData().loadSystem(servletContext, appVersion)
        } else {
            SecurityService.syncCompanyAdminRole()
            SystemWorkarea.executeUpdate('delete from SystemWorkarea')
            def dataVersion = SystemSetting.findByCode('dataVersion') ?: new SystemSetting(code: 'dataVersion', dataType: 'string', value: '1.0', systemOnly: true)
            if (appVersion != dataVersion.value && !UpgradeService.upgrade(dataVersion, appVersion, servletContext)) {
                throw new IllegalArgumentException('Unable to upgrade from ' + dataVersion.value + ' to ' + appVersion)
            }
        }

        // Initialize the Util service
        BookService.prepareForUse(grailsApplication)

        // Start the task system
        TaskService.start()
    }

    def destroy = {
        TaskService.stop()
        ReportService.stop()
    }
}