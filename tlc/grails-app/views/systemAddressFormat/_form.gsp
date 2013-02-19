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
                <label for="code"><g:msg code="systemAddressFormat.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemAddressFormatInstance, field: 'code', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="10" size="10" id="code" name="code" value="${display(bean: systemAddressFormatInstance, field: 'code')}"/>&nbsp;<g:help code="systemAddressFormat.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="systemAddressFormat.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: systemAddressFormatInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="30" size="30" id="name" name="name" value="${systemAddressFormatInstance.id ? msg(code: 'systemAddressFormat.name.' + systemAddressFormatInstance.code, default: systemAddressFormatInstance.name) : systemAddressFormatInstance.name}"/>&nbsp;<g:help code="systemAddressFormat.name"/>
            </td>
        </tr>

        </tbody>
    </table>
    <table>
        <thead>
        <tr>
            <th class="nowrap"><g:msg code="systemAddressFormat.field.label" default="Field"/>&nbsp;<g:help code="systemAddressFormat.field"/></th>
            <th class="nowrap"><g:msg code="systemAddressFormat.prompt.label" default="Prompts"/>&nbsp;<g:help code="systemAddressFormat.prompt"/></th>
            <th class="nowrap"><g:msg code="systemAddressFormat.width.label" default="Width"/>&nbsp;<g:help code="systemAddressFormat.width"/></th>
            <th class="nowrap"><g:msg code="systemAddressFormat.mandatory.label" default="Mandatory"/>&nbsp;<g:help code="systemAddressFormat.mandatory"/></th>
            <th class="nowrap"><g:msg code="systemAddressFormat.pattern.label" default="Pattern"/>&nbsp;<g:help code="systemAddressFormat.pattern"/></th>
            <th class="nowrap"><g:msg code="systemAddressFormat.joinBy.label" default="Join By"/>&nbsp;<g:help code="systemAddressFormat.joinBy"/></th>
        </tr>
        </thead>
        <tbody>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field1', 'errors')}"><g:domainSelect name="field1" options="${fieldList}" selected="${systemAddressFormatInstance.field1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field1Prompt1', 'errors')}"><g:domainSelect name="field1Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field1Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width1', 'errors')}"><input type="text" maxlength="2" size="5" id="width1" name="width1" value="${display(bean: systemAddressFormatInstance, field: 'width1')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory1', 'errors')}"><g:checkBox name="mandatory1" value="${systemAddressFormatInstance.mandatory1}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern1', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern1" name="pattern1" value="${display(bean: systemAddressFormatInstance, field: 'pattern1')}"/></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field1Prompt2', 'errors')}"><g:domainSelect name="field1Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field1Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field1Prompt3', 'errors')}"><g:domainSelect name="field1Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field1Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field2', 'errors')}"><g:domainSelect name="field2" options="${fieldList}" selected="${systemAddressFormatInstance.field2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field2Prompt1', 'errors')}"><g:domainSelect name="field2Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field2Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width2', 'errors')}"><input type="text" maxlength="2" size="5" id="width2" name="width2" value="${display(bean: systemAddressFormatInstance, field: 'width2')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory2', 'errors')}"><g:checkBox name="mandatory2" value="${systemAddressFormatInstance.mandatory2}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern2', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern2" name="pattern2" value="${display(bean: systemAddressFormatInstance, field: 'pattern2')}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'joinBy2', 'errors')}"><input type="text" maxlength="20" size="20" id="joinBy2" name="joinBy2" value="${display(bean: systemAddressFormatInstance, field: 'joinBy2')}"/></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field2Prompt2', 'errors')}"><g:domainSelect name="field2Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field2Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field2Prompt3', 'errors')}"><g:domainSelect name="field2Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field2Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field3', 'errors')}"><g:domainSelect name="field3" options="${fieldList}" selected="${systemAddressFormatInstance.field3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field3Prompt1', 'errors')}"><g:domainSelect name="field3Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field3Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width3', 'errors')}"><input type="text" maxlength="2" size="5" id="width3" name="width3" value="${display(bean: systemAddressFormatInstance, field: 'width3')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory3', 'errors')}"><g:checkBox name="mandatory3" value="${systemAddressFormatInstance.mandatory3}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern3', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern3" name="pattern3" value="${display(bean: systemAddressFormatInstance, field: 'pattern3')}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'joinBy3', 'errors')}"><input type="text" maxlength="20" size="20" id="joinBy3" name="joinBy3" value="${display(bean: systemAddressFormatInstance, field: 'joinBy3')}"/></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field3Prompt2', 'errors')}"><g:domainSelect name="field3Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field3Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field3Prompt3', 'errors')}"><g:domainSelect name="field3Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field3Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field4', 'errors')}"><g:domainSelect name="field4" options="${fieldList}" selected="${systemAddressFormatInstance.field4}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field4Prompt1', 'errors')}"><g:domainSelect name="field4Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field4Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width4', 'errors')}"><input type="text" maxlength="2" size="5" id="width4" name="width4" value="${display(bean: systemAddressFormatInstance, field: 'width4')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory4', 'errors')}"><g:checkBox name="mandatory4" value="${systemAddressFormatInstance.mandatory4}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern4', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern4" name="pattern4" value="${display(bean: systemAddressFormatInstance, field: 'pattern4')}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'joinBy4', 'errors')}"><input type="text" maxlength="20" size="20" id="joinBy4" name="joinBy4" value="${display(bean: systemAddressFormatInstance, field: 'joinBy4')}"/></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field4Prompt2', 'errors')}"><g:domainSelect name="field4Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field4Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field4Prompt3', 'errors')}"><g:domainSelect name="field4Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field4Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field5', 'errors')}"><g:domainSelect name="field5" options="${fieldList}" selected="${systemAddressFormatInstance.field5}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field5Prompt1', 'errors')}"><g:domainSelect name="field5Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field5Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width5', 'errors')}"><input type="text" maxlength="2" size="5" id="width5" name="width5" value="${display(bean: systemAddressFormatInstance, field: 'width5')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory5', 'errors')}"><g:checkBox name="mandatory5" value="${systemAddressFormatInstance.mandatory5}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern5', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern5" name="pattern5" value="${display(bean: systemAddressFormatInstance, field: 'pattern5')}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'joinBy5', 'errors')}"><input type="text" maxlength="20" size="20" id="joinBy5" name="joinBy5" value="${display(bean: systemAddressFormatInstance, field: 'joinBy5')}"/></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field5Prompt2', 'errors')}"><g:domainSelect name="field5Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field5Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field5Prompt3', 'errors')}"><g:domainSelect name="field5Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field5Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field6', 'errors')}"><g:domainSelect name="field6" options="${fieldList}" selected="${systemAddressFormatInstance.field6}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field6Prompt1', 'errors')}"><g:domainSelect name="field6Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field6Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width6', 'errors')}"><input type="text" maxlength="2" size="5" id="width6" name="width6" value="${display(bean: systemAddressFormatInstance, field: 'width6')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory6', 'errors')}"><g:checkBox name="mandatory6" value="${systemAddressFormatInstance.mandatory6}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern6', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern6" name="pattern6" value="${display(bean: systemAddressFormatInstance, field: 'pattern6')}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'joinBy6', 'errors')}"><input type="text" maxlength="20" size="20" id="joinBy6" name="joinBy6" value="${display(bean: systemAddressFormatInstance, field: 'joinBy6')}"/></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field6Prompt2', 'errors')}"><g:domainSelect name="field6Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field6Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field6Prompt3', 'errors')}"><g:domainSelect name="field6Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field6Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field7', 'errors')}"><g:domainSelect name="field7" options="${fieldList}" selected="${systemAddressFormatInstance.field7}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field7Prompt1', 'errors')}"><g:domainSelect name="field7Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field7Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width7', 'errors')}"><input type="text" maxlength="2" size="5" id="width7" name="width7" value="${display(bean: systemAddressFormatInstance, field: 'width7')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory7', 'errors')}"><g:checkBox name="mandatory7" value="${systemAddressFormatInstance.mandatory7}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern7', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern7" name="pattern7" value="${display(bean: systemAddressFormatInstance, field: 'pattern7')}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'joinBy7', 'errors')}"><input type="text" maxlength="20" size="20" id="joinBy7" name="joinBy7" value="${display(bean: systemAddressFormatInstance, field: 'joinBy7')}"/></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field7Prompt2', 'errors')}"><g:domainSelect name="field7Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field7Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field7Prompt3', 'errors')}"><g:domainSelect name="field7Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field7Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field8', 'errors')}"><g:domainSelect name="field8" options="${fieldList}" selected="${systemAddressFormatInstance.field8}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field8Prompt1', 'errors')}"><g:domainSelect name="field8Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field8Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width8', 'errors')}"><input type="text" maxlength="2" size="5" id="width8" name="width8" value="${display(bean: systemAddressFormatInstance, field: 'width8')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory8', 'errors')}"><g:checkBox name="mandatory8" value="${systemAddressFormatInstance.mandatory8}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern8', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern8" name="pattern8" value="${display(bean: systemAddressFormatInstance, field: 'pattern8')}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'joinBy8', 'errors')}"><input type="text" maxlength="20" size="20" id="joinBy8" name="joinBy8" value="${display(bean: systemAddressFormatInstance, field: 'joinBy8')}"/></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field8Prompt2', 'errors')}"><g:domainSelect name="field8Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field8Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field8Prompt3', 'errors')}"><g:domainSelect name="field8Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field8Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field9', 'errors')}"><g:domainSelect name="field9" options="${fieldList}" selected="${systemAddressFormatInstance.field9}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field9Prompt1', 'errors')}"><g:domainSelect name="field9Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field9Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width9', 'errors')}"><input type="text" maxlength="2" size="5" id="width9" name="width9" value="${display(bean: systemAddressFormatInstance, field: 'width9')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory9', 'errors')}"><g:checkBox name="mandatory9" value="${systemAddressFormatInstance.mandatory9}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern9', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern9" name="pattern9" value="${display(bean: systemAddressFormatInstance, field: 'pattern9')}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'joinBy9', 'errors')}"><input type="text" maxlength="20" size="20" id="joinBy9" name="joinBy9" value="${display(bean: systemAddressFormatInstance, field: 'joinBy9')}"/></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field9Prompt2', 'errors')}"><g:domainSelect name="field9Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field9Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field9Prompt3', 'errors')}"><g:domainSelect name="field9Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field9Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field10', 'errors')}"><g:domainSelect name="field10" options="${fieldList}" selected="${systemAddressFormatInstance.field10}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field10Prompt1', 'errors')}"><g:domainSelect name="field10Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field10Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width10', 'errors')}"><input type="text" maxlength="2" size="5" id="width10" name="width10" value="${display(bean: systemAddressFormatInstance, field: 'width10')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory10', 'errors')}"><g:checkBox name="mandatory10" value="${systemAddressFormatInstance.mandatory10}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern10', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern10" name="pattern10" value="${display(bean: systemAddressFormatInstance, field: 'pattern10')}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'joinBy10', 'errors')}"><input type="text" maxlength="20" size="20" id="joinBy10" name="joinBy10" value="${display(bean: systemAddressFormatInstance, field: 'joinBy10')}"/></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field10Prompt2', 'errors')}"><g:domainSelect name="field10Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field10Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field10Prompt3', 'errors')}"><g:domainSelect name="field10Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field10Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field11', 'errors')}"><g:domainSelect name="field11" options="${fieldList}" selected="${systemAddressFormatInstance.field11}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field11Prompt1', 'errors')}"><g:domainSelect name="field11Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field11Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width11', 'errors')}"><input type="text" maxlength="2" size="5" id="width11" name="width11" value="${display(bean: systemAddressFormatInstance, field: 'width11')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory11', 'errors')}"><g:checkBox name="mandatory11" value="${systemAddressFormatInstance.mandatory11}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern11', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern11" name="pattern11" value="${display(bean: systemAddressFormatInstance, field: 'pattern11')}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'joinBy11', 'errors')}"><input type="text" maxlength="20" size="20" id="joinBy11" name="joinBy11" value="${display(bean: systemAddressFormatInstance, field: 'joinBy11')}"/></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field11Prompt2', 'errors')}"><g:domainSelect name="field11Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field11Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field11Prompt3', 'errors')}"><g:domainSelect name="field11Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field11Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>

        <tr>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field12', 'errors')}"><g:domainSelect name="field12" options="${fieldList}" selected="${systemAddressFormatInstance.field12}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field12Prompt1', 'errors')}"><g:domainSelect name="field12Prompt1" options="${promptList}" selected="${systemAddressFormatInstance.field12Prompt1}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'width12', 'errors')}"><input type="text" maxlength="2" size="5" id="width12" name="width12" value="${display(bean: systemAddressFormatInstance, field: 'width12')}"/></td>
            <td class="narrow center ${hasErrors(bean: systemAddressFormatInstance, field: 'mandatory12', 'errors')}"><g:checkBox name="mandatory12" value="${systemAddressFormatInstance.mandatory12}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'pattern12', 'errors')}"><input type="text" maxlength="100" size="50" id="pattern12" name="pattern12" value="${display(bean: systemAddressFormatInstance, field: 'pattern12')}"/></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'joinBy12', 'errors')}"><input type="text" maxlength="20" size="20" id="joinBy12" name="joinBy12" value="${display(bean: systemAddressFormatInstance, field: 'joinBy12')}"/></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field12Prompt2', 'errors')}"><g:domainSelect name="field12Prompt2" options="${promptList}" selected="${systemAddressFormatInstance.field12Prompt2}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        <tr>
            <td class="narrow"></td>
            <td class="narrow ${hasErrors(bean: systemAddressFormatInstance, field: 'field12Prompt3', 'errors')}"><g:domainSelect name="field12Prompt3" options="${promptList}" selected="${systemAddressFormatInstance.field12Prompt3}" returns="code" displays="name" sort="false" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
            <td class="narrow"></td>
        </tr>
        </tbody>
    </table>
</div>
