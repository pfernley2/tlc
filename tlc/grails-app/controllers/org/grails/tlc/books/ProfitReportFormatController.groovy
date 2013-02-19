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

class ProfitReportFormatController {

    // Injected services
    def utilService
	def postingService
    def sessionFactory

    // Security settings
    def activities = [default: 'actadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', process: 'POST', adding: 'POST', cloning: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = 'name'
        [profitReportFormatInstanceList: ProfitReportFormat.selectList(company: utilService.currentCompany()), profitReportFormatInstanceTotal: ProfitReportFormat.selectCount()]
    }

    def show() {
        def profitReportFormatInstance = ProfitReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!profitReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'profitReportFormat', params.id)
            redirect(action: 'list')
        } else {
            def profitReportPercentInstanceList = ProfitReportPercent.findAll('from ProfitReportPercent as x where x.format = ? order by x.section.treeSequence', [profitReportFormatInstance])
            return [profitReportFormatInstance: profitReportFormatInstance, profitReportPercentInstanceList: profitReportPercentInstanceList]
        }
    }

    def delete() {
        def profitReportFormatInstance = ProfitReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (profitReportFormatInstance) {
            try {
                profitReportFormatInstance.delete(flush: true)
                flash.message = utilService.standardMessage('deleted', profitReportFormatInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', profitReportFormatInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'profitReportFormat', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def profitReportFormatInstance = ProfitReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!profitReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'profitReportFormat', params.id)
            redirect(action: 'list')
        } else {
            return [profitReportFormatInstance: profitReportFormatInstance,
                    chartSectionInstanceList: createChartSectionList(), percentageSections: createPercentageSectionList(profitReportFormatInstance)]
        }
    }

    def update(Long version) {
        def profitReportFormatInstance = ProfitReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (profitReportFormatInstance) {
            if (version != null && profitReportFormatInstance.version > version) {
                profitReportFormatInstance.errorMessage(code: 'locking.failure', domain: 'profitReportFormat')
                render(view: 'edit', model: [profitReportFormatInstance: profitReportFormatInstance,
                        chartSectionInstanceList: createChartSectionList(), percentageSections: createPercentageSectionList(profitReportFormatInstance)])
                return
            }

            profitReportFormatInstance.properties['name', 'title', 'subTitle', 'column1Heading', 'column1SubHeading', 'column1PrimaryData', 'column1Calculation', 'column1SecondaryData',
                    'column2Heading', 'column2SubHeading', 'column2PrimaryData', 'column2Calculation', 'column2SecondaryData',
                    'column3Heading', 'column3SubHeading', 'column3PrimaryData', 'column3Calculation', 'column3SecondaryData',
                    'column4Heading', 'column4SubHeading', 'column4PrimaryData', 'column4Calculation', 'column4SecondaryData'] = params
            def hasTotalPercentage = (profitReportFormatInstance.column1Calculation == 'percentage' ||
                    profitReportFormatInstance.column2Calculation == 'percentage' ||
                    profitReportFormatInstance.column3Calculation == 'percentage' ||
                    profitReportFormatInstance.column4Calculation == 'percentage')
            def sections = []
            if (params.percentages) sections = params.percentages instanceof String ? [params.percentages.toLong()] : params.percentages*.toLong() as List
            def percentageList = []
            for (it in profitReportFormatInstance.percentages) percentageList << it	// Avoid concurrent modification exceptions
            if (hasTotalPercentage) {
                if (sections) {
                    for (percentage in percentageList) {
                        if (!sections.contains(percentage.section.id)) {
                            profitReportFormatInstance.removeFromPercentages(percentage)
                            sessionFactory.currentSession.delete(percentage)
                        }
                    }

                    for (section in sections) {
                        def found = false
                        for (percentage in percentageList) {
                            if (percentage.section.id == section) {
                                found = true
                                break
                            }
                        }

                        if (!found) profitReportFormatInstance.addToPercentages(new ProfitReportPercent(section: ChartSection.findByIdAndCompany(section, utilService.currentCompany())))
                    }
                } else {
                    profitReportFormatInstance.errorMessage(field: 'percentages', code: 'profitReportFormat.need.percentages',
                            default: 'You must specify at least one Total Percentage Section when a column uses a Total Percentage Calculation')
                }
            } else {
                if (sections) {
                    profitReportFormatInstance.errorMessage(field: 'percentages', code: 'profitReportFormat.no.percentages',
                            default: 'You may not specify Total Percentage Section(s) unless at least one column uses a Total Percentage Calculation')
                } else {

                    // Ensure we remove any previous percentage sections
                    for (percentage in percentageList) {
                        profitReportFormatInstance.removeFromPercentages(percentage)
                        sessionFactory.currentSession.delete(percentage)
                    }
                }
            }

            if (!profitReportFormatInstance.hasErrors() && profitReportFormatInstance.save()) {     // With deep validation
                if (params.linesClicked) {
                    redirect(action: 'lines', id: profitReportFormatInstance.id)
                } else {
                    flash.message = utilService.standardMessage('updated', profitReportFormatInstance)
                    redirect(action: 'show', id: profitReportFormatInstance.id)
                }
            } else {
                render(view: 'edit', model: [profitReportFormatInstance: profitReportFormatInstance,
                        chartSectionInstanceList: createChartSectionList(), percentageSections: createPercentageSectionList(profitReportFormatInstance, sections)])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'profitReportFormat', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def profitReportFormatInstance = new ProfitReportFormat()
        profitReportFormatInstance.company = utilService.currentCompany()   // Ensure correct company
        return [profitReportFormatInstance: profitReportFormatInstance, chartSectionInstanceList: createChartSectionList(), percentageSections: []]
    }

    def save() {
        def profitReportFormatInstance = new ProfitReportFormat()
        profitReportFormatInstance.properties['name', 'title', 'subTitle', 'column1Heading', 'column1SubHeading', 'column1PrimaryData', 'column1Calculation', 'column1SecondaryData',
                'column2Heading', 'column2SubHeading', 'column2PrimaryData', 'column2Calculation', 'column2SecondaryData',
                'column3Heading', 'column3SubHeading', 'column3PrimaryData', 'column3Calculation', 'column3SecondaryData',
                'column4Heading', 'column4SubHeading', 'column4PrimaryData', 'column4Calculation', 'column4SecondaryData'] = params
        profitReportFormatInstance.company = utilService.currentCompany()   // Ensure correct company
        def hasTotalPercentage = (profitReportFormatInstance.column1Calculation == 'percentage' ||
                profitReportFormatInstance.column2Calculation == 'percentage' ||
                profitReportFormatInstance.column3Calculation == 'percentage' ||
                profitReportFormatInstance.column4Calculation == 'percentage')
        def sections = []
        if (params.percentages) sections = params.percentages instanceof String ? [params.percentages.toLong()] : params.percentages*.toLong() as List
        if (hasTotalPercentage) {
            if (sections) {
                for (section in sections) profitReportFormatInstance.addToPercentages(new ProfitReportPercent(section: ChartSection.findByIdAndCompany(section, utilService.currentCompany())))
            } else {
                profitReportFormatInstance.errorMessage(field: 'percentages', code: 'profitReportFormat.need.percentages',
                        default: 'You must specify at least one Total Percentage Section when a column uses a Total Percentage Calculation')
            }
        } else {
            if (sections) profitReportFormatInstance.errorMessage(field: 'percentages', code: 'profitReportFormat.no.percentages',
                    default: 'You may not specify Total Percentage Section(s) unless at least one column uses a Total Percentage Calculation')
        }

        if (!profitReportFormatInstance.hasErrors() && profitReportFormatInstance.save(flush: true)) {     // With deep validation
            flash.message = utilService.standardMessage('created', profitReportFormatInstance)
            if (params.linesClicked) {
                redirect(action: 'lines', id: profitReportFormatInstance.id)
            } else {
                redirect(action: 'show', id: profitReportFormatInstance.id)
            }
        } else {
            render(view: 'create', model: [profitReportFormatInstance: profitReportFormatInstance,
                    chartSectionInstanceList: createChartSectionList(), percentageSections: createPercentageSectionList(profitReportFormatInstance, sections)])
        }
    }

    def lines() {
        def profitReportFormatInstance = ProfitReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!profitReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'profitReportFormat', params.id)
            redirect(action: 'list')
        } else {
            if (!profitReportFormatInstance.lines?.size()) {
                for (int i = 0; i < 10; i++) profitReportFormatInstance.addToLines(new ProfitReportLine())
                profitReportFormatInstance.discard()
            }

            return [profitReportFormatInstance: profitReportFormatInstance, chartSectionInstanceList: createChartSectionList()]
        }
    }

    def process() {
        def profitReportFormatInstance = ProfitReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!profitReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'profitReportFormat', params.id)
            redirect(action: 'list')
            return
        }

        def removables = []
        def valueLines = []
        def accumulators = []
        def valid = true
        def blanks = 0
        postingService.refreshReportLines(profitReportFormatInstance, params, ProfitReportLine)

        // Check out everything seems ok on the face of it
        if (profitReportFormatInstance.lines) {
            for (line in profitReportFormatInstance.lines) {
                if (line.hasErrors()) {
                    valid = false
                    break
                }

                if (line.lineNumber) {
                    if (line.validate()) {
                        if (line.section || line.accumulation) valueLines << line.lineNumber
                        if (line.accumulation) accumulators << line
                    } else {
                        valid = false
                    }
                } else {
                    removables << line
                }
            }
        }

        // Work through checking accumulation references to line numbers etc
        if (valid) {
            def items
            def seen = []
            for (line in accumulators) {
                seen.clear()
                items = line.accumulation.substring(1).split(',')*.trim()
                for (item in items) {
                    if (seen.contains(item)) {
                        line.errorMessage(field: 'accumulation', code: 'profitReportLine.dup.reference', args: [line.lineNumber.toString(), item],
                                default: "Report Format line number ${line.lineNumber} contains a duplicate Accumulation reference to ${item}")
                        valid = false
                        break
                    }

                    seen << item
                    if (item.isInteger()) {    // It's a line number
                        if (!valueLines.contains(item.toInteger())) {
                            line.errorMessage(field: 'accumulation', code: 'profitReportLine.bad.reference', args: [line.lineNumber.toString(), item],
                                    default: "Report Format line number ${line.lineNumber} refers to line number ${item} which is not a line that can be accumulated")
                            valid = false
                            break
                        }
                    }
                }
            }
        }

        if (valid) {

            // At the domain level we specify that the lines association is to be sorted by
            // line number, consequently we have to pre-process the lines to be deleted as
            // their line numbers are now null and Hibernate would get upset when it tries to
            // keep the lines in the correct order.
            for (line in removables) {
                if (line.id) {
                    line.refresh()
                } else {
                    profitReportFormatInstance.removeFromLines(line)
                    blanks++
                }
            }

            ProfitReportFormat.withTransaction {status ->
                for (line in removables) {
                    if (line.id) {
                        profitReportFormatInstance.removeFromLines(line)
                        line.delete(flush: true)
                        line.discard()
                    }
                }

                if (!profitReportFormatInstance.save(flush: true)) {    // With deep validation
                    status.setRollbackOnly()
                    valid = false
                }
            }
        }

        if (valid) {
            profitReportFormatInstance.refresh()
            if (params.resequence && profitReportFormatInstance.lines) {
                def num = 0
                def mappings = [:]
                def renumbered = false
                accumulators.clear()
                for (line in profitReportFormatInstance.lines) {
                    num += 100
                    mappings.put(line.lineNumber.toString(), num.toString())
                    if (line.lineNumber != num) {
                        line.lineNumber = num + 2000000000
                        line.resequencing = true
                        renumbered = true
                    }

                    if (line.accumulation) accumulators << line
                }

                if (renumbered) {
                    def items, data, ftt, lineMod, newVal
                    for (line in accumulators) {
                        data = line.accumulation[0] + ' '
                        items = line.accumulation.substring(1).split(',')*.trim()
                        ftt = true
                        for (item in items) {
                            if (item.isInteger()) {    // It's a line number
                                newVal = mappings.get(item) ?: item
                                if (item != newVal) {
                                    item = newVal
                                    lineMod = true
                                }
                            }

                            if (ftt) {
                                data += item
                                ftt = false
                            } else {
                                data += ', ' + item
                            }
                        }

                        if (lineMod) line.accumulation = data
                    }

                    ProfitReportFormat.withTransaction {status ->
                        if (profitReportFormatInstance.save(flush: true)) {  // With deep validation
                            for (line in profitReportFormatInstance.lines) {
                                if (line.lineNumber > 2000000000) line.lineNumber -= 2000000000
                            }

                            if (!profitReportFormatInstance.save(flush: true)) {  // With deep validation
                                status.setRollbackOnly()
                                valid = false
                            }
                        } else {
                            status.setRollbackOnly()
                            valid = false
                        }
                    }

                    if (valid) {
                        if (blanks) {
                            for (int i = 0; i < blanks; i++) profitReportFormatInstance.addToLines(new ProfitReportLine())
                            profitReportFormatInstance.discard()
                        }

                        flash.message = message(code: 'profitReportFormat.updated.resequenced', args: [profitReportFormatInstance.toString()], default: "Report Format ${profitReportFormatInstance.toString()} updated and resequenced")
                    } else {
                        profitReportFormatInstance.refresh()
                        flash.message = message(code: 'profitReportFormat.updated.bad.resequence', args: [profitReportFormatInstance.toString()], default: "Report Format ${profitReportFormatInstance.toString()} updated but NOT resequenced")
                    }
                } else {
                    flash.message = message(code: 'profitReportFormat.updated.no.resequence', args: [profitReportFormatInstance.toString()], default: "Report Format ${profitReportFormatInstance.toString()} updated, no resequencing necessary")
                }
            } else {
                flash.message = utilService.standardMessage('updated', profitReportFormatInstance)
            }
        } else if (profitReportFormatInstance.lines) {

            // Propagate line error messages to the header
            for (line in profitReportFormatInstance.lines) {
                for (lineError in utilService.getAllErrorMessages(line)) profitReportFormatInstance.errors.reject(null, lineError)
            }

            profitReportFormatInstance.discard()
        }

        if (!valid) flash.clear()

        render(view: 'lines', model: [profitReportFormatInstance: profitReportFormatInstance, chartSectionInstanceList: createChartSectionList()])
    }

    def adding() {
        def profitReportFormatInstance = ProfitReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!profitReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'profitReportFormat', params.id)
            redirect(action: 'list')
        } else {
            postingService.refreshReportLines(profitReportFormatInstance, params, ProfitReportLine)

            // Add some blank lines
            for (int i = 0; i < 10; i++) profitReportFormatInstance.addToLines(new ProfitReportLine())

            // Grails would automatically save an existing record that was modified if we didn't discard it
            profitReportFormatInstance.discard()
            render(view: 'lines', model: [profitReportFormatInstance: profitReportFormatInstance, chartSectionInstanceList: createChartSectionList()])
        }
    }

    def clone() {
        def profitReportFormatInstance = ProfitReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!profitReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'profitReportFormat', params.id)
            redirect(action: 'list')
            return
        }

        [profitReportFormatInstance: profitReportFormatInstance]
    }

    def cloning() {
        def profitReportFormatInstance = ProfitReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!profitReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'profitReportFormat', params.id)
            redirect(action: 'list')
            return
        }

        profitReportFormatInstance.name = params.name

        def newFormat = new ProfitReportFormat(company: profitReportFormatInstance.company,
                name: profitReportFormatInstance.name, title: profitReportFormatInstance.title, subTitle: profitReportFormatInstance.subTitle,
                column1Heading: profitReportFormatInstance.column1Heading, column1SubHeading: profitReportFormatInstance.column1SubHeading,
                column1PrimaryData: profitReportFormatInstance.column1PrimaryData, column1Calculation: profitReportFormatInstance.column1Calculation,
                column1SecondaryData: profitReportFormatInstance.column1SecondaryData, column2Heading: profitReportFormatInstance.column2Heading,
                column2SubHeading: profitReportFormatInstance.column2SubHeading, column2PrimaryData: profitReportFormatInstance.column2PrimaryData,
                column2Calculation: profitReportFormatInstance.column2Calculation, column2SecondaryData: profitReportFormatInstance.column2SecondaryData,
                column3Heading: profitReportFormatInstance.column3Heading, column3SubHeading: profitReportFormatInstance.column3SubHeading,
                column3PrimaryData: profitReportFormatInstance.column3PrimaryData, column3Calculation: profitReportFormatInstance.column3Calculation,
                column3SecondaryData: profitReportFormatInstance.column3SecondaryData, column4Heading: profitReportFormatInstance.column4Heading,
                column4SubHeading: profitReportFormatInstance.column4SubHeading, column4PrimaryData: profitReportFormatInstance.column4PrimaryData,
                column4Calculation: profitReportFormatInstance.column4Calculation, column4SecondaryData: profitReportFormatInstance.column4SecondaryData)

        if (profitReportFormatInstance.percentages) {
            for (percentage in profitReportFormatInstance.percentages) {
                newFormat.addToPercentages(new ProfitReportPercent(section: percentage.section))
            }
        }

        if (profitReportFormatInstance.lines) {
            for (line in profitReportFormatInstance.lines) {
                newFormat.addToLines(new ProfitReportLine(lineNumber: line.lineNumber, text: line.text, section: line.section, accumulation: line.accumulation))
            }
        }

        profitReportFormatInstance.discard()    // Don't let Grails save the original
        if (newFormat.save(flush: true)) {     // With deep validation
            redirect(action: 'edit', id: newFormat.id)
        } else {
            def msgs = utilService.getAllErrorMessages(newFormat)
            if (msgs) {
                for (newError in msgs) profitReportFormatInstance.errors.reject(null, newError)
            } else {
                profitReportFormatInstance.errorMessage(code: 'profitReportFormat.bad.clone', default: 'Unable to save the cloned format')
            }

            render(view: 'clone', model: [profitReportFormatInstance: profitReportFormatInstance])
        }
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private createChartSectionList() {
        return ChartSection.findAll('from ChartSection where company = ? and type = ? and accountSegment > ? order by treeSequence', [utilService.currentCompany(), 'ie', (byte) 0])
    }

    private createPercentageSectionList(profitReportFormatInstance, sections = null) {
        def percentageSections = []
        if (sections == null) {
            for (it in ProfitReportPercent.findAllByFormat(profitReportFormatInstance)) percentageSections << it.section
        } else {
            def chartSectionInstance
            for (section in sections) {
                chartSectionInstance = ChartSection.findByIdAndCompany(section, utilService.currentCompany())
                if (chartSectionInstance) percentageSections << chartSectionInstance
            }
        }

        return percentageSections
    }
}