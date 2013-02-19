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
                <label for="code"><g:msg code="systemActivity.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemActivityInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="50" size="20" id="code" name="code" value="${display(bean: systemActivityInstance, field: 'code')}"/>&nbsp;<g:help code="systemActivity.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="systemOnly"><g:msg code="systemActivity.systemOnly.label" default="System Only"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemActivityInstance, field: 'systemOnly', 'errors')}">
                <g:checkBox name="systemOnly" value="${systemActivityInstance?.systemOnly}"></g:checkBox>&nbsp;<g:help code="systemActivity.systemOnly"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
