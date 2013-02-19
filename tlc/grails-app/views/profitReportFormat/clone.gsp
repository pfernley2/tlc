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
    <title><g:msg code="profitReportFormat.clone" default="Clone Report Format"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="profitReportFormat"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="profitReportFormat.clone" default="Clone Report Format"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${profitReportFormatInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${profitReportFormatInstance}"/>
        </div>
    </g:hasErrors>
    <g:form action="cloning" method="post" >
        <input type="hidden" name="id" value="${profitReportFormatInstance?.id}"/>
        <div class="dialog">
            <table>
                <tbody>
                <tr class="prop">
                    <td class="name">
                        <label for="name"><g:msg code="profitReportFormat.name.label" default="Name"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: profitReportFormatInstance, field: 'name', 'errors')}">
                        <input autofocus="autofocus" type="text" maxlength="30" size="30" id="name" name="name" value="${display(bean: profitReportFormatInstance, field: 'name')}"/>&nbsp;<g:help code="profitReportFormat.name"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="${msg(code:'default.button.create.label', 'default':'Create')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
