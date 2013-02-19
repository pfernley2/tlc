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
    <title><g:msg code="list" domain="taskResult"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="taskResult"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:if test="${ddSource}">
        <g:pageTitle code="list.for" domain="taskResult" forDomain="${ddSource}" value="${message(code: 'task.name.' + ddSource.code, default: ddSource.name)}" returns="true"/>
    </g:if>
    <g:else>
        <g:pageTitle code="list" domain="taskResult"/>
    </g:else>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="criteria">
        <g:criteria include="code, sequencer, dataType, dataScale"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="taskResult.code.label"/>

                <th><g:msg code="taskResult.name.label" default="Name"/></th>

                <g:sortableColumn property="sequencer" title="Sequencer" titleKey="taskResult.sequencer.label"/>

                <g:sortableColumn property="dataType" title="Data Type" titleKey="taskResult.dataType.label"/>

                <g:sortableColumn property="dataScale" title="Data Scale" titleKey="taskResult.dataScale.label"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${taskResultInstanceList}" status="i" var="taskResultInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${taskResultInstance.id}">${display(bean: taskResultInstance, field: 'code')}</g:link></td>

                    <td><g:msg code="taskResult.name.${taskResultInstance.task.code}.${taskResultInstance.code}" default="${taskResultInstance.name}"/></td>

                    <td>${display(bean: taskResultInstance, field: 'sequencer')}</td>

                    <td><g:msg code="generic.dataType.${taskResultInstance.dataType}" default="${taskResultInstance.dataType}"/></td>

                    <td>${display(bean: taskResultInstance, field: 'dataScale')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${taskResultInstanceTotal}"/>
    </div>
</div>
</body>
</html>
