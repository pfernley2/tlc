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
<%@ page import="org.grails.tlc.corp.TaxStatement" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="create" domain="taxStatement"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="taxStatement"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="create" domain="taxStatement"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${taxStatementInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${taxStatementInstance}"/>
        </div>
    </g:hasErrors>
    <g:if test="${queueNumber}">
        <g:render template="/system/submitted" model="[queueNumber: queueNumber]"/>
    </g:if>
    <g:else>
        <g:if test="${taxAccount}">
            <g:if test="${authorityList}">
                <div class="standout">
                    <g:msg code="taxStatement.text" default="Click the submit button to create the new statement."/>
                </div>
                <g:form action="save" method="post">
                    <div class="dialog">
                        <table>
                            <tbody>
                            <tr class="prop">
                                <td class="name">
                                    <label for="authority.id"><g:msg code="taxStatement.authority.label" default="Tax Authority"/></label>
                                </td>
                                <td class="value ${hasErrors(bean: revaluationInstance, field: 'period', 'errors')}">
                                    <g:select autofocus="autofocus" optionKey="id" optionValue="name" from="${authorityList}" name="authority.id" value="${taxStatementInstance.authority?.id}"/>&nbsp;<g:help code="taxStatement.authority"/>
                                </td>
                            </tr>

                            <tr class="prop">
                                <td class="name">
                                    <label for="statementDate"><g:msg code="taxStatement.statementDate.label" default="Statement Date"/></label>
                                </td>
                                <td class="value ${hasErrors(bean: revaluationInstance, field: 'account', 'errors')}">
                                    <input type="text" name="statementDate" id="statementDate" size="20" value="${display(bean:taxStatementInstance,field:'statementDate', scale: 1)}"/>&nbsp;<g:help code="taxStatement.statementDate"/>
                                </td>
                            </tr>

                            <td class="name">
                                <label for="description"><g:msg code="taxStatement.description.label" default="Description"/></label>
                            </td>
                            <td class="value ${hasErrors(bean: taxStatement, field: 'description', 'errors')}">
                                <input type="text" maxlength="50" size="45" id="description" name="description" value="${display(bean: taxStatementInstance, field: 'description')}"/>&nbsp;<g:help code="taxStatement.description"/>
                            </td>

                            <tr class="prop">
                                <td class="name">
                                    <label for="preferredStart"><g:msg code="queuedTask.demand.delay.label" default="Delay Until"/></label>
                                </td>
                                <td class="value">
                                    <input type="text" size="20" id="preferredStart" name="preferredStart" value=""/>&nbsp;<g:help code="queuedTask.demand.delay"/>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="buttons">
                        <span class="button"><input class="save" type="submit" value="${msg(code: 'generic.submit', 'default': 'Submit')}"/></span>
                    </div>
                </g:form>
            </g:if>
            <g:else>
                <div class="standout">
                    <g:msg code="taxStatement.no.authorities" default="No tax authorities are available to create statements for. A new statement can only be created if the preceding one for that authority has been finalized."/>
                </div>
            </g:else>
        </g:if>
        <g:else>
            <div class="standout">
                <g:msg code="taxStatement.no.control" default="No tax control account is defined within the General Ledger"/>
            </div>
        </g:else>
    </g:else>
</div>
</body>
</html>
