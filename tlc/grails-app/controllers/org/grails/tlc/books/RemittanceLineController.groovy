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

class RemittanceLineController {

    // Injected services
    def utilService
    def bookService

    // Security settings
    def activities = [default: 'apremit']

    // List of actions with specific request types
    static allowedMethods = [update: 'POST', save: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        //params.sort = ['code', 'sequencer', 'dataType', 'dataScale', 'defaultValue', 'required'].contains(params.sort) ? params.sort : 'code'
        params.max = utilService.max
        params.sort = ['type', 'code', 'documentDate', 'dueDate', 'reference'].contains(params.sort) ? params.sort : 'documentDate'
        def ddSource = utilService.source('remittance.list')
        if (!ddSource || ddSource.authorizedDate || !bookService.hasSupplierAccess(ddSource.supplier)) {
            flash.message = utilService.standardMessage('not.found', 'remittance', params.id)
            redirect(controller: 'remittance', action: 'list')
            return
        }

        [remittanceLineInstanceList: RemittanceLine.selectList(), remittanceLineInstanceTotal: RemittanceLine.selectCount(), ddSource: ddSource]
    }

    def update() {
        def ddSource = utilService.reSource('remittance.list')
        if (ddSource) {
            ddSource.discard()
            ddSource = Remittance.lock(ddSource.id)
        }

        def remittanceLineInstance = RemittanceLine.findByIdAndRemittance(params.id, ddSource)
        if (remittanceLineInstance && !ddSource.authorizedDate && bookService.hasSupplierAccess(ddSource.supplier)) {
            if (params.payment) {

                // The 'reference' in the following line is needed to avoid a bug in Grails 1.2.0
                remittanceLineInstance.properties['payment', 'reference'] = params
                if (remittanceLineInstance.hasErrors() || remittanceLineInstance.payment != utilService.round(remittanceLineInstance.payment, ddSource.supplier.currency.decimals)) {
                    flash.message = message(code: 'remittanceLine.bad.payment', args: [params.payment], default: "Invalid amount: ${params.payment}")
                } else {
                    def payment = -remittanceLineInstance.payment
                    def total = Remittance.executeQuery('select sum(accountUnallocated) from RemittanceLine where remittance = ?', [ddSource])[0]
                    if (total - remittanceLineInstance.accountUnallocated + payment < 0.0) {
                        if (payment) {
                            remittanceLineInstance.accountUnallocated = payment
                            if (remittanceLineInstance.saveThis()) {
                                flash.message = message(code: 'remittanceLine.changed', default: 'Allocation updated')
                            } else {
                                flash.message = message(code: 'remittanceLine.bad.save', default: 'Unable to update the allocation')
                            }
                        } else {
                            remittanceLineInstance.delete(flush: true)
                            flash.message = message(code: 'remittanceLine.deleted', default: 'Allocation deleted')
                        }
                    } else {
                        flash.message = message(code: 'remittanceLine.bad.change', default: 'The change would make the remitttance less than or equal to zero, which is not allowed')
                    }
                }
            } else {
                flash.message = message(code: 'remittanceLine.unchanged', default: 'No changes made')
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'remittance', ddSource?.id)
            redirect(controller: 'remittance', action: 'list')
            return
        }

        redirect(action: 'list', params: params)
    }

    def create() {
        def ddSource = utilService.reSource('remittance.list')
        if (!ddSource || ddSource.authorizedDate || !bookService.hasSupplierAccess(ddSource.supplier)) {
            flash.message = utilService.standardMessage('not.found', 'remittance', ddSource?.id)
            redirect(controller: 'remittance', action: 'list')
            return
        }

        def remittanceLineInstance = new RemittanceLine()
        def documentTypeList = DocumentType.findAll("from DocumentType as x where x.company = ? and x.type.supplierAllocate = ?", [utilService.currentCompany(), true])
        [remittanceLineInstance: remittanceLineInstance, documentTypeList: documentTypeList, ddSource: ddSource]
    }

