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

import org.grails.tlc.sys.SystemLanguage
import org.grails.tlc.sys.SystemCountry
import grails.validation.Validateable

@Validateable
class Translation {

    def utilService

    SystemLanguage fromLanguage
    SystemCountry fromCountry
    SystemLanguage toLanguage
    SystemCountry toCountry
    Boolean strict = false
    String display = 'all'
    String sort
    String order
    String max
    String offset

    static constraints = {
        fromLanguage(nullable: true)
        fromCountry(nullable: true, validator: {val, obj ->
            if (val && obj.fromLanguage == null) return 'no.fromLanguage'
            return true
        })
        toLanguage(nullable: true, validator: {val, obj ->
            if (!val) {
                SystemLanguage.withNewSession {session ->
                    val = SystemLanguage.findByCode(obj.utilService.currentLocale().language)
                }

                if (!val) return 'is.null'
                obj.toLanguage = val
            }

            if (val.id == obj.fromLanguage?.id && obj.fromCountry?.id == obj.toCountry?.id) return 'no.translation'
            return true
        })
        toCountry(nullable: true, validator: {val, obj ->
            if (val && obj.toLanguage == null) return 'no.toLanguage'
            return true
        })
        display(inList: ['all', 'translated', 'untranslated'])
        sort(nullable: true)
        order(nullable: true)
        max(nullable: true)
        offset(nullable: true)
    }

    // Returns a map of this object's data suitable for passing the
    // values as params values between methods
    def getData(map = null) {
        if (map == null) map = [:]
        if (fromLanguage) map.'fromLanguage.id' = fromLanguage.id
        if (fromCountry) map.'fromCountry.id' = fromCountry.id
        if (toLanguage) map.'toLanguage.id' = toLanguage.id
        if (toCountry) map.'toCountry.id' = toCountry?.id
        map._strict = ''
        if (strict) map.strict = 'on'
        map.display = display
        if (sort) map.sort = sort
        if (order) map.order = order
        if (max) map.max = max
        if (offset) map.offset = offset
        return map
    }

    def clearPagination() {
        sort = null
        order = null
        max = null
        offset = null
    }

    def getText() {
        def toLocale = toLanguage.code
        if (toCountry) toLocale += toCountry.code
        def fromLocale = fromLanguage ? fromLanguage.code : '*'
        if (fromCountry) fromLocale += fromCountry.code
        def strictText
        if (strict) {
            strictText = utilService.message(code: 'translation.strict.label', default: 'Strict')
        } else {
            strictText = utilService.message(code: 'translation.text.not.strict', default: 'Best-Fit')
        }

        def displayText = utilService.message(code: 'translation.display.label', default: 'Display')
        displayText += ': '
        displayText += utilService.message(code: 'translation.display.' + display, default: display)
        return utilService.message(code: 'translation.text', args: [toLocale, fromLocale, strictText, displayText],
            default: "Translate to locale ${toLocale} from locale ${fromLocale} (${strictText}). ${displayText}.")
    }

    def getToLocale() {
        return toCountry ? toLanguage.code + toCountry.code : toLanguage.code
    }
}
