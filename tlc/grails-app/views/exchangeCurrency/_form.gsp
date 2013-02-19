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
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="code"><g:msg code="exchangeCurrency.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean:exchangeCurrencyInstance,field:'code','errors')}">
                <input autofocus="autofocus" type="text" size="5" id="code" name="code" value="${display(bean:exchangeCurrencyInstance,field:'code')}"/>&nbsp;<g:help code="exchangeCurrency.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="exchangeCurrency.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean:exchangeCurrencyInstance,field:'name','errors')}">
                <input type="text" maxlength="30" size="30" id="name" name="name" value="${exchangeCurrencyInstance.id ? msg(code: 'currency.name.' + exchangeCurrencyInstance.code, default: exchangeCurrencyInstance.name) : exchangeCurrencyInstance.name}"/>&nbsp;<g:help code="exchangeCurrency.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="decimals"><g:msg code="exchangeCurrency.decimals.label" default="Decimals"/></label>
            </td>
            <td class="value ${hasErrors(bean:exchangeCurrencyInstance,field:'decimals','errors')}">
                <g:select from="${0..3}" id="decimals" name="decimals" value="${exchangeCurrencyInstance?.decimals}"/>&nbsp;<g:help code="exchangeCurrency.decimals"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="autoUpdate"><g:msg code="exchangeCurrency.autoUpdate.label" default="Auto Update"/></label>
            </td>
            <td class="value ${hasErrors(bean:exchangeCurrencyInstance,field:'autoUpdate','errors')}">
                <g:checkBox name="autoUpdate" value="${exchangeCurrencyInstance?.autoUpdate}" ></g:checkBox>&nbsp;<g:help code="exchangeCurrency.autoUpdate"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
