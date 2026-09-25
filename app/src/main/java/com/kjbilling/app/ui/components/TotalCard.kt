package com.kjbilling.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.kjbilling.app.ui.theme.Dimens

@Composable
fun TotalCard(
    subtotal: String,
    discount: String?,
    taxLabel: String,
    taxAmount: String,
    grandTotal: String,
    modifier: Modifier = Modifier,
    amountPaid: String? = null,
    balanceDue: String? = null,
    taxBreakdown: List<Pair<String, String>> = emptyList()
) {
    AppCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.CardPadding)) {
            TotalRow("Subtotal", subtotal)
            if (discount != null && discount != "₹0.00" && discount != "₹0") {
                TotalRow("Discount", "-$discount")
            }
            TotalRow(taxLabel, taxAmount)
            taxBreakdown.forEach { (label, amount) ->
                TotalRow(label, amount)
            }
            if (amountPaid != null) {
                TotalRow("Amount Paid", amountPaid)
            }
            if (balanceDue != null) {
                TotalRow("Balance Due", balanceDue)
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = Dimens.Sm),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Grand Total",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    grandTotal,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TotalRow(label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.Xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(amount, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
    }
}
