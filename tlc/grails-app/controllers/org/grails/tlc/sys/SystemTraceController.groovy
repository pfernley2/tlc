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
package org.grails.tlc.sys

import org.grails.tlc.corp.Company

class SystemTraceController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        if (!['id', 'databaseAction', 'domainName', 'domainId', 'domainVersion', 'domainData', 'dateCreated'].contains(params.sort)) {
            params.sort = 'id'
            params.order = 'desc'
        }

        def systemTraceInstanceList = SystemTrace.selectList()
        for (trace in systemTraceInstanceList) {
            trace.companyDecode = decodeCompany(trace.domainSecurityCode)
            trace.userDecode = decodeUser(trace.userId)
        }

        [systemTraceInstanceList: systemTraceInstanceList, systemTraceInstanceTotal: SystemTrace.selectCount()]
    }

    def show() {
        def systemTraceInstance = SystemTrace.get(params.id)
        if (!systemTraceInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemTrace', params.id)
            redirect(action: 'list')
        } else {
            systemTraceInstance.companyDecode = decodeCompany(systemTraceInstance.domainSecurityCode)
            systemTraceInstance.userDecode = decodeUser(systemTraceInstance.userId)
            return [systemTraceInstance: systemTraceInstance]
        }
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private decodeCompany(code) {
        if (code) {
            def company = Company.findBySecurityCode(code)
            if (company) return company.name
            return message(code: 'systemTrace.bad.company', args: [code], default: "Unknown company security code ${code}")
        } else {
            return message(code: 'systemTrace.no.company', default: 'No Company')
        }
    }

    private decodeUser(code) {
        if (code) {
            def user = SystemUser.get(code)
            if (user) return user.name
            return message(code: 'systemTrace.bad.user', args: [code], default: "Unknown user id ${code}")
        } else {
            return message(code: 'systemTrace.no.user', default: 'No User')
        }
    }
}