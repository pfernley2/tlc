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

import org.grails.tlc.books.Mnemonic
import org.grails.tlc.corp.CompanyUser
import org.grails.tlc.corp.QueuedTask
import org.grails.tlc.corp.Task
import java.security.MessageDigest
import java.util.concurrent.atomic.AtomicLong
import sun.misc.BASE64Encoder

class SystemUser {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    private static final String chars = '0123456789abcdef'

    static final PASSWORD_ATTEMPTS = 3
    static final PASSWORD_ATTEMPT_MINUTES = 15
    static final PASSWORD_LOCKOUT_MINUTES = 60
    static final PASSWORD_LIFE_DAYS = 180

    static belongsTo = [country: SystemCountry, language: SystemLanguage]
    static hasMany = [companies: CompanyUser, tasks: Task, queued: QueuedTask, mnemonics: Mnemonic]

    static transients = ['password', 'passwordConfirmation', 'currentPassword', 'accessCode']

    String loginId
    String name
    String email
    String currentPassword
    String password
    String passwordConfirmation
    String salt = CacheService.IMPOSSIBLE_VALUE
    String encryptedPassword = CacheService.IMPOSSIBLE_VALUE
    String securityQuestion
    String securityAnswer
    Date lastLogin
    Date disabledUntil
    Date nextPasswordChange = new Date(System.currentTimeMillis() + (PASSWORD_LIFE_DAYS * 86400000L))
    String oldPassword1
    String oldPassword2
    String oldPassword3
    Boolean administrator = false
    Boolean disableHelp = false
    String accessCode
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        cache true
        columns {
            country lazy: true
            language lazy: true
            companies cascade: 'all'
            tasks cascade: 'save-update'
            queued cascade: 'all'
            mnemonics cascade: 'all'
        }
    }

    static constraints = {
        loginId(blank: false, size: 1..20, matches: '[a-zA-Z][a-zA-Z_0-9]*', unique: true)
        name(blank: false, size: 1..50)
        email(blank: false, email: true, size: 1..100)
        securityQuestion(blank: false, size: 1..100)
        securityAnswer(blank: false, size: 1..30)
        lastLogin(nullable: true, range: UtilService.validDateRange())
        disabledUntil(nullable: true, range: UtilService.validDateRange())
        nextPasswordChange(range: UtilService.validDateRange())
        oldPassword1(nullable: true)
        oldPassword2(nullable: true)
        oldPassword3(nullable: true)
        securityCode(validator: {val, obj ->
            return (val == 0)
        })
    }

    def verifyPasswordStatus() {
        def newUser = (salt == CacheService.IMPOSSIBLE_VALUE)
        def newPassword = (password || passwordConfirmation)
        def cp, np

        // If they want to change an existing password (and it's really
        // the end user that's doing this rather than us or the administrator)
        if (newPassword && !newUser && currentPassword != CacheService.IMPOSSIBLE_VALUE) {

            // Check they correctly entered their current password
            if (!currentPassword || encryptedPassword != encryptPassword(currentPassword, salt)) {
                this.errorMessage(code: 'systemUser.salt.missing.error', default: 'Current password incorrect')
                return false
            }

            // Check they are not re-using a recent password again
            cp = currentPassword.encodeAsBase64()
            np = password?.encodeAsBase64()
            if (np && (np == cp || np == oldPassword1 || np == oldPassword2 || np == oldPassword3)) {
                this.errorMessage(code: 'systemUser.salt.duplication.error', default: 'New password cannot be the same as previous passwords')
                return false
            }
        }

        // If a new password needs to be checked out
        if (newUser || newPassword) {
            if (!password || !passwordConfirmation || password != passwordConfirmation || password.length() < 8 || password.length() > 30) {
                this.errorMessage(code: 'systemUser.salt.mismatch.error', default: 'Matching password and password confirmation values from 8 to 30 characters long are required')
                return false
            }

            salt = createSalt()
            encryptedPassword = encryptPassword(password, salt)

            // The only time the nextPasswordChange property is set to the epoch
            // is when we are setting a new password for them since they have
            // forgotten their old one. In such cases, we expire the new password
            // immediately. In all other cases we expire it at the end of the
            // normal interval.
            nextPasswordChange = (nextPasswordChange == UtilService.EPOCH) ? new Date() : new Date(System.currentTimeMillis() + (PASSWORD_LIFE_DAYS * 86400000L))

            // If they are changing an existing password update their history
            if (cp) {
                oldPassword1 = oldPassword2
                oldPassword2 = oldPassword3
                oldPassword3 = cp
            }
        }

        return true
    }

    def passwordValid(pw) {
        return (pw && encryptedPassword == encryptPassword(pw, salt))
    }

    def passwordExpired() {
        return (nextPasswordChange < new Date())
    }

    def accountEnabled() {
        return (disabledUntil == null || disabledUntil <= new Date())
    }

    def afterInsert() {
        UtilService.trace('insert', this)
    }

    def afterUpdate() {
        UtilService.trace('update', this)
    }

    def afterDelete() {
        UtilService.trace('delete', this)
    }

    public String toString() {
        return loginId
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private static createSalt() {
        Random rnd = new Random()
        int len = rnd.nextInt(5) + 4
        byte[] bytes = new byte[len]
        rnd.nextBytes(bytes)
        StringBuffer sb = new StringBuffer(len * 2)
        for (byte b: bytes) {
            sb.append(chars[(b >> 4) & 0x0f])
            sb.append(chars[b & 0x0f])

        }

        return sb.toString()
    }

    private static encryptPassword(password, salt) {
        int pos = salt.length() / 2
        password = salt.substring(pos) + password + salt.substring(0, pos)
        MessageDigest md = MessageDigest.getInstance('SHA')
        md.reset()
        md.update(password.getBytes('UTF-8'))
        return (new BASE64Encoder()).encode(md.digest()).substring(0, 27)
    }
}
