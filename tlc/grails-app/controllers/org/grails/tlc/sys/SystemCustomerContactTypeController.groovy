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

class SystemCustomerContactTypeController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = 'code'
        [systemCustomerContactTypeInstanceList: SystemCustomerContactType.selectList(), systemCustomerContactTypeInstanceTotal: SystemCustomerContactType.selectCount()]
    }

    def show() {
        def systemCustomerContactTypeInstance = SystemCustomerContactType.get(params.id)
        if (!systemCustomerContactTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemCustomerContactType', params.id)
            redirect(action: 'list')
        } else {
            return [systemCustomerContactTypeInstance: systemCustomerContactTypeInstance]
        }
    }

    def delete() {
        def systemCustomerContactTypeInstance = SystemCustomerContactType.get(params.id)
        if (systemCustomerContactTypeInstance) {
            try {
                utilService.deleteWithMessages(systemCustomerContactTypeInstance, [prefix: 'customerContactType.name', code: systemCustomerContactTypeInstance.code])
                flash.message = utilService.standardMessage('deleted', systemCustomerContactTypeInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemCustomerContactTypeInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemCustomerContactType', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemCustomerContactTypeInstance = SystemCustomerContactType.get(params.id)
        if (!systemCustomerContactTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemCustomerContactType', params.id)
            redirect(action: 'list')
        } else {
            return [systemCustomerContactTypeInstance: systemCustomerContactTypeInstance]
        }
    }

    def update(Long version) {
        def systemCustomerContactTypeInstance = SystemCustomerContactType.get(params.id)
        if (systemCustomerContactTypeInstance) {
            if (version != null && systemCustomerContactTypeInstance.version > version) {
                systemCustomerContactTypeInstance.errorMessage(code: 'locking.failure', domain: 'systemCustomerContactType')
                render(view: 'edit', model: [systemCustomerContactTypeInstance: systemCustomerContactTypeInstance])
                return
            }

            def oldCode = systemCustomerContactTypeInstance.code
            systemCustomerContactTypeInstance.properties['code', 'name'] = params
            if (utilService.saveWithMessages(systemCustomerContactTypeInstance, [prefix: 'customerContactType.name', code: systemCustomerContactTypeInstance.code, oldCode: oldCode, field: 'name'])) {
                flash.message = utilService.standardMessage('updated', systemCustomerContactTypeInstance)
                redirect(action: 'show', id: systemCustomerContactTypeInstance.id)
            } else {
                render(view: 'edit', model: [systemCustomerContactTypeInstance: systemCustomerContactTypeInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemCustomerContactType', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemCustomerContactTypeInstance = new SystemCustomerContactType()
        return [systemCustomerContactTypeInstance: systemCustomerContactTypeInstance]
    }

    def save() {
        def systemCustomerContactTypeInstance = new SystemCustomerContactType()
        systemCustomerContactTypeInstance.properties['code', 'name'] = params
        if (utilService.saveWithMessages(systemCustomerContactTypeInstance, [prefix: 'customerContactType.name', code: systemCustomerContactTypeInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', systemCustomerContactTypeInstance)
            redirect(action: 'show', id: systemCustomerContactTypeInstance.id)
        } else {
            render(view: 'create', model: [systemCustomerContactTypeInstance: systemCustomerContactTypeInstance])
        }
    }
}