package com.kjbilling.app.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.R
import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.ui.components.EmptyState
import com.kjbilling.app.ui.components.LoadingState
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToNewInvoice: () -> Unit,
    onNavigateToInvoiceDetail: (Long) -> Unit,
    onNavigate: (String) -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: DashboardViewModel = viewModel(factory = DashboardViewModel.factory(app.container))
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "PrimeInvoice Logo",
                            modifier = Modifier.size(34.dp)
                        )
                        Column {
                            Text(
                                text = "PrimeInvoice",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = getGreetingMessage(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToNewInvoice,
                icon = { Icon(Icons.Filled.Add, "New Invoice") },
                text = { Text("New Invoice") }
            )
        }
    ) { innerPadding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(innerPadding))
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        StatCard(
                            title = "Today's Sales",
                            amount = CurrencyFormatter.format(state.todaysSales),
                            modifier = Modifier.weight(1f)
                        )
                        StatCard(
                            title = "Pending",
                            amount = CurrencyFormatter.format(state.pendingAmount),
                            modifier = Modifier.weight(1f),
                            amountColor = MaterialTheme.colorScheme.error
                        )
                    }
                }

                item {
                    Text(
                        text = "Recent Invoices",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }

                if (state.recentInvoices.isEmpty()) {
                    item {
                        EmptyState(
                            title = "No invoices yet",
                            subtitle = "Create your first professional invoice in under a minute",
                            modifier = Modifier.padding(vertical = 32.dp)
                        )
                    }
                } else {
                    items(state.recentInvoices) { invoice ->
                        InvoiceCard(
                            invoice = invoice,
                            onClick = { onNavigateToInvoiceDetail(invoice.id) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    amount: String,
    modifier: Modifier = Modifier,
    amountColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = title, style = MaterialTheme.typography.bodyMedium)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = amount,
                style = MaterialTheme.typography.titleLarge,
                color = amountColor
            )
        }
    }
}

@Composable
fun InvoiceCard(
    invoice: Invoice,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = invoice.invoiceNumber,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = invoice.customerName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault())
                        .format(java.util.Date(invoice.invoiceDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = CurrencyFormatter.format(invoice.grandTotal),
                    style = MaterialTheme.typography.titleMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                StatusChip(status = invoice.status, paymentStatus = invoice.paymentStatus)
            }
        }
    }
}

@Composable
fun StatusChip(status: InvoiceStatus, paymentStatus: PaymentStatus) {
    val (text, color) = when {
        status == InvoiceStatus.CANCELLED -> "Cancelled" to Color.Red
        status == InvoiceStatus.DRAFT -> "Draft" to Color.Gray
        paymentStatus == PaymentStatus.PAID -> "Paid" to Color(0xFF4CAF50)
        else -> "Unpaid" to Color(0xFFFF9800)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = text,
            color = color,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

private fun getGreetingMessage(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 0..11 -> "Good morning 👋"
        in 12..16 -> "Good afternoon 👋"
        else -> "Good evening 👋"
    }
}
