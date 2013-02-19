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

import org.grails.tlc.obj.SystemConversionTest

class SystemConversionController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', testing: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'preAddition', 'multiplier', 'postAddition'].contains(params.sort) ? params.sort : 'code'
        [systemConversionInstanceList: SystemConversion.selectList(), systemConversionInstanceTotal: SystemConversion.selectCount()]
    }

    def show() {
        def systemConversionInstance = SystemConversion.get(params.id)
        if (!systemConversionInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemConversion', params.id)
            redirect(action: 'list')
        } else {
            return [systemConversionInstance: systemConversionInstance]
        }
    }

    def delete() {
        def systemConversionInstance = SystemConversion.get(params.id)
        if (systemConversionInstance) {
            try {
                utilService.deleteWithMessages(systemConversionInstance, [prefix: 'conversion.name', code: systemConversionInstance.code])
                utilService.cacheService.resetThis('conversion', 0L, systemConversionInstance.source.code)
                utilService.cacheService.resetThis('conversion', 0L, systemConversionInstance.target.code)
                flash.message = utilService.standardMessage('deleted', systemConversionInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemConversionInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemConversion', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemConversionInstance = SystemConversion.get(params.id)
        if (!systemConversionInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemConversion', params.id)
            redirect(action: 'list')
        } else {
            return [systemConversionInstance: systemConversionInstance]
        }
    }

    def update(Long version) {
        def systemConversionInstance = SystemConversion.get(params.id)
        if (systemConversionInstance) {
            if (version != null && systemConversionInstance.version > version) {
                systemConversionInstance.errorMessage(code: 'locking.failure', domain: 'systemConversion')
                render(view: 'edit', model: [systemConversionInstance: systemConversionInstance])
                return
            }

            def oldCode = systemConversionInstance.code
            systemConversionInstance.properties['code', 'name', 'preAddition', 'multiplier', 'postAddition', 'target', 'source'] = params
            if (utilService.saveWithMessages(systemConversionInstance, [prefix: 'conversion.name', code: systemConversionInstance.code, oldCode: oldCode, field: 'name'])) {
                utilService.cacheService.resetThis('conversion', 0L, systemConversionInstance.source.code)
                utilService.cacheService.resetThis('conversion', 0L, systemConversionInstance.target.code)
                flash.message = utilService.standardMessage('updated', systemConversionInstance)
                redirect(action: 'show', id: systemConversionInstance.id)
            } else {
                render(view: 'edit', model: [systemConversionInstance: systemConversionInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemConversion', params.id)
            redirect(action: 'edit', id: params.id)
        }
    }

    def create() {
        return [systemConversionInstance: new SystemConversion()]
    }

    def save() {
        def systemConversionInstance = new SystemConversion()
        systemConversionInstance.properties['code', 'name', 'preAddition', 'multiplier', 'postAddition', 'target', 'source'] = params
        if (utilService.saveWithMessages(systemConversionInstance, [prefix: 'conversion.name', code: systemConversionInstance.code, field: 'name'])) {
            utilService.cacheService.resetThis('conversion', 0L, systemConversionInstance.source.code)
            utilService.cacheService.resetThis('conversion', 0L, systemConversionInstance.target.code)
            flash.message = utilService.standardMessage('created', systemConversionInstance)
            redirect(action: 'show', id: systemConversionInstance.id)
        } else {
            render(view: 'create', model: [systemConversionInstance: systemConversionInstance])
        }
    }

    def test() {
        return [testInstance: new SystemConversionTest()]
    }

    def testing(SystemConversionTest testInstance) {
        if (!testInstance.hasErrors()) {
            if (testInstance.quantity == null) testInstance.quantity = 0
            try {
                def rslt = utilService.convertUnit(testInstance.fromUnit, testInstance.toUnit, testInstance.quantity, 10)
                testInstance.result = (rslt == null) ? '' : rslt.toPlainString()
            } catch (IllegalArgumentException ex) {
                testInstance.result = ex.message
            }
        }

        render(view: 'test', model: [testInstance: testInstance])
    }
}