package com.kjbilling.app.domain.insights

import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Test
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZonedDateTime

class HistoryFiltersTest {

    private val ist = ZoneId.of("Asia/Kolkata")
    private fun at(d: Int, h: Int = 12) = ZonedDateTime.of(2026, 9, d, h, 0, 0, 0, ist).toInstant().toEpochMilli()
    private val now = at(25, 15)

    private fun inv(
        id: Long,
        date: Long,
        payment: PaymentStatus,
        status: InvoiceStatus = InvoiceStatus.GENERATED
    ) = Invoice(
        id = id, invoiceNumber = "INV-$id", invoiceDate = date, customerName = "x",
        grandTotal = BigDecimal("100"), status = status, paymentStatus = payment
    )

    private val all = listOf(
        inv(1, at(25), PaymentStatus.PAID, InvoiceStatus.PAID),
        inv(2, at(25, 9), PaymentStatus.UNPAID),
        inv(3, at(24), PaymentStatus.PARTIAL),
        inv(4, at(23), PaymentStatus.UNPAID, InvoiceStatus.DRAFT),
        inv(5, at(25, 8), PaymentStatus.UNPAID, InvoiceStatus.CANCELLED)
    )

    @Test
    fun all_returnsEverything() {
        assertEquals(listOf(1L, 2L, 3L, 4L, 5L), HistoryFilters.apply(all, HistoryFilter.ALL, now, ist).map { it.id })
    }

    @Test
    fun unpaid_includesPartialAndDraft_excludesPaidAndCancelled() {
        assertEquals(listOf(2L, 3L, 4L), HistoryFilters.apply(all, HistoryFilter.UNPAID, now, ist).map { it.id })
    }

    @Test
    fun today_onlyBillsDatedToday() {
        assertEquals(listOf(1L, 2L, 5L), HistoryFilters.apply(all, HistoryFilter.TODAY, now, ist).map { it.id })
    }
}
