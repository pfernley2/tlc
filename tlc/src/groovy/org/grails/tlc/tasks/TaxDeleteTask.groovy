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

import org.grails.tlc.books.GeneralTransaction
import org.grails.tlc.corp.TaxStatement
import org.grails.tlc.sys.TaskExecutable

class TaxDeleteTask extends TaskExecutable {

    def execute() {

        // Make sure the tax statement is ok
        def taxStatement = TaxStatement.get(params.stringId)
        if (!taxStatement || taxStatement.securityCode != company.securityCode || taxStatement.finalized) {
			completionMessage = utilService.standardMessage('not.found', 'taxStatement', params.stringId)
            return false
        }

        def reconciliationKey = 'T' + taxStatement.authority.id.toString()
        def sql = 'update GeneralTransaction as gt set gt.reconciled = null, gt.version = gt.version + 1, gt.lastUpdated = ? where gt.reconciliationKey = ? and gt.reconciled = ?'
        def parameters = [new Date(), reconciliationKey, taxStatement.statementDate]
        GeneralTransaction.withTransaction {status ->
            GeneralTransaction.executeUpdate(sql, parameters)
            taxStatement.delete(flush: true)
        }

        return true
    }
}
