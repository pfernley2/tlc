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

import org.grails.tlc.obj.BalanceReport
import org.grails.tlc.obj.IncomeReport

class GeneralBalanceController {

    // Injected services
    def utilService
    def bookService

    // Security settings
    def activities = [default: 'glreport', incomeReport: 'iereport', incomeReporting: 'iereport', balanceReport: 'bsreport', balanceReporting: 'bsreport']

    // List of actions with specific request types
    static allowedMethods = [trialBalanceReport: 'POST', incomeReporting: 'POST', balanceReporting: 'POST']

    def trialBalance() {
        def generalBalanceInstance = new GeneralBalance()
        def periodInstanceList = bookService.getUsedPeriods(utilService.currentCompany())
        generalBalanceInstance.period = bookService.selectPeriod(periodInstanceList)
        periodInstanceList = periodInstanceList.reverse()
        [generalBalanceInstance: generalBalanceInstance, periodInstanceList: periodInstanceList, omitZero: true]
    }

    def trialBalanceReport() {
        def generalBalanceInstance = new GeneralBalance(params)
        utilService.verify(generalBalanceInstance, ['period'])
        if (generalBalanceInstance.period && generalBalanceInstance.period.status != 'new') {
            params.p_stringId = generalBalanceInstance.period.id.toString()
            params.p_omitZero = params.omitZero ? 'true' : 'false'
            def result = utilService.demandRunFromParams('tb', params)
            if (result instanceof String) {
                flash.message = result
            } else {
                flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            }

            redirect(controller: 'systemMenu', action: 'display')
        } else {
            generalBalanceInstance.errorMessage(field: 'period', code: 'not.found', domain: 'period', value: params.period.id)
            def periodInstanceList = bookService.getUsedPeriods(utilService.currentCompany())
            generalBalanceInstance.period = bookService.selectPeriod(periodInstanceList)
            periodInstanceList = periodInstanceList.reverse()
            render(view: 'trialBalance', model: [generalBalanceInstance: generalBalanceInstance, periodInstanceList: periodInstanceList, omitZero: params.omitZero])
        }
    }

    def detailedPostings() {
        def generalBalanceInstance = new GeneralBalance()
        def periodInstanceList = bookService.getUsedPeriods(utilService.currentCompany())
        if (!generalBalanceInstance.period) generalBalanceInstance.period = bookService.selectPeriod(periodInstanceList)
        periodInstanceList = periodInstanceList.reverse()
        [generalBalanceInstance: generalBalanceInstance, periodInstanceList: periodInstanceList, scopeList: getScopeList(), omitZero: true]
    }

    def detailedPostingsReport() {

        def valid = true
        def generalBalanceInstance = new GeneralBalance(params)
        utilService.verify(generalBalanceInstance, ['period'])
        if (generalBalanceInstance.period && generalBalanceInstance.period.status != 'new') {
            if (['combined', 'ie', 'bs', 'split', 'separate', 'section', 'account'].contains(params.scope.code)) {
                def scopeId = ''
                if (params.scope.code == 'section') {
                    def section = ChartSection.findByCompanyAndCode(utilService.currentCompany(), params.selector)
                    if (!section) {
                        generalBalanceInstance.errorMessage(field: 'companyBudget', code: 'report.postings.no.section', default: 'Invalid chart section code')
                        valid = false
                    } else if (!Account.countBySection(section)) {
                        generalBalanceInstance.errorMessage(field: 'companyBudget', code: 'report.postings.bad.section', default: 'The specified section does not contain any accounts')
                        valid = false
                    } else {
                        scopeId = section.id.toString()
                    }
                } else if (params.scope.code == 'account') {
                    def account = Account.findBySecurityCodeAndCode(utilService.currentCompany().securityCode, params.selector)
                    if (account) {
                        scopeId = account.id.toString()
                    } else {
                        generalBalanceInstance.errorMessage(field: 'companyBudget', code: 'report.postings.no.account', default: 'Invalid general ledger account code')
                        valid = false
                    }
                }

                if (valid) {
                    params.p_stringId = generalBalanceInstance.period.id.toString()
					params.p_omitZero = params.omitZero ? 'true' : 'false'
                    params.p_scope = params.scope.code
                    params.p_scopeId = scopeId
                }
            } else {
                generalBalanceInstance.errorMessage(field: 'generalBudget', code: 'report.postings.scope.invalid', default: 'Invalid report scope')
                valid = false
            }
        } else {
            generalBalanceInstance.errorMessage(field: 'period', code: 'not.found', domain: 'period', value: params.period.id)
            valid = false
        }

        if (valid) {
            def result = utilService.demandRunFromParams('postings', params)
            if (result instanceof String) {
                flash.message = result
            } else {
                flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            }

            redirect(controller: 'systemMenu', action: 'display')
        } else {
            def periodInstanceList = bookService.getUsedPeriods(utilService.currentCompany())
            if (!generalBalanceInstance.period) generalBalanceInstance.period = bookService.selectPeriod(periodInstanceList)
            periodInstanceList = periodInstanceList.reverse()
            render(view: 'detailedPostings', model: [generalBalanceInstance: generalBalanceInstance, periodInstanceList: periodInstanceList,
                    scopeList: getScopeList(), scope: [code: params.scope.code], selector: params.selector, omitZero: params.omitZero])
        }
    }

