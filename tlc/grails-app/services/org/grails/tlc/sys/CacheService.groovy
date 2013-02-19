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

class CacheService {

    public static final IMPOSSIBLE_VALUE = '\b'
    public static final DUMMY_VALUE = '\t'
    public static final DYNAMIC_DATA_SIZE = -1
    public static final LENGTH_DATA_SIZE = -2
    public static final KEY_FULL = 1
    public static final KEY_FIRST = 2
    public static final KEY_LAST = 3
    public static final KEY_FIRST_OR_LAST = 4
    public static final COMPANY_INSENSITIVE = Long.MIN_VALUE
    private static final cacheMap = [:]
    private static final listOfCaches = [
            [code: 'conversion', key: KEY_FIRST_OR_LAST, dataSize: 200, maxKB: 32],                     // unitCode | unitCode
            [code: 'exchangeRate', key: KEY_FIRST, dataSize: 20, maxKB: 16],                            // currencyCode | dateAsMillis
            [code: 'message', key: KEY_FIRST, dataSize: LENGTH_DATA_SIZE, maxKB: 512],                  // messageCode | localeLanguageLocaleCountry
            [code: 'setting', key: KEY_FULL, dataSize: DYNAMIC_DATA_SIZE, maxKB: 8],                    // settingCode
            [code: 'actionActivity', key: KEY_FULL, dataSize: LENGTH_DATA_SIZE, maxKB: 48],             // 'controller.action'
            [code: 'userActivity', key: KEY_FIRST_OR_LAST, dataSize: LENGTH_DATA_SIZE, maxKB: 48],      // userId | activityCode (Also sensitive to CompanyUser)
            [code: 'menuCrumb', key: KEY_FULL, dataSize: DYNAMIC_DATA_SIZE, maxKB: 16],                 // menuOptionId
            [code: 'pageHelp', key: KEY_FIRST, dataSize: LENGTH_DATA_SIZE, maxKB: 32],                  // pageHelpCode | localeLanguageLocaleCountry
            [code: 'mnemonic', key: KEY_FIRST, dataSize: LENGTH_DATA_SIZE, maxKB: 16],                  // userId | mnemonicCode
            [code: 'ranges', key: KEY_FULL, dataSize: DYNAMIC_DATA_SIZE, maxKB: 32],                    // sectionCode
            [code: 'account', key: KEY_FULL, dataSize: LENGTH_DATA_SIZE, maxKB: 64],                    // accountCode (unexpanded for defaults)
            [code: 'accessGroup', key: KEY_FULL, dataSize: DYNAMIC_DATA_SIZE, maxKB: 16],               // accessGroupCode
            [code: 'userAccessGroup', key: KEY_FULL, dataSize: DYNAMIC_DATA_SIZE, maxKB: 8],            // userId (Also sensitive to CompanyUser)
            [code: 'userAccount', key: KEY_FIRST_OR_LAST, dataSize: LENGTH_DATA_SIZE, maxKB: 1024],     // userId | accountCode (Also sensitive to CompanyUser)
            [code: 'userCustomer', key: KEY_FIRST_OR_LAST, dataSize: LENGTH_DATA_SIZE, maxKB: 32],      // userId | customerAccessCode (Also sensitive to CompanyUser)
            [code: 'userSupplier', key: KEY_FIRST_OR_LAST, dataSize: LENGTH_DATA_SIZE, maxKB: 32]       // userId | supplierAccessCode (Also sensitive to CompanyUser)
    ]

    static transactional = false

    static createCaches(grailsApplication) {

        synchronized (cacheMap) {
            if (cacheMap.size() == 0) {
                def entry, size
                for (it in listOfCaches) {
                    entry = new MapEntry()
                    size = grailsApplication.config."${it.code}".cache.size.kb
                    if (size != null && size instanceof Integer && size >= 0 && size <= 1024 * 1024) {
                        entry.maxCacheSize = size * 1024L
                    } else {
                        entry.maxCacheSize = it.maxKB * 1024L
                    }

                    entry.keyStructure = it.key
                    entry.dataSize = it.dataSize ?: DYNAMIC_DATA_SIZE
                    entry.dependents = it.dependents

                    cacheMap.put(it.code, entry)
                }
            }
        }
    }

