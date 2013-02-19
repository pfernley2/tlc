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
<%@ page import="org.grails.tlc.books.SupplierContact" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="list" domain="supplierContact"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="supplierContact"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="supplierContact" returns="true"/>
    <p>
    <g:each in="${supplierAddressLines}" status="j" var="supplierAddressLine">
        <g:if test="${j}"><br/></g:if>${supplierAddressLine?.encodeAsHTML()}
    </g:each>
    </p>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="name, identifier"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>
                <g:sortableColumn property="name" title="Name" titleKey="supplierContact.name.label"/>

                <g:sortableColumn property="identifier" title="Identifier" titleKey="supplierContact.identifier.label"/>

                <th><g:msg code="supplierContact.usages.label" default="Usages"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${supplierContactInstanceList}" status="i" var="supplierContactInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${supplierContactInstance.id}">${display(bean:supplierContactInstance, field:'name')}</g:link></td>

                    <td>${display(bean:supplierContactInstance, field:'identifier')}</td>

                    <td>
                        <g:each in="${supplierContactInstance.usages}" status="k" var="supplierContactUsage">
                            <g:if test="${k}"><br/></g:if><g:msg code="supplierContactType.name.${supplierContactUsage.type.code}" default="${supplierContactUsage.type.name}"/>
                        </g:each>
                    </td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${supplierContactInstanceTotal}"/>
    </div>
</div>
</body>
</html>
