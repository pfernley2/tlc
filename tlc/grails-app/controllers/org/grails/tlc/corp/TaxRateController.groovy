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

class TaxRateController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        if (!['validFrom', 'rate'].contains(params.sort)) {
            params.sort = 'validFrom'
            params.order = 'desc'
        }
        def ddSource = utilService.source('taxCode.list')
        [taxRateInstanceList: TaxRate.selectList(securityCode: utilService.currentCompany().securityCode), taxRateInstanceTotal: TaxRate.selectCount(), ddSource: ddSource]
    }

    def show() {
        def taxRateInstance = TaxRate.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!taxRateInstance) {
            flash.message = utilService.standardMessage('not.found', 'taxRate', params.id)
            redirect(action: 'list')
        } else {
            return [taxRateInstance: taxRateInstance]
        }
    }

    def delete() {
        def taxRateInstance = TaxRate.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (taxRateInstance) {
            try {
                taxRateInstance.delete(flush: true)
                flash.message = utilService.standardMessage('deleted', taxRateInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', taxRateInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'taxRate', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def taxRateInstance = TaxRate.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!taxRateInstance) {
            flash.message = utilService.standardMessage('not.found', 'taxRate', params.id)
            redirect(action: 'list')
        } else {
            return [taxRateInstance: taxRateInstance]
        }
    }

    def update(Long version) {
        def taxRateInstance = TaxRate.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (taxRateInstance) {
            if (version != null && taxRateInstance.version > version) {
                taxRateInstance.errorMessage(code: 'locking.failure', domain: 'taxRate')
                render(view: 'edit', model: [taxRateInstance: taxRateInstance])
                return
            }

            taxRateInstance.properties['validFrom', 'rate'] = params
            if (!taxRateInstance.hasErrors() && taxRateInstance.saveThis()) {
                flash.message = utilService.standardMessage('updated', taxRateInstance)
                redirect(action: 'show', id: taxRateInstance.id)
            } else {
                render(view: 'edit', model: [taxRateInstance: taxRateInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'taxRate', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def taxRateInstance = new TaxRate()
        taxRateInstance.taxCode = utilService.reSource('taxCode.list')
        return [taxRateInstance: taxRateInstance]
    }

    def save() {
        def taxRateInstance = new TaxRate()
        taxRateInstance.properties['validFrom', 'rate'] = params
        taxRateInstance.taxCode = utilService.reSource('taxCode.list')
        if (!taxRateInstance.hasErrors() && taxRateInstance.saveThis()) {
            flash.message = utilService.standardMessage('created', taxRateInstance)
            redirect(action: 'show', id: taxRateInstance.id)
        } else {
            render(view: 'create', model: [taxRateInstance: taxRateInstance])
        }
    }
}