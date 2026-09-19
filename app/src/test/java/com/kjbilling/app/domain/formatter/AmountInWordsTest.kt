package com.kjbilling.app.domain.formatter

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class AmountInWordsTest {

    @Test
    fun testZero() {
        assertEquals("Zero Rupees Only", AmountInWords.convert(BigDecimal("0.00")))
        assertEquals("Zero Rupees Only", AmountInWords.convert(BigDecimal("0")))
    }

    @Test
    fun testOne() {
        assertEquals("One Rupee Only", AmountInWords.convert(BigDecimal("1.00")))
    }

    @Test
    fun testOnlyPaise() {
        assertEquals("Fifty Paise Only", AmountInWords.convert(BigDecimal("0.50")))
    }

    @Test
    fun testRupeesAndPaise() {
        assertEquals(
            "One Thousand Two Hundred Thirty Four Rupees and Fifty Six Paise Only",
            AmountInWords.convert(BigDecimal("1234.56"))
        )
    }

    @Test
    fun testLakhs() {
        assertEquals("One Lakh Rupees Only", AmountInWords.convert(BigDecimal("100000")))
    }

    @Test
    fun testCrores() {
        assertEquals("One Crore Rupees Only", AmountInWords.convert(BigDecimal("10000000")))
    }

    @Test
    fun testMaxAmount() {
        assertEquals(
            "Nine Hundred Ninety Nine Crore Ninety Nine Lakh Ninety Nine Thousand Nine Hundred Ninety Nine Rupees and Ninety Nine Paise Only",
            AmountInWords.convert(BigDecimal("9999999999.99"))
        )
    }
}
