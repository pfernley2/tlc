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

class SystemCurrencyController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'decimals', 'autoUpdate'].contains(params.sort) ? params.sort : 'code'
        [systemCurrencyInstanceList: SystemCurrency.selectList(), systemCurrencyInstanceTotal: SystemCurrency.selectCount()]
    }

    def show() {
        def systemCurrencyInstance = SystemCurrency.get(params.id)
        if (!systemCurrencyInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemCurrency', params.id)
            redirect(action: 'list')
        } else {
            return [systemCurrencyInstance: systemCurrencyInstance]
        }
    }

    def delete() {
        def systemCurrencyInstance = SystemCurrency.get(params.id)
        if (systemCurrencyInstance) {
            if (systemCurrencyInstance.code == UtilService.BASE_CURRENCY_CODE) {
                flash.message = message(code: 'exchangeCurrency.base.delete', default: 'This is the system base currency and cannot be deleted')
                redirect(action: 'show', id: params.id)
            } else {
                try {
                    utilService.deleteWithMessages(systemCurrencyInstance, [prefix: 'currency.name', code: systemCurrencyInstance.code])
                    flash.message = utilService.standardMessage('deleted', systemCurrencyInstance)
                    redirect(action: 'list')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', systemCurrencyInstance)
                    redirect(action: 'show', id: params.id)
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemCurrency', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemCurrencyInstance = SystemCurrency.get(params.id)
        if (!systemCurrencyInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemCurrency', params.id)
            redirect(action: 'list')
        } else {
            return [systemCurrencyInstance: systemCurrencyInstance]
        }
    }

    def update(Long version) {
        def systemCurrencyInstance = SystemCurrency.get(params.id)
        if (systemCurrencyInstance) {
            if (version != null && systemCurrencyInstance.version > version) {
                systemCurrencyInstance.errorMessage(code: 'locking.failure', domain: 'systemCurrency')
                render(view: 'edit', model: [systemCurrencyInstance: systemCurrencyInstance])
                return
            }

            def oldCode = systemCurrencyInstance.code
            systemCurrencyInstance.properties['code', 'name', 'decimals', 'autoUpdate'] = params
            if (oldCode != systemCurrencyInstance.code && oldCode == UtilService.BASE_CURRENCY_CODE) {
                systemCurrencyInstance.errorMessage(field: 'code', code: 'exchangeCurrency.base.change', default: 'This is the system base currency and cannot have its code changed')
                render(view: 'edit', model: [systemCurrencyInstance: systemCurrencyInstance])
            } else {
                if (utilService.saveWithMessages(systemCurrencyInstance, [prefix: 'currency.name', code: systemCurrencyInstance.code, oldCode: oldCode, field: 'name'])) {
                    flash.message = utilService.standardMessage('updated', systemCurrencyInstance)
                    redirect(action: 'show', id: systemCurrencyInstance.id)
                } else {
                    render(view: 'edit', model: [systemCurrencyInstance: systemCurrencyInstance])
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemCurrency', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        return [systemCurrencyInstance: new SystemCurrency()]
    }

    def save() {
        def systemCurrencyInstance = new SystemCurrency()
        systemCurrencyInstance.properties['code', 'name', 'decimals', 'autoUpdate'] = params
        if (utilService.saveWithMessages(systemCurrencyInstance, [prefix: 'currency.name', code: systemCurrencyInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', systemCurrencyInstance)
            redirect(action: 'show', id: systemCurrencyInstance.id)
        } else {
            render(view: 'create', model: [systemCurrencyInstance: systemCurrencyInstance])
        }
    }
}