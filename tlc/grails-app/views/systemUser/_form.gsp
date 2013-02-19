<%--
 ~  Copyright 2010-2013 Paul Fernley.
 ~
 ~  This file is part of the Three Ledger Core (TLC) software
 ~  from Paul Fernley.
 ~
 ~  TLC is free software: you can redistribute it and/or modify
 ~  it under the terms of the GNU General Public License as published by
 ~  the Free Software Foundation, either version 3 of the License, or
 ~  (at your option) any later version.
 ~
 ~  TLC is distributed in the hope that it will be useful,
 ~  but WITHOUT ANY WARRANTY; without even the implied warranty of
 ~  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ~  GNU General Public License for more details.
 ~
 ~  You should have received a copy of the GNU General Public License
 ~  along with TLC. If not, see <http://www.gnu.org/licenses/>.
 --%>
<%@ page import="org.grails.tlc.sys.SystemLanguage; org.grails.tlc.sys.SystemCountry" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="loginId"><g:msg code="systemUser.loginId.label" default="Login Id"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'loginId', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="20" size="20" id="loginId" name="loginId" value="${display(bean: systemUserInstance, field: 'loginId')}"/>&nbsp;<g:help code="systemUser.loginId"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="systemUser.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="50" size="30" id="name" name="name" value="${display(bean: systemUserInstance, field: 'name')}"/>&nbsp;<g:help code="systemUser.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="email"><g:msg code="systemUser.email.label" default="Email"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'email', 'errors')}">
                <input type="text" maxlength="100" size="30" id="email" name="email" value="${display(bean: systemUserInstance, field: 'email')}"/>&nbsp;<g:help code="systemUser.email"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="password"><g:msg code="systemUser.password.label" default="Password"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'password', 'errors')}">
                <input type="password" size="20" name="password" id="password" value="${display(bean: systemUserInstance, field: 'password')}"/>&nbsp;<g:help code="systemUser.password"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="passwordConfirmation"><g:msg code="systemUser.passwordConfirmation.label" default="Password Confirmation"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'passwordConfirmation', 'errors')}">
                <input type="password" size="20" name="passwordConfirmation" id="passwordConfirmation" value="${display(bean: systemUserInstance, field: 'passwordConfirmation')}"/>&nbsp;<g:help code="systemUser.passwordConfirmation"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="securityQuestion"><g:msg code="systemUser.securityQuestion.label" default="Security Question"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'securityQuestion', 'errors')}">
                <input type="text" maxlength="100" size="30" id="securityQuestion" name="securityQuestion" value="${display(bean: systemUserInstance, field: 'securityQuestion')}"/>&nbsp;<g:help code="systemUser.securityQuestion"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="securityAnswer"><g:msg code="systemUser.securityAnswer.label" default="Security Answer"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'securityAnswer', 'errors')}">
                <input type="text" maxlength="30" size="20" id="securityAnswer" name="securityAnswer" value="${display(bean: systemUserInstance, field: 'securityAnswer')}"/>&nbsp;<g:help code="systemUser.securityAnswer"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="country"><g:msg code="systemUser.country.label" default="Country"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'country', 'errors')}">
                <g:domainSelect name="country.id" options="${SystemCountry.list()}" selected="${systemUserInstance?.country}" prefix="country.name" code="code" default="name"/>&nbsp;<g:help code="systemUser.country"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="language"><g:msg code="systemUser.language.label" default="Language"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'language', 'errors')}">
                <g:domainSelect name="language.id" options="${SystemLanguage.list()}" selected="${systemUserInstance?.language}" prefix="language.name" code="code" default="name"/>&nbsp;<g:help code="systemUser.language"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="lastLogin"><g:msg code="systemUser.lastLogin.label" default="Last Login"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'lastLogin', 'errors')}">
                <input type="text" size="20" id="lastLogin" name="lastLogin" value="${display(bean: systemUserInstance, field: 'lastLogin', scale: 2)}"/>&nbsp;<g:help code="systemUser.lastLogin"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="disabledUntil"><g:msg code="systemUser.disabledUntil.label" default="Disabled Until"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'disabledUntil', 'errors')}">
                <input type="text" size="20" id="disabledUntil" name="disabledUntil" value="${display(bean: systemUserInstance, field: 'disabledUntil', scale: 2)}"/>&nbsp;<g:help code="systemUser.disabledUntil"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="nextPasswordChange"><g:msg code="systemUser.nextPasswordChange.label" default="Next Password Change"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'nextPasswordChange', 'errors')}">
                <g:if test="${systemUserInstance?.id}">
                    <input type="text" size="20" id="nextPasswordChange" name="nextPasswordChange" value="${display(bean: systemUserInstance, field: 'nextPasswordChange', scale: 2)}"/>&nbsp;<g:help code="systemUser.nextPasswordChange"/>
                </g:if>
                <g:else>
                    ${display(bean: systemUserInstance, field: 'nextPasswordChange', scale: 2)}
                </g:else>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="oldPassword1"><g:msg code="systemUser.oldPassword1.label" default="Old Password 1"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'oldPassword1', 'errors')}">
                <input type="text" id="oldPassword1" name="oldPassword1" value="${display(bean: systemUserInstance, field: 'oldPassword1')}"/>&nbsp;<g:help code="systemUser.oldPassword1"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="oldPassword2"><g:msg code="systemUser.oldPassword2.label" default="Old Password 2"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'oldPassword2', 'errors')}">
                <input type="text" id="oldPassword2" name="oldPassword2" value="${display(bean: systemUserInstance, field: 'oldPassword2')}"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="oldPassword3"><g:msg code="systemUser.oldPassword3.label" default="Old Password 3"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'oldPassword3', 'errors')}">
                <input type="text" id="oldPassword3" name="oldPassword3" value="${display(bean: systemUserInstance, field: 'oldPassword3')}"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="administrator"><g:msg code="systemUser.administrator.label" default="Administrator"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'administrator', 'errors')}">
                <g:checkBox name="administrator" value="${systemUserInstance?.administrator}"></g:checkBox>&nbsp;<g:help code="systemUser.administrator"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="disableHelp"><g:msg code="systemUser.disableHelp.label" default="Disable Help"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUserInstance, field: 'disableHelp', 'errors')}">
                <g:checkBox name="disableHelp" value="${systemUserInstance?.disableHelp}"></g:checkBox>&nbsp;<g:help code="systemUser.disableHelp"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
