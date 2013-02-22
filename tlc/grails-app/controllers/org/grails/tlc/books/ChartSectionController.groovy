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

import grails.converters.JSON

class ChartSectionController {

    // Injected services
    def utilService
    def bookService

    // Security settings
    def activities = [default: 'actadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', printing: 'POST', dynatree: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['path', 'name', 'type', 'autoCreate', 'sequencer', 'status'].contains(params.sort) ? params.sort : 'treeSequence'
        def chartSectionInstanceList = ChartSection.selectList(company: utilService.currentCompany())
        def segmentsList = []
        def defaultsList = []
        def segments, segment, defaults, dflt
        for (section in chartSectionInstanceList) {
            segments = ''
            defaults = ''
            for (int i = 1; i <= 8; i++) {
                segment = section."segment${i}"
                if (segment) {
                    if (i > 1) {
                        segments += '<br/>'
                        defaults += '<br/>'
                    }

                    segments += segment.name.encodeAsHTML()
                    dflt = section."default${i}"
                    if (dflt) {
                        defaults += dflt.encodeAsHTML()
                    } else {
                        defaults += '&nbsp;'
                    }
                } else {
                    break
                }
            }

            segmentsList << segments
            defaultsList << defaults
        }

        [chartSectionInstanceList: chartSectionInstanceList, chartSectionInstanceTotal: ChartSection.selectCount(), segmentsList: segmentsList, defaultsList: defaultsList]
    }

