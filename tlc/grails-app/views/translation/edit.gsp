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
    <title><g:msg code="edit" domain="translation"/></title>
</head>
<body>
<div class="nav" role="navigation">
    <span class="menuButton"><a class="home" href="${createLink(uri: '/')}"><g:msg code="default.home.label" default="Home"/></a></span>
    <span class="menuButton"><g:link class="menu" controller="systemMenu" action="display"><g:msg code="systemMenu.display" default="Menu"/></g:link></span>
    <span class="menuButton"><g:link class="list" action="list" params="${translationInstance.data}"><g:msg code="list" domain="translation"/></g:link></span>
</div>
<div id="main-content" class="body" role="main">
    <g:pageTitle code="edit" domain="translation"/>
    <g:if test="${flash.message}">
        <div class="message" role="status">${flash.message.encodeAsHTML()}${flash.clear()}</div>
    </g:if>
    <g:hasErrors bean="${systemMessageInstance}">
        <div class="errors" role="alert">
            <g:listErrors bean="${systemMessageInstance}"/>
        </div>
    </g:hasErrors>
    <div class="center smallBottomMargin">${translationInstance.text.encodeAsHTML()}</div>
    <g:form method="post">
        <input type="hidden" name="id" value="${systemMessageInstance?.id}"/>
        <input type="hidden" name="version" value="${systemMessageInstance?.version}"/>
        <g:mapAsFields params="${translationInstance.data}"/>
        <input type="hidden" name="translatedMessageId" value="${translatedMessageId}"/>
		<div class="dialog">
		    <table>
		        <tbody>

		        <tr class="prop">
		            <td class="name">
		                <label for="code"><g:msg code="systemMessage.code.label" default="Code"/></label>
		            </td>
		            <td class="value ${hasErrors(bean: systemMessageInstance, field: 'code', 'errors')}">
		                <input disabled="disabled" type="text" maxlength="250" size="40" id="code" name="code" value="${display(bean: systemMessageInstance, field: 'code')}"/>
		            </td>
		        </tr>
		
		        <tr class="prop">
		            <td class="name vtop">
		                <label for="text"><g:msg code="systemMessage.text.label" default="Text"/></label>
		            </td>
		            <td class="value largeArea ${hasErrors(bean: systemMessageInstance, field: 'text', 'errors')}">
		                <textarea disabled="disabled" rows="5" cols="40" name="text">${display(bean: systemMessageInstance, field: 'text')}</textarea>
		            </td>
		        </tr>
        
                <tr class="prop">
                    <td class="name vtop">
                        <label for="translation"><g:msg code="translation.translation.label" default="Translation"/></label>
                    </td>
                    <td class="value largeArea${(translatedTextError ? ' errors' : '')}">
                        <textarea autofocus="autofocus" rows="5" cols="40" name="translatedText">${display(value: translatedText)}</textarea>
                        <g:help code="translation.translation"/>
                    </td>
                </tr>
        
                <tr class="prop">
                    <td class="name vtop">
                        <label for="propagate"><g:msg code="translation.propagate.label" default="Propagate"/></label>
                    </td>
                    <td class="value">
                           <g:checkBox name="propagate" value="${propagate}"></g:checkBox>&nbsp;<g:help code="translation.propagate"/>
                    </td>
                </tr>
		
		        </tbody>
		    </table>
		</div>
        <div class="buttons">
            <span class="button"><g:actionSubmit class="save" action="Update" value="${msg(code:'default.button.update.label', 'default':'Update')}"/></span>
            <g:if test="${systemMessageInstance.locale == translationInstance.toLocale || translatedMessageId}">
                <span class="button"><g:actionSubmit class="delete" onclick="return confirm('${msg(code:'translation.deletion.warning', 'default':'Are you sure?')}');" action="Delete" value="${msg(code:'default.button.delete.label', 'default':'Delete')}"/></span>
            </g:if>
        </div>
    </g:form>
</div>
</body>
</html>
