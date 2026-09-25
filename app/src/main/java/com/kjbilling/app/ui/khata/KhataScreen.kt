package com.kjbilling.app.ui.khata

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.khata.CustomerDue
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.ui.components.AppCard
import com.kjbilling.app.ui.components.EmptyState
import com.kjbilling.app.ui.components.InitialsAvatar
import com.kjbilling.app.ui.components.LoadingState
import com.kjbilling.app.ui.theme.Dimens
import com.kjbilling.app.ui.theme.ext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private fun formatDate(millis: Long): String = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(millis))

/** Khata overview: total to collect and who owes it, highest first. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KhataScreen(
    onNavigateBack: () -> Unit,
    onOpenCustomer: (Long) -> Unit,
    onOpenInvoice: (Long) -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: KhataViewModel = viewModel(factory = KhataViewModel.factory(app.container))
    val summary by viewModel.summary.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Khata") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        val data = summary
        when {
            data == null -> LoadingState(modifier = Modifier.padding(padding))
            data.customers.isEmpty() && data.unlinked.isEmpty() -> EmptyState(
                icon = Icons.Outlined.TaskAlt,
                title = "No pending dues",
                subtitle = "Every bill is paid. Udhaar bills from Quick Bill will show up here.",
                modifier = Modifier.padding(padding)
            )
            else -> LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(Dimens.ScreenPadding),
                verticalArrangement = Arrangement.spacedBy(Dimens.ListGap)
            ) {
                item(key = "total") {
                    TotalToCollectCard(
                        total = CurrencyFormatter.format(data.total),
                        people = data.customers.size
                    )
                }
                items(data.customers, key = { "c${it.customerId}" }) { due ->
                    CustomerDueRow(due = due, onClick = { onOpenCustomer(due.customerId) })
                }
                if (data.unlinked.isNotEmpty()) {
                    item(key = "unlinked-header") {
                        Text(
                            text = "Bills without a customer",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = Dimens.Md)
                        )
                    }
                    items(data.unlinked, key = { "i${it.id}" }) { invoice ->
                        UnlinkedBillRow(invoice = invoice, onClick = { onOpenInvoice(invoice.id) })
                    }
                }
            }
        }
    }
}

@Composable
private fun TotalToCollectCard(total: String, people: Int) {
    AppCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = MaterialTheme.ext.hero
    ) {
        Column(modifier = Modifier.padding(Dimens.Xl)) {
            Text("Total to collect", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.ext.onHero)
            Text(total, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.ext.onHero)
            Text(
                text = if (people == 1) "1 customer owes you" else "$people customers owe you",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.ext.onHero.copy(alpha = 0.9f)
            )
        }
    }
}

@Composable
private fun CustomerDueRow(due: CustomerDue, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Dimens.Md)
        ) {
            InitialsAvatar(due.name)
            Column(modifier = Modifier.weight(1f)) {
                Text(due.name, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(
                    text = "${due.openBills} ${if (due.openBills == 1) "bill" else "bills"} · last ${formatDate(due.lastBillDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = CurrencyFormatter.format(due.due),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.ext.unpaid.text
            )
        }
    }
}

@Composable
private fun UnlinkedBillRow(invoice: Invoice, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.padding(Dimens.CardPadding),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(invoice.invoiceNumber, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = "${invoice.customerName} · ${formatDate(invoice.invoiceDate)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = CurrencyFormatter.format(invoice.balanceDue),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.ext.unpaid.text
            )
        }
    }
}
