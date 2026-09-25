package com.kjbilling.app.domain.upi

import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class UpiPaymentTest {

    private val profile = BusinessProfile(
        businessName = "KJ Plastic",
        ownerName = "Sahil",
        mobile = "9876543210",
        address = "Shop 12",
        state = "Maharashtra",
        upiId = "kjplastic@okaxis"
    )

    private fun invoice(
        total: String = "118.00",
        paid: String = "0.00",
        status: InvoiceStatus = InvoiceStatus.GENERATED,
        paymentStatus: PaymentStatus = PaymentStatus.UNPAID
    ) = Invoice(
        invoiceNumber = "INV-0008",
        invoiceDate = 0L,
        customerName = "Rakesh",
        grandTotal = BigDecimal(total),
        amountPaid = BigDecimal(paid),
        status = status,
        paymentStatus = paymentStatus
    )

    @Test
    fun validVpas_areAccepted() {
        listOf("shop@okaxis", "98765@ybl", "kj.t-1@kotak811", "a_b@paytm").forEach {
            assertTrue(it, UpiPayment.isValidVpa(it))
        }
    }

    @Test
    fun invalidVpas_areRejected() {
        listOf(null, "", "   ", "shop", "@okaxis", "shop@", "a@@b", "sh op@ybl", "shop@123", "a@gmail.com", "shop@ok axis").forEach {
            assertFalse("$it", UpiPayment.isValidVpa(it))
        }
    }

    @Test
    fun normalizeVpa_trimsAndLowercases_blankBecomesNull() {
        assertEquals("shop@okaxis", UpiPayment.normalizeVpa("  Shop@OkAxis "))
        assertNull(UpiPayment.normalizeVpa("   "))
        assertNull(UpiPayment.normalizeVpa(null))
    }

    @Test
    fun formatAmount_alwaysTwoDecimals_halfUp() {
        assertEquals("100.00", UpiPayment.formatAmount(BigDecimal("100")))
        assertEquals("10.01", UpiPayment.formatAmount(BigDecimal("10.005")))
        assertEquals("1234567.80", UpiPayment.formatAmount(BigDecimal("1234567.8")))
    }

    @Test
    fun buildUri_exactFormat() {
        val uri = UpiPayment.buildUri("kjplastic@okaxis", "KJ Plastic", BigDecimal("118"), "INV-0008")

        assertEquals("upi://pay?pa=kjplastic@okaxis&pn=KJ%20Plastic&am=118.00&cu=INR&tn=INV-0008", uri)
    }

    @Test
    fun buildUri_encodesSpecialCharactersAndHindi_omitsBlankNote() {
        val uri = UpiPayment.buildUri("a@ybl", "A&B Stores", BigDecimal("5"), "INV/01")
        assertTrue(uri, uri.contains("pn=A%26B%20Stores"))
        assertTrue(uri, uri.contains("tn=INV%2F01"))

        val hindi = UpiPayment.buildUri("a@ybl", "राम स्टोर", BigDecimal("5"), " ")
        assertTrue(hindi, hindi.contains("pn=%E0%A4%B0"))
        assertFalse(hindi, hindi.contains("tn="))
    }

    @Test
    fun forInvoice_unpaid_usesFullBalance() {
        val request = UpiPayment.forInvoice(invoice(), profile)

        assertNotNull(request)
        assertEquals(BigDecimal("118.00"), request!!.amount)
        assertEquals("₹118.00", request.amountLabel)
        assertEquals("kjplastic@okaxis", request.vpa)
        assertTrue(request.uri.contains("am=118.00"))
        assertTrue(request.uri.contains("tn=INV-0008"))
    }

    @Test
    fun forInvoice_partial_usesRemainingBalance() {
        val request = UpiPayment.forInvoice(invoice(paid = "18.00", paymentStatus = PaymentStatus.PARTIAL), profile)

        assertEquals(BigDecimal("100.00"), request!!.amount)
    }

    @Test
    fun forInvoice_draftIsAllowed() {
        assertNotNull(UpiPayment.forInvoice(invoice(status = InvoiceStatus.DRAFT), profile))
    }

    @Test
    fun forInvoice_noQrWhenPaidCancelledOrNoUpi() {
        assertNull(UpiPayment.forInvoice(invoice(paid = "118.00", paymentStatus = PaymentStatus.PAID), profile))
        assertNull(UpiPayment.forInvoice(invoice(status = InvoiceStatus.CANCELLED), profile))
        assertNull(UpiPayment.forInvoice(invoice(), profile.copy(upiId = null)))
        assertNull(UpiPayment.forInvoice(invoice(), profile.copy(upiId = "not-a-vpa")))
        assertNull(UpiPayment.forInvoice(invoice(), null))
    }

    @Test
    fun forAmount_requiresPositiveAmount() {
        assertNotNull(UpiPayment.forAmount(BigDecimal("50"), profile))
        assertNull(UpiPayment.forAmount(BigDecimal.ZERO, profile))
    }
}
