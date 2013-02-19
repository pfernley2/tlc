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
    <title><g:msg code="sales.manual" default="Manual Allocation"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="invoice"><g:msg code="sales.invoice.entry" default="Sales Invoice/Cr. Note Entry"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="sales.manual" default="Manual Allocation" help="manual.allocation"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${allocationInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${allocationInstance}"/>
        </div>
    </g:hasErrors>
    <g:compressor>
    <g:form method="post">
        <input type="hidden" name="id" value="${lineInstance?.id}"/>
        <div class="dialog">
            <table>
                <tbody>
                <tr class="prop">
                    <td class="name">
                        <g:msg code="generic.document" default="Document"/>:
                    </td>
                    <td class="value">
                        <g:format value="${lineInstance.document.type.code + lineInstance.document.code}"/>
                    </td>

                    <td class="name">
                        <g:msg code="document.lineValue" default="Line Value"/>:
                    </td>
                    <td class="value">
                        <g:drcr context="${customerInstance}" line="${lineInstance}" field="value" zeroIsUnmarked="true"/>
                    </td>

                    <td class="name">
                        <g:msg code="generalTransaction.accountUnallocated.label" default="Unallocated"/>:
                    </td>
                    <td class="value">
                        <g:drcr context="${customerInstance}" line="${lineInstance}" field="unallocated" zeroIsUnmarked="true"/>
                    </td>

                    <td class="name">
                        <label for="displayPeriod"><g:msg code="customer.enquire.period.label" default="Period"/></label>
                    </td>
                    <td class="value nowrap">
                        <g:select optionKey="id" optionValue="code" from="${periodList}" name="displayPeriod" value="${displayPeriod?.id}" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="customer.enquire.period"/>
                    </td>

                    <td><span class="button"><g:actionSubmit class="save" action="allocate" value="${msg(code:'generic.enquire', 'default':'Enquire')}"/></span></td>
                </tr>
                <tr class="prop">
                    <td class="name"><g:msg code="document.customer.label" default="Customer"/></td>
                    <td class="value">${display(bean: customerInstance, field: 'code')}</td>

                    <td class="name"><g:msg code="customer.name.label" default="Name"/></td>
                    <td class="value nowrap" colspan="3">${display(bean: customerInstance, field: 'name')}</td>

                    <td class="name"><g:msg code="document.currency.label" default="Currency"/></td>
                    <td class="value" colspan="2">${msg(code: 'currency.name.' + customerInstance.currency.code, default: customerInstance.currency.name)}</td>
                </tr>
                </tbody>
            </table>
            <h2><g:msg code="document.allocate.to" default="Allocate To"/></h2>
            <table>
                <tbody>
                <tr class="prop">
                    <td class="name">
                        <label for="targetType.id"><g:msg code="document.allocation.type.label" default="Document Type"/></label>
                    </td>
                    <td class="value nowrap ${hasErrors(bean: allocationInstance, field: 'targetType', 'errors')}">
                        <g:domainSelect autofocus="autofocus" name="targetType.id" options="${documentTypeList}" selected="${allocationInstance.targetType}" displays="${['code', 'name']}"/>&nbsp;<g:help code="document.allocation.type"/>
                    </td>

                    <td class="name">
                        <label for="targetCode"><g:msg code="document.allocation.code.label" default="Code"/></label>
                    </td>
                    <td class="value nowrap ${hasErrors(bean: allocationInstance, field: 'code', 'errors')}">
                        <input type="text" maxlength="10" size="10" id="targetCode" name="targetCode" value="${display(bean: allocationInstance, field: 'targetCode')}"/>&nbsp;<g:help code="document.allocation.code"/>
                    </td>

                    <td class="name">
                        <label for="accountValue"><g:msg code="document.allocation.amount.label" default="Amount"/></label>
                    </td>
                    <td class="value nowrap">
                        <input type="text" size="12" id="accountValue" name="accountValue" value="${display(bean: allocationInstance, field: 'accountValue', scale: customerInstance.currency.decimals)}"/>&nbsp;<g:help code="document.allocation.amount"/>
                    </td>

                    <g:if test="${allowDifference}">
                        <td class="name">
                            <label for="accountDifference"><g:msg code="document.allocation.difference.label" default="FX Difference"/></label>
                        </td>
                        <td class="value nowrap">
                            <input type="text" size="12" id="accountDifference" name="accountDifference" value="${display(bean: allocationInstance, field: 'accountDifference', scale: customerInstance.currency.decimals)}"/>&nbsp;<g:help code="document.allocation.difference"/>
                        </td>
                    </g:if>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="buttons">
            <span class="button"><g:actionSubmit class="add" action="allocating" value="${msg(code:'document.allocate', 'default':'Allocate')}"/></span>
        </div>
    </g:form>
    <h2><g:msg code="document.allocation.enquiry" default="Enquiry Results"/></h2>
    <div class="list">
        <table>
            <thead>
            <tr>

                <th><g:msg code="generic.document" default="Document"/></th>

                <th><g:msg code="generic.documentDate" default="Document Date"/></th>

                <th><g:msg code="generalTransaction.description.label" default="Description"/></th>

                <th><g:msg code="generalTransaction.reconciled.label" default="Statement"/></th>

                <th><g:msg code="generalTransaction.onHold.label" default="On Hold"/></th>

                <th class="right"><g:msg code="generalTransaction.accountUnallocated.label" default="Unallocated"/></th>

                <th class="right"><g:msg code="generic.debit" default="Debit"/></th>

                <th class="right"><g:msg code="generic.credit" default="Credit"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${transactionInstanceList}" status="i" var="transactionInstance">
                <tr class="pointer ${(i % 2) == 0 ? 'odd' : 'even'}" onclick="allocationSelect('${transactionInstance.document.type.id}', '${transactionInstance.document.code}')">

                    <td><g:format value="${transactionInstance.document.type.code + transactionInstance.document.code}"/></td>

                    <td><g:format value="${transactionInstance.document.documentDate}" scale="1"/></td>

                    <td>${display(bean: transactionInstance, field: 'description')}</td>

                    <td>${display(bean: transactionInstance, field: 'reconciled', scale: 1)}</td>

                    <td><g:if test="${transactionInstance.onHold}">${display(bean: transactionInstance, field: 'onHold')}</g:if></td>

                    <td class="right"><g:drcr context="${customerInstance}" line="${transactionInstance}" field="unallocated" zeroIsNull="true"/></td>

                    <td class="right"><g:debit context="${customerInstance}" line="${transactionInstance}" field="value"/></td>

                    <td class="right"><g:credit context="${customerInstance}" line="${transactionInstance}" field="value"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    </g:compressor>
    <div class="paginateButtons">
        <g:paginate total="${transactionInstanceTotal}" action="allocate" id="${lineInstance.id}" params="${[displayPeriod: displayPeriod?.id, 'targetType.id': allocationInstance.targetType?.id, targetCode: allocationInstance.targetCode, accountValue: allocationInstance.accountValue]}"/>
    </div>
</div>
</body>
</html>
