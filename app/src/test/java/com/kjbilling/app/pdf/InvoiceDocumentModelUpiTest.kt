package com.kjbilling.app.pdf

import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.domain.model.TaxBreakdown
import com.kjbilling.app.domain.model.TaxType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal

class InvoiceDocumentModelUpiTest {

    private val profile = BusinessProfile(
        businessName = "KJ Plastic", ownerName = "Sahil", mobile = "9876543210",
        address = "Shop 12", state = "Maharashtra", upiId = "kjplastic@okaxis"
    )

    private fun invoice(paid: String = "0.00", status: PaymentStatus = PaymentStatus.UNPAID) = Invoice(
        invoiceNumber = "INV-0008", invoiceDate = 0L, customerName = "Rakesh",
        subtotal = BigDecimal("84.75"), totalTax = BigDecimal("15.25"), grandTotal = BigDecimal("100.00"),
        amountPaid = BigDecimal(paid), paymentStatus = status, status = InvoiceStatus.GENERATED,
        taxType = TaxType.CGST_SGST
    )

    private val oddPaiseBreakdown = listOf(
        TaxBreakdown(
            gstRate = BigDecimal("18"), taxableAmount = BigDecimal("84.75"),
            cgstAmount = BigDecimal("7.63"), sgstAmount = BigDecimal("7.62"), totalTax = BigDecimal("15.25")
        )
    )

    @Test
    fun unpaidInvoiceWithUpi_hasQrForBalance() {
        val doc = InvoiceDocumentModel.fromInvoice(invoice(), profile, emptyList())

        assertNotNull(doc.upiQr)
        assertEquals("Scan to pay ₹100.00", doc.upiQr!!.caption)
        assertEquals("UPI: kjplastic@okaxis", doc.upiQr!!.vpaLine)
    }

    @Test
    fun paidInvoiceOrNoUpi_hasNoQr() {
        assertNull(InvoiceDocumentModel.fromInvoice(invoice("100.00", PaymentStatus.PAID), profile, emptyList()).upiQr)
        assertNull(InvoiceDocumentModel.fromInvoice(invoice(), profile.copy(upiId = null), emptyList()).upiQr)
    }

    @Test
    fun partialPayment_isPrintedOnPdf() {
        val doc = InvoiceDocumentModel.fromInvoice(invoice("40.00", PaymentStatus.PARTIAL), profile, emptyList())

        assertEquals(true, doc.payment.lines.contains("Paid: ₹40.00"))
        assertEquals(true, doc.payment.lines.contains("Balance: ₹60.00"))
    }

    @Test
    fun cgstSgstRows_useActualSplit_notHalfOfTotal() {
        val doc = InvoiceDocumentModel.fromInvoice(invoice(), profile, oddPaiseBreakdown)

        assertEquals(listOf("₹7.63", "₹7.62"), doc.taxSummary.map { it.amount })
    }
}
