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

class TaxCodeController {

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
        [taxCodeInstanceList: TaxCode.selectList(company: utilService.currentCompany()), taxCodeInstanceTotal: TaxCode.selectCount()]
    }

    def show() {
        def taxCodeInstance = TaxCode.findByIdAndCompany(params.id, utilService.currentCompany())
        if (!taxCodeInstance) {
            flash.message = utilService.standardMessage('not.found', 'taxCode', params.id)
            redirect(action: 'list')
        } else {
            return [taxCodeInstance: taxCodeInstance]
        }
    }

    def delete() {
        def taxCodeInstance = TaxCode.findByIdAndCompany(params.id, utilService.currentCompany())
        if (taxCodeInstance) {
            if (taxCodeInstance.companyTaxCode) {
                flash.message = message(code: 'taxCode.corp.delete', default: 'The company tax code cannot be deleted')
                redirect(action: 'show', id: params.id)
            } else {
                try {
                    utilService.deleteWithMessages(taxCodeInstance, [prefix: 'taxCode.name', code: taxCodeInstance.code])
                    flash.message = utilService.standardMessage('deleted', taxCodeInstance)
                    redirect(action: 'list')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', taxCodeInstance)
                    redirect(action: 'show', id: params.id)
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'taxCode', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def taxCodeInstance = TaxCode.findByIdAndCompany(params.id, utilService.currentCompany())
        if (!taxCodeInstance) {
            flash.message = utilService.standardMessage('not.found', 'taxCode', params.id)
            redirect(action: 'list')
        } else {
            return [taxCodeInstance: taxCodeInstance]
        }
    }

    def update(Long version) {
        def taxCodeInstance = TaxCode.findByIdAndCompany(params.id, utilService.currentCompany())
        if (taxCodeInstance) {
            if (version != null && taxCodeInstance.version > version) {
                taxCodeInstance.errorMessage(code: 'locking.failure', domain: 'taxCode')
                render(view: 'edit', model: [taxCodeInstance: taxCodeInstance])
                return
            }

            def oldCode = taxCodeInstance.code
            taxCodeInstance.properties['code', 'name', 'authority'] = params
            utilService.verify(taxCodeInstance, ['authority'])             // Ensure correct references
            if (utilService.saveWithMessages(taxCodeInstance, [prefix: 'taxCode.name', code: taxCodeInstance.code, field: 'name', oldCode: oldCode])) {
                flash.message = utilService.standardMessage('updated', taxCodeInstance)
                redirect(action: 'show', id: taxCodeInstance.id)
            } else {
                render(view: 'edit', model: [taxCodeInstance: taxCodeInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'taxCode', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def taxCodeInstance = new TaxCode()
        taxCodeInstance.company = utilService.currentCompany()
        return [taxCodeInstance: taxCodeInstance]
    }

    def save() {
        def taxCodeInstance = new TaxCode()
        taxCodeInstance.properties['code', 'name', 'authority'] = params
        taxCodeInstance.company = utilService.currentCompany()
        utilService.verify(taxCodeInstance, ['authority'])             // Ensure correct references
        if (utilService.saveWithMessages(taxCodeInstance, [prefix: 'taxCode.name', code: taxCodeInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', taxCodeInstance)
            redirect(action: 'show', id: taxCodeInstance.id)
        } else {
            render(view: 'create', model: [taxCodeInstance: taxCodeInstance])
        }
    }
}