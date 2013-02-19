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
    <title><g:msg code="customer.enquire" default="Customer Account Enquiry"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="customer.enquire" default="Customer Account Enquiry"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${customerInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${customerInstance}"/>
        </div>
    </g:hasErrors>
    <g:compressor>
    <g:form action="enquire" method="post">
        <div class="dialog">
            <table>
                <tbody>
                <tr class="prop">
                    <td class="name">
                        <label for="code"><g:msg code="customer.enquire.code.label" default="Code"/></label>
                    </td>
                    <td class="value nowrap ${hasErrors(bean: customerInstance, field: 'code', 'errors')}">
                        <input autofocus="autofocus" type="text" maxlength="20" size="20" id="code" name="code" value="${display(bean: customerInstance, field: 'code')}"/>&nbsp;<g:help code="customer.enquire.code"/>
                    </td>

                    <td class="name">
                        <label for="displayPeriod"><g:msg code="customer.enquire.period.label" default="Period"/></label>
                    </td>
                    <td class="value nowrap">
                        <g:select optionKey="id" optionValue="code" from="${periodList}" name="displayPeriod" value="${displayPeriod?.id}" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="customer.enquire.period"/>
                    </td>

                    <td class="name">
                        <label for="displayCurrency"><g:msg code="generic.displayCurrency.label" default="Display Currency"/></label>
                    </td>
                    <td class="value nowrap">
                        <g:domainSelect class="${displayCurrencyClass}" name="displayCurrency" options="${currencyList}" selected="${displayCurrency}" prefix="currency.name" code="code" default="name" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="generic.displayCurrency"/>
                    </td>
                    <td><span class="button"><input class="save" type="submit" value="${msg(code: 'generic.enquire', 'default': 'Enquire')}"/></span></td>
                    <td></td>
                </tr>
                <g:if test="${customerInstance.id}">
                    <tr class="prop">
                        <td class="name"><g:msg code="customer.name.label" default="Name"/></td>
                        <td class="value" colspan="2">${display(bean: customerInstance, field: 'name')}</td>

                        <td class="vtop nowrap"><span class="name"><g:msg code="customer.active.label" default="Active"/>:</span>&nbsp;&nbsp;<span class="value">${display(bean: customerInstance, field: 'active')}</span></td>

                        <td class="name"><g:msg code="generic.accountCurrency" default="Account Currency"/></td>
                        <td class="value">${msg(code: 'currency.name.' + customerInstance.currency.code, default: customerInstance.currency.name)}</td>

                        <td class="name"><g:msg code="customer.accountCurrentBalance.label" default="Current Balance"/></td>
                        <td class="value nowrap">
                            <g:if test="${statementCount}">
                                <g:link action="statementEnquiry" params="${[customerId: customerInstance.id, displayPeriod: displayPeriod?.id, displayCurrency: displayCurrency?.id]}">
                                    <g:cr context="${customerInstance}" field="balance" currency="${displayCurrency}"/>
                                </g:link>
                            </g:if>
                            <g:else>
                                <g:cr context="${customerInstance}" field="balance" currency="${displayCurrency}"/>
                            </g:else>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name"><g:msg code="customer.country.label" default="Country"/></td>
                        <td class="value" colspan="2"><g:msg code="country.name.${customerInstance.country.code}" default="${customerInstance.country.name}"/></td>

                        <td class="vtop nowrap"><span class="name"><g:msg code="customer.periodic" default="Periodic"/>:</span>&nbsp;&nbsp;<span class="value">${display(bean: customerInstance, field: 'periodicSettlement')}</span></td>

                        <td class="name"><g:msg code="customer.settlementDays.label" default="Settlement Days"/></td>
                        <td class="value">${display(bean: customerInstance, field: 'settlementDays')}</td>

                        <td class="name"><g:msg code="customer.accountCreditLimit.label" default="Credit Limit"/></td>
                        <td class="value nowrap"><g:amount context="${customerInstance}" field="limit" currency="${displayCurrency}"/></td>
                    </tr>
                    <tr class="prop">
                        <td class="name"><g:msg code="customer.taxCode.label" default="Tax Code"/></td>
                        <td class="value">${customerInstance.taxCode ? msg(code: 'taxCode.name.' + customerInstance.taxCode.code, default: customerInstance.taxCode.name) : ''}</td>

                        <td class="name"><g:msg code="customer.taxId.label" default="Tax Id"/></td>
                        <td class="value">${display(bean: customerInstance, field: 'taxId')}</td>

                        <td class="name"><g:msg code="customer.turnovers.label" default="Sales"/></td>
                        <td class="vtop"><g:domainSelect name="dummy" options="${turnoverList}" selected="${displayTurnover}" displays="data" sort="false"/></td>

                        <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>
                        <td class="value nowrap">${display(bean: customerInstance, field: 'dateCreated', scale: 1)}</td>
                    </tr>
                </g:if>
                </tbody>
            </table>
        </div>
    </g:form>
    <g:if test="${customerInstance.id}">
        <div class="list">
            <table>
                <thead>
                <tr>

                    <th><g:msg code="generic.document" default="Document"/></th>

                    <th><g:msg code="generic.documentDate" default="Document Date"/></th>

                    <th><g:msg code="generalTransaction.description.label" default="Description"/></th>

                    <th><g:msg code="document.dueDate.label" default="Due Date"/></th>

                    <th><g:msg code="generalTransaction.onHold.label" default="On Hold"/></th>

                    <th class="right"><g:msg code="generalTransaction.accountUnallocated.label" default="Unallocated"/></th>

                    <th class="right"><g:msg code="generic.debit" default="Debit"/></th>

                    <th class="right"><g:msg code="generic.credit" default="Credit"/></th>

                </tr>
                </thead>
                <tbody>
                <g:each in="${transactionInstanceList}" status="i" var="transactionInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                        <td><g:enquiryLink target="${transactionInstance.document}" displayPeriod="${displayPeriod}" displayCurrency="${displayCurrency}"><g:format value="${transactionInstance.document.type.code + transactionInstance.document.code}"/></g:enquiryLink></td>

                        <td><g:format value="${transactionInstance.document.documentDate}" scale="1"/></td>

                        <td>${display(bean: transactionInstance, field: 'description')}</td>

                        <td><g:format value="${transactionInstance.document.dueDate}" scale="1"/></td>

                        <td><g:if test="${transactionInstance.onHold}">${display(bean: transactionInstance, field: 'onHold')}</g:if></td>

                        <td class="right"><g:enquiryLink target="${transactionInstance}" displayPeriod="${displayPeriod}" displayCurrency="${displayCurrency}"><g:drcr context="${customerInstance}" line="${transactionInstance}" field="unallocated" currency="${displayCurrency}" zeroIsHTML="${'&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;'}"/></g:enquiryLink></td>

                        <td class="right"><g:debit context="${customerInstance}" line="${transactionInstance}" field="value" currency="${displayCurrency}"/></td>

                        <td class="right"><g:credit context="${customerInstance}" line="${transactionInstance}" field="value" currency="${displayCurrency}"/></td>

                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
        <div class="paginateButtons">
            <g:paginate total="${transactionInstanceTotal}" id="${customerInstance.id}" params="${[displayCurrency: displayCurrency?.id, displayPeriod: displayPeriod?.id]}"/>
        </div>
    </g:if>
    </g:compressor>
</div>
</body>
</html>
