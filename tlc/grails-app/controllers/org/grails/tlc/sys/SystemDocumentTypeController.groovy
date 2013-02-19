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

class SystemDocumentTypeController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'analysisIsDebit', 'customerAllocate', 'supplierAllocate'].contains(params.sort) ? params.sort : 'code'
        [systemDocumentTypeInstanceList: SystemDocumentType.selectList(), systemDocumentTypeInstanceTotal: SystemDocumentType.selectCount()]
    }

    def show() {
        def systemDocumentTypeInstance = SystemDocumentType.get(params.id)
        if (!systemDocumentTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemDocumentType', params.id)
            redirect(action: 'list')
        } else {
            return [systemDocumentTypeInstance: systemDocumentTypeInstance]
        }
    }

    def delete() {
        def systemDocumentTypeInstance = SystemDocumentType.get(params.id)
        if (systemDocumentTypeInstance) {
            try {
                utilService.deleteWithMessages(systemDocumentTypeInstance, [prefix: 'systemDocumentType.name', code: systemDocumentTypeInstance.code])
                flash.message = utilService.standardMessage('deleted', systemDocumentTypeInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemDocumentTypeInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemDocumentType', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemDocumentTypeInstance = SystemDocumentType.get(params.id)
        if (!systemDocumentTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemDocumentType', params.id)
            redirect(action: 'list')
        } else {
            return [systemDocumentTypeInstance: systemDocumentTypeInstance]
        }
    }

    def update(Long version) {
        def systemDocumentTypeInstance = SystemDocumentType.get(params.id)
        if (systemDocumentTypeInstance) {
            if (version != null && systemDocumentTypeInstance.version > version) {
                systemDocumentTypeInstance.errorMessage(code: 'locking.failure', domain: 'systemDocumentType')
                render(view: 'edit', model: [systemDocumentTypeInstance: systemDocumentTypeInstance])
                return
            }

            def oldCode = systemDocumentTypeInstance.code
            systemDocumentTypeInstance.properties['code', 'name', 'activity', 'metaType', 'analysisIsDebit', 'customerAllocate', 'supplierAllocate'] = params
            if (utilService.saveWithMessages(systemDocumentTypeInstance, [prefix: 'systemDocumentType.name', code: systemDocumentTypeInstance.code, oldCode: oldCode, field: 'name'])) {
                flash.message = utilService.standardMessage('updated', systemDocumentTypeInstance)
                redirect(action: 'show', id: systemDocumentTypeInstance.id)
            } else {
                render(view: 'edit', model: [systemDocumentTypeInstance: systemDocumentTypeInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemDocumentType', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemDocumentTypeInstance = new SystemDocumentType()
        return [systemDocumentTypeInstance: systemDocumentTypeInstance]
    }

    def save() {
        def systemDocumentTypeInstance = new SystemDocumentType()
        systemDocumentTypeInstance.properties['code', 'name', 'activity', 'metaType', 'analysisIsDebit', 'customerAllocate', 'supplierAllocate'] = params
        if (utilService.saveWithMessages(systemDocumentTypeInstance, [prefix: 'systemDocumentType.name', code: systemDocumentTypeInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', systemDocumentTypeInstance)
            redirect(action: 'show', id: systemDocumentTypeInstance.id)
        } else {
            render(view: 'create', model: [systemDocumentTypeInstance: systemDocumentTypeInstance])
        }
    }
}