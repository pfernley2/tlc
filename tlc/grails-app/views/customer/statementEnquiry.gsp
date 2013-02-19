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
    <title><g:msg code="customer.statement.enquire" default="Customer Statement Enquiry"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="print" action="statementPrint" params="${[customerId: customerInstance?.id, statement: statementInstance?.id, priorStatement: statementInstance?.id, displayCurrency: displayCurrency?.id, displayPeriod: displayPeriod?.id]}"><g:msg code="generic.print" default="Print"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="customer.statement.enquire" default="Customer Statement Enquiry"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${customerInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${customerInstance}"/>
        </div>
    </g:hasErrors>
    <g:if test="${customerInstance.id}">
        <g:compressor>
        <g:form action="statementEnquiry" method="post">
            <input type="hidden" name="customerId" value="${customerInstance.id}"/>
            <input type="hidden" name="displayPeriod" value="${displayPeriod?.id}"/>
            <input type="hidden" name="displayCurrency" value="${displayCurrency?.id}"/>
            <input type="hidden" name="priorStatement" value="${statementInstance?.id}"/>
            <div class="dialog">
                <table>
                    <tbody>
                    <tr class="prop">
                        <td class="name"><g:msg code="document.customer.label" default="Customer"/></td>
                        <td class="value"><g:enquiryLink target="${customerInstance}" displayPeriod="${displayPeriod}" displayCurrency="${displayCurrency}">${customerInstance.code.encodeAsHTML()}</g:enquiryLink>&nbsp;${customerInstance.name.encodeAsHTML()}</td>

                        <td class="name"><g:msg code="document.currency.label" default="Currency"/></td>
                        <td class="value">${customerInstance.currency.code.encodeAsHTML()}</td>

                        <td class="name">
                            <label for="statement"><g:msg code="customer.statement.enquire.date.label" default="Statement"/></label>
                        </td>
                        <td class="value nowrap">
                            <g:domainSelect name="statement" options="${statementList}" selected="${statementInstance}" displays="statementDate" sort="false"/>&nbsp;<g:help code="customer.statement.enquire.date"/>
                        </td>

                        <td><span class="button"><input class="save" type="submit" value="${msg(code: 'generic.enquire', 'default': 'Enquire')}"/></span></td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </g:form>
        <g:if test="${statementLineInstanceList}">
            <div class="list">
                <table>
                    <thead>
                    <tr>

                        <th><g:msg code="customer.statement.docdate" default="Date"/></th>

                        <th><g:msg code="customer.statement.doc" default="Document"/></th>

                        <th><g:msg code="customer.statement.ref" default="Reference"/></th>

                        <th class="center"><g:msg code="customer.statement.part" default="Part"/></th>

                        <th class="right"><g:msg code="customer.statement.amount" default="Amount"/></th>

                        <th class="right"><g:msg code="customer.statement.unallocated" default="Unallocated"/></th>

                        <th><g:msg code="customer.statement.duedate" default="Due"/></th>

                        <th class="center"><g:msg code="customer.statement.od" default="O/D"/></th>

                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${statementLineInstanceList}" status="i" var="statementLineInstance">
                        <g:if test="${i == 0}">
                            <tr>
                                <td colspan="8" class="emphasized">
                                    <g:if test="${statementLineInstance.currentStatement == 0}">
                                        <g:msg code="customer.statement.previous" default="From Previous Statements"/>
                                    </g:if>
                                    <g:else>
                                        <g:msg code="customer.statement.current" default="New Items"/>
                                    </g:else>
                                </td>
                            </tr>
                        </g:if>
                        <g:elseif test="${statementLineInstance.currentStatement != statementLineInstanceList[i - 1].currentStatement}">
                            <tr>
                                <td colspan="8" class="emphasized"><g:msg code="customer.statement.current" default="New Items"/></td>
                            </tr>
                        </g:elseif>
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                            <td>${display(bean: statementLineInstance, field: 'documentDate', scale: 1, locale: locale)}</td>

                            <td><g:enquiryLink target="${statementLineInstance.source?.document}" displayPeriod="${displayPeriod}" displayCurrency="${displayCurrency}"><g:format value="${statementLineInstance.type + statementLineInstance.code}"/></g:enquiryLink></td>

                            <td>${display(bean: statementLineInstance, field: 'reference')}</td>

                            <td class="center">
                                <g:if test="${statementLineInstance.originalValue != statementLineInstance.openingUnallocated}">
                                    ${display(value: true)}
                                </g:if>
                            </td>

                            <td class="right">${display(bean: statementLineInstance, field: 'openingUnallocated', scale: customerInstance.currency.decimals, locale: locale)}</td>

                            <td class="right">
                                <g:if test="${statementLineInstance.closingUnallocated}">
                                    ${display(bean: statementLineInstance, field: 'closingUnallocated', scale: customerInstance.currency.decimals, locale: locale)}
                                </g:if>
                            </td>

                            <td>
                                <g:if test="${statementLineInstance.closingUnallocated}">
                                    ${display(bean: statementLineInstance, field: 'dueDate', scale: 1, locale: locale)}
                                </g:if>
                            </td>

                            <td class="center">
                                <g:if test="${statementLineInstance.closingUnallocated && statementLineInstance.dueDate < statementInstance.statementDate}">
                                    *
                                </g:if>
                            </td>

                        </tr>
                    </g:each>
                    <tr>
                        <td colspan="8"><hr/></td>
                    </tr>
                    <tr class="overlined">
                        <td colspan="5" class="right"><g:msg code="customer.statement.overdue" default="Overdue"/></td>
                        <td class="right">${format(value: overdue, scale: customerInstance.currency.decimals, locale: locale)}</td>
                        <td colspan="2"></td>
                    </tr>
                    <tr>
                        <td colspan="5" class="right"><g:msg code="customer.statement.due" default="Due"/></td>
                        <td class="right">${format(value: due, scale: customerInstance.currency.decimals, locale: locale)}</td>
                        <td colspan="2"></td>
                    </tr>
                    <tr>
                        <td colspan="5" class="right highlighted"><g:msg code="document.docTotal.label" default="Total"/></td>
                        <td class="right highlighted">${format(value: due + overdue, scale: customerInstance.currency.decimals, locale: locale)}</td>
                        <td colspan="2"></td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${statementLineInstanceTotal}" params="${[customerId: customerInstance?.id, statement: statementInstance?.id, priorStatement: statementInstance?.id, displayCurrency: displayCurrency?.id, displayPeriod: displayPeriod?.id]}"/>
            </div>
        </g:if>
        </g:compressor>
    </g:if>
</div>
</body>
</html>
