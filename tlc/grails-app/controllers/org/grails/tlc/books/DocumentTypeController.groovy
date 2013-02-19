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

class DocumentTypeController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'coadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'name', 'nextSequenceNumber', 'autoGenerate', 'allowEdit'].contains(params.sort) ? params.sort : 'code'
        [documentTypeInstanceList: DocumentType.selectList(company: utilService.currentCompany()), documentTypeInstanceTotal: DocumentType.selectCount()]
    }

    def show() {
        def documentTypeInstance = DocumentType.findByIdAndCompany(params.id, utilService.currentCompany())
        if (!documentTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'documentType', params.id)
            redirect(action: 'list')
        } else {
            return [documentTypeInstance: documentTypeInstance]
        }
    }

    def delete() {
        def documentTypeInstance = DocumentType.findByIdAndCompany(params.id, utilService.currentCompany())
        if (documentTypeInstance) {
            try {
                documentTypeInstance.delete(flush: true)
                flash.message = utilService.standardMessage('deleted', documentTypeInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', documentTypeInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'documentType', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def documentTypeInstance = DocumentType.findByIdAndCompany(params.id, utilService.currentCompany())
        if (!documentTypeInstance) {
            flash.message = utilService.standardMessage('not.found', 'documentType', params.id)
            redirect(action: 'list')
        } else {
            def bankAccountList
            if (documentTypeInstance.type.code == 'BP') {
                bankAccountList = Account.findAll("from Account as x where x.securityCode = ? and x.active = ? and x.type.code = 'bank' order by x.name",
                        [utilService.currentCompany().securityCode, true])
            }

            return [documentTypeInstance: documentTypeInstance, bankAccountList: bankAccountList]
        }
    }

    def update(Long version) {
        def documentTypeInstance = DocumentType.findByIdAndCompany(params.id, utilService.currentCompany())
        if (documentTypeInstance) {
            def bankAccountList
            if (documentTypeInstance.type.code == 'BP') {
                bankAccountList = Account.findAll("from Account as x where x.securityCode = ? and x.active = ? and x.type.code = 'bank' order by x.name",
                        [utilService.currentCompany().securityCode, true])
            }

            if (version != null && documentTypeInstance.version > version) {
                documentTypeInstance.errorMessage(code: 'locking.failure', domain: 'documentType')
                render(view: 'edit', model: [documentTypeInstance: documentTypeInstance, bankAccountList: bankAccountList])
                return
            }

            def oldBank = documentTypeInstance.autoBankAccount
            def oldFlag = documentTypeInstance.autoForeignCurrency
            def oldBankDetails = documentTypeInstance.autoBankDetails
            documentTypeInstance.properties['code', 'name', 'nextSequenceNumber', 'autoGenerate', 'allowEdit', 'autoBankAccount', 'autoForeignCurrency', 'autoMaxPayees', 'autoBankDetails'] = params
            utilService.verify(documentTypeInstance, ['autoBankAccount'])             // Ensure correct references
            def valid = (!documentTypeInstance.hasErrors() && documentTypeInstance.validate())

            // If it's no longer an auto-payment type
            if (valid && oldBank && !documentTypeInstance.autoBankAccount && Supplier.countByDocumentType(documentTypeInstance)) {
                documentTypeInstance.errorMessage(field: 'autoBankAccount', code: 'documentType.auto.suppliers',
                        default: 'You cannot remove the auto-payment specification when their are suppliers that use this document type for their auto-payments')
                valid = false
            }

            // If they've changed the auto-payment currencies allowed
            if (valid && oldBank && documentTypeInstance.autoBankAccount && !documentTypeInstance.autoForeignCurrency &&
                    (oldFlag || oldBank.currency.id != documentTypeInstance.autoBankAccount.currency.id) &&
                    Supplier.countByDocumentTypeAndCurrencyNotEqual(documentTypeInstance, documentTypeInstance.autoBankAccount.currency)) {
                if (oldFlag) {
                    documentTypeInstance.errorMessage(field: 'autoForeignCurrency', code: 'documentType.auto.flag',
                            default: 'The change of the foreign currency flag would invalidate suppliers using this document type for their auto-payments')
                } else {
                    documentTypeInstance.errorMessage(field: 'autoBankAccount', code: 'documentType.auto.bank',
                            default: 'The change of bank currency would invalidate suppliers using this document type for their auto-payments')
                }

                valid = false
            }

            // If they have changed to requiring bank details
            if (valid && !oldBankDetails && documentTypeInstance.autoBankDetails) {
                def count = Supplier.executeQuery('select count(*) from Supplier where documentType = ? and (bankSortCode is null or bankAccountName is null or bankAccountNumber is null)', [documentTypeInstance])[0]
                if (count) {
                    documentTypeInstance.errorMessage(field: 'autoBankDetails', code: 'documentType.details.flag',
                            default: 'The change to requiring bank details would invalidate suppliers using this document type for their auto-payments')
                    valid = false
                }
            }

            if (valid && documentTypeInstance.saveThis()) {
                flash.message = utilService.standardMessage('updated', documentTypeInstance)
                redirect(action: 'show', id: documentTypeInstance.id)
            } else {
                render(view: 'edit', model: [documentTypeInstance: documentTypeInstance, bankAccountList: bankAccountList])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'documentType', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def documentTypeInstance = new DocumentType()
        documentTypeInstance.company = utilService.currentCompany()   // Ensure correct company
        def bankAccountList = Account.findAll("from Account as x where x.securityCode = ? and x.active = ? and x.type.code = 'bank' order by x.name",
                [utilService.currentCompany().securityCode, true])
        return [documentTypeInstance: documentTypeInstance, bankAccountList: bankAccountList]
    }

    def save() {
        def documentTypeInstance = new DocumentType()
        documentTypeInstance.properties['code', 'name', 'nextSequenceNumber', 'type', 'autoGenerate', 'allowEdit', 'autoBankAccount', 'autoForeignCurrency', 'autoMaxPayees', 'autoBankDetails'] = params
        documentTypeInstance.company = utilService.currentCompany()   // Ensure correct company
        utilService.verify(documentTypeInstance, ['autoBankAccount'])             // Ensure correct references
        if (!documentTypeInstance.hasErrors() && documentTypeInstance.saveThis()) {
            flash.message = utilService.standardMessage('created', documentTypeInstance)
            redirect(action: 'show', id: documentTypeInstance.id)
        } else {
            def bankAccountList = Account.findAll("from Account as x where x.securityCode = ? and x.active = ? and x.type.code = 'bank' order by x.name",
                    [utilService.currentCompany().securityCode, true])
            render(view: 'create', model: [documentTypeInstance: documentTypeInstance, bankAccountList: bankAccountList])
        }
    }
}