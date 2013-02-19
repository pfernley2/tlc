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
    <title><g:msg code="list" domain="remittance"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <g:if test="${remittanceInstanceList}">
        <span class="menuButton"><g:link class="print" action="print"><g:msg code="generic.print" default="Print"/></g:link></span>
    </g:if>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="remittance"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="remittance"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:if test="${remittanceInstanceList}">
        <g:if test="${multipage}">
            <div class="center mediumTopMargin largeBottomMargin">
                <g:msg code="remittance.warn" default="NOTE: Clicking the Authorize All button will affect ALL remittance advices in the listing, not just the ones on this page."/>
            </div>
        </g:if>
        <div class="list">
            <table>
                <thead>
                <tr>

                    <th><g:msg code="supplier.code.label" default="Code"/></th>

                    <th><g:msg code="supplier.name.label" default="Name"/></th>

                    <th><g:msg code="remittance.adviceDate.label" default="Advice Date"/></th>

                    <th><g:msg code="supplier.currency.label" default="Currency"/></th>

                    <th class="right"><g:msg code="remittance.payment" default="Payment"/></th>

                    <th><g:msg code="remittance.authorize" default="Authorize"/></th>

                    <th><g:msg code="remittance.allocations" default="Allocations"/></th>

                </tr>
                </thead>
                <tbody>
                <g:each in="${remittanceInstanceList}" status="i" var="remittanceInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                        <td><g:link action="show" id="${remittanceInstance.id}">${remittanceInstance.supplier.code.encodeAsHTML()}</g:link></td>

                        <td>${remittanceInstance.supplier.name.encodeAsHTML()}</td>

                        <td>${display(bean: remittanceInstance, field: 'adviceDate', scale: 1)}</td>

                        <td>${remittanceInstance.supplier.currency.code.encodeAsHTML()}</td>

                        <td class="right">${display(bean: remittanceInstance, field: 'accountValue', scale: remittanceInstance.supplier.currency.decimals)}</td>

                        <td><g:form method="post" action="authorize" id="${remittanceInstance.id}"><input type="submit" value="${msg(code: 'remittance.authorize', default: 'Authorize')}"/></g:form></td>

                        <td><g:drilldown controller="remittanceLine" action="list" value="${remittanceInstance.id}"/></td>

                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
        <div class="paginateButtons">
            <g:paginate total="${remittanceInstanceTotal}"/>
        </div>
        <div class="buttons">
            <g:form action="authorizeAll" method="post">
                <span class="button"><input class="save" type="submit" value="${msg(code: 'remittance.authorize.all', 'default': 'Authorize All')}"/></span>
            </g:form>
        </div>
    </g:if>
    <g:else>
        <div class="center">
            <h2><g:msg code="remittance.none" default="There are no unauthorized remittance advices to display"/></h2>
        </div>
    </g:else>
</div>
</body>
</html>
