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

import org.apache.log4j.Logger
import org.grails.tlc.corp.*

class InitialSystemData {

    def loadSystem(servletContext, appVersion) {

        // Load data used in all environments
		println "Tables and indexes created"
		println "Loading system settings"
        loadSystemSettings(appVersion)
		println "Loading language definitions"
        loadSystemLanguages()
		println "Loading currency information"
        loadSystemCurrencies()
		println "Loading address and contact formats"
        loadSystemAddressFormats()
        loadSystemCustomerAddressTypes()
        loadSystemSupplierAddressTypes()
        loadSystemCustomerContactTypes()
        loadSystemSupplierContactTypes()
		println "Loading Geo, Region and Country settings"
        loadSystemGeos()
        loadSystemRegions()
        loadSystemCountries()
		println "Loading Measures, Scales and Units"
        loadSystemMeasures()
        loadSystemScales()
        loadSystemUnits()
        loadSystemConversions()
		println "Loading basic accounting structures"
        loadSystemPaymentSchedules()
        loadSystemDocumentTypes()
        loadSystemAccountTypes()
		println "Loading security data"
        loadSystemMenus()
        loadSystemRoles()
        loadSystemUsers()
		println "Creating the System company"
        loadCompanies()
        loadTasks()
		println "Loading messages and texts"
        loadMessages(servletContext)
        loadPageHelp(servletContext)

		println "Initializing the System company's data"
        initializeCompanyData() // Must be last
		println "Installation complete"
		println ""
    }

    private initializeCompanyData() {
        def company = Company.findBySystemOnly(true)
        def user = SystemUser.findByLoginId('system')
        def currency = SystemCurrency.findByCode(UtilService.BASE_CURRENCY_CODE)
        def result = company.initializeData(user, currency)
        if (result != null) Logger.getLogger(InitialSystemData).error("Error loading the initial company data for ${result}")
    }

    private loadSystemSettings(appVersion) {
        new SystemSetting(code: 'dataVersion', dataType: 'string', value: appVersion, systemOnly: true).saveThis()
        new SystemSetting(code: 'isDemoSystem', dataType: 'boolean', value: 'false', systemOnly: true).saveThis()
        new SystemSetting(code: 'pagination.max', dataType: 'integer', value: '50', systemOnly: false).saveThis()
        new SystemSetting(code: 'pagination.default', dataType: 'integer', value: '20', systemOnly: false).saveThis()
        new SystemSetting(code: 'customer.settlement.days', dataType: 'integer', value: '30', systemOnly: false).saveThis()
        new SystemSetting(code: 'customer.settlement.periodic', dataType: 'boolean', value: 'false', systemOnly: false).saveThis()
        new SystemSetting(code: 'customer.age.days.1', dataType: 'integer', value: '30', systemOnly: false).saveThis()
        new SystemSetting(code: 'customer.age.days.2', dataType: 'integer', value: '60', systemOnly: false).saveThis()
        new SystemSetting(code: 'customer.age.days.3', dataType: 'integer', value: '90', systemOnly: false).saveThis()
        new SystemSetting(code: 'supplier.settlement.days', dataType: 'integer', value: '30', systemOnly: false).saveThis()
        new SystemSetting(code: 'supplier.settlement.periodic', dataType: 'boolean', value: 'false', systemOnly: false).saveThis()
        new SystemSetting(code: 'supplier.age.days.1', dataType: 'integer', value: '30', systemOnly: false).saveThis()
        new SystemSetting(code: 'supplier.age.days.2', dataType: 'integer', value: '60', systemOnly: false).saveThis()
        new SystemSetting(code: 'supplier.age.days.3', dataType: 'integer', value: '90', systemOnly: false).saveThis()
        new SystemSetting(code: 'supplier.dataEntry.fxDiff.allowed', dataType: 'boolean', value: 'false', systemOnly: false).saveThis()
        new SystemSetting(code: 'customer.dataEntry.fxDiff.allowed', dataType: 'boolean', value: 'false', systemOnly: false).saveThis()
        new SystemSetting(code: 'supplier.dataEntry.fxDiff.percent', dataType: 'integer', value: '5', systemOnly: false).saveThis()
        new SystemSetting(code: 'customer.dataEntry.fxDiff.percent', dataType: 'integer', value: '5', systemOnly: false).saveThis()
        new SystemSetting(code: 'statements.use.posting.date.cutoff', dataType: 'boolean', value: 'true', systemOnly: false).saveThis()
        new SystemSetting(code: 'statements.use.document.date.cutoff', dataType: 'boolean', value: 'false', systemOnly: false).saveThis()
        new SystemSetting(code: 'statement.retention.months', dataType: 'integer', value: '12', systemOnly: false).saveThis()
        new SystemSetting(code: 'remittance.retention.months', dataType: 'integer', value: '12', systemOnly: false).saveThis()
        new SystemSetting(code: 'reconciliation.retention.months', dataType: 'integer', value: '12', systemOnly: false).saveThis()
        new SystemSetting(code: 'tax.statement.retention.months', dataType: 'integer', value: '12', systemOnly: false).saveThis()
    }

    private loadSystemLanguages() {
        createLanguage(name: 'Abkhazian', code: 'ab')
        createLanguage(name: 'Afar', code: 'aa')
        createLanguage(name: 'Afrikaans', code: 'af')
        createLanguage(name: 'Albanian', code: 'sq')
        createLanguage(name: 'Amharic', code: 'am')
        createLanguage(name: 'Arabic', code: 'ar')
        createLanguage(name: 'Armenian', code: 'hy')
        createLanguage(name: 'Assamese', code: 'as')
        createLanguage(name: 'Aymara', code: 'ay')
        createLanguage(name: 'Azerbaijani', code: 'az')
        createLanguage(name: 'Bashkir', code: 'ba')
        createLanguage(name: 'Basque', code: 'eu')
        createLanguage(name: 'Bengali', code: 'bn')
        createLanguage(name: 'Bhutani', code: 'dz')
        createLanguage(name: 'Bihari', code: 'bh')
        createLanguage(name: 'Bislama', code: 'bi')
        createLanguage(name: 'Breton', code: 'br')
        createLanguage(name: 'Bulgarian', code: 'bg')
        createLanguage(name: 'Burmese', code: 'my')
        createLanguage(name: 'Byelorussian', code: 'be')
        createLanguage(name: 'Cambodian', code: 'km')
        createLanguage(name: 'Catalan', code: 'ca')
        createLanguage(name: 'Chinese', code: 'zh')
        createLanguage(name: 'Corsican', code: 'co')
        createLanguage(name: 'Croatian', code: 'hr')
        createLanguage(name: 'Czech', code: 'cs')
        createLanguage(name: 'Danish', code: 'da')
        createLanguage(name: 'Dutch', code: 'nl')
        createLanguage(name: 'English', code: 'en')
        createLanguage(name: 'Esperanto', code: 'eo')
        createLanguage(name: 'Estonian', code: 'et')
        createLanguage(name: 'Faeroese', code: 'fo')
        createLanguage(name: 'Farsi', code: 'fa')
        createLanguage(name: 'Fiji', code: 'fj')
        createLanguage(name: 'Finnish', code: 'fi')
        createLanguage(name: 'French', code: 'fr')
        createLanguage(name: 'Frisian', code: 'fy')
        createLanguage(name: 'Galician', code: 'gl')
        createLanguage(name: 'Gaelic (Scottish)', code: 'gd')
        createLanguage(name: 'Gaelic (Manx)', code: 'gv')
        createLanguage(name: 'Georgian', code: 'ka')
        createLanguage(name: 'German', code: 'de')
        createLanguage(name: 'Greek', code: 'el')
        createLanguage(name: 'Greenlandic', code: 'kl')
        createLanguage(name: 'Guarani', code: 'gn')
        createLanguage(name: 'Gujarati', code: 'gu')
        createLanguage(name: 'Hausa', code: 'ha')
        createLanguage(name: 'Hebrew', code: 'he')
        createLanguage(name: 'Hindi', code: 'hi')
        createLanguage(name: 'Hungarian', code: 'hu')
        createLanguage(name: 'Icelandic', code: 'is')
        createLanguage(name: 'Indonesian', code: 'id')
        createLanguage(name: 'Interlingua', code: 'ia')
        createLanguage(name: 'Interlingue', code: 'ie')
        createLanguage(name: 'Inuktitut', code: 'iu')
        createLanguage(name: 'Inupiak', code: 'ik')
        createLanguage(name: 'Irish', code: 'ga')
        createLanguage(name: 'Italian', code: 'it')
        createLanguage(name: 'Japanese', code: 'ja')
        createLanguage(name: 'Javanese', code: 'jv')
        createLanguage(name: 'Kannada', code: 'kn')
        createLanguage(name: 'Kashmiri', code: 'ks')
        createLanguage(name: 'Kazakh', code: 'kk')
        createLanguage(name: 'Kinyarwanda', code: 'rw')
        createLanguage(name: 'Kirghiz', code: 'ky')
        createLanguage(name: 'Kirundi', code: 'rn')
        createLanguage(name: 'Korean', code: 'ko')
        createLanguage(name: 'Kurdish', code: 'ku')
        createLanguage(name: 'Laothian', code: 'lo')
        createLanguage(name: 'Latin', code: 'la')
        createLanguage(name: 'Latvian', code: 'lv')
        createLanguage(name: 'Limburgish', code: 'li')
        createLanguage(name: 'Lingala', code: 'ln')
        createLanguage(name: 'Lithuanian', code: 'lt')
        createLanguage(name: 'Macedonian', code: 'mk')
        createLanguage(name: 'Malagasy', code: 'mg')
        createLanguage(name: 'Malay', code: 'ms')
        createLanguage(name: 'Malayalam', code: 'ml')
        createLanguage(name: 'Maltese', code: 'mt')
        createLanguage(name: 'Maori', code: 'mi')
        createLanguage(name: 'Marathi', code: 'mr')
        createLanguage(name: 'Moldavian', code: 'mo')
        createLanguage(name: 'Mongolian', code: 'mn')
        createLanguage(name: 'Nauru', code: 'na')
        createLanguage(name: 'Nepali', code: 'ne')
        createLanguage(name: 'Norwegian', code: 'nb')
        createLanguage(name: 'Occitan', code: 'oc')
        createLanguage(name: 'Oriya', code: 'or')
        createLanguage(name: 'Oromo', code: 'om')
        createLanguage(name: 'Pashto', code: 'ps')
        createLanguage(name: 'Polish', code: 'pl')
        createLanguage(name: 'Portuguese', code: 'pt')
        createLanguage(name: 'Punjabi', code: 'pa')
        createLanguage(name: 'Quechua', code: 'qu')
        createLanguage(name: 'Rhaeto-Romance', code: 'rm')
        createLanguage(name: 'Romanian', code: 'ro')
        createLanguage(name: 'Russian', code: 'ru')
        createLanguage(name: 'Samoan', code: 'sm')
        createLanguage(name: 'Sangro', code: 'sg')
        createLanguage(name: 'Sanskrit', code: 'sa')
        createLanguage(name: 'Serbian', code: 'sr')
        createLanguage(name: 'Serbo-Croatian', code: 'sh')
        createLanguage(name: 'Sesotho', code: 'st')
        createLanguage(name: 'Setswana', code: 'tn')
        createLanguage(name: 'Shona', code: 'sn')
        createLanguage(name: 'Sindhi', code: 'sd')
        createLanguage(name: 'Sinhalese', code: 'si')
        createLanguage(name: 'Siswati', code: 'ss')
        createLanguage(name: 'Slovak', code: 'sk')
        createLanguage(name: 'Slovenian', code: 'sl')
        createLanguage(name: 'Somali', code: 'so')
        createLanguage(name: 'Spanish', code: 'es')
        createLanguage(name: 'Sundanese', code: 'su')
        createLanguage(name: 'Swahili', code: 'sw')
        createLanguage(name: 'Swedish', code: 'sv')
        createLanguage(name: 'Tagalog', code: 'tl')
        createLanguage(name: 'Tajik', code: 'tg')
        createLanguage(name: 'Tamil', code: 'ta')
        createLanguage(name: 'Tatar', code: 'tt')
        createLanguage(name: 'Telugu', code: 'te')
        createLanguage(name: 'Thai', code: 'th')
        createLanguage(name: 'Tibetan', code: 'bo')
        createLanguage(name: 'Tigrinya', code: 'ti')
        createLanguage(name: 'Tonga', code: 'to')
        createLanguage(name: 'Tsonga', code: 'ts')
        createLanguage(name: 'Turkish', code: 'tr')
        createLanguage(name: 'Turkmen', code: 'tk')
        createLanguage(name: 'Twi', code: 'tw')
        createLanguage(name: 'Uighur', code: 'ug')
        createLanguage(name: 'Ukrainian', code: 'uk')
        createLanguage(name: 'Urdu', code: 'ur')
        createLanguage(name: 'Uzbek', code: 'uz')
        createLanguage(name: 'Vietnamese', code: 'vi')
        createLanguage(name: 'Volapuk', code: 'vo')
        createLanguage(name: 'Welsh', code: 'cy')
        createLanguage(name: 'Wolof', code: 'wo')
        createLanguage(name: 'Xhosa', code: 'xh')
        createLanguage(name: 'Yiddish', code: 'yi')
        createLanguage(name: 'Yoruba', code: 'yo')
        createLanguage(name: 'Zulu', code: 'zu')
    }

