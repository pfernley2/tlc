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
                <label for="code"><g:msg code="period.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: periodInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: periodInstance, field: 'code')}"/>&nbsp;<g:help code="period.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="validFrom"><g:msg code="period.validFrom.label" default="Valid From"/></label>
            </td>
            <td class="value ${hasErrors(bean: periodInstance, field: 'validFrom', 'errors')}">
                <input type="text" name="validFrom" id="validFrom" size="20" value="${display(bean: periodInstance, field: 'validFrom', scale: 1)}"/>&nbsp;<g:help code="period.validFrom"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="validTo"><g:msg code="period.validTo.label" default="Valid To"/></label>
            </td>
            <td class="value ${hasErrors(bean: periodInstance, field: 'validTo', 'errors')}">
                <input type="text" name="validTo" id="validTo" size="20" value="${display(bean: periodInstance, field: 'validTo', scale: 1)}"/>&nbsp;<g:help code="period.validTo"/>
            </td>
        </tr>

        <g:if test="${statusOptions}">
            <tr class="prop">
                <td class="name">
                    <label for="status"><g:msg code="period.status.label" default="Status"/></label>
                </td>
                <td class="value ${hasErrors(bean: periodInstance, field: 'status', 'errors')}">
                    <g:select id="status" name="status" from="${statusOptions}" value="${periodInstance.status}" valueMessagePrefix="period.status"/>&nbsp;<g:help code="period.status"/>
                </td>
            </tr>
        </g:if>

        </tbody>
    </table>
</div>
