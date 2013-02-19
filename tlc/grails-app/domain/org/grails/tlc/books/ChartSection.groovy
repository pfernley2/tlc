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

import org.grails.tlc.corp.Company
import org.grails.tlc.sys.UtilService
import java.util.concurrent.atomic.AtomicLong

class ChartSection {

    static traceInsertCode = new AtomicLong()
    static traceUpdateCode = new AtomicLong()
    static traceDeleteCode = new AtomicLong()

    static belongsTo = [company: Company, segment1: CodeElement, segment2: CodeElement, segment3: CodeElement,
            segment4: CodeElement, segment5: CodeElement, segment6: CodeElement, segment7: CodeElement, segment8: CodeElement]
    static hasMany = [ranges: ChartSectionRange, accounts: Account, profitPercentages: ProfitReportPercent,
            profitLines: ProfitReportLine, balanceLines: BalanceReportLine]
    static transients = ['parentObject']

    ChartSection parentObject
    String path
    String code
    String name
    Integer sequencer
    String type
    Boolean autoCreate = false
    String status
    String default1
    String default2
    String default3
    String default4
    String default5
    String default6
    String default7
    String default8
    String pattern
    String treeSequence = '000000000000000000000000000000000000000000000000000000'
    Byte accountSegment = (byte) 0
    Long parent = 0L
    Long securityCode = 0L
    Date dateCreated
    Date lastUpdated

    static mapping = {
        columns {
            company lazy: true
            segment1 lazy: true
            segment2 lazy: true
            segment3 lazy: true
            segment4 lazy: true
            segment5 lazy: true
            segment6 lazy: true
            segment7 lazy: true
            segment8 lazy: true
            path column: 'chart_path'
            type column: 'section_type'
            ranges cascade: 'all'
            accounts cascade: 'save-update'
            profitPercentages cascade: 'all'
            profitLines cascade: 'all'
            balanceLines cascade: 'all'
        }
    }

