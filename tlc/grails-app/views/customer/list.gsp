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
<%@ page import="org.grails.tlc.books.Customer" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="list" domain="customer"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="import" action="imports"><g:msg code="customer.imports" default="Import Customers"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="customer"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="customer"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="code, name, accountCreditLimit, accountCurrentBalance, settlementDays, taxId, periodicSettlement, active, revaluationMethod*"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="customer.code.label"/>

                <g:sortableColumn property="name" title="Name" titleKey="customer.name.label"/>

                <g:sortableColumn property="accountCurrentBalance" title="Current Balance" titleKey="customer.accountCurrentBalance.label" class="right"/>

                <g:sortableColumn property="accountCreditLimit" title="Credit Limit" titleKey="customer.accountCreditLimit.label" class="right"/>

                <g:sortableColumn property="settlementDays" title="Settlement Days" titleKey="customer.settlementDays.label" class="right"/>

                <g:sortableColumn property="periodicSettlement" title="Periodic Settlement" titleKey="customer.periodicSettlement.label"/>

                <g:sortableColumn property="active" title="Active" titleKey="customer.active.label"/>

                <th><g:msg code="customer.currency.label" default="Currency"/></th>

                <g:sortableColumn property="revaluationMethod" title="Revaluation Method" titleKey="customer.revaluationMethod.label"/>

                <g:sortableColumn property="taxId" title="Tax Id" titleKey="customer.taxId.label"/>

                <th><g:msg code="customer.taxCode.label" default="Tax Code"/></th>

                <th><g:msg code="customer.accessCode.label" default="Access Code"/></th>

                <th><g:msg code="customer.country.label" default="Country"/></th>

                <th><g:msg code="customer.addresses.label" default="Addresses"/></th>

                <th><g:msg code="customer.transactions.label" default="Transactions"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${customerInstanceList}" status="i" var="customerInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${customerInstance.id}">${display(bean:customerInstance, field:'code')}</g:link></td>

                    <td>${display(bean:customerInstance, field:'name')}</td>

                    <td class="right">${display(bean:customerInstance, field:'accountCurrentBalance', scale: customerInstance.currency.decimals)}</td>

                    <td class="right">${display(bean:customerInstance, field:'accountCreditLimit', scale: 0)}</td>

                    <td class="right">${display(bean:customerInstance, field:'settlementDays')}</td>

                    <td>${display(bean:customerInstance, field:'periodicSettlement')}</td>

                    <td>${display(bean:customerInstance, field:'active')}</td>

                    <td>${customerInstance.currency.code.encodeAsHTML()}</td>

                    <td>${customerInstance.revaluationMethod ? msg(code: 'customer.revaluationMethod.' + customerInstance.revaluationMethod, default: customerInstance.revaluationMethod) : ''}</td>

                    <td>${display(bean:customerInstance, field:'taxId')}</td>

                    <td>${customerInstance.taxCode?.code?.encodeAsHTML()}</td>

                    <td>${customerInstance.accessCode.code.encodeAsHTML()}</td>

                    <td><g:msg code="country.name.${customerInstance.country.code}" default="${customerInstance.country.name}"/></td>

                    <td><g:drilldown controller="customerAddress" action="list" value="${customerInstance.id}"/></td>

                    <td><g:drilldown controller="customer" action="transactions" value="${customerInstance.id}"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${customerInstanceTotal}"/>
    </div>
</div>
</body>
</html>
