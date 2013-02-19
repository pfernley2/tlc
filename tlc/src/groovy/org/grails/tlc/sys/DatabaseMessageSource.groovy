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

import org.grails.tlc.corp.Message
import grails.util.GrailsWebUtil
import java.text.MessageFormat
import org.apache.log4j.Logger
import org.hibernate.SessionFactory
import org.springframework.context.ResourceLoaderAware
import org.springframework.context.support.AbstractMessageSource
import org.springframework.core.io.ResourceLoader
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.support.WebApplicationContextUtils
import org.springframework.web.servlet.support.RequestContextUtils

public class DatabaseMessageSource extends AbstractMessageSource implements ResourceLoaderAware {

	private static final log = Logger.getLogger(DatabaseMessageSource)
    private static final String CACHE_CODE = 'message'
    private static UtilService utilService
    private static SessionFactory sessionFactory
    private ResourceLoader resourceLoader = null

    @Override
    protected MessageFormat resolveCode(String code, Locale locale) {
        String msg = getBaseText(code, locale)
        return (msg != null) ? new MessageFormat(msg.replace("'", "''"), locale) : null
    }

    @Override
    protected String resolveCodeWithoutArguments(String code, Locale locale) {
        return getBaseText(code, locale)
    }
	
	// Our default messages should already be fully resolved
	protected String renderDefaultMessage(String defaultMessage, Object[] args, Locale locale) {
		return defaultMessage
	}

    // Used by Spring to inject our util service
    public void setUtilService(UtilService utilService) {
        this.utilService = utilService
    }

