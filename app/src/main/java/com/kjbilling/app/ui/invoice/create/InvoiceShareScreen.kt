package com.kjbilling.app.ui.invoice.create

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.pdf.InvoiceShareHelper
import com.kjbilling.app.pdf.PdfDownloadHelper
import com.kjbilling.app.ui.components.ErrorState
import com.kjbilling.app.ui.components.PrimaryButton
import com.kjbilling.app.ui.components.SecondaryButton
import java.io.File
import java.math.BigDecimal
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceShareScreen(
    invoiceId: Long,
    filePath: String,
    onNavigateHome: () -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as KJInvoiceApp
    val file = remember(filePath) { File(filePath) }
    var invoice by remember { mutableStateOf<Invoice?>(null) }
    var loadFailed by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        try {
            invoice = app.container.invoiceRepository.getById(invoiceId)
            loadFailed = invoice == null
        } catch (_: Exception) {
            loadFailed = true
        }
    }

    val fileExists = remember(filePath) { file.exists() && file.isFile }
    val invoiceNumber = invoice?.invoiceNumber ?: "Invoice"
    val customerName = invoice?.customerName ?: "Customer"
    val grandTotal = CurrencyFormatter.format(invoice?.grandTotal ?: BigDecimal.ZERO)

    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var isDownloading by remember { mutableStateOf(false) }

    suspend fun downloadPdf() {
        isDownloading = true
        val result = PdfDownloadHelper.downloadPdf(context, file)
        isDownloading = false
        val message = when (result) {
            is PdfDownloadHelper.DownloadResult.Success -> "Invoice PDF saved to Downloads"
            is PdfDownloadHelper.DownloadResult.Failure -> "Download failed: ${result.reason}"
        }
        snackbarHostState.showSnackbar(message)
    }

    val legacyPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            scope.launch { downloadPdf() }
        } else {
            scope.launch { snackbarHostState.showSnackbar("Storage permission denied") }
        }
    }

    fun startDownload() {
        if (fileExists && !isDownloading) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                legacyPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            } else {
                scope.launch { downloadPdf() }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Invoice Ready") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Success",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(100.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text(
                text = "Invoice Generated Successfully!",
                style = MaterialTheme.typography.titleLarge
            )

            if (invoice != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$invoiceNumber • $customerName • $grandTotal",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (loadFailed) {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorState(message = "Could not load invoice details, but the PDF was saved.")
            }

            if (!fileExists) {
                Spacer(modifier = Modifier.height(16.dp))
                ErrorState(message = "PDF file not found. Regenerate it from invoice details.")
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            PrimaryButton(
                text = "Share via WhatsApp",
                onClick = {
                    InvoiceShareHelper.shareFile(
                        context = context,
                        file = file,
                        invoiceNumber = invoiceNumber,
                        customerName = customerName,
                        grandTotal = grandTotal,
                        targetPackage = "com.whatsapp"
                    )
                },
                enabled = fileExists,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            SecondaryButton(
                text = if (isDownloading) "Downloading…" else "Download PDF",
                onClick = { startDownload() },
                enabled = fileExists && !isDownloading,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(
                onClick = {
                    InvoiceShareHelper.shareFile(
                        context = context,
                        file = file,
                        invoiceNumber = invoiceNumber,
                        customerName = customerName,
                        grandTotal = grandTotal
                    )
                },
                enabled = fileExists,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Share Options")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            TextButton(onClick = onNavigateHome) {
                Text("Done")
            }
        }
    }
}
