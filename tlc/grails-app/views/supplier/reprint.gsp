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
    <title><g:msg code="remittance.reprint.title" default="Remittance Advice Reprint"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="remittance.reprint.title" default="Remittance Advice Reprint"/>
    <g:if test="${accessCodeList}">
        <g:if test="${flash.message}">
            <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
        </g:if>
        <g:hasErrors bean="${supplierInstance}">
            <div class="errors" role="alert">
                <g:listErrors bean="${supplierInstance}"/>
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
                            <label for="code"><g:msg code="remittance.supplier.label" default="Specific Supplier"/></label>
                        </td>
                        <td class="value ${hasErrors(bean:supplierInstance,field:'code','errors')}">
                            <input autofocus="autofocus" type="text" size="20" id="code" name="code" value="${display(bean:supplierInstance,field:'code')}"/>&nbsp;<g:help code="remittance.supplier"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="vtop name">
                            <label for="codes"><g:msg code="report.accessCode.label" default="Access Code(s)"/></label>
                        </td>
                        <td class="value ${hasErrors(bean: supplierInstance, field: 'accessCode', 'errors')}">
                            <g:domainSelect name="codes" options="${accessCodeList}" selected="${selectedCodes}" displays="name" size="10"/>&nbsp;<g:help code="report.accessCode"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="adviceDate"><g:msg code="remittance.adviceDate.label" default="Advice Date"/></label>
                        </td>
                        <td class="value ${hasErrors(bean:supplierInstance,field:'dateCreated','errors')}">
                            <input type="text" size="20" id="adviceDate" name="adviceDate" value="${statementDate?.encodeAsHTML()}"/>&nbsp;<g:help code="remittance.adviceDate"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="taxId"><g:msg code="remittance.batchSize.label" default="Batch Size"/></label>
                        </td>
                        <td class="value ${hasErrors(bean:supplierInstance,field:'taxId','errors')}">
                            <input type="text" maxlength="5" size="5" id="taxId" name="taxId" value="${display(bean:supplierInstance,field:'taxId')}"/>&nbsp;<g:help code="remittance.batchSize"/>
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
