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

import org.grails.tlc.corp.Company
import org.grails.tlc.corp.CompanyUser
import org.grails.tlc.obj.Operation

class SystemController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin', access: 'any', loginDisabled: 'any', actionsDisabled: 'any', intro: 'login', assign: 'login', assigned: 'login',
            create: 'login', attach: 'login', paymentProgram: 'any', restProgram: 'any', noticeFile: 'any', licenseFile: 'any']

    // List of actions with specific request types
    static allowedMethods = [resize: 'POST', clear: 'POST', clearAll: 'POST', operate: 'POST', assigned: 'POST', create: 'POST', attach: 'POST']

    def environment() {
        [environmentInstance: utilService.environment()]
    }

    def statistics() {
        def stats = utilService.cacheService.statistics()
        for (c in stats) {
            if (c.size != c.actual) {
                flash.message = message(code: 'system.statistics.mismatch', args: [c.code], default: "One or more caches (e.g. ${c.code}) have a size mismatch. Please check the error log for details.")
                break
            }
        }

        [statisticsInstanceList: stats]
    }

    def resize() {
        def id = params.id
        def size = params.size
        if (id && size) {
            try {
                utilService.cacheService.resize(id, new Integer(size))
            } catch (Exception ex) {}
        }

        redirect(action: 'statistics')
    }

    def clear() {
        if (params.id) utilService.cacheService.clearThis(params.id)

        redirect(action: 'statistics')
    }

    def clearAll() {
        utilService.cacheService.clearAll()
        redirect(action: 'statistics')
    }

    def operation() {
        def queueStatus = utilService.taskService.statistics()
        if (queueStatus) queueStatus.status = message(code: "queuedTask.queue.status.${queueStatus.status}", default: queueStatus.status)
		def operationInstance = new Operation()
        operationInstance.state = UtilService.getCurrentOperatingState()
        return [operationInstance: operationInstance, queueStatus: queueStatus]
    }

    def operate(Operation operationInstance) {
        if (!operationInstance.hasErrors()) {
            UtilService.setCurrentOperatingState(operationInstance.state)
            flash.message = message(code: 'operation.updated', default: 'Operating state updated')
            redirect(action: 'operation')
        } else {
            def queueStatus = utilService.taskService.statistics()
            if (queueStatus) queueStatus.status = message(code: "queuedTask.queue.status.${queueStatus.status}", default: queueStatus.status)
            render(view: 'operation', model: [operationInstance: operationInstance, queueStatus: queueStatus])
        }
    }

    def access() {}

    def loginDisabled() {}

    def actionsDisabled() {}

    def assign() {}

    def assigned() {
        def companies = CompanyUser.findAllByUser(utilService.currentUser())
        if (!companies) {
            flash.message = message(code: 'companyUser.not.assigned', default: 'We are sorry, but you have still not been assigned to a company. Please contact your company administrator.')
            redirect(action: 'assign')
        } else if (companies.size() == 1) {
            utilService.newCurrentCompany(companies[0].company.id)
            redirect(controller: 'systemMenu', action: 'display')
        } else {
            utilService.setNextStep([sourceController: 'companyUser', sourceAction: 'select',
                    targetController: 'companyUser', targetAction: 'attach'])
            redirect(controller: 'companyUser', action: 'select')
        }
    }

    def intro() {
        def companyInstance = new Company()
        def locale = utilService.currentLocale()
        def language = SystemLanguage.findByCode(locale.getLanguage() ?: 'en') ?: SystemLanguage.findByCode('en')
        def country = SystemCountry.findByCode(locale.getCountry() ?: ((language.code == 'en') ? 'US' : language.code.toUpperCase(Locale.US))) ?: SystemCountry.findByCode('US')
        companyInstance.language = language
        companyInstance.country = country
        companyInstance.currency = country.currency

        // The US, Canada, Mexico, Columbia, Venzuela, Chile and the Phillippines use US Letter stationery
        // but we let the PDF system handle the resizing and so the stationery property has been removed
        //if (['US', 'CA', 'MX', 'CO', 'VE', 'CL', 'PH'].contains(country.code)) companyInstance.stationery = 'letter'

        return [companyInstance: companyInstance]
    }

    def create() {
        def user = utilService.currentUser()

        // Only system administrators can create companies in a live system and, in a demo system, registered users
        // can only create one company for themselves (additional ones must be created by a system administrator)
        if (!user.administrator && (!utilService.systemSetting('isDemoSystem') || CompanyUser.countByUser(user))) {
            redirect(action: 'intro')
            return
        }

        def companyInstance = new Company()
        companyInstance.properties['name', 'country', 'language'] = params
        def currency = SystemCurrency.get(params.currency.id)
        def rateValid = true
        def valid = !companyInstance.hasErrors()
        if (valid) {
            def rate = 1.0
            if (currency.code != utilService.BASE_CURRENCY_CODE) {
                if (currency.autoUpdate) {
                    rate = utilService.readExchangeRate(currency.code)
					if (!rate) {
						rate = 1.0
						rateValid = false
					}
                } else {
                    rateValid = false
                }
            }

            // Now create the record and its initial data
            Company.withTransaction {status ->
                if (companyInstance.saveThis()) {
                    def companyUser = new CompanyUser(company: companyInstance, user: user)
                    if (companyUser.saveThis()) {
                        companyUser.addToRoles(SystemRole.findByCode('companyAdmin'))
                        if (companyUser.save()) {   // With deep validation
                            def result = companyInstance.initializeData(user, currency, rate)
                            if (result != null) {
                                companyInstance.errorMessage(code: 'company.initialization.error', args: [result], default: "Unable to load the initial company data for ${result}")
                                status.setRollbackOnly()
                                valid = false
                            }
                        } else {
                            companyInstance.errorMessage(code: 'company.role.error', default: 'Unable to set the company administration role for the initial user')
                            status.setRollbackOnly()
                            valid = false
                        }
                    } else {
                        companyInstance.errorMessage(code: 'company.user.error', default: 'Unable to create the initial company user')
                        status.setRollbackOnly()
                        valid = false
                    }
                } else {
                    status.setRollbackOnly()
                    valid = false
                }
            }
        }

        if (!valid) {
            render(view: 'intro', model: [companyInstance: companyInstance])
            return
        }

        if (rateValid) {
            flash.message = utilService.standardMessage('created', companyInstance)
        } else {
            flash.message = message(code: 'company.created.warning', args: ["${companyInstance.id}", currency.name], default: "Company ${companyInstance.toString()} created. NOTE that if you intend to use foreign currencies, you should now set the exchange rate for ${currency.name}.")
        }

        utilService.newCurrentCompany(companyInstance.id)
    }

    def attach() {
        def companies = CompanyUser.findAllByUser(utilService.currentUser())
        if (!companies) {
            flash.message = message(code: 'companyUser.not.assigned', default: 'We are sorry, but you have still not been assigned to a company. Please contact your company administrator.')
            redirect(action: 'intro')
            return
        } else if (companies.size() == 1) {
            utilService.newCurrentCompany(companies[0].company.id)
        } else {
            utilService.setNextStep([sourceController: 'companyUser', sourceAction: 'select',
                    targetController: 'companyUser', targetAction: 'attach'])
            redirect(controller: 'companyUser', action: 'select')
            return
        }
    }
	
	// Display the /documentation/sample_payment_service.groovy program text from the index page of the application
	def paymentProgram() {
		render(contentType: 'text/plain', encoding: 'UTF-8', text: utilService.realFile('/documentation/PaymentService.groovy').getText('UTF-8'))
	}
	
	// Display the /documentation/rest_test.groovy program text from the index page of the application
	def restProgram() {
		render(contentType: 'text/plain', encoding: 'UTF-8', text: utilService.realFile('/documentation/RestTest.groovy').getText('UTF-8'))
	}
	
	// Display the /documentation/NOTICE text file from the index page of the application
	def noticeFile() {
		render(contentType: 'text/plain', encoding: 'UTF-8', text: utilService.realFile('/documentation/NOTICE').getText('UTF-8'))
	}
	
	// Display the /documentation/LICENSE text file from the index page of the application
	def licenseFile() {
		render(contentType: 'text/plain', encoding: 'UTF-8', text: utilService.realFile('/documentation/LICENSE').getText('UTF-8'))
	}
}
