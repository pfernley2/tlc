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

import org.grails.tlc.obj.ConversionTest

class ConversionController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', testing: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'preAddition', 'multiplier', 'postAddition'].contains(params.sort) ? params.sort : 'code'
        [conversionInstanceList: Conversion.selectList(securityCode: utilService.currentCompany().securityCode), conversionInstanceTotal: Conversion.selectCount()]
    }

    def show() {
        def conversionInstance = Conversion.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!conversionInstance) {
            flash.message = utilService.standardMessage('not.found', 'conversion', params.id)

            redirect(action: 'list')
        } else {
            return [conversionInstance: conversionInstance]
        }
    }

    def delete() {
        def conversionInstance = Conversion.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (conversionInstance) {
            try {
                utilService.deleteWithMessages(conversionInstance, [prefix: 'conversion.name', code: conversionInstance.code])
                utilService.cacheService.resetThis('conversion', conversionInstance.securityCode, conversionInstance.source.code)
                utilService.cacheService.resetThis('conversion', conversionInstance.securityCode, conversionInstance.target.code)
                flash.message = utilService.standardMessage('deleted', conversionInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', conversionInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'conversion', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def conversionInstance = Conversion.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!conversionInstance) {
            flash.message = utilService.standardMessage('not.found', 'conversion', params.id)
            redirect(action: 'list')
        } else {
            def unitList = Unit.findAllBySecurityCode(utilService.currentCompany().securityCode)
            return [conversionInstance: conversionInstance, unitList: unitList]
        }
    }

    def update(Long version) {
        def conversionInstance = Conversion.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (conversionInstance) {
            if (version != null && conversionInstance.version > version) {
                conversionInstance.errorMessage(code: 'locking.failure', domain: 'conversion')
                def unitList = Unit.findAllBySecurityCode(utilService.currentCompany().securityCode)
                render(view: 'edit', model: [conversionInstance: conversionInstance, unitList: unitList])
                return
            }

            def oldCode = conversionInstance.code
            conversionInstance.properties['code', 'name', 'preAddition', 'multiplier', 'postAddition', 'target', 'source'] = params
            utilService.verify(conversionInstance, ['target', 'source'])             // Ensure correct references
            if (utilService.saveWithMessages(conversionInstance, [prefix: 'conversion.name', code: conversionInstance.code, oldCode: oldCode, field: 'name'])) {
                utilService.cacheService.resetThis('conversion', conversionInstance.securityCode, conversionInstance.source.code)
                utilService.cacheService.resetThis('conversion', conversionInstance.securityCode, conversionInstance.target.code)
                flash.message = utilService.standardMessage('updated', conversionInstance)
                redirect(action: 'show', id: conversionInstance.id)
            } else {
                def unitList = Unit.findAllBySecurityCode(utilService.currentCompany().securityCode)
                render(view: 'edit', model: [conversionInstance: conversionInstance, unitList: unitList])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'conversion', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def conversionInstance = new Conversion()
        def unitList = Unit.findAllBySecurityCode(utilService.currentCompany().securityCode)
        return [conversionInstance: conversionInstance, unitList: unitList]
    }

    def save() {
        def conversionInstance = new Conversion()
        conversionInstance.properties['code', 'name', 'preAddition', 'multiplier', 'postAddition', 'target', 'source'] = params
        utilService.verify(conversionInstance, ['target', 'source'])             // Ensure correct references
        if (utilService.saveWithMessages(conversionInstance, [prefix: 'conversion.name', code: conversionInstance.code, field: 'name'])) {
            utilService.cacheService.resetThis('conversion', conversionInstance.securityCode, conversionInstance.source.code)
            utilService.cacheService.resetThis('conversion', conversionInstance.securityCode, conversionInstance.target.code)
            flash.message = utilService.standardMessage('created', conversionInstance)
            redirect(action: 'show', id: conversionInstance.id)
        } else {
            def unitList = Unit.findAllBySecurityCode(utilService.currentCompany().securityCode)
            render(view: 'create', model: [conversionInstance: conversionInstance, unitList: unitList])
        }
    }

    def test() {
        def unitList = Unit.findAllBySecurityCode(utilService.currentCompany().securityCode)
        return [testInstance: new ConversionTest(), unitList: unitList]
    }

    def testing(ConversionTest testInstance) {
        if (!testInstance.hasErrors()) {
            if (testInstance.quantity == null) testInstance.quantity = 0
            try {
                def rslt = utilService.convertUnit(testInstance.fromUnit, testInstance.toUnit, testInstance.quantity, 10)
                testInstance.result = (rslt == null) ? '' : rslt.toPlainString()
            } catch (IllegalArgumentException ex) {
                testInstance.result = ex.message
            }
        }

        def unitList = Unit.findAllBySecurityCode(utilService.currentCompany().securityCode)
        render(view: 'test', model: [testInstance: testInstance, unitList: unitList])
    }
}