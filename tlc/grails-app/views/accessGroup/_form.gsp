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
                <label for="code"><g:msg code="accessGroup.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: accessGroupInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: accessGroupInstance, field: 'code')}"/>&nbsp;<g:help code="accessGroup.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="accessGroup.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: accessGroupInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="30" size="30" id="name" name="name" value="${display(bean: accessGroupInstance, field: 'name')}"/>&nbsp;<g:help code="accessGroup.name"/>
            </td>
        </tr>

        <g:if test="${elementList[0]}">
            <tr class="prop">
                <td class="name">
                    <label for="element1">${elementList[0].encodeAsHTML()}</label>
                </td>
                <td class="value ${hasErrors(bean: accessGroupInstance, field: 'element1', 'errors')}">
                    <input type="text" maxlength="250" size="50" id="element1" name="element1" value="${display(bean: accessGroupInstance, field: 'element1')}"/>&nbsp;<g:help code="accessGroup.element"/>
                </td>
            </tr>
        </g:if>

        <g:if test="${elementList[1]}">
            <tr class="prop">
                <td class="name">
                    <label for="element2">${elementList[1].encodeAsHTML()}</label>
                </td>
                <td class="value ${hasErrors(bean: accessGroupInstance, field: 'element2', 'errors')}">
                    <input type="text" maxlength="250" size="50" id="element2" name="element2" value="${display(bean: accessGroupInstance, field: 'element2')}"/>&nbsp;<g:help code="accessGroup.element"/>
                </td>
            </tr>
        </g:if>

        <g:if test="${elementList[2]}">
            <tr class="prop">
                <td class="name">
                    <label for="element3">${elementList[2].encodeAsHTML()}</label>
                </td>
                <td class="value ${hasErrors(bean: accessGroupInstance, field: 'element3', 'errors')}">
                    <input type="text" maxlength="250" size="50" id="element3" name="element3" value="${display(bean: accessGroupInstance, field: 'element3')}"/>&nbsp;<g:help code="accessGroup.element"/>
                </td>
            </tr>
        </g:if>

        <g:if test="${elementList[3]}">
            <tr class="prop">
                <td class="name">
                    <label for="element4">${elementList[3].encodeAsHTML()}</label>
                </td>
                <td class="value ${hasErrors(bean: accessGroupInstance, field: 'element4', 'errors')}">
                    <input type="text" maxlength="250" size="50" id="element4" name="element4" value="${display(bean: accessGroupInstance, field: 'element4')}"/>&nbsp;<g:help code="accessGroup.element"/>
                </td>
            </tr>
        </g:if>

        <g:if test="${elementList[4]}">
            <tr class="prop">
                <td class="name">
                    <label for="element5">${elementList[4].encodeAsHTML()}</label>
                </td>
                <td class="value ${hasErrors(bean: accessGroupInstance, field: 'element5', 'errors')}">
                    <input type="text" maxlength="250" size="50" id="element5" name="element5" value="${display(bean: accessGroupInstance, field: 'element5')}"/>&nbsp;<g:help code="accessGroup.element"/>
                </td>
            </tr>
        </g:if>

        <g:if test="${elementList[5]}">
            <tr class="prop">
                <td class="name">
                    <label for="element6">${elementList[5].encodeAsHTML()}</label>
                </td>
                <td class="value ${hasErrors(bean: accessGroupInstance, field: 'element6', 'errors')}">
                    <input type="text" maxlength="250" size="50" id="element6" name="element6" value="${display(bean: accessGroupInstance, field: 'element6')}"/>&nbsp;<g:help code="accessGroup.element"/>
                </td>
            </tr>
        </g:if>

        <g:if test="${elementList[6]}">
            <tr class="prop">
                <td class="name">
                    <label for="element7">${elementList[6].encodeAsHTML()}</label>
                </td>
                <td class="value ${hasErrors(bean: accessGroupInstance, field: 'element7', 'errors')}">
                    <input type="text" maxlength="250" size="50" id="element7" name="element7" value="${display(bean: accessGroupInstance, field: 'element7')}"/>&nbsp;<g:help code="accessGroup.element"/>
                </td>
            </tr>
        </g:if>

        <g:if test="${elementList[7]}">
            <tr class="prop">
                <td class="name">
                    <label for="element8">${elementList[7].encodeAsHTML()}</label>
                </td>
                <td class="value ${hasErrors(bean: accessGroupInstance, field: 'element8', 'errors')}">
                    <input type="text" maxlength="250" size="50" id="element8" name="element8" value="${display(bean: accessGroupInstance, field: 'element8')}"/>&nbsp;<g:help code="accessGroup.element"/>
                </td>
            </tr>
        </g:if>

        <tr class="prop">
            <td class="name">
                <label for="customers"><g:msg code="accessGroup.customers.label" default="Customers"/></label>
            </td>
            <td class="value ${hasErrors(bean: accessGroupInstance, field: 'customers', 'errors')}">
                <input type="text" maxlength="250" size="50" id="customers" name="customers" value="${display(bean: accessGroupInstance, field: 'customers')}"/>&nbsp;<g:help code="accessGroup.customers"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="suppliers"><g:msg code="accessGroup.suppliers.label" default="Suppliers"/></label>
            </td>
            <td class="value ${hasErrors(bean: accessGroupInstance, field: 'suppliers', 'errors')}">
                <input type="text" maxlength="250" size="50" id="suppliers" name="suppliers" value="${display(bean: accessGroupInstance, field: 'suppliers')}"/>&nbsp;<g:help code="accessGroup.suppliers"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