    def incomeReport() {
        def company = utilService.currentCompany()
        def incomeReportInstance = new IncomeReport()
        def formatInstanceList = []
        for (it in ProfitReportFormat.findAllByCompany(company, [sort: 'name'])) {
            if (it.lines?.size()) formatInstanceList << it  // Only show them formats that have lines defined
        }

        def periodInstanceList = bookService.getUsedPeriods(company)
        incomeReportInstance.period = bookService.selectPeriod(periodInstanceList)
        periodInstanceList = periodInstanceList.reverse()
        def codeElementInstanceList = CodeElement.findAllByCompanyAndElementNumberGreaterThan(company, (byte) 1, [sort: 'elementNumber'])
        def valueLists = [:]
        def key, val, fragment
        for (element in codeElementInstanceList) {
            key = element.elementNumber.toString()
            fragment = bookService.createElementAccessFragment('x', element)
            if (fragment != null) {     // If they have some sort of access
                val = fragment ? 'from CodeElementValue as x where x.element = ? and ' + fragment + ' order by x.code' : 'from CodeElementValue as x where x.element = ? order by x.code'
                valueLists.put(key, CodeElementValue.findAll(val, [element]))
            }
        }

        def groupingList = [CodeElement.findByCompanyAndElementNumber(company, (byte) 1)]
        groupingList.addAll(codeElementInstanceList)

        [incomeReportInstance: incomeReportInstance, formatInstanceList: formatInstanceList, periodInstanceList: periodInstanceList,
                codeElementInstanceList: codeElementInstanceList, valueLists: valueLists, groupingList: groupingList]
    }

