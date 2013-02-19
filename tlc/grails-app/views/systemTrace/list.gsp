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
<%@ page import="org.grails.tlc.sys.SystemTrace" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="system"/>
    <title><g:msg code="list" domain="systemTrace"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="systemTrace"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="id, dateCreated, databaseAction*, domainName, domainId, domainVersion, domainData"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="id" title="Id" titleKey="generic.id.label"/>

                <g:sortableColumn property="dateCreated" title="Date Created" titleKey="generic.dateCreated.label"/>

                <th><g:msg code="systemTrace.domainSecurityCode.label" default="Company"/></th>

                <g:sortableColumn property="databaseAction" title="Database Action" titleKey="systemTrace.databaseAction.label"/>

                <g:sortableColumn property="domainName" title="Domain Name" titleKey="systemTrace.domainName.label"/>

                <g:sortableColumn property="domainData" title="Domain Data" titleKey="systemTrace.domainData.label"/>

                <g:sortableColumn property="domainId" title="Domain Id" titleKey="systemTrace.domainId.label"/>

                <g:sortableColumn property="domainVersion" title="Domain Version" titleKey="systemTrace.domainVersion.label"/>

                <th><g:msg code="systemTrace.userId.label" default="User"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${systemTraceInstanceList}" status="i" var="systemTraceInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${systemTraceInstance.id}">${display(bean:systemTraceInstance, field:'id')}</g:link></td>

                    <td>${display(bean:systemTraceInstance, field:'dateCreated')}</td>

                    <td>${display(bean:systemTraceInstance, field:'companyDecode')}</td>

                    <td><g:msg code="systemTrace.databaseAction.${systemTraceInstance.databaseAction}" default="${systemTraceInstance.databaseAction}"/></td>

                    <td>${display(bean:systemTraceInstance, field:'domainName')}</td>

                    <td>${display(bean:systemTraceInstance, field:'domainData')}</td>

                    <td>${display(bean:systemTraceInstance, field:'domainId')}</td>

                    <td>${display(bean:systemTraceInstance, field:'domainVersion')}</td>

                    <td>${display(bean:systemTraceInstance, field:'userDecode')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${systemTraceInstanceTotal}"/>
    </div>
</div>
</body>
</html>
