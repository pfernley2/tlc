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
                <label for="path"><g:msg code="chartSection.path.label" default="Path"/></label>
            </td>
            <td class="value ${hasErrors(bean: chartSectionInstance, field: 'path', 'errors')}">
                <input autofocus="autofocus" type="text" maxlength="99" size="30" id="path" name="path" value="${display(bean: chartSectionInstance, field: 'path')}"/>&nbsp;<g:help code="chartSection.path"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="chartSection.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: chartSectionInstance, field: 'name', 'errors')}">
                <input type="text" maxlength="50" size="30" id="name" name="name" value="${display(bean: chartSectionInstance, field: 'name')}"/>&nbsp;<g:help code="chartSection.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="sequencer"><g:msg code="chartSection.sequencer.label" default="Sequencer"/></label>
            </td>
            <td class="value ${hasErrors(bean: chartSectionInstance, field: 'sequencer', 'errors')}">
                <input type="text" id="sequencer" name="sequencer" size="5" value="${display(bean: chartSectionInstance, field: 'sequencer')}"/>&nbsp;<g:help code="chartSection.sequencer"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="type"><g:msg code="chartSection.type.label" default="Type"/></label>
            </td>
            <td class="value ${hasErrors(bean: chartSectionInstance, field: 'type', 'errors')}">
                <g:select id="type" name="type" from="${chartSectionInstance.constraints.type.inList}" value="${chartSectionInstance.type}" valueMessagePrefix="chartSection.type" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="chartSection.type"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="status"><g:msg code="chartSection.status.label" default="Status"/></label>
            </td>
            <td class="value ${hasErrors(bean: chartSectionInstance, field: 'status', 'errors')}">
                <g:select id="status" name="status" from="${chartSectionInstance.constraints.status.inList}" value="${chartSectionInstance.status}" valueMessagePrefix="chartSection.status"/>&nbsp;<g:help code="chartSection.status"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="autoCreate"><g:msg code="chartSection.autoCreate.label" default="Auto Create"/></label>
            </td>
            <td class="value ${hasErrors(bean: chartSectionInstance, field: 'autoCreate', 'errors')}">
                <g:checkBox name="autoCreate" value="${chartSectionInstance.autoCreate}"></g:checkBox>&nbsp;<g:help code="chartSection.autoCreate"/>
            </td>
        </tr>

        <tr>
            <td colspan="2">
                <table class="borderless">
                    <tbody>
                    <tr class="prop">
                        <td></td>
                        <td class="highlighted"><g:msg code="chartSection.segments.label" default="Segments"/>&nbsp;<g:help code="chartSection.segments"/></td>
                        <td class="highlighted"><g:msg code="chartSection.defaults.label" default="Defaults"/>&nbsp;<g:help code="chartSection.defaults"/></td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="segment1">1</label>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'segment1', 'errors')}">
                            <g:select optionKey="id" optionValue="name" from="${elementList}" name="segment1.id" value="${chartSectionInstance?.segment1?.id}" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'default1', 'errors')}">
                            <input type="text" maxlength="10" size="10" id="default1" name="default1" value="${display(bean: chartSectionInstance, field: 'default1')}"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="segment2">2</label>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'segment2', 'errors')}">
                            <g:select optionKey="id" optionValue="name" from="${elementList}" name="segment2.id" value="${chartSectionInstance?.segment2?.id}" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'default2', 'errors')}">
                            <input type="text" maxlength="10" size="10" id="default2" name="default2" value="${display(bean: chartSectionInstance, field: 'default2')}"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="segment3">3</label>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'segment3', 'errors')}">
                            <g:select optionKey="id" optionValue="name" from="${elementList}" name="segment3.id" value="${chartSectionInstance?.segment3?.id}" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'default3', 'errors')}">
                            <input type="text" maxlength="10" size="10" id="default3" name="default3" value="${display(bean: chartSectionInstance, field: 'default3')}"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="segment4">4</label>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'segment4', 'errors')}">
                            <g:select optionKey="id" optionValue="name" from="${elementList}" name="segment4.id" value="${chartSectionInstance?.segment4?.id}" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'default4', 'errors')}">
                            <input type="text" maxlength="10" size="10" id="default4" name="default4" value="${display(bean: chartSectionInstance, field: 'default4')}"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="segment5">5</label>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'segment5', 'errors')}">
                            <g:select optionKey="id" optionValue="name" from="${elementList}" name="segment5.id" value="${chartSectionInstance?.segment5?.id}" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'default5', 'errors')}">
                            <input type="text" maxlength="10" size="10" id="default5" name="default5" value="${display(bean: chartSectionInstance, field: 'default5')}"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="segment6">6</label>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'segment6', 'errors')}">
                            <g:select optionKey="id" optionValue="name" from="${elementList}" name="segment6.id" value="${chartSectionInstance?.segment6?.id}" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'default6', 'errors')}">
                            <input type="text" maxlength="10" size="10" id="default6" name="default6" value="${display(bean: chartSectionInstance, field: 'default6')}"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="segment7">7</label>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'segment7', 'errors')}">
                            <g:select optionKey="id" optionValue="name" from="${elementList}" name="segment7.id" value="${chartSectionInstance?.segment7?.id}" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'default7', 'errors')}">
                            <input type="text" maxlength="10" size="10" id="default7" name="default7" value="${display(bean: chartSectionInstance, field: 'default7')}"/>
                        </td>
                    </tr>

                    <tr class="prop">
                        <td class="name">
                            <label for="segment8">8</label>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'segment8', 'errors')}">
                            <g:select optionKey="id" optionValue="name" from="${elementList}" name="segment8.id" value="${chartSectionInstance?.segment8?.id}" noSelection="['null': msg(code: 'generic.no.selection', default: '-- none --')]"/>
                        </td>
                        <td class="value ${hasErrors(bean: chartSectionInstance, field: 'default8', 'errors')}">
                            <input type="text" maxlength="10" size="10" id="default8" name="default8" value="${display(bean: chartSectionInstance, field: 'default8')}"/>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </td>
        </tr>

        </tbody>
    </table>
</div>
