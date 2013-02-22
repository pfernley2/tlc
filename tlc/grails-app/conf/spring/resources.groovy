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
import org.springframework.web.context.request.RequestContextHolder
import org.grails.tlc.sys.BasicDatePropertyEditorRegistrar
import org.grails.tlc.sys.DatabaseMessageSource
import org.grails.tlc.sys.TaskExecutable
import org.grails.tlc.sys.TaskScanner
import org.grails.tlc.sys.App

beans = {
    basicDatePropertyEditorRegistrar(BasicDatePropertyEditorRegistrar) {}

    messageSource(DatabaseMessageSource) {
        utilService = ref('utilService')
        sessionFactory = ref('sessionFactory')
    }

    app(App) {
        grailsApplication = ref('grailsApplication')
        messageSource = ref('messageSource')
        sessionFactory = ref('sessionFactory')
    }

    for (domainClass in application.domainClasses) {
        domainClass.metaClass.message = {Map parameters ->
            App.messageSource.getMessageText(parameters)
        }

        domainClass.metaClass.errorMessage = {Map parameters ->
            App.messageSource.setError(delegate, parameters)
        }

        domainClass.metaClass.saveThis = {flags ->
            delegate.save(deepValidate: false, flush: (flags && flags['flush'] != null) ? flags['flush'] : true)
        }

        domainClass.metaClass.validateThis = {
            delegate.validate(deepValidate: false)
        }

        domainClass.metaClass.static.selectList = {
            def arguments = it ?: [:]
            def webRequest = RequestContextHolder.currentRequestAttributes()
            def session = webRequest.getSession()
            def params = webRequest.getParams()
            def queryStatement = ''
            def queryParameters = []
            def joins = ''
            def joinId, member
            def selectors = session.selectors
            def controller = arguments.controller ?: params.controller
            def action = arguments.action ?: params.action
            def selector = null

            // We need a controller and an action for any sort of limitation
            if (controller && action) {

                // See if they've passed in any where-clause of their own
                if (arguments.where) {
                    queryStatement = " where (${arguments.where})"
                    if (arguments.params) {
                        if (arguments.params instanceof List) {
                            queryParameters.addAll(arguments.params)
                        } else {
                            queryParameters.add(arguments.params)
                        }
                    }
                }

                // Check for any company or securityCode restriction
                if (arguments.company) {
                    if (queryStatement) {
                        queryStatement += ' and x.company = ?'
                    } else {
                        queryStatement = ' where x.company = ?'
                    }

                    queryParameters.add(arguments.company)
                } else if (arguments.securityCode) {
                    if (queryStatement) {
                        queryStatement += ' and x.securityCode = ?'
                    } else {
                        queryStatement = ' where x.securityCode = ?'
                    }

                    queryParameters.add(arguments.securityCode)
                }

                // Look for any limitations in the selectors (as opposed to those passed in as arguments to this method)
                if (selectors) {
                    selector = selectors.get(controller + "." + action)

                    // If there limitations stored within the selector for this controller and action
                    if (selector?.members) {
                        for (item in selector.members) {
                            member = item.value
                            def prefix = 'x'
                            def join = member.get('joinProperty')
                            if (join) {
                                joinId = member.get('joinId')
                                prefix = join + '_ref'
                                joins += " join x.${join} as ${prefix}"
                            }

                            def properties = member.get('properties')
                            def tests = member.get('tests')
                            def parameters = member.get('parameters')

                            for (int i = 0; i < properties.size(); i++) {
                                if (queryStatement) {
                                    queryStatement += " and ${prefix}."
                                } else {
                                    queryStatement = " where ${prefix}."
                                }

                                queryStatement += properties[i]
                                switch (tests[i]) {
                                    case 'equal':
                                        queryStatement += ' = ?'
                                        break

                                    case 'not.equal':
                                        queryStatement += ' != ?'
                                        break

                                    case 'less':
                                        queryStatement += ' < ?'
                                        break

                                    case 'less.or.equal':
                                        queryStatement += ' <= ?'
                                        break

                                    case 'greater':
                                        queryStatement += ' > ?'
                                        break

                                    case 'greater.or.equal':
                                        queryStatement += ' >= ?'
                                        break

                                    case 'null':
                                        queryStatement += ' is null'
                                        break

                                    case 'not.null':
                                        queryStatement += ' is not null'
                                        break

                                    case 'like':
                                        queryStatement += ' like ?'
                                        break

                                    case 'not.like':
                                        queryStatement += ' not like ?'
                                        break

                                    case 'between':
                                        queryStatement += ' between ? and ?'
                                        break

                                    case 'not.between':
                                        queryStatement += ' not between ? and ?'
                                        break

                                    case 'in':
                                        queryStatement += ' in ('
                                        for (int j = 0; j < parameters[i].size(); j++) {
                                            queryStatement += (j == 0) ? '?' : ', ?'
                                        }
                                        queryStatement += ')'
                                        break

                                    case 'not.in':
                                        queryStatement += ' not in ('
                                        for (int j = 0; j < parameters[i].size(); j++) {
                                            queryStatement += (j == 0) ? '?' : ', ?'
                                        }
                                        queryStatement += ')'
                                        break

                                    default:
                                        def msg = "Unknown selector test of '${tests[i]}'"
                                        log.error(msg)
                                        throw new IllegalArgumentException(msg)
                                }

                                if (parameters) {
                                    queryParameters.addAll(parameters[i])
                                }
                            }
                        }
                    } else {
                        selector = null
                    }
                }

                // If there is a limitation but it was created from parameters to this method rather than from
                // members in a selector, then we need to create a selector and put it in the selectors map
                if (queryStatement && !selector) {
                    if (!selectors) {
                        selectors = [:]
                        session.selectors = selectors
                    }

                    selector = [:]
                    selectors.put(controller + "." + action, selector)
                }
            }

            queryStatement = "from ${delegate.name} as x" + joins + queryStatement

            // Store the finished statement in the selector for our controller and action, if any. This is typically
            // used later by a selectCount() call to avoid having to recreate the statement all over again.
            if (selector != null) {
                selector.queryStatement = queryStatement
                selector.queryParameters = queryParameters
            }

            if (joins) queryStatement = "from ${delegate.name} as y where y.id in (select x.id ${queryStatement})"

            def max = params.max ? params.max.toInteger() : 1000
            def offset = params.offset ? params.offset.toInteger() : 0
            def sort = params.sort
            def order = params.order ?: 'asc'
            if (sort) queryStatement += " order by ${joins ? 'y' : 'x'}.${sort} ${order}"

            log.debug("${queryStatement}; Parameters = ${queryParameters}; Pagination = [max:${max}, offset:${offset}]")
            return delegate.findAll(queryStatement, queryParameters, [max: max, offset: offset])
        }

        domainClass.metaClass.static.selectCount = {
            def arguments = it ?: [:]
            def webRequest = RequestContextHolder.currentRequestAttributes()
            def session = webRequest.getSession()
            def params = webRequest.getParams()
            def queryStatement = "select count(*) from ${delegate.name} as x"
            def queryParameters = []
            def selectors = session.selectors
            if (selectors) {
                def controller = arguments.controller ?: params.controller
                def action = arguments.action ?: params.action
                if (controller && action) {
                    def selector = selectors.get(controller + '.' + action)
                    if (selector?.queryStatement) {
                        queryStatement = 'select count(*) ' + selector.queryStatement
                        queryParameters = selector.queryParameters
                    }
                }
            }

            log.debug("${queryStatement}; Parameters = ${queryParameters}")
            return delegate.executeQuery(queryStatement, queryParameters)[0]
        }
    }

    for (serviceClass in application.serviceClasses) {
        serviceClass.metaClass.message = {Map parameters ->
            App.messageSource.getMessageText(parameters)
        }
    }

    taskScanner(TaskScanner) {
        sessionFactory = ref('sessionFactory')
    }

    taskExecutable(TaskExecutable) {
        messageSource = ref('messageSource')
        grailsApplication = ref('grailsApplication')
        sessionFactory = ref('sessionFactory')
        utilService = ref('utilService')
        mailService = ref('mailService')
        bookService = ref('bookService')
        postingService = ref('postingService')
    }
}