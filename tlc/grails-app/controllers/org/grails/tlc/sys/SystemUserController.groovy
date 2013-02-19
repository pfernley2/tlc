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

import org.grails.tlc.corp.Company
import org.grails.tlc.corp.CompanyUser
import org.grails.tlc.corp.QueuedTask
import org.grails.tlc.corp.Task

import java.awt.Color
import java.awt.Font
import java.awt.Graphics2D
import java.awt.RenderingHints
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage

class SystemUserController {
    private static final String CAPTCHACHARS = 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'

    // Injected services
    def utilService

    // Security settings
    def activities = [default: 'sysadmin', login: 'any', connect: 'any', logout: 'logout', forgot: 'any', notification: 'any', change: 'login',
            proceed: 'login', profile: 'login', modify: 'login', register: 'any', registration: 'any', captcha: 'any']

    // List of actions with specific request types
    static allowedMethods = [delete: 'POST', save: 'POST', update: 'POST', connect: 'POST', notification: 'POST', registration: 'POST', proceed: 'POST', modify: 'POST']

    def index() { redirect(action: 'list', params: params) }

    def list() {
        params.sort = ['loginId', 'name', 'email', 'lastLogin', 'disabledUntil', 'nextPasswordChange', 'administrator'].contains(params.sort) ? params.sort : 'loginId'
        params.max = utilService.max
        [systemUserInstanceList: SystemUser.selectList(), systemUserInstanceTotal: SystemUser.selectCount()]
    }

    def show() {
        def systemUserInstance = SystemUser.get(params.id)
        if (!systemUserInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemUser', params.id)
            redirect(action: 'list')
			return
        }
		
        [systemUserInstance: systemUserInstance]
    }

