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
<%@ page import="org.grails.tlc.corp.Company; org.grails.tlc.sys.SystemUser" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <g:msg code="companyUser.user.label" default="User"/>
            </td>
            <td class="value">
                ${companyUserInstance?.user?.name?.encodeAsHTML()}
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="company"><g:msg code="companyUser.company.label" default="Company"/></label>
            </td>
            <td class="value ${hasErrors(bean: companyUserInstance, field: 'company', 'errors')}">
                <g:select autofocus="autofocus" optionKey="id" from="${Company.list([sort: 'name'])}" name="company.id" value="${companyUserInstance?.company?.id}"/>&nbsp;<g:help code="companyUser.company"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="lastUsed"><g:msg code="companyUser.lastUsed.label" default="Last Used"/></label>
            </td>
            <td class="value ${hasErrors(bean: companyUserInstance, field: 'lastUsed', 'errors')}">
                <input type="text" size="20" id="lastUsed" name="lastUsed" value="${display(bean: companyUserInstance, field: 'lastUsed', scale: 2)}"/>&nbsp;<g:help code="companyUser.lastUsed"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
