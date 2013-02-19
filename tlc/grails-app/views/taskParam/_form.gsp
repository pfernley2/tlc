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
                <label for="code"><g:msg code="taskParam.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskParamInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: taskParamInstance, field: 'code')}"/>&nbsp;<g:help code="taskParam.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="taskParam.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskParamInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="30" size="30" id="name" name="name" value="${taskParamInstance.id ? msg(code: 'taskParam.name.' + taskParamInstance.task?.code + '.' + taskParamInstance.code, default: taskParamInstance.name) : taskParamInstance.name}"/>&nbsp;<g:help code="taskParam.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="sequencer"><g:msg code="taskParam.sequencer.label" default="Sequencer"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskParamInstance, field: 'sequencer', 'errors')}">
                <input type="text" size="5" id="sequencer" name="sequencer" value="${display(bean: taskParamInstance, field: 'sequencer')}"/>&nbsp;<g:help code="taskParam.sequencer"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="dataType"><g:msg code="taskParam.dataType.label" default="Data Type"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskParamInstance, field: 'dataType', 'errors')}">
                <g:select id="dataType" name="dataType" from="${taskParamInstance.constraints.dataType.inList}" value="${taskParamInstance.dataType}" valueMessagePrefix="generic.dataType"/>&nbsp;<g:help code="taskParam.dataType"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="dataScale"><g:msg code="taskParam.dataScale.label" default="Data Scale"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskParamInstance, field: 'dataScale', 'errors')}">
                <g:select from="${1..10}" id="dataScale" name="dataScale" value="${taskParamInstance?.dataScale}" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="taskParam.dataScale"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="defaultValue"><g:msg code="taskParam.defaultValue.label" default="Default Value"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskParamInstance, field: 'defaultValue', 'errors')}">
                <input type="text" maxlength="100" size="20" id="defaultValue" name="defaultValue" value="${display(bean: taskParamInstance, field: 'defaultValue')}"/>&nbsp;<g:help code="taskParam.defaultValue"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="required"><g:msg code="taskParam.required.label" default="Required"/></label>
            </td>
            <td class="value ${hasErrors(bean: taskParamInstance, field: 'required', 'errors')}">
                <g:checkBox name="required" value="${taskParamInstance?.required}"></g:checkBox>&nbsp;<g:help code="taskParam.required"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
