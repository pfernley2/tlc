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
    <title><g:msg code="documentSearch.search" default="Document Search"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="documentSearch.search" default="Document Search"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${documentSearchInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${documentSearchInstance}"/>
        </div>
    </g:hasErrors>
    <g:form action="list" method="post">
        <div class="dialog">
            <g:compressor>
            <table>
                <tbody>

                <tr class="prop nowrap">
                    <td class="name">
                        <label for="type.id"><g:msg code="documentSearch.type.label" default="Document Type"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: documentSearchInstance, field: 'type', 'errors')}">
                        <g:domainSelect autofocus="autofocus" name="type.id" options="${documentTypeList}" selected="${documentSearchInstance?.type}" displays="${['code', 'name']}"/>&nbsp;<g:help code="documentSearch.type"/>
                    </td>

                    <td class="name">
                        <label for="code"><g:msg code="documentSearch.code.label" default="Code"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: documentSearchInstance, field: 'code', 'errors')}">
                        <input type="text" size="20" id="code" name="code" value="${display(bean: documentSearchInstance, field: 'code')}"/>&nbsp;<g:help code="documentSearch.code"/>
                    </td>
                </tr>

                <tr class="prop nowrap">
                    <td class="name">
                        <label for="reference"><g:msg code="documentSearch.reference.label" default="Reference"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: documentSearchInstance, field: 'reference', 'errors')}">
                        <input type="text" size="30" id="reference" name="reference" value="${display(bean: documentSearchInstance, field: 'reference')}"/>&nbsp;<g:help code="documentSearch.reference"/>
                    </td>

                    <td class="name">
                        <label for="description"><g:msg code="documentSearch.description.label" default="Description"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: documentSearchInstance, field: 'description', 'errors')}">
                        <input type="text" size="45" id="description" name="description" value="${display(bean: documentSearchInstance, field: 'description')}"/>&nbsp;<g:help code="documentSearch.description"/>
                    </td>
                </tr>

                <tr class="prop nowrap">
                    <td class="name">
                        <label for="documentFrom"><g:msg code="documentSearch.documentFrom.label" default="Documents Dated From"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: documentSearchInstance, field: 'documentFrom', 'errors')}">
                        <input type="text" size="20" id="documentFrom" name="documentFrom" value="${format(value: documentSearchInstance.documentFrom, scale: 1)}"/>&nbsp;<g:help code="documentSearch.documentFrom"/>
                    </td>

                    <td class="name">
                        <label for="documentTo"><g:msg code="documentSearch.documentTo.label" default="Documents Dated To"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: documentSearchInstance, field: 'documentTo', 'errors')}">
                        <input type="text" size="20" id="documentTo" name="documentTo" value="${format(value: documentSearchInstance.documentTo, scale: 1)}"/>&nbsp;<g:help code="documentSearch.documentTo"/>
                    </td>
                </tr>

                <tr class="prop nowrap">
                    <td class="name">
                        <label for="postedFrom"><g:msg code="documentSearch.postedFrom.label" default="Documents Created From"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: documentSearchInstance, field: 'postedFrom', 'errors')}">
                        <input type="text" size="20" id="postedFrom" name="postedFrom" value="${format(value: documentSearchInstance.postedFrom, scale: 1)}"/>&nbsp;<g:help code="documentSearch.postedFrom"/>
                    </td>

                    <td class="name">
                        <label for="postedTo"><g:msg code="documentSearch.postedTo.label" default="Documents Created To"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: documentSearchInstance, field: 'postedTo', 'errors')}">
                        <input type="text" size="20" id="postedTo" name="postedTo" value="${format(value: documentSearchInstance.postedTo, scale: 1)}"/>&nbsp;<g:help code="documentSearch.postedTo"/>
                    </td>
                </tr>
                </tbody>
            </table>
            </g:compressor>
        </div>
        <div class="buttons">
            <span class="button"><input class="search" type="submit" value="${msg(code: 'generic.search', 'default': 'Search')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
