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

import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class ChartSectionRange {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [section: ChartSection]

    String type = 'include'
    String rangeFrom
    String rangeTo
    String comment
    String messageText
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            section lazy: true
            type column: 'range_type'
            comment column: 'range_comment'
            messageText column: 'range_message'
        }
    }

    static constraints = {
        type(inList: ['include', 'exclude', 'disallow'], validator: {val, obj ->
            if (!obj.section.segment1) return 'bad.segment'
        })
        rangeFrom(blank: false, size: 1..87, validator: {val, obj ->
            return obj.section.matchesTemplate(val) ?: 'mismatch'
        })
        rangeTo(blank: false, size: 1..87, validator: {val, obj ->
            if (!obj.section.matchesTemplate(val)) return 'mismatch'
            if (val && obj.rangeFrom) {
                if (!rangeCompatible(obj.rangeFrom, val)) return 'lesser'
                def count = testCount(obj.rangeFrom, val)
                if (count == 0 && obj.comment != '\t') return 'no.test'      // The tab as a comment is used to disable zero test error for bulk account creation
                if (count < 2 && obj.type == 'disallow') return 'few.test'
            }
            return true
        })
        comment(nullable: true, size: 1..30)
        messageText(nullable: true, size: 1..100)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.section.securityCode
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

    static rangeCompatible(from, to) {
        def fromSet = from.split("\\${BookService.SEGMENT_DELIMITER}")
        def toSet = to.split("\\${BookService.SEGMENT_DELIMITER}")
        if (fromSet.size() != toSet.size()) return false
        for (int i = 0; i < fromSet.size(); i++) {
            if (fromSet[i] != '*' && toSet[i] != '*' && fromSet[i].compareTo(toSet[i]) > 0) return false
        }

        return true
    }

    static testCount(from, to) {
        def fromSet = from.split("\\${BookService.SEGMENT_DELIMITER}")
        def toSet = to.split("\\${BookService.SEGMENT_DELIMITER}")
        def count = 0
        for (int i = 0; i < fromSet.size(); i++) {
            if (fromSet[i] != '*' || toSet[i] != '*') count++
        }

        return count
    }

    public String toString() {
        return "${id}"
    }
}
