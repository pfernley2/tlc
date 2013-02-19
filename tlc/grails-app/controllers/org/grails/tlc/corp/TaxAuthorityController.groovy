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

class TaxAuthorityController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['name', 'usage'].contains(params.sort) ? params.sort : 'name'
        [taxAuthorityInstanceList: TaxAuthority.selectList(company: utilService.currentCompany()), taxAuthorityInstanceTotal: TaxAuthority.selectCount()]
    }

    def show() {
        def taxAuthorityInstance = TaxAuthority.findByIdAndCompany(params.id, utilService.currentCompany())
        if (!taxAuthorityInstance) {
            flash.message = utilService.standardMessage('not.found', 'taxAuthority', params.id)
            redirect(action: 'list')
        } else {
            return [taxAuthorityInstance: taxAuthorityInstance]
        }
    }

    def delete() {
        def taxAuthorityInstance = TaxAuthority.findByIdAndCompany(params.id, utilService.currentCompany())
        if (taxAuthorityInstance) {
            try {
                taxAuthorityInstance.delete(flush: true)
                flash.message = utilService.standardMessage('deleted', taxAuthorityInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', taxAuthorityInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'taxAuthority', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def taxAuthorityInstance = TaxAuthority.findByIdAndCompany(params.id, utilService.currentCompany())
        if (!taxAuthorityInstance) {
            flash.message = utilService.standardMessage('not.found', 'taxAuthority', params.id)
            redirect(action: 'list')
        } else {
            return [taxAuthorityInstance: taxAuthorityInstance]
        }
    }

    def update(Long version) {
        def taxAuthorityInstance = TaxAuthority.findByIdAndCompany(params.id, utilService.currentCompany())
        if (taxAuthorityInstance) {
            if (version != null && taxAuthorityInstance.version > version) {
                taxAuthorityInstance.errorMessage(code: 'locking.failure', domain: 'taxAuthority')
                render(view: 'edit', model: [taxAuthorityInstance: taxAuthorityInstance])
                return
            }

            taxAuthorityInstance.properties['name', 'usage'] = params
            if (!taxAuthorityInstance.hasErrors() && taxAuthorityInstance.saveThis()) {
                flash.message = utilService.standardMessage('updated', taxAuthorityInstance)
                redirect(action: 'show', id: taxAuthorityInstance.id)
            } else {
                render(view: 'edit', model: [taxAuthorityInstance: taxAuthorityInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'taxAuthority', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def taxAuthorityInstance = new TaxAuthority()
        taxAuthorityInstance.company = utilService.currentCompany()   // Ensure correct company
        return [taxAuthorityInstance: taxAuthorityInstance]
    }

    def save() {
        def taxAuthorityInstance = new TaxAuthority()
        taxAuthorityInstance.properties['name', 'usage'] = params
        taxAuthorityInstance.company = utilService.currentCompany()   // Ensure correct company
        if (!taxAuthorityInstance.hasErrors() && taxAuthorityInstance.saveThis()) {
            flash.message = utilService.standardMessage('created', taxAuthorityInstance)
            redirect(action: 'show', id: taxAuthorityInstance.id)
        } else {
            render(view: 'create', model: [taxAuthorityInstance: taxAuthorityInstance])
        }
    }
}