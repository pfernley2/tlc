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

import grails.util.Environment

class SystemMessageController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', output: 'POST', load: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'locale', 'text'].contains(params.sort) ? params.sort : 'code'
        [systemMessageInstanceList: SystemMessage.selectList(), systemMessageInstanceTotal: SystemMessage.selectCount()]
    }

    def show() {
        def systemMessageInstance = SystemMessage.get(params.id)
        if (!systemMessageInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemMessage', params.id)
            redirect(action: 'list')
        } else {
            return [systemMessageInstance: systemMessageInstance]
        }
    }

    def delete() {
        def systemMessageInstance = SystemMessage.get(params.id)
        if (systemMessageInstance) {
            try {
                systemMessageInstance.delete(flush: true)
                utilService.cacheService.resetThis('message', utilService.cacheService.COMPANY_INSENSITIVE, systemMessageInstance.code)
                flash.message = utilService.standardMessage('deleted', systemMessageInstance)
                redirect(action: 'list')
            } catch (Exception e) {
                flash.message = utilService.standardMessage('not.deleted', systemMessageInstance)
                redirect(action: 'show', id: params.id)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemMessage', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemMessageInstance = SystemMessage.get(params.id)
        if (!systemMessageInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemMessage', params.id)
            redirect(action: 'list')
        } else {
            return [systemMessageInstance: systemMessageInstance]
        }
    }

    def update(Long version) {
        def systemMessageInstance = SystemMessage.get(params.id)
        if (systemMessageInstance) {
            if (version != null && systemMessageInstance.version > version) {
                systemMessageInstance.errorMessage(code: 'locking.failure', domain: 'systemMessage')
                render(view: 'edit', model: [systemMessageInstance: systemMessageInstance])
                return
            }

            def oldCode = systemMessageInstance.code
            def oldLocale = systemMessageInstance.locale
            systemMessageInstance.properties['code', 'locale', 'text'] = params
            def valid = !systemMessageInstance.hasErrors()
            if (valid && systemMessageInstance.locale != oldLocale && !localeIsValid(systemMessageInstance.locale)) {
                systemMessageInstance.errorMessage(field: 'locale', code: 'systemMessage.bad.locale', default: 'Invalid locale')
                valid = false
            }

            if (valid) valid = systemMessageInstance.saveThis()
            if (valid) {
                if (systemMessageInstance.code != oldCode) utilService.cacheService.resetThis('message', utilService.cacheService.COMPANY_INSENSITIVE, oldCode)
                utilService.cacheService.resetThis('message', utilService.cacheService.COMPANY_INSENSITIVE, systemMessageInstance.code)
                flash.message = utilService.standardMessage('updated', systemMessageInstance)
                redirect(action: 'show', id: systemMessageInstance.id)
            } else {
                render(view: 'edit', model: [systemMessageInstance: systemMessageInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemMessage', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        return [systemMessageInstance: new SystemMessage()]
    }

    def save() {
        def systemMessageInstance = new SystemMessage()
        systemMessageInstance.properties['code', 'locale', 'text'] = params
        def valid = !systemMessageInstance.hasErrors()
        if (valid && !localeIsValid(systemMessageInstance.locale)) {
            systemMessageInstance.errorMessage(field: 'locale', code: 'systemMessage.bad.locale', default: 'Invalid locale')
            valid = false
        }

        if (valid) valid = systemMessageInstance.saveThis()
        if (valid) {
            utilService.cacheService.resetThis('message', utilService.cacheService.COMPANY_INSENSITIVE, systemMessageInstance.code)
            flash.message = utilService.standardMessage('created', systemMessageInstance)
            redirect(action: 'show', id: systemMessageInstance.id)
        } else {
            render(view: 'create', model: [systemMessageInstance: systemMessageInstance])
        }
    }

    def export() {
        [currentRecordCount: [count: SystemMessage.count()]]
    }

    def output() {
        def dir
        if (Environment.current == Environment.PRODUCTION) {
            dir = new File(new File(servletContext.getRealPath('/')), "WEB-INF${File.separator}grails-app${File.separator}i18n")
        } else {
            dir = new File(new File(servletContext.getRealPath('/')).getParent(), "grails-app${File.separator}i18n")
        }

        if (dir.canWrite()) {
            def map = [:]
            for (it in SystemMessage.list()) {
                def list = map.get(it.locale)
                if (list == null) {
                    list = []
                    map.put(it.locale, list)
                }

                def code = it.code.trim()
                def text = it.text.trim()

                list << [code: ((code.indexOf('.') == -1) ? CacheService.IMPOSSIBLE_VALUE + code : code), text: text]
            }

            def fileCount = 0
            def recordCount = 0
            for (entry in map) {
                def locale = entry.key
                def list = entry.value
                list.sort {it.code}

                if (locale == '*') {
                    locale = ''
                } else if (locale.length() == 2) {
                    locale = '_' + locale
                } else {
                    locale = '_' + locale.substring(0, 2) + '_' + locale.substring(2)
                }

                def file = new File(dir, "application${locale}.properties")
                def out = null
                out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), 'UTF-8'))
                fileCount++
                def prefix = ''
                try {
                    for (line in list) {
                        recordCount++
                        def code, first
                        if (line.code.startsWith(CacheService.IMPOSSIBLE_VALUE)) {
                            code = line.code.substring(CacheService.IMPOSSIBLE_VALUE.length())
                            first = ''
                        } else {
                            code = line.code
                            first = code.substring(0, code.indexOf('.'))
                        }

                        if (first != prefix) {
                            out.newLine()
                            prefix = first
                        }

                        first = true
                        for (text in SystemMessage.breakLine(code, line.text)) {
                            if (first) {
                                out.write("${code}=${text}")
                                first = false
                            } else {
                                out.write(text)
                            }

                            out.newLine()
                        }
                    }
                } finally {
                    if (out != null) {
                        try {
                            out.close()
                        } catch (Exception ex) {}
                    }
                }
            }

            flash.message = message(code: 'systemMessage.exported', args: ["${recordCount}", "${fileCount}"], default: "${recordCount} records were exported into ${fileCount} files")
        } else {
            flash.message = message(code: 'systemMessage.imports.access', args: [dir], default: "Unable to access ${dir}")
        }

        redirect(action: 'export')
    }

    def imports() {
        def names = []
        def dir
        if (Environment.current == Environment.PRODUCTION) {
            dir = new File(new File(servletContext.getRealPath('/')), "WEB-INF${File.separator}grails-app${File.separator}i18n")
        } else {
            dir = new File(new File(servletContext.getRealPath('/')).getParent(), "grails-app${File.separator}i18n")
        }

        if (dir.canRead()) {
            def name
            for (it in dir.listFiles()) {
                if (it.isFile() && it.canRead() && it.getName().endsWith('.properties')) {
                    name = it.getName()
                    names << name.substring(0, name.length() - 11)
                }
            }

            names.sort()
        } else {
            flash.message = message(code: 'systemMessage.imports.access', args: [dir], default: "Unable to access ${dir}")
        }

        return [names: names]
    }

    def load() {
        def name = params.file
        if (name) {
            name += '.properties'
            def dir
            if (Environment.current == Environment.PRODUCTION) {
                dir = new File(new File(servletContext.getRealPath('/')), "WEB-INF${File.separator}grails-app${File.separator}i18n")
            } else {
                dir = new File(new File(servletContext.getRealPath('/')).getParent(), "grails-app${File.separator}i18n")
            }

            if (dir.canRead()) {
                def file = new File(dir, name)
                if (file.isFile() && file.canRead()) {
                    def locale
                    if (name ==~ /.+_[a-z][a-z]_[A-Z][A-Z]\.properties$/) {
                        locale = new Locale(name.substring(name.length() - 16, name.length() - 14), name.substring(name.length() - 13, name.length() - 11))
                    } else if (name ==~ /.+_[a-z][a-z]\.properties$/) {
                        locale = new Locale(name.substring(name.length() - 13, name.length() - 11))
                    } else {
                        locale = null
                    }

                    def counts = DatabaseMessageSource.loadPropertyFile(file, locale)
                    if (counts.imported) utilService.cacheService.clearThis('message')
                    flash.message = message(code: 'systemMessage.imports.counts', args: [counts.imported, counts.skipped], default: "Imported ${counts.imported} key(s). Skipped ${counts.skipped} key(s).")
                } else {
                    flash.message = message(code: 'systemMessage.imports.access', args: [file], default: "Unable to access ${file}")
                }
            } else {
                flash.message = message(code: 'systemMessage.imports.access', args: [dir], default: "Unable to access ${dir}")
            }
        } else {
            flash.message = message(code: 'systemMessage.imports.missing', default: 'No properties file selected')
        }

        redirect(action: 'imports')
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private localeIsValid(locale) {
        if (locale) {
            if (locale == '*') return true

            if (locale.length() == 2) {
                return (SystemLanguage.countByCode(locale) == 1)
            }

            if (locale.length() == 4) {
                return (SystemLanguage.countByCode(locale.substring(0, 2)) == 1 && SystemCountry.countByCode(locale.substring(2)) == 1)
            }
        }

        return false
    }
}