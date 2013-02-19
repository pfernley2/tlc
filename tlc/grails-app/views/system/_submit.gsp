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
    <p>&nbsp;</p>
    <div class="center">
        <g:msg code="generic.task.submit" default="Complete any parameters below that you wish to set, then press the Submit button to place this task in the queue for execution. Any results will be emailed to you when the task has been executed."/>
        <g:msg code="generic.server.time" args="${[now]}" default="The current date and time on the server is ${now}."/>
    </div>
    <p>&nbsp;</p>
    <table>
        <tbody>
        <g:each in="${parameters}" status="i" var="parameter">
            <tr class="prop">
                <td class="name">
                    <label for="${parameter.code}">${parameter.prompt.encodeAsHTML()}</label>
                </td>
                <td class="value">
                    <g:if test="${parameter.type == 'boolean'}">
                        <input ${(i == 0) ? 'autofocus="autofocus" ' : ''}type="checkbox" id="${parameter.code}" name="${parameter.code}" value="true" ${(parameter.value == 'true' ? 'checked="true"' : '')}/>
                    </g:if>
                    <g:else>
                        <input ${(i == 0) ? 'autofocus="autofocus" ' : ''}type="text" size="20" id="${parameter.code}" name="${parameter.code}" value="${parameter.value.encodeAsHTML()}"/>
                    </g:else>
                    <g:if test="${parameter.help}">
                        <g:help code="${parameter.help}"/>
                    </g:if>
                </td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
