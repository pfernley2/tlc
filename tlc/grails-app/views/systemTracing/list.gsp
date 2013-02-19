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
<%@ page import="org.grails.tlc.sys.SystemTracing" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="system"/>
    <title><g:msg code="list" domain="systemTracing"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemTracing"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="systemTracing"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="domainName, insertSecurityCode*, updateSecurityCode*, deleteSecurityCode*, insertRetentionDays, updateRetentionDays, deleteRetentionDays, systemOnly"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="domainName" title="Domain Name" titleKey="systemTracing.domainName.label"/>

                <th><g:msg code="systemTracing.insertSecurityCode.label" default="Insert Trace Setting"/></th>

                <th><g:msg code="systemTracing.updateSecurityCode.label" default="Update Trace Setting"/></th>

                <th><g:msg code="systemTracing.deleteSecurityCode.label" default="Delete Trace Setting"/></th>

                <g:sortableColumn property="insertRetentionDays" title="Insert Retention Days" titleKey="systemTracing.insertRetentionDays.label"/>

                <g:sortableColumn property="updateRetentionDays" title="Update Retention Days" titleKey="systemTracing.updateRetentionDays.label"/>

                <g:sortableColumn property="deleteRetentionDays" title="Delete Retention Days" titleKey="systemTracing.deleteRetentionDays.label"/>

                <g:sortableColumn property="systemOnly" title="System Only" titleKey="systemTracing.systemOnly.label"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${systemTracingInstanceList}" status="i" var="systemTracingInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${systemTracingInstance.id}">${display(bean:systemTracingInstance, field:'domainName')}</g:link></td>

                    <td>${display(bean:systemTracingInstance, field:'insertDecode')}</td>

                    <td>${display(bean:systemTracingInstance, field:'updateDecode')}</td>

                    <td>${display(bean:systemTracingInstance, field:'deleteDecode')}</td>

                    <td>${display(bean:systemTracingInstance, field:'insertRetentionDays')}</td>

                    <td>${display(bean:systemTracingInstance, field:'updateRetentionDays')}</td>

                    <td>${display(bean:systemTracingInstance, field:'deleteRetentionDays')}</td>

                    <td>${display(bean:systemTracingInstance, field:'systemOnly')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${systemTracingInstanceTotal}"/>
    </div>
</div>
</body>
</html>
