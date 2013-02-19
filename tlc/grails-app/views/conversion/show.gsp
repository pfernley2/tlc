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
<%@ page import="org.grails.tlc.corp.Conversion" %>
<!doctype html>
<html>
<head>
    <meta name="generator" content="company"/>
    <title><g:msg code="show" domain="conversion"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="conversion"/></g:link></span>
    <span class="menuButton"><g:link class="create" action="create"><g:msg code="new" domain="conversion"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="show" domain="conversion"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="dialog">
        <table>
            <tbody>

            <g:permit activity="sysadmin">
            <tr class="prop">
                <td class="name"><g:msg code="generic.id.label" default="Id"/></td>

                <td class="value">${display(bean:conversionInstance, field:'id')}</td>

            </tr>
            </g:permit>


            <tr class="prop">
                <td class="name"><g:msg code="conversion.code.label" default="Code"/></td>

                <td class="value">${display(bean:conversionInstance, field:'code')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="conversion.name.label" default="Name"/></td>

                <td class="value"><g:msg code="conversion.name.${conversionInstance.code}" default="${conversionInstance.name}"/></td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="conversion.source.label" default="Source"/></td>

                <td class="value"><g:msg code="unit.name.${conversionInstance.source.code}" default="${conversionInstance.source.name}"/></td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="conversion.target.label" default="Target"/></td>

                <td class="value"><g:msg code="unit.name.${conversionInstance.target.code}" default="${conversionInstance.target.name}"/></td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="conversion.preAddition.label" default="Pre Addition"/></td>

                <td class="value">${display(bean:conversionInstance, field:'preAddition')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="conversion.multiplier.label" default="Multiplier"/></td>

                <td class="value">${display(bean:conversionInstance, field:'multiplier')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="conversion.postAddition.label" default="Post Addition"/></td>

                <td class="value">${display(bean:conversionInstance, field:'postAddition')}</td>

            </tr>


            <g:permit activity="sysadmin">
            <tr class="prop">
                <td class="name"><g:msg code="generic.securityCode.label" default="Security Code"/></td>

                <td class="value">${display(bean:conversionInstance, field:'securityCode')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="generic.dateCreated.label" default="Date Created"/></td>

                <td class="value">${display(bean:conversionInstance, field:'dateCreated')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="generic.lastUpdated.label" default="Last Updated"/></td>

                <td class="value">${display(bean:conversionInstance, field:'lastUpdated')}</td>

            </tr>



            <tr class="prop">
                <td class="name"><g:msg code="generic.version.label" default="Version"/></td>

                <td class="value">${display(bean:conversionInstance, field:'version')}</td>

            </tr>
            </g:permit>

            </tbody>
        </table>
    </div>
    <div class="buttons">
        <g:form>
            <input type="hidden" name="id" value="${conversionInstance?.id}"/>
            <span class="button"><g:actionSubmit class="edit" action="Edit" value="${msg(code:'default.button.edit.label', 'default':'Edit')}"/></span>
            <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'default.button.delete.confirm.message', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
        </g:form>
    </div>
</div>
</body>
</html>
