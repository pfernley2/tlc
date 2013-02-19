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

import org.grails.tlc.books.GeneralBalance
import org.grails.tlc.books.Period
import org.grails.tlc.sys.TaskExecutable

public class PeriodOpenTask extends TaskExecutable {

    def execute() {
        def session = runSessionFactory.currentSession
        def valid = true
        def lock = bookService.getCompanyLock(company)
        lock.lock()
        try {
            def pd = Period.get(params.stringId)
            if (!pd || pd.securityCode != company.securityCode) {
                completionMessage = utilService.standardMessage('not.found', 'period', params.stringId)
                return false
            }

            yield()
            def pds = bookService.getActivePeriods(company)
            if (pd.status != 'new' || (pds && pd.validFrom != pds[-1].validTo + 1) || (!pds && Period.countBySecurityCodeAndValidFromLessThan(company.securityCode, pd.validFrom))) {
                completionMessage = message(code: 'period.status.invalid', default: 'Invalid change of period status')
                return false
            }

            GeneralBalance.withTransaction {status ->

                // If there are existing periods, we need to bring forward the balances
                if (pds) {
                    def newYear = (pd.year.id != pds[-1].year.id)
                    def revTotal, revBalanceId, balances, bal, balance
                    if (newYear) {
                        revBalanceId = GeneralBalance.findByAccountAndPeriod(bookService.getControlAccount(company, 'retained'), pd)?.id
                        if (revBalanceId) {
                            revTotal = GeneralBalance.executeQuery('select sum(x.companyClosingBalance) from GeneralBalance as x where x.period = ? and x.account.section.type = ?', [pds[-1], 'ie'])
                        } else {
                            completionMessage = message(code: 'period.gl.retained', default: 'Unable to locate the retained profits account in the balance sheet')
                            status.setRollbackOnly()
                            valid = false
                        }
                    } else {
                        balances = GeneralBalance.executeQuery('select id from GeneralBalance where period = ? and account.section.type = ?', [pds[-1], 'ie'])
                        for (id in balances) {
                            balance = GeneralBalance.get(id)
                            bal = GeneralBalance.findByAccountAndPeriod(balance.account, pd)
                            bal.companyOpeningBalance = balance.companyClosingBalance
                            bal.companyClosingBalance = bal.companyOpeningBalance
                            bal.generalOpeningBalance = balance.generalClosingBalance
                            bal.generalClosingBalance = bal.generalOpeningBalance
                            if (!bal.saveThis()) {
                                completionMessage = message(code: 'period.gl.balance', args: [pd.code, bal.account.code], default: "Unable to save the period ${pd.code} balance record for GL account ${account.code}")
                                status.setRollbackOnly()
                                valid = false
                                break
                            }

                            session.evict(balance)
                        }
                    }

                    if (valid) {
                        balances = GeneralBalance.executeQuery('select id from GeneralBalance where period = ? and account.section.type = ?', [pds[-1], 'bs'])
                        for (id in balances) {
                            balance = GeneralBalance.get(id)
                            bal = GeneralBalance.findByAccountAndPeriod(balance.account, pd)
                            bal.companyOpeningBalance = balance.companyClosingBalance
                            bal.generalOpeningBalance = balance.generalClosingBalance
                            if (newYear && bal.id == revBalanceId) {
                                bal.companyOpeningBalance += revTotal
                                bal.generalOpeningBalance += revTotal
                            }

                            bal.companyClosingBalance = bal.companyOpeningBalance
                            bal.generalClosingBalance = bal.generalOpeningBalance
                            if (!bal.saveThis()) {
                                completionMessage = message(code: 'period.gl.balance', args: [pd.code, bal.account.code], default: "Unable to save the period ${pd.code} balance record for GL account ${account.code}")
                                status.setRollbackOnly()
                                valid = false
                                break
                            }

                            session.evict(balance)
                        }
                    }
                }

                if (valid) {
                    pd.status = 'open'
                    if (!pd.saveThis()) {
                        completionMessage = message(code: 'period.gl.status', args: [pd.code], default: "Unable to save the change of status for period ${pd.code}")
                        status.setRollbackOnly()
                        valid = false
                    }
                }
            }
        } finally {
            lock.unlock()
        }

        return valid
    }
}