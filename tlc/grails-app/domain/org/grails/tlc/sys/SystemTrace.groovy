/*
 *  Copyright 2010-2013 Paul Fernley.
 *
 *  This file is part of the Three Ledger Core (TLC) software
 *  from Paul Fernley.
 *
 *  TLC is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  TLC is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with TLC. If not, see <http://www.gnu.org/licenses/>.
 */
package org.grails.tlc.sys

class SystemTrace {

    static transients = ['companyDecode', 'userDecode']

    String databaseAction
    String domainName
    Long domainId
    Long domainSecurityCode
    Long domainVersion
    String domainData
    Long userId
    String companyDecode
    String userDecode
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static constraints = {
        databaseAction(blank: false, inList: ['insert', 'update', 'delete'])
        domainName(blank: false, size: 1..50, matches: '[A-Z][a-zA-Z_0-9]*')
        domainId(min: 1L)
        domainSecurityCode(min: 0L)
        domainVersion(min: 0L)
        domainData(blank: false, size: 1..100)
        userId(min: 0L)
        securityCode(validator: {val, obj ->
            return (val == 0)
        })
    }

    public String toString() {
        return "${domainName} - ${domainId}"
    }
}