    // Note that this method will return IMPOSSIBLE_VALUE if this is a known
    // missing key. A value of null will be returned if this key has never been
    // asked for before and cannot be found. It is up to the caller to then
    // decide if they wish to put the value in the cache as a known missing key.
    // The countFailure parameter is usually only set to false when performing a
    // hierarchical search such as the 'message' system would do, in which case
    // it is unreasonable to count any but the last failure
    def get(cacheCode, securityCode, key, countFailure = true) {
        def entry = cacheMap.get(cacheCode)
        if (!entry || !entry.maxCacheSize) return null

        def data
        synchronized (entry) {
            data = entry.cache.get(securityCode + IMPOSSIBLE_VALUE + key)?.data
            if (data != null) {
                entry.cacheHits++
            } else if (countFailure) {
                entry.cacheMisses++
            }
        }

        return data
    }

    // Note that this method will automatically substitute IMPOSSIBLE_VALUE if
    // it is given null as the 'value' parameter (in other words, a cache
    // cannot have null as a valid value
    def put(cacheCode, securityCode, key, value, length = 0) {
        def entry = cacheMap.get(cacheCode)
        if (!entry || !entry.maxCacheSize) return null

        key = securityCode + IMPOSSIBLE_VALUE + key
        if (value == null) value = IMPOSSIBLE_VALUE

        def val = new CacheValue()
        val.length = entry.sizeof(cacheCode, key, value, length)
        val.data = value

        def prev
        synchronized (entry) {
            prev = entry.cache.put(key, val)
            if (prev) entry.currentCacheSize -= prev.length

            entry.currentCacheSize += val.length

            // Adjust the cache size if required
            adjustCacheToSize(entry)
        }

        return prev
    }

    // Company sensitive clearance of a cache
    def resetAll(cacheCode, securityCode) {
        def entry = cacheMap.get(cacheCode)
        if (!entry) return

        if (entry.maxCacheSize) {
            def code = securityCode + IMPOSSIBLE_VALUE
            synchronized (entry) {
                def val
                def entries = entry.cache.entrySet().iterator()
                while (entries.hasNext()) {
                    val = entries.next()
                    if (val.key.startsWith(code)) {
                        entry.currentCacheSize -= val.value.length
                        entries.remove()
                    }
                }
            }
        }

        if (entry.dependents) {
            for (it in entry.dependents) resetAll(it, securityCode)
        }
    }

    // Company sensitive removal of a specific key from a cache. Can be made
    // company insensitive if the security code is the constant COMPANY_INSENSITIVE
    def resetThis(cacheCode, securityCode, key) {
        def entry = cacheMap.get(cacheCode)
        if (!entry) return

        // If the cache is active
        if (entry.maxCacheSize) {

            // Encode the key since it will be used in a regular expression pattern
            key = key.encodeAsRegex()

            // Create the start of the pattern based upon company sensitivity
            def pattern = (securityCode == COMPANY_INSENSITIVE) ? '[0-9]+' : "${securityCode}"
            pattern += IMPOSSIBLE_VALUE

            // Create the rest of the pattern based on the key structure of the cache
            if (entry.keyStructure == KEY_FIRST) {
                pattern += key + IMPOSSIBLE_VALUE + '.*'
            } else if (entry.keyStructure == KEY_LAST) {
                pattern += '.*' + IMPOSSIBLE_VALUE + key
            } else if (entry.keyStructure == KEY_FIRST_OR_LAST) {
                pattern += '((' + key + IMPOSSIBLE_VALUE + '.*)|(.*' + IMPOSSIBLE_VALUE + key + '))'
            } else {    // Full key
                pattern += key
            }

            // Actually turn the string in to a pattern
            pattern = ~pattern

            // Work through the cache removing everything that matches the pattern
            synchronized (entry) {
                def val
                def entries = entry.cache.entrySet().iterator()
                while (entries.hasNext()) {
                    val = entries.next()
                    if (val.key ==~ pattern) {
                        entry.currentCacheSize -= val.value.length
                        entries.remove()
                    }
                }
            }
        }

        // Process any dependent caches
        if (entry.dependents) {
            for (it in entry.dependents) resetThis(it, securityCode, key)
        }
    }

