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
package org.grails.tlc.rest

class AgentCredentialController {

    // Injected services
    def utilService
    def restService

    // Security settings
    def activities = [default: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['dateCreated', 'code', 'secret', 'active'].contains(params.sort) ? params.sort : 'dateCreated'
        def ddSource = utilService.source('companyUser.display')
        [agentCredentialInstanceList: AgentCredential.selectList(securityCode: utilService.currentCompany().securityCode), agentCredentialInstanceTotal: AgentCredential.selectCount(), ddSource: ddSource]
    }

    def show() {
        def agentCredentialInstance = AgentCredential.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!agentCredentialInstance) {
            flash.message = utilService.standardMessage('not.found', 'agentCredential', params.id)
            redirect(action: 'list')
        } else {
            return [agentCredentialInstance: agentCredentialInstance]
        }
    }

    def delete() {
        def agentCredentialInstance = AgentCredential.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (agentCredentialInstance) {
            try {
                agentCredentialInstance.delete(flush: true)
                flash.message = utilService.standardMessage('deleted', agentCredentialInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', agentCredentialInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'agentCredential', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def agentCredentialInstance = AgentCredential.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!agentCredentialInstance) {
            flash.message = utilService.standardMessage('not.found', 'agentCredential', params.id)
            redirect(action: 'list')
        } else {
            return [agentCredentialInstance: agentCredentialInstance]
        }
    }

    def update(Long version) {
        def agentCredentialInstance = AgentCredential.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (agentCredentialInstance) {
            if (version != null && agentCredentialInstance.version > version) {
                agentCredentialInstance.errorMessage(code: 'locking.failure', domain: 'agentCredential')
                render(view: 'edit', model: [agentCredentialInstance: agentCredentialInstance])
                return
            }

            agentCredentialInstance.active = params.active ? true : false
            if (!agentCredentialInstance.hasErrors() && agentCredentialInstance.saveThis()) {
                flash.message = utilService.standardMessage('updated', agentCredentialInstance)
                redirect(action: 'show', id: agentCredentialInstance.id)
            } else {
                render(view: 'edit', model: [agentCredentialInstance: agentCredentialInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'agentCredential', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def agentCredentialInstance = new AgentCredential()
        agentCredentialInstance.companyUser = utilService.reSource('companyUser.display')   // Ensure correct parent
        agentCredentialInstance.code = restService.createAgentCode()
        agentCredentialInstance.secret = restService.createAgentSecret()
        return [agentCredentialInstance: agentCredentialInstance]
    }

    def save() {
        def agentCredentialInstance = new AgentCredential()
        agentCredentialInstance.properties['code', 'secret', 'active'] = params
        agentCredentialInstance.companyUser = utilService.reSource('companyUser.display')   // Ensure correct parent
        if (!agentCredentialInstance.hasErrors() && agentCredentialInstance.saveThis()) {
            flash.message = utilService.standardMessage('created', agentCredentialInstance)
            redirect(action: 'show', id: agentCredentialInstance.id)
        } else {
            render(view: 'create', model: [agentCredentialInstance: agentCredentialInstance])
        }
    }
}