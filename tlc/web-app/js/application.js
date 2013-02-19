//  Copyright 2010-2013 Paul Fernley.
//
//  This file is part of the Three Ledger Core (TLC) software
//  from Paul Fernley.
//
//  TLC is free software: you can redistribute it and/or modify
//  it under the terms of the GNU General Public License as published by
//  the Free Software Foundation, either version 3 of the License, or
//  (at your option) any later version.
//
//  TLC is distributed in the hope that it will be useful,
//  but WITHOUT ANY WARRANTY; without even the implied warranty of
//  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//  GNU General Public License for more details.
//
//  You should have received a copy of the GNU General Public License
//  along with TLC. If not, see <http://www.gnu.org/licenses/>.
$(document).ready(function() {
	
	// Set up the special body colour, if any
	var code = $('meta[name=generator]').attr('content');
	if (code) document.body.className = code;

	// Set up any field helps
	$('a.fieldHelp').cluetip({activation: 'click', sticky: true, height: 250, closePosition: 'title', closeText: '<img src="' + fieldHelpCloseURL + '" alt="' + fieldHelpCloseAlt + '" width="19" height="19"/>', arrows: true, cursor: 'pointer', cluetipClass: 'rounded', local: true, hideLocal: false});	
});

//Return true if a given variable name is defined, else false
function isDefined(varName) {
	return typeof(window[varName]) != 'undefined';
}

//Return the number of keys in a given map
function mapSize(map) {
	var size = 0;
	for (var key in map) if (map.hasOwnProperty(key)) size++; 
	return size;
}

//Return true if a given map is empty, else false
function mapIsEmpty(map) {
	for (var key in map) if (map.hasOwnProperty(key)) return false; 
	return true;
}

//HTML encode some plain text
function htmlEncode(value) {
	if (value) return $('<div />').text(value).html();
	return '';
}

//Decode some HTML encoded text in to plain text
function htmlDecode(value) {
	if (value) return $('<div />').html(value).text();
	return '';
}

// Set up default AJAX processing and error handling
$.ajaxSetup({
	type: 'POST',
	cache: false,
	timeout: 10000,
	error: function(jqXHR, textStatus, errorThrown) {
		document.body.style.cursor = 'auto';
		var msg = (isDefined('ajaxErrorPrefix') ? ajaxErrorPrefix : 'AJAX error') + ': ';
		switch (textStatus) {
			case 'timeout':
				msg += isDefined('ajaxErrorTimeout') ? ajaxErrorTimeout : 'Timed out waiting for the server';
				break;
				
			case 'error':
				msg += isDefined('ajaxErrorServer') ? ajaxErrorMessage : 'The server encountered an error. The message from the server was {0}';
				msg = msg.replace('{0}', errorThrown ? "'" + errorThrown + "'" : 'null');
				break;
				
			case 'abort':
				msg += isDefined('ajaxErrorAbort') ? ajaxErrorAbort : 'The request to the server was aborted';
				break;
				
			case 'parsererror':
				msg += isDefined('ajaxErrorParse') ? ajaxErrorParse : 'Could not understand the reply received from the server';
				break;
				
			default:
				msg += isDefined('ajaxErrorDefault') ? ajaxErrorDefault : 'An unspecified error occurred communicating with the server (the error code was {0})';
				msg = msg.replace('{0}', textStatus ? "'" + textStatus + "'" : 'null');
		}
		
		alert(msg);
	}
});

// Pop up page help
function openPageHelp(loc) {
    var width = 400;
    var height = 400;
    var x = Math.max((screen.width - width) / 2, 0);
    var y = Math.max((screen.height - height) / 2, 0);
    var ph = window.open(loc, 'pagehelp', 'width=' + width + ',height=' + height + ',status=no,toolbar=no,location=no,menubar=no,directories=no,resizable=yes,scrollbars=yes,titlebar=no');
    ph.moveTo(x, y);
}

// Select a document to allocate to
function allocationSelect(docType, docCode) {
    if (docType > 0 && docCode > '') {
        var type = document.getElementById('targetType.id');
        var code = document.getElementById('targetCode');
        if (type != null && code != null) {
            type.value = docType;
            code.value = docCode;
        }
    }
}

//++++++++++++++++ Page specific methods ++++++++++++++++

// Used by P&L and B/S report formats to move to editing lines next
function linesNext() {
    document.getElementById('linesClicked').value = 'true';
    return true;
}

//Used by P&L and B/S report formats to request line resequencing
function needResequence() {
    document.getElementById('resequence').value = 'true';
    return true;
}

// Used by customer and supplier addresses when and major change of address format is requested
function submitform(mod) {
    document.getElementById('modified').value = mod;
    document.jsform.submit();
}

// Used by customers and suppliers to set the default revaluation method
function setRevaluation(currencyId) {
    document.getElementById('revaluationMethod').value = (document.getElementById('currency.id').value == currencyId) ? '' : 'standard';
    return true;
}

