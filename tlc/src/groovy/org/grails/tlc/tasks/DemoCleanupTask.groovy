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

import org.grails.tlc.corp.Company
import org.grails.tlc.corp.CompanyUser
import org.grails.tlc.corp.Task
import org.grails.tlc.sys.SystemUser
import org.grails.tlc.sys.TaskExecutable

public class DemoCleanupTask extends TaskExecutable {

    def execute() {
        def userCount = 0
        def userFails = 0
        def companyCount = 0
        def companyFails = 0

        if (utilService.systemSetting('isDemoSystem')) {
            def retentionDays = params.days
            if (!retentionDays || retentionDays <= 0) retentionDays = 30  // Default to 30 days grace from last login
            def cutoff = utilService.fixDate(new Date() - retentionDays)

            // Find non-system companies created before the cutoff date where no user
            // has logged in to that company after the cutoff date
            def companies = Company.findAllBySystemOnlyAndDateCreatedLessThan(false, cutoff)
            for (company in companies) {
                if (CompanyUser.countByCompanyAndLastUsedGreaterThanEquals(company, cutoff) == 0) {
                    company.prepareForDeletion(runSessionFactory.currentSession)    // Notice not in a transaction
                    Company.withTransaction {status ->
                        try {
                            bookService.deleteCompany(company)
                            utilService.cacheService.clearAll(company.securityCode)
                            def logo = utilService.realFile("/images/logos/L${company.securityCode}.png")
                            if (logo.exists()) logo.delete()
                            companyCount++
                        } catch (Exception e3) {
                            status.setRollbackOnly()
                            companyFails++
                        }
                    }
                }
            }

            // Find non-administrator users with a last login date before the cutoff date
            def users = SystemUser.findAllByLastLoginLessThanAndAdministrator(cutoff, false)
            for (user in users) {
                if (!isTaskOwner(user)) {
                    try {
                        user.delete(flush: true)
                        userCount++
                    } catch (Exception e1) {
                        userFails++
                    }
                }
            }

            // Find users with no login date, but created before the cutoff date
            users = SystemUser.findAllByLastLoginIsNullAndDateCreatedLessThan(cutoff)
            for (user in users) {
                if (!user.administrator && !isTaskOwner(user)) {
                    try {
                        user.delete(flush: true)
                        userCount++
                    } catch (Exception e2) {
                        userFails++
                    }
                }
            }
        }

        results.userCount = userCount
        results.userFails = userFails
        results.coCount = companyCount
        results.coFails = companyFails
    }

    private isTaskOwner(user) {
        def companyUsers = CompanyUser.findAllByUser(user)
        for (coUser in companyUsers) {
            if (Task.countByCompanyAndUser(coUser.company, coUser.user)) return true
        }

        return false
    }
}