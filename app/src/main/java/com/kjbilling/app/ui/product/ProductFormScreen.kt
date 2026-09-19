package com.kjbilling.app.ui.product

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.model.Product
import com.kjbilling.app.ui.components.AppTextField
import com.kjbilling.app.ui.components.MoneyInput
import com.kjbilling.app.ui.components.PrimaryButton
import java.math.BigDecimal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductFormScreen(
    productId: Long?,
    onNavigateBack: () -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: ProductViewModel = viewModel(factory = ProductViewModel.factory(app.container))

    val products by viewModel.products.collectAsState()
    val existingProduct = products.find { it.id == productId }

    // Key on id so async Flow load populates edit fields (was stuck empty).
    var name by remember(existingProduct?.id) { mutableStateOf(existingProduct?.name ?: "") }
    var sellingPriceStr by remember(existingProduct?.id) { mutableStateOf(existingProduct?.sellingPrice?.toPlainString() ?: "") }
    var hsnCode by remember(existingProduct?.id) { mutableStateOf(existingProduct?.hsnCode ?: "") }
    var unit by remember(existingProduct?.id) { mutableStateOf(existingProduct?.unit ?: "PCS") }
    var gstRateStr by remember(existingProduct?.id) { mutableStateOf(existingProduct?.gstRate?.toPlainString() ?: "") }
    var description by remember(existingProduct?.id) { mutableStateOf(existingProduct?.description ?: "") }
    var sku by remember(existingProduct?.id) { mutableStateOf(existingProduct?.sku ?: "") }

    val units = listOf("PCS", "NOS", "BOX", "MTR", "KG", "GM", "LTR", "SET", "PACK", "HOUR", "DAY")
    val gstRates = listOf("Use Default", "0", "5", "12", "18", "28")

    val sellingPrice = sellingPriceStr.toBigDecimalOrNull()
    val isFormValid = name.isNotBlank() && sellingPrice != null && sellingPrice > BigDecimal.ZERO

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (productId != null) "Edit Product" else "New Product") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTextField(
                value = name,
                onValueChange = { name = it },
                label = "Product Name*",
                modifier = Modifier.fillMaxWidth()
            )

            MoneyInput(
                value = sellingPriceStr,
                onValueChange = { sellingPriceStr = it },
                label = "Selling Price*",
                modifier = Modifier.fillMaxWidth()
            )

            // Using simple TextFields instead of dropdowns for simplicity,
            // as standard ExposeDropdown is complex to write without full context.
            // A simple implementation of dropdown using ExposedDropdownMenuBox:
            var unitExpanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = unitExpanded,
                onExpandedChange = { unitExpanded = !unitExpanded }
            ) {
                AppTextField(
                    value = unit,
                    onValueChange = {},
                    readOnly = true,
                    label = "Unit",
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = unitExpanded,
                    onDismissRequest = { unitExpanded = false }
                ) {
                    units.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                unit = selectionOption
                                unitExpanded = false
                            }
                        )
                    }
                }
            }

            var gstExpanded by remember { mutableStateOf(false) }
            val currentGstDisplay = if (gstRateStr.isBlank()) "Use Default" else gstRateStr
            ExposedDropdownMenuBox(
                expanded = gstExpanded,
                onExpandedChange = { gstExpanded = !gstExpanded }
            ) {
                AppTextField(
                    value = currentGstDisplay,
                    onValueChange = {},
                    readOnly = true,
                    label = "GST Rate (%)",
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gstExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = gstExpanded,
                    onDismissRequest = { gstExpanded = false }
                ) {
                    gstRates.forEach { selectionOption ->
                        DropdownMenuItem(
                            text = { Text(selectionOption) },
                            onClick = {
                                gstRateStr = if (selectionOption == "Use Default") "" else selectionOption
                                gstExpanded = false
                            }
                        )
                    }
                }
            }

            AppTextField(
                value = hsnCode,
                onValueChange = { hsnCode = it },
                label = "HSN Code",
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = sku,
                onValueChange = { sku = it },
                label = "SKU",
                modifier = Modifier.fillMaxWidth()
            )

            AppTextField(
                value = description,
                onValueChange = { description = it },
                label = "Description",
                modifier = Modifier.fillMaxWidth(),
                singleLine = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            PrimaryButton(
                text = "Save",
                onClick = {
                    if (isFormValid) {
                        val finalGst = gstRateStr.toBigDecimalOrNull()
                        viewModel.saveProduct(
                            Product(
                                id = existingProduct?.id ?: 0L,
                                name = name,
                                sellingPrice = sellingPrice!!,
                                hsnCode = hsnCode.takeIf { it.isNotBlank() },
                                unit = unit,
                                gstRate = finalGst,
                                description = description.takeIf { it.isNotBlank() },
                                sku = sku.takeIf { it.isNotBlank() }
                            )
                        )
                        onNavigateBack()
                    }
                },
                enabled = isFormValid,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