// ++++++++++++++++ Ajax Processing ++++++++++++++++

var typeModified = false;
var accountModified = false;
var lineNumber = '';

// Returns true if the response from the server is valid (i.e. no error
// message), else false. Sets or clears any current on-screen error
// message as appropriate. Will pop up an error message if there is no
// on-screen area for displaying AJAX application errors.
function responseIsValid(message) {
	document.body.style.cursor = 'auto';
    var div = document.getElementById('ajaxErrorMessage');
    var li = document.getElementById('ajaxErrorText');
    if (div && li) {
        if (message) {
            li.innerHTML = htmlEncode(message);
            div.style.visibility = 'visible';
        } else {
            li.innerHTML = '_';
            div.style.visibility = 'hidden';
        }
    } else {
    	if (message) alert(message);
    }
    
    return (message) ? false : true;
}

// Set a 'document type changed' flag
function typeChanged() {
    typeModified = true;
}

// Set an 'account changed' flag
function accountChanged() {
    accountModified = true;
}

// Gets the index of a field with an id in the format 'name[nnn]'
function getFieldIndex(field) {
    var fieldId = field.id;
    var start = fieldId.indexOf('[') + 1;
    var end = fieldId.indexOf(']', start + 1);
    return fieldId.substring(start, end);
}

// Get customer or supplier details
function getLedger(field, requestURL) {
    document.body.style.cursor = 'wait';
	$.ajax({
		url: requestURL,
		data: {sourceCode: field.value},
		success: function(data, textStatus, jqXHR) {
			if (responseIsValid(data.errorMessage)) {
	            document.getElementById('sourceCode').value = data.sourceCode;
	            document.getElementById('sourceName').value = data.sourceName;
	            var td = document.getElementById('currency.id');
	            for (var i = 0; i < td.options.length; i++) {
	                if (td.options[i].value == data.currencyId) {
	                    td.selectedIndex = i;
	                    break;
	                }
	            }
			}
		}
	});
	
    return true;
}

// Get the period associated with a changed document date
function getPeriod(field, requestURL, requestType, requestAdjustment) {
    document.body.style.cursor = 'wait';
	var requestData = {documentDate: field.value, type: document.getElementById('type.id').value};
	if (requestAdjustment != null) requestData.adjustment = requestAdjustment;
	if (requestType) {
		if (requestType == 'common') {
			requestData.common = true;
		} else {
			requestData[requestType] = document.getElementById('sourceCode').value;
		}
	}
	
	$.ajax({
		url: requestURL,
		data: requestData,
		success: function(data, textStatus, jqXHR) {
			if (responseIsValid(data.errorMessage)) {
		        if (data.periodId > 0) {
		            if (data.dueDate) document.getElementById('dueDate').value = data.dueDate;
		            var td = document.getElementById('period.id');
		            for (var i = 0; i < td.options.length; i++) {
		                if (td.options[i].value == data.periodId) {
		                    td.selectedIndex = i;
		                    break;
		                }
		            }
		        }
			}
		}
	});
	
    return true;
}

// Get the next document code number
function getCode(field, requestURL, requestNextField) {
    if (typeModified) {
        document.body.style.cursor = 'wait';
    	$.ajax({
    		url: requestURL,
    		data: {typeId: field.value, nextField: requestNextField},
    		success: function(data, textStatus, jqXHR) {
    		    typeModified = false;
    			if (responseIsValid(data.errorMessage)) {
    	            if (data.allowEdit == true) {
    	                document.getElementById('code').disabled = false;
    	                document.getElementById('code').focus();
    	            } else {
    	                document.getElementById('code').disabled = true;
    	                if (data.nextField) {
    	                    var nxt = document.getElementById(data.nextField);
    	                    if (nxt) nxt.focus();
    	                }
    	            }
    	            
    	            if (data.sourceNumber) {
    	                document.getElementById('code').value = data.sourceNumber;
    	                document.getElementById('sourceNumber').value = data.sourceNumber;
    	            }
    			}
    		}
    	});
    }

    return true;
}

// Get GL account details
function getAccount(field, requestURL, requestMetaType) {
    document.body.style.cursor = 'wait';
    lineNumber = getFieldIndex(field);
	$.ajax({
		url: requestURL,
		data: {accountCode: field.value, metaType: requestMetaType},
		success: function(data, textStatus, jqXHR) {
			if (responseIsValid(data.errorMessage)) {
	            document.getElementById('lines[' + lineNumber + '].accountCode').value = data.accountCode;
	            document.getElementById('lines[' + lineNumber + '].accountName').value = data.accountName;
	            document.getElementById('lines[' + lineNumber + '].displayName').value = data.accountName;
			}
		}
	});

    return true;
}

