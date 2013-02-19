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
<%@ page import="org.grails.tlc.sys.SystemAccountType" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="system"/>
    <title><g:msg code="show" domain="systemAccountType"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="systemAccountType"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemAccountType"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="systemAccountType"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
            <tr class="prop">
                <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'id')}</td>

            </tr>
            </g:permit>


            <tr class="prop">
                <td class="name"><g:msg code="systemAccountType.code.label" default="Code"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'code')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="systemAccountType.name.label" default="Name"/></td>

                <td class="value"><g:msg code="systemAccountType.name.${systemAccountTypeInstance.code}" default="${systemAccountTypeInstance.name}"/></td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="systemAccountType.sectionType.label" default="Section Type"/></td>

                <td class="value"><g:msg code="systemAccountType.sectionType.${systemAccountTypeInstance.sectionType}" default="${systemAccountTypeInstance.sectionType}"/></td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="systemAccountType.singleton.label" default="Singleton"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'singleton')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemAccountType.changeable.label" default="Changeable"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'changeable')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemAccountType.allowInvoices.label" default="Allow Invoices"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'allowInvoices')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemAccountType.allowCash.label" default="Allow Cash"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'allowCash')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemAccountType.allowProvisions.label" default="Allow Provisions"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'allowProvisions')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemAccountType.allowJournals.label" default="Allow Journals"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'allowJournals')}</td>

            </tr>


            <g:permit activity="sysadmin">
            <tr class="prop">
                <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'securityCode')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'dateCreated')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'lastUpdated')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                <td class="value">${display(bean:systemAccountTypeInstance, field:'version')}</td>

            </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${systemAccountTypeInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
