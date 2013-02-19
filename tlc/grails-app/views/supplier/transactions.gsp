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
    <title><g:msg code="supplier.transactions.for" args="${[supplierInstance.name]}" default="Transaction List for Supplier ${supplierInstance.name}"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="supplier.transactions.for" args="${[supplierInstance.name]}" default="Transaction List for Supplier ${supplierInstance.name}" help="maintenance.transactions" returns="true"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${supplierInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${supplierInstance}"/>
        </div>
    </g:hasErrors>
    <g:render template="/system/ajax"/>
    <g:compressor>
    <g:form method="post">
        <div class="dialog">
            <table>
                <tbody>
                <tr class="prop">
                    <td class="name"><g:msg code="supplier.code.label" default="Code"/></td>
                    <td class="value">${display(bean: supplierInstance, field: 'code')}</td>

                    <td class="name"><g:msg code="supplier.name.label" default="Name"/></td>
                    <td class="value nowrap">${display(bean: supplierInstance, field: 'name')}</td>

                    <td class="name">
                        <label for="displayPeriod"><g:msg code="supplier.enquire.period.label" default="Period"/></label>
                    </td>
                    <td class="value nowrap">
                        <g:select optionKey="id" optionValue="code" from="${periodList}" name="displayPeriod" value="${displayPeriod?.id}" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="supplier.enquire.period"/>
                    </td>

                    <td><span class="button"><g:actionSubmit class="save" action="transactions" value="${msg(code:'generic.enquire', 'default':'Enquire')}"/></span></td>
                </tr>

                <tr>
                    <td class="name nowrap"><g:msg code="supplier.accountCurrentBalance.label" default="Current Balance"/></td>
                    <td class="value"><g:dr context="${supplierInstance}" field="balance" negate="true"/></td>

                    <td class="name"><g:msg code="supplier.currency.label" default="Currency"/></td>
                    <td class="value nowrap">${msg(code: 'currency.name.' + supplierInstance.currency.code, default: supplierInstance.currency.name)}</td>

                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="buttons">
            <span class="button"><g:actionSubmit class="add" action="auto" value="${msg(code:'document.auto.allocate', 'default':'Auto Allocation')}"/></span>
        </div>
    </g:form>
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

                <th><g:msg code="purchase.manual" default="Manual Allocation"/></th>

            </tr>
            </thead>
            <form method="post">
                <tbody>
                <g:each in="${transactionInstanceList}" status="i" var="transactionInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                        <td><g:format value="${transactionInstance.document.type.code + transactionInstance.document.code}"/></td>

                        <td><g:format value="${transactionInstance.document.documentDate}" scale="1"/></td>

                        <td>${display(bean: transactionInstance, field: 'description')}</td>

                        <td><g:format value="${transactionInstance.document.dueDate}" scale="1"/></td>

                        <td class="value center">
                            <g:checkBox name="onHold[${transactionInstance.id}]" value="${transactionInstance.onHold}" onclick="setHold(this, '${createLink(controller: 'document', action: 'hold')}')"></g:checkBox>
                        </td>

                        <td class="right"><g:drcr context="${supplierInstance}" line="${transactionInstance}" field="unallocated" zeroIsNull="true"/></td>

                        <td class="right"><g:debit context="${supplierInstance}" line="${transactionInstance}" field="value"/></td>

                        <td class="right"><g:credit context="${supplierInstance}" line="${transactionInstance}" field="value"/></td>

                        <td>
                            <g:if test="${transactionInstance.accountValue}">
                                <g:drilldown controller="supplier" action="allocate" domain="GeneralTransaction" value="${transactionInstance.id}" params="${[transactionPeriod: displayPeriod?.id]}"/>
                            </g:if>
                        </td>

                    </tr>
                </g:each>
                </tbody>
            </form>
        </table>
    </div>
    </g:compressor>
    <div class="paginateButtons">
        <g:paginate total="${transactionInstanceTotal}" params="${[displayPeriod: displayPeriod?.id]}"/>
    </div>
</div>
</body>
</html>
