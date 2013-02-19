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
<%@ page import="org.grails.tlc.corp.TaxStatementLine" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="list" domain="taxStatementLine"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="taxStatementLine" returns="true" params="${[id: taxStatementInstance.id]}"/>
    <g:compressor>
    <div class="dialog">
        <table>
            <tbody>
            <tr class="prop">
                <td class="name"><g:msg code="taxStatement.authority.label" default="Authority"/></td>
                <td class="value">${taxStatementInstance.authority.name.encodeAsHTML()}</td>

                <td class="name"><g:msg code="taxStatement.statementDate.label" default="Statement Date"/></td>
                <td class="value">${display(bean: taxStatementInstance, field: 'statementDate', scale: 1)}</td>
            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taxStatementLine.currentStatement.label" default="Current"/></td>
                <td class="value">${display(bean: taxStatementLineInstance, field: 'currentStatement')}</td>

                <td class="name"><g:msg code="taxStatementLine.expenditure.label" default="Input"/></td>
                <td class="value">${display(bean: taxStatementLineInstance, field: 'expenditure')}</td>
            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taxStatementLine.taxCode.label" default="Tax Code"/></td>
                <td class="value">${(taxStatementLineInstance.taxCode.code + ' - ' + taxStatementLineInstance.taxCode.name).encodeAsHTML()}</td>

                <td class="name"><g:msg code="taxStatementLine.taxPercentage.label" default="Tax Rate"/></td>
                <td class="value">${display(bean: taxStatementLineInstance, field: 'taxPercentage', scale: 3)}</td>
            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="taxStatementLine.companyGoodsValue.label" default="Goods Value"/></td>
                <td class="value"><g:drcr value="${goods}" scale="${decimals}" zeroIsUnmarked="true"/></td>

                <td class="name"><g:msg code="taxStatementLine.companyTaxValue.label" default="Tax Value"/></td>
                <td class="value"><g:drcr value="${tax}" scale="${decimals}" zeroIsUnmarked="true"/></td>
            </tr>
            </tbody>
        </table>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>
                <th><g:msg code="generic.document" default="Document"/></th>
                <th><g:msg code="generic.documentDate" default="Document Date"/></th>
                <th><g:msg code="generalTransaction.description.label" default="Description"/></th>
                <th class="right"><g:msg code="taxStatementLine.companyGoodsValue.label" default="Goods Value"/></th>
                <th class="right"><g:msg code="taxStatementLine.companyTaxValue.label" default="Tax Value"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${transactionInstanceList}" status="i" var="transactionInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                    <td><g:format value="${transactionInstance.document.type.code + transactionInstance.document.code}"/></td>
                    <td><g:format value="${transactionInstance.document.documentDate}" scale="1"/></td>
                    <td>${display(bean: transactionInstance.document, field: 'description')}</td>
                    <g:if test="${taxStatementLineInstance.expenditure}">
                        <td class="right"><g:format value="${transactionInstance.companyTax}" scale="${decimals}"/></td>
                        <td class="right"><g:format value="${transactionInstance.companyValue}" scale="${decimals}"/></td>
                    </g:if>
                    <g:else>
                        <td class="right"><g:format value="${-transactionInstance.companyTax}" scale="${decimals}"/></td>
                        <td class="right"><g:format value="${-transactionInstance.companyValue}" scale="${decimals}"/></td>
                    </g:else>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    </g:compressor>
    <div class="paginateButtons">
        <g:paginate total="${transactionInstanceTotal}"/>
    </div>
</div>
</body>
</html>