    def delete() {
        def systemUserInstance = SystemUser.get(params.id)
        if (systemUserInstance) {

            def valid = true

            // Check if last system administrator
            if (systemUserInstance.administrator && SystemUser.countByAdministrator(true) < 2) {
                systemUserInstance.errorMessage(code: 'systemUser.last.system.admin', args: [systemUserInstance.name],
                        default: "User ${systemUserInstance.name} is the last system administrator and may not be disabled or deleted")
                valid = false
            }

            // Check if owns task definitions
            if (valid && Task.countByUser(systemUserInstance) > 0) {
                systemUserInstance.errorMessage(code: 'systemUser.owns.tasks', args: [systemUserInstance.name],
                        default: "User ${systemUserInstance.name} owns task definitions. These tasks must be re-assigned to another user before continuing.")
                valid = false
            }

            // Check if owns queued tasks that are awaiting execution
            if (valid && QueuedTask.countByUserAndCurrentStatus(systemUserInstance, 'waiting') > 0) {
                systemUserInstance.errorMessage(code: 'systemUser.waiting.tasks', args: [systemUserInstance.name],
                        default: "User ${systemUserInstance.name} has tasks in the queue awaiting execution. These tasks must be deleted, executed or re-assigned before continuing.")
                valid = false
            }

            // Check if a lone company administrator
            if (valid) {

                // Grab all the companies the user is a member of
                def coUsers = CompanyUser.findAllByUser(systemUserInstance)

                // Work through those companies
                for (coUser in coUsers) {

                    // Look for at least two company administrators
                    def coAdmins = CompanyUser.findAll('from CompanyUser as x where x.id in (select cu.id from SystemRole as r join r.users as cu where r.code = ? and cu.company.id = ?)',
                            ['companyAdmin', coUser.company.id], [max: 2])

                    // If there is only one company administrator and it's this user, complain
                    if (coAdmins.size() == 1 && coAdmins[0].id == coUser.id) {
                        systemUserInstance.errorMessage(code: 'systemUser.last.company.admin', args: [systemUserInstance.name, coUser.company.name],
                                default: "User ${systemUserInstance.name} is the last administrator in company ${coUser.company.name} and may not be deleted")
                        valid = false
                        break
                    }
                }
            }

            if (valid) {
                try {
                    systemUserInstance.delete(flush: true)
                    utilService.cacheService.resetThis('userActivity', utilService.cacheService.COMPANY_INSENSITIVE, "${systemUserInstance.id}")
                    utilService.cacheService.resetThis('userAccessGroup', utilService.cacheService.COMPANY_INSENSITIVE, "${systemUserInstance.id}")
                    utilService.cacheService.resetThis('userAccount', utilService.cacheService.COMPANY_INSENSITIVE, "${systemUserInstance.id}")
                    utilService.cacheService.resetThis('userCustomer', utilService.cacheService.COMPANY_INSENSITIVE, "${systemUserInstance.id}")
                    utilService.cacheService.resetThis('userSupplier', utilService.cacheService.COMPANY_INSENSITIVE, "${systemUserInstance.id}")
                    utilService.cacheService.resetThis('mnemonic', 0L, "${systemUserInstance.id}")
                    flash.message = utilService.standardMessage('deleted', systemUserInstance)
                    redirect(action: 'list')
                } catch (Exception e) {
                    flash.message = utilService.standardMessage('not.deleted', systemUserInstance)
                    redirect(action: 'show', id: params.id)
                }
            } else {
                render(view: 'show', model: [systemUserInstance: systemUserInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemUser', params.id)
            redirect(action: 'list')
        }
    }

    def edit() {
        def systemUserInstance = SystemUser.get(params.id)

        if (!systemUserInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemUser', params.id)
            redirect(action: 'list')
        } else {
            return [systemUserInstance: systemUserInstance]
        }
    }

    def update(Long version) {
        def systemUserInstance = SystemUser.get(params.id)
        if (systemUserInstance) {
            if (version != null && systemUserInstance.version > version) {
                systemUserInstance.errorMessage(code: 'locking.failure', domain: 'systemUser')
                render(view: 'edit', model: [systemUserInstance: systemUserInstance])
                return
            }

            def oldAdministrator = systemUserInstance.administrator
            def oldCountryId = systemUserInstance.country.id
            def oldLanguageId = systemUserInstance.language.id
            systemUserInstance.properties['loginId', 'name', 'email', 'password', 'passwordConfirmation', 'securityQuestion', 'securityAnswer', 'lastLogin', 'disabledUntil',
                    'nextPasswordChange', 'administrator', 'country', 'language', 'oldPassword1', 'oldPassword2', 'oldPassword3', 'disableHelp'] = params
            systemUserInstance.currentPassword = CacheService.IMPOSSIBLE_VALUE  // Not a real end-user password change
            def valid = !systemUserInstance.hasErrors()

            // If they are changing from being a system administrator to not being
            // a system administrator then check that there is at least one other
            // system administrator
            if (valid && !systemUserInstance.administrator && oldAdministrator && SystemUser.countByAdministrator(true) < 2) {
                systemUserInstance.errorMessage(code: 'systemUser.last.system.admin', args: [systemUserInstance.name],
                        default: "User ${systemUserInstance.name} is the last system administrator and may not be disabled or deleted")
                valid = false
            }

            if (valid) {
                SystemUser.withTransaction {status ->
                    valid = systemUserInstance.verifyPasswordStatus() && systemUserInstance.saveThis()

                    // If they weren't a system administrator before, but are now, add them to the system company
                    if (valid && !oldAdministrator && systemUserInstance.administrator) {
                        def systemCompany = Company.findBySystemOnly(true)
                        def existing = systemUserInstance.companies.find {it.company.id == systemCompany.id}
                        if (!existing) {
                            systemUserInstance.addToCompanies(new CompanyUser(company: systemCompany, user: systemUserInstance))
                            valid = systemUserInstance.save()   // With deep validation - Need to keep an eye on this one
                        }
                    }

                    if (!valid) {
                        status.setRollbackOnly()
                    }
                }
            }

            if (valid) {
                if (systemUserInstance.id == utilService.currentUser().id && (oldCountryId != systemUserInstance.country.id || oldLanguageId != systemUserInstance.language.id)) {
                    utilService.setSessionLocale(new Locale(systemUserInstance.language.code, systemUserInstance.country.code))
                }

                flash.message = utilService.standardMessage('updated', systemUserInstance)
                redirect(action: 'show', id: systemUserInstance.id)
            } else {
                systemUserInstance.password = null
                systemUserInstance.passwordConfirmation = null
                render(view: 'edit', model: [systemUserInstance: systemUserInstance])
            }
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemUser', params.id)
            redirect(action: 'list')
        }
    }

    def create() {
        def systemUserInstance = new SystemUser()
        def locale = utilService.currentLocale()
        def language = SystemLanguage.findByCode(locale.getLanguage() ?: 'en') ?: SystemLanguage.findByCode('en')
        def country = SystemCountry.findByCode(locale.getCountry() ?: ((language.code == 'en') ? 'US' : language.code.toUpperCase(Locale.US))) ?: SystemCountry.findByCode('US')
        systemUserInstance.country = country
        systemUserInstance.language = language
        return [systemUserInstance: systemUserInstance]
    }

    def save() {
        def systemUserInstance = new SystemUser()
        systemUserInstance.properties['loginId', 'name', 'email', 'password', 'passwordConfirmation', 'securityQuestion', 'securityAnswer', 'lastLogin', 'disabledUntil',
                'nextPasswordChange', 'administrator', 'country', 'language', 'oldPassword1', 'oldPassword2', 'oldPassword3', 'disableHelp'] = params
        def valid = !systemUserInstance.hasErrors()
        if (valid) {
            SystemUser.withTransaction {status ->
                valid = systemUserInstance.verifyPasswordStatus() && systemUserInstance.saveThis()

                // If they are a system administrator, add them to the system company
                if (valid && systemUserInstance.administrator) {
                    valid = new CompanyUser(company: Company.findBySystemOnly(true), user: systemUserInstance).saveThis()
                }

                if (!valid) {
                    status.setRollbackOnly()
                }
            }
        }

        if (valid) {
            flash.message = utilService.standardMessage('created', systemUserInstance)
            redirect(action: 'show', id: systemUserInstance.id)
        } else {
            systemUserInstance.password = null
            systemUserInstance.passwordConfirmation = null
            render(view: 'create', model: [systemUserInstance: systemUserInstance])
        }
    }

    def login() {
        def systemUserInstance = new SystemUser()
        systemUserInstance.properties['loginId', 'password'] = params
        return ['systemUserInstance': systemUserInstance]
    }

    def connect() {
        utilService.clearCurrentUser()

        // Handle request for forgotten password (id but no password)
        if (params.loginId && !params.password) {
            redirect(action: 'forgot', params: [loginId: params.loginId])
        } else {    // An ordinary login attempt
            def systemUserInstance = SystemUser.findByLoginId(params.loginId)
            if (systemUserInstance?.passwordValid(params.password)) {
                if (systemUserInstance.administrator || utilService.getCurrentOperatingState() == 'active') {
                    if (systemUserInstance.accountEnabled()) {
                        systemUserInstance.lastLogin = new Date()
                        systemUserInstance.disabledUntil = null
                        if (systemUserInstance.saveThis()) {
                            utilService.newCurrentUser(systemUserInstance)
                            if (systemUserInstance.passwordExpired()) {
                                utilService.setNextStep([sourceController: 'systemUser', sourceAction: 'change',
                                        targetController: 'systemUser', targetAction: 'proceed'])
                                render(view: 'change', model: [systemUserInstance: systemUserInstance])
                            } else {
                                utilService.setNextStep(null)
                                def companies = CompanyUser.findAllByUser(systemUserInstance)
                                if (!companies) {
                                    redirect(controller: 'system', action: (utilService.systemSetting('isDemoSystem', false) ? 'intro' : 'assign'))
                                } else if (companies.size() == 1) {
                                    utilService.newCurrentCompany(companies[0].company.id)
                                    redirect(controller: 'systemMenu', action: 'display')
                                } else {
                                    utilService.setNextStep([sourceController: 'companyUser', sourceAction: 'select',
                                            targetController: 'companyUser', targetAction: 'attach'])
                                    redirect(controller: 'companyUser', action: 'select')
                                }
                            }
                        } else {
                            utilService.newCurrentUser(null)    // Clear out login data etc
                            systemUserInstance = new SystemUser()
                            systemUserInstance.errorMessage(code: 'systemUser.login.update.error', default: 'Unable to update your user record. Please retry logging in.')
                            render(view: 'login', model: [systemUserInstance: systemUserInstance])
                        }
                    } else {
                        def until = utilService.format(systemUserInstance.disabledUntil, 2)
                        systemUserInstance = new SystemUser()
                        systemUserInstance.errorMessage(code: 'systemUser.login.disabled.error', args: [until], default: "This account has been disabled until ${until}")
                        render(view: 'login', model: [systemUserInstance: systemUserInstance])
                    }
                } else {
                    redirect(controller: 'system', action: 'loginDisabled')
                }
            } else {
                if (systemUserInstance && utilService.excessiveLoginAttempts(SystemUser.PASSWORD_ATTEMPT_MINUTES, SystemUser.PASSWORD_ATTEMPTS, systemUserInstance.id)) {
                    def until = new Date(System.currentTimeMillis() + (SystemUser.PASSWORD_LOCKOUT_MINUTES * 60000L))
                    systemUserInstance.disabledUntil = until
                    systemUserInstance.saveThis()
                    systemUserInstance = new SystemUser()
                    until = utilService.format(until, 2)
                    systemUserInstance.errorMessage(code: 'systemUser.login.attempts.error', args: [until], default: "Account has been disabled until ${until} due to excessive password failures")
                } else {
                    systemUserInstance = new SystemUser()
                    systemUserInstance.errorMessage(code: 'systemUser.credentials.error', default: 'Invalid login credentials')
                }

                render(view: 'login', model: [systemUserInstance: systemUserInstance])
            }
        }
    }

    def logout() {
        utilService.clearCurrentUser()
        redirect(action: 'login')
    }

    def forgot() {
        def systemUserInstance = SystemUser.findByLoginId(params.loginId)

        if (!systemUserInstance) {
            systemUserInstance = new SystemUser()
            systemUserInstance.errorMessage(code: 'systemUser.not.known', args: [params.loginId], default: "Unknown user ${params.loginId}")
            render(view: 'login', model: [systemUserInstance: systemUserInstance])
        } else {
            return [systemUserInstance: systemUserInstance]
        }
    }

    def notification() {
        def systemUserInstance = SystemUser.findByLoginId(params.loginId)

        if (!systemUserInstance) {
            systemUserInstance = new SystemUser()
            systemUserInstance.errorMessage(code: 'systemUser.not.known', args: [params.loginId], default: "Unknown user ${params.loginId}")
            render(view: 'login', model: [systemUserInstance: systemUserInstance])
        } else if (params.answer?.equalsIgnoreCase(systemUserInstance.securityAnswer)) {
            def pw = generateNewPassword()
            systemUserInstance.password = pw
            systemUserInstance.passwordConfirmation = pw
            systemUserInstance.currentPassword = CacheService.IMPOSSIBLE_VALUE  // Not a real end-user password change
            systemUserInstance.nextPasswordChange = UtilService.EPOCH    // Let the domain know we want to expire the new password immediately
            def title = message(code: 'systemUser.forgot.email.subject', default: 'Password changed')
            if (systemUserInstance.verifyPasswordStatus() && systemUserInstance.saveThis()) {
                sendMail {
                    to systemUserInstance.email
                    subject title
                    body(view: '/emails/newPassword', model: [systemUserInstance: systemUserInstance, title: title])
                }

                flash.message = message(code: 'systemUser.good.answer', default: 'An email has been sent to you containing your new password. You will be obliged to change this password the next time you log in.')
                systemUserInstance = new SystemUser()
                render(view: 'login', model: [systemUserInstance: systemUserInstance])
            } else {
                flash.message = message(code: 'systemUser.failed.answer', default: 'Was unable to generate a new password for you. Please try again.')
                redirect(action: 'login')
            }
        } else {
            systemUserInstance.errorMessage(code: 'systemUser.bad.answer', default: 'Your answer was incorrect')
            render(view: 'forgot', model: [systemUserInstance: systemUserInstance, answer: params.answer])
        }
    }

    def change() {
        def systemUserInstance = SystemUser.get(utilService.currentUser()?.id)

        if (!systemUserInstance) {
            systemUserInstance = new SystemUser()
            systemUserInstance.errorMessage(code: 'not.found', domain: 'systemUser', value: utilService.currentUser()?.id)
            render(view: 'login', model: [systemUserInstance: systemUserInstance])
        } else {
            return [systemUserInstance: systemUserInstance]
        }
    }

    def proceed() {
        def systemUserInstance = SystemUser.get(utilService.currentUser()?.id)
        if (systemUserInstance) {
            systemUserInstance.properties['loginId', 'currentPassword', 'password', 'passwordConfirmation'] = params
            if (!systemUserInstance.hasErrors() && systemUserInstance.verifyPasswordStatus() && systemUserInstance.saveThis()) {
                flash.message = message(code: 'systemUser.password.changed', default: 'Your password has been changed')
                utilService.setNextStep(null)
                def companies = CompanyUser.findAllByUser(systemUserInstance)
                if (!companies) {
                    redirect(controller: 'system', action: (utilService.systemSetting('isDemoSystem', false) ? 'intro' : 'assign'))
                } else if (companies.size() == 1) {
                    utilService.newCurrentCompany(companies[0].company.id)
                    redirect(controller: 'systemMenu', action: 'display')
                } else {
                    utilService.setNextStep([sourceController: 'companyUser', sourceAction: 'select',
                            targetController: 'companyUser', targetAction: 'attach'])
                    redirect(controller: 'companyUser', action: 'select')
                }
            } else {
                systemUserInstance.currentPassword = null
                systemUserInstance.password = null
                systemUserInstance.passwordConfirmation = null
                render(view: 'change', model: [systemUserInstance: systemUserInstance])
            }
        } else {
            systemUserInstance = new SystemUser()
            systemUserInstance.errorMessage(code: 'not.found', domain: 'systemUser', value: utilService.currentUser()?.id)
            render(view: 'login', model: [systemUserInstance: systemUserInstance])
        }
    }

    def profile() {
        def systemUserInstance = SystemUser.get(utilService.currentUser()?.id)

        if (!systemUserInstance) {
            flash.message = utilService.standardMessage('not.found', 'systemUser', utilService.currentUser()?.id)
            redirect(uri: '/')
        } else {
            return [systemUserInstance: systemUserInstance, currentCompany: utilService.currentCompany()]
        }
    }

    def modify(Long version) {
        def systemUserInstance = SystemUser.get(utilService.currentUser()?.id)
        if (systemUserInstance) {
            if (version != null && systemUserInstance.version > version) {
                systemUserInstance.errorMessage(code: 'locking.failure', domain: 'systemUser')
                render(view: 'profile', model: [systemUserInstance: systemUserInstance, currentCompany: utilService.currentCompany()])
                return
            }

            def oldCountryId = systemUserInstance.country.id
            def oldLanguageId = systemUserInstance.language.id
            systemUserInstance.properties['loginId', 'name', 'email', 'currentPassword', 'password', 'passwordConfirmation',
                    'securityQuestion', 'securityAnswer', 'country', 'language', 'disableHelp'] = params
            if (!systemUserInstance.hasErrors() && systemUserInstance.verifyPasswordStatus() && systemUserInstance.saveThis()) {
                if (oldCountryId != systemUserInstance.country.id || oldLanguageId != systemUserInstance.language.id) {
                    utilService.setSessionLocale(new Locale(systemUserInstance.language.code, systemUserInstance.country.code))
                }

                flash.message = message(code: 'systemUser.modified', default: 'User profile updated')
            }

            systemUserInstance.currentPassword = null
            systemUserInstance.password = null
            systemUserInstance.passwordConfirmation = null

            render(view: 'profile', model: [systemUserInstance: systemUserInstance, currentCompany: utilService.currentCompany()])
        } else {
            flash.message = utilService.standardMessage('not.found', 'systemUser', utilService.currentUser()?.id)
            redirect(uri: '/')
        }
    }

    def register() {
        if (UtilService.getCurrentOperatingState() != 'active') {
            redirect(controller: 'system', action: 'loginDisabled')
        }

        def systemUserInstance = new SystemUser()
        systemUserInstance.properties['loginId', 'name', 'email', 'password', 'passwordConfirmation',
                'securityQuestion', 'securityAnswer', 'country', 'language', 'accessCode'] = params
        if (!systemUserInstance.country && !systemUserInstance.language) {
            def locale = utilService.currentLocale()
            systemUserInstance.country = SystemCountry.findByCode(locale.getCountry() ?: 'US')
            systemUserInstance.language = SystemLanguage.findByCode(locale.getLanguage() ?: 'en')
        }

        return [systemUserInstance: systemUserInstance]
    }

    def registration() {
        if (UtilService.getCurrentOperatingState() != 'active') {
            redirect(controller: 'system', action: 'loginDisabled')
        }

        utilService.clearCurrentUser()

        // Grab the access code and don't pass it on to the domain
        def accessCode = params.remove('accessCode')

        def systemUserInstance = new SystemUser()
        systemUserInstance.properties['loginId', 'name', 'email', 'password', 'passwordConfirmation', 'securityQuestion', 'securityAnswer', 'country', 'language'] = params
        systemUserInstance.lastLogin = new Date()

        // Check if they got the access code correct
        def captcha = session.captcha
        session.captcha = null
        if (!accessCode || !captcha || !accessCode.equalsIgnoreCase(captcha)) {
            systemUserInstance.errorMessage(field: 'accessCode', code: 'systemUser.accessCode.invalid', default: 'Invalid access code. Please enter the code shown in the image.')
        }

        if (!systemUserInstance.hasErrors() && systemUserInstance.verifyPasswordStatus() && systemUserInstance.saveThis()) {
            utilService.newCurrentUser(systemUserInstance)
            flash.message = message(code: 'systemUser.registered', args: [systemUserInstance.loginId], default: "You are now registered and logged in as user ${systemUserInstance.loginId}")
            redirect(controller: 'system', action: (utilService.systemSetting('isDemoSystem', false) ? 'intro' : 'assign'))
        } else {
            systemUserInstance.password = null
            systemUserInstance.passwordConfirmation = null
            render(view: 'register', model: [systemUserInstance: systemUserInstance])
        }
    }

    def captcha() {

        // We're going to output a png image which should not be cached by the browser
        response.setContentType('image/png')
        response.setHeader('Cache-control', 'no-cache')

        // Generate and remember the Source Character string (6 characters)
        def length = CAPTCHACHARS.length()
        def sb = new StringBuilder()
        def pos
        6.times {
            pos = (int) (Math.random() * length)
            sb.append(CAPTCHACHARS.charAt(pos))
        }

        def height = 200
        def width = 200
        def space = 8

        Font font = new Font('Serif', Font.BOLD, 18)
        Rectangle2D fontRect
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB)
        Graphics2D g = bufferedImage.createGraphics()
        try {
            g.setFont(font)
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            fontRect = font.getStringBounds(sb.toString(), g.getFontRenderContext())
        } finally {
            g.dispose()
        }

        // Now, create a graphic 'space' pixels wider and taller than the the font
        bufferedImage = new BufferedImage((int) fontRect.getWidth() + space, (int) fontRect.getHeight() + space, BufferedImage.TYPE_INT_RGB)
        g = bufferedImage.createGraphics()
        try {
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g.setFont(font)

            // Draw the background
            g.setColor(Color.WHITE)
            g.fillRect(0, 0, width, height)

            // Draw the lines
            g.setColor(Color.GRAY)

            def x1, y1, x2, y2
            def step = 10
            x1 = 0
            y1 = step
            x2 = step
            y2 = 0

            while (x1 < width || x2 < width || y1 < height || y2 < height) {
                g.drawLine(x1, y1, x2, y2)
                if (y1 < height) {
                    x1 = 0
                    y1 += step
                } else if (x1 < width) {
                    y1 = height
                    x1 += step
                } else {
                    x1 = width
                    y1 = height
                }

                if (x2 < width) {
                    y2 = 0
                    x2 += step
                } else if (y2 < height) {
                    x2 = width
                    y2 += step
                } else {
                    y2 = height
                    x2 = width
                }
            }

            // Draw the String
            g.setColor(Color.BLACK)

            g.drawString(sb.toString(), (int) (space / 2), (int) (space / 4) + (int) fontRect.getHeight())
        } finally {
            g.dispose()
        }

        // Write the image out to the browser
        OutputStream out = response.getOutputStream()
        try {
            javax.imageio.ImageIO.write(bufferedImage, 'PNG', out)
        } finally {
            out.close()
        }

        // Remember the code
        session.captcha = sb.toString()
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private generateNewPassword() {
        def words = ['anchor', 'ability', 'antonym', 'battery', 'beached', 'bedrock', 'canton', 'carbon', 'concise',
                'decider', 'digital', 'dulcet', 'edited', 'effect', 'elastic', 'faster', 'feather', 'formic', 'general', 'glance', 'gnomic',
                'habitat', 'halter', 'hiking', 'ignite', 'images', 'inboard', 'jointed', 'judges', 'justify', 'kaftan', 'kennel', 'kindled',
                'linkup', 'loofah', 'lordly', 'majestic', 'manager', 'monarch', 'nearly', 'nickel', 'nutrient', 'oarlock', 'opaque', 'opulent',
                'parkland', 'parsnip', 'popcorn', 'quaver', 'queried', 'quoted', 'rabbit', 'require', 'roadside', 'school', 'sender', 'sonnet',
                'teacup', 'thorns', 'trumpet', 'unarmed', 'unbend', 'urgent', 'vizier', 'volatile', 'voyage', 'waffle', 'waiter', 'weight',
                'xenophobia', 'xeroxed', 'xylophone', 'yachting', 'yarrow', 'yellow', 'zealous', 'zipped', 'zircon']

        def rnd = new Random()
        def word = words[rnd.nextInt(78)]
        word += Integer.toString(rnd.nextInt(1000) + 1000).substring(1)
        return word
    }
}