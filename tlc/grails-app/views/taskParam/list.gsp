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
    <title><g:msg code="list" domain="taskParam"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="taskParam"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:if test="${ddSource}">
        <g:pageTitle code="list.for" domain="taskParam" forDomain="${ddSource}" value="${message(code: 'task.name.' + ddSource.code, default: ddSource.name)}" returns="true"/>
    </g:if>
    <g:else>
        <g:pageTitle code="list" domain="taskParam"/>
    </g:else>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="criteria">
        <g:criteria include="code, sequencer, dataType, dataScale, defaultValue, required"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="taskParam.code.label"/>

                <th><g:msg code="taskParam.name.label" default="Name"/></th>

                <g:sortableColumn property="sequencer" title="Sequencer" titleKey="taskParam.sequencer.label"/>

                <g:sortableColumn property="dataType" title="Data Type" titleKey="taskParam.dataType.label"/>

                <g:sortableColumn property="dataScale" title="Data Scale" titleKey="taskParam.dataScale.label"/>

                <g:sortableColumn property="defaultValue" title="Default Value" titleKey="taskParam.defaultValue.label"/>

                <g:sortableColumn property="required" title="Required" titleKey="taskParam.required.label"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${taskParamInstanceList}" status="i" var="taskParamInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${taskParamInstance.id}">${display(bean: taskParamInstance, field: 'code')}</g:link></td>

                    <td><g:msg code="taskParam.name.${taskParamInstance.task.code}.${taskParamInstance.code}" default="${taskParamInstance.name}"/></td>

                    <td>${display(bean: taskParamInstance, field: 'sequencer')}</td>

                    <td><g:msg code="generic.dataType.${taskParamInstance.dataType}" default="${taskParamInstance.dataType}"/></td>

                    <td>${display(bean: taskParamInstance, field: 'dataScale')}</td>

                    <td>${display(bean: taskParamInstance, field: 'defaultValue')}</td>

                    <td>${display(bean: taskParamInstance, field: 'required')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${taskParamInstanceTotal}"/>
    </div>
</div>
</body>
</html>
