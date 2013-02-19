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

public class BalanceSheetReport extends TaskExecutable {

    private static final BASE_SELECTED_DATA = ['selectedPeriodMovement', 'selectedPeriodBudget', 'selectedYearBalance', 'selectedYearMovement']
    private static final EXTRA_SELECTED_DATA = ['selectedYearBudget']
    private static final OPENING_SELECTED_DATA = ['selectedYearMovement']
    private static final BASE_COMPARATIVE_DATA = ['comparativePeriodMovement', 'comparativePeriodBudget', 'comparativeYearBalance', 'comparativeYearMovement']
    private static final EXTRA_COMPARATIVE_DATA = ['comparativeYearBudget']
    private static final OPENING_COMPARATIVE_DATA = ['comparativeYearMovement']
    private static final PERCENTAGE_TESTS = ['variance']

    def execute() {
        def session = runSessionFactory.currentSession
        def reportParams = [:]
        def format = BalanceReportFormat.findByIdAndCompany(params.formatId, company)
        if (!format) {
            completionMessage = utilService.standardMessage('not.found', 'balanceReportFormat', params.formatId)
            return false
        }

        if (!format.lines?.size()) {
            completionMessage = message(code: 'balanceReportFormat.no.lines', default: 'The Report Format has no lines')
            return false
        }

        def selectedPeriod = Period.findByIdAndSecurityCode(params.periodId, company.securityCode)
        if (!selectedPeriod || selectedPeriod.status == 'new') {
            completionMessage = utilService.standardMessage('not.found', 'period', params.periodId)
            return false
        }

        def selectedPeriodList = Period.findAllByYearAndValidFromLessThanEquals(selectedPeriod.year, selectedPeriod.validFrom, [sort: 'validFrom'])
        def selectedOpeningPeriod = selectedPeriodList[0]
        yield()

        def comparativePeriod, comparativePeriodList, comparativeOpeningPeriod
        def yearList = Year.findAllByCompanyAndValidFromLessThan(company, selectedPeriod.year.validFrom, [sort: 'validFrom', order: 'desc', max: 1])
        if (yearList) {
            def cal = Calendar.getInstance()
            cal.setTime(selectedPeriod.validFrom)
            cal.add(Calendar.YEAR, -1)
            comparativePeriodList = Period.findAllByYearAndValidFromLessThanEquals(yearList[0], cal.getTime(), [sort: 'validFrom'])
            if (comparativePeriodList) {
                comparativePeriod = comparativePeriodList[-1]
                comparativeOpeningPeriod = comparativePeriodList[0]
            }
        }

        yield()
        def retainedAccount = bookService.getControlAccount(company, 'retained')
        if (!retainedAccount) {
            completionMessage = message(code: 'document.no.control', args: ['retained'], default: 'Could not find the retained control account in the General Ledger')
            return false
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
        def openingSelectedPID = utilService.getNextProcessId()
        def baseComparativePID = utilService.getNextProcessId()
        def extraComparativePID = utilService.getNextProcessId()
        def openingComparativePID = utilService.getNextProcessId()
        def interimPID = utilService.getNextProcessId()
        def reportPID = utilService.getNextProcessId()
        reportParams.pid = reportPID
        def needBaseSelected = usesBaseSelected(format)
        def needExtraSelected = usesExtraSelected(format)
        def needYearMovementSelected = usesYearMovementSelected(format)
        def needBaseComparative = comparativePeriod && usesBaseComparative(format)
        def needExtraComparative = comparativePeriod && usesExtraComparative(format)
        def needYearMovementComparative = comparativeOpeningPeriod && usesYearMovementComparative(format)
        if (!needBaseSelected && !needExtraSelected && !needYearMovementSelected && !needBaseComparative && !needExtraComparative && !needYearMovementComparative) {
            completionMessage = message(code: 'balanceReportFormat.no.data', default: 'The report would not have retrieved any data')
            return false
        }

        // Find out what groupings they've asked for, if any
        def groupings = []
        def valueId = params."grouping1"
        if (valueId) {
            def val = CodeElement.get(valueId)
            if (val) {
                groupings << val
            } else {
                completionMessage = utilService.standardMessage('not.found', 'codeElement', valueId)
                return false
            }
        }

        // Note whether they'va asked for detailed accounts to be printed or not
        def detailed = params.detailed

        // Work out the last total lines (one for each 'side' of the balance sheet)
        def lastTotalLines = [0, 0]
        for (formatLine in format.lines) {
            if (formatLine.accumulation) {
                lastTotalLines[1] = formatLine.lineNumber
            } else if (!formatLine.section && formatLine.text == '<=>') {
                lastTotalLines[0] = lastTotalLines[1]
            }
        }

        yield()
        loadData(session, format, baseSelectedPID, extraSelectedPID, openingSelectedPID, baseComparativePID, extraComparativePID, openingComparativePID,
                selectedPeriod, selectedOpeningPeriod, comparativePeriod, comparativeOpeningPeriod, selectedPeriodList, comparativePeriodList,
                needYearMovementSelected, needExtraSelected, needBaseComparative, needYearMovementComparative, needExtraComparative, groupings, retainedAccount)
        yield()

        def accumulators = [:]
        def baseList = SystemWorkarea.findAllByProcess(baseSelectedPID)
        for (rec in baseList) {
            if (!createInterimData(session, rec, format, baseSelectedPID, extraSelectedPID, openingSelectedPID, baseComparativePID, extraComparativePID, openingComparativePID, interimPID,
                    needExtraSelected, needYearMovementSelected, needBaseComparative, needExtraComparative, needYearMovementComparative)) {
                clearWorkarea([baseSelectedPID, extraSelectedPID, openingSelectedPID, baseComparativePID, extraComparativePID, openingComparativePID, interimPID])
                completionMessage = message(code: 'generic.workarea', default: 'Unable to update the work table')
                return false
            }

            yield()
        }

        clearWorkarea([baseSelectedPID, extraSelectedPID, openingSelectedPID, baseComparativePID, extraComparativePID, openingComparativePID])
        yield()
        def reportLine
        def error = [:]
        def reportLineNumber = 1L
        def priorGroupingHadTotal = false
        def totalPrefix = message(code: 'balanceReport.total.prefix', default: 'Total')
        for (formatLine in format.lines) {
            if (formatLine.section) {
                accumulators.put(formatLine.lineNumber.toString(), [formatLine.section.id])
                reportLine = new ReportSectionGrouping(formatLine.section, formatLine.text, groupings, interimPID)
                reportLineNumber = reportLine.print(session, interimPID, reportPID, reportLineNumber, format,
                        formatLine.lineNumber, null, totalPrefix, detailed, priorGroupingHadTotal)
                priorGroupingHadTotal = reportLine.lastLineWasTotal
            } else if (formatLine.accumulation) {
                reportLine = new ReportAccumulationGrouping(formatLine, accumulators, groupings, interimPID, error)
                if (error) {
                    clearWorkarea([interimPID, reportPID])
                    completionMessage = message(code: error.code, args: error.args, default: error.default)
                    return false
                }

                reportLineNumber = reportLine.print(session, interimPID, reportPID, reportLineNumber, format,
                        formatLine.lineNumber, null, totalPrefix, detailed, priorGroupingHadTotal, lastTotalLines.contains(formatLine.lineNumber))
                priorGroupingHadTotal = true
            } else {    // A text (or blank) line that might also be a divider line
                reportLine = new SystemWorkarea(process: reportPID, identifier: reportLineNumber++, integer1: formatLine.lineNumber)
                reportLine.string1 = (formatLine.text && formatLine.text != '<=>') ? formatLine.text : ' '  // Turn the divider in to a blank line if required
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

        def pdfFile = createReportPDF('BalanceSheet', reportParams)
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

    private usesYearMovementSelected(format) {
        return (OPENING_SELECTED_DATA.contains(format.column1PrimaryData) || OPENING_SELECTED_DATA.contains(format.column1SecondaryData) ||
                OPENING_SELECTED_DATA.contains(format.column2PrimaryData) || OPENING_SELECTED_DATA.contains(format.column2SecondaryData) ||
                OPENING_SELECTED_DATA.contains(format.column3PrimaryData) || OPENING_SELECTED_DATA.contains(format.column3SecondaryData) ||
                OPENING_SELECTED_DATA.contains(format.column4PrimaryData) || OPENING_SELECTED_DATA.contains(format.column4SecondaryData))
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

    private usesYearMovementComparative(format) {
        return (OPENING_COMPARATIVE_DATA.contains(format.column1PrimaryData) || OPENING_COMPARATIVE_DATA.contains(format.column1SecondaryData) ||
                OPENING_COMPARATIVE_DATA.contains(format.column2PrimaryData) || OPENING_COMPARATIVE_DATA.contains(format.column2SecondaryData) ||
                OPENING_COMPARATIVE_DATA.contains(format.column3PrimaryData) || OPENING_COMPARATIVE_DATA.contains(format.column3SecondaryData) ||
                OPENING_COMPARATIVE_DATA.contains(format.column4PrimaryData) || OPENING_COMPARATIVE_DATA.contains(format.column4SecondaryData))
    }

    // Loads system workarea table with the raw data needed to fill the report. Returns true if succeeded or false if the user has no access rights
    private loadData(session, format, baseSelectedPID, extraSelectedPID, openingSelectedPID, baseComparativePID, extraComparativePID, openingComparativePID,
                     selectedPeriod, selectedOpeningPeriod, comparativePeriod, comparativeOpeningPeriod, selectedPeriodList, comparativePeriodList,
                     needYearMovementSelected, needExtraSelected, needBaseComparative, needYearMovementComparative, needExtraComparative, groupings, retainedAccount) {

        // At the time of writing there was a fault in Hibernate which stopped us using HQL for the following.
        // We therefore use raw SQL - ugly, but it works! Hibernate was translating the HQL to SQL incorrectly.
        def selectedProfitSQL, selectedYearBudgetSQL, comparativeProfitSQL, comparativeYearBudgetSQL
        def baseSelectedSQL, extraSelectedSQL, baseComparativeSQL, extraComparativeSQL
        def yearMovementSelectedSQL, yearMovementComparativeSQL

        selectedProfitSQL = 'select sum(b.company_closing_balance), sum(b.company_transaction_total + b.company_adjustment_total), sum(b.company_budget)' +
                ' from account as a, chart_section as s, general_balance as b where a.security_code = ' +
                company.securityCode.toString() + ' and a.section_id = s.id and s.section_type = \'ie\' and b.account_id = a.id and b.period_id = ' + selectedPeriod.id.toString()

        // Work out the grouping info we will need
        def fieldClause = ''
        def elementClause = ''
        for (int i = 0; i < groupings.size(); i++) {
            fieldClause += ", long${i + 2}"
            elementClause += ", a.element${groupings[i].elementNumber}_id"
        }

        baseSelectedSQL = 'insert into system_workarea (process, identifier, long1, string1, string2, decimal1, decimal2, decimal3, decimal4, version' + fieldClause +
                ') select ' + baseSelectedPID.toString() +
                ', b.account_id, a.section_id, a.code, a.name, b.company_transaction_total, b.company_adjustment_total, b.company_closing_balance, b.company_budget, 0' +
                elementClause + ' from account as a, chart_section as s, general_balance as b where a.security_code = ' +
                company.securityCode.toString() + ' and a.section_id = s.id and s.section_type = \'bs\' and b.account_id = a.id and b.period_id = ' + selectedPeriod.id.toString()

        if (needExtraSelected) {
            extraSelectedSQL = 'insert into system_workarea (process, identifier, decimal1, version) select ' +
                    extraSelectedPID.toString() + ', b.account_id, sum(b.company_budget), sum(b.version) ' +
                    'from general_balance as b, system_workarea as w where w.process = ' +
                    baseSelectedPID.toString() + ' and w.identifier = b.account_id and b.period_id in ('
            for (int i = 0; i < selectedPeriodList.size(); i++) {
                if (i) extraSelectedSQL += ', '
                extraSelectedSQL += selectedPeriodList[i].id.toString()
            }

            extraSelectedSQL += ') group by b.account_id'

            // Will need to adjust the retained profits ytd budget also
            selectedYearBudgetSQL = 'select sum(b.company_budget) from account as a, chart_section as s, general_balance as b where a.security_code = ' +
                    company.securityCode.toString() + ' and a.section_id = s.id and s.section_type = \'ie\' and b.account_id = a.id and b.period_id in ('
            for (int i = 0; i < selectedPeriodList.size(); i++) {
                if (i) selectedYearBudgetSQL += ', '
                selectedYearBudgetSQL += selectedPeriodList[i].id.toString()
            }

            selectedYearBudgetSQL += ')'
        }

        if (needYearMovementSelected) {
            yearMovementSelectedSQL = 'insert into system_workarea (process, identifier, decimal1, version) select ' +
                    openingSelectedPID.toString() + ', b.account_id, b.company_opening_balance, 0 ' +
                    'from general_balance as b, system_workarea as w where w.process = ' +
                    baseSelectedPID.toString() + ' and w.identifier = b.account_id and b.period_id = ' + selectedOpeningPeriod.id.toString()
        }

        if (needBaseComparative) {
            baseComparativeSQL = 'insert into system_workarea (process, identifier, decimal1, decimal2, decimal3, decimal4, version) select ' + baseComparativePID.toString() +
                    ', b.account_id, b.company_transaction_total, b.company_adjustment_total, b.company_closing_balance, b.company_budget, 0 ' +
                    'from general_balance as b, system_workarea as w where w.process = ' +
                    baseSelectedPID.toString() + ' and w.identifier = b.account_id and b.period_id = ' + comparativePeriod.id.toString()

            comparativeProfitSQL = 'select sum(b.company_closing_balance), sum(b.company_transaction_total + b.company_adjustment_total), sum(b.company_budget)' +
                    ' from account as a, chart_section as s, general_balance as b where a.security_code = ' +
                    company.securityCode.toString() + ' and a.section_id = s.id and s.section_type = \'ie\' and b.account_id = a.id and b.period_id = ' + comparativePeriod.id.toString()
        }

        if (needExtraComparative) {
            extraComparativeSQL = 'insert into system_workarea (process, identifier, decimal1, version) select ' +
                    extraComparativePID.toString() + ', b.account_id, sum(b.company_budget), sum(b.version) ' +
                    'from general_balance as b, system_workarea as w where w.process = ' +
                    baseSelectedPID.toString() + ' and w.identifier = b.account_id and b.period_id in ('
            for (int i = 0; i < comparativePeriodList.size(); i++) {
                if (i) extraComparativeSQL += ', '
                extraComparativeSQL += comparativePeriodList[i].id.toString()
            }

            extraComparativeSQL += ') group by b.account_id'

            // Will need to adjust the retained profits ytd budget also
            comparativeYearBudgetSQL = 'select sum(b.company_budget) from account as a, chart_section as s, general_balance as b where a.security_code = ' +
                    company.securityCode.toString() + ' and a.section_id = s.id and s.section_type = \'ie\' and b.account_id = a.id and b.period_id in ('
            for (int i = 0; i < comparativePeriodList.size(); i++) {
                if (i) comparativeYearBudgetSQL += ', '
                comparativeYearBudgetSQL += comparativePeriodList[i].id.toString()
            }

            comparativeYearBudgetSQL += ')'
        }

        if (needYearMovementComparative) {
            yearMovementComparativeSQL = 'insert into system_workarea (process, identifier, decimal1, version) select ' +
                    openingComparativePID.toString() + ', b.account_id, b.company_opening_balance, 0 ' +
                    'from general_balance as b, system_workarea as w where w.process = ' +
                    baseSelectedPID.toString() + ' and w.identifier = b.account_id and b.period_id = ' + comparativeOpeningPeriod.id.toString()
        }

        def selectedYearProfit, selectedPeriodProfit, selectedPeriodBudget, selectedYearBudget, results
        def comparativeYearProfit, comparativePeriodProfit, comparativePeriodBudget, comparativeYearBudget
        def statement = session.connection().createStatement()
        def lock = bookService.getCompanyLock(company)
        if (selectedPeriod.status != 'closed') lock.lock()  // Don't lock the company unless we have to
        try {

            results = statement.executeQuery(selectedProfitSQL)
            results.next()
            selectedYearProfit = results.getBigDecimal(1) ?: 0.0
            selectedPeriodProfit = results.getBigDecimal(2) ?: 0.0
            selectedPeriodBudget = results.getBigDecimal(3) ?: 0.0

            if (needExtraSelected) {
                results = statement.executeQuery(selectedYearBudgetSQL)
                results.next()
                selectedYearBudget = results.getBigDecimal(1) ?: 0.0
            }

            if (needBaseComparative && comparativePeriod.status != 'closed') {
                results = statement.executeQuery(comparativeProfitSQL)
                results.next()
                comparativeYearProfit = results.getBigDecimal(1) ?: 0.0
                comparativePeriodProfit = results.getBigDecimal(2) ?: 0.0
                comparativePeriodBudget = results.getBigDecimal(3) ?: 0.0
            }

            if (needExtraComparative && comparativePeriod.status != 'closed') {
                results = statement.executeQuery(comparativeYearBudgetSQL)
                results.next()
                comparativeYearBudget = results.getBigDecimal(1) ?: 0.0
            }

            SystemWorkarea.withTransaction {status ->

                // Always get the basic stuff for the selected period, plus the extra if needed
                statement.executeUpdate(baseSelectedSQL)
                if (needExtraSelected) statement.executeUpdate(extraSelectedSQL)

                // Only do the following within the lock if we absolutely have to
                if (needBaseComparative && comparativePeriod.status != 'closed') statement.executeUpdate(baseComparativeSQL)
                if (needExtraComparative && comparativePeriod.status != 'closed') statement.executeUpdate(extraComparativeSQL)

                if (needYearMovementSelected && selectedOpeningPeriod.status != 'closed') statement.executeUpdate(yearMovementSelectedSQL)
                if (needYearMovementComparative && comparativeOpeningPeriod.status != 'closed') statement.executeUpdate(yearMovementComparativeSQL)
            }
        } finally {
            if (selectedPeriod.status != 'closed') lock.unlock()
            statement.close()
        }

        // Check if still need to load comparative data outside of the lock
        if (needBaseComparative && comparativePeriod.status == 'closed') {
            statement = session.connection().createStatement()
            try {
                results = statement.executeQuery(comparativeProfitSQL)
                results.next()
                comparativeYearProfit = results.getBigDecimal(1) ?: 0.0
                comparativePeriodProfit = results.getBigDecimal(2) ?: 0.0
                comparativePeriodBudget = results.getBigDecimal(3) ?: 0.0

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
                results = statement.executeQuery(comparativeYearBudgetSQL)
                results.next()
                comparativeYearBudget = results.getBigDecimal(1) ?: 0.0

                SystemWorkarea.withTransaction {status ->
                    statement.executeUpdate(extraComparativeSQL)
                }
            } finally {
                statement.close()
            }
        }

        // Check if still need to load opening balance data outside of the lock
        if (needYearMovementSelected && selectedOpeningPeriod.status == 'closed') {
            statement = session.connection().createStatement()
            try {
                SystemWorkarea.withTransaction {status ->
                    statement.executeUpdate(yearMovementSelectedSQL)
                }
            } finally {
                statement.close()
            }
        }

        if (needYearMovementComparative && comparativeOpeningPeriod.status == 'closed') {
            statement = session.connection().createStatement()
            try {
                SystemWorkarea.withTransaction {status ->
                    statement.executeUpdate(yearMovementComparativeSQL)
                }
            } finally {
                statement.close()
            }
        }

        SystemWorkarea.withTransaction {status ->

            // Update the base selected retained profit account data
            results = SystemWorkarea.findByProcessAndIdentifier(baseSelectedPID, retainedAccount.id)
            results.decimal2 += selectedPeriodProfit
            results.decimal3 += selectedYearProfit
            results.decimal4 = selectedPeriodBudget
            results.saveThis()

            // Update the extra selected retained profit YTD budget if required
            if (needExtraSelected) {
                results = SystemWorkarea.findByProcessAndIdentifier(extraSelectedPID, retainedAccount.id)
                results.decimal1 = selectedYearBudget
                results.saveThis()
            }

            // Update the base comparative retained profit account data
            if (needBaseComparative) {
                results = SystemWorkarea.findByProcessAndIdentifier(baseComparativePID, retainedAccount.id)
                results.decimal2 += comparativePeriodProfit
                results.decimal3 += comparativeYearProfit
                results.decimal4 = comparativePeriodBudget
                results.saveThis()
            }

            // Update the extra comparative retained profit YTD budget if required
            if (needExtraComparative) {
                results = SystemWorkarea.findByProcessAndIdentifier(extraComparativePID, retainedAccount.id)
                results.decimal1 = comparativeYearBudget
                results.saveThis()
            }
        }
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
    private createInterimData(session, baseSelected, format, baseSelectedPID, extraSelectedPID, openingSelectedPID, baseComparativePID, extraComparativePID, openingComparativePID, interimPID,
                              needExtraSelected, needYearMovementSelected, needBaseComparative, needExtraComparative, needYearMovementComparative) {
        def extraSelected, openingSelected, baseComparative, extraComparative, openingComparative
        if (needExtraSelected) extraSelected = SystemWorkarea.findByProcessAndIdentifier(extraSelectedPID, baseSelected.identifier)
        if (needYearMovementSelected) openingSelected = SystemWorkarea.findByProcessAndIdentifier(openingSelectedPID, baseSelected.identifier)
        if (needBaseComparative) baseComparative = SystemWorkarea.findByProcessAndIdentifier(baseComparativePID, baseSelected.identifier)
        if (needExtraComparative) extraComparative = SystemWorkarea.findByProcessAndIdentifier(extraComparativePID, baseSelected.identifier)
        if (needYearMovementComparative) openingComparative = SystemWorkarea.findByProcessAndIdentifier(openingComparativePID, baseSelected.identifier)
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
            primaryData = getColumnData(dataOption, baseSelected, extraSelected, openingSelected, baseComparative, extraComparative, openingComparative)
            switch (format."column${i}Calculation") {

                case 'difference':
                    primaryData -= getColumnData(format."column${i}SecondaryData", baseSelected, extraSelected, openingSelected, baseComparative, extraComparative, openingComparative)
                    break

                case 'variance':

                    // Need to store the primary data for possible future summation
                    data."decimal${i + 4}" = primaryData

                    // Get the secondary data since we need to store it for possible future summation
                    secondaryData = getColumnData(format."column${i}SecondaryData", baseSelected, extraSelected, openingSelected, baseComparative, extraComparative, openingComparative)
                    data."decimal${i + 8}" = secondaryData

                    primaryData -= secondaryData

                    // Don't need to do anything if the primary or secondary value is zero since the result must be zero
                    if (primaryData && secondaryData) {
                        primaryData = utilService.round((primaryData * 100.0) / secondaryData, 2)
                    } else {
                        primaryData = 0.0
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
        if (needYearMovementSelected) session.evict(openingSelected)
        if (baseComparative) session.evict(baseComparative)
        if (extraComparative) session.evict(extraComparative)
        if (needYearMovementComparative) session.evict(openingComparative)
        return true
    }

    private getColumnData(dataOption, baseSelected, extraSelected, openingSelected, baseComparative, extraComparative, openingComparative) {
        switch (dataOption) {

            case 'selectedPeriodMovement':
                return (baseSelected?.decimal1 ?: 0.0) + (baseSelected?.decimal2 ?: 0.0)
                break

            case 'selectedPeriodBudget':
                return baseSelected?.decimal4 ?: 0.0
                break

            case 'selectedYearBalance':
                return baseSelected?.decimal3 ?: 0.0
                break

            case 'selectedYearMovement':
                return (baseSelected?.decimal3 ?: 0.0) - (openingSelected?.decimal1 ?: 0.0)
                break

            case 'selectedYearBudget':
                return extraSelected?.decimal1 ?: 0.0
                break

            case 'comparativePeriodMovement':
                return (baseComparative?.decimal1 ?: 0.0) + (baseComparative?.decimal2 ?: 0.0)
                break

            case 'comparativePeriodBudget':
                return baseComparative?.decimal4 ?: 0.0
                break

            case 'comparativeYearBalance':
                return baseComparative?.decimal3 ?: 0.0
                break

            case 'comparativeYearMovement':
                return (baseComparative?.decimal3 ?: 0.0) - (openingComparative?.decimal1 ?: 0.0)
                break

            case 'comparativeYearBudget':
                return extraComparative?.decimal1 ?: 0.0
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
}
