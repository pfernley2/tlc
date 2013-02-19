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
<%@ page import="org.grails.tlc.sys.SystemAccountType; org.grails.tlc.books.ChartSection" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="code"><g:msg code="account.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: accountInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="87" size="30" id="code" name="code" value="${display(bean: accountInstance, field: 'code')}"/>&nbsp;<g:help code="account.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="account.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: accountInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="87" size="30" id="name" name="name" value="${display(bean: accountInstance, field: 'name')}"/>&nbsp;<g:help code="account.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="currency.id"><g:msg code="account.currency.label" default="Currency"/></label>
            </td>
            <td class="value ${hasErrors(bean: accountInstance, field: 'currency', 'errors')}">
                <g:if test="${currencyList}">
                    <g:domainSelect name="currency.id" options="${currencyList}" selected="${accountInstance.currency}" prefix="currency.name" code="code" default="name"/>&nbsp;<g:help code="account.currency"/>
                </g:if>
                <g:else>
                    ${msg(code: 'currency.name.' + accountInstance.currency.code, default: accountInstance.currency.name)}
                </g:else>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="revaluationAccount.id"><g:msg code="account.revaluationAccount.label" default="Revaluation Account"/></label>
            </td>
            <td class="value ${hasErrors(bean: accountInstance, field: 'revaluationAccount', 'errors')}">
                <g:domainSelect name="revaluationAccount.id" options="${revaluationAccountList}" selected="${accountInstance.revaluationAccount}"  displays="${['code', 'name']}" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="account.revaluationAccount"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="revaluationMethod"><g:msg code="account.revaluationMethod.label" default="Revaluation Method"/></label>
            </td>
            <td class="value ${hasErrors(bean: accountInstance, field: 'revaluationMethod', 'errors')}">
                <g:select id="revaluationMethod" name="revaluationMethod" from="${accountInstance.constraints.revaluationMethod.inList}" value="${accountInstance.revaluationMethod}" valueMessagePrefix="account.revaluationMethod" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="account.revaluationMethod"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="status"><g:msg code="account.status.label" default="Status"/></label>
            </td>
            <td class="value ${hasErrors(bean: accountInstance, field: 'status', 'errors')}">
                <g:select id="status" name="status" from="${accountInstance.constraints.status.inList}" value="${accountInstance.status}" valueMessagePrefix="account.status"/>&nbsp;<g:help code="account.status"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="type.id"><g:msg code="account.type.label" default="Type"/></label>
            </td>
            <td class="value ${hasErrors(bean: accountInstance, field: 'type', 'errors')}">
                <g:domainSelect name="type.id" options="${SystemAccountType.list()}" selected="${accountInstance.type}" prefix="systemAccountType.name" code="code" default="name" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="account.type"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="active"><g:msg code="account.active.label" default="Active"/></label>
            </td>
            <td class="value ${hasErrors(bean: accountInstance, field: 'active', 'errors')}">
                <g:checkBox name="active" value="${accountInstance?.active}"></g:checkBox>&nbsp;<g:help code="account.active"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="section.id"><g:msg code="account.section.label" default="Section"/></label>
            </td>
            <td class="value ${hasErrors(bean: accountInstance, field: 'section', 'errors')}">
                <g:domainSelect name="section.id" options="${chartSectionList}" selected="${accountInstance.section}" displays="name"/>&nbsp;<g:help code="account.section"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="autoCreateElementValues"><g:msg code="account.autoCreateElementValues.label" default="Auto Create Element Values"/></label>
            </td>
            <td class="value ${hasErrors(bean: accountInstance, field: 'autoCreateElementValues', 'errors')}">
                <g:checkBox name="autoCreateElementValues" value="${accountInstance?.autoCreateElementValues}"></g:checkBox>&nbsp;<g:help code="account.autoCreateElementValues"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
