package com.kjbilling.app.ui.customer

import androidx.compose.foundation.background
import androidx.compose.ui.draw.clip
import com.kjbilling.app.ui.components.AppCard
import com.kjbilling.app.ui.theme.Dimens
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.ui.components.ConfirmationDialog
import com.kjbilling.app.ui.components.EmptyState
import com.kjbilling.app.ui.components.SearchBar
import com.kjbilling.app.ui.components.InitialsAvatar
import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.ui.theme.ext
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToAdd: () -> Unit,
    onOpenKhata: (Long) -> Unit = {}
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: CustomerViewModel = viewModel(factory = CustomerViewModel.factory(app.container))

    val customers by viewModel.customers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filter by viewModel.filter.collectAsState()
    val dues by viewModel.dues.collectAsState()

    var customerToDelete by remember { mutableStateOf<Customer?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Customers") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAdd) {
                Icon(Icons.Default.Add, contentDescription = "Add Customer")
            }
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
                FilterChip(
                    selected = filter == CustomerFilter.ALL,
                    onClick = { viewModel.setFilter(CustomerFilter.ALL) },
                    label = { Text("All") }
                )
                FilterChip(
                    selected = filter == CustomerFilter.WITH_DUES,
                    onClick = { viewModel.setFilter(CustomerFilter.WITH_DUES) },
                    label = { Text(if (dues.isEmpty()) "With dues" else "With dues (${dues.size})") }
                )
            }

            if (customers.isEmpty()) {
                EmptyState(
                    title = when {
                        searchQuery.isNotBlank() -> "No customers found"
                        filter == CustomerFilter.WITH_DUES -> "No pending dues"
                        else -> "No customers yet"
                    },
                    subtitle = if (searchQuery.isNotBlank()) null else "Add your first customer to get started",
                    modifier = Modifier.weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(horizontal = Dimens.ScreenPadding, vertical = Dimens.Sm),
                    verticalArrangement = Arrangement.spacedBy(Dimens.ListGap)
                ) {
                    items(customers, key = { it.id }) { customer ->
                        SwipeToDeleteCustomerCard(
                            customer = customer,
                            due = dues[customer.id],
                            onClick = { onNavigateToEdit(customer.id) },
                            onOpenKhata = { onOpenKhata(customer.id) },
                            onDelete = { customerToDelete = customer }
                        )
                    }
                }
            }
        }
    }

    customerToDelete?.let { customer ->
        ConfirmationDialog(
            title = "Delete Customer",
            message = "Are you sure you want to delete ${customer.name}? This action cannot be undone.",
            onConfirm = {
                viewModel.deleteCustomer(customer)
                customerToDelete = null
            },
            onDismiss = { customerToDelete = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeToDeleteCustomerCard(
    customer: Customer,
    due: BigDecimal?,
    onClick: () -> Unit,
    onOpenKhata: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { dismissValue ->
            if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false // Don't dismiss immediately, let dialog handle it
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        enableDismissFromStartToEnd = false,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = Dimens.Xl)
                )
            }
        },
        content = {
            AppCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onClick)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Dimens.CardPadding),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Dimens.Md)
                ) {
                    InitialsAvatar(customer.name)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = customer.name,
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (!customer.businessName.isNullOrBlank()) {
                            Text(
                                text = customer.businessName,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        val details = listOfNotNull(
                            customer.mobile?.takeIf { it.isNotBlank() },
                            customer.state?.takeIf { it.isNotBlank() }
                        ).joinToString(" · ")
                        if (details.isNotEmpty()) {
                            Text(
                                text = details,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    if (due != null && due.signum() > 0) {
                        DueBadge(amount = due, onClick = onOpenKhata)
                    }
                }
            }
        }
    )
}

/** "₹1,250.00 due" — opens the customer's Khata (the row itself still opens Edit Customer). */
@Composable
private fun DueBadge(amount: BigDecimal, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.ext.unpaid.container,
        contentColor = MaterialTheme.ext.unpaid.onContainer
    ) {
        Text(
            text = "${CurrencyFormatter.format(amount)} due",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier
                .heightIn(min = Dimens.MinTouch)
                .padding(horizontal = Dimens.Md, vertical = Dimens.Md)
        )
    }
}
