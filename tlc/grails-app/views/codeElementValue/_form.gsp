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

        <g:if test="${accountTotal == 0}">
            <tr class="prop">
                <td class="name">
                    <label for="code"><g:msg code="codeElementValue.code.label" default="Code"/></label>
                </td>
                <td class="value ${hasErrors(bean: codeElementValueInstance, field: 'code', 'errors')}">
                    <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: codeElementValueInstance, field: 'code')}"/>&nbsp;<g:help code="codeElementValue.code"/>
                </td>
            </tr>
        </g:if>

        <tr class="prop">
            <td class="name">
                <label for="shortName"><g:msg code="codeElementValue.shortName.label" default="Short Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: codeElementValueInstance, field: 'shortName', 'errors')}">
                <input ${(accountTotal > 0) ? 'autofocus="autofocus" ' : ''}type="text" maxlength="10" size="10" id="shortName" name="shortName" value="${display(bean: codeElementValueInstance, field: 'shortName')}"/>&nbsp;<g:help code="codeElementValue.shortName"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="codeElementValue.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: codeElementValueInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="30" size="30" id="name" name="name" value="${display(bean: codeElementValueInstance, field: 'name')}"/>&nbsp;<g:help code="codeElementValue.name"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
