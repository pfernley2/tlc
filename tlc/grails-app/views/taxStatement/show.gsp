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
    <title><g:msg code="show" domain="taxStatement"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="taxStatement"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="taxStatement"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="taxStatement"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                    <td class="value">${display(bean: taxStatementInstance, field: 'id')}</td>

                </tr>
            </g:permit>

            <tr class="prop">
                <td class="name"><g:msg code="taxStatement.authority.label" default="Authority"/></td>

                <td class="value">${taxStatementInstance?.authority?.name?.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taxStatement.statementDate.label" default="Statement Date"/></td>

                <td class="value">${display(bean: taxStatementInstance, field: 'statementDate', scale: 1)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taxStatement.description.label" default="Description"/></td>

                <td class="value">${display(bean: taxStatementInstance, field: 'description')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taxStatement.document.label" default="Document"/></td>

                <g:if test="${taxStatementInstance.finalized}">
                    <td class="value">${taxStatementInstance.document ? (taxStatementInstance.document.type.code + taxStatementInstance.document.code).encodeAsHTML() : msg(code: 'generic.not.applicable', default: 'n/a')}</td>
                </g:if>
                <g:else>
                    <td class="value"></td>
                </g:else>

            </tr>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                    <td class="value">${display(bean: taxStatementInstance, field: 'securityCode')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: taxStatementInstance, field: 'dateCreated')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                    <td class="value">${display(bean: taxStatementInstance, field: 'lastUpdated')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                    <td class="value">${display(bean: taxStatementInstance, field: 'version')}</td>

                </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${taxStatementInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <g:if test="${!taxStatementInstance.finalized}">
                <span class="button"><g:actionSubmit class="delete" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
            </g:if>
        </g:form>
    </div>
</div>
</body>
</html>
