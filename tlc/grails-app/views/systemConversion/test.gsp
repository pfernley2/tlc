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
<%@ page import="org.grails.tlc.sys.SystemUnit" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="system"/>
    <title><g:msg code="conversionTest.sys.title" default="Test System Conversions"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="systemConversion"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="conversionTest.sys.title" default="Test System Conversions"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${testInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${testInstance}"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="fromUnit.id"><g:msg code="conversionTest.fromUnit.label" default="From Unit"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: testInstance, field: 'fromUnit', 'errors')}">
                        <g:domainSelect autofocus="autofocus" name="fromUnit.id" options="${SystemUnit.list()}" selected="${testInstance?.fromUnit}" prefix="unit.name" code="code" default="name"/>&nbsp;<g:help code="conversionTest.fromUnit"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="toUnit.id"><g:msg code="conversionTest.toUnit.label" default="To Unit"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: testInstance, field: 'toUnit', 'errors')}">
                        <g:domainSelect name="toUnit.id" options="${SystemUnit.list()}" selected="${testInstance?.toUnit}" prefix="unit.name" code="code" default="name"/>&nbsp;<g:help code="conversionTest.toUnit"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="quantity"><g:msg code="conversionTest.quantity.label" default="Quantity"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: testInstance, field: 'quantity', 'errors')}">
                        <input type="text" id="quantity" name="quantity" size="20" value="${display(bean: testInstance, field: 'quantity')}"/>&nbsp;<g:help code="conversionTest.quantity"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <g:msg code="conversionTest.result" default="Result"/>:
                    </td>
                    <td class="value">
                        ${testInstance.result?.encodeAsHTML()}
                    </td>
                </tr>

                </tbody>
            </table>
        </div>
        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="testing" value="${msg(code:'conversionTest.test', 'default':'Test')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
