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

class MenuService {

    static transactional = false

    // Return the current menu option record or null if on the main menu
    def currentMenuOption(request, session, params) {
        def option

        if (params.id == '0') {
            session.userdata.menuId = 0
        } else {
            if (params.id) {
                if ("${request.menurec?.id}" == params.id) {
                    option = request.menurec
                } else {
                    option = SystemMenu.get(params.id)
                    if (option) {
                        request.menurec = option
                        session.userdata.menuId = option.id
                    }
                }
            }

            if (!option && session.userdata.menuId) {
                if (request.menurec?.id == session.userdata.menuId) {
                    option = request.menurec
                } else {
                    option = SystemMenu.get(session.userdata.menuId)
                    if (option) request.menurec = option
                }
            }
        }

        return option
    }

    // Returns the current list of options to be displayed on the current menu page
    def listMenuOptions(utilService, request, session, params) {
        def parent = 0
        def option = currentMenuOption(request, session, params)
        if (option) parent = (option.type == 'submenu') ? option.id : option.parent
        session.userdata.menuId = parent
        def list = SystemMenu.findAllByParent(parent, [sort: 'sequencer', cache: true])

        // Now handle the security stuff
        def securityService = utilService.securityService
        def cacheService = utilService.cacheService
        def user = utilService.currentUser()
        def company = utilService.currentCompany()

        return list.findAll {securityService.isUserActivity(cacheService, company, user, it.activity.code)}
    }

    // Return the list of menu crumbs to be displayed on the current menu page
    def listMenuCrumbs(cacheService, request, session, params) {
        def option = currentMenuOption(request, session, params)
        if (!option) return []
        def id = option.id
        if (option.type != 'submenu') {
            id = option.parent
            session.userdata.menuId = id
        }

        return getCrumbs(cacheService, request, id)
    }

    // Return the list of crumbs to be displayed on an option page
    def listOptionCrumbs(cacheService, request, session) {
        return getCrumbs(cacheService, request, session.userdata.menuId)
    }

    // Reset to the main menu page
    def reset(session) {
        if (session.userdata) session.userdata.menuId = 0
    }

    // Create a map of parameters for a menu option
    def getParamsAsMap(parameters) {
        def params = [:]
        if (parameters) {
            def items = parameters.split(',')
            def pos, code, value
            for (String item : items) {
                pos = item.indexOf(':')
                if (pos > 0) {
                    code = item.substring(0, pos).trim()
                    value = item.substring(pos + 1).trim()
                    if (code) {
                        params[code] = value
                    }
                }
            }
        }

        return params
    }

    // Create a list of parameters for a menu option
    def getParamsAsList(parameters) {
        def params = []
        if (parameters) {
            def items = parameters.split(',')
            def pos, code, value
            for (String item : items) {
                pos = item.indexOf(':')
                if (pos > 0) {
                    code = item.substring(0, pos).trim()
                    value = item.substring(pos + 1).trim()
                    if (code) {
                        params << code + '=' + value
                    }
                }
            }
        }

        return params
    }

    // Return a url encoded parameter string for a menu option
    def getParamsAsString(parameters) {
        def params = ""
        if (parameters) {
            def items = parameters.split(',')
            def pos, code, value
            for (String item : items) {
                pos = item.indexOf(':')
                if (pos > 0) {
                    code = item.substring(0, pos).trim()
                    value = item.substring(pos + 1).trim()
                    if (code) {
                        if (params) {
                            params = params + '&' + code.encodeAsURL() + '=' + value.encodeAsURL()
                        } else {
                            params = '?' + code.encodeAsURL() + '=' + value.encodeAsURL()
                        }
                    }
                }
            }
        }

        return params
    }

    def clearCrumbs(cacheService) {
        cacheService.clearThis('menuCrumb')
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private getCrumbs(cacheService, request, id) {
        if (!id) return []
        def crumb = getCrumb(cacheService, request, id)
        def list = [crumb]
        while(crumb.parent) {
            crumb = getCrumb(cacheService, request, crumb.parent)
            list << crumb
        }

        if (list.size() > 1) {
            list = list.reverse()
        }

        return list
    }

    private getCrumb(cacheService, request, id) {
        def value = cacheService.get('menuCrumb', 0L, "$id")
        if (value == null) {
            def option
            if (request.menurec?.id == id) {
                option = request.menurec
            } else {
                option = SystemMenu.get(id)
            }

            value = Collections.unmodifiableMap([id: option.id, path: option.path, parent: option.parent])
            cacheService.put('menuCrumb', 0L, "$id", value, 48 + option.path.length())
        }

        return value
    }
}
