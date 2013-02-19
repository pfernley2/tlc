<%--
 ~   Copyright 2010-2013 Paul Fernley
 ~
 ~   This file is part of the Three Ledger Core (TLC) software
 ~   from Paul Fernley.
 ~
 ~   TLC is free software: you can redistribute it and/or modify
 ~   it under the terms of the GNU General Public License as published by
 ~   the Free Software Foundation, either version 3 of the License, or
 ~   (at your option) any later version.
 ~
 ~   TLC is distributed in the hope that it will be useful,
 ~   but WITHOUT ANY WARRANTY; without even the implied warranty of
 ~   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ~   GNU General Public License for more details.
 ~
 ~   You should have received a copy of the GNU General Public License
 ~   along with TLC. If not, see <http://www.gnu.org/licenses/>.
 --%>
<!doctype html>
<html>
<head>
    <title><g:msg code="edit" domain="reconciliation"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list" params="${[bankAccount: bankAccount.id]}"><g:msg code="list" domain="reconciliation"/></g:link></span>
    <span class="menuButton"><g:link class="print" action="print" params="${[id: reconciliationInstance?.id, caller: 'edit']}"><g:msg code="generic.print" default="Print"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="add" params="${[reconciliation: reconciliationInstance.id]}"><g:msg code="add" domain="document"/></g:link></span>
    <g:if test="${hasAdded}">
        <span class="menuButton"><g:link class="delete" action="remove" params="${[reconciliation: reconciliationInstance.id]}"><g:msg code="reconciliation.remove.document" default="Remove Document"/></g:link></span>
    </g:if>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="reconciliation.edit.for" args="${[g.format(value: reconciliationInstance.statementDate, scale: 1), bankAccount.name, bankAccount.currency.code]}" default="Edit Reconciliation on ${g.format(value: reconciliationInstance.statementDate, scale: 1)} for ${bankAccount.name} in ${bankAccount.currency.code}"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${reconciliationInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${reconciliationInstance}"/>
        </div>
    </g:hasErrors>
    <g:render template="/system/ajax"/>
    <g:form action="finalization" method="post" id="${reconciliationInstance.id}">
        <div class="dialog">
            <table>
                <tbody>
                <tr class="prop">
                    <td class="name"><g:msg code="reconciliation.statementBalance.label" default="Statement Balance"/></td>
                    <td class="value columnar">${display(bean: reconciliationInstance, field: 'statementBalance', scale: decimals)}</td>
                    <td></td>
                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="reconciliation.add.unreconciled" default="add Unreconciled Items"/></td>
                    <td class="value columnar underlined" id="unrec">${display(value: unreconciled)}</td>
                    <td></td>
                </tr>

                <tr class="prop">
                    <td></td>
                    <td class="value columnar" id="subtot">${display(value: subtotal)}</td>
                    <td></td>
                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="reconciliation.less.bankAccountBalance" default="less Bank Account Balance"/></td>
                    <td class="value columnar underlined">${display(bean: reconciliationInstance, field: 'bankAccountBalance', scale: decimals)}</td>
                    <td></td>
                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="reconciliation.difference" default="Difference"/></td>
                    <td class="value columnar underlined" id="diff">${display(value: difference)}</td>
                    <td style="visibility:${canFinalize ? 'visible' : 'hidden'};"><input id="fin" name="fin" type="submit" value="${msg(code:'reconciliation.finalize', 'default':'Finalize')}"/></td>
                </tr>
                </tbody>
            </table>
        </div>
    </g:form>
    <g:form method="post">
        <div class="list">
            <table>
                <thead>
                <tr>
                    <th><g:msg code="generic.bf" default="b/f"/></th>
                    <th><g:msg code="reconciliationLine.date" default="Date"/></th>
                    <th><g:msg code="generic.document" default="Document"/></th>
                    <th><g:msg code="reconciliationLine.reference" default="Reference"/></th>
                    <th><g:msg code="reconciliationLine.details.label" default="Details"/></th>
                    <th><g:msg code="reconciliationLine.reconciled" default="Reconciled"/></th>
                    <th class="right"><g:msg code="reconciliationLine.payment" default="Payment"/></th>
                    <th class="right"><g:msg code="reconciliationLine.receipt" default="Receipt"/></th>
                    <th class="center"><g:msg code="reconciliationLine.breakdown" default="Breakdown"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${reconciliationLineInstanceList}" status="i" var="reconciliationLineInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        <td>
                            <g:if test="${reconciliationLineInstance.broughtForward}">
                                <g:if test="${reconciliationLineInstance.part}">
                                    <g:msg code="reconciliationLine.part.label" default="Part"/>
                                </g:if>
                                <g:else>
                                    <g:msg code="reconciliationLine.full" default="Full"/>
                                </g:else>
                            </g:if>
                        </td>
                        <td>${display(bean: reconciliationLineInstance, field: 'documentDate', scale: 1)}</td>
                        <td>${display(bean: reconciliationLineInstance, field: 'documentCode')}</td>
                        <td>${display(bean: reconciliationLineInstance, field: 'documentReference')}</td>
                        <td>
                            <g:if test="${reconciliationLineInstance.detailCount == 1}">
                                ${display(bean: reconciliationLineInstance, field: 'detailDescription')}
                            </g:if>
                            <g:else>
                                ${display(bean: reconciliationLineInstance, field: 'documentDescription')}
                            </g:else>
                        </td>
                        <td class="checkboxStyle">
                            <g:checkBox name="line[${reconciliationLineInstance.id}]" value="${reconciliationLineInstance.bankAccountValue == reconciliationLineInstance.reconciledValue}" onclick="setReconciled(this, '${createLink(controller: 'reconciliation', action: 'reconcileLine')}')"/>
                            <g:if test="${(reconciliationLineInstance.reconciledValue && reconciliationLineInstance.bankAccountValue != reconciliationLineInstance.reconciledValue)}">
                                <g:msg code="reconciliationLine.part.label" default="Part"/>
                            </g:if>
                        </td>
                        <td class="right"><g:credit value="${reconciliationLineInstance.bankAccountValue}" scale="${decimals}"/></td>
                        <td class="right"><g:debit value="${reconciliationLineInstance.bankAccountValue}" scale="${decimals}"/></td>
                        <td class="center">
                            <g:if test="${reconciliationLineInstance.detailCount > 1}">
                                <g:drilldown domain="ReconciliationLine" controller="reconciliationLineDetail" value="${reconciliationLineInstance.id}"/>
                            </g:if>
                        </td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:form>
    <div class="paginateButtons">
        <g:paginate action="edit" total="${reconciliationLineInstanceTotal}" id="${reconciliationInstance.id}"/>
    </div>
</div>
</body>
</html>
