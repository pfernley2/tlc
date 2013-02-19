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
package org.grails.tlc.obj

import grails.validation.Validateable
import org.grails.tlc.books.DocumentType
import org.grails.tlc.sys.UtilService

@Validateable
public class DocumentSearch {
    DocumentType type
    String code
    String reference
    String description
    Date documentFrom
    Date documentTo
    Date postedFrom
    Date postedTo

    static constraints = {
        code(nullable: true)
        reference(nullable: true)
        description(nullable: true)
        documentFrom(nullable: true, range: UtilService.validDateRange(), validator: {val, obj ->
            if (val && val != UtilService.fixDate(val)) return 'bad'
            return true
        })
        documentTo(nullable: true, range: UtilService.validDateRange(), validator: {val, obj ->
            if (val) {
                if (val != UtilService.fixDate(val)) return 'bad'
                if (obj.documentFrom && val < obj.documentFrom) return 'before'
            }

            return true
        })
        postedFrom(nullable: true, range: UtilService.validDateRange(), validator: {val, obj ->
            if (val && val != UtilService.fixDate(val)) return 'bad'
            return true
        })
        postedTo(nullable: true, range: UtilService.validDateRange(), validator: {val, obj ->
            if (val) {
                if (val != UtilService.fixDate(val)) return 'bad'
                if (obj.postedFrom && val < obj.postedFrom) return 'before'
            }

            return true
        })
    }
}