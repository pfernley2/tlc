import net.sf.json.*
import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import sun.misc.BASE64Encoder

@Grab(group='org.codehaus.groovy.modules.http-builder', module='http-builder', version='0.6')
class RestTest {

    // The type of HMAC we use
    private static final HMAC_SHA1_ALGORITHM = 'HmacSHA1'

    // The values you need to change for your particular test circumstances
    def tlcAgent = 'UL3Iu5ZBAXta0Lx56G3E'                       // The 20 character agent credentials code
    def tlcSecret = 'Jt8WWyC0Ip4ctzj8rZMJcHBu5tv-fVA01re2ZsgX'  // The 40 character shared secret for the above credentials
    def tlcURL = 'http://localhost:8080/tlc/rest/save'          // For local testing. Change this to the appropriate URL for you.

    // Some test data. There are sales invoice, sales credit note, bank receipt and inter-
    // ledger set-off journal examples. There is also a fifth example, unrelated to the other
    // four, that illustrates the less common posting of an accrual. If you are acessing a
    // company that was created by an instance of TLC running in 'demonstration mode' then
    // these examples should work. If you are using company created manually then you will
    // need to modify the examples to suit (e.g. changing the account codes to those your
    // company uses). In all cases, simply uncomment the particular example you wish to
    // execute (commenting out the others, of course).

    // EXAMPLE 1. A sales invoice as at today's date. Allows the due date to default to
    // the account setting. Notice how the monetary values are set as strings to avoid
    // JSON's use of Double values and their inherent rounding problem. If you run this
    // example first and make a note of the invoice 'number' returned, you will be able
    // to allocate the Example 2 Credit Note (below) specifically to this invoice.
    def tlcData = [header: [type: 'SI', date: new Date().format('yyyy-MM-dd'), reference: 'Order 12345', description: 'Hardware and software sale'],
            lines: [[ledger: 'gl', account: '100000-HW-CHO-000', value: '20000.0', code: 'exempt', tax: '0.0', description: 'Hardware sale'],
                    [ledger: 'gl', account: '100000-SW-CHO-000', value: '10000.0', code: 'exempt', tax: '0.0', description: 'Software sale']],
            total: [ledger: 'ar', account: 'C100200', value: '30000.0', tax: '0.0']]

    // EXAMPLE 2. A sales credit note. If you recorded the 'number' of the Example 1 Sales
    // Invoice (above) then substitute it for the '999999' allocation code below. Otherwise,
    // remove the 'allocations' entry from the total line below. Alternatively, you could
    // leave the '999999' as it is so that you can see what an error returned by the server
    // looks like (assuming that there is no SI 999999 document in account C100200).
    //def tlcData = [header: [type: 'SC', date: new Date().format('yyyy-MM-dd'), reference: 'Order 12345', description: 'Software return'],
    //        lines: [[ledger: 'gl', account: '100000-SW-CHO-000', value: '10000.0', code: 'exempt', tax: '0.0', description: 'Software return']],
    //        total: [ledger: 'ar', account: 'C100200', value: '10000.0', tax: '0.0', allocations: [[type: 'SI', code: '999999', value: '10000.0']]]]

    // EXAMPLE 3. A Bank receipt. This receipt will pay off most (but not all) of the
    // outstanding 20,000 sales invoice left after executing examples 1 and 2 above.
    // We will receive 12,500 and auto-allocate it to the customer account. If you
    // have not posted any other transactions to the customer account. This will leave
    // the invoice with 7,500 outstanding.
    //def tlcData = [header: [type: 'BR', date: new Date().format('yyyy-MM-dd'), reference: 'Receipt 54321', description: 'Receipt from customer'],
    //        lines: [[ledger: 'ar', account: 'C100200', value: '12500.0', description: 'Partial settlement', auto: true]],
    //        total: [ledger: 'gl', account: '640000', value: '12500.0']]

    // EXAMPLE 4. An inter-ledger set-off journal. We will transfer 1,500 from the customer
    // account to a supplier account. This is typically done when a customer is also a
    // supplier and only the net difference is to be paid or received. We do not perform
    // any allocations and, just as an example, we specify that the postings are to be placed
    // on settlement hold in both the AR and AP accounts. Remember that, for journal type
    // documents, debit values are positive and credit values are negative.
    //def tlcData = [header: [type: 'SOJ', date: new Date().format('yyyy-MM-dd'), reference: 'Set Off', description: 'Inter-ledger transfer', hold: true],
    //        lines: [[ledger: 'ar', account: 'C100200', value: '-1500.0', description: 'S100200 set-off'],
    //                [ledger: 'ap', account: 'S100200', value: '1500.0', description: 'C100200 set-off']]]

