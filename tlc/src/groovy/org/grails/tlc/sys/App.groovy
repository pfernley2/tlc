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

import org.codehaus.groovy.grails.commons.GrailsApplication
import org.hibernate.SessionFactory
import org.springframework.context.MessageSource

class App {
    static MessageSource messageSource          // The Spring message handler to use
    static GrailsApplication grailsApplication  // The application in which we are running
    static SessionFactory sessionFactory        // The hibernate session factory

    static getConfig() {
        return grailsApplication.config
    }

    static getMetadata() {
        return grailsApplication.metadata
    }

    static getServletContext() {
        return grailsApplication.mainContext?.servletContext
    }

    static getPluginManager() {
        return grailsApplication.mainContext?.pluginManager
    }

    // --------------------------------------------- Initialization Methods ---------------------------------------------

    // Used by the Spring IoC container to inject the message source bean
    public void setMessageSource(MessageSource messageSource) {
        this.messageSource = messageSource
    }

    // Used by Spring to inject the Grails application
    public void setGrailsApplication(GrailsApplication grailsApplication) {
        this.grailsApplication = grailsApplication
    }

    // Used by Spring to inject the Hibernate session factory
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory
    }
}
