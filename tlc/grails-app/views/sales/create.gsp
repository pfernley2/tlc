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
<%@ page import="org.grails.tlc.books.TemplateDocument" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="create" domain="templateDocument"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="templateDocument"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="create" domain="templateDocument"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${templateDocumentInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${templateDocumentInstance}"/>
        </div>
    </g:hasErrors>
    <g:render template="/system/ajax"/>
    <g:form method="post" >
        <input type="hidden" id="view" name="view" value="create"/>
        <g:render template="form" model="[templateDocumentInstance: templateDocumentInstance, documentTypeList: documentTypeList, currencyList: currencyList, settings: settings]"/>
        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="save" value="${msg(code:'default.button.create.label', 'default':'Create')}"/></span>
            <span class="button"><g:actionSubmit class="edit" action="templateLines" value="${msg(code:'document.more.lines', 'default':'More Lines')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
