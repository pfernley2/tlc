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
<%@ page import="org.grails.tlc.books.SupplierAddress" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="list" domain="supplierAddress"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="supplierAddress"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list.for" domain="supplierAddress" forDomain="${ddSource}" value="${ddSource?.name}" returns="true"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>
                <th><g:msg code="supplier.address" default="Address"/></th>
                <th><g:msg code="supplierAddress.addressUsages.label" default="Usages"/></th>
                <th><g:msg code="supplierAddress.contacts.label" default="Contacts"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${supplierAddressInstanceList}" status="i" var="supplierAddressInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td>
                        <g:link action="show" id="${supplierAddressInstance.id}">
                            <g:each in="${supplierAddressLines[i]}" status="j" var="supplierAddressLine">
                                <g:if test="${j}"><br/></g:if>${supplierAddressLine?.encodeAsHTML()}
                            </g:each>
                        </g:link>
                    </td>
                    <td>
                        <g:each in="${supplierAddressInstance.addressUsages}" status="k" var="supplierAddressUsage">
                            <g:if test="${k}"><br/></g:if><g:msg code="supplierAddressType.name.${supplierAddressUsage.type.code}" default="${supplierAddressUsage.type.name}"/>
                        </g:each>
                    </td>

                    <td><g:drilldown controller="supplierContact" action="list" value="${supplierAddressInstance.id}"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${supplierAddressInstanceTotal}"/>
    </div>
</div>
</body>
</html>
