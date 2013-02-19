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
<%@ page import="org.grails.tlc.books.Supplier" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="list" domain="supplier"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="import" action="imports"><g:msg code="supplier.imports" default="Import Suppliers"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="supplier"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="supplier"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="code, name, accountCreditLimit, accountCurrentBalance, settlementDays, taxId, periodicSettlement, active, nextAutoPaymentDate, revaluationMethod*"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="supplier.code.label"/>

                <g:sortableColumn property="name" title="Name" titleKey="supplier.name.label"/>

                <g:sortableColumn property="accountCurrentBalance" title="Current Balance" titleKey="supplier.accountCurrentBalance.label" class="right"/>

                <g:sortableColumn property="accountCreditLimit" title="Credit Limit" titleKey="supplier.accountCreditLimit.label" class="right"/>

                <g:sortableColumn property="settlementDays" title="Settlement Days" titleKey="supplier.settlementDays.label" class="right"/>

                <g:sortableColumn property="periodicSettlement" title="Periodic Settlement" titleKey="supplier.periodicSettlement.label"/>

                <g:sortableColumn property="nextAutoPaymentDate" title="Next Auto-Payment Date" titleKey="supplier.nextAutoPaymentDate.label"/>

                <g:sortableColumn property="active" title="Active" titleKey="supplier.active.label"/>

                <th><g:msg code="supplier.currency.label" default="Currency"/></th>

                <g:sortableColumn property="revaluationMethod" title="Revaluation Method" titleKey="supplier.revaluationMethod.label"/>

                <g:sortableColumn property="taxId" title="Tax Id" titleKey="supplier.taxId.label"/>

                <th><g:msg code="supplier.taxCode.label" default="Tax Code"/></th>

                <th><g:msg code="supplier.accessCode.label" default="Access Code"/></th>

                <th><g:msg code="supplier.country.label" default="Country"/></th>

                <th><g:msg code="supplier.addresses.label" default="Addresses"/></th>

                <th><g:msg code="supplier.transactions.label" default="Transactions"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${supplierInstanceList}" status="i" var="supplierInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${supplierInstance.id}">${display(bean:supplierInstance, field:'code')}</g:link></td>

                    <td>${display(bean:supplierInstance, field:'name')}</td>

                    <td class="right">${display(bean:supplierInstance, field:'accountCurrentBalance', scale: supplierInstance.currency.decimals)}</td>

                    <td class="right">${display(bean:supplierInstance, field:'accountCreditLimit', scale: 0)}</td>

                    <td class="right">${display(bean:supplierInstance, field:'settlementDays')}</td>

                    <td>${display(bean:supplierInstance, field:'periodicSettlement')}</td>

                    <td>${display(bean:supplierInstance, field:'nextAutoPaymentDate', scale: 1)}</td>

                    <td>${display(bean:supplierInstance, field:'active')}</td>

                    <td>${supplierInstance.currency.code.encodeAsHTML()}</td>

                    <td>${supplierInstance.revaluationMethod ? msg(code: 'supplier.revaluationMethod.' + supplierInstance.revaluationMethod, default: supplierInstance.revaluationMethod) : ''}</td>

                    <td>${display(bean:supplierInstance, field:'taxId')}</td>

                    <td>${supplierInstance.taxCode?.code?.encodeAsHTML()}</td>

                    <td>${supplierInstance.accessCode.code.encodeAsHTML()}</td>

                    <td><g:msg code="country.name.${supplierInstance.country.code}" default="${supplierInstance.country.name}"/></td>

                    <td><g:drilldown controller="supplierAddress" action="list" value="${supplierInstance.id}"/></td>

                    <td><g:drilldown controller="supplier" action="transactions" value="${supplierInstance.id}"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${supplierInstanceTotal}"/>
    </div>
</div>
</body>
</html>
