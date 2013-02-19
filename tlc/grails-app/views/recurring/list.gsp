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
<%@ page import="org.grails.tlc.books.Recurring" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="list" domain="recurring"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="recurring"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="recurring"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <th><g:msg code="recurring.account.label" default="Bank Account"/></th>

                <th><g:msg code="recurring.reference.label" default="Reference"/></th>

                <th><g:msg code="recurring.type.label" default="Document Type"/></th>

                <th><g:msg code="recurring.description.label" default="Description"/></th>

                <th><g:msg code="recurring.totalTransactions.label" default="Total Transactions"/></th>

                <th><g:msg code="recurring.processedCount.label" default="Processed Count"/></th>

                <th><g:msg code="recurring.nextDue.label" default="Next Due"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${recurringInstanceList}" status="i" var="recurringInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${recurringInstance.id}">${recurringInstance.account.name.encodeAsHTML()}</g:link></td>

                    <td>${display(bean:recurringInstance, field:'reference')}</td>

                    <td>${display(bean:recurringInstance, field:'type')}</td>

                    <td>${display(bean:recurringInstance, field:'description')}</td>

                    <td>${display(bean:recurringInstance, field:'totalTransactions')}</td>

                    <td>${display(bean:recurringInstance, field:'processedCount')}</td>

                    <td>${display(bean:recurringInstance, field:'nextDue', scale: 1)}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${recurringInstanceTotal}"/>
    </div>
</div>
</body>
</html>
