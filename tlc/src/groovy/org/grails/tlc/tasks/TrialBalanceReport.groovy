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

import org.grails.tlc.books.Period
import org.grails.tlc.sys.SystemWorkarea
import org.grails.tlc.sys.TaskExecutable

class TrialBalanceReport extends TaskExecutable {

    def execute() {
        def pd = Period.get(params.stringId)
        if (pd?.securityCode != company.securityCode) {
            completionMessage = utilService.standardMessage('not.found', 'period', params.stringId)
            return false
        }

        yield()
        def pid = utilService.getNextProcessId()
        def sql = 'insert into SystemWorkarea (process, identifier, decimal1) select ' + pid.toString() + 'L, id, companyClosingBalance from GeneralBalance where period = ?'
        if (params.omitZero) sql += ' and companyClosingBalance != 0.0'
        def lock = bookService.getCompanyLock(company)
        if (pd.status != 'closed') lock.lock()      // Don't lock the company unless we have to
        try {
            SystemWorkarea.withTransaction {status ->
                SystemWorkarea.executeUpdate(sql, [pd])
            }
        } finally {
            if (pd.status != 'closed') lock.unlock()
        }

        yield()
        def reportParams = [:]
        def title = message(code: 'report.tb.title', args: [pd.code], default: 'Trial Balance for ' + pd.code)
        reportParams.put('reportTitle', title)
        reportParams.put('pid', pid)
        reportParams.put('colDebit', message(code: 'generic.debit', default: 'Debit'))
        reportParams.put('colCredit', message(code: 'generic.credit', default: 'Credit'))
        reportParams.put('txtOmitZero', message(code: 'report.omitZero.label', default: 'Omit Zero Values'))
        reportParams.put('valOmitZero', params.omitZero)
        reportParams.txtBF = message(code: 'generic.bf', default: 'b/f')
        reportParams.txtCF = message(code: 'generic.cf', default: 'c/f')
        def pdfFile = createReportPDF('TrialBalance', reportParams)
        yield()
        SystemWorkarea.withTransaction {status ->
            SystemWorkarea.executeUpdate('delete from SystemWorkarea where process = ?', [pid])
        }

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
