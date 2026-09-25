package com.kjbilling.app.ui.dashboard

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import com.kjbilling.app.ui.components.AppCard
import com.kjbilling.app.ui.components.EmptyState
import com.kjbilling.app.ui.components.StatusBadge
import com.kjbilling.app.ui.components.statusToneFor
import com.kjbilling.app.ui.theme.Dimens
import com.kjbilling.app.ui.theme.ext
import com.kjbilling.app.ui.components.LoadingState
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onNavigateToNewInvoice: () -> Unit,
    onNavigateToQuickBill: () -> Unit,
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
                            painter = painterResource(id = R.drawable.parchi_logo),
                            contentDescription = "Parchi Logo",
                            modifier = Modifier.size(34.dp)
                        )
                        Column {
                            Text(
                                text = "Parchi",
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
                text = { Text("New Invoice", style = MaterialTheme.typography.labelLarge) },
                containerColor = MaterialTheme.ext.action,
                contentColor = MaterialTheme.ext.onAction
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
                    .padding(horizontal = Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.ListGap)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        onClick = onNavigateToQuickBill,
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.ext.hero,
                            contentColor = MaterialTheme.ext.onHero
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = Dimens.Lg, vertical = Dimens.Xl),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(MaterialTheme.ext.action, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Add,
                                        contentDescription = null,
                                        tint = MaterialTheme.ext.onAction,
                                        modifier = Modifier.size(30.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "⚡ QUICK COUNTER BILL",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Zero Typing • 1-Tap Sale for Seniors",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.ext.onHero.copy(alpha = 0.9f)
                                    )
                                }
                            }
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = null
                            )
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Dimens.ListGap)
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
                            amountColor = MaterialTheme.ext.unpaid.text
                        )
                    }
                }

                item {
                    Text(
                        text = "Recent Invoices",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.padding(top = Dimens.Md, bottom = Dimens.Xs)
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
    AppCard(modifier = modifier) {
        Column(
            modifier = Modifier.padding(Dimens.CardPadding)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(Dimens.Sm))
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
    AppCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.CardPadding),
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
                Spacer(modifier = Modifier.height(Dimens.Sm))
                StatusChip(status = invoice.status, paymentStatus = invoice.paymentStatus)
            }
        }
    }
}

/** Dashboard wording is title case ("Paid", "Unpaid", ...); tone/icon come from the shared badge. */
@Composable
fun StatusChip(status: InvoiceStatus, paymentStatus: PaymentStatus) {
    val label = when {
        status == InvoiceStatus.CANCELLED -> "Cancelled"
        status == InvoiceStatus.DRAFT -> "Draft"
        paymentStatus == PaymentStatus.PAID -> "Paid"
        paymentStatus == PaymentStatus.PARTIAL -> "Partial"
        else -> "Unpaid"
    }
    StatusBadge(label = label, tone = statusToneFor(status, paymentStatus))
}

private fun getGreetingMessage(): String {
    val hour = LocalTime.now().hour
    return when (hour) {
        in 0..11 -> "Good morning 👋"
        in 12..16 -> "Good afternoon 👋"
        else -> "Good evening 👋"
    }
}
