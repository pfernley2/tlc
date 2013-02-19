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
<%@ page import="org.grails.tlc.books.AccessGroup" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="company"/>
    <title><g:msg code="list" domain="accessGroup"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="accessGroup"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="accessGroup"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="code, name, element1, element2, element3, element4, element5, element6, element7, element8, customers, suppliers"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="accessGroup.code.label"/>

                <g:sortableColumn property="name" title="Name" titleKey="accessGroup.name.label"/>

                <g:if test="${elementList[0]}"><g:sortableColumn property="element1" title="${elementList[0].encodeAsHTML()}"/></g:if>

                <g:if test="${elementList[1]}"><g:sortableColumn property="element2" title="${elementList[1].encodeAsHTML()}"/></g:if>

                <g:if test="${elementList[2]}"><g:sortableColumn property="element3" title="${elementList[2].encodeAsHTML()}"/></g:if>

                <g:if test="${elementList[3]}"><g:sortableColumn property="element4" title="${elementList[3].encodeAsHTML()}"/></g:if>

                <g:if test="${elementList[4]}"><g:sortableColumn property="element5" title="${elementList[4].encodeAsHTML()}"/></g:if>

                <g:if test="${elementList[5]}"><g:sortableColumn property="element6" title="${elementList[5].encodeAsHTML()}"/></g:if>

                <g:if test="${elementList[6]}"><g:sortableColumn property="element7" title="${elementList[6].encodeAsHTML()}"/></g:if>

                <g:if test="${elementList[7]}"><g:sortableColumn property="element8" title="${elementList[7].encodeAsHTML()}"/></g:if>

                <g:sortableColumn property="customers" title="Customers" titleKey="accessGroup.customers.label"/>

                <g:sortableColumn property="suppliers" title="Suppliers" titleKey="accessGroup.suppliers.label"/>

                <th><g:msg code="accessGroup.member" default="Members"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${accessGroupInstanceList}" status="i" var="accessGroupInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${accessGroupInstance.id}">${display(bean:accessGroupInstance, field:'code')}</g:link></td>

                    <td>${display(bean:accessGroupInstance, field:'name')}</td>

                    <g:if test="${elementList[0]}"><td>${display(bean:accessGroupInstance, field:'element1')}</td></g:if>

                    <g:if test="${elementList[1]}"><td>${display(bean:accessGroupInstance, field:'element2')}</td></g:if>

                    <g:if test="${elementList[2]}"><td>${display(bean:accessGroupInstance, field:'element3')}</td></g:if>

                    <g:if test="${elementList[3]}"><td>${display(bean:accessGroupInstance, field:'element4')}</td></g:if>

                    <g:if test="${elementList[4]}"><td>${display(bean:accessGroupInstance, field:'element5')}</td></g:if>

                    <g:if test="${elementList[5]}"><td>${display(bean:accessGroupInstance, field:'element6')}</td></g:if>

                    <g:if test="${elementList[6]}"><td>${display(bean:accessGroupInstance, field:'element7')}</td></g:if>

                    <g:if test="${elementList[7]}"><td>${display(bean:accessGroupInstance, field:'element8')}</td></g:if>

                    <td>${display(bean:accessGroupInstance, field:'customers')}</td>

                    <td>${display(bean:accessGroupInstance, field:'suppliers')}</td>

                    <td><g:drilldown controller="accessGroup" action="members" value="${accessGroupInstance.id}"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${accessGroupInstanceTotal}"/>
    </div>
</div>
</body>
</html>
