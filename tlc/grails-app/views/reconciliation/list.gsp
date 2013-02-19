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
<%@ page import="org.grails.tlc.books.Reconciliation" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="list" domain="reconciliation"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <g:if test="${allFinalized}">
        <span class="menuButton"><g:link class="create" action="create" params="${[bankAccount: bankAccount.id]}"><g:msg code="new" domain="reconciliation"/></g:link></span>
    </g:if>
</div>
<div id="main-content" class="body" role="main">
    <g:if test="${bankAccount.id}">
        <g:pageTitle code="reconciliation.list.for" args="${[bankAccount.name]}" default="Bank Reconciliation List for ${bankAccount.name}"/>
    </g:if>
    <g:else>
        <g:pageTitle code="list" domain="reconciliation"/>
    </g:else>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:form action="list" method="post" name="bankform">
            <input type="hidden" name="changed" value="true"/>
            <g:msg code="reconciliation.bankAccount.label" default="Bank Account"/>
            <g:domainSelect name="bankAccount" options="${bankAccountList}" selected="${bankAccount}" displays="${['code', 'name']}" sort="false" noSelection="['': msg(code: 'generic.select', default: '-- select --')]" onchange="document.bankform.submit();"/>&nbsp;<g:help code="reconciliation.bankAccount"/>
        </g:form>
    </div>
    <g:if test="${bankAccount.id}">
        <div class="center smallBottomMargin">
            <g:msg code="reconciliation.currency.note" args="${[bankAccount.currency.code]}" default="(All values shown in ${bankAccount.currency.code})"/>
        </div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="statementDate" title="Statement Date" titleKey="reconciliation.statementDate.label"/>

                <g:sortableColumn property="statementBalance" title="Statement Balance" titleKey="reconciliation.statementBalance.label"/>

                <g:sortableColumn property="bankAccountBalance" title="Bank Account Balance" titleKey="reconciliation.bankAccountBalance.label"/>

                <g:sortableColumn property="finalizedDate" title="Finalized Date" titleKey="reconciliation.finalizedDate.label"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${reconciliationInstanceList}" status="i" var="reconciliationInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="${reconciliationInstance.finalizedDate ? 'display' : 'show'}" id="${reconciliationInstance.id}">${display(bean: reconciliationInstance, field: 'statementDate', scale: 1)}</g:link></td>

                    <td>${display(bean: reconciliationInstance, field: 'statementBalance', scale: bankAccount.currency.decimals)}</td>

                    <td>${display(bean: reconciliationInstance, field: 'bankAccountBalance', scale: bankAccount.currency.decimals)}</td>

                    <td>${display(bean: reconciliationInstance, field: 'finalizedDate', scale: 1)}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${reconciliationInstanceTotal}" params="${[bankAccount: bankAccount.id]}"/>
    </div>
</div>
</body>
</html>
