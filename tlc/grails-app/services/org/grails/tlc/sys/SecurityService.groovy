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

import grails.util.GrailsNameUtils
import org.grails.tlc.books.AccessCode
import org.grails.tlc.books.AccessGroup
import org.grails.tlc.books.CodeElement
import org.grails.tlc.books.CodeRange
import org.grails.tlc.corp.CompanyUser
import org.apache.log4j.Logger
import org.codehaus.groovy.grails.commons.ControllerArtefactHandler
import org.codehaus.groovy.grails.commons.DomainClassArtefactHandler

class SecurityService {

    static transactional = false

    // Return the activity code for a given controller/action combination
    def getActionActivity(cacheService, controller, action) {
        def activity = cacheService.get('actionActivity', 0L, "${controller}.${action}")
        if (activity == null) {
            def actn = SystemAction.findByAppControllerAndAppAction(controller, action)
            activity = actn?.activity?.code ?: CacheService.IMPOSSIBLE_VALUE
            cacheService.put('actionActivity', 0L, "${controller}.${action}", activity)
        }

        return (activity == CacheService.IMPOSSIBLE_VALUE) ? null : activity
    }

    // Return true if the given company/user combination has the specified activity code
    def isUserActivity(cacheService, company, user, activity) {

        // Since the user is both logged in and attached to a company, any of the system activities
        // other than sysadmin are allowed - plus a system administrator is allowed full access.
        if (user.administrator || activity == 'any' || activity == 'logout' || activity == 'login' || activity == 'attached') return true

        // Since we checked if they were an administrator above, it must be a security
        // violation if the required activity is the system activity sysadmin
        if (activity == 'sysadmin') return false

        // It's nothing special so we need to check it out
        def result = cacheService.get('userActivity', company.securityCode, user.id.toString() + CacheService.IMPOSSIBLE_VALUE + activity)
        if (result == null) {
            def activities = SystemActivity.findAll('from SystemActivity as w where w.id in (select z.id from CompanyUser as x join x.roles as y join y.activities as z where x.company.id = ? and x.user.id = ? and z.code = ?)', [company.id, user.id, activity], [max: 1])
            result = (activities.size() > 0) ? CacheService.DUMMY_VALUE : CacheService.IMPOSSIBLE_VALUE
            cacheService.put('userActivity', company.securityCode, user.id.toString() + CacheService.IMPOSSIBLE_VALUE + activity, result)
        }

        return (result == CacheService.DUMMY_VALUE)
    }

    // Check whether a user has access to a given GL account
    def hasAccountAccess(cacheService, company, user, account) {
        if (user.administrator || isUserActivity(cacheService, company, user, 'coadmin') || isUserActivity(cacheService, company, user, 'actadmin')) return true      // Full access
        def key = user.id.toString() + CacheService.IMPOSSIBLE_VALUE + account.code
        def result = cacheService.get('userAccount', company.securityCode, key)
        if (result == null) {
            def groups = getUserAccessGroups(cacheService, company, user)
            if (groups) {
                result = CacheService.DUMMY_VALUE  // Assume access is allowed
                for (int i = 1; i < 9; i++) {
                    def value = account."element${i}"?.code
                    if (value) {
                        def found = false
                        def element = "e${i}"
                        for (group in groups) {
                            if (hasAccess(value, group?.get(element))) {
                                found = true
                                break
                            }
                        }

                        if (!found) {
                            result = CacheService.IMPOSSIBLE_VALUE  // Access is not allowed
                            break
                        }
                    }
                }
            } else {
                result = CacheService.IMPOSSIBLE_VALUE  // Access is not allowed
            }

            cacheService.put('userAccount', company.securityCode, key, result)
        }

        return (result == CacheService.DUMMY_VALUE)
    }

    // Checks whether the user has access to a given CodeElementValue
    def hasCodeElementValueAccess(cacheService, company, user, value) {
        if (user.administrator || isUserActivity(cacheService, company, user, 'coadmin') || isUserActivity(cacheService, company, user, 'actadmin')) return true      // Full access
        def groups = getUserAccessGroups(cacheService, company, user)
        if (!groups) return false   // No access groups means access not allowed
        def element = "e${value.element.elementNumber}"
        def code = value.code
        for (group in groups) {
            if (hasAccess(code, group?.get(element))) return true   // This group gives permission
        }

        return false    // No group gave permission
    }

