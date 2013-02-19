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

class BalanceReportFormatController {

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
        [balanceReportFormatInstanceList: BalanceReportFormat.selectList(company: utilService.currentCompany()), balanceReportFormatInstanceTotal: BalanceReportFormat.selectCount()]
    }

    def show() {
        def balanceReportFormatInstance = BalanceReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!balanceReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'balanceReportFormat', params.id)
            redirect(action: 'list')
        } else {
            return [balanceReportFormatInstance: balanceReportFormatInstance]
        }
    }

    def delete() {
        def balanceReportFormatInstance = BalanceReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (balanceReportFormatInstance) {
            try {
                balanceReportFormatInstance.delete(flush: true)
                flash.message = utilService.standardMessage('deleted', balanceReportFormatInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', balanceReportFormatInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'balanceReportFormat', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def balanceReportFormatInstance = BalanceReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!balanceReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'balanceReportFormat', params.id)
            redirect(action: 'list')
        } else {
            return [balanceReportFormatInstance: balanceReportFormatInstance]
        }
    }

    def update(Long version) {
        def balanceReportFormatInstance = BalanceReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (balanceReportFormatInstance) {
            if (version != null && balanceReportFormatInstance.version > version) {
				balanceReportFormatInstance.errorMessage(code: 'locking.failure', domain: 'balanceReportFormat')
                render(view: 'edit', model: [balanceReportFormatInstance: balanceReportFormatInstance])
                return
            }

            balanceReportFormatInstance.properties['name', 'title', 'subTitle', 'column1Heading', 'column1SubHeading', 'column1PrimaryData', 'column1Calculation', 'column1SecondaryData',
                    'column2Heading', 'column2SubHeading', 'column2PrimaryData', 'column2Calculation', 'column2SecondaryData',
                    'column3Heading', 'column3SubHeading', 'column3PrimaryData', 'column3Calculation', 'column3SecondaryData',
                    'column4Heading', 'column4SubHeading', 'column4PrimaryData', 'column4Calculation', 'column4SecondaryData'] = params
            if (!balanceReportFormatInstance.hasErrors() && balanceReportFormatInstance.saveThis()) {
                if (params.linesClicked) {
                    redirect(action: 'lines', id: balanceReportFormatInstance.id)
                } else {
                    flash.message = utilService.standardMessage('updated', balanceReportFormatInstance)
                    redirect(action: 'show', id: balanceReportFormatInstance.id)
                }
            } else {
                render(view: 'edit', model: [balanceReportFormatInstance: balanceReportFormatInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'balanceReportFormat', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def balanceReportFormatInstance = new BalanceReportFormat()
        balanceReportFormatInstance.company = utilService.currentCompany()   // Ensure correct company
        return [balanceReportFormatInstance: balanceReportFormatInstance]
    }

    def save() {
        def balanceReportFormatInstance = new BalanceReportFormat()
        balanceReportFormatInstance.properties['name', 'title', 'subTitle', 'column1Heading', 'column1SubHeading', 'column1PrimaryData', 'column1Calculation', 'column1SecondaryData',
                'column2Heading', 'column2SubHeading', 'column2PrimaryData', 'column2Calculation', 'column2SecondaryData',
                'column3Heading', 'column3SubHeading', 'column3PrimaryData', 'column3Calculation', 'column3SecondaryData',
                'column4Heading', 'column4SubHeading', 'column4PrimaryData', 'column4Calculation', 'column4SecondaryData'] = params
        balanceReportFormatInstance.company = utilService.currentCompany()   // Ensure correct company
        if (!balanceReportFormatInstance.hasErrors() && balanceReportFormatInstance.saveThis()) {
            flash.message = utilService.standardMessage('created', balanceReportFormatInstance)
            if (params.linesClicked) {
                redirect(action: 'lines', id: balanceReportFormatInstance.id)
            } else {
                redirect(action: 'show', id: balanceReportFormatInstance.id)
            }
        } else {
            render(view: 'create', model: [balanceReportFormatInstance: balanceReportFormatInstance])
        }
    }

    def lines() {
        def balanceReportFormatInstance = BalanceReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!balanceReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'balanceReportFormat', params.id)
            redirect(action: 'list')
        } else {
            if (!balanceReportFormatInstance.lines?.size()) {
                for (int i = 0; i < 10; i++) balanceReportFormatInstance.addToLines(new BalanceReportLine())
                balanceReportFormatInstance.discard()
            }

            return [balanceReportFormatInstance: balanceReportFormatInstance, chartSectionInstanceList: createChartSectionList()]
        }
    }

    def process() {
        def balanceReportFormatInstance = BalanceReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!balanceReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'balanceReportFormat', params.id)
            redirect(action: 'list')
            return
        }

        def removables = []
        def valueLines = []
        def accumulators = []
        def valid = true
        def blanks = 0
        postingService.refreshReportLines(balanceReportFormatInstance, params, BalanceReportLine)

        // Check out everything seems ok on the face of it
        if (balanceReportFormatInstance.lines) {
            def dividerSeen = false
            for (line in balanceReportFormatInstance.lines) {
                if (line.hasErrors()) {
                    valid = false
                    break
                }

                if (line.lineNumber) {
                    if (line.validate()) {
                        if (line.section || line.accumulation) {
                            valueLines << line.lineNumber
                            if (line.accumulation) accumulators << line
                        } else if (line.text == '<=>') {
                            if (dividerSeen) {
                                line.errorMessage(field: 'text', code: 'balanceReportLine.divider', args: [line.lineNumber.toString()],
                                        default: "Report Format line number ${line.lineNumber} is a duplicate divider line")
                                valid = false
                            } else {
                                dividerSeen = true
                            }
                        }
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
                        line.errorMessage(field: 'accumulation', code: 'balanceReportLine.dup.reference', args: [line.lineNumber.toString(), item],
                                default: "Report Format line number ${line.lineNumber} contains a duplicate Accumulation reference to ${item}")
                        valid = false
                        break
                    }

                    seen << item
                    if (item.isInteger()) {    // It's a line number
                        if (!valueLines.contains(item.toInteger())) {
                            line.errorMessage(field: 'accumulation', code: 'balanceReportLine.bad.reference', args: [line.lineNumber.toString(), item],
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
                    balanceReportFormatInstance.removeFromLines(line)
                    blanks++
                }
            }

            BalanceReportFormat.withTransaction {status ->
                for (line in removables) {
                    if (line.id) {
                        balanceReportFormatInstance.removeFromLines(line)
                        line.delete(flush: true)
                        line.discard()
                    }
                }

                if (!balanceReportFormatInstance.save(flush: true)) {    // With deep validation
                    status.setRollbackOnly()
                    valid = false
                }
            }
        }

        if (valid) {
            balanceReportFormatInstance.refresh()
            if (params.resequence && balanceReportFormatInstance.lines) {
                def num = 0
                def mappings = [:]
                def renumbered = false
                accumulators.clear()
                for (line in balanceReportFormatInstance.lines) {
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

                    BalanceReportFormat.withTransaction {status ->
                        if (balanceReportFormatInstance.save(flush: true)) {  // With deep validation
                            for (line in balanceReportFormatInstance.lines) {
                                if (line.lineNumber > 2000000000) line.lineNumber -= 2000000000
                            }

                            if (!balanceReportFormatInstance.save(flush: true)) {  // With deep validation
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
                            for (int i = 0; i < blanks; i++) balanceReportFormatInstance.addToLines(new BalanceReportLine())
                            balanceReportFormatInstance.discard()
                        }

                        flash.message = message(code: 'balanceReportFormat.updated.resequenced', args: [balanceReportFormatInstance.toString()], default: "Report Format ${balanceReportFormatInstance.toString()} updated and resequenced")
                    } else {
                        balanceReportFormatInstance.refresh()
                        flash.message = message(code: 'balanceReportFormat.updated.bad.resequence', args: [balanceReportFormatInstance.toString()], default: "Report Format ${balanceReportFormatInstance.toString()} updated but NOT resequenced")
                    }
                } else {
                    flash.message = message(code: 'balanceReportFormat.updated.no.resequence', args: [balanceReportFormatInstance.toString()], default: "Report Format ${balanceReportFormatInstance.toString()} updated, no resequencing necessary")
                }
            } else {
                flash.message = utilService.standardMessage('updated', balanceReportFormatInstance)
            }
        } else if (balanceReportFormatInstance.lines) {

            // Propagate line error messages to the header
            for (line in balanceReportFormatInstance.lines) {
                for (lineError in utilService.getAllErrorMessages(line)) balanceReportFormatInstance.errors.reject(null, lineError)
            }

            balanceReportFormatInstance.discard()
        }

        if (!valid) flash.clear()

        render(view: 'lines', model: [balanceReportFormatInstance: balanceReportFormatInstance, chartSectionInstanceList: createChartSectionList()])
    }

    def adding() {
        def balanceReportFormatInstance = BalanceReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!balanceReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'balanceReportFormat', params.id)
            redirect(action: 'list')
        } else {
            postingService.refreshReportLines(balanceReportFormatInstance, params, BalanceReportLine)

            // Add some blank lines
            for (int i = 0; i < 10; i++) balanceReportFormatInstance.addToLines(new BalanceReportLine())

            // Grails would automatically save an existing record that was modified if we didn't discard it
            balanceReportFormatInstance.discard()
            render(view: 'lines', model: [balanceReportFormatInstance: balanceReportFormatInstance, chartSectionInstanceList: createChartSectionList()])
        }
    }

    def clone() {
        def balanceReportFormatInstance = BalanceReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!balanceReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'balanceReportFormat', params.id)
            redirect(action: 'list')
            return
        }

        [balanceReportFormatInstance: balanceReportFormatInstance]
    }

    def cloning() {
        def balanceReportFormatInstance = BalanceReportFormat.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!balanceReportFormatInstance) {
            flash.message = utilService.standardMessage('not.found', 'balanceReportFormat', params.id)
            redirect(action: 'list')
            return
        }

        balanceReportFormatInstance.name = params.name

        def newFormat = new BalanceReportFormat(company: balanceReportFormatInstance.company,
                name: balanceReportFormatInstance.name, title: balanceReportFormatInstance.title, subTitle: balanceReportFormatInstance.subTitle,
                column1Heading: balanceReportFormatInstance.column1Heading, column1SubHeading: balanceReportFormatInstance.column1SubHeading,
                column1PrimaryData: balanceReportFormatInstance.column1PrimaryData, column1Calculation: balanceReportFormatInstance.column1Calculation,
                column1SecondaryData: balanceReportFormatInstance.column1SecondaryData, column2Heading: balanceReportFormatInstance.column2Heading,
                column2SubHeading: balanceReportFormatInstance.column2SubHeading, column2PrimaryData: balanceReportFormatInstance.column2PrimaryData,
                column2Calculation: balanceReportFormatInstance.column2Calculation, column2SecondaryData: balanceReportFormatInstance.column2SecondaryData,
                column3Heading: balanceReportFormatInstance.column3Heading, column3SubHeading: balanceReportFormatInstance.column3SubHeading,
                column3PrimaryData: balanceReportFormatInstance.column3PrimaryData, column3Calculation: balanceReportFormatInstance.column3Calculation,
                column3SecondaryData: balanceReportFormatInstance.column3SecondaryData, column4Heading: balanceReportFormatInstance.column4Heading,
                column4SubHeading: balanceReportFormatInstance.column4SubHeading, column4PrimaryData: balanceReportFormatInstance.column4PrimaryData,
                column4Calculation: balanceReportFormatInstance.column4Calculation, column4SecondaryData: balanceReportFormatInstance.column4SecondaryData)

        if (balanceReportFormatInstance.lines) {
            for (line in balanceReportFormatInstance.lines) {
                newFormat.addToLines(new BalanceReportLine(lineNumber: line.lineNumber, text: line.text, section: line.section, accumulation: line.accumulation))
            }
        }

        balanceReportFormatInstance.discard()    // Don't let Grails save the original
        if (newFormat.save(flush: true)) {     // With deep validation
            redirect(action: 'edit', id: newFormat.id)
        } else {
            def msgs = utilService.getAllErrorMessages(newFormat)
            if (msgs) {
                for (newError in msgs) balanceReportFormatInstance.errors.reject(null, newError)
            } else {
                balanceReportFormatInstance.errorMessage(code: 'balanceReportFormat.bad.clone', default: 'Unable to save the cloned format')
            }

            render(view: 'clone', model: [balanceReportFormatInstance: balanceReportFormatInstance])
        }
    }
	
	// --------------------------------------------- Support Methods ---------------------------------------------
	
    private createChartSectionList() {
        return ChartSection.findAll('from ChartSection where company = ? and type = ? and accountSegment > ? order by treeSequence', [utilService.currentCompany(), 'bs', (byte) 0])
    }
}