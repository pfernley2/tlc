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

import grails.converters.JSON

class SecurityFilters {

    def utilService
    def restService

    def filters = {
        accessControl(controller: '*', action: '*') {
            before = {

                // Recognizes the five special system activity codes as follows:
                //
                // any      - User does not even need to be logged in
                // logout   - As above, but a special singleton code to identify the logout action
                // login    - User must be logged in
                // sysadmin - User must be logged in as a system administrator but neeed not necessarily be attached to a company
                // attached - User must be logged in and attached to a company but need not be specifically granted this activity
                //
                // Also recognizes the 'administrator' flag in a user record as
                // defining whether a user is a system administrator or not

                // Root page request, so allow
                if (!controllerName) return true

                // Grab the activity associated with the controller and action
                def activity = utilService.securityService.getActionActivity(utilService.cacheService, controllerName, actionName ?: 'index')

                // Always allow logout
                if (activity == 'logout') return true

                // Enforce any next step
                def nextStep = utilService.getNextStep()
                if (nextStep) {
                    if (!(nextStep.sourceController == controllerName && nextStep.sourceAction == actionName) &&
                    !(nextStep.targetController == controllerName && nextStep.targetAction == actionName)) {

                        redirect(controller: nextStep.sourceController, action: nextStep.sourceAction)
                        return false
                    }
                }

                // Check for unknown controller/action activity
                if (!activity) {
                    redirect(controller: 'system', action: 'access')
                    return false
                }

                // Check for global accessibility
                if (activity == 'any') return true

                // At this point we need to check if this is a RESTful request
                def restful = restService.isRestful(session, request, params)
                if (restful instanceof String) {
                    response.status = 400
                    render(text: [reason: restful] as JSON, contentType: 'text/plain', encoding: 'UTF-8')
                    return false
                }

                // Get the current user, if any
                def user = utilService.currentUser()

                // Since we've dealt with 'any' and 'logout' (the only two system
                // activities that don't require a valid user) the lack of a logged
                // in user at this point is definitely a security violation
                if (!user) {
                    redirect(controller: 'system', action: 'access')
                    return false
                }

                // For everyone other than a system administrator, refuse to go futher if actions
                // have been disabled. Note that the login and registration actions of the system
                // controller take care of not allowing new logins
                if (!user.administrator && utilService.getCurrentOperatingState() == 'actionsDisabled') {

                    // Need a different reaction if a RESTful request
                    if (restful) {
                        response.status = 400
                        render(text: [reason: restService.getMessage(code: 'rest.actions.disabled', default: 'REST processing is temporarily disabled')] as JSON, contentType: 'text/plain', encoding: 'UTF-8')
                    } else {
                        redirect(controller: 'system', action: 'actionsDisabled')
                    }

                    return false
                }

                // If we only need a logged in user, then we're happy
                if (activity == 'login') return true

                // The only other system activity which does not need the user to be
                // attached to a company is 'sysadmin' which can only be used by a
                // system administrator
                if (activity == 'sysadmin') {
                    if (user.administrator) return true

                    // Need a different reaction if a RESTful request
                    if (restful) {
                        response.status = 404
                        render(text: [reason: restService.getMessage(code: 'rest.no.permission', default: 'REST request permission denied')] as JSON, contentType: 'text/plain', encoding: 'UTF-8')
                    } else {
                        redirect(controller: 'system', action: 'access')
                    }

                    return false
                }

                // Grab the current company, if any
                def company = utilService.currentCompany()

                // Since we've exhausted all system activity codes that don't need
                // the user to be attached to a company, no company at this point
                // must represent an unacceptable situation
                if (!company) {

                    // Need a different reaction if a RESTful request
                    if (restful) {
                        response.status = 404
                        render(text: [reason: restService.getMessage(code: 'rest.no.permission', default: 'REST request permission denied')] as JSON, contentType: 'text/plain', encoding: 'UTF-8')
                    } else {
                        redirect(controller: 'system', action: 'access')
                    }

                    return false
                }

                // A system administrator attached to a company can do anything
                if (user.administrator) return true

                // If all we need is an attached user, then we're happy
                if (activity == 'attached') return true

                // Need to check if the company user is allowed to perform this activity
                if (!utilService.securityService.isUserActivity(utilService.cacheService, company, user, activity)) {

                    // Need a different reaction if a RESTful request
                    if (restful) {
                        response.status = 404
                        render(text: [reason: restService.getMessage(code: 'rest.no.permission', default: 'REST request permission denied')] as JSON, contentType: 'text/plain', encoding: 'UTF-8')
                    } else {
                        redirect(controller: 'system', action: 'access')
                    }

                    return false
                }

                // Everything is ok
                return true
            }
        }
    }
}
