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
package org.grails.tlc.books

import org.grails.tlc.corp.ExchangeCurrency
import org.grails.tlc.sys.SystemAccountType
import org.grails.tlc.sys.SystemCustomerAddressType
import org.grails.tlc.sys.SystemSupplierAddressType
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.servlet.support.RequestContextUtils
import java.util.concurrent.locks.ReentrantLock

class BookService {

    public static SEGMENT_DELIMITER
    public static MNEMONIC_IDENTIFIER
    public static ALPHA_CODES_UPPERCASE
    public static CUSTOMER_CODES_UPPERCASE
    public static SUPPLIER_CODES_UPPERCASE
    private static final companyLocks = [:]
    private static final controlAccounts = [:]

    static transactional = false

    def utilService
    def autoPayService
    def sessionFactory
    def paymentService  // Customization of bank automatic payments

    // Initialize the static variables
    static prepareForUse(grailsApplication) {
        def val = grailsApplication.config.books.segment.delimiter
        SEGMENT_DELIMITER = (val && val instanceof String && [':', '.', '|', '/', '-', '+'].contains(val)) ? val : '-'
        val = grailsApplication.config.books.mnemonic.identifier
        if (val && val instanceof String && [':', '.', '|', '/', '-', '+'].contains(val) && val != SEGMENT_DELIMITER) {
            MNEMONIC_IDENTIFIER = val
        } else {
            MNEMONIC_IDENTIFIER = (SEGMENT_DELIMITER == '+') ? '-' : '+'
        }

        val = grailsApplication.config.books.alphabetic.codes.uppercase
        ALPHA_CODES_UPPERCASE = (val != null && val instanceof Boolean) ? val : true
        val = grailsApplication.config.books.customer.codes.uppercase
        CUSTOMER_CODES_UPPERCASE = (val != null && val instanceof Boolean) ? val : true
        val = grailsApplication.config.books.supplier.codes.uppercase
        SUPPLIER_CODES_UPPERCASE = (val != null && val instanceof Boolean) ? val : true
    }

    // Returns true if a string GL account code element is alphabetic as specified by ALPHA_CODES_UPPERCASE
    static isAlphabetic(val) {
        if (ALPHA_CODES_UPPERCASE) return (val ==~ /[A-Z]+/)

        return val ==~ (/[a-z]+/)
    }

    // Returns true if a string code element is numeric
    static isNumeric(val) {
        return val ==~ (/[0-9]+/)
    }

    // Corrects the case of a GL account code (or code element) to match the system setting
    static fixCase(val) {
        if (!val) return val
        if (ALPHA_CODES_UPPERCASE) return val.toUpperCase(getForcedLocale())
        return val.toLowerCase(getForcedLocale())
    }

    // Converts a customer account code to upper case if required
    static fixCustomerCase(val) {
        return (val && CUSTOMER_CODES_UPPERCASE) ? val.toUpperCase(getForcedLocale()) : val
    }

    // Converts a supplier account code to upper case if required
    static fixSupplierCase(val) {
        return (val && SUPPLIER_CODES_UPPERCASE) ? val.toUpperCase(getForcedLocale()) : val
    }

    // Returns a lock object for a company. The lock object is not actually locked by this method - that's up to the
    // caller with a try/finally block. Use to serialize structural and posting changes to the books.
    static getCompanyLock(company) {
        def lock
        synchronized (companyLocks) {
            lock = companyLocks.get(company.securityCode.toString())
            if (!lock) {
                lock = new ReentrantLock()
                companyLocks.put(company.securityCode.toString(), lock)
            }
        }

        return lock
    }

    // Returns a control account for a company. The key is the type attribute of a SystemAccountType object. Note that
    // this method can only handle 'singleton' type control accounts such as retained profits account, NOT multiple
    // accounts such as bank accounts.
    static getControlAccount(company, key) {
        def account, id
        synchronized (controlAccounts) {
            id = controlAccounts.get(company.securityCode.toString() + key)
            if (id == null) {
                account = SystemAccountType.findByCode(key)
                if (account) account = Account.findBySecurityCodeAndType(company.securityCode, account)
                id = account ? account.id : 0L
                controlAccounts.put(company.securityCode.toString() + key, id)
            }
        }

        if (id && !account) account = Account.get(id)

        return account
    }

    // Clears the control account map
    static resetControlAccounts() {
        synchronized (controlAccounts) {
            controlAccounts.clear()
        }
    }

