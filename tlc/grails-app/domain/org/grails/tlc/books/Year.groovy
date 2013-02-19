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

import org.grails.tlc.corp.Company
import org.grails.tlc.corp.TaxAuthority
import org.grails.tlc.corp.TaxStatement
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class Year {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company]
    static hasMany = [periods: Period]

    String code
    Date validFrom
    Date validTo
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache true
        table 'accounting_year'
        columns {
            company lazy: true
            periods cascade: 'all'
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, unique: 'company')
        validFrom(validator: {val, obj ->
            if (val) {
                // Approx 5 years either side of today and must not include a time
                if (val.getTime() < System.currentTimeMillis() - 160000000000L || val.getTime() > System.currentTimeMillis() + 160000000000L || val != UtilService.fixDate(val)) return 'bad'
                if (obj.validTo && val > obj.validTo) return 'mismatch'
            }

            return true
        })
        validTo(validator: {val, obj ->
            // Approx 5 years either side of today and must not include a time
            if (val) {
                if (val.getTime() < System.currentTimeMillis() - 160000000000L || val.getTime() > System.currentTimeMillis() + 160000000000L || val != UtilService.fixDate(val)) return 'bad'
            }

            return true
        })
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.company.securityCode
            return true
        })
    }

    // Deletes all transaction data associated with the year. Should NOT be called within a database transaction since
    // we use transactions ourselves as some database with fixed rollback segments might be unable to delete the year
    // within an overall transaction, therefore, we take the risk of working without an overall transactions. NOTE that
    // this method does not delete data that will be cascade deleted when the year instance itself is deleted (e.g.
    // periods, balances etc).
    def deleteTransactions() {
        def pds = Period.findAllByYear(this)
        for (pd in pds) {

            // Delete any allocations first of all
            Allocation.withTransaction {status ->
                Allocation.executeUpdate('delete from Allocation where period = ?', [pd])
            }

            // Delete any revaluations associated with the period
            PeriodRevaluation.withTransaction {status ->
                PeriodRevaluation.executeUpdate('delete from PeriodRevaluation where period = ?', [pd])
            }

            // Now work through the balances deleting all their transactions
            def bals = GeneralBalance.findAllByPeriod(pd)
            for (bal in bals) {
                GeneralTransaction.withTransaction {status ->
                    GeneralTransaction.executeUpdate('delete from GeneralTransaction where balance = ?', [bal])
                }
            }

            // Need to get rid of tax statements that reference documents in this period
            // so that we can do a bulk delete of the documents in this period
            TaxStatement.withTransaction {status ->
                for (it in TaxStatement.executeQuery('from TaxStatement as x where x.securityCode = ? and x.document.period = ?', [this.company.securityCode, pd])) {
                    it.delete(flush: true)
                }
            }

            // Finally delete any documents associated with the period
            Document.withTransaction {status ->
                Document.executeUpdate('delete from Document where period = ?', [pd])
            }
        }

        // Need to clean up any finalized tax statements that do not have documents, that are now
        // the oldest extant tax statements and whose statement date is on or before this year end
        def authorities = TaxAuthority.findAllByCompany(this.company)
        for (authority in authorities) {
            def statements = TaxStatement.findAllByAuthorityAndFinalized(authority, true, [sort: 'statementDate'])
            for (statement in statements) {
                if (statement.document || statement.statementDate > this.validTo) break

                TaxStatement.withTransaction {status ->
                    statement.delete(flush: true)
                }
            }
        }
    }

    def afterInsert() {
        UtilService.trace('insert', this)
    }

    def afterUpdate() {
        UtilService.trace('update', this)
    }

    def afterDelete() {
        UtilService.trace('delete', this)
    }

    public String toString() {
        return code
    }
}
