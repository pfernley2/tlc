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
package org.grails.tlc.corp

import org.grails.tlc.tasks.TaxStatementTask

class TaxStatementLineController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'taxstmt']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.offset = utilService.offset
        def ddSource = utilService.source('taxStatement.edit')
        def transactions = TaxStatementTask.listStatementLineTransactions(ddSource, params.max, params.offset)
        def sums = TaxStatementTask.summarizeStatementLineTransactions(ddSource)
        [taxStatementInstance: ddSource.statement, taxStatementLineInstance: ddSource, transactionInstanceList: transactions,
                    transactionInstanceTotal: sums[0], goods: sums[1], tax: sums[2], decimals: utilService.companyCurrency().decimals]
    }
}