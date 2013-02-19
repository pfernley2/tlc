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
    <title><g:msg code="account.imported" default="Account Import Results"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="import" action="imports"><g:msg code="account.imports" default="Import Account Codes"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="account.imported" default="Account Import Results"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <th><g:msg code="account.code.label" default="Code"/></th>
                <th><g:msg code="account.name.label" default="Name"/></th>
                <th><g:msg code="account.result" default="Result"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${results}" status="i" var="result">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td>${result.code?.encodeAsHTML()}</td>
                    <td>${result.name?.encodeAsHTML()}</td>
                    <td>${result.text?.encodeAsHTML()}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
</div>
</body>
</html>
