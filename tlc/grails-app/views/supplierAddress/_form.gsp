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
<%@ page import="org.grails.tlc.sys.SystemCountry; org.grails.tlc.sys.SystemAddressFormat" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td></td>
            <td><g:msg code="systemAddressFormat.required" default="Required fields are marked with *"/></td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="country.id"><g:msg code="supplierAddress.country.label" default="Country"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierAddressInstance, field: 'country', 'errors')}">
                <g:domainSelect autofocus="autofocus" onChange="submitform('country')" name="country.id" options="${SystemCountry.list()}" selected="${supplierAddressInstance?.country}" prefix="country.name" code="code" default="name"/>&nbsp;<g:help code="supplierAddress.country"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="format.id"><g:msg code="supplierAddress.format.label" default="Format"/></label>
            </td>
            <td class="value ${hasErrors(bean: supplierAddressInstance, field: 'format', 'errors')}">
                <g:domainSelect onChange="submitform('format')" name="format.id" options="${SystemAddressFormat.list()}" selected="${supplierAddressInstance?.format}" prefix="systemAddressFormat.name" code="code" default="name"/>&nbsp;<g:help code="supplierAddress.format"/>
            </td>
        </tr>

        <g:each in="${supplierAddressLines}" status="i" var="supplierAddressLine">
            <tr class="prop">
                <td class="name">
                    <label for="${supplierAddressLine.property}">${supplierAddressLine.label?.encodeAsHTML()}</label>
                </td>
                <td class="value ${hasErrors(bean: supplierAddressInstance, field: supplierAddressLine.property, 'errors')}">
                    <input type="text" maxlength="50" size="${supplierAddressLine.width}" id="${supplierAddressLine.property}" name="${supplierAddressLine.property}" value="${supplierAddressLine.value?.encodeAsHTML()}"/>${supplierAddressLine.required ? '*' : ''}
                </td>
            </tr>
        </g:each>

        <g:if test="${transferList}">
            <tr class="prop">
                <td class="vtop name">
                    <label for="transfers"><g:msg code="supplierAddress.usageTransfers.label" default="Transfer Usages"/></label>
                </td>
                <td class="value">
                    <g:domainSelect name="transfers" size="5" options="${transferList}" selected="${supplierAddressInstance.usageTransfers}" prefix="supplierAddressType.name" code="code" default="name"/>&nbsp;<g:help code="supplierAddress.usageTransfers"/>
                </td>
            </tr>
        </g:if>

        </tbody>
    </table>
</div>
