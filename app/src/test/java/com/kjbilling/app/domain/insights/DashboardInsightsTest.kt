package com.kjbilling.app.domain.insights

import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.math.BigDecimal
import java.time.ZoneId
import java.time.ZonedDateTime

class DashboardInsightsTest {

    private val ist = ZoneId.of("Asia/Kolkata")
    private val utc = ZoneId.of("UTC")

    // Fri 25 Sep 2026, 15:00 IST
    private val now = ZonedDateTime.of(2026, 9, 25, 15, 0, 0, 0, ist).toInstant().toEpochMilli()

    private fun at(y: Int, m: Int, d: Int, h: Int = 12, min: Int = 0, s: Int = 0, nano: Int = 0, zone: ZoneId = ist) =
        ZonedDateTime.of(y, m, d, h, min, s, nano, zone).toInstant().toEpochMilli()

    private fun inv(
        id: Long,
        total: String,
        date: Long,
        paid: String = "0",
        status: InvoiceStatus = InvoiceStatus.GENERATED
    ) = Invoice(
        id = id, invoiceNumber = "INV-$id", invoiceDate = date, customerName = "x",
        grandTotal = BigDecimal(total), amountPaid = BigDecimal(paid), status = status,
        paymentStatus = when {
            BigDecimal(paid).compareTo(BigDecimal(total)) >= 0 -> PaymentStatus.PAID
            BigDecimal(paid).signum() > 0 -> PaymentStatus.PARTIAL
            else -> PaymentStatus.UNPAID
        }
    )

    private fun sold(productId: Long?, name: String, qty: String, revenue: String, date: Long) =
        SoldItem(productId, name, BigDecimal(qty), BigDecimal(revenue), date)

    @Test
    fun todaysSales_sumsNonCancelledBillsOfToday_paidOrNot() {
        val invoices = listOf(
            inv(1, "100", at(2026, 9, 25, 9)),
            inv(2, "50", at(2026, 9, 25, 14), paid = "50"),
            inv(3, "999", at(2026, 9, 25, 10), status = InvoiceStatus.CANCELLED),
            inv(4, "70", at(2026, 9, 24, 23, 59))
        )

        val insights = DashboardInsights.compute(invoices, emptyList(), now, ist)

        assertEquals(0, BigDecimal("150").compareTo(insights.todaysSales))
        assertEquals(2, insights.billsToday)
    }

    @Test
    fun pending_isOutstandingBalanceOfAllNonCancelledBills() {
        val invoices = listOf(
            inv(1, "100", at(2026, 9, 1)),
            inv(2, "80", at(2026, 9, 2), paid = "30"),
            inv(3, "60", at(2026, 9, 3), paid = "60"),
            inv(4, "70", at(2026, 9, 4), status = InvoiceStatus.CANCELLED)
        )

        assertEquals(0, BigDecimal("150").compareTo(DashboardInsights.compute(invoices, emptyList(), now, ist).pending))
    }

    @Test
    fun sevenDailyBuckets_endToday_withCorrectTotals() {
        val invoices = listOf(
            inv(1, "10", at(2026, 9, 25, 0, 0)),                       // today 00:00 → today
            inv(2, "20", at(2026, 9, 24, 23, 59, 59, 999_000_000)),    // yesterday last ms
            inv(3, "40", at(2026, 9, 19, 8)),                           // 6 days ago → first bucket
            inv(4, "80", at(2026, 9, 18, 23, 59)),                      // 7 days ago → excluded
            inv(5, "160", at(2026, 9, 26, 1)),                          // tomorrow → excluded
            inv(6, "320", at(2026, 9, 22), status = InvoiceStatus.CANCELLED)
        )

        val days = DashboardInsights.compute(invoices, emptyList(), now, ist).days

        assertEquals(7, days.size)
        assertEquals(listOf(40, 0, 0, 0, 0, 20, 10), days.map { it.total.toInt() })
        assertEquals(at(2026, 9, 19, 0, 0), days.first().dayStart)
        assertEquals(at(2026, 9, 25, 0, 0), days.last().dayStart)
        assertEquals(listOf("S", "S", "M", "T", "W", "T", "F"), days.map { it.dayLetter }) // Sat 19 … Fri 25 Sep 2026
    }

    @Test
    fun lastBucket_equalsTodaysSales() {
        val invoices = listOf(inv(1, "100", at(2026, 9, 25, 9)), inv(2, "35.50", at(2026, 9, 25, 11)))

        val insights = DashboardInsights.compute(invoices, emptyList(), now, ist)

        assertEquals(0, insights.todaysSales.compareTo(insights.days.last().total))
    }

    @Test
    fun timeZone_changesWhichDayABillFallsOn() {
        // 23:30 IST on the 24th is 18:00 UTC on the 24th; 01:00 IST on the 25th is 19:30 UTC on the 24th.
        val invoices = listOf(inv(1, "100", at(2026, 9, 25, 1, 0)))

        val ist = DashboardInsights.compute(invoices, emptyList(), now, ist)
        val utc = DashboardInsights.compute(invoices, emptyList(), at(2026, 9, 25, 15, 0, zone = utc), utc)

        assertEquals(1, ist.billsToday)
        assertEquals(0, utc.billsToday)
    }

    @Test
    fun topSeller_byQuantity_catalogOnly_lastSevenDays() {
        val items = listOf(
            sold(1, "Bulb 9W", "10", "1000", at(2026, 9, 25)),
            sold(1, "Bulb 9W", "8", "800", at(2026, 9, 24)),
            sold(2, "Wire", "5", "9999", at(2026, 9, 25)),
            sold(null, "Misc item", "100", "5000", at(2026, 9, 25)),
            sold(3, "Old thing", "500", "500", at(2026, 9, 10))
        )

        val top = DashboardInsights.compute(emptyList(), items, now, ist).topSeller

        assertEquals("Bulb 9W", top!!.name)
        assertEquals(0, BigDecimal("18").compareTo(top.quantity))
    }

    @Test
    fun topSeller_tieBrokenByRevenueThenName() {
        val byRevenue = listOf(sold(1, "A", "5", "100", now), sold(2, "B", "5", "300", now))
        assertEquals("B", DashboardInsights.compute(emptyList(), byRevenue, now, ist).topSeller!!.name)

        val byName = listOf(sold(1, "Zed", "5", "100", now), sold(2, "Alpha", "5", "100", now))
        assertEquals("Alpha", DashboardInsights.compute(emptyList(), byName, now, ist).topSeller!!.name)
    }

    @Test
    fun topSeller_decimalQuantities_andNoneWhenEmpty() {
        val items = listOf(sold(1, "Rice", "2.5", "100", now), sold(1, "Rice", "0.5", "20", now))

        assertEquals(0, BigDecimal("3.0").compareTo(DashboardInsights.compute(emptyList(), items, now, ist).topSeller!!.quantity))
        assertNull(DashboardInsights.compute(emptyList(), emptyList(), now, ist).topSeller)
    }
}
