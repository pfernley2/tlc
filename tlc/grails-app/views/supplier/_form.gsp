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
<%@ page import="org.grails.tlc.corp.ExchangeCurrency; org.grails.tlc.corp.TaxCode; org.grails.tlc.books.AccessCode; org.grails.tlc.sys.SystemCountry" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="code"><g:msg code="supplier.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="20" size="20" id="code" name="code" value="${display(bean: supplierInstance, field: 'code')}"/>&nbsp;<g:help code="supplier.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="supplier.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="50" size="30" id="name" name="name" value="${display(bean: supplierInstance, field: 'name')}"/>&nbsp;<g:help code="supplier.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="country.id"><g:msg code="supplier.country.label" default="Country"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'country', 'errors')}">
                <g:domainSelect name="country.id" options="${SystemCountry.list()}" selected="${supplierInstance?.country}" prefix="country.name" code="code" default="name"/>&nbsp;<g:help code="supplier.country"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="currency.id"><g:msg code="supplier.currency.label" default="Currency"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'currency', 'errors')}">
                <g:domainSelect name="currency.id" options="${currencyList}" selected="${supplierInstance?.currency}" prefix="currency.name" code="code" default="name" onchange="setRevaluation(${companyCurrencyId})"/>&nbsp;<g:help code="supplier.currency"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="revaluationMethod"><g:msg code="supplier.revaluationMethod.label" default="Revaluation Method"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'revaluationMethod', 'errors')}">
                <g:select id="revaluationMethod" name="revaluationMethod" from="${supplierInstance.constraints.revaluationMethod.inList}" value="${supplierInstance.revaluationMethod}" valueMessagePrefix="supplier.revaluationMethod" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="supplier.revaluationMethod"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="taxId"><g:msg code="supplier.taxId.label" default="Tax Id"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'taxId', 'errors')}">
                <input type="text" maxlength="20" size="20" id="taxId" name="taxId" value="${display(bean: supplierInstance, field: 'taxId')}"/>&nbsp;<g:help code="supplier.taxId"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="taxCode.id"><g:msg code="supplier.taxCode.label" default="Tax Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'taxCode', 'errors')}">
                <g:domainSelect name="taxCode.id" options="${taxList}" selected="${supplierInstance?.taxCode}" prefix="taxCode.name" code="code" default="name" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="supplier.taxCode"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="accountCreditLimit"><g:msg code="supplier.accountCreditLimit.label" default="Credit Limit"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'accountCreditLimit', 'errors')}">
                <input type="text" id="accountCreditLimit" name="accountCreditLimit" size="20" value="${display(bean: supplierInstance, field: 'accountCreditLimit', scale: 0)}"/>&nbsp;<g:help code="supplier.accountCreditLimit"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="settlementDays"><g:msg code="supplier.settlementDays.label" default="Settlement Days"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'settlementDays', 'errors')}">
                <input type="text" maxlength="3" size="5" id="settlementDays" name="settlementDays" value="${display(bean: supplierInstance, field: 'settlementDays')}"/>&nbsp;<g:help code="supplier.settlementDays"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="periodicSettlement"><g:msg code="supplier.periodicSettlement.label" default="Periodic Settlement"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'periodicSettlement', 'errors')}">
                <g:checkBox name="periodicSettlement" value="${supplierInstance.periodicSettlement}"></g:checkBox>&nbsp;<g:help code="supplier.periodicSettlement"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="active"><g:msg code="supplier.active.label" default="Active"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'active', 'errors')}">
                <g:checkBox name="active" value="${supplierInstance?.active}"></g:checkBox>&nbsp;<g:help code="supplier.active"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="accessCode.id"><g:msg code="supplier.accessCode.label" default="Access Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'accessCode', 'errors')}">
                <g:select optionKey="id" optionValue="name" from="${accessList}" name="accessCode.id" value="${supplierInstance?.accessCode?.id}"/>&nbsp;<g:help code="supplier.accessCode"/>
            </td>
        </tr>

        <tr>
            <td colspan="2" class="name"><h2><g:msg code="documentType.autoPayments" default="Auto Payments"/></h2></td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="schedule.id"><g:msg code="supplier.schedule.label" default="Auto-Payment Schedule"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'schedule', 'errors')}">
                <g:if test="${scheduleList}">
                    <g:domainSelect name="schedule.id" options="${scheduleList}" selected="${supplierInstance.schedule?.id}" code="code" prefix="paymentSchedule.name" default="name" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="supplier.schedule"/>
                </g:if>
                <g:else>
                    <g:msg code="supplier.no.schedules" default="No auto-payment schedules have been defined"/>
                </g:else>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="documentType.id"><g:msg code="supplier.documentType.label" default="Auto-Payment Document Type"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'documentType', 'errors')}">
                <g:if test="${documentTypeList}">
                    <g:domainSelect name="documentType.id" options="${documentTypeList}" selected="${supplierInstance.documentType?.id}" displays="name" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="supplier.documentType"/>
                </g:if>
                <g:else>
                    <g:msg code="supplier.no.doctypes" default="No auto-payment document types have been defined"/>
                </g:else>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="bankSortCode"><g:msg code="supplier.bankSortCode.label" default="Bank Sort Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'bankSortCode', 'errors')}">
                <input type="text" maxlength="20" size="20" id="bankSortCode" name="bankSortCode" value="${display(bean: supplierInstance, field: 'bankSortCode')}"/>&nbsp;<g:help code="supplier.bankSortCode"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="bankAccountName"><g:msg code="supplier.bankAccountName.label" default="Bank Account Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'bankAccountName', 'errors')}">
                <input type="text" maxlength="20" size="20" id="bankAccountName" name="bankAccountName" value="${display(bean: supplierInstance, field: 'bankAccountName')}"/>&nbsp;<g:help code="supplier.bankAccountName"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="bankAccountNumber"><g:msg code="supplier.bankAccountNumber.label" default="Bank Account Number"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierInstance, field: 'bankAccountNumber', 'errors')}">
                <input type="text" maxlength="20" size="20" id="bankAccountNumber" name="bankAccountNumber" value="${display(bean: supplierInstance, field: 'bankAccountNumber')}"/>&nbsp;<g:help code="supplier.bankAccountNumber"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
