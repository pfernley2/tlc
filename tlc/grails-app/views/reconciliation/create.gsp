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
    <title><g:msg code="create" domain="reconciliation"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list" params="${[bankAccount: reconciliationInstance.bankAccount.id]}"><g:msg code="list" domain="reconciliation"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="reconciliation.create.for" args="${[reconciliationInstance.bankAccount.name, reconciliationInstance.bankAccount.currency.code]}" default="Create Reconciliation in ${reconciliationInstance.bankAccount.currency.code} for ${reconciliationInstance.bankAccount.name}"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${reconciliationInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${reconciliationInstance}"/>
        </div>
    </g:hasErrors>
    <div class="textual">
        <g:msg code="generic.task.submit" default="Click the Submit button to run the report."/>
    </div>
    <g:form action="save" method="post">
        <input type="hidden" name="bankAccount" value="${reconciliationInstance.bankAccount.id}"/>
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td colspan="2" class="mediumTopPadding mediumBottomPadding"><g:msg code="reconciliation.warn" default="Be sure to enter the correct statement date and balance otherwise you will have to delete the reconciliation and recreate it."/></td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="statementDate"><g:msg code="reconciliation.statementDate.label" default="Statement Date"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: reconciliationInstance, field: 'statementDate', 'errors')}">
                        <input autofocus="autofocus" type="text" name="statementDate" id="statementDate" size="20" value="${display(bean: reconciliationInstance, field: 'statementDate', scale: 1)}"/>&nbsp;<g:help code="reconciliation.statementDate"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="statementBalance"><g:msg code="reconciliation.statementBalance.label" default="Statement Balance"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: reconciliationInstance, field: 'statementBalance', 'errors')}">
                        <input type="text" id="statementBalance" name="statementBalance" size="20" value="${display(bean: reconciliationInstance, field: 'statementBalance', scale: reconciliationInstance.bankAccount.currency.decimals)}"/>&nbsp;<g:help code="reconciliation.statementBalance"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="preferredStart"><g:msg code="queuedTask.demand.delay.label" default="Delay Until"/></label>
                    </td>
                    <td class="value">
                        <input type="text" size="20" id="preferredStart" name="preferredStart" value=""/>&nbsp;<g:help code="queuedTask.demand.delay"/>
                    </td>
                </tr>

                </tbody>
            </table>
        </div>
        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="${msg(code: 'generic.submit', 'default': 'Submit')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
