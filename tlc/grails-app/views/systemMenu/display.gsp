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
    <%--
    The following tag is decribed in more detail further down this page.
    --%>
    <title><g:menuTitle default="Main Menu"/></title>
</head>
<body>
<%--
The following two tags are special to the menu page. They automatically
reset the drilldowns stack and criteria in acknowldgement that the user
has returned to a menu page.
--%>
<g:drilldownReset/>
<g:criteriaReset/>
<g:filterReset/>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
</div>
<div id="main-content" class="body" role="main">
    <%--
    The following tag (also used in the <title> region above) is specific
    to the menu page and determines what title to display. The default is
    only used for the main menu, all sub-menus have their title in their
    domain record.
    --%>
    <h1><g:menuTitle default="Main Menu"/></h1>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <div class="crumbs">
        <%--
        The following tag is specific to the menu page and is used to display
        a breadcrumb trail so that the user may move backwords through the
        menu hierarchy. The default is used only for the main menu since all
        sub-menus use the last node in their path as their own crumb. By
        default, when the main menu is displayed, no breadcrumb trail is displayed
        since there is nowhere to go back to. However, you can force a breadcrumb
        trail to be displayed even for the main menu by including an attribute of
        single="true" in the following tag.
        --%>
        <g:menuCrumbs default="Main"/>
    </div>
    <div class="options">
        <ul>
            <g:each in="${optionList}" var="option">
            <%--
            See the menuOptions.properties file in the i18n directory
            of your application to determine how to internationalize
            your menus.
            --%>
                <li><g:link action="${(option.type == 'submenu') ? 'display' : 'execute'}" id="${option.id}"><g:msg code="menu.option.${option.path}" default="${option.title}"/>
                    <g:if test="${option.type == 'submenu'}">
                        &nbsp;<img src="${resource(dir: 'images', file: 'submenu.gif')}" alt="${msg(code: 'generic.submenu.indicator', default: 'Sub-menu indicator')}" class="borderless"/>
                    </g:if>
                </g:link></li>
            </g:each>
        </ul>
    </div>
</div>
</body>
</html>
