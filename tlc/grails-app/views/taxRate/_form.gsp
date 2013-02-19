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
<%@ page import="org.grails.tlc.corp.TaxCode" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="validFrom"><g:msg code="taxRate.validFrom.label" default="Valid From"/></label>
            </td>
            <td class="value ${hasErrors(bean: taxRateInstance, field: 'validFrom', 'errors')}">
                <input autofocus="autofocus" type="text" size="20" id="validFrom" name="validFrom" value="${display(bean: taxRateInstance, field: 'validFrom', scale: 1)}"/>&nbsp;<g:help code="taxRate.validFrom"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="rate"><g:msg code="taxRate.rate.label" default="Rate"/></label>
            </td>
            <td class="value ${hasErrors(bean: taxRateInstance, field: 'rate', 'errors')}">
                <input type="text" size="20" id="rate" name="rate" value="${display(bean: taxRateInstance, field: 'rate', scale: 3)}"/>&nbsp;<g:help code="taxRate.rate"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
