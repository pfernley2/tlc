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

public class ChartOfAccountsReport extends TaskExecutable {

    def execute() {
        def reportParams = [:]
        def title = message(code: 'report.chartAcct.title', default: 'Chart of Accounts')
        reportParams.put('reportTitle', title)
        reportParams.put('column1', message(code: 'report.chartAcct.column1', default: 'Section'))
        reportParams.put('column2', message(code: 'report.chartAcct.column2', default: 'Accounts'))
        yield()
        def pdfFile = createReportPDF('ChartOfAccounts', reportParams)
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
    }
}