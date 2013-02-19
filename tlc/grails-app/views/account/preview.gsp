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
<%@ page import="org.grails.tlc.books.Account" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="accounts"/>
    <title><g:msg code="account.preview" default="Bulk Account Creation Preview"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="bulk"><g:msg code="account.bulk" default="Bulk Account Creation"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="account.preview" default="Bulk Account Creation Preview"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${chartSectionInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${chartSectionInstance}"/>
        </div>
    </g:hasErrors>
    <div class="list">
        <p>&nbsp;</p>
        <p><g:msg code="account.bulk.text" default="Un-check the Create checkbox of an account to stop the creation of that particular account. You may edit the names of accounts if you wish."/></p>
        <p>&nbsp;</p>
        <g:form action="confirm" method="post">
            <input type="hidden" name="id" value="${chartSectionInstance.id}"/>
            <table>
                <thead>
                <tr>

                    <th><g:msg code="default.button.create.label" default="Create"/></th>

                    <th><g:msg code="account.code.label" default="Code"/></th>

                    <th><g:msg code="account.name.label" default="Name"/></th>

                </tr>
                </thead>
                <tbody>
                <g:each in="${accountInstanceList}" status="i" var="accountInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                        <td><g:checkBox name="${'accounts[' + i.toString() + '].active'}" value="${accountInstance.active}"/></td>

                        <td><input type="hidden" name="accounts[${i}].code" value="${display(bean: accountInstance, field: 'code')}"/>${display(bean: accountInstance, field: 'code')}</td>

                        <td><input type="text" maxlength="87" size="50" name="accounts[${i}].name" value="${display(bean:accountInstance,field:'name')}"/></td>

                    </tr>
                </g:each>
                </tbody>
            </table>
            <div class="buttons">
                <span class="button"><input class="save" type="submit" value="${msg(code: 'default.button.create.label', 'default': 'Create')}"/></span>
            </div>
        </g:form>
    </div>
</div>
</body>
</html>
