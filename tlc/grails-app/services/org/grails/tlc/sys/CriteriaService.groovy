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
import java.text.NumberFormat
import java.text.ParseException
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.exceptions.InvalidPropertyException

class CriteriaService {

    static transactional = false

	static genericLabels = ['id', 'securityCode', 'dateCreated', 'lastUpdated', 'version']
    static HashSet criteriaTypes = new HashSet([
            'char',
            'java.lang.Character',
            'java.lang.String',
            'java.util.Currency',
            'java.util.Locale',
            'java.util.TimeZone',
            'byte',
            'java.lang.Byte',
            'short',
            'java.lang.Short',
            'int',
            'java.lang.Integer',
            'long',
            'java.lang.Long',
            'java.math.BigInteger',
            'float',
            'java.lang.Float',
            'double',
            'java.lang.Double',
            'java.math.BigDecimal',
            'java.util.Date',
            'java.sql.Date',
            'java.sql.Time',
            'java.sql.Timestamp',
            'java.util.Calendar',
            'boolean',
            'java.lang.Boolean'
    ])

    def getDomainProperties(params, options = null) {
        def dom = getDomain(params, options)
        def codePrefix = dom[0].toLowerCase(Locale.US) + dom[1..-1] + '.'	// Use US locale to avoid problems like Turkish undotted i
        def include = options?.include
        def exclude // = options?.exclude        Exclusions disabled in the bookkeeping system

        GrailsDomainClass dc = UtilService.getGrailsDomainClass(dom)
        def props = []
        appendProperty(dc.getIdentifier(), props, include, exclude, codePrefix, '')

        try {
            appendProperty(dc.getVersion(), props, include, exclude, codePrefix, '')
        } catch (InvalidPropertyException ipe1) {}

        try {
            appendProperty(dc.getPropertyByName('dateCreated'), props, include, exclude, codePrefix, '')
        } catch (InvalidPropertyException ipe2) {}

        try {
            appendProperty(dc.getPropertyByName('lastUpdated'), props, include, exclude, codePrefix, '')
        } catch (InvalidPropertyException ipe3) {}

        processPersistents(dc, props, include, exclude)

        return props
    }

    def getDomain(params, options) {
        return options?.domain ?: params.controller[0].toUpperCase(Locale.US) + params.controller[1..-1]
    }

    def apply(session, controller, action, domain, property, test, value, locale) {
        GrailsDomainClass dc = UtilService.getGrailsDomainClass(domain)

        def valid = true

        if (property == 'none') {
            removeCriteria(session, controller + '.' + action)
        } else {
            def prop

            for (it in property.tokenize('.')) {
                try {   // Bug in Grails 1.0.3
                    prop = dc.getPropertyByName(it)
                } catch (Exception ex) {
                    prop = dc.getPropertyByName(it[0].toUpperCase(Locale.US) + it[1..-1])
                }

                // Switch the domain class to the embedded class
                if (prop.isEmbedded()) dc = prop.getComponent()
            }

            def type = getPropertyType(prop)
            def values = []
            def val
            switch (test) {
                case 'equal':
                case 'not.equal':
                case 'less':
                case 'less.or.equal':
                case 'greater':
                case 'greater.or.equal':
                    val = getValue(type, value, test, locale)
                    if (val != null) {
                        values << val
                    } else {
                        valid = false
                        break
                    }
                    break

                case 'null':
                case 'not.null':
                    // Nothing to do
                    break

                case 'like':
                case 'not.like':
                    val = getValue(type, value, test, locale)
                    if (val != null) {
                        values << val
                    } else {
                        valid = false
                        break
                    }
                    break

                case 'between':
                case 'not.between':
                    def vals = value.split(' & ')*.trim()
                    if (vals.size() == 2) {
                        for (String v: vals) {
                            val = getValue(type, v, test, locale)
                            if (val != null) {
                                values << val
                            } else {
                                valid = false
                                break
                            }
                        }
                    } else {
                        valid = false
                    }
                    break

                case 'in':
                case 'not.in':
                    def vals = value.split(' | ')*.trim()
                    for (String v: vals) {

                        // Weirdo in groovy split functionality with pipe character
                        if (v != '|') {
                            val = getValue(type, v, test, locale)
                            if (val != null) {
                                values << val
                            } else {
                                valid = false
                                break
                            }
                        }
                    }
                    break

                default:
                    valid = false
                    break
            }

            if (valid) setCriteria(session, controller + '.' + action, property, test, values, value)
        }

        return valid
    }

    def getCriteria(session, params) {
        return getSelectors(session).get(params.controller + '.' + params.action)?.get('members')?.get('criteria') ?: [:]
    }

    def reset(session) {
        def selectors = getSelectors(session)
        def removables = []
        def selector, members
        for (item in selectors) {
			selector = item.value
            members = selector.get('members')
            if (members?.containsKey('criteria')) {
                members.remove('criteria')
                if (members.size() == 0) {
                    selector.remove('members')
                }

                selector.remove('queryStatement')
                selector.remove('queryParameters')

                if (selector.size() == 0) removables << item.key
            }
        }

        for (it in removables) selectors.remove(it)
    }

