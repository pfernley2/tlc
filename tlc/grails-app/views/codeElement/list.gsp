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
<%@ page import="org.grails.tlc.books.CodeElement" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="accounts"/>
    <title><g:msg code="list" domain="codeElement"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <g:if test="${codeElementInstanceTotal < 8}">
        <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="codeElement"/></g:link></span>
    </g:if>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="codeElement"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="elementNumber, name, dataType*, dataLength"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="elementNumber" title="Element Number" titleKey="codeElement.elementNumber.label"/>

                <g:sortableColumn property="name" title="Name" titleKey="codeElement.name.label"/>

                <th><g:msg code="codeElement.dataType.label" default="Data Type"/></th>

                <g:sortableColumn property="dataLength" title="Data Length" titleKey="codeElement.dataLength.label"/>

                <th><g:msg code="codeElement.values.label" default="Values"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${codeElementInstanceList}" status="i" var="codeElementInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${codeElementInstance.id}">${display(bean: codeElementInstance, field: 'elementNumber')}</g:link></td>

                    <td>${display(bean: codeElementInstance, field: 'name')}</td>

                    <td><g:msg code="codeElement.dataType.${codeElementInstance.dataType}" default="${codeElementInstance.dataType}"/></td>

                    <td>${display(bean: codeElementInstance, field: 'dataLength')}</td>

                    <td><g:drilldown controller="codeElementValue" value="${codeElementInstance.id}"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${codeElementInstanceTotal}"/>
    </div>
</div>
</body>
</html>
