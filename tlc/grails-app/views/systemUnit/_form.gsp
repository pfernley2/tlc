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
<%@ page import="org.grails.tlc.sys.SystemMeasure; org.grails.tlc.sys.SystemScale" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="code"><g:msg code="systemUnit.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUnitInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: systemUnitInstance, field: 'code')}"/>&nbsp;<g:help code="systemUnit.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="systemUnit.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUnitInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="30" size="30" id="name" name="name" value="${systemUnitInstance.id ? msg(code: 'unit.name.' + systemUnitInstance.code, default: systemUnitInstance.name) : systemUnitInstance.name}"/>&nbsp;<g:help code="systemUnit.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="measure.id"><g:msg code="systemUnit.measure.label" default="Measure"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUnitInstance, field: 'measure', 'errors')}">
                <g:domainSelect name="measure.id" options="${SystemMeasure.list()}" selected="${systemUnitInstance?.measure}" prefix="measure.name" code="code" default="name"/>&nbsp;<g:help code="systemUnit.measure"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="scale.id"><g:msg code="systemUnit.scale.label" default="Scale"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUnitInstance, field: 'scale', 'errors')}">
                <g:domainSelect name="scale.id" options="${SystemScale.list()}" selected="${systemUnitInstance?.scale}" prefix="scale.name" code="code" default="name"/>&nbsp;<g:help code="systemUnit.scale"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="multiplier"><g:msg code="systemUnit.multiplier.label" default="Multiplier"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemUnitInstance, field: 'multiplier', 'errors')}">
                <input type="text" size="20" id="multiplier" name="multiplier" size="20" value="${display(bean: systemUnitInstance, field: 'multiplier')}"/>&nbsp;<g:help code="systemUnit.multiplier"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