    def removeCriteria(session, key) {
        def selectors = getSelectors(session)
        if (selectors) {
            def selector = selectors.get(key)
            if (selector) {
                def members = selector.members
                if (members?.containsKey('criteria')) {
                    members.remove('criteria')
                    if (members.size() == 0) {
                        selector.remove('members')
                    }

                    selector.remove('queryStatement')
                    selector.remove('queryParameters')
                    if (selector.size() == 0) {
                        selectors.remove(key)
                    }
                }
            }
        }
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private processPersistents(domainClass, props, include, exclude, defaultPrefix = '', propertyPrefix = '') {
        def name
        def domain = domainClass.getPropertyName()
        def codePrefix = domain[0].toLowerCase(Locale.US) + domain[1..-1] + '.'

        for (it in domainClass.getPersistentProperties()) {

            // Deal with embedded classes
            if (it.isEmbedded()) {
                processPersistents(it.getComponent(), props, include, exclude, it.getNaturalName() + ' - ', getPropertyName(it) + '.')
            } else {
                name = it.getName()

                // Do not included id, version, dateCreated or lastUpdated for
                // embedded classes. The parent domain class has had them
                // 'manually' included already.
                if (!it.isIdentity() && name != 'version' && name != 'dateCreated' && name != 'lastUpdated') {
                    appendProperty(it, props, include, exclude, codePrefix, defaultPrefix, propertyPrefix)
                }
            }
        }
    }

    private getValue(type, val, test, locale) {

        // Need some sort of value. We return null to mean 'invalid value'
        if (val != null) {

            // 'Like' and 'Not Like' tests need at least two characters to make any sense
            if (test == 'like' || test == 'not.like') {

                // A valid Like/Not like value cannot be type checked (e.g. for
                // a valid Currency code etc), so just pass it back, but such
                // tests can only be applied to string type fields
                if (val.length() < 2
                        || (type != 'java.lang.String'
                        && type != 'java.util.Currency'
                        && type != 'java.util.Locale'
                        && type != 'java.util.TimeZone')) {

                    val = null
                }
            } else if (val.length() == 0 && type != 'java.lang.String') {

                // Only String data types may be tested againt a blank value
                val = null
            } else if ((type == 'boolean' || type == 'java.lang.Boolean')
                    && test != 'equal' && test != 'not.equal'
                    && test != 'null' && test != 'not.null') {

                // Boolean values can only be tested for equal, not equal, null and not null
                val = null
            } else if (!(type instanceof String)) {

                // Check for enum where the 'type' is actually an array of
                // the enum constants
                def found = false
                for (int i = 0; i < type.length; i++) {
                    if (type[i].toString() == val) {
                        val = type[i]
                        found = true
                        break
                    }
                }

                if (!found) val = null
            } else {
                switch (type) {
                    case 'java.lang.String':
                        // Nothing to do since it's already a String
                        break;

                    case 'char':
                    case 'java.lang.Character':
                        if (val.length() == 1) {
                            val = new Character(val.charAt(0))
                        } else {
                            val = null
                        }
                        break;

                    case 'java.util.Currency':
                        try {
                            val = Currency.getInstance(val)
                        } catch (IllegalArgumentException curex) {
                            val = null
                        }
                        break

                    case 'java.util.Locale':
                        if (val ==~ /[a-z][a-z]/) {
                            val = new Locale(val)
                        } else if (val ==~ /[a-z][a-z]_[A-Z][A-Z]/) {
                            val = new Locale(val[0..1], val[3..4])
                        } else {
                            val = null
                        }

                        break

                    case 'java.util.TimeZone':
                        val = TimeZone.getTimeZone(val)
                        break

                    case 'byte':
                    case 'java.lang.Byte':
                        try {
                            val = NumberFormat.getIntegerInstance(locale).parse(val)
                            val = (val < Byte.MIN_VALUE || val > Byte.MAX_VALUE) ? null : val.byteValue()
                        } catch (ParseException byex) {
                            val = null
                        }
                        break

                    case 'short':
                    case 'java.lang.Short':
                        try {
                            val = NumberFormat.getIntegerInstance(locale).parse(val)
                            val = (val < Short.MIN_VALUE || val > Short.MAX_VALUE) ? null : val.shortValue()
                        } catch (ParseException shex) {
                            val = null
                        }
                        break

                    case 'int':
                    case 'java.lang.Integer':
                        try {
                            val = NumberFormat.getIntegerInstance(locale).parse(val)
                            val = (val < Integer.MIN_VALUE || val > Integer.MAX_VALUE) ? null : val.intValue()
                        } catch (ParseException inex) {
                            val = null
                        }
                        break

                    case 'long':
                    case 'java.lang.Long':
                        try {
                            val = NumberFormat.getIntegerInstance(locale).parse(val)
                            val = (val < Long.MIN_VALUE || val > Long.MAX_VALUE) ? null : val.longValue()
                        } catch (ParseException loex) {
                            val = null
                        }
                        break

                    case 'java.math.BigInteger':
                        try {
                            val = NumberFormat.getIntegerInstance(locale).parse(val)
                            if (!(val instanceof BigInteger)) val = new BigInteger(val.toString())
                        } catch (ParseException biex) {
                            val = null
                        }
                        break

                    case 'float':
                    case 'java.lang.Float':
                        try {
                            val = NumberFormat.getInstance(locale).parse(val)
                            val = (val < Float.MIN_VALUE || val > Float.MAX_VALUE) ? null : val.floatValue()
                        } catch (ParseException flex) {
                            val = null
                        }
                        break

                    case 'double':
                    case 'java.lang.Double':
                        try {
                            val = NumberFormat.getInstance(locale).parse(val)
                            val = (val < Double.MIN_VALUE || val > Double.MAX_VALUE) ? null : val.doubleValue()
                        } catch (ParseException duex) {
                            val = null
                        }
                        break

                    case 'java.math.BigDecimal':
                        try {
                            val = NumberFormat.getInstance(locale).parse(val)
                            if (!(val instanceof BigDecimal)) val = new BigDecimal(val.toString())
                        } catch (ParseException bdex) {
                            val = null
                        }
                        break

                    case 'java.util.Date':
                        val = parseDate(val, locale)
                        break

                    case 'java.util.Calendar':
                        def dt = parseDate(val, locale)
                        if (dt) {
                            val = Calendar.getInstance()
                            val.setTime(dt)
                        } else {
                            val = null
                        }
                        break

                    case 'java.sql.Date':
                        def dt = parseDate(val, locale)
                        val = dt ? new java.sql.Date(UtilService.fixDate(dt).getTime()) : null
                        break

                    case 'java.sql.Time':
                        def dt = parseDate(val, locale)
                        if (dt) {
                            def cal = Calendar.getInstance()
                            cal.setTime(dt)
                            cal.set(Calendar.YEAR, 1970)
                            cal.set(Calendar.MONTH, 0)
                            cal.set(Calendar.DAY_OF_MONTH, 1)
                            val = new java.sql.Time(cal.getTimeInMillis())
                        } else {
                            val = null
                        }
                        break

                    case 'java.sql.Timestamp':
                        val = parseDate(val, locale)
                        if (val) val = new java.sql.Timestamp(val.getTime())
                        break

                    case 'boolean':
                    case 'java.lang.Boolean':
                        if (val.equalsIgnoreCase('true') || val == '1') {
                            val = true
                        } else if (val.equalsIgnoreCase('false') || val == '0') {
                            val = false
                        } else {
                            val = null
                        }
                        break

                    default:  // Unknown data type
                        val = null
                        break
                }
            }
        }

        return val
    }

    private parseDate(val, locale) {
        try {
            return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).parse(val)
        } catch (ParseException pe1) {
            try {
                return DateFormat.getDateInstance(DateFormat.SHORT, locale).parse(val)
            } catch (ParseException pe2) {}
        }

        return null
    }

