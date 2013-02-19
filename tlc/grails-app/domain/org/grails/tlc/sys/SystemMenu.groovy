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

class SystemMenu {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [activity: SystemActivity]
    static transients = ['parentObject']

    SystemMenu parentObject
    String path
    String title
    Integer sequencer
    String type
    String command
    String parameters
    Long parent = 0
    String treeSequence = '000000000000000000000000000000000000000000000000000000'
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache true
        columns {
            activity lazy: true
            path column: 'menu_path'
            parent index: 'sysmenu_parent_idx'
        }
    }

    static constraints = {
        path(blank: false, size: 1..100, unique: true, validator: {val, obj ->
            if (val) {
                if (val.startsWith('.') || val.endsWith('.') || val.contains('..')) {
                    return false
                }

                char c
                int cp
                for (int i = 0; i < val.length(); i++) {
                    c = val.charAt(i)
                    if (c != '.') {
                        if (!Character.isLetterOrDigit(val.codePointAt(i))) return false
                        if (Character.isHighSurrogate(c)) i++
                    }
                }

                def seq = "${1000000 + ((obj.sequencer >= 0 && obj.sequencer <= 999999) ? obj.sequencer : 0)}"[1..6]
                def pos = val.count('.') * 6
                if (pos > 48) return false
                if (pos) {
                    if (!obj.parentObject) return false

                    obj.parent = obj.parentObject.id
                    seq = obj.parentObject.treeSequence.substring(0, pos) + seq
                } else {
                    obj.parent = 0
                }

                obj.treeSequence = seq.padRight(54, '0')
            }

            return true
        })
        title(blank: false, size: 1..100)
        sequencer(range: 0..999999)
        type(blank: false, inList: ['action', 'submenu', 'url', 'program'])
        command(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.type == 'submenu') {
                if (val) return false
            } else if (!val) {
                return false
            } else if (obj.type == 'action') {
                if (val.contains(' ') || val.startsWith('.') || val.endsWith('.') || !val.contains('.') || val.indexOf('.') != val.lastIndexOf('.')) {
                    return false
                }
            } else if (obj.type == 'url') {
                try {
                    new URL(val)
                } catch (MalformedURLException mue) {
                    return false
                }
            }

            return true
        })
        parameters(nullable: true, size: 1..200)
        treeSequence(blank: false, matches: '\\d{54}')
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
        return path
    }
}
