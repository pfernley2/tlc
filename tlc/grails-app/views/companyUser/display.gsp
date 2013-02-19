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
    <title><g:msg code="list" domain="companyUser"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="add"><g:msg code="add" domain="companyUser"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="companyUser"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="criteria">
        <g:criteria domain="SystemUser" include="loginId, name, lastLogin"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="loginId" title="Login Id" titleKey="systemUser.loginId.label"/>

                <g:sortableColumn property="name" title="Name" titleKey="systemUser.name.label"/>

                <g:sortableColumn property="lastLogin" title="Last Login" titleKey="systemUser.lastLogin.label"/>

                <th><g:msg code="companyUser.roles.label" default="Roles"/>

                <th><g:msg code="companyUser.accessGroups.label" default="Access Groups"/>

                <th><g:msg code="companyUser.agentCredentials.label" default="Agent Credentials"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${companyUserInstanceList}" status="i" var="companyUserInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="inspect" id="${companyUserInstance.id}">${companyUserInstance.user.loginId.encodeAsHTML()}</g:link></td>

                    <td>${companyUserInstance.user.name.encodeAsHTML()}</td>

                    <td>${display(value: companyUserInstance.user.lastLogin, scale: 2)}</td>

                    <td><g:drilldown controller="systemRole" action="display" value="${companyUserInstance.id}"/></td>

                    <td><g:drilldown controller="accessGroup" action="display" value="${companyUserInstance.id}"/></td>

                    <td><g:drilldown controller="agentCredential" action="list" value="${companyUserInstance.id}"/></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${companyUserInstanceTotal}"/>
    </div>
</div>
</body>
</html>
