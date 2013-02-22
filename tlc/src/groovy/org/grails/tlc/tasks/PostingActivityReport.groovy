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

import org.grails.tlc.books.Document
import org.grails.tlc.sys.TaskExecutable

class PostingActivityReport extends TaskExecutable {

    def execute() {
        if (company.systemOnly) return true     // Don't do this for the System company
        if (isPreempted()) {
            completionMessage = message(code: 'report.activity.preempted', default: 'This task would duplicate one currently running')
            return false
        }

        def seriesDate, minId
        def maxId = Document.executeQuery('select max(id) from Document')[0]
        if (!maxId) {
            completionMessage = message(code: 'report.activity.none', default: 'Nothing to report')
            return true
        }

        def today = utilService.fixDate()
        def priorRun = getPriorRun(true)
        if (priorRun?.completedAt >= today - 8) {
            def priorResults = getPriorResults()
            if (priorResults?.maxId?.isLong()) minId = priorResults.maxId.toLong() + 1L
        }

        if (!minId) {
            minId = Document.executeQuery('select min(id) from Document where securityCode = ? and dateCreated >= ?', [company.securityCode, today])[0]
            if (!minId || minId > maxId) {
                results.maxId = maxId.toString()
                results.seriesDate = today
                completionMessage = message(code: 'report.activity.none', default: 'Nothing to report')
                return true
            }

            seriesDate = today
        }

        yield()
        def reportParams = ['NO_FILE_IF_NO_PAGES': true]
        def title = message(code: 'report.activity.title', default: 'Posting Activity')
        reportParams.put('reportTitle', title)
        reportParams.put('colDocument', message(code: 'report.postings.colDocument', default: 'Document'))
        reportParams.put('colDate', message(code: 'report.postings.colDate', default: 'Date'))
        reportParams.put('colPeriod', message(code: 'report.period.label', default: 'Period'))
        reportParams.put('colAccount', message(code: 'generalBalance.account.label', default: 'Account'))
        reportParams.put('colDebit', message(code: 'generic.debit', default: 'Debit'))
        reportParams.put('colCredit', message(code: 'generic.credit', default: 'Credit'))
        reportParams.put('txtError', message(code: 'report.postings.txtError', default: '<<< Error >>>'))
        reportParams.put('minId', minId)
        reportParams.put('maxId', maxId)
        reportParams.put('minIdPrompt', message(code: 'report.activity.txtError', default: 'Start document id'))
        reportParams.put('maxIdPrompt', message(code: 'report.activity.txtError', default: 'End document id'))
        reportParams.put('seriesDatePrompt', message(code: 'report.activity.txtError', default: 'New report series start date'))
        if (seriesDate) reportParams.put('seriesDate', seriesDate)
        def pdfFile = createReportPDF('PostingActivity', reportParams)
        yield()
        if (pdfFile) {
            mailService.sendMail {
                multipart true
                to user.email
                subject title
                body(view: '/emails/genericReport', model: [companyInstance: company, systemUserInstance: user, title: title])
                attach pdfFile
            }

            yield()
            pdfFile.delete()
        } else {
            completionMessage = message(code: 'report.activity.none', default: 'Nothing to report')
        }

        results.minId = minId
        results.maxId = maxId
        if (seriesDate) results.seriesDate = seriesDate
        return true
    }
}
