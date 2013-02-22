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

import grails.converters.JSON
import grails.util.Environment
import grails.util.GrailsNameUtils
import grails.util.GrailsUtil
import java.math.RoundingMode
import java.text.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import org.apache.log4j.Level
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler
import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.springframework.validation.FieldError
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.support.RequestContextUtils
import org.grails.tlc.corp.*

class UtilService {

    // Stuff we've already decided upon
    public static final BASE_CURRENCY_CODE = 'USD'
    public static final AtomicLong nextFileNumber = new AtomicLong(((long) ((long) (System.currentTimeMillis() / 1000L)) % 308915776L))
    public static final AtomicLong nextProcessNumber = new AtomicLong(System.currentTimeMillis())
    public static final EPOCH = createEpoch()   // A date representing 1970-01-01 00:00:00.000 in the server's locale
    public static final Long TRACE_NONE = 0L
    public static final Long TRACE_ALL = Long.MAX_VALUE
    public static final Integer STATE_ACTIVE = 0
    public static final Integer STATE_LOGIN_DISABLED = 1
    public static final Integer STATE_ACTIONS_DISABLED = 2
    private static final operatingState = new AtomicInteger(STATE_ACTIVE)

    // Centrally loaded things
    private static final domainClasses = [:]
    private static applicationBase

    // As a utility, we aren't transactional
    static transactional = false

    // Spring framework injections
    def grailsApplication

    // Other services we encapsulate
    def cacheService
    def taskService
    def drilldownService
    def criteriaService
    def securityService
    def menuService
    def pageHelpService
    def reportService

    // Read a single line of text from a URL (passed as a string) with optional
    // connect and read timeouts (given in seconds). Returns null if the content
    // cannot be retrieved. Read timeout will be set to the same as the connect
    // timeout unless otherwise stated. If no connect timeout is given, 10
    // seconds will be assumed.
    def readURL(url, connectTimeout = 10, readTimeout = null) {
        def data
        def reader
        try {
            def con = url.toURL().openConnection()
            connectTimeout *= 1000
            readTimeout = (readTimeout == null) ? connectTimeout : readTimeout * 1000
            con.setConnectTimeout(connectTimeout)
            con.setReadTimeout(readTimeout)
            reader = new BufferedReader(new InputStreamReader(con.getInputStream()))
            data = reader.readLine()
        } catch (Exception ex1) {
        } finally {
            if (reader != null) {
                try {
                    reader.close()
                } catch (Exception ex2) {}
            }
        }

        return data
    }

    // Get an exchange rate Google or, if not available in Google, from Yahoo.
    // Returns a BigDecimal exchange rate rounded to 6 decimal places or null
    // if no exchange rate could be retrieved.
    def readExchangeRate(code) {
        def rate = readGoogleRate(code)
        if (!rate) rate = readYahooRate(code)
        return rate
    }

    // Convert between units of the same measure
    def convertUnit(from, to, value, decs) {
        if (value == null) return value
        if (value == 0.0 || from.equivalentTo(to)) return round(value, decs)
        if (from.similarTo(to)) return round((value * from.multiplier) / to.multiplier, decs)

        def conversion = cacheService.get('conversion', from.securityCode, from.code + cacheService.IMPOSSIBLE_VALUE + to.code)

        // If we've looked before but couldn't find it
        if (conversion == cacheService.IMPOSSIBLE_VALUE) {
            throw new IllegalArgumentException(message(code: 'conversion.no.conversion', args: [from.name, to.name],
                default: "No conversion from ${from.name} to ${to.name} available"))

        }

        // If we've not looked for this one before
        if (!conversion) {
            def stmt = 'from ' + (from.securityCode ? 'Conversion' : 'SystemConversion') + ' as x where x.securityCode = :sc and ' +
                    '((x.source.measure = :fm and x.source.scale = :fs and x.target.measure = :tm and x.target.scale = :ts)' +
                    ' or ' +
                    '(x.source.measure = :tm and x.source.scale = :ts and x.target.measure = :fm and x.target.scale = :fs))'

            def conversions
            if (from.securityCode) {
                conversions = Conversion.findAll(stmt, [sc: from.securityCode, fm: from.measure, fs: from.scale, tm: to.measure, ts: to.scale])
            } else {
                conversions = SystemConversion.findAll(stmt, [sc: from.securityCode, fm: from.measure, fs: from.scale, tm: to.measure, ts: to.scale])
            }

            if (!conversions) {
                cacheService.put('conversion', from.securityCode, from.code + cacheService.IMPOSSIBLE_VALUE + to.code, null)
                throw new IllegalArgumentException(message(code: 'conversion.no.conversion', args: [from.name, to.name],
                    default: "No conversion from ${from.name} to ${to.name} available"))
            }

            def conv = bestConversion(from, conversions)
            conversion = [:]
            conversion.reversed = reversedConversion(from, conv)
            conversion.srcMultiplier = conv.source.multiplier
            conversion.preAddition = conv.preAddition
            conversion.multiplier = conv.multiplier
            conversion.postAddition = conv.postAddition
            conversion.tgtMultiplier = conv.target.multiplier

            cacheService.put('conversion', from.securityCode, from.code + cacheService.IMPOSSIBLE_VALUE + to.code, conversion)
        }

        if (conversion.reversed) {
            if (conversion.tgtMultiplier != from.multiplier) value = (value * from.multiplier) / conversion.tgtMultiplier
            value = ((value - conversion.postAddition) / conversion.multiplier) - conversion.preAddition
            if (conversion.srcMultiplier != to.multiplier) value = (value * conversion.srcMultiplier) / to.multiplier
        } else {
            if (conversion.srcMultiplier != from.multiplier) value = (value * from.multiplier) / conversion.srcMultiplier
            value = ((value + conversion.preAddition) * conversion.multiplier) + conversion.postAddition
            if (conversion.tgtMultiplier != to.multiplier) value = (value * conversion.tgtMultiplier) / to.multiplier
        }

        return round(value, decs)
    }

    // Get the exchange rate between two currencies on a given date (or today if no date given)
    def getExchangeRate(from, to, date = new Date()) {
        if (from.code == to.code) return 1.0

        // Ensure the date has no time portion
        fixDate(date)

        // Grab the exchange rates involved
        def fromRate = getRate(from, date)
        def toRate = getRate(to, date)

        // If either rate is unavailable return null
        if (!fromRate || !toRate) return null

        return round(toRate / fromRate, 6)
    }

    // Convert between currencies
    def convertCurrency(from, to, value, date = new Date()) {
        if (value == null) return value
        if (value == 0.0 || from.code == to.code) return round(value, to.decimals)

        // Get the exchange rate
        def rate = getExchangeRate(from, to, date)
        return (rate == null) ? null : round(value * rate, to.decimals)
    }

    // Returns a File object based on a virtual path on the server (e.g. /temp
    // for a directory or /images/myImage.gif for a file) or null if the path
    // cannot be resolved (e.g. it's in a war file). NOTE that there is no
    // guarantee that the file or directory actually exists on disk.
    def realFile(path) {
        path = App.servletContext.getRealPath(path)
        return path ? new File(path) : null
    }

    // Create a file in the temp directory with a unique name. The prefix should
    // be something like 'mail' and the suffix should be something like 'txt'
    def tempFile(prefix, suffix) {
        def file = realFile("/temp/${prefix}_${encodeNextFileNumber()}.${suffix}")
        while (file.exists()) {
            file = realFile("/temp/${prefix}_${encodeNextFileNumber()}.${suffix}")
        }

        return file
    }

    // Return a map of useful environment settings
    def environment() {

        // Grab the memory info
        def rt = Runtime.getRuntime()
        rt.runFinalization()
        rt.gc()
        def tm = rt.totalMemory()
        def fm = rt.freeMemory()
        def mm = rt.maxMemory()

        // Convert the memory info to MegaBytes
        tm = round(tm / 1048576.0, 0)
        fm = round(fm / 1048576.0, 0)
        def um = tm - fm
        mm = (mm == Long.MAX_VALUE) ? 0.0 : round(mm / 1048576.0, 0)

        // Fill in the details
        def map = [:]
        map.osName = System.getProperty('os.name')
        map.osVersion = System.getProperty('os.version')
        map.osArchitecture = System.getProperty('os.arch')
        map.javaName = System.getProperty('java.vm.name')
        map.javaVendor = System.getProperty('java.vm.vendor')
        map.javaVersion = System.getProperty('java.version')
        map.groovyVersion = GroovySystem.version
        map.grailsVersion = GrailsUtil.getGrailsVersion()
        map.grailsEnvironment = Environment.current
        map.applicationName = grailsApplication.metadata.'app.name'
        map.applicationVersion = grailsApplication.metadata.'app.version'
        map.memoryTotal = tm.toPlainString() + ' MB'
        map.memoryUsed = um.toPlainString() + ' MB'
        map.memoryFree = fm.toPlainString() + ' MB'
        map.memoryLimit = mm.toPlainString() + ' MB'

        return map
    }

