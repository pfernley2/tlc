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
<%@ page import="org.grails.tlc.sys.SystemAccountType" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="system"/>
    <title><g:msg code="list" domain="systemAccountType"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemAccountType"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="systemAccountType"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="code, sectionType*, singleton, changeable, allowInvoices, allowCash, allowProvisions, allowJournals"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="systemAccountType.code.label"/>

                <th><g:msg code="systemAccountType.name.label" default="Name"/></th>

                <g:sortableColumn property="sectionType" title="Section Type" titleKey="systemAccountType.sectionType.label"/>

                <g:sortableColumn property="singleton" title="Singleton" titleKey="systemAccountType.singleton.label"/>

                <g:sortableColumn property="changeable" title="Changeable" titleKey="systemAccountType.changeable.label"/>

                <g:sortableColumn property="allowInvoices" title="Allow Invoices" titleKey="systemAccountType.allowInvoices.label"/>

                <g:sortableColumn property="allowCash" title="Allow Cash" titleKey="systemAccountType.allowCash.label"/>

                <g:sortableColumn property="allowProvisions" title="Allow Provisions" titleKey="systemAccountType.allowProvisions.label"/>

                <g:sortableColumn property="allowJournals" title="Allow Journals" titleKey="systemAccountType.allowJournals.label"/>

            </tr>
            </thead>
            <tbody>
            <g:each in="${systemAccountTypeInstanceList}" status="i" var="systemAccountTypeInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${systemAccountTypeInstance.id}">${display(bean:systemAccountTypeInstance, field:'code')}</g:link></td>

                    <td><g:msg code="systemAccountType.name.${systemAccountTypeInstance.code}" default="${systemAccountTypeInstance.name}"/></td>

                    <td><g:msg code="systemAccountType.sectionType.${systemAccountTypeInstance.sectionType}" default="${systemAccountTypeInstance.sectionType}"/></td>

                    <td>${display(bean: systemAccountTypeInstance, field: 'singleton')}</td>

                    <td>${display(bean: systemAccountTypeInstance, field: 'changeable')}</td>

                    <td>${display(bean: systemAccountTypeInstance, field: 'allowInvoices')}</td>

                    <td>${display(bean: systemAccountTypeInstance, field: 'allowCash')}</td>

                    <td>${display(bean: systemAccountTypeInstance, field: 'allowProvisions')}</td>

                    <td>${display(bean: systemAccountTypeInstance, field: 'allowJournals')}</td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${systemAccountTypeInstanceTotal}"/>
    </div>
</div>
</body>
</html>
