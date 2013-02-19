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
package org.grails.tlc.books

class MnemonicController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'login']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'name', 'accountCodeFragment'].contains(params.sort) ? params.sort : 'code'
        [mnemonicInstanceList: Mnemonic.selectList(where: 'x.user = ?', params: utilService.currentUser()), mnemonicInstanceTotal: Mnemonic.selectCount()]
    }

    def show() {
        def mnemonicInstance = Mnemonic.findByIdAndUser(params.id, utilService.currentUser())
        if (!mnemonicInstance) {
            flash.message = utilService.standardMessage('not.found', 'mnemonic', params.id)
            redirect(action: 'list')
        } else {
            return [mnemonicInstance: mnemonicInstance]
        }
    }

    def delete() {
        def mnemonicInstance = Mnemonic.findByIdAndUser(params.id, utilService.currentUser())
        if (mnemonicInstance) {
            try {
                mnemonicInstance.delete(flush: true)
                utilService.cacheService.resetThis('mnemonic', 0L, utilService.currentUser().id.toString())
                flash.message = utilService.standardMessage('deleted', mnemonicInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', mnemonicInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'mnemonic', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def mnemonicInstance = Mnemonic.findByIdAndUser(params.id, utilService.currentUser())
        if (!mnemonicInstance) {
            flash.message = utilService.standardMessage('not.found', 'mnemonic', params.id)
            redirect(action: 'list')
        } else {
            return [mnemonicInstance: mnemonicInstance]
        }
    }

    def update(Long version) {
        def mnemonicInstance = Mnemonic.findByIdAndUser(params.id, utilService.currentUser())
        if (mnemonicInstance) {
            if (version != null && mnemonicInstance.version > version) {
                mnemonicInstance.errorMessage(code: 'locking.failure', domain: 'mnemonic')
                render(view: 'edit', model: [mnemonicInstance: mnemonicInstance])
                return
            }

            mnemonicInstance.properties['code', 'name', 'accountCodeFragment'] = params
            if (!mnemonicInstance.hasErrors() && mnemonicInstance.saveThis()) {
                utilService.cacheService.resetThis('mnemonic', 0L, utilService.currentUser().id.toString())
                flash.message = utilService.standardMessage('updated', mnemonicInstance)
                redirect(action: 'show', id: mnemonicInstance.id)
            } else {
                render(view: 'edit', model: [mnemonicInstance: mnemonicInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'mnemonic', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def mnemonicInstance = new Mnemonic()
        mnemonicInstance.user = utilService.currentUser()   // Ensure correct parent
        return [mnemonicInstance: mnemonicInstance]
    }

    def save() {
        def mnemonicInstance = new Mnemonic()
        mnemonicInstance.properties['code', 'name', 'accountCodeFragment'] = params
        mnemonicInstance.user = utilService.currentUser()   // Ensure correct parent
        if (!mnemonicInstance.hasErrors() && mnemonicInstance.saveThis()) {
            utilService.cacheService.resetThis('mnemonic', 0L, utilService.currentUser().id.toString())
            flash.message = utilService.standardMessage('created', mnemonicInstance)
            redirect(action: 'show', id: mnemonicInstance.id)
        } else {
            render(view: 'create', model: [mnemonicInstance: mnemonicInstance])
        }
    }
}