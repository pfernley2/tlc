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
    <meta name="generator" content="system"/>
    <title><g:msg code="queuedTask.sys.show" default="System Queued Task"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="queue"><g:msg code="queuedTask.sys.list" default="System Task Queue"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="queuedTask.sys.show" default="System Queued Task"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="center mediumBottomPadding"><g:msg code="generic.server.time" args="${[new Date()]}" default="The current date and time on the server is ${new Date()}."/></div>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                <td class="value">${display(bean: queuedTaskInstance, field: 'id')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="queuedTask.submittedAt" default="Submitted At"/></td>

                <td class="value">${display(bean: queuedTaskInstance, field: 'dateCreated', scale: 2)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="task.company.label" default="Company"/></td>

                <td class="value">${queuedTaskInstance?.task?.company?.name?.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="queuedTask.task.label" default="Task"/></td>

                <td class="value"><g:msg code="task.name.${queuedTaskInstance.task.code}" default="${queuedTaskInstance.task.name}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="queuedTask.user.label" default="User"/></td>

                <td class="value">${queuedTaskInstance?.user?.name?.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="queuedTask.currentStatus.label" default="Current Status"/></td>

                <td class="value"><g:msg code="queuedTask.currentStatus.${queuedTaskInstance.currentStatus}" default="${queuedTaskInstance.currentStatus}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="queuedTask.scheduled.label" default="Scheduled"/></td>

                <td class="value">${display(bean: queuedTaskInstance, field: 'scheduled')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="queuedTask.preferredStart.label" default="Preferred Start"/></td>

                <td class="value">${display(bean: queuedTaskInstance, field: 'preferredStart', scale: 2)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="queuedTask.startedAt.label" default="Started At"/></td>

                <td class="value">${display(bean: queuedTaskInstance, field: 'startedAt', scale: 2)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="queuedTask.completedAt.label" default="Completed At"/></td>

                <td class="value">${display(bean: queuedTaskInstance, field: 'completedAt', scale: 2)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="queuedTask.completionMessage.label" default="Completion Message"/></td>

                <td class="value">${display(bean: queuedTaskInstance, field: 'completionMessage')}</td>

            </tr>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                    <td class="value">${display(bean: queuedTaskInstance, field: 'securityCode')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: queuedTaskInstance, field: 'dateCreated')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                    <td class="value">${display(bean: queuedTaskInstance, field: 'lastUpdated')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                    <td class="value">${display(bean: queuedTaskInstance, field: 'version')}</td>

                </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
        <g:if test="${queuedTaskInstance?.currentStatus == 'waiting'}">
            <div class="buttons">
            <g:form>
                <input type="hidden" name="id" value="${queuedTaskInstance?.id}"/>
                <span class="button"><g:actionSubmit class="edit" action="queueEdit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
                <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="queueDelete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
            </g:form>
            </div>
        </g:if>
        <g:elseif test="${queuedTaskInstance?.currentStatus != 'running'}">
            <div class="buttons">
            <g:form>
                <input type="hidden" name="id" value="${queuedTaskInstance?.id}"/>
                <span class="button"><g:actionSubmit class="exec" action="queueRerun" value="${msg(code:'queuedTask.rerun.button', 'default':'Rerun')}"/></span>
            </g:form>
            </div>
        </g:elseif>
</div>
</body>
</html>
