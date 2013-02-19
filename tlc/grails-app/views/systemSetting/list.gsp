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
    <title><g:msg code="list" domain="systemSetting"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemSetting"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="systemSetting"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="criteria">
        <g:criteria include="code, dataType*, dataScale, value, systemOnly"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="code" title="Code" titleKey="systemSetting.code.label"/>

                <th><g:msg code="systemSetting.dataType.label" default="Data Type"/></th>

                <g:sortableColumn property="dataScale" title="Data Scale" titleKey="systemSetting.dataScale.label"/>

                <g:sortableColumn property="value" title="Value" titleKey="systemSetting.value.label"/>

                <g:sortableColumn property="systemOnly" title="System Only" titleKey="systemSetting.systemOnly.label"/>

                <th><g:msg code="systemSetting.propagate.label" default="Propagate"/>&nbsp;<g:help code="systemSetting.propagate"/></th>
            </tr>
            </thead>
            <tbody>
            <g:each in="${systemSettingInstanceList}" status="i" var="systemSettingInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${systemSettingInstance.id}">${display(bean: systemSettingInstance, field: 'code')}</g:link></td>

                    <td><g:msg code="generic.dataType.${systemSettingInstance.dataType}" default="${systemSettingInstance.dataType}"/></td>

                    <td>${display(bean: systemSettingInstance, field: 'dataScale')}</td>

                    <td>${display(bean: systemSettingInstance, field: 'value')}</td>

                    <td>${display(bean: systemSettingInstance, field: 'systemOnly')}</td>

                    <g:if test="${!systemSettingInstance.systemOnly}">
                        <td><g:form method="post" action="propagate" id="${systemSettingInstance.id}"><input type="submit" value="${msg(code: 'systemSetting.propagate.label', default: 'Propagate')}"/></g:form></td>
                    </g:if>
                    <g:else>
                        <td></td>
                    </g:else>
                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${systemSettingInstanceTotal}"/>
    </div>
</div>
</body>
</html>
