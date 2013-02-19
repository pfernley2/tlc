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
                <label for="type"><g:msg code="chartSectionRange.type.label" default="Type"/></label>
            </td>
            <td class="value ${hasErrors(bean:chartSectionRangeInstance,field:'type','errors')}">
                <g:select autofocus="autofocus" id="type" name="type" from="${chartSectionRangeInstance.constraints.type.inList}" value="${chartSectionRangeInstance.type}" valueMessagePrefix="chartSectionRange.type"/>&nbsp;<g:help code="chartSectionRange.type"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="rangeFrom"><g:msg code="chartSectionRange.rangeFrom.label" default="Range From"/></label>
            </td>
            <td class="value ${hasErrors(bean:chartSectionRangeInstance,field:'rangeFrom','errors')}">
                <input type="text" maxlength="87" size="30" id="rangeFrom" name="rangeFrom" value="${display(bean:chartSectionRangeInstance,field:'rangeFrom')}"/>&nbsp;<g:help code="chartSectionRange.rangeFrom"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="rangeTo"><g:msg code="chartSectionRange.rangeTo.label" default="Range To"/></label>
            </td>
            <td class="value ${hasErrors(bean:chartSectionRangeInstance,field:'rangeTo','errors')}">
                <input type="text" maxlength="87" size="30" id="rangeTo" name="rangeTo" value="${display(bean:chartSectionRangeInstance,field:'rangeTo')}"/>&nbsp;<g:help code="chartSectionRange.rangeTo"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="comment"><g:msg code="chartSectionRange.comment.label" default="Comment"/></label>
            </td>
            <td class="value ${hasErrors(bean:chartSectionRangeInstance,field:'comment','errors')}">
                <input type="text" maxlength="30" size="30" id="comment" name="comment" value="${display(bean:chartSectionRangeInstance,field:'comment')}"/>&nbsp;<g:help code="chartSectionRange.comment"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="messageText"><g:msg code="chartSectionRange.messageText.label" default="Message Text"/></label>
            </td>
            <td class="value ${hasErrors(bean:chartSectionRangeInstance,field:'messageText','errors')}">
                <input type="text" maxlength="100" size="30" id="messageText" name="messageText" value="${display(bean:chartSectionRangeInstance,field:'messageText')}"/>&nbsp;<g:help code="chartSectionRange.messageText"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