    // Checks the validity of the range records for a section returning true if they are ok. If an error is
    // encountered, false is returned and the victim domain will have a 'domain level' error message attached
    // or, if no victim domain is supplied, the section domain will have the error message attached instead.
    def verifyRangeTests(section, victim = null) {
        def ranges = section.accountSegment ? ChartSectionRange.findAllBySection(section, [sort: 'type', order: 'desc']) : null
        if (ranges) {
            if (!victim) victim = section
            if (ranges[0].type != 'include') {
                victim.errorMessage(code: 'chartSectionRange.no.include', args: [section.toString()], default: "The first range of section ${section.toString()} is not an include range")
                return false
            }

            if (ranges.size() > 1 && ranges[1].type == 'include') {
                victim.errorMessage(code: 'chartSectionRange.multi.include', args: [section.toString()], default: "Section ${section.toString()} has more than one include range")
                return false
            }

            def accountSegmentIndex = section.accountSegment - 1
            def minSet = ranges[0].rangeFrom.split("\\${SEGMENT_DELIMITER}")
            def maxSet = ranges[0].rangeTo.split("\\${SEGMENT_DELIMITER}")
            if (minSet[accountSegmentIndex] == '*' && maxSet[accountSegmentIndex] == '*') {
                victim.errorMessage(code: 'chartSectionRange.bad.include', args: [section.toString()], default: "The include range for section ${section.toString()} does not specify the account number range")
                return false
            }

            def rangeTests = []
            def rangeElements = []
            for (int i = 0; i < minSet.size(); i++) {
                rangeElements << section."segment${i + 1}"
                rangeTests << [new CodeRange(rangeElements[i].dataType, rangeElements[i].dataLength, minSet[i], maxSet[i])]
            }

            def fromSet, toSet, rangeList, rangeRec
            for (int r = 1; r < ranges.size(); r++) {
                rangeRec = ranges[r]
                fromSet = rangeRec.rangeFrom.split("\\${SEGMENT_DELIMITER}")
                toSet = rangeRec.rangeTo.split("\\${SEGMENT_DELIMITER}")
                for (int i = 0; i < fromSet.size(); i++) {

                    // Ignore segements not to be tested
                    if (fromSet[i] == '*' && toSet[i] == '*') continue
                    rangeList = rangeTests[i]
                    if (fromSet[i] != '*' && minSet[i] != '*' && fromSet[i] < minSet[i]) {
                        if (rangeRec.type == 'exclude') {
                            victim.errorMessage(code: 'chartSectionRange.exclude.min.low', args: [section.toString(), fromSet[i], i + 1, minSet[i]],
                                default: "Section ${section.toString()} exclusion minimum of ${fromSet[i]} for segment ${i + 1} is below the overall minimum of ${minSet[i]}")
                        } else {
                            victim.errorMessage(code: 'chartSectionRange.disallow.min.low', args: [section.toString(), fromSet[i], i + 1, minSet[i]],
                                default: "Section ${section.toString()} disallowed minimum of ${fromSet[i]} for segment ${i + 1} is below the overall minimum of ${minSet[i]}")
                        }

                        return false
                    }

                    if (fromSet[i] != '*' && maxSet[i] != '*' && fromSet[i] > maxSet[i]) {
                        if (rangeRec.type == 'exclude') {
                            victim.errorMessage(code: 'chartSectionRange.exclude.min.high', args: [section.toString(), fromSet[i], i + 1, maxSet[i]],
                                default: "Section ${section.toString()} exclusion minimum of ${fromSet[i]} for segment ${i + 1} is above the overall maximum of ${maxSet[i]}")
                        } else {
                            victim.errorMessage(code: 'chartSectionRange.disallow.min.high', args: [section.toString(), fromSet[i], i + 1, maxSet[i]],
                                default: "Section ${section.toString()} disallowed minimum of ${fromSet[i]} for segment ${i + 1} is above the overall maximum of ${maxSet[i]}")
                        }

                        return false
                    }

                    if (toSet[i] != '*' && maxSet[i] != '*' && toSet[i] > maxSet[i]) {
                        if (rangeRec.type == 'exclude') {
                            victim.errorMessage(code: 'chartSectionRange.exclude.max.high', args: [section.toString(), toSet[i], i + 1, maxSet[i]],
                                default: "Section ${section.toString()} exclusion maximum of ${toSet[i]} for segment ${i + 1} is above the overall maximum of ${maxSet[i]}")
                        } else {
                            victim.errorMessage(code: 'chartSectionRange.disallow.max.high', args: [section.toString(), toSet[i], i + 1, maxSet[i]],
                                default: "Section ${section.toString()} disallowed maximum of ${toSet[i]} for segment ${i + 1} is above the overall maximum of ${maxSet[i]}")
                        }

                        return false
                    }

                    if (toSet[i] != '*' && minSet[i] != '*' && toSet[i] < minSet[i]) {
                        if (rangeRec.type == 'exclude') {
                            victim.errorMessage(code: 'chartSectionRange.exclude.max.low', args: [section.toString(), toSet[i], i + 1, minSet[i]],
                                default: "Section ${section.toString()} exclusion maximum of ${toSet[i]} for segment ${i + 1} is below the overall minimum of ${minSet[i]}")
                        } else {
                            victim.errorMessage(code: 'chartSectionRange.disallow.max.low', args: [section.toString(), toSet[i], i + 1, rangeTest[0][0]],
                                default: "Section ${section.toString()} disallowed maximum of ${toSet[i]} for segment ${i + 1} is below the overall minimum of ${minSet[i]}")
                        }

                        return false
                    }

                    if (rangeRec.type == 'exclude') new CodeRange(rangeElements[i].dataType, rangeElements[i].dataLength, fromSet[i], toSet[i]).excludeFrom(rangeList)
                }
            }

            // Grab the list containing the account code ranges
            rangeList = rangeTests[accountSegmentIndex]

            // Check that the exclusions have not removed all account ranges
            if (!rangeList) {
                victim.errorMessage(code: 'chartSectionRange.excludes.all', args: [section.toString(), accountSegmentIndex + 1],
                    default: "Section ${section.toString()} exclusions for segment ${accountSegmentIndex + 1} have excluded all possible account code values")

                return false
            }

            // Work through all sections looking for overlapping account code ranges
            def sections = ChartSection.findAll('from ChartSection as x where x.company = ? and x.accountSegment > 0 and x.code != ?', [section.company, section.code])
            for (s in sections) {
                if (CodeRange.rangesOverlap(rangeList, createAccountRanges(s))) {
                    victim.errorMessage(code: 'chartSectionRange.overlap', args: [section.toString(), s.toString()],
                        default: "The account codes permitted in section ${section.toString()} overlap those permitted in section ${s.toString()}")
                    return false
                }
            }

            // Need to verify that the range change hasn't invalidated any accounts
            def accounts = Account.findAllBySection(section)
            for (account in accounts) {
                def result = isInRange(section, account.code.split("\\${SEGMENT_DELIMITER}"), false)
                if (result != true) {
                    victim.errorMessage(code: 'chartSectionRange.account.bad', default: 'The change to the ranges would invalidate accounts in this section')
                    return false
                }
            }
        }

        return true
    }

    // Returns the account code ranges for a given section or null if there are no ranges. This needs to be kept as
    // a disk based (rather than cache based) method
    def createAccountRanges(section) {
        def rangeList = null
        def ranges = section.accountSegment ? ChartSectionRange.findAllBySectionAndTypeNotEqual(section, 'disallow', [sort: 'type', order: 'desc']) : null
        if (ranges && ranges[0].type == 'include') {
            def accountSegmentIndex = section.accountSegment - 1
            def element = section."segment${accountSegmentIndex + 1}"
            def fromSet, toSet
            for (range in ranges) {
                fromSet = range.rangeFrom.split("\\${SEGMENT_DELIMITER}")
                toSet = range.rangeTo.split("\\${SEGMENT_DELIMITER}")

                // If the include range
                if (rangeList == null) {
                    rangeList = [new CodeRange(element.dataType, element.dataLength, fromSet[accountSegmentIndex], toSet[accountSegmentIndex])]
                } else if (fromSet[accountSegmentIndex] != '*' || toSet[accountSegmentIndex] != '*') {  // Exclude range with account number values
                    new CodeRange(element.dataType, element.dataLength, fromSet[accountSegmentIndex], toSet[accountSegmentIndex]).excludeFrom(rangeList)
                }
            }
        }

        return rangeList
    }

    // Returns all ranges for the segments of a section. The returned value is a list. The first n elements of the
    // list are range lists for the segments of the section. The remaining elements of the returned list, if any,
    // are the disallowed tests for the section. Each disallowed entry in the returned list is a two element list
    // where the first element is the 'from' test and the second element is the to test (both as strings).
    def createAllRanges(section) {

        // Try and get it from the cache
        def rangeTests = utilService.cacheService.get('ranges', section.securityCode, section.toString())

        // If we've not been asked for this before
        if (rangeTests == null) {

            // Create the list we will be returning
            rangeTests = []

            // We will need the elements of each segment
            def elements = []

            // Grab the elements and fill in the returned list with empty segment range lists
            for (int i = 0; i < 8; i++) {
                def element = section."segment${i + 1}"
                if (!element) break
                elements << element
                rangeTests << []
            }

            // Get the ranges records for this section
            def ranges = ChartSectionRange.findAllBySection(section, [sort: 'type', order: 'desc'])
            def fromSet, toSet
            def length = 1      // Cannot have zero length as the cache system would think it was an error
            for (range in ranges) {
                if (range.type == 'include') {
                    fromSet = range.rangeFrom.split("\\${SEGMENT_DELIMITER}")
                    toSet = range.rangeTo.split("\\${SEGMENT_DELIMITER}")
                    for (int i = 0; i < elements.size(); i++) {
                        rangeTests[i] << new CodeRange(elements[i].dataType, elements[i].dataLength, fromSet[i], toSet[i])
                    }
                } else if (range.type == 'exclude') {
                    fromSet = range.rangeFrom.split("\\${SEGMENT_DELIMITER}")
                    toSet = range.rangeTo.split("\\${SEGMENT_DELIMITER}")
                    for (int i = 0; i < elements.size(); i++) {
                        if (fromSet[i] != '*' || toSet[i] != '*')
                            new CodeRange(elements[i].dataType, elements[i].dataLength, fromSet[i], toSet[i]).excludeFrom(rangeTests[i])
                    }
                } else {    // Disallow

                    // Might as well add in the lengths at this point since it's convenient
                    length += range.rangeFrom.length() + range.rangeTo.length()

                    rangeTests << Collections.unmodifiableList([range.rangeFrom, range.rangeTo])
                }
            }

            // Need to add in the sizes of all range objects. Also
            // protect everything by making it unmodifiable.
            for (int i = 0; i < elements.size(); i++) {
                ranges = rangeTests[i]
                rangeTests[i] = Collections.unmodifiableList(ranges)
                for (range in ranges) {
                    length += range.size()
                    range.modifiable = false
                }
            }

            utilService.cacheService.put('ranges', section.securityCode, section.toString(), Collections.unmodifiableList(rangeTests), length)
        }

        return rangeTests
    }

