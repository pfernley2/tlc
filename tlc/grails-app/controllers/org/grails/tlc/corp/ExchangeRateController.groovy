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

import org.grails.tlc.sys.UtilService

class ExchangeRateController {

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

        def ddSource = utilService.source('exchangeCurrency.list')
        [exchangeRateInstanceList: ExchangeRate.selectList(securityCode: utilService.currentCompany().securityCode), exchangeRateInstanceTotal: ExchangeRate.selectCount(), ddSource: ddSource]
    }

    def show() {
        def exchangeRateInstance = ExchangeRate.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!exchangeRateInstance) {
            flash.message = utilService.standardMessage('not.found', 'exchangeRate', params.id)
            redirect(action: 'list')
        } else {
            return [exchangeRateInstance: exchangeRateInstance]
        }
    }

    def delete() {
        def exchangeRateInstance = ExchangeRate.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (exchangeRateInstance) {
            if (exchangeRateInstance.currency.code == UtilService.BASE_CURRENCY_CODE && ExchangeRate.countByCurrency(exchangeRateInstance.currency) == 1) {
                flash.message = message(code: 'exchangeRate.base.delete', default: 'This is the fixed rate for the system base currency and cannot be deleted')
                redirect(action: 'show', id: params.id)
            } else {
                try {
                    exchangeRateInstance.delete(flush: true)
                    utilService.cacheService.resetThis('exchangeRate', exchangeRateInstance.securityCode, exchangeRateInstance.currency.code)
                    flash.message = utilService.standardMessage('deleted', exchangeRateInstance)
                    redirect(action: 'list')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', exchangeRateInstance)
                    redirect(action: 'show', id: params.id)
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'exchangeRate', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def exchangeRateInstance = ExchangeRate.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!exchangeRateInstance) {
            flash.message = utilService.standardMessage('not.found', 'exchangeRate', params.id)
            redirect(action: 'list')
        } else {
            return [exchangeRateInstance: exchangeRateInstance]
        }
    }

    def update(Long version) {
        def exchangeRateInstance = ExchangeRate.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (exchangeRateInstance) {
            if (version != null && exchangeRateInstance.version > version) {
                exchangeRateInstance.errorMessage(code: 'locking.failure', domain: 'exchangeRate')
                render(view: 'edit', model: [exchangeRateInstance: exchangeRateInstance])
                return
            }

            exchangeRateInstance.properties['validFrom', 'rate'] = params
            def valid = !exchangeRateInstance.hasErrors()
            if (valid && exchangeRateInstance.currency.code == UtilService.BASE_CURRENCY_CODE && exchangeRateInstance.rate != 1.0) {
                exchangeRateInstance.errorMessage(field: 'rate', code: 'exchangeRate.base.rate', default: 'This is the system base currency and must have a fixed exchange rate of 1.0')
                valid = false
            }

            if (valid) valid = exchangeRateInstance.saveThis()
            if (valid) {
                utilService.cacheService.resetThis('exchangeRate', exchangeRateInstance.securityCode, exchangeRateInstance.currency.code)
                flash.message = utilService.standardMessage('updated', exchangeRateInstance)
                redirect(action: 'show', id: exchangeRateInstance.id)
            } else {
                render(view: 'edit', model: [exchangeRateInstance: exchangeRateInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'exchangeRate', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def exchangeRateInstance = new ExchangeRate()
        exchangeRateInstance.currency = utilService.reSource('exchangeCurrency.list')   // Ensure correct parent
        exchangeRateInstance.validFrom = utilService.fixDate()
        return [exchangeRateInstance: exchangeRateInstance]
    }

    def save() {
        def exchangeRateInstance = new ExchangeRate()
        exchangeRateInstance.properties['validFrom', 'rate'] = params
        exchangeRateInstance.currency = utilService.reSource('exchangeCurrency.list')   // Ensure correct parent
        def valid = !exchangeRateInstance.hasErrors()
        if (valid && exchangeRateInstance.currency.code == UtilService.BASE_CURRENCY_CODE && exchangeRateInstance.rate != 1.0) {
            exchangeRateInstance.errorMessage(field: 'rate', code: 'exchangeRate.base.rate', default: 'This is the system base currency and must have a fixed exchange rate of 1.0')
            valid = false
        }

        if (valid) valid = exchangeRateInstance.saveThis()
        if (valid) {
            utilService.cacheService.resetThis('exchangeRate', exchangeRateInstance.securityCode, exchangeRateInstance.currency.code)
            flash.message = utilService.standardMessage('created', exchangeRateInstance)
            redirect(action: 'show', id: exchangeRateInstance.id)
        } else {
            render(view: 'create', model: [exchangeRateInstance: exchangeRateInstance])
        }
    }
}