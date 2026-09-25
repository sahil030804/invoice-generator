package com.kjbilling.app.domain.payment

import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class PaymentRulesTest {

    private fun invoice(total: String, paid: String = "0", status: InvoiceStatus = InvoiceStatus.GENERATED) = Invoice(
        invoiceNumber = "INV-1", invoiceDate = 0L, customerName = "x",
        grandTotal = BigDecimal(total), amountPaid = BigDecimal(paid), status = status,
        paymentStatus = if (BigDecimal(paid).signum() > 0) PaymentStatus.PARTIAL else PaymentStatus.UNPAID
    )

    @Test
    fun ignoresCancelled_nonPositive_andOverBalance() {
        assertNull(PaymentRules.apply(invoice("100", status = InvoiceStatus.CANCELLED), BigDecimal("10")))
        assertNull(PaymentRules.apply(invoice("100"), BigDecimal.ZERO))
        assertNull(PaymentRules.apply(invoice("100"), BigDecimal("-5")))
        assertNull(PaymentRules.apply(invoice("100", "60"), BigDecimal("41")))
    }

    @Test
    fun partPayment_isPartial_andKeepsStatus() {
        val update = PaymentRules.apply(invoice("100"), BigDecimal("40"))!!

        assertEquals(BigDecimal("40"), update.amountPaid)
        assertEquals(PaymentStatus.PARTIAL, update.paymentStatus)
        assertEquals(InvoiceStatus.GENERATED, update.status)
    }

    @Test
    fun fullPayment_marksPaid() {
        val update = PaymentRules.apply(invoice("100", "60"), BigDecimal("40"))!!

        assertEquals(0, BigDecimal("100").compareTo(update.amountPaid))
        assertEquals(PaymentStatus.PAID, update.paymentStatus)
        assertEquals(InvoiceStatus.PAID, update.status)
    }

    @Test
    fun draftStaysDraft_onPartPayment() {
        val update = PaymentRules.apply(invoice("100", status = InvoiceStatus.DRAFT), BigDecimal("10"))!!

        assertEquals(InvoiceStatus.DRAFT, update.status)
    }

    @Test
    fun scaleDifferences_doNotMatter() {
        val update = PaymentRules.apply(invoice("500.00"), BigDecimal("500"))!!

        assertEquals(PaymentStatus.PAID, update.paymentStatus)
    }
}
