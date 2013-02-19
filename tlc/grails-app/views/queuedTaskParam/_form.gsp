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
<%@ page import="org.grails.tlc.corp.TaskParam; org.grails.tlc.corp.QueuedTask" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <g:msg code="queuedTaskParam.param.label" default="Parameter"/>:
            </td>
            <td class="value">
                <g:msg code="taskParam.name.${queuedTaskParamInstance.queued.task.code}.${queuedTaskParamInstance.param.code}" default="${queuedTaskParamInstance.param.name}"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="value"><g:msg code="queuedTaskParam.value.label" default="Value"/></label>
            </td>
            <td class="value ${hasErrors(bean: queuedTaskParamInstance, field: 'value', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="200" size="30" id="value" name="value" value="${display(bean: queuedTaskParamInstance, field: 'value')}"/>&nbsp;<g:help code="queuedTaskParam.value"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
