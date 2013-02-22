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

import org.apache.log4j.Level
import org.apache.log4j.Logger

// Based on the runtime loggin plugin by Jason Morris (jason.morris@torusit.com)
class RuntimeLoggingController {

    static final levelList = ['OFF','TRACE','DEBUG','INFO','WARN','ERROR','FATAL']

    static final grailsLogs = [
        [name: 'Apache Commons', logger: 'org.codehaus.groovy.grails.commons'],
        [name: 'Controllers', logger: 'grails.app.controllers'],
        [name: 'Domains', logger: 'grails.app.domain'],
        [name: 'Filters', logger: 'grails.app.filters'],
        [name: 'Grails Application', logger: 'grails.app'],
        [name: 'Grails Web Requests', logger: 'org.codehaus.groovy.grails.web'],
        [name: 'Plugins', logger: 'org.codehaus.groovy.grails.plugins'],
        [name: 'Services', logger: 'grails.app.services'],
        [name: 'TagLibs', logger: 'grails.app.taglib'],
        [name: 'URL Mappings', logger: 'org.codehaus.groovy.grails.web.mapping']
    ]

    static final otherLogs = [
        [name: 'Ehcache', logger: 'net.sf.ehcache.hibernate'],
        [name: 'Hibernate', logger: 'org.hibernate'],
        [name: 'Message Source', logger: 'org.grails.tlc.sys.DatabaseMessageSource'],
        [name: 'Spring', logger: 'org.springframework'],
        [name: 'SQL', logger: 'org.hibernate.SQL']
    ]

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [setLogLevel: 'POST']

    // By default render the standard "chooser" view
    def index() {
        def rootLevel = Logger.rootLogger.level

        def domainLoggers = buildArtefactLoggerMapList('Domain')
        addCurrentLevelToLoggerMapList(domainLoggers, rootLevel)

        def controllerLoggers = buildArtefactLoggerMapList('Controller')
        addCurrentLevelToLoggerMapList(controllerLoggers, rootLevel)

        def serviceLoggers = buildArtefactLoggerMapList('Service')
        addCurrentLevelToLoggerMapList(serviceLoggers, rootLevel)

        def grailsLoggers = []
        for (it in grailsLogs) grailsLoggers << it.clone()
        addCurrentLevelToLoggerMapList(grailsLoggers, rootLevel)

        def otherLoggers = []
        for (it in otherLogs) otherLoggers << it.clone()
        addCurrentLevelToLoggerMapList(otherLoggers, rootLevel)

        render (view: 'logging',
                model: [
                    controllerLoggers: controllerLoggers,
                    serviceLoggers: serviceLoggers,
                    domainLoggers: domainLoggers,
                    grailsLoggers: grailsLoggers,
                    otherLoggers: otherLoggers,
                    levelList: levelList,
                    rootLevel: rootLevel.toString()
                ])
    }

    // Sets the log level based on parameter values
    def setLogLevel() {
        def logger = params.logger
        def level = Level.toLevel(params.level)

        // Find the right Logger
        def l
        if (logger) {
            l = Logger.getLogger(logger)
        } else {
            l = Logger.getRootLogger()
        }

        // Set the Logger level
        l.setLevel(level)
        log.info("Logger $logger set to level $level")
        render (view: 'confirm', model: [logger: logger, level: level])
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private addCurrentLevelToLoggerMapList(loggerMapList, rootLevel) {
        for (it in loggerMapList) it.name = "${it.name} - ${Logger.getLogger(it.logger).getLevel() ?: rootLevel}"
    }

    private classCase(s) {

        // Effectively ucFirst() i.e. first letter of artefacts must be upper case
        if (!s) return s;

        def head = s[0].toUpperCase(Locale.US)
        def tail = (s.length() > 1 ? s.substring(1) : '')

        return "${head}${tail}"
    }

    private loggerName(name, artefactType) {

        // Domains just use the artefact name, controllers/services etc need "Controller"/"Service" etc appended
        return (artefactType.toLowerCase(Locale.US) == 'domain') ? "${classCase(name)}" : "${classCase(name)}${artefactType}"
    }

    private buildArtefactLoggerMapList(artefactType) {
        def artefacts = grailsApplication.getArtefacts(artefactType)
        def artefactList = []
        for (it in artefacts) artefactList << it

        // sort the artefacts into alphabetical order
        Collections.sort(artefactList, new GrailsArtefactComparator())
        def logMapList = []
        for (it in artefactList) {
            def artefactLoggerMap = [name: it.fullName, logger: "grails.app.${artefactTypeName(artefactType)}.${loggerName(it.logicalPropertyName, artefactType)}"]
            logMapList += artefactLoggerMap
        }

        return logMapList
    }

    private artefactTypeName(artefactType) {
        artefactType = artefactType.toLowerCase(Locale.US)
        if (artefactType == 'controller' || artefactType == 'service') artefactType += 's'
        return artefactType
    }
}