    // Format an object in to a localized string
    def format(val, scale = null, grouped = null, locale = null) {
        if (val == null) return ''
        if (val instanceof String) return val
        if (val instanceof Date) {
            def dateFormat = DateFormat.SHORT
            def fmt, timeFormat
            if (scale == 1) {
                fmt = DateFormat.getDateInstance(dateFormat, locale ?: currentLocale())
            } else if (scale == 2) {
                timeFormat = DateFormat.SHORT
                fmt = DateFormat.getDateTimeInstance(dateFormat, timeFormat, locale ?: currentLocale())
            } else {
                timeFormat = DateFormat.MEDIUM
                fmt = DateFormat.getDateTimeInstance(dateFormat, timeFormat, locale ?: currentLocale())
            }

            // If the date is outside the 20/80 rule, we need to show the full year
            if (val.time < System.currentTimeMillis() - 2520000000000L || val.time > System.currentTimeMillis() + 631000000000L) {
                if (fmt instanceof SimpleDateFormat) {
                    def pat = fmt.toPattern()
                    if (pat.indexOf('yyyy') == -1) {
                        if (pat.indexOf('yyy') != -1) {
                            fmt.applyPattern(pat.replace('yyy', 'yyyy'))
                        } else if (pat.indexOf('yy') != -1) {
                            fmt.applyPattern(pat.replace('yy', 'yyyy'))
                        } else if (pat.indexOf('y') != -1) {
                            fmt.applyPattern(pat.replace('y', 'yyyy'))
                        } else {
                            dateFormat = DateFormat.MEDIUM
                            fmt = null
                        }
                    }
                } else {
                    dateFormat = DateFormat.MEDIUM
                    fmt = null
                }

                if (!fmt) {
                    if (timeFormat == null) {
                        fmt = DateFormat.getDateInstance(dateFormat, locale ?: currentLocale())
                    } else {
                        fmt = DateFormat.getDateTimeInstance(dateFormat, timeFormat, locale ?: currentLocale())
                    }
                }
            }

            return fmt.format(val)
        }

        if (val instanceof Number) {
            def fmt = NumberFormat.getInstance(locale ?: currentLocale())
            fmt.setMinimumIntegerDigits(1)
            if (val instanceof BigDecimal || val instanceof Double || val instanceof Float) {
                if (grouped == null) grouped = true
                if (scale == null || scale > 10) {
                    if (val instanceof BigDecimal) {
                        scale = (val.scale() >= 0) ? Math.min(val.scale(), 10) : 0
                    } else {
                        scale = (val == Math.round(val)) ? 0 : 10
                    }
                } else if (scale < 0) {
                    scale = 0
                }

                fmt.setMinimumFractionDigits(scale)
                fmt.setMaximumFractionDigits(scale)
                try {
                    fmt.setRoundingMode(RoundingMode.HALF_UP)
                } catch (UnsupportedOperationException uoe) {}
            } else {
                if (grouped == null) grouped = false
            }

            fmt.setGroupingUsed(grouped)
            return fmt.format(val)
        }

        if (val instanceof Class) return val.name
        if (val instanceof List || val instanceof Object[]) return val.collect {format(it, scale, grouped, locale)}
        if (val instanceof Map) {
            def map = [:]
            for (it in val) map.put(format(it.key, scale, grouped, locale), format(it.value, scale, grouped, locale))
            return map
        }

        return val.toString()
    }

    // Returns the value for the given setting code for the system, or null if no setting exists
    // for the given code (unless a default is supplied, in which case the default value is returned)
    def systemSetting(code, dflt = null) {
        def val = cacheService.get('setting', 0L, code)
        if (val == null) {
            def rec = SystemSetting.findByCode(code)
            if (rec) val = valueOf(rec.dataType, rec.dataScale, rec.value)
            cacheService.put('setting', 0L, code, val, standardDataLength(val))
        }

        if (val == CacheService.IMPOSSIBLE_VALUE) val = null
        if (val == null) val = dflt

        return val
    }

    // Returns the value for the given setting code for the current company or null if no setting exists for the
    // given code (unless a default is supplied, in which case the default value is returned)
    def setting(code, dflt = null, company = currentCompany()) {
        def val = cacheService.get('setting', company.securityCode, code)
        if (val == null) {
            def rec = Setting.findByCompanyAndCode(company, code)
            if (rec) val = valueOf(rec.dataType, rec.dataScale, rec.value)
            cacheService.put('setting', company.securityCode, code, val, standardDataLength(val))
        }

        if (val == CacheService.IMPOSSIBLE_VALUE) val = null
        if (val == null) val = dflt

        return val
    }

    // Returns the first error message attached to a bean or null if there is no error attached. This method
    // simply picks the first error in any list of errors. This may or may not be the most important error!
    def getFirstErrorMessage(bean) {
        if (bean) {
            def errors = bean.errors?.allErrors
            if (errors) return errorMessage(errors[0])
        }

        return null
    }

    // Returns a list containing all the error messages attached to the bean. The list will
    // be empty if the bean has no errors.
    def getAllErrorMessages(bean) {
        if (bean) {
            def list = []
            def errors = bean.errors?.allErrors
            if (errors) {
                for (error in errors) list << errorMessage(error)
                return list
            }
        }

        return null
    }

