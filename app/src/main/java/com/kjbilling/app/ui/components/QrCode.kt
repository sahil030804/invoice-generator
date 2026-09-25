package com.kjbilling.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.kjbilling.app.domain.upi.QrEncoder
import com.kjbilling.app.domain.upi.UpiQrRequest
import com.kjbilling.app.ui.theme.Dimens
import kotlin.math.floor

private val QR_MAX_WIDTH = 280.dp

/**
 * Scannable QR code. Always black on white (scanners need that contrast, even in dark mode),
 * drawn with whole-pixel modules so edges stay sharp.
 */
@Composable
fun QrCode(
    content: String,
    modifier: Modifier = Modifier,
    contentDescription: String = "UPI QR code"
) {
    val matrix = remember(content) { QrEncoder.encode(content) }
    val runs = remember(matrix) { matrix.darkRuns() }

    Box(
        modifier = modifier
            .widthIn(max = QR_MAX_WIDTH)
            .aspectRatio(1f)
            .clip(MaterialTheme.shapes.small)
            .background(Color.White)
            .semantics { this.contentDescription = contentDescription }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val modules = matrix.size + 2 * QrEncoder.QUIET_ZONE_MODULES
            val module = floor(size.minDimension / modules).coerceAtLeast(1f)
            val codeSize = module * matrix.size
            val originX = floor((size.width - codeSize) / 2f)
            val originY = floor((size.height - codeSize) / 2f)
            runs.forEach { run ->
                drawRect(
                    color = Color.Black,
                    topLeft = Offset(originX + run.start * module, originY + run.row * module),
                    size = Size((run.endExclusive - run.start) * module, module)
                )
            }
        }
    }
}

/** QR plus the amount and UPI ID, so customers can also pay by typing the ID. */
@Composable
fun UpiQrCard(request: UpiQrRequest, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Dimens.Sm)
    ) {
        QrCode(content = request.uri, modifier = Modifier.fillMaxWidth(0.8f))
        Text(
            text = "Scan to pay ${request.amountLabel}",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "UPI: ${request.vpa}",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Text(
            text = "GPay · PhonePe · Paytm · any UPI app",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = Dimens.Xs)
        )
    }
}