    def show() {
        def chartSectionInstance = ChartSection.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!chartSectionInstance) {
            flash.message = utilService.standardMessage('not.found', 'chartSection', params.id)
            redirect(action: 'list')
        } else {
            return [chartSectionInstance: chartSectionInstance]
        }
    }

    def delete() {
        def chartSectionInstance = ChartSection.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (chartSectionInstance) {
            def dependents = ChartSection.findAllByCompanyAndPathLike(utilService.currentCompany(), chartSectionInstance.path + '.%')
            try {
                ChartSection.withTransaction {status ->
                    chartSectionInstance.delete(flush: true)
                    utilService.cacheService.resetThis('ranges', chartSectionInstance.securityCode, chartSectionInstance.toString())
                    for (child in dependents) {
                        child.delete(flush: true)
                        utilService.cacheService.resetThis('ranges', child.securityCode, child.toString())
                    }
                }

                flash.message = utilService.standardMessage('deleted', chartSectionInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', chartSectionInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'chartSection', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def chartSectionInstance = ChartSection.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!chartSectionInstance) {
            flash.message = utilService.standardMessage('not.found', 'chartSection', params.id)
            redirect(action: 'list')
        } else {
            return [chartSectionInstance: chartSectionInstance, elementList: CodeElement.findAllByCompany(utilService.currentCompany(), [sort: 'elementNumber'])]
        }
    }

    def update(Long version) {
        def chartSectionInstance = ChartSection.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (chartSectionInstance) {
            if (version != null && chartSectionInstance.version > version) {
                chartSectionInstance.errorMessage(code: 'locking.failure', domain: 'chartSection')
                render(view: 'edit', model: [chartSectionInstance: chartSectionInstance, elementList: CodeElement.findAllByCompany(utilService.currentCompany(), [sort: 'elementNumber'])])
                return
            }

            def oldPath = chartSectionInstance.path
            def oldCode = chartSectionInstance.code
            def oldSequencer = chartSectionInstance.sequencer
            def oldType = chartSectionInstance.type
            def oldSegments = [chartSectionInstance.segment1, chartSectionInstance.segment2, chartSectionInstance.segment3, chartSectionInstance.segment4,
                chartSectionInstance.segment5, chartSectionInstance.segment6, chartSectionInstance.segment7, chartSectionInstance.segment8]
            chartSectionInstance.properties['path', 'name', 'type', 'autoCreate', 'segment1', 'segment2', 'segment3', 'segment4', 'segment5', 'segment6', 'segment7',
                'segment8', 'default1', 'default2', 'default3', 'default4', 'default5', 'default6', 'default7', 'default8', 'sequencer', 'status'] = params
            if (chartSectionInstance.path && chartSectionInstance.path.contains('.') && !chartSectionInstance.path.endsWith('.')) {
                chartSectionInstance.parentObject = ChartSection.findByCompanyAndPath(chartSectionInstance.company, chartSectionInstance.path.substring(0, chartSectionInstance.path.lastIndexOf('.')))
            }

            fixDefaults(chartSectionInstance)
            def typeInserted = false
            def cacheCleared = false
            if (chartSectionInstance.path && !chartSectionInstance.path.endsWith('.')) {
                if (chartSectionInstance.path.contains('.')) {
                    chartSectionInstance.code = chartSectionInstance.path.substring(chartSectionInstance.path.lastIndexOf('.') + 1)
                    if (!chartSectionInstance.type) {
                        chartSectionInstance.type = 'ie'    // Anything will do since it will be overwritten on save
                        typeInserted = true
                    }
                } else {
                    chartSectionInstance.code = chartSectionInstance.path
                }
            }

            utilService.verify(chartSectionInstance, ['segment1', 'segment2', 'segment3', 'segment4', 'segment5', 'segment6', 'segment7', 'segment8']) // Ensure correct references
            def valid = !chartSectionInstance.hasErrors()
            if (valid) {
                ChartSection.withTransaction {status ->
                    if (chartSectionInstance.saveThis()) {

                        // Update the cache if the code has changed
                        if (chartSectionInstance.code != oldCode) {
                            utilService.cacheService.resetThis('ranges', chartSectionInstance.securityCode, oldCode)
                            utilService.cacheService.resetThis('ranges', chartSectionInstance.securityCode, chartSectionInstance.toString())
                            cacheCleared = true
                        }

                        if (rangesAreValid(chartSectionInstance, oldSegments)) {
                            if (bookService.defaultsAreValid(chartSectionInstance)) {
                                if (chartSectionInstance.path != oldPath || chartSectionInstance.sequencer != oldSequencer || chartSectionInstance.type != oldType) {
                                    if (accountsAreValid(chartSectionInstance, oldType, oldSegments)) {
                                        def len = (chartSectionInstance.path != oldPath) ? oldPath.length() : 0
                                        def sections = ChartSection.findAllByCompanyAndPathLike(chartSectionInstance.company, oldPath + '.%')
                                        for (section in sections) {
                                            section.parentObject = ChartSection.get(section.parent)
                                            if (len) section.path = chartSectionInstance.path + section.path.substring(len)
                                            if (chartSectionInstance.type != oldType) section.type = chartSectionInstance.type
                                            if (section.saveThis()) {
                                                if (!accountsAreValid(section, oldType)) {
                                                    status.setRollbackOnly()
                                                    valid = false
                                                    break
                                                }
                                            } else {
                                                status.setRollbackOnly()
                                                valid = false
                                                chartSectionInstance.errorMessage(code: 'chartSection.child.bad', args: [section.code], default: "Unable to save child section ${section.code}")
                                                break
                                            }
                                        }
                                    } else {
                                        status.setRollbackOnly()
                                        valid = false
                                    }
                                }
                            } else {
                                status.setRollbackOnly()
                                valid = false
                            }
                        } else {
                            status.setRollbackOnly()
                            valid = false
                        }
                    } else {
                        status.setRollbackOnly()
                        valid = false
                    }
                }
            }

            if (valid) {
                if (chartSectionInstance.code != oldCode) {
                    def accumulators = ProfitReportLine.findAll('from ProfitReportLine as x where x.format.company = ? and x.accumulation like ?', [chartSectionInstance.company, "%${oldCode}%"])
                    if (accumulators) valid = fixAccumulators(accumulators, oldCode, chartSectionInstance.code)
                    if (valid) {
                        accumulators = BalanceReportLine.findAll('from BalanceReportLine as x where x.format.company = ? and x.accumulation like ?', [chartSectionInstance.company, "%${oldCode}%"])
                        if (accumulators) valid = fixAccumulators(accumulators, oldCode, chartSectionInstance.code)
                    }
                }

                if (valid) {
                    flash.message = utilService.standardMessage('updated', chartSectionInstance)
                } else {
                    flash.message = message(code: 'chartSection.updated.no.formats', args: [chartSectionInstance.toString()], default: "Chart Section ${chartSectionInstance.toString()} updated BUT COULD NOT UPDATE THE REPORT FORMATS for the change of code. Please fix manually.")
                }

                redirect(action: 'show', id: chartSectionInstance.id)
            } else {

                // Update the cache if we cleared it but something subsequently went wrong
                if (cacheCleared) {
                    utilService.cacheService.resetThis('ranges', chartSectionInstance.securityCode, oldCode)
                    utilService.cacheService.resetThis('ranges', chartSectionInstance.securityCode, chartSectionInstance.toString())
                }

                if (typeInserted) chartSectionInstance.type = null
                render(view: 'edit', model: [chartSectionInstance: chartSectionInstance, elementList: CodeElement.findAllByCompany(utilService.currentCompany(), [sort: 'elementNumber'])])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'chartSection', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def chartSectionInstance = new ChartSection()
        chartSectionInstance.company = utilService.currentCompany()   // Ensure correct company
        return [chartSectionInstance: chartSectionInstance, elementList: CodeElement.findAllByCompany(utilService.currentCompany(), [sort: 'elementNumber'])]
    }

    def save() {
        def chartSectionInstance = new ChartSection()
        chartSectionInstance.properties['path', 'name', 'type', 'autoCreate', 'segment1', 'segment2', 'segment3', 'segment4', 'segment5', 'segment6', 'segment7',
            'segment8', 'default1', 'default2', 'default3', 'default4', 'default5', 'default6', 'default7', 'default8', 'sequencer', 'status'] = params
        chartSectionInstance.company = utilService.currentCompany()   // Ensure correct company
        if (chartSectionInstance.path && chartSectionInstance.path.contains('.') && !chartSectionInstance.path.endsWith('.')) {
            chartSectionInstance.parentObject = ChartSection.findByCompanyAndPath(chartSectionInstance.company, chartSectionInstance.path.substring(0, chartSectionInstance.path.lastIndexOf('.')))
        }

        fixDefaults(chartSectionInstance)
        def typeInserted = false
        if (chartSectionInstance.path && !chartSectionInstance.path.endsWith('.')) {
            if (chartSectionInstance.path.contains('.')) {
                chartSectionInstance.code = chartSectionInstance.path.substring(chartSectionInstance.path.lastIndexOf('.') + 1)
                if (!chartSectionInstance.type) {
                    chartSectionInstance.type = 'ie'    // Anything will do since it will be overwritten on save
                    typeInserted = true
                }
            } else {
                chartSectionInstance.code = chartSectionInstance.path
            }
        }

        utilService.verify(chartSectionInstance, ['segment1', 'segment2', 'segment3', 'segment4', 'segment5', 'segment6', 'segment7', 'segment8']) // Ensure correct references
        if (!chartSectionInstance.hasErrors() && chartSectionInstance.saveThis()) {
            utilService.cacheService.resetThis('ranges', chartSectionInstance.securityCode, chartSectionInstance.toString())
            flash.message = utilService.standardMessage('created', chartSectionInstance)
            redirect(action: 'show', id: chartSectionInstance.id)
        } else {
            if (typeInserted) chartSectionInstance.type = null
            render(view: 'create', model: [chartSectionInstance: chartSectionInstance, elementList: CodeElement.findAllByCompany(utilService.currentCompany(), [sort: 'elementNumber'])])
        }
    }

    def tree() { }

    def dynatree() {
        def results = []
        if (params.id?.isLong()) {

            // Get the id of the chart section whose children we are being asked for
            def parentId = params.id.toLong()

            // Only worth attempting to get the direct descendant accounts if we have a parent
            // section id (i.e. we are not being asked for the children of the 'root node')
            if (parentId) {
                def parentInstance = ChartSection.findByIdAndSecurityCode(parentId, utilService.currentCompany().securityCode)
                if (parentInstance) {
                    for (account in Account.findAllBySection(parentInstance, [sort: 'code'])) {
                        results << [title: account.name, tooltip: account.code]
                    }
                }
            }

            // Find any child sections (includes looking for the children of the 'root node')
            for (section in ChartSection.findAllByCompanyAndParent(utilService.currentCompany(), parentId, [sort: 'treeSequence'])) {
                results << [title: section.name, tooltip: section.path, id: section.id, isFolder: true, isLazy: true]
            }

            // If we were asked for the children of the 'root node' but there aren't any
            // (i.e. the chart of accounts is empty) then pass back a message to that effect
            if (!parentId && !results) {
                results << [title: message(code: 'chartSection.empty', default: 'The chart of accounts is empty'), unselectable: true, icon: false]
            }
        }

        render results as JSON
    }

    def print() {
        def result = params.queueNumber ? params.queueNumber.toLong() : 0L
        def parameters = result ? [] : utilService.createTaskParameters('chartAcct', params)
        [queueNumber: result, parameters: parameters, now: utilService.format(new Date(), 2)]
    }

    def printing() {
        def result = utilService.demandRunFromParams('chartAcct', params)
        if (result instanceof String) {
            flash.message = result
            result = 0L
        }

        params.put('queueNumber', result.toString())
        redirect(action: 'print', params: params)
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private accountsAreValid(section, oldType, oldSegments = null) {
        if (section.type != oldType) {
            def accounts = Account.findAllBySectionAndTypeIsNotNull(section)
            for (account in accounts) {
                if (account.type.sectionType != section.type) {
                    section.errorMessage(code: 'chartSection.accounts.bad', args: [section.code], default: "The change of type would leave accounts in section ${section.code} invalid")
                    return false
                }
            }
        }

        return true
    }

    // Validates the ranges against the segments and, if there are now more segments than previously, will adjust both
    // the ranges and accounts to reflect the new segment(s). This method assumes that it is called within a transaction
    private rangesAreValid(section, oldSegments) {

        // If there are any ranges (without ranges there can't be accounts either)
        if (ChartSectionRange.countBySection(section)) {
            def oldSeg, newSeg
            def pos = 0

            // Work through the old segments
            for (; pos < 8; pos++) {

                // Grab the relevant old segment
                oldSeg = oldSegments[pos]

                // Finished if no more old segments
                if (!oldSeg) break

                // Get the new segment
                newSeg = section."segment${pos + 1}"

                // Error if there is no new segment or it's a different element (since this would mess up the ranges)
                if (!newSeg || newSeg.id != oldSeg.id) {
                    section.errorMessage(code: 'chartSection.ranges.bad', args: [section.code], default: "The change of segments would leave the ranges for section ${section.code} invalid")
                    return false
                }
            }

            // Work variables we will need
            def rangeAppend = ''
            def accountAppend = ''
            def hasAccounts = null
            def elementNumbers = []
            def elementValues = []
            def elementValue

            // Continue on from the old segments looking for added new segments
            for (; pos < 8; pos++) {

                // Get the new segment
                newSeg = section."segment${pos + 1}"

                // Finished if there is no new segment
                if (!newSeg) break

                // We adjust the ranges by adding an asterisk to indicate all values of the new segment are alllowed
                rangeAppend = rangeAppend + BookService.SEGMENT_DELIMITER + '*'

                // We have an extra segment and so we need to know if it has any accounts or not
                if (hasAccounts == null) hasAccounts = (Account.countBySection(section) > 0)

                // If the section has accounts
                if (hasAccounts) {

                    // Grab the default value for this new segment
                    oldSeg = section."default${pos + 1}"    // Re-use the oldSeg variable to hold the default

                    // Error if there isn't a default since we need it to append to the end of the account codes
                    if (!oldSeg) {
                        section.errorMessage(code: 'chartSection.no.default', default: 'You cannot add a new code segment to an active section without supplying a default value to update its accounts with')
                        return false
                    }

                    // Error if the default value is not a real code element value since we append it to account codes
                    elementValue = CodeElementValue.findByElementAndCode(newSeg, oldSeg)
                    if (!elementValue) {
                        section.errorMessage(code: 'chartSection.no.value', default: "${oldSeg} is not a valid default value")
                        return false
                    }

                    // We adjust the account codes by appending the default value
                    accountAppend = accountAppend + BookService.SEGMENT_DELIMITER + oldSeg

                    // We will need the element number and the actual value record to modify the accounts with
                    elementNumbers << newSeg.elementNumber
                    elementValues << elementValue
                }
            }

            // If we need to modify any ranges for new segments
            if (rangeAppend) {
                def ranges = ChartSectionRange.findAllBySection(section)
                for (range in ranges) {
                    range.rangeFrom = range.rangeFrom + rangeAppend
                    range.rangeTo = range.rangeTo + rangeAppend
                    if (!range.saveThis()) {
                        section.errorMessage(code: 'chartSection.range.save', default: 'Error saving the modified range records')
                        return false
                    }
                }

                // Update the cache since we have modified the ranges
                utilService.cacheService.resetThis('ranges', section.securityCode, section.toString())

                // If there are account codes that need modifying
                if (accountAppend) {
                    def accounts = Account.findAllBySection(section)
                    for (account in accounts) {
                        def oldCode = account.code
                        account.code = account.code + accountAppend

                        // Update the account's element values
                        for (int i = 0; i < elementNumbers.size(); i++) {
                            account."element${elementNumbers[i]}" = elementValues[i]
                        }

                        if (account.saveThis()) {
                            utilService.cacheService.resetByValue('account', account.securityCode, oldCode)
                            utilService.cacheService.resetThis('userAccount', account.securityCode, oldCode)
                        } else {
                            section.errorMessage(code: 'chartSection.account.save', default: 'Error saving the modified account records')
                            return false
                        }
                    }
                }
            }
        }

        return true
    }

    private fixDefaults(section) {
        section.default1 = bookService.fixCase(section.default1)
        section.default2 = bookService.fixCase(section.default2)
        section.default3 = bookService.fixCase(section.default3)
        section.default4 = bookService.fixCase(section.default4)
        section.default5 = bookService.fixCase(section.default5)
        section.default6 = bookService.fixCase(section.default6)
        section.default7 = bookService.fixCase(section.default7)
        section.default8 = bookService.fixCase(section.default8)
    }

    private fixAccumulators(list, oldCode, newCode) {
        def valid = true
        def items, data, modified, ftt
        ChartSection.withTransaction {status ->
            for (line in list) {
                modified = false
                ftt = true
                data = line.accumulation.substring(0, 1) + ' '
                items = line.accumulation.substring(1).split(',')*.trim()
                for (item in items) {
                    if (item == oldCode) {
                        item = newCode
                        modified = true
                    }

                    if (ftt) {
                        data += item
                        ftt = false
                    } else {
                        data += ', ' + item
                    }
                }

                if (modified) {
                    line.accumulation = data
                    if (!line.saveThis()) {
                        status.setRolbackOnly()
                        valid = false
                        break
                    }
                }
            }
        }

        return valid
    }
}