    // Creates an HQL fragment that concicts of one or more tests to restrict a selection
    // (or count) of GL accounts to those permitted by the user's access groups. The
    // intended use is such as...
    //
    // def fragment = bookService.createAccountAccessFragment('x')
    // if (fragment == null) return []
    // def sql = 'from Account as x where x.securityCode = ?'
    // if (fragment) sql += ' and ' + fragment
    // return Account.findAll(sql + ' order by x.code', [utilService.currentCompany().securityCode])
    //
    // NOTE that the bookService encapsulates this method and can automatically fill in
    // the first three parameters for us.
    //
    // The required alias parameter is a String that is added to the front of each field
    // reference. For example, if you pass the alias as 'x' then references to element1.code
    // would be modified to be x.element.code and so forth. You must supply an alias.
    //
    // The optional exclude parameter can be either be a single item or a list of items
    // where each item can be either a CodeElement object or a number equivalent to a
    // CodeElement.elementNumber property (an Integer will do, it doesn't have to be a
    // Byte). Elements thus identified in this way will NOT be subjected to testing. This
    // can be useful where the user has specified a specific element value in which case
    // there is no point in testing that element further.
    //
    // The optional createAsSQL, if set to true will return SQL rather than HQL. This is used
    // by reports. The default is to return HQL.
    //
    // Returns null if the user has no permissions, nullstring if no tests are required
    // or the tests as an HQL fragment. The HQL fragment can be treated as a single test
    // in that it will be surrounded by parentheses where necessary.
    def createAccountAccessFragment(cacheService, company, user, alias, exclude = null, createAsSQL = false) {
        if (user.administrator || isUserActivity(cacheService, company, user, 'coadmin') || isUserActivity(cacheService, company, user, 'actadmin')) return ''  // Full access
        def elements = CodeElement.findAllByCompany(company)
        if (!elements) return null  // No elements defined yet
        def accessGroups = getUserAccessGroups(cacheService, company, user)
        if (!accessGroups) return null      // User has no access groups
        def exclusions = []
        if (exclude) {
            if (!(exclude instanceof List)) exclude = [exclude]
            for (int i = 0; i < exclude.size(); i++) {
                if (exclude[i] instanceof CodeElement) {
                    exclusions << exclude[i].elementNumber
                } else {
                    exclusions << (byte) exclude[i]
                }
            }
        }

        if (!alias.endsWith('.')) alias = alias + '.'
        def elementTests = ''
        def test
        def needsParentheses = false
        def groupTests, tests
        for (element in elements) {
            if (exclusions.contains(element.elementNumber)) continue     // They specifically excluded this element from testing
            tests = []
            for (group in accessGroups) {
                groupTests = group?.get("e${element.elementNumber}")    // Group could be missing
                if (groupTests) tests.addAll(groupTests)
            }

            test = createElementHQL(element, tests, alias, createAsSQL)
            if (test) {
                if (elementTests) {
                    elementTests += ' and '
                    needsParentheses = true
                }

                elementTests += test
            }
        }

        return needsParentheses ? '(' + elementTests + ')' : elementTests
    }

    // This method is similar to the createAccountAccessFragment method except that it creates the HQL
    // where clause fragment for selecting the code element values for a particular code element that
    // a user is allowed to access rather than the accounts that a user can access. Consequently there
    // is no need for an exclude parameter but, in its place, you must supply the code element object
    // to create the fragment for. The ntended use is such as...
    //
    // def fragment = bookService.createElementAccessFragment('x', element)
    // if (fragment == null) return []
    // def sql = 'from CodeElementValue as x where x.element = ?'
    // if (fragment) sql += ' and ' + fragment
    // return CodeElementValue.findAll(sql + ' order by x.code', [element])
    //
    // NOTE that the bookService encapsulates this method and can automatically fill in
    // the first three parameters for us.
    //
    // Returns null if the user has no permissions, nullstring if no tests are required
    // or the tests as an HQL fragment. The HQL fragment can be treated as a single test
    // in that it will be surrounded by parentheses where necessary.
    def createElementAccessFragment(cacheService, company, user, alias, element) {
        if (user.administrator || isUserActivity(cacheService, company, user, 'coadmin') || isUserActivity(cacheService, company, user, 'actadmin')) return ''    // Full access
        def accessGroups = getUserAccessGroups(cacheService, company, user)
        if (!accessGroups) return null      // User has no access groups
        if (!alias.endsWith('.')) alias = alias + '.'
        def groupTests
        def tests = []
        for (group in accessGroups) {
            groupTests = group?.get("e${element.elementNumber}")    // Group could be missing
            if (groupTests) tests.addAll(groupTests)
        }

        if (!tests) return null     // No tests means no permission for this element

        return createElementValueHQL(element, tests, alias)
    }

