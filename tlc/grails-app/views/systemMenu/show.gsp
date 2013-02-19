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
<!doctype html>
<html>
<head>
    <meta name="generator" content="system"/>
    <title><g:msg code="show" domain="systemMenu"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="systemMenu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemMenu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="systemMenu"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <tr class="prop">
                <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                <td class="value">${display(bean: systemMenuInstance, field: 'id')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemMenu.path.label" default="Path"/></td>

                <td class="value">${display(bean: systemMenuInstance, field: 'path')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemMenu.title.label" default="Title"/></td>

                <td class="value"><g:msg code="menu.option.${systemMenuInstance.path}" default="${systemMenuInstance.title}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemMenu.sequencer.label" default="Sequencer"/></td>

                <td class="value">${display(bean: systemMenuInstance, field: 'sequencer')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemMenu.type.label" default="Type"/></td>

                <td class="value"><g:msg code="systemMenu.type.${systemMenuInstance.type}" default="${systemMenuInstance.type}"/></td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemMenu.command.label" default="Command"/></td>

                <td class="value">${display(bean: systemMenuInstance, field: 'command')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemMenu.parameters.label" default="Parameters"/></td>

                <g:if test="${(systemMenuInstance.type == 'submenu' && systemMenuInstance.parameters)}">
                    <td class="value"><g:msg code="menu.submenu.${systemMenuInstance.path}" default="${systemMenuInstance.parameters}"/></td>
                </g:if>
                <g:else>
                    <td class="value">${display(bean: systemMenuInstance, field: 'parameters')}</td>
                </g:else>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemMenu.activity.label" default="Activity"/></td>

                <td class="value">${systemMenuInstance.activity.code.encodeAsHTML()}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemMenu.parent.label" default="Parent"/></td>

                <td class="value">${display(bean: systemMenuInstance, field: 'parent')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="systemMenu.treeSequence.label" default="Tree Sequence"/></td>

                <td class="value">${display(bean: systemMenuInstance, field: 'treeSequence')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                <td class="value">${display(bean: systemMenuInstance, field: 'securityCode')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                <td class="value">${display(bean: systemMenuInstance, field: 'dateCreated')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                <td class="value">${display(bean: systemMenuInstance, field: 'lastUpdated')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                <td class="value">${display(bean: systemMenuInstance, field: 'version')}</td>

            </tr>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${systemMenuInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'systemMenu.delete.confirm', 'default':'If this is a sub-menu, the children will be deleted also. Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
