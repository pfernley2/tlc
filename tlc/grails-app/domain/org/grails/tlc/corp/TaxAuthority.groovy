package org.grails.tlc.corp

import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class TaxAuthority {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company]
    static hasMany = [taxCodes: TaxCode, statements: TaxStatement]

    String name
    String usage
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache true
        columns {
            company lazy: true
            taxCodes cascade: 'save-update'
            statements cascade: 'save-update'
            usage column: 'code_usage'
        }
    }

    static constraints = {
        name(blank: false, size: 1..30, unique: 'company')
        usage(inList: ['mandatory', 'optional', 'ad-hoc'])
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.company.securityCode
            return true
        })
    }

    def afterInsert() {
        UtilService.trace('insert', this)
    }

    def afterUpdate() {
        UtilService.trace('update', this)
    }

    def afterDelete() {
        UtilService.trace('delete', this)
    }

    public String toString() {
        return name
    }
}
