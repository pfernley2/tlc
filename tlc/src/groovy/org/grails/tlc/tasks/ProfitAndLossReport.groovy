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
package org.grails.tlc.tasks

import org.grails.tlc.sys.SystemWorkarea
import org.grails.tlc.sys.TaskExecutable
import org.grails.tlc.books.*

public class ProfitAndLossReport extends TaskExecutable {

    private static final BASE_SELECTED_DATA = ['selectedPeriodActual', 'selectedPeriodAdjusted', 'selectedPeriodBudget', 'selectedYearAdjusted']
    private static final EXTRA_SELECTED_DATA = ['selectedYearActual', 'selectedYearBudget']
    private static final BASE_COMPARATIVE_DATA = ['comparativePeriodActual', 'comparativePeriodAdjusted', 'comparativePeriodBudget', 'comparativeYearAdjusted']
    private static final EXTRA_COMPARATIVE_DATA = ['comparativeYearActual', 'comparativeYearBudget']
    private static final PERCENTAGE_TESTS = ['variance', 'percentage']

    def execute() {
        def session = runSessionFactory.currentSession
        def reportParams = [:]
        def format = ProfitReportFormat.findByIdAndCompany(params.formatId, company)
        if (!format) {
            completionMessage = utilService.standardMessage('not.found', 'profitReportFormat', params.formatId)
            return false
        }

        if (!format.lines?.size()) {
            completionMessage = message(code: 'profitReportFormat.no.lines', default: 'The Report Format has no lines')
            return false
        }

        def selectedPeriod = Period.findByIdAndSecurityCode(params.periodId, company.securityCode)
        if (!selectedPeriod || selectedPeriod.status == 'new') {
            completionMessage = utilService.standardMessage('not.found', 'period', params.periodId)
            return false
        }

        def selectedPeriodList = Period.findAllByYearAndValidFromLessThanEquals(selectedPeriod.year, selectedPeriod.validFrom)
        yield()

        def comparativePeriod, comparativePeriodList
        def yearList = Year.findAllByCompanyAndValidFromLessThan(company, selectedPeriod.year.validFrom, [sort: 'validFrom', order: 'desc', max: 1])
        if (yearList) {
            def cal = Calendar.getInstance()
            cal.setTime(selectedPeriod.validFrom)
            cal.add(Calendar.YEAR, -1)
            comparativePeriodList = Period.findAllByYearAndValidFromLessThanEquals(yearList[0], cal.getTime(), [sort: 'validFrom'])
            if (comparativePeriodList) comparativePeriod = comparativePeriodList[-1]
        }

        yield()
        def valueId, val
        def selectedElementValueList = []
        def elements = CodeElement.findAllByCompanyAndElementNumberGreaterThan(company, (byte) 1, [sort: 'elementNumber'])
        for (element in elements) {
            if (bookService.createElementAccessFragment('x', element, company, user) != null) {     // They have some access to this element
                reportParams."txtElement${element.elementNumber}" = element.name
                valueId = params."element${element.elementNumber}"
                if (valueId) {
                    val = CodeElementValue.findByIdAndElement(valueId, element)
                    if (val && bookService.hasCodeElementValueAccess(val, company, user)) {
                        selectedElementValueList << val
                        reportParams."valElement${element.elementNumber}" = val.code
                    } else {
                        completionMessage = utilService.standardMessage('not.found', 'codeElementValue', valueId)
                        return false
                    }
                } else {
                    reportParams."valElement${element.elementNumber}" = message(code: 'generic.all.selection', default: '-- all --')
                }
            }
        }

        def title = format.name
        reportParams.txtPeriod = message(code: 'report.period.label', default: 'Period')
        reportParams.valPeriod = selectedPeriod.code
        reportParams.reportTitle = setPlaceHolders(format.title, selectedPeriod, comparativePeriod)
        reportParams.reportSubTitle = setPlaceHolders(format.subTitle, selectedPeriod, comparativePeriod)
        reportParams.txtHeading1 = setPlaceHolders(format.column1Heading, selectedPeriod, comparativePeriod)
        reportParams.txtHeading2 = setPlaceHolders(format.column2Heading, selectedPeriod, comparativePeriod)
        reportParams.txtHeading3 = setPlaceHolders(format.column3Heading, selectedPeriod, comparativePeriod)
        reportParams.txtHeading4 = setPlaceHolders(format.column4Heading, selectedPeriod, comparativePeriod)
        reportParams.txtSubHeading1 = setPlaceHolders(format.column1SubHeading, selectedPeriod, comparativePeriod)
        reportParams.txtSubHeading2 = setPlaceHolders(format.column2SubHeading, selectedPeriod, comparativePeriod)
        reportParams.txtSubHeading3 = setPlaceHolders(format.column3SubHeading, selectedPeriod, comparativePeriod)
        reportParams.txtSubHeading4 = setPlaceHolders(format.column4SubHeading, selectedPeriod, comparativePeriod)
        reportParams.isPercentage1 = PERCENTAGE_TESTS.contains(format.column1Calculation)
        reportParams.isPercentage2 = PERCENTAGE_TESTS.contains(format.column2Calculation)
        reportParams.isPercentage3 = PERCENTAGE_TESTS.contains(format.column3Calculation)
        reportParams.isPercentage4 = PERCENTAGE_TESTS.contains(format.column4Calculation)

        yield()
        def baseSelectedPID = utilService.getNextProcessId()
        def extraSelectedPID = utilService.getNextProcessId()
        def baseComparativePID = utilService.getNextProcessId()
        def extraComparativePID = utilService.getNextProcessId()
        def interimPID = utilService.getNextProcessId()
        def reportPID = utilService.getNextProcessId()
        reportParams.pid = reportPID
        def needBaseSelected = usesBaseSelected(format)
        def needExtraSelected = usesExtraSelected(format)
        def needBaseComparative = comparativePeriod && usesBaseComparative(format)
        def needExtraComparative = comparativePeriod && usesExtraComparative(format)
        if (!needBaseSelected && !needExtraSelected && !needBaseComparative && !needExtraComparative) {
            completionMessage = message(code: 'profitReportFormat.no.data', default: 'The report would not have retrieved any data')
            return false
        }

        // Find out what groupings they've asked for, if any
        def groupings = []
        for (int i = 1; i <= 3; i++) {
            valueId = params."grouping${i}"
            if (valueId) {
                val = CodeElement.get(valueId)
                if (val && bookService.createElementAccessFragment('x', val, company, user) != null) {
                    groupings << val
                } else {
					completionMessage = utilService.standardMessage('not.found', 'codeElement', valueId)
                    return false
                }
            } else {
                break
            }
        }

        // Note whether they'va asked for detailed accounts to be printed or not
        def detailed = params.detailed

        // Work out the last total line
        def lastTotalLine = 0
        for (formatLine in format.lines) {
            if (formatLine.accumulation) lastTotalLine = formatLine.lineNumber
        }

        if (!loadData(session, format, baseSelectedPID, extraSelectedPID, baseComparativePID, extraComparativePID,
                selectedPeriod, comparativePeriod, selectedPeriodList, comparativePeriodList, selectedElementValueList,
                needExtraSelected, needBaseComparative, needExtraComparative, groupings)) {
            completionMessage = message(code: 'report.no.access', default: 'You do not have permission to access any accounts and therefore cannot run this report.')
            return false
        }

        yield()
        def totals = [:]
        def accumulators = [:]
        def baseList = SystemWorkarea.findAllByProcess(baseSelectedPID)
        for (rec in baseList) {
            if (!createInterimData(session, rec, format, baseSelectedPID, extraSelectedPID, baseComparativePID, extraComparativePID, interimPID,
                    totals, needExtraSelected, needBaseComparative, needExtraComparative)) {
                clearWorkarea([baseSelectedPID, extraSelectedPID, baseComparativePID, extraComparativePID, interimPID])
                completionMessage = message(code: 'generic.workarea', default: 'Unable to update the work table')
                return false
            }

            yield()
        }

        clearWorkarea([baseSelectedPID, extraSelectedPID, baseComparativePID, extraComparativePID])
        yield()
        def reportLine
        def error = [:]
        def reportLineNumber = 1L
        def priorGroupingHadTotal = false
        def totalPrefix = message(code: 'incomeReport.total.prefix', default: 'Total')
        for (formatLine in format.lines) {
            if (formatLine.section) {
                accumulators.put(formatLine.lineNumber.toString(), [formatLine.section.id])
                reportLine = new ReportSectionGrouping(formatLine.section, formatLine.text, groupings, interimPID)
                reportLineNumber = reportLine.print(session, interimPID, reportPID, reportLineNumber, format,
                        formatLine.lineNumber, totals, totalPrefix, detailed, priorGroupingHadTotal)
                priorGroupingHadTotal = reportLine.lastLineWasTotal
            } else if (formatLine.accumulation) {
                reportLine = new ReportAccumulationGrouping(formatLine, accumulators, groupings, interimPID, error)
                if (error) {
                    clearWorkarea([interimPID, reportPID])
                    completionMessage = message(code: error.code, args: error.args, default: error.default)
                    return false
                }

                reportLineNumber = reportLine.print(session, interimPID, reportPID, reportLineNumber, format,
                        formatLine.lineNumber, totals, totalPrefix, detailed, priorGroupingHadTotal, formatLine.lineNumber == lastTotalLine)
                priorGroupingHadTotal = true
            } else {    // Just a text (or blank) line
                reportLine = new SystemWorkarea(process: reportPID, identifier: reportLineNumber++, integer1: formatLine.lineNumber, string1: formatLine.text ?: ' ')
                SystemWorkarea.withTransaction {status ->
                    if (!reportLine.saveThis()) {
                        status.setRollbackOnly()
                        reportLineNumber = 0
                    }
                }

                session.evict(reportLine)
                priorGroupingHadTotal = false
            }

            if (!reportLineNumber) {
                clearWorkarea([interimPID, reportPID])
                completionMessage = message(code: 'generic.workarea', default: 'Unable to update the work table')
                return false
            }

            session.evict(formatLine)
            yield()
        }

        clearWorkarea(interimPID)
        yield()

        def pdfFile = createReportPDF('ProfitAndLoss', reportParams)
        yield()

        clearWorkarea(reportPID)
        yield()

        mailService.sendMail {
			multipart true
            to user.email
            subject title
            body(view: '/emails/genericReport', model: [companyInstance: company, systemUserInstance: user, title: title])
            attach pdfFile
        }

        yield()
        pdfFile.delete()

        return true
    }

