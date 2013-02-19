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
package org.grails.tlc.sys

import org.grails.tlc.books.Allocation
import org.grails.tlc.books.Customer
import org.grails.tlc.books.GeneralTransaction
import org.grails.tlc.books.Supplier
import grails.util.Environment

class UpgradeService {

    static transactional = false

    static upgrade(dataVersion, to, servletContext) {
        if (dataVersion.value == '1.0') {
            if (!upgradeTo_1_1(dataVersion, to, servletContext)) return false
        }

        if (dataVersion.value == '1.1') {
            if (!upgradeTo_1_2(dataVersion, to, servletContext)) return false
        }

        if (dataVersion.value == '1.2') {
            if (!upgradeTo_1_3(dataVersion, to, servletContext)) return false
        }
		
		if (dataVersion.value == '1.3') {
			if (!upgradeTo_2_0(dataVersion, to, servletContext)) return false
		}

        return true
    }

    static upgradeTo_1_1(dataVersion, to, servletContext) {

        // Do the non-transactional stuff first
        importMessageFile(servletContext, 'tlc')
        setPageHelp(servletContext, 'revaluation')
        setPageHelp(servletContext, 'account.enquire')
        setPageHelp(servletContext, 'customer.enquire')
        setPageHelp(servletContext, 'supplier.enquire')
        setPageHelp(servletContext, 'document.allocations')

        // Now for the serious stuff
        def valid = true
        def customers = Customer.findAll('from Customer as x where x.revaluationMethod is null and x.currency.companyCurrency = ?', [false])
        def suppliers = Supplier.findAll('from Supplier as x where x.revaluationMethod is null and x.currency.companyCurrency = ?', [false])
        def transactions = GeneralTransaction.findAll('from GeneralTransaction where (customer is not null or supplier is not null) and companyUnallocated is null')
        Allocation.withTransaction {status ->
            if (valid && !SystemAccountType.findByCode('glRevalue')) {
                if (!createAccountType(code: 'glRevalue', name: 'GL revaluation account', sectionType: 'bs',
                        singleton: false, changeable: true, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: true)) valid = false
            }

            if (valid && !SystemAccountType.findByCode('arRevalue')) {
                if (!createAccountType(code: 'arRevalue', name: 'AR revaluation account', sectionType: 'bs',
                        singleton: true, changeable: true, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: true)) valid = false
            }

            if (valid && !SystemAccountType.findByCode('apRevalue')) {
                if (!createAccountType(code: 'apRevalue', name: 'AP revaluation account', sectionType: 'bs',
                        singleton: true, changeable: true, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: true)) valid = false
            }

            if (valid) {
                for (customer in customers) {
                    customer.revaluationMethod = 'standard'
                    if (!customer.saveThis()) {
                        valid = false
                        break
                    }
                }
            }

            if (valid) {
                for (supplier in suppliers) {
                    supplier.revaluationMethod = 'standard'
                    if (!supplier.saveThis()) {
                        valid = false
                        break
                    }
                }
            }

            if (valid) {
                def total, val, currency, lastAlloc
                for (tran in transactions) {
                    if (tran.accountValue == tran.companyValue) {                   // It's in company currency
                        tran.companyUnallocated = tran.accountUnallocated
                        for (alloc in tran.allocations) alloc.companyValue = alloc.accountValue
                    } else {    // Foreign Currency
                        currency = tran.balance.account.currency                    // Get the company currency
                        if (tran.accountValue == 0.0 && tran.companyValue != 0.0) { // An fx adjustment document
                            tran.companyUnallocated = tran.companyValue
                        } else if (tran.accountUnallocated == 0.0) {                // Fully allocated
                            tran.companyUnallocated = 0.0
                        } else if (tran.accountUnallocated == tran.accountValue) {  // Completely unallocated
                            tran.companyUnallocated = tran.companyValue
                        } else {                                                    // Partially allocated
                            tran.companyUnallocated = UtilService.round((tran.companyValue * tran.accountUnallocated) / tran.accountValue, currency.decimals)
                        }

                        total = tran.companyValue - tran.companyUnallocated
                        lastAlloc = null
                        for (alloc in tran.allocations) {
                            lastAlloc = alloc
                            val = UtilService.round((tran.companyValue * alloc.accountValue) / tran.accountValue, currency.decimals)
                            alloc.companyValue = val
                            total += val
                        }

                        if (lastAlloc && total) lastAlloc.companyValue -= total     // Take care of any rounding differnces
                    }

                    if (!tran.save(flush: true)) {  // With deep validation
                        valid = false
                        break
                    }
                }
            }

            if (valid) {

                // Update the data version number
                dataVersion.value = '1.1'
                if (!dataVersion.saveThis()) valid = false
            }

            if (!valid) status.setRollbackOnly()
        }

        return valid
    }

