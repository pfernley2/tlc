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
                <label for="code"><g:msg code="systemSetting.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemSettingInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="100" size="30" id="code" name="code" value="${display(bean: systemSettingInstance, field: 'code')}"/>&nbsp;<g:help code="systemSetting.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="dataType"><g:msg code="systemSetting.dataType.label" default="Data Type"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemSettingInstance, field: 'dataType', 'errors')}">
                <g:select id="dataType" name="dataType" from="${systemSettingInstance.constraints.dataType.inList}" value="${systemSettingInstance.dataType}" valueMessagePrefix="generic.dataType"/>&nbsp;<g:help code="systemSetting.dataType"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="dataScale"><g:msg code="systemSetting.dataScale.label" default="Data Scale"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemSettingInstance, field: 'dataScale', 'errors')}">
                <g:select from="${1..10}" id="dataScale" name="dataScale" value="${systemSettingInstance?.dataScale}" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="systemSetting.dataScale"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="value"><g:msg code="systemSetting.value.label" default="Value"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemSettingInstance, field: 'value', 'errors')}">
                <input type="text" maxlength="100" size="30" id="value" name="value" value="${display(bean: systemSettingInstance, field: 'value')}"/>&nbsp;<g:help code="systemSetting.value"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="systemOnly"><g:msg code="systemSetting.systemOnly.label" default="System Only"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemSettingInstance, field: 'systemOnly', 'errors')}">
                <g:checkBox name="systemOnly" value="${systemSettingInstance?.systemOnly}"></g:checkBox>&nbsp;<g:help code="systemSetting.systemOnly"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