    // Check whether a user has access to a given customer account
    def hasCustomerAccess(cacheService, company, user, customer) {
        return hasCustomerAccessCode(cacheService, company, user, customer.accessCode.code)
    }

    // Check whether a user has access to a given customer access code
    def hasCustomerAccessCode(cacheService, company, user, code) {
        if (user.administrator || isUserActivity(cacheService, company, user, 'coadmin')) return true
        def key = user.id.toString() + CacheService.IMPOSSIBLE_VALUE + code
        def result = cacheService.get('userCustomer', company.securityCode, key)
        if (result == null) {
            result = CacheService.IMPOSSIBLE_VALUE  // Assume access is not allowed
            def groups = getUserAccessGroups(cacheService, company, user)
            if (groups) {
                for (group in groups) {
                    if (hasAccess(code, group?.get('ar'))) {
                        result = CacheService.DUMMY_VALUE  // Access is allowed
                        break
                    }
                }
            }

            cacheService.put('userCustomer', company.securityCode, key, result)
        }

        return (result == CacheService.DUMMY_VALUE)
    }

    // Returns a list of the customer access codes objects that the given company user may use
    def customerAccessCodes(cacheService, company, user) {
        def codes = AccessCode.findAllByCompany(company, [sort: 'name', cache: true])
        if (user.administrator || isUserActivity(cacheService, company, user, 'coadmin')) return codes
        def allowed = []
        for (item in codes) {
            if (hasCustomerAccessCode(cacheService, company, user, item.code)) allowed << item
        }

        return allowed
    }

    // Check whether a user has access to a given supplier account
    def hasSupplierAccess(cacheService, company, user, supplier) {
        return hasSupplierAccessCode(cacheService, company, user, supplier.accessCode.code)
    }

    // Check whether a user has access to a given supplier access code
    def hasSupplierAccessCode(cacheService, company, user, code) {
        if (user.administrator || isUserActivity(cacheService, company, user, 'coadmin')) return true
        def key = user.id.toString() + CacheService.IMPOSSIBLE_VALUE + code
        def result = cacheService.get('userSupplier', company.securityCode, key)
        if (result == null) {
            result = CacheService.IMPOSSIBLE_VALUE  // Assume access is not allowed
            def groups = getUserAccessGroups(cacheService, company, user)
            if (groups) {
                for (group in groups) {
                    if (hasAccess(code, group?.get('ap'))) {
                        result = CacheService.DUMMY_VALUE  // Access is allowed
                        break
                    }
                }
            }

            cacheService.put('userSupplier', company.securityCode, key, result)
        }

        return (result == CacheService.DUMMY_VALUE)
    }

    // Returns a list of the supplier access codes objects that the given company user may use
    def supplierAccessCodes(cacheService, company, user) {
        def codes = AccessCode.findAllByCompany(company, [sort: 'name', cache: true])
        if (user.administrator || isUserActivity(cacheService, company, user, 'coadmin')) return codes
        def allowed = []
        for (item in codes) {
            if (hasSupplierAccessCode(cacheService, company, user, item.code)) allowed << item
        }

        return allowed
    }

    // Get the access groups (as a list of maps) for a given user in the specified company.
    def getUserAccessGroups(cacheService, company, user) {

        // Get a list of the access group codes the user belongs to
        def groups = cacheService.get('userAccessGroup', company.securityCode, user.id.toString())
        if (groups == null) {
            def results = loadUserAccessGroups(company, user)
            def size = results[0]
            groups = results[1]
            cacheService.put('userAccessGroup', company.securityCode, user.id.toString(), groups, size)
        }

        if (groups == CacheService.IMPOSSIBLE_VALUE) return null

        // Get a list (one element for each group they belong to) of the permissions
        // the groups endow the user with. Each element in the list is a map of tests
        // where the keys are 'ar', 'ap' and 'e1' through 'e8' for the accounts payable,
        // accounts receivable and general ledger elements respectively. The corresponding
        // values in the map are lists of tests where each element in that list is itself
        // a two element list consisting of a 'from' and 'to' value (both of which are
        // inclusive). These test ranges can have a from and/or to value of '*' signifying
        // from and/or to any value. The from and to test values are Strings since all
        // testing is done using String values.
        def maps = []
        for (group in groups) {
            maps << getAccessGroup(cacheService, company, group)
        }

        return maps
    }

