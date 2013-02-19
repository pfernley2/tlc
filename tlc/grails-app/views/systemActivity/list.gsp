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
    <title><g:msg code="list" domain="systemActivity"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemActivity"/></g:link></span>
    <g:if test="${ddSource}">
        <span class="menuButton"><g:link class="links" action="links"><g:msg code="generic.define.links" default="Define Links"/></g:link></span>
    </g:if>
</div>
<div id="main-content" class="body" role="main">
    <g:if test="${ddSource}">
        <g:pageTitle code="list.for" domain="systemActivity" forDomain="${ddSource}" value="${message(code: 'role.name.' + ddSource.code, default: ddSource.name)}" returns="true"/>
    </g:if>
    <g:else>
        <g:pageTitle code="list" domain="systemActivity"/>
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

                <g:sortableColumn property="code" title="Code" titleKey="systemActivity.code.label"/>

                <g:sortableColumn property="systemOnly" title="System Only" titleKey="systemActivity.systemOnly.label"/>

                <th><g:msg code="systemActivity.actions.label" default="Actions"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${systemActivityInstanceList}" status="i" var="systemActivityInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${systemActivityInstance.id}">${display(bean: systemActivityInstance, field: 'code')}</g:link></td>

                    <td>${display(bean: systemActivityInstance, field: 'systemOnly')}</td>

                    <td><g:drilldown controller="systemAction" action="list" value="${systemActivityInstance.id}"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${systemActivityInstanceTotal}"/>
    </div>
</div>
</body>
</html>
