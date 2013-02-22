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

import java.text.NumberFormat
import java.text.ParseException

class BudgetController {

    // Injected services
    def utilService
    def bookService
    def sessionFactory

    // Security settings
    def activities = [default: 'budgets']

    // List of actions with specific request types
    static allowedMethods = [apply: 'POST', imported: 'POST', adjusting: 'POST']

    def index() { redirect(action: 'filterSettings', params: [target: 'list']) }

    def filterSettings() {
        if (!params.target) params.target = 'list'
        def filterValues = utilService.getFilterValues('budgets')
        def company = utilService.currentCompany()
        def yearInstanceList = Year.findAllByCompany(company, [sort: 'validFrom', order: 'desc'])
        while (yearInstanceList && !Period.countByYear(yearInstanceList[0])) yearInstanceList.remove((int) 0)
        def yearInstance = Year.findByIdAndCompany(filterValues.year, company) ?: (yearInstanceList ? yearInstanceList[0] : new Year())
        filterValues.year = yearInstance.id
        def periodInstanceList = yearInstance.id ? Period.findAllByYear(yearInstance, [sort: 'validFrom', order: 'asc']) : []
        if (!periodInstanceList) periodInstanceList << new Period()
        def selectedPeriodIds = (yearInstance.id && filterValues.periods) ? filterValues.periods : []
        filterValues.periods = selectedPeriodIds
        def selectedPeriods = []
        def iter = selectedPeriodIds.iterator()
        def val
        while (iter.hasNext()) {
            val = Period.findByIdAndYear(iter.next(), yearInstance)
            if (val) {
                selectedPeriods << val
            } else {
                iter.remove()
            }
        }

        def fragment
        def sectionType = filterValues.type
        def parameters = [company]
        if (sectionType == null) sectionType = 'ie'
        if (['ie', 'bs'].contains(sectionType)) {
            fragment = ' and x.type = ?'
            parameters << sectionType
        } else {
            fragment = ''
        }

        filterValues.type = sectionType
        def chartSectionInstanceList = ChartSection.findAll('from ChartSection as x where x.company = ? and x.accountSegment > 0' + fragment +
                ' and exists(select y.id from Account as y where y.section = x) order by x.treeSequence', parameters)
        def chartSectionInstance = ChartSection.findByIdAndCompany(filterValues.section, company) ?: new ChartSection()
        if (sectionType && chartSectionInstance.id && chartSectionInstance.type != sectionType) chartSectionInstance = new ChartSection()
        filterValues.section = chartSectionInstance.id
        def codeElementInstanceList = CodeElement.findAllByCompanyAndElementNumberGreaterThan(company, (byte) 1, [sort: 'elementNumber'])
        def selectedValueIds = filterValues.values ?: [:]
        filterValues.values = selectedValueIds
        def valueLists = [:]
        def selectedValues = [:]
        def key
        for (element in codeElementInstanceList) {
            key = element.elementNumber.toString()
            val = CodeElementValue.findByIdAndElement(selectedValueIds.get(key), element) ?: new CodeElementValue()
            if (val.id && bookService.hasCodeElementValueAccess(val)) {
                selectedValues.put(key, val)
            } else {
                selectedValueIds.remove(key)
            }

            fragment = bookService.createElementAccessFragment('x', element)
            if (fragment != null) {
                val = fragment ? 'from CodeElementValue as x where x.element = ? and ' + fragment + ' order by x.code' : 'from CodeElementValue as x where x.element = ? order by x.code'
                valueLists.put(key, CodeElementValue.findAll(val, [element]))
            }
        }

        [yearInstanceList: yearInstanceList, yearInstance: yearInstance, periodInstanceList: periodInstanceList, selectedPeriods: selectedPeriods, sectionType: sectionType,
                    chartSectionInstanceList: chartSectionInstanceList, chartSectionInstance: chartSectionInstance, codeElementInstanceList: codeElementInstanceList,
                    valueLists: valueLists, selectedValues: selectedValues, target: params.target, targetName: message(code: 'budget.' + params.target, default: params.target)]
    }

