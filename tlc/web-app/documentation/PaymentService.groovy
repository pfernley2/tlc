// Specimen payment service for handling actual payments (cheques, electronic transfers etc)
// to a bank or banks. You could create this service by placing it within the TLC project source
// code or by creating a plugin that defines the service and any other artefacts (including
// domains) that you require. If you include it directly within the TLC source code, be aware that
// any upgrades to TLC will need the service recreating. The standard TLC system will automatically
// find and use a service called paymentService due to the automatic wiring of Grails.

// Change the following package specification to whatever you require
package org.grails.tlc.books

// Remember that there is only one instance of a service and it can be used by more than one caller
// at the same time. Consequently you cannot store any instance level variable data, only method
// level variables. All methods therefore have a customizationMap parameter passed to them by the
// caller in which you may store 'run-level' variables.
class PaymentService {

    // We use manual transactions rather than JTA
    static transactional = false

    // Called once per run of the AutoPayProcessTask job to allow you to prepare for use. The
    // customizationMap parameter is there for you to store 'run-level' data in and initially
    // contains the following which you should not alter or remove from the map):
    //
    // Key          Value Type      Comment
    // ---          ----------      -------
    // pid          Long            A unique id for this run
    // company      Company         The company for which this run is being made
    // user         SystemUser      The user making this run
    // locale       Locale          The locale of the user making this run
    // today        Date            The start date of the run (time portion is all zeros) per the server
    //
    // The call to this method is NOT within any database transaction initiated by the caller. If
    // you wish to abandon the run, return a String giving the reason for the cancellation. Otherwise,
    // return null to allow the run to continue. Cancelling a run in no way affects the remittances
    // awaiting payment but is regarded as a failure of the run.
    def initializePaymentRun(customizationMap) {

        // return 'Internet link to our bank is down'

        return null     // Carry on with the run
    }

    // Called once for each remittance advice in the run. It is a last 'sanity check' that the
    // remittance should be processed. The parameters other than the customizationMap are as follows:
    //
    // Name         Value Type      Comment
    // ----         ----------      -------
    // supplier     Supplier        The supplier to be paid
    // type         DocumentType    The type of document they are being paid with
    // account      Account         The bank account from which they are being paid
    // currency     Currency        The currency in which they are being paid
    // amount       BigDecimal      The amount they are being paid (will be greater than zero)
    //
    // This is the point at which to check that, if needed, the supplier's bank account details
    // are valid for a payment of this type made from this bank account.
    //
    // Note that the supplier record is not 'locked for update' in the database since you are only
    // expected to be reading data at this point. If you want to allow this amount to be paid to
    // the supplier (i.e. added to the payment document currently being constructed), return null
    // from this method. If you want to stop the payment of this amount to the supplier then return
    // a String containing an explanation why the supplier is not to be paid. Stopping payment of a
    // supplier in this way leads to the corresponding remittance advice record being deleted and
    // the user making this run is notified of the fact. Note that this method is called within a
    // database transaction initiated by the caller.
    def verifyPayment(customizationMap, supplier, type, account, currency, amount) {

        // Example tests
        if (amount > 10000000.00) return 'Payment amount is excessive'
        if (type.code == 'SWIFT' && !supplier.bankSortCode) return 'Missing bank account details'
        if (type.code == 'CHQ' && currency.code != 'GBP') return 'We only write cheques in Sterling'

        return null     // Allow the payment
    }

    // Called once for each bank payment document created by the run. There may be one or more
    // payees in the document lines as defined by the DocumentType records of the company.
    // Cheque type payment documents would have just one line, payment lists (i.e. a list of
    // multiple cheque payments) and electronic transfer documents would (probably) have multiple
    // payees, one per line of the document. Note that the document has not actually been posted
    // yet and so it's id (and the ids of its lines and total) will all be zero. You can, however,
    // find the document again later by noting (the id of) it's document type and the document
    // code then using a statement such as:
    //
    //   Document.find('from Document where type.id = ? and code = ?', [documentTypeId, documentCode])
    //
    // Because the document has not been posted yet, you are at liberty to change the reference and
    // description attributes in the document itself, together with the description attributes in
    // the lines of the document and its total. If you change anything other than these attributes,
    // it will almost certainly lead to problems. Also, because of the document not having been posted
    // yet, all the values on the lines are positive AS IS THE TOTAL. After posting the lines on a
    // bank payment document would still be positive (debit) but the total of the document would be
    // negative (credit).
    //
    // This method is called within a database transaction initiated by the caller and therefore any
    // database activity by you will be rolled back if the overall transaction fails. To allow the
    // posting to continue, return null. To stop the document being posted (and thus delete all its
    // associated remittances) return a String containing the explanation of why the document is
    // being cancelled. Processing then continues with the next document, if any.
    def preProcessDocument(customizationMap, document) {

        // Example tests
        switch (document.type.code) {
            case 'CHQ':
                // Do something with a cheque payment
                break

            case 'CPL':
                // Do something with a list of cheque payments
                // such as: for (line in document.lines) etc...
                break

            case 'BACS':
                // Do something with a list of electronic transfers
                // (called 'BACS transfers' in the UK)
                break

            default:
                return 'Unknown document type'  // Stop the document being posted
        }

        return null     // Continue with posting the document
    }

    // This method is called once per document created by the run after an attempt has been made to
    // post the document. The document parameter is of type Document and is the same instance as was
    // passed earlier to your preProcessDocument method. The posted parameter is a Boolean value. If
    // true then the document has been successfully posted and will have a valid id (as will it's lines
    // and total). If false, then the posting of the document failed and the transaction will be rolled
    // back after return from this method. This method is called within a database transaction initiated
    // by the caller. If the posted parameter is true but you wish the caller to abandon this document
    // (rolling back the transaction and deleting associated remittances) then return a String containing
    // an explanation as to why this document has been cancelled. Otherwise, return null to signify that
    // all is ok. In both cases, processing will continue with the next document (if any). Note that if
    // the posted parameter is true, the total of the document will now be negative (credit) and the lines
    // will still be positive (debits). If the posted flag is false then, just as for your preProcessDocument
    // method, all values will be positive and, since the posting failed, any return code from this method
    // will be ignored.
    def postProcessDocument(customizationMap, document, posted) {

        // Example code
        if (posted) {
            // Do some processing, but if an error occurs...
            // return 'Our bank rejected the document'
        } else {
            // Clean up after a failed posting
        }

        return null     // We handled the situation ok
    }

    // Called once per run when the last payment document (if any) has been created but before the
    // remittance advices are printed. The call to this method is NOT within any database transaction
    // initiated by the caller. This method should return null if the remittance advices should be printed
    // or a String giving the reason why they should not be printed. Cancelling printing of the remittance
    // advices is not regarded as a failure of the run.
    def finalizePaymentRun(customizationMap) {

        // return 'Remittance advices sent electronically'

        return null     // Continue with printing any remittance advices
    }
}
