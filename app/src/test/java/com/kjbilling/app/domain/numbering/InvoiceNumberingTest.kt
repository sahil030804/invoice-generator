package com.kjbilling.app.domain.numbering

import org.junit.Assert.assertEquals
import org.junit.Test

class InvoiceNumberingTest {

    @Test
    fun format_padsToFourDigits() {
        assertEquals("INV-0007", InvoiceNumbering.format("INV-", 7))
    }

    @Test
    fun format_keepsFullNumberBeyondFourDigits() {
        assertEquals("INV-12345", InvoiceNumbering.format("INV-", 12345))
    }

    @Test
    fun nextFree_unusedStart_returnsItAndAdvancesCounter() {
        val next = InvoiceNumbering.nextFree("INV-", 8) { false }

        assertEquals("INV-0008", next.number)
        assertEquals(9L, next.nextCounter)
    }

    @Test
    fun nextFree_skipsNumbersAlreadyIssued() {
        val used = setOf("INV-0003", "INV-0004", "INV-0005")

        val next = InvoiceNumbering.nextFree("INV-", 3) { it in used }

        assertEquals("INV-0006", next.number)
        assertEquals(7L, next.nextCounter)
    }

    @Test
    fun nextFree_counterLoweredBelowIssued_neverReturnsDuplicate() {
        val used = (1..7).map { InvoiceNumbering.format("INV-", it.toLong()) }.toSet()

        val next = InvoiceNumbering.nextFree("INV-", 1) { it in used }

        assertEquals("INV-0008", next.number)
    }
}
