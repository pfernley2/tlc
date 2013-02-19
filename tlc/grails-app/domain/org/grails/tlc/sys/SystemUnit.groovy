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

class SystemUnit {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [measure: SystemMeasure, scale: SystemScale]
    static hasMany = [conversions: SystemConversion, convertees: SystemConversion]
    static mappedBy = [conversions: 'source', convertees: 'target']

    String code
    String name
    BigDecimal multiplier
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            measure lazy: true
            scale lazy: true
            conversions cascade: 'all'
            convertees cascade: 'all'
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, matches: '[a-zA-Z][a-zA-Z_0-9()\\.]*', unique: true)
        name(blank: false, size: 1..30)
        multiplier(scale: 10, min: 0.0000000001, max: 999999999999.9999999999)
        securityCode(validator: {val, obj ->
            return (val == 0)
        })
    }

    def similarTo(other) {
        return (other?.measure.id == measure.id && other?.scale.id == scale.id)
    }

    def equivalentTo(other) {
        return (similarTo(other) && other?.multiplier == multiplier)
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
        return code
    }
}
