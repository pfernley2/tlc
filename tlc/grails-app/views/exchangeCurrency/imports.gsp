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
<%@ page import="org.grails.tlc.corp.ExchangeCurrency; org.grails.tlc.sys.SystemCurrency" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="company"/>
    <title><g:msg code="exchangeCurrency.imports" default="Import Predefined Currency"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="exchangeCurrency"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="exchangeCurrency"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="exchangeCurrency.imports" default="Import Predefined Currency"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="center tinyTopMargin mediumBottomMargin">
        <p><g:msg code="exchangeCurrency.imports.message" default="The system has many currencies already defined for you. Use the Import Predefined Currency facility to save time and effort."/></p>
    </div>
    <g:form action="importing" method="post">
        <table>
            <tbody>

            <tr class="prop">
                <td class="name">
                    <label for="systemCurrency.id"><g:msg code="exchangeCurrency.imports.currency.label" default="Predefined Currency"/></label>
                </td>
                <td class="value ${hasErrors(bean: exchangeCurrencyInstance, field: 'code', 'errors')}">
                    <g:domainSelect autofocus="autofocus" name="systemCurrency.id" options="${SystemCurrency.list()}" prefix="currency.name" code="code" default="name"/>&nbsp;<g:help code="exchangeCurrency.imports.currency"/>
                </td>
            </tr>

            </tbody>
        </table>
        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="${msg(code: 'exchangeCurrency.imports.button', 'default': 'Import')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
