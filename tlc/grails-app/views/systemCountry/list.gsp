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
<%@ page import="org.grails.tlc.sys.SystemCountry" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="system"/>
    <title><g:msg code="list" domain="systemCountry"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemCountry"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:if test="${ddSource}">
        <g:pageTitle code="list.for" domain="systemCountry" forDomain="${ddSource}" value="${message(code: 'region.name.' + ddSource.code, default: ddSource.name)}" returns="true"/>
    </g:if>
    <g:else>
        <g:pageTitle code="list" domain="systemCountry"/>
    </g:else>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="code, flag"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="systemCountry.code.label"/>

                <th><g:msg code="systemCountry.name.label" default="Name"/></th>

                <g:sortableColumn property="flag" title="Flag" titleKey="systemCountry.flag.label"/>

                <th><g:msg code="systemCountry.currency.label" default="Currency"/></th>

                <th><g:msg code="systemCountry.language.label" default="Language"/></th>

                <th><g:msg code="systemCountry.region.label" default="Region"/></th>

                <th><g:msg code="systemCountry.addressFormat.label" default="Address Format"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${systemCountryInstanceList}" status="i" var="systemCountryInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${systemCountryInstance.id}">${display(bean: systemCountryInstance, field: 'code')}</g:link></td>

                    <td><g:msg code="country.name.${systemCountryInstance.code}" default="${systemCountryInstance.name}"/></td>

                    <td>${display(bean: systemCountryInstance, field: 'flag')}</td>

                    <td><g:msg code="currency.name.${systemCountryInstance.currency.code}" default="${systemCountryInstance.currency.name}"/></td>

                    <td><g:msg code="language.name.${systemCountryInstance.language.code}" default="${systemCountryInstance.language.name}"/></td>

                    <td><g:msg code="region.name.${systemCountryInstance.region.code}" default="${systemCountryInstance.region.name}"/></td>

                    <td><g:msg code="systemAddressFormat.name.${systemCountryInstance.addressFormat.code}" default="${systemCountryInstance.addressFormat.name}"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${systemCountryInstanceTotal}"/>
    </div>
</div>
</body>
</html>