    def incomeReporting(IncomeReport incomeReportInstance) {
        def company = utilService.currentCompany()
        def key, val, fragment
        def codeElementInstanceList = CodeElement.findAllByCompanyAndElementNumberGreaterThan(company, (byte) 1, [sort: 'elementNumber'])
        def valid = !incomeReportInstance.hasErrors()
        if (valid) {
            if (incomeReportInstance.format?.securityCode == company.securityCode && incomeReportInstance.format.lines?.size()) {
                params.p_formatId = incomeReportInstance.format.id.toString()
                if (incomeReportInstance.period?.securityCode == company.securityCode && incomeReportInstance.period.status != 'new') {
                    params.p_periodId = incomeReportInstance.period.id.toString()
                    for (element in codeElementInstanceList) {
                        key = element.elementNumber.toString()
                        val = incomeReportInstance."element${key}"
                        if (val) {
                            if (val.element.id == element.id && bookService.hasCodeElementValueAccess(val)) {
                                if (element.id == incomeReportInstance.grouping1?.id || element.id == incomeReportInstance.grouping2?.id || element.id == incomeReportInstance.grouping3?.id) {
                                    incomeReportInstance.errorMessage(field: 'element' + key, code: 'incomeReport.mismatch',
                                            default: 'Cannot limit a report to a specific Code Element Value and have that same Code Element used for grouping')
                                    valid = false
                                } else {
                                    params."p_element${key}" = val.id.toString()
                                }
                            } else {
								incomeReportInstance.errorMessage(field: 'element', code: 'not.found', domain: 'codeElementValue', value: val.id)
                                valid = false
                                break
                            }
                        }
                    }

                    if (valid) {
                        if (incomeReportInstance.grouping1) {
                            if (bookService.createElementAccessFragment('x', incomeReportInstance.grouping1) != null) {
                                params.p_grouping1 = incomeReportInstance.grouping1.id.toString()
                                if (incomeReportInstance.grouping2) {
                                    if (bookService.createElementAccessFragment('x', incomeReportInstance.grouping2) != null) {
                                        if (incomeReportInstance.grouping2.id != incomeReportInstance.grouping1.id) {
                                            params.p_grouping2 = incomeReportInstance.grouping2.id.toString()
                                            if (incomeReportInstance.grouping3) {
                                                if (bookService.createElementAccessFragment('x', incomeReportInstance.grouping3) != null) {
                                                    if (incomeReportInstance.grouping3.id != incomeReportInstance.grouping1.id &&
                                                            incomeReportInstance.grouping3.id != incomeReportInstance.grouping2.id) {
                                                        params.p_grouping3 = incomeReportInstance.grouping3.id.toString()
                                                    } else {
                                                        incomeReportInstance.errorMessage(field: 'grouping3', code: 'incomeReport.duplicate',
                                                                default: 'Each grouping selection must be different')
                                                        valid = false
                                                    }
                                                } else {
													incomeReportInstance.errorMessage(field: 'grouping3', code: 'not.found', domain: 'codeElementValue', value: incomeReportInstance.grouping3.id)
                                                    valid = false
                                                }
                                            }
                                        } else {
                                            incomeReportInstance.errorMessage(field: 'grouping2', code: 'incomeReport.duplicate',
                                                    default: 'Each grouping selection must be different')
                                            valid = false
                                        }
                                    } else {
										incomeReportInstance.errorMessage(field: 'grouping2', code: 'not.found', domain: 'codeElementValue', value: incomeReportInstance.grouping2.id)
                                        valid = false
                                    }
                                } else if (incomeReportInstance.grouping3) {
                                    incomeReportInstance.errorMessage(field: 'grouping2', code: 'incomeReport.groupings',
                                            default: 'Groupings must be entered in the order Grouping 1, Grouping 2 and then Grouping 3')
                                    valid = false
                                }
                            } else {
								incomeReportInstance.errorMessage(field: 'grouping1', code: 'not.found', domain: 'codeElementValue', value: incomeReportInstance.grouping1.id)
                                valid = false
                            }
                        } else if (incomeReportInstance.grouping2 || incomeReportInstance.grouping3) {
                            incomeReportInstance.errorMessage(field: 'grouping1', code: 'incomeReport.groupings',
                                    default: 'Groupings must be entered in the order Grouping 1, Grouping 2 and then Grouping 3')
                            valid = false
                        }
                    }
                } else {
					incomeReportInstance.errorMessage(field: 'period', code: 'not.found', domain: 'period', value: incomeReportInstance.period?.id)
                    valid = false
                }
            } else {
				incomeReportInstance.errorMessage(field: 'format', code: 'not.found', domain: 'profitReportFormat', value: incomeReportInstance.format?.id)
                valid = false
            }
        }

        if (valid) {
            params.p_detailed = "${incomeReportInstance.detailed}"
            def result = utilService.demandRunFromParams('ieReport', params)
            if (result instanceof String) {
                flash.message = result
                valid = false
            } else {
                flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            }
        }

        if (valid) {
            redirect(controller: 'systemMenu', action: 'display')
        } else {
            def formatInstanceList = []
            for (it in ProfitReportFormat.findAllByCompany(company, [sort: 'name'])) {
                if (it.lines?.size()) formatInstanceList << it  // Only show them formats that have lines defined
            }

            def periodInstanceList = bookService.getUsedPeriods(company)
            periodInstanceList = periodInstanceList.reverse()
            def valueLists = [:]
            for (element in codeElementInstanceList) {
                key = element.elementNumber.toString()
                fragment = bookService.createElementAccessFragment('x', element)
                if (fragment != null) {     // If they have some sort of access
                    val = fragment ? 'from CodeElementValue as x where x.element = ? and ' + fragment + ' order by x.code' : 'from CodeElementValue as x where x.element = ? order by x.code'
                    valueLists.put(key, CodeElementValue.findAll(val, [element]))
                }
            }

            def groupingList = [CodeElement.findByCompanyAndElementNumber(company, (byte) 1)]
            groupingList.addAll(codeElementInstanceList)

            def model = [incomeReportInstance: incomeReportInstance, formatInstanceList: formatInstanceList, periodInstanceList: periodInstanceList,
                    codeElementInstanceList: codeElementInstanceList, valueLists: valueLists, groupingList: groupingList]
            render(view: 'incomeReport', model: model)
        }
    }

