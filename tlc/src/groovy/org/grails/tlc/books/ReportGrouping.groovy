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

import org.grails.tlc.sys.SystemWorkarea
import org.grails.tlc.sys.UtilService

class ReportGrouping {
    static final NONE = 0
    static final SPACE_ABOVE = 1
    static final LINE_ABOVE = 2
    static final LINE_BELOW = 4
    static final DOUBLE_BELOW = 8
    static final INDENT_DEPTH = 2

    def text
    def summarySelectors
    def summaryValues = [0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0]
    def isCredit
    def indentLevel = 0
    def children
    def lastLineWasTotal = false

    ReportGrouping() {}

    def print(session, interimPID, reportPID, reportLineNumber, format, formatLineNumber, totals, totalPrefix, detailed, priorGroupingHadTotal, isLastTotal = false) {
        def line
        def detailsPrinted = false
        if (children) {

            // Need a title line
            line = new SystemWorkarea(process: reportPID, identifier: reportLineNumber++, integer1: formatLineNumber, string1: makeText(text, indentLevel))
            setLongValues(line)
            if (priorGroupingHadTotal) {
                line.integer2 = SPACE_ABOVE
                priorGroupingHadTotal = false
            }

            if (!saveReportLine(session, line)) return 0  // Indicate an error saving the line data

            // Now print the children
            for (child in children) {
                reportLineNumber = child.print(session, interimPID, reportPID, reportLineNumber, format, formatLineNumber, totals, totalPrefix, detailed, priorGroupingHadTotal, false)
                if (!reportLineNumber) return 0 // Check for child error
            }

            lastLineWasTotal = children[-1].lastLineWasTotal    // Need to know if the last child record printed a total or not
        } else if (detailed && !isAccumulation()) {  // It's up to us to print the detailed accounts (not for accumulation lines, though)
            def sql = 'from SystemWorkarea where process = ?'
            def parameters = [interimPID]
            for (int i = 0; i < summarySelectors.size(); i++) sql += " and long${i + 1} = ?"
            parameters.addAll(summarySelectors)
            sql += ' and (decimal1 != 0.0 or decimal2 != 0.0 or decimal3 != 0.0 or decimal4 != 0.0) order by string1'
            def accounts = SystemWorkarea.findAll(sql, parameters)
            if (accounts) {
                detailsPrinted = true

                // Need a title line
                line = new SystemWorkarea(process: reportPID, identifier: reportLineNumber++, integer1: formatLineNumber, string1: makeText(text, indentLevel))
                setLongValues(line)
                if (priorGroupingHadTotal) {
                    line.integer2 = SPACE_ABOVE
                    priorGroupingHadTotal = false
                }

                if (!saveReportLine(session, line)) return 0  // Indicate an error saving the line data

                // Work through the interim account data creating new report records
                for (account in accounts) {
                    line = new SystemWorkarea(process: reportPID, identifier: reportLineNumber++, integer1: formatLineNumber, string1: makeText(account.string1, indentLevel + 1))
                    setLongValues(line)
                    for (int i = 1; i <= 12; i++) line."decimal${i}" = account."decimal${i}"
                    setNegationFlags(format, line, isCredit)
                    if (!saveReportLine(session, line)) return 0  // Indicate an error saving the line data
                    session.evict(account)
                }
            }
        }

        // Work out what spacing, overlining and underlining we may need, if any
        def decoration = (priorGroupingHadTotal || lastLineWasTotal) ? SPACE_ABOVE : NONE
        if (children || detailsPrinted || (isAccumulation() && isTopLevel())) {
            decoration |= LINE_ABOVE
            decoration |= LINE_BELOW
            if (isLastTotal && isAccumulation()) decoration |= DOUBLE_BELOW     // Only applies to accumulation lines
            lastLineWasTotal = true // Need to tell any parent that we have printed a total
        }

        line = new SystemWorkarea(process: reportPID, identifier: reportLineNumber++, integer1: formatLineNumber, integer2: decoration)
        line.string1 = (children || detailsPrinted) ? makeText(totalPrefix + ' ' + text, indentLevel) : makeText(text, indentLevel)
        setLongValues(line)
        for (int i = 0; i < 12; i++) line."decimal${i + 1}" = summaryValues[i]
        completeReportLine(format, line, totals, isCredit)
        if (!saveReportLine(session, line)) return 0  // Indicate an error saving the line data

        return reportLineNumber
    }

