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

public class CodeRange {

    def modifiable = true
    def type
    def length
    def from
    def to

    public CodeRange(type, length, from, to) {
        this.type = type
        this.length = length
        this.from = (from == '*') ? makeMin(type, length) : from
        this.to = (to == '*') ? makeMax(type, length) : to
    }

    public setFrom(val) {
        if (!modifiable) throw new UnsupportedOperationException("Attempt to modify the 'from' value of an unmodifiable CodeRange")
        from = val
    }

    public setTo(val) {
        if (!modifiable) throw new UnsupportedOperationException("Attempt to modify the 'to' value of an unmodifiable CodeRange")
        to = val
    }

    public size() {
        return type.length() + 1 + from.length() + to.length()
    }

    // Excludes this range from the list of ranges
    public excludeFrom(list) {
        if (!list) return                                       // Nothing in the list
        if (to < list[0].from || from > list[-1].to) return     // Completely below or above the ranges in the list
        if (from <= list[0].from && to >= list[-1].to) {        // Completely overlaps the ranges in the list
            list.clear()
            return
        }

        // Skip all ranges below us
        def pos = 0;
        while (from > list[pos].to) pos++

        // Done if we fit in to an existing gap in the ranges
        if (to < list[pos].from) return

        // If we start in a gap position, or exactly at the beginning of a range
        if (from <= list[pos].from) {

            // Chop out any element completely covered by us
            while (pos < list.size() && to >= list[pos].to) list.remove(pos)

            // If our end point is within an element, adjust that element's start point
            if (pos < list.size() && to >= list[pos].from) list[pos].from = incrementCode(to)

        } else if (to < list[pos].to) {    // We start within a range and will need to split it

            // Add the new element to the list and adjust the end point of the old one
            list.add(pos + 1, new CodeRange(type, length, incrementCode(to), list[pos].to))
            list[pos].to = decrementCode(from)

        } else {    // We start within a range but don't need to split it

            // Set the end point of the target range
            list[pos].to = decrementCode(from)

            // Point to the next element (if any)
            pos++

            // Chop out any element completely covered by us
            while (pos < list.size() && to >= list[pos].to) list.remove(pos)

            // If our end point is within an element, adjust that element's start point
            if (pos < list.size() && to >= list[pos].from) list[pos].from = incrementCode(to)
        }
    }

    // Add this range to the list, coalescing entries where possible
    public addToList(list) {
        def pos = 0
        while (pos < list.size()) {
            if (from < list[pos].from) {
                if (to >= list[pos].from || incrementCode(to) == list[pos].from) {
                    list[pos].from = from
                    if (to > list[pos].to) {
                        list[pos].to = to
                        coalesceFrom(pos, list)
                    }
                } else {    // Found a hole
                    list.add(pos, this)
                }

                return
            } else if (from <= list[pos].to || from == decrementCode(list[pos].to)) {
                if (to > list[pos].to) {
                    list[pos].to = to
                    coalesceFrom(pos, list)
                }

                return
            }

            pos++
        }

        list.add(this)
    }

    // Limits the list of ranges to this range for testing purposes. This
    // method can return either the original list (which is probably
    // immutable) or a new list if modifications were required. The 'list'
    // input parameter must be a list of CodeRange objects (i.e. not
    // 'disallowed' tests).
    public includeOnly(list) {
        def results = []
        if (!list) return results                                  	// Nothing in the list
        if (from <= list[0].from && to >= list[-1].to) return list	// Completely overlaps the ranges in the list
        if (to < list[0].from || from > list[-1].to) return results	// Completely below or above the ranges in the list

        def result
        for (range in list) {
            if (to < range.from || from > range.to) continue		// We can ignore this since it's completely outside of our range

            // Here, we effectively clone the current CodeRange
            // object allowing it to be modifiable (on the
            // assumption that it isn't).
            result = new CodeRange(range.type, range.length, range.from, range.to)
            if (from > result.from) result.from = from
            if (to < result.to) result.to = to
            results << result
        }

        return results
    }

    // Returns true if the 'from' value is the minimum possible value
    public startsFromMinimum() {
        return from == makeMin(type, length)
    }

    // Returns true if the 'to' value is the maximum possible value
    public endsWithMaximum() {
        return to == makeMax(type, length)
    }

    // Return true if the ranges in the two lists overlap at all. This is used for checking
    // for overlapping account code ranges (which is not permitted).
    static rangesOverlap(list1, list2) {
        if (!list1 || !list2 || list1[0].from > list2[-1].to || list1[-1].to < list2[0].from) return false
        for (range1 in list1) {
            for (range2 in list2) {
                if (range1.to >= range2.from && range1.from <= range2.to) return true
            }
        }

        return false
    }

    static contains(list, val) {
        if (!list || val < list[0].from || val > list[-1].to) return false
        for (int i = 0; i < list.size(); i++) {
            if (val >= list[i].from && val <= list[i].to) return true
        }

        return false
    }

    // --------------------------------------------- Support Methods ---------------------------------------------

    private incrementCode(val) {
        if (type == 'numeric') return (val.toInteger() + 1).toString().padLeft(length, '0')

        def min = BookService.ALPHA_CODES_UPPERCASE ? 'A' : 'a'
        def max = BookService.ALPHA_CODES_UPPERCASE ? 'Z' : 'z'
        char[] chars = val.toCharArray()
        for (int i = chars.length - 1; i >= 0; i--) {
            chars[i]++
            if (chars[i] <= max) break
            if (i > 0) chars[i] = min
        }

        return new String(chars)
    }

    private decrementCode(val) {
        if (BookService.isNumeric(val)) return (val.toInteger() - 1).toString().padLeft(length, '0')

        def min = BookService.ALPHA_CODES_UPPERCASE ? 'A' : 'a'
        def max = BookService.ALPHA_CODES_UPPERCASE ? 'Z' : 'z'
        char[] chars = val.toCharArray()
        for (int i = chars.length - 1; i >= 0; i--) {
            chars[i]--
            if (chars[i] >= min) break
            if (i > 0) chars[i] = max
        }

        return new String(chars)
    }

    private makeMin(type, length) {
        if (type == 'alphabetic') {
            if (BookService.ALPHA_CODES_UPPERCASE) return 'AAAAAAAAAA'.substring(0, length)
            return 'aaaaaaaaaa'.substring(0, length)
        }

        return '0000000000'.substring(0, length)
    }

    private makeMax(type, length) {
        if (type == 'alphabetic') {
            if (BookService.ALPHA_CODES_UPPERCASE) return 'ZZZZZZZZZZ'.substring(0, length)
            return 'zzzzzzzzzz'.substring(0, length)
        }

        return '9999999999'.substring(0, length)
    }

    private coalesceFrom(pos, list) {
        def base = list[pos++]
        while (pos < list.size()) {
            if (base.to >= list[pos].from || incrementCode(base.to) == list[pos].from) {
                if (list[pos].to >= base.to) {
                    base.to = list[pos].to
                    list.remove((int) pos)
                    return
                }

                list.remove((int) pos)
            } else {
                return
            }
        }
    }

    public String toString() {
        return "'${from}'..'${to}'"
    }
}