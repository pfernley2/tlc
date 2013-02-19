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

import java.text.DateFormat
import org.codehaus.groovy.grails.commons.GrailsDomainClassProperty

class DrilldownService {

    static transactional = false

    // Returns the parent domain instance by which the child records are to be
    // limited when the test parameter matches the controller.action for this
    // drilldown instance (if any). If the test parameter does not match the
    // parent controller.action or the security code is invalid then null is returned.
    def source(administrator, company, session, params, test, options = null) {

        def result
        def entry = entry(session, params, options)
        if (entry && test == entry.srcController + "." + entry.srcAction) {
            result = UtilService.getGrailsDomainClass(entry.srcDomain).newInstance().get(entry.srcValue)
            if (!administrator && result?.securityCode && result.securityCode != company?.securityCode) {
                throw new IllegalArgumentException('Drilldown security violation')
            }
        }

        return result
    }

    // Clears the list of drilldowns. This is typically called by menus to
    // indicate that any prior chain of drilldowns is now no longer required.
    def reset(session) {
        truncate(session, getDrilldowns(session), 0)
    }

    def entry(session, params, options) {

        // Get the list of drilldowns for the session
        def drilldowns = getDrilldowns(session)

        def entry

        // If there are drilldown parameters, we've just come from a 'parent' page
        if (params.ddController) {
            def joinId  // Indicates a many to many relationship. Actually holds the id property name of this domain

            def domain = options?.domain ?: params.controller
            domain = domain[0].toUpperCase(Locale.US) + domain[1..-1]

            def parent = params.ddDomain ?: params.ddController
            parent = parent[0].toUpperCase(Locale.US) + parent[1..-1]

            def property = options?.property
            def dom = UtilService.getGrailsDomainClass(domain)
            if (property) {
                if (dom.getPropertyByName(property).isManyToMany()) joinId = dom.getIdentifier().getName()
            } else {
                def persistents = dom.getPersistentProperties()
                for (GrailsDomainClassProperty dcp: persistents) {
                    if (dcp.getReferencedDomainClass()?.getName() == parent) {
                        property = dcp.getName()
                        if (dcp.isManyToMany()) joinId = dom.getIdentifier().getName()
                        break
                    }
                }
            }

            def id = UtilService.getGrailsDomainClass(parent).getIdentifier()
            property += "." + id.getName()

            def value = createValue(id.getType().getName(), params.ddValue)

            // Create and fill in the new drilldown entry map
            entry = [:]
            entry.srcController = params.ddController
            entry.srcAction = params.ddAction
            entry.srcDomain = parent
            entry.srcValue = value
            entry.srcMax = params.ddMax
            entry.srcOffset = params.ddOffset
            entry.srcSort = params.ddSort
            entry.srcOrder = params.ddOrder
            entry.tgtController = params.controller
            entry.tgtAction = params.action
            entry.tgtProperty = property
            entry.joinId = joinId

            // Clean up the request parameters (forces a lookup of existing
            // drilldowns on future calls within this request)
            params.remove("ddController")
            params.remove("ddAction")
            params.remove("ddDomain")
            params.remove("ddValue")
            params.remove("ddMax")
            params.remove("ddOffset")
            params.remove("ddSort")
            params.remove("ddOrder")

            // Work through the existing drilldown entries
            for (int pos = 0; pos < drilldowns.size; pos++) {

                // If we find one whose source is the given parent controller and action
                if (drilldowns[pos].srcController == entry.srcController && drilldowns[pos].srcAction == entry.srcAction) {
                    truncate(session, drilldowns, pos)
                    break // We've done
                }
            }

            // Append the new one to the end
            append(session, drilldowns, entry)
        } else {  // No new drilldown parameters, so look for old ones

            // Work backwards through the existing drilldown entries
            for (int pos = drilldowns.size - 1; pos >= 0; pos--) {

                // If we have an entry whose target is the current controller and action
                if (drilldowns[pos].tgtController == params.controller && drilldowns[pos].tgtAction == params.action) {

                    // Grab the details
                    entry = drilldowns[pos]
                    truncate(session, drilldowns, pos + 1)
                    break // We've done
                }
            }
        }

        return entry
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private getDrilldowns(session) {

        // Get the list of drilldowns for the session, creating it if required
        def drilldowns = session.drilldowns
        if (drilldowns == null) {
            drilldowns = []
            session.drilldowns = drilldowns
        }

        return drilldowns
    }

    private truncate(session, drilldowns, pos) {

        def selectors = getSelectors(session)
        if (selectors) {
            while (pos < drilldowns.size()) {
                selectors.remove(drilldowns[pos].tgtController + "." + drilldowns[pos].tgtAction)
                drilldowns.remove(pos)
            }
        }
    }

    private append(session, drilldowns, entry) {

        def selectors = getSelectors(session)

        // Need to ensure the target isn't already in the list
        for (int i = 0; i < drilldowns.size(); i++) {
            if (drilldowns[i].tgtController == entry.tgtController && drilldowns[i].tgtAction == entry.tgtAction) {

                // Chop out every entry up to and including the duplicate target entry
                for (int j = 0; j <= i; j++) {
                    selectors.remove(drilldowns[0].tgtController + "." + drilldowns[0].tgtAction)
                    drilldowns.remove(0)
                }

                break
            }
        }

        // Now append the new entry to the end of the drilldown list
        drilldowns.add(entry)

        def selector = [:]
        def members = [:]
        def memberEntry = [:]
        def properties = [entry.joinId ? entry.tgtProperty.substring(entry.tgtProperty.indexOf(".") + 1) : entry.tgtProperty]
        def tests = ["equal"]
        def parameters = [[entry.srcValue]]

        memberEntry.put("properties", properties)
        memberEntry.put("tests", tests)
        memberEntry.put("parameters", parameters)
        if (entry.joinId) {
            memberEntry.put("joinId", entry.joinId)
            memberEntry.put("joinProperty", entry.tgtProperty.substring(0, entry.tgtProperty.indexOf(".")))
        }
        members.put("drilldowns", memberEntry)
        selector.put("members", members)
        selectors.put(entry.tgtController + "." + entry.tgtAction, selector)
    }

    private getSelectors(session) {

        // Get the map of selectors for the session, creating it if required
        def selectors = session.selectors
        if (selectors == null) {
            selectors = [:]
            session.selectors = selectors
        }

        return selectors
    }

    private createValue(type, val) {
        switch (type) {
            case "java.lang.String":
                // Nothing to do since it's already a String
                break;

            case "char":
            case "java.lang.Character":
                if (val.length() == 1) {
                    val = new Character(val.charAt(0))
                } else {
                    val = null
                }
                break;

            case "java.util.Currency":
                try {
                    val = Currency.getInstance(val)
                } catch (IllegalArgumentException curex) {
                    val = null
                }
                break

            case "java.util.Locale":
                if (val ==~ /[a-z][a-z]/) {
                    val = new Locale(val)
                } else if (val ==~ /[a-z][a-z]_[A-Z][A-Z]/) {
                    val = new Locale(val[0..1], val[3..4])
                } else {
                    val = null
                }

                break

            case "java.util.TimeZone":
                val = TimeZone.getTimeZone(val)
                break

            case "byte":
            case "java.lang.Byte":
                try {
                    val = Byte.valueOf(val)
                } catch (NumberFormatException byex) {
                    val = null
                }
                break

            case "short":
            case "java.lang.Short":
                try {
                    val = Short.valueOf(val)
                } catch (NumberFormatException shex) {
                    val = null
                }
                break

            case "int":
            case "java.lang.Integer":
                try {
                    val = Integer.valueOf(val)
                } catch (NumberFormatException inex) {
                    val = null
                }
                break

            case "long":
            case "java.lang.Long":
                try {
                    val = Long.valueOf(val)
                } catch (NumberFormatException loex) {
                    val = null
                }
                break

            case "java.math.BigInteger":
                try {
                    val = new BigInteger(val)
                } catch (NumberFormatException biex) {
                    val = null
                }
                break

            case "float":
            case "java.lang.Float":
                try {
                    val = Float.valueOf(val)
                } catch (NumberFormatException flex) {
                    val = null
                }
                break

            case "double":
            case "java.lang.Double":
                try {
                    val = Double.valueOf(val)
                } catch (NumberFormatException duex) {
                    val = null
                }
                break

            case "java.math.BigDecimal":
                try {
                    val = new BigDecimal(val)
                } catch (NumberFormatException bdex) {
                    val = null
                }
                break

            case "java.util.Date":
                val = DateFormat.getInstance().parse(val)
                break

            case "java.util.Calendar":
                def dt = DateFormat.getInstance().parse(val)
                if (dt) {
                    val = Calendar.getInstance()
                    val.setTime(dt)
                } else {
                    val = null
                }
                break

            case "java.sql.Date":
                if (val.length() == 10) {
                    try {
                        val = java.sql.Date.valueOf(val)
                    } catch (IllegalArgumentException sd) {
                        val = null
                    }
                } else {
                    val = null
                }
                break

            case "java.sql.Time":
                if (val.length() == 5) {
                    try {
                        val = java.sql.Time.valueOf(val)
                    } catch (IllegalArgumentException sd) {
                        val = null
                    }
                } else {
                    val = null
                }
                break

            case "java.sql.Timestamp":
                if (val.length() == 16) {
                    try {
                        val = java.sql.Timestamp.valueOf(val)
                    } catch (IllegalArgumentException sd) {
                        val = null
                    }
                } else {
                    val = null
                }
                break

            case "boolean":
            case "java.lang.Boolean":
                if (val.equalsIgnoreCase("true")) {
                    val = true
                } else if (val.equalsIgnoreCase("false")) {
                    val = false
                } else {
                    val = null
                }
                break

            default:  // Unknown data type
                val = null
                break
        }

        return val
    }
}
