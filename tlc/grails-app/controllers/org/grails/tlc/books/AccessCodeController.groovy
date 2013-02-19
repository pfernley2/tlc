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
package org.grails.tlc.books

class AccessCodeController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'name'].contains(params.sort) ? params.sort : 'code'
        [accessCodeInstanceList: AccessCode.selectList(company: utilService.currentCompany()), accessCodeInstanceTotal: AccessCode.selectCount()]
    }

    def show() {
        def accessCodeInstance = AccessCode.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!accessCodeInstance) {
            flash.message = utilService.standardMessage('not.found', 'accessCode', params.id)
            redirect(action: 'list')
        } else {
            return [accessCodeInstance: accessCodeInstance]
        }
    }

    def delete() {
        def accessCodeInstance = AccessCode.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (accessCodeInstance) {
            try {
                accessCodeInstance.delete(flush: true)
                flash.message = utilService.standardMessage('deleted', accessCodeInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', accessCodeInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'accessCode', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def accessCodeInstance = AccessCode.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!accessCodeInstance) {
            flash.message = utilService.standardMessage('not.found', 'accessCode', params.id)
            redirect(action: 'list')
        } else {
            return [accessCodeInstance: accessCodeInstance]
        }
    }

    def update(Long version) {
        def accessCodeInstance = AccessCode.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (accessCodeInstance) {
            if (version != null && accessCodeInstance.version > version) {
                accessCodeInstance.errorMessage(code: 'locking.failure', domain: 'accessCode')
                render(view: 'edit', model: [accessCodeInstance: accessCodeInstance])
                return
            }

            accessCodeInstance.properties['code', 'name'] = params
            if (!accessCodeInstance.hasErrors() && accessCodeInstance.saveThis()) {
                flash.message = utilService.standardMessage('updated', accessCodeInstance)
                redirect(action: 'show', id: accessCodeInstance.id)
            } else {
                render(view: 'edit', model: [accessCodeInstance: accessCodeInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'accessCode', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def accessCodeInstance = new AccessCode()
        accessCodeInstance.company = utilService.currentCompany()   // Ensure correct company
        return [accessCodeInstance: accessCodeInstance]
    }

    def save() {
        def accessCodeInstance = new AccessCode()
        accessCodeInstance.properties['code', 'name'] = params
        accessCodeInstance.company = utilService.currentCompany()   // Ensure correct company
        if (!accessCodeInstance.hasErrors() && accessCodeInstance.saveThis()) {
            flash.message = utilService.standardMessage('created', accessCodeInstance)
            redirect(action: 'show', id: accessCodeInstance.id)
        } else {
            render(view: 'create', model: [accessCodeInstance: accessCodeInstance])
        }
    }
}