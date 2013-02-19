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
    <title><g:msg code="supplier.print" default="Supplier List Report"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="supplier.print" default="Supplier List Report"/>
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
        <g:form action="printing" method="post">
            <div class="dialog">
                <table>
                    <tbody>
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
                            <label for="active"><g:msg code="report.active.label" default="Active Only"/></label>
                        </td>
                        <td class="value ${hasErrors(bean: supplierInstance, field: 'active', 'errors')}">
                            <g:checkBox name="active" value="${supplierInstance?.active}"></g:checkBox>&nbsp;<g:help code="report.active"/>
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
