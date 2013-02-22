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
package org.grails.tlc.rest

import org.grails.tlc.books.Document
import grails.converters.JSON

class RestController {

    // Injected services
    def utilService
    def bookService
    def postingService
    def restService

    // Security settings
    def activities = [default: 'attached']

    // List of actions with specific request types
    static allowedMethods = [save: 'POST']

    def save() {

        // Get the document and any allocation info
        def map = restService.getDocument(params.data)
        if (map instanceof CharSequence) {
            response.status = 400
            render(text: [reason: map] as JSON, contentType: 'text/plain', encoding: 'UTF-8')
            return
        }

        // For testing purposes
        //restService.dump(map)

        // Grab the details from the returned map
        def documentInstance = map.document
        def manualAllocations = map.manual
        def autoAllocations = map.auto
        def reversalInstance = map.reversal
        def valid

        // Check if we need an extended transaction
        if (reversalInstance) {

            // This outer lock (the posting service will inner-lock for each document) is needed so that
            // we can guarantee a stable situation for BOTH documents to be posted since, without this,
            // it is possible that a database deadlock could occur
            def lock = bookService.getCompanyLock(utilService.currentCompany())
            lock.lock()
            try {
                Document.withTransaction {status ->
                    valid = postingService.post(documentInstance, status)
                    if (valid) {
                        valid = postingService.post(reversalInstance, status)
                        if (!valid) {
                            documentInstance.errorMessage(code: 'document.reversal.error', default: 'Error posting the reversal')
                            status.setRollbackOnly()
                        }
                    } else {
                        status.setRollbackOnly()
                    }
                }
            } finally {
                lock.unlock()
            }
        } else {    // No extended transaction required
            valid = postingService.post(documentInstance)
        }

        // Check we posted the document(s) ok
        if (!valid) {
            response.status = 400
            render(text: [reason: getAnyError(documentInstance, message(code: 'rest.posting.error',
                        default: 'Error posting the document'))] as JSON, contentType: 'text/plain', encoding: 'UTF-8')
            return
        }

        // Perform any manual allocations requested
        for (alloc in manualAllocations) postingService.allocateLine(alloc.transaction, alloc)

        // Perform any auto-allocations requested
        for (account in autoAllocations) postingService.autoAllocate(account)

        // Successful return
        render(text: [code: documentInstance.code] as JSON, contentType: 'text/plain', encoding: 'UTF-8')
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private getAnyError(doc, defaultMessage) {
        return utilService.getFirstErrorMessage(doc) ?: (defaultMessage ?: message(code: 'rest.gen.error', default: 'Unspecified error'))
    }
}
