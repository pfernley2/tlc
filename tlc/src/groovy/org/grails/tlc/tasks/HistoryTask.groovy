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

import org.grails.tlc.books.Reconciliation
import org.grails.tlc.books.Remittance
import org.grails.tlc.books.Statement
import org.grails.tlc.corp.Company
import org.grails.tlc.corp.TaxStatement
import org.grails.tlc.sys.TaskExecutable

public class HistoryTask extends TaskExecutable {

    // Clean up old accounting documents such as Debtor's Statements and Remittance Advices,
    // Bank Reconciliations and Tax Statements on a company by company basis.

    def execute() {
        def session = runSessionFactory.currentSession
        def today = utilService.fixDate()
        def cal = Calendar.getInstance()
        def val, candidates
        def minStatementMonths = 1
        def defaultStatementMonths = 12
        def statementsDeleted = 0
        def minRemittanceMonths = 1
        def defaultRemittanceMonths = 12
        def remittancesDeleted = 0
        def minReconciliationMonths = 1
        def defaultReconciliationMonths = 12
        def reconciliationsDeleted = 0
        def minTaxMonths = 1
        def defaultTaxMonths = 12
        def taxDeleted = 0

        def companies = Company.findAllBySystemOnly(false)
        for (corp in companies) {

            // Do debtor's statements
            val = utilService.setting('statement.retention.months', defaultStatementMonths, corp)
            if (val < minStatementMonths) val = defaultStatementMonths
            cal.setTime(today)
            cal.add(Calendar.MONTH, -val)
            candidates = Statement.findAll('from Statement as x where x.customer.company = ? and x.statementDate < ?', [corp, cal.getTime()])
            if (candidates) {
                Statement.withTransaction {status ->
                    for (victim in candidates) {
                        victim.delete(flush: true)
                        session.evict(victim)
                    }
                }

                statementsDeleted += candidates.size()
            }

            // Do remittance advices
            val = utilService.setting('remittance.retention.months', defaultRemittanceMonths, corp)
            if (val < minRemittanceMonths) val = defaultRemittanceMonths
            cal.setTime(today)
            cal.add(Calendar.MONTH, -val)
            candidates = Remittance.findAll('from Remittance as x where x.supplier.company = ? and x.paymentDate < ?', [corp, cal.getTime()])
            if (candidates) {
                Remittance.withTransaction {status ->
                    for (victim in candidates) {
                        victim.delete(flush: true)
                        session.evict(victim)
                    }
                }

                remittancesDeleted += candidates.size()
            }

            // Do bank reconciliations
            val = utilService.setting('reconciliation.retention.months', defaultReconciliationMonths, corp)
            if (val < minReconciliationMonths) val = defaultReconciliationMonths
            cal.setTime(today)
            cal.add(Calendar.MONTH, -val)
            candidates = Reconciliation.findAll('from Reconciliation as x where x.securityCode = ? and x.statementDate < ? and x.finalizedDate is not null order by x.bankAccount.id, x.statementDate desc',
                    [corp.securityCode, cal.getTime()])
            if (candidates) {
                def lastBankId = 0L
                Reconciliation.withTransaction {status ->
                    for (victim in candidates) {

                        // Make sure we leave at least one finalized reconciliation
                        if (victim.bankAccount.id != lastBankId) {
                            lastBankId = victim.bankAccount.id
                            if (Reconciliation.executeQuery('select count(*) from Reconciliation where bankAccount = ? and statementDate >= ? and finalizedDate is not null',
                                [victim.bankAccount, cal.getTime()])[0] == 0) {
                                session.evict(victim)
                                continue
                            }
                        }

                        victim.delete(flush: true)
                        session.evict(victim)
                        reconciliationsDeleted++
                    }
                }
            }

            // Do tax statements
            val = utilService.setting('tax.statement.retention.months', defaultTaxMonths, corp)
            if (val < minTaxMonths) val = defaultTaxMonths
            cal.setTime(today)
            cal.add(Calendar.MONTH, -val)
            candidates = TaxStatement.findAll('from TaxStatement as x where x.authority.company = ? and x.statementDate < ? and x.finalized = ? order by x.authority.id, x.statementDate desc',
                    [corp, cal.getTime(), true])
            if (candidates) {
                def lastAuthorityId = 0L
                TaxStatement.withTransaction {status ->
                    for (victim in candidates) {

                        // Make sure we leave at least one finalized tax statement
                        if (victim.authority.id != lastAuthorityId) {
                            lastAuthorityId = victim.authority.id
                            if (TaxStatement.executeQuery('select count(*) from TaxStatement where authority = ? and statementDate >= ? and finalized = ?',
                                [victim.authority, cal.getTime(), true])[0] == 0) {
                                session.evict(victim)
                                continue
                            }
                        }

                        victim.delete(flush: true)
                        session.evict(victim)
                        taxDeleted++
                    }
                }
            }
        }

        results.statements = statementsDeleted
        results.remittance = remittancesDeleted
        results.bankrecs = reconciliationsDeleted
        results.taxstmts = taxDeleted

        return true
    }
}