    private usesBaseSelected(format) {
        return (BASE_SELECTED_DATA.contains(format.column1PrimaryData) || BASE_SELECTED_DATA.contains(format.column1SecondaryData) ||
                BASE_SELECTED_DATA.contains(format.column2PrimaryData) || BASE_SELECTED_DATA.contains(format.column2SecondaryData) ||
                BASE_SELECTED_DATA.contains(format.column3PrimaryData) || BASE_SELECTED_DATA.contains(format.column3SecondaryData) ||
                BASE_SELECTED_DATA.contains(format.column4PrimaryData) || BASE_SELECTED_DATA.contains(format.column4SecondaryData))
    }

    private usesExtraSelected(format) {
        return (EXTRA_SELECTED_DATA.contains(format.column1PrimaryData) || EXTRA_SELECTED_DATA.contains(format.column1SecondaryData) ||
                EXTRA_SELECTED_DATA.contains(format.column2PrimaryData) || EXTRA_SELECTED_DATA.contains(format.column2SecondaryData) ||
                EXTRA_SELECTED_DATA.contains(format.column3PrimaryData) || EXTRA_SELECTED_DATA.contains(format.column3SecondaryData) ||
                EXTRA_SELECTED_DATA.contains(format.column4PrimaryData) || EXTRA_SELECTED_DATA.contains(format.column4SecondaryData))
    }

