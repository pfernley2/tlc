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
 <g:compressor>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="type.id"><g:msg code="templateDocument.type.label" default="Document Type"/></label>
            </td>
            <td class="vtop small-value ${hasErrors(bean: templateDocumentInstance, field: 'type', 'errors')}">
                <g:domainSelect autofocus="autofocus"  name="type.id" options="${documentTypeList}" selected="${templateDocumentInstance?.type}" displays="${['code', 'name']}" noSelection="['null': msg(code: 'generic.select', default: '-- select --')]"/>&nbsp;<g:help code="templateDocument.type"/>
            </td>

            <td class="name">
                <label for="currency"><g:msg code="templateDocument.currency.label" default="Currency"/></label>
            </td>
            <td class="vtop small-value ${hasErrors(bean: templateDocumentInstance, field: 'currency', 'errors')}">
                <g:domainSelect name="currency.id" options="${currencyList}" selected="${templateDocumentInstance?.currency}" prefix="currency.name" code="code" default="name"/>&nbsp;<g:help code="templateDocument.currency"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="reference"><g:msg code="templateDocument.reference.label" default="Reference"/></label>
            </td>
            <td class="vtop small-value ${hasErrors(bean: templateDocumentInstance, field: 'reference', 'errors')}">
                <input type="text" maxLength="30" size="30" id="reference" name="reference" value="${display(bean: templateDocumentInstance, field: 'reference')}"/>&nbsp;<g:help code="templateDocument.reference"/>
            </td>

            <td class="name">
                <label for="description"><g:msg code="templateDocument.description.label" default="Description"/></label>
            </td>
            <td class="vtop small-value ${hasErrors(bean: templateDocumentInstance, field: 'description', 'errors')}">
                <input type="text" maxlength="50" size="45" id="description" name="description" value="${display(bean: templateDocumentInstance, field: 'description')}"/>&nbsp;<g:help code="templateDocument.description"/>
            </td>
        </tr>

        <tr class="prop">
            <td></td>
            <td></td>

            <td class="name">
                <label for="sourceAffectsTurnover"><g:msg code="templateDocument.sourceAffectsTurnover.label" default="Affects Turnover"/></label>
            </td>
            <td class="vtop small-value ${hasErrors(bean: templateDocumentInstance, field: 'sourceAffectsTurnover', 'errors')}">
                <g:checkBox name="sourceAffectsTurnover" value="${templateDocumentInstance?.sourceAffectsTurnover}"></g:checkBox>&nbsp;<g:help code="templateDocument.sourceAffectsTurnover"/>
            </td>
        </tr>
        </tbody>
    </table>
</div>
<div class="list entry">
    <table>
        <thead>
        <tr>
            <th><g:msg code="templateDocument.journal.customer.label" default="Customer"/>&nbsp;<g:help code="templateDocument.journal.customer"/></th>
            <th><g:msg code="document.line.description.label" default="Description"/>&nbsp;<g:help code="document.line.description"/></th>
            <th><g:msg code="generic.debit" default="Debit"/></th>
            <th><g:msg code="generic.credit" default="Credit"/></th>
        </tr>
        </thead>
        <tbody>
        <g:each in="${templateDocumentInstance.lines}" status="i" var="documentLine">
            <tr>
                <td class="narrow ${hasErrors(bean: documentLine, field: 'accountCode', 'errors')}">
                    <input type="text" maxLength="20" size="20" onchange="setAccount(this, '${createLink(controller: 'document', action: 'accounts')}', 'journal', 'ar')" id="lines[${i}].accountCode" name="lines[${i}].accountCode" value="${display(bean: documentLine, field: 'accountCode')}"/>
                </td>
                <td class="narrow"><input type="text" maxLength="50" size="40" id="lines[${i}].description" name="lines[${i}].description" value="${display(bean: documentLine, field: 'description')}"/></td>
                <td class="narrow"><input type="text" size="12" id="lines[${i}].documentDebit" name="lines[${i}].documentDebit" value="${display(bean: documentLine, field: 'documentDebit', scale: settings.decimals)}"/></td>
                <td class="narrow"><input type="text" size="12" id="lines[${i}].documentCredit" name="lines[${i}].documentCredit" value="${display(bean: documentLine, field: 'documentCredit', scale: settings.decimals)}"/></td>
            </tr>
            <tr>
                <td class="narrow smallBottomPadding" colspan="2"><input disabled="disabled" type="text" size="76" id="lines[${i}].displayName" name="lines[${i}].displayName" value="${display(bean: documentLine, field: 'accountName')}"/></td>
                <td class="narrow"><input type="hidden" id="lines[${i}].accountName" name="lines[${i}].accountName" value="${display(bean: documentLine, field: 'accountName')}"/></td>
                <td class="narrow"><input type="hidden" id="lines[${i}].ident" name="lines[${i}].ident" value="${display(bean: documentLine, field: 'id')}"/></td>
            </tr>
        </g:each>
        </tbody>
    </table>
</div>
</g:compressor>
