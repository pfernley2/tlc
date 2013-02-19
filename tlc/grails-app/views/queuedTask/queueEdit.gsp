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
    <title><g:msg code="queuedTask.sys.edit" default="Edit Task Queue Entry"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="queue"><g:msg code="queuedTask.sys.list" default="System Task Queue"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="queuedTask.sys.edit" default="Edit Task Queue Entry"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${queuedTaskInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${queuedTaskInstance}"/>
        </div>
    </g:hasErrors>
    <div class="center"><g:msg code="generic.server.time" args="${[new Date()]}" default="The current date and time on the server is ${new Date()}."/></div>
    <g:form method="post">
        <input type="hidden" name="id" value="${queuedTaskInstance?.id}"/>
        <input type="hidden" name="version" value="${queuedTaskInstance?.version}"/>
        <g:render template="queueDialog" model="[queuedTaskInstance: queuedTaskInstance]"/>
        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="queueUpdate" value="${msg(code:'default.button.update.label', 'default':'Update')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="queueDelete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