    private usesBaseComparative(format) {
        return (BASE_COMPARATIVE_DATA.contains(format.column1PrimaryData) || BASE_COMPARATIVE_DATA.contains(format.column1SecondaryData) ||
                BASE_COMPARATIVE_DATA.contains(format.column2PrimaryData) || BASE_COMPARATIVE_DATA.contains(format.column2SecondaryData) ||
                BASE_COMPARATIVE_DATA.contains(format.column3PrimaryData) || BASE_COMPARATIVE_DATA.contains(format.column3SecondaryData) ||
                BASE_COMPARATIVE_DATA.contains(format.column4PrimaryData) || BASE_COMPARATIVE_DATA.contains(format.column4SecondaryData))
    }

    private usesExtraComparative(format) {
        return (EXTRA_COMPARATIVE_DATA.contains(format.column1PrimaryData) || EXTRA_COMPARATIVE_DATA.contains(format.column1SecondaryData) ||
                EXTRA_COMPARATIVE_DATA.contains(format.column2PrimaryData) || EXTRA_COMPARATIVE_DATA.contains(format.column2SecondaryData) ||
                EXTRA_COMPARATIVE_DATA.contains(format.column3PrimaryData) || EXTRA_COMPARATIVE_DATA.contains(format.column3SecondaryData) ||
                EXTRA_COMPARATIVE_DATA.contains(format.column4PrimaryData) || EXTRA_COMPARATIVE_DATA.contains(format.column4SecondaryData))
    }