    // Returns a message String corresponding to the Spring error supplied.
    // NOTE that the returned message is 'raw' i.e. is not HTML encoded etc.
    // This method started life many moons ago (around about Grails version 1.2)
    // to correct some 'oddities' with the standard Grails error message
    // resolution. It then evolved to also minimize the number of attempts to
    // find a suitable message by using knowledge of how TLC is written (e.g.
    // we never use package names in our error message keys as is allowed by
    // Grails 2.2) thus minimizing useless database accesses and avoiding
    // cluttering up the log with uninteresting messages about our attempts
    // to find an appropriate message to use. Consequently, it only allows for
    // the use of a single type of custom message to override Grails' default
    // messages in the i18n/messages.properties file (plus normal custom validator
    // error messages, of course).
    def errorMessage(error) {
        def codes = []
        def arguments

        // Check if a field level error
        if (error instanceof FieldError) {

            // Check if a data binding error
            if (error.isBindingFailure()) {

                // Allow for custom binding error code. Note that the class name starts with an upper
                // case letter, as in: typeMismatch.Author.name
                codes << "typeMismatch.${GrailsNameUtils.getShortName(error.objectName)}.${error.field}"

                // Pick out the default binding error code
                for (code in error.codes) {
                    if (code.startsWith('typeMismatch.java.')) {
                        codes << code
                        break
                    }
                }

                // Create some sensible arguments
                arguments = [message(code: "${error.objectName}.${error.field}.label",
                    default: GrailsNameUtils.getNaturalName(error.field)),
                    getDomainLabel(error.objectName), error.rejectedValue]
            } else {    // A validation error

                // The last code in the list of codes is the generic id
                // so include a possible custom code plus the default code
                switch (error.code) {
                    case 'not.inList':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.not.inList"
                        codes << 'default.not.inlist.message'
                        break

                    case 'max.exceeded':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.max.exceeded"
                        codes << 'default.invalid.max.message'
                        break

                    case 'min.notmet':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.min.notmet"
                        codes << 'default.invalid.min.message'
                        break

                    case 'range.toosmall':
                    case 'range.toobig':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.${error.code}"
                        codes << 'default.invalid.range.message'
                        break

                    case 'notEqual':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.notEqual"
                        codes << 'default.not.equal.message'
                        break

                    case 'blank':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.blank"
                        codes << 'default.blank.message'
                        break

                    case 'creditCard.invalid':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.creditCard.invalid"
                        codes << 'default.invalid.creditCard.message'
                        break

                    case 'email.invalid':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.email.invalid"
                        codes << 'default.invalid.email.message'
                        break

                    case 'matches.invalid':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.matches.invalid"
                        codes << 'default.doesnt.match.message'
                        break

                    case 'size.toosmall':
                    case 'size.toobig':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.${error.code}"
                        codes << 'default.invalid.size.message'
                        break

                    case 'url.invalid':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.url.invalid"
                        codes << 'default.invalid.url.message'
                        break

                    case 'unique':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.unique"
                        codes << 'default.not.unique.message'
                        break

                    case 'validator.invalid':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.validator.error"
                        codes << 'default.invalid.validator.message'
                        break

                    case 'minSize.notmet':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.minSize.notmet"
                        codes << 'default.invalid.min.size.message'
                        break

                    case 'maxSize.exceeded':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.maxSize.exceeded"
                        codes << 'default.invalid.max.size.message'
                        break

                    case 'nullable':
                        codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.nullable"
                        codes << 'default.null.message'
                        break

                    default:    // A custom message of some sort
                        if (error.code && error.code != 'null') {
                            if (!error.code.startsWith('null.')) codes << error.code
                            codes << "${GrailsNameUtils.getPropertyName(error.objectName)}.${error.field}.${error.code}"
                        }

                        break
                }

                // Grab the arguments
                arguments = error.arguments as List
            }
        } else {    // A bean level error

            // Just use the code they specified (as long as it's sensible)
            if (error.code && error.code != 'null' && !error.code.startsWith('null.')) codes << error.code

            // Grab the arguments
            arguments = error.arguments as List
        }

        // Format all arguments into strings
        if (arguments) arguments = arguments.collect {format(it)}          // Format all arguments into strings

        // Assume no appropriate message can be found
        def msg = '_'

        // Work through the possible error codes
        for (int i = 0; i < codes.size(); i++) {

            // For all except the last one, prepend the special value cacheService.DUMMY_VALUE
            // to the front of the code to tell the message source that we only want to log
            // this if at debug level (or higher, of course). Normally, the message source logs
            // missing messages at info level or higher. This saves cluttering up the log file
            // with our attempts to find an available message.
            if (i < codes.size() - 1) codes[i] = CacheService.DUMMY_VALUE + codes[i]
            msg = message(code: codes[i], args: arguments, default: '_')
            if (msg != '_') break
        }

        // Make sure we have something sensible to output
        if (msg == '_') {
            msg = error.defaultMessage ?: 'Property [{0}] of class [{1}] with value [{2}] is invalid'

            if (arguments) msg = new MessageFormat(msg).format(arguments as Object[])
        }

        return msg
    }

    // See the three parameter version below
    def collate(values) {
        collate(values, null, null)
    }

    // See the three parameter version below
    def collate(values, obj) {
        (obj instanceof Closure) ? collate(values, null, obj) : collate(values, obj, null)
    }

    // Sort a list (or array), IN SITU, in to ascending order using a Collator. If no locale
    // parameter is supplied, the locale of the current request will be used. If no closure
    // parameter is supplied the sort will be based on the toString() value of each object
    // in the values list. If a closure is supplied, then any value it returns will be forced
    // in to a String by using the returned value's toString() method. This group of 'collate'
    // methods can be called with something like: utilService.collate(domainList) {it.name}
    //
    // The intent of this method is to provide better i18n sorting than the standard Groovy
    // sort facility without involving the programmer in too much additional hassle. NOTE that
    // Collator based sorting is usually case insensitive and usually ignores accents, but
    // that this is actually locale dependent in both cases.
    def collate(values, locale, closure) {
        if (values?.size() > 1) {
            def collator = Collator.getInstance(locale ?: currentLocale())
            def list = []
            for (item in values) list << new Collatable(collator, item, closure)
            Collections.sort(list)
            for (int i = 0; i < list.size(); i++) values[i] = list[i].object
        }
    }

    // --------------------------------------------- Static Methods ----------------------------------------------

    // Returns a unique identifier (as a Long)
    static getNextProcessId() {
        return nextProcessNumber.getAndIncrement()
    }

    // Round a BigDecimal to a given number of decimal places.
    static round(value, decs) {
        if (value == null) return value

        return value.setScale(decs, RoundingMode.HALF_UP)
    }

    // Clear any time portion of a Date
    static fixDate(date = new Date()) {
        def cal = Calendar.getInstance()
        cal.setTime(date)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        return cal.getTime()
    }

