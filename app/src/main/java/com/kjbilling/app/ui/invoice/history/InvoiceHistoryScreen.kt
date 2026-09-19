package com.kjbilling.app.ui.invoice.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.ui.components.EmptyState
import com.kjbilling.app.ui.components.SearchBar
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHistoryScreen(
    onNavigateToDetail: (Long) -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: InvoiceHistoryViewModel = viewModel(factory = InvoiceHistoryViewModel.factory(app.container))

    val invoices by viewModel.invoices.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Invoice History") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = viewModel::onSearchQueryChange,
                modifier = Modifier.padding(16.dp)
            )

            if (invoices.isEmpty()) {
                EmptyState(
                    title = if (searchQuery.isNotBlank()) "No invoices found" else "No invoices yet",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(invoices, key = { it.id }) { invoice ->
                        InvoiceCard(
                            invoice = invoice,
                            onClick = { onNavigateToDetail(invoice.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InvoiceCard(
    invoice: Invoice,
    onClick: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }
    val dateString = formatter.format(Date(invoice.invoiceDate))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = CurrencyFormatter.format(invoice.grandTotal),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = invoice.customerName,
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateString,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    StatusChip(status = invoice.status.name, isSuccess = invoice.status == InvoiceStatus.GENERATED, isError = invoice.status == InvoiceStatus.CANCELLED)
                    if (invoice.status != InvoiceStatus.CANCELLED) {
                        StatusChip(status = invoice.paymentStatus.name, isSuccess = invoice.paymentStatus == PaymentStatus.PAID, isError = invoice.paymentStatus == PaymentStatus.UNPAID)
                    }
                }
            }
        }
    }
}

@Composable
fun StatusChip(status: String, isSuccess: Boolean, isError: Boolean) {
    val containerColor = when {
        isSuccess -> MaterialTheme.colorScheme.primaryContainer
        isError -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        isSuccess -> MaterialTheme.colorScheme.onPrimaryContainer
        isError -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    
    Surface(
        color = containerColor,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor
        )
    }
}
