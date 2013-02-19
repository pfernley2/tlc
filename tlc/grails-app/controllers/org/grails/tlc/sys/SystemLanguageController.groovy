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

class SystemLanguageController {

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
        [systemLanguageInstanceList: SystemLanguage.selectList(), systemLanguageInstanceTotal: SystemLanguage.selectCount()]
    }

    def show() {
        def systemLanguageInstance = SystemLanguage.get(params.id)
        if (!systemLanguageInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemLanguage', params.id)
            redirect(action: 'list')
        } else {
            return [systemLanguageInstance: systemLanguageInstance]
        }
    }

    def delete() {
        def systemLanguageInstance = SystemLanguage.get(params.id)
        if (systemLanguageInstance) {
            try {
                utilService.deleteWithMessages(systemLanguageInstance, [prefix: 'language.name', code: systemLanguageInstance.code])
                utilService.cacheService.clearThis('message')
                flash.message = utilService.standardMessage('deleted', systemLanguageInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemLanguageInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemLanguage', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemLanguageInstance = SystemLanguage.get(params.id)
        if (!systemLanguageInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemLanguage', params.id)
            redirect(action: 'list')
        } else {
            return [systemLanguageInstance: systemLanguageInstance]
        }
    }

    def update(Long version) {
        def systemLanguageInstance = SystemLanguage.get(params.id)
        if (systemLanguageInstance) {
            if (version != null && systemLanguageInstance.version > version) {
                systemLanguageInstance.errorMessage(code: 'locking.failure', domain: 'systemLanguage')
                render(view: 'edit', model: [systemLanguageInstance: systemLanguageInstance])
                return
            }

            def oldCode = systemLanguageInstance.code
            systemLanguageInstance.properties['code', 'name'] = params
            if (utilService.saveWithMessages(systemLanguageInstance, [prefix: 'language.name', code: systemLanguageInstance.code, oldCode: oldCode, field: 'name'])) {
                utilService.cacheService.clearThis('message')
                flash.message = utilService.standardMessage('updated', systemLanguageInstance)
                redirect(action: 'show', id: systemLanguageInstance.id)
            } else {
                render(view: 'edit', model: [systemLanguageInstance: systemLanguageInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemLanguage', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        return [systemLanguageInstance: new SystemLanguage()]
    }

    def save() {
        def systemLanguageInstance = new SystemLanguage()
        systemLanguageInstance.properties['code', 'name'] = params
        if (utilService.saveWithMessages(systemLanguageInstance, [prefix: 'language.name', code: systemLanguageInstance.code, field: 'name'])) {
            utilService.cacheService.clearThis('message')
            flash.message = utilService.standardMessage('created', systemLanguageInstance)
            redirect(action: 'show', id: systemLanguageInstance.id)
        } else {
            render(view: 'create', model: [systemLanguageInstance: systemLanguageInstance])
        }
    }
}