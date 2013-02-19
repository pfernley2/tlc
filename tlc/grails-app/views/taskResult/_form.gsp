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
<%@ page import="org.grails.tlc.corp.Task" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="code"><g:msg code="taskResult.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskResultInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: taskResultInstance, field: 'code')}"/>&nbsp;<g:help code="taskResult.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="taskResult.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskResultInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="30" size="30" id="name" name="name" value="${taskResultInstance.id ? msg(code: 'taskResult.name.' + taskResultInstance.task?.code + '.' + taskResultInstance.code, default: taskResultInstance.name) : taskResultInstance.name}"/>&nbsp;<g:help code="taskResult.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="sequencer"><g:msg code="taskResult.sequencer.label" default="Sequencer"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskResultInstance, field: 'sequencer', 'errors')}">
                <input type="text" size="5" id="sequencer" name="sequencer" value="${display(bean: taskResultInstance, field: 'sequencer')}"/>&nbsp;<g:help code="taskResult.sequencer"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="dataType"><g:msg code="taskResult.dataType.label" default="Data Type"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskResultInstance, field: 'dataType', 'errors')}">
                <g:select id="dataType" name="dataType" from="${taskResultInstance.constraints.dataType.inList}" value="${taskResultInstance.dataType}" valueMessagePrefix="generic.dataType"/>&nbsp;<g:help code="taskResult.dataType"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="dataScale"><g:msg code="taskResult.dataScale.label" default="Data Scale"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskResultInstance, field: 'dataScale', 'errors')}">
                <g:select from="${1..10}" id="dataScale" name="dataScale" value="${taskResultInstance?.dataScale}" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="taskResult.dataScale"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
