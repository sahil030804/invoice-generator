package com.kjbilling.app.ui.dashboard

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kjbilling.app.domain.insights.ChartScaling
import com.kjbilling.app.domain.insights.DaySales
import com.kjbilling.app.domain.insights.InsightsResult
import com.kjbilling.app.ui.components.AppCard
import com.kjbilling.app.ui.theme.Dimens
import com.kjbilling.app.ui.theme.ext

/** One 2x2-grid shortcut: icon, title and a short status line. The whole tile is a single tap target. */
@Composable
fun ActionTile(
    icon: ImageVector,
    title: String,
    subtitle: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconDescription: String? = null,
    highlightSubtitle: Boolean = false
) {
    AppCard(onClick = onClick, modifier = modifier.heightIn(min = TILE_MIN_HEIGHT)) {
        Column(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.Xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconDescription,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Text(text = title, style = MaterialTheme.typography.titleSmall)
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (highlightSubtitle) MaterialTheme.ext.unpaid.text else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val TILE_MIN_HEIGHT = 112.dp

/**
 * "This week" card: bar chart with weekday letters (drawn in the chart, so no ₹ text appears),
 * plus bills today and the top seller.
 */
@Composable
fun WeekCard(insights: InsightsResult, modifier: Modifier = Modifier) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.CardPadding), verticalArrangement = Arrangement.spacedBy(Dimens.Md)) {
            Text("This week", style = MaterialTheme.typography.titleMedium)
            SalesBars(days = insights.days, modifier = Modifier.fillMaxWidth().height(CHART_HEIGHT))
            Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Lg)) {
                Text(
                    text = "${insights.billsToday} ${if (insights.billsToday == 1) "bill" else "bills"} today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            insights.topSeller?.let { top ->
                Text(
                    text = "Top seller: ${top.name} · ${top.quantity.stripTrailingZeros().toPlainString()} sold",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private val CHART_HEIGHT = 110.dp
private const val BAR_WIDTH_FRACTION = 0.56f
private const val LABEL_SIZE_SP = 12

@Composable
private fun SalesBars(days: List<DaySales>, modifier: Modifier = Modifier) {
    val fractions = remember(days) { ChartScaling.fractions(days.map { it.total }) }
    val barColor = MaterialTheme.colorScheme.primary
    val todayColor = MaterialTheme.ext.action
    val trackColor = MaterialTheme.colorScheme.surfaceVariant
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val measurer: TextMeasurer = rememberTextMeasurer()
    val labelStyle = TextStyle(fontSize = LABEL_SIZE_SP.sp, color = labelColor)

    Canvas(modifier = modifier) {
        val slot = size.width / days.size
        val barWidth = slot * BAR_WIDTH_FRACTION
        val labelHeight = LABEL_SIZE_SP.sp.toPx() * 1.6f
        val chartHeight = size.height - labelHeight
        val corner = CornerRadius(barWidth / 4f)

        days.forEachIndexed { index, day ->
            val x = slot * index + (slot - barWidth) / 2f
            drawRoundRect(color = trackColor, topLeft = Offset(x, 0f), size = Size(barWidth, chartHeight), cornerRadius = corner)
            val barHeight = chartHeight * fractions[index]
            if (barHeight > 0f) {
                drawRoundRect(
                    color = if (index == days.lastIndex) todayColor else barColor,
                    topLeft = Offset(x, chartHeight - barHeight),
                    size = Size(barWidth, barHeight),
                    cornerRadius = corner
                )
            }
            val label = measurer.measure(day.dayLetter, labelStyle)
            drawText(label, topLeft = Offset(slot * index + (slot - label.size.width) / 2f, chartHeight + (labelHeight - label.size.height) / 2f))
        }
    }
}

/** Date header for grouped lists ("Today", "Yesterday", "21 Sep 2026"). No amounts, so tests can count ₹ safely. */
@Composable
fun DayHeader(label: String, modifier: Modifier = Modifier) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = modifier.fillMaxWidth().padding(top = Dimens.Md, bottom = Dimens.Xs)
    )
}

