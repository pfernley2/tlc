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
<!doctype html>
<html>
<head>
    <meta name="generator" content="company"/>
    <title><g:msg code="show" domain="taskParam"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="taskParam"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="taskParam"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="taskParam"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                    <td class="value">${display(bean: taskParamInstance, field: 'id')}</td>

                </tr>
            </g:permit>

            <tr class="prop">
                <td class="name"><g:msg code="taskParam.code.label" default="Code"/></td>

                <td class="value">${display(bean: taskParamInstance, field: 'code')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taskParam.name.label" default="Name"/></td>

                <td class="value"><g:msg code="taskParam.name.${taskParamInstance.task.code}.${taskParamInstance.code}" default="${taskParamInstance.name}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taskParam.sequencer.label" default="Sequencer"/></td>

                <td class="value">${display(bean: taskParamInstance, field: 'sequencer')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taskParam.dataType.label" default="Data Type"/></td>

                <td class="value"><g:msg code="generic.dataType.${taskParamInstance.dataType}" default="${taskParamInstance.dataType}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taskParam.dataScale.label" default="Data Scale"/></td>

                <td class="value">${display(bean: taskParamInstance, field: 'dataScale')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taskParam.defaultValue.label" default="Default Value"/></td>

                <td class="value">${display(bean: taskParamInstance, field: 'defaultValue')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taskParam.required.label" default="Required"/></td>

                <td class="value">${display(bean: taskParamInstance, field: 'required')}</td>

            </tr>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                    <td class="value">${display(bean: taskParamInstance, field: 'securityCode')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: taskParamInstance, field: 'dateCreated')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                    <td class="value">${display(bean: taskParamInstance, field: 'lastUpdated')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                    <td class="value">${display(bean: taskParamInstance, field: 'version')}</td>

                </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${taskParamInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
