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
    <title><g:msg code="systemAddressFormat.test" default="Test Address Formats"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="systemAddressFormat"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="systemAddressFormat.test" default="Test Address Formats"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${customerAddressInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${customerAddressInstance}"/>
        </div>
    </g:hasErrors>
    <g:form name="jsform" action="testing" method="post">
        <g:if test="${result}">
            <input type="hidden" name="retest" id="retest" value="true"/>
            <input type="hidden" name="country.id" id="country.id" value="${customerAddressInstance.country.id}"/>
            <input type="hidden" name="format.id" id="format.id" value="${customerAddressInstance.format.id}"/>
            <div class="comment">
                <g:msg code="systemAddressFormat.valid" default="Click the Test button to perform another test."/>
            </div>
            <div class="center">
                <table>
                    <tbody>
                    <tr class="prop">
                        <td>
                            <g:each in="${result}" status="j" var="customerAddressLine">
                                <g:if test="${j}"><br/></g:if>${customerAddressLine?.encodeAsHTML()}
                            </g:each>
                        </td>
                    </tr>

                    </tbody>
                </table>
            </div>
            <p>&nbsp;</p>
        </g:if>
        <g:else>
            <input type="hidden" name="modified" id="modified" value=""/>
            <g:render template="/customerAddress/form" model="[customerAddressInstance: customerAddressInstance, customerAddressLines: customerAddressLines, transferList: transferList]"/>
        </g:else>
        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="${msg(code: 'systemAddressFormat.test.button', 'default': 'Test')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
