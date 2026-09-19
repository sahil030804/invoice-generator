package com.kjbilling.app.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.model.TaxType
import com.kjbilling.app.ui.components.AppTextField
import com.kjbilling.app.ui.components.LoadingState
import java.math.BigDecimal

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
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    )
}
