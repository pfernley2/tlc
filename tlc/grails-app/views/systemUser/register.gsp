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
<!doctype html>
<html>
<head>
    <title><g:msg code="systemUser.registration" default="New User Registration"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="systemUser.registration" default="New User Registration"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${systemUserInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${systemUserInstance}"/>
        </div>
    </g:hasErrors>
    <p class="center smallTopMargin mediumBottomMargin"><g:msg code="systemUser.privacy" default="Privacy Statement: The data you enter here is used solely for the purpose of operating the system."/></p>
    <g:form action="registration" method="post">
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
                        <label for="accessCode"><g:msg code="systemUser.accessCode.label" default="Access Code (shown below)"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: systemUserInstance, field: 'accessCode', 'errors')}">
                        <input type="text" id="accessCode" name="accessCode" value="${display(bean: systemUserInstance, field: 'accessCode')}"/>&nbsp;<g:help code="systemUser.accessCode"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td></td>
                    <td class="value">
                        <img src="${createLink(action: 'captcha')}" alt="${msg(code: 'generic.captcha.alt.text', default: 'Captcha image')}"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>
        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="${msg(code: 'systemUser.register', 'default': 'Register')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
