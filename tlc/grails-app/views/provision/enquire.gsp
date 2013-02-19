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
    <title><g:msg code="provision.enquire" default="Accrual/Prepayment Enquiry"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="provision.enquire" default="Accrual/Prepayment Enquiry" help="document.enquire"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${documentInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${documentInstance}"/>
        </div>
    </g:hasErrors>
    <g:compressor>
    <g:form action="enquire" method="post">
        <div class="dialog">
            <table>
                <tbody>
                <tr class="prop">
                    <td class="name">
                        <label for="type.id"><g:msg code="document.enquire.type.label" default="Document Type"/></label>
                    </td>
                    <td class="value nowrap ${hasErrors(bean: documentInstance, field: 'type', 'errors')}">
                        <g:domainSelect autofocus="autofocus" name="type.id" options="${documentTypeList}" selected="${documentInstance?.type}" displays="${['code', 'name']}"/>&nbsp;<g:help code="document.enquire.type"/>
                    </td>

                    <td class="name">
                        <label for="code"><g:msg code="document.enquire.code.label" default="Code"/></label>
                    </td>
                    <td class="value nowrap ${hasErrors(bean: documentInstance, field: 'code', 'errors')}">
                        <input type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: documentInstance, field: 'code')}"/>&nbsp;<g:help code="document.enquire.code"/>
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
                <g:if test="${documentInstance.id}">
                    <tr class="prop">
                        <td class="name"><g:msg code="document.description.label" default="Description"/></td>
                        <td class="value" colspan="2">${display(bean: documentInstance, field: 'description')}</td>

                        <td class="name"><g:msg code="generalTransaction.adjustment.label" default="Adjustment"/>:&nbsp;&nbsp;${display(bean: documentInstance, field: 'sourceAdjustment')}</td>

                        <td class="name"><g:msg code="document.currency.text" default="Document Currency"/></td>
                        <td class="value">${msg(code: 'currency.name.' + documentInstance.currency.code, default: documentInstance.currency.name)}</td>

                        <td class="name"><g:msg code="account.enquire.period.label" default="Period"/></td>
                        <td class="value nowrap">${documentInstance.period.code.encodeAsHTML()}</td>
                    </tr>
                    <tr class="prop">
                        <td class="name"><g:msg code="generic.documentDate" default="Document Date"/></td>
                        <td class="value">${display(bean: documentInstance, field: 'documentDate', scale: 1)}</td>

                        <td></td>
                        <td></td>

                        <td class="name"><g:msg code="document.customer.reference.label" default="Reference"/></td>
                        <td class="value">${display(bean: documentInstance, field: 'reference')}</td>

                        <td class="name"><g:msg code="document.posted" default="Posted"/></td>
                        <td class="value nowrap">${display(bean: documentInstance, field: 'dateCreated', scale: 1)}</td>
                    </tr>
                </g:if>
                </tbody>
            </table>
        </div>
    </g:form>
    <g:if test="${documentInstance.id}">
        <div class="list">
            <table>
                <thead>
                <tr>
                    <th><g:msg code="document.line.accountCode.label" default="Account"/></th>
                    <th><g:msg code="account.name.label" default="Name"/></th>
                    <th><g:msg code="document.line.description.label" default="Description"/></th>
                    <th class="right"><g:msg code="document.value.label" default="Value"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${documentInstance.lines}" status="i" var="lineInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        <td><g:enquiryLink target="${lineInstance.balance.account}" displayPeriod="${documentInstance.period}" displayCurrency="${displayCurrency}">${lineInstance.balance.account.code.encodeAsHTML()}</g:enquiryLink></td>
                        <td>${lineInstance.balance.account.name.encodeAsHTML()}</td>
                        <td>${display(bean: lineInstance, field: 'description')}</td>
                        <td class="right"><g:amount context="${documentInstance}" line="${lineInstance}" field="value" negate="${!analysisIsDebit}" currency="${displayCurrency}"/></td>
                    </tr>
                </g:each>
                <tr>
                    <th></th>
                    <th></th>
                    <th class="right"><g:msg code="document.sourceTotal.label" default="Total"/>:</th>
                    <th class="right"><g:amount context="${documentInstance}" line="${totalInstance}" field="value" negate="${analysisIsDebit}" currency="${displayCurrency}"/></th>
                </tr>
                </tbody>
            </table>
        </div>
    </g:if>
    </g:compressor>
</div>
</body>
</html>
