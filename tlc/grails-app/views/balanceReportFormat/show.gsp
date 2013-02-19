<%--
~   Copyright 2010-2013 Paul Fernley
~
~   This file is part of the Three Ledger Core (TLC) software
~   from Paul Fernley.
~
~   TLC is free software: you can redistribute it and/or modify
~   it under the terms of the GNU General Public License as published by
~   the Free Software Foundation, either version 3 of the License, or
~   (at your option) any later version.
~
~   TLC is distributed in the hope that it will be useful,
~   but WITHOUT ANY WARRANTY; without even the implied warranty of
~   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
~   GNU General Public License for more details.
~
~   You should have received a copy of the GNU General Public License
~   along with TLC. If not, see <http://www.gnu.org/licenses/>.
--%>
<%@ page import="org.grails.tlc.books.BalanceReportFormat" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="accounts"/>
    <title><g:msg code="show" domain="balanceReportFormat"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="balanceReportFormat"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="balanceReportFormat"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="clone" id="${balanceReportFormatInstance.id}"><g:msg code="balanceReportFormat.clone" default="Clone Report Format"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="balanceReportFormat"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <g:compressor>
        <table>
            <tbody>
            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.id.label" default="Id"/></td>
                    <td class="value">${display(bean: balanceReportFormatInstance, field: 'id')}</td>

                </tr>
            </g:permit>

            <tr class="prop">
                <td class="name"><g:msg code="balanceReportFormat.name.label" default="Name"/></td>
                <td class="value">${display(bean: balanceReportFormatInstance, field: 'name')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="balanceReportFormat.title.label" default="Title"/></td>
                <td class="value">${display(bean: balanceReportFormatInstance, field: 'title')}</td>

            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="balanceReportFormat.subTitle.label" default="Sub-Title"/></td>
                <td class="value">${display(bean: balanceReportFormatInstance, field: 'subTitle')}</td>

            </tr>
            </tbody>
        </table>

        <table>
            <thead>
            <tr>
                <th></th>
                <th class="nowrap"><g:msg code="balanceReportFormat.column1" default="Column 1"/></th>
                <th class="nowrap"><g:msg code="balanceReportFormat.column2" default="Column 2"/></th>
                <th class="nowrap"><g:msg code="balanceReportFormat.column3" default="Column 3"/></th>
                <th class="nowrap"><g:msg code="balanceReportFormat.column4" default="Column 4"/></th>
            </tr>
            </thead>

            <tbody>
            <tr class="prop">
                <td class="name"><g:msg code="balanceReportFormat.column1Heading.label" default="Heading"/></td>
                <td class="value">${display(bean: balanceReportFormatInstance, field: 'column1Heading')}</td>
                <td class="value">${display(bean: balanceReportFormatInstance, field: 'column2Heading')}</td>
                <td class="value">${display(bean: balanceReportFormatInstance, field: 'column3Heading')}</td>
                <td class="value">${display(bean: balanceReportFormatInstance, field: 'column4Heading')}</td>
            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="balanceReportFormat.column1SubHeading.label" default="Sub-Heading"/></td>
                <td class="value">${display(bean: balanceReportFormatInstance, field: 'column1SubHeading')}</td>
                <td class="value">${display(bean: balanceReportFormatInstance, field: 'column2SubHeading')}</td>
                <td class="value">${display(bean: balanceReportFormatInstance, field: 'column3SubHeading')}</td>
                <td class="value">${display(bean: balanceReportFormatInstance, field: 'column4SubHeading')}</td>
            </tr>

            <tr class="prop">
                <td class="name nowrap"><g:msg code="balanceReportFormat.column1PrimaryData.label" default="Primary Data"/></td>
                <td class="value nowrap">${(balanceReportFormatInstance.column1PrimaryData ? msg(code: 'balanceReportFormat.dataOptions.' + balanceReportFormatInstance.column1PrimaryData, default: balanceReportFormatInstance.column1PrimaryData) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
                <td class="value nowrap">${(balanceReportFormatInstance.column2PrimaryData ? msg(code: 'balanceReportFormat.dataOptions.' + balanceReportFormatInstance.column2PrimaryData, default: balanceReportFormatInstance.column2PrimaryData) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
                <td class="value nowrap">${(balanceReportFormatInstance.column3PrimaryData ? msg(code: 'balanceReportFormat.dataOptions.' + balanceReportFormatInstance.column3PrimaryData, default: balanceReportFormatInstance.column3PrimaryData) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
                <td class="value nowrap">${(balanceReportFormatInstance.column4PrimaryData ? msg(code: 'balanceReportFormat.dataOptions.' + balanceReportFormatInstance.column4PrimaryData, default: balanceReportFormatInstance.column4PrimaryData) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="balanceReportFormat.column1Calculation.label" default="Calculation"/></td>
                <td class="value">${(balanceReportFormatInstance.column1Calculation ? msg(code: 'balanceReportFormat.calculationOptions.' + balanceReportFormatInstance.column1Calculation, default: balanceReportFormatInstance.column1Calculation) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
                <td class="value">${(balanceReportFormatInstance.column2Calculation ? msg(code: 'balanceReportFormat.calculationOptions.' + balanceReportFormatInstance.column2Calculation, default: balanceReportFormatInstance.column2Calculation) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
                <td class="value">${(balanceReportFormatInstance.column3Calculation ? msg(code: 'balanceReportFormat.calculationOptions.' + balanceReportFormatInstance.column3Calculation, default: balanceReportFormatInstance.column3Calculation) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
                <td class="value">${(balanceReportFormatInstance.column4Calculation ? msg(code: 'balanceReportFormat.calculationOptions.' + balanceReportFormatInstance.column4Calculation, default: balanceReportFormatInstance.column4Calculation) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
            </tr>

            <tr class="prop">
                <td class="name"><g:msg code="balanceReportFormat.column1SecondaryData.label" default="Secondary Data"/></td>
                <td class="value">${(balanceReportFormatInstance.column1SecondaryData ? msg(code: 'balanceReportFormat.dataOptions.' + balanceReportFormatInstance.column1SecondaryData, default: balanceReportFormatInstance.column1SecondaryData) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
                <td class="value">${(balanceReportFormatInstance.column2SecondaryData ? msg(code: 'balanceReportFormat.dataOptions.' + balanceReportFormatInstance.column2SecondaryData, default: balanceReportFormatInstance.column2SecondaryData) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
                <td class="value">${(balanceReportFormatInstance.column3SecondaryData ? msg(code: 'balanceReportFormat.dataOptions.' + balanceReportFormatInstance.column3SecondaryData, default: balanceReportFormatInstance.column3SecondaryData) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
                <td class="value">${(balanceReportFormatInstance.column4SecondaryData ? msg(code: 'balanceReportFormat.dataOptions.' + balanceReportFormatInstance.column4SecondaryData, default: balanceReportFormatInstance.column4SecondaryData) : msg(code: 'generic.no.selection', default: '-- none --'))}</td>
            </tr>
            </tbody>
        </table>
        <g:permit activity="sysadmin">
            <table>
                <tbody>
                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>
                    <td class="value">${display(bean: balanceReportFormatInstance, field: 'securityCode')}</td>
                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: balanceReportFormatInstance, field: 'dateCreated')}</td>
                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>
                    <td class="value">${display(bean: balanceReportFormatInstance, field: 'lastUpdated')}</td>
                </tr>

                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>
                    <td class="value">${display(bean: balanceReportFormatInstance, field: 'version')}</td>
                </tr>
                </tbody>
            </table>
        </g:permit>

        <table>
            <thead>
            <tr>
                <th><g:msg code="balanceReportLine.lineNumber.label" default="Line Number"/></th>
                <th><g:msg code="balanceReportLine.text.label" default="Text"/></th>
                <th><g:msg code="balanceReportLine.section.label" default="Chart Section"/></th>
                <th><g:msg code="balanceReportLine.accumulation.label" default="Accumulation"/></th>
            </tr>
            </thead>

            <tbody>
            <g:if test="${balanceReportFormatInstance.lines}">
                <g:each in="${balanceReportFormatInstance.lines}" status="j" var="balanceReportLineInstance">
                    <tr class="${(j % 2) == 0 ? 'odd' : 'even'}">
                        <td>${display(bean: balanceReportLineInstance, field: 'lineNumber')}</td>
                        <td>${display(bean: balanceReportLineInstance, field: 'text')}</td>
                        <td>${balanceReportLineInstance.section ? (balanceReportLineInstance.section.code + ' - ' + balanceReportLineInstance.section.name).encodeAsHTML() : ''}</td>
                        <td>${display(bean: balanceReportLineInstance, field: 'accumulation')}</td>
                    </tr>
                </g:each>
            </g:if>
            <g:else>
                <tr>
                    <td colspan="4"><g:msg code="balanceReportFormat.no.lines" default="The Report Format has no lines"/></td>
                </tr>
            </g:else>
            </tbody>
        </table>
        </g:compressor>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${balanceReportFormatInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
