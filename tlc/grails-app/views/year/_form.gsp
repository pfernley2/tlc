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
                <label for="code"><g:msg code="year.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean:yearInstance,field:'code','errors')}">
                <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean:yearInstance,field:'code')}"/>&nbsp;<g:help code="year.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="validFrom"><g:msg code="year.validFrom.label" default="Valid From"/></label>
            </td>
            <td class="value ${hasErrors(bean:yearInstance,field:'validFrom','errors')}">
                <input type="text" name="validFrom" id="validFrom" size="20" value="${display(bean:yearInstance,field:'validFrom', scale: 1)}"/>&nbsp;<g:help code="year.validFrom"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="validTo"><g:msg code="year.validTo.label" default="Valid To"/></label>
            </td>
            <td class="value ${hasErrors(bean:yearInstance,field:'validTo','errors')}">
                <input type="text" name="validTo" id="validTo" size="20" value="${display(bean:yearInstance,field:'validTo', scale: 1)}"/>&nbsp;<g:help code="year.validTo"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
