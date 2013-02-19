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
                <label for="code"><g:msg code="setting.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean:settingInstance,field:'code','errors')}">
                <input autofocus="autofocus" type="text" maxlength="100" size="30" id="code" name="code" value="${display(bean:settingInstance,field:'code')}"/>&nbsp;<g:help code="setting.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="dataType"><g:msg code="setting.dataType.label" default="Data Type"/></label>
            </td>
            <td class="value ${hasErrors(bean:settingInstance,field:'dataType','errors')}">
                <g:select id="dataType" name="dataType" from="${settingInstance.constraints.dataType.inList}" value="${settingInstance.dataType}" valueMessagePrefix="generic.dataType"/>&nbsp;<g:help code="setting.dataType"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="dataScale"><g:msg code="setting.dataScale.label" default="Data Scale"/></label>
            </td>
            <td class="value ${hasErrors(bean:settingInstance,field:'dataScale','errors')}">
                <g:select from="${1..10}" id="dataScale" name="dataScale" value="${settingInstance?.dataScale}" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="setting.dataScale"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="value"><g:msg code="setting.value.label" default="Value"/></label>
            </td>
            <td class="value ${hasErrors(bean:settingInstance,field:'value','errors')}">
                <input type="text" maxlength="100" size="30" id="value" name="value" value="${display(bean:settingInstance,field:'value')}"/>&nbsp;<g:help code="setting.value"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
