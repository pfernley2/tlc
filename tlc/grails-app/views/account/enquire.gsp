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
<%@ page import="org.grails.tlc.books.Account" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="account.enquire" default="General Ledger Account Enquiry"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="account.enquire" default="General Ledger Account Enquiry"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${accountInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${accountInstance}"/>
        </div>
    </g:hasErrors>
    <g:compressor>
    <g:form action="enquire" method="post">
        <div class="dialog">
            <table>
                <tbody>
                <tr class="prop">
                    <td class="name">
                        <label for="code"><g:msg code="account.enquire.code.label" default="Code"/></label>
                    </td>
                    <td class="value nowrap ${hasErrors(bean: accountInstance, field: 'code', 'errors')}">
                        <input autofocus="autofocus" type="text" maxlength="87" size="30" id="code" name="code" value="${display(bean: accountInstance, field: 'code')}"/>&nbsp;<g:help code="account.enquire.code"/>
                    </td>

                    <td class="name">
                        <label for="displayPeriod"><g:msg code="account.enquire.period.label" default="Period"/></label>
                    </td>
                    <td class="value nowrap">
                        <g:select optionKey="id" optionValue="code" from="${periodList}" name="displayPeriod" value="${displayPeriod?.id}"/>&nbsp;<g:help code="account.enquire.period"/>
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
                <g:if test="${accountInstance.id}">
                    <tr class="prop">
                        <td class="name"><g:msg code="account.name.label" default="Name"/></td>
                        <td class="value" colspan="2">${display(bean: accountInstance, field: 'name')}</td>

                        <td class="vtop nowrap"><span class="name"><g:msg code="account.active.label" default="Active"/>:</span>&nbsp;&nbsp;<span class="value">${display(bean: accountInstance, field: 'active')}</span></td>

                        <td class="name"><g:msg code="generic.accountCurrency" default="Account Currency"/></td>
                        <td class="value">${msg(code: 'currency.name.' + accountInstance.currency.code, default: accountInstance.currency.name)}</td>

                        <td class="name"><g:msg code="generalBalance.budget.ytd" default="Budget YTD"/></td>
                        <td class="value nowrap"><g:cr context="${accountInstance}" balance="${balanceInstance}" field="budgetYTD" currency="${displayCurrency}"/></td>
                    </tr>
                </g:if>
                </tbody>
            </table>
        </div>
        <g:if test="${balanceInstance.id}">
            <div class="list">
                <table>
                    <thead>
                    <tr>

                        <th class="right"><g:msg code="generalBalance.companyOpeningBalance.label" default="Opening Balance"/></th>

                        <th class="right"><g:msg code="generic.transactions" default="Transactions"/></th>

                        <th class="right"><g:msg code="generic.adjustments" default="Adjustments"/></th>

                        <th class="right"><g:msg code="generalBalance.companyClosingBalance.label" default="Closing Balance"/></th>

                    </tr>
                    </thead>
                    <tbody>

                    <td class="right"><g:cr context="${accountInstance}" balance="${balanceInstance}" field="opening" currency="${displayCurrency}"/></td>

                    <td class="right"><g:cr context="${accountInstance}" balance="${balanceInstance}" field="transactions" currency="${displayCurrency}"/></td>

                    <td class="right"><g:cr context="${accountInstance}" balance="${balanceInstance}" field="adjustments" currency="${displayCurrency}"/></td>

                    <td class="right"><g:cr context="${accountInstance}" balance="${balanceInstance}" field="closing" currency="${displayCurrency}"/></td>
                    </tbody>
                </table>
            </div>
        </g:if>
    </g:form>
    <g:if test="${accountInstance.id}">
        <div class="list">
            <table>
                <thead>
                <tr>

                    <th><g:msg code="generic.document" default="Document"/></th>

                    <th><g:msg code="generic.documentDate" default="Document Date"/></th>

                    <th><g:msg code="generalTransaction.description.label" default="Description"/></th>

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

                        <td class="right"><g:debit context="${accountInstance}" line="${transactionInstance}" field="value" currency="${displayCurrency}"/></td>

                        <td class="right"><g:credit context="${accountInstance}" line="${transactionInstance}" field="value" currency="${displayCurrency}"/></td>

                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
        <div class="paginateButtons">
            <g:paginate total="${transactionInstanceTotal}" id="${accountInstance.id}" params="${[displayCurrency: displayCurrency?.id, displayPeriod: displayPeriod?.id]}"/>
        </div>
    </g:if>
    </g:compressor>
</div>
</body>
</html>
