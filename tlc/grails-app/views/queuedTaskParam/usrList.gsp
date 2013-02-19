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
    <title><g:msg code="list" domain="queuedTaskParam"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:if test="${ddSource}">
        <g:pageTitle code="queuedTaskParam.list.for" args="${[ddSource.toString()]}" default="Parameter List for Queued Task ${ddSource.toString()}" returns="true"/>
    </g:if>
    <g:else>
        <g:pageTitle code="list" domain="queuedTaskParam"/>
    </g:else>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="criteria">
        <g:criteria include="value"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <th><g:msg code="queuedTaskParam.param.label" default="Parameter"/></th>

                <g:sortableColumn property="value" title="Value" titleKey="queuedTaskParam.value.label"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${queuedTaskParamInstanceList}" status="i" var="queuedTaskParamInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="usrShow" id="${queuedTaskParamInstance.id}"><g:msg code="taskParam.name.${queuedTaskParamInstance.queued.task.code}.${queuedTaskParamInstance.param.code}" default="${queuedTaskParamInstance.param.name}"/></g:link></td>

                    <td>${display(bean: queuedTaskParamInstance, field: 'value')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${queuedTaskParamInstanceTotal}"/>
    </div>
</div>
</body>
</html>
