package com.kjbilling.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

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
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Grand Total",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    grandTotal,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun TotalRow(label: String, amount: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium)
        Text(amount, style = MaterialTheme.typography.bodyMedium)
    }
}