    static constraints = {
        path(blank: false, size: 1..98, unique: 'company', validator: {val, obj ->
            if (val) {
                if (val.startsWith('.') || val.endsWith('.') || val.contains('..')) return 'bad'
                char c
                int cp
                for (int i = 0; i < val.length(); i++) {
                    c = val.charAt(i)
                    if (c != '.') {
                        if (!Character.isLetterOrDigit(val.codePointAt(i))) return 'bad'
                        if (Character.isHighSurrogate(c)) i++
                    }
                }

                def seq = "${1000000 + ((obj.sequencer >= 0 && obj.sequencer <= 999999) ? obj.sequencer : 0)}"[1..6]
                def pos = val.count('.') * 6
                if (pos > 48) return 'levels'
                if (pos) {
                    if (!obj.parentObject) return 'bad'
                    obj.parent = obj.parentObject.id
                    seq = obj.parentObject.treeSequence.substring(0, pos) + seq
                } else {
                    obj.parent = 0L
                }

                obj.treeSequence = seq.padRight(54, '0')
            }

            return true
        })
        sequencer(range: 0..999999, unique: ['company', 'parent'])
        code(blank: false, size: 1..10, unique: 'company', validator: {val, obj ->
            if (val && !Character.isLetter(val.codePointAt(0))) return 'letter'
            if (obj.path && !obj.path.endsWith('.')) return (val == (obj.path.contains('.') ? obj.path.substring(obj.path.lastIndexOf('.') + 1) : obj.path))

            return true
        })
        name(blank: false, size: 1..50)
        type(blank: false, inList: ['ie', 'bs'], validator: {val, obj ->
            if (obj.parentObject) obj.type = obj.parentObject.type
            return true
        })
        status(blank: false, inList: ['dr', 'cr'])
        segment1(nullable: true, validator: {val, obj ->
            if (val) {
                if (val.elementNumber == 1) obj.accountSegment = (byte) 1
            } else {
                if (obj.id) {
                    def range
                    ChartSectionRange.withNewSession {
                        range = ChartSectionRange.findAll('from ChartSectionRange as x where x.section.id = ?', [obj.id], [max: 1])
                    }

                    if (range) return 'no.ranges'
                }

                obj.accountSegment = (byte) 0
            }

            return true
        })
        segment2(nullable: true, validator: {val, obj ->
            if (val) {
                if (!obj.segment1) return 'no.accounts'
                if (val.id == obj.segment1?.id) return ['chartSection.segment.dup.element', 2]
                if (val.elementNumber == 1) obj.accountSegment = (byte) 2
            }

            return true
        })
        segment3(nullable: true, validator: {val, obj ->
            if (val) {
                if (!obj.segment2) return 'chartSection.segment.blank.inter'
                if (val.id == obj.segment1?.id || val.id == obj.segment2?.id) return ['chartSection.segment.dup.element', 3]
                if (val.elementNumber == 1) obj.accountSegment = (byte) 3
            }

            return true
        })
        segment4(nullable: true, validator: {val, obj ->
            if (val) {
                if (!obj.segment3) return 'chartSection.segment.blank.inter'
                if (val.id == obj.segment1?.id || val.id == obj.segment2?.id || val.id == obj.segment3?.id) return ['chartSection.segment.dup.element', 4]
                if (val.elementNumber == 1) obj.accountSegment = (byte) 4
            }

            return true
        })
        segment5(nullable: true, validator: {val, obj ->
            if (val) {
                if (!obj.segment4) return 'chartSection.segment.blank.inter'
                if (val.id == obj.segment1?.id || val.id == obj.segment2?.id || val.id == obj.segment3?.id ||
                        val.id == obj.segment4?.id) return ['chartSection.segment.dup.element', 5]
                if (val.elementNumber == 1) obj.accountSegment = (byte) 5
            }

            return true
        })
        segment6(nullable: true, validator: {val, obj ->
            if (val) {
                if (!obj.segment5) return 'chartSection.segment.blank.inter'
                if (val.id == obj.segment1?.id || val.id == obj.segment2?.id || val.id == obj.segment3?.id ||
                        val.id == obj.segment4?.id || val.id == obj.segment5?.id) return ['chartSection.segment.dup.element', 6]
                if (val.elementNumber == 1) obj.accountSegment = (byte) 6
            }

            return true
        })
        segment7(nullable: true, validator: {val, obj ->
            if (val) {
                if (!obj.segment6) return 'chartSection.segment.blank.inter'
                if (val.id == obj.segment1?.id || val.id == obj.segment2?.id || val.id == obj.segment3?.id ||
                        val.id == obj.segment4?.id || val.id == obj.segment5?.id || val.id == obj.segment6?.id) return ['chartSection.segment.dup.element', 7]
                if (val.elementNumber == 1) obj.accountSegment = (byte) 7
            }

            return true
        })
        segment8(nullable: true, validator: {val, obj ->
            if (val) {
                if (!obj.segment7) return 'chartSection.segment.blank.inter'
                if (val.id == obj.segment1?.id || val.id == obj.segment2?.id || val.id == obj.segment3?.id ||
                        val.id == obj.segment4?.id || val.id == obj.segment5?.id || val.id == obj.segment6?.id ||
                        val.id == obj.segment7?.id) return ['chartSection.segment.dup.element', 8]
                if (val.elementNumber == 1) obj.accountSegment = (byte) 8
            }

            return true
        })
        default1(nullable: true, size: 1..10, validator: {val, obj ->
            if (val) {
                if (!obj.segment1) return ['chartSection.default.excess', 1]
                if (obj.segment1.elementNumber == 1) return ['chartSection.default.number', 1]
                if (!matchesElement(val, obj.segment1)) return ['chartSection.default.bad.default', 1]
                if (!isValidCodeValue(val, obj.segment1)) return ['chartSection.default.default.val', 1]
            }

            return true
        })
        default2(nullable: true, size: 1..10, validator: {val, obj ->
            if (val) {
                if (!obj.segment2) return ['chartSection.default.excess', 2]
                if (obj.segment2.elementNumber == 1) return ['chartSection.default.number', 2]
                if (!matchesElement(val, obj.segment2)) return ['chartSection.default.bad.default', 2]
                if (!isValidCodeValue(val, obj.segment2)) return ['chartSection.default.default.val', 2]
            }

            return true
        })
        default3(nullable: true, size: 1..10, validator: {val, obj ->
            if (val) {
                if (!obj.segment3) return ['chartSection.default.excess', 3]
                if (obj.segment3.elementNumber == 1) return ['chartSection.default.number', 3]
                if (!matchesElement(val, obj.segment3)) return ['chartSection.default.bad.default', 3]
                if (!isValidCodeValue(val, obj.segment3)) return ['chartSection.default.default.val', 3]
            }

            return true
        })
        default4(nullable: true, size: 1..10, validator: {val, obj ->
            if (val) {
                if (!obj.segment4) return ['chartSection.default.excess', 4]
                if (obj.segment4.elementNumber == 1) return ['chartSection.default.number', 4]
                if (!matchesElement(val, obj.segment4)) return ['chartSection.default.bad.default', 4]
                if (!isValidCodeValue(val, obj.segment4)) return ['chartSection.default.default.val', 4]
            }

            return true
        })
        default5(nullable: true, size: 1..10, validator: {val, obj ->
            if (val) {
                if (!obj.segment5) return ['chartSection.default.excess', 5]
                if (obj.segment5.elementNumber == 1) return ['chartSection.default.number', 5]
                if (!matchesElement(val, obj.segment5)) return ['chartSection.default.bad.default', 5]
                if (!isValidCodeValue(val, obj.segment5)) return ['chartSection.default.default.val', 5]
            }

            return true
        })
        default6(nullable: true, size: 1..10, validator: {val, obj ->
            if (val) {
                if (!obj.segment6) return ['chartSection.default.excess', 6]
                if (obj.segment6.elementNumber == 1) return ['chartSection.default.number', 6]
                if (!matchesElement(val, obj.segment6)) return ['chartSection.default.bad.default', 6]
                if (!isValidCodeValue(val, obj.segment6)) return ['chartSection.default.default.val', 6]
            }

            return true
        })
        default7(nullable: true, size: 1..10, validator: {val, obj ->
            if (val) {
                if (!obj.segment7) return ['chartSection.default.excess', 7]
                if (obj.segment7.elementNumber == 1) return ['chartSection.default.number', 7]
                if (!matchesElement(val, obj.segment7)) return ['chartSection.default.bad.default', 7]
                if (!isValidCodeValue(val, obj.segment7)) return ['chartSection.default.default.val', 7]
            }

            return true
        })
        default8(nullable: true, size: 1..10, validator: {val, obj ->
            if (val) {
                if (!obj.segment8) return ['chartSection.default.excess', 8]
                if (obj.segment8.elementNumber == 1) return ['chartSection.default.number', 8]
                if (!matchesElement(val, obj.segment8)) return ['chartSection.default.bad.default', 8]
                if (!isValidCodeValue(val, obj.segment8)) return ['chartSection.default.default.val', 8]
            }

            return true
        })
        autoCreate(validator: {val, obj ->
            if (val && !obj.segment1) return 'bad'
        })
        pattern(nullable: true, size: 1..87, validator: {val, obj ->
            if (obj.segment1) {
                def accountNumberSeen = (obj.segment1.elementNumber == 1)
                def ptn = appendToPattern('', obj.segment1, obj.default1)
                if (obj.segment2) {
                    if (!accountNumberSeen) accountNumberSeen = (obj.segment2.elementNumber == 1)
                    ptn = appendToPattern(ptn, obj.segment2, obj.default2)
                    if (obj.segment3) {
                        if (!accountNumberSeen) accountNumberSeen = (obj.segment3.elementNumber == 1)
                        ptn = appendToPattern(ptn, obj.segment3, obj.default3)
                        if (obj.segment4) {
                            if (!accountNumberSeen) accountNumberSeen = (obj.segment4.elementNumber == 1)
                            ptn = appendToPattern(ptn, obj.segment4, obj.default4)
                            if (obj.segment5) {
                                if (!accountNumberSeen) accountNumberSeen = (obj.segment5.elementNumber == 1)
                                ptn = appendToPattern(ptn, obj.segment5, obj.default5)
                                if (obj.segment6) {
                                    if (!accountNumberSeen) accountNumberSeen = (obj.segment6.elementNumber == 1)
                                    ptn = appendToPattern(ptn, obj.segment6, obj.default6)
                                    if (obj.segment7) {
                                        if (!accountNumberSeen) accountNumberSeen = (obj.segment7.elementNumber == 1)
                                        ptn = appendToPattern(ptn, obj.segment7, obj.default7)
                                        if (obj.segment8) {
                                            if (!accountNumberSeen) accountNumberSeen = (obj.segment8.elementNumber == 8)
                                            ptn = appendToPattern(ptn, obj.segment8, obj.default8)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                if (!accountNumberSeen) return 'no.number'
                if (isAmbiguous(ptn)) return 'ambiguous'

                obj.pattern = ptn
            } else {
                obj.pattern = null
            }

            return true
        })
        treeSequence(blank: false, matches: '\\d{54}')
        accountSegment(range: 0..8)
        securityCode(validator: {val, obj ->
            obj.securityCode = obj.company.securityCode
            return true
        })
    }

    def afterInsert() {
        UtilService.trace('insert', this)
    }

    def afterUpdate() {
        UtilService.trace('update', this)
    }

    def afterDelete() {
        UtilService.trace('delete', this)
    }

    private static appendToPattern(ptn, seg, dflt) {
        if (ptn) ptn += BookService.SEGMENT_DELIMITER
        if (dflt) {
            ptn += dflt
        } else if (seg.dataType == 'alphabetic') {
            ptn += '@@@@@@@@@@'.substring(0, seg.dataLength)
        } else {
            ptn += '##########'.substring(0, seg.dataLength)
        }

        return ptn
    }

    private static matchesElement(val, element) {
        if (!val || !element || element.dataLength != val.length()) return false
        if (element.dataType == 'alphabetic') return BookService.isAlphabetic(val)
        return BookService.isNumeric(val)
    }

    private static isValidCodeValue(val, element) {
		def result
		CodeElementValue.withNewSession {session ->
			result = (CodeElementValue.countByElementAndCode(element, val) > 0)
		}
		
        return result
    }

    private static isAmbiguous(ptn) {
        def set = ptn.split("\\${BookService.SEGMENT_DELIMITER}")
        for (int i = 0; i < set.size(); i++) {
            if (!set[i].startsWith('@') && !set[i].startsWith('#')) {
                if (Character.isDigit(set[i].charAt(0))) {
                    set[i] = 'N' + set[i].length().toString()
                } else {
                    set[i] = 'A' + set[i].length().toString()
                }

                if (i && set[i] == set[i - 1]) return true
            }
        }
        return false
    }

    public matchesTemplate(val) {
        if (!val) return true
        def valSet = val.split("\\${BookService.SEGMENT_DELIMITER}")
        def ptnSet = pattern.split("\\${BookService.SEGMENT_DELIMITER}")
        if (ptnSet.size() != valSet.size()) return false
        for (int i = 0; i < ptnSet.size(); i++) {
            if (valSet[i] != '*') {
                if (ptnSet[i].length() != valSet[i].length()) return false
                if (ptnSet[i].startsWith('#') || Character.isDigit(ptnSet[i].charAt(0))) {
                    if (!BookService.isNumeric(valSet[i])) return false
                } else {
                    if (!BookService.isAlphabetic(valSet[i])) return false
                }
            }
        }

        return true
    }

    public hasSegments() {
        return (segment1 != null || segment2 != null || segment3 != null || segment4 != null || segment5 != null || segment6 != null || segment7 != null || segment8 != null)
    }

    public String toString() {
        return code
    }
}