    // Finds an account (creating it if possible) based on its code. Any mnemonics must have already been
    // substituted and the case corrected. Defaults will be handled by this method. If this method returns
    // a string rather than an account the string will be an error message. If the testing flag is true, a
    // newly created account will not actually be saved (i.e. its id will still be zero). If the manual flag
    // is set to true, then this will not be regarded as an auto-creation (used by import facility).
    def getAccount(company, code, testing = false, manual = false) {

        // Make sure they gave us a code to work with
        if (!code) return message(code: 'account.code.missing', default: 'Account code is empty')

        // Make sure it's vaguely sensible
        if (code.startsWith(SEGMENT_DELIMITER) || code.endsWith(SEGMENT_DELIMITER) || code.indexOf(SEGMENT_DELIMITER + SEGMENT_DELIMITER) >= 0) {
            return message(code: 'account.code.malformed', default: 'Account code is malformed')
        }

        // See if this is a known code
        def originalCode = code
        def cachedCode = utilService.cacheService.get('account', company.securityCode, originalCode)
        if (cachedCode) code = cachedCode

        // See if it already exists
        def account = Account.findBySecurityCodeAndCode(company.securityCode, code)
        if (account) {
            if (cachedCode == null) utilService.cacheService.put('account', company.securityCode, originalCode, account.code)
            return account
        } else {
            if (cachedCode) utilService.cacheService.resetThis('account', company.securityCode, originalCode)
        }

        // Break the code down in to segments
        def accountSegments = code.split("\\${SEGMENT_DELIMITER}")

        // Ensure that all segment values are either numeric or alphabetic
        for (int i = 0; i < accountSegments.size(); i++) {
            if (!accountSegments[i]) {
                return message(code: 'account.code.no.type', args: [i + 1, accountSegments[i]],
                    default: "Account code segment ${i + 1} is blank")
            }

            if (!isNumeric(accountSegments[i]) && !isAlphabetic(accountSegments[i])) {
                return message(code: 'account.code.bad.type', args: [i + 1, accountSegments[i]],
                    default: "Account code segment ${i + 1} with the value of ${accountSegments[i]} is neither alphabetic nor numeric")
            }
        }

        // Find all the sections that can contain accounts and work through them
        def sections = ChartSection.findAllByCompanyAndAccountSegmentGreaterThan(company, (byte) 0)
        def accountSegmentIndex, patternSegments, effectiveSegments, result
        for (section in sections) {

            // Get the account segment index for this section
            accountSegmentIndex = section.accountSegment - 1

            // Break the section account ocde pattern down in to segments
            patternSegments = section.pattern.split("\\${SEGMENT_DELIMITER}")

            // If we have more account segements than pattern segments, this can't possibly be the right section
            if (accountSegments.size() > patternSegments.size()) continue

            // Use the account segments if correct length, else try and use defaults
            effectiveSegments = (accountSegments.size() == patternSegments.size()) ? verifyAccountCode(patternSegments, accountSegments) : completeAccountCode(patternSegments, accountSegments)

            // If the code now has the correct number of segments
            if (effectiveSegments) {

                // See if the account would fit in this section
                result = isInRange(section, effectiveSegments, true)

                // If we found an esisting account, return it
                if (result instanceof Account) {
                    utilService.cacheService.put('account', company.securityCode, originalCode, result.code)
                    return result
                }

                // If we got back an error message, just pass it back
                if (result instanceof String) return result

                // If the account would fit in this section
                if (result) {

                    // Construct the account code we are actually looking for
                    def effectiveCode = effectiveSegments.join(SEGMENT_DELIMITER)

                    // If we are allowed to dynamically create accounts in this section or they have specifically
                    // stated that is is not an auto-creation
                    if (section.autoCreate || manual) {
                        account = new Account()
                        account.section = section
                        account.status = section.status
                        account.code = effectiveCode
                        def name = ''
                        for (int i = 0; i < effectiveSegments.size(); i++) {
                            def element = section."segment${i + 1}"
                            def elementValue = CodeElementValue.findByElementAndCode(element, effectiveSegments[i])
                            if (!elementValue) {
                                return message(code: 'account.code.elementValue', args: [section.name, i + 1, effectiveSegments[i], element.name],
                                    default: "Error in section ${section.name}: Code segment ${i + 1} with a value of ${effectiveSegments[i]} could not be found as a value for element ${element.name}")
                            }

                            account."element${element.elementNumber}" = elementValue
                            if (name) name = name + ' '
                            name = name + elementValue.shortName
                        }

                        account.name = name
                        account.currency = ExchangeCurrency.findByCompanyAndCompanyCurrency(company, true, [cache: true])
                        if (testing) return account
                        if (insertAccount(account)) {
                            utilService.cacheService.put('account', company.securityCode, originalCode, account.code)
                            return account
                        }

                        name = message(code: 'account.code.bad.save', args: [section.name, effectiveCode],
                            default: "Error in section ${section.name}: Unable to save new account with code ${effectiveCode}")
                        log.error(name)
                        return name
                    } else {
                        return message(code: 'account.code.no.dynamic', args: [effectiveCode, section.name],
                            default: "Account ${effectiveCode} could not be created since dynamic creation is not allowed in section ${section.name}")
                    }
                }
            }
        }

        return message(code: 'account.code.no.section', args: [originalCode], default: "Unable to find a section that could contain an account with code ${originalCode}")
    }

    // Check whether the code segments of an account code are permitted within the given section. If the loadIfExists
    // flag is set and the effective account code already exists in the section, then the account will be returned on
    // the ASSUMPTION it is valid within the section. If the loadIfExists flag is not set then Boolean true will be
    // returned if the effective account code is valid for this section. Boolean false will be returned when the
    // 'account number' would not fit in the range of permitted account numbers for this section. If the 'account number'
    // segment WOULD fit in this section, but the full effective account code would be invalid then a String error
    // message is returned. Reasons why a String error message might be returned include: A code segment value does not
    // actually exist as an element value; the account number might be in range but other segment values are not; the
    // combination of segment values is specifically disallowed. This method assumes that the number and type of code
    // segments matches the given section.
    def isInRange(section, codeSegments, loadIfExists, omitExistenceCheck = false) {

        // Get all the range tests including the disllow tests
        def rangeLists = createAllRanges(section)
        if (!rangeLists) return false

        def accountSegmentIndex = section.accountSegment - 1

        // Grab the 'account number' segment tests
        def ranges = rangeLists[accountSegmentIndex]

        // If the account number is within the range(s) for this section
        if (CodeRange.contains(ranges, codeSegments[accountSegmentIndex])) {

            // See that it is actually allowed as an account number
            def invalidMessage
            if (!omitExistenceCheck) {
                invalidMessage = segmentInvalid(section, codeSegments, accountSegmentIndex)
                if (invalidMessage) return invalidMessage
            }

            // Work through the remainder of the ranges
            for (int i = 0; i < codeSegments.size(); i++) {
                if (i != accountSegmentIndex) {
                    ranges = rangeLists[i]
                    if (CodeRange.contains(ranges, codeSegments[i])) {
                        if (!omitExistenceCheck) {
                            invalidMessage = segmentInvalid(section, codeSegments, i)
                            if (invalidMessage) return invalidMessage
                        }
                    } else {
                        return message(code: 'account.code.excluded', args: [section.name, i + 1, codeSegments[i]],
                            default: "Error in section ${section.name}: Code segment ${i + 1} with a value of ${codeSegments[i]} is not within the permitted range of values for this section")
                    }
                }
            }

            // Construct the account code we are actually looking for
            def effectiveCode = codeSegments.join(SEGMENT_DELIMITER)

            // If they have asked for us to load it if possible, check now whether the account exists
            if (loadIfExists) {
                def account = Account.findBySecurityCodeAndCode(section.securityCode, effectiveCode)
                if (account) return account
            }

            // Work through the disallowed tests
            for (int i = codeSegments.size(); i < rangeLists.size(); i++) {

                // Grab the from and to specs for this disallowed test
                def from = rangeLists[i][0].split("\\${SEGMENT_DELIMITER}")
                def to = rangeLists[i][1].split("\\${SEGMENT_DELIMITER}")

                // Assume this disallowed test will not exclude our code
                def disallowed = false

                // Work through the disallowed test segments
                for (int j = 0; j < codeSegments.size(); j++) {

                    // Ignore 'no test' segments
                    if (from[j] == '*' && to[j] == '*') continue

                    // If this segment is included by the disallowed test, mark it as potentially disallowed
                    if ((from[j] == '*' || from[j] <= codeSegments[j]) && (to[j] == '*' || to[j] >= codeSegments[j])) {
                        disallowed = true
                    } else {

                        // It's not disallowed since this segment is outside the disallowed test range
                        disallowed = false
                        break
                    }
                }

                // If disallowed, tell them about it
                if (disallowed) {

                    // If there is a specific message that goes with this failure, use it
                    if (rangeLists[i][2]) return rangeLists[i][2]

                    // No specific message, so construct one
                    return message(code: 'account.code.disallowed', args: [effectiveCode, section.name],
                        default: "The code combination of ${effectiveCode} is specifically disallowed for section ${section.name}")
                }
            }

            return true
        }

        return false
    }

