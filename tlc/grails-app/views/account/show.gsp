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
<%@ page import="org.grails.tlc.books.Account" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="accounts"/>
    <title><g:msg code="show" domain="account"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="account"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="account"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="account"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                    <td class="value">${display(bean: accountInstance, field: 'id')}</td>

                </tr>
            </g:permit>


            <tr class="prop">
                <td class="name"><g:msg code="account.code.label" default="Code"/></td>

                <td class="value">${display(bean: accountInstance, field: 'code')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="account.name.label" default="Name"/></td>

                <td class="value">${display(bean: accountInstance, field: 'name')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="account.currency.label" default="Currency"/></td>

                <td class="value">${msg(code: 'currency.name.' + accountInstance.currency.code, default: accountInstance.currency.name)}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="account.revaluationAccount.label" default="Revaluation Account"/></td>

                <td class="value">${accountInstance.revaluationAccount ? (accountInstance.revaluationAccount.code + ' '+ accountInstance.revaluationAccount.name).encodeAsHTML() : ''}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="account.revaluationMethod.label" default="Revaluation Method"/></td>

                <td class="value">${accountInstance.revaluationMethod ? msg(code: 'account.revaluationMethod.' + accountInstance.revaluationMethod, default: accountInstance.revaluationMethod) : ''}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="account.status.label" default="Status"/></td>

                <td class="value">${msg(code: 'account.status.' + accountInstance.status, default: accountInstance.status)}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="account.type.label" default="Type"/></td>

                <td class="value">${accountInstance.type ? msg(code: 'systemAccountType.name.' + accountInstance.type.code, default: accountInstance.type.name) : ''}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="account.active.label" default="Active"/></td>

                <td class="value">${display(bean: accountInstance, field: 'active')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="account.section.label" default="Section"/></td>

                <td class="value">${accountInstance?.section?.name?.encodeAsHTML()}</td>

            </tr>



            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="account.element1.label" default="Element 1"/></td>

                    <td class="value">${accountInstance?.element1?.encodeAsHTML()}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="account.element2.label" default="Element 2"/></td>

                    <td class="value">${accountInstance?.element2?.encodeAsHTML()}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="account.element3.label" default="Element 3"/></td>

                    <td class="value">${accountInstance?.element3?.encodeAsHTML()}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="account.element4.label" default="Element 4"/></td>

                    <td class="value">${accountInstance?.element4?.encodeAsHTML()}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="account.element5.label" default="Element 5"/></td>

                    <td class="value">${accountInstance?.element5?.encodeAsHTML()}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="account.element6.label" default="Element 6"/></td>

                    <td class="value">${accountInstance?.element6?.encodeAsHTML()}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="account.element7.label" default="Element 7"/></td>

                    <td class="value">${accountInstance?.element7?.encodeAsHTML()}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="account.element8.label" default="Element 8"/></td>

                    <td class="value">${accountInstance?.element8?.encodeAsHTML()}</td>

                </tr>


                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                    <td class="value">${display(bean: accountInstance, field: 'securityCode')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: accountInstance, field: 'dateCreated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                    <td class="value">${display(bean: accountInstance, field: 'lastUpdated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                    <td class="value">${display(bean: accountInstance, field: 'version')}</td>

                </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${accountInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <g:if test="${!hasTransactions}">
                <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
            </g:if>
        </g:form>
    </div>
</div>
</body>
</html>
