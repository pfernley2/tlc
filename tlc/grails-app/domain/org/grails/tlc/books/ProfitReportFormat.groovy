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

import org.grails.tlc.corp.Company
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class ProfitReportFormat {

    static final CALC_OPTIONS = ['difference', 'variance', 'percentage']
    static final DATA_OPTIONS = ['selectedPeriodActual', 'selectedPeriodAdjusted', 'selectedPeriodBudget',
        'selectedYearActual', 'selectedYearAdjusted', 'selectedYearBudget', 'comparativePeriodActual',
        'comparativePeriodAdjusted', 'comparativePeriodBudget', 'comparativeYearActual', 'comparativeYearAdjusted',
        'comparativeYearBudget']

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company]
    static hasMany = [percentages: ProfitReportPercent, lines: ProfitReportLine]

    String name
    String title
    String subTitle
    String column1Heading
    String column1SubHeading
    String column1PrimaryData
    String column1Calculation
    String column1SecondaryData
    String column2Heading
    String column2SubHeading
    String column2PrimaryData
    String column2Calculation
    String column2SecondaryData
    String column3Heading
    String column3SubHeading
    String column3PrimaryData
    String column3Calculation
    String column3SecondaryData
    String column4Heading
    String column4SubHeading
    String column4PrimaryData
    String column4Calculation
    String column4SecondaryData
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            company lazy: true
            percentages cascade: 'all'
            lines cascade: 'all', sort: 'lineNumber'
        }
    }

    static constraints = {
        name(blank: false, size: 1..30, unique: 'company')
        title(blank: false, size: 1..30)
        subTitle(nullable: true, size: 1..30)
        column1Heading(nullable: true, size: 1..10, validator: {val, obj ->
            if (val && !obj.column1PrimaryData) return 'profitReportFormat.columnHeading.no.heading'
            return true
        })
        column1SubHeading(nullable: true, size: 1..10, validator: {val, obj ->
            if (val && !obj.column1PrimaryData) return 'profitReportFormat.columnSubHeading.no.heading'
            return true
        })
        column1PrimaryData(nullable: true, inList: DATA_OPTIONS, validator: {val, obj ->
            if (val) {
                if (!obj.column1Heading) return 'profitReportFormat.columnPrimaryData.no.heading'
            } else {
                if (!obj.column2PrimaryData && !obj.column3PrimaryData && !obj.column4PrimaryData) return 'no.data'
            }

            return true
        })
        column1Calculation(nullable: true, inList: CALC_OPTIONS, validator: {val, obj ->
            if (val && !obj.column1PrimaryData) return 'profitReportFormat.columnCalculation.no.primary'
            return true
        })
        column1SecondaryData(nullable: true, inList: DATA_OPTIONS, validator: {val, obj ->
            if (val) {
                if (obj.column1Calculation != 'difference' && obj.column1Calculation != 'variance') return 'profitReportFormat.columnSecondaryData.no.secondary'
                if (val == obj.column1PrimaryData) return 'profitReportFormat.columnSecondaryData.same.secondary'
            } else {
                if (obj.column1Calculation == 'difference' || obj.column1Calculation == 'variance') return 'profitReportFormat.columnSecondaryData.need.secondary'
            }

            return true
        })
        column2Heading(nullable: true, size: 1..10, validator: {val, obj ->
            if (val && !obj.column2PrimaryData) return 'profitReportFormat.columnHeading.no.heading'
            return true
        })
        column2SubHeading(nullable: true, size: 1..10, validator: {val, obj ->
            if (val && !obj.column2PrimaryData) return 'profitReportFormat.columnSubHeading.no.heading'
            return true
        })
        column2PrimaryData(nullable: true, inList: DATA_OPTIONS, validator: {val, obj ->
            if (val) {
                if (!obj.column2Heading) return 'profitReportFormat.columnPrimaryData.no.heading'
                if (val == obj.column1PrimaryData && obj.column2Calculation == obj.column1Calculation && obj.column2SecondaryData == obj.column1SecondaryData) return 'profitReportFormat.columnPrimaryData.dup.data'
            }

            return true
        })
        column2Calculation(nullable: true, inList: CALC_OPTIONS, validator: {val, obj ->
            if (val && !obj.column2PrimaryData) return 'profitReportFormat.columnCalculation.no.primary'
            return true
        })
        column2SecondaryData(nullable: true, inList: DATA_OPTIONS, validator: {val, obj ->
            if (val) {
                if (obj.column2Calculation != 'difference' && obj.column2Calculation != 'variance') return 'profitReportFormat.columnSecondaryData.no.secondary'
                if (val == obj.column2PrimaryData) return 'profitReportFormat.columnSecondaryData.same.secondary'
            } else {
                if (obj.column2Calculation == 'difference' || obj.column2Calculation == 'variance') return 'profitReportFormat.columnSecondaryData.need.secondary'
            }

            return true
        })
        column3Heading(nullable: true, size: 1..10, validator: {val, obj ->
            if (val && !obj.column3PrimaryData) return 'profitReportFormat.columnHeading.no.heading'
            return true
        })
        column3SubHeading(nullable: true, size: 1..10, validator: {val, obj ->
            if (val && !obj.column3PrimaryData) return 'profitReportFormat.columnSubHeading.no.heading'
            return true
        })
        column3PrimaryData(nullable: true, inList: DATA_OPTIONS, validator: {val, obj ->
            if (val) {
                if (!obj.column3Heading) return 'profitReportFormat.columnPrimaryData.no.heading'
                if (val == obj.column1PrimaryData && obj.column3Calculation == obj.column1Calculation && obj.column3SecondaryData == obj.column1SecondaryData) return 'profitReportFormat.columnPrimaryData.dup.data'
                if (val == obj.column2PrimaryData && obj.column3Calculation == obj.column2Calculation && obj.column3SecondaryData == obj.column2SecondaryData) return 'profitReportFormat.columnPrimaryData.dup.data'
            }

            return true
        })
        column3Calculation(nullable: true, inList: CALC_OPTIONS, validator: {val, obj ->
            if (val && !obj.column3PrimaryData) return 'profitReportFormat.columnCalculation.no.primary'
            return true
        })
        column3SecondaryData(nullable: true, inList: DATA_OPTIONS, validator: {val, obj ->
            if (val) {
                if (obj.column3Calculation != 'difference' && obj.column3Calculation != 'variance') return 'profitReportFormat.columnSecondaryData.no.secondary'
                if (val == obj.column3PrimaryData) return 'profitReportFormat.columnSecondaryData.same.secondary'
            } else {
                if (obj.column3Calculation == 'difference' || obj.column3Calculation == 'variance') return 'profitReportFormat.columnSecondaryData.need.secondary'
            }

            return true
        })
        column4Heading(nullable: true, size: 1..10, validator: {val, obj ->
            if (val && !obj.column4PrimaryData) return 'profitReportFormat.columnHeading.no.heading'
            return true
        })
        column4SubHeading(nullable: true, size: 1..10, validator: {val, obj ->
            if (val && !obj.column4PrimaryData) return 'profitReportFormat.columnSubHeading.no.heading'
            return true
        })
        column4PrimaryData(nullable: true, inList: DATA_OPTIONS, validator: {val, obj ->
            if (val) {
                if (!obj.column4Heading) return 'profitReportFormat.columnPrimaryData.no.heading'
                if (val == obj.column1PrimaryData && obj.column4Calculation == obj.column1Calculation && obj.column4SecondaryData == obj.column1SecondaryData) return 'profitReportFormat.columnPrimaryData.dup.data'
                if (val == obj.column2PrimaryData && obj.column4Calculation == obj.column2Calculation && obj.column4SecondaryData == obj.column2SecondaryData) return 'profitReportFormat.columnPrimaryData.dup.data'
                if (val == obj.column3PrimaryData && obj.column4Calculation == obj.column3Calculation && obj.column4SecondaryData == obj.column3SecondaryData) return 'profitReportFormat.columnPrimaryData.dup.data'
            }

            return true
        })
        column4Calculation(nullable: true, inList: CALC_OPTIONS, validator: {val, obj ->
            if (val && !obj.column4PrimaryData) return 'profitReportFormat.columnCalculation.no.primary'
            return true
        })
        column4SecondaryData(nullable: true, inList: DATA_OPTIONS, validator: {val, obj ->
            if (val) {
                if (obj.column4Calculation != 'difference' && obj.column4Calculation != 'variance') return 'profitReportFormat.columnSecondaryData.no.secondary'
                if (val == obj.column4PrimaryData) return 'profitReportFormat.columnSecondaryData.same.secondary'
            } else {
                if (obj.column4Calculation == 'difference' || obj.column4Calculation == 'variance') return 'profitReportFormat.columnSecondaryData.need.secondary'
            }

            return true
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.company.securityCode
            return true
        })
    }

    def afterInsert() {
        UtilService.trace('insert', this)
    }

    def afterUpdate() {
        UtilService.trace('update', this)
    }

    def afterDelete() {
        UtilService.trace('delete', this)
    }

    public String toString() {
        return name
    }
}
