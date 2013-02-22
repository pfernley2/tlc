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
    <meta name="Keywords" content="accounting,accounts,accountant,bookkeeping,books of account,computerized accounting,grails,java,open source software"/>
    <meta name="Description" content="Open source accounting software from Paul Fernley"/>
    <title>Open Source Accounting Software From Paul Fernley</title>
    <script type="text/javascript">
        var fieldHelpCloseURL = '${g.resource(dir: 'images', file: 'balloon-button.png')}';
        var fieldHelpCloseAlt = '${g.message(code: 'fieldHelp.close.label', default: 'Close', encodeAs: 'JavaScript')}';
    </script>
    <r:require module="intro"/>
</head>
<body>
<div id="page">
    <g:menuReset/>
    <div id="topbar">
        <div id="menu">
            <span class="nowrap">
                <g:if test="${userName()}">
                    <g:formatHelp/>&nbsp;&nbsp;<g:link controller="systemUser" action="profile"><g:userName/></g:link>&nbsp;|&nbsp;<g:link controller="systemUser" action="logout"><g:msg code="topbar.logout" default="Logout"/></g:link>
                </g:if>
            </span>
        </div>
    </div>
    <div id="header">
        <img src="${resource(dir: 'images/logos', file: companyLogo())}" alt="${msg(code: 'topbar.logo', default: 'Logo')}" width="48" height="48"/>
        <g:if test="${companyName()}">
            <g:companyName/>
        </g:if>
        <g:else>
            <g:msg code="generic.company"/>
        </g:else>
    </div>
    <div id="main-content" role="main">
        <h1>Open Source Accounting Software</h1>
        <p>
            TLC from Paul Fernley is an Open Source three-ledger (GL, AR and AP) core accounting system designed for use by medium to large enterprises.
            Capable of working as a 'bookkeeping engine' in to which your existing applications can feed and retrieve accounting data, TLC comes complete
            with a fully functioning browser-based front-end for manual data entry plus an extensive reporting system including Trial Balance, Income and
            Expenditure Statement, Balance Sheet, detailed posting reports, Aged Lists of debtors and creditors etc. Its main features are outlined below.
        </p>
        <div id="bullets">
            <h2>Overview</h2>
            <ul>
                <li>Multi-Company</li>
                <li>Multi-Currency</li>
                <li>Multilingual</li>
                <li>Multi-User</li>
            </ul>
            <h2>General Ledger</h2>
            <ul>
                <li>Up to eight levels of GL analysis</li>
                <li>GL code defaults and mnemonics</li>
                <li>Any number of periods per year</li>
                <li>Multiple open periods</li>
                <li>Multi-year history retention</li>
                <li>Multiple bank and cash accounts</li>
                <li>Recurring bank transactions</li>
                <li>Bank reconciliation facility</li>
                <li>Foreign currency account revaluation</li>
                <li>Automatic exchange rate update, if required</li>
                <li>Recurring journal templates</li>
                <li>Auto-reversing provisions</li>
                <li>Budget recording and reporting</li>
                <li>Document searching and account enquiries</li>
            </ul>
            <h2>AR and AP Ledgers</h2>
            <ul>
                <li>Open-item accounts (with multi-year history)</li>
                <li>Multiple document types (e.g. Home and Export invoices)</li>
                <li>Generic Sales/Purchase/VAT taxation facilities</li>
                <li>Foreign exchange difference recording</li>
                <li>Manual or automatic invoice/cash allocations</li>
                <li>Intra-ledger and inter-ledger journals</li>
                <li>Customer statements</li>
                <li>Automatic supplier payments with remittance advices</li>
                <li>Account and document enquiry facilities</li>
            </ul>
            <h2>Security and Other Features</h2>
            <ul>
                <li>Roles control access to functionality</li>
                <li>Access Groups control access to data</li>
                <li>Role sensitive menu display</li>
                <li>Powerful system administration functions</li>
                <li>Extensive on-line help at 'page' and 'field' levels</li>
                <li>Immediate postings, no batch processing</li>
                <li>Background processing system for report creation etc.</li>
            </ul>
        </div>
        <p class="mediumTopMargin">
            TLC is written in <a href="http://www.grails.org">Grails</a>, a leading <a href="http://www.java.com">Java</a> based
        development framework, and was developed using <a href="http://www.mysql.com">MySQL</a> as its underlying database
        plus <a href="http://www.jasperforge.org/projects/jasperreports">JasperReports</a> for production of its PDF report
        output. TLC, MySQL and JasperReports are all freely available for download and use, as
        is <a href="http://www.jasperforge.org/projects/ireport">iReport</a>, the visual report designer for JasperReports.
        TLC is made available under the <a href="http://www.gnu.org/copyleft/gpl.html">GNU General Public License</a> version 3.
        </p>
        <p>&nbsp;</p>
    </div>
    <div id="sidebar">
        <h1>Fast Track</h1>
        <div id="actions">
            <h1>What do you want to do...</h1>
            <g:if test="${companyName()}">
                <p><g:link controller="systemMenu" action="display">Return to the application</g:link></p>
            </g:if>
            <g:else>
                <p><g:link controller="systemUser" action="login">Login as an existing user</g:link></p>
                <p><g:link controller="systemUser" action="register">Register as a new user</g:link></p>
            </g:else>
            <p><g:link controller="system" action="noticeFile">View the NOTICE file</g:link></p>
            <p><g:link controller="system" action="licenseFile">View the LICENSE file</g:link></p>
            <p><a href="${resource(dir: '/documentation', file: 'technotes.pdf')}">View technical installation notes</a></p>
            <p><a href="${resource(dir: '/documentation', file: 'accountnotes.pdf')}">View accounting set-up notes</a></p>
            <p><g:link controller="system" action="paymentProgram">View sample payment program</g:link></p>
        </div>
        <div id="rest">
            <h1>RESTful interface...</h1>
            <p><a href="${resource(dir: '/documentation', file: 'restnotes.pdf')}">View RESTful interface notes</a></p>
            <p><g:link controller="system" action="restProgram">View the test program</g:link></p>
        </div>
        <div id="status">
            <h1>Status information...</h1>
            <p>Current version: ${appVersion()}</p>
            <p>Is in demonstration mode: <img src="${resource(dir: 'images', file: isDemoSystem() + '.png')}" alt="Status indicator" width="10" height="10"/></p>
            <p><a href="${resource(dir: '/documentation', file: 'issues.pdf')}">View known issues</a></p>
            <p><a href="${resource(dir: '/documentation', file: 'changelog.pdf')}">View change log</a></p>
        </div>
    </div>
    <div id="footer">Copyright 2010-2013 Paul Fernley</div>
</div>
</body>
</html>