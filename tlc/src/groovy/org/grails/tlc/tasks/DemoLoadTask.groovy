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

import org.grails.tlc.corp.Company
import org.grails.tlc.corp.CompanyUser
import org.grails.tlc.corp.ExchangeCurrency
import org.grails.tlc.corp.TaxCode
import doc.Line
import org.grails.tlc.books.*
import org.grails.tlc.sys.*

class DemoLoadTask extends TaskExecutable {

    def execute() {
        def corp = Company.get(params.companyId)
        if (!corp) {
            completionMessage = utilService.standardMessage('not.found', 'company', params.companyId)
            return false
        }

        def usr = SystemUser.get(params.userId)
        if (!usr) {
            completionMessage = utilService.standardMessage('not.found', 'systemUser', params.userId)
            return false
        }

        def companyUser = CompanyUser.findByCompanyAndUser(corp, usr, [cache: true])
        if (!companyUser) {
            completionMessage = message(code: 'task.no.combo', default: 'Invalid company and user combination')
            return false
        }

        def curr = ExchangeCurrency.get(params.currencyId)
        if (curr?.company?.id != corp.id) {
            completionMessage = utilService.standardMessage('not.found', 'exchangeCurrency', params.currencyId)
            return false
        }

        def taxCode = TaxCode.get(params.taxCodeId)
        if (taxCode?.company?.id != corp.id) {
            completionMessage = utilService.standardMessage('not.found', 'taxCode', params.taxCodeId)
            return false
        }

        if (ChartSection.countByCompany(corp) || Year.countByCompany(corp) || AccessCode.countByCompany(corp)) {
            completionMessage = message(code: 'account.existing', default: 'Account already exists')
            return false
        }

        def ctry = corp.country
        def addressFormat = SystemAddressFormat.findByCode('default')
        def customerAddressTypes = SystemCustomerAddressType.list()
        def customerContactTypes = SystemCustomerContactType.list()
        def supplierAddressTypes = SystemSupplierAddressType.list()
        def supplierContactTypes = SystemSupplierContactType.list()
        yield()

        def accountElement, domSales, intSales, purchases, invAdjust, wages, salaries, expenses, establishment, repairs, legal, depnCharge, assetSale, fxDiff
        def buildingsCost, buildingsDepn, fnfCost, fnfDepn, carsCost, carsDepn, inventory, ar, prepay, cash, bank, ap, accrue, tax, due, revalue, capital, retained
        def lobElement, hwLob, swLob, officeElement, choOffice, redOffice, grnOffice, bluOffice, projectElement, noProject, alphaProject, betaProject, gammaProject
        def arRev, apRev, faRevalue, bkRevalue

        // Code elements and values
        Account.withTransaction {status ->
            accountElement = new CodeElement(company: corp, elementNumber: (byte) 1, name: 'Account', dataType: 'numeric', dataLength: (byte) 6).saveThis()
            domSales = new CodeElementValue(element: accountElement, code: '100000', shortName: 'DomSales', name: 'Domestic Sales').saveThis()
            intSales = new CodeElementValue(element: accountElement, code: '150000', shortName: 'IntSales', name: 'International Sales').saveThis()
            purchases = new CodeElementValue(element: accountElement, code: '200000', shortName: 'Purchases', name: 'Purchases').saveThis()
            invAdjust = new CodeElementValue(element: accountElement, code: '240000', shortName: 'InvAdjust', name: 'Inventory Adjustment').saveThis()
            wages = new CodeElementValue(element: accountElement, code: '280000', shortName: 'Wages', name: 'Wages').saveThis()
            salaries = new CodeElementValue(element: accountElement, code: '300000', shortName: 'Salaries', name: 'Salaries').saveThis()
            expenses = new CodeElementValue(element: accountElement, code: '310000', shortName: 'Expenses', name: 'Reimbursed Expenses').saveThis()
            establishment = new CodeElementValue(element: accountElement, code: '320000', shortName: 'Establish', name: 'Establishment Costs').saveThis()
            repairs = new CodeElementValue(element: accountElement, code: '330000', shortName: 'Repairs', name: 'Repairs and Renewals').saveThis()
            legal = new CodeElementValue(element: accountElement, code: '340000', shortName: 'Legal', name: 'Legal and Professional').saveThis()
            depnCharge = new CodeElementValue(element: accountElement, code: '350000', shortName: 'DepnCharge', name: 'Depreciation Charge').saveThis()
            assetSale = new CodeElementValue(element: accountElement, code: '400000', shortName: 'AssetSales', name: 'P&L on sale of Assets').saveThis()
            fxDiff = new CodeElementValue(element: accountElement, code: '450000', shortName: 'Exchange', name: 'Foreign Exchange Differences').saveThis()
            buildingsCost = new CodeElementValue(element: accountElement, code: '500000', shortName: 'Buildings', name: 'Land & Buildings at Cost').saveThis()
            buildingsDepn = new CodeElementValue(element: accountElement, code: '500500', shortName: 'BuildDepn', name: 'Land & Buildings Depreciation').saveThis()
            fnfCost = new CodeElementValue(element: accountElement, code: '510000', shortName: 'Fixtures', name: 'Fixtures & Fittings at Cost').saveThis()
            fnfDepn = new CodeElementValue(element: accountElement, code: '510500', shortName: 'FixtDepn', name: "Fixtures & Fittings Dep'n").saveThis()
            carsCost = new CodeElementValue(element: accountElement, code: '520000', shortName: 'Vehicles', name: 'Vehicles at Cost').saveThis()
            carsDepn = new CodeElementValue(element: accountElement, code: '520500', shortName: 'VehDepn', name: 'Vehicles Depreciation').saveThis()
            faRevalue = new CodeElementValue(element: accountElement, code: '590000', shortName: 'FA Revalue', name: 'Fixed Asset FX Revaluation').saveThis()
            inventory = new CodeElementValue(element: accountElement, code: '600000', shortName: 'Inventory', name: 'Inventory').saveThis()
            ar = new CodeElementValue(element: accountElement, code: '610000', shortName: 'Receivable', name: 'Accounts Receivable').saveThis()
            arRev = new CodeElementValue(element: accountElement, code: '610500', shortName: 'AR Revalue', name: 'AR Revaluation').saveThis()
            prepay = new CodeElementValue(element: accountElement, code: '620000', shortName: 'Prepayment', name: 'Prepayments').saveThis()
            cash = new CodeElementValue(element: accountElement, code: '630000', shortName: 'Cash', name: 'Cash in Hand').saveThis()
            bank = new CodeElementValue(element: accountElement, code: '640000', shortName: 'Bank', name: 'Main Bank Account').saveThis()
            bkRevalue = new CodeElementValue(element: accountElement, code: '690000', shortName: 'BK Revalue', name: 'Bank Account FX Revaluation').saveThis()
            ap = new CodeElementValue(element: accountElement, code: '700000', shortName: 'Payable', name: 'Accounts Payable').saveThis()
            apRev = new CodeElementValue(element: accountElement, code: '700500', shortName: 'AP Revalue', name: 'AP Revaluation').saveThis()
            accrue = new CodeElementValue(element: accountElement, code: '710000', shortName: 'Accrue', name: 'Accruals').saveThis()
            tax = new CodeElementValue(element: accountElement, code: '720000', shortName: 'Tax', name: 'Sales/Purchase Tax Control').saveThis()
            due = new CodeElementValue(element: accountElement, code: '730000', shortName: 'TaxDue', name: 'Sales/Purchase Tax Due').saveThis()
            revalue = new CodeElementValue(element: accountElement, code: '800000', shortName: 'Revalue', name: 'Foreign Exchange Revaluation').saveThis()
            capital = new CodeElementValue(element: accountElement, code: '900000', shortName: 'Capital', name: "Share Capital").saveThis()
            retained = new CodeElementValue(element: accountElement, code: '950000', shortName: 'Retained', name: 'Retained Profits').saveThis()

            lobElement = new CodeElement(company: corp, elementNumber: (byte) 2, name: 'Line of Business', dataType: 'alphabetic', dataLength: (byte) 2).saveThis()
            hwLob = new CodeElementValue(element: lobElement, code: 'HW', shortName: 'Hardware', name: 'Hardware LOB').saveThis()
            swLob = new CodeElementValue(element: lobElement, code: 'SW', shortName: 'Software', name: 'Software LOB').saveThis()

            officeElement = new CodeElement(company: corp, elementNumber: (byte) 3, name: 'Office', dataType: 'alphabetic', dataLength: (byte) 3).saveThis()
            choOffice = new CodeElementValue(element: officeElement, code: 'CHO', shortName: 'Corporate', name: 'Corporate Head Office').saveThis()
            redOffice = new CodeElementValue(element: officeElement, code: 'RED', shortName: 'Redmond', name: 'Redmond Office').saveThis()
            grnOffice = new CodeElementValue(element: officeElement, code: 'GRN', shortName: 'Greenwich', name: 'Greenwich Office').saveThis()
            bluOffice = new CodeElementValue(element: officeElement, code: 'BLU', shortName: 'Bloomfield', name: 'Bloomfield Office').saveThis()

            projectElement = new CodeElement(company: corp, elementNumber: (byte) 4, name: 'Project', dataType: 'numeric', dataLength: (byte) 3).saveThis()
            noProject = new CodeElementValue(element: projectElement, code: '000', shortName: 'No Project', name: 'No Project').saveThis()
            alphaProject = new CodeElementValue(element: projectElement, code: '100', shortName: 'Alpha', name: 'Alpha-Intergalactic Project').saveThis()
            betaProject = new CodeElementValue(element: projectElement, code: '200', shortName: 'Beta', name: 'Beta-Cosmic Project ').saveThis()
            gammaProject = new CodeElementValue(element: projectElement, code: '300', shortName: 'Gamma', name: 'Gamma-Galactic Project ').saveThis()
        }

        yield()

        def ieSection, salesSection, cosSection, expensesSection, otherSection, bsSection, fixedSection, netSection, caSection, clSection, nonSection, equitySection

        // Chart Sections and ranges
        Account.withTransaction {status ->
            ieSection = new ChartSection(company: corp, path: 'ie', code: 'ie', name: 'Income and Expenditure', sequencer: 100, type: 'ie', status: 'cr').saveThis()
            salesSection = new ChartSection(company: corp, path: 'ie.sales', code: 'sales', name: 'Sales', sequencer: 100, type: 'ie',
                    segment1: accountElement, segment2: lobElement, segment3: officeElement, segment4: projectElement,
                    default3: 'CHO', default4: '000', status: 'cr')
            salesSection.parentObject = ieSection
            salesSection.saveThis()
            new ChartSectionRange(section: salesSection, rangeFrom: '100000-*-*-*', rangeTo: '199999-*-*-*').saveThis()
            cosSection = new ChartSection(company: corp, path: 'ie.cos', code: 'cos', name: 'Cost of Sales', sequencer: 200, type: 'ie',
                    segment1: accountElement, segment2: lobElement, segment3: officeElement, segment4: projectElement,
                    default3: 'CHO', default4: '000', status: 'dr')
            cosSection.parentObject = ieSection
            cosSection.saveThis()
            new ChartSectionRange(section: cosSection, rangeFrom: '200000-*-*-*', rangeTo: '299999-*-*-*').saveThis()
            expensesSection = new ChartSection(company: corp, path: 'ie.expenses', code: 'expenses', name: 'Expenses', sequencer: 300, type: 'ie',
                    segment1: accountElement, segment2: officeElement,
                    default2: 'CHO', status: 'dr')
            expensesSection.parentObject = ieSection
            expensesSection.saveThis()
            new ChartSectionRange(section: expensesSection, rangeFrom: '300000-*', rangeTo: '399999-*').saveThis()
            otherSection = new ChartSection(company: corp, path: 'ie.other', code: 'other', name: 'Non-Trading Items', sequencer: 400, type: 'ie',
                    segment1: accountElement, status: 'dr')
            otherSection.parentObject = ieSection
            otherSection.saveThis()
            new ChartSectionRange(section: otherSection, rangeFrom: '400000', rangeTo: '499999').saveThis()

            bsSection = new ChartSection(company: corp, path: 'bs', code: 'bs', name: 'Balance Sheet', sequencer: 500, type: 'bs', status: 'dr').saveThis()
            fixedSection = new ChartSection(company: corp, path: 'bs.fixed', code: 'fixed', name: 'Fixed Assets', sequencer: 100, type: 'bs',
                    segment1: accountElement, status: 'dr')
            fixedSection.parentObject = bsSection
            fixedSection.saveThis()
            new ChartSectionRange(section: fixedSection, rangeFrom: '500000', rangeTo: '599999').saveThis()
            netSection = new ChartSection(company: corp, path: 'bs.net', code: 'net', name: 'Net Current Assets', sequencer: 200, type: 'bs', status: 'dr')
            netSection.parentObject = bsSection
            netSection.saveThis()
            caSection = new ChartSection(company: corp, path: 'bs.net.ca', code: 'ca', name: 'Current Assets', sequencer: 100, type: 'bs',
                    segment1: accountElement, status: 'dr')
            caSection.parentObject = netSection
            caSection.saveThis()
            new ChartSectionRange(section: caSection, rangeFrom: '600000', rangeTo: '699999').saveThis()
            clSection = new ChartSection(company: corp, path: 'bs.net.cl', code: 'cl', name: 'Current Liabilities', sequencer: 200, type: 'bs',
                    segment1: accountElement, status: 'cr')
            clSection.parentObject = netSection
            clSection.saveThis()
            new ChartSectionRange(section: clSection, rangeFrom: '700000', rangeTo: '799999').saveThis()
            nonSection = new ChartSection(company: corp, path: 'bs.non', code: 'non', name: 'Non-Current Assets', sequencer: 300, type: 'bs',
                    segment1: accountElement, status: 'dr')
            nonSection.parentObject = bsSection
            nonSection.saveThis()
            new ChartSectionRange(section: nonSection, rangeFrom: '800000', rangeTo: '899999').saveThis()
            equitySection = new ChartSection(company: corp, path: 'bs.equity', code: 'equity', name: "Shareholder's Equity", sequencer: 400, type: 'bs',
                    segment1: accountElement, status: 'cr')
            equitySection.parentObject = bsSection
            equitySection.saveThis()
            new ChartSectionRange(section: equitySection, rangeFrom: '900000', rangeTo: '999999').saveThis()
        }

        yield()

        def buildingsCostAccount, buildingsDepnAccount, fnfCostAccount, fnfDepnAccount, carsCostAccount, carsDepnAccount
        def inventoryAccount, arAccount, cashAccount, bankAccount, apAccount, capitalAccount, retainedAccount
        def arRevAccount, apRevAccount, faRevAccount, bkRevAccount

        // GL Accounts
        Account.withTransaction {status ->
            createQuadAccounts(section: salesSection, currency: curr, status: 'cr', segment1: [domSales, intSales], segment2: [hwLob, swLob],
                segment3: [choOffice, redOffice, grnOffice, bluOffice], segment4: [noProject, alphaProject, betaProject, gammaProject])
            createQuadAccounts(section: cosSection, currency: curr, status: 'dr', segment1: [purchases], segment2: [hwLob, swLob],
                segment3: [choOffice, redOffice, grnOffice, bluOffice], segment4: [noProject, alphaProject, betaProject, gammaProject])
            createQuadAccounts(section: cosSection, currency: curr, status: 'dr', segment1: [invAdjust], segment2: [hwLob, swLob],
                segment3: [redOffice, grnOffice, bluOffice], segment4: [noProject])
            createQuadAccounts(section: cosSection, currency: curr, status: 'dr', segment1: [wages], segment2: [hwLob, swLob],
                segment3: [choOffice, redOffice, grnOffice, bluOffice], segment4: [noProject, alphaProject, betaProject, gammaProject])
            createDualAccounts(section: expensesSection, currency: curr, status: 'dr', segment1: [salaries, expenses, establishment, repairs, legal, depnCharge],
                segment2: [choOffice, redOffice, grnOffice, bluOffice])
            createAccount(section: otherSection, currency: curr, status: 'dr', segment1: assetSale)
            createAccount(section: otherSection, currency: curr, status: 'dr', segment1: fxDiff, type: 'fxDiff')
            def singles = createSingleAccounts(section: fixedSection, currency: curr, status: 'dr', segment1: [buildingsCost, fnfCost, carsCost])
            buildingsCostAccount = singles[0]
            fnfCostAccount = singles[1]
            carsCostAccount = singles[2]
            singles = createSingleAccounts(section: fixedSection, currency: curr, status: 'cr', segment1: [buildingsDepn, fnfDepn, carsDepn])
            buildingsDepnAccount = singles[0]
            fnfDepnAccount = singles[1]
            carsDepnAccount = singles[2]
            faRevAccount = createAccount(section: fixedSection, currency: curr, status: 'dr', segment1: faRevalue, type: 'glRevalue')
            inventoryAccount = createAccount(section: caSection, currency: curr, status: 'dr', segment1: inventory)
            arAccount = createAccount(section: caSection, currency: curr, status: 'dr', segment1: ar, type: 'ar')
            arRevAccount = createAccount(section: caSection, currency: curr, status: 'dr', segment1: arRev, type: 'arRevalue')
            createAccount(section: caSection, currency: curr, status: 'dr', segment1: prepay, type: 'prepay')
            cashAccount = createAccount(section: caSection, currency: curr, status: 'dr', segment1: cash, type: 'cash')
            bankAccount = createAccount(section: caSection, currency: curr, status: 'dr', segment1: bank, type: 'bank')
            bkRevAccount = createAccount(section: caSection, currency: curr, status: 'dr', segment1: bkRevalue, type: 'glRevalue')
            apAccount = createAccount(section: clSection, currency: curr, status: 'cr', segment1: ap, type: 'ap')
            apRevAccount = createAccount(section: clSection, currency: curr, status: 'cr', segment1: apRev, type: 'apRevalue')
            createAccount(section: clSection, currency: curr, status: 'cr', segment1: accrue, type: 'accrue')
            createAccount(section: clSection, currency: curr, status: 'cr', segment1: tax, type: 'tax')
            createAccount(section: clSection, currency: curr, status: 'cr', segment1: due)
            createAccount(section: nonSection, currency: curr, status: 'dr', segment1: revalue, type: 'fxRevalue')
            capitalAccount = createAccount(section: equitySection, currency: curr, status: 'cr', segment1: capital)
            retainedAccount = createAccount(section: equitySection, currency: curr, status: 'cr', segment1: retained, type: 'retained')
        }

        yield()

        def north, south, east, west, full

        // Access codes and groups
        Account.withTransaction {status ->
            north = new AccessCode(company: corp, code: 'north', name: 'Northern Region').saveThis()
            south = new AccessCode(company: corp, code: 'south', name: 'Southern Region').saveThis()
            east = new AccessCode(company: corp, code: 'east', name: 'Eastern Region').saveThis()
            west = new AccessCode(company: corp, code: 'west', name: 'Western Region').saveThis()
            full = new AccessGroup(company: corp, code: 'full', name: 'Full Access', element1: '*', element2: '*', element3: '*', element4: '*', customers: '*', suppliers: '*')
            full.addToUsers(companyUser)
            full.save(flush: true)
        }

        yield()

        def c100100, c100200, c100300, c100400, s100100, s100200, s100300, s100400

        // Customers and suppliers (with addresses and contacts)
        Account.withTransaction {status ->
            c100100 = new Customer(company: corp, taxCode: taxCode, country: ctry, currency: curr, accessCode: north, code: 'C100100', name: 'First Rate Enterprises').saveThis()
            c100200 = new Customer(company: corp, taxCode: taxCode, country: ctry, currency: curr, accessCode: south, code: 'C100200', name: 'Second Editions').saveThis()
            c100300 = new Customer(company: corp, taxCode: taxCode, country: ctry, currency: curr, accessCode: east, code: 'C100300', name: 'Third Man Services').saveThis()
            c100400 = new Customer(company: corp, taxCode: taxCode, country: ctry, currency: curr, accessCode: west, code: 'C100400', name: 'Fourth Protocol Classics').saveThis()

            s100100 = new Supplier(company: corp, taxCode: taxCode, country: ctry, currency: curr, accessCode: north, code: 'S100100', name: 'First Call Deliveries').saveThis()
            s100200 = new Supplier(company: corp, taxCode: taxCode, country: ctry, currency: curr, accessCode: south, code: 'S100200', name: 'Second Chance Services').saveThis()
            s100300 = new Supplier(company: corp, taxCode: taxCode, country: ctry, currency: curr, accessCode: east, code: 'S100300', name: 'Third Place Chemicals').saveThis()
            s100400 = new Supplier(company: corp, taxCode: taxCode, country: ctry, currency: curr, accessCode: west, code: 'S100400', name: 'Fourth Computing').saveThis()

            if (addressFormat) {
                def c100100Address = new CustomerAddress(customer: c100100, country: ctry, format: addressFormat,
                        location1: '27 Main Street', metro2: 'Stockport', area2: 'Cheshire').saveThis()
                def c100200Address = new CustomerAddress(customer: c100200, country: ctry, format: addressFormat,
                        location1: '16 London Road', metro2: 'Bradford', area2: 'Yorkshire').saveThis()
                def c100300Address = new CustomerAddress(customer: c100300, country: ctry, format: addressFormat,
                        location1: '102 Curling Avenue', metro2: 'Aldershot', area2: 'Hampshire').saveThis()
                def c100400Address = new CustomerAddress(customer: c100400, country: ctry, format: addressFormat,
                        location1: 'Tower House', metro2: 'Reading', area2: 'Berkshire').saveThis()
                createCustomerAddressUsages([c100100Address, c100200Address, c100300Address, c100400Address], customerAddressTypes)

                def c100100Contact = new CustomerContact(address: c100100Address, name: 'John Dean').saveThis()
                def c100200Contact = new CustomerContact(address: c100200Address, name: 'Frances Smith').saveThis()
                def c100300Contact = new CustomerContact(address: c100300Address, name: 'Peter Jones').saveThis()
                def c100400Contact = new CustomerContact(address: c100400Address, name: 'Louise Carter').saveThis()
                createCustomerContactUsages([c100100Contact, c100200Contact, c100300Contact, c100400Contact], customerContactTypes)

                def s100100Address = new SupplierAddress(supplier: s100100, country: ctry, format: addressFormat,
                        location1: 'Town Gate House', metro2: 'Bury', area2: 'Lancashire').saveThis()
                def s100200Address = new SupplierAddress(supplier: s100200, country: ctry, format: addressFormat,
                        location1: '107 Lake View Drive', metro2: 'Matlock', area2: 'Derbyshire').saveThis()
                def s100300Address = new SupplierAddress(supplier: s100300, country: ctry, format: addressFormat,
                        location1: '34 Castle Boulevard', metro2: 'Farnham', area2: 'Surrey').saveThis()
                def s100400Address = new SupplierAddress(supplier: s100400, country: ctry, format: addressFormat,
                        location1: '27 Coveringham Close', metro2: 'High Wycombe', area2: 'Buckinghamshire').saveThis()
                createSupplierAddressUsages([s100100Address, s100200Address, s100300Address, s100400Address], supplierAddressTypes)

                def s100100Contact = new SupplierContact(address: s100100Address, name: 'Jane Piper').saveThis()
                def s100200Contact = new SupplierContact(address: s100200Address, name: 'Howard Johnson').saveThis()
                def s100300Contact = new SupplierContact(address: s100300Address, name: 'Patricia Brown').saveThis()
                def s100400Contact = new SupplierContact(address: s100400Address, name: 'Mark Green').saveThis()
                createSupplierContactUsages([s100100Contact, s100200Contact, s100300Contact, s100400Contact], supplierContactTypes)
            }
        }

        yield()

        def cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        def yr = cal.get(Calendar.YEAR)
        def mth = cal.get(Calendar.MONTH) + 1
        def yearCode = yr.toString()
        if (mth > 1) {
            def nextYr = (yr + 1).toString()
            yearCode = yearCode + '/' + nextYr.substring(nextYr.length() - 2)
        }

        def year = new Year(company: corp, code: yearCode, validFrom: cal.getTime())
        def periods = []
        def pd, pdCode
        for (int i = 1; i <= 12; i++) {
            pd = new Period(validFrom: cal.getTime())
            if (i <= 2) pd.status = 'open'
            pd.code = (i < 10) ? "${yearCode}-0${i}" : "${yearCode}-${i}"
            cal.add(Calendar.MONTH, 1)
            pd.validTo = cal.getTime() - 1
            periods << pd
        }

        year.validTo = periods[-1].validTo

        yield()

        // Years and periods
        Account.withTransaction {status ->
            year.saveThis()
            for (period in periods) {
                year.addToPeriods(period)
            }

            year.save(flush: true)
            def accounts = Account.findAllBySecurityCode(corp.securityCode)
            for (account in accounts) {
                for (period in periods) {
                    new GeneralBalance(account: account, period: period).saveThis()
                }
            }

            accounts = Customer.findAllByCompany(corp)
            for (account in accounts) {
                for (period in periods) {
                    new CustomerTurnover(customer: account, period: period).saveThis()
                }
            }

            accounts = Supplier.findAllByCompany(corp)
            for (account in accounts) {
                for (period in periods) {
                    new SupplierTurnover(supplier: account, period: period).saveThis()
                }
            }
        }

        yield()

        // Set up auto-payments
        def docType = DocumentType.findByCompanyAndType(corp, SystemDocumentType.findByCode('BP'))
        if (docType) {
            docType.autoBankAccount = bankAccount
            docType.autoForeignCurrency = false
            docType.autoMaxPayees = 100
            Account.withTransaction {status ->
                docType.saveThis()
            }
        }

        yield()

        // Create some opening balances
        def description = 'Initial Balance'
        docType = DocumentType.findByCompanyAndType(corp, SystemDocumentType.findByCode('FJ'))
        if (docType) {
            def jnl = new Document(currency: curr, type: docType, period: periods[0], code: '000000', description: 'Initial Balances', documentDate: periods[0].validFrom, reference: 'Demo')
            def jnlLine = new Line(documentValue: 6239107.0, generalValue: 6239107.0, companyValue: 6239107.0, description: description)
            jnlLine.account = buildingsCostAccount
            jnl.addToLines(jnlLine)
            jnlLine = new Line(documentValue: 704219.0, generalValue: 704219.0, companyValue: 704219.0, description: description)
            jnlLine.account = fnfCostAccount
            jnl.addToLines(jnlLine)
            jnlLine = new Line(documentValue: 1138002.0, generalValue: 1138002.0, companyValue: 1138002.0, description: description)
            jnlLine.account = carsCostAccount
            jnl.addToLines(jnlLine)
            jnlLine = new Line(documentValue: 520798.0, generalValue: 520798.0, companyValue: 520798.0, description: description)
            jnlLine.account = inventoryAccount
            jnl.addToLines(jnlLine)
            jnlLine = new Line(documentValue: 5000.0, generalValue: 5000.0, companyValue: 5000.0, description: description)
            jnlLine.account = cashAccount
            jnl.addToLines(jnlLine)
            jnlLine = new Line(documentValue: 1392874.0, generalValue: 1392874.0, companyValue: 1392874.0, description: description)
            jnlLine.account = bankAccount
            jnl.addToLines(jnlLine)
            jnlLine = new Line(documentValue: -10000000.0, generalValue: -10000000.0, companyValue: -10000000.0, description: description)
            jnlLine.account = capitalAccount
            jnl.addToLines(jnlLine)

            postingService.post(jnl)
        }

        yield()

        // Create an Income and Expenditure report format
        def format = new ProfitReportFormat(company: corp, name: 'Current Month and Year To Date', title: 'Income & Expenditure Statement', subTitle: 'For Period {sPd}',
                column1Heading: 'Period', column1SubHeading: '{sPd}', column1PrimaryData: 'selectedPeriodAdjusted', column2Heading: '% of Sales', column2SubHeading: '{sPd}',
                column2PrimaryData: 'selectedPeriodAdjusted', column2Calculation: 'percentage', column3Heading: 'Cumulative', column3SubHeading: 'Pd To Date',
                column3PrimaryData: 'selectedYearAdjusted', column4Heading: '% of Sales', column4SubHeading: 'Pd To Date', column4PrimaryData: 'selectedYearAdjusted',
                column4Calculation: 'percentage')
        format.addToPercentages(new ProfitReportPercent(section: salesSection))
        format.addToLines(new ProfitReportLine(lineNumber: 100, section: salesSection))
        format.addToLines(new ProfitReportLine(lineNumber: 200, section: cosSection))
        format.addToLines(new ProfitReportLine(lineNumber: 300, text: 'Gross Profit', accumulation: '- 100, 200'))
        format.addToLines(new ProfitReportLine(lineNumber: 400, section: expensesSection))
        format.addToLines(new ProfitReportLine(lineNumber: 500, text: 'Net Profit', accumulation: '- 300, 400'))
        format.addToLines(new ProfitReportLine(lineNumber: 600, section: otherSection))
        format.addToLines(new ProfitReportLine(lineNumber: 700, text: 'Profit After Non-Trading Items', accumulation: '- 500, 600'))
        Account.withTransaction {status ->
            format.save()   // With deep validation
        }

        yield()

        // Create a Balance Sheet report format
        format = new BalanceReportFormat(company: corp, name: 'Balances and Period Movements', title: 'Balance Sheet', subTitle: 'For Period {sPd}', column3Heading: '{sPd}',
                column3SubHeading: 'Movements', column3PrimaryData: 'selectedPeriodMovement', column4Heading: 'Balances', column4SubHeading: 'Pd To Date', column4PrimaryData: 'selectedYearBalance')
        format.addToLines(new BalanceReportLine(lineNumber: 100, section: fixedSection))
        format.addToLines(new BalanceReportLine(lineNumber: 200))
        format.addToLines(new BalanceReportLine(lineNumber: 300, section: caSection))
        format.addToLines(new BalanceReportLine(lineNumber: 400, section: clSection))
        format.addToLines(new BalanceReportLine(lineNumber: 500, text: 'Net Current Assets', accumulation: '+ 300, 400'))
        format.addToLines(new BalanceReportLine(lineNumber: 600))
        format.addToLines(new BalanceReportLine(lineNumber: 700, section: nonSection))
        format.addToLines(new BalanceReportLine(lineNumber: 800))
        format.addToLines(new BalanceReportLine(lineNumber: 900, text: 'Assets Employed', accumulation: '+ 100, 500, 700'))
        format.addToLines(new BalanceReportLine(lineNumber: 1000, text: '<=>'))
        format.addToLines(new BalanceReportLine(lineNumber: 1100, text: 'Financed By:'))
        format.addToLines(new BalanceReportLine(lineNumber: 1200))
        format.addToLines(new BalanceReportLine(lineNumber: 1300, section: equitySection))
        format.addToLines(new BalanceReportLine(lineNumber: 1400, text: 'Capital Employed', accumulation: '- 1300'))
        Account.withTransaction {status ->
            format.save()   // With deep validation
        }
    }

