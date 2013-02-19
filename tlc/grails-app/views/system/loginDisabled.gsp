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
    <meta name="layout" content="basic"/>
    <title><g:msg code="operation.loginDisabled.title" default="Login/Registration Disabled"/></title>
</head>
<body>
<g:pageTitle code="operation.loginDisabled.title" default="Login/Registration Disabled"/>
<div id="main-content" role="main" style="text-align:center;">
    <img src="${resource(dir: 'images', file: 'wip.jpg')}" alt="${msg(code: 'generic.wip.alt.text', default: 'Roadworks road sign')}" style="border:0;"/>
    <p>&nbsp;</p>
    <strong><g:msg code="operation.loginDisabled.text" default="We are sorry, but the system is undergoing maintenance at the moment and login/registration has been temporarily suspended. Please try again later."/></strong>
</div>
</body>
</html>