    // EXAMPLE 5. An accrual. Note that we do not need to specify the account for the total
    // line since the system can work this out for itself. Also note that we do not need to
    // specify the reversal of the accrual in the next accounting period since this is created
    // automatically for us, but that this means that the 'next' period must have a status of
    // either open or adjust. Like all documents, the period that the document is to be posted
    // to is selected using the document date in combination with the status of the periods
    // within your system (with allowance for the setting of any adjustment flag in the
    // document header map). Just for illustration purposes, we will set the adjutment flag in
    // our header so that the document can be posted to either an open or adjustment period.
    //def tlcData = [header: [type: 'AC', date: new Date().format('yyyy-MM-dd'), reference: 'Provision', description: 'Establishment cost provisions', adjustment: true],
    //        lines: [[ledger: 'gl', account: '320000-RED', value: '1234.0', description: 'Redmond office'],
    //                [ledger: 'gl', account: '320000-GRN', value: '2345.0', description: 'Greenwich office'],
    //                [ledger: 'gl', account: '320000-BLU', value: '3456.0', description: 'Bloomfield office']],
    //        total: [value: '7035.0']]

    // We can run this Groovy test program from the command line with: groovy RestTest
    static main(args) {
        new RestTest().post()
    }

    // Perform the RESTful posting test
    def post() {

        // Get the test data as a JSON string
        def text = JSONObject.fromObject(tlcData).toString()

        // Create the http builder object
        def http = new HTTPBuilder(tlcURL)

        // Perform a POST request, expecting PLAIN TEXT response data
        http.request(POST, TEXT) {req ->

            // Set our request header information
            headers.'tlc-agent' = tlcAgent
            headers.'tlc-timestamp' = System.currentTimeMillis()

            // Set the body of the request
            body = text

            // Build up the tlc headers (lower case keys) in alphabetic order
            def hdrs = new TreeMap()
            def val
            for (hdr in headers) {
                val = hdr.key.toLowerCase(Locale.US)	// Need to use US locale to avoid things like the Turkish undotted i
                if (val.startsWith('tlc-') && val.length() > 4 && val != 'tlc-signature') hdrs.put(val, hdr.value)
            }

            // Build up the plain text starting with the tlc headers
            def sb2 = new StringBuilder()
            for (hdr in hdrs) {

                // Only include non-empty headers
                if (hdr.value != null && hdr.value != '') {
                    sb2.append(hdr.key)
                    sb2.append(hdr.value)
                }
            }

            // Add in any request body
            if (text) sb2.append(text)

            // Create the message signature (SHA1 HMAC, Base64 encoded) and add it to our headers
            Mac mac = Mac.getInstance(HMAC_SHA1_ALGORITHM)
            mac.init(new SecretKeySpec(tlcSecret.getBytes(), HMAC_SHA1_ALGORITHM))
            headers.'tlc-signature' = (new BASE64Encoder()).encode(mac.doFinal(sb2.toString().getBytes()))

            // Response handler for a success response code:
            response.success = {resp, reader ->
                def results = getBody(reader)
                println "Success: ${tlcData.header.type} document code is ${results?.code}"
            }

            // Handler for any failure status code:
            response.failure = {resp, reader ->
                if (resp.statusLine.statusCode >= 500) {
                    println "Error: ${resp.statusLine.statusCode} Server error"
                } else {
                    def results = getBody(reader)
                    println "Error: ${resp.statusLine.statusCode} ${results?.reason}"
                }
            }
        }
    }

    // Return the body of a response as a JSONObject (which implements the Map interface)
    def getBody(reader) {
        if (!reader) return null
        char[] chars = new char[100]
        def sb = new StringBuilder()
        def count
        while ((count = reader.read(chars)) > 0) {
            sb.append(chars, 0, count)
            if (count < chars.size()) break
        }

        return sb.length() ? JSONObject.fromObject(sb.toString()) : null
    }
}