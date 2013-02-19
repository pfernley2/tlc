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
    <title><g:msg code="runtimeLogging.changed" default="Logging Level Changed"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="index"><g:msg code="runtimeLogging.set" default="Set Logging Levels"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="runtimeLogging.changed" default="Logging Level Changed"/>
    <p>&nbsp;</p>
    <g:msg code="runtimeLogging.setting" args="${[logger, level]}" default="Logger ${logger} set to level ${level}"/>
    <p>&nbsp;</p>
    <p>&nbsp;</p>
    <g:pageTitle code="runtimeLogging.equivalent" default="Config.groovy equivalent"/>
    <p>&nbsp;</p>
    <g:msg code="runtimeLogging.update" default="Update the log4j section in Config.groovy to achieve the same effect permanently"/>:<br/><br/>
    <p>
    <pre>
        log4j = {
            ...
            ${level.toString().toLowerCase()}    '${logger}'
            ...
        }
    </pre>
</p>
</div>
</body>
</html>