    def apply() {
        if (!params.target) params.target = 'list'
        def filterValues = utilService.getFilterValues('budgets')
        def company = utilService.currentCompany()
        def valid = true
        def yearInstance = Year.findByIdAndCompany(params.year, company)
        filterValues.year = yearInstance?.id
        def periodInstanceList = []
        if (yearInstance) {
            def period
            def pds = params.list('periods')
            if (!Period.countByYear(yearInstance)) {
                flash.message = message(code: 'budget.no.periods', default: 'There are no periods available in the selected year')
                valid = false
            } else {
                for (pd in pds) {
                    period = Period.findByIdAndYear(pd, yearInstance)
                    if (period) {
                        periodInstanceList << period.id
                    } else {
                        flash.message = utilService.standardMessage('not.found', 'period', pd)
                        valid = false
                        break
                    }
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'year', params.year)
            valid = false
        }

        filterValues.periods = periodInstanceList
        def sectionType = ['ie', 'bs'].contains(params.sectionType) ? params.sectionType : ''
        filterValues.type = sectionType
        def chartSectionInstance
        if (params.section) {
            chartSectionInstance = ChartSection.findByIdAndCompany(params.section, company)
            if (!chartSectionInstance || (sectionType && chartSectionInstance.type != sectionType)) {
                chartSectionInstance = null
                if (valid) {
                    flash.message = utilService.standardMessage('not.found', 'chartSection', params.section)
                    valid = false
                }
            }
        }

        filterValues.section = chartSectionInstance?.id
        def elements = CodeElement.findAllByCompanyAndElementNumberGreaterThan(company, (byte) 1, [sort: 'elementNumber'])
        def values = [:]
        def val, value
        for (element in elements) {
            val = params."value${element.elementNumber}"
            if (val) {
                value = CodeElementValue.findByIdAndElement(val, element)
                if (value && bookService.hasCodeElementValueAccess(value)) {
                    values.put(element.elementNumber.toString(), value.id)
                } else if (valid) {
                    flash.message = utilService.standardMessage('not.found', 'codeElementValue', val)
                    valid = false
                    break
                }
            }
        }

        filterValues.values = values

        if (valid) {
            redirect(action: params.target)
        } else {
            redirect(action: 'filterSettings', params: [target: params.target])
        }
    }

    def list() {
        def filterValues = utilService.getFilterValues('budgets')
        if (!filterValues) {    // Need at least the year
            redirect(action: 'filterSettings', params: [target: 'list'])
            return
        }

        params.max = utilService.max
        params.sort = ['code', 'name', 'active'].contains(params.sort) ? params.sort : 'code'
        params.offset = utilService.offset

        def company = utilService.currentCompany()
        def currency = utilService.companyCurrency()
        def parameters = []
        def sql = createFromClause(company, filterValues, parameters)
        if (!sql) {
            flash.message = message(code: 'generic.no.permission', default: 'There are no accounts you can access and therefore you cannot perform this operation')
            redirect(controller: 'systemMenu', action: 'display')
            return
        }

        def accountInstanceList = Account.findAll(sql + ' order by x.' + params.sort + ' ' + (params.order ?: 'asc'), parameters, [max: params.max, offset: params.offset])
        def accountInstanceTotal = Account.executeQuery('select count(*) ' + sql, parameters)[0]
        def yearInstance = Year.findByIdAndCompany(filterValues.year, company)
        def periodInstanceList
        if (filterValues.periods) {
            periodInstanceList = Period.findAllByYearAndIdInList(yearInstance, filterValues.periods, [sort: 'validFrom'])
        } else {
            periodInstanceList = Period.findAllByYear(yearInstance, [sort: 'validFrom'])
        }

        def generalBalanceInstanceList
        def fieldWidth = 10
        if (accountInstanceList && periodInstanceList) {
            parameters = []
            def accountTest, periodTest
            if (accountInstanceList.size() == 1) {
                accountTest = '= ?'
            } else {
                accountTest = 'in (?'
                for (int i = 1; i < accountInstanceList.size(); i++) accountTest += ', ?'
                accountTest += ')'
            }

            if (periodInstanceList.size() == 1) {
                periodTest = '= ?'
            } else {
                periodTest = 'in (?'
                for (int i = 1; i < periodInstanceList.size(); i++) periodTest += ', ?'
                periodTest += ')'
            }

            parameters.addAll(accountInstanceList)
            parameters.addAll(periodInstanceList)
            sql = 'from GeneralBalance where account ' + accountTest + ' and period ' + periodTest + ' order by account.code, period.validFrom'
            generalBalanceInstanceList = GeneralBalance.findAll(sql, parameters)
            if (generalBalanceInstanceList.size() != accountInstanceList.size() * periodInstanceList.size()) {
                flash.message = message(code: 'budget.bad.count', args: [(accountInstanceList.size() * periodInstanceList.size()).toString(), generalBalanceInstanceList.size().toString()], default: "Inconsistent number of GL balance records returned (${generalBalanceInstanceList.size()} expected but ${accountInstanceList.size() * periodInstanceList.size()} returned)")
                redirect(action: 'filterSettings', params: [target: 'list'])
                return
            }

            for (bal in generalBalanceInstanceList) {
                if (bal.companyBudget.abs() > 999999999.0) {
                    fieldWidth = 14
                    break
                }
            }
        }

        [accountInstanceList: accountInstanceList, accountInstanceTotal: accountInstanceTotal, periodInstanceList: periodInstanceList,
                    generalBalanceInstanceList: generalBalanceInstanceList, currencyCode: currency.code, decimals: currency.decimals, fieldWidth: fieldWidth]
    }

    def imports() {
        def company = utilService.currentCompany()
        def yearInstanceList = Year.findAllByCompany(company, [sort: 'validFrom', order: 'desc'])
        def yearInstance = Year.findByIdAndCompany(params.id, company)
        if (!yearInstance) yearInstance = yearInstanceList ? yearInstanceList[0] : new Year()

        [yearInstance: yearInstance, yearInstanceList: yearInstanceList]
    }

    def imported() {
        def company = utilService.currentCompany()
        def currency = utilService.companyCurrency()
        def session = sessionFactory.currentSession
        def today = utilService.fixDate()
        def yearInstance = Year.findByIdAndCompany(params.id, company)
        if (!yearInstance) {
            flash.message = utilService.standardMessage('not.found', 'year', params.id)
        } else {
            def periods = Period.findAllByYear(yearInstance, [sort: 'validFrom'])
            if (!periods) {
                flash.message = message(code: 'budget.no.periods', default: 'There are no periods defined in the selected year')
            } else {
                def uploadFile = request.getFile('file')
                if (uploadFile.isEmpty()) {
                    flash.message = message(code: 'account.empty', default: 'File is empty')
                } else {
                    if (uploadFile.getSize() > 1024 * 1024) {
                        flash.message = message(code: 'account.size', default: 'File exceeds the 1 MB limit')
                    } else {
                        def account, budget, rate, modified
                        def lines = []
                        def rates = [:]
                        def valid = true
                        try {
                            def nf = NumberFormat.getInstance(utilService.currentLocale())
                            def sourceFile = utilService.tempFile('BU', 'txt')
                            uploadFile.transferTo(sourceFile)
                            def fields, field
                            sourceFile.eachLine {
                                if (valid && it.trim()) {
                                    fields = it.split('\\t')*.trim()
                                    if (fields.size() > 1 && fields[0]) {
                                        modified = false
                                        for (int i = 0; i < fields.size(); i++) {
                                            field = fields[i]
                                            if (i) {
                                                if (field) {
                                                    try {
                                                        budget = nf.parse(field)
                                                        if (!(budget instanceof BigDecimal)) budget = new BigDecimal(budget.toString())
                                                        if (account.status == 'cr') budget = -budget
                                                        fields[i] = utilService.round(budget, 0)
                                                        modified = true
                                                    } catch (ParseException bdex) {
                                                        flash.message = message(code: 'budget.bad.amount', args: [field], default: "Invalid value: ${field}")
                                                        valid = false
                                                        break
                                                    }
                                                } else {
                                                    fields[i] = null
                                                }
                                            } else {
                                                field = bookService.fixCase(field)
                                                account = bookService.getAccount(company, field)
                                                if (account instanceof String) {
                                                    flash.message = account
                                                    valid = false
                                                    break
                                                } else if (!bookService.hasAccountAccess(account)) {
                                                    flash.message = message(code: 'budget.permission', args: [account.code], default: "Account ${account.code} not found")
                                                    valid = false
                                                    break
                                                } else {
                                                    if (account.currency.id != currency.id && !rates.get(account.currency.code)) {
                                                        rate = utilService.getExchangeRate(currency, account.currency, today)
                                                        if (rate) {
                                                            rates.put(account.currency.code, rate)
                                                        } else {
                                                            flash.message = message(code: 'document.bad.exchangeRate', args: [currency.code, account.currency.code], default: "No exchange rate available from ${currency.code} to ${account.currency.code}")
                                                            valid = false
                                                            break
                                                        }
                                                    }

                                                    fields[i] = account
                                                }
                                            }
                                        }

                                        if (modified) lines << fields
                                    }
                                }
                            }

                            try {
                                sourceFile.delete()
                            } catch (Exception e1) {}

                        } catch (Exception ex) {
                            log.error(ex)
                            ex.printStackTrace()
                            flash.message = message(code: 'account.bad.upload', default: 'Unable to upload the file')
                            valid = false
                        }

                        if (valid) {
                            def limit, balance
                            def count = 0
                            def lock = bookService.getCompanyLock(company)
                            for (line in lines) {
                                modified = false
                                account = line[0]
                                limit = Math.min(line.size() - 1, periods.size())
                                lock.lock()
                                try {
                                    GeneralTransaction.withTransaction {status ->
                                        for (int i = 0; i < limit; i++) {
                                            budget = line[i + 1]
                                            if (budget != null) {
                                                balance = GeneralBalance.findByAccountAndPeriod(account, periods[i])
                                                if (!balance) {
                                                    flash.message = utilService.standardMessage('not.found', 'generalBalance', account.code + '/' + period.code)
                                                    status.setRollbackOnly()
                                                    valid = false
                                                    break
                                                }

                                                balance.companyBudget = budget
                                                if (account.currency.id == currency.id) {
                                                    balance.generalBudget = budget
                                                } else {
                                                    balance.generalBudget = utilService.round(budget * rates.get(account.currency.code), 0)
                                                }

                                                if (balance.saveThis()) {
                                                    modified = true
                                                } else {
                                                    flash.message = message(code: 'account.balance.save', args: [account.code], default: "Unable to save the balance record(s) for account ${account.code}")
                                                    status.setRollbackOnly()
                                                    valid = false
                                                    break
                                                }

                                                session.evict(balance)
                                            }
                                        }
                                    }
                                } finally {
                                    lock.unlock()
                                }

                                if (modified) count++

                                // Clean up
                                session.evict(account)
                            }

                            if (valid) {
                                flash.message = message(code: 'budget.imported', args: [count.toString()], default: "${count} account(s) had their budgets updated")
                            }
                        }
                    }
                }
            }
        }

        redirect(action: 'imports', params: params)
    }

    def print() {
        def filterValues = utilService.getFilterValues('budgets')
        if (!filterValues) {    // Need at least the year
            redirect(action: 'filterSettings', params: [target: 'list'])
            return
        }

        def company = utilService.currentCompany()
        def yearInstance = Year.findByIdAndCompany(filterValues.year, company)
        if (!yearInstance) {
            flash.message = utilService.standardMessage('not.found', 'year', filterValues.year)
            redirect(action: 'filterSettings', params: [target: 'list'])
            return
        }

        params.p_yearId = yearInstance.id.toString()
        def periodInstanceList
        if (filterValues.periods) {
            periodInstanceList = Period.findAllByYearAndIdInList(yearInstance, filterValues.periods, [sort: 'validFrom'])
        } else {
            periodInstanceList = Period.findAllByYear(yearInstance, [sort: 'validFrom'])
        }

        if (periodInstanceList) {
            if (periodInstanceList.size() > 13) {
                flash.message = message(code: 'budget.excess.periods', default: 'There is a maximum of 13 periods that can be included in a Budget Listing report')
                redirect(action: 'filterSettings', params: [target: 'list'])
                return
            }

            def offset = 13 - periodInstanceList.size()
            for (int i = 1; i <= periodInstanceList.size(); i++) {
                params."p_periodId${i + offset}" = periodInstanceList[i - 1].id.toString()
            }
        } else {
            flash.message = message(code: 'budget.no.periods', default: 'There are no periods available in the selected year')
            redirect(action: 'filterSettings', params: [target: 'list'])
            return
        }

        def sectionInstance
        if (filterValues.section) sectionInstance = ChartSection.findByIdAndCompany(filterValues.section, company)
        if (sectionInstance) {
            params.p_section = sectionInstance.id.toString()
        } else if (['ie', 'bs'].contains(filterValues.type)) {
            params.p_section = filterValues.type
        }

        def elementValuesMap = filterValues.values
        if (elementValuesMap) {
            def val
            for (int i = 2; i <= 8; i++) {
                val = elementValuesMap.get(i.toString())
                if (val) params."p_element${i}" = val.toString()
            }
        }

        def result = utilService.demandRunFromParams('budgetList', params)
        if (result instanceof String) {
            flash.message = result
        } else {
            flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
        }

        redirect(action: 'list')
    }

    def adjust() {
        def filterValues = utilService.getFilterValues('budgets')
        def company = utilService.currentCompany()

        def yearInstanceList = Year.findAllByCompany(company, [sort: 'validFrom', order: 'desc'])
        while (yearInstanceList && !Period.countByYear(yearInstanceList[0])) yearInstanceList.remove((int) 0)
        def yearInstance = Year.findByIdAndCompany(filterValues.year, company) ?: (yearInstanceList ? yearInstanceList[0] : new Year())
        filterValues.year = yearInstance.id
        def targetYearInstanceList = yearInstanceList.clone()
        def targetYearInstance = Year.findByIdAndCompany(filterValues.targetYear, company) ?: yearInstance
        filterValues.targetYear = targetYearInstance.id

        def periodInstanceList = yearInstance.id ? Period.findAllByYear(yearInstance, [sort: 'validFrom', order: 'asc']) : []
        if (!periodInstanceList) periodInstanceList << new Period()
        def selectedPeriodIds = (yearInstance.id && filterValues.periods) ? filterValues.periods : []
        filterValues.periods = selectedPeriodIds
        def selectedPeriods = []
        def iter = selectedPeriodIds.iterator()
        def val
        while (iter.hasNext()) {
            val = Period.findByIdAndYear(iter.next(), yearInstance)
            if (val) {
                selectedPeriods << val
            } else {
                iter.remove()
            }
        }

        def targetPeriodInstanceList = targetYearInstance.id ? Period.findAllByYear(targetYearInstance, [sort: 'validFrom', order: 'asc']) : []
        if (!targetPeriodInstanceList) targetPeriodInstanceList << new Period()
        selectedPeriodIds = (targetYearInstance.id && filterValues.targetPeriods) ? filterValues.targetPeriods : []
        filterValues.targetPeriods = selectedPeriodIds
        def targetSelectedPeriods = []
        iter = selectedPeriodIds.iterator()
        while (iter.hasNext()) {
            val = Period.findByIdAndYear(iter.next(), targetYearInstance)
            if (val) {
                targetSelectedPeriods << val
            } else {
                iter.remove()
            }
        }

        def fragment
        def sectionType = filterValues.type
        def parameters = [company]
        if (sectionType == null) sectionType = 'ie'
        if (['ie', 'bs'].contains(sectionType)) {
            fragment = ' and x.type = ?'
            parameters << sectionType
        } else {
            fragment = ''
        }

        filterValues.type = sectionType
        def chartSectionInstanceList = ChartSection.findAll('from ChartSection as x where x.company = ? and x.accountSegment > 0' + fragment +
                ' and exists(select y.id from Account as y where y.section = x) order by x.treeSequence', parameters)
        def chartSectionInstance = ChartSection.findByIdAndCompany(filterValues.section, company) ?: new ChartSection()
        if (sectionType && chartSectionInstance.id && chartSectionInstance.type != sectionType) chartSectionInstance = new ChartSection()
        filterValues.section = chartSectionInstance.id
        def codeElementInstanceList = CodeElement.findAllByCompanyAndElementNumberGreaterThan(company, (byte) 1, [sort: 'elementNumber'])
        def selectedValueIds = filterValues.values ?: [:]
        filterValues.values = selectedValueIds
        def valueLists = [:]
        def selectedValues = [:]
        def key
        for (element in codeElementInstanceList) {
            key = element.elementNumber.toString()
            val = CodeElementValue.findByIdAndElement(selectedValueIds.get(key), element) ?: new CodeElementValue()
            if (val.id && bookService.hasCodeElementValueAccess(val)) {
                selectedValues.put(key, val)
            } else {
                selectedValueIds.remove(key)
            }

            fragment = bookService.createElementAccessFragment('x', element)
            if (fragment != null) {
                val = fragment ? 'from CodeElementValue as x where x.element = ? and ' + fragment + ' order by x.code' : 'from CodeElementValue as x where x.element = ? order by x.code'
                valueLists.put(key, CodeElementValue.findAll(val, [element]))
            }
        }

        def sourceDataInstanceList = []
        sourceDataInstanceList << [id: 'budgets', name: message(code: 'budget.adjust.budgets', default: 'Budgets')]
        sourceDataInstanceList << [id: 'actuals', name: message(code: 'budget.adjust.actuals', default: 'Actuals (excluding adjustments)')]
        sourceDataInstanceList << [id: 'adjusted', name: message(code: 'budget.adjust.adjusted', default: 'Actuals (including adjustments)')]
        def sourceDataInstance = ['budgets', 'actuals', 'adjusted'].contains(filterValues.data) ? [id: filterValues.data] : [id: 'budgets']
        filterValues.data = sourceDataInstance.id

        def adjustmentTypeInstanceList = []
        adjustmentTypeInstanceList << [id: 'value', name: message(code: 'budget.adjust.value', default: 'Set to a monetary value')]
        adjustmentTypeInstanceList << [id: 'amount', name: message(code: 'budget.adjust.amount', default: 'Adjust by a monetary amount')]
        adjustmentTypeInstanceList << [id: 'percent', name: message(code: 'budget.adjust.percent', default: 'Adjust by a percentage')]
        def adjustmentTypeInstance = ['value', 'amount', 'percent'].contains(filterValues.adjust) ? [id: filterValues.adjust] : [:]
        filterValues.adjust = adjustmentTypeInstance.id

        def adjustmentValue = filterValues.value
        def adjustmentScale = (adjustmentTypeInstance.id == 'percent') ? 3 : 0
        if (adjustmentValue != null) {
            adjustmentValue = utilService.round(adjustmentValue, adjustmentScale)
            filterValues.value = adjustmentValue
        }

        [yearInstanceList: yearInstanceList, yearInstance: yearInstance, periodInstanceList: periodInstanceList, selectedPeriods: selectedPeriods,
                    targetYearInstanceList: targetYearInstanceList, targetYearInstance: targetYearInstance, targetPeriodInstanceList: targetPeriodInstanceList,
                    targetSelectedPeriods: targetSelectedPeriods, sectionType: sectionType, chartSectionInstanceList: chartSectionInstanceList,
                    chartSectionInstance: chartSectionInstance, codeElementInstanceList: codeElementInstanceList, valueLists: valueLists, selectedValues: selectedValues,
                    sourceDataInstanceList: sourceDataInstanceList, sourceDataInstance: sourceDataInstance, adjustmentTypeInstanceList: adjustmentTypeInstanceList,
                    adjustmentTypeInstance: adjustmentTypeInstance, adjustmentValue: adjustmentValue, adjustmentScale: adjustmentScale]
    }

    def adjusting() {
        def filterValues = utilService.getFilterValues('budgets')
        def company = utilService.currentCompany()
        def valid = true
        def yearInstance = Year.findByIdAndCompany(params.year, company)
        filterValues.year = yearInstance?.id
        def periodInstanceList = []
        if (yearInstance) {
            def period
            def pds = params.list('periods')
            if (!Period.countByYear(yearInstance)) {
                flash.message = message(code: 'budget.no.periods', default: 'There are no periods available in the selected year')
                valid = false
            } else {
                for (pd in pds) {
                    period = Period.findByIdAndYear(pd, yearInstance)
                    if (period) {
                        periodInstanceList << period.id
                    } else {
                        flash.message = utilService.standardMessage('not.found', 'period', pd)
                        valid = false
                        break
                    }
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'year', params.year)
            valid = false
        }

        filterValues.periods = periodInstanceList?.sort {it}
        def targetYearInstance = Year.findByIdAndCompany(params.targetYear, company)
        filterValues.targetYear = targetYearInstance?.id
        def targetPeriodInstanceList = []
        if (targetYearInstance) {
            def period
            def pds = params.list('targetPeriods')
            if (!Period.countByYear(targetYearInstance)) {
                flash.message = message(code: 'budget.no.periods', default: 'There are no periods available in the selected year')
                valid = false
            } else {
                for (pd in pds) {
                    period = Period.findByIdAndYear(pd, targetYearInstance)
                    if (period) {
                        targetPeriodInstanceList << period.id
                    } else {
                        flash.message = utilService.standardMessage('not.found', 'period', pd)
                        valid = false
                        break
                    }
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'year', params.year)
            valid = false
        }

        filterValues.targetPeriods = targetPeriodInstanceList?.sort {it}
        def sectionType = ['ie', 'bs'].contains(params.sectionType) ? params.sectionType : ''
        filterValues.type = sectionType
        def chartSectionInstance
        if (params.section) {
            chartSectionInstance = ChartSection.findByIdAndCompany(params.section, company)
            if (!chartSectionInstance || (sectionType && chartSectionInstance.type != sectionType)) {
                chartSectionInstance = null
                if (valid) {
                    flash.message = utilService.standardMessage('not.found', 'chartSection', params.section)
                    valid = false
                }
            }
        }

        filterValues.section = chartSectionInstance?.id
        def elements = CodeElement.findAllByCompanyAndElementNumberGreaterThan(company, (byte) 1, [sort: 'elementNumber'])
        def values = [:]
        def val, value
        for (element in elements) {
            val = params."value${element.elementNumber}"
            if (val) {
                value = CodeElementValue.findByIdAndElement(val, element)
                if (value && bookService.hasCodeElementValueAccess(value)) {
                    values.put(element.elementNumber.toString(), value.id)
                } else if (valid) {
                    flash.message = utilService.standardMessage('not.found', 'codeElementValue', val)
                    valid = false
                    break
                }
            }
        }

        filterValues.values = values
        def sourceDataInstance = params.sourceData
        if (!['budgets', 'actuals', 'adjusted'].contains(sourceDataInstance)) {
            flash.message = message(code: 'budget.adjust.bad.source', default: 'Invalid Source Data selection')
            sourceDataInstance = null
            valid = false
        }

        filterValues.data = sourceDataInstance
        def adjustmentTypeInstance = params.adjustmentType
        if (adjustmentTypeInstance && !['value', 'amount', 'percent'].contains(adjustmentTypeInstance)) {
            flash.message = message(code: 'budget.adjust.bad.type', default: 'Invalid Adjustment Type selection')
            adjustmentTypeInstance = null
            valid = false
        }

        filterValues.adjust = adjustmentTypeInstance
        def adjustmentScale = (adjustmentTypeInstance == 'percent') ? 3 : 0
        def adjustmentValue = params.adjustmentValue
        if (adjustmentValue) {
            try {
                def nf = NumberFormat.getInstance(utilService.currentLocale())
                adjustmentValue = nf.parse(adjustmentValue)
                if (!(adjustmentValue instanceof BigDecimal)) adjustmentValue = new BigDecimal(adjustmentValue.toString())
                if (adjustmentValue != utilService.round(adjustmentValue, adjustmentScale)) {
                    flash.message = message(code: 'budget.bad.amount', args: [params.adjustmentValue], default: "Invalid value: ${params.adjustmentValue}")
                    adjustmentValue = null
                    valid = false
                }
            } catch (ParseException bdex) {
                flash.message = message(code: 'budget.bad.amount', args: [params.adjustmentValue], default: "Invalid value: ${params.adjustmentValue}")
                adjustmentValue = null
                valid = false
            }
        } else {
            adjustmentValue = null
        }

        filterValues.value = adjustmentValue
        if (valid) {
            if (!adjustmentTypeInstance && adjustmentValue) {
                flash.message = message(code: 'budget.adjust.bad.amount', default: "Cannot have an Adjustment Value when the Adjustment Type is 'none'")
            } else if (adjustmentTypeInstance && ((adjustmentTypeInstance == 'value' && adjustmentValue == null) || (adjustmentTypeInstance != 'value' && !adjustmentValue))) {
                flash.message = message(code: 'budget.adjust.no.amount', default: 'You must supply and Adjustment Amount')
            } else if (adjustmentTypeInstance == 'percent' && adjustmentValue.abs() > 1000.0) {
                flash.message = message(code: 'budget.adjust.bad.percent', default: 'The maximum percentage adjustment is 1000% up or down')
            } else if (yearInstance.id == targetYearInstance.id && periodInstanceList == targetPeriodInstanceList && sourceDataInstance == 'budgets' && !adjustmentTypeInstance) {
                flash.message = message(code: 'budget.adjust.no.change', default: 'The settings you have made would not make any changes')
            } else {
                def parameters = []
                def sql = createFromClause(company, filterValues, parameters)
                if (sql) {
                    def count = performAdjustment(company, filterValues, sql, parameters, yearInstance, targetYearInstance)
                    flash.message = message(code: 'budget.adjust.changed', args: [count.toString()], default: "${count} budget values updated")
                } else {
                    flash.message = message(code: 'generic.no.permission', default: 'There are no accounts you can access and therefore you cannot perform this operation')
                }
            }
        }

        redirect(action: 'adjust')
    }

    // Create the 'from Account...' clause and returns it with the 'parameters' input parameter
    // filled in with the appropriate values. Returns the HQL clause as a String or null if the
    // user has no access to the accounts.
    private createFromClause(company, filterValues, parameters) {
        def sql, sectionInstance
        if (filterValues.section) sectionInstance = ChartSection.findByIdAndCompany(filterValues.section, company)
        if (sectionInstance) {
            sql = 'from Account as x where x.section = ?'
            parameters << sectionInstance
        } else if (['ie', 'bs'].contains(filterValues.type)) {
            sql = 'from Account as x where x.securityCode = ? and x.section.type = ?'
            parameters << company.securityCode
            parameters << filterValues.type
        }

        def exclusions = []
        if (filterValues.values) {
            def val
            for (it in filterValues.values) {
                val = CodeElementValue.findByIdAndSecurityCode(it.value, company.securityCode)
                if (val) {
                    exclusions << val.element
                    if (sql) {
                        sql += " and x.element${it.key} = ?"
                    } else {
                        sql = "from Account as x where x.element${it.key} = ?"
                    }

                    parameters << val
                }
            }
        }

        if (!sql) {
            sql = 'from Account as x where x.securityCode = ?'
            parameters << company.securityCode
        }

        def tests = bookService.createAccountAccessFragment('x', exclusions)
        if (tests) {
            sql += ' and ' + tests
        } else if (tests == null) {
            sql = null  // No access rights
        }

        return sql
    }

    private performAdjustment(company, filterValues, fromClause, parameters, srcYear, tgtYear) {
        def count = 0
        def accounts = Account.executeQuery(fromClause + ' order by x.code', parameters)
        if (accounts) {
            def currency = utilService.companyCurrency()
            def srcPeriods, tgtPeriods
            def adjustmentType = filterValues.adjust
            if (adjustmentType != 'value') {    // Don't need source periods for setting an absolute value
                if (filterValues.periods) {
                    srcPeriods = Period.findAllByYearAndIdInList(srcYear, filterValues.periods, [sort: 'validFrom'])
                } else {
                    srcPeriods = Period.findAllByYear(srcYear, [sort: 'validFrom'])
                }
            }

            if (filterValues.targetPeriods) {
                tgtPeriods = Period.findAllByYearAndIdInList(tgtYear, filterValues.targetPeriods, [sort: 'validFrom'])
            } else {
                tgtPeriods = Period.findAllByYear(tgtYear, [sort: 'validFrom'])
            }

            if (tgtPeriods && (adjustmentType == 'value' || srcPeriods)) {
                def adjustmentValue = filterValues.value
                def tgtSQL = makeBalanceSQL(tgtPeriods)
                def session = sessionFactory.currentSession
                def lock = bookService.getCompanyLock(company)
                def errorMessage, tgtBalances
                def rates = [:]
                if (adjustmentType == 'value') {
                    for (account in accounts) {
                        lock.lock()
                        try {
                            tgtBalances = GeneralBalance.findAll(tgtSQL, [account])
                            GeneralBalance.withTransaction {status ->
                                for (bal in tgtBalances) {
                                    bal.companyBudget = (account.status == 'cr') ? -adjustmentValue : adjustmentValue
                                    bal.generalBudget = createValue(rates, currency, account, bal.companyBudget)
                                    if (bal.saveThis()) {
                                        count++
                                    } else {
                                        errorMessage = message(code: 'account.balance.save', args: [account.code], default: "Unable to save the balance record(s) for account ${account.code}")
                                        status.setRollbackOnly()
                                        break
                                    }
                                }
                            }
                        } finally {
                            lock.unlock()
                        }

                        session.evict(account)
                        for (bal in tgtBalances) session.evict(bal)
                        if (errorMessage) return errorMessage
                    }
                } else {
                    def dataSource = filterValues.data
                    def srcSQL = makeBalanceSQL(srcPeriods)
                    def srcBalances, srcPos
                    def companyValues = []
                    def generalValues = []
                    for (account in accounts) {
                        srcBalances = GeneralBalance.findAll(srcSQL, [account])
                        createValues(dataSource, adjustmentType, adjustmentValue, srcBalances, rates, currency, account, companyValues, generalValues)
                        for (bal in srcBalances) session.evict(bal)
                        srcPos = 0
                        lock.lock()
                        try {
                            tgtBalances = GeneralBalance.findAll(tgtSQL, [account])
                            GeneralBalance.withTransaction {status ->
                                for (bal in tgtBalances) {
                                    bal.companyBudget = companyValues[srcPos]
                                    bal.generalBudget = generalValues[srcPos]
                                    if (bal.saveThis()) {
                                        srcPos++
                                        if (srcPos == companyValues.size()) srcPos = 0
                                        count++
                                    } else {
                                        errorMessage = message(code: 'account.balance.save', args: [account.code], default: "Unable to save the balance record(s) for account ${account.code}")
                                        status.setRollbackOnly()
                                        break
                                    }
                                }
                            }
                        } finally {
                            lock.unlock()
                        }

                        session.evict(account)
                        for (bal in tgtBalances) session.evict(bal)
                        if (errorMessage) return errorMessage
                    }
                }
            }
        }

        return count
    }

    private makeBalanceSQL(periods) {
        def sql = 'from GeneralBalance as x where x.account = ? and x.period.id in ('
        for (int i = 0; i < periods.size(); i++) {
            if (i) sql += ', '
            sql += periods[i].id.toString()
        }

        sql += ') order by x.period.validFrom'
        return sql
    }

    private createValues(dataSource, adjustmentType, adjustmentValue, srcBalances, rates, currency, account, companyValues, generalValues) {
        companyValues.clear()
        generalValues.clear()
        def companyValue
        if (adjustmentType == 'percent') adjustmentValue = adjustmentValue / 100.0
        for (balance in srcBalances) {
            switch (dataSource) {
                case 'actuals':
                    companyValue = balance.companyTransactionTotal
                    break

                case 'adjusted':
                    companyValue = balance.companyTransactionTotal + balance.companyAdjustmentTotal
                    break

                default:    // Budget value
                    companyValue = balance.companyBudget
                    break
            }

            switch (adjustmentType) {
                case 'amount':
                    companyValue += (account.status == 'cr' ? -adjustmentValue : adjustmentValue)
                    break

                case 'percent':
                    companyValue += utilService.round(companyValue * adjustmentValue, 0)
                    break

                default:    // Straight copy
                    break
            }

            companyValues << companyValue
            generalValues << createValue(rates, currency, account, companyValue)
        }
    }

    private createValue(rates, currency, account, companyValue) {
        if (!companyValue || currency.id == account.currency.id) return companyValue
        def rate = rates.get(account.currency.code)
        if (!rate) {
            rate = utilService.getExchangeRate(currency, account.currency)
            if (rate) {
                rates.put(account.currency.code, rate)
            } else {
                throw new IllegalArgumentException(message(code: 'document.bad.exchangeRate', args: [currency.code, account.currency.code],
                default: "No exchange rate available from ${currency.code} to ${account.currency.code}"))
            }
        }

        return utilService.round(companyValue * rate, 0)
    }
}
