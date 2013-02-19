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
<%@ page import="org.grails.tlc.books.Recurring" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="edit" domain="recurring"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="recurring"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="recurring"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="edit" domain="recurring"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${recurringInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${recurringInstance}"/>
        </div>
    </g:hasErrors>
    <g:render template="/system/ajax"/>
    <g:if test="${settings.isComplete}">
        <div class="center mediumBottomMargin"><g:msg code="recurring.isComplete" default="This recurring transaction has completed and cannot be edited. It is displayed for viewing purposes only."/></div>
    </g:if>
    <g:form method="post">
        <input type="hidden" id="view" name="view" value="edit"/>
        <input type="hidden" name="id" value="${recurringInstance?.id}"/>
        <input type="hidden" name="version" value="${recurringInstance?.version}"/>
        <g:render template="form" model="[recurringInstance: recurringInstance, bankAccountList: bankAccountList, documentTypeList: documentTypeList, currencyList: currencyList, settings: settings]"/>
        <g:if test="${settings.isOngoing}">
            <div class="buttons">
                <span class="button"><g:actionSubmit class="save" action="Update" value="${msg(code:'default.button.update.label', 'default':'Update')}"/></span>
                <span class="button"><g:actionSubmit class="edit" action="lines" value="${msg(code:'document.more.lines', 'default':'More Lines')}"/></span>
            </div>
        </g:if>
    </g:form>
</div>
</body>
</html>
