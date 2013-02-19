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
package doc

import org.grails.tlc.books.Document
import org.grails.tlc.books.GeneralTransaction

class Tax extends GeneralTransaction {

    static belongsTo = [document: Document]

    static constraints = {
        taxCode(validator: {val, obj ->
            return (val != null)
        })
        taxPercentage(validator: {val, obj ->
            return (val != null)
        })
        documentTax(validator: {val, obj ->     // Used to hold the document currency goods total on a tax line since the posting amount is the tax itself
            return (val != null)
        })
        accountTax(validator: {val, obj ->      // Used to hold the account currency goods total on a tax line since the posting amount is the tax itself
            return (obj.customer || obj.supplier) ? (val != null) : true
        })
        companyTax(validator: {val, obj ->      // Used to hold the company currency goods total on a tax line since the posting amount is the tax itself
            return (val != null)
        })
    }
}
