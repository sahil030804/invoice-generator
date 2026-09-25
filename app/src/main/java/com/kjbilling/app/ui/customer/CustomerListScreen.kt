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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerListScreen(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToAdd: () -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: CustomerViewModel = viewModel(factory = CustomerViewModel.factory(app.container))

    val customers by viewModel.customers.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

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
                modifier = Modifier.padding(16.dp)
            )

            if (customers.isEmpty()) {
                EmptyState(
                    title = if (searchQuery.isNotBlank()) "No customers found" else "No customers yet",
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
                            onClick = { onNavigateToEdit(customer.id) },
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
    onClick: () -> Unit,
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
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
                    if (!customer.mobile.isNullOrBlank()) {
                        Text(
                            text = customer.mobile,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!customer.state.isNullOrBlank()) {
                        Text(
                            text = customer.state,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    )
}
