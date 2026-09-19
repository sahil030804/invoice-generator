package com.kjbilling.app.domain.formatter

import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal

class CurrencyFormatterTest {

    @Test
    fun testZero() {
        assertEquals("₹0.00", CurrencyFormatter.format(BigDecimal("0")))
        assertEquals("0.00", CurrencyFormatter.formatWithoutSymbol(BigDecimal("0")))
    }

    @Test
    fun testThousands() {
        assertEquals("₹1,000.00", CurrencyFormatter.format(BigDecimal("1000")))
    }

    @Test
    fun testLakhs() {
        assertEquals("₹1,25,000.00", CurrencyFormatter.format(BigDecimal("125000")))
    }

    @Test
    fun testMillionsAndAbove() {
        assertEquals("₹12,34,567.89", CurrencyFormatter.format(BigDecimal("1234567.89")))
    }
    
    @Test
    fun testOnlyPaise() {
        assertEquals("₹0.50", CurrencyFormatter.format(BigDecimal("0.50")))
    }

    @Test
    fun testMaxAmount() {
        assertEquals("₹9,99,99,99,999.99", CurrencyFormatter.format(BigDecimal("9999999999.99")))
    }
}
