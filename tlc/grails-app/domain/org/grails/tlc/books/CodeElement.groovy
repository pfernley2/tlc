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

import org.grails.tlc.corp.Company
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class CodeElement {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company]
    static hasMany = [values: CodeElementValue, sections1: ChartSection, sections2: ChartSection, sections3: ChartSection,
            sections4: ChartSection, sections5: ChartSection, sections6: ChartSection, sections7: ChartSection, sections8: ChartSection]
    static mappedBy = [sections1: 'segment1', sections2: 'segment2', sections3: 'segment3', sections4: 'segment4',
            sections5: 'segment5', sections6: 'segment6', sections7: 'segment7', sections8: 'segment8']

    Byte elementNumber
    String name
    String dataType = 'numeric'
    Byte dataLength
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache true
        columns {
            company lazy: true
            values cascade: 'save-update'
            sections1 cascade: 'save-update'
            sections2 cascade: 'save-update'
            sections3 cascade: 'save-update'
            sections4 cascade: 'save-update'
            sections5 cascade: 'save-update'
            sections6 cascade: 'save-update'
            sections7 cascade: 'save-update'
            sections8 cascade: 'save-update'
        }
    }

    static constraints = {
        elementNumber(range: 1..8, unique: 'company')
        name(blank: false, size: 1..30)
        dataType(blank: false, inList: ['alphabetic', 'numeric'])
        dataLength(range: 1..10)
        securityCode(validator: {val, obj ->
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
        return "${elementNumber}"
    }
}
