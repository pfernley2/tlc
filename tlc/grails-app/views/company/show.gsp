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
    <meta name="generator" content="system"/>
    <title><g:msg code="show" domain="company"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="company"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="company"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="company"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                <td class="value">${display(bean: companyInstance, field: 'id')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="company.name.label" default="Name"/></td>

                <td class="value">${display(bean: companyInstance, field: 'name')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="company.country.label" default="Country"/></td>

                <td class="value">${msg(code: 'country.name.' + companyInstance.country?.code, default: companyInstance.country?.name)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="company.language.label" default="Language"/></td>

                <td class="value">${msg(code: 'language.name.' + companyInstance.language?.code, default: companyInstance.language?.name)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="company.currency.label" default="Currency"/></td>

                <td class="value">${msg(code: 'currency.name.' + companyInstance.displayCurrency?.code, default: companyInstance.displayCurrency?.name)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="company.displayTaxCode.label" default="Tax Code"/></td>

                <td class="value">${msg(code: 'taxCode.name.' + companyInstance.displayTaxCode?.code, default: companyInstance.displayTaxCode?.name)}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                <td class="value">${display(bean: companyInstance, field: 'securityCode')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="company.systemOnly.label" default="System Only"/></td>

                <td class="value">${display(bean: companyInstance, field: 'systemOnly')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                <td class="value">${display(bean: companyInstance, field: 'dateCreated')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                <td class="value">${display(bean: companyInstance, field: 'lastUpdated')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                <td class="value">${display(bean: companyInstance, field: 'version')}</td>

            </tr>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${companyInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
