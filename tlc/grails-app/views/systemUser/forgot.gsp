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
    <title><g:msg code="systemUser.forgot" default="Forgotten Password"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="login" action="logout"><g:msg code="systemUser.login.button" default="Login"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="systemUser.forgot" default="Forgotten Password"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${systemUserInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${systemUserInstance}"/>
        </div>
    </g:hasErrors>
    <g:form action="notification" method="post">
        <input type="hidden" name="loginId" value="${systemUserInstance?.loginId}"/>
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="left mediumTopPadding mediumBottomPadding" colspan="2">
                        <g:msg code="systemUser.forgot.security" default="You need to correctly answer the security question below and then press the New Password button. You will then be emailed with a new password which you will be obliged to change the next time you log in."/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="systemUser.loginId.label" default="Login Id"/></td>

                    <td class="value">${display(bean: systemUserInstance, field: 'loginId')}</td>
                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="systemUser.securityQuestion.label" default="Security Question"/></td>

                    <td class="value">${display(bean: systemUserInstance, field: 'securityQuestion')}</td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="answer"><g:msg code="systemUser.securityAnswer.label" default="Security Answer"/></label>
                    </td>
                    <td class="value">
                        <input autofocus="autofocus" type="text" maxlength="30" id="answer" name="answer" value="${answer?.encodeAsHTML()}"/>&nbsp;<g:help code="systemUser.securityAnswer"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>
        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="${msg(code: 'systemUser.forgot.button', 'default': 'New Password')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
