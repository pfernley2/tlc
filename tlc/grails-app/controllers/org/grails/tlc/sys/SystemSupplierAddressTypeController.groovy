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

class SystemSupplierAddressTypeController {

    // Injected services
    def utilService

    def sessionFactory

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = 'code'
        [systemSupplierAddressTypeInstanceList: SystemSupplierAddressType.selectList(), systemSupplierAddressTypeInstanceTotal: SystemSupplierAddressType.selectCount()]
    }

    def show() {
        def systemSupplierAddressTypeInstance = SystemSupplierAddressType.get(params.id)
        if (!systemSupplierAddressTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemSupplierAddressType', params.id)
            redirect(action: 'list')
        } else {
            return [systemSupplierAddressTypeInstance: systemSupplierAddressTypeInstance]
        }
    }

    def delete() {
        def systemSupplierAddressTypeInstance = SystemSupplierAddressType.get(params.id)
        if (systemSupplierAddressTypeInstance) {
            if (systemSupplierAddressTypeInstance.code == 'default') {
                flash.message = message(code: 'systemSupplierAddressType.bad.delete', default: 'You may not delete the default address type')
                redirect(action: 'show', id: params.id)
            } else {
                try {
                    utilService.deleteWithMessages(systemSupplierAddressTypeInstance, [prefix: 'supplierAddressType.name', code: systemSupplierAddressTypeInstance.code])
                    flash.message = utilService.standardMessage('deleted', systemSupplierAddressTypeInstance)
                    redirect(action: 'list')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', systemSupplierAddressTypeInstance)
                    redirect(action: 'show', id: params.id)
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemSupplierAddressType', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemSupplierAddressTypeInstance = SystemSupplierAddressType.get(params.id)
        if (!systemSupplierAddressTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemSupplierAddressType', params.id)
            redirect(action: 'list')
        } else {
            return [systemSupplierAddressTypeInstance: systemSupplierAddressTypeInstance]
        }
    }

    def update(Long version) {
        def systemSupplierAddressTypeInstance = SystemSupplierAddressType.get(params.id)
        if (systemSupplierAddressTypeInstance) {
            if (version != null && systemSupplierAddressTypeInstance.version > version) {
                systemSupplierAddressTypeInstance.errorMessage(code: 'locking.failure', domain: 'systemSupplierAddressType')
                render(view: 'edit', model: [systemSupplierAddressTypeInstance: systemSupplierAddressTypeInstance])
                return
            }

            def oldCode = systemSupplierAddressTypeInstance.code
            systemSupplierAddressTypeInstance.properties['code', 'name'] = params
            if (oldCode == 'default' && systemSupplierAddressTypeInstance.code != oldCode) {
                systemSupplierAddressTypeInstance.errorMessage(field: 'code', code: 'systemSupplierAddressType.bad.change', default: 'You may not change the code of the default address type')
                render(view: 'edit', model: [systemSupplierAddressTypeInstance: systemSupplierAddressTypeInstance])
            } else {
                if (utilService.saveWithMessages(systemSupplierAddressTypeInstance, [prefix: 'supplierAddressType.name', code: systemSupplierAddressTypeInstance.code, oldCode: oldCode, field: 'name'])) {
                    flash.message = utilService.standardMessage('updated', systemSupplierAddressTypeInstance)
                    redirect(action: 'show', id: systemSupplierAddressTypeInstance.id)
                } else {
                    render(view: 'edit', model: [systemSupplierAddressTypeInstance: systemSupplierAddressTypeInstance])
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemSupplierAddressType', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemSupplierAddressTypeInstance = new SystemSupplierAddressType()
        return [systemSupplierAddressTypeInstance: systemSupplierAddressTypeInstance]
    }

    def save() {
        def systemSupplierAddressTypeInstance = new SystemSupplierAddressType()
        systemSupplierAddressTypeInstance.properties['code', 'name'] = params
        def defaultType = SystemSupplierAddressType.findByCode('default')
        def valid = true
        if (defaultType) {
            SystemSupplierAddressType.withTransaction {status ->
                valid = utilService.saveWithMessages(systemSupplierAddressTypeInstance, [prefix: 'supplierAddressType.name', code: systemSupplierAddressTypeInstance.code, field: 'name'])
                if (valid) {
                    def statement
                    try {
                        statement = sessionFactory.getCurrentSession().connection().createStatement()
                        def sql = 'insert into supplier_address_usage (supplier_id, address_id, type_id, security_code, date_created, last_updated, version) select x.supplier_id, x.address_id, ' +
                                systemSupplierAddressTypeInstance.id.toString() + ', x.security_code, x.date_created, x.last_updated, 0 from supplier_address_usage as x where x.type_id = ' +
                                defaultType.id.toString()
                        statement.executeUpdate(sql)
                    } catch (Exception ex1) {
                        ex1.printStackTrace()
                        systemSupplierAddressTypeInstance.errorMessage(code: 'systemSupplierAddressType.bad.update', default: 'Unable to update existing suppliers')
                        status.setRollbackOnly()
                        valid = false
                    } finally {
                        if (statement) {
                            try {
                                statement.close()
                            } catch (Exception ex2) {
                                ex2.printStackTrace()
                                systemSupplierAddressTypeInstance.errorMessage(code: 'systemSupplierAddressType.bad.update', default: 'Unable to update existing suppliers')
                                status.setRollbackOnly()
                                valid = false
                            }
                        }
                    }
                } else {
                    status.setRollbackOnly()
                }
            }
        } else {
            systemSupplierAddressTypeInstance.errorMessage(code: 'systemSupplierAddressType.no.default', default: 'Unable to find the default address type')
            valid = false
        }

        if (valid) {
            flash.message = utilService.standardMessage('created', systemSupplierAddressTypeInstance)
            redirect(action: 'show', id: systemSupplierAddressTypeInstance.id)
        } else {
            render(view: 'create', model: [systemSupplierAddressTypeInstance: systemSupplierAddressTypeInstance])
        }
    }
}