    // Check a section to ensure that the defaults are acceptable to the ranges specified for the section, if any
    def defaultsAreValid(section) {
        if (section.default1 || section.default2 || section.default3 || section.default4 || section.default5 || section.default6 || section.default7 || section.default8) {
            def tests = createAllRanges(section)
            def patternSet = section.pattern.split("\\${SEGMENT_DELIMITER}")
            for (int i = 0; i < patternSet.size(); i++) {
                if (!patternSet[i].startsWith('@') && !patternSet[i].startsWith('#') && tests[i]) {
                    if (!CodeRange.contains(tests[i], patternSet[i])) {
                        section.errorMessage(code: 'chartSection.default.bad', args: [i + 1], default: "The default for segment ${i + 1} is not within the ranges for this section")
                        return false
                    }
                }
            }
        }

        return true
    }

    // Returns a list of account code segments adjusted for defaults, or null if the account code
    // segments cannot be adjusted to the required pattern segments
    def completeAccountCode(patternSegments, accountSegments) {

        // Clone the two lists since we will modify them
        def clonedPatternSegments = []
        for (it in patternSegments) clonedPatternSegments << it
        def clonedAccountSegments = []
        for (it in accountSegments) clonedAccountSegments << it

        // Look for a mismatch
        def badPos = findMismatch(clonedPatternSegments, clonedAccountSegments, 0)
        while (badPos >= 0) {

            // Find a pattern segment that DOES match the account segment
            def goodPos = findMatch(clonedPatternSegments, clonedAccountSegments, badPos)

            // No match found
            if (goodPos == -1) return null

            // Use up any defaults after the mismatch position but only up to the first non-default segent
            for (; badPos < goodPos && (isNumeric(clonedPatternSegments[badPos]) || isAlphabetic(clonedPatternSegments[badPos])); badPos++) {
                clonedAccountSegments.add(badPos, clonedPatternSegments[badPos])
                clonedPatternSegments[badPos] = isNumeric(clonedPatternSegments[badPos]) ? '##########'.substring(0, clonedPatternSegments[badPos].length()) : '@@@@@@@@@@'.substring(0, clonedPatternSegments[badPos].length())
            }

            // If we still need defaults, we will have to look backwards from the mismatch position
            while (badPos < goodPos) {
                def found = false
                for (int i = badPos; i >= 0; i--) {
                    if (!matchesPattern(clonedPatternSegments[i + 1], clonedAccountSegments[i])) return null
                    if (isNumeric(clonedPatternSegments[i]) || isAlphabetic(clonedPatternSegments[i])) {
                        clonedAccountSegments.add(i, clonedPatternSegments(i))
                        clonedPatternSegments[i] = isNumeric(clonedPatternSegments[i]) ? '##########'.substring(0, clonedPatternSegments[i].length()) : '@@@@@@@@@@'.substring(0, clonedPatternSegments[i].length())
                        badPos++
                        found = true
                        break
                    }
                }

                if (!found) return null
            }

            badPos = findMismatch(clonedPatternSegments, clonedAccountSegments, goodPos)
        }

        // If we have less account segments than pattern segments then append pattern defaults to the account
        while (clonedAccountSegments.size() < clonedPatternSegments.size()) {

            // Index in to the next pattern segment
            badPos = clonedAccountSegments.size()

            // If the next pattern segment is not a default then this must be wrong
            if (clonedPatternSegments[badPos].startsWith('#') || clonedPatternSegments[badPos].startsWith('@')) return null

            // Append the pattern default
            clonedAccountSegments << clonedPatternSegments[badPos]
        }

        // We have appended any defaults requires, so now we are done
        return clonedAccountSegments
    }

    // Substitutes mnemonics and corrects the case of an account code. Returns null if the code cannot be expanded
    // due to such as a bad mnemonic code
    def expandAccountCode(user, code) {
        def pos = code.indexOf(MNEMONIC_IDENTIFIER)
        while (pos >= 0) {
            def pos2 = pos + 1
            for (; pos2 < code.length(); pos2++) {
                if (code[pos2] == SEGMENT_DELIMITER || code[pos2] == MNEMONIC_IDENTIFIER) break
            }

            def sub = getMnemonic(user, code.substring(pos + 1, pos2))
            if (!sub) return null
            code = code.substring(0, pos) + sub + code.substring(pos2)
            pos = code.indexOf(MNEMONIC_IDENTIFIER)
        }

        return fixCase(code)
    }

    // Check whethet a segment value exists and return null if it does, else return a message stating what the error is
    def segmentInvalid(section, segmentValues, segmentIndex) {

        // Grab the code element
        def element = section."segment${segmentIndex + 1}"

        // If we can find it, then everything is ok
        if (CodeElementValue.findByElementAndCode(element, segmentValues[segmentIndex])) return null

        return message(code: 'account.code.not.exist', args: [section.name, segmentIndex + 1, segmentValues[segmentIndex], element.name],
            default: "Error in section ${section.name}: Code segment ${segmentIndex + 1} with a value of ${segmentValues[segmentIndex]} does not exist as a value for element ${element.name}")
    }

    // Checks that account code segments match the pattern segments, returning the account segements if they do
    // or null if they don't. There are assumed to be the same number of segments in each list
    def verifyAccountCode(patternSegments, accountSegments) {
        for (int i = 0; i < accountSegments.size(); i++) {
            if (!matchesPattern(patternSegments[i], accountSegments[i])) return null
        }

        return accountSegments
    }

    // Inserts a new general ledger account returning true if the save was successful, false if not. If the save failed
    // then the account will have an error message attached to it at domain level. This method creates the required
    // balance records associated with the account
    def insertAccount(account) {
        def valid = true
        def lock = getCompanyLock(account.section.company)
        lock.lock()
        try {
            // Not sure why we need to do this manual check, but Grails 1.1 is throwing an exception
            // if the code is not unique when it is saved rather than just returning false from the
            // saveThis() method
            if (Account.countByCodeAndSecurityCode(account.code, account.section.securityCode)) {
                def temp = message(code: 'account.code.label', default: 'Code')
                account.errorMessage(field: 'code', code: 'default.not.unique.message', args: [temp, '', account.code], default: "${temp} (${account.code}) must be unique")
                return false
            }

            def pds = Period.findAllBySecurityCode(account.section.securityCode)
            Account.withTransaction {status ->
                if (account.saveThis()) {
                    for (pd in pds) {
                        if (!new GeneralBalance(account: account, period: pd).saveThis()) {
                            account.errorMessage(code: 'account.balance.save', args: [account.toString()], default: "Unable to save the balance record(s) for account ${account.toString()}")
                            status.setRollbackOnly()
                            valid = false
                            break
                        }
                    }
                } else {
                    status.setRollbackOnly()
                    valid = false
                }
            }
        } finally {
            lock.unlock()
        }

        return valid
    }