    private appendProperty(prop, props, include, exclude, codePrefix, defaultPrefix, propertyPrefix = '') {
        if (prop) {
            def type = getPropertyType(prop)
            if (type) {
                def name = propertyPrefix + getPropertyName(prop)
                if (include) {
                    if (!include.contains(name)) prop = null
                } else if (exclude) {
                    if (exclude.contains(getPropertyName(prop))) prop = null
                }

                if (prop) {
					if (genericLabels.contains(prop.name)) codePrefix = 'generic.'
                    props << [name: name, type: type, code: codePrefix + prop.name, default: defaultPrefix + prop.naturalName]
                }
            }
        }
    }

    private getPropertyType(prop) {

        // Check for enums
        if (prop.isEnum()) {

            // Return a list of the enum constants as it's 'type'
            return prop.getType().getEnumConstants()
        }

        return criteriaTypes.contains(prop.getType().name) ? prop.getType().name : null
    }

    private getPropertyName(prop) {
        def name = prop.getName()
        return name[0].toLowerCase(Locale.US) + name[1..-1]
    }

    private setCriteria(session, key, property, test, values, value) {
        def selectors = getSelectors(session)
        def selector = selectors.get(key)
        if (selector == null) {
            selector = [:]
            selectors.put(key, selector)
        } else {
            selector.remove('queryStatement')
            selector.remove('queryParameters')
        }

        def members = selector.members
        if (members == null) {
            members = [:]
            selector.put('members', members)
        }

        def properties = [property]
        def tests = [test]
        def parameters = [values]
        def member = [:]
        member.put('properties', properties)
        member.put('tests', tests)
        member.put('parameters', parameters)
        member.put('property', property)
        member.put('test', test)
        member.put('value', value)

        members.put('criteria', member)
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
}
