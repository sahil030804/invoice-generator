package com.kjbilling.app.pdf

import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class PaymentSummaryTest {

    @Test
    fun partial_showsPaidAndBalance() {
        val summary = PaymentSummary.build(PaymentStatus.PARTIAL, PaymentMethod.UPI, BigDecimal("500"), BigDecimal("916"))

        assertEquals("Payment Details", summary.title)
        assertTrue(summary.lines.contains("Status: Partly paid (UPI)"))
        assertTrue(summary.lines.contains("Paid: ₹500.00"))
        assertTrue(summary.lines.contains("Balance: ₹916.00"))
    }

    @Test
    fun paid_hasNoBalance() {
        val summary = PaymentSummary.build(PaymentStatus.PAID, PaymentMethod.CASH, BigDecimal("118"), BigDecimal.ZERO)

        assertEquals("Payment Status: Paid", summary.title)
        assertEquals(listOf("Status: Paid (Cash)"), summary.lines)
    }

    @Test
    fun unpaid_showsPendingAndBalance_withoutMethod() {
        val summary = PaymentSummary.build(PaymentStatus.UNPAID, null, BigDecimal.ZERO, BigDecimal("1416"))

        assertEquals(listOf("Status: Pending", "Balance: ₹1,416.00"), summary.lines)
        assertFalse(summary.lines.any { it.startsWith("Paid:") })
    }

    @Test
    fun singleLine_joinsWithDots() {
        val summary = PaymentSummary.build(PaymentStatus.PARTIAL, null, BigDecimal("500"), BigDecimal("916"))

        assertEquals("Status: Partly paid  ·  Paid: ₹500.00  ·  Balance: ₹916.00", summary.singleLine())
    }
}