    // Used by Spring to inject the Hibernate session factory
    public void setSessionFactory(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory
    }

    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader
    }

    // Used by the two main 'resolve code' methods above plus the two methods
    // below: getMessageText and setError
    private String getBaseText(String code, Locale locale, Long securityCode = null) {

		// Forget it if no code supplied
		if (!code) return null
		
		// Avoid clogging up the cache with Grails' automatic attempt to get a message code based on
		// the package name. Grails does this to allow plugins to use the same domain and property
		// names. We do not use this facility since we are not a plugin.
		if (code.startsWith('org.grails.tlc.')) return null
		
		// Fix the legacy pagination texts that are still (as of Grails 2.2.0) still requested
		// by the pagination system before asking for the newer 'default' texts.
		if (code == 'paginate.next' || code == 'paginate.prev') code = 'default.' + code
		
		// A message starting with a special character of CacheService.DUMMY_VALUE will be taken to
		// be a code which, if not found, need only be logged at debug level rather that at info
		// level. This is used by the utilService.errorMessage() method to identify its attempts to
		// find an available message but not wanting to clutter up the log with unnecessary messages.
		// The dummy value is stripped off once it has been detected so that the plain code still
		// works as normal.
		def debugOnly = false
		if (code.startsWith(CacheService.DUMMY_VALUE) && code.length() > 1) {
			code = code.substring(1)
			debugOnly = true
		}
		
		// Grab the cache service
        def cache = utilService.cacheService
		
		// Ensure we always have a security code, even if we have to
		// default it to 0L (i.e. system level)
        if (securityCode == null) {
            def unbindRequest = false
            if (!RequestContextHolder.requestAttributes) {
                def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(App.servletContext)
                GrailsWebUtil.bindMockWebRequest(applicationContext)
                unbindRequest = true
            }

            securityCode = utilService.currentCompany()?.securityCode ?: 0L
            if (unbindRequest) RequestContextHolder.setRequestAttributes(null)
        }

		// Construct the cache key we're looking for
        def key = code + cache.IMPOSSIBLE_VALUE + locale.language + locale.country
		
		// Try and get it from the relevant cache.
        def msg = cache.get(CACHE_CODE, securityCode, key)
		
		// If we didn't find it in the relevant cache
		if (!msg) {
			
			// If it's a company specific search, try and find it in the
			// company specific database table.
			if (securityCode > 0L) {
				def lst = Message.findAll(
					"from Message as x where x.securityCode = ? and x.code = ? and x.locale in ('*', ?, ?) order by x.relevance desc",
					[securityCode, code, locale.language, locale.language + locale.country], [max: 1])
				if (lst) {
					msg = lst[0].text
					cache.put(CACHE_CODE, securityCode, key, msg)
				}
			}
			
			// If we didn't find it in the company specific database table
			// OR it was not a company specific search in the first place
			if (!msg) {
				
				// If it is a company specific search, we won't have looked
				// in the system cache yet, so look there now. Note that we
				// don't log this as a second cache miss in such cases.
				if (securityCode > 0L) msg = cache.get(CACHE_CODE, 0L, key, false)
				
				// If we still can't find it, look in the system level database table
				if (!msg) {
					def lst = SystemMessage.findAll(
						"from SystemMessage as x where x.code = ? and x.locale in ('*', ?, ?) order by x.relevance desc",
						[code, locale.language, locale.language + locale.country], [max: 1])
					if (lst) {
						msg = lst[0].text
					} else {
						msg = cache.IMPOSSIBLE_VALUE
						lst = "Unknown code '${code}' requested"
						debugOnly ? log.debug(lst) : log.info(lst)
					}
					
					cache.put(CACHE_CODE, securityCode, key, msg)
				}
			}
		}

        return (msg == cache.IMPOSSIBLE_VALUE) ? null : msg
    }

    def getMessageText(Map parameters) {

        def unbindRequest = false
        Locale locale = parameters.locale
        if (!locale) {
            if (RequestContextHolder.getRequestAttributes()) {
                locale = utilService.currentLocale()
            } else {    // Outside of an executing request, establish a mock version
                def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(App.servletContext)
                def requestAttributes = GrailsWebUtil.bindMockWebRequest(applicationContext)
                unbindRequest = true
                locale = RequestContextUtils.getLocale(requestAttributes.request)
            }
        }

        def msg
		if (parameters.domain) {
			msg = utilService.standardMessage(parameters.code, parameters.domain, parameters.value, parameters.forDomain)
		} else {
			msg = getBaseText(parameters.code, locale, parameters.securityCode)
	        if (msg) {
	            if (parameters.args) msg = new MessageFormat(msg.replace("'", "''"), locale).format(parameters.args as Object[])
	        } else {
	            msg = parameters.default
	        }
		}

        if (unbindRequest) RequestContextHolder.setRequestAttributes(null)
        if (msg && parameters.encodeAs) {
            switch (parameters.encodeAs.toLowerCase(Locale.US)) {	// Use US locale to avoid problems with Turkish undotted i
                case 'html':
                    msg = msg.encodeAsHTML()
                    break

                case 'xml':
                    msg = msg.encodeAsXML()
                    break

                case 'url':
                    msg = msg.encodeAsURL()
                    break

                case 'javascript':
                    msg = msg.encodeAsJavaScript()
                    break

                case 'base64':
                    msg = msg.encodeAsBase64()
                    break
            }
        }

        return msg
    }

    def setError(domain, parameters) {
        def msg = getMessageText(parameters)
        if (parameters.field) {
            domain.errors.rejectValue(parameters.field, null, msg)
        } else {
            domain.errors.reject(null, msg)
        }

        // If they didn't specify eviction and the domain id is for an existing record (i.e. id > 0),
        // OR if they specifically set evict to true, then remove the domain object from the Hibernate cache
        if ((parameters.evict == null && domain.id) || parameters.evict == true) sessionFactory.currentSession.evict(domain)

        return msg
    }

    static loadPropertyFile(file, locale, company = null) {
        def loc = locale ? locale.getLanguage() + locale.getCountry() : '*'
        def props = new Properties()
        def reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), 'UTF-8'))
        try {
            props.load(reader)
        } finally {
            if (reader) reader.close()
        }

        def rec, txt
        def counts = [imported: 0, skipped: 0]
		for (key in props.stringPropertyNames()) {
            txt = props.getProperty(key)
            if (key && key.length() <= 250 && txt && txt.length() <= 2000) {
                if (company) {
                    rec = Message.find('from Message as x where x.company = ? and x.code = ? and x.locale = ?', [company, key, loc])
                } else {
                    rec = SystemMessage.findByCodeAndLocale(key, loc)
                }

                if (!rec) {
                    if (company) {
                        rec = new Message()
                        rec.company = company
                    } else {
                        rec = new SystemMessage()
                    }

                    rec.code = key
                    rec.locale = loc
                    rec.text = txt
                    rec.saveThis()
                    counts.imported = counts.imported + 1
                } else {
                    counts.skipped = counts.skipped + 1
                }
            } else {
                counts.skipped = counts.skipped + 1
            }
        }

        return counts
    }

    static loadPageHelpFile(file, locale, key) {
        def loc = locale ? locale.getLanguage() + locale.getCountry() : '*'
        def reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), 'UTF-8'))
        def text = null
        try {
            def line = reader.readLine()
            while (line != null) {
                if (text == null)  {
                    text = line
                } else {
                    text = text + '\n' + line
                }

                line = reader.readLine()
            }
        } finally {
            if (reader) reader.close()
        }

        if (key.length() <= 250 && text.length() <= 8000) {
            def rec = SystemPageHelp.findByCodeAndLocale(key, loc)
            if (!rec) {
                rec = new SystemPageHelp()
                rec.code = key
                rec.locale = loc
                rec.text = text
                return rec.saveThis()
            }
        }

        return false
    }
}
