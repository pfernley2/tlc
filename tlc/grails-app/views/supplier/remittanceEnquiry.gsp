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
    <title><g:msg code="remittance.enquire" default="Remittance Advice Enquiry"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="print" action="remittancePrint" params="${[supplierId: supplierInstance?.id, remittance: remittanceInstance?.id, priorStatement: remittanceInstance?.id, displayCurrency: displayCurrency?.id, displayPeriod: displayPeriod?.id]}"><g:msg code="generic.print" default="Print"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="remittance.enquire" default="Remittance Advice Enquiry"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${supplierInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${supplierInstance}"/>
        </div>
    </g:hasErrors>
    <g:if test="${supplierInstance.id}">
        <g:compressor>
        <g:form action="remittanceEnquiry" method="post">
            <input type="hidden" name="supplierId" value="${supplierInstance.id}"/>
            <input type="hidden" name="displayPeriod" value="${displayPeriod?.id}"/>
            <input type="hidden" name="displayCurrency" value="${displayCurrency?.id}"/>
            <input type="hidden" name="priorStatement" value="${remittanceInstance?.id}"/>
            <div class="dialog">
                <table>
                    <tbody>
                    <tr class="prop">
                        <td class="name"><g:msg code="document.supplier.label" default="Supplier"/></td>
                        <td class="value"><g:enquiryLink target="${supplierInstance}" displayPeriod="${displayPeriod}" displayCurrency="${displayCurrency}">${supplierInstance.code.encodeAsHTML()}</g:enquiryLink>&nbsp;${supplierInstance.name.encodeAsHTML()}</td>

                        <td class="name"><g:msg code="document.currency.label" default="Currency"/></td>
                        <td class="value">${supplierInstance.currency.code.encodeAsHTML()}</td>

                        <td class="name">
                            <label for="remittance"><g:msg code="remittance.enquire.date.label" default="Remittance"/></label>
                        </td>
                        <td class="value nowrap">
                            <g:domainSelect name="remittance" options="${remittanceList}" selected="${remittanceInstance}" displays="adviceDate" sort="false"/>&nbsp;<g:help code="remittance.enquire.date"/>
                        </td>

                        <td><span class="button"><input class="save" type="submit" value="${msg(code: 'generic.enquire', 'default': 'Enquire')}"/></span></td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </g:form>
        <g:if test="${remittanceLineInstanceList}">
            <div class="list">
                <table>
                    <thead>
                    <tr>

                        <th><g:msg code="remittance.docdate" default="Date"/></th>

                        <th><g:msg code="remittance.doc" default="Document"/></th>

                        <th><g:msg code="remittance.ref" default="Reference"/></th>

                        <th class="center"><g:msg code="remittance.part" default="Part"/></th>

                        <th class="right"><g:msg code="generic.debit" default="Debit"/></th>

                        <th class="right"><g:msg code="generic.credit" default="Credit"/></th>

                    </tr>
                    </thead>
                    <tbody>
                    <g:each in="${remittanceLineInstanceList}" status="i" var="remittanceLineInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                            <td>${display(bean: remittanceLineInstance, field: 'documentDate', scale: 1, locale: locale)}</td>

                            <td><g:enquiryLink target="${remittanceLineInstance.source?.document}" displayPeriod="${displayPeriod}" displayCurrency="${displayCurrency}"><g:format value="${remittanceLineInstance.type + remittanceLineInstance.code}"/></g:enquiryLink></td>

                            <td>${display(bean: remittanceLineInstance, field: 'reference')}</td>

                            <td class="center">
                                <g:if test="${remittanceLineInstance.originalValue != remittanceLineInstance.accountUnallocated}">
                                    ${display(value: true)}
                                </g:if>
                            </td>

                            <td class="right">
                                <g:if test="${remittanceLineInstance.accountUnallocated >= 0.0}">
                                    ${display(value: remittanceLineInstance.accountUnallocated, scale: supplierInstance.currency.decimals, locale: locale)}
                                </g:if>
                            </td>

                            <td class="right">
                                <g:if test="${remittanceLineInstance.accountUnallocated < 0.0}">
                                    ${display(value: -remittanceLineInstance.accountUnallocated, scale: supplierInstance.currency.decimals, locale: locale)}
                                </g:if>
                            </td>

                        </tr>
                    </g:each>
                    <g:if test="${debitTotal > 0.0}">
                        <tr>
                            <td colspan="5"><hr/></td>
                            <td></td>
                        </tr>
                        <tr>
                            <td colspan="4"></td>
                            <td class="right">${format(value: debitTotal, scale: supplierInstance.currency.decimals, locale: locale)}</td>
                            <td></td>
                        </tr>
                    </g:if>
                    <tr>
                        <td colspan="4" class="right highlighted">
                            <g:msg code="remittance.total" default="Payment"/>
                            <g:if test="${remittanceInstance.sourceDocument}">
                                <g:enquiryLink target="${remittanceInstance.sourceDocument}" displayPeriod="${displayPeriod}" displayCurrency="${displayCurrency}"><g:format value="${remittanceInstance.sourceDocument.type.code + remittanceInstance.sourceDocument.code}"/></g:enquiryLink>
                            </g:if>
                        </td>
                        <td class="right highlighted">${format(value: creditTotal - debitTotal, scale: supplierInstance.currency.decimals, locale: locale)}</td>
                        <td></td>
                    </tr>
                    <tr>
                        <td colspan="6"><hr/></td>
                    </tr>
                    <tr>
                        <td colspan="4" class="right"><g:msg code="document.sourceTotals" default="Totals"/></td>
                        <td class="right">${format(value: creditTotal, scale: supplierInstance.currency.decimals, locale: locale)}</td>
                        <td class="right">${format(value: creditTotal, scale: supplierInstance.currency.decimals, locale: locale)}</td>
                    </tr>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${remittanceLineInstanceTotal}" params="${[supplierId: supplierInstance?.id, remittance: remittanceInstance?.id, priorStatement: remittanceInstance?.id, displayCurrency: displayCurrency?.id, displayPeriod: displayPeriod?.id]}"/>
            </div>
        </g:if>
        </g:compressor>
    </g:if>
</div>
</body>
</html>
