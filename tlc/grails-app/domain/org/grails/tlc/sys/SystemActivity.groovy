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

import java.util.concurrent.atomic.AtomicLong

class SystemActivity {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = SystemRole
    static hasMany = [menus: SystemMenu, actions: SystemAction, roles: SystemRole, types: SystemDocumentType]

    String code
    Boolean systemOnly = false
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            systemRole lazy: true
            menus cascade: 'save-update'
            actions cascade: 'save-update'
            types cascade: 'save-update'
        }
    }

    static constraints = {
        code(blank: false, size: 1..30, matches: '[a-zA-Z][a-zA-Z_0-9]*', unique: true, validator: {val, obj ->
            obj.systemOnly = (val == 'any' || val == 'logout' || val == 'login' || val == 'sysadmin' || val == 'attached')
            return true
        })
        securityCode(validator: {val, obj ->
            return (val == 0)
        })
    }

    def afterInsert() {
        return UtilService.trace('insert', this)
    }

    def afterUpdate() {
        return UtilService.trace('update', this)
    }

    def afterDelete() {
        return UtilService.trace('delete', this)
    }

    public String toString() {
        return code
    }
}
