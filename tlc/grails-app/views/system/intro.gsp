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
    <title><g:msg code="companyUser.intro.title" default="Introduction to the Demonstration System"/></title>
</head>
<body>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="companyUser.intro.title" default="Introduction to the Demonstration System"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${companyInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${companyInstance}"/>
        </div>
    </g:hasErrors>
    <div class="largeTopMargin">
        <p><g:msg code="companyUser.intro.text1" default="Welcome to our demonstration system. You may either create a new test company or join an existing test company. Choose from the options below:"/></p>
        <h2><g:msg code="companyUser.subhead2" default="Creating a New Test Company"/></h2>
        <p><g:msg code="companyUser.intro.text2" default="If you wish to create a test company for yourself, simply fill in the details below and then press the Create button."/></p>
        <div class="center smallTopMargin">
            <g:form action="create" method="post">
                <g:render template="/company/form" model="[companyInstance: companyInstance]"/>
                <input type="submit" value="${msg(code: 'default.button.create.label', 'default': 'Create')}"/>
            </g:form>
        </div>
        <h2><g:msg code="companyUser.subhead3" default="Joining an Existing Test Company"/></h2>
        <p><g:msg code="companyUser.intro.text3" default="Contact the person who created the test company you wish to join, giving them your login id, and ask them to assign you to the company. When they confirm they have done this, press the Continue button below."/></p>
        <div class="center smallTopMargin">
            <g:form action="attach" method="post">
                <input type="submit" value="${msg(code: 'companyUser.assign.button', 'default': 'Continue')}"/>
            </g:form>
        </div>
    </div>
</div>
</body>
</html>
