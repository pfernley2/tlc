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
import org.grails.tlc.corp.Setting

class SystemSettingController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', propagate: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.sort = ['code', 'dataScale', 'value', 'systemOnly'].contains(params.sort) ? params.sort : 'code'
        params.max = utilService.max
        [systemSettingInstanceList: SystemSetting.selectList(), systemSettingInstanceTotal: SystemSetting.selectCount()]
    }

    def show() {
        def systemSettingInstance = SystemSetting.get(params.id)

        if (!systemSettingInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemSetting', params.id)
            redirect(action: 'list')
        } else {
            return [systemSettingInstance: systemSettingInstance]
        }
    }

    def delete() {
        def systemSettingInstance = SystemSetting.get(params.id)
        if (systemSettingInstance) {
            try {
                systemSettingInstance.delete(flush: true)
                utilService.cacheService.resetThis('setting', 0L, systemSettingInstance.code)
                flash.message = utilService.standardMessage('deleted', systemSettingInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemSettingInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemSetting', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemSettingInstance = SystemSetting.get(params.id)

        if (!systemSettingInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemSetting', params.id)
            redirect(action: 'list')
        } else {
            return [systemSettingInstance: systemSettingInstance]
        }
    }

    def update(Long version) {
        def systemSettingInstance = SystemSetting.get(params.id)
        if (systemSettingInstance) {
            if (version != null && systemSettingInstance.version > version) {
                systemSettingInstance.errorMessage(code: 'locking.failure', domain: 'systemSetting')
                render(view: 'edit', model: [systemSettingInstance: systemSettingInstance])
                return
            }

            def oldCode = systemSettingInstance.code
            systemSettingInstance.properties['code', 'dataType', 'dataScale', 'value', 'systemOnly'] = params
            if (!systemSettingInstance.hasErrors() && systemSettingInstance.saveThis()) {
                utilService.cacheService.resetThis('setting', 0L, oldCode)
                if (systemSettingInstance.code != oldCode) utilService.cacheService.resetThis('setting', 0L, systemSettingInstance.code)
                flash.message = utilService.standardMessage('updated', systemSettingInstance)
                redirect(action: 'show', id: systemSettingInstance.id)
            } else {
                render(view: 'edit', model: [systemSettingInstance: systemSettingInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemSetting', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        return ['systemSettingInstance': new SystemSetting()]
    }

    def save() {
        def systemSettingInstance = new SystemSetting()
        systemSettingInstance.properties['code', 'dataType', 'dataScale', 'value', 'systemOnly'] = params
        if (!systemSettingInstance.hasErrors() && systemSettingInstance.saveThis()) {
            utilService.cacheService.resetThis('setting', 0L, systemSettingInstance.code)
            flash.message = utilService.standardMessage('created', systemSettingInstance)
            redirect(action: 'show', id: systemSettingInstance.id)
        } else {
            render(view: 'create', model: [systemSettingInstance: systemSettingInstance])
        }
    }

    def propagate() {
        def count = 0
        def systemSettingInstance = SystemSetting.get(params.id)
        if (systemSettingInstance && !systemSettingInstance.systemOnly) {
            def setting
            def companies = Company.list()
            for (company in companies) {
                setting = Setting.findByCompanyAndCode(company, systemSettingInstance.code)
                if (!setting) {
                    setting = new Setting(company: company, code: systemSettingInstance.code, dataType: systemSettingInstance.dataType,
                            dataScale: systemSettingInstance.dataScale, value: systemSettingInstance.value)
                    if (setting.saveThis()) count++
                }
            }
        }

        flash.message = message(code: 'systemSetting.propagated', args: ["${count}"], default: "${count} company/companies updated")
        redirect(action: 'list')
    }
}