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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.ui.components.ActionButton
import com.kjbilling.app.ui.components.AppCard
import com.kjbilling.app.ui.components.EmptyState
import com.kjbilling.app.ui.components.InvoiceStatusBadge
import com.kjbilling.app.ui.components.LoadingState
import com.kjbilling.app.ui.components.MoneyInput
import com.kjbilling.app.ui.components.SecondaryButton
import com.kjbilling.app.ui.theme.Dimens
import com.kjbilling.app.ui.theme.ext
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** One customer's Khata: what they owe, unpaid bills, record money received, WhatsApp reminder. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerKhataScreen(
    customerId: Long,
    onNavigateBack: () -> Unit,
    onOpenInvoice: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as KJInvoiceApp
    val viewModel: CustomerKhataViewModel = viewModel(
        key = "khata-$customerId",
        factory = CustomerKhataViewModel.factory(app.container, customerId)
    )
    val state by viewModel.state.collectAsState()
    val message by viewModel.message.collectAsState()
    var showReceive by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.customerName.ifBlank { "Khata" }) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            LoadingState(modifier = Modifier.padding(padding))
            return@Scaffold
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(Dimens.ScreenPadding),
            verticalArrangement = Arrangement.spacedBy(Dimens.ListGap)
        ) {
            item(key = "summary") {
                DueSummaryCard(
                    totalDue = CurrencyFormatter.format(state.totalDue),
                    mobile = state.customer?.mobile,
                    hasDue = state.totalDue.signum() > 0,
                    onReceived = { showReceive = true },
                    onRemind = { WhatsAppReminder.send(context, state.customer?.mobile, state.reminderText) }
                )
            }
            if (state.openBills.isEmpty()) {
                item(key = "empty") {
                    EmptyState(icon = Icons.Outlined.TaskAlt, title = "No pending dues", subtitle = "All bills from this customer are paid.")
                }
            } else {
                item(key = "bills-header") {
                    Text("Unpaid bills", style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = Dimens.Sm))
                }
                items(state.openBills, key = { it.id }) { bill ->
                    UnpaidBillRow(bill = bill, onClick = { onOpenInvoice(bill.id) })
                }
            }
        }
    }

    if (showReceive) {
        ReceivePaymentDialog(
            due = state.totalDue,
            onConfirm = { amount, method ->
                viewModel.receive(amount, method)
                showReceive = false
            },
            onDismiss = { showReceive = false }
        )
    }

    message?.let {
        AlertDialog(
            onDismissRequest = viewModel::clearMessage,
            text = { Text(it) },
            confirmButton = { TextButton(onClick = viewModel::clearMessage) { Text("OK") } }
        )
    }
}

@Composable
private fun DueSummaryCard(
    totalDue: String,
    mobile: String?,
    hasDue: Boolean,
    onReceived: () -> Unit,
    onRemind: () -> Unit
) {
    AppCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(Dimens.Xl), verticalArrangement = Arrangement.spacedBy(Dimens.Sm)) {
            Text("Total due", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                text = totalDue,
                style = MaterialTheme.typography.headlineLarge,
                color = if (hasDue) MaterialTheme.ext.unpaid.text else MaterialTheme.ext.paid.text
            )
            if (!mobile.isNullOrBlank()) {
                Text(mobile, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (hasDue) {
                ActionButton(text = "Received", onClick = onReceived, modifier = Modifier.padding(top = Dimens.Sm))
                SecondaryButton(text = "Remind on WhatsApp", onClick = onRemind)
            }
        }
    }
}

@Composable
private fun UnpaidBillRow(bill: Invoice, onClick: () -> Unit) {
    AppCard(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(modifier = Modifier.padding(Dimens.CardPadding), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(Dimens.Xs)) {
                Text(bill.invoiceNumber, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(bill.invoiceDate)),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                InvoiceStatusBadge(status = bill.status, paymentStatus = bill.paymentStatus)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${CurrencyFormatter.format(bill.balanceDue)} due",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.ext.unpaid.text
                )
                Text(
                    text = "of ${CurrencyFormatter.format(bill.grandTotal)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Money received from the customer; applied to their oldest bills first. */
@Composable
private fun ReceivePaymentDialog(
    due: BigDecimal,
    onConfirm: (BigDecimal, PaymentMethod) -> Unit,
    onDismiss: () -> Unit
) {
    var amountText by remember { mutableStateOf(due.stripTrailingZeros().toPlainString()) }
    var method by remember { mutableStateOf(PaymentMethod.CASH) }
    val amount = amountText.toBigDecimalOrNull()
    val isValid = amount != null && amount.signum() > 0 && amount <= due

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Money received") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(Dimens.Md)) {
                MoneyInput(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = "Amount (Due ${due.stripTrailingZeros().toPlainString()})",
                    isError = !isValid && amountText.isNotBlank(),
                    errorMessage = if (!isValid && amountText.isNotBlank()) "Enter 0 < amount ≤ total due" else null
                )
                Row(horizontalArrangement = Arrangement.spacedBy(Dimens.Sm)) {
                    listOf(PaymentMethod.CASH to "Cash", PaymentMethod.UPI to "UPI").forEach { (option, label) ->
                        FilterChip(selected = method == option, onClick = { method = option }, label = { Text(label) })
                    }
                }
                Text(
                    text = "Oldest bills are cleared first.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { amount?.let { onConfirm(it, method) } }, enabled = isValid) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
