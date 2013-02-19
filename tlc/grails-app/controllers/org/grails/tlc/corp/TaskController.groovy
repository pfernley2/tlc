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

import it.sauronsoftware.cron4j.Predictor

class TaskController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin', propagate: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', propagate: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.sort = ['code', 'executable', 'allowOnDemand', 'schedule', 'nextScheduledRun', 'retentionDays'].contains(params.sort) ? params.sort : 'code'
        params.max = utilService.max
        [taskInstanceList: Task.selectList(company: utilService.currentCompany()), taskInstanceTotal: Task.selectCount()]
    }

    def show() {
        def taskInstance = Task.findByIdAndCompany(params.id, utilService.currentCompany())

        if (!taskInstance) {
            flash.message = utilService.standardMessage('not.found', 'task', params.id)
            redirect(action: 'list')
        } else {
            return [taskInstance: taskInstance]
        }
    }

    def delete() {
        def taskInstance = Task.findByIdAndCompany(params.id, utilService.currentCompany())
        if (taskInstance) {
            if (QueuedTask.countByTaskAndCurrentStatus(taskInstance, 'waiting') > 0) {
                taskInstance.errorMessage(code: 'task.waiting.tasks', args: [taskInstance.name],
                        default: "Task ${taskInstance.name} has entries in the queue awaiting execution. These queue entries must be deleted or executed before continuing.")
                render(view: 'show', model: [taskInstance: taskInstance])
            } else {
                try {
                    utilService.deleteWithMessages(taskInstance, [prefix: 'task.name', code: taskInstance.code, propagate: ['taskParam.name', 'taskResult.name']])
                    flash.message = utilService.standardMessage('deleted', taskInstance)
                    redirect(action: 'list')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', taskInstance)
                    redirect(action: 'show', id: params.id)
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'task', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def taskInstance = Task.findByIdAndCompany(params.id, utilService.currentCompany())

        if (!taskInstance) {
            flash.message = utilService.standardMessage('not.found', 'task', params.id)
            redirect(action: 'list')
        } else {
            return [taskInstance: taskInstance, companyUserList: utilService.currentCompanyUserList()]
        }
    }

    def update(Long version) {
        def taskInstance = Task.findByIdAndCompany(params.id, utilService.currentCompany())
        if (taskInstance) {
            if (version != null && taskInstance.version > version) {
                taskInstance.errorMessage(code: 'locking.failure', domain: 'task')
                render(view: 'edit', model: [taskInstance: taskInstance, companyUserList: utilService.currentCompanyUserList()])
                return
            }

            def oldUser = taskInstance.user
            def oldCode = taskInstance.code
            taskInstance.properties['user', 'activity', 'code', 'name', 'executable', 'allowOnDemand', 'schedule', 'nextScheduledRun', 'retentionDays', 'systemOnly'] = params

            // Fix a wobbly in Grails
            if (!params.user.id) taskInstance.user = null
            if (!params.activity.id) taskInstance.activity = null

            def valid = !taskInstance.hasErrors()

            if (valid && taskInstance.user && taskInstance.user.id != oldUser?.id) {
                if (CompanyUser.countByCompanyAndUser(taskInstance.company, taskInstance.user) == 0) {
                    taskInstance.errorMessage(code: 'task.no.combo', default: "Invalid company and user combination")
                    valid = false
                }
            }

            if (valid) valid = utilService.saveWithMessages(taskInstance, [prefix: 'task.name', code: taskInstance.code, field: 'name', oldCode: oldCode, propagate: ['taskParam.name', 'taskResult.name']])
            if (valid) {
                flash.message = utilService.standardMessage('updated', taskInstance)
                redirect(action: 'show', id: taskInstance.id)
            } else {
                render(view: 'edit', model: [taskInstance: taskInstance, companyUserList: utilService.currentCompanyUserList()])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'task', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def taskInstance = new Task()
        taskInstance.company = utilService.currentCompany()
        return [taskInstance: taskInstance, companyUserList: utilService.currentCompanyUserList()]
    }

    def save() {
        def taskInstance = new Task()
        taskInstance.properties['user', 'activity', 'code', 'name', 'executable', 'allowOnDemand', 'schedule', 'nextScheduledRun', 'retentionDays', 'systemOnly'] = params

        // Fix a wobbly in Grails
        if (!params.user.id) taskInstance.user = null
        if (!params.activity.id) taskInstance.activity = null

        taskInstance.company = utilService.currentCompany()
        def valid = !taskInstance.hasErrors()
        if (valid && taskInstance.user && CompanyUser.countByCompanyAndUser(utilService.currentCompany(), taskInstance.user) == 0) {
            taskInstance.errorMessage(code: 'task.no.combo', default: "Invalid company and user combination")
            valid = false
        }

        if (valid && utilService.saveWithMessages(taskInstance, [prefix: 'task.name', code: taskInstance.code, field: 'name'])) {
            flash.message = utilService.standardMessage('created', taskInstance)
            redirect(action: 'show', id: taskInstance.id)
        } else {
            render(view: 'create', model: [taskInstance: taskInstance, companyUserList: utilService.currentCompanyUserList()])
        }
    }

    def propagate() {
        def count = 0
        def taskInstance = Task.get(params.id)
        if (taskInstance && !taskInstance.systemOnly && taskInstance.activity?.code != 'sysadmin') {
            def parameters = [:]
            def results = [:]
            for (it in taskInstance.parameters) parameters.put(it.code, it)
            for (it in taskInstance.results) results.put(it.code, it)
            def currentCompany = utilService.currentCompany()
            def companies = Company.list()
            for (company in companies) {
                if (company.id != currentCompany.id) count += syncTask(company, taskInstance, parameters, results)
            }
        }

        flash.message = message(code: 'task.propagated', args: ["${count}"], default: "${count} company/companies updated")
        redirect(action: 'list')
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private syncTask(company, srcTask, parameters, results) {
        def childParams
        def childResults
        def tgtTask = Task.findByCompanyAndCode(company, srcTask.code)
        if (tgtTask) {
            childParams = TaskParam.findAllByTask(tgtTask)
            childResults = TaskResult.findAllByTask(tgtTask)
        } else {
            tgtTask = new Task(company: company, code: srcTask.code)
            childParams = []
            childResults = []
        }

        tgtTask.activity = srcTask.activity
        tgtTask.name = srcTask.name
        tgtTask.executable = srcTask.executable
        tgtTask.allowOnDemand = srcTask.allowOnDemand
        tgtTask.activity = srcTask.activity
        tgtTask.retentionDays = srcTask.retentionDays
        if (tgtTask.user && !srcTask.user) {
            tgtTask.user = null
        } else if (!tgtTask.user && srcTask.user) {
            def companyUsers = CompanyUser.findAll('from CompanyUser as x where x.id in (select cu.id from SystemRole as r join r.users as cu where r.code = ? and cu.company.id = ?)',
                    ['companyAdmin', company.id], [max: 1])
            if (!companyUsers) {
                tgtTask.discard()
                return 0
            }

            tgtTask.user = companyUsers[0].user
        }

        if (tgtTask.schedule && !srcTask.schedule) {
            tgtTask.schedule = null
            tgtTask.nextScheduledRun = null
        } else if (!tgtTask.schedule && srcTask.schedule) {
            tgtTask.schedule = (company.id % 60).toString() + srcTask.schedule.substring(srcTask.schedule.indexOf(' '))
            tgtTask.nextScheduledRun = srcTask.nextScheduledRun ? new Predictor(tgtTask.schedule, new Date(System.currentTimeMillis() + 900000L)).nextMatchingDate() : null
        }

        def valid = true
        Task.withTransaction {status ->
            if (utilService.saveWithMessages(tgtTask, [prefix: 'task.name', code: tgtTask.code, field: 'name'], status)) {
                def val, newChild
                def cloned = parameters.clone()
                for (child in childParams) {
                    val = cloned.remove(child.code)
                    if (val) {
                        child.name = val.name
                        child.sequencer = val.sequencer
                        child.dataType = val.dataType
                        child.dataScale = val.dataScale
                        child.defaultValue = val.defaultValue
                        child.required = val.required
                        if (!utilService.saveWithMessages(child, [prefix: "taskParam.name.${tgtTask.code}", code: child.code, field: 'name'], status)) {
                            status.setRollbackOnly()
                            valid = false
                            break
                        }
                    } else {
                        utilService.deleteWithMessages(child, [prefix: "taskParam.name.${tgtTask.code}", code: child.code], status)
                    }
                }

                for (it in cloned) {
                    if (valid) {
                        val = it.value
                        newChild = new TaskParam(task: tgtTask, code: val.code, name: val.name, sequencer: val.sequencer,
                                dataType: val.dataType, dataScale: val.dataScale, defaultValue: val.defaultValue, required: val.required)
                        if (!utilService.saveWithMessages(newChild, [prefix: "taskParam.name.${tgtTask.code}", code: newChild.code, field: 'name'], status)) {
                            status.setRollbackOnly()
                            valid = false
                        }
                    }
                }

                if (valid) {
                    cloned = results.clone()
                    for (child in childResults) {
                        val = cloned.remove(child.code)
                        if (val) {
                            child.name = val.name
                            child.sequencer = val.sequencer
                            child.dataType = val.dataType
                            child.dataScale = val.dataScale
                            if (!utilService.saveWithMessages(child, [prefix: "taskResult.name.${tgtTask.code}", code: child.code, field: 'name'], status)) {
                                status.setRollbackOnly()
                                valid = false
                                break
                            }
                        } else {
                            utilService.deleteWithMessages(child, [prefix: "taskResult.name.${tgtTask.code}", code: child.code], status)
                        }
                    }

                    for (it in cloned) {
                        if (valid) {
                            val = it.value
                            newChild = new TaskResult(task: tgtTask, code: val.code, name: val.name, sequencer: val.sequencer, dataType: val.dataType, dataScale: val.dataScale)
                            if (!utilService.saveWithMessages(newChild, [prefix: "taskResult.name.${tgtTask.code}", code: newChild.code, field: 'name'], status)) {
                                status.setRollbackOnly()
                                valid = false
                            }
                        }
                    }
                }
            } else {
                status.setRollbackOnly()
                valid = false
            }
        }

        return valid ? 1 : 0
    }
}