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
    <title><g:msg code="list" domain="systemUser"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemUser"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="systemUser"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="criteria">
        <g:criteria include="loginId, name, email, lastLogin, disabledUntil, nextPasswordChange, administrator"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="loginId" title="Login Id" titleKey="systemUser.loginId.label"/>

                <g:sortableColumn property="name" title="Name" titleKey="systemUser.name.label"/>

                <g:sortableColumn property="email" title="Email" titleKey="systemUser.email.label"/>

                <th><g:msg code="systemUser.country.label" default="Country"/>

                <th><g:msg code="systemUser.language.label" default="Language"/>

                <g:sortableColumn property="lastLogin" title="Last Login" titleKey="systemUser.lastLogin.label"/>

                <g:sortableColumn property="disabledUntil" title="Disabled Until" titleKey="systemUser.disabledUntil.label"/>

                <g:sortableColumn property="nextPasswordChange" title="Next Password Change" titleKey="systemUser.nextPasswordChange.label"/>

                <g:sortableColumn property="administrator" title="Administrator" titleKey="systemUser.administrator.label"/>

                <th><g:msg code="systemUser.companies.label" default="Companies"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${systemUserInstanceList}" status="i" var="systemUserInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${systemUserInstance.id}">${display(bean: systemUserInstance, field: 'loginId')}</g:link></td>

                    <td>${display(bean: systemUserInstance, field: 'name')}</td>

                    <td>${display(bean: systemUserInstance, field: 'email')}</td>

                    <td><g:msg code="country.name.${systemUserInstance.country.code}" default="${systemUserInstance.country.name}"/></td>

                    <td><g:msg code="language.name.${systemUserInstance.language.code}" default="${systemUserInstance.language.name}"/></td>

                    <td>${display(bean: systemUserInstance, field: 'lastLogin', scale: 2)}</td>

                    <td>${display(bean: systemUserInstance, field: 'disabledUntil', scale: 2)}</td>

                    <td>${display(bean: systemUserInstance, field: 'nextPasswordChange', scale: 2)}</td>

                    <td>${display(bean: systemUserInstance, field: 'administrator')}</td>

                    <td><g:drilldown controller="companyUser" action="list" value="${systemUserInstance.id}"/></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${systemUserInstanceTotal}"/>
    </div>
</div>
</body>
</html>
