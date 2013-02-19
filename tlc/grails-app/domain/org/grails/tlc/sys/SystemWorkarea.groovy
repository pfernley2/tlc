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

class SystemWorkarea {
    Long process
    Long identifier
    Integer integer1
    Integer integer2
    Integer integer3
    Integer integer4
    Long long1
    Long long2
    Long long3
    Long long4
    BigDecimal decimal1
    BigDecimal decimal2
    BigDecimal decimal3
    BigDecimal decimal4
    BigDecimal decimal5
    BigDecimal decimal6
    BigDecimal decimal7
    BigDecimal decimal8
    BigDecimal decimal9
    BigDecimal decimal10
    BigDecimal decimal11
    BigDecimal decimal12
    String string1
    String string2
    String string3
    String string4
    Date date1
    Date date2
    Date date3
    Date date4
    Boolean boolean1
    Boolean boolean2
    Boolean boolean3
    Boolean boolean4

    static constraints = {
        identifier(unique: 'process')
        integer1(nullable: true)
        integer2(nullable: true)
        integer3(nullable: true)
        integer4(nullable: true)
        long1(nullable: true)
        long2(nullable: true)
        long3(nullable: true)
        long4(nullable: true)
        decimal1(nullable: true, scale: 3)
        decimal2(nullable: true, scale: 3)
        decimal3(nullable: true, scale: 3)
        decimal4(nullable: true, scale: 3)
        decimal5(nullable: true, scale: 3)
        decimal6(nullable: true, scale: 3)
        decimal7(nullable: true, scale: 3)
        decimal8(nullable: true, scale: 3)
        decimal9(nullable: true, scale: 3)
        decimal10(nullable: true, scale: 3)
        decimal11(nullable: true, scale: 3)
        decimal12(nullable: true, scale: 3)
        string1(nullable: true)
        string2(nullable: true)
        string3(nullable: true)
        string4(nullable: true)
        date1(nullable: true)
        date2(nullable: true)
        date3(nullable: true)
        date4(nullable: true)
        boolean1(nullable: true)
        boolean2(nullable: true)
        boolean3(nullable: true)
        boolean4(nullable: true)
    }
}
