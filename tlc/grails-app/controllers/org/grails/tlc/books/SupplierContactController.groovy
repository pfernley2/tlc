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

import org.grails.tlc.sys.SystemSupplierContactType

class SupplierContactController {

    // Injected services
    def utilService
    def bookService
    def addressService

    // Security settings
    def activities = [default: 'apadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['name', 'identifier'].contains(params.sort) ? params.sort : 'name'
        def ddSource = utilService.source('supplierAddress.list')
        def supplierContactInstanceList = []
        def supplierContactInstanceTotal = 0
        def supplierAddressLines = []
        if (bookService.hasSupplierAccess(ddSource?.supplier)) {
            supplierContactInstanceList = SupplierContact.selectList(securityCode: utilService.currentCompany().securityCode)
            supplierContactInstanceTotal = SupplierContact.selectCount()
            supplierAddressLines = addressService.formatAddress(ddSource, ddSource.supplier, null, ddSource.supplier.country)
        }

        [supplierContactInstanceList: supplierContactInstanceList, supplierContactInstanceTotal: supplierContactInstanceTotal,
			ddSource: ddSource, supplierAddressLines: supplierAddressLines]
    }

    def show() {
        def supplierContactInstance = SupplierContact.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!bookService.hasSupplierAccess(supplierContactInstance?.address?.supplier)) {
            flash.message = utilService.standardMessage('not.found', 'supplierContact', params.id)
            redirect(action: 'list')
        } else {
            return [supplierContactInstance: supplierContactInstance]
        }
    }

    def delete() {
        def supplierContactInstance = SupplierContact.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (bookService.hasSupplierAccess(supplierContactInstance?.address?.supplier)) {
            try {
                supplierContactInstance.delete(flush: true)
                flash.message = utilService.standardMessage('deleted', supplierContactInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', supplierContactInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'supplierContact', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def supplierContactInstance = SupplierContact.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!bookService.hasSupplierAccess(supplierContactInstance?.address?.supplier)) {
            flash.message = utilService.standardMessage('not.found', 'supplierContact', params.id)
            redirect(action: 'list')
        } else {
            return [supplierContactInstance: supplierContactInstance, transferList: createTransferList(supplierContactInstance)]
        }
    }

    def update(Long version) {
        def supplierContactInstance = SupplierContact.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (bookService.hasSupplierAccess(supplierContactInstance?.address?.supplier)) {
            if (version != null && supplierContactInstance.version > version) {
                supplierContactInstance.errorMessage(code: 'locking.failure', domain: 'supplierContact')
                render(view: 'edit', model: [supplierContactInstance: loadTransfers(supplierContactInstance, params), transferList: createTransferList(supplierContactInstance)])
                return
            }

            supplierContactInstance.properties['name', 'identifier'] = params
            loadTransfers(supplierContactInstance, params)
            def valid = (!supplierContactInstance.hasErrors() && supplierContactInstance.validate())
            if (valid) {
                def usage
                SupplierContact.withTransaction {status ->
                    for (trf in supplierContactInstance.usageTransfers) {
                        usage = SupplierContactUsage.findByAddressAndType(supplierContactInstance.address, trf)
                        if (usage && usage.contact.id != supplierContactInstance.id) usage.delete()
                        supplierContactInstance.addToUsages(new SupplierContactUsage(address: supplierContactInstance.address, type: trf))
                    }

                    if (!supplierContactInstance.save(flush: true)) {
                        status.setRollbackOnly()
                        valid = false
                    }
                }
            }

            if (valid) {
                flash.message = utilService.standardMessage('updated', supplierContactInstance)
                redirect(action: 'show', id: supplierContactInstance.id)
            } else {
                render(view: 'edit', model: [supplierContactInstance: supplierContactInstance, transferList: createTransferList(supplierContactInstance)])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'supplierContact', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def ddSource = utilService.reSource('supplierAddress.list')
        if (bookService.hasSupplierAccess(ddSource?.supplier)) {
            def supplierContactInstance = new SupplierContact()
            supplierContactInstance.address = ddSource
            return [supplierContactInstance: supplierContactInstance, transferList: createTransferList(supplierContactInstance)]
        } else {
            redirect(action: 'list')

        }
    }

    def save() {
        def ddSource = utilService.reSource('supplierAddress.list')
        if (bookService.hasSupplierAccess(ddSource?.supplier)) {
            def supplierContactInstance = new SupplierContact()
            supplierContactInstance.properties['name', 'identifier'] = params
            supplierContactInstance.address = ddSource
            loadTransfers(supplierContactInstance, params)
            def valid = (!supplierContactInstance.hasErrors() && supplierContactInstance.validate())
            if (valid) {
                def usage
                SupplierContact.withTransaction {status ->
                    for (trf in supplierContactInstance.usageTransfers) {
                        usage = SupplierContactUsage.findByAddressAndType(supplierContactInstance.address, trf)
                        if (usage && usage.contact.id != supplierContactInstance.id) usage.delete()
                        supplierContactInstance.addToUsages(new SupplierContactUsage(address: supplierContactInstance.address, type: trf))
                    }

                    if (!supplierContactInstance.save(flush: true)) {
                        status.setRollbackOnly()
                        valid = false
                    }
                }
            }

            if (valid) {
                flash.message = utilService.standardMessage('created', supplierContactInstance)
                redirect(action: 'show', id: supplierContactInstance.id)
            } else {
                render(view: 'create', model: [supplierContactInstance: supplierContactInstance, transferList: createTransferList(supplierContactInstance)])
            }
        } else {
            redirect(action: 'list')
        }
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private createTransferList(contact) {
        def types = SystemSupplierContactType.list()
        if (contact.id) {
            def usages = SupplierContactUsage.findAllByContact(contact)
            for (usage in usages) {
                for (int i = 0; i < types.size(); i++) {
                    if (types[i].id == usage.type.id) {
                        types.remove(i)
                        break
                    }
                }
            }
        }

        return types
    }

    private loadTransfers(contact, params) {
        if (params.transfers) {
            contact.usageTransfers = []
            def transfers = (params.transfers instanceof String) ? [params.transfers] : params.transfers
            for (transfer in transfers) contact.usageTransfers << SystemSupplierContactType.get(transfer)
        }

        return contact
    }
}