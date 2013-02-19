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
    <title><g:msg code="system.statistics" default="Cache Statistics"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="system.statistics" default="Cache Statistics"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <th><g:msg code="system.statistics.cache" default="Cache"/></th>

                <th><g:msg code="system.statistics.max" default="Size Limit (KB)"/></th>

                <th><g:msg code="system.statistics.size" default="Current Size (KB)"/></th>

                <th><g:msg code="system.statistics.count" default="Number of Entries"/></th>

                <th><g:msg code="system.statistics.hits" default="Hit Percentage"/></th>

                <th><g:msg code="system.statistics.misses" default="Miss Percentage"/></th>

                <th><g:msg code="system.statistics.resize" default="Resize (KB)"/></th>

                <th><g:msg code="system.statistics.clear" default="Clear"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${statisticsInstanceList}" status="i" var="statisticsInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td>${statisticsInstance.code}</td>

                    <td><g:format value="${statisticsInstance.max / 1024.0}" scale="2" grouped="true"/></td>

                    <td><g:format value="${statisticsInstance.size / 1024.0}" scale="2" grouped="true"/></td>

                    <td><g:format value="${statisticsInstance.count}" scale="0" grouped="true"/></td>

                    <td><g:format value="${(statisticsInstance.hits == 0) ? 0.0 : (statisticsInstance.hits * 100.0) / (statisticsInstance.hits + statisticsInstance.misses)}" scale="2" grouped="true"/></td>

                    <td><g:format value="${(statisticsInstance.misses == 0) ? 0.0 : (statisticsInstance.misses * 100.0) / (statisticsInstance.hits + statisticsInstance.misses)}" scale="2" grouped="true"/></td>

                    <td><g:form method="post" action="resize" id="${statisticsInstance.code}"><input type="text" id="size" name="size" size="4"/>&nbsp;<input type="submit" value="${msg(code: 'system.statistics.resize.button', default: 'Resize')}"/></g:form></td>

                    <td><g:form method="post" action="clear" id="${statisticsInstance.code}"><input type="submit" value="${msg(code: 'system.statistics.clear.button', default: 'Clear')}"/></g:form></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <g:form method="post">
        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="clearAll" value="${msg(code:'system.statistics.clearAll', 'default':'Clear All')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
