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
<%@ page import="org.grails.tlc.books.ChartSectionRange" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="accounts"/>
    <title><g:msg code="account.bulk" default="Bulk Account Creation"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="account.bulk" default="Bulk Account Creation"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${chartSectionRangeInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${chartSectionRangeInstance}"/>
        </div>
    </g:hasErrors>
    <g:if test="${chartSectionList}">
        <p>&nbsp;</p>
        <p><g:msg code="account.bulk.limit" args="${[limit]}" default="Note that there is a limit of ${limit} accounts that can be created in any one go. You may re-run bulk creation multiple times with the same criteria if you need more than this number of accounts creating in one section."/></p>
        <p>&nbsp;</p>
        <g:form action="preview" method="post">
            <div class="dialog">
                <table>
                    <tbody>
                    <tr class="prop">
                        <td class="name">
                            <label for="section.id"><g:msg code="account.bulk.section.label" default="Section"/></label>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionRangeInstance, field: 'section', 'errors')}">
                            <g:domainSelect autofocus="autofocus" name="section.id" options="${chartSectionList}" selected="${chartSectionRangeInstance.section}" displays="name"/>&nbsp;<g:help code="account.bulk.section"/>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <label for="rangeFrom"><g:msg code="account.bulk.rangeFrom.label" default="Range From"/></label>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionRangeInstance, field: 'rangeFrom', 'errors')}">
                            <input type="text" maxlength="87" size="30" id="rangeFrom" name="rangeFrom" value="${display(bean: chartSectionRangeInstance, field: 'rangeFrom')}"/>&nbsp;<g:help code="account.bulk.rangeFrom"/>
                        </td>
                    </tr>
                    <tr class="prop">
                        <td class="name">
                            <label for="rangeTo"><g:msg code="account.bulk.rangeTo.label" default="Range To"/></label>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionRangeInstance, field: 'rangeTo', 'errors')}">
                            <input type="text" maxlength="87" size="30" id="rangeTo" name="rangeTo" value="${display(bean: chartSectionRangeInstance, field: 'rangeTo')}"/>&nbsp;<g:help code="account.bulk.rangeTo"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <span class="button"><input class="save" type="submit" value="${msg(code: 'account.bulk.button', 'default': 'Preview')}"/></span>
            </div>
        </g:form>
    </g:if>
    <g:else>
        <h2><g:msg code="account.no.sections" default="No sections found with ranges defined"/></h2>
    </g:else>
</div>
</body>
</html>
