package com.kjbilling.app.domain.insights

import com.kjbilling.app.domain.model.Invoice
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.ZoneId
import java.time.ZonedDateTime

class DateGroupingTest {

    private val ist = ZoneId.of("Asia/Kolkata")
    private val utc = ZoneId.of("UTC")

    private fun at(y: Int, m: Int, d: Int, h: Int = 12, min: Int = 0, zone: ZoneId = ist) =
        ZonedDateTime.of(y, m, d, h, min, 0, 0, zone).toInstant().toEpochMilli()

    private fun inv(id: Long, date: Long) = Invoice(id = id, invoiceNumber = "INV-$id", invoiceDate = date, customerName = "x")

    private val now = at(2026, 9, 25, 15)

    @Test
    fun labels_todayYesterdayAndDate() {
        assertEquals("Today", DateGrouping.label(at(2026, 9, 25, 0, 1), now, ist))
        assertEquals("Yesterday", DateGrouping.label(at(2026, 9, 24, 23, 59), now, ist))
        assertEquals("21 Sep 2026", DateGrouping.label(at(2026, 9, 21), now, ist))
        assertEquals("1 Jan 2026", DateGrouping.label(at(2026, 1, 1), now, ist))
    }

    @Test
    fun yesterday_acrossYearBoundary() {
        val newYear = at(2027, 1, 1, 10)

        assertEquals("Yesterday", DateGrouping.label(at(2026, 12, 31, 20), newYear, ist))
        assertEquals("30 Dec 2026", DateGrouping.label(at(2026, 12, 30, 20), newYear, ist))
    }

    @Test
    fun sameInstant_isDifferentDayInDifferentZones() {
        val lateNightIst = at(2026, 9, 24, 23, 30) // 18:00 UTC on the 24th
        val nowUtc = at(2026, 9, 25, 3, 0, utc)

        assertEquals("Yesterday", DateGrouping.label(lateNightIst, now, ist))
        assertEquals("Yesterday", DateGrouping.label(lateNightIst, nowUtc, utc))
        assertEquals("Today", DateGrouping.label(at(2026, 9, 25, 1, 0), at(2026, 9, 25, 15), ist))
        assertEquals("Yesterday", DateGrouping.label(at(2026, 9, 25, 1, 0), at(2026, 9, 25, 15), utc))
    }

    @Test
    fun groups_keepOrder_andSplitByDay() {
        val list = listOf(
            inv(5, at(2026, 9, 25, 14)),
            inv(4, at(2026, 9, 25, 9)),
            inv(3, at(2026, 9, 24, 18)),
            inv(2, at(2026, 9, 20)),
            inv(1, at(2026, 9, 20, 8))
        )

        val groups = DateGrouping.groupByDay(list, now, ist)

        assertEquals(listOf("Today", "Yesterday", "20 Sep 2026"), groups.map { it.label })
        assertEquals(listOf(listOf(5L, 4L), listOf(3L), listOf(2L, 1L)), groups.map { g -> g.invoices.map { it.id } })
    }

    @Test
    fun unsortedInput_stillGivesOneGroupPerDay_newestFirst() {
        // Out-of-order dates (e.g. after a phone clock change) must never repeat a day header:
        // the list screen uses the label as a key, and duplicate keys crash Compose.
        val list = listOf(
            inv(1, at(2026, 9, 25, 9)),
            inv(2, at(2026, 9, 20)),
            inv(3, at(2026, 9, 25, 14)),
            inv(4, at(2026, 9, 24, 18)),
            inv(5, at(2026, 9, 20, 8))
        )

        val groups = DateGrouping.groupByDay(list, now, ist)

        assertEquals(listOf("Today", "Yesterday", "20 Sep 2026"), groups.map { it.label })
        assertEquals(listOf(listOf(3L, 1L), listOf(4L), listOf(2L, 5L)), groups.map { g -> g.invoices.map { it.id } })
    }
}
