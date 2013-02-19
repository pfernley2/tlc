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
<html>
<head>
    <meta name="generator" content="system"/>
    <title><g:msg code="show" domain="systemTrace"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="systemTrace"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="systemTrace"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                <td class="value">${display(bean:systemTraceInstance, field:'id')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="systemTrace.domainSecurityCode.label" default="Company"/></td>

                <td class="value">${display(bean:systemTraceInstance, field:'companyDecode')}</td>

            </tr>


            <tr class="prop">
                <td class="name"><g:msg code="systemTrace.databaseAction.label" default="Database Action"/></td>

                <td class="value"><g:msg code="systemTrace.databaseAction.${systemTraceInstance.databaseAction}" default="${systemTraceInstance.databaseAction}"/></td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="systemTrace.domainName.label" default="Domain Name"/></td>

                <td class="value">${display(bean:systemTraceInstance, field:'domainName')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="systemTrace.domainData.label" default="Domain Data"/></td>

                <td class="value">${display(bean:systemTraceInstance, field:'domainData')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="systemTrace.domainId.label" default="Domain Id"/></td>

                <td class="value">${display(bean:systemTraceInstance, field:'domainId')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="systemTrace.domainVersion.label" default="Domain Version"/></td>

                <td class="value">${display(bean:systemTraceInstance, field:'domainVersion')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="systemTrace.userId.label" default="User"/></td>

                <td class="value">${display(bean:systemTraceInstance, field:'userDecode')}</td>

            </tr>


            <tr class="prop">
                <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                <td class="value">${display(bean:systemTraceInstance, field:'securityCode')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                <td class="value">${display(bean:systemTraceInstance, field:'dateCreated')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                <td class="value">${display(bean:systemTraceInstance, field:'lastUpdated')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                <td class="value">${display(bean:systemTraceInstance, field:'version')}</td>

            </tr>

            </tbody>
        </table>
    </div>
</div>
</body>
</html>
