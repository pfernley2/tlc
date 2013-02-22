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

class SystemPageHelpController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin', display: 'any']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'locale', 'text'].contains(params.sort) ? params.sort : 'code'
        [systemPageHelpInstanceList: SystemPageHelp.selectList(), systemPageHelpInstanceTotal: SystemPageHelp.selectCount()]
    }

    def show() {
        def systemPageHelpInstance = SystemPageHelp.get(params.id)
        if (!systemPageHelpInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemPageHelp', params.id)
            redirect(action: 'list')
        } else {
            return [systemPageHelpInstance: systemPageHelpInstance]
        }
    }

    def delete() {
        def systemPageHelpInstance = SystemPageHelp.get(params.id)
        if (systemPageHelpInstance) {
            try {
                systemPageHelpInstance.delete(flush: true)
                utilService.cacheService.clearThis('pageHelp')
                flash.message = utilService.standardMessage('deleted', systemPageHelpInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemPageHelpInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemPageHelp', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemPageHelpInstance = SystemPageHelp.get(params.id)
        if (!systemPageHelpInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemPageHelp', params.id)
            redirect(action: 'list')
        } else {
            return [systemPageHelpInstance: systemPageHelpInstance]
        }
    }

    def update(Long version) {
        def systemPageHelpInstance = SystemPageHelp.get(params.id)
        if (systemPageHelpInstance) {
            if (version != null && systemPageHelpInstance.version > version) {
                systemPageHelpInstance.errorMessage(code: 'locking.failure', domain: 'systemPageHelp')
                render(view: 'edit', model: [systemPageHelpInstance: systemPageHelpInstance])
                return
            }

            def oldCode = systemPageHelpInstance.code
            def oldLocale = systemPageHelpInstance.locale
            systemPageHelpInstance.properties['code', 'locale', 'text'] = params
            def valid = !systemPageHelpInstance.hasErrors()
            if (valid && systemPageHelpInstance.locale != oldLocale && !localeIsValid(systemPageHelpInstance.locale)) {
                systemPageHelpInstance.errorMessage(field: 'locale', code: 'systemMessage.bad.locale', default: 'Invalid locale')
                valid = false
            }

            if (valid) valid = systemPageHelpInstance.saveThis()

            if (valid) {
                utilService.cacheService.clearThis('pageHelp')
                flash.message = utilService.standardMessage('updated', systemPageHelpInstance)
                redirect(action: 'show', id: systemPageHelpInstance.id)
            } else {
                render(view: 'edit', model: [systemPageHelpInstance: systemPageHelpInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemPageHelp', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemPageHelpInstance = new SystemPageHelp()
        return [systemPageHelpInstance: systemPageHelpInstance]
    }

    def save() {
        def systemPageHelpInstance = new SystemPageHelp()
        systemPageHelpInstance.properties['code', 'locale', 'text'] = params
        def valid = !systemPageHelpInstance.hasErrors()
        if (valid && !localeIsValid(systemPageHelpInstance.locale)) {
            systemPageHelpInstance.errorMessage(field: 'locale', code: 'systemMessage.bad.locale', default: 'Invalid locale')
            valid = false
        }

        if (valid) valid = systemPageHelpInstance.saveThis()
        if (valid) {
            utilService.cacheService.clearThis('pageHelp')
            flash.message = utilService.standardMessage('created', systemPageHelpInstance)
            redirect(action: 'show', id: systemPageHelpInstance.id)
        } else {
            render(view: 'create', model: [systemPageHelpInstance: systemPageHelpInstance])
        }
    }

    def display() {
        def code = params.code
        def model = [:]
        if (code) model.lines = utilService.getPageHelp(code)
        [displayInstance: model]
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private localeIsValid(locale) {
        if (locale) {
            if (locale == '*') return true

            if (locale.length() == 2) {
                return (SystemLanguage.countByCode(locale) == 1)
            }

            if (locale.length() == 4) {
                return (SystemLanguage.countByCode(locale.substring(0, 2)) == 1 && SystemCountry.countByCode(locale.substring(2)) == 1)
            }
        }

        return false
    }
}