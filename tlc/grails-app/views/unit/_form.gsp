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
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="code"><g:msg code="unit.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean:unitInstance,field:'code','errors')}">
                <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean:unitInstance,field:'code')}"/>&nbsp;<g:help code="unit.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="unit.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean:unitInstance,field:'name','errors')}">
                <input type="text" maxlength="30" size="30" id="name" name="name" value="${unitInstance.id ? msg(code: 'unit.name.' + unitInstance.code, default: unitInstance.name) : unitInstance.name}"/>&nbsp;<g:help code="unit.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="measure.id"><g:msg code="unit.measure.label" default="Measure"/></label>
            </td>
            <td class="value ${hasErrors(bean:unitInstance,field:'measure','errors')}">
                <g:domainSelect name="measure.id" options="${measureList}" selected="${unitInstance?.measure}" prefix="measure.name" code="code" default="name"/>&nbsp;<g:help code="unit.measure"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="scale.id"><g:msg code="unit.scale.label" default="Scale"/></label>
            </td>
            <td class="value ${hasErrors(bean:unitInstance,field:'scale','errors')}">
                <g:domainSelect name="scale.id" options="${scaleList}" selected="${unitInstance?.scale}" prefix="scale.name" code="code" default="name"/>&nbsp;<g:help code="unit.scale"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="multiplier"><g:msg code="unit.multiplier.label" default="Multiplier"/></label>
            </td>
            <td class="value ${hasErrors(bean:unitInstance,field:'multiplier','errors')}">
                <input type="text" id="multiplier" name="multiplier" size="20" value="${display(bean:unitInstance,field:'multiplier')}"/>&nbsp;<g:help code="unit.multiplier"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