    // Loads system workarea table with the raw data needed to fill the report. Returns true if succeeded or false if the user has no access rights
    private loadData(session, format, baseSelectedPID, extraSelectedPID, baseComparativePID, extraComparativePID,
                     selectedPeriod, comparativePeriod, selectedPeriodList, comparativePeriodList, selectedElementValueList,
                     needExtraSelected, needBaseComparative, needExtraComparative, groupings) {

        // At the time of writing there was a fault in Hibernate which stopped us using HQL for the following.
        // We therefore use raw SQL - ugly, but it works! Hibernate was translating the HQL to SQL incorrectly.
        def baseSelectedSQL, extraSelectedSQL, baseComparativeSQL, extraComparativeSQL

        // Work out the grouping info we will need
        def fieldClause = ''
        def elementClause = ''
        for (int i = 0; i < groupings.size(); i++) {
            fieldClause += ", long${i + 2}"
            elementClause += ", a.element${groupings[i].elementNumber}_id"
        }

        baseSelectedSQL = 'insert into system_workarea (process, identifier, long1, string1, string2, decimal1, decimal2, decimal3, decimal4, version' + fieldClause +
                ') select ' + baseSelectedPID.toString() +
                ', b.account_id, a.section_id, a.code, a.name, b.company_transaction_total, b.company_adjustment_total, b.company_closing_balance, b.company_budget, 0' + elementClause +
                ' from account as a, chart_section as s, general_balance as b where a.security_code = ' +
                company.securityCode.toString() + ' and a.section_id = s.id and s.section_type = \'ie\' and b.account_id = a.id and b.period_id = ' + selectedPeriod.id.toString()
        def exclusions = []
        for (val in selectedElementValueList) {
            baseSelectedSQL += ' and a.element' + val.element.elementNumber + '_id = ' + val.id.toString()
            exclusions << val.element
        }

        def fragment = bookService.createAccountAccessFragment('a', exclusions, true, company, user)
        if (fragment == null) return false
        if (fragment) baseSelectedSQL += ' and ' + fragment

        if (needExtraSelected) {
            extraSelectedSQL = 'insert into system_workarea (process, identifier, decimal1, decimal2, version) select ' +
                    extraSelectedPID.toString() + ', b.account_id, sum(b.company_transaction_total), sum(b.company_budget), sum(b.version) ' +
                    'from general_balance as b, system_workarea as w where w.process = ' +
                    baseSelectedPID.toString() + ' and w.identifier = b.account_id and b.period_id in ('
            for (int i = 0; i < selectedPeriodList.size(); i++) {
                if (i) extraSelectedSQL += ', '
                extraSelectedSQL += selectedPeriodList[i].id.toString()
            }

            extraSelectedSQL += ') group by b.account_id'
        }

        if (needBaseComparative) {
            baseComparativeSQL = 'insert into system_workarea (process, identifier, decimal1, decimal2, decimal3, decimal4, version) select ' +
                    baseComparativePID.toString() + ', b.account_id, b.company_transaction_total, b.company_adjustment_total, b.company_closing_balance, b.company_budget, 0 ' +
                    'from general_balance as b, system_workarea as w where w.process = ' +
                    baseSelectedPID.toString() + ' and w.identifier = b.account_id and b.period_id = ' + comparativePeriod.id.toString()
        }

        if (needExtraComparative) {
            extraComparativeSQL = 'insert into system_workarea (process, identifier, decimal1, decimal2, version) select ' +
                    extraComparativePID.toString() + ', b.account_id, sum(b.company_transaction_total), sum(b.company_budget), sum(b.version) ' +
                    'from general_balance as b, system_workarea as w where w.process = ' +
                    baseSelectedPID.toString() + ' and w.identifier = b.account_id and b.period_id in ('
            for (int i = 0; i < comparativePeriodList.size(); i++) {
                if (i) extraComparativeSQL += ', '
                extraComparativeSQL += comparativePeriodList[i].id.toString()
            }

            extraComparativeSQL += ') group by b.account_id'
        }

        def statement = session.connection().createStatement()
        def lock = bookService.getCompanyLock(company)
        if (selectedPeriod.status != 'closed') lock.lock()  // Don't lock the company unless we have to
        try {
            SystemWorkarea.withTransaction {status ->

                // Always get the basic stuff for the selected period, plus the extra if needed
                statement.executeUpdate(baseSelectedSQL)
                if (needExtraSelected) statement.executeUpdate(extraSelectedSQL)

                // Only do the following within the lock if we absolutely have to
                if (needBaseComparative && comparativePeriod.status != 'closed') statement.executeUpdate(baseComparativeSQL)
                if (needExtraComparative && comparativePeriod.status != 'closed') statement.executeUpdate(extraComparativeSQL)
            }
        } finally {
            if (selectedPeriod.status != 'closed') lock.unlock()
            statement.close()
        }

        // Check if still need to load comparative data outside of the lock
        if (needBaseComparative && comparativePeriod.status == 'closed') {
            statement = session.connection().createStatement()
            try {
                SystemWorkarea.withTransaction {status ->
                    statement.executeUpdate(baseComparativeSQL)
                }
            } finally {
                statement.close()
            }
        }

        if (needExtraComparative && comparativePeriod.status == 'closed') {
            statement = session.connection().createStatement()
            try {
                SystemWorkarea.withTransaction {status ->
                    statement.executeUpdate(extraComparativeSQL)
                }
            } finally {
                statement.close()
            }
        }

        return true
    }

