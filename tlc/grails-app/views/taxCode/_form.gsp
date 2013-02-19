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
<%@ page import="org.grails.tlc.corp.Company; org.grails.tlc.corp.TaxAuthority" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="code"><g:msg code="taxCode.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: taxCodeInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: taxCodeInstance, field: 'code')}"/>&nbsp;<g:help code="taxCode.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="taxCode.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: taxCodeInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="50" size="30" id="name" name="name" value="${taxCodeInstance.id ? msg(code: 'taxCode.name.' + taxCodeInstance.code, default: taxCodeInstance.name) : taxCodeInstance.name}"/>&nbsp;<g:help code="taxCode.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="authority.id"><g:msg code="taxAuthority.name.label" default="Tax Authority"/></label>
            </td>
            <td class="value ${hasErrors(bean: taxCodeInstance, field: 'authority', 'errors')}">
                <g:select optionKey="id" from="${TaxAuthority.findAllByCompany(taxCodeInstance.company, [sort: 'name'])}" name="authority.id" value="${taxCodeInstance?.authority?.id}"/>&nbsp;<g:help code="taxCode.authority"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
