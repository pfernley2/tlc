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
<%@ page import="org.grails.tlc.corp.TaxCode; org.grails.tlc.sys.SystemLanguage; org.grails.tlc.sys.SystemCurrency; org.grails.tlc.sys.SystemCountry" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="company.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: companyInstance, field: 'name', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="100" size="30" id="name" name="name" value="${display(bean: companyInstance, field: 'name')}"/>&nbsp;<g:help code="company.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="country"><g:msg code="company.country.label" default="Country"/></label>
            </td>
            <td class="value ${hasErrors(bean: companyInstance, field: 'country', 'errors')}">
                <g:domainSelect name="country.id" options="${SystemCountry.list()}" selected="${companyInstance?.country}" prefix="country.name" code="code" default="name"/>&nbsp;<g:help code="company.country"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="language"><g:msg code="company.language.label" default="Language"/></label>
            </td>
            <td class="value ${hasErrors(bean: companyInstance, field: 'language', 'errors')}">
                <g:domainSelect name="language.id" options="${SystemLanguage.list()}" selected="${companyInstance?.language}" prefix="language.name" code="code" default="name"/>&nbsp;<g:help code="company.language"/>
            </td>
        </tr>

        <g:if test="${companyInstance?.id}">
            <tr class="prop">
                <td class="name"><g:msg code="company.currency.label" default="Currency"/></td>
                <td class="value">${msg(code: 'currency.name.' + companyInstance.displayCurrency?.code, default: companyInstance.displayCurrency?.name)}</td>
            </tr>
            <tr class="prop">
                <td class="name">
                    <label for="displayTaxCode"><g:msg code="company.displayTaxCode.label" default="Tax Code"/></label>
                </td>
                <td class="value ${hasErrors(bean: companyInstance, field: 'displayTaxCode', 'errors')}">
                    <g:domainSelect name="displayTaxCode.id" options="${TaxCode.findAllByCompany(companyInstance, [cache: true])}" selected="${companyInstance?.displayTaxCode}" prefix="taxCode.name" code="code" default="name"/>&nbsp;<g:help code="company.taxCode"/>
                </td>
            </tr>
        </g:if>
        <g:else>
            <tr class="prop">
                <td class="name">
                    <label for="currency"><g:msg code="company.currency.label" default="Currency"/></label>
                </td>
                <td class="value ${hasErrors(bean: companyInstance, field: 'currency', 'errors')}">
                    <g:domainSelect name="currency.id" options="${SystemCurrency.list()}" selected="${companyInstance?.currency}" prefix="currency.name" code="code" default="name"/>&nbsp;<g:help code="company.currency"/>
                </td>
            </tr>
            <g:if test="${companyInstance.loadDemo != null}">
                <tr class="prop">
                    <td class="name">
                        <label for="loadDemo"><g:msg code="company.loadDemo.label" default="Load Demo"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: companyInstance, field: 'loadDemo', 'errors')}">
                        <g:checkBox name="loadDemo" value="${companyInstance.loadDemo}"></g:checkBox>&nbsp;<g:help code="company.loadDemo"/>
                    </td>
                </tr>
            </g:if>
        </g:else>

        </tbody>
    </table>
</div>
