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

import org.grails.tlc.books.Customer
import org.grails.tlc.books.CustomerAddress
import org.grails.tlc.books.CustomerContact
import org.grails.tlc.corp.Company

class SystemAddressFormatController {

    // Injected services
    def utilService
    def addressService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', testing: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = 'code'
        [systemAddressFormatInstanceList: SystemAddressFormat.selectList(), systemAddressFormatInstanceTotal: SystemAddressFormat.selectCount()]
    }

    def show() {
        def systemAddressFormatInstance = SystemAddressFormat.get(params.id)
        if (!systemAddressFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemAddressFormat', params.id)
            redirect(action: 'list')
        } else {
            return [systemAddressFormatInstance: systemAddressFormatInstance]
        }
    }

    def delete() {
        def systemAddressFormatInstance = SystemAddressFormat.get(params.id)
        if (systemAddressFormatInstance) {
            try {
                utilService.deleteWithMessages(systemAddressFormatInstance, [prefix: 'systemAddressFormat.name', code: systemAddressFormatInstance.code])
                flash.message = utilService.standardMessage('deleted', systemAddressFormatInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemAddressFormatInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemAddressFormat', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemAddressFormatInstance = SystemAddressFormat.get(params.id)
        if (!systemAddressFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemAddressFormat', params.id)
            redirect(action: 'list')
        } else {
            return [systemAddressFormatInstance: systemAddressFormatInstance, fieldList: createFieldList(), promptList: createPromptList()]
        }
    }

    def update(Long version) {
        def systemAddressFormatInstance = SystemAddressFormat.get(params.id)
        if (systemAddressFormatInstance) {
            if (version != null && systemAddressFormatInstance.version > version) {
                systemAddressFormatInstance.errorMessage(code: 'locking.failure', domain: 'systemAddressFormat')
                render(view: 'edit', model: [systemAddressFormatInstance: systemAddressFormatInstance, fieldList: createFieldList(), promptList: createPromptList()])
                return
            }

            def oldCode = systemAddressFormatInstance.code
            systemAddressFormatInstance.properties['code', 'name', 'field1', 'field1Prompt1', 'field1Prompt2', 'field1Prompt3', 'width1', 'mandatory1', 'pattern1', 'field2', 'field2Prompt1',
                'field2Prompt2', 'field2Prompt3', 'width2', 'mandatory2', 'pattern2', 'joinBy2', 'field3', 'field3Prompt1', 'field3Prompt2', 'field3Prompt3', 'width3', 'mandatory3',
                'pattern3', 'joinBy3', 'field4', 'field4Prompt1', 'field4Prompt2', 'field4Prompt3', 'width4', 'mandatory4', 'pattern4', 'joinBy4', 'field5', 'field5Prompt1', 'field5Prompt2',
                'field5Prompt3', 'width5', 'mandatory5', 'pattern5', 'joinBy5', 'field6', 'field6Prompt1', 'field6Prompt2', 'field6Prompt3', 'width6', 'mandatory6', 'pattern6', 'joinBy6',
                'field7', 'field7Prompt1', 'field7Prompt2', 'field7Prompt3', 'width7', 'mandatory7', 'pattern7', 'joinBy7', 'field8', 'field8Prompt1', 'field8Prompt2', 'field8Prompt3',
                'width8', 'mandatory8', 'pattern8', 'joinBy8', 'field9', 'field9Prompt1', 'field9Prompt2', 'field9Prompt3', 'width9', 'mandatory9', 'pattern9', 'joinBy9', 'field10',
                'field10Prompt1', 'field10Prompt2', 'field10Prompt3', 'width10', 'mandatory10', 'pattern10', 'joinBy10', 'field11', 'field11Prompt1', 'field11Prompt2', 'field11Prompt3',
                'width11', 'mandatory11', 'pattern11', 'joinBy11', 'field12', 'field12Prompt1', 'field12Prompt2', 'field12Prompt3', 'width12', 'mandatory12', 'pattern12', 'joinBy12'] = params

            if (utilService.saveWithMessages(systemAddressFormatInstance, [prefix: 'systemAddressFormat.name', code: systemAddressFormatInstance.code, oldCode: oldCode, field: 'name'])) {
                flash.message = utilService.standardMessage('updated', systemAddressFormatInstance)
                redirect(action: 'show', id: systemAddressFormatInstance.id)
            } else {
                render(view: 'edit', model: [systemAddressFormatInstance: systemAddressFormatInstance, fieldList: createFieldList(), promptList: createPromptList()])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemAddressFormat', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemAddressFormatInstance = new SystemAddressFormat()
        return [systemAddressFormatInstance: systemAddressFormatInstance, fieldList: createFieldList(), promptList: createPromptList()]
    }

    def save() {
        def systemAddressFormatInstance = new SystemAddressFormat()
        systemAddressFormatInstance.properties['code', 'name', 'field1', 'field1Prompt1', 'field1Prompt2', 'field1Prompt3', 'width1', 'mandatory1', 'pattern1', 'field2', 'field2Prompt1',
            'field2Prompt2', 'field2Prompt3', 'width2', 'mandatory2', 'pattern2', 'joinBy2', 'field3', 'field3Prompt1', 'field3Prompt2', 'field3Prompt3', 'width3', 'mandatory3',
            'pattern3', 'joinBy3', 'field4', 'field4Prompt1', 'field4Prompt2', 'field4Prompt3', 'width4', 'mandatory4', 'pattern4', 'joinBy4', 'field5', 'field5Prompt1', 'field5Prompt2',
            'field5Prompt3', 'width5', 'mandatory5', 'pattern5', 'joinBy5', 'field6', 'field6Prompt1', 'field6Prompt2', 'field6Prompt3', 'width6', 'mandatory6', 'pattern6', 'joinBy6',
            'field7', 'field7Prompt1', 'field7Prompt2', 'field7Prompt3', 'width7', 'mandatory7', 'pattern7', 'joinBy7', 'field8', 'field8Prompt1', 'field8Prompt2', 'field8Prompt3',
            'width8', 'mandatory8', 'pattern8', 'joinBy8', 'field9', 'field9Prompt1', 'field9Prompt2', 'field9Prompt3', 'width9', 'mandatory9', 'pattern9', 'joinBy9', 'field10',
            'field10Prompt1', 'field10Prompt2', 'field10Prompt3', 'width10', 'mandatory10', 'pattern10', 'joinBy10', 'field11', 'field11Prompt1', 'field11Prompt2', 'field11Prompt3',
            'width11', 'mandatory11', 'pattern11', 'joinBy11', 'field12', 'field12Prompt1', 'field12Prompt2', 'field12Prompt3', 'width12', 'mandatory12', 'pattern12', 'joinBy12'] = params

        if (utilService.saveWithMessages(systemAddressFormatInstance, [prefix: 'systemAddressFormat.name', code: systemAddressFormatInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', systemAddressFormatInstance)
            redirect(action: 'show', id: systemAddressFormatInstance.id)
        } else {
            render(view: 'create', model: [systemAddressFormatInstance: systemAddressFormatInstance, fieldList: createFieldList(), promptList: createPromptList()])
        }
    }

    def test() {
        def customerAddressInstance = new CustomerAddress()
        customerAddressInstance.country = utilService.currentCompany().country
        customerAddressInstance.format = customerAddressInstance.country.addressFormat
        [customerAddressInstance: customerAddressInstance, customerAddressLines: addressService.getAsLineMaps(customerAddressInstance), transferList: null]
    }

    def testing() {
        def model
        def customerAddressInstance = new CustomerAddress()
        customerAddressInstance.properties['location1', 'location2', 'location3', 'metro1', 'metro2', 'area1', 'area2', 'encoding', 'country', 'format'] = params
        if (!params.retest) {
            if (params.modified) {
                if (params.modified == 'country') customerAddressInstance.format = SystemCountry.get(params.country.id).addressFormat
            } else {
                customerAddressInstance.customer = new Customer()
                if (addressService.validate(customerAddressInstance)) {
                    def receivingCompany = new Company(name: message(code: 'systemAddressFormat.dummy.company', default: 'Test Company Inc.'))
                    def receivingContact = new CustomerContact(name: message(code: 'systemAddressFormat.dummy.contact', default: 'John Doe'),
                            identifier: message(code: 'systemAddressFormat.dummy.identifier', default: 'Chief Accountant'))
                    def sendingCountry = new SystemCountry()
                    model = [customerAddressInstance: customerAddressInstance, result: addressService.formatAddress(customerAddressInstance, receivingCompany, receivingContact, sendingCountry)]
                }
            }
        }

        if (!model) model = [customerAddressInstance: customerAddressInstance, customerAddressLines: addressService.getAsLineMaps(customerAddressInstance), transferList: null]

        render(view: 'test', model: model)
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private createFieldList() {
        def list = []
        for (it in SystemAddressFormat.fieldTypes) list << [code: it, name: message(code: 'systemAddressFormat.field.' + it, default: it)]
        utilService.collate(list) {it.name}
        return list
    }

    private createPromptList() {
        def list = []
        for (it in SystemAddressFormat.fieldPrompts) list << [code: it, name: message(code: 'address.prompt.' + it, default: it)]
        utilService.collate(list) {it.name}
        return list
    }
}