    static endOfMonth(date) {
        def cal = Calendar.getInstance()
        cal.setTime(date)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MONTH, 1)
        cal.add(Calendar.DAY_OF_MONTH, -1)
        return cal.getTime()
    }

    // The date range that seems reasonable (approx 100 years before or after now)
    static validDateRange() {
        return minDate()..maxDate()
    }

    // The minimum date that seems reasonable (approx 100 years ago)
    static minDate() {
        return fixDate(new Date(System.currentTimeMillis() - 3150000000000L))
    }

    // The maximum date that seems reasonable (approx 100 years from now)
    static maxDate() {
        return fixDate(new Date(System.currentTimeMillis() + 3150000000000L))
    }

    // Create a value object from its string representation for the standard
    // data types. Returns null if no valid value can be created
    static valueOf(type, scale, val) {
        if (val == null || type == null) return null

        switch (type) {
            case 'string':
                // Nothing to do
                break

            case 'integer':
                try {
                    val = new Integer(val)
                } catch (NumberFormatException ne) {
                    val = null
                }
                break

            case 'decimal':
                try {
                    val = new BigDecimal(val)
                    if (scale != null) {
                        try {
                            val = val.setScale(scale)
                        } catch (ArithmeticException ae) {
                            val = null
                        }
                    }
                } catch (NumberFormatException ne) {
                    val = null
                }
                break

            case 'date':
                try {
                    def fmt = new SimpleDateFormat((scale == 1 || val.length() == 10) ? "yyyy-MM-dd" : "yyyy-MM-dd HH:mm", Locale.US)
                    val = fmt.parse(val)
                } catch (ParseException pe) {
                    val = null
                }
                break

            case 'boolean':
                if (val.equalsIgnoreCase('true')) {
                    val = true
                } else if (val.equalsIgnoreCase('false')) {
                    val = false
                } else {
                    val = null
                }
                break

            default:
                val = null
                break
        }

        return val
    }

    // Turn a value object in to its internal String format for one of the
    // standard data types. Returns null if conversion could not be made
    static stringOf(type, scale, val) {
        if (val == null || type == null) return null

        switch (type) {
            case 'string':
                if (!(val instanceof String)) val = val.toString()
                break

            case 'integer':
                if (val instanceof Number) {
                    val = Integer.toString(val.intValue())
                } else {
                    val = null
                }
                break

            case 'decimal':
                if (val instanceof Number) {
                    if (!(val instanceof BigDecimal)) {
                        val = new BigDecimal(val.toString())
                    }

                    if (scale != null) val = val.setScale(scale, RoundingMode.HALF_UP)
                    val = val.toPlainString()
                } else {
                    val = null
                }
                break

            case 'date':
                if (val instanceof Calendar) val = val.getTime()
                if (val instanceof Date) {
                    val = new SimpleDateFormat((scale == 1) ? "yyyy-MM-dd" : "yyyy-MM-dd HH:mm", Locale.US).format(val)
                } else {
                    val = null
                }
                break

            case 'boolean':
                if (val instanceof Boolean) {
                    val = val.toString()
                } else {
                    val = null
                }
                break

            default:
                val = null
                break
        }

        return val
    }

    // Return a GrailDomainClass by name (such as Book or Author). Note that duplicate domain names are not supported
    // even when in different packages
    static GrailsDomainClass getGrailsDomainClass(domain) {
        synchronized (domainClasses) {
            if (domainClasses.size() == 0) {
                for (it in App.grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE)) domainClasses.put(it.name, it)
            }
        }

        return (GrailsDomainClass) domainClasses.get(domain)
    }

    // Returns the current system operating state as a string
    static getCurrentOperatingState() {
        def state = operatingState.get()
        if (state == STATE_LOGIN_DISABLED) {
            return 'loginDisabled'
        }

        if (state == STATE_ACTIONS_DISABLED) {
            return 'actionsDisabled'
        }

        return 'active'
    }

    // Sets the current system operating state from a string
    static setCurrentOperatingState(state) {
        if (state == 'loginDisabled') {
            operatingState.set(STATE_LOGIN_DISABLED)
        } else if (state == 'actionsDisabled') {
            operatingState.set(STATE_ACTIONS_DISABLED)
        } else {
            operatingState.set(STATE_ACTIVE)
        }
    }

    // Used for development purposes
    static logSQL(status, message = null) {
        Logger.getLogger('org.hibernate.SQL').setLevel(status ? Level.DEBUG : Level.OFF)
        println "${status ? 'Start' : 'Stop'} SQL logging${message ? ': ' + message : ''}"
    }

    // Create a trace if required
    static trace(action, obj, data = null) {
        def traceSecurityCode
        def valid = true
        switch (action) {
            case 'insert':
                traceSecurityCode = obj.traceInsertCode.get()
                break

            case 'update':
                traceSecurityCode = obj.traceUpdateCode.get()
                break

            case 'delete':
                traceSecurityCode = obj.traceDeleteCode.get()
                break

            default:
                throw new IllegalArgumentException("Trace type invalid: ${action}")
        }

        if (traceSecurityCode == TRACE_ALL || (obj.securityCode && traceSecurityCode == obj.securityCode)) {
            def usr = 0L
            try {
                usr = RequestContextHolder.currentRequestAttributes().getSession()?.userdata?.userId ?: 0L
            } catch (Exception ex) {}

            if (!data) data = obj.toString()
            if (data.length() > 100) data = data.substring(0, 97) + '...'

            SystemTrace.withNewSession {

                // No deep validaton required but cannot have a flush during an event
                valid = new SystemTrace(databaseAction: action, domainName: obj.class.simpleName, domainId: obj.id,
                        domainSecurityCode: obj.securityCode, domainVersion: obj.version, domainData: data, userId: usr).save()
            }
        }

        return valid
    }

    // ---------------------------------------- Interactive Only Methods -----------------------------------------

    // Return the current user or null if no currently logged in user
    def currentUser() {
        def user = getRequest().userrec
        if (!user && getSession().userdata?.userId) {
            user = SystemUser.get(getSession().userdata.userId)
            getRequest().userrec = user
        }

        return user
    }

    // Return the current company or null if no currently attached company
    def currentCompany() {
        def company = getRequest().companyrec
        if (!company && getSession().userdata?.companyId) {
            company = Company.get(getSession().userdata.companyId)
            getRequest().companyrec = company
        }

        return company
    }

    // Return the currency for the current company or null if no current company
    def companyCurrency() {
        def currency = getRequest().currencyrec
        if (!currency) {
            def company = currentCompany()
            if (company) currency = ExchangeCurrency.findByCompanyAndCompanyCurrency(company, true, [cache: true])
        }

        return currency
    }

    // Return the tax code for the current company or null if no current company
    def companyTaxCode() {
        return TaxCode.findByCompanyAndCompanyTaxCode(currentCompany(), true, [cache: true])
    }

    // Return the current locale for this request
    def currentLocale() {
        return RequestContextUtils.getLocale(getRequest())
    }

    // Return the name (e.g. myLogo.png) of the current logo to diplay on pages
    def currentLogo() {
        def session = getSession()
        def logo = 'default.png'
        if (session.userdata) {
            logo = session.userdata.logoName
            if (!logo) {
                def co = currentCompany()
                if (co && realFile("/images/logos/L${co.securityCode}.png").exists()) {
                    logo = "L${co.securityCode}.png"
                } else {
                    logo = 'default.png'
                }

                session.userdata.logoName = logo
            }
        }

        return logo
    }

    // Returns the 2 upper case character code of the flag that represents the user's
    // country (per their current locale). Defaults to returning UN if all else fails
    def currentFlag() {
        def session = getSession()
        def flag = session.userdata?.flag
        if (!flag) {
            if (session.userdata) {
                def locale = currentLocale()
                if (locale) {
                    def country = SystemCountry.findByCode(locale.country)
                    if (country) {
                        flag = country.flag
                        session.userdata.flag = flag
                    }
                }
            }
        }

        return flag ?: 'UN'
    }

    // Clear the logo setting for the current user
    def clearCurrentLogo() {
        def session = getSession()
        if (session.userdata?.logoName) session.userdata.logoName = null
    }

    // Remove the currently logged in user info
    def clearCurrentUser() {
        def session = getSession()
        session.userdata = null
        session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' = null

        def request = getRequest()
        request.userrec = null
        request.companyrec = null
        request.currencyrec = null
        request.menurec = null
    }

    // Record a newly logged in user (the user parameter may be null)
    def newCurrentUser(user) {
        def session = getSession()
        session.userdata = [userId: user?.id]
        session.logindata = null
        if (user) session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' = new Locale(user.language.code, user.country.code)

        def request = getRequest()
        request.userrec = null
        request.companyrec = null
        request.currencyrec = null
        request.menurec = null
    }

    // Record a newly attached company
    def newCurrentCompany(id) {
        def session = getSession()
        session.userdata.companyId = id
        session.userdata.logoName = null
        session.userdata.menuId = null

        def request = getRequest()
        request.companyrec = null
        request.currencyrec = null
        request.menurec = null

        // Need to record the last time this user used this company
        def companyUserInstance = CompanyUser.findByCompanyAndUser(currentCompany(), currentUser(), [cache: true])
        if (companyUserInstance) {
            companyUserInstance.lastUsed = new Date()
            companyUserInstance.saveThis()
        }
    }

    def createDummySessionData(company, currency, user, locale) {
        def session = getSession()
        session.userdata = [companyId: company.id, userId: user.id, logoName: null, menuId: null]
        session.logindata = null
        session.filterdata = null
        session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' = locale

        def request = getRequest()
        request.userrec = user
        request.companyrec = company
        request.currencyrec = currency
        request.menurec = null
    }

    def setSessionLocale(locale) {
        def session = getSession()
        session.'org.springframework.web.servlet.i18n.SessionLocaleResolver.LOCALE' = locale
        if (session.userdata?.flag) session.userdata.flag = null
    }

    // Determine whether too many login password failures have occurred
    def excessiveLoginAttempts(spanMinutes, maxAttempts, userId) {
        def id = userId.toString()
        def session = getSession()
        def map = session.logindata
        if (!map) {
            map = [:]
            session.logindata = map
        }

        def date = new Date()
        def attempts = map.get(id)
        if (!attempts) {
            attempts = []
            map.put(id, attempts)
        }

        // Note the latest failed attempt
        attempts << date

        // Work out the time span we're checking within
        date = new Date(date.getTime() - (spanMinutes * 60000L))

        // Remove any attempts before the time span
        while (attempts[0] < date) {
            attempts.remove(0)
        }

        return (attempts.size() >= maxAttempts)
    }

    // Set the next step in a mandatory chain of web pages. If the map
    // parameter is null, this effectively terminates the chain of pages.
    // The map should contain source and target controller and action values.
    def setNextStep(map) {
        def data = getSession().userdata
        if (data) data.nextStep = map
    }

    // Return the next step in a mandatory chain of web pages or null if no next step
    def getNextStep() {
        return getSession().userdata?.nextStep
    }

    // Encapsulate the 'source' method of the drilldown service
    def source(test, options = null) {
        return drilldownService.source(currentUser().administrator, currentCompany(), getSession(), getParams(), test, options)
    }

    // Use the 'source' method of the drilldown service to return the parent
    // object to be used when creating new child objects. This method assumes
    // a 'default' environment (i.e. action = list) unless 'options' contains
    // an origin parameter in which case that will be taken as the original action
    def reSource(test, options = null) {
        return drilldownService.source(currentUser().administrator, currentCompany(), getSession(), [controller: getParams().controller, action: (options?.origin ?: 'list')], test, options)
    }

    // Returns a map of filter values for the named filter, creating it if
    // necessary. The actual map is returned rather than a clone of it.
    def getFilterValues(filterName) {
        def session = getSession()
        def filterMaps = session.filterdata
        if (filterMaps == null) {
            filterMaps = [:]
            session.filterdata = filterMaps
        }

        def map = filterMaps.get(filterName)
        if (map == null) {
            map = [:]
            filterMaps.put(filterName, map)
        }

        return map
    }

    // Clears all filters from the session
    def resetFilters() {
        getSession().filterdata = null
    }

    // Returns true if the current user and company combination is allowed to
    // perform the given activity code
    def permitted(activityCode) {
        if (!currentUser() || !currentCompany() || !activityCode) return false

        return securityService.isUserActivity(cacheService, currentCompany(), currentUser(), activityCode)
    }

    // Perform a save of a domain object and deal with the setting of translations
    // in the SystemMessage or Message tables as appropriate. The translatables
    // parameter is a list of maps (or a single map) of fields within the domainObject
    // that are subject to localization (i.e. message texts) where each element in the
    // list is a map containing the following:
    //
    // prefix:      The message key prefix that the specific code will be added to (in
    //              the form of a dot separated path) to form the full message key.
    // code:        The specific code value to append to the prefix to get the full
    //              message key (a dot being used to separate the prefix and code).
    // oldCode:     If the code value has changed then this is the old value of the
    //              code. Otherwise just omit this parameter (or set it the same as
    //              the code parameter). Messages will have their keys updated accordingly.
    // propagate:   If oldCode exists and is different from code, and if this field is
    //              a String or an array of Strings, then each of the Strings will be
    //              treated as the prefix of a dot separated path and all messages
    //              starting with that prefix (followed by the oldCode) will have their
    //              key changed to use the new code value. This is used for things such
    //              as propagating a code change of a Task record to its Param and Result
    //              child records.
    // field:       The name of the property that may be translated (see also text: below)
    // text:        If an absolute piece of text is to be used rather than a field value,
    //              supply the text here. If both field and text parameters are supplied,
    //              the field parameter takes precedence.
    //
    // The useExistingTransaction is simply checked for being Groovy true or false
    // and is not used in any other way. It is used to begin a new transaction if
    // one is not already in progress. It *DOES NOT* roll a pre-existing transaction
    // back if there is an error or any save fails. This is left to the caller.
    // Note that any transaction we start will also be finished within this method
    // and is not available to the caller for further use. ALSO NOTE that the validation
    // and saving of records is done WITHOUT DEEP VALIDATION.
    def saveWithMessages(domainObject, translatables, useExistingTransaction = null) {

        // Don't attempt anything if the domainObject has errors. Note that
        // calling the validate method sets such things as the securityCode
        // which we need to determine whether this record is a system record
        // (securityCode == 0) or a company record (securityCode > 0)
        if (domainObject.hasErrors() || !domainObject.validateThis()) return false

        // Make sure the translatables are a list
        if (translatables instanceof Map) translatables = [translatables]

        // Determine whether we need to begin a transaction or not
        if (useExistingTransaction) {

            // Decide whether this is a system record or a company record
            if (domainObject.securityCode) {

                // Get the company involved
                def company = getCompanyFromSecurityCode(domainObject.securityCode)

                // Implement any changes to message keys
                if (!syncCompanyKeys(company, translatables)) return false

                // Handle the translations
                return localCompanySave(company, domainObject, translatables, currentLocale())
            } else {

                // Implement any changes to message keys
                if (!syncSystemKeys(translatables)) return false

                // Handle the translations
                return localSystemSave(domainObject, translatables, currentLocale())
            }
        } else {

            def valid = true

            // Perform the updates within a transaction
            domainObject.class.withTransaction {status ->

                // Decide whether this is a system record or a company record
                if (domainObject.securityCode) {

                    // Get the company involved
                    def company = getCompanyFromSecurityCode(domainObject.securityCode)

                    // Implement any changes to message keys
                    if (!syncCompanyKeys(company, translatables)) {
                        status.setRollbackOnly()
                        valid = false
                    }

                    // Handle the translations
                    if (valid && !localCompanySave(company, domainObject, translatables, currentLocale())) {
                        status.setRollbackOnly()
                        valid = false
                    }
                } else {

                    // Implement any changes to message keys
                    if (!syncSystemKeys(translatables)) {
                        status.setRollbackOnly()
                        valid = false
                    }

                    // Handle the translations
                    if (valid && !localSystemSave(domainObject, translatables, currentLocale())) {
                        status.setRollbackOnly()
                        return false
                    }
                }
            }

            return valid
        }
    }

    // Returns a company record based on its security code
    def getCompanyFromSecurityCode(securityCode) {
        def company = currentCompany()
        if (company && company.securityCode == securityCode) return company

        return Company.findBySecurityCode(securityCode)
    }

    // Returns a standard message from a list of known standard messages
    // within Grails (initially defined in the messages.properties file.)
    // The returned message is 'raw' in the sense that it is not HTML encoded.
    // Throws an exception if the given code is not in the known list of codes.
    def standardMessage(code, domain, value = null, forDomain = null) {
        def text
        def label = toDomainLabel(domain)
        switch (code) {
            case 'created':
            case 'updated':
            case 'deleted':
                value = toDomainValue(domain, value)
                text = message(code: "default.${code}.message", args: [label, value], default: "${label} ${value} ${code}")
                break

            case 'not.deleted':
                value = toDomainValue(domain, value)
                text = message(code: 'default.not.deleted.message', args: [label, value], default: "${label} ${value} could not be deleted")
                break

            case 'not.found':
                value = toDomainValue(domain, value)
                text = message(code: 'default.not.found.message', args: [label, value], default: "${label} not found with id ${value}")
                break

            case 'locking.failure':
                text = message(code: 'default.optimistic.locking.failure', args: [label],
                    default: "Another user has updated this ${label} while you were editing")
                break

            case 'list':
                text = message(code: 'default.list.label', args: [label], default: "${label} List")
                break

            case 'list.for':
                text = message(code: 'default.list.label', args: [label], default: "${label} List")
                text += ": ${toDomainLabel(forDomain)} ${toDomainValue(forDomain, value)}"
                break

            case 'add':
            case 'new':
            case 'create':
            case 'show':
            case 'edit':
                text = message(code: "default.${code}.label", args: [label], default: "${GrailsNameUtils.getNaturalName(code)} ${label}")
                break

            default:
                throw new IllegalArgumentException("Invalid standard message code: ${code}")
        }

        return text
    }

    // Perform a deletion of a domain object and deal with the removal of translations
    // in the SystemMessage or Message tables as appropriate. The translatables
    // parameter is a list of maps (or a single map) of codes that the domainObject
    // uses, where each element in the list is a map containing the following:
    //
    // prefix:      The message key prefix that the specific code will be added to (in
    //              the form of a dot separated path) to form the full message key
    // code:        The specific code value to append to the prefix to get the full
    //              message key (a dot being used to separate the prefix and code)
    // propagate:   If this field is a String or an array of Strings, then each of the
    //              Strings will be treated as the prefix of a dot separated path and
    //              all messages starting with that prefix (followed by the code) will
    //              have their messages deleted.
    //
    // The useExistingTransaction is simply checked for being Groovy true or false
    // and is not used in any other way. It is used to begin a new transaction if
    // one is not already in progress. It *DOES NOT* roll a pre-existing transaction
    // back if there is an error or any save fails. This is left to the caller.
    // Note that any transaction we start will also be finished within this method
    // and is not available to the caller for further use.
    def deleteWithMessages(domainObject, translatables, useExistingTransaction = null) {

        // Make sure the translatables are a list
        if (translatables instanceof Map) translatables = [translatables]

        // Determine whether we need to begin a transaction or not
        if (useExistingTransaction) {

            // Perform the main deletion
            domainObject.delete(flush: true)

            // Decide whether this is a system record or a company record
            if (domainObject.securityCode) {

                // Get the company involved
                def company = getCompanyFromSecurityCode(domainObject.securityCode)

                // Handle the translations
                localCompanyDelete(company, domainObject, translatables)
            } else {

                // Handle the translations
                localSystemDelete(domainObject, translatables)
            }
        } else {

            // Perform the updates within a transaction
            domainObject.class.withTransaction {status ->

                // Perform the main deletion
                domainObject.delete(flush: true)

                // Decide whether this is a system record or a company record
                if (domainObject.securityCode) {

                    // Get the company involved
                    def company = getCompanyFromSecurityCode(domainObject.securityCode)

                    // Handle the translations
                    localCompanyDelete(company, domainObject, translatables)
                } else {

                    // Handle the translations
                    localSystemDelete(domainObject, translatables)
                }
            }
        }
    }

    // Return a list, sorted by name, of all the users in the current company. Note that the returned list is
    // of SystemUser objects, not CompanyUser objects
    def currentCompanyUserList() {
        SystemUser.findAll('from SystemUser as x where x.id in (select y.user.id from CompanyUser as y where y.company = ?) order by x.name', [currentCompany()])
    }

    // Ensure references in a 'current company' owned domain object are to other object(s) in the same company
    def verify(obj, refs) {

        // Make sure the references are a list
        if (refs instanceof String) refs = [refs]

        // Get the current company's security code
        def securityCode = currentCompany()?.securityCode

        // Work through the references
        def val
        for (ref in refs) {
            val = obj."${ref}"

            // If they don't have the same security code, clear the reference
            if (val && val.securityCode != securityCode) obj."${ref}" = null
        }
    }

    // Returns whether a given page title code has page help associated with it (actually return the locale to use or null)
    def hasPageHelp(code) {
        return pageHelpService.hasPageHelp(code, currentLocale(), cacheService)
    }

    // Returns the page help text for a given page title code
    def getPageHelp(code) {
        return pageHelpService.getPageHelp(code, currentLocale(), cacheService, applicationURI())
    }

    // Returns the base URI of the application (e.g. /tlc)
    def applicationURI() {
        if (!applicationBase) {
            synchronized (domainClasses) {
                if (!applicationBase) {
                    def webRequest = RequestContextHolder.currentRequestAttributes()
                    applicationBase = webRequest.attributes.getApplicationUri(webRequest.currentRequest)
                }
            }
        }

        return applicationBase
    }

    // Run a given task code using the supplied parameters an optional start date/time. Returns the
    // queued task id or throws an exception if the task could not be submitted. The parameters are
    // a list of maps where each map contains... [param: TaskParamInstance, value: StringValueOrNull]
    def demandRun(taskCode, parameters, preferredStart = null) {
        def companyUser = CompanyUser.findByCompanyAndUser(currentCompany(), currentUser(), [cache: true])
        if (!companyUser) throw new IllegalArgumentException(message(code: 'queuedTask.demand.user', default: 'Unknown company user'))
        def task = Task.findByCompanyAndCode(currentCompany(), taskCode)
        if (!task) throw new IllegalArgumentException(message(code: 'queuedTask.demand.task', default: 'Unknown task requested'))
        if (!task.allowOnDemand) throw new IllegalArgumentException()
        if (!permitted(task.activity.code)) throw new IllegalArgumentException(message(code: 'queuedTask.demand.activity', default: 'You do not have permission to execute this task'))
        if (preferredStart == null) {
            preferredStart = new Date()
        } else if (preferredStart.getTime() < System.currentTimeMillis() - 60000L || preferredStart > new Date() + 365) {
            throw new IllegalArgumentException(message(code: 'queuedTask.demand.date', default: "The 'Delay Until' date and time is invalid"))
        }

        def queueNumber = taskService.submit(task, parameters, currentUser(), preferredStart)
        if (!queueNumber) throw new IllegalArgumentException(message(code: 'queuedTask.demand.bad', default: 'Unable to queue the task for execution'))
        return queueNumber
    }

    // Runs a task with the given code getting all its information (except the task code itself)
    // from the request params. If it returns a String, this will be an error message. If it does
    // not return a String then it returns the queued task id (which is a Long)
    def demandRunFromParams(taskCode, params) {
        def task = Task.findByCompanyAndCode(currentCompany(), taskCode)
        if (!task) return message(code: 'queuedTask.demand.task', default: 'Unknown task requested')
        def preferredStart = null
        if (params.preferredStart) {
            try {
                preferredStart = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, currentLocale()).parse(params.preferredStart)
            } catch (ParseException pe1) {
                try {
                    preferredStart = DateFormat.getDateInstance(DateFormat.SHORT, currentLocale()).parse(params.preferredStart)
                } catch (ParseException pe2) {
                    return message(code: 'queuedTask.demand.date', default: "The 'Delay Until' date and time is invalid")
                }
            }
        }

        def parameters = []
        def tpList = TaskParam.findAllByTask(task)
        for (it in tpList) {
            def val = params."p_${it.code}"
            if (val || it.dataType == 'boolean') {
                def obj = null
                switch (it.dataType) {
                    case 'string':
                        obj = val
                        break

                    case 'integer':
                        try {
                            obj = stringOf(it.dataType, it.dataScale, NumberFormat.getIntegerInstance(currentLocale()).parse(val))
                        } catch (ParseException pe1) {}
                        break

                    case 'decimal':
                        try {
                            obj = stringOf(it.dataType, it.dataScale, NumberFormat.getInstance(currentLocale()).parse(val))
                        } catch (ParseException pe2) {}
                        break

                    case 'boolean':
                        obj = val == 'true' ? 'true' : 'false'
                        break

                    case 'date':
                        def fmt = it.dataScale == 1 ? DateFormat.getDateInstance(DateFormat.SHORT, currentLocale()) : DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, currentLocale())
                        try {
                            obj = stringOf(it.dataType, it.dataScale, fmt.parse(val))
                        } catch (ParseException pe3) {}
                        break
                }

                if (obj) {
                    parameters << [param: it, value: obj]
                } else {
                    return message(code: 'queuedTask.demand.bad.param', args: [val], default: "The parameter value ${val} is invalid")
                }
            } else if (it.defaultValue) {
                parameters << [param: it, value: it.defaultValue]
            } else if (it.required) {
                val = message(code: "taskParam.name.${taskCode}.${it.code}", default: it.name)
                return message(code: 'queuedTask.demand.no.param', args: [val], default: "Parameter ${val} is required")
            }
        }

        try {
            return demandRun(taskCode, parameters, preferredStart)
        } catch (IllegalArgumentException ex) {
            return ex.message
        }
    }

    // Creates an array of parameters for a task, with the first parameters being the preferred start date/time
    def createTaskParameters(taskCode, params) {
        def companyUser = CompanyUser.findByCompanyAndUser(currentCompany(), currentUser(), [cache: true])
        if (!companyUser) throw new IllegalArgumentException(message(code: 'queuedTask.demand.user', default: 'Unknown company user'))
        def task = Task.findByCompanyAndCode(currentCompany(), taskCode)
        if (!task) throw new IllegalArgumentException(message(code: 'queuedTask.demand.task', default: 'Unknown task requested'))
        if (!task.allowOnDemand) throw new IllegalArgumentException()
        if (!permitted(task.activity.code)) throw new IllegalArgumentException(message(code: 'queuedTask.demand.activity', default: 'You do not have permission to execute this task'))
        def parameters = []
        parameters << [code: 'preferredStart', prompt: message(code: 'queuedTask.demand.delay.label', default: 'Delay Until'),
                    type: 'date', value: params.preferredStart ?: '', help: 'queuedTask.demand.delay']
        for (it in TaskParam.findAllByTask(task, [sort: 'sequencer'])) {
            def name = message(code: "taskParam.name.${taskCode}.${it.code}", default: it.name)
            def val = params."p_${it.code}"
            if (val) {
                parameters.put("p_${it.code}", prompt: name, type: it.dataType, val)
            } else if (it.defaultValue) {
                switch (it.dataType) {
                    case 'integer':
                        def fmt = NumberFormat.getIntegerInstance(currentLocale())
                        fmt.setGroupingUsed(false)
                        val = fmt.format(valueOf(it.dataType, it.dataScale, val))
                        break

                    case 'decimal':
                        def fmt = NumberFormat.getInstance(currentLocale())
                        fmt.setGroupingUsed(true)
                        fmt.setMinimumFractionDigits(it.dataScale)
                        fmt.setMaximumFractionDigits(it.dataScale)
                        val = fmt.format(valueOf(it.dataType, it.dataScale, val))
                        break

                    case 'date':
                        def fmt = it.dataScale == 1 ? DateFormat.getDateInstance(DateFormat.SHORT, currentLocale()) : DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, currentLocale())
                        val = fmt.format(valueOf(it.dataType, it.dataScale, val))
                        break
                }

                parameters << [code: 'p_' + it.code, prompt: name, type: it.dataType, value: val]
            } else {
                parameters << [code: 'p_' + it.code, prompt: name, type: it.dataType, value: '']
            }
        }

        return parameters
    }

    def getMax() {
        def max = getParams().max
        max = max?.isInteger() ? max.toInteger() : 0
        return (max > 0) ? Math.min(max, setting('pagination.max', 50)) : setting('pagination.default', 20)
    }

    def getOffset() {
        def offset = getParams().offset
        offset = offset?.isInteger() ? offset.toInteger() : 0
        return (offset >= 0) ? offset : 0
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private reversedConversion(from, conversion) {
        return (from.scale.id != conversion.source.scale.id)
    }

    private bestConversion(from, conversions) {
        if (conversions.size() == 1) return conversions[0]

        def best = conversions[0]

        def diff = Math.abs(from.multiplier - (reversedConversion(from, best) ? best.target.multiplier : best.source.multiplier))

        def pos = 1
        def val
        while (pos < conversions.size() && diff != 0.0) {
            val = Math.abs(from.multiplier - (reversedConversion(from, conversions[pos]) ? conversions[pos].target.multiplier : conversions[pos].source.multiplier))
            if (val < diff) {
                best = conversions[pos]
                diff = val
            }

            pos++
        }

        return best
    }

    private getRate(currency, date) {
        def value = cacheService.get('exchangeRate', currency.securityCode, currency.code + cacheService.IMPOSSIBLE_VALUE + "${date.getTime()}")
        if (value == null) {
            def stmt = 'from ExchangeRate as x where x.currency = ? and x.validFrom <= ? order by x.validFrom desc'
            def rates = ExchangeRate.findAll(stmt, [currency, date], [max: 1])
            if (rates) value = rates[0].rate
            cacheService.put('exchangeRate', currency.securityCode, currency.code + cacheService.IMPOSSIBLE_VALUE + "${date.getTime()}", value)
        }

        return (value == cacheService.IMPOSSIBLE_VALUE) ? null : value
    }

    private encodeNextFileNumber() {
        long val = nextFileNumber.getAndIncrement()
        long radix = 26L
        String chars = 'abcdefghijklmnopqrstuvwxyz'
        StringBuilder sb = new StringBuilder()
        while (val > 0L) {
            sb.append(chars[(int) (val % radix)])
            val = (long) (val / radix)
        }

        while (sb.length() < 6) {
            sb.append('a')
        }

        return sb.toString().reverse()
    }

    // Used for cache entry sizing
    private standardDataLength(val) {
        if (val == null) return CacheService.IMPOSSIBLE_VALUE.length()
        if (val instanceof String) return val.length()
        if (val instanceof Integer) return 4
        if (val instanceof BigDecimal) return 10
        if (val instanceof Date) return 8
        if (val instanceof Boolean) return 2

        return 8   // A reasonable default
    }

    // Handle a change of key into the company message table
    private syncCompanyKeys(company, translatables) {
        for (map in translatables) {
            if (map.oldCode && map.oldCode != map.code) {

                def recs = Message.findAllByCompanyAndCode(company, "${map.prefix}.${map.oldCode}")
                for (rec in recs) {
                    rec.code = "${map.prefix}.${map.code}"
                    if (!rec.saveThis()) return appendMessageError(domainObject, rec)
                }

                cacheService.resetThis('message', company.securityCode, "${map.prefix}.${map.oldCode}")
                cacheService.resetThis('message', company.securityCode, "${map.prefix}.${map.code}")

                if (map.propagate) {
                    def paths = (map.propagate instanceof String) ? [map.propagate] : map.propagate
                    for (path in paths) {
                        recs = Message.findAllByCompanyAndCodeLike(company, "${path}.${map.oldCode}.%", [sort: 'code'])
                        def prevOldCode = ''
                        def prevNewCode = ''
                        for (rec in recs) {
                            def oldCode = rec.code
                            def newCode = "${path}.${map.code}.${oldCode.substring(path.length() + oldCode.length() + 2)}"
                            rec.code = newCode
                            if (!rec.saveThis()) return appendMessageError(domainObject, rec)

                            if (oldCode != prevOldCode) {
                                if (prevOldCode) {
                                    cacheService.resetThis('message', company.securityCode, prevOldCode)
                                    cacheService.resetThis('message', company.securityCode, prevNewCode)
                                }

                                prevOldCode = oldCode
                                prevNewCode = newCode
                            }
                        }

                        // Handle any last propagation
                        if (prevOldCode) {
                            cacheService.resetThis('message', company.securityCode, prevOldCode)
                            cacheService.resetThis('message', company.securityCode, prevNewCode)
                        }
                    }
                }
            }
        }

        return true
    }

    // Handle a change of key into the system message table
    private syncSystemKeys(translatables) {
        for (map in translatables) {
            if (map.oldCode && map.oldCode != map.code) {

                def recs = SystemMessage.findAllByCode("${map.prefix}.${map.oldCode}")
                for (rec in recs) {
                    rec.code = "${map.prefix}.${map.code}"
                    if (!rec.saveThis()) return appendMessageError(domainObject, rec)
                }

                cacheService.resetThis('message', CacheService.COMPANY_INSENSITIVE, "${map.prefix}.${map.oldCode}")
                cacheService.resetThis('message', CacheService.COMPANY_INSENSITIVE, "${map.prefix}.${map.code}")

                if (map.propagate) {
                    def paths = (map.propagate instanceof String) ? [map.propagate] : map.propagate
                    for (path in paths) {
                        recs = SystemMessage.findAllByCodeLike("${path}.${map.oldCode}.%", [sort: 'code'])
                        def prevOldCode = ''
                        def prevNewCode = ''
                        for (rec in recs) {
                            def oldCode = rec.code
                            def newCode = "${path}.${map.code}.${oldCode.substring(path.length() + oldCode.length() + 2)}"
                            rec.code = newCode
                            if (!rec.saveThis()) return appendMessageError(domainObject, rec)

                            if (oldCode != prevOldCode) {
                                if (prevOldCode) {
                                    cacheService.resetThis('message', CacheService.COMPANY_INSENSITIVE, prevOldCode)
                                    cacheService.resetThis('message', CacheService.COMPANY_INSENSITIVE, prevNewCode)
                                }

                                prevOldCode = oldCode
                                prevNewCode = newCode
                            }
                        }

                        // Handle any last propagation
                        if (prevOldCode) {
                            cacheService.resetThis('message', CacheService.COMPANY_INSENSITIVE, prevOldCode)
                            cacheService.resetThis('message', CacheService.COMPANY_INSENSITIVE, prevNewCode)
                        }
                    }
                }
            }
        }

        return true
    }

    // Save a given company domain record and sort out its translations
    private localCompanySave(company, domainObject, translatables, locale) {

        // Note if we are dealing with the default locale
        def isDefaultLocale = locale.equals(Locale.default)

        // Work through the fields that can be translated
        for (map in translatables) {

            // Assume no changes will be made
            def modified = false

            // Grab the new text as entered in to the domain object or as supplied by a text parameter
            def text = map.field ? domainObject."${map.field}" : map.text

            // Create the message key
            def key = "${map.prefix}.${map.code}"

            // Grab all relevant messages
            def messages = Message.findAll("from Message as x where x.company = ? and x.code = ? and x.locale in ('*', ?, ?)",
                    [company, key, locale.getLanguage(), locale.getLanguage() + locale.getCountry()])

            // See if there is a default message
            def msg = messages.find {it.locale == '*'}
            if (msg) {

                // If we are dealing with the default locale and the text has changed, save the change
                if (isDefaultLocale) {
                    if (msg.text != text) {
                        msg.text = text
                        if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                        modified = true
                    }
                } else if (map.field) { // Not the default locale and the text was from a field in the domain object

                    // Need to reset the domain object's field to the original default locale text
                    // since we will be creating/updating locale specific texts and we want to keep
                    // the domain object using the default locale text
                    domainObject."${map.field}" = msg.text
                }
            } else {

                // No default message, so create one
                msg = new Message(company: company, code: key, locale: '*', text: text)
                if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                modified = true
            }

            // Look for a language specific message
            msg = messages.find {it.locale == locale.getLanguage()}
            if (msg) {

                // If we will be setting a message for language AND country, leave
                // this message alone unless this is the default locale, otherwise
                // update it if changed. We accept the change if we are using the
                // default locale because it could be a correction etc.
                if ((isDefaultLocale || !locale.getCountry()) && msg.text != text) {
                    msg.text = text
                    if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                    modified = true
                }
            } else {

                // No language specific message, so create one
                msg = new Message(company: company, code: key, locale: locale.getLanguage(), text: text)
                if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                modified = true
            }

            // If we have a country code as well as a language code
            if (locale.getCountry()) {

                // See if there is a language and country specific message
                msg = messages.find {it.locale == locale.getLanguage() + locale.getCountry()}
                if (msg) {

                    // Update it if different
                    if (msg.text != text) {
                        msg.text = text
                        if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                        modified = true
                    }
                } else {

                    // No language and country specific message, so create one
                    msg = new Message(company: company, code: key, locale: locale.getLanguage() + locale.getCountry(), text: text)
                    if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                    modified = true
                }
            }

            // Clear the cache for this key if we changed anything
            if (modified) cacheService.resetThis('message', company.securityCode, key)
        }

        // Save the domain object, passing back the success code
        return domainObject.saveThis()
    }

    // Save a given system domain record and sort out its translations
    private localSystemSave(domainObject, translatables, locale) {

        // Note if we are dealing with the default locale
        def isDefaultLocale = locale.equals(Locale.default)

        // Work through the fields that can be translated
        for (map in translatables) {

            // Assume no changes will be made
            def modified = false

            // Grab the new text as entered in to the domain object or as supplied by a text parameter
            def text = map.field ? domainObject."${map.field}" : map.text

            // Create the message key
            def key = "${map.prefix}.${map.code}"

            // Grab all relevant messages
            def messages = SystemMessage.findAll("from SystemMessage as x where x.code = ? and x.locale in ('*', ?, ?)",
                    [key, locale.getLanguage(), locale.getLanguage() + locale.getCountry()])

            // See if there is a default message
            def msg = messages.find {it.locale == '*'}
            if (msg) {

                // If we are dealing with the default locale and the text has changed, save the change
                if (isDefaultLocale) {
                    if (msg.text != text) {
                        msg.text = text
                        if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                        modified = true
                    }
                } else if (map.field) { // Not the default locale and the text was from a field in the domain object

                    // Need to reset the domain object's field to the original default locale text
                    // since we will be creating/updating locale specific texts and we want to keep
                    // the domain object using the default locale text
                    domainObject."${map.field}" = msg.text
                }
            } else {

                // No default message, so create one
                msg = new SystemMessage(code: key, locale: '*', text: text)
                if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                modified = true
            }

            // Look for a language specific message
            msg = messages.find {it.locale == locale.getLanguage()}
            if (msg) {

                // If we will be setting a message for language AND country, leave
                // this message alone unless this is the default locale, otherwise
                // update it if changed. We accept the change if we are using the
                // default locale because it could be a correction etc.
                if ((isDefaultLocale || !locale.getCountry()) && msg.text != text) {
                    msg.text = text
                    if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                    modified = true
                }
            } else {

                // No language specific message, so create one
                msg = new SystemMessage(code: key, locale: locale.getLanguage(), text: text)
                if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                modified = true
            }

            // If we have a country code as well as a language code
            if (locale.getCountry()) {

                // See if there is a language and country specific message
                msg = messages.find {it.locale == locale.getLanguage() + locale.getCountry()}
                if (msg) {

                    // Update it if different
                    if (msg.text != text) {
                        msg.text = text
                        if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                        modified = true
                    }
                } else {

                    // No language and country specific message, so create one
                    msg = new SystemMessage(code: key, locale: locale.getLanguage() + locale.getCountry(), text: text)
                    if (!msg.saveThis()) return appendMessageError(domainObject, msg)
                    modified = true
                }
            }

            // Clear the cache for this key if we changed anything
            if (modified) cacheService.resetThis('message', CacheService.COMPANY_INSENSITIVE, key)
        }

        // Save the domain object, passing back the success code
        return domainObject.saveThis()
    }

    // Handle the deletion of company messages
    private localCompanyDelete(company, domainObject, translatables) {
        for (map in translatables) {
            for (msg in Message.findAllByCompanyAndCode(company, "${map.prefix}.${map.code}")) msg.delete(flush: true)
            cacheService.resetThis('message', company.securityCode, "${map.prefix}.${map.code}")
            if (map.propagate) {
                def paths = (map.propagate instanceof String) ? [map.propagate] : map.propagate
                for (path in paths) {
                    def recs = Message.findAllByCompanyAndCodeLike(company, "${path}.${map.code}.%", [sort: 'code'])
                    def prevCode = ''
                    for (rec in recs) {
                        rec.delete(flush: true)
                        if (rec.code != prevCode) {
                            if (prevCode) cacheService.resetThis('message', company.securityCode, prevCode)
                            prevCode = rec.code
                        }
                    }

                    if (prevCode) cacheService.resetThis('message', company.securityCode, prevCode)
                }
            }
        }
    }

    // Handle the deletion of system messages
    private localSystemDelete(domainObject, translatables) {
        for (map in translatables) {
            for (msg in SystemMessage.findAllByCode("${map.prefix}.${map.code}")) msg.delete(flush: true)
            cacheService.resetThis('message', CacheService.COMPANY_INSENSITIVE, "${map.prefix}.${map.code}")
            if (map.propagate) {
                def paths = (map.propagate instanceof String) ? [map.propagate] : map.propagate
                for (path in paths) {
                    def recs = SystemMessage.findAllByCodeLike("${path}.${map.code}.%", [sort: 'code'])
                    def prevCode = ''
                    for (rec in recs) {
                        rec.delete(flush: true)
                        if (rec.code != prevCode) {
                            if (prevCode) cacheService.resetThis('message', CacheService.COMPANY_INSENSITIVE, prevCode)
                            prevCode = rec.code
                        }
                    }

                    if (prevCode) cacheService.resetThis('message', CacheService.COMPANY_INSENSITIVE, prevCode)
                }
            }
        }
    }

    private appendMessageError(domainObject, msg) {
        domainObject.errorMessage(code: 'generic.message.error', args: [msg.code, msg.locale, msg.text], default: "Error saving message with code ${msg.code} for locale ${msg.locale} with text of: ${msg.text}")

        return false    // This gets passed as a failure code of the calling method
    }

    private static createEpoch() {
        def cal = Calendar.getInstance()
        cal.set(1970, 0, 1, 0, 0, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.getTime()
    }

    private getRequest() {
        return RequestContextHolder.currentRequestAttributes().getCurrentRequest()
    }

    private getParams() {
        return RequestContextHolder.currentRequestAttributes().getParams()
    }

    private getSession() {
        //        def webRequest = RequestContextHolder.currentRequestAttributes()
        //        def request = webRequest.getCurrentRequest()
        //        def session = webRequest.getSession()
        //        def params = webRequest.getParams()
        //        def servletContext = webRequest.getServletContext()
        //        def grailsAttributes = webRequest.getAttributes()
        //        def messageSource = grailsAttributes.getApplicationContext().getBean("messageSource")
        //        def locale = RequestContextUtils.getLocale(request)
        return RequestContextHolder.currentRequestAttributes().getSession()
    }

    private readGoogleRate(code) {
        def data = readURL("http://www.google.com/ig/calculator?hl=en&q=1${BASE_CURRENCY_CODE}%3D%3F${code}")
        if (data) {
            data = JSON.parse(data)
            if (!data.error) {
                data = data?.rhs?.replace(' ', '')
                if (data) {
                    def pos = data.length() - 1
                    def c
                    for (; pos >= 0; pos--) {
                        c = data.charAt(pos)
                        if (c >= '0' && c <= '9') break
                    }

                    if (pos > 0) {
                        data = data.substring(0, pos)
                        if (data.isBigDecimal()) {
                            data = data.toBigDecimal().setScale(6, RoundingMode.HALF_UP)
                            if (data > 0.0) return data
                        }
                    }
                }
            }
        }

        return null
    }

    private readYahooRate(code) {
        def data = readURL("http://finance.yahoo.com/d/quotes.csv?s=${BASE_CURRENCY_CODE}${code}=X&f=b")
        if (data?.isBigDecimal()) {
            data = data.toBigDecimal().setScale(6, RoundingMode.HALF_UP)
            if (data > 0.0) return data
        }

        return null
    }

    private toDomainLabel(domain) {
        if (!domain) return '???'
        if (domain instanceof CharSequence) return getDomainLabel(domain.toString())
        if (domain instanceof Class) return getDomainLabel(domain.getSimpleName())
        return getDomainLabel(domain.getClass().getSimpleName())
    }

    private getDomainLabel(name) {
        return message(code: GrailsNameUtils.getPropertyName(name) + '.label', default: GrailsNameUtils.getNaturalName(name))
    }

    private toDomainValue(domain, value) {
        return (value || !domain || domain instanceof CharSequence || domain instanceof Class) ? value?.toString() : domain.toString()
    }
}
