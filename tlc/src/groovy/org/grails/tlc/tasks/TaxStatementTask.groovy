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
import org.grails.tlc.corp.TaxAuthority
import org.grails.tlc.corp.TaxCode
import org.grails.tlc.corp.TaxStatement
import org.grails.tlc.corp.TaxStatementLine
import org.grails.tlc.sys.TaskExecutable
import java.text.MessageFormat

class TaxStatementTask extends TaskExecutable {

    // The following are the system document types that have data entry tax analysis capabilities (and can therefore post tax lines to the tax account).
    // If the system is changed or tailored to allow other system document types to create tax lines, or new system document types are added that can create
    // tax lines, then the following two arrays should be updated.
    public static final inputDocumentTypes = ['PI', 'PC', 'CP']
    public static final outputDocumentTypes = ['SI', 'SC', 'CR']

    private static final SELECTOR = 'from GeneralTransaction as x where x.reconciliationKey = ? and x.reconciled = ? and x.document.documentDate {0} ? and (x.document.type.type.code in ({1}) or (x.document.type.type.code not in ({2}) and x.companyValue {3} 0)) '
    private static final INPUT_ARGS = createInputArguments()
    private static final OUTPUT_ARGS = createOutputArguments()

    def execute() {

        // Make sure the tax authority is ok
        def taxAuthority = TaxAuthority.get(params.authority)
        if (!taxAuthority || taxAuthority.securityCode != company.securityCode) {
            completionMessage = utilService.standardMessage('not.found', 'taxAuthority', params.authority)
            return false
        }

        def reconciliationKey = 'T' + taxAuthority.id.toString()
        yield()

        // Get any previous statement and ensure it has been finalized
        def priorStatements = TaxStatement.findAllByAuthority(taxAuthority, [sort: 'statementDate', order: 'desc', max: 1])
        def priorStatementDate = utilService.EPOCH
        if (priorStatements) {
            if (!priorStatements[0].finalized) {
                completionMessage = message(code: 'taxStatement.bad.previous', default: 'You cannot create a new tax statement until the previous one has been finalized')
                return false
            }

            priorStatementDate = priorStatements[0].statementDate
        }

        yield()

        // Make sure the new statement date is ok
        def today = utilService.fixDate()
        def statementDate = params.date
        if (statementDate <= priorStatementDate || statementDate < today - 365 || statementDate > today) {
            completionMessage = message(code: 'taxStatement.statementDate.bad', default: 'Invalid statement date')
            return false
        }

        def sql = 'update GeneralTransaction as x set x.reconciled = ?, x.version = x.version + 1, x.lastUpdated = ? ' +
                'where x.reconciliationKey = ? and x.reconciled is null and exists (from Document as y where y = x.document and y.documentDate <= ?)'
        def parameters = [statementDate, new Date(), reconciliationKey, statementDate]
        def itemCount
        GeneralTransaction.withTransaction {status ->
            itemCount = GeneralTransaction.executeUpdate(sql, parameters)
        }

        yield()
        if (!itemCount) {
            completionMessage = message(code: 'taxStatement.no.items', default: 'No items found to include in the statement')
            return false
        }

        def description = params.describe ?: taxAuthority.name + ' - ' + utilService.stringOf('date', 1, statementDate)
        if (description.length() > 50) description = description.substring(0, 50)
        def statement = new TaxStatement(authority: taxAuthority, statementDate: statementDate, priorDate: priorStatementDate, description: description)
        summarize(statement, false, false)  // Prior period outputs
        yield()
        summarize(statement, false, true)   // Prior period inputs
        yield()
        summarize(statement, true, false)   // Current period outputs
        yield()
        summarize(statement, true, true)    // Current period inputs
        yield()
        if (!statement.save()) {            // With deep validation
            completionMessage = message(code: 'taxStatement.bad.save', default: 'Unable to save the new tax statement')
            return false
        }

        return true
    }

    static listStatementLineTransactions(line, max, offset) {
        if (!line) return []
        def args = [line.currentStatement ? '>' : '<=']
        args.addAll(line.expenditure ? INPUT_ARGS : OUTPUT_ARGS)
        def sql = MessageFormat.format(SELECTOR + 'and x.taxCode = ? and x.taxPercentage = ? order by x.document.documentDate desc, x.document.id desc', args as String[])

        args = ['T' + line.statement.authority.id.toString(), line.statement.statementDate, line.statement.priorDate, line.taxCode, line.taxPercentage]
        return TaxStatement.executeQuery(sql, args, [max: max, offset: offset])
    }

    static summarizeStatementLineTransactions(line) {
        if (!line) return 0
        def args = [line.currentStatement ? '>' : '<=']
        args.addAll(line.expenditure ? INPUT_ARGS : OUTPUT_ARGS)
        def sql = MessageFormat.format('select count(*), sum(x.companyTax), sum(x.companyValue) ' + SELECTOR + 'and x.taxCode = ? and x.taxPercentage = ?', args as String[])

        args = ['T' + line.statement.authority.id.toString(), line.statement.statementDate, line.statement.priorDate, line.taxCode, line.taxPercentage]
        return TaxStatement.executeQuery(sql, args)[0]
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private static createInputArguments() {
        def args = []
        args << "'" + inputDocumentTypes.join("','") + "'"
        args << "'" + outputDocumentTypes.join("','") + "'"
        args << '>='
        return args
    }

    private static createOutputArguments() {
        def args = []
        args << "'" + outputDocumentTypes.join("','") + "'"
        args << "'" + inputDocumentTypes.join("','") + "'"
        args << '<'
        return args
    }

    private summarize(statement, current, input) {
        def args = [current ? '>' : '<=']
        args.addAll(input ? INPUT_ARGS : OUTPUT_ARGS)
        def sql = MessageFormat.format('select x.taxCode.id, x.taxPercentage, sum(x.companyTax), sum(x.companyValue) ' + SELECTOR + 'group by x.taxCode.id, x.taxPercentage', args as String[])

        for (it in GeneralTransaction.executeQuery(sql, ['T' + statement.authority.id.toString(), statement.statementDate, statement.priorDate])) {
            if (it[2] != 0.0 || it[3] != 0.0) {
                statement.addToLines(new TaxStatementLine(taxCode: TaxCode.get(it[0]), currentStatement: current,
                    expenditure: input, taxPercentage: it[1], companyGoodsValue: it[2], companyTaxValue: it[3]))
            }
        }
    }
}
