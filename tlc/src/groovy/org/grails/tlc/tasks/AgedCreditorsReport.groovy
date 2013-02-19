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

import org.grails.tlc.sys.TaskExecutable

class AgedCreditorsReport extends TaskExecutable {

    def execute() {
        def reportParams = [:]
        def accessCodeList = bookService.supplierAccessCodes(company, user)
        def selectedCodes
        def codes = params.codes?.split(',') as List
        if (codes) {
            selectedCodes = accessCodeList.findAll {codes.contains(it.code)}
            reportParams.codes = params.codes
        } else {
            selectedCodes = accessCodeList
            reportParams.codes = message(code: 'generic.all.selection', default: '-- all --')
        }

        if (!selectedCodes) {
            completionMessage = message(code: 'report.no.access', default: 'You do not have permission to access any accounts and therefore cannot run this report.')
            return false
        }

        def mainClause = ' and access_code_id in ('
        for (int i = 0; i < selectedCodes.size(); i++) {
            if (i) mainClause += ','
            mainClause += selectedCodes[i].id.toString()
        }

        mainClause += ')'
        reportParams.mainClause = mainClause

        def days1 = utilService.setting('supplier.age.days.1', 30, company)
        def days2 = utilService.setting('supplier.age.days.2', 60, company)
        def days3 = utilService.setting('supplier.age.days.3', 90, company)
        if (days1 <= 0 || days2 <= days1 || days3 <= days2 || days3 > 1000) {
            completionMessage = message(code: 'report.bad.ages', default: 'The ageing days are invalid in the company settings')
            return false
        }

        def today = utilService.fixDate()
        reportParams.age1Date = today
        reportParams.age2Date = today - days1
        reportParams.age3Date = today - days2
        reportParams.olderDate = today - days3
        reportParams.colCode = message(code: 'supplier.code.label', default: 'Code')
        reportParams.colName = message(code: 'supplier.name.label', default: 'Name')
        reportParams.colBalance = message(code: 'report.balance', default: 'Balance')
        reportParams.colCurrent = message(code: 'report.current', args: [days1], default: 'Current')
        reportParams.colAge1 = message(code: 'report.ages', args: [days1], default: days1.toString() + ' Days')
        reportParams.colAge2 = message(code: 'report.ages', args: [days2], default: days2.toString() + ' Days')
        reportParams.colAge3 = message(code: 'report.ages', args: [days3], default: days3.toString() + ' Days')
        reportParams.colOlder = message(code: 'report.older', args: [days3], default: 'Older')
        reportParams.codesPrompt = message(code: 'report.accessCode.label', default: 'Access Code(s)')
        def title = message(code: 'supplier.aged', default: 'Aged List of Creditors')
        reportParams.reportTitle = title
        yield()
        def pdfFile = createReportPDF('AgedCreditors', reportParams)
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
}
