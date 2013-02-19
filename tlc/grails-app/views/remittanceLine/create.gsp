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
    <title><g:msg code="remittanceLine.new" default="New Remittance Allocation"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="remittanceLine.list" default="Remittance Allocation List"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="remittanceLine.new" default="New Remittance Allocation" help="remittance"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${remittanceLineInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${remittanceLineInstance}"/>
        </div>
    </g:hasErrors>
    <g:form action="save" method="post">
        <div class="dialog">
            <table>
                <tbody>
                <tr class="prop">
                    <td class="name">
                        <label for="targetType.id"><g:msg code="document.allocation.type.label" default="Document Type"/></label>
                    </td>
                    <td class="value nowrap ${hasErrors(bean: remittanceLineInstance, field: 'targetType', 'errors')}">
                        <g:domainSelect autofocus="autofocus" name="targetType.id" options="${documentTypeList}" selected="${remittanceLineInstance.targetType}" displays="${['code', 'name']}"/>&nbsp;<g:help code="document.allocation.type"/>
                    </td>

                    <td class="name">
                        <label for="code"><g:msg code="document.allocation.code.label" default="Code"/></label>
                    </td>
                    <td class="value nowrap ${hasErrors(bean: remittanceLineInstance, field: 'code', 'errors')}">
                        <input type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: remittanceLineInstance, field: 'code')}"/>&nbsp;<g:help code="document.allocation.code"/>
                    </td>

                    <td class="name">
                        <label for="payment"><g:msg code="remittanceLine.amount.label" default="Amount"/></label>
                    </td>
                    <td class="value nowrap ${hasErrors(bean: remittanceLineInstance, field: 'payment', 'errors')}">
                        <input type="text" size="12" id="payment" name="payment" value="${display(bean: remittanceLineInstance, field: 'payment', scale: ddSource.supplier.currency.decimals)}"/>&nbsp;<g:help code="remittanceLine.amount"/>
                    </td>
                </tr>
                </tbody>
            </table>
        </div>
        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="${msg(code: 'default.button.create.label', 'default': 'Create')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
