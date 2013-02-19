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
<%@ page import="org.grails.tlc.corp.Unit; org.grails.tlc.corp.Measure; org.grails.tlc.corp.Scale" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="company"/>
    <title><g:msg code="list" domain="unit"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="unit"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:if test="${(ddSource && ddSource instanceof Measure)}">
        <g:pageTitle code="list.for" domain="unit" forDomain="${ddSource}" value="${message(code: 'measure.name.' + ddSource.code, default: ddSource.name)}" returns="true"/>
    </g:if>
    <g:elseif test="${(ddSource && ddSource instanceof Scale)}">
        <g:pageTitle code="list.for" domain="unit" forDomain="${ddSource}" value="${message(code: 'scale.name.' + ddSource.code, default: ddSource.name)}" returns="true"/>
    </g:elseif>
    <g:else>
        <g:pageTitle code="list" domain="unit"/>
    </g:else>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="code, multiplier"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="unit.code.label"/>

                <th><g:msg code="unit.name.label" default="Name"/></th>

                <th><g:msg code="unit.measure.label" default="Measure"/></th>

                <th><g:msg code="unit.scale.label" default="Scale"/></th>

                <g:sortableColumn property="multiplier" title="Multiplier" titleKey="unit.multiplier.label"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${unitInstanceList}" status="i" var="unitInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${unitInstance.id}">${display(bean:unitInstance, field:'code')}</g:link></td>

                    <td><g:msg code="unit.name.${unitInstance.code}" default="${unitInstance.name}"/></td>

                    <td><g:msg code="measure.name.${unitInstance.measure.code}" default="${unitInstance.measure.name}"/></td>

                    <td><g:msg code="scale.name.${unitInstance.scale.code}" default="${unitInstance.scale.name}"/></td>

                    <td>${display(bean:unitInstance, field:'multiplier')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${unitInstanceTotal}"/>
    </div>
</div>
</body>
</html>
