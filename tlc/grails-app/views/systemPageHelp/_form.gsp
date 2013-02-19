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
                <label for="code"><g:msg code="systemPageHelp.code.label" default="Code"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemPageHelpInstance,field:'code','errors')}">
                <input autofocus="autofocus" type="text" maxlength="250" size="30" id="code" name="code" value="${display(bean:systemPageHelpInstance,field:'code')}"/>&nbsp;<g:help code="systemPageHelp.code"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="locale"><g:msg code="systemPageHelp.locale.label" default="Locale"/></label>
            </td>
            <td class="value ${hasErrors(bean:systemPageHelpInstance,field:'locale','errors')}">
                <input type="text" size="5" id="locale" name="locale" value="${display(bean:systemPageHelpInstance,field:'locale')}"/>&nbsp;<g:help code="systemPageHelp.locale"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="vtop name">
                <label for="text"><g:msg code="systemPageHelp.text.label" default="Text"/></label>
            </td>
            <td class="value largeArea ${hasErrors(bean:systemPageHelpInstance,field:'text','errors')}">
                <textarea rows="5" cols="40" name="text">${display(bean:systemPageHelpInstance, field:'text')}</textarea>
                <g:help code="systemPageHelp.text"/>
            </td>
        </tr>

        </tbody>
    </table>
</div>
