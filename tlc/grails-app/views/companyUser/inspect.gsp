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
    <meta name="generator" content="company"/>
    <title><g:msg code="show" domain="companyUser"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="display"><g:msg code="list" domain="companyUser"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="companyUser"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${companyUserInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${companyUserInstance}"/>
        </div>
    </g:hasErrors>
    <g:if test="${!companyUserInstance.user.administrator}">
        <div class="center tinyTopMargin smallBottomMargin">
            <g:if test="${otherCompanies <= 0}">
                <g:msg code="companyUser.zero.message" default="This user does not belong to any other company. You may simply remove them as a member of this company, leaving their login id active for future use by another company, or you may delete their login id altogether."/>
            </g:if>
            <g:elseif test="${otherCompanies == 1}">
                <g:msg code="companyUser.one.message" default="This user belongs to 1 other company. You may remove them from membership of the current company but this will not remove them from the other company. Only a System Administrator can delete a user from multiple companies simultaneously."/>
            </g:elseif>
            <g:else>
                <g:msg code="companyUser.many.message" args="${[otherCompanies]}" default="This user belongs to ${otherCompanies} other companies. You may remove them from membership of the current company but this will not remove them from the other companies. Only a System Administrator can delete a user from multiple companies simultaneously."/>
            </g:else>
        </div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.loginId.label" default="Login Id"/></td>

                <td class="value">${companyUserInstance.user.loginId.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.name.label" default="Name"/></td>

                <td class="value">${companyUserInstance.user.name.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemUser.lastLogin.label" default="Last Login"/></td>

                <td class="value">${display(value: companyUserInstance.user.lastLogin, scale: 2)}</td>

            </tr>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${companyUserInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="remove" value="${msg(code:'companyUser.remove', 'default':'Remove')}"/></span>
            <g:if test="${(!companyUserInstance.user.administrator && otherCompanies <= 0)}">
                <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="terminate" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
            </g:if>
        </g:form>
    </div>
</div>
</body>
</html>
