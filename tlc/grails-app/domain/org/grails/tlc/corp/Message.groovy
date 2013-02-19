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

import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class Message {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company]

    String code
    String locale
    String text
    Byte relevance = 0
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            company lazy: true
        }
    }

    static constraints = {
        code(blank: false, size: 1..250, matches: '[^:\\= ]+', unique: ['company', 'locale'])
        locale(blank: false, matches: '\\*|([a-z][a-z]([A-Z][A-Z])?)')
        text(blank: false, size: 1..2000)
        relevance(validator: {val, obj ->
            if (obj.locale) obj.relevance = obj.locale.length()
            return true
        })
        securityCode(unique: ['code', 'locale'], validator: {val, obj ->
            obj.securityCode = obj.company.securityCode
            return true
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
        return code + ' - ' + locale
    }
}