    def save() {
        def valid = false
        def noRemittance = false
        def remittanceLineInstance = new RemittanceLine()
        def ddSource = utilService.reSource('remittance.list')
        RemittanceLine.withTransaction {status ->
            if (ddSource) {
                ddSource.discard()
                ddSource = Remittance.lock(ddSource.id)
            }

            if (ddSource && !ddSource.authorizedDate && bookService.hasSupplierAccess(ddSource.supplier)) {
                remittanceLineInstance.remittance = ddSource
                remittanceLineInstance.properties['code', 'payment'] = params
                if (params.targetType?.id) remittanceLineInstance.targetType = DocumentType.get(params.targetType?.id)
                valid = !remittanceLineInstance.hasErrors()
                if (valid) {
                    utilService.verify(remittanceLineInstance, ['targetType'])
                    if (!remittanceLineInstance.targetType) {
                        remittanceLineInstance.errorMessage(field: 'targetType', code: 'document.bad.type', default: 'Invalid document type')
                        valid = false
                    }
                }

                if (valid && (!remittanceLineInstance.payment || remittanceLineInstance.payment != utilService.round(remittanceLineInstance.payment, ddSource.supplier.currency.decimals))) {
                    remittanceLineInstance.errorMessage(field: 'payment', code: 'remittanceLine.bad.payment', args: [params.payment], default: "Invalid amount: ${params.payment}")
                    valid = false
                }

                def document, tran
                if (valid) {
                    document = Document.findByTypeAndCode(remittanceLineInstance.targetType, remittanceLineInstance.code)
                    if (document) tran = GeneralTransaction.findByDocumentAndSupplier(document, ddSource.supplier)
                    if (!document || !tran) {
                        remittanceLineInstance.errorMessage(field: 'code', code: 'document.invalid', default: 'Invalid document')
                        valid = false
                    }
                }

                def total = 0.0
                if (valid) {
                    for (line in ddSource.lines) {
                        if (line.code == remittanceLineInstance.code && line.type == remittanceLineInstance.targetType.code) {
                            remittanceLineInstance.errorMessage(field: 'code', code: 'remittanceLine.duplicate', default: 'This document is already included in the remittance advice')
                            valid = false
                            break
                        }

                        total += line.accountUnallocated
                    }
                }

                if (valid) {
                    if (total - remittanceLineInstance.payment < 0.0) {
                        remittanceLineInstance.type = document.type.code
                        remittanceLineInstance.code = document.code
                        remittanceLineInstance.documentDate = document.documentDate
                        remittanceLineInstance.dueDate = document.dueDate
                        remittanceLineInstance.reference = document.reference
                        remittanceLineInstance.originalValue = tran.accountValue
                        remittanceLineInstance.accountUnallocated = -remittanceLineInstance.payment
                        remittanceLineInstance.sequencer = tran.id
                        tran.reconciled = ddSource.adviceDate
                        if (tran.saveThis()) {
                            if (remittanceLineInstance.saveThis()) {
                                flash.message = message(code: 'remittanceLine.changed', default: 'Allocation updated')
                            } else {
                                remittanceLineInstance.errorMessage(code: 'remittanceLine.bad.save', default: 'Unable to update the allocation')
                                status.setRollbackOnly()
                                valid = false
                            }
                        } else {
                            remittanceLineInstance.errorMessage(code: 'supplier.bad.tran', args: [ddSource.supplier.code],
                            default: "Unable to update the GL transaction for supplier ${ddSource.supplier.code}")
                            status.setRollbackOnly()
                            valid = false
                        }
                    } else {
                        remittanceLineInstance.errorMessage(field: 'payment', code: 'remittanceLine.bad.change',
                                default: 'The change would make the remitttance less than or equal to zero, which is not allowed')
                        valid = false
                    }
                }
            } else {
                flash.message = utilService.standardMessage('not.found', 'remittance', ddSource?.id)
                noRemittance = true
            }
        }

        if (valid) {
            redirect(action: 'list', params: params)
        } else if (noRemittance) {
            redirect(controller: 'remittance', action: 'list')
        } else {
            def documentTypeList = DocumentType.findAll("from DocumentType as x where x.company = ? and x.type.supplierAllocate = ?", [utilService.currentCompany(), true])
            render(view: 'create', model: [remittanceLineInstance: remittanceLineInstance, documentTypeList: documentTypeList, ddSource: ddSource])
        }
    }
}
