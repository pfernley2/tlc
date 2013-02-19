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
    <title><g:msg code="runtimeLogging.set" default="Set Logging Levels"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="runtimeLogging.set" default="Set Logging Levels"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <fieldset>
            <legend><g:msg code="runtimeLogging.controller" default="Controller Loggers"/></legend>
            <g:form action="setLogLevel" method="POST">
                <span class="loggerName">
                    <g:select name="logger" from="${controllerLoggers}" optionValue="name" optionKey="logger"/></span>
                <span class="level"><g:select name="level" from="${levelList}" value="${rootLevel}"/></span>
                <span class="submitButton"><input type="submit" value="Submit"/></span>
            </g:form>
        </fieldset>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <fieldset>
            <legend><g:msg code="runtimeLogging.service" default="Service Loggers"/></legend>
            <g:form action="setLogLevel" method="POST">
                <span class="loggerName">
                    <g:select name="logger" from="${serviceLoggers}" optionValue="name" optionKey="logger"/></span>
                <span class="level"><g:select name="level" from="${levelList}" value="${rootLevel}"/></span>
                <span class="submitButton"><input type="submit" value="Submit"/></span>
            </g:form>
        </fieldset>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <fieldset>
            <legend><g:msg code="runtimeLogging.domain" default="Domain Class Loggers"/></legend>
            <g:form action="setLogLevel" method="POST">
                <span class="loggerName">
                    <g:select name="logger" from="${domainLoggers}" optionValue="name" optionKey="logger"/></span>
                <span class="level"><g:select name="level" from="${levelList}" value="${rootLevel}"/></span>
                <span class="submitButton"><input type="submit" value="Submit"/></span>
            </g:form>
        </fieldset>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <fieldset>
            <legend><g:msg code="runtimeLogging.grails" default="Grails Loggers"/></legend>
            <g:form action="setLogLevel" method="POST">
                <span class="loggerName">
                    <g:select name="logger" from="${grailsLoggers}" optionValue="name" optionKey="logger"/></span>
                <span class="level"><g:select name="level" from="${levelList}" value="${rootLevel}"/></span>
                <span class="submitButton"><input type="submit" value="Submit"/></span>
            </g:form>
        </fieldset>
        <p>&nbsp;</p>
        <p>&nbsp;</p>
        <fieldset>
            <legend><g:msg code="runtimeLogging.other" default="Third Party Loggers"/></legend>
            <g:form action="setLogLevel" method="POST">
                <span class="loggerName">
                    <g:select name="logger" from="${otherLoggers}" optionValue="name" optionKey="logger"/></span>
                <span class="level"><g:select name="level" from="${levelList}" value="${rootLevel}"/></span>
                <span class="submitButton"><input type="submit" value="Submit"/></span>
            </g:form>
        </fieldset>
    </div>
</div>
</body>
</html>
