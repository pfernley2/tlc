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

class ScaleController {

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
        [scaleInstanceList: Scale.selectList(company: utilService.currentCompany()), scaleInstanceTotal: Scale.selectCount()]
    }

    def show() {
        def scaleInstance = Scale.findByIdAndCompany(params.id, utilService.currentCompany())
        if (!scaleInstance) {
            flash.message = utilService.standardMessage('not.found', 'scale', params.id)
            redirect(action: 'list')
        } else {
            return [scaleInstance: scaleInstance]
        }
    }

    def delete() {
        def scaleInstance = Scale.findByIdAndCompany(params.id, utilService.currentCompany())
        if (scaleInstance) {
            try {
                utilService.deleteWithMessages(scaleInstance, [prefix: 'scale.name', code: scaleInstance.code])
                flash.message = utilService.standardMessage('deleted', scaleInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', scaleInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'scale', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def scaleInstance = Scale.findByIdAndCompany(params.id, utilService.currentCompany())
        if (!scaleInstance) {
            flash.message = utilService.standardMessage('not.found', 'scale', params.id)
            redirect(action: 'list')
        } else {
            return [scaleInstance: scaleInstance]
        }
    }

    def update(Long version) {
        def scaleInstance = Scale.findByIdAndCompany(params.id, utilService.currentCompany())
        if (scaleInstance) {
            if (version != null && scaleInstance.version > version) {
                scaleInstance.errorMessage(code: 'locking.failure', domain: 'scale')
                render(view: 'edit', model: [scaleInstance: scaleInstance])
                return
            }

            def oldCode = scaleInstance.code
            scaleInstance.properties['code', 'name'] = params
            if (utilService.saveWithMessages(scaleInstance, [prefix: 'scale.name', code: scaleInstance.code, oldCode: oldCode, field: 'name'])) {
                flash.message = utilService.standardMessage('updated', scaleInstance)
                redirect(action: 'show', id: scaleInstance.id)
            } else {
                render(view: 'edit', model: [scaleInstance: scaleInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'scale', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def scaleInstance = new Scale()
        scaleInstance.company = utilService.currentCompany()
        return [scaleInstance: scaleInstance]
    }

    def save() {
        def scaleInstance = new Scale()
        scaleInstance.properties['code', 'name'] = params
        scaleInstance.company = utilService.currentCompany()
        if (utilService.saveWithMessages(scaleInstance, [prefix: 'scale.name', code: scaleInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', scaleInstance)
            redirect(action: 'show', id: scaleInstance.id)
        } else {
            render(view: 'create', model: [scaleInstance: scaleInstance])
        }
    }
}