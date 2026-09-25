package com.kjbilling.app.domain.numbering

/**
 * Pure invoice-number logic (no DB), so it can be unit tested.
 *
 *   prefix "INV-" + counter 8  →  "INV-0008"
 *   if "INV-0008" already exists, try 9, 10, ... until a free one is found
 */
object InvoiceNumbering {

    const val MIN_DIGITS = 4

    /** [number] to use now, and the [nextCounter] value to store for the following invoice. */
    data class Next(val number: String, val nextCounter: Long)

    fun format(prefix: String, counter: Long): String {
        return "$prefix${counter.toString().padStart(MIN_DIGITS, '0')}"
    }

    /** First number at or after [startCounter] for which [exists] is false. */
    inline fun nextFree(prefix: String, startCounter: Long, exists: (String) -> Boolean): Next {
        var counter = startCounter
        var number = format(prefix, counter)
        while (exists(number)) {
            counter++
            number = format(prefix, counter)
        }
        return Next(number, counter + 1)
    }
}
