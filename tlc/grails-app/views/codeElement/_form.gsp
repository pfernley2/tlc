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

        <g:if test="${codeElementValueTotal == 0}">
            <tr class="prop">
                <td class="name">
                    <label for="elementNumber"><g:msg code="codeElement.elementNumber.label" default="Element Number"/></label>
                </td>
                <td class="value ${hasErrors(bean: codeElementInstance, field: 'elementNumber', 'errors')}">
                    <g:select autofocus="autofocus" from="${1..8}" id="elementNumber" name="elementNumber" value="${codeElementInstance?.elementNumber}"/>&nbsp;<g:help code="codeElement.elementNumber"/>
                </td>
            </tr>
        </g:if>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="codeElement.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: codeElementInstance, field: 'name', 'errors')}">
                <input ${(codeElementValueTotal > 0) ? 'autofocus="autofocus" ' : ''}type="text" maxlength="30" size="30" id="name" name="name" value="${display(bean: codeElementInstance, field: 'name')}"/>&nbsp;<g:help code="codeElement.name"/>
            </td>
        </tr>

        <g:if test="${codeElementValueTotal == 0}">
            <tr class="prop">
                <td class="name">
                    <label for="dataType"><g:msg code="codeElement.dataType.label" default="Data Type"/></label>
                </td>
                <td class="value ${hasErrors(bean: codeElementInstance, field: 'dataType', 'errors')}">
                    <g:select id="dataType" name="dataType" from="${codeElementInstance.constraints.dataType.inList}" value="${codeElementInstance.dataType}" valueMessagePrefix="codeElement.dataType"/>&nbsp;<g:help code="codeElement.dataType"/>
                </td>
            </tr>

            <tr class="prop">
                <td class="name">
                    <label for="dataLength"><g:msg code="codeElement.dataLength.label" default="Data Length"/></label>
                </td>
                <td class="value ${hasErrors(bean: codeElementInstance, field: 'dataLength', 'errors')}">
                    <g:select from="${1..10}" id="dataLength" name="dataLength" value="${codeElementInstance?.dataLength}"/>&nbsp;<g:help code="codeElement.dataLength"/>
                </td>
            </tr>
        </g:if>

        </tbody>
    </table>
</div>
