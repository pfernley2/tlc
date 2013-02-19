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
    <title><g:msg code="accessGroup.membership" default="Edit Access Group Membership"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="members"><g:msg code="accessGroup.members" default="Access Group Member List"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="accessGroup.membership.for" args="${[ddSource.name]}" default="Edit Membership for Access Group ${ddSource.name}"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:form action="process" method="post">
        <p>&nbsp;</p>
        <p><g:msg code="generic.multi.select" default="Use Ctrl-click to add or remove selections. Shift-click will add or remove ranges of selections. A mouse click on its own will select just the clicked item, deselecting all others. If you make a mistake, use the Reset button at the bottom of the page to reset the list to its original state."/></p>
        <p>&nbsp;</p>
        <div class="wideLeftMargin">
            <g:domainSelect name="linkages" options="${userList}" selected="${memberList}" displays="${['name', 'loginId']}" size="30"/>
        </div>
        <div class="buttons">
            <span class="button"><input class="save" type="submit" value="${msg(code: 'generic.save', 'default': 'Save')}"/></span>
            <span class="button"><input class="reset" type="reset" value="${msg(code: 'generic.reset', 'default': 'Reset')}"/></span>
        </div>
    </g:form>
</div>
</body>
</html>
