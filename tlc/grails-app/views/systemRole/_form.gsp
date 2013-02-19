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
                <label for="code"><g:msg code="systemRole.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemRoleInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="20" size="20" id="code" name="code" value="${display(bean: systemRoleInstance, field: 'code')}"/>&nbsp;<g:help code="systemRole.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="systemRole.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemRoleInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="100" size="30" id="name" name="name" value="${systemRoleInstance.id ? msg(code: 'role.name.' + systemRoleInstance.code, default: systemRoleInstance.name) : systemRoleInstance.name}"/>&nbsp;<g:help code="systemRole.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="systemOnly"><g:msg code="systemRole.systemOnly.label" default="System Only"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemRoleInstance, field: 'systemOnly', 'errors')}">
                <g:checkBox name="systemOnly" value="${systemRoleInstance?.systemOnly}"></g:checkBox>&nbsp;<g:help code="systemRole.systemOnly"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
