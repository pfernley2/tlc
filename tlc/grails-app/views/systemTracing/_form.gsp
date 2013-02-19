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
                <label for="domainName"><g:msg code="systemTracing.domainName.label" default="Domain Name"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemTracingInstance,field:'domainName','errors')}">
                <input autofocus="autofocus" type="text" maxlength="50" size="30" id="domainName" name="domainName" value="${display(bean:systemTracingInstance,field:'domainName')}"/>&nbsp;<g:help code="systemTracing.domainName"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="insertSecurityCode"><g:msg code="systemTracing.insertSecurityCode.label" default="Insert Trace Setting"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemTracingInstance,field:'insertSecurityCode','errors')}">
                <g:select name="insertSecurityCode" from="${selectionList}" optionKey="id" optionValue="name" value="${systemTracingInstance.insertSecurityCode}"/>&nbsp;<g:help code="systemTracing.insertSecurityCode"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="updateSecurityCode"><g:msg code="systemTracing.updateSecurityCode.label" default="Update Trace Setting"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemTracingInstance,field:'updateSecurityCode','errors')}">
                <g:select name="updateSecurityCode" from="${selectionList}" optionKey="id" optionValue="name" value="${systemTracingInstance.updateSecurityCode}"/>&nbsp;<g:help code="systemTracing.updateSecurityCode"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="deleteSecurityCode"><g:msg code="systemTracing.deleteSecurityCode.label" default="Delete Trace Setting"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemTracingInstance,field:'deleteSecurityCode','errors')}">
                <g:select name="deleteSecurityCode" from="${selectionList}" optionKey="id" optionValue="name" value="${systemTracingInstance.deleteSecurityCode}"/>&nbsp;<g:help code="systemTracing.deleteSecurityCode"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="insertRetentionDays"><g:msg code="systemTracing.insertRetentionDays.label" default="Insert Retention Days"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemTracingInstance,field:'insertRetentionDays','errors')}">
                <input type="text" id="insertRetentionDays" name="insertRetentionDays" size="5" value="${display(bean:systemTracingInstance,field:'insertRetentionDays')}"/>&nbsp;<g:help code="systemTracing.insertRetentionDays"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="updateRetentionDays"><g:msg code="systemTracing.updateRetentionDays.label" default="Update Retention Days"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemTracingInstance,field:'updateRetentionDays','errors')}">
                <input type="text" id="updateRetentionDays" name="updateRetentionDays" size="5" value="${display(bean:systemTracingInstance,field:'updateRetentionDays')}"/>&nbsp;<g:help code="systemTracing.updateRetentionDays"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="deleteRetentionDays"><g:msg code="systemTracing.deleteRetentionDays.label" default="Delete Retention Days"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemTracingInstance,field:'deleteRetentionDays','errors')}">
                <input type="text" id="deleteRetentionDays" name="deleteRetentionDays" size="5" value="${display(bean:systemTracingInstance,field:'deleteRetentionDays')}"/>&nbsp;<g:help code="systemTracing.deleteRetentionDays"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="systemOnly"><g:msg code="systemTracing.systemOnly.label" default="System Only"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemTracingInstance,field:'systemOnly','errors')}">
                <g:checkBox name="systemOnly" value="${systemTracingInstance?.systemOnly}" ></g:checkBox>&nbsp;<g:help code="systemTracing.systemOnly"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
