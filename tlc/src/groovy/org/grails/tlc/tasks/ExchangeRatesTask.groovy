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

import org.grails.tlc.corp.ExchangeCurrency
import org.grails.tlc.corp.ExchangeRate
import org.grails.tlc.sys.TaskExecutable

class ExchangeRatesTask extends TaskExecutable {

    def execute() {
        def today = utilService.fixDate()
        def currencies = []
        for (it in ExchangeCurrency.findAllByAutoUpdate(true, [sort: 'code', order: 'asc'])) {
            def cur = ExchangeRate.findByCurrencyAndValidFrom(it, today)
            if (!cur) currencies << it // Only get rate if no rate currently exists
        }

        def code = ''
        def rate = null
        def retrieved = 0
        def failed = 0
        def created = 0
        def er
        for (currency in currencies) {
            yield()
            if (currency.code != code) {
                code = currency.code
                rate = utilService.readExchangeRate(code)
                if (rate) {
                    retrieved++
                } else {
                    failed++
                }
            }

            if (rate) {
                er = new ExchangeRate()
                er.currency = currency
                er.validFrom = today
                er.rate = rate
                if (er.saveThis()) {
                    utilService.cacheService.resetThis('exchangeRate', currency.securityCode, currency.code)
                    created++
                } else {
                    failed++
                }
            }
        }

        results.retrieved = retrieved
        results.failed = failed
        results.created = created

        if (failed) {
            completionMessage = message(code: 'exchangeCurrency.autoUpdate.task.failures', args: [failed], default: "${failed} rates could not be retrieved from the Internet")
        }
    }
}

