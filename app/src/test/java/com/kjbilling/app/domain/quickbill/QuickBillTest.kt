package com.kjbilling.app.domain.quickbill

import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class QuickBillTest {

    private val rakesh = Customer(id = 7, name = "Rakesh")
    private val walkIn = Customer(id = 1, name = "Walk-in Customer", isWalkIn = true)

    @Test
    fun udhaar_needsARealCustomer() {
        assertTrue(QuickBill.canGenerate(1, QuickPaymentMode.UDHAAR, rakesh))
        assertFalse(QuickBill.canGenerate(1, QuickPaymentMode.UDHAAR, null))
        assertFalse(QuickBill.canGenerate(1, QuickPaymentMode.UDHAAR, walkIn))
    }

    @Test
    fun cashAndUpi_workForWalkIn_butNeedItems() {
        assertTrue(QuickBill.canGenerate(1, QuickPaymentMode.CASH, null))
        assertTrue(QuickBill.canGenerate(2, QuickPaymentMode.UPI, null))
        assertFalse(QuickBill.canGenerate(0, QuickPaymentMode.CASH, rakesh))
    }

    @Test
    fun paymentFields_perMode() {
        val total = BigDecimal("118.00")

        val cash = QuickBill.paymentFields(QuickPaymentMode.CASH, total)
        assertEquals(InvoiceStatus.PAID, cash.status)
        assertEquals(PaymentStatus.PAID, cash.paymentStatus)
        assertEquals(PaymentMethod.CASH, cash.method)
        assertEquals(total, cash.amountPaid)

        assertEquals(PaymentMethod.UPI, QuickBill.paymentFields(QuickPaymentMode.UPI, total).method)

        val udhaar = QuickBill.paymentFields(QuickPaymentMode.UDHAAR, total)
        assertEquals(InvoiceStatus.GENERATED, udhaar.status)
        assertEquals(PaymentStatus.UNPAID, udhaar.paymentStatus)
        assertNull(udhaar.method)
        assertEquals(BigDecimal.ZERO, udhaar.amountPaid)
    }

    @Test
    fun successLabel_describesHowItWasPaid() {
        fun invoice(status: PaymentStatus, method: PaymentMethod?) =
            Invoice(invoiceNumber = "INV-1", invoiceDate = 0L, customerName = "x", paymentStatus = status, paymentMethod = method)

        assertEquals("(Paid in Cash)", QuickBill.successLabel(invoice(PaymentStatus.PAID, PaymentMethod.CASH)))
        assertEquals("(Paid by UPI)", QuickBill.successLabel(invoice(PaymentStatus.PAID, PaymentMethod.UPI)))
        assertEquals("(Udhaar - added to Khata)", QuickBill.successLabel(invoice(PaymentStatus.UNPAID, null)))
    }

    @Test
    fun customItemName_defaultsToMiscItem() {
        assertEquals("Misc item", QuickBill.customItemName("  "))
        assertEquals("Chai", QuickBill.customItemName(" Chai "))
    }

    @Test
    fun billableCount_ignoresDeletedProductsAndZeroQuantities() {
        val quantities = mapOf(1L to 2, 2L to 3, 3L to 0)

        // product 2 was deleted from the catalog while in the cart
        assertEquals(2, QuickBill.billableCount(quantities, customItemCount = 0, existingProductIds = setOf(1L, 3L)))
    }

    @Test
    fun billableCount_addsCustomItems() {
        assertEquals(3, QuickBill.billableCount(mapOf(1L to 2), customItemCount = 1, existingProductIds = setOf(1L)))
        assertEquals(1, QuickBill.billableCount(emptyMap(), customItemCount = 1, existingProductIds = emptySet()))
    }

    @Test
    fun billableCount_zeroWhenOnlyGhostProducts_soGenerateStaysDisabled() {
        val count = QuickBill.billableCount(mapOf(9L to 4), customItemCount = 0, existingProductIds = setOf(1L))

        assertEquals(0, count)
        assertFalse(QuickBill.canGenerate(count, QuickPaymentMode.CASH, null))
    }
}