// Get a GL, AR or AP account's details based on the type parameter if
// supplied OR the setting of an account type selection list if not.
function setAccount(field, requestURL, requestMetaType, requestType) {
    document.body.style.cursor = 'wait';
    lineNumber = getFieldIndex(field);
    var code = document.getElementById('lines[' + lineNumber + '].accountCode').value;
    var type = (requestType) ? requestType : document.getElementById('lines[' + lineNumber + '].accountType').value;
	$.ajax({
		url: requestURL,
		data: {accountCode: code, metaType: requestMetaType, accountType: type},
		success: function(data, textStatus, jqXHR) {
			if (responseIsValid(data.errorMessage)) {
	            document.getElementById('lines[' + lineNumber + '].accountCode').value = data.accountCode;
	            document.getElementById('lines[' + lineNumber + '].accountName').value = data.accountName;
	            document.getElementById('lines[' + lineNumber + '].displayName').value = data.accountName;
			}
		}
	});

    return true;
}

// Set the onHold checkbox state
function setHold(field, requestURL) {
    document.body.style.cursor = 'wait';
    var state = field.checked ? 'true' : 'false';
	$.ajax({
		url: requestURL,
		data: {lineId: getFieldIndex(field), newState: state},
		success: function(data, textStatus, jqXHR) {
			responseIsValid(data.errorMessage);	// Nothing more to do other than set/clear any error message
		}
	});

    return true;
}

// Get a bank or cash account depending upon the type parameter
function getBank(field, requestURL, requestType) {
    if (accountModified) {
        document.body.style.cursor = 'wait';
    	$.ajax({
    		url: requestURL,
    		data: {accountCode: field.value, type: requestType},
    		success: function(data, textStatus, jqXHR) {
    			if (responseIsValid(data.errorMessage)) {
    	            if (data.currencyId > 0) {
    	                var td = document.getElementById('currency.id');
    	                for (var i = 0; i < td.options.length; i++) {
    	                    if (td.options[i].value == data.currencyId) {
    	                        td.selectedIndex = i;
    	                        break;
    	                    }
    	                }
    	            }
    			}
    		}
    	});
    }

    return true;
}

// Set the reconciled checkbox state for a bank reconciliation line or detailed line
function setReconciled(field, requestURL) {
    document.body.style.cursor = 'wait';
    var state = field.checked ? 'true' : 'false';
	$.ajax({
		url: requestURL,
		data: {lineId: getFieldIndex(field), newState: state},
		success: function(data, textStatus, jqXHR) {
			if (responseIsValid(data.errorMessage)) {
	            document.getElementById('unrec').innerHTML = htmlEncode(data.unreconciledValue);
	            document.getElementById('subtot').innerHTML = htmlEncode(data.subtotalValue);
	            document.getElementById('diff').innerHTML = htmlEncode(data.differenceValue);
	            document.getElementById('fin').style.visibility = (data.canFinalize == true) ? 'visible' : 'hidden';
			}
		}
	});

    return true;
}

// Ask the server to get the periods of a year
function changeYear(field, requestURL, requestTarget, requestStatusCodes, requestOrder) {
    document.body.style.cursor = 'wait';
    document.getElementById(requestTarget).options.length = 0;
    var requestData = {yearId: field.value, targetId: requestTarget};
    if (requestStatusCodes) requestData.statusCodes = requestStatusCodes;
    if (requestOrder) requestData.order = requestOrder;
	$.ajax({
		url: requestURL,
		data: requestData,
		success: function(data, textStatus, jqXHR) {
			if (responseIsValid(data.errorMessage)) {
	            var target = document.getElementById(data.targetId);
	            var pds = data.periods;
	            var pd;
	            target.size = Math.min(20, pds.length);
	            for (var i = 0; i < pds.length; i++) {
	                pd = pds[i];
	                target.options[i] = new Option(pd.txt, pd.val, false, false);
	            }
			}
		}
	});

    return true;
}

// Sets a new budget value for a given GL balance record
function changeBudget(field, requestURL) {
    document.body.style.cursor = 'wait';
	$.ajax({
		url: requestURL,
		data: {balanceId: field.id, budgetValue: field.value},
		success: function(data, textStatus, jqXHR) {
			if (responseIsValid(data.errorMessage)) document.getElementById(data.balanceId).value = data.budgetValue;
		}
	});

    return true;
}

// Handle the change of chart section type (ie, bs or both)
function changeSectionType(field, requestURL, requestTarget) {
    document.body.style.cursor = 'wait';
	$.ajax({
		url: requestURL,
		data: {sectionType: field.value, targetId: requestTarget},
		success: function(data, textStatus, jqXHR) {
			if (responseIsValid(data.errorMessage)) {
	            var target = document.getElementById(data.targetId);
	            var sections = data.sections;
	            var section;
	            target.options.length = 0;
	            for (var i = 0; i < sections.length; i++) {
	                section = sections[i];
	                target.options[i] = new Option(section.txt, section.val, false, false);
	            }
			}
		}
	});

    return true;
}
