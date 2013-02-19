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
<div class="dialog">
    <g:compressor>
    <table>
        <tbody>

        <tr class="prop">
            <td class="name">
                <label for="name"><g:msg code="balanceReportFormat.name.label" default="Name"/></label>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'name', 'errors')}">
                <input autofocus="autofocus" tabindex="100" type="text" maxlength="30" size="30" id="name" name="name" value="${display(bean: balanceReportFormatInstance, field: 'name')}"/>&nbsp;<g:help code="balanceReportFormat.name"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="title"><g:msg code="balanceReportFormat.title.label" default="Title"/></label>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'title', 'errors')}">
                <input type="text" tabindex="200" maxlength="30" size="30" id="title" name="title" value="${display(bean: balanceReportFormatInstance, field: 'title')}"/>&nbsp;<g:help code="balanceReportFormat.title"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="subTitle"><g:msg code="balanceReportFormat.subTitle.label" default="Sub-Title"/></label>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'subTitle', 'errors')}">
                <input type="text" tabindex="300" maxlength="30" size="30" id="subTitle" name="subTitle" value="${display(bean: balanceReportFormatInstance, field: 'subTitle')}"/>&nbsp;<g:help code="balanceReportFormat.subTitle"/>
            </td>
        </tr>

        </tbody>
    </table>
    <table>
        <thead>
        <tr>
            <th></th>
            <th><g:msg code="balanceReportFormat.column1" default="Column 1"/></th>
            <th><g:msg code="balanceReportFormat.column2" default="Column 2"/></th>
            <th><g:msg code="balanceReportFormat.column3" default="Column 3"/></th>
            <th><g:msg code="balanceReportFormat.column4" default="Column 4"/></th>
        </tr>
        </thead>

        <tbody>
        <tr class="prop">
            <td class="name">
                <label for="column1Heading"><g:msg code="balanceReportFormat.column1Heading.label" default="Heading"/></label>&nbsp;<g:help code="balanceReportFormat.column1Heading"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column1Heading', 'errors')}">
                <input tabindex="400" type="text" maxlength="10" size="10" id="column1Heading" name="column1Heading" value="${display(bean: balanceReportFormatInstance, field: 'column1Heading')}"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column2Heading', 'errors')}">
                <input tabindex="900" type="text" maxlength="10" size="10" id="column2Heading" name="column2Heading" value="${display(bean: balanceReportFormatInstance, field: 'column2Heading')}"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column3Heading', 'errors')}">
                <input tabindex="1400" type="text" maxlength="10" size="10" id="column3Heading" name="column3Heading" value="${display(bean: balanceReportFormatInstance, field: 'column3Heading')}"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column4Heading', 'errors')}">
                <input tabindex="1900" type="text" maxlength="10" size="10" id="column4Heading" name="column4Heading" value="${display(bean: balanceReportFormatInstance, field: 'column4Heading')}"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="column1SubHeading"><g:msg code="balanceReportFormat.column1SubHeading.label" default="Sub-Heading"/></label>&nbsp;<g:help code="balanceReportFormat.column1SubHeading"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column1SubHeading', 'errors')}">
                <input tabindex="500" type="text" maxlength="10" size="10" id="column1SubHeading" name="column1SubHeading" value="${display(bean: balanceReportFormatInstance, field: 'column1SubHeading')}"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column2SubHeading', 'errors')}">
                <input tabindex="1000" type="text" maxlength="10" size="10" id="column2SubHeading" name="column2SubHeading" value="${display(bean: balanceReportFormatInstance, field: 'column2SubHeading')}"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column3SubHeading', 'errors')}">
                <input tabindex="1500" type="text" maxlength="10" size="10" id="column3SubHeading" name="column3SubHeading" value="${display(bean: balanceReportFormatInstance, field: 'column3SubHeading')}"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column4SubHeading', 'errors')}">
                <input tabindex="2000" type="text" maxlength="10" size="10" id="column4SubHeading" name="column4SubHeading" value="${display(bean: balanceReportFormatInstance, field: 'column4SubHeading')}"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="column1PrimaryData"><g:msg code="balanceReportFormat.column1PrimaryData.label" default="Primary Data"/></label>&nbsp;<g:help code="balanceReportFormat.column1PrimaryData"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column1PrimaryData', 'errors')}">
                <g:select tabindex="600" id="column1PrimaryData" name="column1PrimaryData" from="${BalanceReportFormat.DATA_OPTIONS}" value="${balanceReportFormatInstance.column1PrimaryData}" valueMessagePrefix="balanceReportFormat.dataOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column2PrimaryData', 'errors')}">
                <g:select tabindex="1100" id="column2PrimaryData" name="column2PrimaryData" from="${BalanceReportFormat.DATA_OPTIONS}" value="${balanceReportFormatInstance.column2PrimaryData}" valueMessagePrefix="balanceReportFormat.dataOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column3PrimaryData', 'errors')}">
                <g:select tabindex="1600" id="column3PrimaryData" name="column3PrimaryData" from="${BalanceReportFormat.DATA_OPTIONS}" value="${balanceReportFormatInstance.column3PrimaryData}" valueMessagePrefix="balanceReportFormat.dataOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column4PrimaryData', 'errors')}">
                <g:select tabindex="2100" id="column4PrimaryData" name="column4PrimaryData" from="${BalanceReportFormat.DATA_OPTIONS}" value="${balanceReportFormatInstance.column4PrimaryData}" valueMessagePrefix="balanceReportFormat.dataOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="column1Calculation"><g:msg code="balanceReportFormat.column1Calculation.label" default="Calculation"/></label>&nbsp;<g:help code="balanceReportFormat.column1Calculation"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column1Calculation', 'errors')}">
                <g:select tabindex="700" id="column1Calculation" name="column1Calculation" from="${BalanceReportFormat.CALC_OPTIONS}" value="${balanceReportFormatInstance.column1Calculation}" valueMessagePrefix="balanceReportFormat.calculationOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column2Calculation', 'errors')}">
                <g:select tabindex="1200" id="column2Calculation" name="column2Calculation" from="${BalanceReportFormat.CALC_OPTIONS}" value="${balanceReportFormatInstance.column2Calculation}" valueMessagePrefix="balanceReportFormat.calculationOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column3Calculation', 'errors')}">
                <g:select tabindex="1700" id="column3Calculation" name="column3Calculation" from="${BalanceReportFormat.CALC_OPTIONS}" value="${balanceReportFormatInstance.column3Calculation}" valueMessagePrefix="balanceReportFormat.calculationOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column4Calculation', 'errors')}">
                <g:select tabindex="2200" id="column4Calculation" name="column4Calculation" from="${BalanceReportFormat.CALC_OPTIONS}" value="${balanceReportFormatInstance.column4Calculation}" valueMessagePrefix="balanceReportFormat.calculationOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
        </tr>

        <tr class="prop">
            <td class="name">
                <label for="column1SecondaryData"><g:msg code="balanceReportFormat.column1SecondaryData.label" default="Secondary Data"/></label>&nbsp;<g:help code="balanceReportFormat.column1SecondaryData"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column1SecondaryData', 'errors')}">
                <g:select tabindex="800" id="column1SecondaryData" name="column1SecondaryData" from="${BalanceReportFormat.DATA_OPTIONS}" value="${balanceReportFormatInstance.column1SecondaryData}" valueMessagePrefix="balanceReportFormat.dataOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column2SecondaryData', 'errors')}">
                <g:select tabindex="1300" id="column2SecondaryData" name="column2SecondaryData" from="${BalanceReportFormat.DATA_OPTIONS}" value="${balanceReportFormatInstance.column2SecondaryData}" valueMessagePrefix="balanceReportFormat.dataOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column3SecondaryData', 'errors')}">
                <g:select tabindex="1800" id="column3SecondaryData" name="column3SecondaryData" from="${BalanceReportFormat.DATA_OPTIONS}" value="${balanceReportFormatInstance.column3SecondaryData}" valueMessagePrefix="balanceReportFormat.dataOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
            <td class="value ${hasErrors(bean: balanceReportFormatInstance, field: 'column4SecondaryData', 'errors')}">
                <g:select tabindex="2300" id="column4SecondaryData" name="column4SecondaryData" from="${BalanceReportFormat.DATA_OPTIONS}" value="${balanceReportFormatInstance.column4SecondaryData}" valueMessagePrefix="balanceReportFormat.dataOptions" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>
            </td>
        </tr>
        </tbody>
    </table>
    </g:compressor>
</div>
