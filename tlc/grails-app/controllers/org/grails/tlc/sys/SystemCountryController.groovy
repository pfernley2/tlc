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

class SystemCountryController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'flag'].contains(params.sort) ? params.sort : 'code'
        def ddSource = utilService.source('systemRegion.list')
        [systemCountryInstanceList: SystemCountry.selectList(), systemCountryInstanceTotal: SystemCountry.selectCount(), ddSource: ddSource]
    }

    def show() {
        def systemCountryInstance = SystemCountry.get(params.id)
        if (!systemCountryInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemCountry', params.id)
            redirect(action: 'list')
        } else {
            return [systemCountryInstance: systemCountryInstance]
        }
    }

    def delete() {
        def systemCountryInstance = SystemCountry.get(params.id)
        if (systemCountryInstance) {
            try {
                utilService.deleteWithMessages(systemCountryInstance, [prefix: 'country.name', code: systemCountryInstance.code])
                flash.message = utilService.standardMessage('deleted', systemCountryInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemCountryInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemCountry', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemCountryInstance = SystemCountry.get(params.id)
        if (!systemCountryInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemCountry', params.id)
            redirect(action: 'list')
        } else {
            return [systemCountryInstance: systemCountryInstance]
        }
    }

    def update(Long version) {
        def systemCountryInstance = SystemCountry.get(params.id)
        if (systemCountryInstance) {
            if (version != null && systemCountryInstance.version > version) {
                systemCountryInstance.errorMessage(code: 'locking.failure', domain: 'systemCountry')
                render(view: 'edit', model: [systemCountryInstance: systemCountryInstance])
                return
            }

            def oldCode = systemCountryInstance.code
            systemCountryInstance.properties['code', 'name', 'flag', 'currency', 'language', 'region', 'addressFormat'] = params
            if (utilService.saveWithMessages(systemCountryInstance, [prefix: 'country.name', code: systemCountryInstance.code, oldCode: oldCode, field: 'name'])) {
                flash.message = utilService.standardMessage('updated', systemCountryInstance)
                redirect(action: 'show', id: systemCountryInstance.id)
            } else {
                render(view: 'edit', model: [systemCountryInstance: systemCountryInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemCountry', params.id)
            redirect(action: 'edit', id: params.id)
        }
    }

    def create() {
        def systemCountryInstance = new SystemCountry()
        systemCountryInstance.addressFormat = SystemAddressFormat.findByCode('default')
        systemCountryInstance.region = utilService.reSource('systemRegion.list')
        return [systemCountryInstance: systemCountryInstance]
    }

    def save() {
        def systemCountryInstance = new SystemCountry()
        systemCountryInstance.properties['code', 'name', 'flag', 'currency', 'language', 'region', 'addressFormat'] = params
        if (utilService.saveWithMessages(systemCountryInstance, [prefix: 'country.name', code: systemCountryInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', systemCountryInstance)
            redirect(action: 'show', id: systemCountryInstance.id)
        } else {
            render(view: 'create', model: [systemCountryInstance: systemCountryInstance])
        }
    }
}