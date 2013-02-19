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
package org.grails.tlc.books

import org.grails.tlc.sys.SystemAddressFormat

class AddressService {

    static transactional = false
	
	def utilService

    // Get the dummy address for a customer or supplier, or null if there is no dummy address
    def getDummyAddress(owner) {
        def addresses
        if (owner instanceof Customer) {
            if (CustomerAddress.countByCustomer(owner) == 1) {
                addresses = CustomerAddress.findAllByCustomer(owner)
            }
        } else {
            if (SupplierAddress.countBySupplier(owner) == 1) {
                addresses = SupplierAddress.findAllBySupplier(owner)
            }
        }

        if (addresses && addresses.size() == 1) {
            def address = addresses[0]
            if (!address.location1 && !address.location2 && !address.location3 && !address.metro1 && !address.metro2 && !address.area1 && !address.area2 && !address.encoding) return address
        }

        return null
    }

    // Formats an address as an array of up to twelve strings. The receivingCompany, receivingContact and sendingCountry
    // parameters determine whether these placeholders should be used in formatting the address, or not. The strings
    // in the returned array are NOT encoded for HTML.
    def formatAddress(address, receivingCompany = null, receivingContact = null, sendingCountry = null) {
        def format = address.format
        def lines = []
        def pos = 0
        def lastWasBlank = true
        def field, joinBy, data
        for (int i = 0; i < 12; i++) {
            field = format."field${i + 1}"
            if (!field) break
            joinBy = i ? format."joinBy${i + 1}" : null
            if (field == 'company') {
                data = receivingCompany?.name
            } else if (field == 'contact') {
                data = receivingContact?.name
            } else if (field == 'identifier') {
                data = receivingContact?.identifier
            } else if (field == 'country') {
                if (sendingCountry && sendingCountry.id != address.country.id) {
                    data = message(code: "country.name.${address.country.code}", default: address.country.name).toUpperCase(utilService.currentLocale())
                } else {
                    data = null
                }
            } else {
                data = address."${field}"
            }

            if (data) {
                if (!lines) lines << ''
                if (!lastWasBlank && joinBy) {
                    if (joinBy != '|') lines[pos] += joinBy.replace('_', ' ')
                    lines[pos] += data
                } else {
                    if (lines[pos]) {
                        lines << ''
                        pos++
                    }

                    lines[pos] = data
                }

                lastWasBlank = false
            } else {
                lastWasBlank = true
            }
        }

        return lines
    }

    // Returns a list of up to twelve maps where each map contains the keys: property, label, width,
    // required and value. The property key in each map has a value that is the domain property name involved.
    // If the width value is zero then this is not an editable field (i.e. it's a placeholder).
    def getAsLineMaps(address, receivingCompany = null, receivingContact = null, sendingCountry = null) {
        def format = address.format
        def lines = []
        def pos = 0
        def field, label, data, width, required
        for (int i = 0; i < 12; i++) {
            field = format."field${i + 1}"
            if (!field) break
            if (field == 'company') {
                if (!receivingCompany) continue
                data = receivingCompany.name
                label = message(code: "address.prompt.${field}", default: field)
                width = 0
                required = false
            } else if (field == 'contact') {
                if (!receivingContact) continue
                data = receivingContact.name
                label = message(code: "address.prompt.${field}", default: field)
                width = 0
                required = false
            } else if (field == 'identifier') {
                if (!receivingContact) continue
                data = receivingContact.identifier
                label = message(code: "address.prompt.${field}", default: field)
                width = 0
                required = false
            } else if (field == 'country') {
                if (!sendingCountry || sendingCountry.id == address.country.id) continue
                data = message(code: "country.name.${address.country.code}", default: address.country.name).toUpperCase(utilService.currentLocale())
                label = message(code: "address.prompt.${field}", default: field)
                width = 0
                required = false
            } else {
                data = address."${field}"
                label = createLabel(format, i)
                width = format."width${i + 1}"
                required = format."mandatory${i + 1}"
            }

            lines << [property: field, label: label, value: data, width: width, required: required]
        }

        return lines
    }

    // Checks the data entered by a user to ensure it complies with the format definition.
    // Returns true if it does and false if it does not. In the case of an error the
    // address will have an error message attached to it. This method also calls the
    // normal Grails hasErrors and validate methods on the caller's behalf. It also
    // 'cleans up' the address in case of a change of format on an existing record.
    def validate(address) {
        if (address.hasErrors() || !address.validate()) return false
        def format = address.format
        def field, pattern, mandatory, data, label
        def used = [company: true, contact: true, identifier: true, country: true]  // Pretend we have used the placeholders
        def valid = true
        for (int i = 0; i < 12; i++) {
            field = format."field${i + 1}"
            if (!field) break
            if (SystemAddressFormat.fieldPlaceholders.contains(field)) continue
            used.put(field, true)   // Note that we have used this particular field
            data = address."${field}"
            mandatory = format."mandatory${i + 1}"
            if (mandatory && !data) {
                label = createLabel(format, i)
                address.errorMessage(field: field, code: 'address.entry.mandatory', args: [label], default: "The ${label} field is mandatory")
                valid = false
            }

            pattern = format."pattern${i + 1}"
            if (pattern && data && !(data ==~ ~pattern)) {
                label = createLabel(format, i)
                address.errorMessage(field: field, code: 'address.entry.pattern', args: [label], default: "The ${label} field does not match the required pattern of characters")
                valid = false
            }
        }

        // If we have a valid record then we want to work through any field of the address that has not
        // been used and set it to null. This is because we might have changed the format for an existing
        // address record and old data still exists in fields which are no longer used by the new format.
        if (valid) {
            for (type in SystemAddressFormat.fieldTypes) {
                if (!used.containsKey(type)) address."${type}" = null
            }
        }

        return valid
    }

// --------------------------------------------- Support Methods ---------------------------------------------

    private createLabel(format, index) {
        def prompt, label
        prompt = format."field${index + 1}Prompt1"
        label = message(code: "address.prompt.${prompt}", default: prompt)
        prompt = format."field${index + 1}Prompt2"
        if (prompt) {
            label += '/' + message(code: "address.prompt.${prompt}", default: prompt)
            prompt = format."field${index + 1}Prompt3"
            if (prompt) {
                label += '/' + message(code: "address.prompt.${prompt}", default: prompt)
            }
        }

        return label
    }
}
