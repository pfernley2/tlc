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
<%@ page import="org.grails.tlc.books.Year" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="accounts"/>
    <title><g:msg code="period.close" default="Close Old Accounting Period"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="period.close" default="Close Old Accounting Period"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:if test="${periodInstance}">
        <div class="standout">
            <g:msg code="period.to.close" args="${[periodInstance?.code]}" default="Click the submit button to close period ${periodInstance.code}."/>
        </div>
        <g:form action="closing" method="post">
            <input type="hidden" name="id" value="${periodInstance?.id}"/>
            <div class="buttons">
                <span class="button"><input class="save" type="submit" value="${msg(code: 'period.close.button', 'default': 'Close')}"/></span>
            </div>
        </g:form>
    </g:if>
    <g:else>
        <div class="standout">
            <g:msg code="period.no.close" default="No period could be found to close."/>
        </div>
    </g:else>
</div>
</body>
</html>
