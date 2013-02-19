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
    <title><g:msg code="reconciliation.remove.document" default="Remove Document"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="edit" id="${reconciliationInstance.id}"><g:msg code="edit" domain="reconciliation"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="reconciliation.remove.document" default="Remove Document"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>
                <th><g:msg code="reconciliationLine.date" default="Date"/></th>
                <th><g:msg code="generic.document" default="Document"/></th>
                <th><g:msg code="reconciliationLine.reference" default="Reference"/></th>
                <th><g:msg code="reconciliationLine.details.label" default="Details"/></th>
                <th class="right"><g:msg code="reconciliationLine.payment" default="Payment"/></th>
                <th class="right"><g:msg code="reconciliationLine.receipt" default="Receipt"/></th>
                <th></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${reconciliationLineInstanceList}" status="i" var="reconciliationLineInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
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
                    <td class="right"><g:credit value="${reconciliationLineInstance.bankAccountValue}" scale="${decimals}"/></td>
                    <td class="right"><g:debit value="${reconciliationLineInstance.bankAccountValue}" scale="${decimals}"/></td>
                    <td>
                        <g:form action="removing" method="post">
                            <input type="hidden" name="reconciliation" value="${reconciliationInstance.id}"/>
                            <input type="hidden" name="id" value="${reconciliationLineInstance.id}"/>
                            <input type="submit" value="${msg(code: 'reconciliation.remove', default: 'Remove')}"/>
                        </g:form>
                    </td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate action="edit" total="${reconciliationLineInstanceTotal}" params="${[reconciliation: reconciliationInstance.id]}"/>
    </div>
</div>
</body>
</html>