    private setPlaceHolders(text, selectedPeriod, comparativePeriod) {
        if (text) {
            text = text.replace('{sPd}', selectedPeriod.code)
            text = text.replace('{sYr}', selectedPeriod.year.code)
            if (comparativePeriod) {
                text = text.replace('{cPd}', comparativePeriod.code)
                text = text.replace('{cYr}', comparativePeriod.year.code)
            } else {
                def msg = message(code: 'generic.not.applicable', default: 'n/a')
                text = text.replace('{cPd}', msg)
                text = text.replace('{cYr}', msg)
            }
        } else {
            text = ''
        }

        return text
    }

    // Create the interim data to be used by the report and return true. Return false if could not save the data.
    private createInterimData(session, baseSelected, format, baseSelectedPID, extraSelectedPID, baseComparativePID, extraComparativePID, interimPID,
                              totals, needExtraSelected, needBaseComparative, needExtraComparative) {
        def extraSelected, baseComparative, extraComparative
        if (needExtraSelected) extraSelected = SystemWorkarea.findByProcessAndIdentifier(extraSelectedPID, baseSelected.identifier)
        if (needBaseComparative) baseComparative = SystemWorkarea.findByProcessAndIdentifier(baseComparativePID, baseSelected.identifier)
        if (needExtraComparative) extraComparative = SystemWorkarea.findByProcessAndIdentifier(extraComparativePID, baseSelected.identifier)
        def data = new SystemWorkarea(process: interimPID, identifier: baseSelected.identifier, string1: (baseSelected.string1 + ' ' + baseSelected.string2),
                long1: baseSelected.long1, long2: baseSelected.long2, long3: baseSelected.long3, long4: baseSelected.long4,
                decimal1: 0.0, decimal2: 0.0, decimal3: 0.0, decimal4: 0.0, decimal5: 0.0, decimal6: 0.0,
                decimal7: 0.0, decimal8: 0.0, decimal9: 0.0, decimal10: 0.0, decimal11: 0.0, decimal12: 0.0)
        def primaryData, secondaryData, dataOption
        for (int i = 1; i <= 4; i++) {

            // Get any element value codes into the string'n' fields (used for grouping)
            if (i > 1) {
                dataOption = baseSelected."long${i}"
                if (dataOption) data."string${i}" = CodeElementValue.get(dataOption).code
            }

            // Now move on to assembling the actual interim data
            dataOption = format."column${i}PrimaryData"
            primaryData = getColumnData(dataOption, baseSelected, extraSelected, baseComparative, extraComparative)
            switch (format."column${i}Calculation") {

                case 'difference':
                    primaryData -= getColumnData(format."column${i}SecondaryData", baseSelected, extraSelected, baseComparative, extraComparative)
                    break

                case 'variance':

                    // Need to store the primary data for possible future summation
                    data."decimal${i + 4}" = primaryData

                    // Get the secondary data since we need to store it for possible future summation
                    secondaryData = getColumnData(format."column${i}SecondaryData", baseSelected, extraSelected, baseComparative, extraComparative)
                    data."decimal${i + 8}" = secondaryData

                    primaryData -= secondaryData

                    // Don't need to do anything if the primary or secondary value is zero since the result must be zero
                    if (primaryData && secondaryData) {
                        primaryData = utilService.round((primaryData * 100.0) / secondaryData, 2)
                    } else {
                        primaryData = 0.0
                    }

                    break

                case 'percentage':

                    // Need to store the primary data for possible future summation
                    data."decimal${i + 4}" = primaryData

                    // Don't need to do anything if the primary value is zero since the result must be zero
                    if (primaryData) {
                        secondaryData = getTotal(format, dataOption, baseSelectedPID, extraSelectedPID, baseComparativePID, extraComparativePID, totals)
                        if (secondaryData) {
                            primaryData = utilService.round((primaryData * 100.0) / secondaryData, 2).abs() // Always display total percentages as positives
                        } else {
                            primaryData = 0.0   // Answer must be zero if secondary data is zero
                        }
                    }

                    break

                default:    // No calculation so just a straight copy
                    break
            }

            data."decimal${i}" = primaryData
        }

        SystemWorkarea.withTransaction {status ->
            if (!data.saveThis()) {
                data = null
                status.setRollbackOnly()
            }
        }

        if (!data) return false
        session.evict(data)
        session.evict(baseSelected)
        if (extraSelected) session.evict(baseSelected)
        if (baseComparative) session.evict(baseComparative)
        if (extraComparative) session.evict(extraComparative)
        return true
    }

