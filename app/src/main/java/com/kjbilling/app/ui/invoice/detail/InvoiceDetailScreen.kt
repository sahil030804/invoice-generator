package com.kjbilling.app.ui.invoice.detail

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.pdf.InvoiceShareHelper
import com.kjbilling.app.pdf.PdfDownloadHelper
import com.kjbilling.app.ui.components.ActionButton
import com.kjbilling.app.ui.components.ConfirmationDialog
import com.kjbilling.app.ui.components.UpiQrCard
import com.kjbilling.app.ui.components.PaymentDialog
import com.kjbilling.app.ui.components.PrimaryButton
import com.kjbilling.app.ui.components.SecondaryButton
import com.kjbilling.app.ui.components.TotalCard
import com.kjbilling.app.ui.components.AppCard
import java.util.Locale
import java.util.Date
import java.text.SimpleDateFormat
import com.kjbilling.app.ui.theme.Dimens
import com.kjbilling.app.ui.components.InitialsAvatar
import com.kjbilling.app.ui.components.InvoiceStatusBadge
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceDetailScreen(
    invoiceId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as KJInvoiceApp
    val viewModel: InvoiceDetailViewModel = viewModel(factory = InvoiceDetailViewModel.factory(app.container))

    LaunchedEffect(invoiceId) {
        viewModel.loadInvoice(invoiceId)
    }

    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        viewModel.loadInvoice(invoiceId)
    }

    val currentInvoice by viewModel.invoice.collectAsState()
    val generatedFile by viewModel.generatedFile.collectAsState()
    val isDownloading by viewModel.isDownloading.collectAsState()
    val upiQr by viewModel.upiQr.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var showCancelDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showUpiQr by remember { mutableStateOf(false) }

    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.downloadPdf()
        } else {
            scope.launch { snackbarHostState.showSnackbar("Storage permission denied") }
        }
    }

    fun onDownloadClick() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            legacyPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        } else {
            viewModel.downloadPdf()
        }
    }

    LaunchedEffect(Unit) {
        viewModel.downloadEvent.collect { file ->
            val result = PdfDownloadHelper.downloadPdf(context, file)
            val message = when (result) {
                is PdfDownloadHelper.DownloadResult.Success -> "Invoice saved to Downloads (${file.name})"
                is PdfDownloadHelper.DownloadResult.Failure -> "Download failed: ${result.reason}"
            }
            snackbarHostState.showSnackbar(message)
        }
    }

    LaunchedEffect(generatedFile) {
        generatedFile?.let { file ->
            val invoiceData = currentInvoice
            InvoiceShareHelper.shareFile(
                context = context,
                file = file,
                invoiceNumber = invoiceData?.invoiceNumber ?: "",
                customerName = invoiceData?.customerName ?: "",
                grandTotal = CurrencyFormatter.format(invoiceData?.grandTotal ?: java.math.BigDecimal.ZERO)
            )
            viewModel.clearGeneratedFile()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentInvoice?.invoiceNumber ?: "Invoice Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onNavigateToEdit(invoiceId) },
                        enabled = currentInvoice != null && currentInvoice?.status != InvoiceStatus.CANCELLED
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                    IconButton(
                        onClick = { onDownloadClick() },
                        enabled = currentInvoice != null && !isDownloading
                    ) {
                        if (isDownloading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            Icon(Icons.Default.FileDownload, contentDescription = "Download")
                        }
                    }
                    IconButton(
                        onClick = { viewModel.generatePdfForSharing() },
                        enabled = currentInvoice != null
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        if (currentInvoice == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            val invoice = currentInvoice!!
            val items = invoice.items

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Summary: who, which bill, and where it stands. One card instead of two + a loose badge.
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(Dimens.Md)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top,
                            horizontalArrangement = Arrangement.spacedBy(Dimens.Md)
                        ) {
                            InitialsAvatar(invoice.customerName, size = 52.dp)
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Customer", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(invoice.customerName, style = MaterialTheme.typography.titleMedium)
                                if (!invoice.customerGstin.isNullOrBlank()) {
                                    Text("GSTIN: ${invoice.customerGstin}", style = MaterialTheme.typography.bodyMedium)
                                }
                                if (!invoice.customerAddress.isNullOrBlank()) {
                                    Text(invoice.customerAddress, style = MaterialTheme.typography.bodyMedium)
                                }
                                if (!invoice.customerState.isNullOrBlank()) {
                                    Text("State: ${invoice.customerState}", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                            InvoiceStatusBadge(status = invoice.status, paymentStatus = invoice.paymentStatus)
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("Invoice ${invoice.invoiceNumber}", style = MaterialTheme.typography.titleSmall)
                            Text(
                                SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(Date(invoice.invoiceDate)),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            invoice.paymentMethod?.let {
                                Text("Method: ${it.name}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text("Paid: ${CurrencyFormatter.format(invoice.amountPaid)}", style = MaterialTheme.typography.bodyMedium)
                            Text("Balance: ${CurrencyFormatter.format(invoice.balanceDue)}", style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // Items list
                Text("Items", style = MaterialTheme.typography.titleMedium)
                AppCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items.forEach { item ->
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(item.itemName, style = MaterialTheme.typography.bodyLarge)
                                    Text("${item.quantity.stripTrailingZeros().toPlainString()} ${item.unit ?: ""} x ${CurrencyFormatter.format(item.unitPrice)}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Text(CurrencyFormatter.format(item.total), style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }

                TotalCard(
                    subtotal = CurrencyFormatter.format(invoice.subtotal),
                    discount = CurrencyFormatter.format(invoice.totalDiscount),
                    taxLabel = "Tax (${invoice.taxType.name})",
                    taxAmount = CurrencyFormatter.format(invoice.totalTax),
                    grandTotal = CurrencyFormatter.format(invoice.grandTotal),
                    amountPaid = CurrencyFormatter.format(invoice.amountPaid),
                    balanceDue = CurrencyFormatter.format(invoice.balanceDue),
                    modifier = Modifier.fillMaxWidth()
                )

                if (!invoice.notes.isNullOrBlank()) {
                    AppCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text("Notes", style = MaterialTheme.typography.labelMedium)
                            Text(invoice.notes, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (invoice.status != InvoiceStatus.CANCELLED) {
                    if (upiQr != null) {
                        ActionButton(
                            text = "Show UPI QR",
                            onClick = { showUpiQr = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (invoice.paymentStatus != PaymentStatus.PAID) {
                        PrimaryButton(
                            text = "Record Payment",
                            onClick = { showPaymentDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    if (invoice.status != InvoiceStatus.PAID) {
                        SecondaryButton(
                            text = "Cancel Invoice",
                            onClick = { showCancelDialog = true },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    upiQr?.let { request ->
        if (showUpiQr) {
            AlertDialog(
                onDismissRequest = { showUpiQr = false },
                text = { UpiQrCard(request) },
                confirmButton = { TextButton(onClick = { showUpiQr = false }) { Text("Close") } }
            )
        }
    }

    if (showCancelDialog) {
        ConfirmationDialog(
            title = "Cancel Invoice",
            message = "Are you sure you want to cancel this invoice? This action cannot be undone.",
            confirmText = "Cancel Invoice",
            onConfirm = {
                viewModel.cancelInvoice()
                showCancelDialog = false
            },
            onDismiss = { showCancelDialog = false }
        )
    }

    if (showPaymentDialog && currentInvoice != null) {
        PaymentDialog(
            grandTotal = currentInvoice!!.grandTotal,
            amountPaid = currentInvoice!!.amountPaid,
            currentMethod = currentInvoice!!.paymentMethod,
            onConfirm = { amount, method ->
                viewModel.recordPayment(amount, method)
                showPaymentDialog = false
            },
            onDismiss = { showPaymentDialog = false }
        )
    }
}
