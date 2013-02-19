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
<%@ page import="org.grails.tlc.sys.SystemGeo" %>
<div class="dialog">
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="code"><g:msg code="systemRegion.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemRegionInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" size="5" id="code" name="code" value="${display(bean: systemRegionInstance, field: 'code')}"/>&nbsp;<g:help code="systemRegion.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="systemRegion.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemRegionInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="30" size="30" id="name" name="name" value="${systemRegionInstance.id ? msg(code: 'region.name.' + systemRegionInstance.code, default: systemRegionInstance.name) : systemRegionInstance.name}"/>&nbsp;<g:help code="systemRegion.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="geo.id"><g:msg code="systemRegion.geo.label" default="Geo"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemRegionInstance, field: 'geo', 'errors')}">
                <g:domainSelect name="geo.id" options="${SystemGeo.list()}" selected="${systemRegionInstance?.geo}" prefix="geo.name" code="code" default="name"/>&nbsp;<g:help code="systemRegion.geo"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
