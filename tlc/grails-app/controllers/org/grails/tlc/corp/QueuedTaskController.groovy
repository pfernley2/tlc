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

import org.grails.tlc.sys.SystemUser
import org.grails.tlc.sys.TaskExecutor
import org.grails.tlc.sys.TaskService

class QueuedTaskController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin', queue: 'sysadmin', start: 'sysadmin', stop: 'sysadmin', pause: 'sysadmin', resize: 'sysadmin',
        queueShow: 'sysadmin', queueDelete: 'sysadmin', queueEdit: 'sysadmin', queueUpdate: 'sysadmin', queueRerun: 'sysadmin',
        usrList: 'attached', usrShow: 'attached', usrDelete: 'attached', usrEdit: 'attached', usrUpdate: 'attached']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', update: 'POST', start: 'POST', stop: 'POST', pause: 'POST', resize: 'POST',
        queueDelete: 'POST', queueUpdate: 'POST', usrDelete: 'POST', usrUpdate: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        if (!['dateCreated', 'scheduled', 'preferredStart', 'startedAt', 'completedAt', 'completionMessage'].contains(params.sort)) {
            params.sort = 'dateCreated'
            params.order = 'desc'
        }
        params.max = utilService.max
        def queueStatus = utilService.taskService.statistics()
        if (queueStatus) queueStatus.status = message(code: "queuedTask.queue.status.${queueStatus.status}", default: queueStatus.status)
        [queuedTaskInstanceList: QueuedTask.selectList(securityCode: utilService.currentCompany().securityCode), queuedTaskInstanceTotal: QueuedTask.selectCount(), queueStatus: queueStatus]
    }

    def show() {
        def queuedTaskInstance = QueuedTask.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)

        if (!queuedTaskInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'list')
        } else {
            return [queuedTaskInstance: queuedTaskInstance]
        }
    }

    def delete() {
        def queuedTaskInstance = QueuedTask.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (queuedTaskInstance) {
            if (queuedTaskInstance.currentStatus == 'waiting') {
                try {
                    queuedTaskInstance.delete(flush: true)
                    flash.message = utilService.standardMessage('deleted', queuedTaskInstance)
                    redirect(action: 'list')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', queuedTaskInstance)
                    redirect(action: 'show', id: params.id)
                }
            } else {
                flash.message = message(code: 'queuedTask.wait.delete', default: 'Only waiting tasks can be deleted')
                redirect(action: 'list')
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def queuedTaskInstance = QueuedTask.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)

        if (!queuedTaskInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'list')
        } else if (queuedTaskInstance.currentStatus != 'waiting') {
            flash.message = message(code: 'queuedTask.wait.edit', default: 'Only waiting tasks can be edited')
            redirect(action: 'list')
        } else {
            return [queuedTaskInstance: queuedTaskInstance, companyUserList: utilService.currentCompanyUserList()]
        }
    }

    def update(Long version) {
        def queuedTaskInstance = QueuedTask.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (queuedTaskInstance) {
            if (queuedTaskInstance.currentStatus != 'waiting') {
                flash.message = message(code: 'queuedTask.wait.edit', default: 'Only waiting tasks can be edited')
                redirect(action: 'list')
                return
            }

            if (version != null && queuedTaskInstance.version > version) {
                queuedTaskInstance.errorMessage(code: 'locking.failure', domain: 'queuedTask')
                render(view: 'edit', model: [queuedTaskInstance: queuedTaskInstance])
                return
            }

            def oldUser = queuedTaskInstance.user
            queuedTaskInstance.properties['user', 'preferredStart'] = params
            def valid = !queuedTaskInstance.hasErrors()
            if (valid && queuedTaskInstance.user.id != oldUser.id) {
                if (CompanyUser.countByCompanyAndUser(utilService.currentCompany(), queuedTaskInstance.user) == 0) {
                    queuedTaskInstance.errorMessage(field: 'user', code: 'queuedTask.no.combo', default: 'Invalid company and user combination')
                    valid = false
                }
            }

            if (valid && queuedTaskInstance.preferredStart &&
            (queuedTaskInstance.preferredStart.getTime() < System.currentTimeMillis() - 60000L || queuedTaskInstance.preferredStart > new Date() + 365)) {
                queuedTaskInstance.errorMessage(field: 'preferredStart', code: 'queuedTask.preferredStart.invalid', default: 'Invalid preferred start date and time')
                valid = false
            }

            if (valid) valid = queuedTaskInstance.saveThis()
            if (valid) {
                flash.message = utilService.standardMessage('updated', queuedTaskInstance)
                redirect(action: 'show', id: queuedTaskInstance.id)
            } else {
                render(view: 'edit', model: [queuedTaskInstance: queuedTaskInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'list')
        }
    }

    // System administrators view of the whole queue
    def queue() {
        if (!['dateCreated', 'scheduled', 'preferredStart', 'startedAt', 'completedAt', 'completionMessage'].contains(params.sort)) {
            params.sort = 'dateCreated'
            params.order = 'desc'
        }
        params.max = utilService.max
        def queueStatus = utilService.taskService.statistics()
        if (queueStatus) {

            // Translate the status message in to a new entry within the map
            queueStatus.statusText = message(code: "queuedTask.queue.status.${queueStatus.status}", default: queueStatus.status)

            // Create a size range for the thread pool
            queueStatus.sizeRange = 1..utilService.taskService.poolSizeLimit()

            // Stop the executor if the scanner is not running
            if (queueStatus.status == TaskExecutor.QUEUE_HALTED) TaskExecutor.stop()
        }

        [queuedTaskInstanceList: QueuedTask.selectList(), queuedTaskInstanceTotal: QueuedTask.selectCount(), queueStatus: queueStatus]
    }

    def start() {
        def status = utilService.taskService.queueStatus()
        if (status == TaskExecutor.QUEUE_STOPPED) {
            if (TaskService.start()) {
                flash.message = message(code: 'queuedTask.sys.started', default: 'Task queue started')
            } else {
                flash.message = message(code: 'queuedTask.sys.not.started', default: 'Task queue NOT started')
            }
        } else if (status == TaskExecutor.QUEUE_PAUSED) {
            if (utilService.taskService.resume()) {
                flash.message = message(code: 'queuedTask.sys.resumed', default: 'Task queue processing resumed')
            } else {
                flash.message = message(code: 'queuedTask.sys.not.resumed', default: 'Task queue processing NOT resumed')
            }
        } else {
            flash.message = message(code: 'queuedTask.sys.no.startup', default: 'Task queue is already running')
        }

        redirect(action: 'queue')
    }

    def stop() {
        if (TaskService.stop()) {
            flash.message = message(code: 'queuedTask.sys.stopped', default: 'Task queue stopped')
        } else {
            flash.message = message(code: 'queuedTask.sys.not.stopped', default: 'Task queue NOT stopped')
        }

        redirect(action: 'queue')
    }

    def pause() {
        if (utilService.taskService.pause()) {
            flash.message = message(code: 'queuedTask.sys.paused', default: 'Task queue paused')
        } else {
            flash.message = message(code: 'queuedTask.sys.not.paused', default: 'Task queue NOT paused')
        }

        redirect(action: 'queue')
    }

    def resize() {
        if (params.newSize && utilService.taskService.resize(params.newSize.toInteger())) {
            flash.message = message(code: 'queuedTask.sys.resized', default: 'Task queue resized')
        } else {
            flash.message = message(code: 'queuedTask.sys.not.resized', default: 'Task queue NOT resized')
        }

        redirect(action: 'queue')
    }

    def queueShow() {
        def queuedTaskInstance = QueuedTask.get(params.id)

        if (!queuedTaskInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'list')
        } else {
            return [queuedTaskInstance: queuedTaskInstance]
        }
    }

    def queueDelete() {
        def queuedTaskInstance = QueuedTask.get(params.id)
        if (queuedTaskInstance) {
            if (queuedTaskInstance.currentStatus == 'waiting') {
                try {
                    queuedTaskInstance.delete(flush: true)
                    flash.message = utilService.standardMessage('deleted', queuedTaskInstance)
                    redirect(action: 'queue')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', queuedTaskInstance)
                    redirect(action: 'queueShow', id: params.id)
                }
            } else {
                flash.message = message(code: 'queuedTask.wait.delete', default: 'Only waiting tasks can be deleted')
                redirect(action: 'queue')
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'queue')
        }
    }

    def queueEdit() {
        def queuedTaskInstance = QueuedTask.get(params.id)

        if (!queuedTaskInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'queue')
        } else if (queuedTaskInstance.currentStatus != 'waiting') {
            flash.message = message(code: 'queuedTask.wait.edit', default: 'Only waiting tasks can be edited')
            redirect(action: 'queue')
        } else {
            def cul = SystemUser.findAll('from SystemUser as x where x.id in (select y.user.id from CompanyUser as y where y.company = ?) order by x.name', [queuedTaskInstance.task.company])
            return [queuedTaskInstance: queuedTaskInstance, companyUserList: cul]
        }
    }

    def queueUpdate(Long version) {
        def queuedTaskInstance = QueuedTask.get(params.id)
        if (queuedTaskInstance) {
            if (queuedTaskInstance.currentStatus != 'waiting') {
                flash.message = message(code: 'queuedTask.wait.edit', default: 'Only waiting tasks can be edited')
                redirect(action: 'queue')
                return
            }

            if (version != null && queuedTaskInstance.version > version) {
                queuedTaskInstance.errorMessage(code: 'locking.failure', domain: 'queuedTask')
                render(view: 'queueEdit', model: [queuedTaskInstance: queuedTaskInstance])
                return
            }

            def oldUser = queuedTaskInstance.user
            queuedTaskInstance.properties['user', 'preferredStart'] = params
            def valid = !queuedTaskInstance.hasErrors()
            if (valid && queuedTaskInstance.user.id != oldUser.id) {
                if (CompanyUser.countByCompanyAndUser(queuedTaskInstance.task.company, queuedTaskInstance.user) == 0) {
                    queuedTaskInstance.errorMessage(field: 'user', code: 'queuedTask.no.combo', default: 'Invalid company and user combination')
                    valid = false
                }
            }

            if (valid && queuedTaskInstance.preferredStart &&
            (queuedTaskInstance.preferredStart.getTime() < System.currentTimeMillis() - 60000L || queuedTaskInstance.preferredStart > new Date() + 365)) {
                queuedTaskInstance.errorMessage(field: 'preferredStart', code: 'queuedTask.preferredStart.invalid', default: 'Invalid preferred start date and time')
                valid = false
            }

            if (valid) valid = queuedTaskInstance.saveThis()
            if (valid) {
                flash.message = utilService.standardMessage('updated', queuedTaskInstance)
                redirect(action: 'queueShow', id: queuedTaskInstance.id)
            } else {
                render(view: 'queueEdit', model: [queuedTaskInstance: queuedTaskInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'queue')
        }
    }

    def queueRerun() {
        def queuedTaskInstance = QueuedTask.get(params.id)
        if (queuedTaskInstance) {
            if (queuedTaskInstance.currentStatus == 'waiting' || queuedTaskInstance.currentStatus == 'running') {
                flash.message = message(code: 'queuedTask.no.rerun', default: 'Tasks that are waiting or running can not be rerun')
                redirect(action: 'queueShow', id: queuedTaskInstance.id)
            } else {
                def list = []
                for (p in queuedTaskInstance.parameters) list << [param: p.param, value: p.value]
                if (utilService.taskService.submit(queuedTaskInstance.task, list, queuedTaskInstance.user)) {
                    flash.message = message(code: 'queuedTask.good.rerun', default: 'Rerun of this task successfully added to the queue')
                    redirect(action: 'queue')
                } else {
                    flash.message = message(code: 'queuedTask.bad.rerun', default: 'Unable to rerun this task')
                    redirect(action: 'queueShow', id: queuedTaskInstance.id)

                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'queue')
        }
    }

    def usrList() {
        if (!['dateCreated', 'scheduled', 'preferredStart', 'startedAt', 'completedAt', 'completionMessage'].contains(params.sort)) {
            params.sort = 'dateCreated'
            params.order = 'desc'
        }
        params.max = utilService.max
        def queueStatus = utilService.taskService.statistics()
        if (queueStatus) queueStatus.status = message(code: "queuedTask.queue.status.${queueStatus.status}", default: queueStatus.status)
        def list = QueuedTask.selectList(securityCode: utilService.currentCompany().securityCode, where: 'x.user = ?', params: utilService.currentUser())
        def total = QueuedTask.selectCount()
        [queuedTaskInstanceList: list, queuedTaskInstanceTotal: total, queueStatus: queueStatus]
    }

    def usrShow() {
        def queuedTaskInstance = QueuedTask.find('from QueuedTask as x where x.id = ? and x.user = ? and x.securityCode = ?',
                [params.id?.toLong(), utilService.currentUser(), utilService.currentCompany().securityCode])

        if (!queuedTaskInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'usrList')
        } else {
            return [queuedTaskInstance: queuedTaskInstance]
        }
    }

    def usrDelete() {
        def queuedTaskInstance = QueuedTask.find('from QueuedTask as x where x.id = ? and x.user = ? and x.securityCode = ?',
                [params.id?.toLong(), utilService.currentUser(), utilService.currentCompany().securityCode])
        if (queuedTaskInstance) {
            if (queuedTaskInstance.currentStatus == 'waiting') {
                try {
                    queuedTaskInstance.delete(flush: true)
                    flash.message = utilService.standardMessage('deleted', queuedTaskInstance)
                    redirect(action: 'usrList')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', queuedTaskInstance)
                    redirect(action: 'usrShow', id: params.id)
                }
            } else {
                flash.message = message(code: 'queuedTask.wait.delete', default: 'Only waiting tasks can be deleted')
                redirect(action: 'usrList')
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'usrList')
        }
    }

    def usrEdit() {
        def queuedTaskInstance = QueuedTask.find('from QueuedTask as x where x.id = ? and x.user = ? and x.securityCode = ?',
                [params.id?.toLong(), utilService.currentUser(), utilService.currentCompany().securityCode])

        if (!queuedTaskInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'usrList')
        } else if (queuedTaskInstance.currentStatus != 'waiting') {
            flash.message = message(code: 'queuedTask.wait.edit', default: 'Only waiting tasks can be edited')
            redirect(action: 'usrList')
        } else {
            return [queuedTaskInstance: queuedTaskInstance, companyUserList: utilService.currentCompanyUserList()]
        }
    }

    def usrUpdate(Long version) {
        def queuedTaskInstance = QueuedTask.find('from QueuedTask as x where x.id = ? and x.user = ? and x.securityCode = ?',
                [params.id?.toLong(), utilService.currentUser(), utilService.currentCompany().securityCode])
        if (queuedTaskInstance) {
            if (queuedTaskInstance.currentStatus != 'waiting') {
                flash.message = message(code: 'queuedTask.wait.edit', default: 'Only waiting tasks can be edited')
                redirect(action: 'usrList')
                return
            }

            if (version != null && queuedTaskInstance.version > version) {
                queuedTaskInstance.errorMessage(code: 'locking.failure', domain: 'queuedTask')
                render(view: 'usrEdit', model: [queuedTaskInstance: queuedTaskInstance])
                return
            }

            // The completion message parameter is not actually required below, but Grails seems to be
            // having a problem when only one property is specified in the list
            queuedTaskInstance.properties['completionMessage', 'preferredStart'] = params
            def valid = !queuedTaskInstance.hasErrors()
            if (valid && queuedTaskInstance.preferredStart &&
            (queuedTaskInstance.preferredStart.getTime() < System.currentTimeMillis() - 60000L || queuedTaskInstance.preferredStart > new Date() + 365)) {
                queuedTaskInstance.errorMessage(field: 'preferredStart', code: 'queuedTask.preferredStart.invalid', default: 'Invalid preferred start date and time')
                valid = false
            }

            if (valid) valid = queuedTaskInstance.saveThis()
            if (valid) {
                flash.message = utilService.standardMessage('updated', queuedTaskInstance)
                redirect(action: 'usrShow', id: queuedTaskInstance.id)
            } else {
                render(view: 'usrEdit', model: [queuedTaskInstance: queuedTaskInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'queuedTask', params.id)
            redirect(action: 'usrList')
        }
    }
}