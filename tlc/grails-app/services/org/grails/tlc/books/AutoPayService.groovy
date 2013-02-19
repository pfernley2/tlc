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

import it.sauronsoftware.cron4j.Predictor

class AutoPayService {

    static transactional = false

    def sessionFactory

    def createRemittanceAdvice(supplierId, isAutoRun, today) {
        def session = sessionFactory.currentSession
        def created = false
        def paymentPending = false
        def errorMessage
        def supplier, lastDate, advice, advices, transactions, line, doc
        Supplier.withTransaction {status ->
            supplier = Supplier.lock(supplierId)
            if (supplier) {
                if (supplier.schedule) {
                    advices = Remittance.findAllBySupplier(supplier, [sort: 'adviceDate', order: 'desc', max: 1, lock: true])
                    if (advices) {
                        advice = advices[0]
                        if (advice.authorizedDate) {
                            if (advice.paymentDate) {
                                lastDate = advice.adviceDate
                            } else {
                                paymentPending = true
                            }
                        } else {
                            advice.delete(flush: true)
                        }

                        session.evict(advice)
                        advice = null
                    }

                    if (!paymentPending) {
                        if (!lastDate || lastDate < today) {
                            if (supplier.active) {
                                transactions = GeneralTransaction.findAll('from GeneralTransaction as x where x.supplier = ? and x.accountValue != 0.0 and x.onHold = ? and x.document.dueDate <= ? and (x.reconciled is null or x.accountUnallocated != 0.0)', [supplier, false, today])
                                if (transactions) {

                                    // Check to see if we owe them anything
                                    def total = 0.0
                                    for (tran in transactions) total += tran.accountUnallocated

                                    // The GL transactions are typically credit items (i.e. negative)
                                    if (total < 0.0) {
                                        advice = new Remittance(supplier: supplier, adviceDate: today)
                                        if (advice.saveThis()) {
                                            for (tran in transactions) {
                                                tran.reconciled = today
                                                if (tran.saveThis()) {
                                                    if (tran.accountUnallocated) {
                                                        doc = tran.document
                                                        line = new RemittanceLine(type: doc.type.code, code: doc.code, documentDate: doc.documentDate, dueDate: doc.dueDate,
                                                                reference: doc.reference, originalValue: tran.accountValue, accountUnallocated: tran.accountUnallocated, sequencer: tran.id)
                                                        advice.addToLines(line)
                                                    }
                                                } else {
                                                    errorMessage = message(code: 'supplier.bad.tran', args: [supplier.code],
                                                            default: "Unable to update the GL transaction for supplier ${supplier.code}")
                                                    status.setRollbackOnly()
                                                    break
                                                }
                                            }

                                            if (!errorMessage) {
                                                if (advice.save(flush: true)) {     // With deep validation
                                                    created = true
                                                } else {
                                                    errorMessage = message(code: 'supplier.bad.lines', args: [supplier.code],
                                                            default: "Unable to save the remittance advice lines for supplier ${supplier.code}")
                                                    status.setRollbackOnly()
                                                }
                                            }
                                        } else {
                                            errorMessage = message(code: 'supplier.bad.advice', args: [supplier.code],
                                                    default: "Unable to save the remittance advice header for supplier ${supplier.code}")
                                            status.setRollbackOnly()
                                        }
                                    }
                                }
                            } else {
                                errorMessage = message(code: 'supplier.not.active', args: [supplier.code], default: "Supplier ${supplier.code} is inactive")
                                status.setRollbackOnly()
                            }

                            // Update the next auto payment date if applicable
                            if (!errorMessage && supplier.schedule) {
                                supplier.nextAutoPaymentDate = new Predictor(supplier.schedule.pattern).nextMatchingDate()
                                if (!supplier.saveThis()) {
                                    errorMessage = message(code: 'supplier.bad.modify', args: [supplier.code],
                                            default: "Unable to update the next auto-payment date for supplier ${supplier.code}")
                                    status.setRollbackOnly()
                                }
                            }

                            if (!errorMessage) {
                                if (transactions) for (tran in transactions) session.evict(tran)
                                if (advice) session.evict(advice)
                            }
                        } else {
                            if (!isAutoRun) errorMessage = message(code: 'supplier.duplicate.advice', args: [supplier.code],
                                    default: "Duplicate of authorized remittance advice for supplier ${supplier.code}")
                            status.setRollbackOnly()
                        }
                    } else {
                        if (!isAutoRun) errorMessage = message(code: 'supplier.pending.payment', args: [supplier.code],
                                default: "An authorized remittance is awaiting payment and so no new remittance can be created for supplier ${supplier.code}")
                        status.setRollbackOnly()
                    }

                    if (isAutoRun && !errorMessage) session.evict(supplier)
                } else {
                    if (!isAutoRun) errorMessage = message(code: 'supplier.not.auto', args: [supplier.code], default: "Supplier ${supplier.code} is not an auto-pay supplier")
                }
            } else {
                if (!isAutoRun) errorMessage = message(code: 'supplier.not.found', args: [supplierId], default: "Supplier not found with id ${supplierId}")
            }
        }

        return errorMessage ?: created
    }
}
