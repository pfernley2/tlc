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
    <title><g:msg code="access.title" default="Access Denied"/></title>
</head>
<body>
<g:pageTitle code="access.title" default="Access Denied"/>
<strong><g:msg code="access.message" default="The access control system cannot complete your request. Please use the back button on your browser to continue, or select one of the links below:"/></strong>
<div id="main-content" role="main" style="margin-left:30px;">
    <p><a href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></p>
    <g:if test="${companyName()}">
        <g:link controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link>
    </g:if>
    <g:else>
        <p><g:link controller="systemUser" action="login"><g:msg code="topbar.login" default="Login"/></g:link></p>
    </g:else>
</div>
</body>
</html>