    static upgradeTo_1_2(dataVersion, to, servletContext) {

        // Just need to import the new Danish message file
        importMessageFile(servletContext, 'messages', new Locale('da'))

        // Update the data version number
        dataVersion.value = '1.2'
        return dataVersion.saveThis()
    }

    static upgradeTo_1_3(dataVersion, to, servletContext) {

        // Import the rest message file
        importMessageFile(servletContext, 'rest')

        // Update relevant page helps
        setPageHelp(servletContext, 'companyUser.list')
        setPageHelp(servletContext, 'agentCredential')

        // Update the data version number
        dataVersion.value = '1.3'
        return dataVersion.saveThis()
    }
	
	static upgradeTo_2_0(dataVersion, to, servletContext) {
		
		// Correct the system messages that were (and still are as of Grails 2.2.0) using illogical locale
		// values. We have renamed the standard Grails property files for TLC version 2.0 onwards.
		SystemMessage.executeUpdate("update SystemMessage set locale = 'pt' where locale = 'ptPT'")
		SystemMessage.executeUpdate("update SystemMessage set locale = 'zh' where locale = 'zhCN'")
		
		// Also handle the change of locale cz to cs
		SystemMessage.executeUpdate("update SystemMessage set locale = 'cs' where locale = 'cz'")
		
		// Add any new tlc specific messages
		importMessageFile(servletContext, 'tlc')
		
		// Add new system messages
		def messageLocales = ['', 'cs', 'da', 'de', 'es', 'fr', 'it', 'ja', 'nb', 'nl', 'pl', 'ptBR', 'pt', 'ru', 'sv', 'th', 'zh']
		def locale
		for (loc in messageLocales) {
			if (!loc) {
				locale = null
			} else if (loc.length() == 2) {
				locale = new Locale(loc)
			} else {
				locale = new Locale(loc.substring(0, 2), loc.substring(2))
			}
			
			importMessageFile(servletContext, 'messages', locale)
		}
		
		// Clean up the old messages that have been replaced by '.label' equivalents
		for (msg in SystemMessage.findAllByLocaleAndCodeLike('*', '%.%.label')) {
			if (!msg.code.startsWith('default.')) SystemMessage.executeUpdate('delete from SystemMessage where code = ?', [msg.code - '.label'])
		}
		
		// Clean up the redundant messages that have been replaced by standard messages
		def standards = [:]
		def sql
		standards.created = ['taskResult.name.autopay.created', 'taskResult.name.fxRates.created', 'document.created', 'remittance.created']
		standards.updated = ['company.logo.updated', 'operation.updated']
		standards.deleted = ['remittance.not.deleted', 'remittanceLine.deleted', 'remittance.deleted']
		standards.'not.deleted' = ['remittance.not.deleted']
		standards.'not.found' = ['customer.statement.not.found']
		standards.'optimistic.locking.failure' = null
		standards.list = ['budget.list', 'documentSearch.list', 'menu.crumb.Customer.Reports.List', 'menu.crumb.Supplier.Reports.List',
			'menu.option.Customer.Reports.List', 'menu.option.Supplier.Reports.List', 'queuedTask.list', 'queuedTask.sys.list',
			'queuedTask.usr.list', 'reconciliationLineDetail.list', 'remittanceLine.list', 'templateDocument.apj.list',
			'templateDocument.arj.list', 'templateDocument.bank.list', 'templateDocument.cash.list', 'templateDocument.fj.list',
			'templateDocument.glj.list', 'templateDocument.pi.list', 'templateDocument.provn.list', 'templateDocument.si.list',
			'templateDocument.so.list']
		standards.'list.for' = ['queuedTaskParam.list.for', 'queuedTaskResult.list.for', 'reconciliation.list.for',
			'remittanceLine.list.for', 'systemRole.list.for']
		standards.add = ['generic.add']
		standards.new = ['year.no.new', 'remittanceLine.new', 'period.status.new']
		standards.create = null
		standards.show = ['queuedTask.sys.show']
		standards.edit = ['queuedTaskParam.wait.edit', 'queuedTask.sys.edit', 'queuedTask.wait.edit']
		for (entry in standards) {
			sql = "delete from SystemMessage where code like '%.${entry.key}' and code not like 'default.%'"
			if (entry.value) {
				sql += ' and code not in ('
				for (int i = 0; i < entry.value.size(); i++) {
					if (i) sql += ', '
					sql += "'${entry.value[i]}'"
				}
				
				sql += ')'
			}
			
			SystemMessage.executeUpdate(sql)
		}
		
		// Load new page help texts
		setPageHelp(servletContext, 'taxAuthority')
		setPageHelp(servletContext, 'taxCode')
		setPageHelp(servletContext, 'translation')
		
		// Link in the new translation system to the menu and roles
		SystemRole.executeUpdate('update SystemRole set systemOnly = true')
		def role = new SystemRole(code: 'translator', name: 'Translator', systemOnly: true)
		role.saveThis()
		def activity = SystemActivity.findByCode('systran')
		if (activity) {
			role.addToActivities(activity)
	        if (role.save(flush: true))	{	// With deep validation
	        	new SystemMessage(code: 'role.name.translator', locale: '*', text: 'Translator').saveThis()
	        }
			
			def menu = new SystemMenu(path: 'Translation', title: 'Translation', sequencer: 20, activity: activity, type: 'action', command: 'translation.translate', parameters: null)
			if (menu.saveThis()) {
				new SystemMessage(code: 'menu.option.Translation', locale: '*', text: 'Translation').saveThis()
				new SystemMessage(code: "menu.crumb.Translation", locale: '*', text: 'Translation').saveThis()
			}
		}

        // Update the data version number
        dataVersion.value = '2.0'
        return dataVersion.saveThis()
	}

