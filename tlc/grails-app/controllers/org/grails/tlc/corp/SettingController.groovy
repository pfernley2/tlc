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
package org.grails.tlc.corp

class SettingController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'dataType', 'dataScale', 'value'].contains(params.sort) ? params.sort : 'code'
        [settingInstanceList: Setting.selectList(company: utilService.currentCompany()), settingInstanceTotal: Setting.selectCount()]
    }

    def show() {
        def settingInstance = Setting.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!settingInstance) {
            flash.message = utilService.standardMessage('not.found', 'setting', params.id)
            redirect(action: 'list')
        } else {
            return [settingInstance: settingInstance]
        }
    }

    def delete() {
        def settingInstance = Setting.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (settingInstance) {
            try {
                settingInstance.delete(flush: true)
                utilService.cacheService.resetThis('setting', settingInstance.securityCode, settingInstance.code)
                flash.message = utilService.standardMessage('deleted', settingInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', settingInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'setting', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def settingInstance = Setting.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!settingInstance) {
            flash.message = utilService.standardMessage('not.found', 'setting', params.id)
            redirect(action: 'list')
        } else {
            return [settingInstance: settingInstance]
        }
    }

    def update(Long version) {
        def settingInstance = Setting.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (settingInstance) {
            if (version != null && settingInstance.version > version) {
                settingInstance.errorMessage(code: 'locking.failure', domain: 'setting')
                render(view: 'edit', model: [settingInstance: settingInstance])
                return
            }

            def oldCode = settingInstance.code
            settingInstance.properties['code', 'dataType', 'dataScale', 'value'] = params
            if (!settingInstance.hasErrors() && settingInstance.saveThis()) {
                utilService.cacheService.resetThis('setting', settingInstance.securityCode, oldCode)
                if (settingInstance.code != oldCode) utilService.cacheService.resetThis('setting', settingInstance.securityCode, settingInstance.code)
                flash.message = utilService.standardMessage('updated', settingInstance)
                redirect(action: 'show', id: settingInstance.id)
            } else {
                render(view: 'edit', model: [settingInstance: settingInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'setting', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def settingInstance = new Setting()
        settingInstance.company = utilService.currentCompany()   // Ensure correct company
        return [settingInstance: settingInstance]
    }

    def save() {
        def settingInstance = new Setting()
        settingInstance.properties['code', 'dataType', 'dataScale', 'value'] = params
        settingInstance.company = utilService.currentCompany()   // Ensure correct company
        if (!settingInstance.hasErrors() && settingInstance.saveThis()) {
            utilService.cacheService.resetThis('setting', settingInstance.securityCode, settingInstance.code)
            flash.message = utilService.standardMessage('created', settingInstance)
            redirect(action: 'show', id: settingInstance.id)
        } else {
            render(view: 'create', model: [settingInstance: settingInstance])
        }
    }
}