    // --------------------------------------------- Static Methods ----------------------------------------------

    // Used on startup to ensure we have an activity for each and every
    // controller.action combination. Also logs any undesirable situations
    // such as missing defaults etc.
    static syncActionActivities(grailsApplication) {
        Logger log = Logger.getLogger(SecurityService)
        def activities

        // Work through each controller in the system
        for (ctrl in grailsApplication.getArtefacts(ControllerArtefactHandler.TYPE)) {
            def controller = GrailsNameUtils.getPropertyName(ctrl.name)

            // See if the controller has any activities defined and, if so, grab them
            try {
                activities = ctrl.newInstance().activities
            } catch (Exception ex) {
                activities = null
            }

            // If activities have been defined
            if (activities) {

                // See if there is a default and warn them if not
                def dflt = activities.remove('default')
                if (!dflt) log.warn("Controller ${controller} has no default activity defined")

                // Get all URLs this controller responds to
                def uris = ctrl.getURIs()

                // Parse the controller's available actions from the list of URLs
                def pos = controller.length() + 2
                for (url in uris) {

                    // Only interested in URLs with the format /controller/action
                    if (url.length() > pos && url.indexOf('/', pos) == -1) {
                        def action = url.substring(pos)
                        def activity = activities.remove(action) ?: dflt
                        if (activity) {

                            // Ensure a system activity exists for this code
                            def acty = SystemActivity.findByCode(activity)
                            if (!acty) {
                                acty = new SystemActivity(code: activity)
                                if (acty.saveThis()) {
                                    log.info("Added activity ${activity}")
                                } else {
                                    log.error("Unable to create activity ${activity}")
                                    acty = null
                                }
                            }

                            // If we managed to find or create a system activity record
                            if (acty) {

                                // Try and get the action record
                                def actn = SystemAction.findByAppControllerAndAppAction(controller, action)
                                if (actn) {

                                    // If the action has changed its activity
                                    if (acty.id != actn.activity.id) {

                                        // Remember the old activity code
                                        def oldActivity = actn.activity.code

                                        actn.activity = acty
                                        if (actn.saveThis()) {
                                            log.info("Changed controller ${controller}, action ${action} from activity ${oldActivity} to activity ${activity}")
                                        } else {
                                            log.error("Unable to change controller ${controller}, action ${action} to activity ${activity}")
                                        }
                                    }
                                } else {    // A new controller.action record needed
                                    actn = new SystemAction(appController: controller, appAction: action)
                                    acty.addToActions(actn)
                                    if (acty.save()) {      // With deep validation
                                        log.info("Controller ${controller}, action ${action} added with activity of ${activity}")
                                    } else {
                                        log.error("Unable to add controller ${controller}, action ${action} with activity of ${activity}")
                                    }
                                }
                            }
                        } else {
                            log.error("Controller ${controller}, action ${action} has no activity")
                        }
                    }
                }

                for (it in activities) log.error("Controller ${controller} refers to a non-existent action ${it.key} with an activity of ${it.value}")
            } else {    // No activities defined
                log.error("Controller ${controller} has no activities defined")
            }
        }
    }

    // Ensure the company administrator role has all non-system
    // permissions other than the special systran activity
    static syncCompanyAdminRole() {

        def role = SystemRole.findByCode('companyAdmin')
        if (role) {
            def allowedActivities = SystemActivity.findAllBySystemOnlyAndCodeNotEqual(false, 'systran')
            def disallowedActivities = []
            for (activity in role.activities) if (!allowedActivities.remove(activity)) disallowedActivities << activity
            if (allowedActivities || disallowedActivities) {
                for (activity in allowedActivities) role.addToActivities(activity)
                for (activity in disallowedActivities) role.removeFromActivities(activity)
                role.save()     // With deep validation
            }
        }
    }

