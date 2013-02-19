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

class MeasureController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code'].contains(params.sort) ? params.sort : 'code'
        [measureInstanceList: Measure.selectList(company: utilService.currentCompany()), measureInstanceTotal: Measure.selectCount()]
    }

    def show() {
        def measureInstance = Measure.findByIdAndCompany(params.id, utilService.currentCompany())
        if (!measureInstance) {
            flash.message = utilService.standardMessage('not.found', 'measure', params.id)
            redirect(action: 'list')
        } else {
            return [measureInstance: measureInstance]
        }
    }

    def delete() {
        def measureInstance = Measure.findByIdAndCompany(params.id, utilService.currentCompany())
        if (measureInstance) {
            try {
                utilService.deleteWithMessages(measureInstance, [prefix: 'measure.name', code: measureInstance.code])
                flash.message = utilService.standardMessage('deleted', measureInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('notDeleted', measureInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'measure', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def measureInstance = Measure.findByIdAndCompany(params.id, utilService.currentCompany())
        if (!measureInstance) {
            flash.message = utilService.standardMessage('not.found', 'measure', params.id)
            redirect(action: 'list')
        } else {
            return [measureInstance: measureInstance]
        }
    }

    def update(Long version) {
        def measureInstance = Measure.findByIdAndCompany(params.id, utilService.currentCompany())
        if (measureInstance) {
            if (version != null && measureInstance.version > version) {
                measureInstance.errorMessage(code: 'locking.failure', domain: 'measure')
                render(view: 'edit', model: [measureInstance: measureInstance])
                return
            }

            def oldCode = measureInstance.code
            measureInstance.properties['code', 'name'] = params
            if (utilService.saveWithMessages(measureInstance, [prefix: 'measure.name', code: measureInstance.code, oldCode: oldCode, field: 'name'])) {
                flash.message = utilService.standardMessage('updated', measureInstance)
                redirect(action: 'show', id: measureInstance.id)
            } else {
                render(view: 'edit', model: [measureInstance: measureInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'measure', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def measureInstance = new Measure()
        measureInstance.company = utilService.currentCompany()
        return [measureInstance: measureInstance]
    }

    def save() {
        def measureInstance = new Measure()
        measureInstance.properties['code', 'name'] = params
        measureInstance.company = utilService.currentCompany()
        if (utilService.saveWithMessages(measureInstance, [prefix: 'measure.name', code: measureInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', measureInstance)
            redirect(action: 'show', id: measureInstance.id)
        } else {
            render(view: 'create', model: [measureInstance: measureInstance])
        }
    }
}