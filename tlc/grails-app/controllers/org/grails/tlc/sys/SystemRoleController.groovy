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
import org.grails.tlc.corp.CompanyUser

class SystemRoleController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin', display: 'coadmin', edits: 'coadmin', adjust: 'coadmin', listing: 'coadmin', members: 'coadmin',
        membership: 'coadmin', process: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', link: 'POST', adjust: 'POST', process: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'systemOnly'].contains(params.sort) ? params.sort : 'code'
        def ddSource = utilService.source('companyUser.list')
        [systemRoleInstanceList: SystemRole.selectList(), systemRoleInstanceTotal: SystemRole.selectCount(), ddSource: ddSource]
    }

    def show() {
        def systemRoleInstance = SystemRole.get(params.id)
        if (!systemRoleInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemRole', params.id)
            redirect(action: 'list')
        } else {
            return [systemRoleInstance: systemRoleInstance]
        }
    }

    def delete() {
        def systemRoleInstance = SystemRole.get(params.id)
        if (systemRoleInstance) {
            if (systemRoleInstance.code == 'companyAdmin') {
                flash.message = message(code: 'systemRole.admin.delete', default: 'The companyAdmin role cannot be deleted')
                redirect(action: 'show', id: params.id)
            } else {
                try {
                    def valid = true
                    SystemRole.withTransaction {status ->

                        // Need to avoid concurrent modification exceptions
                        def temp = []
                        for (it in systemRoleInstance.users) temp << it
                        for (it in temp) systemRoleInstance.removeFromUsers(it)
                        temp = []
                        for (it in systemRoleInstance.activities) temp << it
                        for (it in temp) systemRoleInstance.removeFromActivities(it)
                        if (systemRoleInstance.save()) {
                            utilService.deleteWithMessages(systemRoleInstance, [prefix: 'role.name', code: systemRoleInstance.code], status)
                        } else {
                            status.setRollbackOnly()
                            systemRoleInstance.errorMessage(code: 'systemRole.remove', default: 'Unable to remove the members and activities from the role')
                            valid = false
                        }
                    }

                    if (valid) {
                        utilService.cacheService.clearThis('userActivity')
                        flash.message = utilService.standardMessage('deleted', systemRoleInstance)
                        redirect(action: 'list')
                    } else {
                        redirect(action: 'show', id: params.id)
                    }
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', systemRoleInstance)
                    redirect(action: 'show', id: params.id)
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemRole', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemRoleInstance = SystemRole.get(params.id)
        if (!systemRoleInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemRole', params.id)
            redirect(action: 'list')
        } else {
            return [systemRoleInstance: systemRoleInstance]
        }
    }

    def update(Long version) {
        def systemRoleInstance = SystemRole.get(params.id)
        if (systemRoleInstance) {
            if (version != null && systemRoleInstance.version > version) {
                systemRoleInstance.errorMessage(code: 'locking.failure', domain: 'systemRole')
                render(view: 'edit', model: [systemRoleInstance: systemRoleInstance])
                return
            }

            def oldCode = systemRoleInstance.code
            def oldSystemOnly = systemRoleInstance.systemOnly
            systemRoleInstance.properties['code', 'name', 'systemOnly'] = params
            if (oldCode == 'companyAdmin' && systemRoleInstance.code != oldCode) {
                systemRoleInstance.errorMessage(field: 'code', code: 'systemRole.admin.change', default: 'The companyAdmin role cannot have its code changed')
                render(view: 'edit', model: [systemRoleInstance: systemRoleInstance])
            } else {
                if (systemRoleInstance.systemOnly && !oldSystemOnly) {
                    def systemCompany = Company.findBySystemOnly(true)
                    def removables = []
                    for (companyUser in systemRoleInstance.users) {
                        if (companyUser.company.id != systemCompany.id) removables << companyUser
                    }

                    for (companyUser in removables) systemRoleInstance.removeFromUsers(companyUser)
                }

                if (utilService.saveWithMessages(systemRoleInstance, [prefix: 'role.name', code: systemRoleInstance.code, oldCode: oldCode, field: 'name'])) {
                    utilService.cacheService.clearThis('userActivity')
                    flash.message = utilService.standardMessage('updated', systemRoleInstance)
                    redirect(action: 'show', id: systemRoleInstance.id)
                } else {
                    render(view: 'edit', model: [systemRoleInstance: systemRoleInstance])
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemRole', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        return [systemRoleInstance: new SystemRole()]
    }

    def save() {
        def systemRoleInstance = new SystemRole()
        systemRoleInstance.properties['code', 'name', 'systemOnly'] = params
        if (utilService.saveWithMessages(systemRoleInstance, [prefix: 'role.name', code: systemRoleInstance.code, field: 'name'])) {
            utilService.cacheService.clearThis('userActivity')
            flash.message = utilService.standardMessage('created', systemRoleInstance)
            redirect(action: 'show', id: systemRoleInstance.id)
        } else {
            render(view: 'create', model: [systemRoleInstance: systemRoleInstance])
        }
    }

    def links() {
        def ddSource = utilService.reSource('companyUser.list')
        [allRoles: SystemRole.list(), userRoles: SystemRole.selectList(action: 'list'), ddSource: ddSource]
    }

    def link() {
        def ddSource = utilService.reSource('companyUser.list')
        def userRoles = SystemRole.selectList(action: 'list')
        def roles = []
        if (params.linkages) roles = params.linkages instanceof String ? [params.linkages.toLong()] : params.linkages*.toLong() as List
        def modified = false
        for (role in userRoles) {
            if (!roles.contains(role.id)) {
                ddSource.removeFromRoles(role)
                modified = true
            }
        }

        for (role in roles) {
            def found = false
            for (r in userRoles) {
                if (r.id == role) {
                    found = true
                    break
                }
            }

            if (!found) {
                ddSource.addToRoles(SystemRole.get(role))
                modified = true
            }
        }

        if (modified) {
            if (ddSource.save(flush: true)) {  // With deep validation
                utilService.cacheService.resetThis('userActivity', ddSource.securityCode, "${ddSource.user.id}")
                flash.message = message(code: 'generic.links.changed', default: 'The links were successfully updated')
            } else {
                flash.message = message(code: 'generic.links.failed', default: 'Error updating the links')
            }
        } else {
            flash.message = message(code: 'generic.links.unchanged', default: 'No links were changed')
        }

        redirect(action: 'list')
    }

    def display() {
        params.max = utilService.max
        params.sort = ['code'].contains(params.sort) ? params.sort : 'code'
        def ddSource = utilService.source('companyUser.display')
        [systemRoleInstanceList: SystemRole.selectList(), systemRoleInstanceTotal: SystemRole.selectCount(), ddSource: ddSource]
    }

    def edits() {
        def ddSource = utilService.reSource('companyUser.display', [origin: 'display'])
        def allRoles = utilService.currentCompany().systemOnly ? SystemRole.list() : SystemRole.findAllBySystemOnly(false)
        [allRoles: allRoles, userRoles: SystemRole.selectList(action: 'display'), ddSource: ddSource]
    }

    def adjust() {
        def ddSource = utilService.reSource('companyUser.display', [origin: 'display'])
        def userRoles = SystemRole.selectList(action: 'display')
        def roles = []
        if (params.linkages) roles = params.linkages instanceof String ? [params.linkages.toLong()] : params.linkages*.toLong() as List
        def modified = false

        // Work through the existing roles
        for (role in userRoles) {

            // If the existing role is not in the new roles, remove it
            if (!roles.contains(role.id)) {
                ddSource.removeFromRoles(role)
                modified = true
            }
        }

        // Work through the new roles
        def newRole
        def isSystemCompany = utilService.currentCompany().systemOnly
        for (role in roles) {
            def found = false

            // Check if the new role is in the exiting roles
            for (r in userRoles) {
                if (r.id == role) {
                    found = true
                    break
                }
            }

            // Add the new role if it wasn't in the existing roles
            if (!found) {
                newRole = SystemRole.get(role)
                if (!newRole.systemOnly || isSystemCompany) {
                    ddSource.addToRoles(newRole)
                    modified = true
                }
            }
        }

        if (modified) {
            if (ddSource.save(flush: true)) {      // With deep validation
                utilService.cacheService.resetThis('userActivity', ddSource.securityCode, "${ddSource.user.id}")
                flash.message = message(code: 'systemRole.roles.changed', default: 'User roles updated')
            } else {
                flash.message = message(code: 'systemRole.roles.failed', default: 'Error updating the roles')
            }
        } else {
            flash.message = message(code: 'systemRole.roles.unchanged', default: 'No roles were changed')
        }

        redirect(action: 'display')
    }

    def listing() {
        params.max = utilService.max
        params.sort = ['code'].contains(params.sort) ? params.sort : 'code'
        def map = [:]
        if (!utilService.currentCompany().systemOnly) map.where = 'x.systemOnly = false'
        [systemRoleInstanceList: SystemRole.selectList(map), systemRoleInstanceTotal: SystemRole.selectCount()]
    }

    def members() {
        params.max = utilService.max
        params.sort = ['loginId', 'name'].contains(params.sort) ? params.sort : 'name'
        def ddSource = utilService.source('systemRole.listing')
        def memberList = SystemUser.findAll('from SystemUser as su where su.id in (select cu.user.id from CompanyUser as cu join cu.roles as r where cu.company = ? and r = ?) order by su.name', [utilService.currentCompany(), ddSource])
        def memberTotal = SystemUser.executeQuery('select count(*) from SystemUser as su where su.id in (select cu.user.id from CompanyUser as cu join cu.roles as r where cu.company = ? and r = ?)', [utilService.currentCompany(), ddSource])[0]
        [memberList: memberList, memberTotal: memberTotal, ddSource: ddSource]
    }

    def membership() {
        def ddSource = utilService.reSource('systemRole.listing', [origin: 'members'])
        def memberList = SystemUser.findAll('from SystemUser as su where su.id in (select cu.user.id from CompanyUser as cu join cu.roles as r where cu.company = ? and r = ?) order by su.name', [utilService.currentCompany(), ddSource])
        def userList = SystemUser.findAll('from SystemUser as su where su.id in (select cu.user.id from CompanyUser as cu where cu.company = ?) order by su.name', [utilService.currentCompany()])
        [userList: userList, memberList: memberList, ddSource: ddSource]
    }

    def process() {
        def company = utilService.currentCompany()
        def ddSource = utilService.reSource('systemRole.listing', [origin: 'members'])
        def memberList = SystemUser.findAll('from SystemUser as su where su.id in (select cu.user.id from CompanyUser as cu join cu.roles as r where cu.company = ? and r = ?) order by su.name', [company, ddSource])
        def members = []
        if (params.linkages) members = params.linkages instanceof String ? [params.linkages.toLong()] : params.linkages*.toLong() as List
        def modified = []

        // Work through the existing membership
        for (member in memberList) {

            // If the existing member is not in the new membership, remove them
            if (!members.contains(member.id)) {
                ddSource.removeFromUsers(CompanyUser.findByCompanyAndUser(company, member, [cache: true]))
                modified << member
            }
        }

        // Work through the new membership
        for (member in members) {
            def found = false

            // Check if the new group is in the exiting groups
            for (m in memberList) {
                if (m.id == member) {
                    found = true
                    break
                }
            }

            // Add the new group if it wasn't in the existing groups
            if (!found) {
                def user = SystemUser.get(member)
                if (user && (!ddSource.systemOnly || company.systemOnly)) {
                    ddSource.addToUsers(CompanyUser.findByCompanyAndUser(company, user, [cache: true]))
                    modified << user
                }
            }
        }

        if (modified) {
            if (ddSource.save(flush: true)) {      // With deep validation
                for (it in modified) utilService.cacheService.resetThis('userActivity', company.securityCode, it.id.toString())
                flash.message = message(code: 'systemRole.members.changed', default: 'Role membership updated')
            } else {
                flash.message = message(code: 'systemRole.members.failed', default: 'Error updating the role membership')
            }
        } else {
            flash.message = message(code: 'systemRole.members.unchanged', default: 'No changes were made to the role membership')
        }

        redirect(action: 'members')
    }
}