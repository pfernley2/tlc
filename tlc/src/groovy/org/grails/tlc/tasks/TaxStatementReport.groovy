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

import org.grails.tlc.corp.TaxStatement
import org.grails.tlc.sys.TaskExecutable

public class TaxStatementReport extends TaskExecutable {

    def execute() {
        def statement = TaxStatement.get(params.stringId)
        if (statement?.securityCode != company.securityCode) {
            completionMessage = utilService.standardMessage('not.found', 'taxStatement', params.stringId)
            return false
        }

        def taxCodeNameMap = [:]
        for (tc in statement.authority.taxCodes) {
            taxCodeNameMap.put(tc.code, message(code: 'taxCode.name.' + tc.code, default: tc.name))
        }

        def reportParams = [:]
        def title = message(code: 'taxStatement.title', args: [utilService.format(statement.statementDate, 1, null, locale), statement.authority.name],
            default: 'Tax Statement on ' + utilService.format(statement.statementDate, 1, null, locale) + ' for ' + statement.authority.name)
        reportParams.put('reportTitle', title)
        reportParams.put('statementId', statement.id)
        reportParams.put('colCode', message(code: 'taxStatementLine.taxCode.label', default: 'Tax Code'))
        reportParams.put('colRate', message(code: 'taxStatementLine.taxPercentage.label', default: 'Tax Rate'))
        reportParams.put('colGoods', message(code: 'taxStatementLine.companyGoodsValue.label', default: 'Goods Value'))
        reportParams.put('colTax', message(code: 'taxStatementLine.companyTaxValue.label', default: 'Tax Value'))
        reportParams.put('currentPd', message(code: 'taxStatementLine.currentStatement.label', default: 'Current'))
        reportParams.put('priorPd', message(code: 'taxStatementLine.priorStatement', default: 'Prior'))
        reportParams.put('inputs', message(code: 'taxStatementLine.inputTax', default: 'Input Tax'))
        reportParams.put('outputs', message(code: 'taxStatementLine.outputTax', default: 'Output Tax'))
        reportParams.put('payable', message(code: 'taxStatement.totalPayable', default: 'Total Payable'))
        reportParams.put('refund', message(code: 'taxStatement.totalRefund', default: 'Total Refund'))
        reportParams.put('summary', message(code: 'generic.summary', default: 'Summary'))
        reportParams.put('taxCodeNameMap', taxCodeNameMap)
        yield()
        def pdfFile = createReportPDF('TaxStatement', reportParams)
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