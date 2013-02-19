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

class SystemActionController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['appController', 'appAction'].contains(params.sort) ? params.sort : 'appController'
        def ddSource = utilService.source('systemActivity.list')
        [systemActionInstanceList: SystemAction.selectList(), systemActionInstanceTotal: SystemAction.selectCount(), ddSource: ddSource]
    }

    def show() {
        def systemActionInstance = SystemAction.get(params.id)
        if (!systemActionInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemAction', params.id)
            redirect(action: 'list')
        } else {
            return [systemActionInstance: systemActionInstance]
        }
    }

    def delete() {
        def systemActionInstance = SystemAction.get(params.id)
        if (systemActionInstance) {
            try {
                systemActionInstance.delete(flush: true)
                utilService.cacheService.resetThis('actionActivity', CacheService.COMPANY_INSENSITIVE, "${systemActionInstance.appController}.${systemActionInstance.appAction}")
                flash.message = utilService.standardMessage('deleted', systemActionInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemActionInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemAction', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemActionInstance = SystemAction.get(params.id)
        if (!systemActionInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemAction', params.id)
            redirect(action: 'list')
        } else {
            return [systemActionInstance: systemActionInstance]
        }
    }

    def update(Long version) {
        def systemActionInstance = SystemAction.get(params.id)
        if (systemActionInstance) {
            if (version != null && systemActionInstance.version > version) {
                systemActionInstance.errorMessage(code: 'locking.failure', domain: 'systemAction')
                render(view: 'edit', model: [systemActionInstance: systemActionInstance])
                return
            }

            def oldController = systemActionInstance.appController
            def oldAction = systemActionInstance.appAction
            def oldActivityId = systemActionInstance.activity.id
            systemActionInstance.properties['appController', 'appAction', 'activity'] = params
            if (!systemActionInstance.hasErrors() && systemActionInstance.saveThis()) {
                if (systemActionInstance.appController != oldController || systemActionInstance.appAction != oldAction || systemActionInstance.activity.id != oldActivityId) {
                    utilService.cacheService.resetThis('actionActivity', CacheService.COMPANY_INSENSITIVE, "${oldController}.${oldAction}")
                    utilService.cacheService.resetThis('actionActivity', CacheService.COMPANY_INSENSITIVE, "${systemActionInstance.appController}.${systemActionInstance.appAction}")
                }

                flash.message = utilService.standardMessage('updated', systemActionInstance)
                redirect(action: 'show', id: systemActionInstance.id)
            } else {
                render(view: 'edit', model: [systemActionInstance: systemActionInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemAction', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemActionInstance = new SystemAction()
        systemActionInstance.activity = utilService.reSource('systemActivity.list')
        return [systemActionInstance: systemActionInstance]
    }

    def save() {
        def systemActionInstance = new SystemAction()
        systemActionInstance.properties['appController', 'appAction', 'activity'] = params
        if (!systemActionInstance.hasErrors() && systemActionInstance.saveThis()) {
            utilService.cacheService.resetThis('actionActivity', CacheService.COMPANY_INSENSITIVE, "${systemActionInstance.appController}.${systemActionInstance.appAction}")
            flash.message = utilService.standardMessage('created', systemActionInstance)
            redirect(action: 'show', id: systemActionInstance.id)
        } else {
            render(view: 'create', model: [systemActionInstance: systemActionInstance])
        }
    }
}