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

import java.beans.PropertyEditorSupport
import java.text.DateFormat
import java.text.ParseException
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.support.RequestContextUtils

class BasicDatePropertyEditor extends PropertyEditorSupport {

    public String getAsText() {
        def value = getValue()
        if (value) {
            def fmt = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, RequestContextUtils.getLocale(RequestContextHolder.currentRequestAttributes().getCurrentRequest()))
            fmt.setLenient(false)
            value = fmt.format(value)
        }

        return value
    }

    public void setAsText(String text) throws IllegalArgumentException {
        if (text) {
            def locale = RequestContextUtils.getLocale(RequestContextHolder.currentRequestAttributes().getCurrentRequest())
            def fmt = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale)
            fmt.setLenient(false)
            try {
                setValue(fmt.parse(text))
            } catch (ParseException pe1) {
                fmt = DateFormat.getDateInstance(DateFormat.SHORT, locale)
                fmt.setLenient(false)
                try {
                    setValue(fmt.parse(text))
                } catch (ParseException pe2) {
                    throw new IllegalArgumentException(pe2)
                }
            }
        } else {
            setValue(null)
        }
    }
}
