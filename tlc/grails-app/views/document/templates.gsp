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
<%@ page import="org.grails.tlc.books.TemplateDocument" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="list" domain="templateDocument"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="templateDocument"/>
    <g:if test="${!templateDocumentInstanceList}">
        <div class="standout">
            <g:msg code="templateDocument.none" default="No templates have been defined for you. Click the Back button on your browser to return to the data entry screen."/>
        </div>
    </g:if>
    <g:else>
        <div class="standout">
            <g:msg code="templateDocument.found" default="Click on the description of the template you would like to load. Alternatively, click the Back button on your browser to return to the data entry screen."/>
        </div>
        <div class="list">
            <table>
                <thead>
                <tr>

                    <th><g:msg code="templateDocument.type.label" default="Document Type"/></th>

                    <th><g:msg code="templateDocument.description.label" default="Description"/></th>

                    <th><g:msg code="templateDocument.reference.label" default="Reference"/></th>

                    <th><g:msg code="templateDocument.currency.label" default="Currency"/></th>

                </tr>
                </thead>
                <tbody>
                <g:each in="${templateDocumentInstanceList}" status="i" var="templateDocumentInstance">
                    <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                        <td>${display(bean: templateDocumentInstance, field: 'type')}</td>

                        <td><g:link controller="${ctrl}" action="${act}" id="${templateDocumentInstance.id}">${display(bean: templateDocumentInstance, field: 'description')}</g:link></td>

                        <td>${display(bean: templateDocumentInstance, field: 'reference')}</td>

                        <td>${msg(code: 'currency.name.' + templateDocumentInstance.currency.code, default: templateDocumentInstance.currency.name)}</td>

                    </tr>
                </g:each>
                </tbody>
            </table>
        </div>
    </g:else>
</div>
</body>
</html>