    // Deletes an existing general ledger account. Throws an exception if the account cannot be deleted.
    def deleteAccount(account) {
        def lock = getCompanyLock(account.section.company)
        lock.lock()
        try {
            Account.withTransaction {status ->
                account.delete(flush: true)
            }
        } finally {
            lock.unlock()
        }
    }

    // Inserts a new customer account returning true if the save was successful, false if not. If the save failed
    // then the customer will have an error message attached to it at domain level. This method creates the required
    // turnover records associated with the customer.
    def insertCustomer(customer) {
        def types = SystemCustomerAddressType.list()
        def valid = true
        def lock = getCompanyLock(customer.company)
        lock.lock()
        try {
            def pds = Period.findAllBySecurityCode(customer.company.securityCode)
            Customer.withTransaction {status ->
                if (customer.saveThis()) {
                    def address = new CustomerAddress(customer: customer, country: customer.country, format: customer.country.addressFormat)
                    if (address.saveThis()) {
                        for (type in types) address.addToAddressUsages(new CustomerAddressUsage(customer: customer, type: type))
                        if (address.save()) {   // With deep validation
                            for (pd in pds) {
                                if (!new CustomerTurnover(customer: customer, period: pd).saveThis()) {
                                    customer.errorMessage(code: 'customer.turnover.save', args: [customer.toString()], default: "Unable to save the turnover record for customer ${customer.toString()}")
                                    status.setRollbackOnly()
                                    valid = false
                                    break
                                }
                            }
                        } else {
                            customer.errorMessage(code: 'customer.address.usage', args: [customer.toString()], default: "Unable to save the address usage for customer ${customer.toString()}")
                            status.setRollbackOnly()
                            valid = false
                        }
                    } else {
                        customer.errorMessage(code: 'customer.address.save', args: [customer.toString()], default: "Unable to save the dummy address for customer ${customer.toString()}")
                        status.setRollbackOnly()
                        valid = false
                    }
                } else {
                    status.setRollbackOnly()
                    valid = false
                }
            }
        } finally {
            lock.unlock()
        }

        return valid
    }

    // Deletes an existing customer account. Throws an exception if the customer cannot be deleted.
    def deleteCustomer(customer) {
        def lock = getCompanyLock(customer.company)
        lock.lock()
        try {
            Customer.withTransaction {status ->
                customer.delete(flush: true)
            }
        } finally {
            lock.unlock()
        }
    }

    // Inserts a new supplier account returning true if the save was successful, false if not. If the save failed
    // then the supplier will have an error message attached to it at domain level. This method creates the required
    // turnover records associated with the supplier.
    def insertSupplier(supplier) {
        def types = SystemSupplierAddressType.list()
        def valid = true
        def lock = getCompanyLock(supplier.company)
        lock.lock()
        try {
            def pds = Period.findAllBySecurityCode(supplier.company.securityCode)
            Supplier.withTransaction {status ->
                if (supplier.saveThis()) {
                    def address = new SupplierAddress(supplier: supplier, country: supplier.country, format: supplier.country.addressFormat)
                    if (address.saveThis()) {
                        for (type in types) address.addToAddressUsages(new SupplierAddressUsage(supplier: supplier, type: type))
                        if (address.save()) {   // With deep validation
                            for (pd in pds) {
                                if (!new SupplierTurnover(supplier: supplier, period: pd).saveThis()) {
                                    supplier.errorMessage(code: 'supplier.turnover.save', args: [supplier.toString()], default: "Unable to save the turnover record for supplier ${supplier.toString()}")
                                    status.setRollbackOnly()
                                    valid = false
                                    break
                                }
                            }
                        } else {
                            supplier.errorMessage(code: 'supplier.address.usage', args: [supplier.toString()], default: "Unable to save the address usage for supplier ${supplier.toString()}")
                            status.setRollbackOnly()
                            valid = false
                        }
                    } else {
                        supplier.errorMessage(code: 'supplier.address.save', args: [supplier.toString()], default: "Unable to save the dummy address for supplier ${supplier.toString()}")
                        status.setRollbackOnly()
                        valid = false
                    }
                } else {
                    status.setRollbackOnly()
                    valid = false
                }
            }
        } finally {
            lock.unlock()
        }

        return valid
    }

    // Deletes an existing supplier account. Throws an exception if the supplier cannot be deleted.
    def deleteSupplier(supplier) {
        def lock = getCompanyLock(supplier.company)
        lock.lock()
        try {
            Supplier.withTransaction {status ->
                supplier.delete(flush: true)
            }
        } finally {
            lock.unlock()
        }
    }

    // Inserts a new accounting period returning true if the save was successful, false if not. If the save failed
    // then the period will have an error message attached to it at domain level. This method creates the required
    // balance records associated with the period
    def insertPeriod(period) {
        def session = sessionFactory.currentSession
        def valid = true
        def company = period.year.company
        def lock = getCompanyLock(company)
        lock.lock()
        try {
            def account, balance, turnover
            def accounts = Account.executeQuery('select id from Account where securityCode = ?', [company.securityCode])
            Period.withTransaction {status ->
                if (period.saveThis()) {
                    for (id in accounts) {
                        account = Account.get(id)
                        balance = new GeneralBalance(account: account, period: period)
                        if (!balance.saveThis()) {
                            period.errorMessage(code: 'account.balance.save', args: [account.toString()], default: "Unable to save the balance record(s) for account ${account.toString()}")
                            status.setRollbackOnly()
                            valid = false
                            break
                        }

                        session.evict(account)
                    }

                    if (valid) {
                        accounts = Customer.executeQuery('select id from Customer where company = ?', [company])
                        for (id in accounts) {
                            account = Customer.get(id)
                            turnover = new CustomerTurnover(customer: account, period: period)
                            if (!turnover.saveThis()) {
                                period.errorMessage(code: 'customer.turnover.save', args: [account.toString()], default: "Unable to save the turnover record for customer ${account.toString()}")
                                status.setRollbackOnly()
                                valid = false
                                break
                            }

                            session.evict(account)
                        }
                    }

                    if (valid) {
                        accounts = Supplier.executeQuery('select id from Supplier where company = ?', [company])
                        for (id in accounts) {
                            account = Supplier.get(id)
                            turnover = new SupplierTurnover(supplier: account, period: period)
                            if (!turnover.saveThis()) {
                                period.errorMessage(code: 'supplier.turnover.save', args: [account.toString()], default: "Unable to save the turnover record for supplier ${account.toString()}")
                                status.setRollbackOnly()
                                valid = false
                                break
                            }

                            session.evict(account)
                        }
                    }
                } else {
                    status.setRollbackOnly()
                    valid = false
                }
            }
        } finally {
            lock.unlock()
        }

        return valid
    }

    // Deletes an existing accounting period. Throws an exception if the period cannot be deleted.
    def deletePeriod(period) {
        def lock = getCompanyLock(period.year.company)
        lock.lock()
        try {
            Period.withTransaction {status ->
                period.delete(flush: true)
            }
        } finally {
            lock.unlock()
        }
    }

    // Deletes an accounting year. Throws an exception if the year cannot be deleted.
    def deleteYear(year) {
        def lock = getCompanyLock(year.company)
        lock.lock()
        try {
            Year.withTransaction {status ->
                year.delete(flush: true)
            }
        } finally {
            lock.unlock()
        }
    }

