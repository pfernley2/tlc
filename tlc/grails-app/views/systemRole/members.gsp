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
    <title><g:msg code="systemRole.members" default="Role Member List"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="membership"><g:msg code="systemRole.membership" default="Edit Role Membership"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="systemRole.members.for" args="${[message(code: 'role.name.' + ddSource.code, default: ddSource.name)]}" default="Member List for Role ${message(code: 'role.name.' + ddSource.code, default: ddSource.name)}" returns="true"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="name" title="Name" titleKey="systemUser.name.label"/>

                <g:sortableColumn property="loginId" title="Login Id" titleKey="systemUser.loginId.label"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${memberList}" status="i" var="member">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td>${display(bean: member, field: 'name')}</td>

                    <td>${display(bean: member, field: 'loginId')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${memberTotal}"/>
    </div>
</div>
</body>
</html>
