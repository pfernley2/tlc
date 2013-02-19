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

class QueuedTaskResultController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin', queueList: 'sysadmin', queueShow: 'sysadmin', usrList: 'attached', usrShow: 'attached']

    // List of actions with specific request types
    static allowedMethods = []

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.sort = ['value'].contains(params.sort) ? params.sort : 'id'
        params.max = utilService.max
        def ddSource = utilService.source('queuedTask.list')
        [queuedTaskResultInstanceList: QueuedTaskResult.selectList(securityCode: utilService.currentCompany().securityCode), queuedTaskResultInstanceTotal: QueuedTaskResult.selectCount(), ddSource: ddSource]
    }

    def show() {
        def queuedTaskResultInstance = QueuedTaskResult.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)

        if (!queuedTaskResultInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskResult', params.id)
            redirect(action: 'list')
        } else {
            return [queuedTaskResultInstance: queuedTaskResultInstance]
        }
    }

    def queueList() {
        params.sort = ['value'].contains(params.sort) ? params.sort : 'id'
        params.max = utilService.max
        def ddSource = utilService.source('queuedTask.queue')
        [queuedTaskResultInstanceList: QueuedTaskResult.selectList(), queuedTaskResultInstanceTotal: QueuedTaskResult.selectCount(), ddSource: ddSource]
    }

    def queueShow() {
        def queuedTaskResultInstance = QueuedTaskResult.get(params.id)

        if (!queuedTaskResultInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskResult', params.id)
            redirect(action: 'queueList')
        } else {
            return [queuedTaskResultInstance: queuedTaskResultInstance]
        }
    }

    def usrList() {
        params.sort = ['value'].contains(params.sort) ? params.sort : 'id'
        params.max = utilService.max
        def ddSource = utilService.source('queuedTask.usrList')
        def list = QueuedTaskResult.selectList(securityCode: utilService.currentCompany().securityCode, where: 'x.queued.user = ?', params: utilService.currentUser())
        def total = QueuedTaskResult.selectCount()
        [queuedTaskResultInstanceList: list, queuedTaskResultInstanceTotal: total, ddSource: ddSource]
    }

    def usrShow() {
        def queuedTaskResultInstance = QueuedTaskResult.find('from QueuedTaskResult as x where x.id = ? and x.queued.user = ? and x.securityCode = ?',
                [params.id?.toLong(), utilService.currentUser(), utilService.currentCompany().securityCode])

        if (!queuedTaskResultInstance) {
            flash.message = utilService.standardMessage('not.found', 'queuedTaskResult', params.id)
            redirect(action: 'usrList')
        } else {
            return [queuedTaskResultInstance: queuedTaskResultInstance]
        }
    }
}