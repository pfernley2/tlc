
<%--
 ~   Copyright 2010-2013 Paul Fernley
 ~
 ~   This file is part of the Three Ledger Core (TLC) software
 ~   from Paul Fernley.
 ~
 ~   TLC is free software: you can redistribute it and/or modify
 ~   it under the terms of the GNU General Public License as published by
 ~   the Free Software Foundation, either version 3 of the License, or
 ~   (at your option) any later version.
 ~
 ~   TLC is distributed in the hope that it will be useful,
 ~   but WITHOUT ANY WARRANTY; without even the implied warranty of
 ~   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ~   GNU General Public License for more details.
 ~
 ~   You should have received a copy of the GNU General Public License
 ~   along with TLC. If not, see <http://www.gnu.org/licenses/>.
 --%>
<%@ page import="org.grails.tlc.rest.AgentCredential" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="company"/>
    <title><g:msg code="list" domain="agentCredential"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="agentCredential"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list.for" domain="agentCredential" forDomain="${ddSource.user}" value="${ddSource.user.name}" returns="true"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="code, secret, dateCreated, active"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="dateCreated" title="Date Created" titleKey="generic.version.label"/>

                <g:sortableColumn property="code" title="Code" titleKey="agentCredential.code.label"/>

                <g:sortableColumn property="secret" title="Secret" titleKey="agentCredential.secret.label"/>

                <g:sortableColumn property="active" title="Active" titleKey="agentCredential.active.label"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${agentCredentialInstanceList}" status="i" var="agentCredentialInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${agentCredentialInstance.id}">${display(bean:agentCredentialInstance, field:'dateCreated', scale: 2)}</g:link></td>

                    <td>${display(bean:agentCredentialInstance, field:'code')}</td>

                    <td>${display(bean:agentCredentialInstance, field:'secret')}</td>

                    <td>${display(bean:agentCredentialInstance, field:'active')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${agentCredentialInstanceTotal}"/>
    </div>
</div>
</body>
</html>
