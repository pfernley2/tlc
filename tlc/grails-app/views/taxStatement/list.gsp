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
<%@ page import="org.grails.tlc.corp.TaxStatement" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="list" domain="taxStatement"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="taxStatement"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="taxStatement"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="statementDate, description"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="statementDate" title="Statement Date" titleKey="taxStatement.statementDate.label"/>

                <th><g:msg code="taxStatement.authority.label" default="Tax Authority"/></th>

                <g:sortableColumn property="description" title="Description" titleKey="taxStatement.description.label"/>

                <th><g:msg code="taxStatement.document.label" default="Document"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${taxStatementInstanceList}" status="i" var="taxStatementInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${taxStatementInstance.id}">${display(bean: taxStatementInstance, field: 'statementDate', scale: 1)}</g:link></td>

                    <td>${taxStatementInstance.authority.name.encodeAsHTML()}</td>

                    <td>${display(bean: taxStatementInstance, field: 'description')}</td>

                    <g:if test="${taxStatementInstance.finalized}">
                        <td>${taxStatementInstance.document ? (taxStatementInstance.document.type.code + taxStatementInstance.document.code).encodeAsHTML() : msg(code: 'generic.not.applicable', default: 'n/a')}</td>
                    </g:if>
                    <g:else>
                        <td></td>
                    </g:else>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${taxStatementInstanceTotal}"/>
    </div>
</div>
</body>
</html>
