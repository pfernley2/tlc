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

import it.sauronsoftware.cron4j.Predictor
import java.util.concurrent.atomic.AtomicLong
import org.grails.tlc.books.*
import org.grails.tlc.sys.*

class Company {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [country: SystemCountry, language: SystemLanguage]
    static hasMany = [users: CompanyUser, currencies: ExchangeCurrency, measures: Measure, scales: Scale, tasks: Task,
            messages: Message, taxCodes: TaxCode, taxAuthority: TaxAuthority, documentTypes: DocumentType, years: Year,
            chartSections: ChartSection, codeElements: CodeElement, customers: Customer, suppliers: Supplier,
            accessCodes: AccessCode, settings: Setting, schedules: PaymentSchedule, profitFormats: ProfitReportFormat,
            balanceFormats: BalanceReportFormat]

    static transients = ['currency', 'displayCurrency', 'displayTaxCode', 'loadDemo']

    String name
    SystemCurrency currency                 // Transient. Used for data entry on initial creation
    ExchangeCurrency displayCurrency        // Transient. Used after creation for displaying the company currency
    TaxCode displayTaxCode                  // Transient. Used for displaying and changing the company tax code
    Boolean loadDemo                        // Transient. Used to specify whether demo data should be loaded or not
    Boolean systemOnly = false              // Only one company (the initial 'System' company) should have this set
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache true
        columns {
            country lazy: true
            language lazy: true
        }
    }

    static constraints = {
        name(blank: false, size: 1..100, unique: true)
        securityCode(validator: {val, obj ->
            if (val == 0) obj.securityCode = new Date().getTime()
            return true
        })
    }

    // This company record must already have been saved. This method assumes it is
    // within a transaction and returns null if everything went ok or a String
    // indicating the area an error occurred and that the transaction should be
    // rolled back. The user must be a valid user for the given company.
    // Currency should be the SystemCurrency that will be the company's currency.
    // The rate is the BigDecimal exchange rate to use if the currency is not USD.
    // If the currency is USD, then the rate parameter is ignored
    def initializeData(user, currency, rate = 1.0, demoData = null) {

        def lst, target

        // Settings
        lst = SystemSetting.findAllBySystemOnly(false)
        for (obj in lst) {
            target = new Setting(company: this, code: obj.code, dataType: obj.dataType, dataScale: obj.dataScale, value: obj.value)
            if (!target.saveThis()) return 'Setting'
        }

        // Measures
        lst = SystemMeasure.list()
        for (obj in lst) {
            target = new Measure(company: this, code: obj.code, name: obj.name)
            if (!target.saveThis()) return 'Measure'

            if (!syncMessages("measure.name.${obj.code}", obj.name)) return 'Message'
        }

        // Scales
        lst = SystemScale.list()
        for (obj in lst) {
            target = new Scale(company: this, code: obj.code, name: obj.name)
            if (!target.saveThis()) return 'Scale'

            if (!syncMessages("scale.name.${obj.code}", obj.name)) return 'Message'
        }

        // Units
        lst = SystemUnit.list()
        for (obj in lst) {
            def measure = Measure.findByCodeAndCompany(obj.measure.code, this)
            def scale = Scale.findByCodeAndCompany(obj.scale.code, this)
            target = new Unit(measure: measure, scale: scale, code: obj.code, name: obj.name, multiplier: obj.multiplier)
            if (!target.saveThis()) return 'Unit'

            if (!syncMessages("unit.name.${obj.code}", obj.name)) return 'Message'
        }

        // Conversions
        lst = SystemConversion.list()
        for (obj in lst) {
            def src = Unit.findByCodeAndSecurityCode(obj.source.code, this.securityCode)
            def tgt = Unit.findByCodeAndSecurityCode(obj.target.code, this.securityCode)
            target = new Conversion(source: src, target: tgt, code: obj.code, name: obj.name,
                    preAddition: obj.preAddition, multiplier: obj.multiplier, postAddition: obj.postAddition)
            if (!target.saveThis()) return 'Conversion'

            if (!syncMessages("conversion.name.${obj.code}", obj.name)) return 'Message'
        }

        // Tasks
        def system = Company.findBySystemOnly(true)
        if (this.id != system.id) {
            def sched, nxt, usr
            lst = Task.findAllByCompanyAndSystemOnly(system, false)
            for (obj in lst) {
                if (obj.schedule) {
                    usr = user
                    sched = (this.id % 60).toString() + obj.schedule.substring(obj.schedule.indexOf(' '))
                    nxt = obj.nextScheduledRun ? new Predictor(sched, new Date(System.currentTimeMillis() + 900000L)).nextMatchingDate() : null
                } else {
                    usr = null
                    sched = null
                    nxt = null
                }

                target = new Task(company: this, user: usr, activity: obj.activity, code: obj.code, name: obj.name, schedule: sched, nextScheduledRun: nxt,
                        executable: obj.executable, allowOnDemand: obj.allowOnDemand, retentionDays: obj.retentionDays)
                if (!target.saveThis()) return 'Task'

                if (!syncMessages("task.name.${obj.code}", obj.name)) return 'Message'
            }

            // Task Parameters
            for (tsk in lst) {
                def parameters = TaskParam.findAllByTask(tsk)
                if (parameters) {
                    def task = Task.findByCompanyAndCode(this, tsk.code)
                    for (obj in parameters) {
                        target = new TaskParam(task: task, code: obj.code, name: obj.name, sequencer: obj.sequencer,
                                dataType: obj.dataType, dataScale: obj.dataScale, defaultValue: obj.defaultValue, required: obj.required)
                        if (!target.saveThis()) return 'TaskParam'

                        if (!syncMessages("taskParam.name.${task.code}.${obj.code}", obj.name)) return 'Message'
                    }
                }
            }

            // Task Results
            for (tsk in lst) {
                def results = TaskResult.findAllByTask(tsk)
                if (results) {
                    def task = Task.findByCompanyAndCode(this, tsk.code)
                    for (obj in results) {
                        target = new TaskResult(task: task, code: obj.code, name: obj.name,
                                sequencer: obj.sequencer, dataType: obj.dataType, dataScale: obj.dataScale)
                        if (!target.saveThis()) return 'TaskResult'

                        if (!syncMessages("taskResult.name.${task.code}.${obj.code}", obj.name)) return 'Message'
                    }
                }
            }
        }

        // Document Types
        lst = SystemDocumentType.list()
        for (obj in lst) {
            target = new DocumentType(company: this, type: obj, code: obj.code, name: obj.name)
            if (!target.saveThis()) return 'DocumentType'
        }

        // Payment Schedules
        lst = SystemPaymentSchedule.list()
        for (obj in lst) {
            target = new PaymentSchedule(company: this, code: obj.code, name: obj.name, monthDayPattern: obj.monthDayPattern, weekDayPattern: obj.weekDayPattern)
            if (!target.saveThis()) return 'PaymentSchedule'
        }

        // Exchange Currencies and Rates
        def companyCurrencyIsBaseCurrency = (currency.code == UtilService.BASE_CURRENCY_CODE)
        def base = companyCurrencyIsBaseCurrency ? currency : SystemCurrency.findByCode(UtilService.BASE_CURRENCY_CODE)
        target = new ExchangeCurrency(company: this, code: base.code, name: base.name,
                decimals: base.decimals, autoUpdate: false, companyCurrency: companyCurrencyIsBaseCurrency)
        if (!target.saveThis()) return 'ExchangeCurrency'

        def companyCurrency = target   // Remember the new base currency and assume that it will be the company currency

        if (!syncMessages("currency.name.${companyCurrency.code}", companyCurrency.name)) return 'Message'

        target = new ExchangeRate(currency: companyCurrency, validFrom: UtilService.EPOCH, rate: 1.0)
        if (!target.saveThis()) return 'ExchangeRate'

        if (!companyCurrencyIsBaseCurrency) {
            target = new ExchangeCurrency(company: this, code: currency.code,
                    name: currency.name, decimals: currency.decimals, autoUpdate: currency.autoUpdate, companyCurrency: true)
            if (!target.saveThis()) return 'ExchangeCurrency'

            companyCurrency = target   // Remember the new company currency

            if (!syncMessages("currency.name.${companyCurrency.code}", companyCurrency.name)) return 'Message'

            target = new ExchangeRate(currency: companyCurrency, validFrom: UtilService.EPOCH, rate: rate)
            if (!target.saveThis()) return 'ExchangeRate'
        }

        // Tax authority
        target = new TaxAuthority(company: this, name: 'Primary tax authority', usage: 'mandatory')
        if (!target.saveThis()) return 'TaxAuthority'

        // Tax codes and rates
        def taxCode = new TaxCode(company: this, code: 'exempt', name: 'Exempt from taxation', companyTaxCode: true, authority: target)
        if (!taxCode.saveThis()) return 'TaxCode'

        if (!syncMessages("taxCode.name.${taxCode.code}", taxCode.name)) return 'Message'

        target = new TaxRate(taxCode: taxCode, validFrom: UtilService.EPOCH, rate: 0.0)
        if (!target.saveThis()) return 'TaxRate'

		def utilService = this.domainClass.grailsApplication.mainContext.getBean('utilService')
		if (demoData == null) demoData = utilService.systemSetting('isDemoSystem')
        if (demoData) {
            def sysUser = SystemUser.findAllByAdministrator(true, [sort: 'id', order: 'desc'])[0]
            def sysTask = Task.findByCompanyAndCode(system, 'demodata')
            if (!utilService.taskService.submit(sysTask,
                    [[param: TaskParam.findByTaskAndCode(sysTask, 'companyId'), value: this.id.toString()],
                    [param: TaskParam.findByTaskAndCode(sysTask, 'userId'), value: user.id.toString()],
                    [param: TaskParam.findByTaskAndCode(sysTask, 'currencyId'), value: companyCurrency.id.toString()],
                    [param: TaskParam.findByTaskAndCode(sysTask, 'taxCodeId'), value: taxCode.id.toString()]],
                    sysUser)) return 'DemoLoad'
        }

        return null // Everything was ok
    }

    // Because there is potentially so much data associated with a company, together
    // with complex inter-relationships which could trip-up cascadng deletes, this
    // method should be called before actually deleting a company. The caller should
    // NOT use a transaction since we use them ourselves given that some databases use
    // fixed size rollback segments which can stop the deletion from working.
    def prepareForDeletion(session) {
        remove(session, Recurring.findAllBySecurityCode(this.securityCode))         // Delete recurring bank transactions since they references GL accounts
        remove(session, Reconciliation.findAllBySecurityCode(this.securityCode))    // Delete bank reconciliations since they references GL accounts
        remove(session, TemplateDocument.findAllBySecurityCode(this.securityCode))  // Delete document templates since they references GL accounts
        remove(session, QueuedTask.findAllBySecurityCode(this.securityCode))        // Delete queued tasks since they references company users and tasks
        remove(session, Task.findAllByCompany(this))                                // Delete tasks since they reference company users
        remove(session, Conversion.findAllBySecurityCode(this.securityCode))        // Delete conversion since they reference units
        remove(session, Unit.findAllBySecurityCode(this.securityCode))              // Delete units since they reference measures and scales
        remove(session, TaxStatement.findAllBySecurityCode(this.securityCode))      // Delete all tax statements making it faster for year deletion and also removes any un-finalized statements
		
        // Delete all transactions and then the years themselves
        def list = Year.findAllByCompany(this)
        for (it in list) it.deleteTransactions()
		
        remove(session, list)
        remove(session, Customer.findAllByCompany(this))                            // Delete all customer records since access codes refer to them
        remove(session, Supplier.findAllByCompany(this))                            // Delete all supplier records since access codes refer to them
        DocumentType.withTransaction {status ->                                     // Clear document type auto-payment details since they reference bank accounts in the GL
            DocumentType.executeUpdate('update DocumentType set autoBankAccount = null, autoForeignCurrency = null, autoMaxPayees = null where company = ? and autoBankAccount is not null', [this])
        }

        Account.withTransaction {status ->                                          // Clear GL revaluation accounts since they self reference other GL accounts
            Account.executeUpdate('update Account set revaluationAccount = null, revaluationMethod = null where securityCode = ? and revaluationAccount is not null', [this.securityCode])
        }
        remove(session, Account.findAllBySecurityCode(this.securityCode))           // Delete general ledger accounts otherwise the chart of accounts sections cannot be deleted
        remove(session, ChartSection.findAllByCompany(this))                        // Delete chart of accounts sections since they reference code elements
        remove(session, CodeElementValue.findAllBySecurityCode(this.securityCode))  // Delete code element values since they are referenced by the code elements themselves
        remove(session, TaxCode.findAllByCompany(this, [cache: true]))              // Delete tax codes since they reference tax authorities

        // Delete members of all access groups otherwise the group delete would reference company users,
        // then delete the group itself
        list = AccessGroup.findAllByCompany(this)
        AccessGroup.withTransaction {status ->
            for (group in list) {

                // Need to avoid concurrent modification exceptions
                def temp = []
                for (it in group.users) temp << it
                for (it in temp) group.removeFromUsers(it)
                group.save()    // With deep validation
                group.delete(flush: true)
            }
        }

        for (item in list) {
            session.evict(item)
        }
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

    // --------------------------------------------- Support Methods ---------------------------------------------

    private syncMessages(code, name) {

        // Always create the default locale record with the given name
        def message = new Message(company: this, code: code, locale: '*', text: name)
        if (!message.saveThis()) return false

        // Grab any other locales for this code
        def messages = SystemMessage.findAllByCodeAndLocaleNotEqual(code, '*')

        for (msg in messages) {
            message = new Message(company: this, code: code, locale: msg.locale, text: msg.text)
            if (!message.saveThis()) return false
        }

        return true
    }

    private remove(session, list) {
		if (list) {
	        Company.withTransaction {
	            for (item in list) {
	                item.delete(flush: true)
	            }
	        }
	
	        for (item in list) {
	            session.evict(item)
	        }
		}
    }
}