    // Delete a company.
    def deleteCompany(company) {

        // We need to check if the current user is a member of the given company and,
        // if so, delete the companyUser link manually to stop Hibernate trying to
        // add the link back in.
        def currentUser = utilService.currentUser()
        def companyUser
        for (member in currentUser.companies) {
            if (member.company.id == company.id) {
                companyUser = member
                break
            }
        }

        if (companyUser) {
            currentUser.removeFromCompanies(companyUser)
            companyUser.delete(flush: true)
        }

        company.delete(flush: true)
        synchronized (companyLocks) {
            companyLocks.remove(company.securityCode.toString())
        }
    }

    // Returns a list, in ascending order of validFrom dates, of all the periods of a company that have
    // a status code of 'open'
    def getOpenPeriods(company) {
        return Period.findAllBySecurityCodeAndStatus(company.securityCode, 'open', [sort: 'validFrom', cache: true])
    }

    // Returns a list, in ascending order of validFrom dates, of all the periods of a company that have
    // a status code of either 'open' or 'adjust'
    def getActivePeriods(company) {
        return Period.findAllBySecurityCodeAndStatusInList(company.securityCode, ['open', 'adjust'], [sort: 'validFrom', cache: true])
    }

    // Returns a list, in ascending order of validFrom dates, of all the periods of a company that have
    // a status code of 'closed', 'open' or 'adjust' (i.e. all except new periods that have never been opened)
    def getUsedPeriods(company) {
        return Period.findAllBySecurityCodeAndStatusInList(company.securityCode, ['open', 'adjust', 'closed'], [sort: 'validFrom', cache: true])
    }

    // Select a period (from a list of period objects in ascending date order) that contains the
    // given date. Returns the best fit if the date is before the first open period or after the
    // last open period. Returns null the list is empty.
    def selectPeriod(periods, date = new Date()) {
        if (periods) {
            date = utilService.fixDate(date)
            if (periods.size() == 1 || date < periods[0].validFrom) return periods[0]
            if (date > periods[-1].validTo) return periods[-1]
            for (period in periods) {
                if (date >= period.validFrom && date <= period.validTo) return period
            }
        }

        return null
    }

    // Check whether a user has access to a given GL account
    def hasAccountAccess(account, company = utilService.currentCompany(), user = utilService.currentUser()) {
        if (!company || !user || !account) return false

        return utilService.securityService.hasAccountAccess(utilService.cacheService, company, user, account)
    }

    // Check whether a user has access to a given code element value
    def hasCodeElementValueAccess(value, company = utilService.currentCompany(), user = utilService.currentUser()) {
        if (!company || !user || !value) return false

        return utilService.securityService.hasCodeElementValueAccess(utilService.cacheService, company, user, value)
    }

    // Gat an HQL where statement fragment limiting GL account access to those allowed by the access groups
    def createAccountAccessFragment(alias, exclude = null, createAsSQL = false, company = utilService.currentCompany(), user = utilService.currentUser()) {
        if (!company || !user || !alias) return null

        return utilService.securityService.createAccountAccessFragment(utilService.cacheService, company, user, alias, exclude, createAsSQL)
    }

    // Gat an HQL where statement fragment limiting code element value selection to those allowed by the access groups
    def createElementAccessFragment(alias, element, company = utilService.currentCompany(), user = utilService.currentUser()) {
        if (!company || !user || !alias || !element) return null

        return utilService.securityService.createElementAccessFragment(utilService.cacheService, company, user, alias, element)
    }

    // Check whether a user has access to a given customer account
    def hasCustomerAccess(customer, company = utilService.currentCompany(), user = utilService.currentUser()) {
        if (!company || !user || !customer?.accessCode?.code) return false

        return utilService.securityService.hasCustomerAccessCode(utilService.cacheService, company, user, customer.accessCode.code)
    }

    // Check whether a user has access to a given customer access code
    def hasCustomerAccessCode(code, company = utilService.currentCompany(), user = utilService.currentUser()) {
        if (!company || !user || !code) return false

        return utilService.securityService.hasCustomerAccessCode(utilService.cacheService, company, user, code)
    }

    // Returns a list of the customer access code objects that the current company user may use
    def customerAccessCodes(company = utilService.currentCompany(), user = utilService.currentUser()) {
        return utilService.securityService.customerAccessCodes(utilService.cacheService, company, user)
    }

    // Check whether a user has access to a given supplier account
    def hasSupplierAccess(supplier, company = utilService.currentCompany(), user = utilService.currentUser()) {
        if (!company || !user || !supplier?.accessCode?.code) return false

        return utilService.securityService.hasSupplierAccessCode(utilService.cacheService, company, user, supplier.accessCode.code)
    }

    // Check whether a user has access to a given supplier access code
    def hasSupplierAccessCode(code, company = utilService.currentCompany(), user = utilService.currentUser()) {
        if (!company || !user || !code) return false

        return utilService.securityService.hasSupplierAccessCode(utilService.cacheService, company, user, code)
    }

    // Returns a list of the supplier access code objects that the current company user may use
    def supplierAccessCodes(company = utilService.currentCompany(), user = utilService.currentUser()) {
        return utilService.securityService.supplierAccessCodes(utilService.cacheService, company, user)
    }

