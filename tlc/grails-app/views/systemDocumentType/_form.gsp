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
<%@ page import="org.grails.tlc.sys.SystemActivity" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="code"><g:msg code="systemDocumentType.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemDocumentTypeInstance,field:'code','errors')}">
                <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean:systemDocumentTypeInstance,field:'code')}"/>&nbsp;<g:help code="systemDocumentType.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="systemDocumentType.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemDocumentTypeInstance,field:'name','errors')}">
                <input type="text" maxlength="30" size="30" id="name" name="name" value="${systemDocumentTypeInstance.id ? msg(code: 'systemDocumentType.name.' + systemDocumentTypeInstance.code, default: systemDocumentTypeInstance.name) : systemDocumentTypeInstance.name}"/>&nbsp;<g:help code="systemDocumentType.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="activity"><g:msg code="systemDocumentType.activity.label" default="Activity"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemDocumentTypeInstance, field: 'activity', 'errors')}">
                <g:select optionKey="id" from="${SystemActivity.list([sort: 'code'])}" name="activity.id" value="${systemDocumentTypeInstance?.activity?.id}"/>&nbsp;<g:help code="systemDocumentType.activity"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="metaType"><g:msg code="systemDocumentType.metaType.label" default="Meta Type"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemDocumentTypeInstance, field: 'metaType', 'errors')}">
                <g:select id="metaType" name="metaType" from="${systemDocumentTypeInstance.constraints.metaType.inList}" value="${systemDocumentTypeInstance.metaType}" valueMessagePrefix="systemDocumentType.metaType"/>&nbsp;<g:help code="systemDocumentType.metaType"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="analysisIsDebit"><g:msg code="systemDocumentType.analysisIsDebit.label" default="Analysis Is Debit"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemDocumentTypeInstance, field:'analysisIsDebit', 'errors')}">
                <g:checkBox name="analysisIsDebit" value="${systemDocumentTypeInstance?.analysisIsDebit}"></g:checkBox>&nbsp;<g:help code="systemDocumentType.analysisIsDebit"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="customerAllocate"><g:msg code="systemDocumentType.customerAllocate.label" default="Customer Allocatable"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemDocumentTypeInstance, field:'customerAllocate', 'errors')}">
                <g:checkBox name="customerAllocate" value="${systemDocumentTypeInstance?.customerAllocate}"></g:checkBox>&nbsp;<g:help code="systemDocumentType.customerAllocate"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="supplierAllocate"><g:msg code="systemDocumentType.supplierAllocate.label" default="Supplier Allocatable"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemDocumentTypeInstance, field:'supplierAllocate', 'errors')}">
                <g:checkBox name="supplierAllocate" value="${systemDocumentTypeInstance?.supplierAllocate}"></g:checkBox>&nbsp;<g:help code="systemDocumentType.supplierAllocate"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
