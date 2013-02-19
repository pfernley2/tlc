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

class SystemAccountTypeController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'sectionType', 'singleton', 'changeable', 'allowInvoices', 'allowCash', 'allowProvisions', 'allowJournals'].contains(params.sort) ? params.sort : 'code'
        [systemAccountTypeInstanceList: SystemAccountType.selectList(), systemAccountTypeInstanceTotal: SystemAccountType.selectCount()]
    }

    def show() {
        def systemAccountTypeInstance = SystemAccountType.get(params.id)
        if (!systemAccountTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemAccountType', params.id)
            redirect(action: 'list')
        } else {
            return [systemAccountTypeInstance: systemAccountTypeInstance]
        }
    }

    def delete() {
        def systemAccountTypeInstance = SystemAccountType.get(params.id)
        if (systemAccountTypeInstance) {
            try {
                utilService.deleteWithMessages(systemAccountTypeInstance, [prefix: 'systemAccountType.name', code: systemAccountTypeInstance.code])
                flash.message = utilService.standardMessage('deleted', systemAccountTypeInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemAccountTypeInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemAccountType', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemAccountTypeInstance = SystemAccountType.get(params.id)
        if (!systemAccountTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemAccountType', params.id)
            redirect(action: 'list')
        } else {
            return [systemAccountTypeInstance: systemAccountTypeInstance]
        }
    }

    def update(Long version) {
        def systemAccountTypeInstance = SystemAccountType.get(params.id)
        if (systemAccountTypeInstance) {
            if (version != null && systemAccountTypeInstance.version > version) {
                systemAccountTypeInstance.errorMessage(code: 'locking.failure', domain: 'systemAccountType')
                render(view: 'edit', model: [systemAccountTypeInstance: systemAccountTypeInstance])
                return
            }

            def oldCode = systemAccountTypeInstance.code
            systemAccountTypeInstance.properties['code', 'name', 'sectionType', 'singleton', 'changeable', 'allowInvoices', 'allowCash', 'allowProvisions', 'allowJournals'] = params
            if (utilService.saveWithMessages(systemAccountTypeInstance, [prefix: 'systemAccountType.name', code: systemAccountTypeInstance.code, oldCode: oldCode, field: 'name'])) {
                flash.message = utilService.standardMessage('updated', systemAccountTypeInstance)
                redirect(action: 'show', id: systemAccountTypeInstance.id)
            } else {
                render(view: 'edit', model: [systemAccountTypeInstance: systemAccountTypeInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemAccountType', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemAccountTypeInstance = new SystemAccountType()
        return [systemAccountTypeInstance: systemAccountTypeInstance]
    }

    def save() {
        def systemAccountTypeInstance = new SystemAccountType()
        systemAccountTypeInstance.properties['code', 'name', 'sectionType', 'singleton', 'changeable', 'allowInvoices', 'allowCash', 'allowProvisions', 'allowJournals'] = params
        if (utilService.saveWithMessages(systemAccountTypeInstance, [prefix: 'systemAccountType.name', code: systemAccountTypeInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', systemAccountTypeInstance)
            redirect(action: 'show', id: systemAccountTypeInstance.id)
        } else {
            render(view: 'create', model: [systemAccountTypeInstance: systemAccountTypeInstance])
        }
    }
}