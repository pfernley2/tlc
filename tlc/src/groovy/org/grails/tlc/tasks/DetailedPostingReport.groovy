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

import org.grails.tlc.books.Account
import org.grails.tlc.books.ChartSection
import org.grails.tlc.books.GeneralTransaction
import org.grails.tlc.books.Period
import org.grails.tlc.sys.SystemWorkarea
import org.grails.tlc.sys.TaskExecutable

class DetailedPostingReport extends TaskExecutable {

    def execute() {
        def pd = Period.get(params.stringId)
        if (pd?.securityCode != company.securityCode) {
            completionMessage = utilService.standardMessage('not.found', 'period', params.stringId)
            return false
        }

        def pid = utilService.getNextProcessId()
        def sql = 'insert into SystemWorkarea (process, identifier, decimal1, decimal2) select ' + pid.toString() +
                        'L, id, companyClosingBalance, companyOpeningBalance from GeneralBalance where period = ?'
        def sqlParams = [pd]
        def temp
        def mainClause = ''
        def subClause = ''
        def sectionIdList
        def title
        if (params.scope == 'account') {
            temp = Account.get(params.scopeId)
            if (temp?.securityCode != company.securityCode || !bookService.hasAccountAccess(temp, company, user)) {
                completionMessage = utilService.standardMessage('not.found', 'account', params.scopeId)
                return false
            }

            mainClause = 'and id = ' + temp.section.id.toString()
            subClause = 'and ac.id = ' + temp.id.toString()
            sql += ' and account = ?'
            sqlParams << temp
            title = message(code: 'report.postings.accountr', args: [pd.code, temp.code], default: 'Detailed Postings for ' + pd.code + ', Account ' + temp.code)
        } else if (params.scope == 'section') {
            temp = ChartSection.get(params.scopeId)
            if (temp?.securityCode != company.securityCode) {
                completionMessage = utilService.standardMessage('not.found', 'chartSection', params.scopeId)
                return false
            }

            mainClause = 'and id = ' + temp.id.toString()

            // Note that the following may actually be slower than storing ALL balances in the work area. It depends on the database
            // and its optimizer. For all other types of report (other than the 'specific account' type above) we DO load ALL the
            // balances. This is true even for the 'income and expediture only' or 'balance sheet only' types since the subquery
            // would almost certainly be more expensive than loading all balances. For 'muli-report' types, we also need all balances.
            sql += ' and account in (from Account where section = ?)'
            sqlParams << temp
            title = message(code: 'report.postings.sectionr', args: [pd.code, temp.code], default: 'Detailed Postings for ' + pd.code + ', Section ' + temp.code)
        } else if (params.scope == 'ie') {
            mainClause = 'and section_type = \'ie\''    // Jasper doesn't like GStrings in its parameters map
            title = message(code: 'report.postings.ier', args: [pd.code], default: 'Detailed Income & Expenditure Postings for ' + pd.code)
        } else if (params.scope == 'bs') {
            mainClause = 'and section_type = \'bs\''
            title = message(code: 'report.postings.bsr', args: [pd.code], default: 'Detailed Balance Sheet Postings for ' + pd.code)
        } else if (params.scope == 'separate') {
            sectionIdList = ChartSection.executeQuery('select x.id from ChartSection as x where x.company = ? and exists (from Account where section = x) order by x.treeSequence', [company])
        } else {
            title = message(code: 'report.postings.title', args: [pd.code], default: 'Detailed Postings for ' + pd.code)
        }

        yield()
        def lock = bookService.getCompanyLock(company)
        lock.lock()
        try {
            SystemWorkarea.withTransaction {status ->
                SystemWorkarea.executeUpdate(sql, sqlParams)
            }

            temp = GeneralTransaction.executeQuery('select max(id) from GeneralTransaction')
        } finally {
            lock.unlock()
        }

        def maxTransactionId = temp[0] ?: 0L
		
		// If requested, remove any records with opening and closing balances of zero
		// and which do not have any transactions in the relevant period
		if (params.omitZero) {
			yield()
			sql = 'delete from SystemWorkarea as gb where gb.process = ?' +
				' and gb.decimal1 = 0.0 and gb.decimal2 = 0.0' +
				' and not exists (select gt.id from GeneralTransaction as gt where gt.balance.id = gb.identifier and gt.id <= ?)'
			SystemWorkarea.executeUpdate(sql, [pid, maxTransactionId])
		}
		
        yield()
        def reportParams = [:]
        reportParams.put('reportTitle', title)
        reportParams.put('pid', pid)
        reportParams.put('periodId', pd.id)
        reportParams.put('mainClause', mainClause)
        reportParams.put('subClause', subClause)
        reportParams.put('maxTransactionId', maxTransactionId)
        reportParams.put('colDebit', message(code: 'generic.debit', default: 'Debit'))
        reportParams.put('colCredit', message(code: 'generic.credit', default: 'Credit'))
        reportParams.put('colTransactions', message(code: 'report.postings.colTransactions', default: 'Transactions'))
        reportParams.put('colBalances', message(code: 'report.postings.colBalances', default: 'Balances'))
        reportParams.put('colDocument', message(code: 'report.postings.colDocument', default: 'Document'))
        reportParams.put('colDate', message(code: 'report.postings.colDate', default: 'Date'))
        reportParams.put('txtOpening', message(code: 'report.postings.txtOpening', default: 'Opening Balance b/f'))
        reportParams.put('txtClosing', message(code: 'report.postings.txtClosing', default: 'Closing Balance c/f'))
        reportParams.put('txtError', message(code: 'report.postings.txtError', default: '<<< Error >>>'))
        reportParams.put('pdStatusPrompt', message(code: 'report.postings.status', default: 'Period Status'))
        reportParams.put('pdStatus', message(code: 'period.status.' + pd.status, default: pd.status))
		reportParams.put('txtOmitZero', message(code: 'report.omitZero.label', default: 'Omit Zero Values'))
		reportParams.put('valOmitZero', params.omitZero)

        if (params.scope == 'split') {
            reportParams.put('mainClause', 'and section_type = \'ie\'') // Jasper doesn't like GStrings in its parameters map
            title = message(code: 'report.postings.ier', args: [pd.code], default: 'Detailed Income & Expenditure Postings for ' + pd.code)
            reportParams.put('reportTitle', title)
            doReport(reportParams)
            reportParams.put('mainClause', 'and section_type = \'bs\'')
            title = message(code: 'report.postings.bsr', args: [pd.code], default: 'Detailed Balance Sheet Postings for ' + pd.code)
            reportParams.put('reportTitle', title)
            doReport(reportParams)
        } else if (params.scope == 'separate') {
            for (id in sectionIdList) {
                reportParams.put('mainClause', 'and id = ' + id.toString())
                temp = ChartSection.get(id)
                title = message(code: 'report.postings.sectionr', args: [pd.code, temp.code], default: 'Detailed Postings for ' + pd.code + ', Section ' + temp.code)
                reportParams.put('reportTitle', title)
                doReport(reportParams)
            }
        } else {
            doReport(reportParams)
        }

        SystemWorkarea.withTransaction {status ->
            SystemWorkarea.executeUpdate('delete from SystemWorkarea where process = ?', [pid])
        }

        yield()

        return true
    }

    private doReport(reportParams) {
        def pdfFile = createReportPDF('DetailedPostings', reportParams)
        yield()

        mailService.sendMail {
			multipart true
            to user.email
            subject reportParams.reportTitle
            body(view: '/emails/genericReport', model: [companyInstance: company, systemUserInstance: user, title: reportParams.reportTitle])
            attach pdfFile
        }

        pdfFile.delete()
        yield()
    }
}
