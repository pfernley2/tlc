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
    <title><g:msg code="system.environment" default="Environment"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="system.environment" default="Environment"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <g:compressor>
        <table>
            <tbody>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.osName" default="Operating System Name"/></td>

                <td class="value">${environmentInstance.osName.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.osVersion" default="Operating System Version"/></td>

                <td class="value">${environmentInstance.osVersion.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.osArchitecture" default="Operating System Architecture"/></td>

                <td class="value">${environmentInstance.osArchitecture.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.javaName" default="Java Name"/></td>

                <td class="value">${environmentInstance.javaName.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.javaVendor" default="Java Vendor"/></td>

                <td class="value">${environmentInstance.javaVendor.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.javaVersion" default="Java Version"/></td>

                <td class="value">${environmentInstance.javaVersion.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.groovyVersion" default="Groovy Version"/></td>

                <td class="value">${environmentInstance.groovyVersion.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.grailsVersion" default="Grails Version"/></td>

                <td class="value">${environmentInstance.grailsVersion.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.grailsEnvironment" default="Grails Environment"/></td>

                <td class="value">${environmentInstance.grailsEnvironment.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.applicationName" default="Application Name"/></td>

                <td class="value">${environmentInstance.applicationName.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.applicationVersion" default="Application Version"/></td>

                <td class="value">${environmentInstance.applicationVersion.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.memoryUsed" default="Memory Used"/></td>

                <td class="value">${environmentInstance.memoryUsed.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.memoryFree" default="Memory Free"/></td>

                <td class="value">${environmentInstance.memoryFree.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.memoryTotal" default="Memory Total"/></td>

                <td class="value">${environmentInstance.memoryTotal.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="system.environment.memoryLimit" default="Memory Limit"/></td>

                <td class="value">${environmentInstance.memoryLimit.encodeAsHTML()}</td>

            </tr>

            </tbody>
        </table>
        </g:compressor>
    </div>
    <h1><g:msg code="system.environment.plugins" default="Installed Plugins"/></h1>
    <ul class="borderedList">
        <g:each var="plugin" in="${applicationContext.getBean('pluginManager').allPlugins.sort{it.name}}">
            <li>${plugin.name} - ${plugin.version}</li>
        </g:each>
    </ul>
</div>
</body>
</html>
