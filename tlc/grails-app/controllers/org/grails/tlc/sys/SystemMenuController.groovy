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

class SystemMenuController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin', display: 'attached', execute: 'attached']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['path', 'sequencer', 'command', 'parameters'].contains(params.sort) ? params.sort : 'treeSequence'
        [systemMenuInstanceList: SystemMenu.selectList(), systemMenuInstanceTotal: SystemMenu.selectCount()]
    }

    def show() {
        def systemMenuInstance = SystemMenu.get(params.id)
        if (!systemMenuInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemMenu', params.id)
            redirect(action: 'list')
        } else {
            return [systemMenuInstance: systemMenuInstance]
        }
    }

    def delete() {
        def systemMenuInstance = SystemMenu.get(params.id)
        if (systemMenuInstance) {
            def dependents = SystemMenu.findAllByPathLike(systemMenuInstance.path + '.%')
            try {
                SystemMenu.withTransaction {status ->
                    utilService.deleteWithMessages(systemMenuInstance, [
                            [prefix: 'menu.option', code: systemMenuInstance.path],
                            [prefix: 'menu.crumb', code: systemMenuInstance.path],
                            [prefix: 'menu.submenu', code: systemMenuInstance.path]
                    ], status)

                    for (child in dependents) {
                        utilService.deleteWithMessages(child, [
                                [prefix: 'menu.option', code: child.path],
                                [prefix: 'menu.crumb', code: child.path],
                                [prefix: 'menu.submenu', code: child.path]
                        ], status)
                    }
                }

                utilService.menuService.clearCrumbs(utilService.cacheService)
                flash.message = utilService.standardMessage('deleted', systemMenuInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemMenuInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemMenu', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemMenuInstance = SystemMenu.get(params.id)
        if (!systemMenuInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemMenu', params.id)
            redirect(action: 'list')
        } else {
            return [systemMenuInstance: systemMenuInstance]
        }
    }

    def update(Long version) {
        def systemMenuInstance = SystemMenu.get(params.id)
        if (systemMenuInstance) {
            if (version != null && systemMenuInstance.version > version) {
                systemMenuInstance.errorMessage(code: 'locking.failure', domain: 'systemMenu')
                render(view: 'edit', model: [systemMenuInstance: systemMenuInstance])
                return
            }

            def oldPath = systemMenuInstance.path
            def oldSequencer = systemMenuInstance.sequencer
            def oldType = systemMenuInstance.type
            def oldParameters = systemMenuInstance.parameters
            systemMenuInstance.properties['path', 'title', 'sequencer', 'type', 'command', 'parameters', 'activity'] = params
            if (systemMenuInstance.path && systemMenuInstance.path.contains('.') && !systemMenuInstance.path.endsWith('.')) {
                systemMenuInstance.parentObject = SystemMenu.findByPathAndType(systemMenuInstance.path.substring(0, systemMenuInstance.path.lastIndexOf('.')), 'submenu')
            }

            def valid = !systemMenuInstance.hasErrors()
            if (valid && oldType == 'submenu' && systemMenuInstance.type != 'submenu' && SystemMenu.countByParent(systemMenuInstance.id) > 0) {
                systemMenuInstance.errorMessage(field: 'type', code: 'systemMenu.update.type', default: 'Cannot alter type from sub-menu when there are child records')
                valid = false
            }

            if (valid) {
                def dependents = (oldType == 'submenu' && systemMenuInstance.type == 'submenu' &&
                        (oldPath != systemMenuInstance.path || oldSequencer != systemMenuInstance.sequencer)) ? SystemMenu.findAllByPathLike(oldPath + '.%', [sort: 'path']) : null
                def translatables = [
                        [prefix: 'menu.option', code: systemMenuInstance.path, oldCode: oldPath, field: 'title'],
                        [prefix: 'menu.crumb', code: systemMenuInstance.path, oldCode: oldPath, text: (systemMenuInstance.path.indexOf('.') == -1) ? systemMenuInstance.path : systemMenuInstance.path.substring(systemMenuInstance.path.lastIndexOf('.') + 1)]
                ]

                if (systemMenuInstance.type == 'submenu' && systemMenuInstance.parameters) {
                    translatables << [prefix: 'menu.submenu', code: systemMenuInstance.path, oldCode: oldPath, field: 'parameters']
                }

                SystemMenu.withTransaction {status ->
                    if (!utilService.saveWithMessages(systemMenuInstance, translatables, status)) {
                        status.setRollbackOnly()
                        valid = false
                    }

                    if (valid && dependents) {

                        // If the path has changed
                        if (oldPath != systemMenuInstance.path) {
                            def len = oldPath.length()
                            for (child in dependents) {
                                child.parentObject = SystemMenu.get(child.parent)
                                oldPath = child.path
                                child.path = systemMenuInstance.path + child.path.substring(len)
                                translatables = [
                                        [prefix: 'menu.option', code: child.path, oldCode: oldPath, field: 'title'],
                                        [prefix: 'menu.crumb', code: child.path, oldCode: oldPath, text: child.path.substring(child.path.lastIndexOf('.') + 1)]
                                ]

                                if (child.type == 'submenu' && child.parameters) {
                                    translatables << [prefix: 'menu.submenu', code: child.path, oldCode: oldPath, field: 'parameters']
                                }

                                if (!utilService.saveWithMessages(child, translatables, status)) {
                                    status.setRollbackOnly()
                                    valid = false
                                    break
                                }
                            }
                        } else {    // Just the sequence number that has changed
                            for (child in dependents) {
                                child.parentObject = SystemMenu.get(child.parent)
                                child.parent = 0    // Force a change
                                if (!child.saveThis()) {
                                    status.setRollbackOnly()
                                    valid = false
                                    break
                                }
                            }
                        }
                    }

                    // If there was sub-menu text, but now there isn't
                    if (valid && oldType == 'submenu' && (systemMenuInstance.type != 'submenu' || (oldParameters && !systemMenuInstance.parameters))) {

                        // Delete all the sub-menu texts
                        for (it in SystemMessage.findAllByCode("menu.submenu.${oldPath}")) it.delete(flush: true)

                        // Clear the cache of those texts
                        utilService.cacheService.resetThis('message', CacheService.COMPANY_INSENSITIVE, "menu.submenu.${oldPath}")
                    }
                }
            }

            if (valid) {
                utilService.menuService.clearCrumbs(utilService.cacheService)
                flash.message = utilService.standardMessage('updated', systemMenuInstance)
                redirect(action: 'show', id: systemMenuInstance.id)
            } else {
                render(view: 'edit', model: [systemMenuInstance: systemMenuInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemMenu', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        return [systemMenuInstance: new SystemMenu()]
    }

    def save() {
        def systemMenuInstance = new SystemMenu()
        systemMenuInstance.properties['path', 'title', 'sequencer', 'type', 'command', 'parameters', 'activity'] = params
        if (systemMenuInstance.path && systemMenuInstance.path.contains('.') && !systemMenuInstance.path.endsWith('.')) {
            systemMenuInstance.parentObject = SystemMenu.findByPathAndType(systemMenuInstance.path.substring(0, systemMenuInstance.path.lastIndexOf('.')), 'submenu')
        }

        def translatables = [
                [prefix: 'menu.option', code: systemMenuInstance.path, field: 'title'],
                [prefix: 'menu.crumb', code: systemMenuInstance.path, text: (systemMenuInstance.path.indexOf('.') == -1) ? systemMenuInstance.path : systemMenuInstance.path.substring(systemMenuInstance.path.lastIndexOf('.') + 1)]
        ]

        if (systemMenuInstance.type == 'submenu' && systemMenuInstance.parameters) {
            translatables << [prefix: 'menu.submenu', code: systemMenuInstance.path, field: 'parameters']
        }

        if (utilService.saveWithMessages(systemMenuInstance, translatables)) {
            utilService.menuService.clearCrumbs(utilService.cacheService)
            flash.message = utilService.standardMessage('created', systemMenuInstance)
            redirect(action: 'show', id: systemMenuInstance.id)
        } else {
            render(view: 'create', model: [systemMenuInstance: systemMenuInstance])
        }
    }

    def display() {
        [optionList: utilService.menuService.listMenuOptions(utilService, request, session, params)]
    }

    def execute() {
        def option = utilService.menuService.currentMenuOption(request, session, params)
        if (!option || option.type == 'submenu') {
            redirect(action: 'display')
        } else if (option.type == 'action') {
            def pos = option.command.indexOf('.')
            def controller = option.command.substring(0, pos)
            def action = option.command.substring(pos + 1)
            def params = utilService.menuService.getParamsAsMap(option.parameters)
            redirect(controller: controller, action: action, params: params)
        } else if (option.type == 'url') {
            def params = utilService.menuService.getParamsAsString(option.parameters)
            redirect(url: option.command + params)
        } else {  // program
            def params = utilService.menuService.getParamsAsList(option.parameters)
            def dir
            for (int i = 0; i < params.size(); i++) {
                if (params[i].startsWith('dir_=')) {
                    dir = new File(params[i].substring(5))
                    params.remove(i)
                    break
                }
            }

            def ex, process
            try {
                process = option.command.execute(params, dir)
            } catch (Exception err) {
                process = null
                ex = err
            }

            if (process) {
                flash.message = message(code: 'systemMenu.program.started', default: 'Program started successfully')
            } else {
                def args = ''
                if (ex) {
                    args = ': ' + ex.getClass().getName()
                    if (ex.getMessage()) args = args + '(' + ex.getMessage() + ')'
                }

                flash.message = message(code: 'systemMenu.program.failed', args: [args], default: "Program COULD NOT be started${args}")
                log.error(option.command + " ->  Program COULD NOT be started${args}")
            }

            redirect(action: 'display')
        }
    }
}