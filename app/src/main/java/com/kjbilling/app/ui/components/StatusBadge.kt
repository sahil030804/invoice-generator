package com.kjbilling.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.ui.theme.Dimens
import com.kjbilling.app.ui.theme.StatusColors
import com.kjbilling.app.ui.theme.ext

enum class StatusTone { PAID, PARTIAL, UNPAID, CANCELLED, DRAFT }

/** Tone for an invoice: cancelled and draft win over payment state. */
fun statusToneFor(status: InvoiceStatus, paymentStatus: PaymentStatus): StatusTone {
    return when {
        status == InvoiceStatus.CANCELLED -> StatusTone.CANCELLED
        status == InvoiceStatus.DRAFT -> StatusTone.DRAFT
        paymentStatus == PaymentStatus.PAID -> StatusTone.PAID
        paymentStatus == PaymentStatus.PARTIAL -> StatusTone.PARTIAL
        else -> StatusTone.UNPAID
    }
}

/**
 * Money-status pill: icon + word on a tinted background, so status never relies on colour alone.
 * The icon has no content description; [label] is the only text (tests match it exactly).
 */
@Composable
fun StatusBadge(label: String, tone: StatusTone, modifier: Modifier = Modifier) {
    val (colors, icon) = toneStyle(tone)

    Surface(
        color = colors.container,
        contentColor = colors.onContainer,
        shape = MaterialTheme.shapes.small,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .heightIn(min = Dimens.BadgeHeight)
                .padding(horizontal = Dimens.Sm, vertical = Dimens.Xs),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Xs)
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(text = label, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun toneStyle(tone: StatusTone): Pair<StatusColors, ImageVector> {
    val ext = MaterialTheme.ext
    return when (tone) {
        StatusTone.PAID -> ext.paid to Icons.Outlined.CheckCircle
        StatusTone.PARTIAL -> ext.partial to Icons.Outlined.Schedule
        StatusTone.UNPAID -> ext.unpaid to Icons.Outlined.ErrorOutline
        StatusTone.CANCELLED -> ext.unpaid to Icons.Outlined.Block
        StatusTone.DRAFT -> ext.draft to Icons.Outlined.Edit
    }
}

/** Upper-case status badge used on History and Invoice detail ("PAID", "UNPAID", "CANCELLED", ...). */
@Composable
fun InvoiceStatusBadge(status: InvoiceStatus, paymentStatus: PaymentStatus, modifier: Modifier = Modifier) {
    val label = when (status) {
        InvoiceStatus.CANCELLED -> "CANCELLED"
        InvoiceStatus.DRAFT -> "DRAFT"
        else -> paymentStatus.name
    }
    StatusBadge(label = label, tone = statusToneFor(status, paymentStatus), modifier = modifier)
}