    // Imports a message file to the database. DOES NOT modify existing
    // records. The name should not include any locale suffix, the optional
    // locale parameter handles this.
    static importMessageFile(servletContext, name, locale = null) {
        def dir
        if (Environment.current == Environment.PRODUCTION) {
            dir = new File(new File(servletContext.getRealPath('/')), "WEB-INF${File.separator}grails-app${File.separator}i18n")
        } else {
            dir = new File(new File(servletContext.getRealPath('/')).getParent(), "grails-app${File.separator}i18n")
        }

        if (dir.exists() && dir.canRead()) {
            if (locale?.getLanguage()) {
                name = name + '_' + locale.getLanguage()
                if (locale.getCountry()) name = name + '_' + locale.getCountry()
            }

            def file = new File(dir, name + '.properties')
            if (file.isFile() && file.canRead()) DatabaseMessageSource.loadPropertyFile(file, locale)
        }
    }

    // Updates a page help in the database, creating it if necessary.
    // The name should not include any locale suffix, the optional locale
    // parameter handles this.
    static setPageHelp(servletContext, name, locale = null) {
        def dir = new File(servletContext.getRealPath('/pagehelp'))
        if (dir.exists() && dir.canRead()) {
            def key = name
            def loc = locale ? locale.getLanguage() + locale.getCountry() : '*'
            if (locale?.getLanguage()) {
                name = name + '_' + locale.getLanguage()
                if (locale.getCountry()) name = name + '_' + locale.getCountry()
            }

            def file = new File(dir, name + '.helptext')
            if (file.isFile() && file.canRead()) {
                def reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), 'UTF-8'))
                def text = null
                try {
                    def line = reader.readLine()
                    while (line != null) {
                        if (text == null) {
                            text = line
                        } else {
                            text = text + '\n' + line
                        }

                        line = reader.readLine()
                    }
                } finally {
                    if (reader) reader.close()
                }

                if (key.length() <= 250 && text.length() <= 8000) {
                    def rec = SystemPageHelp.findByCodeAndLocale(key, loc) ?: new SystemPageHelp(code: key, locale: loc)
                    rec.text = text
                    rec.saveThis()
                }
            }
        }
    }

	// Create a new account type. Caller should handle transactions.
    static createAccountType(map) {
        if (new SystemAccountType(map).saveThis()) {
            if (new SystemMessage(code: "systemAccountType.name.${map.code}", locale: '*', text: map.name).saveThis()) return true
        }

        return false
    }
	
	// Add a new system message, if possible. Caller should handle transactions if required
	static addNewMessage(code, text) {
		if (SystemMessage.countByCode(code)) return true	// If they already use this code, there is nothing we can do, so just carry on
		if (new SystemMessage(code: code, locale: '*', text: text).saveThis()) return true
		
		return false
	}
}
