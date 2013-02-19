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

class SystemGeoController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code'].contains(params.sort) ? params.sort : 'code'
        [systemGeoInstanceList: SystemGeo.selectList(), systemGeoInstanceTotal: SystemGeo.selectCount()]
    }

    def show() {
        def systemGeoInstance = SystemGeo.get(params.id)
        if (!systemGeoInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemGeo', params.id)
            redirect(action: 'list')
        } else {
            return [systemGeoInstance: systemGeoInstance]
        }
    }

    def delete() {
        def systemGeoInstance = SystemGeo.get(params.id)
        if (systemGeoInstance) {
            try {
                utilService.deleteWithMessages(systemGeoInstance, [prefix: 'geo.name', code: systemGeoInstance.code])
                flash.message = utilService.standardMessage('deleted', systemGeoInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemGeoInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemGeo', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemGeoInstance = SystemGeo.get(params.id)
        if (!systemGeoInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemGeo', params.id)
            redirect(action: 'list')
        } else {
            return [systemGeoInstance: systemGeoInstance]
        }
    }

    def update(Long version) {
        def systemGeoInstance = SystemGeo.get(params.id)
        if (systemGeoInstance) {
            if (version != null && systemGeoInstance.version > version) {
                systemGeoInstance.errorMessage(code: 'locking.failure', domain: 'systemGeo')
                render(view: 'edit', model: [systemGeoInstance: systemGeoInstance])
                return
            }

            def oldCode = systemGeoInstance.code
            systemGeoInstance.properties['code', 'name'] = params
            if (utilService.saveWithMessages(systemGeoInstance, [prefix: 'geo.name', code: systemGeoInstance.code, oldCode: oldCode, field: 'name'])) {
                flash.message = utilService.standardMessage('updated', systemGeoInstance)
                redirect(action: 'show', id: systemGeoInstance.id)
            } else {
                render(view: 'edit', model: [systemGeoInstance: systemGeoInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemGeo', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        return [systemGeoInstance: new SystemGeo()]
    }

    def save() {
        def systemGeoInstance = new SystemGeo()
        systemGeoInstance.properties['code', 'name'] = params
        if (utilService.saveWithMessages(systemGeoInstance, [prefix: 'geo.name', code: systemGeoInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', systemGeoInstance)
            redirect(action: 'show', id: systemGeoInstance.id)
        } else {
            render(view: 'create', model: [systemGeoInstance: systemGeoInstance])
        }
    }
}