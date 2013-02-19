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
<!doctype html>
<html>
<head>
    <meta name="generator" content="system"/>
    <title><g:msg code="show" domain="systemUser"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="systemUser"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemUser"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="systemUser"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${systemUserInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${systemUserInstance}"/>
        </div>
    </g:hasErrors>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'id')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.loginId.label" default="Login Id"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'loginId')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.name.label" default="Name"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'name')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.email.label" default="Email"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'email')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.salt.label" default="Salt"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'salt')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.encryptedPassword.label" default="Encrypted Password"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'encryptedPassword')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.securityQuestion.label" default="Security Question"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'securityQuestion')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.securityAnswer.label" default="Security Answer"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'securityAnswer')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.country.label" default="Country"/></td>

                <td class="value"><g:msg code="country.name.${systemUserInstance.country.code}" default="${systemUserInstance.country.name}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.language.label" default="Language"/></td>

                <td class="value"><g:msg code="language.name.${systemUserInstance.language.code}" default="${systemUserInstance.language.name}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.lastLogin.label" default="Last Login"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'lastLogin', scale: 2)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.disabledUntil.label" default="Disabled Until"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'disabledUntil', scale: 2)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.nextPasswordChange.label" default="Next Password Change"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'nextPasswordChange', scale: 2)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.oldPassword1.label" default="Old Password 1"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'oldPassword1')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.oldPassword2.label" default="Old Password 2"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'oldPassword2')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.oldPassword3.label" default="Old Password 3"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'oldPassword3')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.administrator.label" default="Administrator"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'administrator')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.disableHelp.label" default="Disable Help"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'disableHelp')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'securityCode')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'dateCreated')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'lastUpdated')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                <td class="value">${display(bean: systemUserInstance, field: 'version')}</td>

            </tr>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${systemUserInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
