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

class TaskResultController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.sort = ['code', 'sequencer', 'dataType', 'dataScale'].contains(params.sort) ? params.sort : 'code'
        params.max = utilService.max
        def ddSource = utilService.source('task.list')
        [taskResultInstanceList: TaskResult.selectList(securityCode: utilService.currentCompany().securityCode), taskResultInstanceTotal: TaskResult.selectCount(), ddSource: ddSource]
    }

    def show() {
        def taskResultInstance = TaskResult.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)

        if (!taskResultInstance) {
            flash.message = utilService.standardMessage('not.found', 'taskResult', params.id)
            redirect(action: 'list')
        } else {
            return [taskResultInstance : taskResultInstance]
        }
    }

    def delete() {
        def taskResultInstance = TaskResult.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (taskResultInstance) {
            try {
                utilService.deleteWithMessages(taskResultInstance, [prefix: "taskResult.name.${taskResultInstance.task.code}", code: taskResultInstance.code])
                flash.message = utilService.standardMessage('deleted', taskResultInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', taskResultInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'taskResult', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def taskResultInstance = TaskResult.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)

        if (!taskResultInstance) {
            flash.message = utilService.standardMessage('not.found', 'taskResult', params.id)
            redirect(action: 'list')
        } else {
            return [taskResultInstance: taskResultInstance]
        }
    }

    def update(Long version) {
        def taskResultInstance = TaskResult.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if(taskResultInstance) {
            if (version != null && taskResultInstance.version > version) {
                taskResultInstance.errorMessage(code: 'locking.failure', domain: 'taskResult')
                render(view: 'edit', model: [taskResultInstance: taskResultInstance])
                return
            }

            def oldCode = taskResultInstance.code
            taskResultInstance.properties['code', 'name', 'sequencer', 'dataType', 'dataScale'] = params
            if (utilService.saveWithMessages(taskResultInstance, [prefix: "taskResult.name.${taskResultInstance.task.code}", code: taskResultInstance.code, field: 'name', oldCode: oldCode])) {
                flash.message = utilService.standardMessage('updated', taskResultInstance)
                redirect(action: 'show', id: taskResultInstance.id)
            } else {
                render(view: 'edit', model: [taskResultInstance: taskResultInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'taskResult', params.id)
            redirect(action: 'edit', id: params.id)
        }
    }

    def create() {
        def taskResultInstance = new TaskResult()
        taskResultInstance.task = utilService.reSource('task.list')
        return ['taskResultInstance': taskResultInstance]
    }

    def save() {
        def taskResultInstance = new TaskResult()
        taskResultInstance.properties['code', 'name', 'sequencer', 'dataType', 'dataScale'] = params
        taskResultInstance.task = utilService.reSource('task.list')
        if (utilService.saveWithMessages(taskResultInstance, [prefix: "taskResult.name.${taskResultInstance.task?.code}", code: taskResultInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', taskResultInstance)
            redirect(action: 'show', id: taskResultInstance.id)
        } else {
            render(view: 'create', model: [taskResultInstance: taskResultInstance])
        }
    }
}