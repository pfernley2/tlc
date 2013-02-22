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

class SystemActivityController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', link: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'systemOnly'].contains(params.sort) ? params.sort : 'code'
        def ddSource = utilService.source('systemRole.list')
        [systemActivityInstanceList: SystemActivity.selectList(), systemActivityInstanceTotal: SystemActivity.selectCount(), ddSource: ddSource]
    }

    def show() {
        def systemActivityInstance = SystemActivity.get(params.id)
        if (!systemActivityInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemActivity', params.id)
            redirect(action: 'list')
        } else {
            return [systemActivityInstance: systemActivityInstance]
        }
    }

    def delete() {
        def systemActivityInstance = SystemActivity.get(params.id)
        if (systemActivityInstance) {
            try {
                systemActivityInstance.delete(flush: true)
                utilService.cacheService.resetThis('userActivity', utilService.cacheService.COMPANY_INSENSITIVE, systemActivityInstance.code)
                flash.message = utilService.standardMessage('deleted', systemActivityInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemActivityInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemActivity', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemActivityInstance = SystemActivity.get(params.id)
        if (!systemActivityInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemActivity', params.id)
            redirect(action: 'list')
        } else {
            return [systemActivityInstance: systemActivityInstance]
        }
    }

    def update(Long version) {
        def systemActivityInstance = SystemActivity.get(params.id)
        if (systemActivityInstance) {
            if (version != null && systemActivityInstance.version > version) {
                systemActivityInstance.errorMessage(code: 'locking.failure', domain: 'systemActivity')
                render(view: 'edit', model: [systemActivityInstance: systemActivityInstance])
                return
            }

            def oldCode = systemActivityInstance.code
            systemActivityInstance.properties['code', 'systemOnly'] = params
            if (!systemActivityInstance.hasErrors() && systemActivityInstance.saveThis()) {
                if (systemActivityInstance.code != oldCode) {
                    utilService.cacheService.resetThis('userActivity', utilService.cacheService.COMPANY_INSENSITIVE, oldCode)
                    utilService.cacheService.resetThis('userActivity', utilService.cacheService.COMPANY_INSENSITIVE, systemActivityInstance.code)
                }

                flash.message = utilService.standardMessage('updated', systemActivityInstance)
                redirect(action: 'show', id: systemActivityInstance.id)
            } else {
                render(view: 'edit', model: [systemActivityInstance: systemActivityInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemActivity', params.id)
            redirect(action: 'edit', id: params.id)
        }
    }

    def create() {
        def systemActivityInstance = new SystemActivity()
        systemActivityInstance.properties['code', 'systemOnly'] = params
        return [systemActivityInstance: systemActivityInstance]
    }

    def save() {
        def systemActivityInstance = new SystemActivity()
        systemActivityInstance.properties['code', 'systemOnly'] = params
        if (!systemActivityInstance.hasErrors() && systemActivityInstance.saveThis()) {
            utilService.cacheService.resetThis('userActivity', utilService.cacheService.COMPANY_INSENSITIVE, systemActivityInstance.code)
            flash.message = utilService.standardMessage('created', systemActivityInstance)
            redirect(action: 'show', id: systemActivityInstance.id)
        } else {
            render(view: 'create', model: [systemActivityInstance: systemActivityInstance])
        }
    }

    def links() {
        def ddSource = utilService.reSource('systemRole.list')
        def allActivities = (ddSource.code == 'companyAdmin') ?
                SystemActivity.findAllBySystemOnlyAndCodeNotEqual(false, 'systran') :
                SystemActivity.findAllBySystemOnly(false)
        [allActivities: allActivities, roleActivities: SystemActivity.selectList(action: 'list'), ddSource: ddSource]
    }

    def link() {
        def ddSource = utilService.reSource('systemRole.list')
        def roleActivities = SystemActivity.selectList(action: 'list')
        def activities = []
        if (params.linkages) activities = params.linkages instanceof String ? [params.linkages.toLong()] : params.linkages*.toLong() as List
        def modified = false
        for (activity in roleActivities) {
            if (!activities.contains(activity.id)) {
                ddSource.removeFromActivities(activity)
                modified = true
            }
        }

        for (activity in activities) {
            def found = false
            for (a in roleActivities) {
                if (a.id == activity) {
                    found = true
                    break
                }
            }

            if (!found) {
                ddSource.addToActivities(SystemActivity.get(activity))
                modified = true
            }
        }

        if (modified) {
            if (ddSource.save(flush: true)) {      // With deep validation
                utilService.cacheService.clearThis('userActivity')
                flash.message = message(code: 'generic.links.changed', default: 'The links were successfully updated')
            } else {
                flash.message = message(code: 'generic.links.failed', default: 'Error updating the links')
            }
        } else {
            flash.message = message(code: 'generic.links.unchanged', default: 'No links were changed')
        }

        redirect(action: 'list')
    }
}