    private createQuadAccounts(map) {
        def section = map.section
        def curr = map.currency
        def stat = map.status
        def s1List = map.segment1
        def s2List = map.segment2
        def s3List = map.segment3
        def s4List = map.segment4
        for (s1 in s1List) {
            for (s2 in s2List) {
                for (s3 in s3List) {
                    for (s4 in s4List) {
                        createAccount(section: section, currency: curr, status: stat, segment1: s1, segment2: s2, segment3: s3, segment4: s4)
                    }
                }
            }
        }
    }

    private createDualAccounts(map) {
        def section = map.section
        def curr = map.currency
        def stat = map.status
        def s1List = map.segment1
        def s2List = map.segment2
        for (s1 in s1List) {
            for (s2 in s2List) {
                createAccount(section: section, currency: curr, status: stat, segment1: s1, segment2: s2)
            }
        }
    }

    private createSingleAccounts(map) {
        def section = map.section
        def curr = map.currency
        def stat = map.status
        def s1List = map.segment1
        def results = []
        for (s1 in s1List) {
            results << createAccount(section: section, currency: curr, status: stat, segment1: s1)
        }

        return results
    }

    private createAccount(map) {
        def section = map.section
        def ac = new Account(section: section, currency: map.currency, status: map.status)
        if (map.type) ac.type = SystemAccountType.findByCode(map.type)
        def elementValue, code, name
        for (int i = 1; i < 9; i++) {
            elementValue = map."segment${i}"
            if (!elementValue) break
            ac."element${elementValue.element.elementNumber}" = elementValue
            if (i == 1) {
                code = elementValue.code
                name = elementValue.name
            } else {
                code = code + '-' + elementValue.code
                name = name + ', ' + elementValue.shortName
            }
        }

        ac.code = code
        ac.name = name
        return ac.saveThis()
    }

    private createCustomerAddressUsages(addresses, types) {
        for (address in addresses) {
            for (type in types) {
                new CustomerAddressUsage(customer: address.customer, address: address, type: type).saveThis()
            }
        }
    }

    private createCustomerContactUsages(contacts, types) {
        for (contact in contacts) {
            for (type in types) {
                new CustomerContactUsage(address: contact.address, contact: contact, type: type).saveThis()
            }
        }
    }

    private createSupplierAddressUsages(addresses, types) {
        for (address in addresses) {
            for (type in types) {
                new SupplierAddressUsage(supplier: address.supplier, address: address, type: type).saveThis()
            }
        }
    }

    private createSupplierContactUsages(contacts, types) {
        for (contact in contacts) {
            for (type in types) {
                new SupplierContactUsage(address: contact.address, contact: contact, type: type).saveThis()
            }
        }
    }
}
