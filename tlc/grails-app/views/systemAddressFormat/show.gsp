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
<%@ page import="org.grails.tlc.sys.SystemAddressFormat" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="system"/>
    <title><g:msg code="show" domain="systemAddressFormat"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="systemAddressFormat"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="systemAddressFormat"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="systemAddressFormat"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                    <td class="value">${display(bean: systemAddressFormatInstance, field: 'id')}</td>

                </tr>
            </g:permit>


            <tr class="prop">
                <td class="name"><g:msg code="systemAddressFormat.code.label" default="Code"/></td>

                <td class="value">${display(bean: systemAddressFormatInstance, field: 'code')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="systemAddressFormat.name.label" default="Name"/></td>

                <td class="value"><g:msg code="systemAddressFormat.name.${systemAddressFormatInstance.code}" default="${systemAddressFormatInstance.name}"/></td>

            </tr>

            <g:permit activity="sysadmin">
                <tr class="prop">
                    <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                    <td class="value">${display(bean: systemAddressFormatInstance, field: 'securityCode')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                    <td class="value">${display(bean: systemAddressFormatInstance, field: 'dateCreated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                    <td class="value">${display(bean: systemAddressFormatInstance, field: 'lastUpdated')}</td>

                </tr>



                <tr class="prop">
                    <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                    <td class="value">${display(bean: systemAddressFormatInstance, field: 'version')}</td>

                </tr>
            </g:permit>

            </tbody>
        </table>
        <table>
            <thead>
            <tr>
                <th><g:msg code="systemAddressFormat.field.label" default="Field"/></th>
                <th><g:msg code="systemAddressFormat.prompt.label" default="Prompts"/></th>
                <th><g:msg code="systemAddressFormat.width.label" default="Width"/></th>
                <th><g:msg code="systemAddressFormat.mandatory.label" default="Mandatory"/></th>
                <th><g:msg code="systemAddressFormat.pattern.label" default="Pattern"/></th>
                <th><g:msg code="systemAddressFormat.joinBy.label" default="Join By"/></th>
            </tr>
            </thead>
            <tbody>

            <tr>
                <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field1}" default="${systemAddressFormatInstance.field1}"/></td>
                <td><g:if test="${systemAddressFormatInstance.field1Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field1Prompt1}" default="${systemAddressFormatInstance.field1Prompt1}"/></g:if></td>
                <td>${display(bean: systemAddressFormatInstance, field: 'width1')}</td>
                <td class="center"><g:if test="${systemAddressFormatInstance.field1Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory1')}</g:if></td>
                <td>${display(bean: systemAddressFormatInstance, field: 'pattern1')}</td>
                <td></td>
            </tr>
            <g:if test="${systemAddressFormatInstance.field1Prompt2}">
                <tr>
                    <td></td>
                    <td><g:if test="${systemAddressFormatInstance.field1Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field1Prompt2}" default="${systemAddressFormatInstance.field1Prompt2}"/></g:if></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
            </g:if>
            <g:if test="${systemAddressFormatInstance.field1Prompt3}">
                <tr>
                    <td></td>
                    <td><g:if test="${systemAddressFormatInstance.field1Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field1Prompt3}" default="${systemAddressFormatInstance.field1Prompt3}"/></g:if></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
            </g:if>

            <tr>
                <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field2}" default="${systemAddressFormatInstance.field2}"/></td>
                <td><g:if test="${systemAddressFormatInstance.field2Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field2Prompt1}" default="${systemAddressFormatInstance.field2Prompt1}"/></g:if></td>
                <td>${display(bean: systemAddressFormatInstance, field: 'width2')}</td>
                <td class="center"><g:if test="${systemAddressFormatInstance.field2Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory2')}</g:if></td>
                <td>${display(bean: systemAddressFormatInstance, field: 'pattern2')}</td>
                <td>${display(bean: systemAddressFormatInstance, field: 'joinBy2')}</td>
            </tr>
            <g:if test="${systemAddressFormatInstance.field2Prompt2}">
                <tr>
                    <td></td>
                    <td><g:if test="${systemAddressFormatInstance.field2Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field2Prompt2}" default="${systemAddressFormatInstance.field2Prompt2}"/></g:if></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
            </g:if>
            <g:if test="${systemAddressFormatInstance.field2Prompt3}">
                <tr>
                    <td></td>
                    <td><g:if test="${systemAddressFormatInstance.field2Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field2Prompt3}" default="${systemAddressFormatInstance.field2Prompt3}"/></g:if></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
            </g:if>

            <tr>
                <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field3}" default="${systemAddressFormatInstance.field3}"/></td>
                <td><g:if test="${systemAddressFormatInstance.field3Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field3Prompt1}" default="${systemAddressFormatInstance.field3Prompt1}"/></g:if></td>
                <td>${display(bean: systemAddressFormatInstance, field: 'width3')}</td>
                <td class="center"><g:if test="${systemAddressFormatInstance.field3Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory3')}</g:if></td>
                <td>${display(bean: systemAddressFormatInstance, field: 'pattern3')}</td>
                <td>${display(bean: systemAddressFormatInstance, field: 'joinBy3')}</td>
            </tr>
            <g:if test="${systemAddressFormatInstance.field3Prompt2}">
                <tr>
                    <td></td>
                    <td><g:if test="${systemAddressFormatInstance.field3Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field3Prompt2}" default="${systemAddressFormatInstance.field3Prompt2}"/></g:if></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
            </g:if>
            <g:if test="${systemAddressFormatInstance.field3Prompt3}">
                <tr>
                    <td></td>
                    <td><g:if test="${systemAddressFormatInstance.field3Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field3Prompt3}" default="${systemAddressFormatInstance.field3Prompt3}"/></g:if></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
            </g:if>

            <tr>
                <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field4}" default="${systemAddressFormatInstance.field4}"/></td>
                <td><g:if test="${systemAddressFormatInstance.field4Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field4Prompt1}" default="${systemAddressFormatInstance.field4Prompt1}"/></g:if></td>
                <td>${display(bean: systemAddressFormatInstance, field: 'width4')}</td>
                <td class="center"><g:if test="${systemAddressFormatInstance.field4Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory4')}</g:if></td>
                <td>${display(bean: systemAddressFormatInstance, field: 'pattern4')}</td>
                <td>${display(bean: systemAddressFormatInstance, field: 'joinBy4')}</td>
            </tr>
            <g:if test="${systemAddressFormatInstance.field4Prompt2}">
                <tr>
                    <td></td>
                    <td><g:if test="${systemAddressFormatInstance.field4Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field4Prompt2}" default="${systemAddressFormatInstance.field4Prompt2}"/></g:if></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
            </g:if>
            <g:if test="${systemAddressFormatInstance.field4Prompt3}">
                <tr>
                    <td></td>
                    <td><g:if test="${systemAddressFormatInstance.field4Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field4Prompt3}" default="${systemAddressFormatInstance.field4Prompt3}"/></g:if></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
            </g:if>

            <tr>
                <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field5}" default="${systemAddressFormatInstance.field5}"/></td>
                <td><g:if test="${systemAddressFormatInstance.field5Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field5Prompt1}" default="${systemAddressFormatInstance.field5Prompt1}"/></g:if></td>
                <td>${display(bean: systemAddressFormatInstance, field: 'width5')}</td>
                <td class="center"><g:if test="${systemAddressFormatInstance.field5Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory5')}</g:if></td>
                <td>${display(bean: systemAddressFormatInstance, field: 'pattern5')}</td>
                <td>${display(bean: systemAddressFormatInstance, field: 'joinBy5')}</td>
            </tr>
            <g:if test="${systemAddressFormatInstance.field5Prompt2}">
                <tr>
                    <td></td>
                    <td><g:if test="${systemAddressFormatInstance.field5Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field5Prompt2}" default="${systemAddressFormatInstance.field5Prompt2}"/></g:if></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
            </g:if>
            <g:if test="${systemAddressFormatInstance.field5Prompt3}">
                <tr>
                    <td></td>
                    <td><g:if test="${systemAddressFormatInstance.field5Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field5Prompt3}" default="${systemAddressFormatInstance.field5Prompt3}"/></g:if></td>
                    <td></td>
                    <td></td>
                    <td></td>
                    <td></td>
                </tr>
            </g:if>

            <g:if test="${systemAddressFormatInstance.field6}">
                <tr>
                    <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field6}" default="${systemAddressFormatInstance.field6}"/></td>
                    <td><g:if test="${systemAddressFormatInstance.field6Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field6Prompt1}" default="${systemAddressFormatInstance.field6Prompt1}"/></g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'width6')}</td>
                    <td class="center"><g:if test="${systemAddressFormatInstance.field6Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory6')}</g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'pattern6')}</td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'joinBy6')}</td>
                </tr>
                <g:if test="${systemAddressFormatInstance.field6Prompt2}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field6Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field6Prompt2}" default="${systemAddressFormatInstance.field6Prompt2}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
                <g:if test="${systemAddressFormatInstance.field6Prompt3}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field6Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field6Prompt3}" default="${systemAddressFormatInstance.field6Prompt3}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
            </g:if>

            <g:if test="${systemAddressFormatInstance.field7}">
                <tr>
                    <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field7}" default="${systemAddressFormatInstance.field7}"/></td>
                    <td><g:if test="${systemAddressFormatInstance.field7Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field7Prompt1}" default="${systemAddressFormatInstance.field7Prompt1}"/></g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'width7')}</td>
                    <td class="center"><g:if test="${systemAddressFormatInstance.field7Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory7')}</g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'pattern7')}</td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'joinBy7')}</td>
                </tr>
                <g:if test="${systemAddressFormatInstance.field7Prompt2}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field7Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field7Prompt2}" default="${systemAddressFormatInstance.field7Prompt2}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
                <g:if test="${systemAddressFormatInstance.field7Prompt3}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field7Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field7Prompt3}" default="${systemAddressFormatInstance.field7Prompt3}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
            </g:if>

            <g:if test="${systemAddressFormatInstance.field8}">
                <tr>
                    <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field8}" default="${systemAddressFormatInstance.field8}"/></td>
                    <td><g:if test="${systemAddressFormatInstance.field8Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field8Prompt1}" default="${systemAddressFormatInstance.field8Prompt1}"/></g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'width8')}</td>
                    <td class="center"><g:if test="${systemAddressFormatInstance.field8Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory8')}</g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'pattern8')}</td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'joinBy8')}</td>
                </tr>
                <g:if test="${systemAddressFormatInstance.field8Prompt2}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field8Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field8Prompt2}" default="${systemAddressFormatInstance.field8Prompt2}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
                <g:if test="${systemAddressFormatInstance.field8Prompt3}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field8Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field8Prompt3}" default="${systemAddressFormatInstance.field8Prompt3}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
            </g:if>

            <g:if test="${systemAddressFormatInstance.field9}">
                <tr>
                    <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field9}" default="${systemAddressFormatInstance.field9}"/></td>
                    <td><g:if test="${systemAddressFormatInstance.field9Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field9Prompt1}" default="${systemAddressFormatInstance.field9Prompt1}"/></g:if></td>
                    <td class="center">${display(bean: systemAddressFormatInstance, field: 'width9')}</td>
                    <td><g:if test="${systemAddressFormatInstance.field9Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory9')}</g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'pattern9')}</td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'joinBy9')}</td>
                </tr>
                <g:if test="${systemAddressFormatInstance.field9Prompt2}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field9Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field9Prompt2}" default="${systemAddressFormatInstance.field9Prompt2}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
                <g:if test="${systemAddressFormatInstance.field9Prompt3}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field9Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field9Prompt3}" default="${systemAddressFormatInstance.field9Prompt3}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
            </g:if>

            <g:if test="${systemAddressFormatInstance.field10}">
                <tr>
                    <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field10}" default="${systemAddressFormatInstance.field10}"/></td>
                    <td><g:if test="${systemAddressFormatInstance.field10Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field10Prompt1}" default="${systemAddressFormatInstance.field10Prompt1}"/></g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'width10')}</td>
                    <td class="center"><g:if test="${systemAddressFormatInstance.field10Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory10')}</g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'pattern10')}</td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'joinBy10')}</td>
                </tr>
                <g:if test="${systemAddressFormatInstance.field10Prompt2}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field10Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field10Prompt2}" default="${systemAddressFormatInstance.field10Prompt2}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
                <g:if test="${systemAddressFormatInstance.field10Prompt3}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field10Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field10Prompt3}" default="${systemAddressFormatInstance.field10Prompt3}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
            </g:if>

            <g:if test="${systemAddressFormatInstance.field11}">
                <tr>
                    <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field11}" default="${systemAddressFormatInstance.field11}"/></td>
                    <td><g:if test="${systemAddressFormatInstance.field11Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field11Prompt1}" default="${systemAddressFormatInstance.field11Prompt1}"/></g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'width11')}</td>
                    <td class="center"><g:if test="${systemAddressFormatInstance.field11Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory11')}</g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'pattern11')}</td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'joinBy11')}</td>
                </tr>
                <g:if test="${systemAddressFormatInstance.field11Prompt2}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field11Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field11Prompt2}" default="${systemAddressFormatInstance.field11Prompt2}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
                <g:if test="${systemAddressFormatInstance.field11Prompt3}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field11Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field11Prompt3}" default="${systemAddressFormatInstance.field11Prompt3}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
            </g:if>

            <g:if test="${systemAddressFormatInstance.field12}">
                <tr>
                    <td><g:msg code="systemAddressFormat.field.${systemAddressFormatInstance.field12}" default="${systemAddressFormatInstance.field12}"/></td>
                    <td><g:if test="${systemAddressFormatInstance.field12Prompt1}"><g:msg code="address.prompt.${systemAddressFormatInstance.field12Prompt1}" default="${systemAddressFormatInstance.field12Prompt1}"/></g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'width12')}</td>
                    <td class="center"><g:if test="${systemAddressFormatInstance.field12Prompt1}">${display(bean: systemAddressFormatInstance, field: 'mandatory12')}</g:if></td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'pattern12')}</td>
                    <td>${display(bean: systemAddressFormatInstance, field: 'joinBy12')}</td>
                </tr>
                <g:if test="${systemAddressFormatInstance.field12Prompt2}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field12Prompt2}"><g:msg code="address.prompt.${systemAddressFormatInstance.field12Prompt2}" default="${systemAddressFormatInstance.field12Prompt2}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
                <g:if test="${systemAddressFormatInstance.field12Prompt3}">
                    <tr>
                        <td></td>
                        <td><g:if test="${systemAddressFormatInstance.field12Prompt3}"><g:msg code="address.prompt.${systemAddressFormatInstance.field12Prompt3}" default="${systemAddressFormatInstance.field12Prompt3}"/></g:if></td>
                        <td></td>
                        <td></td>
                        <td></td>
                        <td></td>
                    </tr>
                </g:if>
            </g:if>
            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${systemAddressFormatInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
