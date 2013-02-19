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
<%@ page contentType="text/html" %>
<body style="font-family:arial;font-size:12px;">
<p><g:msg code="generic.salutation" args="${[systemUserInstance.name]}" default="Dear ${systemUserInstance.name},"/></p>
<p><g:msg code="remittance.email.message" args="${[companyInstance.name]}" default="The following remittances were rejected for automatic payment by ${companyInstance.name}:"/></p>
<table>
    <tbody>
    <g:each in="${rejections}" var="rejection">
        <td>${rejection.encodeAsHTML()}</td>
    </g:each>
    </tbody>
</table>
<p><g:msg code="generic.company"/></p>
</body>
