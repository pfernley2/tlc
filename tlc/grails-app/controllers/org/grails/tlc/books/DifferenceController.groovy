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

class DifferenceController {

    // Injected services
    def utilService
    def bookService

    // Security settings
    def activities = [default: 'enquire']

    // List of actions with specific request types
    //static allowedMethods = [lines: 'POST', invoicing: 'POST', auto: 'POST', manual: 'POST', allocating: 'POST']

    def enquire() {
        def model = bookService.loadDocumentModel(params, ['FXD'])
        def documentInstance = model.documentInstance
        if (documentInstance.id) {
            def debit = 0.0
            def credit = 0.0
            def val
            def parameters = [context: documentInstance, field: 'value', currency: model.displayCurrency]
            for (line in documentInstance.lines) {
                parameters.line = line
                val = bookService.getBookValue(parameters)
                if (val != null && !(val instanceof String)) {
                    if (val < 0.0) {
                        credit -= val
                    } else {
                        debit += val
                    }
                }
            }

            model.totalInstance = [debit: debit, credit: credit, scale: parameters.scale]
        }

        model
    }
}