    // Returns a displayable account value (a BigDecimal) from a bookkeeping object. May return null and may also
    // return a string of '######' if a foreign exchange rate cannot be found. The attrs must contain a context
    // key that is one of a Customer, Supplier, Account or Document object. One of the following may also be passed in:
    //
    //      line        - A GeneralTransaction object from which to get the values
    //      balance     - A GeneralBalance object from which to get the values
    //      turnover    - A CustomerTurnover or SupplierTurnover object from which to get the values
    //      allocation  - An allocation object from which to get the allocated value
    //
    // If none of the above three keys is present, the values will be pulled from the context object. The value to be
    // retrieved is specified by a field key. For each type of object that the method can get values from, the possible
    // field values are as follows:
    //
    //      Customer/Supplier   - 'balance'         - The current balance on the account
    //                          - 'limit'           - The credit limit of the account (always set to zero decimal places)
    //      GeneralTransaction  - 'value'           - The line value posted to the account
    //                          - 'tax'             - The line tax value
    //                          - 'total'           - The line total value (posted value + tax)
    //                          - 'nett'            - Special case for total lines where 'goods' is actually 'value' minus 'tax'
    //                          - 'unallocated'     - The unallocated amount of the line
    //      GeneralBalance      - 'opening'         - The opening balance
    //                          - 'closing'         - The closing balance
    //                          - 'transactions'    - The total of transactions
    //                          - 'adjustments'     - The total of adjustments
    //                          - 'budget'          - The budget value (always set to zero decimal places)
    //                          - 'budgetYTD'       - A pseudo field that is the cumulative budget, year-to-date
    //      xxxxTurnover        - 'turnover'        - The turnover value
    //      allocation          - 'value'           - The allocated value
    //
    // Other keys in the attrs parameter can be as follows:
    //
    //      currency            - The ExchangeCurrency object to return the value in. If null, uses the currency of the context
    //      negate              - If true, negates the value before returning it
    //      scale               - The number of decimal digits to use. If null, uses the displayCurrency decimal places.
    //      zeroIsNull          - Returns null if the value is zero
    //      zeroIsText          - Returns the given text (HTML encoded) if the value is zero
    //      zeroIsHTML          - Returns the given HTML fragment if the value is zero
    //
    // The negate and zeroIsNull values can either be booleans or String versions of a boolean (i.e. 'true' or 'false').
    // Similarly, the scale value can be either an integer or the string representation of an integer. The seroIsText
    // and zeroIsHTML should attributes should contains String data. The three 'zeroIs' attributes are evaluated in the
    // order shown.
    def getBookValue(attrs) {
        def value
        if (attrs.field && attrs.context) {
            def context = attrs.context
            def contextCurrency = context.currency
            def field = attrs.field
            def companyCurrency = utilService.companyCurrency()
            def negate = attrs.negate
            if (negate != null && negate instanceof String) negate = negate.equalsIgnoreCase('true')
            def zeroIsNull = attrs.zeroIsNull
            if (zeroIsNull != null && zeroIsNull instanceof String) zeroIsNull = zeroIsNull.equalsIgnoreCase('true')
            def displayCurrency = attrs.currency
            if (!displayCurrency) {
                displayCurrency = contextCurrency
                attrs.currency = displayCurrency
            }

            if (attrs.scale == null) {
                attrs.scale = displayCurrency.decimals
            } else if (attrs.scale instanceof String) {
                attrs.scale = attrs.scale.toInteger()
            }

            if (attrs.line) {       // A GL transaction line
                def line = attrs.line
                switch (field) {
                    case 'value':
                        value = getLineValue(context, line, contextCurrency, displayCurrency, companyCurrency,
                            line.documentValue, line.accountValue, line.generalValue, line.companyValue)
                        break

                    case 'tax':
                        value = getLineValue(context, line, contextCurrency, displayCurrency, companyCurrency,
                            line.documentTax, line.accountTax, line.generalTax, line.companyTax)
                        break

                    case 'total':
                        value = getLineValue(context, line, contextCurrency, displayCurrency, companyCurrency,
                            makeTotal(line.documentValue, line.documentTax), makeTotal(line.accountValue, line.accountTax),
                            makeTotal(line.generalValue, line.generalTax), makeTotal(line.companyValue, line.companyTax))
                        break

                    case 'nett':
                        value = getLineValue(context, line, contextCurrency, displayCurrency, companyCurrency,
                            makeNett(line.documentValue, line.documentTax), makeNett(line.accountValue, line.accountTax),
                            makeNett(line.generalValue, line.generalTax), makeNett(line.companyValue, line.companyTax))
                        break

                    case 'unallocated':
                        if (displayCurrency.id == companyCurrency.id) {     // A display in company currency is 'absolute'
                            value = line.companyUnallocated
                        } else {    // Not displaying in company currency

                            // If the context isn't a sub-ledger account, need to set the context currency to the sub-ledger account currency
                            if (!(context instanceof Customer) && !(context instanceof Supplier)) contextCurrency = line.customer ? line.customer.currency : line.supplier.currency
                            if (displayCurrency.id == contextCurrency.id) { // If displaying in the account currency
                                value = line.accountUnallocated
                            } else if (!line.companyUnallocated) {  // Will need a conversion from company currency, but don't bother if the answer is zero or nulll
                                value = line.companyUnallocated
                            } else if (line.accountUnallocated == line.accountValue && context instanceof Document && displayCurrency.id == context.currency.id) {
                                value = line.documentValue
                            } else {
                                value = utilService.convertCurrency(companyCurrency, displayCurrency, line.companyUnallocated)
                                if (value == null) value = '######'
                            }
                        }
                        break

                    case 'allocated':
                        if (displayCurrency.id == companyCurrency.id) {     // A display in company currency is 'absolute'
                            value = makeNett(line.companyValue, line.companyUnallocated)
                        } else {    // Not displaying in company currency
                            // If the context isn't a sub-ledger account, need to set the context currency to the sub-ledger account currency
                            if (!(context instanceof Customer) && !(context instanceof Supplier)) contextCurrency = line.customer ? line.customer.currency : line.supplier.currency
                            if (displayCurrency.id == contextCurrency.id) { // If displaying in the account currency
                                value = makeNett(line.accountValue, line.accountUnallocated)
                            } else if (!line.companyUnallocated) {  // Will need a conversion from company currency, but don't bother if the answer is the line companyValue
                                value = line.companyValue
                            } else if (line.accountUnallocated == 0.0 && context instanceof Document && displayCurrency.id == context.currency.id) {
                                value = line.documentValue
                            } else {
                                value = utilService.convertCurrency(companyCurrency, displayCurrency, makeNett(line.companyValue, line.companyUnallocated))
                                if (value == null) value = '######'
                            }
                        }
                        break
                }
            } else if (attrs.balance) {     // A GL balance record
                def balance = attrs.balance
                switch (field) {
                    case 'opening':
                        if (displayCurrency.id == contextCurrency.id) {
                            value = balance.generalOpeningBalance
                        } else if (displayCurrency.id == companyCurrency.id) {
                            value = balance.companyOpeningBalance
                        } else {
                            value = utilService.convertCurrency(companyCurrency, displayCurrency, balance.companyOpeningBalance)
                            if (value == null) value = '######'
                        }
                        break

                    case 'closing':
                        if (displayCurrency.id == contextCurrency.id) {
                            value = balance.generalClosingBalance
                        } else if (displayCurrency.id == companyCurrency.id) {
                            value = balance.companyClosingBalance
                        } else {
                            value = utilService.convertCurrency(companyCurrency, displayCurrency, balance.companyClosingBalance)
                            if (value == null) value = '######'
                        }
                        break

                    case 'transactions':
                        if (displayCurrency.id == contextCurrency.id) {
                            value = balance.generalTransactionTotal
                        } else if (displayCurrency.id == companyCurrency.id) {
                            value = balance.companyTransactionTotal
                        } else {
                            value = utilService.convertCurrency(companyCurrency, displayCurrency, balance.companyTransactionTotal)
                            if (value == null) value = '######'
                        }
                        break

                    case 'adjustments':
                        if (displayCurrency.id == contextCurrency.id) {
                            value = balance.generalAdjustmentTotal
                        } else if (displayCurrency.id == companyCurrency.id) {
                            value = balance.companyAdjustmentTotal
                        } else {
                            value = utilService.convertCurrency(companyCurrency, displayCurrency, balance.companyAdjustmentTotal)
                            if (value == null) value = '######'
                        }
                        break

                    case 'budget':
                        attrs.scale = 0
                        if (displayCurrency.id == contextCurrency.id) {
                            value = balance.generalBudget
                        } else if (displayCurrency.id == companyCurrency.id) {
                            value = balance.companyBudget
                        } else {
                            value = utilService.convertCurrency(companyCurrency, displayCurrency, balance.companyBudget)
                            if (value == null) value = '######'
                        }
                        break

                    case 'budgetYTD':       // A pseudo field for the year-to-date budget
                        attrs.scale = 0
                        value = GeneralBalance.executeQuery('select sum(x.' + ((displayCurrency.id == contextCurrency.id) ? 'generalBudget' : 'companyBudget') +
                                ') from GeneralBalance as x where x.account = ? and x.period.year = ? and x.period.validFrom <= ?',
                                [balance.account, balance.period.year, balance.period.validFrom])[0]
                        if (displayCurrency.id != contextCurrency.id && displayCurrency.id != companyCurrency.id) {
                            value = utilService.convertCurrency(companyCurrency, displayCurrency, value)
                            if (value == null) value = '######'
                        }
                        break
                }

            } else if (attrs.turnover) {    // A customer or supplier turnover record
                if (field == 'turnover') {
                    def turnover = attrs.turnover
                    if (displayCurrency.id == contextCurrency.id) {
                        value = turnover.accountTurnover
                    } else if (displayCurrency.id == companyCurrency.id) {
                        value = turnover.companyTurnover
                    } else {
                        value = utilService.convertCurrency(companyCurrency, displayCurrency, turnover.companyTurnover)
                        if (value == null) value = '######'
                    }
                }
            } else if (attrs.allocation) {    // A customer or supplier allocation record
                if (field == 'value') {
                    def allocation = attrs.allocation
                    if (displayCurrency.id == companyCurrency.id) {
                        value = allocation.companyValue
                    } else if (displayCurrency.id == contextCurrency.id) {
                        value = allocation.accountValue
                    } else if (!allocation.companyValue) {
                        value = allocation.companyValue
                    } else {
                        value = utilService.convertCurrency(companyCurrency, displayCurrency, allocation.companyValue)
                        if (value == null) value = '######'
                    }
                }
            } else {    // Customer or supplier account
                switch (field) {
                    case 'balance':
                        if (displayCurrency.id == contextCurrency.id) {
                            value = context.accountCurrentBalance
                        } else if (displayCurrency.id == companyCurrency.id) {
                            value = context.companyCurrentBalance
                        } else {
                            value = utilService.convertCurrency(companyCurrency, displayCurrency, context.companyCurrentBalance)
                            if (value == null) value = '######'
                        }
                        break

                    case 'limit':
                        attrs.scale = 0
                        if (displayCurrency.id == contextCurrency.id) {
                            value = context.accountCreditLimit
                        } else {
                            value = utilService.convertCurrency(contextCurrency, displayCurrency, context.accountCreditLimit)
                            if (value == null) value = '######'
                        }
                        break
                }
            }

            if (value != null && !(value instanceof String)) {
                if (negate && value) value = -value
                value = utilService.round(value, attrs.scale)
                if (value == 0.0) {
                    if (zeroIsNull) {
                        value = null
                    } else if (attrs.zeroIsText != null) {
                        value = attrs.zeroIsText.encodeAsHTML()
                    } else if (attrs.zeroIsHTML != null) {
                        value = attrs.zeroIsHTML
                    }
                }
            }
        }

        return value
    }

