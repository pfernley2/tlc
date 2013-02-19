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
package org.grails.tlc.books

class CodeElementValueController {

    // Injected services
    def utilService
    def bookService

    // Security settings
    def activities = [default: 'actadmin']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', importing: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.max = utilService.max
        params.sort = ['code', 'shortName', 'name'].contains(params.sort) ? params.sort : 'code'
        def ddSource = utilService.source('codeElement.list')
        [codeElementValueInstanceList: CodeElementValue.selectList(securityCode: utilService.currentCompany().securityCode), codeElementValueInstanceTotal: CodeElementValue.selectCount(), ddSource: ddSource]
    }

    def show() {
        def codeElementValueInstance = CodeElementValue.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!codeElementValueInstance) {
            flash.message = utilService.standardMessage('not.found', 'codeElementValue', params.id)
            redirect(action: 'list')
        } else {
            return [codeElementValueInstance: codeElementValueInstance, accountTotal: childCount(codeElementValueInstance)]
        }
    }

    def delete() {
        def codeElementValueInstance = CodeElementValue.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (codeElementValueInstance) {
            def defaults = findDependentDefaults(codeElementValueInstance.element, codeElementValueInstance.code)
            if (defaults) {
                flash.message = message(code: 'codeElementValue.has.defaults', args: [codeElementValueInstance.toString(), defaults[0].name], default: "Value ${codeElementValueInstance.toString()} can not be deleted because it is used as a default by chart section ${defaults[0].name}")
                redirect(action: 'show', id: params.id)
            } else {
                try {
                    codeElementValueInstance.delete(flush: true)
                    flash.message = utilService.standardMessage('deleted', codeElementValueInstance)
                    redirect(action: 'list')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', codeElementValueInstance)
                    redirect(action: 'show', id: params.id)
                }
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'codeElementValue', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def codeElementValueInstance = CodeElementValue.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (!codeElementValueInstance) {
            flash.message = utilService.standardMessage('not.found', 'codeElementValue', params.id)
            redirect(action: 'list')
        } else {
            return [codeElementValueInstance: codeElementValueInstance, accountTotal: childCount(codeElementValueInstance)]
        }
    }

    def update(Long version) {
        def codeElementValueInstance = CodeElementValue.findByIdAndSecurityCode(params.id, utilService.currentCompany().securityCode)
        if (codeElementValueInstance) {
            def accountTotal = childCount(codeElementValueInstance)
            if (version != null && codeElementValueInstance.version > version) {
                codeElementValueInstance.errorMessage(code: 'locking.failure', domain: 'codeElementValue')
                render(view: 'edit', model: [codeElementValueInstance: codeElementValueInstance, accountTotal: accountTotal])
                return
            }

            def oldCode = codeElementValueInstance.code
            codeElementValueInstance.properties['code', 'shortName', 'name'] = params.code
            codeElementValueInstance.code = bookService.fixCase(codeElementValueInstance.code)
            fixNames(codeElementValueInstance)
            def valid = !codeElementValueInstance.hasErrors()
            if (valid && codeElementValueInstance.code != oldCode && accountTotal > 0) {
                codeElementValueInstance.errorMessage(field: 'code', code: 'codeElementValue.code.changed', default: 'You may not change the code once accounts have been created')
                valid = false
            }
            if (valid) {
                def defaults = (codeElementValueInstance.code != oldCode) ? findDependentDefaults(codeElementValueInstance.element, oldCode) : []
                CodeElement.withTransaction {status ->
                    if (codeElementValueInstance.saveThis()) {
                        for (dflt in defaults) {
                            for (int i = 1; i <= 8; i++) {
                                if (dflt."segment${i}"?.id == codeElementValueInstance.element.id) {
                                    dflt."default${i}" = codeElementValueInstance.code
                                    if (!dflt.saveThis()) {
                                        codeElementValueInstance.errorMessage(code: 'codeElementValue.bad.defaults', args: [dflt.name], default: "Unable to update the default for chart section ${dflt.name}")
                                        status.setRollbackOnly()
                                        valid = false
                                    }

                                    break
                                }
                            }

                            if (!valid) break
                        }
                    } else {
                        status.setRollbackOnly()
                        valid = false
                    }
                }
            }

            if (valid) {
                flash.message = utilService.standardMessage('updated', codeElementValueInstance)
                redirect(action: 'show', id: codeElementValueInstance.id)
            } else {
                render(view: 'edit', model: [codeElementValueInstance: codeElementValueInstance, accountTotal: accountTotal])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'codeElementValue', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def codeElementValueInstance = new CodeElementValue()
        codeElementValueInstance.element = utilService.reSource('codeElement.list')   // Ensure correct parent
        return [codeElementValueInstance: codeElementValueInstance, accountTotal: 0]
    }

    def save() {
        def codeElementValueInstance = new CodeElementValue()
        codeElementValueInstance.properties['code', 'shortName', 'name'] = params
        codeElementValueInstance.element = utilService.reSource('codeElement.list')   // Ensure correct parent
        codeElementValueInstance.code = bookService.fixCase(codeElementValueInstance.code)
        fixNames(codeElementValueInstance)
        if (!codeElementValueInstance.hasErrors() && codeElementValueInstance.saveThis()) {
            flash.message = utilService.standardMessage('created', codeElementValueInstance)
            redirect(action: 'show', id: codeElementValueInstance.id)
        } else {
            render(view: 'create', model: [codeElementValueInstance: codeElementValueInstance, accountTotal: 0])
        }
    }

    def imports() {
        [ddSource: utilService.reSource('codeElement.list')]
    }

    def importing() {
        def ddSource = utilService.reSource('codeElement.list')
        def valid = true
        def added = 0
        def ignored = 0
        def errors = 0
        def uploadFile = request.getFile('file')
        if (uploadFile.isEmpty()) {
            ddSource.errorMessage(code: 'codeElementValue.empty', default: 'File is empty')
            valid = false
        } else {
            if (uploadFile.getSize() > 1024 * 1024) {
                ddSource.errorMessage(code: 'codeElementValue.size', default: 'File exceeds the 1 MB limit')
                valid = false
            } else {
                def sourceFile = utilService.tempFile('CEV', 'txt')
                def fields, code, rec
                def isAlpha = (ddSource.dataType == 'alphabetic')
                def isNumeric = !isAlpha
                def size = ddSource.dataLength
                try {
                    uploadFile.transferTo(sourceFile)
                    sourceFile.eachLine {
                        if (it.trim()) {
                            fields = it.split('\\t')*.trim()
                            code = isAlpha ? bookService.fixCase(fields[0]) : fields[0]
                            if (code.length() == size && ((isNumeric && bookService.isNumeric(code)) || (isAlpha && bookService.isAlphabetic(code)))) {
                                rec = CodeElementValue.findByElementAndCode(ddSource, code)
                                if (rec) {
                                    ignored++
                                } else {
                                    rec = new CodeElementValue()
                                    rec.element = ddSource
                                    rec.code = code
                                    if (fields.size() == 1) {
                                        rec.shortName = code
                                        rec.name = code
                                    } else if (fields.size() == 2) {
                                        rec.shortName = fields[1] ?: code
                                        rec.name = rec.shortName
                                        if (rec.shortName.length() > 10) rec.shortName = rec.shortName.substring(0, 10)
                                        if (rec.name.length() > 30) rec.name = rec.name.substring(0, 30)
                                    } else {
                                        rec.shortName = fields[1]
                                        rec.name = fields[2]
                                        if (rec.shortName.length() > 10) rec.shortName = rec.shortName.substring(0, 10)
                                        if (rec.name.length() > 30) rec.name = rec.name.substring(0, 30)
                                        fixNames(rec)
                                    }

                                    if (!rec.hasErrors() && rec.saveThis()) {
                                        added++
                                    } else {
                                        errors++
                                    }
                                }
                            } else {
                                errors++
                            }
                        }
                    }

                    try {
                        sourceFile.delete()
                    } catch (Exception e1) {}
                } catch (Exception ex) {
                    log.error(ex)
                    ddSource.errorMessage(code: 'codeElementValue.bad.upload', default: 'Unable to upload the file')
                    valid = false
                }
            }
        }

        if (valid) {
            flash.message = message(code: 'codeElementValue.uploaded', args: [added.toString(), ignored.toString(), errors.toString()], default: "${added} code(s) added, ${ignored} skipped, ${errors} had errors")
            redirect(action: 'list')
        } else {
            render(view: 'imports', model: [ddSource: ddSource])
        }
    }

    private childCount(val) {
        return Account.executeQuery("select count(*) from Account where element${val.element.elementNumber} = ?", [val])[0]
    }

    private fixNames(val) {
        if (!val.shortName) {
            if (val.name) {
                val.shortName = (val.name.length() > 10) ? val.name.substring(0, 10) : val.name
            } else {
                val.shortName = val.code
            }
        }

        if (!val.name) val.name = val.shortName
    }

    private findDependentDefaults(element, code) {
        def defaults = []
        def list
        for (int i = 1; i <= 8; i++) {
            list = ChartSection.findAll("from ChartSection as x where x.segment${i} = ? and x.default${i} = ?", [element, code])
            defaults.addAll(list)
        }

        return defaults
    }
}