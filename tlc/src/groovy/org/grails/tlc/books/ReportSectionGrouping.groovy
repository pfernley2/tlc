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

class ReportSectionGrouping extends ReportGrouping {

    // Child grouping constructor
    ReportSectionGrouping(results, groupings, lateralIndex, dataOffset, indentLevel, isCredit) {
        super()
        def elementValue = CodeElementValue.get(results[0][lateralIndex])
        text = elementValue.name
        this.isCredit = isCredit
        this.indentLevel = indentLevel
        summarySelectors = results[0][0..lateralIndex]   // The long'n' values that identify us
        summarizeGroupings(results, dataOffset) // Summarize our own values
        lateralIndex = getNextLateralIndex(results, groupings.size() + 1, lateralIndex)   // See if there are any sub-analyses
        if (lateralIndex > 0) {     // If there are sub-analyses
            children = []
            def childGroups = createChildGroups(results, lateralIndex)
            for (childGroup in childGroups) {
                children << new ReportSectionGrouping(childGroup, groupings, lateralIndex, dataOffset, indentLevel + 1, isCredit)
            }
        }
    }

    // Top level grouping constructor
    ReportSectionGrouping(section, text, groupings, interimPID) {
        super()
        this.text = text ?: section.name    // Default text for a chart section is the section name
        isCredit = section.status == 'cr'
        summarySelectors = [section.id] // This is the long1 value that identifies our top-level value

        // Allow for child records when grouping is reqested
        def groupingClause = 'long1'
        def orderingClause = ''
        def dataOffset = 1
        if (groupings) {
            dataOffset += groupings.size() * 2
            for (int i = 0; i < groupings.size(); i++) {
                groupingClause += ", long${i + 2}"
                if (i) {
                    orderingClause += ", string${i + 2}"
                } else {
                    orderingClause = "string${i + 2}"
                }
            }

            groupingClause += ', ' + orderingClause
            orderingClause = ' order by ' + orderingClause
        }

        // Get our summary total(s)
        def sql = 'select ' + groupingClause + ', sum(decimal1), sum(decimal2), sum(decimal3), sum(decimal4), ' +
                'sum(decimal5), sum(decimal6), sum(decimal7), sum(decimal8), ' +
                'sum(decimal9), sum(decimal10), sum(decimal11), sum(decimal12) ' +
                'from SystemWorkarea where process = ? and long1 = ? group by ' + groupingClause + orderingClause
        def results = SystemWorkarea.executeQuery(sql, [interimPID, section.id])

        // If we have at least one total
        if (results) {
            summarizeGroupings(results, dataOffset) // Summarize our own values
            def lateralIndex = getNextLateralIndex(results, groupings.size() + 1, 0)   // See if there are any sub-analyses
            if (lateralIndex > 0) {     // If there are sub-analyses
                children = []
                def childGroups = createChildGroups(results, lateralIndex)
                for (childGroup in childGroups) {
                    children << new ReportSectionGrouping(childGroup, groupings, lateralIndex, dataOffset, indentLevel + 1, isCredit)
                }
            }
        }
    }
}
