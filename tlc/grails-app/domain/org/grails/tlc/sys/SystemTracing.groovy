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

class SystemTracing {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static transients = ['insertDecode', 'updateDecode', 'deleteDecode']

    String domainName
    Long insertSecurityCode = UtilService.TRACE_NONE
    Long updateSecurityCode = UtilService.TRACE_NONE
    Long deleteSecurityCode = UtilService.TRACE_NONE
    Integer insertRetentionDays = 30
    Integer updateRetentionDays = 30
    Integer deleteRetentionDays = 30
    Boolean systemOnly
    String insertDecode
    String updateDecode
    String deleteDecode
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static constraints = {
        domainName(blank: false, size: 1..50, matches: '[A-Z][a-zA-Z_0-9]*', unique: true)
        insertSecurityCode(min: 0L)
        updateSecurityCode(min: 0L)
        deleteSecurityCode(min: 0L)
        insertRetentionDays(range: 1..1000)
        updateRetentionDays(range: 1..1000)
        deleteRetentionDays(range: 1..1000)
        securityCode(validator: {val, obj ->
            return (val == 0)
        })
    }

    def afterInsert() {
        UtilService.trace('insert', this)
    }

    def afterUpdate() {
        UtilService.trace('update', this)
    }

    def afterDelete() {
        UtilService.trace('delete', this)
    }

    public String toString() {
        return domainName
    }
}
