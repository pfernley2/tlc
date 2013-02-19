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
    <title><g:msg code="list" domain="account"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="account"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="account"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="code, name, active, status*, revaluationMethod*"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="account.code.label"/>

                <g:sortableColumn property="name" title="Name" titleKey="account.name.label"/>

                <th><g:msg code="account.currency.label" default="Currency"/></th>

                <g:sortableColumn property="revaluationMethod" title="Revaluation Method" titleKey="account.revaluationMethod.label"/>

                <g:sortableColumn property="status" title="Status" titleKey="account.status.label"/>

                <th><g:msg code="account.type.label" default="Type"/></th>

                <g:sortableColumn property="active" title="Active" titleKey="account.active.label"/>

                <th><g:msg code="account.section.label" default="Section"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${accountInstanceList}" status="i" var="accountInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${accountInstance.id}">${display(bean:accountInstance, field:'code')}</g:link></td>

                    <td>${display(bean:accountInstance, field:'name')}</td>

                    <td>${msg(code: 'currency.name.' + accountInstance.currency.code, default: accountInstance.currency.name)}</td>

                    <td>${accountInstance.revaluationMethod ? msg(code: 'account.revaluationMethod.' + accountInstance.revaluationMethod, default: accountInstance.revaluationMethod) : ''}</td>

                    <td><g:msg code="account.status.${accountInstance.status}" default="${accountInstance.status}"/></td>

                    <td>${accountInstance.type ? msg(code: 'systemAccountType.name.' + accountInstance.type.code, default: accountInstance.type.name) : ''}</td>

                    <td>${display(bean:accountInstance, field:'active')}</td>

                    <td>${accountInstance?.section?.name?.encodeAsHTML()}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${accountInstanceTotal}"/>
    </div>
</div>
</body>
</html>
