<%--
 ~   Copyright 2010-2013 Paul Fernley
 ~
 ~   This file is part of the Three Ledger Core (TLC) software
 ~   from Paul Fernley.
 ~
 ~   TLC is free software: you can redistribute it and/or modify
 ~   it under the terms of the GNU General Public License as published by
 ~   the Free Software Foundation, either version 3 of the License, or
 ~   (at your option) any later version.
 ~
 ~   TLC is distributed in the hope that it will be useful,
 ~   but WITHOUT ANY WARRANTY; without even the implied warranty of
 ~   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 ~   GNU General Public License for more details.
 ~
 ~   You should have received a copy of the GNU General Public License
 ~   along with TLC. If not, see <http://www.gnu.org/licenses/>.
 --%>
<%@ page import="org.grails.tlc.books.ProfitReportFormat" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="accounts"/>
    <title><g:msg code="create" domain="profitReportFormat"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="profitReportFormat"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="create" domain="profitReportFormat"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${profitReportFormatInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${profitReportFormatInstance}"/>
        </div>
    </g:hasErrors>
    <g:form method="post" >
        <input type="hidden" id="linesClicked" name="linesClicked" value=""/>
        <g:render template="form" model="[profitReportFormatInstance: profitReportFormatInstance]"/>
        <div class="buttons">
            <span class="button"><g:actionSubmit tabindex="5000" class="save" action="Save" value="${msg(code:'default.button.create.label', 'default':'Create')}"/></span>
            <span class="button"><g:actionSubmit tabindex="5500" class="save" onclick="return linesNext()" action="Save" value="${msg(code:'profitReportFormat.lines.label', 'default':'Lines')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
