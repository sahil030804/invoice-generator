package com.kjbilling.app.domain.keypad

import com.kjbilling.app.domain.keypad.KeypadKey.Back
import com.kjbilling.app.domain.keypad.KeypadKey.Digit
import com.kjbilling.app.domain.keypad.KeypadKey.Dot
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class AmountInputTest {

    private fun type(vararg keys: KeypadKey): String = keys.fold("") { acc, key -> AmountInput.press(acc, key) }

    @Test
    fun leadingZero_isReplacedByNextDigit() {
        assertEquals("7", type(Digit(0), Digit(7)))
        assertEquals("0", type(Digit(0), Digit(0)))
    }

    @Test
    fun dot_onEmpty_becomesZeroDot_andOnlyOneDotAllowed() {
        assertEquals("0.", type(Dot))
        assertEquals("12.5", type(Digit(1), Digit(2), Dot, Dot, Digit(5)))
    }

    @Test
    fun atMostTwoDecimals() {
        assertEquals("9.99", type(Digit(9), Dot, Digit(9), Digit(9), Digit(9)))
    }

    @Test
    fun atMostSevenIntegerDigits() {
        assertEquals("1234567", type(Digit(1), Digit(2), Digit(3), Digit(4), Digit(5), Digit(6), Digit(7), Digit(8)))
    }

    @Test
    fun back_removesLastCharacter_downToEmpty() {
        assertEquals("1", type(Digit(1), Digit(2), Back))
        assertEquals("", type(Digit(1), Back, Back))
    }

    @Test
    fun toAmount_nullForBlankOrZero() {
        assertNull(AmountInput.toAmount(""))
        assertNull(AmountInput.toAmount("0"))
        assertNull(AmountInput.toAmount("0.0"))
        assertEquals(BigDecimal("12.5"), AmountInput.toAmount("12.5"))
        assertEquals(BigDecimal("250"), AmountInput.toAmount("250."))
    }

    @Test
    fun display_usesIndianGrouping_andKeepsTypedDecimals() {
        assertEquals("₹0", AmountInput.display(""))
        assertEquals("₹0.", AmountInput.display("0."))
        assertEquals("₹250", AmountInput.display("250"))
        assertEquals("₹12,34,567.5", AmountInput.display("1234567.5"))
        assertEquals("₹1,000", AmountInput.display("1000"))
    }
}
