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
<%@ page import="org.grails.tlc.obj.Translation; org.grails.tlc.sys.SystemLanguage; org.grails.tlc.sys.SystemCountry" %>
<!doctype html>
<html>
<head>
    <title><g:msg code="translation.selectors.title" default="Translation Selectors"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="translation.selectors.title" default="Translation Selectors"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${translationInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${translationInstance}"/>
        </div>
    </g:hasErrors>
    <g:compressor>
    <g:form action="list" method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="toLanguage.id"><g:msg code="translation.toLanguage.label" default="To Language"/></label>
                    </td>
                    <td class="nowrap value ${hasErrors(bean: translationInstance, field: 'toLanguage', 'errors')}">
                        <g:domainSelect autofocus="autofocus" name="toLanguage.id" options="${SystemLanguage.list()}" selected="${translationInstance?.toLanguage}" prefix="language.name" code="code" default="name"/>&nbsp;<g:help code="translation.toLanguage"/>
                    </td>
                    
                    <td class="name">
                        <label for="toCountry.id"><g:msg code="translation.country" default="Country"/></label>
                    </td>
                    <td class="nowrap value ${hasErrors(bean: translationInstance, field: 'toCountry', 'errors')}">
                        <g:domainSelect name="toCountry.id" options="${SystemCountry.list()}" selected="${translationInstance?.toCountry}" prefix="country.name" code="code" default="name" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="translation.toCountry"/>
                    </td>
                    
                    <td></td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="fromLanguage.id"><g:msg code="translation.fromLanguage.label" default="From Language"/></label>
                    </td>
                    <td class="nowrap value ${hasErrors(bean: translationInstance, field: 'fromLanguage', 'errors')}">
                        <g:domainSelect name="fromLanguage.id" options="${SystemLanguage.list()}" selected="${translationInstance?.fromLanguage}" prefix="language.name" code="code" default="name" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="translation.fromLanguage"/>
                    </td>
                    
                    <td class="name">
                        <label for="fromCountry.id"><g:msg code="translation.country" default="Country"/></label>
                    </td>
                    <td class="nowrap value ${hasErrors(bean: translationInstance, field: 'fromCountry', 'errors')}">
                        <g:domainSelect name="fromCountry.id" options="${SystemCountry.list()}" selected="${translationInstance?.fromCountry}" prefix="country.name" code="code" default="name" noSelection="['': msg(code: 'generic.no.selection', default: '-- none --')]"/>&nbsp;<g:help code="translation.fromCountry"/>
                    </td>
                    
                    <td class="name">
                        <label for="strict"><g:msg code="translation.strict.label" default="Strict"/></label>
                    </td>
                    <td class="nowrap value ${hasErrors(bean: translationInstance, field: 'strict', 'errors')}">
                        <g:checkBox name="strict" value="${translationInstance?.strict}"></g:checkBox>&nbsp;<g:help code="translation.strict"/>
                    </td>
                </tr>

                <tr class="prop">
                    <td class="name">
                        <label for="display"><g:msg code="translation.display.label" default="Display"/></label>
                    </td>
                    <td class="nowrap value ${hasErrors(bean: translationInstance, field: 'display', 'errors')}">
                        <g:select id="display" name="display" from="${Translation.constraints.display.inList}" value="${translationInstance?.display}" valueMessagePrefix="translation.display"/>&nbsp;<g:help code="translation.display"/>
                    </td>
                    
                    <td></td>
                    
                    <td></td>
                </tr>

                </tbody>
            </table>
        </div>
        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="${msg(code: 'translation.select.button', 'default': 'Select')}"/></span>
        </div>
    </g:form>
    </g:compressor>
</div>
</body>
</html>
