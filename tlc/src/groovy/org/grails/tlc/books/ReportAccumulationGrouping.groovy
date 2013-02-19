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

class ReportAccumulationGrouping extends ReportGrouping {

    // Child grouping constructor
    ReportAccumulationGrouping(results, groupings, lateralIndex, dataOffset, indentLevel, isCredit) {
        super()
        def elementValue = CodeElementValue.get(results[0][lateralIndex])
        text = elementValue.name
        this.isCredit = isCredit
        this.indentLevel = indentLevel
        summarySelectors = [0L]
        summarySelectors.addAll(results[0][0..lateralIndex])   // The long'n' values that identify us
        summarizeGroupings(results, dataOffset) // Summarize our own values
        lateralIndex = getNextLateralIndex(results, groupings.size(), lateralIndex)   // See if there are any sub-analyses
        if (lateralIndex > 0) {     // If there are sub-analyses
            children = []
            def childGroups = createChildGroups(results, lateralIndex)
            for (childGroup in childGroups) {
                children << new ReportAccumulationGrouping(childGroup, groupings, lateralIndex, dataOffset, indentLevel + 1, isCredit)
            }
        }
    }

    // Top level grouping constructor
    ReportAccumulationGrouping(formatLine, accumulators, groupings, interimPID, error) {
        super()
        this.text = formatLine.text
        isCredit = formatLine.accumulation.startsWith('-')
        summarySelectors = [0L] // An accumulation line does not have a section id and so we use zero
        def items = formatLine.accumulation.substring(1).split(',')*.trim()
        def sectionIds = []
        def vals
        for (item in items) {
            if (item.isInteger()) {    // It's a line number

                // Get the list of section id's that the line number represents
                vals = accumulators.get(item)
                if (!vals) {
                    error.code = 'profitReportLine.bad.lineNumber'
                    error.args = [formatLine.lineNumber, item]
                    error.default = "Report Format line number ${formatLine.lineNumber} refers to line number ${item} which cannot be found"
                    return
                }

                // Add that line number's section id's to ours (ensuring no duplicates)
                for (sectionId in vals) {
                    if (!sectionIds.contains(sectionId)) sectionIds << sectionId
                }
            } else {    // It's a section code
                vals = ChartSection.findByCompanyAndCode(company, item)
                if (!vals) {
                    error.code = 'profitReportLine.accumulation.section'
                    error.args = ['', '', '', formatLine.lineNumber, item]
                    error.default = "Report Format line number ${formatLine.lineNumber} refers to Chart Section code ${item} which does not exist"
                    return
                }

                if (!sectionIds.contains(vals.id)) sectionIds << vals.id
            }
        }

        accumulators.put(formatLine.lineNumber.toString(), sectionIds)  // Let other accumulators lines know about us

        // Allow for child records when grouping is reqested
        def groupingClause = ''
        def orderingClause = ''
        def dataOffset = 0
        def subGroupings = []
        if (groupings) {
            for (int i = 0; i < groupings.size(); i++) {

                // It makes no sense to include the account segment in accumulation groupings
                if (groupings[i].elementNumber > 1) {
                    subGroupings << groupings[i]
                    dataOffset += 2
                    if (groupingClause) {
                        groupingClause += ", long${i + 2}"
                        orderingClause += ", string${i + 2}"
                    } else {
                        groupingClause = "long${i + 2}"
                        orderingClause = "string${i + 2}"
                    }
                }
            }

            // We might have ended up with no groupings if we knocked out the account element
            if (groupingClause) {
                groupingClause += ', ' + orderingClause
                orderingClause = ' order by ' + orderingClause
            }
        }

        // Create the 'in' clause
        def inClause = ''
        for (sectionId in sectionIds) {
            if (inClause) {
                inClause += ', ?'
            } else {
                inClause = '(?'
            }
        }

        inClause += ')'
        def parameters = [interimPID]
        parameters.addAll(sectionIds)

        // Get our summary total(s)
        def sql = 'select ' + (groupingClause ? groupingClause + ', ' : '') + 'sum(decimal1), sum(decimal2), sum(decimal3), sum(decimal4), ' +
                'sum(decimal5), sum(decimal6), sum(decimal7), sum(decimal8), ' +
                'sum(decimal9), sum(decimal10), sum(decimal11), sum(decimal12) ' +
                'from SystemWorkarea where process = ? and long1 in ' + inClause
        if (groupingClause) sql += ' group by ' + groupingClause + orderingClause
        def results = SystemWorkarea.executeQuery(sql, parameters)

        // If we have at least one total
        if (results && results[0][dataOffset] != null) {
            results = normalizeGroups(results, dataOffset)  // Reduce the results to a common set of code element values
            summarizeGroupings(results, dataOffset) // Summarize our own values
            def lateralIndex = getNextLateralIndex(results, subGroupings.size(), -1)   // See if there are any sub-analyses
            if (lateralIndex >= 0) {     // If there are sub-analyses
                children = []
                def childGroups = createChildGroups(results, lateralIndex)
                for (childGroup in childGroups) {
                    children << new ReportAccumulationGrouping(childGroup, subGroupings, lateralIndex, dataOffset, indentLevel + 1, isCredit)
                }
            }
        }
    }

    private normalizeGroups(results, dataOffset) {

        // If there was no grouping then just return the results since there can be no children.
        // Similarly, if there is only one result then there is no need to normalize it.
        if (dataOffset && results.size() > 1) {

            // Since we might have selected data from multiple chart sections, each of which
            // can have a different code structure, we need to reduce the results to a common
            // set of code elements that this accumulation can display
            def modified = false
            def hasValues = false
            def pos = (int) (dataOffset / 2)    // The data offset is always divisible by two (one long'n' and one 'string'n')
            def foundNull, foundNotNull
            for (int i = 0; i < pos; i++) {
                foundNull = false
                foundNotNull = false
                for (int j = 0; j < results.size(); j++) {
                    if (results[j][i] == null) {
                        foundNull = true
                    } else {
                        foundNotNull = true
                    }

                    if (foundNull && foundNotNull) break
                }

                // If we found a value
                if (foundNotNull) {

                    // If we had a mixture of values and nulls for this code element, then it
                    // needs to be knocked out as a code element since this accumulation
                    // cannot display it as a child
                    if (foundNull) {
                        modified = true
                        for (int j = 0; j < results.size(); j++) {
                            results[j][i] = null            // Clear the long'n' value
                            results[j][i + pos] = null      // Clear the string'n' value
                        }
                    } else {
                        hasValues = true
                    }
                }
            }

            // If we modified the results and any column still has data values
            // then we need to combine the results but still keeping them in
            // child order. If we didn't modify the results, or they now contain
            // no values, then we need not do anything
            if (modified && hasValues) {
                def map = new TreeMap() // A sorted map to accumulate our results in to
                def key, vals

                // Work through the results
                for (result in results) {
                    key = makeKey(result[pos..<dataOffset]) // Use the string'n' values as the key
                    vals = map.get(key)
                    if (vals) {
                        for (int i = dataOffset; i < dataOffset + 12; i++) vals[i] += result[i]
                    } else {
                        map.put(key, result)
                    }
                }

                results = map.values().toArray() as List
            }
        }

        return results
    }

// Construct a string key from a list of long values
    private makeKey(list) {
        def key = ''
        for (item in list) {
            if (item) key += item
        }

        return key
    }
}
