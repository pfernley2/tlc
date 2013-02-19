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
<%@ page import="org.grails.tlc.books.Customer" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="show" domain="customer"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="customer"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="customer"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="customer"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                    <td class="value">${display(bean: customerInstance, field: 'id')}</td>

                </tr>
            </g:permit>



            <tr class="prop">
                <td class="name"><g:msg code="customer.code.label" default="Code"/></td>

                <td class="value">${display(bean: customerInstance, field: 'code')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="customer.name.label" default="Name"/></td>

                <td class="value">${display(bean: customerInstance, field: 'name')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="customer.country.label" default="Country"/></td>

                <td class="value"><g:msg code="country.name.${customerInstance.country.code}" default="${customerInstance.country.name}"/></td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="customer.currency.label" default="Currency"/></td>

                <td class="value"><g:msg code="currency.name.${customerInstance.currency.code}" default="${customerInstance.currency.name}"/></td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="customer.revaluationMethod.label" default="Revaluation Method"/></td>

                <td class="value">${customerInstance.revaluationMethod ? msg(code: 'customer.revaluationMethod.' + customerInstance.revaluationMethod, default: customerInstance.revaluationMethod) : ''}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="customer.taxId.label" default="Tax Id"/></td>

                <td class="value">${display(bean: customerInstance, field: 'taxId')}</td>

            </tr>


            <tr class="prop">
                <td class="name"><g:msg code="customer.taxCode.label" default="Tax Code"/></td>

                <td class="value">${customerInstance.taxCode ? msg(code: 'taxCode.name.' + customerInstance.taxCode.code, default: customerInstance.taxCode.name) : ''}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="customer.accountCurrentBalance.label" default="Current Balance"/></td>

                <td class="value">${display(bean: customerInstance, field: 'accountCurrentBalance', scale: customerInstance.currency.decimals)}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="customer.accountCreditLimit.label" default="Credit Limit"/></td>

                <td class="value">${display(bean: customerInstance, field: 'accountCreditLimit', scale: 0)}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="customer.settlementDays.label" default="Settlement Days"/></td>

                <td class="value">${display(bean: customerInstance, field: 'settlementDays')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="customer.periodicSettlement.label" default="Periodic Settlement"/></td>

                <td class="value">${display(bean: customerInstance, field: 'periodicSettlement')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="customer.active.label" default="Active"/></td>

                <td class="value">${display(bean: customerInstance, field: 'active')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="customer.accessCode.label" default="Access Code"/></td>

                <td class="value">${customerInstance?.accessCode?.encodeAsHTML()}</td>

            </tr>


            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                    <td class="value">${display(bean: customerInstance, field: 'securityCode')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: customerInstance, field: 'dateCreated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                    <td class="value">${display(bean: customerInstance, field: 'lastUpdated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                    <td class="value">${display(bean: customerInstance, field: 'version')}</td>

                </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${customerInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <g:if test="${!hasTransactions}">
                <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
            </g:if>
        </g:form>
    </div>
</div>
</body>
</html>