    private getColumnData(dataOption, baseSelected, extraSelected, baseComparative, extraComparative) {
        switch (dataOption) {

            case 'selectedPeriodActual':
                return baseSelected?.decimal1 ?: 0.0
                break

            case 'selectedPeriodAdjusted':
                return (baseSelected?.decimal1 ?: 0.0) + (baseSelected?.decimal2 ?: 0.0)
                break

            case 'selectedPeriodBudget':
                return baseSelected?.decimal4 ?: 0.0
                break

            case 'selectedYearActual':
                return extraSelected?.decimal1 ?: 0.0
                break

            case 'selectedYearAdjusted':
                return baseSelected?.decimal3 ?: 0.0
                break

            case 'selectedYearBudget':
                return extraSelected?.decimal2 ?: 0.0
                break

            case 'comparativePeriodActual':
                return baseComparative?.decimal1 ?: 0.0
                break

            case 'comparativePeriodAdjusted':
                return (baseComparative?.decimal1 ?: 0.0) + (baseComparative?.decimal2 ?: 0.0)
                break

            case 'comparativePeriodBudget':
                return baseComparative?.decimal4 ?: 0.0
                break

            case 'comparativeYearActual':
                return extraComparative?.decimal1 ?: 0.0
                break

            case 'comparativeYearAdjusted':
                return baseComparative?.decimal3 ?: 0.0
                break

            case 'comparativeYearBudget':
                return extraComparative?.decimal2 ?: 0.0
                break

            default:
                return 0.0
        }
    }

