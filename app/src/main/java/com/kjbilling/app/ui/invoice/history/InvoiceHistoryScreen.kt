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
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.insights.DateGrouping
import com.kjbilling.app.domain.insights.HistoryFilter
import com.kjbilling.app.ui.components.AppCard
import com.kjbilling.app.ui.components.InitialsAvatar
import com.kjbilling.app.ui.dashboard.DayHeader
import com.kjbilling.app.ui.components.InvoiceStatusBadge
import com.kjbilling.app.ui.theme.Dimens
import com.kjbilling.app.ui.components.EmptyState
import com.kjbilling.app.ui.components.SearchBar
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceHistoryScreen(
    onNavigateToDetail: (Long) -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: InvoiceHistoryViewModel = viewModel(factory = InvoiceHistoryViewModel.factory(app.container))

    val invoices by viewModel.invoices.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filter by viewModel.filter.collectAsState()

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
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp)
            )

            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = Dimens.Sm),
                horizontalArrangement = Arrangement.spacedBy(Dimens.Sm)
            ) {
                FILTER_CHIPS.forEach { (option, label) ->
                    FilterChip(
                        selected = filter == option,
                        onClick = { viewModel.onFilterChange(option) },
                        label = { Text(label) }
                    )
                }
            }

            if (invoices.isEmpty()) {
                EmptyState(
                    title = if (searchQuery.isNotBlank() || filter != HistoryFilter.ALL) "No invoices found" else "No invoices yet",
                    modifier = Modifier.weight(1f)
                )
            } else {
                val groups = remember(invoices) { DateGrouping.groupByDay(invoices, System.currentTimeMillis(), ZoneId.systemDefault()) }
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding, vertical = Dimens.Sm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.ListGap)
                ) {
                    groups.forEachIndexed { index, group ->
                        item(key = "day-$index") {
                            DayHeader(group.label)
                        }
                        items(group.invoices, key = { it.id }) { invoice ->
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
}

private val FILTER_CHIPS = listOf(
    HistoryFilter.ALL to "All",
    HistoryFilter.UNPAID to "Unpaid",
    HistoryFilter.TODAY to "Today"
)

/** Compact row: avatar, number + customer, amount + status. The date lives in the day header above. */
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
            horizontalArrangement = Arrangement.spacedBy(Dimens.Md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InitialsAvatar(invoice.customerName)
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
            }
            Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(Dimens.Sm)) {
                Text(
                    text = CurrencyFormatter.format(invoice.grandTotal),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                InvoiceStatusBadge(status = invoice.status, paymentStatus = invoice.paymentStatus)
            }
        }
    }
}
