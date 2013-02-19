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
<!doctype html>
<html>
<head>
    <title><g:msg code="document.allocations" default="Allocation Enquiry"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="document.allocations" default="Allocation Enquiry" help="document.allocations"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${lineInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${lineInstance}"/>
        </div>
    </g:hasErrors>
    <g:compressor>
    <g:form action="allocations" method="post">
        <input type="hidden" name="id" value="${lineInstance?.id}"/>
        <div class="dialog">
            <table>
                <tbody>
                <tr class="prop">
                    <td class="name">
                        <g:msg code="generic.document" default="Document"/>:
                    </td>
                    <td class="value">
                        <g:enquiryLink target="${lineInstance.document}" displayPeriod="${displayPeriod}" displayCurrency="${displayCurrency}"><g:format value="${lineInstance.document.type.code + lineInstance.document.code}"/></g:enquiryLink>
                    </td>

                    <td class="name">
                        <g:msg code="document.description.label" default="Description"/>:
                    </td>
                    <td class="value nowrap">
                        ${display(bean: lineInstance, field: 'description')}
                    </td>

                    <td class="name">
                        <label for="displayCurrency"><g:msg code="generic.displayCurrency.label" default="Display Currency"/></label>
                    </td>
                    <td class="value nowrap">
                        <g:domainSelect class="${displayCurrencyClass}" name="displayCurrency" options="${currencyList}" selected="${displayCurrency}" prefix="currency.name" code="code" default="name" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="generic.displayCurrency"/>
                    </td>

                    <td><span class="button"><input class="save" type="submit" value="${msg(code: 'generic.enquire', 'default': 'Enquire')}"/></span></td>
                </tr>
                <g:if test="${lineInstance.id}">
                    <tr class="prop">
                        <td class="name"><g:msg code="document.customer.label" default="Customer"/></td>
                        <td class="value"><g:enquiryLink target="${lineInstance.customer}" displayPeriod="${lineInstance.document.period}" displayCurrency="${displayCurrency}">${lineInstance.customer.code.encodeAsHTML()}</g:enquiryLink></td>

                        <td class="name"><g:msg code="customer.name.label" default="Name"/></td>
                        <td class="value nowrap">${lineInstance.customer.name.encodeAsHTML()}</td>

                        <td class="name"><g:msg code="generic.accountCurrency" default="Account Currency"/></td>
                        <td class="value">${msg(code: 'currency.name.' + lineInstance.customer.currency.code, default: lineInstance.customer.currency.name)}</td>
                    </tr>
                </g:if>
                </tbody>
            </table>
        </div>
    </g:form>
    <g:if test="${lineInstance.id}">
        <div class="list">
            <table>
                <thead>
                <tr>
                    <th><g:msg code="generic.document" default="Document"/></th>
                    <th class="right"><g:msg code="generic.debit" default="Debit"/></th>
                    <th class="right"><g:msg code="generic.credit" default="Credit"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${allocationInstanceList}" status="i" var="allocationInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        <td><g:enquiryLink target="${allocationInstance.target}" displayCurrency="${displayCurrency}"><g:format value="${allocationInstance.code}"/></g:enquiryLink></td>
                        <td class="right"><g:format value="${allocationInstance.debit}"/></td>
                        <td class="right"><g:format value="${allocationInstance.credit}"/></td>
                    </tr>
                </g:each>
                <tr>
                    <th class="right"><g:format value="${totalInstance.code}"/>:</th>
                    <th class="right"><g:format value="${totalInstance.debit}"/></th>
                    <th class="right"><g:format value="${totalInstance.credit}"/></th>
                </tr>
                </tbody>
            </table>
        </div>
    </g:if>
    </g:compressor>
</div>
</body>
</html>