    def saveReportLine(session, line) {
        def valid = true
        SystemWorkarea.withTransaction {status ->
            if (!line.saveThis()) {
                status.setRollbackOnly()
                valid = false
            }
        }

        session.evict(line)
        return valid
    }

    def setLongValues(line) {
        for (int i = 0; i < summarySelectors.size(); i++) line."long${i + 1}" = summarySelectors[i]
    }

    def makeText(txt, level) {
        return txt.padLeft((level * INDENT_DEPTH) + txt.length())
    }

    def summarizeGroupings(resultList, dataOffset) {
        if (resultList) {
            for (result in resultList) {

                // Total up our values
                for (int i = 0; i < 12; i++) summaryValues[i] += result[i + dataOffset]
            }
        }
    }

    def getNextLateralIndex(childData, limit, lateralIndex) {

        // Can't be any sub-analysis if there are no records
        if (!childData) return -1

        // Look across the first record, starting with the given
        // lateralIndex value + 1, looking for any sub-analysis
        for (lateralIndex = lateralIndex + 1; lateralIndex < limit; lateralIndex++) {
            if (childData[0][lateralIndex]) break
        }

        // If there are no more long'n' values then return zero
        // to indicate there is no further sub-analysis
        return (lateralIndex >= limit) ? -1 : lateralIndex
    }

    def createChildGroups(childData, lateralIndex) {

        def childGroups = []    // This will be a list of lists
        def lastLong, currentChild

        // Break the single record list down in to one or more child lists
        // of records based on the value of the lateralIndex value
        for (record in childData) {
            if (record[lateralIndex] != lastLong) {
                if (lastLong) childGroups << currentChild
                currentChild = []
                lastLong = record[lateralIndex]
            }

            currentChild << record
        }

        // Dont forget the last child grouping
        childGroups << currentChild

        return childGroups
    }

    def completeReportLine(format, line, totals, isCredit) {
        def primaryData, secondaryData
        for (int i = 1; i <= 4; i++) {
            line."boolean${i}" = false  // Assume no negation required
            switch (format."column${i}Calculation") {

                case 'variance':

                    primaryData = line."decimal${i + 4}"
                    secondaryData = line."decimal${i + 8}"
                    primaryData -= secondaryData

                    // Don't need to do anything if the primary or secondary value is zero since the result must be zero
                    if (primaryData && secondaryData) {
                        primaryData = UtilService.round((primaryData * 100.0) / secondaryData, 2)
                    } else {
                        primaryData = 0.0
                    }

                    line."decimal${i}" = primaryData
                    break

                case 'percentage':

                    primaryData = line."decimal${i + 4}"

                    // Don't need to do anything if the primary value is zero since the result must be zero
                    if (primaryData) {
                        secondaryData = totals.get(format."column${i}PrimaryData")
                        if (secondaryData) {
                            primaryData = UtilService.round((primaryData * 100.0) / secondaryData, 2).abs() // Always display total percentages as positives
                        } else {
                            primaryData = 0.0   // Answer must be zero if secondary data is zero
                        }
                    }

                    line."decimal${i}" = primaryData
                    break

                default:    // A monetary value, so just need to set the negation flag
                    line."boolean${i}" = isCredit
                    break
            }
        }
    }

    def isAccumulation() {
        return (summarySelectors[0] == 0)
    }

    def isTopLevel() {
        return (summarySelectors.size() == 1)
    }

    def setNegationFlags(format, line, isCredit) {
        def calc
        for (int i = 1; i <= 4; i++) {
            calc = format."column${i}Calculation"
            line."boolean${i}" = (calc == 'variance' || calc == 'percentage') ? false : isCredit
        }
    }
}
