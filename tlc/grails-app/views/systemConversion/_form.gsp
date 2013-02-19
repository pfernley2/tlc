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
<%@ page import="org.grails.tlc.sys.SystemUnit" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="code"><g:msg code="systemConversion.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemConversionInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: systemConversionInstance, field: 'code')}"/>&nbsp;<g:help code="systemConversion.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="systemConversion.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemConversionInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="50" size="30" id="name" name="name" value="${systemConversionInstance.id ? msg(code: 'conversion.name.' + systemConversionInstance.code, default: systemConversionInstance.name) : systemConversionInstance.name}"/>&nbsp;<g:help code="systemConversion.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="source.id"><g:msg code="systemConversion.source.label" default="Source"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemConversionInstance, field: 'source', 'errors')}">
                <g:domainSelect name="source.id" options="${SystemUnit.list()}" selected="${systemConversionInstance?.source}" prefix="unit.name" code="code" default="name"/>&nbsp;<g:help code="systemConversion.source"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="target.id"><g:msg code="systemConversion.target.label" default="Target"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemConversionInstance, field: 'target', 'errors')}">
                <g:domainSelect name="target.id" options="${SystemUnit.list()}" selected="${systemConversionInstance?.target}" prefix="unit.name" code="code" default="name"/>&nbsp;<g:help code="systemConversion.target"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="preAddition"><g:msg code="systemConversion.preAddition.label" default="Pre Addition"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemConversionInstance, field: 'preAddition', 'errors')}">
                <input type="text" id="preAddition" name="preAddition" size="20" value="${display(bean: systemConversionInstance, field: 'preAddition')}"/>&nbsp;<g:help code="systemConversion.preAddition"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="multiplier"><g:msg code="systemConversion.multiplier.label" default="Multiplier"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemConversionInstance, field: 'multiplier', 'errors')}">
                <input type="text" id="multiplier" name="multiplier" size="20" value="${display(bean: systemConversionInstance, field: 'multiplier')}"/>&nbsp;<g:help code="systemConversion.multiplier"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="postAddition"><g:msg code="systemConversion.postAddition.label" default="Post Addition"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemConversionInstance, field: 'postAddition', 'errors')}">
                <input type="text" id="postAddition" name="postAddition" size="20" value="${display(bean: systemConversionInstance, field: 'postAddition')}"/>&nbsp;<g:help code="systemConversion.postAddition"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
