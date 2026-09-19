package com.kjbilling.app.domain.formatter

import java.math.BigDecimal
import java.math.RoundingMode

object CurrencyFormatter {

    fun format(amount: BigDecimal): String {
        return "₹${formatWithoutSymbol(amount)}"
    }

    fun formatWithoutSymbol(amount: BigDecimal): String {
        val isNegative = amount < BigDecimal.ZERO
        val rounded = amount.abs().setScale(2, RoundingMode.HALF_UP)
        val parts = rounded.toPlainString().split(".")
        val groupedInt = groupIndian(parts[0])
        val fraction = parts.getOrElse(1) { "00" }.padEnd(2, '0').take(2)
        val formatted = "$groupedInt.$fraction"
        return if (isNegative) "-$formatted" else formatted
    }

    /**
     * Indian digit grouping: last 3 digits together, then groups of 2.
     * 125000 -> 1,25,000 | 1234567 -> 12,34,567 | 9999999999 -> 9,99,99,99,999
     */
    private fun groupIndian(intPart: String): String {
        val digits = intPart.trimStart('0').ifEmpty { "0" }
        if (digits.length <= 3) {
            return digits
        }
        val lastThree = digits.takeLast(3)
        var remaining = digits.dropLast(3)
        val groups = mutableListOf<String>()
        while (remaining.length > 2) {
            groups.add(0, remaining.takeLast(2))
            remaining = remaining.dropLast(2)
        }
        if (remaining.isNotEmpty()) {
            groups.add(0, remaining)
        }
        groups.add(lastThree)
        return groups.joinToString(",")
    }
}
