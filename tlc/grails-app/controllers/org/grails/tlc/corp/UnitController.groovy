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

class UnitController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'multiplier'].contains(params.sort) ? params.sort : 'code'
        def ddSource = utilService.source('measure.list')
        if (!ddSource) ddSource = utilService.source('scale.list')
        [unitInstanceList: Unit.selectList(securityCode: utilService.currentCompany().securityCode), unitInstanceTotal: Unit.selectCount(), ddSource: ddSource]
    }

    def show() {
        def unitInstance = Unit.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!unitInstance) {
            flash.message = utilService.standardMessage('not.found', 'unit', params.id)
            redirect(action: 'list')
        } else {
            return [unitInstance: unitInstance]
        }
    }

    def delete() {
        def unitInstance = Unit.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (unitInstance) {
            try {
                utilService.deleteWithMessages(unitInstance, [prefix: 'unit.name', code: unitInstance.code])
                utilService.cacheService.resetThis('conversion', unitInstance.securityCode, unitInstance.code)
                flash.message = utilService.standardMessage('deleted', unitInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', unitInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'unit', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def unitInstance = Unit.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!unitInstance) {
            flash.message = utilService.standardMessage('not.found', 'unit', params.id)
            redirect(action: 'list')
        } else {
            def measureList = Measure.findAllByCompany(utilService.currentCompany())
            def scaleList = Scale.findAllByCompany(utilService.currentCompany())
            return [unitInstance: unitInstance, measureList: measureList, scaleList: scaleList]
        }
    }

    def update(Long version) {
        def unitInstance = Unit.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (unitInstance) {
            if (version != null && unitInstance.version > version) {
                unitInstance.errorMessage(code: 'locking.failure', domain: 'unit')
                def measureList = Measure.findAllByCompany(utilService.currentCompany())
                def scaleList = Scale.findAllByCompany(utilService.currentCompany())
                render(view: 'edit', model: [unitInstance: unitInstance, measureList: measureList, scaleList: scaleList])
                return
            }

            def oldCode = unitInstance.code
            unitInstance.properties['code', 'name', 'multiplier', 'measure', 'scale'] = params
            utilService.verify(unitInstance, ['measure', 'scale'])             // Ensure correct references
            if (utilService.saveWithMessages(unitInstance, [prefix: 'unit.name', code: unitInstance.code, oldCode: oldCode, field: 'name'])) {
                utilService.cacheService.resetThis('conversion', unitInstance.securityCode, unitInstance.code)
                flash.message = utilService.standardMessage('updated', unitInstance)
                redirect(action: 'show', id: unitInstance.id)
            } else {
                def measureList = Measure.findAllByCompany(utilService.currentCompany())
                def scaleList = Scale.findAllByCompany(utilService.currentCompany())
                render(view: 'edit', model: [unitInstance: unitInstance, measureList: measureList, scaleList: scaleList])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'unit', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def unitInstance = new Unit()
        def ddSource = utilService.reSource('measure.list')
        if (ddSource) {
            unitInstance.measure = ddSource
        } else {
            ddSource = utilService.reSource('scale.list')
            if (ddSource) unitInstance.scale = ddSource
        }

        def measureList = Measure.findAllByCompany(utilService.currentCompany())
        def scaleList = Scale.findAllByCompany(utilService.currentCompany())
        return [unitInstance: unitInstance, measureList: measureList, scaleList: scaleList]
    }

    def save() {
        def unitInstance = new Unit()
        unitInstance.properties['code', 'name', 'multiplier', 'measure', 'scale'] = params
        utilService.verify(unitInstance, ['measure', 'scale'])             // Ensure correct references
        if (utilService.saveWithMessages(unitInstance, [prefix: 'unit.name', code: unitInstance.code, field: 'name'])) {
            utilService.cacheService.resetThis('conversion', unitInstance.securityCode, unitInstance.code)
            flash.message = utilService.standardMessage('created', unitInstance)
            redirect(action: 'show', id: unitInstance.id)
        } else {
            def measureList = Measure.findAllByCompany(utilService.currentCompany())
            def scaleList = Scale.findAllByCompany(utilService.currentCompany())
            render(view: 'create', model: [unitInstance: unitInstance, measureList: measureList, scaleList: scaleList])
        }
    }
}