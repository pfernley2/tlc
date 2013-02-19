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
    <title><g:msg code="queuedTask.sys.list" default="System Task Queue"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="queuedTask.sys.list" default="System Task Queue"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="center"><g:msg code="generic.server.time" args="${[new Date()]}" default="The current date and time on the server is ${new Date()}."/>
    <g:if test="${queueStatus}">
        <g:msg code="queuedTask.queue.status" args="${[queueStatus.started, queueStatus.statusText, queueStatus.size, queueStatus.active, queueStatus.interval]}" default="The queue is currently ${queueStatus.status}"/>
        <g:if test="${queueStatus.scanned}">
            <g:msg code="queuedTask.sys.scan" args="${[queueStatus.scanned]}" default="The last scan was performed at ${queueStatus.scanned}."/>
        </g:if>
        <g:else>
            <g:msg code="queuedTask.sys.no.scan" default="The queue has not yet been scanned."/>
        </g:else>
        <g:msg code="queuedTask.sys.snooze" args="${[queueStatus.snooze, queueStatus.delay]}" default="The inter-scan snooze interval is set to ${queueStatus.scanned} seconds and the initial scan delay was ${queueStatus.delay} seconds."/>
        <g:if test="${queueStatus.goodCount}">
            <g:msg code="queuedTask.sys.good" args="${[queueStatus.goodCount, queueStatus.goodTime]}" default="${queueStatus.goodCount} task(s) have completed successfully since the queue was started, the last one at ${queueStatus.goodTime}."/>
        </g:if>
        <g:else>
            <g:msg code="queuedTask.sys.no.good" default="No tasks have completed successfully since the queue was started."/>
        </g:else>
        <g:if test="${queueStatus.badCount}">
            <g:msg code="queuedTask.sys.bad" args="${[queueStatus.badCount, queueStatus.badTime]}" default="${queueStatus.badCount} task(s) have failed since the queue was started, the last one at ${queueStatus.badTime}."/>
        </g:if>
        <g:else>
            <g:msg code="queuedTask.sys.no.bad" default="No tasks have failed since the queue was started."/>
        </g:else>
    </g:if>
    <g:else>
        <g:msg code="queuedTask.queue.status.missing" default="Currently there is no task queue executor available. Please notify the system supervisor."/>
    </g:else>
    </div>
    <div class="criteria">
        <g:criteria include="dateCreated, currentStatus*, scheduled, preferredStart, startedAt, completedAt, completionMessage"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="dateCreated" title="Submitted At" titleKey="queuedTask.submittedAt"/>

                <th><g:msg code="task.company.label" default="Company"/></th>

                <th><g:msg code="queuedTask.task.label" default="Task"/></th>

                <th><g:msg code="queuedTask.user.label" default="User"/></th>

                <th><g:msg code="queuedTask.currentStatus.label" default="Current Status"/></th>

                <g:sortableColumn property="scheduled" title="Scheduled" titleKey="queuedTask.scheduled.label"/>

                <g:sortableColumn property="preferredStart" title="Preferred Start" titleKey="queuedTask.preferredStart.label"/>

                <g:sortableColumn property="startedAt" title="Started At" titleKey="queuedTask.startedAt.label"/>

                <g:sortableColumn property="completedAt" title="Completed At" titleKey="queuedTask.completedAt.label"/>

                <g:sortableColumn property="completionMessage" title="Completion Message" titleKey="queuedTask.completionMessage.label"/>

                <th><g:msg code="queuedTask.parameters.label" default="Parameters"/></th>

                <th><g:msg code="queuedTask.results.label" default="Results"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${queuedTaskInstanceList}" status="i" var="queuedTaskInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="queueShow" id="${queuedTaskInstance.id}">${display(bean: queuedTaskInstance, field: 'dateCreated', scale: 2)}</g:link></td>

                    <td>${queuedTaskInstance?.task?.company?.name?.encodeAsHTML()}</td>

                    <td><g:msg code="task.name.${queuedTaskInstance.task.code}" default="${queuedTaskInstance.task.name}"/></td>

                    <td>${queuedTaskInstance?.user?.name?.encodeAsHTML()}</td>

                    <td><g:msg code="queuedTask.currentStatus.${queuedTaskInstance.currentStatus}" default="${queuedTaskInstance.currentStatus}"/></td>

                    <td>${display(bean: queuedTaskInstance, field: 'scheduled')}</td>

                    <td>${display(bean: queuedTaskInstance, field: 'preferredStart', scale: 2)}</td>

                    <td>${display(bean: queuedTaskInstance, field: 'startedAt', scale: 2)}</td>

                    <td>${display(bean: queuedTaskInstance, field: 'completedAt', scale: 2)}</td>

                    <td>${display(bean: queuedTaskInstance, field: 'completionMessage')}</td>

                    <td><g:drilldown controller="queuedTaskParam" action="queueList" value="${queuedTaskInstance.id}"/></td>

                    <td><g:drilldown controller="queuedTaskResult" action="queueList" value="${queuedTaskInstance.id}"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${queuedTaskInstanceTotal}"/>
    </div>
    <g:if test="queueStatus">
        <div class="buttons">
            <g:form>
                <g:if test="${(queueStatus.status == 'stopped' || queueStatus.status == 'halted')}">
                    <span class="button"><g:actionSubmit class="start" action="start" value="${msg(code:'queuedTask.sys.start', 'default':'Start')}"/></span>
                </g:if>
                <g:elseif test="${queueStatus.status == 'paused'}">
                    <span class="button"><g:actionSubmit class="start" action="start" value="${msg(code:'queuedTask.sys.start', 'default':'Start')}"/></span>
                    <span class="button"><g:actionSubmit class="stop" action="stop" value="${msg(code:'queuedTask.sys.stop', 'default':'Stop')}"/></span>
                    <g:select name="newSize" from="${queueStatus.sizeRange}" value="${queueStatus.size}"/>
                    <span class="button"><g:actionSubmit class="resize" action="resize" value="${msg(code:'queuedTask.sys.resize', 'default':'Resize')}"/></span>
                </g:elseif>
                <g:else>
                    <span class="button"><g:actionSubmit class="pause" action="pause" value="${msg(code:'queuedTask.sys.pause', 'default':'Pause')}"/></span>
                    <span class="button"><g:actionSubmit class="stop" action="stop" value="${msg(code:'queuedTask.sys.stop', 'default':'Stop')}"/></span>
                    <g:select name="newSize" from="${queueStatus.sizeRange}" value="${queueStatus.size}"/>
                    <span class="button"><g:actionSubmit class="resize" action="resize" value="${msg(code:'queuedTask.sys.resize', 'default':'Resize')}"/></span>
                </g:else>
            </g:form>
        </div>
    </g:if>
</div>
</body>
</html>
