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

import org.grails.tlc.books.CustomerAddress
import org.grails.tlc.books.SupplierAddress
import java.util.concurrent.atomic.AtomicLong

class SystemAddressFormat {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static fieldTypes = ['location1', 'location2', 'location3', 'metro1', 'metro2', 'area1', 'area2', 'encoding',
        'company', 'contact', 'identifier', 'country']
    static fieldPlaceholders = ['company', 'contact', 'identifier', 'country']
    static fieldPrompts = ['location1', 'location2', 'location3', 'community', 'village', 'suburb', 'zone', 'town',
        'city', 'place', 'district', 'quarter', 'prefecture', 'ward', 'parish', 'locality', 'department', 'division',
        'arrondissement', 'county', 'state', 'commonwealth', 'region', 'province', 'territory', 'island', 'zip',
        'postcode', 'postalcode', 'country', 'contact', 'identifier', 'company', 'municipality', 'canton']

    static hasMany = [countries: SystemCountry, customerAddresses: CustomerAddress, supplierAddresses: SupplierAddress]

    String code
    String name
    String field1
    String field1Prompt1
    String field1Prompt2
    String field1Prompt3
    Integer width1
    Boolean mandatory1
    String pattern1
    String field2
    String field2Prompt1
    String field2Prompt2
    String field2Prompt3
    Integer width2
    Boolean mandatory2
    String pattern2
    String joinBy2
    String field3
    String field3Prompt1
    String field3Prompt2
    String field3Prompt3
    Integer width3
    Boolean mandatory3
    String pattern3
    String joinBy3
    String field4
    String field4Prompt1
    String field4Prompt2
    String field4Prompt3
    Integer width4
    Boolean mandatory4
    String pattern4
    String joinBy4
    String field5
    String field5Prompt1
    String field5Prompt2
    String field5Prompt3
    Integer width5
    Boolean mandatory5
    String pattern5
    String joinBy5
    String field6
    String field6Prompt1
    String field6Prompt2
    String field6Prompt3
    Integer width6
    Boolean mandatory6
    String pattern6
    String joinBy6
    String field7
    String field7Prompt1
    String field7Prompt2
    String field7Prompt3
    Integer width7
    Boolean mandatory7
    String pattern7
    String joinBy7
    String field8
    String field8Prompt1
    String field8Prompt2
    String field8Prompt3
    Integer width8
    Boolean mandatory8
    String pattern8
    String joinBy8
    String field9
    String field9Prompt1
    String field9Prompt2
    String field9Prompt3
    Integer width9
    Boolean mandatory9
    String pattern9
    String joinBy9
    String field10
    String field10Prompt1
    String field10Prompt2
    String field10Prompt3
    Integer width10
    Boolean mandatory10
    String pattern10
    String joinBy10
    String field11
    String field11Prompt1
    String field11Prompt2
    String field11Prompt3
    Integer width11
    Boolean mandatory11
    String pattern11
    String joinBy11
    String field12
    String field12Prompt1
    String field12Prompt2
    String field12Prompt3
    Integer width12
    Boolean mandatory12
    String pattern12
    String joinBy12
    Long securityCode = 0
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            countries cascade: 'save-update'
            customerAddresses: 'save-update'
            supplierAddresses: 'save-update'
        }
    }

    static constraints = {
        code(blank: false, size: 1..10, matches: '[a-zA-Z][a-zA-Z_0-9]*', unique: true)
        name(blank: false, size: 1..30)
        field1(size: 1..10, inList: fieldTypes, validator: {val, obj ->
            for (type in fieldPlaceholders) {
                if (!typeExists(obj, type)) return type
            }

            return true
        })
        field1Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, valdator: {val, obj ->
            if (obj.field1) {
                if (fieldPlaceholders.contains(obj.field1)) {
                    if (val) obj.field1Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            }

            return true
        })
        field1Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field1) {
                if (fieldPlaceholders.contains(obj.field1)) {
                    if (val) obj.field1Prompt2 = null
                } else if (val && val == obj.field1Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            }

            return true
        })
        field1Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field1) {
                if (fieldPlaceholders.contains(obj.field1)) {
                    if (val) obj.field1Prompt3 = null
                } else if (val) {
                    if (!obj.field1Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field1Prompt1 || val == obj.field1Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            }

            return true
        })
        width1(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field1) {
                if (fieldPlaceholders.contains(obj.field1)) {
                    if (val) obj.width1 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            }

            return true
        })
        mandatory1(nullable: true, validator: {val, obj ->
            if (obj.field1) {
                if (fieldPlaceholders.contains(obj.field1)) {
                    if (val) obj.mandatory1 = null
                } else if (val == null) {
                    obj.mandatory1 = false
                }
            }

            return true
        })
        pattern1(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field1) {
                if (fieldPlaceholders.contains(obj.field1)) {
                    if (val) obj.pattern1 = null
                }
            }

            return true
        })
        field2(size: 1..10, inList: fieldTypes, validator: {val, obj ->
            if (val && (val == obj.field1)) {
                return 'systemAddressFormat.field.duplicate'
            }

            return true
        })
        field2Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, valdator: {val, obj ->
            if (obj.field2) {
                if (fieldPlaceholders.contains(obj.field2)) {
                    if (val) obj.field2Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            }

            return true
        })
        field2Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field2) {
                if (fieldPlaceholders.contains(obj.field2)) {
                    if (val) obj.field2Prompt2 = null
                } else if (val && val == obj.field2Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            }

            return true
        })
        field2Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field2) {
                if (fieldPlaceholders.contains(obj.field2)) {
                    if (val) obj.field2Prompt3 = null
                } else if (val) {
                    if (!obj.field2Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field2Prompt1 || val == obj.field2Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            }

            return true
        })
        width2(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field2) {
                if (fieldPlaceholders.contains(obj.field2)) {
                    if (val) obj.width2 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            }

            return true
        })
        mandatory2(nullable: true, validator: {val, obj ->
            if (obj.field2) {
                if (fieldPlaceholders.contains(obj.field2)) {
                    if (val) obj.mandatory2 = null
                } else if (val == null) {
                    obj.mandatory2 = false
                }
            }

            return true
        })
        pattern2(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field2) {
                if (fieldPlaceholders.contains(obj.field2)) {
                    if (val) obj.pattern2 = null
                }
            }

            return true
        })
        joinBy2(nullable: true, size: 1..20, validator: {val, obj ->
            if (obj.field2) {
                if (fieldPlaceholders.contains(obj.field2)) {
                    if (val) obj.joinBy2 = null
                } else if (val && val.indexOf(' ') >= 0) {
                    return 'systemAddressFormat.joinBy.spaces'
                }
            }

            return true
        })
        field3(size: 1..10, inList: fieldTypes, validator: {val, obj ->
            if (val && (val == obj.field1 || val == obj.field2)) {
                return 'systemAddressFormat.field.duplicate'
            }

            return true
        })
        field3Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, valdator: {val, obj ->
            if (obj.field3) {
                if (fieldPlaceholders.contains(obj.field3)) {
                    if (val) obj.field3Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            }

            return true
        })
        field3Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field3) {
                if (fieldPlaceholders.contains(obj.field3)) {
                    if (val) obj.field3Prompt2 = null
                } else if (val && val == obj.field3Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            }

            return true
        })
        field3Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field3) {
                if (fieldPlaceholders.contains(obj.field3)) {
                    if (val) obj.field3Prompt3 = null
                } else if (val) {
                    if (!obj.field3Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field3Prompt1 || val == obj.field3Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            }

            return true
        })
        width3(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field3) {
                if (fieldPlaceholders.contains(obj.field3)) {
                    if (val) obj.width3 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            }

            return true
        })
        mandatory3(nullable: true, validator: {val, obj ->
            if (obj.field3) {
                if (fieldPlaceholders.contains(obj.field3)) {
                    if (val) obj.mandatory3 = null
                } else if (val == null) {
                    obj.mandatory3 = false
                }
            }

            return true
        })
        pattern3(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field3) {
                if (fieldPlaceholders.contains(obj.field3)) {
                    if (val) obj.pattern3 = null
                }
            }

            return true
        })
        joinBy3(nullable: true, size: 1..20, validator: {val, obj ->
            if (obj.field3) {
                if (fieldPlaceholders.contains(obj.field3)) {
                    if (val) obj.joinBy3 = null
                } else if (val && val.indexOf(' ') >= 0) {
                    return 'systemAddressFormat.joinBy.spaces'
                }
            }

            return true
        })
        field4(size: 1..10, inList: fieldTypes, validator: {val, obj ->
            if (val && (val == obj.field1 || val == obj.field2 || val == obj.field3)) {
                return 'systemAddressFormat.field.duplicate'
            }

            return true
        })
        field4Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field4) {
                if (fieldPlaceholders.contains(obj.field4)) {
                    if (val) obj.field4Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            }

            return true
        })
        field4Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field4) {
                if (fieldPlaceholders.contains(obj.field4)) {
                    if (val) obj.field4Prompt2 = null
                } else if (val && val == obj.field4Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            }

            return true
        })
        field4Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field4) {
                if (fieldPlaceholders.contains(obj.field4)) {
                    if (val) obj.field4Prompt3 = null
                } else if (val) {
                    if (!obj.field4Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field4Prompt1 || val == obj.field4Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            }

            return true
        })
        width4(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field4) {
                if (fieldPlaceholders.contains(obj.field4)) {
                    if (val) obj.width4 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            }

            return true
        })
        mandatory4(nullable: true, validator: {val, obj ->
            if (obj.field4) {
                if (fieldPlaceholders.contains(obj.field4)) {
                    if (val) obj.mandatory4 = null
                } else if (val == null) {
                    obj.mandatory4 = false
                }
            }

            return true
        })
        pattern4(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field4) {
                if (fieldPlaceholders.contains(obj.field4)) {
                    if (val) obj.pattern4 = null
                }
            }

            return true
        })
        joinBy4(nullable: true, size: 1..20, validator: {val, obj ->
            if (obj.field4) {
                if (fieldPlaceholders.contains(obj.field4)) {
                    if (val) obj.joinBy4 = null
                } else if (val && val.indexOf(' ') >= 0) {
                    return 'systemAddressFormat.joinBy.spaces'
                }
            }

            return true
        })
        field5(size: 1..10, inList: fieldTypes, validator: {val, obj ->
            if (val && (val == obj.field1 || val == obj.field2 || val == obj.field3 || val == obj.field4)) {
                return 'systemAddressFormat.field.duplicate'
            }

            return true
        })
        field5Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field5) {
                if (fieldPlaceholders.contains(obj.field5)) {
                    if (val) obj.field5Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            }

            return true
        })
        field5Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field5) {
                if (fieldPlaceholders.contains(obj.field5)) {
                    if (val) obj.field5Prompt2 = null
                } else if (val && val == obj.field5Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            }

            return true
        })
        field5Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field5) {
                if (fieldPlaceholders.contains(obj.field5)) {
                    if (val) obj.field5Prompt3 = null
                } else if (val) {
                    if (!obj.field5Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field5Prompt1 || val == obj.field5Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            }

            return true
        })
        width5(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field5) {
                if (fieldPlaceholders.contains(obj.field5)) {
                    if (val) obj.width5 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            }

            return true
        })
        mandatory5(nullable: true, validator: {val, obj ->
            if (obj.field5) {
                if (fieldPlaceholders.contains(obj.field5)) {
                    if (val) obj.mandatory5 = null
                } else if (val == null) {
                    obj.mandatory5 = false
                }
            }

            return true
        })
        pattern5(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field5) {
                if (fieldPlaceholders.contains(obj.field5)) {
                    if (val) obj.pattern5 = null
                }
            }

            return true
        })
        joinBy5(nullable: true, size: 1..20, validator: {val, obj ->
            if (obj.field5) {
                if (fieldPlaceholders.contains(obj.field5)) {
                    if (val) obj.joinBy5 = null
                } else if (val && val.indexOf(' ') >= 0) {
                    return 'systemAddressFormat.joinBy.spaces'
                }
            }

            return true
        })
        field6(nullable: true, size: 1..10, inList: fieldTypes, validator: {val, obj ->
            if (val && (val == obj.field1 || val == obj.field2 || val == obj.field3 || val == obj.field4 || val == obj.field5)) {
                return 'systemAddressFormat.field.duplicate'
            }

            return true
        })
        field6Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, valdator: {val, obj ->
            if (obj.field6) {
                if (fieldPlaceholders.contains(obj.field6)) {
                    if (val) obj.field6Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            } else if (val) {
                obj.field6Prompt1 = null
            }

            return true
        })
        field6Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field6) {
                if (fieldPlaceholders.contains(obj.field6)) {
                    if (val) obj.field6Prompt2 = null
                } else if (val && val == obj.field6Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            } else if (val) {
                obj.field6Prompt2 = null
            }

            return true
        })
        field6Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field6) {
                if (fieldPlaceholders.contains(obj.field6)) {
                    if (val) obj.field6Prompt3 = null
                } else if (val) {
                    if (!obj.field6Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field6Prompt1 || val == obj.field6Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            } else if (val) {
                obj.field6Prompt3 = null
            }

            return true
        })
        width6(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field6) {
                if (fieldPlaceholders.contains(obj.field6)) {
                    if (val) obj.width6 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            } else if (val) {
                obj.width6 = null
            }

            return true
        })
        mandatory6(nullable: true, validator: {val, obj ->
            if (obj.field6) {
                if (fieldPlaceholders.contains(obj.field6)) {
                    if (val) obj.mandatory6 = null
                } else if (val == null) {
                    obj.mandatory6 = false
                }
            } else if (val) {
                obj.mandatory6 = null
            }

            return true
        })
        pattern6(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field6) {
                if (fieldPlaceholders.contains(obj.field6)) {
                    if (val) obj.pattern6 = null
                }
            } else if (val) {
                obj.pattern6 = null
            }

            return true
        })
        joinBy6(nullable: true, size: 1..20, validator: {val, obj ->
            if (obj.field6) {
                if (fieldPlaceholders.contains(obj.field6)) {
                    if (val) obj.joinBy6 = null
                } else if (val && val.indexOf(' ') >= 0) {
                    return 'systemAddressFormat.joinBy.spaces'
                }
            } else if (val) {
                obj.joinBy6 = null
            }

            return true
        })
        field7(nullable: true, size: 1..10, inList: fieldTypes, validator: {val, obj ->
            if (val) {
                if (!obj.field6) return 'systemAddressFormat.field.prior'
                if (val == obj.field1 || val == obj.field2 || val == obj.field3 || val == obj.field4 ||
                    val == obj.field5 || val == obj.field6) {
                    return 'systemAddressFormat.field.duplicate'
                }
            }

            return true
        })
        field7Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field7) {
                if (fieldPlaceholders.contains(obj.field7)) {
                    if (val) obj.field7Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            } else if (val) {
                obj.field7Prompt1 = null
            }

            return true
        })
        field7Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field7) {
                if (fieldPlaceholders.contains(obj.field7)) {
                    if (val) obj.field7Prompt2 = null
                } else if (val && val == obj.field7Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            } else if (val) {
                obj.field7Prompt2 = null
            }

            return true
        })
        field7Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field7) {
                if (fieldPlaceholders.contains(obj.field7)) {
                    if (val) obj.field7Prompt3 = null
                } else if (val) {
                    if (!obj.field7Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field7Prompt1 || val == obj.field7Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            } else if (val) {
                obj.field7Prompt3 = null
            }

            return true
        })
        width7(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field7) {
                if (fieldPlaceholders.contains(obj.field7)) {
                    if (val) obj.width7 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            } else if (val) {
                obj.width7 = null
            }

            return true
        })
        mandatory7(nullable: true, validator: {val, obj ->
            if (obj.field7) {
                if (fieldPlaceholders.contains(obj.field7)) {
                    if (val) obj.mandatory7 = null
                } else if (val == null) {
                    obj.mandatory7 = false
                }
            } else if (val) {
                obj.mandatory7 = null
            }

            return true
        })
        pattern7(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field7) {
                if (fieldPlaceholders.contains(obj.field7)) {
                    if (val) obj.pattern7 = null
                }
            } else if (val) {
                obj.pattern7 = null
            }

            return true
        })
        joinBy7(nullable: true, size: 1..20, validator: {val, obj ->
            if (obj.field7) {
                if (fieldPlaceholders.contains(obj.field7)) {
                    if (val) obj.joinBy7 = null
                } else if (val && val.indexOf(' ') >= 0) {
                    return 'systemAddressFormat.joinBy.spaces'
                }
            } else if (val) {
                obj.joinBy7 = null
            }

            return true
        })
        field8(nullable: true, size: 1..10, inList: fieldTypes, validator: {val, obj ->
            if (val) {
                if (!obj.field7) return 'systemAddressFormat.field.prior'
                if (val == obj.field1 || val == obj.field2 || val == obj.field3 || val == obj.field4 ||
                    val == obj.field5 || val == obj.field6 || val == obj.field7) {
                    return 'systemAddressFormat.field.duplicate'
                }
            }

            return true
        })
        field8Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field8) {
                if (fieldPlaceholders.contains(obj.field8)) {
                    if (val) obj.field8Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            } else if (val) {
                obj.field8Prompt1 = null
            }

            return true
        })
        field8Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field8) {
                if (fieldPlaceholders.contains(obj.field8)) {
                    if (val) obj.field8Prompt2 = null
                } else if (val && val == obj.field8Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            } else if (val) {
                obj.field8Prompt2 = null
            }

            return true
        })
        field8Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field8) {
                if (fieldPlaceholders.contains(obj.field8)) {
                    if (val) obj.field8Prompt3 = null
                } else if (val) {
                    if (!obj.field8Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field8Prompt1 || val == obj.field8Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            } else if (val) {
                obj.field8Prompt3 = null
            }

            return true
        })
        width8(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field8) {
                if (fieldPlaceholders.contains(obj.field8)) {
                    if (val) obj.width8 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            } else if (val) {
                obj.width8 = null
            }

            return true
        })
        mandatory8(nullable: true, validator: {val, obj ->
            if (obj.field8) {
                if (fieldPlaceholders.contains(obj.field8)) {
                    if (val) obj.mandatory8 = null
                } else if (val == null) {
                    obj.mandatory8 = false
                }
            } else if (val) {
                obj.mandatory8 = null
            }

            return true
        })
        pattern8(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field8) {
                if (fieldPlaceholders.contains(obj.field8)) {
                    if (val) obj.pattern8 = null
                }
            } else if (val) {
                obj.pattern8 = null
            }

            return true
        })
        joinBy8(nullable: true, size: 1..20, validator: {val, obj ->
            if (obj.field8) {
                if (fieldPlaceholders.contains(obj.field8)) {
                    if (val) obj.joinBy8 = null
                } else if (val && val.indexOf(' ') >= 0) {
                    return 'systemAddressFormat.joinBy.spaces'
                }
            } else if (val) {
                obj.joinBy8 = null
            }

            return true
        })
        field9(nullable: true, size: 1..10, inList: fieldTypes, validator: {val, obj ->
            if (val) {
                if (!obj.field8) return 'systemAddressFormat.field.prior'
                if (val == obj.field1 || val == obj.field2 || val == obj.field3 || val == obj.field4 ||
                    val == obj.field5 || val == obj.field6 || val == obj.field7 || val == obj.field8) {
                    return 'systemAddressFormat.field.duplicate'
                }
            }

            return true
        })
        field9Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field9) {
                if (fieldPlaceholders.contains(obj.field9)) {
                    if (val) obj.field9Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            } else if (val) {
                obj.field9Prompt1 = null
            }

            return true
        })
        field9Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field9) {
                if (fieldPlaceholders.contains(obj.field9)) {
                    if (val) obj.field9Prompt2 = null
                } else if (val && val == obj.field9Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            } else if (val) {
                obj.field9Prompt2 = null
            }

            return true
        })
        field9Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field9) {
                if (fieldPlaceholders.contains(obj.field9)) {
                    if (val) obj.field9Prompt3 = null
                } else if (val) {
                    if (!obj.field9Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field9Prompt1 || val == obj.field9Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            } else if (val) {
                obj.field9Prompt3 = null
            }

            return true
        })
        width9(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field9) {
                if (fieldPlaceholders.contains(obj.field9)) {
                    if (val) obj.width9 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            } else if (val) {
                obj.width9 = null
            }

            return true
        })
        mandatory9(nullable: true, validator: {val, obj ->
            if (obj.field9) {
                if (fieldPlaceholders.contains(obj.field9)) {
                    if (val) obj.mandatory9 = null
                } else if (val == null) {
                    obj.mandatory9 = false
                }
            } else if (val) {
                obj.mandatory9 = null
            }

            return true
        })
        pattern9(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field9) {
                if (fieldPlaceholders.contains(obj.field9)) {
                    if (val) obj.pattern9 = null
                }
            } else if (val) {
                obj.pattern9 = null
            }

            return true
        })
        joinBy9(nullable: true, size: 1..20, validator: {val, obj ->
            if (obj.field9) {
                if (fieldPlaceholders.contains(obj.field9)) {
                    if (val) obj.joinBy9 = null
                } else if (val && val.indexOf(' ') >= 0) {
                    return 'systemAddressFormat.joinBy.spaces'
                }
            } else if (val) {
                obj.joinBy9 = null
            }

            return true
        })
        field10(nullable: true, size: 1..10, inList: fieldTypes, validator: {val, obj ->
            if (val) {
                if (!obj.field9) return 'systemAddressFormat.field.prior'
                if (val == obj.field1 || val == obj.field2 || val == obj.field3 || val == obj.field4 ||
                    val == obj.field5 || val == obj.field6 || val == obj.field7 || val == obj.field8 || val == obj.field9) {
                    return 'systemAddressFormat.field.duplicate'
                }
            }

            return true
        })
        field10Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field10) {
                if (fieldPlaceholders.contains(obj.field10)) {
                    if (val) obj.field10Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            } else if (val) {
                obj.field10Prompt1 = null
            }

            return true
        })
        field10Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field10) {
                if (fieldPlaceholders.contains(obj.field10)) {
                    if (val) obj.field10Prompt2 = null
                } else if (val && val == obj.field10Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            } else if (val) {
                obj.field10Prompt2 = null
            }

            return true
        })
        field10Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field10) {
                if (fieldPlaceholders.contains(obj.field10)) {
                    if (val) obj.field10Prompt3 = null
                } else if (val) {
                    if (!obj.field10Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field10Prompt1 || val == obj.field10Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            } else if (val) {
                obj.field10Prompt3 = null
            }

            return true
        })
        width10(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field10) {
                if (fieldPlaceholders.contains(obj.field10)) {
                    if (val) obj.width10 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            } else if (val) {
                obj.width10 = null
            }

            return true
        })
        mandatory10(nullable: true, validator: {val, obj ->
            if (obj.field10) {
                if (fieldPlaceholders.contains(obj.field10)) {
                    if (val) obj.mandatory10 = null
                } else if (val == null) {
                    obj.mandatory10 = false
                }
            } else if (val) {
                obj.mandatory10 = null
            }

            return true
        })
        pattern10(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field10) {
                if (fieldPlaceholders.contains(obj.field10)) {
                    if (val) obj.pattern10 = null
                }
            } else if (val) {
                obj.pattern10 = null
            }

            return true
        })
        joinBy10(nullable: true, size: 1..20, validator: {val, obj ->
            if (obj.field10) {
                if (fieldPlaceholders.contains(obj.field10)) {
                    if (val) obj.joinBy10 = null
                } else if (val && val.indexOf(' ') >= 0) {
                    return 'systemAddressFormat.joinBy.spaces'
                }
            } else if (val) {
                obj.joinBy10 = null
            }

            return true
        })
        field11(nullable: true, size: 1..10, inList: fieldTypes, validator: {val, obj ->
            if (val) {
                if (!obj.field10) return 'systemAddressFormat.field.prior'
                if (val == obj.field1 || val == obj.field2 || val == obj.field3 || val == obj.field4 ||
                    val == obj.field5 || val == obj.field6 || val == obj.field7 || val == obj.field8 ||
                    val == obj.field9 || val == obj.field10) {
                    return 'systemAddressFormat.field.duplicate'
                }
            }

            return true
        })
        field11Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field11) {
                if (fieldPlaceholders.contains(obj.field11)) {
                    if (val) obj.field11Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            } else if (val) {
                obj.field11Prompt1 = null
            }

            return true
        })
        field11Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field11) {
                if (fieldPlaceholders.contains(obj.field11)) {
                    if (val) obj.field11Prompt2 = null
                } else if (val && val == obj.field11Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            } else if (val) {
                obj.field11Prompt2 = null
            }

            return true
        })
        field11Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field11) {
                if (fieldPlaceholders.contains(obj.field11)) {
                    if (val) obj.field11Prompt3 = null
                } else if (val) {
                    if (!obj.field11Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field11Prompt1 || val == obj.field11Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            } else if (val) {
                obj.field11Prompt3 = null
            }

            return true
        })
        width11(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field11) {
                if (fieldPlaceholders.contains(obj.field11)) {
                    if (val) obj.width11 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            } else if (val) {
                obj.width11 = null
            }

            return true
        })
        mandatory11(nullable: true, validator: {val, obj ->
            if (obj.field11) {
                if (fieldPlaceholders.contains(obj.field11)) {
                    if (val) obj.mandatory11 = null
                } else if (val == null) {
                    obj.mandatory11 = false
                }
            } else if (val) {
                obj.mandatory11 = null
            }

            return true
        })
        pattern11(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field11) {
                if (fieldPlaceholders.contains(obj.field11)) {
                    if (val) obj.pattern11 = null
                }
            } else if (val) {
                obj.pattern11 = null
            }

            return true
        })
        joinBy11(nullable: true, size: 1..20, validator: {val, obj ->
            if (obj.field11) {
                if (fieldPlaceholders.contains(obj.field11)) {
                    if (val) obj.joinBy11 = null
                } else if (val && val.indexOf(' ') >= 0) {
                    return 'systemAddressFormat.joinBy.spaces'
                }
            } else if (val) {
                obj.joinBy11 = null
            }

            return true
        })
        field12(nullable: true, size: 1..10, inList: fieldTypes, validator: {val, obj ->
            if (val) {
                if (!obj.field11) return 'systemAddressFormat.field.prior'
                if (val == obj.field1 || val == obj.field2 || val == obj.field3 || val == obj.field4 ||
                    val == obj.field5 || val == obj.field6 || val == obj.field7 || val == obj.field8 ||
                    val == obj.field9 || val == obj.field10 || val == obj.field11) {
                    return 'systemAddressFormat.field.duplicate'
                }
            }

            return true
        })
        field12Prompt1(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field12) {
                if (fieldPlaceholders.contains(obj.field12)) {
                    if (val) obj.field12Prompt1 = null
                } else if (!val) {
                    return 'systemAddressFormat.fieldPrompt1.missing'
                }
            } else if (val) {
                obj.field12Prompt1 = null
            }

            return true
        })
        field12Prompt2(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field12) {
                if (fieldPlaceholders.contains(obj.field12)) {
                    if (val) obj.field12Prompt2 = null
                } else if (val && val == obj.field12Prompt1) {
                    return 'systemAddressFormat.fieldPrompt2.repeat'
                }
            } else if (val) {
                obj.field12Prompt2 = null
            }

            return true
        })
        field12Prompt3(nullable: true, size: 1..20, inList: fieldPrompts, validator: {val, obj ->
            if (obj.field12) {
                if (fieldPlaceholders.contains(obj.field12)) {
                    if (val) obj.field12Prompt3 = null
                } else if (val) {
                    if (!obj.field12Prompt2) return 'systemAddressFormat.fieldPrompt3.gap'
                    if (val == obj.field12Prompt1 || val == obj.field12Prompt2) return 'systemAddressFormat.fieldPrompt3.repeat'
                }
            } else if (val) {
                obj.field12Prompt3 = null
            }

            return true
        })
        width12(nullable: true, range: 1..50, validator: {val, obj ->
            if (obj.field12) {
                if (fieldPlaceholders.contains(obj.field12)) {
                    if (val) obj.width12 = null
                } else if (!val) {
                    return 'systemAddressFormat.width.invalid'
                }
            } else if (val) {
                obj.width12 = null
            }

            return true
        })
        mandatory12(nullable: true, validator: {val, obj ->
            if (obj.field12) {
                if (fieldPlaceholders.contains(obj.field12)) {
                    if (val) obj.mandatory12 = null
                } else if (val == null) {
                    obj.mandatory12 = false
                }
            } else if (val) {
                obj.mandatory12 = null
            }

            return true
        })
        pattern12(nullable: true, size: 1..100, validator: {val, obj ->
            if (obj.field12) {
                if (fieldPlaceholders.contains(obj.field12)) {
                    if (val) obj.pattern12 = null
                }
            } else if (val) {
                obj.pattern12 = null
            }

            return true
        })
        joinBy12(nullable: true, size: 1..20, validator: {val, obj ->
            if (obj.field12) {
                if (fieldPlaceholders.contains(obj.field12)) {
                    if (val) obj.joinBy12 = null
                } else if (val && val.indexOf(' ') >= 0) {
                    return 'systemAddressFormat.joinBy.spaces'
                }
            } else if (val) {
                obj.joinBy12 = null
            }

            return true
        })
        securityCode(validator: {val, obj ->
            return (val == 0)
        })
    }

    private static typeExists(obj, str) {
        for (int i = 1; i <= 12; i++) {
            if (obj."field${i}" == str) return true
        }

        return false
    }

    def afterInsert() {
        return UtilService.trace('insert', this)
    }

    def afterUpdate() {
        return UtilService.trace('update', this)
    }

    def afterDelete() {
        return UtilService.trace('delete', this)
    }

    public String toString() {
        return code
    }
}
