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
    <title><g:msg code="show" domain="task"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="task"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="task"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="task"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="center tinyBottomMargin"><g:msg code="generic.server.time" args="${[new Date()]}" default="The current date and time on the server is ${new Date()}."/></div>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                    <td class="value">${display(bean: taskInstance, field: 'id')}</td>

                </tr>
            </g:permit>

            <tr class="prop">
                <td class="name"><g:msg code="task.code.label" default="Code"/></td>

                <td class="value">${display(bean: taskInstance, field: 'code')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="task.name.label" default="Name"/></td>

                <td class="value"><g:msg code="task.name.${taskInstance.code}" default="${taskInstance.name}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="task.executable.label" default="Executable"/></td>

                <td class="value">${display(bean: taskInstance, field: 'executable')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="task.allowOnDemand.label" default="Allow On Demand"/></td>

                <td class="value">${display(bean: taskInstance, field: 'allowOnDemand')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="task.activity.label" default="Activity"/></td>

                <td class="value">${taskInstance?.activity?.code?.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="task.schedule.label" default="Schedule"/></td>

                <td class="value">${display(bean: taskInstance, field: 'schedule')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="task.nextScheduledRun.label" default="Next Scheduled Run"/></td>

                <td class="value">${display(bean: taskInstance, field: 'nextScheduledRun', scale: 2)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="task.user.label" default="User"/></td>

                <td class="value">${taskInstance?.user?.name?.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="task.retentionDays.label" default="Retention Days"/></td>

                <td class="value">${display(bean: taskInstance, field: 'retentionDays')}</td>

            </tr>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="task.systemOnly.label" default="System Only"/></td>

                    <td class="value">${display(bean: taskInstance, field: 'systemOnly')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                    <td class="value">${display(bean: taskInstance, field: 'securityCode')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: taskInstance, field: 'dateCreated')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                    <td class="value">${display(bean: taskInstance, field: 'lastUpdated')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                    <td class="value">${display(bean: taskInstance, field: 'version')}</td>

                </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${taskInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
