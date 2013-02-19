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
<%@ page import="org.grails.tlc.books.ChartSection" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="accounts"/>
    <title><g:msg code="show" domain="chartSection"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="chartSection"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="chartSection"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="chartSection"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                    <td class="value">${display(bean: chartSectionInstance, field: 'id')}</td>

                </tr>
            </g:permit>


            <tr class="prop">
                <td class="name"><g:msg code="chartSection.path.label" default="Path"/></td>

                <td class="value">${display(bean: chartSectionInstance, field: 'path')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="chartSection.name.label" default="Name"/></td>

                <td class="value">${display(bean: chartSectionInstance, field: 'name')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="chartSection.sequencer.label" default="Sequencer"/></td>

                <td class="value">${display(bean: chartSectionInstance, field: 'sequencer')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="chartSection.type.label" default="Type"/></td>

                <td class="value"><g:msg code="chartSection.type.${chartSectionInstance.type}" default="${chartSectionInstance.type}"/></td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="chartSection.status.label" default="Status"/></td>

                <td class="value"><g:msg code="chartSection.status.${chartSectionInstance.status}" default="${chartSectionInstance.status}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="chartSection.autoCreate.label" default="Auto Create"/></td>

                <td class="value">${display(bean: chartSectionInstance, field: 'autoCreate')}</td>

            </tr>

            <tr>
                <td colspan="2">
                    <table class="borderless">
                        <tbody>
                        <tr class="prop">
                            <td></td>
                            <td class="highlighted"><g:msg code="chartSection.segments.label" default="Segments"/></td>
                            <td class="highlighted"><g:msg code="chartSection.defaults.label" default="Defaults"/></td>
                        </tr>
                        <tr class="prop">
                            <td class="name">1</td>
                            <td class="value">${chartSectionInstance?.segment1?.name?.encodeAsHTML()}</td>
                            <td class="value">${display(bean: chartSectionInstance, field: 'default1')}</td>
                        </tr>
                        <tr class="prop">
                            <td class="name">2</td>
                            <td class="value">${chartSectionInstance?.segment2?.name?.encodeAsHTML()}</td>
                            <td class="value">${display(bean: chartSectionInstance, field: 'default2')}</td>

                        </tr>
                        <tr class="prop">
                            <td class="name">3</td>
                            <td class="value">${chartSectionInstance?.segment3?.name?.encodeAsHTML()}</td>
                            <td class="value">${display(bean: chartSectionInstance, field: 'default3')}</td>
                        </tr>
                        <tr class="prop">
                            <td class="name">4</td>
                            <td class="value">${chartSectionInstance?.segment4?.name?.encodeAsHTML()}</td>
                            <td class="value">${display(bean: chartSectionInstance, field: 'default4')}</td>
                        </tr>
                        <tr class="prop">
                            <td class="name">5</td>
                            <td class="value">${chartSectionInstance?.segment5?.name?.encodeAsHTML()}</td>
                            <td class="value">${display(bean: chartSectionInstance, field: 'default5')}</td>
                        </tr>
                        <tr class="prop">
                            <td class="name">6</td>
                            <td class="value">${chartSectionInstance?.segment6?.name?.encodeAsHTML()}</td>
                            <td class="value">${display(bean: chartSectionInstance, field: 'default6')}</td>
                        </tr>
                        <tr class="prop">
                            <td class="name">7</td>
                            <td class="value">${chartSectionInstance?.segment7?.name?.encodeAsHTML()}</td>
                            <td class="value">${display(bean: chartSectionInstance, field: 'default7')}</td>
                        </tr>
                        <tr class="prop">
                            <td class="name">8</td>
                            <td class="value">${chartSectionInstance?.segment8?.name?.encodeAsHTML()}</td>
                            <td class="value">${display(bean: chartSectionInstance, field: 'default8')}</td>
                        </tr>
                        </tbody>
                    </table>
                </td>
            </tr>


            <g:permit activity="sysadmin">

                <tr class="prop">
                    <td class="name"><g:msg code="chartSection.code.label" default="Code"/></td>

                    <td class="value">${display(bean: chartSectionInstance, field: 'code')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="chartSection.pattern.label" default="Pattern"/></td>

                    <td class="value">${display(bean: chartSectionInstance, field: 'pattern')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="chartSection.treeSequence.label" default="Tree Sequence"/></td>

                    <td class="value">${display(bean: chartSectionInstance, field: 'treeSequence')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="chartSection.accountSegment.label" default="Account Segment"/></td>

                    <td class="value">${display(bean: chartSectionInstance, field: 'accountSegment')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="chartSection.parent.label" default="Parent"/></td>

                    <td class="value">${display(bean: chartSectionInstance, field: 'parent')}</td>

                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                    <td class="value">${display(bean: chartSectionInstance, field: 'securityCode')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: chartSectionInstance, field: 'dateCreated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                    <td class="value">${display(bean: chartSectionInstance, field: 'lastUpdated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                    <td class="value">${display(bean: chartSectionInstance, field: 'version')}</td>

                </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${chartSectionInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
