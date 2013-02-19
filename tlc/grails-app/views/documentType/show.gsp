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
<%@ page import="org.grails.tlc.books.DocumentType" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="company"/>
    <title><g:msg code="show" domain="documentType"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="documentType"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="documentType"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="documentType"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                    <td class="value">${display(bean: documentTypeInstance, field: 'id')}</td>

                </tr>
            </g:permit>


            <tr class="prop">
                <td class="name"><g:msg code="documentType.code.label" default="Code"/></td>

                <td class="value">${display(bean: documentTypeInstance, field: 'code')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="documentType.name.label" default="Name"/></td>

                <td class="value">${display(bean: documentTypeInstance, field: 'name')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="documentType.type.label" default="Type"/></td>

                <td class="value"><g:msg code="systemDocumentType.name.${documentTypeInstance.type.code}" default="${documentTypeInstance.type.name}"/></td>

            </tr>


            <g:if test="${!['ACR', 'PRR'].contains(documentTypeInstance?.type?.code)}">
                <tr class="prop">
                    <td class="name"><g:msg code="documentType.nextSequenceNumber.label" default="Next Sequence Number"/></td>

                    <td class="value">${display(bean: documentTypeInstance, field: 'nextSequenceNumber')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="documentType.autoGenerate.label" default="Auto Generate"/></td>

                    <td class="value">${display(bean: documentTypeInstance, field: 'autoGenerate')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="documentType.allowEdit.label" default="Allow Edit"/></td>

                    <td class="value">${display(bean: documentTypeInstance, field: 'allowEdit')}</td>

                </tr>

                <g:if test="${documentTypeInstance.type.code == 'BP'}">
                    <tr>
                        <td colspan="2" class="name"><h2><g:msg code="documentType.autoPayments" default="Auto Payments"/></h2></td>
                    </tr>

                    <tr class="prop">
                        <td class="name"><g:msg code="documentType.autoBankAccount.label" default="Bank Account"/></td>

                        <td class="value">${display(value: documentTypeInstance.autoBankAccount?.name)}</td>

                    </tr>

                    <tr class="prop">
                        <td class="name"><g:msg code="documentType.autoForeignCurrency.label" default="Foreign Currency"/></td>

                        <td class="value">
                            <g:if test="${documentTypeInstance.autoBankAccount}">
                                ${display(value: documentTypeInstance.autoForeignCurrency)}
                            </g:if>
                        </td>

                    </tr>

                    <tr class="prop">
                        <td class="name"><g:msg code="documentType.autoMaxPayees.label" default="Maximum Payees"/></td>

                        <td class="value">${display(value: documentTypeInstance.autoMaxPayees)}</td>

                    </tr>

                    <tr class="prop">
                        <td class="name"><g:msg code="documentType.autoBankDetails.label" default="Bank Details"/></td>

                        <td class="value">
                            <g:if test="${documentTypeInstance.autoBankAccount}">
                                ${display(value: documentTypeInstance.autoBankDetails)}
                            </g:if>
                        </td>

                    </tr>
                </g:if>
            </g:if>


            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                    <td class="value">${display(bean: documentTypeInstance, field: 'securityCode')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: documentTypeInstance, field: 'dateCreated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                    <td class="value">${display(bean: documentTypeInstance, field: 'lastUpdated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                    <td class="value">${display(bean: documentTypeInstance, field: 'version')}</td>

                </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${documentTypeInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
