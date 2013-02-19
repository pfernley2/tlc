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

class CriteriaController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'attached']

    // List of actions with specific request types
    static allowedMethods = [apply: 'POST']

    def apply() {

        // Grab the parameters
        def property = params.property
        def test = params.test
        def value = params.value
        def crController = params.crController
        def crAction = params.crAction
        def crDomain = params.crDomain

        if (property != 'none') {
            def verification = Obfusticator.decrypt("${crController}.${crAction}", params.crLinkage)
            if (verification.indexOf(CacheService.IMPOSSIBLE_VALUE) != -1) {
                verification = verification.substring(0, verification.indexOf(CacheService.IMPOSSIBLE_VALUE))
            }

            verification = verification.split(',') as List
            if (!verification.contains(property)) {
                throw new IllegalArgumentException("Unknown property '${property}'")
            }
        }

        // Clean up the parameters
        params.remove('property')
        params.remove('test')
        params.remove('value')
        params.remove('crController')
        params.remove('crAction')
        params.remove('crDomain')
        params.remove('crLinkage')

        // Clean up the user responses if required
        if (property == 'none' || test == 'none' ) {
            property = 'none'
            test = 'none'
            value = ''
        } else if (test == 'null' || test == 'not.null') {
            value = ''
        }

        // Get our service to store the info required for the target controller/action
        if (utilService.criteriaService.apply(session, crController, crAction, crDomain, property, test, value, utilService.currentLocale())) {
            flash.message = message(code: 'criteria.applied', default: 'Criteria changes applied')
            // Need to start on the first page if new criteria
            if (params.offset) params.remove('offset')
        } else {
            flash.message = message(code: 'criteria.invalid', default: 'Invalid criteria, no changes made')
        }

		// Clean up the params map removing some of the odd
		// things Grails automatically adds to it.
        test = params.keySet().iterator()
        while (test.hasNext()) {
            value = params.get(test.next())
            if (value instanceof Map || value instanceof List) test.remove()
        }

        // Redirect to the target controller/action
        redirect(controller: crController, action: crAction, params: params)
    }
}
