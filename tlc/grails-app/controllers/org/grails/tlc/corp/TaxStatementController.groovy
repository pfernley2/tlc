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

import org.grails.tlc.books.Document
import org.grails.tlc.books.DocumentType
import doc.Line

class TaxStatementController {

    // Injected services
    def utilService
    def bookService
    def postingService

    // Security settings
    def activities = [default: 'taxstmt']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', process: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        if (!['statementDate', 'description'].contains(params.sort)) {
            params.sort = 'statementDate'
            params.order = 'desc'
        }

        [taxStatementInstanceList: TaxStatement.selectList(securityCode: utilService.currentCompany().securityCode), taxStatementInstanceTotal: TaxStatement.selectCount()]
    }

    def show() {
        def taxStatementInstance = TaxStatement.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!taxStatementInstance) {
            flash.message = utilService.standardMessage('not.found', 'taxStatement', params.id)
            redirect(action: 'list')
        } else {
            return [taxStatementInstance: taxStatementInstance]
        }
    }

    def delete() {
        [taxStatementInstance: TaxStatement.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)]
    }

    def process() {
        def taxStatementInstance = TaxStatement.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (taxStatementInstance && !taxStatementInstance.finalized) {
            def result = utilService.demandRunFromParams('taxDelete', [p_stringId: taxStatementInstance.id.toString(), preferredStart: params.preferredStart])
            if (result instanceof String) {
                flash.message = result
                redirect(action: 'delete', id: taxStatementInstance.id)
                return
            }

            flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
        } else {
            flash.message = utilService.standardMessage('not.found', 'taxStatement', params.id)
        }

        redirect(action: 'list')
    }

    def edit() {
        def taxStatementInstance = TaxStatement.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!taxStatementInstance) {
            flash.message = utilService.standardMessage('not.found', 'taxStatement', params.id)
            redirect(action: 'list')
        } else {
            return getParameters(taxStatementInstance, new Document(), new Line())
        }
    }

    def update(Long version) {
        def company = utilService.currentCompany()
        def companyCurrency = utilService.companyCurrency()
        def account, temp, taxControl
        def accountCurrency = companyCurrency
        def accountRate = 1.0
        def companyTaxValue = 0.0
        def valid = true
        def taxStatementInstance = TaxStatement.findByIdAndSecurityCode(params.id, company.securityCode, [lock: true])
        if (taxStatementInstance) {
            def documentInstance = new Document()
            def documentLine = new Line()
            if (!params.code && params.sourceNumber) params.code = params.sourceNumber  // A disabled field would not be in the params, so we keep a copy in a hidden field

            // Get the total tax value
            for (it in TaxStatementLine.findAllByStatement(taxStatementInstance)) companyTaxValue += it.companyTaxValue

            // If there is a tax value to be paid/refunded, we need to create and post a journal
            if (companyTaxValue) {

                // Get the line data out of the params map otherwise Grails will try and load it in to a lines array, but there are two line arrays: tax statement and document
                documentLine.accountCode = params.remove('lines[0].accountCode')
                documentLine.accountName = params.remove('lines[0].accountName')
                params.remove('lines[0]')

                // Load up the other two domains
                documentInstance.properties['type', 'code', 'period', 'reference'] = params
                taxStatementInstance.description = params.description

                if (taxStatementInstance.finalized || (version != null && taxStatementInstance.version > version)) {
                    taxStatementInstance.errorMessage(code: 'locking.failure', domain: 'taxStatement')
                    valid = false
                }

                // Do the basic checks
                if (valid) valid = (!taxStatementInstance.hasErrors() && taxStatementInstance.validate() && !documentInstance.hasErrors() && !documentLine.hasErrors())

                // Verify references are for the correct company
                if (valid) {
                    utilService.verify(documentInstance, ['type', 'period'])
                    if (documentInstance.type == null || documentInstance.type.type.code != 'GLJ') {
                        taxStatementInstance.errorMessage(code: 'document.bad.type', default: 'Invalid document type')
                        documentInstance.errors.rejectValue('type', null)
                        valid = false
                    }

                    if (documentInstance.period == null || documentInstance.period.status != 'open') {
                        taxStatementInstance.errorMessage(code: 'document.bad.period', default: 'Invalid document period')
                        documentInstance.errors.rejectValue('period', null)
                        valid = false
                    }
                }

                // Check for missing/invalid document data
                if (valid) {
                    if (!documentInstance.code || documentInstance.code.length() > 10) {
                        documentInstance.errors.rejectValue('code', null)
                        valid = false
                    } else if (documentInstance.reference?.length() > 30) {
                        documentInstance.errors.rejectValue('reference', null)
                        valid = false
                    }

                    if (!valid) taxStatementInstance.errorMessage(code: 'document.invalid', default: 'Invalid document')
                }

                // Check out the account they want to post to and get any exchange rate required
                if (valid) {
                    if (documentLine.accountCode) {

                        // Make sure the GL account code is expanded for mnemonics and case is correct
                        temp = bookService.expandAccountCode(utilService.currentUser(), documentLine.accountCode)
                        if (!temp) {
                            taxStatementInstance.errorMessage(code: 'account.not.exists', default: 'Invalid GL account')
                            documentLine.errors.rejectValue('accountCode', null)
                            valid = false
                        } else {
                            documentLine.accountCode = temp

                            // See if the GL account actually exists
                            account = bookService.getAccount(company, documentLine.accountCode)
                            if (account instanceof String) {
                                taxStatementInstance.errorMessage(code: null, default: account)
                                documentLine.errors.rejectValue('accountCode', null)
                                valid = false
                            } else {

                                // Make sure the GL account is active and that the user is allowed to access this account and
                                // that the account is not restricted as to what sort of documents can be posted to it
                                if (account?.active && bookService.hasAccountAccess(account)) {
                                    if (postingService.canPostDocumentToAccount(documentInstance, documentLine, 0, account)) {

                                        // Get any exchange rate we may need
                                        if (account.currency.id != companyCurrency.id) {
                                            accountCurrency = account.currency
                                            accountRate = utilService.getExchangeRate(companyCurrency, accountCurrency)
                                            if (!accountRate) {
                                                taxStatementInstance.errorMessage(code: 'document.bad.exchangeRate', args: [companyCurrency.code, accountCurrency.code],
                                                        default: "No exchange rate available from ${companyCurrency.code} to ${accountCurrency.code}")
                                                valid = false
                                            }
                                        }
                                    } else {
                                        taxStatementInstance.errorMessage(code: 'account.not.exists', default: 'Invalid GL account')
                                        documentLine.errors.rejectValue('accountCode', null)
                                        valid = false
                                    }
                                } else {
                                    taxStatementInstance.errorMessage(code: 'account.not.exists', default: 'Invalid GL account')
                                    documentLine.errors.rejectValue('accountCode', null)
                                    valid = false
                                }
                            }
                        }
                    } else {
                        taxStatementInstance.errorMessage(code: 'account.not.exists', default: 'Invalid GL account')
                        documentLine.errors.rejectValue('accountCode', null)
                        valid = false
                    }
                }

                // Make sure we can find the tax control account
                if (valid) {
                    taxControl = bookService.getControlAccount(company, 'tax')
                    if (!taxControl) {
                        taxStatementInstance.errorMessage(code: 'taxStatement.no.control', default: 'No tax control account is defined within the General Ledger')
                        valid = false
                    }
                }

                // Create the document and post it
                if (valid) {
                    // Complete the document header
                    documentInstance.currency = companyCurrency
                    documentInstance.description = taxStatementInstance.description
                    documentInstance.documentDate = utilService.fixDate()

                    // Create the document target account line
                    def line = new Line()
                    line.account = account
                    line.description = taxStatementInstance.description
                    line.documentValue = companyTaxValue
                    line.generalValue = utilService.round(companyTaxValue * accountRate, accountCurrency.decimals)
                    line.companyValue = companyTaxValue
                    documentInstance.addToLines(line)

                    // Create the document tax control account line
                    line = new Line()
                    line.account = taxControl
                    line.description = taxStatementInstance.description
                    line.documentValue = -companyTaxValue
                    line.generalValue = -companyTaxValue
                    line.companyValue = -companyTaxValue
                    documentInstance.addToLines(line)

                    TaxStatement.withTransaction {status ->
                        valid = postingService.post(documentInstance, status)
                        if (valid) {

                            // Need to modify the tax control account line in the document to set its
                            // reconciliation details. We couldn't do this on the insert since the
                            // domain validation criteria would have altered the details itself.
                            line.reconciled = taxStatementInstance.statementDate
                            line.reconciliationKey = 'T' + taxStatementInstance.authority.id.toString()
                            valid = line.saveThis()
                            if (valid) {
                                taxStatementInstance.document = documentInstance
                                taxStatementInstance.finalized = true
                                valid = taxStatementInstance.saveThis()
                                if (!valid) {
                                    taxStatementInstance.document = null
                                    taxStatementInstance.finalized = false
                                    status.setRollbackOnly()
                                }
                            } else {
                                taxStatementInstance.errorMessage(code: 'taxStatement.bad.update', default: 'Unable to update the reconcilition details of the journal line')
                                status.setRollbackOnly()
                            }
                        } else {
                            taxStatementInstance.errorMessage(code: 'taxStatement.bad.posting', default: 'Unable to post the journal')
                            status.setRollbackOnly()
                        }
                    }
                }
            } else {                // If there is no value to be paid/refunded, we just need to set the finalized flag
                taxStatementInstance.finalized = true
                if (!taxStatementInstance.saveThis()) {
                    taxStatementInstance.finalized = false
                    valid = false
                }
            }

            if (valid) {
                flash.message = utilService.standardMessage('updated', taxStatementInstance)
                redirect(action: 'show', id: taxStatementInstance.id)
            } else {
                render(view: 'edit', model: getParameters(taxStatementInstance, documentInstance, documentLine))
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'taxStatement', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def result = 0L
        def taxStatementInstance = new TaxStatement()
        def taxAccount = bookService.getControlAccount(utilService.currentCompany(), 'tax')
        def authorities = TaxAuthority.findAllByCompany(utilService.currentCompany(), [sort: 'name'])
        def authorityList = []
        for (authority in authorities) {
            def priorStatements = TaxStatement.findAllByAuthority(authority, [sort: 'statementDate', order: 'desc', max: 1])
            if (!priorStatements || priorStatements[0].finalized) authorityList << authority
        }

        return [taxStatementInstance: taxStatementInstance, taxAccount: taxAccount, authorityList: authorityList, queueNumber: result]
    }

    def save() {
        def result = 0L
        def taxStatementInstance = new TaxStatement()
        def taxAccount = bookService.getControlAccount(utilService.currentCompany(), 'tax')
        def authorities = TaxAuthority.findAllByCompany(utilService.currentCompany(), [sort: 'name'])
        def authorityList = []
        for (authority in authorities) {
            def priorStatements = TaxStatement.findAllByAuthority(authority, [sort: 'statementDate', order: 'desc', max: 1])
            if (!priorStatements || priorStatements[0].finalized) authorityList << authority
        }

        taxStatementInstance.properties['statementDate', 'description', 'authority'] = params
        def valid = !taxStatementInstance.hasErrors()
        if (valid) {
            utilService.verify(taxStatementInstance, ['authority'])             // Ensure correct references
            if (!taxStatementInstance.authority) {
                taxStatementInstance.errorMessage(field: 'authority', code: 'taxStatement.bad.authority', default: 'Invalid tax authority')
                valid = false
            }
        }

        if (valid) {
            def today = utilService.fixDate()
            if (!taxStatementInstance.statementDate ||
                    taxStatementInstance.statementDate != utilService.fixDate(taxStatementInstance.statementDate) ||
                    taxStatementInstance.statementDate < today - 365 ||
                    taxStatementInstance.statementDate > today) {
                taxStatementInstance.errorMessage(field: 'statementDate', code: 'taxStatement.statementDate.bad', default: 'Invalid statement date')
                valid = false
            }
        }

        if (valid) {
            def priorStatements = TaxStatement.findAllByAuthority(taxStatementInstance.authority, [sort: 'statementDate', order: 'desc', max: 1])
            if (priorStatements) {
                if (!priorStatements[0].finalized) {
                    taxStatementInstance.errorMessage(field: 'authority', code: 'taxStatement.bad.previous',
                            default: 'You cannot create a new tax statement until the previous one has been finalized')
                    valid = false
                } else if (taxStatementInstance.statementDate <= priorStatements[0].statementDate) {
                    taxStatementInstance.errorMessage(field: 'statementDate', code: 'taxStatement.previous.date', default: 'The statement date must be after the date of the preceding statement')
                    valid = false
                }
            }
        }

        if (valid) {
            params.p_authority = taxStatementInstance.authority.id.toString()
            params.p_date = params.statementDate
            params.p_describe = taxStatementInstance.description
            result = utilService.demandRunFromParams('taxStmt', params)
            if (result instanceof String) {
                flash.message = result
                result = 0L
            }
        }

        render(view: 'create', model: [taxStatementInstance: taxStatementInstance, taxAccount: taxAccount, authorityList: authorityList, queueNumber: result])
    }

    def print() {
        def taxStatementInstance = TaxStatement.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!taxStatementInstance) {
            flash.message = utilService.standardMessage('not.found', 'taxStatement', params.id)
            redirect(action: 'list')
        } else {
            params.p_stringId = taxStatementInstance.id.toString()
            def result = utilService.demandRunFromParams('taxReport', params)
            if (result instanceof String) {
                flash.message = result
            } else {
                flash.message = message(code: 'queuedTask.demand.good', args: [result], default: "The task has been placed in the queue for execution as task number ${result}")
            }

            redirect(action: 'edit', id: taxStatementInstance.id)
        }
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private getParameters(taxStatementInstance, documentInstance, documentLine) {
        def taxStatementLineList = TaxStatementLine.findAll('from TaxStatementLine as x where x.statement = ? order by x.currentStatement, x.expenditure, x.taxCode.code, x.taxPercentage',
                [taxStatementInstance])
        def settings = [:]
        settings.decimals = utilService.companyCurrency().decimals
        settings.hasPriorOutput = false
        settings.hasPriorInput = false
        settings.hasCurrentOutut = false
        settings.hasCurrentInput = false
        settings.priorOutputGoods = 0.0
        settings.priorOutputTax = 0.0
        settings.priorInputGoods = 0.0
        settings.priorInputTax = 0.0
        settings.currentOutputGoods = 0.0
        settings.currentOutputTax = 0.0
        settings.currentInputGoods = 0.0
        settings.currentInputTax = 0.0
        for (line in taxStatementLineList) {
            if (line.currentStatement) {
                if (line.expenditure) {
                    settings.currentInputGoods += line.companyGoodsValue
                    settings.currentInputTax += line.companyTaxValue
                    settings.hasCurrentInput = (settings.currentInputGoods || settings.currentInputTax)
                } else {
                    settings.currentOutputGoods += line.companyGoodsValue
                    settings.currentOutputTax += line.companyTaxValue
                    settings.hasCurrentOutput = (settings.currentOutputGoods || settings.currentOutputTax)
                }
            } else {
                if (line.expenditure) {
                    settings.priorInputGoods += line.companyGoodsValue
                    settings.priorInputTax += line.companyTaxValue
                    settings.hasPriorInput = (settings.priorInputGoods || settings.priorInputTax)
                } else {
                    settings.priorOutputGoods += line.companyGoodsValue
                    settings.priorOutputTax += line.companyTaxValue
                    settings.hasPriorOutput = (settings.priorOutputGoods || settings.priorOutputTax)
                }
            }
        }

        settings.totalInputTax = settings.priorInputTax + settings.currentInputTax
        settings.totalInputGoods = settings.priorInputGoods + settings.currentInputGoods
        settings.totalOutputTax = settings.priorOutputTax + settings.currentOutputTax
        settings.totalOutputGoods = settings.priorOutputGoods + settings.currentOutputGoods
        settings.totalPriorTax = settings.priorOutputTax + settings.priorInputTax
        settings.totalCurrentTax = settings.currentOutputTax + settings.currentInputTax
        settings.totalTax = settings.totalPriorTax + settings.totalCurrentTax
        settings.codeGenerate = documentInstance.type?.autoGenerate
        settings.codeEdit = documentInstance.type?.allowEdit
        def documentTypeList = DocumentType.findAll("from DocumentType as dt where dt.company = ? and dt.type.code = 'GLJ'", [utilService.currentCompany()])
        def periodList = bookService.getOpenPeriods(utilService.currentCompany())
        if (documentInstance.period) documentInstance.period = bookService.selectPeriod(periodList)
        periodList = periodList.reverse()

        return [taxStatementInstance: taxStatementInstance, taxStatementLineList: taxStatementLineList, documentInstance: documentInstance,
                documentLine: documentLine, documentTypeList: documentTypeList, periodList: periodList, settings: settings]
    }
}