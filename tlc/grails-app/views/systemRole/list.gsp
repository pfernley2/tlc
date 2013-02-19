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
    <title><g:msg code="list" domain="systemRole"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemRole"/></g:link></span>
    <g:if test="${ddSource}">
        <span class="menuButton"><g:link class="links" action="links"><g:msg code="generic.define.links" default="Define Links"/></g:link></span>
    </g:if>
</div>
<div id="main-content" class="body" role="main">
    <g:if test="${ddSource}">
        <g:pageTitle code="systemRole.list.for" args="${[ddSource.user.name, ddSource.company.name]}" default="Role List for User ${ddSource.user.name} in Company ${ddSource.company.name}" returns="true"/>
    </g:if>
    <g:else>
        <g:pageTitle code="list" domain="systemRole"/>
    </g:else>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="criteria">
        <g:criteria include="code, systemOnly"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="systemRole.code.label"/>

                <th><g:msg code="systemRole.name.label" default="Name"/></th>

                <g:sortableColumn property="systemOnly" title="System Only" titleKey="systemRole.systemOnly.label" class="center"/>

                <th><g:msg code="systemRole.activities.label" default="Activities"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${systemRoleInstanceList}" status="i" var="systemRoleInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${systemRoleInstance.id}">${display(bean: systemRoleInstance, field: 'code')}</g:link></td>

                    <td><g:msg code="role.name.${systemRoleInstance.code}" default="${systemRoleInstance.name}"/></td>

                    <td class="center">${display(bean: systemRoleInstance, field: 'systemOnly')}</td>

                    <td><g:drilldown controller="systemActivity" action="list" value="${systemRoleInstance.id}"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${systemRoleInstanceTotal}"/>
    </div>
</div>
</body>
</html>