    private clearWorkarea(pids) {
        if (pids instanceof Long) pids = [pids]
        for (pid in pids) {
            SystemWorkarea.withTransaction {status ->
                SystemWorkarea.executeUpdate('delete from SystemWorkarea where process = ?', [pid])
            }
        }
    }

    // Load any total percentage required
    private getTotal(format, dataOption, baseSelectedPID, extraSelectedPID, baseComparativePID, extraComparativePID, totals) {
        def val = totals.get(dataOption)
        if (val == null) {
            if (!format.percentages?.size()) {
                val = 0.0
                totals.put(dataOption, val)
                return val
            }

            def sql
            def parameters = [baseSelectedPID]
            switch (dataOption) {

                case 'selectedPeriodActual':
                    sql = 'select sum(x.decimal1) from SystemWorkarea as x where x.process = ?'
                    break

                case 'selectedPeriodAdjusted':
                    sql = 'select sum(x.decimal1 + x.decimal2) from SystemWorkarea as x where x.process = ?'
                    break

                case 'selectedPeriodBudget':
                    sql = 'select sum(x.decimal4) from SystemWorkarea as x where x.process = ?'
                    break

                case 'selectedYearActual':
                    sql = 'select sum(y.decimal1) from SystemWorkarea as x, SystemWorkarea as y where x.process = ? and y.process = ? and x.identifier = y.identifier'
                    parameters << extraSelectedPID
                    break

                case 'selectedYearAdjusted':
                    sql = 'select sum(x.decimal3) from SystemWorkarea as x where x.process = ?'
                    break

                case 'selectedYearBudget':
                    sql = 'select sum(y.decimal2) from SystemWorkarea as x, SystemWorkarea as y where x.process = ? and y.process = ? and x.identifier = y.identifier'
                    parameters << extraSelectedPID
                    break

                case 'comparativePeriodActual':
                    sql = 'select sum(y.decimal1) from SystemWorkarea as x, SystemWorkarea as y where x.process = ? and y.process = ? and x.identifier = y.identifier'
                    parameters << baseComparativePID
                    break

                case 'comparativePeriodAdjusted':
                    sql = 'select sum(y.decimal1 + y.decimal2) from SystemWorkarea as x, SystemWorkarea as y where x.process = ? and y.process = ? and x.identifier = y.identifier'
                    parameters << baseComparativePID
                    break

                case 'comparativePeriodBudget':
                    sql = 'select sum(y.decimal4) from SystemWorkarea as x, SystemWorkarea as y where x.process = ? and y.process = ? and x.identifier = y.identifier'
                    parameters << baseComparativePID
                    break

                case 'comparativeYearActual':
                    sql = 'select sum(y.decimal1) from SystemWorkarea as x, SystemWorkarea as y where x.process = ? and y.process = ? and x.identifier = y.identifier'
                    parameters << extraComparativePID
                    break

                case 'comparativeYearAdjusted':
                    sql = 'select sum(y.decimal3) from SystemWorkarea as x, SystemWorkarea as y where x.process = ? and y.process = ? and x.identifier = y.identifier'
                    parameters << baseComparativePID
                    break

                case 'comparativeYearBudget':
                    sql = 'select sum(y.decimal2) from SystemWorkarea as x, SystemWorkarea as y where x.process = ? and y.process = ? and x.identifier = y.identifier'
                    parameters << extraComparativePID
                    break

                default:
                    val = 0.0
                    totals.put(dataOption, val)
                    return val
            }

            sql += ' and x.long1 in ('
            def ftt = true
            for (it in format.percentages) {
                if (ftt) {
                    sql += '?'
                    ftt = false
                } else {
                    sql += ', ?'
                }

                parameters << it.section.id
            }

            sql += ')'
            val = SystemWorkarea.executeQuery(sql, parameters)[0] ?: 0.0
            totals.put(dataOption, val)
        }

        return val
    }
}