    // Company sensitive clearance of a cache by VALUE rather than by KEY. The value to be removed must
    // be testable by == (e.g. a String or an Integer etc).
    def resetByValue(cacheCode, securityCode, value) {
        def entry = cacheMap.get(cacheCode)
        if (!entry) return

        if (entry.maxCacheSize) {
            def code = securityCode + IMPOSSIBLE_VALUE
            synchronized (entry) {
                def val
                def entries = entry.cache.entrySet().iterator()
                while (entries.hasNext()) {
                    val = entries.next()
                    if (val.key.startsWith(code) && val.value.data == value) {
                        entry.currentCacheSize -= val.value.length
                        entries.remove()
                    }
                }
            }
        }

        if (entry.dependents) {
            for (it in entry.dependents) resetByValue(it, securityCode, value)
        }
    }

    // Global clearance of all caches for all companies
    def clearAll() {
        def entry
        for (it in listOfCaches) {
            entry = cacheMap.get(it.code)
            synchronized (entry) {
                entry.cache.clear()
                entry.currentCacheSize = 0L
                entry.cacheHits = 0L
                entry.cacheMisses = 0L
            }
        }
    }

    // Global clearance of all caches for a specific company
    def clearAll(securityCode) {
        for (it in listOfCaches) resetAll(it.code, securityCode)
    }

    // Global clearance of a specific cache (for all companies)
    def clearThis(cacheCode) {
        def entry = cacheMap.get(cacheCode)
        if (!entry) return

        synchronized (entry) {
            entry.cache.clear()
            entry.currentCacheSize = 0L
            entry.cacheHits = 0L
            entry.cacheMisses = 0L
        }

        if (entry.dependents) {
            for (it in entry.dependents) clearThis(it)
        }
    }

    // Global statistics for all caches (for all companies)
    def statistics() {

        def list = []
        def entry, stats, actual, entries, val
        for (it in listOfCaches) {
            entry = cacheMap.get(it.code)
            stats = [:]
            stats.code = it.code
            actual = 0
            synchronized (entry) {
                stats.max = entry.maxCacheSize
                stats.size = entry.currentCacheSize
                stats.count = entry.cache.size()
                stats.hits = entry.cacheHits
                stats.misses = entry.cacheMisses
                entries = entry.cache.entrySet().iterator()
                while (entries.hasNext()) {
                    val = entries.next()
                    actual += val.value.length
                }
            }

            stats.actual = actual
            if (stats.actual != stats.size) {
                log.error("Cache '${stats.code}' size mismatch: Computed = ${stats.size}, Actual = ${stats.actual}")
            }

            list.add(stats)
        }

        list.sort {it.code}

        return list
    }

    // Global resize of a specified cache (for all companies)
    def resize(cacheCode, maxKB) {
        def entry = cacheMap.get(cacheCode)
        if (!entry || maxKB < 0 || maxKB > 1024 * 1024 || maxKB * 1024 == entry.maxCacheSize) return
        synchronized (entry) {
            if (maxKB) {
                entry.maxCacheSize = maxKB * 1024L
                adjustCacheToSize(entry)
            } else {
                entry.maxCacheSize = 0L
                entry.cache.clear()
                entry.currentCacheSize = 0L
                entry.cacheHits = 0L
                entry.cacheMisses = 0L
            }
        }
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private adjustCacheToSize(entry) {
        if (entry.currentCacheSize > entry.maxCacheSize) {
            def val
            def entries = entry.cache.entrySet().iterator()
            while (entries.hasNext() && entry.currentCacheSize > entry.maxCacheSize) {
                val = entries.next()
                entry.currentCacheSize -= val.value.length
                entries.remove()
            }
        }
    }
}

class MapEntry {
    def keyStructure
    def maxCacheSize
    def dataSize
    def dependents
    def currentCacheSize = 0L
    def cacheHits = 0L
    def cacheMisses = 0L
    def cache = new LinkedHashMap((int) 16, (float) 0.75, (boolean) true)

    def sizeof(code, key, data, length) {
        if (length > 0) return length + key.length() + 16
        if (dataSize > 0) return dataSize + key.length() + 16
        if (dataSize == CacheService.LENGTH_DATA_SIZE) return data.length() + key.length() + 16
        throw new IllegalArgumentException('Missing data size parameter for cache ' + code)
    }
}

class CacheValue {
    def length
    def data
}
