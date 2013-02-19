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

class SystemRegionController {

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
        def ddSource = utilService.source('systemGeo.list')
        [systemRegionInstanceList: SystemRegion.selectList(), systemRegionInstanceTotal: SystemRegion.selectCount(), ddSource: ddSource]
    }

    def show() {
        def systemRegionInstance = SystemRegion.get(params.id)
        if (!systemRegionInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemRegion', params.id)
            redirect(action: 'list')
        } else {
            return [systemRegionInstance: systemRegionInstance]
        }
    }

    def delete() {
        def systemRegionInstance = SystemRegion.get(params.id)
        if (systemRegionInstance) {
            try {
                utilService.deleteWithMessages(systemRegionInstance, [prefix: 'region.name', code: systemRegionInstance.code])
                flash.message = utilService.standardMessage('deleted', systemRegionInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemRegionInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemRegion', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemRegionInstance = SystemRegion.get(params.id)
        if (!systemRegionInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemRegion', params.id)
            redirect(action: 'list')
        } else {
            return [systemRegionInstance: systemRegionInstance]
        }
    }

    def update(Long version) {
        def systemRegionInstance = SystemRegion.get(params.id)
        if (systemRegionInstance) {
            if (version != null && systemRegionInstance.version > version) {
                systemRegionInstance.errorMessage(code: 'locking.failure', domain: 'systemRegion')
                render(view: 'edit', model: [systemRegionInstance: systemRegionInstance])
                return
            }

            def oldCode = systemRegionInstance.code
            systemRegionInstance.properties['code', 'name', 'geo'] = params
            if (utilService.saveWithMessages(systemRegionInstance, [prefix: 'region.name', code: systemRegionInstance.code, oldCode: oldCode, field: 'name'])) {
                flash.message = utilService.standardMessage('updated', systemRegionInstance)
                redirect(action: 'show', id: systemRegionInstance.id)
            } else {
                render(view: 'edit', model: [systemRegionInstance: systemRegionInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemRegion', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemRegionInstance = new SystemRegion()
        systemRegionInstance.geo = utilService.reSource('systemGeo.list')
        return [systemRegionInstance: systemRegionInstance]
    }

    def save() {
        def systemRegionInstance = new SystemRegion()
        systemRegionInstance.properties['code', 'name', 'geo'] = params
        if (utilService.saveWithMessages(systemRegionInstance, [prefix: 'region.name', code: systemRegionInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', systemRegionInstance)
            redirect(action: 'show', id: systemRegionInstance.id)
        } else {
            render(view: 'create', model: [systemRegionInstance: systemRegionInstance])
        }
    }
}