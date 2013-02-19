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

class SystemMessage {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    String code
    String locale
    Byte relevance = 0
    String text
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static constraints = {
        code(blank: false, size: 1..250, matches: '[^:\\= ]+')
        locale(blank: false, matches: '\\*|([a-z][a-z]([A-Z][A-Z])?)', unique: 'code')
        relevance(validator: {val, obj ->
                if (obj.locale) obj.relevance = obj.locale.length()
                return true
            })
        text(blank: false, size: 1..2000)
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
        return code + ' - ' + locale
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    // Break a single line of properties text in to multiple lines if required. Also encode the text
    // suitable for output to the propoerties file.
    static breakLine(code, text) {
        text = text.replace('\\', '\\\\')   // Replace any backslash with a double backslash
        def maxLen = 100 - (code.length() + 1)
        if (maxLen < 25) maxLen = 25
        def elements = []
        def found = true
        while (text.length() > maxLen && found) {
            found = false
            for (int i = maxLen - 20; i < text.length() - 15; i++) {
                if (text[i] == ' ') {
                    elements << text.substring(0, i + 1) + '\\'
                    text = ' ' + text.substring(i)
                    found = true
                    maxLen = 100
                    break
                }
            }
        }

        elements << text
    }
}
