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
    <title><g:msg code="show" domain="supplier"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="supplier"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="supplier"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="supplier"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                    <td class="value">${display(bean: supplierInstance, field: 'id')}</td>

                </tr>
            </g:permit>



            <tr class="prop">
                <td class="name"><g:msg code="supplier.code.label" default="Code"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'code')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="supplier.name.label" default="Name"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'name')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="supplier.country.label" default="Country"/></td>

                <td class="value"><g:msg code="country.name.${supplierInstance.country.code}" default="${supplierInstance.country.name}"/></td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="supplier.currency.label" default="Currency"/></td>

                <td class="value"><g:msg code="currency.name.${supplierInstance.currency.code}" default="${supplierInstance.currency.name}"/></td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="supplier.revaluationMethod.label" default="Revaluation Method"/></td>

                <td class="value">${supplierInstance.revaluationMethod ? msg(code: 'supplier.revaluationMethod.' + supplierInstance.revaluationMethod, default: supplierInstance.revaluationMethod) : ''}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="supplier.taxId.label" default="Tax Id"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'taxId')}</td>

            </tr>


            <tr class="prop">
                <td class="name"><g:msg code="supplier.taxCode.label" default="Tax Code"/></td>

                <td class="value">${supplierInstance.taxCode ? msg(code: 'taxCode.name.' + supplierInstance.taxCode.code, default: supplierInstance.taxCode.name) : ''}</td>
            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="supplier.accountCurrentBalance.label" default="Current Balance"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'accountCurrentBalance', scale: supplierInstance.currency.decimals)}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="supplier.accountCreditLimit.label" default="Credit Limit"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'accountCreditLimit', scale: 0)}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="supplier.settlementDays.label" default="Settlement Days"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'settlementDays')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="supplier.periodicSettlement.label" default="Periodic Settlement"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'periodicSettlement')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="supplier.active.label" default="Active"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'active')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="supplier.accessCode.label" default="Access Code"/></td>

                <td class="value">${supplierInstance.accessCode?.encodeAsHTML()}</td>

            </tr>

            <tr>
                <td colspan="2" class="name"><h2><g:msg code="documentType.autoPayments" default="Auto Payments"/></h2></td>
            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="supplier.schedule.label" default="Auto-Payment Schedule"/></td>

                <td class="value">${supplierInstance.schedule ? msg(code: 'paymentSchedule.name.' + supplierInstance.schedule.code, default: supplierInstance.schedule.name) : ''}</td>

            </tr>


            <tr class="prop">
                <td class="name"><g:msg code="supplier.documentType.label" default="Auto-Payment Document Type"/></td>

                <td class="value"><g:display value="${supplierInstance.documentType?.name}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="supplier.bankSortCode.label" default="Bank Sort Code"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'bankSortCode')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="supplier.bankAccountName.label" default="Bank Account Name"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'bankAccountName')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="supplier.bankAccountNumber.label" default="Bank Account Number"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'bankAccountNumber')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="supplier.nextAutoPaymentDate.label" default="Next Auto-Payment Date"/></td>

                <td class="value">${display(bean: supplierInstance, field: 'nextAutoPaymentDate', scale: 1)}</td>

            </tr>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                    <td class="value">${display(bean: supplierInstance, field: 'securityCode')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: supplierInstance, field: 'dateCreated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                    <td class="value">${display(bean: supplierInstance, field: 'lastUpdated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                    <td class="value">${display(bean: supplierInstance, field: 'version')}</td>

                </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${supplierInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <g:if test="${!hasTransactions}">
                <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
            </g:if>
        </g:form>
    </div>
</div>
</body>
</html>
