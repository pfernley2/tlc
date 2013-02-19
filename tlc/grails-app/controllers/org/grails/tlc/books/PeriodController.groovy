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

class PeriodController {

    // Injected services
    def utilService
    def bookService

    // Security settings
    def activities = [default: 'actadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', opening: 'POST', closing: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'validFrom', 'status', 'validTo'].contains(params.sort) ? params.sort : 'code'
        def ddSource = utilService.source('year.list')
        [periodInstanceList: Period.selectList(securityCode: utilService.currentCompany().securityCode), periodInstanceTotal: Period.selectCount(), ddSource: ddSource]
    }

    def show() {
        def periodInstance = Period.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!periodInstance) {
            flash.message = utilService.standardMessage('not.found', 'period', params.id)
            redirect(action: 'list')
        } else {
            return [periodInstance: periodInstance]
        }
    }

    def delete() {
        def periodInstance = Period.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (periodInstance) {
            def pd = Period.countBySecurityCodeAndValidFrom(periodInstance.securityCode, periodInstance.validTo + 1)
            if (pd) {
                flash.message = message(code: 'period.delete.gap', default: 'Deleting this period would leave a gap in the date ranges. Please delete subsequent periods first.')
                redirect(action: 'show', id: params.id)
            } else if (periodInstance.validTo == periodInstance.year.validTo && Year.countByCompanyAndValidFrom(utilService.currentCompany(), periodInstance.validTo + 1)) {
                flash.message = message(code: 'period.delete.year', default: 'Deleting this period would invalidate the following year. Delete the following year record first.')
                redirect(action: 'show', id: params.id)
            } else {
                try {
                    bookService.deletePeriod(periodInstance)
                    flash.message = utilService.standardMessage('deleted', periodInstance)
                    redirect(action: 'list')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', periodInstance)
                    redirect(action: 'show', id: params.id)
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'period', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def periodInstance = Period.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!periodInstance) {
            flash.message = utilService.standardMessage('not.found', 'period', params.id)
            redirect(action: 'list')
        } else {
            def statusOptions
            if (periodInstance.status == 'open' || periodInstance.status == 'adjust') statusOptions = ['open', 'adjust']

            return [periodInstance: periodInstance, statusOptions: statusOptions]
        }
    }

    def update(Long version) {
        def periodInstance = Period.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (periodInstance) {
            if (version != null && periodInstance.version > version) {
                periodInstance.errorMessage(code: 'locking.failure', domain: 'period')
                def statusOptions
                if (periodInstance.status == 'open' || periodInstance.status == 'adjust') statusOptions = ['open', 'adjust']
                render(view: 'edit', model: [periodInstance: periodInstance, statusOptions: statusOptions])
                return
            }

            def oldFrom = periodInstance.validFrom
            def oldTo = periodInstance.validTo
            def oldStatus = periodInstance.status
            periodInstance.properties['code', 'validFrom', 'status', 'validTo'] = params
            def valid = !periodInstance.hasErrors()
            if (valid && oldFrom == periodInstance.year.validFrom && oldFrom != periodInstance.validFrom) {

                // Must be an error if we were the first period and now our start date has changed
                periodInstance.errorMessage(field: 'validFrom', code: 'period.validFrom.initial', default: 'The Valid From date must be the same as the start date of the year')
                valid = false
            }

            if (valid && periodInstance.status != oldStatus && periodInstance.status == 'new' || periodInstance.status == 'closed') {
                periodInstance.errorMessage(field: 'status', code: 'period.status.invalid', default: 'Invalid change of period status')
                valid = false
            }

            if (valid) {
                Period.withTransaction {status ->
                    if (periodInstance.saveThis()) {
                        if (periodInstance.validFrom != oldFrom) {
                            def pd = Period.findByYearAndValidTo(periodInstance.year, oldFrom - 1)
                            if (pd.validFrom <= periodInstance.validFrom - 1) {
                                pd.validTo = periodInstance.validFrom - 1
                                if (!pd.saveThis()) {
                                    periodInstance.errorMessage(field: 'validFrom', code: 'period.validFrom.precede', default: 'Unable to update the Valid To date of the preceding period')
                                    status.setRollbackOnly()
                                    valid = false
                                }
                            } else {
                                periodInstance.errorMessage(field: 'validFrom', code: 'period.validFrom.before', default: 'The new Valid From date would invalidate the preceding period')
                                status.setRollbackOnly()
                                valid = false
                            }
                        }

                        if (valid && periodInstance.validTo != oldTo) {
                            def pd = Period.findByYearAndValidFrom(periodInstance.year, oldTo + 1)
                            if (pd) {
                                if (pd.validTo >= periodInstance.validTo + 1) {
                                    pd.validFrom = periodInstance.validTo + 1
                                    if (!pd.saveThis()) {
                                        periodInstance.errorMessage(field: 'validTo', code: 'period.validFrom.follow', default: 'Unable to update the Valid From date of the following period')
                                        status.setRollbackOnly()
                                        valid = false
                                    }
                                } else {
                                    periodInstance.errorMessage(field: 'validTo', code: 'period.validFrom.after', default: 'The new Valid To date would invalidate the following period')
                                    status.setRollbackOnly()
                                    valid = false
                                }
                            }
                        }
                    } else {
                        status.setRollbackOnly()
                        valid = false
                    }
                }
            }

            if (valid) {
                flash.message = utilService.standardMessage('updated', periodInstance)
                redirect(action: 'show', id: periodInstance.id)
            } else {
                def statusOptions
                if (periodInstance.status == 'open' || periodInstance.status == 'adjust') statusOptions = ['open', 'adjust']

                render(view: 'edit', model: [periodInstance: periodInstance, statusOptions: statusOptions])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'period', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def periodInstance = new Period()
        def year = utilService.reSource('year.list')
        periodInstance.year = year   // Ensure correct parent
        def pds = Period.findAllByYear(year, [sort: 'validFrom', order: 'desc', max: 1])
        def pd
        if (pds) {
            pd = pds[0]
            if (pd.validTo < year.validTo) periodInstance.validFrom = pd.validTo + 1
        } else {
            periodInstance.validFrom = year.validFrom
        }

        if (periodInstance.validFrom) {
            def cal = Calendar.getInstance()
            cal.setTime(periodInstance.validFrom)
            cal.add(Calendar.MONTH, 1)
            cal.add(Calendar.DATE, -1)
            periodInstance.validTo = (cal.getTime() > year.validTo) ? year.validTo : cal.getTime()
            def code, val
            if (pd) {
                if (pd.code == convertDateToPeriodCode(pd.validFrom)) {
                    code = convertDateToPeriodCode(periodInstance.validFrom)
                } else if (pd.code.startsWith(pd.year.code) && pd.code.length() > pd.year.code.length() && pd.code[-1] >= '0' && pd.code[-1] <= '9') {
                    def suffix = pd.code.substring(pd.year.code.length())
                    def pos
                    for (pos = suffix.length() - 1; pos >= 0; pos--) {
                        if (suffix[pos] < '0' || suffix[pos] > '9') break
                    }

                    if (pos < suffix.length() - 1) {
                        val = (suffix.substring(pos + 1).toInteger() + 1).toString()
                        code = periodInstance.year.code
                        if (pos >= 0) code += suffix.substring(0, pos + 1)
                        if (val.length() < suffix.length() - 1 - pos) val = val.padLeft(suffix.length() - 1 - pos, '0')
                        code += val
                    }
                }
            } else {
                code = convertDateToPeriodCode(periodInstance.validFrom)
            }

            if (code && code.length() <= 10 && !Period.countByYearAndCode(periodInstance.year, code)) periodInstance.code = code
        }

        return [periodInstance: periodInstance, statusOptions: null]
    }

    def save() {
        def periodInstance = new Period()
        periodInstance.properties['code', 'validFrom', 'validTo'] = params
        periodInstance.year = utilService.reSource('year.list')   // Ensure correct parent
        def valid = !periodInstance.hasErrors()
        def pds = Period.findAllByYear(periodInstance.year, [sort: 'validFrom', order: 'desc', max: 1])
        if (pds) {
            if (periodInstance.validFrom != pds[0].validTo + 1) {
                periodInstance.errorMessage(field: 'validFrom', code: 'period.validFrom.consecutive', default: "The Valid From date must be one day after the preceeding period's Valid To date")
                valid = false
            }
        } else {
            if (periodInstance.validFrom != periodInstance.year.validFrom) {
                periodInstance.errorMessage(field: 'validFrom', code: 'period.validFrom.initial', default: "The Valid From date must be the same as the start date of the year")
                valid = false
            }
        }

        if (valid) valid = bookService.insertPeriod(periodInstance)
        if (valid) {
            flash.message = utilService.standardMessage('created', periodInstance)
            redirect(action: 'show', id: periodInstance.id)
        } else {
            render(view: 'create', model: [periodInstance: periodInstance, statusOptions: null])
        }
    }

    def open() {
        def queueNumber = params.queueNumber ? params.queueNumber.toLong() : 0L
        def periodInstance
        if (!queueNumber) {
            def pds = bookService.getActivePeriods(utilService.currentCompany())
            if (pds) {
                periodInstance = Period.findBySecurityCodeAndValidFrom(utilService.currentCompany().securityCode, pds[-1].validTo + 1)
            } else {
                def allPds = Period.findAllBySecurityCode(utilService.currentCompany().securityCode, [sort: 'validFrom', max: 1])
                if (allPds) periodInstance = allPds[0]
            }

            // Check any period has not been opened before
            if (periodInstance && periodInstance.status != 'new') periodInstance = null

            // Check any period will not create a situation of having more than two years open at the same time
            if (periodInstance && pds) {
                def count = 0
                def lastYear = 0L
                for (pd in pds) {
                    if (pd.year.id != lastYear) {
                        lastYear = pd.year.id
                        count++
                    }

                    // If the period to be opened is in an already open year, then everything is ok
                    if (periodInstance.year.id == lastYear) {
                        count = 0
                        break
                    }
                }

                // Error if there are already two years open, not including the one we are trying to open
                if (count >= 2) periodInstance = null
            }
        }

        return [periodInstance: periodInstance, queueNumber: queueNumber]
    }

    def opening() {
        def result = utilService.demandRunFromParams('pdOpen', params)
        if (result instanceof String) {
            flash.message = result
            result = 0L
        }

        params.put('queueNumber', result.toString())
        redirect(action: 'open', params: params)
    }

    def close() {
        def periodInstance
        def pds = bookService.getActivePeriods(utilService.currentCompany())

        // Can't close the only open period
        if (pds.size() > 1) periodInstance = pds[0]

        return [periodInstance: periodInstance]
    }

    def closing() {
        def valid = true
        def periodInstance
        def lock = bookService.getCompanyLock(utilService.currentCompany())
        lock.lock()
        try {
            def pds = bookService.getActivePeriods(utilService.currentCompany())
            if (pds.size() < 2 || pds[0].id.toString() != params.id) {
                flash.message = message(code: 'period.status.invalid', default: 'Invalid change of period status')
                valid = false
            } else {
                pds[0].status = 'closed'
                if (pds[0].saveThis()) {
                    periodInstance = pds[0]
                } else {
                    flash.message = message(code: 'period.gl.status', args: [pds[0].code], default: "Unable to save the change of status for period ${pds[0].code}")
                    valid = false
                }
            }
        } finally {
            lock.unlock()
        }

        if (valid) {
            flash.message = utilService.standardMessage('updated', periodInstance)
            redirect(controller: 'systemMenu', action: 'display')
        } else {
            redirect(action: 'close')
        }
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private convertDateToPeriodCode(date) {
        def cal = Calendar.getInstance()
        cal.setTime(date)
        return cal.get(Calendar.YEAR).toString() + '-' + (cal.get(Calendar.MONTH) + 1).toString().padLeft(2, '0')
    }
}