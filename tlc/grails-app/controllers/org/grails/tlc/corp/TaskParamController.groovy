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

class TaskParamController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.sort = ['code', 'sequencer', 'dataType', 'dataScale', 'defaultValue', 'required'].contains(params.sort) ? params.sort : 'code'
        params.max = utilService.max
        def ddSource = utilService.source('task.list')
        [taskParamInstanceList: TaskParam.selectList(securityCode: utilService.currentCompany().securityCode), taskParamInstanceTotal: TaskParam.selectCount(), ddSource: ddSource]
    }

    def show() {
        def taskParamInstance = TaskParam.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)

        if (!taskParamInstance) {
            flash.message = utilService.standardMessage('not.found', 'taskParam', params.id)
            redirect(action: 'list')
        } else {
            return [taskParamInstance: taskParamInstance]
        }
    }

    def delete() {
        def taskParamInstance = TaskParam.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (taskParamInstance) {
            try {
                utilService.deleteWithMessages(taskParamInstance, [prefix: "taskParam.name.${taskParamInstance.task.code}", code: taskParamInstance.code])
                flash.message = utilService.standardMessage('deleted', taskParamInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', taskParamInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'taskParam', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def taskParamInstance = TaskParam.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)

        if (!taskParamInstance) {
            flash.message = utilService.standardMessage('not.found', 'taskParam', params.id)
            redirect(action: 'list')
        } else {
            return [taskParamInstance: taskParamInstance]
        }
    }

    def update(Long version) {
        def taskParamInstance = TaskParam.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (taskParamInstance) {
            if (version != null && taskParamInstance.version > version) {
                taskParamInstance.errorMessage(code: 'locking.failure', domain: 'taskParam')
                render(view: 'edit', model: [taskParamInstance: taskParamInstance])
                return
            }

            def oldCode = taskParamInstance.code
            taskParamInstance.properties['code', 'name', 'sequencer', 'dataType', 'dataScale', 'defaultValue', 'required'] = params
            if (utilService.saveWithMessages(taskParamInstance, [prefix: "taskParam.name.${taskParamInstance.task.code}", code: taskParamInstance.code, field: 'name', oldCode: oldCode])) {
                flash.message = utilService.standardMessage('updated', taskParamInstance)
                redirect(action: 'show', id:taskParamInstance.id)
            } else {
                render(view: 'edit', model: [taskParamInstance: taskParamInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'taskParam', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def taskParamInstance = new TaskParam()
        taskParamInstance.task = utilService.reSource('task.list')
        return ['taskParamInstance': taskParamInstance]
    }

    def save() {
        def taskParamInstance = new TaskParam()
        taskParamInstance.properties['code', 'name', 'sequencer', 'dataType', 'dataScale', 'defaultValue', 'required'] = params
        taskParamInstance.task = utilService.reSource('task.list')
        if (utilService.saveWithMessages(taskParamInstance, [prefix: "taskParam.name.${taskParamInstance.task?.code}", code: taskParamInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', taskParamInstance)
            redirect(action: 'show', id: taskParamInstance.id)
        } else {
            render(view: 'create', model: [taskParamInstance: taskParamInstance])
        }
    }
}