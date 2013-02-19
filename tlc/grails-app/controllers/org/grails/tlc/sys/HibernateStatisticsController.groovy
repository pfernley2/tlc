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

import java.text.NumberFormat

class HibernateStatisticsController {

    // Injected services
    def utilService
    def sessionFactory

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [enable: 'POST', disable: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        def statistics = sessionFactory.statistics
        def enabled = statistics.isStatisticsEnabled()
        def tables = []
        if (enabled) {
            def secondHits = statistics.secondLevelCacheHitCount
            def secondMisses = statistics.secondLevelCacheMissCount
            def secondPuts = statistics.secondLevelCachePutCount
            def queryHits = statistics.queryCacheHitCount
            def queryMisses = statistics.queryCacheMissCount
            def queryPuts = statistics.queryCachePutCount
            def rows = []
            rows << [name: msg(code: 'hibernate.second.hit', default: 'Second Level Cache Hits'), value: makePercentage(secondHits, secondHits + secondMisses)]
            rows << [name: msg(code: 'hibernate.second.miss', default: 'Second Level Cache Misses'), value: makePercentage(secondMisses, secondHits + secondMisses)]
            rows << [name: msg(code: 'hibernate.second.put', default: 'Second Level Cache Puts'), value: makeRatio(secondPuts, secondHits + secondMisses)]
            rows << [name: msg(code: 'hibernate.query.hit', default: 'Query Cache Hits'), value: makePercentage(queryHits, queryHits + queryMisses)]
            rows << [name: msg(code: 'hibernate.query.miss', default: 'Query Cache Misses'), value: makePercentage(queryMisses, queryHits + queryMisses)]
            rows << [name: msg(code: 'hibernate.query.put', default: 'Query Cache Puts'), value: makeRatio(queryPuts, queryHits + queryMisses)]

            tables << [name: msg(code: 'hibernate.summary', default: 'Summary'), rows: rows]

            def regions = statistics.secondLevelCacheRegionNames
            if (regions) {
                regions = regions.sort {it}
                def stats, hits, misses, puts, disk, memory, size
                for (it in regions) {
                    stats = statistics.getSecondLevelCacheStatistics(it)
                    rows = []

                    hits = stats.hitCount
                    misses = stats.missCount
                    puts = stats.putCount
                    disk = stats.elementCountOnDisk
                    memory = stats.elementCountInMemory
                    size = stats.sizeInMemory

                    rows << [name: msg(code: 'hibernate.region.hit', default: 'Hits'), value: makePercentage(hits, hits + misses)]
                    rows << [name: msg(code: 'hibernate.region.miss', default: 'Misses'), value: makePercentage(misses, hits + misses)]
                    rows << [name: msg(code: 'hibernate.region.put', default: 'Puts'), value: makeRatio(puts, hits + misses)]
                    rows << [name: msg(code: 'hibernate.region.disk.count', default: 'On-Disk Count'), value: format(disk, 0)]
                    rows << [name: msg(code: 'hibernate.region.memory.count', default: 'In-Memory Count'), value: format(memory, 0)]
                    rows << [name: msg(code: 'hibernate.region.memory.size', default: 'In Memory Size (KB)'), value: format(size <= 0 ? size : size / 1024, 2)]

                    tables << [name: msg(code: 'hibernate.region', args: [it], default: "Region ${it}"), rows: rows]
                }
            }
        }

        [enabled: enabled, tables: tables]
    }

    def enable() {
        def statistics = sessionFactory.statistics
        statistics.clear()
        statistics.setStatisticsEnabled(true)
        redirect(action: 'list')
    }

    def disable() {
        def statistics = sessionFactory.statistics
        statistics.setStatisticsEnabled(false)
        statistics.clear()
        redirect(action: 'list')
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private format(value, decimals) {
        if (value < 0) return msg(code: 'generic.not.applicable', default: 'n/a')
        return utilService.format(value, decimals, true).encodeAsHTML()
    }

    private makePercentage(val, tot) {
        def text = utilService.format(val, 0, true)
        if (tot > 0) {
            val = utilService.round((val * 100) / tot, 2)
            if (val) {
                def format = NumberFormat.getPercentInstance(utilService.currentLocale())
                format.setMinimumIntegerDigits(1)
                format.setMinimumFractionDigits(2)
                text += " (${format.format(val / 100.0)})"
            }
        }

        return text.encodeAsHTML()
    }

    private makeRatio(val1, val2) {
        if (val1 < 0) return msg(code: 'generic.not.applicable', default: 'n/a')
        def text = utilService.format(val1, 0, true)
        if (val1 > 0 && val2 > 0) {
            def decs = 2
            val1 = utilService.round(val2 / val1, decs)
            if (val1) {
                if (val1 == utilService.round(val1, 0)) {
                    decs = 0
                } else if (val1 == utilService.round(val1, 1)) {
                    decs = 1
                }

                text += " (1:${utilService.format(val1, decs, false)})"
            }
        }

        return text.encodeAsHTML()
    }
}
