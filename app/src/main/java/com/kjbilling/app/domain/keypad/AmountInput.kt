package com.kjbilling.app.domain.keypad

import java.math.BigDecimal

/** One key on the amount keypad. */
sealed interface KeypadKey {
    data class Digit(val value: Int) : KeypadKey {
        init {
            require(value in 0..9) { "Digit must be 0-9" }
        }
    }

    data object Dot : KeypadKey

    data object Back : KeypadKey
}

/**
 * Pure keypad state machine: the typed text is the state.
 *
 *   ""  -1-> "1"  -.-> "1."  -5-> "1.5"  -⌫-> "1."
 *
 * Rules: no leading zeros, one decimal point, at most 2 decimals and 7 integer digits
 * (₹99,99,999.99 max, well under the invoice price limit).
 */
object AmountInput {

    const val MAX_INTEGER_DIGITS = 7
    const val MAX_DECIMALS = 2

    private const val RUPEE = "₹"

    fun press(text: String, key: KeypadKey): String {
        return when (key) {
            is KeypadKey.Back -> text.dropLast(1)
            is KeypadKey.Dot -> when {
                text.contains('.') -> text
                text.isEmpty() -> "0."
                else -> "$text."
            }
            is KeypadKey.Digit -> appendDigit(text, key.value)
        }
    }

    /** Parsed amount, or null when nothing billable was typed (blank or zero). */
    fun toAmount(text: String): BigDecimal? {
        val amount = text.trimEnd('.').toBigDecimalOrNull() ?: return null
        return amount.takeIf { it.signum() > 0 }
    }

    /** "1234567.5" → "₹12,34,567.5" (Indian grouping, typed decimals kept as-is). */
    fun display(text: String): String {
        if (text.isEmpty()) {
            return "${RUPEE}0"
        }
        val integerPart = text.substringBefore('.')
        val rest = if (text.contains('.')) "." + text.substringAfter('.') else ""
        return RUPEE + groupIndian(integerPart) + rest
    }

    private fun appendDigit(text: String, digit: Int): String {
        if (text == "0") {
            return digit.toString()
        }
        val dot = text.indexOf('.')
        return if (dot >= 0) {
            if (text.length - dot - 1 >= MAX_DECIMALS) text else text + digit
        } else {
            if (text.length >= MAX_INTEGER_DIGITS) text else text + digit
        }
    }

    private fun groupIndian(digits: String): String {
        if (digits.length <= 3) {
            return digits
        }
        val lastThree = digits.takeLast(3)
        val head = digits.dropLast(3).reversed().chunked(2).joinToString(",").reversed()
        return "$head,$lastThree"
    }
}
