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
<%@ page import="org.grails.tlc.sys.SystemRegion" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="system"/>
    <title><g:msg code="list" domain="systemRegion"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemRegion"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:if test="${ddSource}">
        <g:pageTitle code="list.for" domain="systemRegion" forDomain="${ddSource}" value="${message(code: 'geo.name.' + ddSource.code, default: ddSource.name)}" returns="true"/>
    </g:if>
    <g:else>
        <g:pageTitle code="list" domain="systemRegion"/>
    </g:else>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="code"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="systemRegion.code.label"/>

                <th><g:msg code="systemRegion.name.label" default="Name"/></th>

                <th><g:msg code="systemRegion.geo.label" default="Geo"/></th>

                <th><g:msg code="systemRegion.countries.label" default="Countries"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${systemRegionInstanceList}" status="i" var="systemRegionInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${systemRegionInstance.id}">${display(bean: systemRegionInstance, field: 'code')}</g:link></td>

                    <td><g:msg code="region.name.${systemRegionInstance.code}" default="${systemRegionInstance.name}"/></td>

                    <td><g:msg code="geo.name.${systemRegionInstance.geo.code}" default="${systemRegionInstance.geo.name}"/></td>

                    <td><g:drilldown controller="systemCountry" value="${systemRegionInstance.id}"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${systemRegionInstanceTotal}"/>
    </div>
</div>
</body>
</html>
