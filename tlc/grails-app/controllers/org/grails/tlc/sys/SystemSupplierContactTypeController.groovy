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

class SystemSupplierContactTypeController {

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
        [systemSupplierContactTypeInstanceList: SystemSupplierContactType.selectList(), systemSupplierContactTypeInstanceTotal: SystemSupplierContactType.selectCount()]
    }

    def show() {
        def systemSupplierContactTypeInstance = SystemSupplierContactType.get(params.id)
        if (!systemSupplierContactTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemSupplierContactType', params.id)
            redirect(action: 'list')
        } else {
            return [systemSupplierContactTypeInstance: systemSupplierContactTypeInstance]
        }
    }

    def delete() {
        def systemSupplierContactTypeInstance = SystemSupplierContactType.get(params.id)
        if (systemSupplierContactTypeInstance) {
            try {
                utilService.deleteWithMessages(systemSupplierContactTypeInstance, [prefix: 'supplierContactType.name', code: systemSupplierContactTypeInstance.code])
                flash.message = utilService.standardMessage('deleted', systemSupplierContactTypeInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemSupplierContactTypeInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemSupplierContactType', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemSupplierContactTypeInstance = SystemSupplierContactType.get(params.id)
        if (!systemSupplierContactTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemSupplierContactType', params.id)
            redirect(action: 'list')
        } else {
            return [systemSupplierContactTypeInstance: systemSupplierContactTypeInstance]
        }
    }

    def update(Long version) {
        def systemSupplierContactTypeInstance = SystemSupplierContactType.get(params.id)
        if (systemSupplierContactTypeInstance) {
            if (version != null && systemSupplierContactTypeInstance.version > version) {
                systemSupplierContactTypeInstance.errorMessage(code: 'locking.failure', domain: 'systemSupplierContactType')
                render(view: 'edit', model: [systemSupplierContactTypeInstance: systemSupplierContactTypeInstance])
                return
            }

            def oldCode = systemSupplierContactTypeInstance.code
            systemSupplierContactTypeInstance.properties['code', 'name'] = params
            if (utilService.saveWithMessages(systemSupplierContactTypeInstance, [prefix: 'supplierContactType.name', code: systemSupplierContactTypeInstance.code, oldCode: oldCode, field: 'name'])) {
                flash.message = utilService.standardMessage('updated', systemSupplierContactTypeInstance)
                redirect(action: 'show', id: systemSupplierContactTypeInstance.id)
            } else {
                render(view: 'edit', model: [systemSupplierContactTypeInstance: systemSupplierContactTypeInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemSupplierContactType', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemSupplierContactTypeInstance = new SystemSupplierContactType()
        return [systemSupplierContactTypeInstance: systemSupplierContactTypeInstance]
    }

    def save() {
        def systemSupplierContactTypeInstance = new SystemSupplierContactType()
        systemSupplierContactTypeInstance.properties['code', 'name'] = params
        if (utilService.saveWithMessages(systemSupplierContactTypeInstance, [prefix: 'supplierContactType.name', code: systemSupplierContactTypeInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', systemSupplierContactTypeInstance)
            redirect(action: 'show', id: systemSupplierContactTypeInstance.id)
        } else {
            render(view: 'create', model: [systemSupplierContactTypeInstance: systemSupplierContactTypeInstance])
        }
    }
}