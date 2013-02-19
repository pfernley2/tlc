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

class QueuedTaskParamController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin', queueList: 'sysadmin', queueShow: 'sysadmin', queueEdit: 'sysadmin', queueUpdate: 'sysadmin',
            usrList: 'attached', usrShow: 'attached', usrEdit: 'attached', usrUpdate: 'attached']

    // List of actions with specific request types
    static allowedMethods = [update: 'POST', queueUpdate: 'POST', usrUpdate: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.sort = ['value'].contains(params.sort) ? params.sort : 'id'
        params.max = utilService.max
        def ddSource = utilService.source('queuedTask.list')
        [queuedTaskParamInstanceList: QueuedTaskParam.selectList(securityCode: utilService.currentCompany().securityCode), queuedTaskParamInstanceTotal: QueuedTaskParam.selectCount(), ddSource: ddSource]
    }

    def show() {
        def queuedTaskParamInstance = QueuedTaskParam.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)

        if (!queuedTaskParamInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskParam', params.id)
            redirect(action: 'list')
        } else {
            return [queuedTaskParamInstance: queuedTaskParamInstance]
        }
    }

    def edit() {
        def queuedTaskParamInstance = QueuedTaskParam.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)

        if (!queuedTaskParamInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskParam', params.id)
            redirect(action: 'list')
        } else if (queuedTaskParamInstance.queued.currentStatus != 'waiting') {
            flash.message = message(code: 'queuedTaskParam.wait.edit', default: 'Only waiting task parameters can be edited')
            redirect(action: 'list')
        } else {
            return [queuedTaskParamInstance: queuedTaskParamInstance]
        }
    }

    def update(Long version) {
        def queuedTaskParamInstance = QueuedTaskParam.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (queuedTaskParamInstance) {
            if (queuedTaskParamInstance.queued.currentStatus != 'waiting') {
                flash.message = message(code: 'queuedTaskParam.wait.edit', default: 'Only waiting task parameters can be edited')
                redirect(action: 'list')
                return
            }

            if (version != null && queuedTaskParamInstance.version > version) {
                queuedTaskParamInstance.errorMessage(code: 'locking.failure', domain: 'queuedTaskParam')
                render(view: 'edit', model: [queuedTaskParamInstance: queuedTaskParamInstance])
                return
            }

            queuedTaskParamInstance.properties['value'] = params
            if (!queuedTaskParamInstance.hasErrors() && queuedTaskParamInstance.saveThis()) {
                flash.message = utilService.standardMessage('updated', queuedTaskParamInstance)
                redirect(action: 'show', id: queuedTaskParamInstance.id)
            } else {
                render(view: 'edit', model: [queuedTaskParamInstance: queuedTaskParamInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskParam', params.id)
            redirect(action: 'list')
        }
    }

    def queueList() {
        params.sort = ['value'].contains(params.sort) ? params.sort : 'id'
        params.max = utilService.max
        def ddSource = utilService.source('queuedTask.queue')
        [queuedTaskParamInstanceList: QueuedTaskParam.selectList(), queuedTaskParamInstanceTotal: QueuedTaskParam.selectCount(), ddSource: ddSource]
    }

    def queueShow() {
        def queuedTaskParamInstance = QueuedTaskParam.get(params.id)

        if (!queuedTaskParamInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskParam', params.id)
            redirect(action: 'queueList')
        } else {
            return [queuedTaskParamInstance: queuedTaskParamInstance]
        }
    }

    def queueEdit() {
        def queuedTaskParamInstance = QueuedTaskParam.get(params.id)

        if (!queuedTaskParamInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskParam', params.id)
            redirect(action: 'queueList')
        } else if (queuedTaskParamInstance.queued.currentStatus != 'waiting') {
            flash.message = message(code: 'queuedTaskParam.wait.edit', default: 'Only waiting task parameters can be edited')
            redirect(action: 'queueList')
        } else {
            return [queuedTaskParamInstance: queuedTaskParamInstance]
        }
    }

    def queueUpdate(Long version) {
        def queuedTaskParamInstance = QueuedTaskParam.get(params.id)
        if (queuedTaskParamInstance) {
            if (queuedTaskParamInstance.queued.currentStatus != 'waiting') {
                flash.message = message(code: 'queuedTaskParam.wait.edit', default: 'Only waiting task parameters can be edited')
                redirect(action: 'queueList')
                return
            }

            if (version != null && queuedTaskParamInstance.version > version) {
                queuedTaskParamInstance.errorMessage(code: 'locking.failure', domain: 'queuedTaskParam')
                render(view: 'queueEdit', model: [queuedTaskParamInstance: queuedTaskParamInstance])
                return
            }

            queuedTaskParamInstance.properties['value'] = params
            if (!queuedTaskParamInstance.hasErrors() && queuedTaskParamInstance.saveThis()) {
                flash.message = utilService.standardMessage('updated', queuedTaskParamInstance)
                redirect(action: 'queueShow', id: queuedTaskParamInstance.id)
            } else {
                render(view: 'queueEdit', model: [queuedTaskParamInstance: queuedTaskParamInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskParam', params.id)
            redirect(action: 'queueList')
        }
    }

    def usrList() {
        params.sort = ['value'].contains(params.sort) ? params.sort : 'id'
        params.max = utilService.max
        def ddSource = utilService.source('queuedTask.usrList')
        def list = QueuedTaskParam.selectList(securityCode: utilService.currentCompany().securityCode, where: 'x.queued.user = ?', params: utilService.currentUser())
        def total = QueuedTaskParam.selectCount()
        [queuedTaskParamInstanceList: list, queuedTaskParamInstanceTotal: total, ddSource: ddSource]
    }

    def usrShow() {
        def queuedTaskParamInstance = QueuedTaskParam.find('from QueuedTaskParam as x where x.id = ? and x.queued.user = ? and x.securityCode = ?',
                [params.id?.toLong(), utilService.currentUser(), utilService.currentCompany().securityCode])

        if (!queuedTaskParamInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskParam', params.id)
            redirect(action: 'usrList')
        } else {
            return [queuedTaskParamInstance: queuedTaskParamInstance]
        }
    }

    def usrEdit() {
        def queuedTaskParamInstance = QueuedTaskParam.find('from QueuedTaskParam as x where x.id = ? and x.queued.user = ? and x.securityCode = ?',
                [params.id?.toLong(), utilService.currentUser(), utilService.currentCompany().securityCode])

        if (!queuedTaskParamInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskParam', params.id)
            redirect(action: 'usrList')
        } else if (queuedTaskParamInstance.queued.currentStatus != 'waiting') {
            flash.message = message(code: 'queuedTaskParam.wait.edit', default: 'Only waiting task parameters can be edited')
            redirect(action: 'usrList')
        } else {
            return [queuedTaskParamInstance: queuedTaskParamInstance]
        }
    }

    def usrUpdate(Long version) {
        def queuedTaskParamInstance = QueuedTaskParam.find('from QueuedTaskParam as x where x.id = ? and x.queued.user = ? and x.securityCode = ?',
                [params.id?.toLong(), utilService.currentUser(), utilService.currentCompany().securityCode])
        if (queuedTaskParamInstance) {
            if (queuedTaskParamInstance.queued.currentStatus != 'waiting') {
                flash.message = message(code: 'queuedTaskParam.wait.edit', default: 'Only waiting task parameters can be edited')
                redirect(action: 'usrList')
                return
            }

            if (version != null && queuedTaskParamInstance.version > version) {
                queuedTaskParamInstance.errorMessage(code: 'locking.failure', domain: 'queuedTaskParam')
                render(view: 'usrEdit', model: [queuedTaskParamInstance: queuedTaskParamInstance])
                return
            }

            queuedTaskParamInstance.properties['value'] = params
            if (!queuedTaskParamInstance.hasErrors() && queuedTaskParamInstance.saveThis()) {
                flash.message = utilService.standardMessage('updated', queuedTaskParamInstance)
                redirect(action: 'usrShow', id: queuedTaskParamInstance.id)
            } else {
                render(view: 'usrEdit', model: [queuedTaskParamInstance: queuedTaskParamInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskParam', params.id)
            redirect(action: 'usrList')
        }
    }
}