    def balanceReport() {
        def company = utilService.currentCompany()
        def balanceReportInstance = new BalanceReport()
        def formatInstanceList = []
        for (it in BalanceReportFormat.findAllByCompany(company, [sort: 'name'])) {
            if (it.lines?.size()) formatInstanceList << it  // Only show them formats that have lines defined
        }

        def periodInstanceList = bookService.getUsedPeriods(company)
        balanceReportInstance.period = bookService.selectPeriod(periodInstanceList)
        periodInstanceList = periodInstanceList.reverse()
        def groupingList = [CodeElement.findByCompanyAndElementNumber(company, (byte) 1)]

        [balanceReportInstance: balanceReportInstance, formatInstanceList: formatInstanceList, periodInstanceList: periodInstanceList, groupingList: groupingList]
    }

    def balanceReporting(BalanceReport balanceReportInstance) {
        def company = utilService.currentCompany()
        def valid = !balanceReportInstance.hasErrors()
        if (valid) {
            if (balanceReportInstance.format?.securityCode == company.securityCode && balanceReportInstance.format.lines?.size()) {
                params.p_formatId = balanceReportInstance.format.id.toString()
                if (balanceReportInstance.period?.securityCode == company.securityCode && balanceReportInstance.period.status != 'new') {
                    params.p_periodId = balanceReportInstance.period.id.toString()
                    if (balanceReportInstance.grouping1) {
                        if (balanceReportInstance.grouping1.securityCode == company.securityCode && balanceReportInstance.grouping1.elementNumber == (byte) 1) {
                            params.p_grouping1 = balanceReportInstance.grouping1.id.toString()
                        } else {
							balanceReportInstance.errorMessage(field: 'grouping1', code: 'not.found', domain: 'codeElementValue', value: balanceReportInstance.grouping1.id)
                            valid = false
                        }
                    }
                } else {
					balanceReportInstance.errorMessage(field: 'period', code: 'not.found', domain: 'period', value: balanceReportInstance.period?.id)
                    valid = false
                }
            } else {
				balanceReportInstance.errorMessage(field: 'format', code: 'not.found', domain: 'balanceReportFormat', value: balanceReportInstance.format?.id)
                valid = false
            }
        }

        if (valid) {
            params.p_detailed = "${balanceReportInstance.detailed}"
            def result = utilService.demandRunFromParams('bsReport', params)
            if (result instanceof String) {
                flash.message = result
                valid = false
            } else {
                flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            }
        }

        if (valid) {
            redirect(controller: 'systemMenu', action: 'display')
        } else {
            def formatInstanceList = []
            for (it in BalanceReportFormat.findAllByCompany(company, [sort: 'name'])) {
                if (it.lines?.size()) formatInstanceList << it  // Only show them formats that have lines defined
            }

            def periodInstanceList = bookService.getUsedPeriods(company)
            periodInstanceList = periodInstanceList.reverse()
            def groupingList = [CodeElement.findByCompanyAndElementNumber(company, (byte) 1)]
            def model = [balanceReportInstance: balanceReportInstance, formatInstanceList: formatInstanceList, periodInstanceList: periodInstanceList, groupingList: groupingList]
            render(view: 'balanceReport', model: model)
        }
    }

    private getScopeList() {
        def list = []
        list << [code: 'combined', name: message(code: 'report.postings.combined', default: 'All sections combined (1 report)')]
        list << [code: 'ie', name: message(code: 'report.postings.ie', default: 'Income & Expenditure sections only (1 report)')]
        list << [code: 'bs', name: message(code: 'report.postings.bs', default: 'Balance Sheet sections only (1 report)')]
        list << [code: 'split', name: message(code: 'report.postings.split', default: 'I&E and B/S sections separately (2 reports)')]
        list << [code: 'separate', name: message(code: 'report.postings.separate', default: 'Each section separately (multiple reports)')]
        list << [code: 'section', name: message(code: 'report.postings.section', default: 'A specific section (1 report)')]
        list << [code: 'account', name: message(code: 'report.postings.account', default: 'A specific account (1 report)')]
        return list
    }
}