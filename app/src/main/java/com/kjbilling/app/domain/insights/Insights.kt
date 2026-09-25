package com.kjbilling.app.domain.insights

import com.kjbilling.app.domain.khata.KhataCalculator
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

private const val CHART_DAYS = 7
private val MONTHS = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
private val DAY_LETTERS = mapOf(
    java.time.DayOfWeek.MONDAY to "M", java.time.DayOfWeek.TUESDAY to "T", java.time.DayOfWeek.WEDNESDAY to "W",
    java.time.DayOfWeek.THURSDAY to "T", java.time.DayOfWeek.FRIDAY to "F", java.time.DayOfWeek.SATURDAY to "S",
    java.time.DayOfWeek.SUNDAY to "S"
)

private fun Long.toLocalDate(zone: ZoneId): LocalDate = Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

private fun LocalDate.startMillis(zone: ZoneId): Long = atStartOfDay(zone).toInstant().toEpochMilli()

/** One invoice line sold (input for "Top seller"). [productId] is null for custom amounts. */
data class SoldItem(
    val productId: Long?,
    val name: String,
    val quantity: BigDecimal,
    val revenue: BigDecimal,
    val invoiceDate: Long
)

data class DaySales(val dayStart: Long, val dayLetter: String, val total: BigDecimal, val bills: Int)

data class TopSeller(val name: String, val quantity: BigDecimal)

data class InsightsResult(
    val todaysSales: BigDecimal,
    val billsToday: Int,
    val pending: BigDecimal,
    /** Last 7 days, oldest first, ending today. */
    val days: List<DaySales>,
    val topSeller: TopSeller?
)

/** Dashboard numbers, computed once from the invoice list. Pure and timezone-explicit. */
object DashboardInsights {

    fun compute(invoices: List<Invoice>, soldItems: List<SoldItem>, now: Long, zone: ZoneId): InsightsResult {
        val today = now.toLocalDate(zone)
        val billed = invoices.filter { it.status != InvoiceStatus.CANCELLED }
        val byDay = billed.groupBy { it.invoiceDate.toLocalDate(zone) }

        val days = (CHART_DAYS - 1 downTo 0).map { back ->
            val date = today.minusDays(back.toLong())
            val bills = byDay[date].orEmpty()
            DaySales(
                dayStart = date.startMillis(zone),
                dayLetter = DAY_LETTERS.getValue(date.dayOfWeek),
                total = bills.fold(BigDecimal.ZERO) { acc, b -> acc + b.grandTotal },
                bills = bills.size
            )
        }
        val todayBucket = days.last()

        val windowStart = today.minusDays((CHART_DAYS - 1).toLong()).startMillis(zone)
        return InsightsResult(
            todaysSales = todayBucket.total,
            billsToday = todayBucket.bills,
            pending = KhataCalculator.openInvoices(invoices).fold(BigDecimal.ZERO) { acc, b -> acc + b.balanceDue },
            days = days,
            topSeller = topSeller(soldItems, windowStart, now)
        )
    }

    private fun topSeller(items: List<SoldItem>, windowStart: Long, now: Long): TopSeller? {
        return items
            .filter { it.productId != null && it.invoiceDate in windowStart..now }
            .groupBy { it.productId }
            .map { (_, rows) ->
                Triple(rows.last().name, rows.fold(BigDecimal.ZERO) { a, r -> a + r.quantity }, rows.fold(BigDecimal.ZERO) { a, r -> a + r.revenue })
            }
            .sortedWith(
                compareByDescending<Triple<String, BigDecimal, BigDecimal>> { it.second }
                    .thenByDescending { it.third }
                    .thenBy { it.first }
            )
            .firstOrNull()
            ?.let { TopSeller(it.first, it.second) }
    }
}

data class DayGroup(val label: String, val invoices: List<Invoice>)

/** "Today" / "Yesterday" / "21 Sep 2026" headers (fixed English month names, so output never depends on device locale). */
object DateGrouping {

    fun label(millis: Long, now: Long, zone: ZoneId): String {
        val date = millis.toLocalDate(zone)
        val today = now.toLocalDate(zone)
        return when (date) {
            today -> "Today"
            today.minusDays(1) -> "Yesterday"
            else -> "${date.dayOfMonth} ${MONTHS[date.monthValue - 1]} ${date.year}"
        }
    }

    /**
     * One group per calendar day, newest day first. Sorting here (stable, so ties keep their order)
     * guarantees a day never repeats even if dates arrive out of order, which matters because list
     * screens use the day label as a key and duplicate keys crash Compose.
     */
    fun groupByDay(invoices: List<Invoice>, now: Long, zone: ZoneId): List<DayGroup> {
        val groups = mutableListOf<DayGroup>()
        var currentDate: LocalDate? = null
        var bucket = mutableListOf<Invoice>()
        var currentLabel = ""

        for (invoice in invoices.sortedByDescending { it.invoiceDate }) {
            val date = invoice.invoiceDate.toLocalDate(zone)
            if (date != currentDate) {
                if (bucket.isNotEmpty()) {
                    groups.add(DayGroup(currentLabel, bucket))
                }
                bucket = mutableListOf()
                currentDate = date
                currentLabel = label(invoice.invoiceDate, now, zone)
            }
            bucket.add(invoice)
        }
        if (bucket.isNotEmpty()) {
            groups.add(DayGroup(currentLabel, bucket))
        }
        return groups
    }
}

enum class HistoryFilter { ALL, UNPAID, TODAY }

object HistoryFilters {

    fun apply(invoices: List<Invoice>, filter: HistoryFilter, now: Long, zone: ZoneId): List<Invoice> {
        return when (filter) {
            HistoryFilter.ALL -> invoices
            // Unpaid = anything still owing money: unpaid, partly paid, draft; never cancelled.
            HistoryFilter.UNPAID -> invoices.filter {
                it.status != InvoiceStatus.CANCELLED && it.paymentStatus != PaymentStatus.PAID
            }
            HistoryFilter.TODAY -> {
                val today = now.toLocalDate(zone)
                invoices.filter { it.invoiceDate.toLocalDate(zone) == today }
            }
        }
    }
}

data class BackupStatusText(val text: String, val needsAttention: Boolean)

/** One-line backup reminder for the dashboard tile. */
object BackupStatus {

    private const val STALE_AFTER_DAYS = 7L

    fun describe(lastBackupAt: Long?, now: Long, hasData: Boolean, zone: ZoneId): BackupStatusText {
        if (lastBackupAt == null) {
            return if (hasData) BackupStatusText("Not backed up yet", true) else BackupStatusText("Nothing to back up yet", false)
        }
        val days = java.time.temporal.ChronoUnit.DAYS.between(lastBackupAt.toLocalDate(zone), now.toLocalDate(zone))
        val text = when {
            days <= 0 -> "Backed up today"
            days == 1L -> "Backed up yesterday"
            else -> "Backed up $days days ago"
        }
        return BackupStatusText(text, days > STALE_AFTER_DAYS)
    }
}

/** Bar heights (0..1) for the weekly chart. */
object ChartScaling {

    const val MIN_VISIBLE_FRACTION = 0.08f

    fun fractions(values: List<BigDecimal>): List<Float> {
        val max = values.maxOrNull() ?: return emptyList()
        if (max.signum() <= 0) {
            return values.map { 0f }
        }
        return values.map { value ->
            if (value.signum() <= 0) 0f else value.divide(max, 4, java.math.RoundingMode.HALF_UP).toFloat().coerceAtLeast(MIN_VISIBLE_FRACTION)
        }
    }
}
