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
<%@ page import="org.grails.tlc.books.Recurring" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="show" domain="recurring"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="recurring"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="recurring"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="recurring"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                    <td class="value">${display(bean: recurringInstance, field: 'id')}</td>

                </tr>
            </g:permit>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.account.label" default="Bank Account"/></td>

                <td class="value">${recurringInstance?.account?.name?.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.currency.label" default="Currency"/></td>

                <td class="value">${recurringInstance?.currency?.name?.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.type.label" default="Document Type"/></td>

                <td class="value">${recurringInstance?.type?.name?.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.reference.label" default="Reference"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'reference')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.description.label" default="Description"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'description')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.totalTransactions.label" default="Total Transactions"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'totalTransactions')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.initialDate.label" default="Initial Date"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'initialDate', scale: 1)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.initialValue.label" default="Initial Value"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'initialValue', scale: settings.decimals)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.recursFrom.label" default="Recurs From"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'recursFrom', scale: 1)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.recurrenceType.label" default="Recurrence Type"/></td>

                <td class="value"><g:msg code="${'recurring.recurrenceType.' + recurringInstance.recurrenceType}" default="${recurringInstance.recurrenceType}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.recurrenceInterval.label" default="Recurrence Interval"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'recurrenceInterval')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.lastDayOfMonth.label" default="Last Day Of Month"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'lastDayOfMonth')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.recurringValue.label" default="Recurring Value"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'recurringValue', scale: settings.decimals)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.finalValue.label" default="Final Value"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'finalValue', scale: settings.decimals)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.autoAllocate.label" default="Auto Allocate"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'autoAllocate')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.processedCount.label" default="Processed Count"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'processedCount')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="recurring.nextDue.label" default="Next Due"/></td>

                <td class="value">${display(bean: recurringInstance, field: 'nextDue', scale: 1)}</td>

            </tr>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                    <td class="value">${display(bean: recurringInstance, field: 'securityCode')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: recurringInstance, field: 'dateCreated')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                    <td class="value">${display(bean: recurringInstance, field: 'lastUpdated')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                    <td class="value">${display(bean: recurringInstance, field: 'version')}</td>

                </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${recurringInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
