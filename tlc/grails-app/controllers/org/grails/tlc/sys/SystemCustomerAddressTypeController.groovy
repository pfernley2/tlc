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

class SystemCustomerAddressTypeController {

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
        [systemCustomerAddressTypeInstanceList: SystemCustomerAddressType.selectList(), systemCustomerAddressTypeInstanceTotal: SystemCustomerAddressType.selectCount()]
    }

    def show() {
        def systemCustomerAddressTypeInstance = SystemCustomerAddressType.get(params.id)
        if (!systemCustomerAddressTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemCustomerAddressType', params.id)
            redirect(action: 'list')
        } else {
            return [systemCustomerAddressTypeInstance: systemCustomerAddressTypeInstance]
        }
    }

    def delete() {
        def systemCustomerAddressTypeInstance = SystemCustomerAddressType.get(params.id)
        if (systemCustomerAddressTypeInstance) {
            if (systemCustomerAddressTypeInstance.code == 'default') {
                flash.message = message(code: 'systemCustomerAddressType.bad.delete', default: 'You may not delete the default address type')
                redirect(action: 'show', id: params.id)
            } else {
                try {
                    utilService.deleteWithMessages(systemCustomerAddressTypeInstance, [prefix: 'customerAddressType.name', code: systemCustomerAddressTypeInstance.code])
                    flash.message = utilService.standardMessage('deleted', systemCustomerAddressTypeInstance)
                    redirect(action: 'list')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', systemCustomerAddressTypeInstance)
                    redirect(action: 'show', id: params.id)
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemCustomerAddressType', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemCustomerAddressTypeInstance = SystemCustomerAddressType.get(params.id)
        if (!systemCustomerAddressTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemCustomerAddressType', params.id)
            redirect(action: 'list')
        } else {
            return [systemCustomerAddressTypeInstance: systemCustomerAddressTypeInstance]
        }
    }

    def update(Long version) {
        def systemCustomerAddressTypeInstance = SystemCustomerAddressType.get(params.id)
        if (systemCustomerAddressTypeInstance) {
            if (version != null && systemCustomerAddressTypeInstance.version > version) {
                systemCustomerAddressTypeInstance.errorMessage(code: 'locking.failure', domain: 'systemCustomerAddressType')
                render(view: 'edit', model: [systemCustomerAddressTypeInstance: systemCustomerAddressTypeInstance])
                return
            }

            def oldCode = systemCustomerAddressTypeInstance.code
            systemCustomerAddressTypeInstance.properties['code', 'name'] = params
            if (oldCode == 'default' && systemCustomerAddressTypeInstance.code != oldCode) {
                systemCustomerAddressTypeInstance.errorMessage(field: 'code', code: 'systemCustomerAddressType.bad.change', default: 'You may not change the code of the default address type')
                render(view: 'edit', model: [systemCustomerAddressTypeInstance: systemCustomerAddressTypeInstance])
            } else {
                if (utilService.saveWithMessages(systemCustomerAddressTypeInstance, [prefix: 'customerAddressType.name', code: systemCustomerAddressTypeInstance.code, oldCode: oldCode, field: 'name'])) {
                    flash.message = utilService.standardMessage('updated', systemCustomerAddressTypeInstance)
                    redirect(action: 'show', id: systemCustomerAddressTypeInstance.id)
                } else {
                    render(view: 'edit', model: [systemCustomerAddressTypeInstance: systemCustomerAddressTypeInstance])
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemCustomerAddressType', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemCustomerAddressTypeInstance = new SystemCustomerAddressType()
        return [systemCustomerAddressTypeInstance: systemCustomerAddressTypeInstance]
    }

    def save() {
        def systemCustomerAddressTypeInstance = new SystemCustomerAddressType()
        systemCustomerAddressTypeInstance.properties['code', 'name'] = params
        def defaultType = SystemCustomerAddressType.findByCode('default')
        def valid = true
        if (defaultType) {
            SystemCustomerAddressType.withTransaction {status ->
                valid = utilService.saveWithMessages(systemCustomerAddressTypeInstance, [prefix: 'customerAddressType.name', code: systemCustomerAddressTypeInstance.code, field: 'name'])
                if (valid) {
                    def statement
                    try {
                        statement = sessionFactory.getCurrentSession().connection().createStatement()
                        def sql = 'insert into customer_address_usage (customer_id, address_id, type_id, security_code, date_created, last_updated, version) select x.customer_id, x.address_id, ' +
                                systemCustomerAddressTypeInstance.id.toString() + ', x.security_code, x.date_created, x.last_updated, 0 from customer_address_usage as x where x.type_id = ' +
                                defaultType.id.toString()
                        statement.executeUpdate(sql)
                    } catch (Exception ex1) {
                        systemCustomerAddressTypeInstance.errorMessage(code: 'systemCustomerAddressType.bad.update', default: 'Unable to update existing customers')
                        status.setRollbackOnly()
                        valid = false
                    } finally {
                        if (statement) {
                            try {
                                statement.close()
                            } catch (Exception ex2) {
                                systemCustomerAddressTypeInstance.errorMessage(code: 'systemCustomerAddressType.bad.update', default: 'Unable to update existing customers')
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
            systemCustomerAddressTypeInstance.errorMessage(code: 'systemCustomerAddressType.no.default', default: 'Unable to find the default address type')
            valid = false
        }

        if (valid) {
            flash.message = utilService.standardMessage('created', systemCustomerAddressTypeInstance)
            redirect(action: 'show', id: systemCustomerAddressTypeInstance.id)
        } else {
            render(view: 'create', model: [systemCustomerAddressTypeInstance: systemCustomerAddressTypeInstance])
        }
    }
}