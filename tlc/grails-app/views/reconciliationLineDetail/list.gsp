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
    <title><g:msg code="reconciliationLineDetail.list" args="${[reconciliationLineInstance.documentCode]}" default="Breakdown for Document ${reconciliationLineInstance.documentCode}"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="reconciliationLineDetail.list" args="${[reconciliationLineInstance.documentCode]}" default="Breakdown for Document ${reconciliationLineInstance.documentCode}" returns="true" params="${[id: reconciliationInstance.id]}"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${reconciliationLineInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${reconciliationLineInstance}"/>
        </div>
    </g:hasErrors>
    <g:render template="/system/ajax"/>
    <g:form controller="reconciliation" action="finalization" method="post">
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
                    <th><g:msg code="document.line.accountType.label" default="Ledger"/></th>
                    <th><g:msg code="document.line.accountCode.label" default="Account"/></th>
                    <th><g:msg code="document.sourceName.label" default="Name"/></th>
                    <th><g:msg code="document.description.label" default="Description"/></th>
                    <th><g:msg code="reconciliationLine.reconciled" default="Reconciled"/></th>
                    <th class="right"><g:msg code="reconciliationLine.payment" default="Payment"/></th>
                    <th class="right"><g:msg code="reconciliationLine.receipt" default="Receipt"/></th>
                </tr>
                </thead>
                <tbody>
                <g:each in="${reconciliationLineDetailInstanceList}" status="i" var="reconciliationLineDetailInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        <td><g:msg code="document.line.accountType.${reconciliationLineDetailInstance.type}" default="${reconciliationLineDetailInstance.type}"/></td>
                        <td>${display(bean: reconciliationLineDetailInstance, field: 'ledgerCode', scale: 1)}</td>
                        <td>${display(bean: reconciliationLineDetailInstance, field: 'ledgerName')}</td>
                        <td>${display(bean: reconciliationLineDetailInstance, field: 'description')}</td>
                        <td class="checkboxStyle">
                            <g:checkBox name="line[${reconciliationLineDetailInstance.id}]" value="${reconciliationLineDetailInstance.reconciled}" onclick="setReconciled(this, '${createLink(controller: 'reconciliationLineDetail', action: 'reconcileDetail')}')"/>
                        </td>
                        <td class="right"><g:credit value="${reconciliationLineDetailInstance.bankAccountValue}" scale="${decimals}"/></td>
                        <td class="right"><g:debit value="${reconciliationLineDetailInstance.bankAccountValue}" scale="${decimals}"/></td>
                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:form>
    <div class="paginateButtons">
        <g:paginate total="${reconciliationLineDetailInstanceTotal}" id="${reconciliationLineInstance.id}"/>
    </div>
</div>
</body>
</html>