    // Set the tracing statuses for a domain class
    static setTracing(domainName, insertSecurityCode, updateSecurityCode, deleteSecurityCode) {
        def domain = UtilService.getGrailsDomainClass(domainName)
        if (domain) {
            try {
                def dom = domain.newInstance()
                dom.traceInsertCode.set(insertSecurityCode)
                dom.traceUpdateCode.set(updateSecurityCode)
                dom.traceDeleteCode.set(deleteSecurityCode)
            } catch (Exception ex) {
                Logger.getLogger(SecurityService).error("Attempt to modify tracing data failed for domain class ${domainName} (${ex.simpleName})")
            }
        } else {
            Logger.getLogger(SecurityService).error("Attempt to modify tracing data for non-existent domain class ${domainName}")
        }
    }

    // Bring the domain tracing info up to date in the database
    static syncTracingDomains(grailsApplication) {

        // Work through each domain class in the system
        for (dom in grailsApplication.getArtefacts(DomainClassArtefactHandler.TYPE)) {
            def domain = dom.newInstance()

            if (domain.class.simpleName != 'SystemTrace' && domain.class.simpleName != 'SystemWorkarea') {
                def traceable

                // See if the domain has the three required fields
                try {
                    traceable = domain.traceInsertCode
                    traceable = domain.traceUpdateCode
                    traceable = domain.traceDeleteCode
                } catch (Exception ex) {
                    traceable = null
                    Logger.getLogger(SecurityService).warn("Untraceable domain: ${domain.class.simpleName}")
                }

                def systemTracingInstance = SystemTracing.findByDomainName(domain.class.simpleName)
                if (systemTracingInstance) {
                    if (traceable == null) {
                        systemTracingInstance.delete(flush: true)
                    } else {
                        if (systemTracingInstance.insertSecurityCode != UtilService.TRACE_NONE ||
                            systemTracingInstance.updateSecurityCode != UtilService.TRACE_NONE ||
                            systemTracingInstance.deleteSecurityCode != UtilService.TRACE_NONE) {
                            setTracing(systemTracingInstance.domainName, systemTracingInstance.insertSecurityCode, systemTracingInstance.updateSecurityCode, systemTracingInstance.deleteSecurityCode)
                        }
                    }
                } else {
                    if (traceable != null) {
                        systemTracingInstance = new SystemTracing()
                        systemTracingInstance.domainName = domain.class.simpleName
                        systemTracingInstance.systemOnly = domain.class.name.startsWith('org.grails.tlc.sys.')
                        systemTracingInstance.saveThis()
                    }
                }
            }
        }
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    // Returns true if the value passes one of the given tests, else false. If there are no tests or no value, returns false.
    private hasAccess(value, tests) {
        if (tests && value) {
            for (test in tests) {
                if ((test[0] == '*' || test[0] <= value) && (test[1] == '*' || test[1] >= value)) return true
            }
        }

        return false
    }

    // Returns the map of tests for a given access group code in the specified company
    private getAccessGroup(cacheService, company, code) {
        def group = cacheService.get('accessGroup', company.securityCode, code)
        if (group == null) {
            def results = loadAccessGroup(company, code)
            def size = results[0]
            group = results[1]
            cacheService.put('accessGroup', company.securityCode, code, group, size)
        }

        return (group == CacheService.IMPOSSIBLE_VALUE) ? null : group
    }

    // Load the access groups for a given user in the specified company
    private loadUserAccessGroups(company, user) {
        def usr = CompanyUser.findByCompanyAndUser(company, user, [cache: true])
        if (!usr) return [1, CacheService.IMPOSSIBLE_VALUE]
        def groups = []
        def size = 0
        for (it in usr.accessGroups) {
            groups << it.code
            size += it.code.length()
        }

        return [size ?: 1, Collections.unmodifiableList(groups)]
    }

    // Load the rules for a given access group in a company
    private loadAccessGroup(company, code) {
        def group = AccessGroup.findByCompanyAndCode(company, code)
        if (!group) return [1, CacheService.IMPOSSIBLE_VALUE]
        def rules = [:]
        def size = createSubRules(rules, 'ar', group.customers, 0)
        size = createSubRules(rules, 'ap', group.suppliers, size)
        for (int i = 1; i < 9; i++) {
            size = createSubRules(rules, "e${i}", group."element${i}", size)
        }

        return [size ?: 1, Collections.unmodifiableMap(rules)]
    }

    // Create the detailed access group rules
    private createSubRules(rules, code, data, size) {
        def subRules = []
        size += code.length()
        if (data) {
            def lst = data.replace("\n", "").split(',')*.trim()
            if (lst.size() == 1 && lst[0] == '*') {
                subRules << Collections.unmodifiableList(['*', '*'])
                size += 2
            } else {
                for (item in lst) {
                    def range
                    def pos = item.indexOf('-')
                    if (pos > 0) {
                        range = [item.substring(0, pos).trim(), item.substring(pos + 1).trim()]
                    } else {
                        range = [item, item]
                    }

                    subRules << Collections.unmodifiableList(range)
                    size += range[0].length() + range[1].length()
                }
            }
        }

        rules.put(code, Collections.unmodifiableList(subRules))
        return size
    }

    // Returns the HQL tests for inclusion in a where clause for the given element when
    // selecting accounts a user is permitted to access, or nullstring if no tests are
    // required (i.e. the user has full access to this element).
    private createElementHQL(element, tests, alias, createAsSQL) {
        def elementProperty = alias + "element${element.elementNumber}"
        if (createAsSQL) elementProperty += '_id'
        def sql = "${elementProperty} is null"
        if (!tests) return sql    // No tests means no permission for this element

        // Build up the list of tests
        def testList = []
        for (test in tests) {
            if (test[0] == '*' && test[1] == '*') return ''           // No test required since they have full access
            new CodeRange(element.dataType, element.dataLength, test[0], test[1]).addToList(testList)
        }

        def singles = []
        def ranges = []
        for (test in testList) {
            if (test.from == test.to) {
                singles << test.from
            } else {
                ranges << test
            }
        }

        // Set up the HQL fragment for a correlated sub-query and give the subquery table an alias
        def prefix = "cevcsq${element.elementNumber}"
        if (createAsSQL) {  // SQL
            sql = "(${sql} or exists (select 1 from code_element_value as ${prefix} where ${prefix}.id = ${elementProperty} and ("
        } else {            // HQL
            sql = "(${sql} or exists (select 1 from CodeElementValue as ${prefix} where ${prefix}.id = ${elementProperty}.id and ("
        }

        if (singles) {
            if (singles.size() == 1) {
                sql += "${prefix}.code = '${singles[0]}'"
            } else {
                sql += "${prefix}.code in ('${singles.join("','")}')"
            }
        }

        if (ranges) {
            def pos = 0
            def last = ranges.size() - 1
            for (range in ranges) {
                if (singles || pos > 0) sql += ' or '
                if (pos == 0 && range.startsFromMinimum()) {
                    sql += "${prefix}.code <= '${range.to}'"
                } else if (pos == last && range.endsWithMaximum()) {
                    sql += "${prefix}.code >= '${range.from}'"
                } else {
                    sql += "${prefix}.code between '${range.from}' and '${range.to}'"
                }

                pos++
            }
        }

        return sql + ')))'
    }

    // Returns the HQL tests for inclusion in a where clause for the given element when
    // selecting the code element values a user is allowed to access, or nullstring if
    // no tests are required (i.e. the user has full access to this element). The tests
    // parameter must have aready been checked to ensure it is not empty.
    private createElementValueHQL(element, tests, alias) {

        // Build up the list of tests
        def testList = []
        for (test in tests) {
            if (test[0] == '*' && test[1] == '*') return ''           // No test required since they have full access
            new CodeRange(element.dataType, element.dataLength, test[0], test[1]).addToList(testList)
        }

        def singles = []
        def ranges = []
        for (test in testList) {
            if (test.from == test.to) {
                singles << test.from
            } else {
                ranges << test
            }
        }

        def sql = '('
        if (singles) {
            if (singles.size() == 1) {
                sql += "${alias}code = '${singles[0]}'"
            } else {
                sql += "${alias}code in ('${singles.join("','")}')"
            }
        }

        if (ranges) {
            def pos = 0
            def last = ranges.size() - 1
            for (range in ranges) {
                if (singles || pos > 0) sql += ' or '
                if (pos == 0 && range.startsFromMinimum()) {
                    sql += "${alias}code <= '${range.to}'"
                } else if (pos == last && range.endsWithMaximum()) {
                    sql += "${alias}code >= '${range.from}'"
                } else {
                    sql += "${alias}code between '${range.from}' and '${range.to}'"
                }

                pos++
            }
        }

        return sql + ')'
    }
}
