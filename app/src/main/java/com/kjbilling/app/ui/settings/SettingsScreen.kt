package com.kjbilling.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.model.TaxType
import com.kjbilling.app.domain.model.ThemeMode
import com.kjbilling.app.ui.components.AppTextField
import com.kjbilling.app.ui.components.LoadingState
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Process
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import java.io.File
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

val GST_RATES = listOf(BigDecimal.ZERO, BigDecimal("5.00"), BigDecimal("12.00"), BigDecimal("18.00"), BigDecimal("28.00"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToBusinessProfile: () -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.factory(app.container))
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
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
            ) {
                // Business Section
                item {
                    SectionHeader("Business")
                    ListItem(
                        headlineContent = { Text("Business Profile") },
                        supportingContent = { Text(state.profile?.businessName ?: "Not set") },
                        trailingContent = { Icon(Icons.Filled.ChevronRight, contentDescription = null) },
                        modifier = Modifier.clickable { onNavigateToBusinessProfile() }
                    )
                    HorizontalDivider()
                }

                // Invoice Section
                item {
                    SectionHeader("Invoice")
                    
                    var invoicePrefix by remember(state.settings?.invoicePrefix) { mutableStateOf(state.settings?.invoicePrefix ?: "") }
                    val isPrefixValid = invoicePrefix.isNotBlank()
                    AppTextField(
                        value = invoicePrefix,
                        onValueChange = { 
                            invoicePrefix = it
                            if (it.isNotBlank()) {
                                viewModel.updateInvoicePrefix(it)
                            }
                        },
                        label = "Invoice Prefix",
                        isError = !isPrefixValid,
                        errorMessage = if (!isPrefixValid) "Prefix cannot be empty" else null,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )

                    var startingNumber by remember(state.settings?.nextInvoiceNumber) { mutableStateOf(state.settings?.nextInvoiceNumber?.toString() ?: "") }
                    val parsedNumber = startingNumber.toLongOrNull()
                    val isNumberValid = parsedNumber != null && parsedNumber >= 1L
                    AppTextField(
                        value = startingNumber,
                        onValueChange = { 
                            startingNumber = it
                            val num = it.toLongOrNull()
                            if (num != null && num >= 1L) {
                                viewModel.updateStartingNumber(num)
                            }
                        },
                        label = "Starting Number",
                        isError = !isNumberValid,
                        errorMessage = if (!isNumberValid) "Enter a number ≥ 1. Lowering may duplicate existing invoices." else null,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    HorizontalDivider()
                }

                // GST Section
                item {
                    SectionHeader("GST Settings")
                    
                    ListItem(
                        headlineContent = { Text("GST Enabled") },
                        trailingContent = {
                            Switch(
                                checked = state.settings?.gstEnabled == true,
                                onCheckedChange = { viewModel.updateGstEnabled(it) }
                            )
                        }
                    )

                    if (state.settings?.gstEnabled == true) {
                        var rateExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = rateExpanded,
                            onExpandedChange = { rateExpanded = !rateExpanded },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            AppTextField(
                                value = "${state.settings?.defaultGstRate?.stripTrailingZeros()?.toPlainString() ?: "0"}%",
                                onValueChange = {},
                                readOnly = true,
                                label = "Default GST Rate",
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = rateExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = rateExpanded,
                                onDismissRequest = { rateExpanded = false }
                            ) {
                                GST_RATES.forEach { rate ->
                                    DropdownMenuItem(
                                        text = { Text("${rate.stripTrailingZeros().toPlainString()}%") },
                                        onClick = {
                                            viewModel.updateDefaultGstRate(rate)
                                            rateExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        var typeExpanded by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = typeExpanded,
                            onExpandedChange = { typeExpanded = !typeExpanded },
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            AppTextField(
                                value = state.settings?.defaultTaxType?.name ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = "Default Tax Type",
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = typeExpanded,
                                onDismissRequest = { typeExpanded = false }
                            ) {
                                TaxType.values().forEach { type ->
                                    DropdownMenuItem(
                                        text = { Text(type.name) },
                                        onClick = {
                                            viewModel.updateDefaultTaxType(type)
                                            typeExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    HorizontalDivider()
                }

                // Appearance Section (kept below Invoice so existing rows don't move)
                item {
                    AppearanceSection(viewModel)
                }

                // Data & Backup Section
                item {
                    BackupSection(viewModel)
                }

                // About Section
                item {
                    SectionHeader("About")
                    ListItem(
                        headlineContent = { Text("App Version") },
                        supportingContent = { Text("1.0.0") }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 24.dp, bottom = 8.dp)
    )
}

private val BACKUP_DATE_FORMAT = SimpleDateFormat("dd MMM yyyy, h:mm a", Locale.getDefault())

/**
 * Local backup/restore. The backup is one .zip the user keeps anywhere (Files, Drive, WhatsApp, USB).
 */
@Composable
private fun BackupSection(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val lastBackupAt by viewModel.lastBackupAt.collectAsState()
    val isBusy by viewModel.isBackupBusy.collectAsState()
    val message by viewModel.backupMessage.collectAsState()
    var pendingRestoreUri by remember { mutableStateOf<Uri?>(null) }

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) {
            pendingRestoreUri = uri
        }
    }

    SectionHeader("Data & Backup")
    ListItem(
        headlineContent = { Text("Back up now") },
        supportingContent = {
            Text(
                lastBackupAt?.let { "Last backup: ${BACKUP_DATE_FORMAT.format(Date(it))}" }
                    ?: "Never backed up. Back up so a lost phone doesn't lose your invoices."
            )
        },
        trailingContent = if (isBusy) {
            { CircularProgressIndicator(modifier = Modifier.size(24.dp)) }
        } else {
            null
        },
        modifier = Modifier.clickable(enabled = !isBusy) {
            viewModel.backupNow { file -> shareBackup(context, file) }
        }
    )
    ListItem(
        headlineContent = { Text("Restore from backup") },
        supportingContent = { Text("Replace all data with a backup file") },
        modifier = Modifier.clickable(enabled = !isBusy) {
            picker.launch(arrayOf("application/zip", "application/octet-stream"))
        }
    )
    HorizontalDivider()

    pendingRestoreUri?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingRestoreUri = null },
            title = { Text("Restore this backup?") },
            text = {
                Text("All current invoices, customers and products will be replaced by the backup. Your current data is kept aside on the phone so nothing is lost by mistake.")
            },
            confirmButton = {
                TextButton(onClick = {
                    pendingRestoreUri = null
                    viewModel.restoreFrom(uri) { restartApp(context) }
                }) { Text("Restore") }
            },
            dismissButton = { TextButton(onClick = { pendingRestoreUri = null }) { Text("Cancel") } }
        )
    }

    message?.let {
        AlertDialog(
            onDismissRequest = { viewModel.clearBackupMessage() },
            text = { Text(it) },
            confirmButton = { TextButton(onClick = { viewModel.clearBackupMessage() }) { Text("OK") } }
        )
    }
}

private fun shareBackup(context: Context, file: File) {
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val send = Intent(Intent.ACTION_SEND).apply {
        type = "application/zip"
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_SUBJECT, file.name)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(send, "Save backup to...").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
}

/** The database was swapped underneath the running app, so start a fresh process. */
private fun restartApp(context: Context) {
    val launch = context.packageManager.getLaunchIntentForPackage(context.packageName) ?: return
    launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
    context.startActivity(launch)
    Process.killProcess(Process.myPid())
}

private val THEME_OPTIONS = listOf(
    ThemeMode.SYSTEM to "System default",
    ThemeMode.LIGHT to "Light",
    ThemeMode.DARK to "Dark"
)

/** System / Light / Dark choice. Applied instantly and remembered across restarts. */
@Composable
private fun AppearanceSection(viewModel: SettingsViewModel) {
    val selected by viewModel.themeMode.collectAsState()

    SectionHeader("Appearance")
    // Surface background so these rows match the ListItem rows of the other sections.
    Column(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .selectableGroup()
    ) {
        THEME_OPTIONS.forEach { (mode, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 56.dp)
                    .selectable(
                        selected = mode == selected,
                        onClick = { viewModel.setThemeMode(mode) },
                        role = Role.RadioButton
                    )
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(selected = mode == selected, onClick = null)
                Spacer(modifier = Modifier.width(16.dp))
                Text(label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
    HorizontalDivider()
}
