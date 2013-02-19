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
    <title><g:msg code="list" domain="translation"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="search" action="translate" params="${translationInstance.data}"><g:msg code="translation.selectors.button" default="Selectors"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="translation"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="center">${translationInstance.text.encodeAsHTML()}</div>
    <div class="criteria">
        <g:criteria domain="SystemMessage" include="code, locale, text*" params="${translationInstance.data}"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="systemMessage.code.label" params="${translationInstance.data}"/>

                <g:sortableColumn property="locale" title="Locale" titleKey="systemMessage.locale.label" params="${translationInstance.data}"/>

                <g:sortableColumn property="text" title="Text" titleKey="systemMessage.text.label" params="${translationInstance.data}"/>
                
                <th><g:msg code="translation.translation.label" default="Translation"/></th>
                
                <th><g:msg code="translation.edit.button" default="Edit"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${systemMessageInstanceList}" status="i" var="systemMessageInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td>${display(value: systemMessageInstance.code)}</td>

                    <td>${display(value: systemMessageInstance.locale)}</td>

                    <td>${display(value: systemMessageInstance.text)}</td>

                    <td>${display(value: systemMessageInstance.translation)}</td>

                    <td><g:form action="edit" method="post" id="${systemMessageInstance.id}" params="${translationInstance.data}"><g:submitButton name="edit" value="${msg(code: 'translation.edit.button', default: 'Edit')}"/></g:form></td>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${systemMessageInstanceTotal}" params="${translationInstance.data}"/>
    </div>
</div>
</body>
</html>
