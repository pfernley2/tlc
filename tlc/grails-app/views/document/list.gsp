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
<%@ page import="org.grails.tlc.books.Document" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="documentSearch.list" default="Document Search Results"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="search" action="search" params="${searchMap}"><g:msg code="generic.search" default="Search"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="documentSearch.list" default="Document Search Results"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="list">
        <table>
            <thead>
            <tr>

                <th><g:msg code="generic.document" default="Document"/></th>

                <th><g:msg code="generic.documentDate" default="Document Date"/></th>

                <th><g:msg code="templateDocument.reference.label" default="Reference"/></th>

                <th><g:msg code="generalTransaction.description.label" default="Description"/></th>

                <th><g:msg code="generic.dateCreated.label" default="Date Created"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${documentInstanceList}" status="i" var="documentInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:enquiryLink target="${documentInstance}"><g:format value="${documentInstance.type.code + documentInstance.code}"/></g:enquiryLink></td>

                    <td><g:format value="${documentInstance.documentDate}" scale="1"/></td>

                    <td>${display(bean: documentInstance, field: 'reference')}</td>

                    <td>${display(bean: documentInstance, field: 'description')}</td>

                    <td><g:format value="${documentInstance.dateCreated}" scale="1"/></td>

                </tr>
            </g:each>
            </tbody>
        </table>
    </div>
    <div class="paginateButtons">
        <g:paginate total="${documentInstanceTotal}" action="list" params="${searchMap}"/>
    </div>
</div>
</body>
</html>
