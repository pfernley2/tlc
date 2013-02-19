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
<!doctype html>
<html>
<head>
    <title><g:msg code="customer.statement.reprint" default="Customer Statement Reprint"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="customer.statement.reprint" default="Customer Statement Reprint"/>
    <g:if test="${accessCodeList}">
        <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
        </g:if>
        <g:hasErrors bean="${customerInstance}">
            <div class="errors" role="alert">
                <g:listErrors bean="${customerInstance}"/>
            </div>
        </g:hasErrors>
        <div class="standout">
            <g:msg code="generic.task.submit" default="Click the Submit button to run the report."/>
        </div>
        <g:form action="reprinting" method="post">
            <div class="dialog">
                <table>
                    <tbody>
                    <tr class="prop">
                        <td class="name">
                            <label for="code"><g:msg code="customer.statement.customer.label" default="Specific Customer"/></label>
                        </td>
                        <td class="value ${hasErrors(bean:customerInstance,field:'code','errors')}">
                            <input autofocus="autofocus" type="text" size="20" id="code" name="code" value="${display(bean:customerInstance,field:'code')}"/>&nbsp;<g:help code="customer.statement.customer"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="vtop name">
                            <label for="codes"><g:msg code="report.accessCode.label" default="Access Code(s)"/></label>
                        </td>
                        <td class="value ${hasErrors(bean: customerInstance, field: 'accessCode', 'errors')}">
                            <g:domainSelect name="codes" options="${accessCodeList}" selected="${selectedCodes}" displays="name" size="10"/>&nbsp;<g:help code="report.accessCode"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="statementDate"><g:msg code="customer.reprint.date.label" default="Statement Date"/></label>
                        </td>
                        <td class="value ${hasErrors(bean:customerInstance,field:'dateCreated','errors')}">
                            <input type="text" size="20" id="statementDate" name="statementDate" value="${statementDate?.encodeAsHTML()}"/>&nbsp;<g:help code="customer.reprint.date"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="taxId"><g:msg code="customer.statement.batchSize.label" default="Batch Size"/></label>
                        </td>
                        <td class="value ${hasErrors(bean:customerInstance,field:'taxId','errors')}">
                            <input type="text" maxlength="5" size="5" id="taxId" name="taxId" value="${display(bean:customerInstance,field:'taxId')}"/>&nbsp;<g:help code="customer.statement.batchSize"/>
                        </td>
                    </tr>

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
            <g:msg code="report.no.access" default="You do not have permission to access any accounts and therefore cannot run this report."/>
        </div>
    </g:else>
</div>
</body>
</html>
