package com.kjbilling.app.domain.khata

import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class KhataTest {

    private fun inv(
        id: Long,
        total: String,
        paid: String = "0",
        customerId: Long? = 7,
        date: Long = id,
        status: InvoiceStatus = InvoiceStatus.GENERATED,
        paymentStatus: PaymentStatus? = null
    ) = Invoice(
        id = id, invoiceNumber = "INV-$id", invoiceDate = date, createdAt = date, customerId = customerId,
        customerName = "Customer $customerId", grandTotal = BigDecimal(total), amountPaid = BigDecimal(paid),
        status = status,
        paymentStatus = paymentStatus ?: when {
            BigDecimal(paid).compareTo(BigDecimal(total)) >= 0 -> PaymentStatus.PAID
            BigDecimal(paid).signum() > 0 -> PaymentStatus.PARTIAL
            else -> PaymentStatus.UNPAID
        }
    )

    // ---- allocator ----

    @Test
    fun allocate_paysOldestFirst_andSpillsOver() {
        val bills = listOf(inv(2, "100", date = 20), inv(1, "50", date = 10), inv(3, "80", date = 30))

        val result = KhataAllocator.allocate(bills, BigDecimal("120"))

        assertEquals(listOf(1L, 2L), result.allocations.map { it.invoiceId })
        assertEquals(listOf(BigDecimal("50"), BigDecimal("70")), result.allocations.map { it.applied })
        assertEquals(PaymentStatus.PAID, result.allocations[0].update.paymentStatus)
        assertEquals(PaymentStatus.PARTIAL, result.allocations[1].update.paymentStatus)
        assertEquals(0, BigDecimal.ZERO.compareTo(result.unapplied))
    }

    @Test
    fun allocate_overpayment_isReportedUnapplied() {
        val result = KhataAllocator.allocate(listOf(inv(1, "100", "40")), BigDecimal("100"))

        assertEquals(BigDecimal("60"), result.allocations.single().applied)
        assertEquals(0, BigDecimal("40").compareTo(result.unapplied))
    }

    @Test
    fun allocate_skipsCancelledAndPaid_includesDraft() {
        val bills = listOf(
            inv(1, "100", status = InvoiceStatus.CANCELLED),
            inv(2, "100", "100"),
            inv(3, "30", status = InvoiceStatus.DRAFT)
        )

        val result = KhataAllocator.allocate(bills, BigDecimal("30"))

        assertEquals(listOf(3L), result.allocations.map { it.invoiceId })
    }

    @Test
    fun allocate_resultIndependentOfInputOrder() {
        val a = listOf(inv(1, "50", date = 10), inv(2, "50", date = 20))

        assertEquals(KhataAllocator.allocate(a, BigDecimal("60")), KhataAllocator.allocate(a.reversed(), BigDecimal("60")))
    }

    // ---- summary ----

    private val customers = listOf(
        Customer(id = 7, name = "Rakesh"),
        Customer(id = 8, name = "Vikram"),
        Customer(id = 1, name = "Walk-in Customer", isWalkIn = true)
    )

    @Test
    fun summarize_groupsByCustomer_sortedByDue_andUnlinkedSeparately() {
        val bills = listOf(
            inv(1, "100", customerId = 7),
            inv(2, "300", "100", customerId = 8),
            inv(3, "50", customerId = 7),
            inv(4, "40", customerId = null),
            inv(5, "25", customerId = 1),
            inv(6, "999", customerId = 7, status = InvoiceStatus.CANCELLED)
        )

        val summary = KhataCalculator.summarize(bills, customers)

        assertEquals(listOf("Vikram", "Rakesh"), summary.customers.map { it.name })
        assertEquals(BigDecimal("200"), summary.customers[0].due)
        assertEquals(BigDecimal("150"), summary.customers[1].due)
        assertEquals(2, summary.customers[1].openBills)
        assertEquals(listOf(4L, 5L), summary.unlinked.map { it.id })
        assertEquals(0, BigDecimal("415").compareTo(summary.total))
    }

    @Test
    fun summarize_deletedCustomer_keepsInvoiceName() {
        val summary = KhataCalculator.summarize(listOf(inv(1, "100", customerId = 99)), customers)

        assertEquals("Customer 99", summary.customers.single().name)
    }

    @Test
    fun total_equalsDashboardPendingFormula() {
        val bills = listOf(inv(1, "100"), inv(2, "80", "30"), inv(3, "60", "60"), inv(4, "70", status = InvoiceStatus.CANCELLED))
        val pending = bills
            .filter { it.paymentStatus != PaymentStatus.PAID && it.status != InvoiceStatus.CANCELLED }
            .fold(BigDecimal.ZERO) { acc, i -> acc + i.balanceDue }

        assertEquals(0, pending.compareTo(KhataCalculator.summarize(bills, customers).total))
    }

    @Test
    fun dueFor_singleCustomer() {
        val bills = listOf(inv(1, "100"), inv(2, "20", customerId = 8))

        assertEquals(0, BigDecimal("100").compareTo(KhataCalculator.dueFor(7, bills)))
    }

    // ---- reminder ----

    @Test
    fun whatsAppNumber_normalizesIndianMobiles() {
        assertEquals("919876543210", PhoneNumbers.toWhatsApp("+91 98765 43210"))
        assertEquals("919876543210", PhoneNumbers.toWhatsApp("09876543210"))
        assertEquals("919876543210", PhoneNumbers.toWhatsApp("919876543210"))
        assertEquals("919876543210", PhoneNumbers.toWhatsApp("98765-43210"))
        assertNull(PhoneNumbers.toWhatsApp("5876543210"))
        assertNull(PhoneNumbers.toWhatsApp("12345"))
        assertNull(PhoneNumbers.toWhatsApp(null))
    }

    @Test
    fun reminderMessage_includesAmountBillsShopAndUpi() {
        val text = ReminderMessage.build("Rakesh", BigDecimal("1250"), 3, "KJ Plastic", "kjplastic@okaxis")

        assertTrue(text, text.contains("Rakesh"))
        assertTrue(text, text.contains("₹1,250.00"))
        assertTrue(text, text.contains("3 bills"))
        assertTrue(text, text.contains("KJ Plastic"))
        assertTrue(text, text.contains("kjplastic@okaxis"))
    }

    @Test
    fun reminderMessage_singleBill_noUpi() {
        val text = ReminderMessage.build("Vikram", BigDecimal("50"), 1, "KJ Plastic", null)

        assertTrue(text, text.contains("1 bill)"))
        assertTrue(text, !text.contains("UPI"))
    }
}
