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
    <meta name="layout" content="basic"/>
    <link rel="stylesheet" href="${resource(dir: 'less', file: 'main.less')}"/>
    <title><g:msg code="systemPageHelp.title" default="Page Help"/></title>
</head>
<body>
<div id="main-content" class="body" role="main">
    <g:if test="${displayInstance.lines}">
        <div class="pageHelp">
            <g:each in="${displayInstance.lines}" var="line">${line}
            </g:each>
        </div>
    </g:if>
    <g:else>
        <div class="largeTopMargin"><h2><g:msg code="systemPageHelp.no.help" default="No page help is available."/></h2></div>
    </g:else>
</div>
</body>
</html>