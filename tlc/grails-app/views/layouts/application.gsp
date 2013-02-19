<!doctype html>
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
<!--
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
 ~  along with TLC. If not, see http://www.gnu.org/licenses.
 -->
<!--[if lt IE 7 ]> <html lang="en" class="no-js ie6"> <![endif]-->
<!--[if IE 7 ]>    <html lang="en" class="no-js ie7"> <![endif]-->
<!--[if IE 8 ]>    <html lang="en" class="no-js ie8"> <![endif]-->
<!--[if IE 9 ]>    <html lang="en" class="no-js ie9"> <![endif]-->
<!--[if (gt IE 9)|!(IE)]><!--> <html lang="en" class="no-js"><!--<![endif]-->
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <meta http-equiv="X-UA-Compatible" content="IE=edge,chrome=1">
    <title><g:layoutTitle default="TLC by Paul Fernley"/></title>
    <link rel="shortcut icon" href="${resource(dir: 'images', file: 'favicon.ico')}" type="image/x-icon"/>
    <script type="text/javascript">
        var fieldHelpCloseURL = '${g.resource(dir: 'images', file: 'balloon-button.png')}';
        var fieldHelpCloseAlt = '${g.message(code: 'fieldHelp.close.label', default: 'Close', encodeAs: 'JavaScript')}';
        var ajaxErrorPrefix = '${g.message(code: 'generic.ajax.prefix', default: 'AJAX error', encodeAs: 'JavaScript')}';
        var ajaxErrorTimeout = '${g.message(code: 'generic.ajax.timeout', default: 'Timed out waiting for the server', encodeAs: 'JavaScript')}';
        var ajaxErrorServer = '${g.message(code: 'generic.ajax.error', default: 'The server encountered an error. The message from the server was {0}', encodeAs: 'JavaScript')}';
        var ajaxErrorAbort = '${g.message(code: 'generic.ajax.abort', default: 'The request to the server was aborted', encodeAs: 'JavaScript')}';
        var ajaxErrorParse = '${g.message(code: 'generic.ajax.response', default: 'Could not understand the reply received from the server', encodeAs: 'JavaScript')}';
        var ajaxErrorDefault = '${g.message(code: 'generic.ajax.default', default: 'An unspecified error occurred communicating with the server (the error code was {0})', encodeAs: 'JavaScript')}';
    </script>
    <r:require module="core"/>
    <g:layoutHead/>
	<r:layoutResources/>
</head>
<body>
<a href="#main-content" class="skip" tabindex="-1"><g:message code="default.link.skip.label" default="Skip to content&hellip;"/></a>
<div id="topbar">
    <div id="menu">
        <span class="nowrap">
            <g:if test="${userName()}">
                <g:formatHelp/>&nbsp;&nbsp;<g:link controller="systemUser" action="profile"><g:userName/></g:link>&nbsp;|&nbsp;<g:link controller="systemUser" action="logout"><g:msg code="topbar.logout" default="Logout"/></g:link>
            </g:if>
            <g:else>
                <g:link controller="systemUser" action="login"><g:msg code="topbar.login" default="Login"/></g:link>&nbsp;|&nbsp;<g:link controller="systemUser" action="register"><g:msg code="topbar.register" default="Register"/></g:link>
            </g:else>
        </span>
    </div>
</div>
<div class="logo">
    <img src="${resource(dir: 'images/logos', file: companyLogo())}" alt="${msg(code: 'topbar.logo', default: 'Logo')}" width="48" height="48"/>
    <g:if test="${companyName()}">
        <g:companyName/>
    </g:if>
    <g:else>
        <g:msg code="generic.company"/>
    </g:else>
</div>
<g:layoutBody/>
<p>&nbsp;</p>
<div id="footer">Copyright 2010-2013 Paul Fernley</div>
<r:layoutResources/>
</body>
</html>