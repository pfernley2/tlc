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
    <meta name="generator" content="company"/>
    <title><g:msg code="paymentSchedule.test" default="Test Payment Schedule"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list"><g:msg code="list" domain="paymentSchedule"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="paymentSchedule.test" default="Test Payment Schedule"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${paymentScheduleInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${paymentScheduleInstance}"/>
        </div>
    </g:hasErrors>
    <g:form method="post">
        <div class="dialog">
            <table>
                <tbody>

                <tr class="prop">
                    <td class="name">
                        <label for="id"><g:msg code="paymentSchedule.test.schedule.label" default="Payment Schedule"/></label>
                    </td>
                    <td class="value ${hasErrors(bean: paymentScheduleInstance, field: 'id', 'errors')}">
                        <g:domainSelect autofocus="autofocus" name="id" options="${scheduleList}" selected="${paymentScheduleInstance?.id}" prefix="paymentSchedule.name" code="code" default="name"/>&nbsp;<g:help code="paymentSchedule.test.schedule"/>
                    </td>
                </tr>

                <g:if test="${predictions}">
                    <tr class="prop">
                        <td class="name">
                            <g:msg code="paymentSchedule.test.results" default="Results"/>:
                        </td>
                        <td></td>
                    </tr>
                    <g:each in="${predictions}" var="prediction">
                        <tr>

                            <td></td>
                            <td class="value">
                                <g:format value="${prediction}" scale="1"/>
                            </td>

                        </tr>
                    </g:each>
                </g:if>

                </tbody>
            </table>
        </div>
        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="testing" value="${msg(code:'paymentSchedule.test.button', 'default':'Test')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
