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
    <title><g:msg code="bank.entry" default="Bank Payment/Receipt Entry"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" controller="document" action="templates" params="${[ctrl: 'bank', types: 'BP,BR']}"><g:msg code="templateDocument.load" default="Load Template"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="bank.entry" default="Bank Payment/Receipt Entry"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${documentInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${documentInstance}"/>
        </div>
    </g:hasErrors>
    <g:render template="/system/ajax"/>
    <g:form method="post">
        <input type="hidden" id="sourceNumber" name="sourceNumber" value="${display(bean: documentInstance, field: 'code')}"/>
        <g:compressor>
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="sourceCode"><g:msg code="bank.account.label" default="Bank Account"/></label>
                    </td>
                    <td class="vtop small-value ${hasErrors(bean: documentInstance, field: 'sourceCode', 'errors')}">
                        <g:select autofocus="autofocus" onchange="accountChanged()" onblur="getBank(this, '${createLink(controller: 'document', action: 'bank')}', 'bank')" optionKey="code" optionValue="name" from="${bankAccountList}" name="sourceCode" value="${documentInstance?.sourceCode}" noSelection="['': msg(code: 'generic.select', default: '-- select --')]"/>&nbsp;<g:help code="bank.account"/>
                    </td>

                    <td class="name">
                        <label for="currency"><g:msg code="document.currency.label" default="Currency"/></label>
                    </td>
                    <td class="vtop small-value ${hasErrors(bean: documentInstance, field: 'currency', 'errors')}">
                        <g:domainSelect name="currency.id" options="${currencyList}" selected="${documentInstance?.currency}" prefix="currency.name" code="code" default="name"/>&nbsp;<g:help code="document.currency"/>
                    </td>

                <tr class="prop">
                    <td class="name">
                        <label for="type.id"><g:msg code="document.type.label" default="Document Type"/></label>
                    </td>
                    <td class="vtop small-value ${hasErrors(bean: documentInstance, field: 'type', 'errors')}">
                        <g:domainSelect onchange="typeChanged()" onblur="getCode(this, '${createLink(controller: 'document', action: 'code')}', 'reference')" name="type.id" options="${documentTypeList}" selected="${documentInstance?.type}" displays="${['code', 'name']}" noSelection="['null': msg(code: 'generic.select', default: '-- select --')]"/>&nbsp;<g:help code="document.type"/>
                    </td>

                    <td class="name">
                        <label for="code"><g:msg code="document.bank.code.label" default="Code"/></label>
                    </td>
                    <td class="vtop small-value ${hasErrors(bean: documentInstance, field: 'code', 'errors')}">
                        <input ${settings.codeEdit ? '' : 'disabled="disabled" '}type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: documentInstance, field: 'code')}"/>&nbsp;<g:help code="document.bank.code"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="reference"><g:msg code="document.journal.reference.label" default="Reference"/></label>
                    </td>
                    <td class="vtop small-value ${hasErrors(bean: documentInstance, field: 'reference', 'errors')}">
                        <input type="text" maxLength="30" size="30" id="reference" name="reference" value="${display(bean: documentInstance, field: 'reference')}"/>&nbsp;<g:help code="document.journal.reference"/>
                    </td>

                    <td class="name">
                        <label for="description"><g:msg code="document.description.label" default="Description"/></label>
                    </td>
                    <td class="vtop small-value ${hasErrors(bean: documentInstance, field: 'description', 'errors')}">
                        <input type="text" maxlength="50" size="45" id="description" name="description" value="${display(bean: documentInstance, field: 'description')}"/>&nbsp;<g:help code="document.description"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="documentDate"><g:msg code="document.bankDate.label" default="Document Date"/></label>
                    </td>
                    <td class="vtop small-value ${hasErrors(bean: documentInstance, field: 'documentDate', 'errors')}">
                        <input type="text" onchange="getPeriod(this, '${createLink(controller: 'document', action: 'period')}', '', true)" size="20" id="documentDate" name="documentDate" value="${display(bean: documentInstance, field: 'documentDate', scale: 1)}"/>&nbsp;<g:help code="document.bankDate"/>
                    </td>

                    <td class="name">
                        <label for="period.id"><g:msg code="document.period.label" default="Accounting Period"/></label>
                    </td>
                    <td class="vtop small-value ${hasErrors(bean: documentInstance, field: 'period', 'errors')}">
                        <g:select optionKey="id" optionValue="code" from="${periodList}" name="period.id" value="${documentInstance?.period?.id}"/>&nbsp;<g:help code="document.period"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td></td>
                    <td></td>

                    <td class="name">
                        <label for="sourceTotal"><g:msg code="bank.total.label" default="Total"/></label>
                    </td>
                    <td class="vtop small-value ${hasErrors(bean: documentInstance, field: 'sourceTotal', 'errors')}">
                        <input type="text" size="15" id="sourceTotal" name="sourceTotal" value="${display(bean: documentInstance, field: 'sourceTotal', scale: settings.decimals)}"/>&nbsp;<g:help code="bank.total"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="list entry">
            <table>
                <thead>
                <tr>
                    <th><g:msg code="document.line.accountType.label" default="Ledger"/>&nbsp;<g:help code="document.line.accountType"/></th>
                    <th><g:msg code="document.line.ledgerCode.label" default="Account"/>&nbsp;<g:help code="document.line.ledgerCode"/></th>
                    <th><g:msg code="document.line.description.label" default="Description"/>&nbsp;<g:help code="document.line.description"/></th>
                    <th><g:msg code="document.amount.label" default="Value"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${documentInstance.lines}" status="i" var="documentLine">
                    <tr>
                        <td class="narrow">
                            <g:select id="lines[${i}].accountType" name="lines[${i}].accountType" from="${['gl','ar','ap']}" value="${documentLine.accountType}" valueMessagePrefix="document.line.accountType" onchange="setAccount(this, '${createLink(controller: 'document', action: 'accounts')}', 'cash')"/>
                        </td>
                        <td class="narrow ${hasErrors(bean: documentLine, field: 'accountCode', 'errors')}">
                            <input type="text" maxLength="87" size="30" onchange="setAccount(this, '${createLink(controller: 'document', action: 'accounts')}', 'cash')" id="lines[${i}].accountCode" name="lines[${i}].accountCode" value="${display(bean: documentLine, field: 'accountCode')}"/>
                        </td>
                        <td class="narrow"><input type="text" maxLength="50" size="40" id="lines[${i}].description" name="lines[${i}].description" value="${display(bean: documentLine, field: 'description')}"/></td>
                        <td class="narrow"><input type="text" size="12" id="lines[${i}].documentValue" name="lines[${i}].documentValue" value="${display(bean: documentLine, field: 'documentValue', scale: settings.decimals)}"/></td>
                    </tr>
                    <tr>
                        <td class="narrow smallBottomPadding" colspan="3"><input disabled="disabled" type="text" size="76" id="lines[${i}].displayName" name="lines[${i}].displayName" value="${display(bean: documentLine, field: 'accountName')}"/></td>
                        <td class="narrow"><input type="hidden" id="lines[${i}].accountName" name="lines[${i}].accountName" value="${display(bean: documentLine, field: 'accountName')}"/></td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
        </g:compressor>
        <div class="buttons">
            <span class="button"><g:actionSubmit class="add" action="auto" value="${msg(code:'document.auto.allocate', 'default':'Auto Allocation')}"/></span>
            <span class="button"><g:actionSubmit class="delete" action="transacting" value="${msg(code:'document.no.allocate', 'default':'No Allocation')}"/></span>
            <span class="button"><g:actionSubmit class="edit" action="lines" value="${msg(code:'document.more.lines', 'default':'More Lines')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
