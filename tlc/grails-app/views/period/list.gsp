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
<%@ page import="org.grails.tlc.books.Period" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="accounts"/>
    <title><g:msg code="list" domain="period"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="period"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:if test="${ddSource}">
        <g:pageTitle code="list.for" domain="period" forDomain="${ddSource}" value="${ddSource.code}" returns="true"/>
    </g:if>
    <g:else>
        <g:pageTitle code="list" domain="period"/>
    </g:else>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="code, validFrom, status*, validTo"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="period.code.label"/>

                <g:sortableColumn property="validFrom" title="Valid From" titleKey="period.validFrom.label"/>

                <g:sortableColumn property="validTo" title="Valid To" titleKey="period.validTo.label"/>

                <g:sortableColumn property="status" title="Status" titleKey="period.status.label"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${periodInstanceList}" status="i" var="periodInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${periodInstance.id}">${display(bean: periodInstance, field: 'code')}</g:link></td>

                    <td>${display(bean: periodInstance, field: 'validFrom', scale: 1)}</td>

                    <td>${display(bean: periodInstance, field: 'validTo', scale: 1)}</td>

                    <td><g:msg code="period.status.${periodInstance.status}" default="${periodInstance.status}"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${periodInstanceTotal}"/>
    </div>
</div>
</body>
</html>