    // Centralized method for loading the model of a document as needed by the enquiry system
    def loadDocumentModel(params, docTypes) {
        params.max = utilService.max
        params.offset = utilService.offset
        def documentInstance = new Document()
        def analysisIsDebit
        def totalInstance
        def currencyList = ExchangeCurrency.findAllByCompany(utilService.currentCompany(), [cache: true])
        def displayCurrency = (params.displayCurrency && params.displayCurrency != 'null') ? ExchangeCurrency.get(params.displayCurrency) : null
        def displayCurrencyClass = ''
        def dtl
        for (dt in docTypes) {
            if (dtl) {
                dtl += ", '${dt}'"
            } else {
                dtl = "'${dt}'"
            }
        }

        def documentTypeList = DocumentType.findAll('from DocumentType as dt where dt.company = ? and dt.type.code in (' + dtl + ')', [utilService.currentCompany()])
        if (params.type?.id && params.code) {
            def type = DocumentType.get(params.type.id)
            documentInstance = Document.findByTypeAndCode(type, params.code)
            if (!documentInstance) {
                documentInstance = new Document()
                documentInstance.type = type
                documentInstance.code = params.code
                documentInstance.errorMessage(field: 'code', code: 'document.invalid', default: 'Invalid document')
            }
        } else if (params.id) {
            documentInstance = Document.get(params.id)
            if (!documentInstance) {
                documentInstance = new Document()
                documentInstance.errorMessage(field: 'code', code: 'document.invalid', default: 'Invalid document')
            }
        }

        if (documentInstance.id) {
            def sysType = documentInstance.type.type
            if (documentInstance.securityCode == utilService.currentCompany().securityCode && docTypes.contains(sysType.code) && utilService.permitted(sysType.activity.code)) {
                analysisIsDebit = sysType.analysisIsDebit
                if (displayCurrency && displayCurrency.id != documentInstance.currency.id &&
                    displayCurrency.id != utilService.companyCurrency().id && displayCurrency.id != getSubLedgerCurrencyId(documentInstance)) {
                    displayCurrencyClass = 'conversion'
                }
            } else {
                def type = documentInstance.type
                def code = documentInstance.code
                documentInstance = new Document()
                documentInstance.type = type
                documentInstance.code = code
                documentInstance.errorMessage(field: 'code', code: 'document.invalid', default: 'Invalid document')
            }
        }

        [documentInstance: documentInstance, analysisIsDebit: analysisIsDebit, documentTypeList: documentTypeList,
                currencyList: currencyList, displayCurrency: displayCurrency, displayCurrencyClass: displayCurrencyClass]
    }

    // Returns the total line of a document, if any.
    def getTotalLine(document) {
        if (document) {
            for (tot in document.total) {
                return tot      // At most one occurrence
            }
        }

        return null
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private getLineValue(context, line, contextCurrency, displayCurrency, companyCurrency, documentVal, accountVal, generalVal, companyVal) {

        // If they want the company currency, just return it
        if (displayCurrency.id == companyCurrency.id) return companyVal

        if (context instanceof Document) {
            if (displayCurrency.id == contextCurrency.id) return documentVal
            if (displayCurrency.id == getSubLedgerCurrencyId(context)) return accountVal
        } else if (context instanceof Account) {     // The context is a GL account
            if (displayCurrency.id == contextCurrency.id) return generalVal
        } else {    // The context must be a supplier or customer account
            if (displayCurrency.id == contextCurrency.id) return accountVal
        }

        if (companyVal == null) return null

        def value = utilService.convertCurrency(companyCurrency, displayCurrency, companyVal)
        return (value == null) ? '######' : value
    }

    private getSubLedgerCurrencyId(document) {
        def currencyId = 0L
        def totalLine = getTotalLine(document)
        if (totalLine) {
            if (totalLine.supplier) {
                currencyId = totalLine.supplier.currency.id
            } else if (totalLine.customer) {
                currencyId = totalLine.customer.currency.id
            }
        } else {
            for (line in document.lines) {
                if (line.supplier) {
                    if (!currencyId) {
                        currencyId = line.supplier.currency.id
                    } else if (currencyId != line.supplier.currency.id) {
                        currencyId = 0L
                        break
                    }
                } else if (line.customer) {
                    if (!currencyId) {
                        currencyId = line.customer.currency.id
                    } else if (currencyId != line.customer.currency.id) {
                        currencyId = 0L
                        break
                    }
                }
            }
        }

        return currencyId
    }

    // Add two numbers together when either or both of them can be null. Returns null if both values are null
    private makeTotal(val1, val2) {
        if (val1 != null && val2 != null) return val1 + val2
        return (val1 != null) ? val1 : val2
    }

    // Subtract the second number from the first number when either or both of them can be null. Returns null if both values are null
    private makeNett(val1, val2) {
        if (val1 != null && val2 != null) return val1 - val2
        return (val2 == null) ? val1 : -val2
    }

    // Get a mnemonic substitution for a specific user
    private getMnemonic(user, code) {

        // Try and get it from the cache
        def val = utilService.cacheService.get('mnemonic', 0L, user.id.toString() + utilService.cacheService.IMPOSSIBLE_VALUE + code)

        // If we've not looked for this before
        if (val == null) {
            val = Mnemonic.findByUserAndCode(user, code)?.accountCodeFragment
            utilService.cacheService.put('mnemonic', 0L, user.id.toString() + utilService.cacheService.IMPOSSIBLE_VALUE + code, val)
        }

        return (val == utilService.cacheService.IMPOSSIBLE_VALUE) ? null : val
    }

    private matchesPattern(pattern, val) {
        if (pattern.length() != val.length()) return false
        if (isNumeric(val)) {
            if (pattern.startsWith('#') || isNumeric(pattern)) return true
        } else {
            if (pattern.startsWith('@') || isAlphabetic(pattern)) return true
        }

        return false
    }

    private findMismatch(pattern, account, pos) {
        while (pos < pattern.size() && pos < account.size()) {
            if (!matchesPattern(pattern[pos], account[pos])) return pos
            pos++
        }

        return -1
    }

    private findMatch(pattern, account, pos) {
        def patternPos = pos + 1
        while (patternPos < pattern.size()) {
            if (matchesPattern(pattern[patternPos], account[pos])) return patternPos
            patternPos++
        }

        return -1
    }

    private static getForcedLocale() {
        try {
            return RequestContextUtils.getLocale(RequestContextHolder.currentRequestAttributes().getCurrentRequest())
        } catch (Exception ex) {
            return Locale.US
        }
    }
}
