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
    <title><g:msg code="list" domain="chartSection"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="tree" action="tree"><g:msg code="chartSection.tree.button" default="Tree View"/></g:link></span>
    <span class="menuButton"><g:link class="print" action="print"><g:msg code="generic.print" default="Print"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="chartSection"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="list" domain="chartSection"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>

    <div class="criteria">
        <g:criteria include="path, name, type*, autoCreate, sequencer, status*"/>
    </div>
    <div class="list">
        <table>
            <thead>
            <tr>

                <g:sortableColumn property="path" title="Path" titleKey="chartSection.path.label"/>

                <g:sortableColumn property="name" title="Name" titleKey="chartSection.name.label"/>

                <g:sortableColumn property="sequencer" title="Sequencer" titleKey="chartSection.sequencer.label"/>

                <g:sortableColumn property="type" title="Type" titleKey="chartSection.type.label"/>

                <g:sortableColumn property="status" title="Status" titleKey="chartSection.status.label"/>

                <g:sortableColumn property="autoCreate" title="Auto Create" titleKey="chartSection.autoCreate.label"/>

                <th><g:msg code="chartSection.segments.label" default="Segments"/></th>

                <th><g:msg code="chartSection.defaults.label" default="Defaults"/></th>

                <th><g:msg code="chartSection.ranges.label" default="Ranges"/></th>

            </tr>
            </thead>
            <tbody>
            <g:each in="${chartSectionInstanceList}" status="i" var="chartSectionInstance">
                <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">

                    <td><g:link action="show" id="${chartSectionInstance.id}">${display(bean:chartSectionInstance, field:'path')}</g:link></td>

                    <td>${display(bean:chartSectionInstance, field:'name')}</td>

                    <td>${display(bean:chartSectionInstance, field:'sequencer')}</td>

                    <td><g:msg code="chartSection.type.${chartSectionInstance.type}" default="${chartSectionInstance.type}"/></td>

                    <td><g:msg code="chartSection.status.${chartSectionInstance.status}" default="${chartSectionInstance.status}"/></td>

                    <td>${display(bean:chartSectionInstance, field:'autoCreate')}</td>

                    <td>${segmentsList[i]}</td>

                    <td>${defaultsList[i]}</td>

                    <g:if test="${chartSectionInstance.segment1}">
                        <td><g:drilldown controller="chartSectionRange" value="${chartSectionInstance.id}"/></td>
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
        <g:paginate total="${chartSectionInstanceTotal}"/>
    </div>
</div>
</body>
</html>
