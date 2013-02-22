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

import org.grails.tlc.obj.Translation

class TranslationController {

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'systran']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', edit: 'POST', update: 'POST']

    def index() { redirect(action: 'translate', params: params) }

    def translate(Translation translationInstance) {
        translationInstance.clearPagination()
        [translationInstance: translationInstance]
    }

    def list(Translation translationInstance) {
        if (translationInstance.hasErrors()) {
            render(view: 'translate', model: [translationInstance: translationInstance])
            return
        }

        params.max = utilService.max
        params.sort = ['code', 'locale', 'text'].contains(params.sort) ? params.sort : 'code'
        if (!params.order) params.order = 'asc'

        def where, temp
        def toLocale = translationInstance.toLocale
        if (translationInstance.display == 'translated') {
            where = "x.locale = '${toLocale}'"
        } else if (translationInstance.strict) {
            if (translationInstance.fromLanguage) {
                temp = translationInstance.fromLanguage.code
                if (translationInstance.fromCountry) temp += translationInstance.fromCountry.code
            } else {
                temp = '*'
            }

            where = "x.locale = '${temp}'"
            if (translationInstance.display == 'untranslated') {
                where += " and not exists (select e.id from SystemMessage as e where e.code = x.code and e.locale = '${toLocale}')"
            }
        } else {
            temp = "'*'"
            def isDisplayAll = (translationInstance.display == 'all')
            def isDifferentLanguage = (translationInstance.toLanguage.code != translationInstance.fromLanguage?.code)
            def hasToCountry = (translationInstance.toCountry != null)
            if (translationInstance.fromLanguage) {

                // Check if they are doing something like 'from frFR to fr, untranslated only' and, if so,
                // we need to knock out the language code from the 'in' test of acceptable locale codes.
                if (isDisplayAll || isDifferentLanguage || hasToCountry) temp += ", '${translationInstance.fromLanguage.code}'"
                if (translationInstance.fromCountry) temp += ", '${translationInstance.fromLanguage.code}${translationInstance.fromCountry.code}'"
            } else {
                if (isDisplayAll || hasToCountry) temp += ", '${translationInstance.toLanguage.code}'"
                if (isDisplayAll && hasToCountry) temp += ", '${toLocale}'"
            }

            // If there is only the default locale in the 'list', we can use a simple '=' test rather than
            // an 'in' test plus the sub-select to pick out the most relevant entry
            if (temp.length() == 3) {
                where = "x.locale = '${temp}'"
            } else {
                where = "x.locale in (${temp}) and x.relevance = (select max(m.relevance) from SystemMessage as m where m.code = x.code and m.locale in (${temp}))"
            }

            if (!isDisplayAll) {
                where += " and not exists (select e.id from SystemMessage as e where e.code = x.code and e.locale = '${toLocale}')"
            }
        }

        def systemMessageInstanceList = SystemMessage.selectList(where: where)
        def systemMessageInstanceTotal = SystemMessage.selectCount()

        def translations = []
        for (rec in systemMessageInstanceList) {
            if (rec.locale == toLocale) {
                temp = rec.text
            } else {
                temp = SystemMessage.findByCodeAndLocale(rec.code, toLocale)?.text ?: ''
            }

            translations << [id: rec.id, code: rec.code, locale: rec.locale, text: rec.text, translation: temp]
        }

        [translationInstance: translationInstance, systemMessageInstanceList: translations, systemMessageInstanceTotal: systemMessageInstanceTotal]
    }

    def edit(Translation translationInstance) {
        if (translationInstance.hasErrors()) {
            render(view: 'translate', model: [translationInstance: translationInstance])
            return
        }

        def propagate = params.propagate ? true : false
        def systemMessageInstance = SystemMessage.get(params.id)
        if (!systemMessageInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemMessage', params.id)
            redirect(action: 'list', params: translationInstance.data)
            return
        }

        def temp
        def translatedMessageId = ''
        if (systemMessageInstance.locale == translationInstance.toLocale) {
            temp = systemMessageInstance.text
        } else {
            temp = SystemMessage.findByCodeAndLocale(systemMessageInstance.code, translationInstance.toLocale)
            if (temp) {
                translatedMessageId = temp.id
                temp = temp.text
            } else {
                temp = ''
            }
        }

        [translationInstance: translationInstance, propagate: propagate, systemMessageInstance: systemMessageInstance,
                    translatedText: temp, translatedMessageId: translatedMessageId]
    }

    def update(Translation translationInstance) {
        if (translationInstance.hasErrors()) {
            render(view: 'translate', model: [translationInstance: translationInstance])
            return
        }

        def propagate = params.propagate ? true : false
        def systemMessageInstance = SystemMessage.get(params.id)
        if (systemMessageInstance) {
            def translatedText = params.translatedText
            if (params.version?.isLong() && systemMessageInstance.version > params.version.toLong()) {
                systemMessageInstance.errorMessage(code: 'locking.failure', domain: 'systemMessage')
                render(view: 'edit', model: [translationInstance: translationInstance, propagate: propagate,
                            systemMessageInstance: systemMessageInstance, translatedText: translatedText, translatedMessageId: params.translatedMessageId])
                return
            }

            def related, temp, result
            def valid = true
            def translatedTextError = false
            if (!translatedText) {
                temp = message(code: 'translation.translation.label', default: 'Translation')
                temp = message(code: 'default.blank.message', args: [temp], default: '${msg} cannot be blank')
                systemMessageInstance.errors.reject(null, temp)
                valid = false
                translatedTextError = true
            } else if (translatedText.length() > 2000) {
                temp = message(code: 'translation.translation.label', default: 'Translation')
                temp = message(code: 'default.invalid.size.message', args: [temp, '', translatedText.substring(0, 20) + '...', 1, 2000],
                default: '${msg} (${translatedText.substring(0, 20)}...) does not fall within the valid size range from 1 to 2000')
                systemMessageInstance.errors.reject(null, temp)
                valid = false
                translatedTextError = true
            }

            if (valid) {
                def updatingOriginal = (systemMessageInstance.locale == translationInstance.toLocale)

                // If they asked to make the same changes to other messages that
                // would display the same original text
                if (propagate) {

                    // Work out what, if any, related records MAY need updating
                    result = "'*', '${translationInstance.toLanguage.code}'"
                    if (translationInstance.toCountry) result += ", '${translationInstance.toLocale}'"
                    temp = "from SystemMessage as x where x.code != :code and x.text = :text and x.locale in (${result}) and x.relevance = " +
                            "(select max(y.relevance) from SystemMessage as y where y.code = x.code and y.locale in (${result}) and y.text = :text) " +
                            "and not exists (select z.id from SystemMessage as z where z.code = x.code" +
                            " and z.locale = '${translationInstance.toLocale}' and z.text != :text)"
                    related = SystemMessage.executeQuery(temp, [code: systemMessageInstance.code, text: systemMessageInstance.text])

                    // If the 'to locale' contains a country, we need to check for the situation
                    // where we want to change text of 'xxx' and the default locale has text of
                    // 'xxx' but there is a language-only version with text of 'yyy' which would
                    // override the default locale record. The preceding SQL query would simply
                    // retrieve the default locale record since the 'language-only' version does
                    // not meet the criteria of its text being 'xxx'.
                    if (translationInstance.toCountry) {
                        for (int i = 0; i < related.size(); i++) {

                            // Note that the following find query works on the principle that, if it can
                            // find a language-only version of the code, it's text must be different
                            // from 'xxx' otherwise it would have been selected by the main query above
                            // in preference to the default locale version. Additionally, if a
                            // language-and-country version of the code had existed with text of 'xxx'
                            // then THAT would have been selected over the language-only version whereas
                            // if a language-and-country version had existed with text not equal to 'xxx',
                            // then nothing at all would have been selected.
                            if (related[i].locale == '*' && SystemMessage.findByCodeAndLocale(related[i].code, translationInstance.toLanguage.code)) {
                                related.remove(i--)
                            }
                        }
                    }
                }

                // Perform the update(s)
                SystemMessage.withTransaction {status ->
                    if (updatingOriginal) {
                        temp = systemMessageInstance.text
                        systemMessageInstance.text = translatedText
                        result = saveMessage(systemMessageInstance)
                        if (result instanceof String || !result) {

                            // If it's a String (i.e. the error was in saving a language-only version)
                            // then bubble the error message up to the systemMessageInstance record.
                            if (result) setErrorMessage(systemMessageInstance, result)
                            systemMessageInstance.text = temp
                            valid = false
                        }
                    } else {

                        // Just because we are not updating an existing translation does NOT mean that
                        // a translation does not exist. This is because, when the user specifies a
                        // from-locale, we always show them the from-locale record (so that sorting and
                        // criteria work in the List screen) and then simply display any translation
                        // record as the translated text. This means that, in such circumstances, the
                        // record in systemMessageInstance will be the from-locale record, even though
                        // a translation record exists. The following code allows for that situation.
                        temp = SystemMessage.findByCodeAndLocale(systemMessageInstance.code, translationInstance.toLocale)
                        if (temp) {
                            temp.text = translatedText
                            params.translatedMessageId = temp.id
                        } else {
                            temp = new SystemMessage(code: systemMessageInstance.code, locale: translationInstance.toLocale, text: translatedText)
                            params.translatedMessageId = ''
                        }

                        result = saveMessage(temp)
                        if (result instanceof String || !result) {

                            // If it's Boolean false (i.e. the error was in saving the main toLocale
                            // record we need to grab the error message text from temp.
                            if (!result) result = utilService.getFirstErrorMessage(temp)
                            setErrorMessage(systemMessageInstance, result)
                            valid = false
                        }
                    }

                    // Perform any related updates
                    if (valid) {
                        for (rec in related) {
                            if (rec.locale == translationInstance.toLocale) {
                                temp = rec
                                temp.text = translatedText
                                result = saveMessage(temp)
                            } else {
                                temp = new SystemMessage(code: rec.code, locale: translationInstance.toLocale, text: translatedText)
                                result = saveMessage(temp)
                            }

                            // If an error in saving a language-only version
                            if (result instanceof String) {
                                setErrorMessage(systemMessageInstance, result)
                                valid = false
                                break
                            } else if (!result) {	// If an error in saving the main record
                                setErrorMessage(systemMessageInstance, utilService.getFirstErrorMessage(temp))
                                valid = false
                                break
                            }
                        }
                    }

                    if (!valid) status.setRollbackOnly()
                }
            }

            if (valid) {
                utilService.cacheService.resetThis('message', utilService.cacheService.COMPANY_INSENSITIVE, systemMessageInstance.code)
                for (rec in related) utilService.cacheService.resetThis('message', utilService.cacheService.COMPANY_INSENSITIVE, rec.code)
                flash.message = utilService.standardMessage('updated', systemMessageInstance)
                redirect(action: 'list', params: translationInstance.data)
            } else {
                render(view: 'edit', model: [translationInstance: translationInstance, propagate: propagate,
                            systemMessageInstance: systemMessageInstance, translatedText: translatedText,
                            translatedTextError: translatedTextError, translatedMessageId: params.translatedMessageId])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemMessage', params.id)
            redirect(action: 'list', params: translationInstance.data)
        }
    }

    def delete(Translation translationInstance) {
        if (translationInstance.hasErrors()) {
            render(view: 'translate', model: [translationInstance: translationInstance])
            return
        }

        def systemMessageInstance = SystemMessage.get(params.translatedMessageId ?: params.id)
        if (systemMessageInstance?.locale == translationInstance.toLocale) {
            def related
            if (params.propagate) {
                related = SystemMessage.executeQuery('from SystemMessage where locale = ? and text = ? and id != ?',
                        [systemMessageInstance.locale, systemMessageInstance.text, systemMessageInstance.id])
            } else {
                related = []
            }

            related << systemMessageInstance
            def valid = true
            SystemMessage.withTransaction {status ->
                for (rec in related) {
                    try {
                        rec.delete(flush: true)
                    } catch (Exception e) {
                        valid = false
                        status.setRollbackOnly()
                        break
                    }
                }
            }

            if (valid) {
                for (rec in related) utilService.cacheService.resetThis('message', utilService.cacheService.COMPANY_INSENSITIVE, rec.code)
                flash.message = utilService.standardMessage('deleted', systemMessageInstance)
                redirect(action: 'list', params: translationInstance.data)
            } else {
                flash.message = utilService.standardMessage('not.deleted', systemMessageInstance)
                redirect(action: 'list', params: translationInstance.data)
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemMessage', params.id)
            redirect(action: 'list', params: translationInstance.data)
        }
    }

    // Save a message and, if it's a language/country message but no language
    // record exists, create the language record also. It returns true if
    // the save was successful, false if the save failed or a String message
    // if the save of a language only record failed. In such cases the returned
    // String is the error message from the language only record. This method
    // assumes it is within a caller's transaction and that the caller will
    // handle any commit or rollback required.
    private saveMessage(messageInstance) {

        // Save the message we have been given
        if (messageInstance.saveThis()) {

            // If this message has both is country specific but no language only
            // version exists, create and save a language only version.
            if (messageInstance.locale.length() == 4 && !SystemMessage.findByCodeAndLocale(messageInstance.code, messageInstance.locale.substring(0, 2))) {
                def languageOnlyMessage = new SystemMessage(code: messageInstance.code, locale: messageInstance.locale.substring(0, 2), text: messageInstance.text)
                if (!languageOnlyMessage.saveThis()) return utilService.getFirstErrorMessage(languageOnlyMessage)
            }

            return true	// Record(s) saved ok
        }

        return false
    }

    // Sets an error message on the given object
    private setErrorMessage(messageInstance, text) {
        messageInstance.errors.reject(null, message(code: 'translation.dependent.error', args: [text],
        default: 'Error saving a dependent record. The message was: ' + text))
    }
}