    private createLanguage(map) {
        new SystemLanguage(map).saveThis()
        new SystemMessage(code: "language.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemCurrencies() {
        createCurrency(name: 'Afghanistan Afghani', code: 'AFN', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Albanian Lek', code: 'ALL', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Algerian Dinar', code: 'DZD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Angolan Kwanza', code: 'AOA', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Argentine Peso', code: 'ARS', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Armenian Dram', code: 'AMD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Aruban Guilder', code: 'AWG', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Australian Dollar', code: 'AUD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Azerbaijanian New Manat', code: 'AZN', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Bahamian Dollar', code: 'BSD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Bahraini Dinar', code: 'BHD', decimals: 3, autoUpdate: true)
        createCurrency(name: 'Bangladeshi Taka', code: 'BDT', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Barbados Dollar', code: 'BBD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Belarusian Ruble', code: 'BYR', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Belize Dollar', code: 'BZD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Bermudian Dollar', code: 'BMD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Bhutan Ngultrum', code: 'BTN', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Bolivian Boliviano', code: 'BOB', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Botswana Pula', code: 'BWP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Brazilian Real', code: 'BRL', decimals: 2, autoUpdate: true)
        createCurrency(name: 'British Pound', code: 'GBP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Brunei Dollar', code: 'BND', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Bulgarian Lev', code: 'BGN', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Burundi Franc', code: 'BIF', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Cambodian Riel', code: 'KHR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Canadian Dollar', code: 'CAD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Cape Verde Escudo', code: 'CVE', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Cayman Islands Dollar', code: 'KYD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'CFA Franc BCEAO', code: 'XOF', decimals: 0, autoUpdate: true)
        createCurrency(name: 'CFA Franc BEAC', code: 'XAF', decimals: 0, autoUpdate: true)
        createCurrency(name: 'CFP Franc', code: 'XPF', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Chilean Peso', code: 'CLP', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Chinese Yuan Renminbi', code: 'CNY', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Colombian Peso', code: 'COP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Comoros Franc', code: 'KMF', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Congolese Franc', code: 'CDF', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Costa Rican Colon', code: 'CRC', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Croatian Kuna', code: 'HRK', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Cuban Peso', code: 'CUP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Czech Koruna', code: 'CZK', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Danish Krone', code: 'DKK', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Djibouti Franc', code: 'DJF', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Dominican Peso', code: 'DOP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'East Caribbean Dollar', code: 'XCD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Egyptian Pound', code: 'EGP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Eritrean Nakfa', code: 'ERN', decimals: 2, autoUpdate: false)
        createCurrency(name: 'Ethiopian Birr', code: 'ETB', decimals: 2, autoUpdate: true)
        createCurrency(name: 'EU Euro', code: 'EUR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Falkland Islands Pound', code: 'FKP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Fiji Dollar', code: 'FJD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Gambian Dalasi', code: 'GMD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Georgian Lari', code: 'GEL', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Ghanaian New Cedi', code: 'GHS', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Gibraltar Pound', code: 'GIP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Guatemalan Quetzal', code: 'GTQ', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Guinean Franc', code: 'GNF', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Guyana Dollar', code: 'GYD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Haitian Gourde', code: 'HTG', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Honduran Lempira', code: 'HNL', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Hong Kong Dollar', code: 'HKD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Hungarian Forint', code: 'HUF', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Icelandic Krona', code: 'ISK', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Indian Rupee', code: 'INR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Indonesian Rupiah', code: 'IDR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Iranian Rial', code: 'IRR', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Iraqi Dinar', code: 'IQD', decimals: 3, autoUpdate: true)
        createCurrency(name: 'Israeli New Shekel', code: 'ILS', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Jamaican Dollar', code: 'JMD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Japanese Yen', code: 'JPY', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Jordanian Dinar', code: 'JOD', decimals: 3, autoUpdate: true)
        createCurrency(name: 'Kazakh Tenge', code: 'KZT', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Kenyan Shilling', code: 'KES', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Kuwaiti Dinar', code: 'KWD', decimals: 3, autoUpdate: true)
        createCurrency(name: 'Kyrgyz Som', code: 'KGS', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Lao Kip', code: 'LAK', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Latvian Lats', code: 'LVL', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Lebanese Pound', code: 'LBP', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Lesotho Loti', code: 'LSL', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Liberian Dollar', code: 'LRD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Libyan Dinar', code: 'LYD', decimals: 3, autoUpdate: true)
        createCurrency(name: 'Lithuanian Litas', code: 'LTL', decimals: 2, autoUpdate: true)
        createCurrency(name: 'MacaoPataca', code: 'MOP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Macedonian Denar', code: 'MKD', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Malagasy Ariary', code: 'MGA', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Malawi Kwacha', code: 'MWK', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Malaysian Ringgit', code: 'MYR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Maldivian Rufiyaa', code: 'MVR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Mauritanian Ouguiya', code: 'MRO', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Mauritius Rupee', code: 'MUR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Mexican Peso', code: 'MXN', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Moldovan Leu', code: 'MDL', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Mongolian Tugrik', code: 'MNT', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Moroccan Dirham', code: 'MAD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Mozambique New Metical', code: 'MZN', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Myanmar Kyat', code: 'MMK', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Namibian Dollar', code: 'NAD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Nepalese Rupee', code: 'NPR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Netherlands Antillian Guilder', code: 'ANG', decimals: 2, autoUpdate: true)
        createCurrency(name: 'New Zealand Dollar', code: 'NZD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Nicaraguan Cordoba Oro', code: 'NIO', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Nigerian Naira', code: 'NGN', decimals: 2, autoUpdate: true)
        createCurrency(name: 'North Korean Won', code: 'KPW', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Norwegian Krone', code: 'NOK', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Omani Rial', code: 'OMR', decimals: 3, autoUpdate: true)
        createCurrency(name: 'Pakistani Rupee', code: 'PKR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Panamanian Balboa', code: 'PAB', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Papua New Guinea Kina', code: 'PGK', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Paraguayan Guarani', code: 'PYG', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Peruvian Nuevo Sol', code: 'PEN', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Philippine Peso', code: 'PHP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Polish Zloty', code: 'PLN', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Qatari Rial', code: 'QAR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Romanian New Leu', code: 'RON', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Russian Ruble', code: 'RUB', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Rwandan Franc', code: 'RWF', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Saint Helena Pound', code: 'SHP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Samoan Tala', code: 'WST', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Sao Tome and Principe Dobra', code: 'STD', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Saudi Riyal', code: 'SAR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Serbian Dinar', code: 'RSD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Seychelles Rupee', code: 'SCR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Sierra Leone Leone', code: 'SLL', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Singapore Dollar', code: 'SGD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Solomon Islands Dollar', code: 'SBD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Somali Shilling', code: 'SOS', decimals: 2, autoUpdate: true)
        createCurrency(name: 'South African Rand', code: 'ZAR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'South Korean Won', code: 'KRW', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Sri Lanka Rupee', code: 'LKR', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Sudanese Pound', code: 'SDG', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Suriname Dollar', code: 'SRD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Swaziland Lilangeni', code: 'SZL', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Swedish Krona', code: 'SEK', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Swiss Franc', code: 'CHF', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Syrian Pound', code: 'SYP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Taiwan New Dollar', code: 'TWD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Tajik Somoni', code: 'TJS', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Tanzanian Shilling', code: 'TZS', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Thai Baht', code: 'THB', decimals: 2, autoUpdate: true)
        createCurrency(name: "Tongan Pa'anga", code: 'TOP', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Trinidad and Tobago Dollar', code: 'TTD', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Tunisian Dinar', code: 'TND', decimals: 3, autoUpdate: true)
        createCurrency(name: 'Turkish Lira', code: 'TRY', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Turkmen Manat', code: 'TMT', decimals: 2, autoUpdate: true)
        createCurrency(name: 'UAE Dirham', code: 'AED', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Uganda New Shilling', code: 'UGX', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Ukrainian Hryvnia', code: 'UAH', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Uruguayan Peso', code: 'UYU', decimals: 2, autoUpdate: true)
        createCurrency(name: 'US Dollar', code: 'USD', decimals: 2, autoUpdate: false)
        createCurrency(name: 'Uzbekistani Sum', code: 'UZS', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Vanuatu Vatu', code: 'VUV', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Venezuelan Bolivar Fuerte', code: 'VEF', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Vietnamese Dong', code: 'VND', decimals: 0, autoUpdate: true)
        createCurrency(name: 'Yemeni Rial', code: 'YER', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Zambian Kwacha', code: 'ZMK', decimals: 2, autoUpdate: true)
        createCurrency(name: 'Convertible Marks', code: 'BAM', decimals: 2, autoUpdate: true)
    }

    private createCurrency(map) {
        new SystemCurrency(map).saveThis()
        new SystemMessage(code: "currency.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemAddressFormats() {
        createAddressFormat(code: 'default', name: 'Default Address Format', field1: 'contact', field2: 'identifier', field3: 'company',
                field4: 'location1', field4Prompt1: 'location1', width4: 30, mandatory4: true,
                field5: 'location2', field5Prompt1: 'location2', width5: 30, mandatory5: false,
                field6: 'metro2', field6Prompt1: 'city', field6Prompt2: 'town', width6: 20, mandatory6: true,
                field7: 'area2', field7Prompt1: 'state', field7Prompt2: 'province', field7Prompt3: 'region', width7: 20, mandatory7: true,
                field8: 'encoding', field8Prompt1: 'zip', field8Prompt2: 'postalcode', width8: 10, mandatory8: false,
                field9: 'country')
        createAddressFormat(code: 'GB', name: 'UK Address Format', field1: 'contact', field2: 'identifier', field3: 'company',
                field4: 'location1', field4Prompt1: 'location1', width4: 30, mandatory4: true,
                field5: 'location2', field5Prompt1: 'location2', width5: 30, mandatory5: false,
                field6: 'metro2', field6Prompt1: 'town', width6: 20, mandatory6: true,
                field7: 'area2', field7Prompt1: 'county', width7: 20, mandatory7: false,
                field8: 'encoding', field8Prompt1: 'postcode', width8: 10, mandatory8: true, pattern8: '(([A-Z]{1,2}[0-9]{1,2}|[A-Z]{1,2}[0-9][A-Z]) [0-9][A-Z]{1,2})|GIR 0AA',
                field9: 'country')
        createAddressFormat(code: 'US', name: 'US Address Format', field1: 'contact', field2: 'identifier', field3: 'company',
                field4: 'location1', field4Prompt1: 'location1', width4: 30, mandatory4: true,
                field5: 'location2', field5Prompt1: 'location2', width5: 30, mandatory5: false,
                field6: 'metro2', field6Prompt1: 'city', width6: 20, mandatory6: true,
                field7: 'area2', field7Prompt1: 'state', width7: 2, mandatory7: true, joinBy7: '_', pattern7: '[A-Z][A-Z]',
                field8: 'encoding', field8Prompt1: 'zip', width8: 10, mandatory8: true, joinBy8: '_', pattern8: '[0-9]{5}(-[0-9]{4})?',
                field9: 'country')
        createAddressFormat(code: 'CA', name: 'Canadian Address Format', field1: 'contact', field2: 'identifier', field3: 'company',
                field4: 'location1', field4Prompt1: 'location1', width4: 30, mandatory4: true,
                field5: 'location2', field5Prompt1: 'location2', width5: 30, mandatory5: false,
                field6: 'metro2', field6Prompt1: 'city', field6Prompt2: 'municipality', width6: 20, mandatory6: true,
                field7: 'area2', field7Prompt1: 'province', width7: 2, mandatory7: true, joinBy7: '_', pattern7: '[A-Z][A-Z]',
                field8: 'encoding', field8Prompt1: 'postalcode', width8: 10, mandatory8: true, joinBy8: '__', pattern8: '[A-Z][0-9][A-Z] [0-9][A-Z][0-9]',
                field9: 'country')
        createAddressFormat(code: 'AU', name: 'Australian Address Format', field1: 'contact', field2: 'identifier', field3: 'company',
                field4: 'location1', field4Prompt1: 'location1', width4: 30, mandatory4: true,
                field5: 'location2', field5Prompt1: 'location2', width5: 30, mandatory5: false,
                field6: 'metro2', field6Prompt1: 'place', field6Prompt2: 'suburb', field6Prompt3: 'locality', width6: 20, mandatory6: true,
                field7: 'area2', field7Prompt1: 'state', field7Prompt2: 'territory', width7: 3, mandatory7: true, pattern7: '[A-Z]{2,3}', joinBy7: '__',
                field8: 'encoding', field8Prompt1: 'postcode', width8: 4, mandatory8: true, pattern8: '[0-9]{4}', joinBy8: '__',
                field9: 'country')
        createAddressFormat(code: 'FR', name: 'French Address Format', field1: 'contact', field2: 'identifier', field3: 'company',
                field4: 'location1', field4Prompt1: 'location1', width4: 30, mandatory4: true,
                field5: 'location2', field5Prompt1: 'location2', width5: 30, mandatory5: false,
                field6: 'metro1', field6Prompt1: 'locality', field6Prompt2: 'place', width6: 20, mandatory6: false,
                field7: 'encoding', field7Prompt1: 'postcode', width7: 5, mandatory7: true, pattern7: '[0-9]{5}',
                field8: 'metro2', field8Prompt1: 'city', width8: 20, mandatory8: true, joinBy8: '_',
                field9: 'country')
        createAddressFormat(code: 'DE', name: 'German Address Format', field1: 'company', field2: 'identifier', field3: 'contact',
                field4: 'metro1', field4Prompt1: 'district', width4: 20, mandatory4: false,
                field5: 'location1', field5Prompt1: 'location1', width5: 30, mandatory5: true,
                field6: 'encoding', field6Prompt1: 'postcode', width6: 5, mandatory6: true, pattern6: '[0-9]{5}',
                field7: 'metro2', field7Prompt1: 'city', field7Prompt2: 'place', width7: 20, mandatory7: true, joinBy7: '_',
                field8: 'country')
        createAddressFormat(code: 'NZ', name: 'New Zealand Address Format', field1: 'contact', field2: 'identifier', field3: 'company',
                field4: 'location1', field4Prompt1: 'location1', width4: 30, mandatory4: true,
                field5: 'location2', field5Prompt1: 'location2', width5: 30, mandatory5: false,
                field6: 'metro1', field6Prompt1: 'suburb', width6: 20, mandatory6: false,
                field7: 'metro2', field7Prompt1: 'town', field7Prompt2: 'city', width7: 20, mandatory7: true,
                field8: 'encoding', field8Prompt1: 'postcode', width8: 4, mandatory8: true, pattern8: '[0-9]{4}', joinBy8: '_',
                field9: 'country')
    }

    private createAddressFormat(map) {
        new SystemAddressFormat(map).saveThis()
        new SystemMessage(code: "systemAddressFormat.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemCustomerAddressTypes() {
        createCustomerAddressType(code: 'default', name: 'Default Address')
        createCustomerAddressType(code: 'statement', name: 'Statement Address')
    }

    private createCustomerAddressType(map) {
        new SystemCustomerAddressType(map).saveThis()
        new SystemMessage(code: "customerAddressType.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemSupplierAddressTypes() {
        createSupplierAddressType(code: 'default', name: 'Default Address')
        createSupplierAddressType(code: 'remittance', name: 'Remittance Address')
    }

    private createSupplierAddressType(map) {
        new SystemSupplierAddressType(map).saveThis()
        new SystemMessage(code: "supplierAddressType.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemCustomerContactTypes() {
        createCustomerContactType(code: 'statement', name: 'Statement Contact')
    }

    private createCustomerContactType(map) {
        new SystemCustomerContactType(map).saveThis()
        new SystemMessage(code: "customerContactType.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemSupplierContactTypes() {
        createSupplierContactType(code: 'remittance', name: 'Remittance Contact')
    }

    private createSupplierContactType(map) {
        new SystemSupplierContactType(map).saveThis()
        new SystemMessage(code: "supplierContactType.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemGeos() {
        createGeo(name: 'Africa', code: '002')
        createGeo(name: 'Asia', code: '142')
        createGeo(name: 'Europe', code: '150')
        createGeo(name: 'Latin America and Caribbean', code: '419')
        createGeo(name: 'Northern America', code: '021')
        createGeo(name: 'Oceania', code: '009')
    }

    private createGeo(map) {
        new SystemGeo(map).saveThis()
        new SystemMessage(code: "geo.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemRegions() {
        createRegion([name: 'Eastern Africa', code: '014'], '002')
        createRegion([name: 'Middle Africa', code: '017'], '002')
        createRegion([name: 'Northern Africa', code: '015'], '002')
        createRegion([name: 'Southern Africa', code: '018'], '002')
        createRegion([name: 'Western Africa', code: '011'], '002')
        createRegion([name: 'Eastern Asia', code: '030'], '142')
        createRegion([name: 'Southern Asia', code: '034'], '142')
        createRegion([name: 'South-Eastern Asia', code: '035'], '142')
        createRegion([name: 'Western Asia', code: '145'], '142')
        createRegion([name: 'Central Asia', code: '143'], '142')
        createRegion([name: 'Eastern Europe', code: '151'], '150')
        createRegion([name: 'Northern Europe', code: '154'], '150')
        createRegion([name: 'Southern Europe', code: '039'], '150')
        createRegion([name: 'Western Europe', code: '155'], '150')
        createRegion([name: 'Caribbean', code: '029'], '419')
        createRegion([name: 'Central America', code: '013'], '419')
        createRegion([name: 'South America', code: '005'], '419')
        createRegion([name: 'North America', code: '021'], '021')
        createRegion([name: 'Australia and New Zealand', code: '053'], '009')
        createRegion([name: 'Melanesia', code: '054'], '009')
        createRegion([name: 'Micronesia', code: '057'], '009')
        createRegion([name: 'Polynesia', code: '061'], '009')
    }

    private createRegion(map, geo) {
        def region = new SystemRegion(map)
        region.geo = SystemGeo.findByCode(geo)
        region.saveThis()
        new SystemMessage(code: "region.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemCountries() {
        createCountry([name: 'Afghanistan', code: 'AF', flag: 'AF'], '034', 'ps', 'AFN')
        createCountry([name: 'Aland Islands', code: 'AX', flag: 'AX'], '154', 'sv', 'EUR')
        createCountry([name: 'Albania', code: 'AL', flag: 'AL'], '039', 'sq', 'ALL')
        createCountry([name: 'Algeria', code: 'DZ', flag: 'DZ'], '015', 'ar', 'DZD')
        createCountry([name: 'American Samoa', code: 'AS', flag: 'AS'], '061', 'sm', 'USD')
        createCountry([name: 'Andorra', code: 'AD', flag: 'AD'], '039', 'ca', 'EUR')
        createCountry([name: 'Angola', code: 'AO', flag: 'AO'], '017', 'pt', 'AOA')
        createCountry([name: 'Anguilla', code: 'AI', flag: 'AI'], '029', 'en', 'XCD')
        createCountry([name: 'Antigua and Barbuda', code: 'AG', flag: 'AG'], '029', 'en', 'XCD')
        createCountry([name: 'Argentina', code: 'AR', flag: 'AR'], '005', 'es', 'ARS')
        createCountry([name: 'Armenia', code: 'AM', flag: 'AM'], '145', 'hy', 'AMD')
        createCountry([name: 'Aruba', code: 'AW', flag: 'AW'], '029', 'en', 'AWG')
        createCountry([name: 'Australia', code: 'AU', flag: 'AU', addressFormat: 'AU'], '053', 'en', 'AUD')
        createCountry([name: 'Austria', code: 'AT', flag: 'AT'], '155', 'de', 'EUR')
        createCountry([name: 'Azerbaijan', code: 'AZ', flag: 'AZ'], '145', 'az', 'AZN')
        createCountry([name: 'Bahamas', code: 'BS', flag: 'BS'], '029', 'en', 'BSD')
        createCountry([name: 'Bahrain', code: 'BH', flag: 'BH'], '145', 'ar', 'BHD')
        createCountry([name: 'Bangladesh', code: 'BD', flag: 'BD'], '034', 'bn', 'BDT')
        createCountry([name: 'Barbados', code: 'BB', flag: 'BB'], '029', 'en', 'BBD')
        createCountry([name: 'Belarus', code: 'BY', flag: 'BY'], '151', 'be', 'BYR')
        createCountry([name: 'Belgium', code: 'BE', flag: 'BE'], '155', 'nl', 'EUR')
        createCountry([name: 'Belize', code: 'BZ', flag: 'BZ'], '013', 'en', 'BZD')
        createCountry([name: 'Benin', code: 'BJ', flag: 'BJ'], '011', 'fr', 'XOF')
        createCountry([name: 'Bermuda', code: 'BM', flag: 'BM'], '021', 'en', 'BMD')
        createCountry([name: 'Bhutan', code: 'BT', flag: 'BT'], '034', 'bo', 'BTN')
        createCountry([name: 'Bolivia', code: 'BO', flag: 'BO'], '005', 'es', 'BOB')
        createCountry([name: 'Bosnia and Herzegovina', code: 'BA', flag: 'BA'], '039', 'hr', 'BAM')
        createCountry([name: 'Botswana', code: 'BW', flag: 'BW'], '018', 'tn', 'BWP')
        createCountry([name: 'Brazil', code: 'BR', flag: 'BR'], '005', 'pt', 'BRL')
        createCountry([name: 'British Indian Ocean Territory', code: 'IO', flag: 'IO'], '034', 'en', 'USD')
        createCountry([name: 'Brunei Darussalam', code: 'BN', flag: 'BN'], '035', 'ms', 'BND')
        createCountry([name: 'Bulgaria', code: 'BG', flag: 'BG'], '151', 'bg', 'BGN')
        createCountry([name: 'Burkina Faso', code: 'BF', flag: 'BF'], '011', 'fr', 'XOF')
        createCountry([name: 'Burundi', code: 'BI', flag: 'BI'], '014', 'rn', 'BIF')
        createCountry([name: 'Cambodia', code: 'KH', flag: 'KH'], '035', 'km', 'KHR')
        createCountry([name: 'Cameroon', code: 'CM', flag: 'CM'], '017', 'fr', 'XAF')
        createCountry([name: 'Canada', code: 'CA', flag: 'CA', addressFormat: 'CA'], '021', 'en', 'CAD')
        createCountry([name: 'Cape Verde', code: 'CV', flag: 'CV'], '011', 'pt', 'CVE')
        createCountry([name: 'Cayman Islands', code: 'KY', flag: 'KY'], '029', 'en', 'KYD')
        createCountry([name: 'Central African Republic', code: 'CF', flag: 'CF'], '017', 'fr', 'XAF')
        createCountry([name: 'Chad', code: 'TD', flag: 'TD'], '017', 'fr', 'XAF')
        createCountry([name: 'Chile', code: 'CL', flag: 'CL'], '005', 'es', 'CLP')
        createCountry([name: 'China', code: 'CN', flag: 'CN'], '030', 'zh', 'CNY')
        createCountry([name: 'Christmas Island', code: 'CX', flag: 'CX'], '035', 'en', 'AUD')
        createCountry([name: 'Cocos Islands', code: 'CC', flag: 'AU'], '035', 'ms', 'AUD')
        createCountry([name: 'Colombia', code: 'CO', flag: 'CO'], '005', 'es', 'COP')
        createCountry([name: 'Comoros', code: 'KM', flag: 'KM'], '014', 'ar', 'KMF')
        createCountry([name: 'Congo', code: 'CG', flag: 'CG'], '017', 'fr', 'XAF')
        createCountry([name: 'Congo (Zaire)', code: 'CD', flag: 'CD'], '017', 'fr', 'CDF')
        createCountry([name: 'Cook Islands', code: 'CK', flag: 'CK'], '061', 'en', 'NZD')
        createCountry([name: 'Costa Rica', code: 'CR', flag: 'CR'], '013', 'es', 'CRC')
        createCountry([name: 'Croatia', code: 'HR', flag: 'HR'], '039', 'hr', 'HRK')
        createCountry([name: 'Cuba', code: 'CU', flag: 'CU'], '029', 'es', 'CUP')
        createCountry([name: 'Cyprus', code: 'CY', flag: 'CY'], '145', 'el', 'EUR')
        createCountry([name: 'Czech Republic', code: 'CZ', flag: 'CZ'], '151', 'cs', 'CZK')
        createCountry([name: 'Denmark', code: 'DK', flag: 'DK'], '154', 'da', 'DKK')
        createCountry([name: 'Djibouti', code: 'DJ', flag: 'DJ'], '014', 'fr', 'DJF')
        createCountry([name: 'Dominica', code: 'DM', flag: 'DM'], '029', 'en', 'XCD')
        createCountry([name: 'Dominican Republic', code: 'DO', flag: 'DO'], '029', 'es', 'DOP')
        createCountry([name: 'Ecuador', code: 'EC', flag: 'EC'], '005', 'es', 'USD')
        createCountry([name: 'Egypt', code: 'EG', flag: 'EG'], '015', 'ar', 'EGP')
        createCountry([name: 'El Salvador', code: 'SV', flag: 'SV'], '013', 'es', 'USD')
        createCountry([name: 'Equatorial Guinea', code: 'GQ', flag: 'GQ'], '017', 'es', 'XAF')
        createCountry([name: 'Eritrea', code: 'ER', flag: 'ER'], '014', 'aa', 'ERN')
        createCountry([name: 'Estonia', code: 'EE', flag: 'EE'], '154', 'et', 'EUR')
        createCountry([name: 'Ethiopia', code: 'ET', flag: 'ET'], '014', 'am', 'ETB')
        createCountry([name: 'Falkland Islands', code: 'FK', flag: 'FK'], '005', 'en', 'FKP')
        createCountry([name: 'Faroe Islands', code: 'FO', flag: 'FO'], '154', 'fo', 'DKK')
        createCountry([name: 'Fiji', code: 'FJ', flag: 'FJ'], '054', 'en', 'FJD')
        createCountry([name: 'Finland', code: 'FI', flag: 'FI'], '154', 'fi', 'EUR')
        createCountry([name: 'France', code: 'FR', flag: 'FR', addressFormat: 'FR'], '155', 'fr', 'EUR')
        createCountry([name: 'French Guiana', code: 'GF', flag: 'FR'], '005', 'fr', 'EUR')
        createCountry([name: 'French Polynesia', code: 'PF', flag: 'PF'], '061', 'fr', 'XPF')
        createCountry([name: 'Gabon', code: 'GA', flag: 'GA'], '017', 'fr', 'XAF')
        createCountry([name: 'Gambia', code: 'GM', flag: 'GM'], '011', 'en', 'GMD')
        createCountry([name: 'Georgia', code: 'GE', flag: 'GE'], '145', 'ka', 'GEL')
        createCountry([name: 'Germany', code: 'DE', flag: 'DE', addressFormat: 'DE'], '155', 'de', 'EUR')
        createCountry([name: 'Ghana', code: 'GH', flag: 'GH'], '011', 'en', 'GHS')
        createCountry([name: 'Gibraltar', code: 'GI', flag: 'GI'], '039', 'en', 'GIP')
        createCountry([name: 'Greece', code: 'GR', flag: 'GR'], '039', 'el', 'EUR')
        createCountry([name: 'Greenland', code: 'GL', flag: 'GL'], '021', 'kl', 'DKK')
        createCountry([name: 'Grenada', code: 'GD', flag: 'GD'], '029', 'en', 'XCD')
        createCountry([name: 'Guadeloupe', code: 'GP', flag: 'FR'], '029', 'fr', 'EUR')
        createCountry([name: 'Guam', code: 'GU', flag: 'GU'], '057', 'en', 'USD')
        createCountry([name: 'Guatemala', code: 'GT', flag: 'GT'], '013', 'es', 'GTQ')
        createCountry([name: 'Guernsey', code: 'GG', flag: 'GG'], '154', 'en', 'GBP')
        createCountry([name: 'Guinea', code: 'GN', flag: 'GN'], '011', 'fr', 'GNF')
        createCountry([name: 'Guinea-Bissau', code: 'GW', flag: 'GW'], '011', 'pt', 'XOF')
        createCountry([name: 'Guyana', code: 'GY', flag: 'GY'], '005', 'en', 'GYD')
        createCountry([name: 'Haiti', code: 'HT', flag: 'HT'], '029', 'fr', 'HTG')
        createCountry([name: 'Holy See (Vatican City State)', code: 'VA', flag: 'VA'], '039', 'it', 'EUR')
        createCountry([name: 'Honduras', code: 'HN', flag: 'HN'], '013', 'es', 'HNL')
        createCountry([name: 'Hong Kong', code: 'HK', flag: 'HK'], '030', 'zh', 'HKD')
        createCountry([name: 'Hungary', code: 'HU', flag: 'HU'], '151', 'hu', 'HUF')
        createCountry([name: 'Iceland', code: 'IS', flag: 'IS'], '154', 'is', 'ISK')
        createCountry([name: 'India', code: 'IN', flag: 'IN'], '034', 'hi', 'INR')
        createCountry([name: 'Indonesia', code: 'ID', flag: 'ID'], '035', 'id', 'IDR')
        createCountry([name: 'Iran', code: 'IR', flag: 'IR'], '034', 'fa', 'IRR')
        createCountry([name: 'Iraq', code: 'IQ', flag: 'IQ'], '145', 'ar', 'IQD')
        createCountry([name: 'Ireland', code: 'IE', flag: 'IE'], '154', 'en', 'EUR')
        createCountry([name: 'Isle of Man', code: 'IM', flag: 'IM'], '154', 'en', 'GBP')
        createCountry([name: 'Israel', code: 'IL', flag: 'IL'], '145', 'he', 'ILS')
        createCountry([name: 'Italy', code: 'IT', flag: 'IT'], '039', 'it', 'EUR')
        createCountry([name: 'Ivory Coast', code: 'CI', flag: 'CI'], '011', 'fr', 'XOF')
        createCountry([name: 'Jamaica', code: 'JM', flag: 'JM'], '029', 'en', 'JMD')
        createCountry([name: 'Japan', code: 'JP', flag: 'JP'], '030', 'ja', 'JPY')
        createCountry([name: 'Jersey', code: 'JE', flag: 'JE'], '154', 'en', 'GBP')
        createCountry([name: 'Jordan', code: 'JO', flag: 'JO'], '145', 'ar', 'JOD')
        createCountry([name: 'Kazakhstan', code: 'KZ', flag: 'KZ'], '143', 'kk', 'KZT')
        createCountry([name: 'Kenya', code: 'KE', flag: 'KE'], '014', 'sw', 'KES')
        createCountry([name: 'Kiribati', code: 'KI', flag: 'KI'], '057', 'en', 'AUD')
        createCountry([name: 'Kosovo', code: 'CS', flag: 'CS'], '039', 'sq', 'EUR')
        createCountry([name: 'Kuwait', code: 'KW', flag: 'KW'], '145', 'ar', 'KWD')
        createCountry([name: 'Kyrgyzstan', code: 'KG', flag: 'KG'], '143', 'ky', 'KGS')
        createCountry([name: 'Laos', code: 'LA', flag: 'LA'], '035', 'lo', 'LAK')
        createCountry([name: 'Latvia', code: 'LV', flag: 'LV'], '154', 'lv', 'LVL')
        createCountry([name: 'Lebanon', code: 'LB', flag: 'LB'], '145', 'ar', 'LBP')
        createCountry([name: 'Lesotho', code: 'LS', flag: 'LS'], '018', 'en', 'LSL')
        createCountry([name: 'Liberia', code: 'LR', flag: 'LR'], '011', 'en', 'LRD')
        createCountry([name: 'Libya', code: 'LY', flag: 'LY'], '015', 'ar', 'LYD')
        createCountry([name: 'Liechtenstein', code: 'LI', flag: 'LI'], '155', 'de', 'CHF')
        createCountry([name: 'Lithuania', code: 'LT', flag: 'LT'], '154', 'lt', 'LTL')
        createCountry([name: 'Luxembourg', code: 'LU', flag: 'LU'], '155', 'de', 'EUR')
        createCountry([name: 'Macao', code: 'MO', flag: 'MO'], '030', 'zh', 'MOP')
        createCountry([name: 'Macedonia', code: 'MK', flag: 'MK'], '039', 'mk', 'MKD')
        createCountry([name: 'Madagascar', code: 'MG', flag: 'MG'], '014', 'mg', 'MGA')
        createCountry([name: 'Malawi', code: 'MW', flag: 'MW'], '014', 'en', 'MWK')
        createCountry([name: 'Malaysia', code: 'MY', flag: 'MY'], '035', 'ms', 'MYR')
        createCountry([name: 'Maldives', code: 'MV', flag: 'MV'], '034', 'en', 'MVR')
        createCountry([name: 'Mali', code: 'ML', flag: 'ML'], '011', 'fr', 'XOF')
        createCountry([name: 'Malta', code: 'MT', flag: 'MT'], '039', 'mt', 'EUR')
        createCountry([name: 'Marshall Islands', code: 'MH', flag: 'MH'], '057', 'en', 'USD')
        createCountry([name: 'Martinique', code: 'MQ', flag: 'FR'], '029', 'fr', 'EUR')
        createCountry([name: 'Mauritania', code: 'MR', flag: 'MR'], '011', 'ar', 'MRO')
        createCountry([name: 'Mauritius', code: 'MU', flag: 'MU'], '014', 'en', 'MUR')
        createCountry([name: 'Mayotte', code: 'YT', flag: 'YT'], '014', 'fr', 'EUR')
        createCountry([name: 'Mexico', code: 'MX', flag: 'MX'], '013', 'es', 'MXN')
        createCountry([name: 'Micronesia', code: 'FM', flag: 'FM'], '057', 'en', 'USD')
        createCountry([name: 'Moldova', code: 'MD', flag: 'MD'], '151', 'mo', 'MDL')
        createCountry([name: 'Monaco', code: 'MC', flag: 'MC'], '155', 'fr', 'EUR')
        createCountry([name: 'Mongolia', code: 'MN', flag: 'MN'], '030', 'mn', 'MNT')
        createCountry([name: 'Montenegro', code: 'ME', flag: 'ME'], '039', 'sr', 'EUR')
        createCountry([name: 'Montserrat', code: 'MS', flag: 'MS'], '029', 'en', 'XCD')
        createCountry([name: 'Morocco', code: 'MA', flag: 'MA'], '015', 'ar', 'MAD')
        createCountry([name: 'Mozambique', code: 'MZ', flag: 'MZ'], '014', 'pt', 'MZN')
        createCountry([name: 'Myanmar', code: 'MM', flag: 'MM'], '035', 'my', 'MMK')
        createCountry([name: 'Namibia', code: 'NA', flag: 'NA'], '018', 'af', 'NAD')
        createCountry([name: 'Nauru', code: 'NR', flag: 'NR'], '057', 'na', 'AUD')
        createCountry([name: 'Nepal', code: 'NP', flag: 'NP'], '034', 'ne', 'NPR')
        createCountry([name: 'Netherlands', code: 'NL', flag: 'NL'], '155', 'nl', 'EUR')
        createCountry([name: 'Netherlands Antilles', code: 'AN', flag: 'AN'], '029', 'en', 'ANG')
        createCountry([name: 'New Caledonia', code: 'NC', flag: 'FR'], '054', 'fr', 'XPF')
        createCountry([name: 'New Zealand', code: 'NZ', flag: 'NZ', addressFormat: 'NZ'], '053', 'en', 'NZD')
        createCountry([name: 'Nicaragua', code: 'NI', flag: 'NI'], '013', 'es', 'NIO')
        createCountry([name: 'Niger', code: 'NE', flag: 'NE'], '011', 'fr', 'XOF')
        createCountry([name: 'Nigeria', code: 'NG', flag: 'NG'], '011', 'en', 'NGN')
        createCountry([name: 'Niue', code: 'NU', flag: 'NU'], '061', 'en', 'NZD')
        createCountry([name: 'Norfolk Island', code: 'NF', flag: 'NF'], '053', 'en', 'AUD')
        createCountry([name: 'North Korea', code: 'KP', flag: 'KP'], '030', 'ko', 'KPW')
        createCountry([name: 'Northern Mariana Islands', code: 'MP', flag: 'MP'], '057', 'zh', 'USD')
        createCountry([name: 'Norway', code: 'NO', flag: 'NO'], '154', 'nb', 'NOK')
        createCountry([name: 'Oman', code: 'OM', flag: 'OM'], '145', 'ar', 'OMR')
        createCountry([name: 'Pakistan', code: 'PK', flag: 'PK'], '034', 'pa', 'PKR')
        createCountry([name: 'Palau', code: 'PW', flag: 'PW'], '057', 'en', 'USD')
        createCountry([name: 'Palestine', code: 'PS', flag: 'UN'], '145', 'ar', 'ILS')
        createCountry([name: 'Panama', code: 'PA', flag: 'PA'], '013', 'es', 'PAB')
        createCountry([name: 'Papua New Guinea', code: 'PG', flag: 'PG'], '054', 'en', 'PGK')
        createCountry([name: 'Paraguay', code: 'PY', flag: 'PY'], '005', 'es', 'PYG')
        createCountry([name: 'Peru', code: 'PE', flag: 'PE'], '005', 'es', 'PEN')
        createCountry([name: 'Philippines', code: 'PH', flag: 'PH'], '035', 'tl', 'PHP')
        createCountry([name: 'Pitcairn', code: 'PN', flag: 'PN'], '061', 'en', 'NZD')
        createCountry([name: 'Poland', code: 'PL', flag: 'PL'], '151', 'pl', 'PLN')
        createCountry([name: 'Portugal', code: 'PT', flag: 'PT'], '039', 'pt', 'EUR')
        createCountry([name: 'Puerto Rico', code: 'PR', flag: 'PR'], '029', 'es', 'USD')
        createCountry([name: 'Qatar', code: 'QA', flag: 'QA'], '145', 'ar', 'QAR')
        createCountry([name: 'Reunion', code: 'RE', flag: 'FR'], '014', 'fr', 'EUR')
        createCountry([name: 'Romania', code: 'RO', flag: 'RO'], '151', 'ro', 'RON')
        createCountry([name: 'Russia', code: 'RU', flag: 'RU'], '151', 'ru', 'RUB')
        createCountry([name: 'Rwanda', code: 'RW', flag: 'RW'], '014', 'rw', 'RWF')
        createCountry([name: 'Saint Barthelemy', code: 'BL', flag: 'FR'], '029', 'fr', 'EUR')
        createCountry([name: 'Saint Helena', code: 'SH', flag: 'SH'], '011', 'en', 'SHP')
        createCountry([name: 'Saint Kitts and Nevis', code: 'KN', flag: 'KN'], '029', 'en', 'XCD')
        createCountry([name: 'Saint Lucia', code: 'LC', flag: 'LC'], '029', 'en', 'XCD')
        createCountry([name: 'Saint Martin', code: 'MF', flag: 'FR'], '029', 'fr', 'EUR')
        createCountry([name: 'Saint Pierre and Miquelon', code: 'PM', flag: 'PM'], '021', 'fr', 'EUR')
        createCountry([name: 'Saint Vincent and the Grenadines', code: 'VC', flag: 'VC'], '029', 'en', 'XCD')
        createCountry([name: 'Samoa', code: 'WS', flag: 'WS'], '061', 'sm', 'WST')
        createCountry([name: 'San Marino', code: 'SM', flag: 'SM'], '039', 'it', 'EUR')
        createCountry([name: 'Sao Tome and Principe', code: 'ST', flag: 'ST'], '017', 'pt', 'STD')
        createCountry([name: 'Saudi Arabia', code: 'SA', flag: 'SA'], '145', 'ar', 'SAR')
        createCountry([name: 'Senegal', code: 'SN', flag: 'SN'], '011', 'fr', 'XOF')
        createCountry([name: 'Serbia', code: 'RS', flag: 'RS'], '039', 'sr', 'RSD')
        createCountry([name: 'Seychelles', code: 'SC', flag: 'SC'], '014', 'en', 'SCR')
        createCountry([name: 'Sierra Leone', code: 'SL', flag: 'SL'], '011', 'en', 'SLL')
        createCountry([name: 'Singapore', code: 'SG', flag: 'SG'], '035', 'zh', 'SGD')
        createCountry([name: 'Slovakia', code: 'SK', flag: 'SK'], '151', 'sk', 'EUR')
        createCountry([name: 'Slovenia', code: 'SI', flag: 'SI'], '039', 'sl', 'EUR')
        createCountry([name: 'Solomon Islands', code: 'SB', flag: 'SB'], '054', 'en', 'SBD')
        createCountry([name: 'Somalia', code: 'SO', flag: 'SO'], '014', 'so', 'SOS')
        createCountry([name: 'South Africa', code: 'ZA', flag: 'ZA'], '018', 'af', 'ZAR')
        createCountry([name: 'South Georgia and South Sandwich Islands', code: 'GS', flag: 'GS'], '005', 'en', 'FKP')
        createCountry([name: 'South Korea', code: 'KR', flag: 'KR'], '030', 'ko', 'KRW')
        createCountry([name: 'Spain', code: 'ES', flag: 'ES'], '039', 'es', 'EUR')
        createCountry([name: 'Sri Lanka', code: 'LK', flag: 'LK'], '034', 'si', 'LKR')
        createCountry([name: 'Sudan', code: 'SD', flag: 'SD'], '015', 'ar', 'SDG')
        createCountry([name: 'Suriname', code: 'SR', flag: 'SR'], '005', 'nl', 'SRD')
        createCountry([name: 'Svalbard and Jan Mayen', code: 'SJ', flag: 'NO'], '154', 'nb', 'NOK')
        createCountry([name: 'Swaziland', code: 'SZ', flag: 'SZ'], '018', 'en', 'SZL')
        createCountry([name: 'Sweden', code: 'SE', flag: 'SE'], '154', 'sv', 'SEK')
        createCountry([name: 'Switzerland', code: 'CH', flag: 'CH'], '155', 'de', 'CHF')
        createCountry([name: 'Syria', code: 'SY', flag: 'SY'], '145', 'ar', 'SYP')
        createCountry([name: 'Taiwan', code: 'TW', flag: 'TW'], '030', 'zh', 'TWD')
        createCountry([name: 'Tajikistan', code: 'TJ', flag: 'TJ'], '143', 'tg', 'TJS')
        createCountry([name: 'Tanzania', code: 'TZ', flag: 'TZ'], '014', 'sw', 'TZS')
        createCountry([name: 'Thailand', code: 'TH', flag: 'TH'], '035', 'th', 'THB')
        createCountry([name: 'Timor-Leste', code: 'TL', flag: 'TL'], '035', 'pt', 'USD')
        createCountry([name: 'Togo', code: 'TG', flag: 'TG'], '011', 'fr', 'XOF')
        createCountry([name: 'Tokelau', code: 'TK', flag: 'NZ'], '061', 'en', 'NZD')
        createCountry([name: 'Tonga', code: 'TO', flag: 'TO'], '061', 'to', 'TOP')
        createCountry([name: 'Trinidad and Tobago', code: 'TT', flag: 'TT'], '029', 'en', 'TTD')
        createCountry([name: 'Tunisia', code: 'TN', flag: 'TN'], '015', 'ar', 'TND')
        createCountry([name: 'Turkey', code: 'TR', flag: 'TR'], '145', 'tr', 'TRY')
        createCountry([name: 'Turkmenistan', code: 'TM', flag: 'TM'], '143', 'tk', 'TMT')
        createCountry([name: 'Turks and Caicos Islands', code: 'TC', flag: 'TC'], '029', 'en', 'USD')
        createCountry([name: 'Tuvalu', code: 'TV', flag: 'TV'], '061', 'en', 'AUD')
        createCountry([name: 'Uganda', code: 'UG', flag: 'UG'], '014', 'en', 'UGX')
        createCountry([name: 'Ukraine', code: 'UA', flag: 'UA'], '151', 'uk', 'UAH')
        createCountry([name: 'United Arab Emirates', code: 'AE', flag: 'AE'], '145', 'ar', 'AED')
        createCountry([name: 'United Kingdom', code: 'GB', flag: 'GB', addressFormat: 'GB'], '154', 'en', 'GBP')
        createCountry([name: 'United States', code: 'US', flag: 'US', addressFormat: 'US'], '021', 'en', 'USD')
        createCountry([name: 'Uruguay', code: 'UY', flag: 'UY'], '005', 'es', 'UYU')
        createCountry([name: 'Uzbekistan', code: 'UZ', flag: 'UZ'], '143', 'uz', 'UZS')
        createCountry([name: 'Vanuatu', code: 'VU', flag: 'VU'], '054', 'bi', 'VUV')
        createCountry([name: 'Venezuela', code: 'VE', flag: 'VE'], '005', 'es', 'VEF')
        createCountry([name: 'Viet Nam', code: 'VN', flag: 'VN'], '035', 'vi', 'VND')
        createCountry([name: 'Virgin Islands, British', code: 'VG', flag: 'VG'], '029', 'en', 'USD')
        createCountry([name: 'Virgin Islands, U.S.', code: 'VI', flag: 'VI'], '029', 'en', 'USD')
        createCountry([name: 'Wallis and Futuna', code: 'WF', flag: 'WF'], '061', 'fr', 'XPF')
        createCountry([name: 'Western Sahara', code: 'EH', flag: 'EH'], '015', 'ar', 'MAD')
        createCountry([name: 'Yemen', code: 'YE', flag: 'YE'], '145', 'ar', 'YER')
        createCountry([name: 'Zambia', code: 'ZM', flag: 'ZM'], '014', 'en', 'ZMK')
        createCountry([name: 'Zimbabwe', code: 'ZW', flag: 'ZW'], '014', 'en', 'USD')
    }

    private createCountry(map, region, language, currency) {
        def format = map.remove('addressFormat') ?: 'default'
        def country = new SystemCountry(map)
        country.region = SystemRegion.findByCode(region)
        country.language = SystemLanguage.findByCode(language)
        country.currency = SystemCurrency.findByCode(currency)
        country.addressFormat = SystemAddressFormat.findByCode(format)
        country.saveThis()
        new SystemMessage(code: "country.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemMeasures() {
        createMeasure(code: 'quantity', name: 'Quantity')
        createMeasure(code: 'length', name: 'Length')
        createMeasure(code: 'area', name: 'Area')
        createMeasure(code: 'volume', name: 'Volume')
        createMeasure(code: 'weight', name: 'Weight')
    }

    private createMeasure(map) {
        new SystemMeasure(map).saveThis()
        new SystemMessage(code: "measure.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemScales() {
        createScale(code: 'universal', name: 'Universal')
        createScale(code: 'metric', name: 'Metric')
        createScale(code: 'imperialUS', name: 'Imperial and US')
    }

    private createScale(map) {
        new SystemScale(map).saveThis()
        new SystemMessage(code: "scale.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemUnits() {
        createUnit([name: 'Individual Item', code: 'item', multiplier: 1.0], 'quantity', 'universal')
        createUnit([name: 'Dozen', code: 'doz', multiplier: 12.0], 'quantity', 'universal')
        createUnit([name: 'Gross', code: 'gross', multiplier: 144.0], 'quantity', 'universal')
        createUnit([name: 'Millimeters', code: 'mm', multiplier: 0.1], 'length', 'metric')
        createUnit([name: 'Centimeters', code: 'cm', multiplier: 1.0], 'length', 'metric')
        createUnit([name: 'Meters', code: 'm', multiplier: 100.0], 'length', 'metric')
        createUnit([name: 'Kilometers', code: 'km', multiplier: 100000.0], 'length', 'metric')
        createUnit([name: 'Inches', code: 'in', multiplier: 1.0], 'length', 'imperialUS')
        createUnit([name: 'Feet', code: 'ft', multiplier: 12.0], 'length', 'imperialUS')
        createUnit([name: 'Yards', code: 'yd', multiplier: 36.0], 'length', 'imperialUS')
        createUnit([name: 'Miles', code: 'mi', multiplier: 63360.0], 'length', 'imperialUS')
        createUnit([name: 'Square Millimeters', code: 'mm2', multiplier: 0.01], 'area', 'metric')
        createUnit([name: 'Square Centimeters', code: 'cm2', multiplier: 1.0], 'area', 'metric')
        createUnit([name: 'Square Meters', code: 'm2', multiplier: 10000.0], 'area', 'metric')
        createUnit([name: 'Square Kilometers', code: 'km2', multiplier: 10000000000.0], 'area', 'metric')
        createUnit([name: 'Hectares', code: 'ha', multiplier: 100000000.0], 'area', 'metric')
        createUnit([name: 'Square Inches', code: 'in2', multiplier: 1.0], 'area', 'imperialUS')
        createUnit([name: 'Square Feet', code: 'ft2', multiplier: 144.0], 'area', 'imperialUS')
        createUnit([name: 'Square Yards', code: 'yd2', multiplier: 1296.0], 'area', 'imperialUS')
        createUnit([name: 'Square Miles', code: 'mi2', multiplier: 4014489600.0], 'area', 'imperialUS')
        createUnit([name: 'Acres', code: 'ac', multiplier: 6272640.0], 'area', 'imperialUS')
        createUnit([name: 'Cubic Centimeters', code: 'cc', multiplier: 1.0], 'volume', 'metric')
        createUnit([name: 'Centiliters', code: 'cl', multiplier: 10.0], 'volume', 'metric')
        createUnit([name: 'Cubic Meters', code: 'm3', multiplier: 1000000.0], 'volume', 'metric')
        createUnit([name: 'Hectoliters', code: 'hl', multiplier: 100000.0], 'volume', 'metric')
        createUnit([name: 'Liters', code: 'L', multiplier: 1000.0], 'volume', 'metric')
        createUnit([name: 'Milliliters', code: 'ml', multiplier: 1.0], 'volume', 'metric')
        createUnit([name: 'Cubic Inches', code: 'in3', multiplier: 1.0], 'volume', 'imperialUS')
        createUnit([name: 'Cubic Feet', code: 'ft3', multiplier: 1728.0], 'volume', 'imperialUS')
        createUnit([name: 'Cubic Yards', code: 'yd3', multiplier: 46656.0], 'volume', 'imperialUS')
        createUnit([name: 'Fluid Ounce (Imperial)', code: 'fl.oz(Imp)', multiplier: 1.7338715625], 'volume', 'imperialUS')
        createUnit([name: 'Pint (Imperial)', code: 'pt(Imp)', multiplier: 34.67743125], 'volume', 'imperialUS')
        createUnit([name: 'Quart (Imperial)', code: 'qt(Imp)', multiplier: 69.3548625], 'volume', 'imperialUS')
        createUnit([name: 'Gallon (Imperial)', code: 'gal(Imp)', multiplier: 277.41945], 'volume', 'imperialUS')
        createUnit([name: 'Fluid Ounce (US)', code: 'fl.oz(US)', multiplier: 1.8046875], 'volume', 'imperialUS')
        createUnit([name: 'Pint (US)', code: 'pt(US)', multiplier: 28.875], 'volume', 'imperialUS')
        createUnit([name: 'Quart (US)', code: 'qt(US)', multiplier: 57.75], 'volume', 'imperialUS')
        createUnit([name: 'Gallon (US)', code: 'gal(US)', multiplier: 231.0], 'volume', 'imperialUS')
        createUnit([name: 'Milligrams', code: 'mg', multiplier: 0.001], 'weight', 'metric')
        createUnit([name: 'Grams', code: 'g', multiplier: 1.0], 'weight', 'metric')
        createUnit([name: 'Kilograms', code: 'kg', multiplier: 1000.0], 'weight', 'metric')
        createUnit([name: 'Tonnes', code: 't', multiplier: 1000000.0], 'weight', 'metric')
        createUnit([name: 'Ounces', code: 'oz', multiplier: 1.0], 'weight', 'imperialUS')
        createUnit([name: 'Pounds', code: 'lb', multiplier: 16.0], 'weight', 'imperialUS')
        createUnit([name: 'Stones (Imperial)', code: 'st', multiplier: 224.0], 'weight', 'imperialUS')
        createUnit([name: 'Hundredweights', code: 'cwt', multiplier: 1792.0], 'weight', 'imperialUS')
        createUnit([name: 'Tons (Imperial Long)', code: 'ton(Imp)', multiplier: 35840.0], 'weight', 'imperialUS')
        createUnit([name: 'Tons (US Short)', code: 'ton(US)', multiplier: 32000.0], 'weight', 'imperialUS')
    }

    private createUnit(map, measure, scale) {
        def unit = new SystemUnit(map)
        unit.measure = SystemMeasure.findByCode(measure)
        unit.scale = SystemScale.findByCode(scale)
        unit.saveThis()
        new SystemMessage(code: "unit.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemConversions() {
        createConversion([code: 'in_to_cm', name: 'Imperial/US to Metric Lengths', preAddition: 0.0, multiplier: 2.54, postAddition: 0.0], 'in', 'cm')
        createConversion([code: 'in2_to_cm2', name: 'Imperial/US to Metric Areas', preAddition: 0.0, multiplier: 6.4516, postAddition: 0.0], 'in2', 'cm2')
        createConversion([code: 'in3_to_cc', name: 'Imperial/US to Metric Volumes', preAddition: 0.0, multiplier: 16.387064, postAddition: 0.0], 'in3', 'cc')
        createConversion([code: 'oz_to_g', name: 'Imperial/US to Metric Weights', preAddition: 0.0, multiplier: 28.349523125, postAddition: 0.0], 'oz', 'g')
    }

    private createConversion(map, source, target) {
        def conversion = new SystemConversion(map)
        conversion.source = SystemUnit.findByCode(source)
        conversion.target = SystemUnit.findByCode(target)
        conversion.saveThis()
        new SystemMessage(code: "conversion.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemPaymentSchedules() {
        createPaymentSchedule(code: 'monthEnd', name: 'Each Month End', monthDayPattern: 'L', weekDayPattern: null)
    }

    private createPaymentSchedule(map) {
        def schedule = new SystemPaymentSchedule(map)
        schedule.saveThis()
        new SystemMessage(code: "paymentSchedule.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemUsers() {
        def locale = Locale.getDefault()
        def language = SystemLanguage.findByCode(locale.getLanguage() ?: 'en') ?: SystemLanguage.findByCode('en')
        def country = SystemCountry.findByCode(locale.getCountry() ?: 'US') ?: SystemCountry.findByCode('US')

        def user = new SystemUser()
        user.loginId = 'system'
        user.name = 'System Administrator'
        user.email = 'system@system.com'
        user.password = 'sysadmin'
        user.passwordConfirmation = 'sysadmin'
        user.securityQuestion = 'Dummy question'
        user.securityAnswer = 'Dummy answer'
        user.administrator = true
        user.country = country
        user.language = language
        user.verifyPasswordStatus()
        user.saveThis()
    }

    private loadCompanies() {
        def locale = Locale.getDefault()
        def language = SystemLanguage.findByCode(locale.getLanguage() ?: 'en') ?: SystemLanguage.findByCode('en')
        def country = SystemCountry.findByCode(locale.getCountry() ?: ((language.code == 'en') ? 'US' : language.code.toUpperCase(Locale.US))) ?: SystemCountry.findByCode('US')
        def user = SystemUser.findByLoginId('system')
        def company = new Company(name: 'System', country: country, language: language, systemOnly: true)

        // The US, Canada, Mexico, Columbia, Venzuela, Chile and the Phillippines use US Letter stationery
        // but we let the PDF system handle the resizing and so the stationery property has been removed
        //if (['US', 'CA', 'MX', 'CO', 'VE', 'CL', 'PH'].contains(country.code)) company.stationery = 'letter'

        company.saveThis()

        // Create the company/user relationships
        new CompanyUser(company: company, user: user).saveThis()
    }

    private loadTasks() {

        def tsk = createTask(code: 'autopay', name: 'Create Auto-Payments', executable: 'AutoPayCreateTask', allowOnDemand: false,
                schedule: '0 21 * * *', nextScheduledRun: new Date(), retentionDays: 7, systemOnly: false)
        createTaskResult(task: tsk, code: 'examined', name: 'Suppliers examined', sequencer: 10, dataType: 'integer')
        createTaskResult(task: tsk, code: 'created', name: 'Advices created', sequencer: 20, dataType: 'integer')

        tsk = createTask(code: 'history', name: 'Remove History', executable: 'HistoryTask', allowOnDemand: false,
                schedule: '0 22 1 * *', nextScheduledRun: new Date(), retentionDays: 31, systemOnly: true)
        createTaskResult(task: tsk, code: 'statements', name: 'Statements deleted', sequencer: 10, dataType: 'integer')
        createTaskResult(task: tsk, code: 'remittance', name: 'Remittance Advices deleted', sequencer: 20, dataType: 'integer')
        createTaskResult(task: tsk, code: 'bankrecs', name: 'Bank Reconciliations deleted', sequencer: 30, dataType: 'integer')
        createTaskResult(task: tsk, code: 'taxstmts', name: 'Tax Statements deleted', sequencer: 40, dataType: 'integer')

        tsk = createTask(code: 'activity', name: 'Posting Activity Report', executable: 'PostingActivityReport', allowOnDemand: false,
                schedule: '0 23 * * *', nextScheduledRun: null, retentionDays: 7, systemOnly: false)
        createTaskResult(task: tsk, code: 'minId', name: 'Minimum document id', sequencer: 10, dataType: 'string')
        createTaskResult(task: tsk, code: 'maxId', name: 'Maximum document id', sequencer: 20, dataType: 'string')
        createTaskResult(task: tsk, code: 'seriesDate', name: 'New report series date', sequencer: 30, dataType: 'date', dataScale: 1)

        tsk = createTask(code: 'maintain', name: 'Database Maintenance', executable: 'MaintenanceTask', allowOnDemand: false,
                schedule: '0 2 * * 6', nextScheduledRun: null, retentionDays: 7, systemOnly: true)
        createTaskResult(task: tsk, code: 'executed', name: 'Number of commands executed', sequencer: 10, dataType: 'integer')

        tsk = createTask(code: 'queueClean', name: 'Remove old task history', executable: 'QueueCleanupTask', allowOnDemand: false,
                schedule: '0 3 * * *', nextScheduledRun: new Date(), retentionDays: 7, systemOnly: true)
        createTaskResult(task: tsk, code: 'count', name: 'Number of records deleted', sequencer: 10, dataType: 'integer')

        tsk = createTask(code: 'tempClean', name: 'Remove old temp files', executable: 'TempCleanupTask', allowOnDemand: false,
                schedule: '15 3 * * *', nextScheduledRun: new Date(), retentionDays: 7, systemOnly: true)
        createTaskParam(task: tsk, code: 'days', name: 'History retention days', sequencer: 10, dataType: 'integer', dataScale: null, defaultValue: '1', required: true)
        createTaskResult(task: tsk, code: 'count', name: 'Number of files deleted', sequencer: 10, dataType: 'integer')

        tsk = createTask(code: 'rateClean', name: 'Remove old exchange rates', executable: 'RatesCleanupTask', allowOnDemand: false,
                schedule: '30 3 * * *', nextScheduledRun: new Date(), retentionDays: 7, systemOnly: true)
        createTaskParam(task: tsk, code: 'days', name: 'History retention days', sequencer: 10, dataType: 'integer', dataScale: null, defaultValue: '1100', required: true)
        createTaskResult(task: tsk, code: 'count', name: 'Number of records deleted', sequencer: 10, dataType: 'integer')
        createTaskResult(task: tsk, code: 'failed', name: 'Number of deletion failures', sequencer: 20, dataType: 'integer')

        tsk = createTask(code: 'traceClean', name: 'Remove old trace history', executable: 'TraceCleanupTask', allowOnDemand: false,
                schedule: '45 3 * * *', nextScheduledRun: new Date(), retentionDays: 7, systemOnly: true)
        createTaskResult(task: tsk, code: 'count', name: 'Number of records deleted', sequencer: 10, dataType: 'integer')

        tsk = createTask(code: 'fxRates', name: 'Update foreign exchange rates', executable: 'ExchangeRatesTask', allowOnDemand: false,
                schedule: '0 4 * * *', nextScheduledRun: new Date(), retentionDays: 7, systemOnly: true)
        createTaskResult(task: tsk, code: 'retrieved', name: 'Internet rate retrievals', sequencer: 10, dataType: 'integer')
        createTaskResult(task: tsk, code: 'failed', name: 'Internet rate failures', sequencer: 20, dataType: 'integer')
        createTaskResult(task: tsk, code: 'created', name: 'Exchange rate records created', sequencer: 30, dataType: 'integer')

        tsk = createTask(code: 'demoClean', name: 'Remove old demo users and companies', executable: 'DemoCleanupTask', allowOnDemand: false,
                schedule: '15 4 * * *', nextScheduledRun: new Date(), retentionDays: 7, systemOnly: true)
        createTaskParam(task: tsk, code: 'days', name: 'Allowed inactivity days', sequencer: 10, dataType: 'integer', dataScale: null, defaultValue: '30', required: true)
        createTaskResult(task: tsk, code: 'userCount', name: 'Number of user records deleted', sequencer: 10, dataType: 'integer')
        createTaskResult(task: tsk, code: 'userFails', name: 'Number of user delete failures', sequencer: 20, dataType: 'integer')
        createTaskResult(task: tsk, code: 'coCount', name: 'Number of co. records deleted', sequencer: 30, dataType: 'integer')
        createTaskResult(task: tsk, code: 'coFails', name: 'Number of co. delete failures', sequencer: 40, dataType: 'integer')

        tsk = createTask(code: 'chartAcct', name: 'Chart of accounts report', executable: 'ChartOfAccountsReport', allowOnDemand: true, activity: 'actadmin', retentionDays: 7)
        createTaskParam(task: tsk, code: 'inactive', name: 'Include inactive accounts', sequencer: 10, dataType: 'boolean', dataScale: null, defaultValue: 'false', required: true)

        tsk = createTask(code: 'delYear', name: 'Delete accounting year', executable: 'DeleteYearTask', allowOnDemand: true, activity: 'actadmin', retentionDays: 7)
        createTaskParam(task: tsk, code: 'stringId', name: 'String id of the year', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)

        tsk = createTask(code: 'delCompany', name: 'Delete company', executable: 'DeleteCompanyTask', allowOnDemand: true, activity: 'sysadmin', retentionDays: 7, systemOnly: true)
        createTaskParam(task: tsk, code: 'stringId', name: 'String id of the company', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)

        tsk = createTask(code: 'pdOpen', name: 'Open new accounting period', executable: 'PeriodOpenTask', allowOnDemand: true, activity: 'actadmin', retentionDays: 7)
        createTaskParam(task: tsk, code: 'stringId', name: 'String id of the period', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)

        tsk = createTask(code: 'fxRevalue', name: 'Foreign Currency Revaluation', executable: 'RevaluationTask', allowOnDemand: true, activity: 'revalue', retentionDays: 7)
        createTaskParam(task: tsk, code: 'periodId', name: 'String id of the period', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'targetId', name: 'String id of the diff. account', sequencer: 20, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'adjustment', name: 'Treat as adjustment', sequencer: 30, dataType: 'boolean', dataScale: null, defaultValue: 'true', required: true)
        createTaskResult(task: tsk, code: 'document', name: 'Revaluation document', sequencer: 10, dataType: 'string')
        createTaskResult(task: tsk, code: 'lineCount', name: 'Document line count', sequencer: 20, dataType: 'integer')
        createTaskResult(task: tsk, code: 'diffValue', name: 'Revaluation amount', sequencer: 30, dataType: 'string')

        tsk = createTask(code: 'bankRecur', name: 'Recurring Bank Transactions', executable: 'BankRecurringTask', allowOnDemand: false,
                schedule: '0 5 * * *', nextScheduledRun: new Date(), retentionDays: 7)
        createTaskResult(task: tsk, code: 'success', name: 'Successful transactions', sequencer: 10, dataType: 'integer')
        createTaskResult(task: tsk, code: 'fail', name: 'Failed transactions', sequencer: 20, dataType: 'integer')

        tsk = createTask(code: 'taxStmt', name: 'Tax Statement Creation', executable: 'TaxStatementTask', allowOnDemand: true, activity: 'taxstmt', retentionDays: 7)
        createTaskParam(task: tsk, code: 'authority', name: 'String id of the tax authority', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'date', name: 'Statement date', sequencer: 20, dataType: 'date', dataScale: 1, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'describe', name: 'Description', sequencer: 30, dataType: 'string', dataScale: null, defaultValue: null, required: false)

        tsk = createTask(code: 'taxDelete', name: 'Tax Statement Deletion', executable: 'TaxDeleteTask', allowOnDemand: true, activity: 'taxstmt', retentionDays: 7)
        createTaskParam(task: tsk, code: 'stringId', name: 'String id of the tax statement', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)

        tsk = createTask(code: 'taxReport', name: 'Tax Statement Report', executable: 'TaxStatementReport', allowOnDemand: true, activity: 'taxstmt', retentionDays: 7)
        createTaskParam(task: tsk, code: 'stringId', name: 'String id of the tax statement', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)

        tsk = createTask(code: 'tb', name: 'Trial Balance Report', executable: 'TrialBalanceReport', allowOnDemand: true, activity: 'glreport', retentionDays: 7)
        createTaskParam(task: tsk, code: 'stringId', name: 'String id of the period', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'omitZero', name: 'Omit zero values', sequencer: 20, dataType: 'boolean', dataScale: null, defaultValue: 'true', required: false)

        tsk = createTask(code: 'postings', name: 'Detailed Postings Report', executable: 'DetailedPostingReport', allowOnDemand: true, activity: 'glreport', retentionDays: 7)
        createTaskParam(task: tsk, code: 'stringId', name: 'String id of the period', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'scope', name: 'Code of the report scope', sequencer: 20, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'scopeId', name: 'Section or account id', sequencer: 30, dataType: 'string', dataScale: null, defaultValue: null, required: false)
		createTaskParam(task: tsk, code: 'omitZero', name: 'Omit zero values', sequencer: 40, dataType: 'boolean', dataScale: null, defaultValue: 'true', required: false)
		
        tsk = createTask(code: 'customers', name: 'Customer List', executable: 'CustomerListReport', allowOnDemand: true, activity: 'arreport', retentionDays: 7)
        createTaskParam(task: tsk, code: 'codes', name: 'Specific access codes', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'active', name: 'Only active accounts', sequencer: 20, dataType: 'boolean', dataScale: null, defaultValue: null, required: true)

        tsk = createTask(code: 'suppliers', name: 'Supplier List', executable: 'SupplierListReport', allowOnDemand: true, activity: 'apreport', retentionDays: 7)
        createTaskParam(task: tsk, code: 'codes', name: 'Specific access codes', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'active', name: 'Only active accounts', sequencer: 20, dataType: 'boolean', dataScale: null, defaultValue: null, required: true)

        tsk = createTask(code: 'agedDebt', name: 'Aged List of Debtors', executable: 'AgedDebtorsReport', allowOnDemand: true, activity: 'arreport', retentionDays: 7)
        createTaskParam(task: tsk, code: 'codes', name: 'Specific access codes', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: false)

        tsk = createTask(code: 'agedCredit', name: 'Aged List of Creditors', executable: 'AgedCreditorsReport', allowOnDemand: true, activity: 'apreport', retentionDays: 7)
        createTaskParam(task: tsk, code: 'codes', name: 'Specific access codes', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: false)

        tsk = createTask(code: 'demodata', name: 'Create Demo Data', executable: 'DemoLoadTask', allowOnDemand: true, activity: 'sysadmin', retentionDays: 7, systemOnly: true)
        createTaskParam(task: tsk, code: 'companyId', name: 'String id of the company', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'userId', name: 'String id of the user', sequencer: 20, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'currencyId', name: 'String id of the corp currency', sequencer: 30, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'taxCodeId', name: 'String id of the corp tax code', sequencer: 40, dataType: 'string', dataScale: null, defaultValue: null, required: true)

        tsk = createTask(code: 'statements', name: 'Customer Statements', executable: 'DebtorsStatementsReport', allowOnDemand: true, activity: 'arreport', retentionDays: 7)
        createTaskParam(task: tsk, code: 'codes', name: 'Specific access codes', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'batchSize', name: 'Number of statements per batch', sequencer: 20, dataType: 'integer', dataScale: null, defaultValue: 0, required: false)
        createTaskParam(task: tsk, code: 'stmtDate', name: 'Statement date', sequencer: 30, dataType: 'date', dataScale: 1, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'customer', name: 'Specific customer', sequencer: 40, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskResult(task: tsk, code: 'statements', name: 'Statement count', sequencer: 10, dataType: 'integer')
        createTaskResult(task: tsk, code: 'batches', name: 'Batch count', sequencer: 20, dataType: 'integer')

        tsk = createTask(code: 'csReprint', name: 'Customer Statement Reprint', executable: 'DebtorsStatementsReprint', allowOnDemand: true, activity: 'enquire', retentionDays: 7)
        createTaskParam(task: tsk, code: 'codes', name: 'Specific access codes', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'batchSize', name: 'Number of statements per batch', sequencer: 20, dataType: 'integer', dataScale: null, defaultValue: 0, required: false)
        createTaskParam(task: tsk, code: 'stmtDate', name: 'Statement date', sequencer: 30, dataType: 'date', dataScale: 1, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'customer', name: 'Specific customer', sequencer: 40, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskResult(task: tsk, code: 'statements', name: 'Statement count', sequencer: 10, dataType: 'integer')
        createTaskResult(task: tsk, code: 'batches', name: 'Batch count', sequencer: 20, dataType: 'integer')

        tsk = createTask(code: 'release', name: 'Release Auto-Payments', executable: 'AutoPayProcessTask', allowOnDemand: true, activity: 'apremit', retentionDays: 7)
        createTaskParam(task: tsk, code: 'batchSize', name: 'Number of advices per batch', sequencer: 10, dataType: 'integer', dataScale: null, defaultValue: 0, required: false)
        createTaskResult(task: tsk, code: 'paid', name: 'Number of suppliers paid', sequencer: 10, dataType: 'integer')
        createTaskResult(task: tsk, code: 'rejected', name: 'No of suppliers rejected', sequencer: 20, dataType: 'integer')
        createTaskResult(task: tsk, code: 'posted', name: 'No of documents posted', sequencer: 30, dataType: 'integer')

        tsk = createTask(code: 'raReprint', name: 'Remittance Advice Reprint', executable: 'RemittanceAdviceReprint', allowOnDemand: true, activity: 'enquire', retentionDays: 7)
        createTaskParam(task: tsk, code: 'codes', name: 'Specific access codes', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'batchSize', name: 'Number of advices per batch', sequencer: 20, dataType: 'integer', dataScale: null, defaultValue: 0, required: false)
        createTaskParam(task: tsk, code: 'adviceDate', name: 'Advice date', sequencer: 30, dataType: 'date', dataScale: 1, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'supplier', name: 'Specific supplier', sequencer: 40, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskResult(task: tsk, code: 'advices', name: 'Advice count', sequencer: 10, dataType: 'integer')
        createTaskResult(task: tsk, code: 'batches', name: 'Batch count', sequencer: 20, dataType: 'integer')

        tsk = createTask(code: 'advices', name: 'Unauthorized Remittance Advices', executable: 'UnauthorizedAdvicesReport', allowOnDemand: true, activity: 'apremit', retentionDays: 7)

        tsk = createTask(code: 'reconcile', name: 'Create Bank Reconciliation', executable: 'ReconciliationTask', allowOnDemand: true, activity: 'bankrec', retentionDays: 7)
        createTaskParam(task: tsk, code: 'account', name: 'String bank account Id', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'date', name: 'Bank statement date', sequencer: 20, dataType: 'date', dataScale: 1, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'balance', name: 'Bank statement balance', sequencer: 30, dataType: 'decimal', dataScale: 3, defaultValue: null, required: true)

        tsk = createTask(code: 'recReport', name: 'Bank Reconciliation Report', executable: 'ReconciliationReport', allowOnDemand: true, activity: 'bankrec', retentionDays: 7)
        createTaskParam(task: tsk, code: 'recId', name: 'String reconciliation Id', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)

        tsk = createTask(code: 'budgetList', name: 'Budget Listing Report', executable: 'BudgetReport', allowOnDemand: true, activity: 'budgets', retentionDays: 7)
        createTaskParam(task: tsk, code: 'yearId', name: 'String year Id', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'section', name: 'Section type or Id', sequencer: 20, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element2', name: 'Element 2 id', sequencer: 30, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element3', name: 'Element 3 id', sequencer: 40, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element4', name: 'Element 4 id', sequencer: 50, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element5', name: 'Element 5 id', sequencer: 60, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element6', name: 'Element 6 id', sequencer: 70, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element7', name: 'Element 7 id', sequencer: 80, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element8', name: 'Element 8 id', sequencer: 90, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId1', name: 'Period 1 id', sequencer: 100, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId2', name: 'Period 2 id', sequencer: 110, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId3', name: 'Period 3 id', sequencer: 120, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId4', name: 'Period 4 id', sequencer: 130, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId5', name: 'Period 5 id', sequencer: 140, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId6', name: 'Period 6 id', sequencer: 150, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId7', name: 'Period 7 id', sequencer: 160, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId8', name: 'Period 8 id', sequencer: 170, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId9', name: 'Period 9 id', sequencer: 180, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId10', name: 'Period 10 id', sequencer: 190, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId11', name: 'Period 11 id', sequencer: 200, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId12', name: 'Period 12 id', sequencer: 210, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'periodId13', name: 'Period 13 id', sequencer: 220, dataType: 'string', dataScale: null, defaultValue: null, required: true)

        tsk = createTask(code: 'ieReport', name: 'Income and Expenditure Report', executable: 'ProfitAndLossReport', allowOnDemand: true, activity: 'iereport', retentionDays: 7)
        createTaskParam(task: tsk, code: 'formatId', name: 'String format Id', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'periodId', name: 'String period Id', sequencer: 20, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'element2', name: 'Element 2 id', sequencer: 30, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element3', name: 'Element 3 id', sequencer: 40, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element4', name: 'Element 4 id', sequencer: 50, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element5', name: 'Element 5 id', sequencer: 60, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element6', name: 'Element 6 id', sequencer: 70, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element7', name: 'Element 7 id', sequencer: 80, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'element8', name: 'Element 8 id', sequencer: 90, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'grouping1', name: 'Grouping 1 id', sequencer: 100, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'grouping2', name: 'Grouping 2 id', sequencer: 110, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'grouping3', name: 'Grouping 3 id', sequencer: 120, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'detailed', name: 'Include detailed accounts', sequencer: 130, dataType: 'boolean', dataScale: null, defaultValue: 'false', required: true)

        tsk = createTask(code: 'bsReport', name: 'Balance Sheet Report', executable: 'BalanceSheetReport', allowOnDemand: true, activity: 'bsreport', retentionDays: 7)
        createTaskParam(task: tsk, code: 'formatId', name: 'String format Id', sequencer: 10, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'periodId', name: 'String period Id', sequencer: 20, dataType: 'string', dataScale: null, defaultValue: null, required: true)
        createTaskParam(task: tsk, code: 'grouping1', name: 'Grouping 1 id', sequencer: 30, dataType: 'string', dataScale: null, defaultValue: null, required: false)
        createTaskParam(task: tsk, code: 'detailed', name: 'Include detailed accounts', sequencer: 40, dataType: 'boolean', dataScale: null, defaultValue: 'false', required: true)
    }

    private createTask(map) {
        if (map.activity) {
            def acty = SystemActivity.findByCode(map.activity)
            if (!acty) {
                acty = new SystemActivity(code: map.activity)
                acty.saveThis()
            }

            map.activity = acty
        }

        def task = new Task(map)
        task.company = Company.findBySystemOnly(true)
        if (map.schedule) task.user = SystemUser.findByLoginId('system')
        task.saveThis()
        new SystemMessage(code: "task.name.${map.code}", locale: '*', text: map.name).saveThis()
        return task
    }

    private createTaskParam(map) {
        new TaskParam(map).saveThis()
        new SystemMessage(code: "taskParam.name.${map.task.code}.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private createTaskResult(map) {
        new TaskResult(map).saveThis()
        new SystemMessage(code: "taskResult.name.${map.task.code}.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadMessages(servletContext) {
        def path = servletContext.getRealPath('/')
        if (path) {
            def dir = new File(new File(path).getParent(), "grails-app${File.separator}i18n")
            if (dir.exists() && dir.canRead()) {
                def names = []
                for (it in dir.listFiles()) {
                    if (it.isFile() && it.canRead() && it.getName().endsWith(".properties")) {
                        names << it.getName()
                    }
                }

                names.sort()

                def locale
                for (it in names) {
                    if (it ==~ /.+_[a-z][a-z]_[A-Z][A-Z]\.properties$/) {
                        locale = new Locale(it.substring(it.length() - 16, it.length() - 14), it.substring(it.length() - 13, it.length() - 11))
                    } else if (it ==~ /.+_[a-z][a-z]\.properties$/) {
                        locale = new Locale(it.substring(it.length() - 13, it.length() - 11))
                    } else {
                        locale = null
                    }

                    DatabaseMessageSource.loadPropertyFile(new File(dir, it), locale)
                }
            }
        }
    }

    private loadPageHelp(servletContext) {
        def dir = new File(servletContext.getRealPath('/pagehelp'))
        if (dir.exists() && dir.canRead()) {
            def names = []
            for (it in dir.listFiles()) {
                if (it.isFile() && it.canRead() && it.getName().endsWith(".helptext")) {
                    names << it.getName()
                }
            }

            names.sort()

            def locale, key
            for (it in names) {
                if (it ==~ /.+_[a-z][a-z]_[A-Z][A-Z]\.helptext$/) {
                    locale = new Locale(it.substring(it.length() - 14, it.length() - 12), it.substring(it.length() - 11, it.length() - 9))
                    key = it.substring(0, it.length() - 15)
                } else if (it ==~ /.+_[a-z][a-z]\.helptext$/) {
                    locale = new Locale(it.substring(it.length() - 11, it.length() - 9))
                    key = it.substring(0, it.length() - 12)
                } else {
                    locale = null
                    key = it.substring(0, it.length() - 9)
                }

                DatabaseMessageSource.loadPageHelpFile(new File(dir, it), locale, key)
            }
        }
    }

    private loadSystemDocumentTypes() {
        createDocumentType(code: 'SI', name: 'Sales Invoice', metaType: 'invoice', analysisIsDebit: false, activity: 'viewarinv', customerAllocate: true, supplierAllocate: false)
        createDocumentType(code: 'SC', name: 'Sales Credit Note', metaType: 'invoice', analysisIsDebit: true, activity: 'viewarinv', customerAllocate: true, supplierAllocate: false)
        createDocumentType(code: 'PI', name: 'Purchase Invoice', metaType: 'invoice', analysisIsDebit: true, activity: 'viewapinv', customerAllocate: false, supplierAllocate: true)
        createDocumentType(code: 'PC', name: 'Purchase Credit Note', metaType: 'invoice', analysisIsDebit: false, activity: 'viewapinv', customerAllocate: false, supplierAllocate: true)
        createDocumentType(code: 'BP', name: 'Bank Payment', metaType: 'cash', analysisIsDebit: true, activity: 'viewbank', customerAllocate: true, supplierAllocate: true)
        createDocumentType(code: 'BR', name: 'Bank Receipt', metaType: 'cash', analysisIsDebit: false, activity: 'viewbank', customerAllocate: true, supplierAllocate: true)
        createDocumentType(code: 'CP', name: 'Cash Payment', metaType: 'cash', analysisIsDebit: true, activity: 'viewcash', customerAllocate: true, supplierAllocate: true)
        createDocumentType(code: 'CR', name: 'Cash Receipt', metaType: 'cash', analysisIsDebit: false, activity: 'viewcash', customerAllocate: true, supplierAllocate: true)
        createDocumentType(code: 'GLJ', name: 'General Ledger Journal', metaType: 'journal', analysisIsDebit: true, activity: 'viewgljnl', customerAllocate: false, supplierAllocate: false)
        createDocumentType(code: 'APJ', name: 'Accounts Payable Journal', metaType: 'journal', analysisIsDebit: true, activity: 'viewapjnl', customerAllocate: false, supplierAllocate: true)
        createDocumentType(code: 'ARJ', name: 'Accounts Receivable Journal', metaType: 'journal', analysisIsDebit: true, activity: 'viewarjnl', customerAllocate: true, supplierAllocate: false)
        createDocumentType(code: 'SOJ', name: 'Inter-Ledger Set-Off Journal', metaType: 'journal', analysisIsDebit: true, activity: 'viewsojnl', customerAllocate: true, supplierAllocate: true)
        createDocumentType(code: 'FJ', name: 'Inter-Ledger Financial Journal', metaType: 'journal', analysisIsDebit: true, activity: 'viewiljnl', customerAllocate: true, supplierAllocate: true)
        createDocumentType(code: 'AC', name: 'Accrual', metaType: 'provision', analysisIsDebit: true, activity: 'viewprovn', customerAllocate: false, supplierAllocate: false)
        createDocumentType(code: 'ACR', name: 'Accrual Reversal', metaType: 'provision', analysisIsDebit: false, activity: 'viewprovn', customerAllocate: false, supplierAllocate: false)
        createDocumentType(code: 'PR', name: 'Prepayment', metaType: 'provision', analysisIsDebit: false, activity: 'viewprovn', customerAllocate: false, supplierAllocate: false)
        createDocumentType(code: 'PRR', name: 'Prepayment Reversal', metaType: 'provision', analysisIsDebit: true, activity: 'viewprovn', customerAllocate: false, supplierAllocate: false)
        createDocumentType(code: 'FXD', name: 'Foreign Currency Difference', metaType: 'journal', analysisIsDebit: true, activity: 'viewfxdjnl', customerAllocate: false, supplierAllocate: false)
        createDocumentType(code: 'FXR', name: 'Foreign Currency Revaluation', metaType: 'journal', analysisIsDebit: true, activity: 'viewfxrjnl', customerAllocate: false, supplierAllocate: false)
    }

    private createDocumentType(map) {
        def acty = SystemActivity.findByCode(map.activity)
        if (!acty) {
            acty = new SystemActivity(code: map.activity)
            acty.saveThis()
        }

        map.activity = acty
        new SystemDocumentType(map).saveThis()
        new SystemMessage(code: "systemDocumentType.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemAccountTypes() {
        createAccountType(code: 'bank', name: 'Bank account', sectionType: 'bs', singleton: false, changeable: true, allowInvoices: false, allowCash: true, allowProvisions: false, allowJournals: true)
        createAccountType(code: 'cash', name: 'Petty cash account', sectionType: 'bs', singleton: false, changeable: true, allowInvoices: false, allowCash: true, allowProvisions: false, allowJournals: true)
        createAccountType(code: 'accrue', name: 'Accruals account', sectionType: 'bs', singleton: true, changeable: true, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: true)
        createAccountType(code: 'prepay', name: 'Prepayments account', sectionType: 'bs', singleton: true, changeable: true, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: true)
        createAccountType(code: 'ar', name: 'Accounts receivable control', sectionType: 'bs', singleton: true, changeable: false, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: false)
        createAccountType(code: 'ap', name: 'Accounts payable control', sectionType: 'bs', singleton: true, changeable: false, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: false)
        createAccountType(code: 'retained', name: 'Retained profits account', sectionType: 'bs', singleton: true, changeable: true, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: true)
        createAccountType(code: 'fxDiff', name: 'Exchange difference account', sectionType: 'ie', singleton: true, changeable: true, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: true)
        createAccountType(code: 'fxRevalue', name: 'Exchange revaluation account', sectionType: 'bs', singleton: true, changeable: true, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: true)
        createAccountType(code: 'glRevalue', name: 'GL revaluation account', sectionType: 'bs', singleton: false, changeable: true, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: true)
        createAccountType(code: 'arRevalue', name: 'AR revaluation account', sectionType: 'bs', singleton: true, changeable: true, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: true)
        createAccountType(code: 'apRevalue', name: 'AP revaluation account', sectionType: 'bs', singleton: true, changeable: true, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: true)
        createAccountType(code: 'tax', name: 'Tax account', sectionType: 'bs', singleton: true, changeable: false, allowInvoices: false, allowCash: false, allowProvisions: false, allowJournals: false)
    }

    private createAccountType(map) {
        new SystemAccountType(map).saveThis()
        new SystemMessage(code: "systemAccountType.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemRoles() {
        createRole(code: 'companyAdmin', name: 'Company Administrator', activities: SystemActivity.findAllBySystemOnly(false))
        createRole(code: 'accountsAdmin', name: 'GL Accounts Administrator', activities: ['general', 'actadmin', 'enquire', 'budgets', 'iereport', 'bsreport',
                'viewarinv', 'viewapinv', 'viewbank', 'viewcash', 'viewfxdjnl', 'viewfxrjnl', 'viewgljnl', 'viewapjnl', 'viewarjnl', 'viewsojnl',
                'viewiljnl', 'viewprovn', 'revalue', 'finjournal', 'gljournal', 'gltemplate', 'provision', 'provntempl', 'taxstmt', 'glreport'])
        createRole(code: 'glEntry', name: 'General Ledger Data Entry', activities: ['general', 'revalue', 'finjournal', 'gljournal', 'enquire',
                'viewgljnl', 'viewiljnl', 'viewprovn', 'viewfxrjnl', 'provision'])
        createRole(code: 'arAdmin', name: 'Accounts Receivable Administrator', activities: ['customer', 'aradmin', 'enquire', 'arinvoice',
                'viewarinv', 'viewarjnl', 'viewfxdjnl', 'viewsojnl', 'viewiljnl', 'artemplate', 'arjournal', 'sojournal', 'sotemplate', 'arreport'])
        createRole(code: 'arInvoice', name: 'Sales Invoice/Cr. Note Entry', activities: ['customer', 'arinvoice', 'enquire',
                'viewarinv', 'viewarjnl', 'viewfxdjnl', 'viewsojnl'])
        createRole(code: 'arJournal', name: 'Accounts Receivable Journal Entry', activities: ['customer', 'arjournal', 'enquire',
                'viewarinv', 'viewarjnl', 'viewfxdjnl', 'viewsojnl'])
        createRole(code: 'apAdmin', name: 'Accounts Payable Administrator', activities: ['supplier', 'apadmin', 'enquire', 'apinvoice',
                'viewapinv', 'viewapjnl', 'viewfxdjnl', 'viewsojnl', 'viewiljnl', 'aptemplate', 'apjournal', 'sojournal', 'sotemplate', 'apreport', 'apremit'])
        createRole(code: 'apInvoice', name: 'Purchase Invoice/Cr. Note Entry', activities: ['supplier', 'apinvoice', 'enquire',
                'viewapinv', 'viewapjnl', 'viewfxdjnl', 'viewsojnl'])
        createRole(code: 'apJournal', name: 'Accounts Payable Journal Entry', activities: ['supplier', 'apjournal', 'enquire',
                'viewapinv', 'viewapjnl', 'viewfxdjnl', 'viewsojnl'])
        createRole(code: 'apRemit', name: 'Accounts Payable Remittances', activities: ['supplier', 'apremit', 'enquire',
                'viewapinv', 'viewapjnl', 'viewfxdjnl', 'viewsojnl'])
        createRole(code: 'soJournal', name: 'Inter-Ledger Set-Off Journal Entry', activities: ['supplier', 'customer', 'sojournal', 'enquire', 'viewsojnl'])
        createRole(code: 'bankAdmin', name: 'Bank and Cash Administrator', activities: ['bank', 'cash', 'bankentry', 'cashentry', 'enquire', 'viewbank', 'viewcash',
                'banktempl', 'cashtempl', 'recurring', 'apremit', 'bankrec'])
        createRole(code: 'bankEntry', name: 'Bank Payment/Receipt Entry', activities: ['bank', 'bankentry', 'enquire', 'viewbank', 'bankrec'])
        createRole(code: 'cashEntry', name: 'Cash Payment/Receipt Entry', activities: ['cash', 'cashentry', 'enquire', 'viewcash'])
        createRole(code: 'translator', name: 'Translator', systemOnly: true, activities: ['systran'])
    }

    private createRole(map) {
        def role = new SystemRole(code: map.code, name: map.name, systemOnly: map.systemOnly ?: false)
        role.saveThis()
		def activity
        for (acty in map.activities) {
            if (acty instanceof SystemActivity) {
                role.addToActivities(acty)
            } else {
				activity = SystemActivity.findByCode(acty)
				activity.refresh()
                role.addToActivities(activity)
            }
        }

        role.save(flush: true)     // With deep validation
        new SystemMessage(code: "role.name.${map.code}", locale: '*', text: map.name).saveThis()
    }

    private loadSystemMenus() {
        new SystemMessage([code: 'menu.main', locale: '*', text: 'Main Menu']).saveThis()
        new SystemMessage([code: 'menu.crumb', locale: '*', text: 'Main']).saveThis()
        createMenu(path: 'System', title: 'System Administration', sequencer: 10, activity: 'sysadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'System.Status', title: 'Status', sequencer: 10, activity: 'sysadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'System.Status.Environment', title: 'Environment', sequencer: 10, activity: 'sysadmin', type: 'action', command: 'system.environment', parameters: null)
        createMenu(path: 'System.Status.Caches', title: 'Cache Statistics', sequencer: 20, activity: 'sysadmin', type: 'action', command: 'system.statistics', parameters: null)
        createMenu(path: 'System.Status.Hibernate', title: 'Hibernate Statistics', sequencer: 30, activity: 'sysadmin', type: 'action', command: 'hibernateStatistics.list', parameters: null)
        createMenu(path: 'System.Status.Connections', title: 'Connection Pool Status', sequencer: 40, activity: 'sysadmin', type: 'action', command: 'connectionPool.list', parameters: null)
        createMenu(path: 'System.Status.Logging', title: 'Logging', sequencer: 50, activity: 'sysadmin', type: 'action', command: 'runtimeLogging.index', parameters: null)
        createMenu(path: 'System.Status.Settings', title: 'Settings', sequencer: 60, activity: 'sysadmin', type: 'action', command: 'systemSetting.list', parameters: null)
        createMenu(path: 'System.Status.Queue', title: 'Task Queue', sequencer: 70, activity: 'sysadmin', type: 'action', command: 'queuedTask.queue', parameters: null)
        createMenu(path: 'System.Status.Operation', title: 'Operation', sequencer: 80, activity: 'sysadmin', type: 'action', command: 'system.operation', parameters: null)
        createMenu(path: 'System.Security', title: 'Security', sequencer: 20, activity: 'sysadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'System.Security.Actions', title: 'Actions', sequencer: 10, activity: 'sysadmin', type: 'action', command: 'systemAction.list', parameters: null)
        createMenu(path: 'System.Security.Activities', title: 'Activities', sequencer: 20, activity: 'sysadmin', type: 'action', command: 'systemActivity.list', parameters: null)
        createMenu(path: 'System.Security.Roles', title: 'Roles', sequencer: 30, activity: 'sysadmin', type: 'action', command: 'systemRole.list', parameters: null)
        createMenu(path: 'System.Security.Menus', title: 'Menus', sequencer: 40, activity: 'sysadmin', type: 'action', command: 'systemMenu.list', parameters: null)
        createMenu(path: 'System.Security.Users', title: 'Users', sequencer: 50, activity: 'sysadmin', type: 'action', command: 'systemUser.list', parameters: null)
        createMenu(path: 'System.Security.Tracing', title: 'Define Tracing', sequencer: 60, activity: 'sysadmin', type: 'action', command: 'systemTracing.list', parameters: null)
        createMenu(path: 'System.Security.Traces', title: 'View Trace Log', sequencer: 70, activity: 'sysadmin', type: 'action', command: 'systemTrace.list', parameters: null)
        createMenu(path: 'System.Companies', title: 'Companies', sequencer: 30, activity: 'sysadmin', type: 'action', command: 'company.list', parameters: null)
        createMenu(path: 'System.Localization', title: 'Localization', sequencer: 40, activity: 'sysadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'System.Localization.Messages', title: 'Messages', sequencer: 10, activity: 'sysadmin', type: 'action', command: 'systemMessage.list', parameters: null)
        createMenu(path: 'System.Localization.Import', title: 'Import', sequencer: 20, activity: 'sysadmin', type: 'action', command: 'systemMessage.imports', parameters: null)
        createMenu(path: 'System.Localization.Export', title: 'Export', sequencer: 30, activity: 'sysadmin', type: 'action', command: 'systemMessage.export', parameters: null)
        createMenu(path: 'System.Localization.PageHelp', title: 'Page Help', sequencer: 40, activity: 'sysadmin', type: 'action', command: 'systemPageHelp.list', parameters: null)
        createMenu(path: 'System.Reference', title: 'Reference Data', sequencer: 50, activity: 'sysadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'System.Reference.Languages', title: 'Languages', sequencer: 10, activity: 'sysadmin', type: 'action', command: 'systemLanguage.list', parameters: null)
        createMenu(path: 'System.Reference.Currencies', title: 'Currencies', sequencer: 20, activity: 'sysadmin', type: 'action', command: 'systemCurrency.list', parameters: null)
        createMenu(path: 'System.Reference.Geos', title: 'Geos', sequencer: 30, activity: 'sysadmin', type: 'action', command: 'systemGeo.list', parameters: null)
        createMenu(path: 'System.Reference.Regions', title: 'Regions', sequencer: 40, activity: 'sysadmin', type: 'action', command: 'systemRegion.list', parameters: null)
        createMenu(path: 'System.Reference.Countries', title: 'Countries', sequencer: 50, activity: 'sysadmin', type: 'action', command: 'systemCountry.list', parameters: null)
        createMenu(path: 'System.Reference.Formats', title: 'Address Formats', sequencer: 60, activity: 'sysadmin', type: 'action', command: 'systemAddressFormat.list', parameters: null)
        createMenu(path: 'System.Reference.CustAddrType', title: 'Customer Address Types', sequencer: 70, activity: 'sysadmin', type: 'action', command: 'systemCustomerAddressType.list', parameters: null)
        createMenu(path: 'System.Reference.CustConType', title: 'Customer Contact Types', sequencer: 80, activity: 'sysadmin', type: 'action', command: 'systemCustomerContactType.list', parameters: null)
        createMenu(path: 'System.Reference.SuppAddrType', title: 'Supplier Address Types', sequencer: 90, activity: 'sysadmin', type: 'action', command: 'systemSupplierAddressType.list', parameters: null)
        createMenu(path: 'System.Reference.SuppConType', title: 'Supplier Contact Types', sequencer: 100, activity: 'sysadmin', type: 'action', command: 'systemSupplierContactType.list', parameters: null)
        createMenu(path: 'System.Reference.Measures', title: 'Measures', sequencer: 110, activity: 'sysadmin', type: 'action', command: 'systemMeasure.list', parameters: null)
        createMenu(path: 'System.Reference.Scales', title: 'Scales', sequencer: 120, activity: 'sysadmin', type: 'action', command: 'systemScale.list', parameters: null)
        createMenu(path: 'System.Reference.Units', title: 'Units', sequencer: 130, activity: 'sysadmin', type: 'action', command: 'systemUnit.list', parameters: null)
        createMenu(path: 'System.Reference.Conversions', title: 'Conversions', sequencer: 140, activity: 'sysadmin', type: 'action', command: 'systemConversion.list', parameters: null)
        createMenu(path: 'System.Reference.Documents', title: 'Document Types', sequencer: 150, activity: 'sysadmin', type: 'action', command: 'systemDocumentType.list', parameters: null)
        createMenu(path: 'System.Reference.Accounts', title: 'Account Types', sequencer: 160, activity: 'sysadmin', type: 'action', command: 'systemAccountType.list', parameters: null)
        createMenu(path: 'System.Reference.Schedules', title: 'Payment Schedules', sequencer: 170, activity: 'sysadmin', type: 'action', command: 'systemPaymentSchedule.list', parameters: null)
        createMenu(path: 'Translation', title: 'Translation', sequencer: 20, activity: 'systran', type: 'action', command: 'translation.translate', parameters: null)
        createMenu(path: 'Company', title: 'Company Administration', sequencer: 30, activity: 'coadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Company.Tasks', title: 'Tasks', sequencer: 10, activity: 'coadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Company.Tasks.Definitions', title: 'Definitions', sequencer: 10, activity: 'coadmin', type: 'action', command: 'task.list', parameters: null)
        createMenu(path: 'Company.Tasks.Queue', title: 'Queue', sequencer: 20, activity: 'coadmin', type: 'action', command: 'queuedTask.list', parameters: null)
        createMenu(path: 'Company.Localization', title: 'Localization', sequencer: 20, activity: 'coadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Company.Localization.Messages', title: 'Messages', sequencer: 10, activity: 'coadmin', type: 'action', command: 'message.list', parameters: null)
        createMenu(path: 'Company.Localization.Import', title: 'Import', sequencer: 20, activity: 'coadmin', type: 'action', command: 'message.imports', parameters: null)
        createMenu(path: 'Company.Localization.Export', title: 'Export', sequencer: 30, activity: 'coadmin', type: 'action', command: 'message.export', parameters: null)
        createMenu(path: 'Company.Reference', title: 'Reference Data', sequencer: 30, activity: 'coadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Company.Reference.TaxAuthorities', title: 'Tax Authorities', sequencer: 10, activity: 'coadmin', type: 'action', command: 'taxAuthority.list', parameters: null)
        createMenu(path: 'Company.Reference.TaxCodes', title: 'Tax Codes', sequencer: 20, activity: 'coadmin', type: 'action', command: 'taxCode.list', parameters: null)
        createMenu(path: 'Company.Reference.Currencies', title: 'Currencies', sequencer: 30, activity: 'coadmin', type: 'action', command: 'exchangeCurrency.list', parameters: null)
        createMenu(path: 'Company.Reference.Measures', title: 'Measures', sequencer: 40, activity: 'coadmin', type: 'action', command: 'measure.list', parameters: null)
        createMenu(path: 'Company.Reference.Scales', title: 'Scales', sequencer: 50, activity: 'coadmin', type: 'action', command: 'scale.list', parameters: null)
        createMenu(path: 'Company.Reference.Units', title: 'Units', sequencer: 60, activity: 'coadmin', type: 'action', command: 'unit.list', parameters: null)
        createMenu(path: 'Company.Reference.Conversions', title: 'Conversions', sequencer: 70, activity: 'coadmin', type: 'action', command: 'conversion.list', parameters: null)
        createMenu(path: 'Company.Reference.Schedules', title: 'Payment Schedules', sequencer: 80, activity: 'coadmin', type: 'action', command: 'paymentSchedule.list', parameters: null)
        createMenu(path: 'Company.Reference.Documents', title: 'Document Types', sequencer: 90, activity: 'coadmin', type: 'action', command: 'documentType.list', parameters: null)
        createMenu(path: 'Company.Configuration', title: 'Configuration', sequencer: 40, activity: 'coadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Company.Configuration.Settings', title: 'Settings', sequencer: 10, activity: 'coadmin', type: 'action', command: 'setting.list', parameters: null)
        createMenu(path: 'Company.Configuration.Company', title: 'Company', sequencer: 20, activity: 'coadmin', type: 'action', command: 'company.details', parameters: null)
        createMenu(path: 'Company.Configuration.Logo', title: 'Logo', sequencer: 30, activity: 'coadmin', type: 'action', command: 'company.logo', parameters: null)
        createMenu(path: 'Company.Security', title: 'Security', sequencer: 50, activity: 'coadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Company.Security.AccessCodes', title: 'Access Codes', sequencer: 10, activity: 'coadmin', type: 'action', command: 'accessCode.list', parameters: null)
        createMenu(path: 'Company.Security.AccessGroups', title: 'Access Groups', sequencer: 20, activity: 'coadmin', type: 'action', command: 'accessGroup.list', parameters: null)
        createMenu(path: 'Company.Security.Roles', title: 'Roles', sequencer: 30, activity: 'coadmin', type: 'action', command: 'systemRole.listing', parameters: null)
        createMenu(path: 'Company.Security.Users', title: 'Users', sequencer: 40, activity: 'coadmin', type: 'action', command: 'companyUser.display', parameters: null)

        createMenu(path: 'General', title: 'General Ledger', sequencer: 40, activity: 'general', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'General.Admin', title: 'Accounts Administration', sequencer: 10, activity: 'actadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'General.Admin.Elements', title: 'Code Elements', sequencer: 10, activity: 'actadmin', type: 'action', command: 'codeElement.list', parameters: null)
        createMenu(path: 'General.Admin.Chart', title: 'Chart of Accounts', sequencer: 20, activity: 'actadmin', type: 'action', command: 'chartSection.list', parameters: null)
        createMenu(path: 'General.Admin.Accounts', title: 'Accounts', sequencer: 30, activity: 'actadmin', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'General.Admin.Accounts.Manual', title: 'Manual Account Maintenance', sequencer: 10, activity: 'actadmin', type: 'action', command: 'account.list', parameters: null)
        createMenu(path: 'General.Admin.Accounts.Bulk', title: 'Bulk Account Creation', sequencer: 20, activity: 'actadmin', type: 'action', command: 'account.bulk', parameters: null)
        createMenu(path: 'General.Admin.Accounts.Imports', title: 'Import Account Codes', sequencer: 30, activity: 'actadmin', type: 'action', command: 'account.imports', parameters: null)
        createMenu(path: 'General.Admin.Accounts.Test', title: 'Test Account Auto-Creation', sequencer: 40, activity: 'actadmin', type: 'action', command: 'account.test', parameters: null)
        createMenu(path: 'General.Admin.Years', title: 'Years and Periods', sequencer: 40, activity: 'actadmin', type: 'action', command: 'year.list', parameters: null)
        createMenu(path: 'General.Admin.OpenPeriod', title: 'Open New Accounting Period', sequencer: 50, activity: 'actadmin', type: 'action', command: 'period.open', parameters: null)
        createMenu(path: 'General.Admin.ClosePeriod', title: 'Close Old Accounting Period', sequencer: 60, activity: 'actadmin', type: 'action', command: 'period.close', parameters: null)
        createMenu(path: 'General.Budgets', title: 'Budgets', sequencer: 20, activity: 'budgets', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'General.Budgets.Manual', title: 'Manual Budget Entry', sequencer: 10, activity: 'budgets', type: 'action', command: 'budget.filterSettings', parameters: 'target:list')
        createMenu(path: 'General.Budgets.Adjust', title: 'Budget Adjustment', sequencer: 20, activity: 'budgets', type: 'action', command: 'budget.adjust', parameters: null)
        createMenu(path: 'General.Budgets.Imports', title: 'Import Budget Values', sequencer: 30, activity: 'budgets', type: 'action', command: 'budget.imports', parameters: null)
        createMenu(path: 'General.Reports', title: 'Financial Reports', sequencer: 30, activity: 'glreport', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'General.Reports.DetailedPostings', title: 'Detailed Postings', sequencer: 10, activity: 'glreport', type: 'action', command: 'generalBalance.detailedPostings', parameters: null)
        createMenu(path: 'General.Reports.TrialBalance', title: 'Trial Balance', sequencer: 20, activity: 'glreport', type: 'action', command: 'generalBalance.trialBalance', parameters: null)
        createMenu(path: 'General.Reports.IncomeFormat', title: 'Income and Expenditure Report Formats', sequencer: 30, activity: 'actadmin', type: 'action', command: 'profitReportFormat.list', parameters: null)
        createMenu(path: 'General.Reports.IncomeReport', title: 'Income and Expenditure Report', sequencer: 40, activity: 'iereport', type: 'action', command: 'generalBalance.incomeReport', parameters: null)
        createMenu(path: 'General.Reports.BalanceFormat', title: 'Balance Sheet Report Formats', sequencer: 50, activity: 'actadmin', type: 'action', command: 'balanceReportFormat.list', parameters: null)
        createMenu(path: 'General.Reports.BalanceReport', title: 'Balance Sheet Report', sequencer: 60, activity: 'bsreport', type: 'action', command: 'generalBalance.balanceReport', parameters: null)
        createMenu(path: 'General.Templates', title: 'Document Templates', sequencer: 40, activity: 'gltemplate', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'General.Templates.Financial', title: 'Financial (Inter-Ledger) Journal Templates', sequencer: 10, activity: 'gltemplate', type: 'action', command: 'financial.list', parameters: null)
        createMenu(path: 'General.Templates.Journal', title: 'General Ledger Journal Templates', sequencer: 20, activity: 'gltemplate', type: 'action', command: 'general.list', parameters: null)
        createMenu(path: 'General.Templates.Provision', title: 'Accrual/Prepayment Templates', sequencer: 30, activity: 'provntempl', type: 'action', command: 'provision.list', parameters: null)
        createMenu(path: 'General.Taxes', title: 'Taxation Statements', sequencer: 50, activity: 'taxstmt', type: 'action', command: 'taxStatement.list', parameters: null)
        createMenu(path: 'General.Revalue', title: 'Foreign Currency Revaluation', sequencer: 60, activity: 'revalue', type: 'action', command: 'revaluation.revalue', parameters: null)
        createMenu(path: 'General.Financial', title: 'Financial (Inter-Ledger) Journal', sequencer: 70, activity: 'finjournal', type: 'action', command: 'financial.journal', parameters: null)
        createMenu(path: 'General.Journal', title: 'General Ledger Journal', sequencer: 80, activity: 'gljournal', type: 'action', command: 'general.journal', parameters: null)
        createMenu(path: 'General.Provision', title: 'Accrual/Prepayment Entry', sequencer: 90, activity: 'provision', type: 'action', command: 'provision.provide', parameters: null)
        createMenu(path: 'General.Enquiry', title: 'Account Enquiry', sequencer: 100, activity: 'enquire', type: 'action', command: 'account.enquire', parameters: null)

        createMenu(path: 'Customer', title: 'Accounts Receivable', sequencer: 50, activity: 'customer', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Customer.Accounts', title: 'Customer Account Maintenance', sequencer: 10, activity: 'aradmin', type: 'action', command: 'customer.list', parameters: null)
        createMenu(path: 'Customer.Reports', title: 'Reports', sequencer: 20, activity: 'arreport', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Customer.Reports.List', title: 'Customer List', sequencer: 10, activity: 'arreport', type: 'action', command: 'customer.print', parameters: null)
        createMenu(path: 'Customer.Reports.Aged', title: 'Aged List of Debtors', sequencer: 20, activity: 'arreport', type: 'action', command: 'customer.aged', parameters: null)
        createMenu(path: 'Customer.Reports.Statements', title: 'Customer Statements', sequencer: 30, activity: 'arreport', type: 'action', command: 'customer.statements', parameters: null)
        createMenu(path: 'Customer.Reports.Reprint', title: 'Customer Statement Reprint', sequencer: 40, activity: 'arreport', type: 'action', command: 'customer.reprint', parameters: null)
        createMenu(path: 'Customer.Templates', title: 'Document Templates', sequencer: 30, activity: 'artemplate', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Customer.Templates.Invoices', title: 'Sales Invoice/Cr. Note Templates', sequencer: 10, activity: 'artemplate', type: 'action', command: 'sales.list', parameters: null)
        createMenu(path: 'Customer.Templates.Journal', title: 'Accounts Receivable Journal Templates', sequencer: 20, activity: 'artemplate', type: 'action', command: 'receivable.list', parameters: null)
        createMenu(path: 'Customer.Templates.SetOff', title: 'Inter-Ledger Set-Off Journal Templates', sequencer: 30, activity: 'sotemplate', type: 'action', command: 'setoff.list', parameters: null)
        createMenu(path: 'Customer.Invoices', title: 'Sales Invoice/Cr. Note Entry', sequencer: 40, activity: 'arinvoice', type: 'action', command: 'sales.invoice', parameters: null)
        createMenu(path: 'Customer.Journal', title: 'Accounts Receivable Journal Entry', sequencer: 50, activity: 'arjournal', type: 'action', command: 'receivable.journal', parameters: null)
        createMenu(path: 'Customer.SetOff', title: 'Inter-Ledger Set-Off Journal Entry', sequencer: 60, activity: 'sojournal', type: 'action', command: 'setoff.journal', parameters: null)
        createMenu(path: 'Customer.Enquiry', title: 'Customer Account Enquiry', sequencer: 70, activity: 'enquire', type: 'action', command: 'customer.enquire', parameters: null)

        createMenu(path: 'Supplier', title: 'Accounts Payable', sequencer: 60, activity: 'supplier', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Supplier.Accounts', title: 'Supplier Account Maintenance', sequencer: 10, activity: 'apadmin', type: 'action', command: 'supplier.list', parameters: null)
        createMenu(path: 'Supplier.Reports', title: 'Reports', sequencer: 20, activity: 'apreport', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Supplier.Reports.List', title: 'Supplier List', sequencer: 10, activity: 'apreport', type: 'action', command: 'supplier.print', parameters: null)
        createMenu(path: 'Supplier.Reports.Aged', title: 'Aged List of Creditors', sequencer: 20, activity: 'apreport', type: 'action', command: 'supplier.aged', parameters: null)
        createMenu(path: 'Supplier.Reports.Reprint', title: 'Remittance Advice Reprint', sequencer: 30, activity: 'apreport', type: 'action', command: 'supplier.reprint', parameters: null)
        createMenu(path: 'Supplier.Templates', title: 'Document Templates', sequencer: 30, activity: 'aptemplate', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Supplier.Templates.Invoices', title: 'Purchase Invoice/Cr. Note Templates', sequencer: 10, activity: 'aptemplate', type: 'action', command: 'purchase.list', parameters: null)
        createMenu(path: 'Supplier.Templates.Journal', title: 'Accounts Payable Journal Templates', sequencer: 20, activity: 'aptemplate', type: 'action', command: 'payable.list', parameters: null)
        createMenu(path: 'Supplier.Templates.SetOff', title: 'Inter-Ledger Set-Off Journal Templates', sequencer: 30, activity: 'sotemplate', type: 'action', command: 'setoff.list', parameters: null)
        createMenu(path: 'Supplier.Invoices', title: 'Purchase Invoice/Cr. Note Entry', sequencer: 40, activity: 'apinvoice', type: 'action', command: 'purchase.invoice', parameters: null)
        createMenu(path: 'Supplier.Journal', title: 'Accounts Payable Journal Entry', sequencer: 50, activity: 'apjournal', type: 'action', command: 'payable.journal', parameters: null)
        createMenu(path: 'Supplier.SetOff', title: 'Inter-Ledger Set-Off Journal Entry', sequencer: 60, activity: 'sojournal', type: 'action', command: 'setoff.journal', parameters: null)
        createMenu(path: 'Supplier.Remittances', title: 'Remittance Advices', sequencer: 70, activity: 'apremit', type: 'action', command: 'remittance.list', parameters: null)
        createMenu(path: 'Supplier.Enquiry', title: 'Supplier Account Enquiry', sequencer: 80, activity: 'enquire', type: 'action', command: 'supplier.enquire', parameters: null)

        createMenu(path: 'Bank', title: 'Bank', sequencer: 70, activity: 'bank', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Bank.Recurring', title: 'Recurring Transactions', sequencer: 10, activity: 'recurring', type: 'action', command: 'recurring.list', parameters: null)
        createMenu(path: 'Bank.Templates', title: 'Document Templates', sequencer: 20, activity: 'banktempl', type: 'action', command: 'bank.list', parameters: null)
        createMenu(path: 'Bank.Transact', title: 'Bank Payment/Receipt Entry', sequencer: 30, activity: 'bankentry', type: 'action', command: 'bank.transact', parameters: null)
        createMenu(path: 'Bank.Release', title: 'Release Auto-Payments', sequencer: 40, activity: 'apremit', type: 'action', command: 'remittance.release', parameters: null)
        createMenu(path: 'Bank.Reconcile', title: 'Bank Reconciliation', sequencer: 50, activity: 'bankrec', type: 'action', command: 'reconciliation.list', parameters: null)

        createMenu(path: 'Cash', title: 'Cash', sequencer: 80, activity: 'cash', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'Cash.Templates', title: 'Document Templates', sequencer: 10, activity: 'cashtempl', type: 'action', command: 'cash.list', parameters: null)
        createMenu(path: 'Cash.Transact', title: 'Cash Payment/Receipt Entry', sequencer: 20, activity: 'cashentry', type: 'action', command: 'cash.transact', parameters: null)

        createMenu(path: 'Search', title: 'Document Search', sequencer: 90, activity: 'enquire', type: 'action', command: 'document.search', parameters: null)

        createMenu(path: 'User', title: 'User Administration', sequencer: 100, activity: 'attached', type: 'submenu', command: null, parameters: null)
        createMenu(path: 'User.Queue', title: 'Task Queue', sequencer: 10, activity: 'attached', type: 'action', command: 'queuedTask.usrList', parameters: null)
        createMenu(path: 'User.Profile', title: 'Profile', sequencer: 20, activity: 'attached', type: 'action', command: 'systemUser.profile', parameters: null)
        createMenu(path: 'User.Company', title: 'Change Company', sequencer: 30, activity: 'attached', type: 'action', command: 'companyUser.change', parameters: null)
        createMenu(path: 'User.Mnemonics', title: 'GL Account Code Mnemonics', sequencer: 40, activity: 'login', type: 'action', command: 'mnemonic.list', parameters: null)
    }

    private createMenu(map) {
        def temp = SystemActivity.findByCode(map.activity)
        if (!temp) {
            temp = new SystemActivity(code: map.activity)
            temp.saveThis()
        }

        map.activity = temp
		temp = new SystemMenu(map)
        if (map.path.contains('.')) temp.parentObject = SystemMenu.findByPathAndType(map.path.substring(0, map.path.lastIndexOf('.')), 'submenu')
        temp.saveThis()
        new SystemMessage(code: "menu.option.${map.path}", locale: '*', text: map.title).saveThis()
        new SystemMessage(code: "menu.crumb.${map.path}", locale: '*', text: (map.path.indexOf('.') == -1) ? map.path : map.path.substring(map.path.lastIndexOf('.') + 1)).saveThis()
        if (map.type == 'submenu' && map.parameters) new SystemMessage(code: "menu.submenu.${map.path}", locale: '*', text: map.parameters).saveThis()
    }
}

