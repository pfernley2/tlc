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

class SystemScaleController {

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
        [systemScaleInstanceList: SystemScale.selectList(), systemScaleInstanceTotal: SystemScale.selectCount()]
    }

    def show() {
        def systemScaleInstance = SystemScale.get(params.id)
        if (!systemScaleInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemScale', params.id)
            redirect(action: 'list')
        } else {
            return [systemScaleInstance: systemScaleInstance]
        }
    }

    def delete() {
        def systemScaleInstance = SystemScale.get(params.id)
        if (systemScaleInstance) {
            try {
                utilService.deleteWithMessages(systemScaleInstance, [prefix: 'scale.name', code: systemScaleInstance.code])
                flash.message = utilService.standardMessage('deleted', systemScaleInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemScaleInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemScale', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemScaleInstance = SystemScale.get(params.id)
        if (!systemScaleInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemScale', params.id)
            redirect(action: 'list')
        } else {
            return [systemScaleInstance: systemScaleInstance]
        }
    }

    def update(Long version) {
        def systemScaleInstance = SystemScale.get(params.id)
        if (systemScaleInstance) {
            if (version != null && systemScaleInstance.version > version) {
                systemScaleInstance.errorMessage(code: 'locking.failure', domain: 'systemScale')
                render(view: 'edit', model: [systemScaleInstance: systemScaleInstance])
                return
            }

            def oldCode = systemScaleInstance.code
            systemScaleInstance.properties['code', 'name'] = params
            if (utilService.saveWithMessages(systemScaleInstance, [prefix: 'scale.name', code: systemScaleInstance.code, oldCode: oldCode, field: 'name'])) {
                flash.message = utilService.standardMessage('updated', systemScaleInstance)
                redirect(action: 'show', id: systemScaleInstance.id)
            } else {
                render(view: 'edit', model: [systemScaleInstance: systemScaleInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemScale', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        return [systemScaleInstance: new SystemScale()]
    }

    def save() {
        def systemScaleInstance = new SystemScale()
        systemScaleInstance.properties['code', 'name'] = params
        if (utilService.saveWithMessages(systemScaleInstance, [prefix: 'scale.name', code: systemScaleInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', systemScaleInstance)
            redirect(action: 'show', id: systemScaleInstance.id)
        } else {
            render(view: 'create', model: [systemScaleInstance: systemScaleInstance])
        }
    }
}