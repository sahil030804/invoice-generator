package com.kjbilling.app.ui.invoice.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.domain.model.Product
import com.kjbilling.app.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InvoiceCreateScreen(
    invoiceId: Long? = null,
    onNavigateBack: () -> Unit,
    onNavigateToShare: (Long, String) -> Unit, // invoiceId, filePath
    onNavigateToNewCustomer: () -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val viewModel: InvoiceCreateViewModel = viewModel(factory = InvoiceCreateViewModel.factory(app.container))

    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val items by viewModel.items.collectAsState()
    val totals by viewModel.invoiceTotals.collectAsState()
    val notes by viewModel.notes.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generatedFile by viewModel.generatedFile.collectAsState()
    val generatedInvoiceId by viewModel.generatedInvoiceId.collectAsState()
    val error by viewModel.error.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val editingInvoiceNumber by viewModel.editingInvoiceNumber.collectAsState()
    val updateSuccess by viewModel.updateSuccess.collectAsState()

    var showCustomerSheet by remember { mutableStateOf(false) }
    var showItemSheet by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    LaunchedEffect(invoiceId) {
        if (invoiceId != null) {
            viewModel.loadInvoice(invoiceId)
        }
    }

    LaunchedEffect(generatedFile, generatedInvoiceId) {
        if (!isEditMode && generatedFile != null && generatedInvoiceId != null) {
            onNavigateToShare(generatedInvoiceId!!, generatedFile!!.absolutePath)
            viewModel.consumeGenerated()
        }
    }

    LaunchedEffect(updateSuccess) {
        if (updateSuccess) {
            viewModel.consumeGenerated()
            onNavigateBack()
        }
    }

    fun handleBack() {
        if (items.isNotEmpty() || selectedCustomer != null || notes.isNotBlank()) {
            showDiscardDialog = true
        } else {
            onNavigateBack()
        }
    }

    // Hardware/gesture back must ask too, not just the toolbar arrow.
    BackHandler(onBack = ::handleBack)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isEditMode) {
                            if (editingInvoiceNumber != null) "Edit Invoice ($editingInvoiceNumber)" else "Edit Invoice"
                        } else {
                            "New Invoice"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = ::handleBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Customer Section
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = selectedCustomer?.name ?: "Walk-in Customer",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.weight(1f)
                            )
                            TextButton(onClick = { showCustomerSheet = true }) {
                                Text("Change")
                            }
                        }
                    }
                }

                // Items Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Items", style = MaterialTheme.typography.titleLarge)
                    TextButton(onClick = { viewModel.addItem() }) {
                        Text("+ Add Empty Item")
                    }
                    TextButton(onClick = { showItemSheet = true }) {
                        Text("+ Add Product")
                    }
                }

                items.forEach { item ->
                    InvoiceItemEditor(
                        item = item,
                        onUpdate = { viewModel.updateItem(item.id, it) },
                        onRemove = { viewModel.removeItem(item.id) }
                    )
                }

                if (items.isEmpty()) {
                    EmptyState(
                        title = "No items added",
                        subtitle = "Add at least one item to generate the invoice",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    )
                }

                // Invoice Discount Section
                val invoiceDiscountValue by viewModel.invoiceDiscountValue.collectAsState()
                val invoiceDiscountType by viewModel.invoiceDiscountType.collectAsState()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Invoice Discount",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "Applies to entire bill",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = if (invoiceDiscountValue == "0") "" else invoiceDiscountValue,
                                onValueChange = { viewModel.updateInvoiceDiscountValue(it.ifBlank { "0" }) },
                                placeholder = { Text("0") },
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                modifier = Modifier.width(90.dp)
                            )

                            // Toggle % / ₹
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Surface(
                                    color = if (invoiceDiscountType == DiscountType.PERCENT) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { viewModel.updateInvoiceDiscountType(DiscountType.PERCENT) }
                                ) {
                                    Text(
                                        text = "%",
                                        color = if (invoiceDiscountType == DiscountType.PERCENT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                                Surface(
                                    color = if (invoiceDiscountType == DiscountType.AMOUNT) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { viewModel.updateInvoiceDiscountType(DiscountType.AMOUNT) }
                                ) {
                                    Text(
                                        text = "₹",
                                        color = if (invoiceDiscountType == DiscountType.AMOUNT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }
                            }
                        }
                    }
                }

                // Total Section
                TotalCard(
                    subtotal = CurrencyFormatter.format(totals.subtotal),
                    discount = CurrencyFormatter.format(totals.totalDiscount),
                    taxLabel = "Tax",
                    taxAmount = CurrencyFormatter.format(totals.totalTax),
                    grandTotal = CurrencyFormatter.format(totals.grandTotal),
                    modifier = Modifier.fillMaxWidth()
                )

                // Notes
                AppTextField(
                    value = notes,
                    onValueChange = viewModel::updateNotes,
                    label = "Notes (Optional)",
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(16.dp))

                PrimaryButton(
                    text = if (isEditMode) "Update Invoice" else "Generate Invoice",
                    onClick = { viewModel.generateInvoice() },
                    enabled = items.isNotEmpty() && !isGenerating,
                    modifier = Modifier.fillMaxWidth()
                )
                
                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (isGenerating) {
                LoadingState(modifier = Modifier.fillMaxSize())
            }
        }
    }

    if (showCustomerSheet) {
        CustomerSelectorSheet(
            onDismiss = { showCustomerSheet = false },
            onCustomerSelect = {
                viewModel.setCustomer(it)
                showCustomerSheet = false
            },
            onNewCustomer = {
                showCustomerSheet = false
                onNavigateToNewCustomer()
            }
        )
    }

    if (showItemSheet) {
        ItemSelectorSheet(
            onDismiss = { showItemSheet = false },
            onProductSelect = {
                viewModel.addProductAsItem(it)
                showItemSheet = false
            }
        )
    }

    if (showDiscardDialog) {
        ConfirmationDialog(
            title = if (isEditMode) "Discard Changes?" else "Discard Invoice?",
            message = if (isEditMode) "Unsaved edits will be lost. Continue?" else "Unsaved items will be lost. Continue?",
            confirmText = "Discard",
            onConfirm = {
                showDiscardDialog = false
                onNavigateBack()
            },
            onDismiss = { showDiscardDialog = false }
        )
    }
}

@Composable
fun InvoiceItemEditor(
    item: InvoiceItemUiState,
    onUpdate: (InvoiceItemUiState) -> Unit,
    onRemove: () -> Unit
) {
    var expanded by remember { mutableStateOf(item.name.isBlank()) }
    
    val qty = item.quantity.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
    val price = item.unitPrice.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
    val itemGross = qty.multiply(price)
    val discAmt = if (item.discountType == DiscountType.PERCENT) {
        val pct = item.discountPercent.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
        itemGross.multiply(pct).divide(java.math.BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP)
    } else {
        item.discountAmount.toBigDecimalOrNull() ?: java.math.BigDecimal.ZERO
    }
    val itemNet = (itemGross.subtract(discAmt)).coerceAtLeast(java.math.BigDecimal.ZERO)

    Card(modifier = Modifier.fillMaxWidth().animateContentSize()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    if (item.name.isNotBlank()) {
                        Text(item.name, style = MaterialTheme.typography.titleMedium)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${item.quantity} x ${CurrencyFormatter.format(price)}")
                            if (discAmt > java.math.BigDecimal.ZERO) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(-${CurrencyFormatter.format(discAmt)})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        Text("New Item", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                
                Text(CurrencyFormatter.format(itemNet), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                
                IconButton(onClick = { expanded = !expanded }) {
                    Icon(
                        imageVector = if (expanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = "Expand"
                    )
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Remove", tint = MaterialTheme.colorScheme.error)
                }
            }
            
            AnimatedVisibility(visible = expanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppTextField(
                        value = item.name,
                        onValueChange = { onUpdate(item.copy(name = it)) },
                        label = "Item Name",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuantityInput(
                            value = item.quantity,
                            onValueChange = { onUpdate(item.copy(quantity = it)) },
                            label = "Qty",
                            modifier = Modifier.weight(1f)
                        )
                        MoneyInput(
                            value = item.unitPrice,
                            onValueChange = { onUpdate(item.copy(unitPrice = it)) },
                            label = "Price",
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Discount input with % / ₹ toggle
                        Row(
                            modifier = Modifier.weight(1.3f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppTextField(
                                value = if (item.discountType == DiscountType.PERCENT) item.discountPercent else item.discountAmount,
                                onValueChange = {
                                    if (item.discountType == DiscountType.PERCENT) {
                                        onUpdate(item.copy(discountPercent = it))
                                    } else {
                                        onUpdate(item.copy(discountAmount = it))
                                    }
                                },
                                label = if (item.discountType == DiscountType.PERCENT) "Disc %" else "Disc ₹",
                                modifier = Modifier.weight(1f)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            ) {
                                Surface(
                                    color = if (item.discountType == DiscountType.PERCENT) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { onUpdate(item.copy(discountType = DiscountType.PERCENT)) }
                                ) {
                                    Text(
                                        text = "%",
                                        color = if (item.discountType == DiscountType.PERCENT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Surface(
                                    color = if (item.discountType == DiscountType.AMOUNT) MaterialTheme.colorScheme.primary else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable { onUpdate(item.copy(discountType = DiscountType.AMOUNT)) }
                                ) {
                                    Text(
                                        text = "₹",
                                        color = if (item.discountType == DiscountType.AMOUNT) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        AppTextField(
                            value = item.gstRate,
                            onValueChange = { onUpdate(item.copy(gstRate = it)) },
                            label = "GST %",
                            modifier = Modifier.weight(0.9f)
                        )
                    }
                    AppTextField(
                        value = item.hsnCode,
                        onValueChange = { onUpdate(item.copy(hsnCode = it)) },
                        label = "HSN Code",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerSelectorSheet(
    onDismiss: () -> Unit,
    onCustomerSelect: (Customer?) -> Unit,
    onNewCustomer: () -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val customerRepo = app.container.customerRepository
    val customers by customerRepo.getAll().collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    
    val filtered = customers.filter { it.name.contains(query, ignoreCase = true) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text("Select Customer", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(query = query, onQueryChange = { query = it })
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn {
                item {
                    ListItem(
                        headlineContent = { Text("Walk-in Customer") },
                        modifier = Modifier.clickable { onCustomerSelect(null) }
                    )
                }
                items(filtered) { customer ->
                    ListItem(
                        headlineContent = { Text(customer.name) },
                        supportingContent = { Text(customer.mobile ?: "") },
                        modifier = Modifier.clickable { onCustomerSelect(customer) }
                    )
                }
                item {
                    TextButton(onClick = onNewCustomer, modifier = Modifier.fillMaxWidth()) {
                        Text("+ Create New Customer")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemSelectorSheet(
    onDismiss: () -> Unit,
    onProductSelect: (Product) -> Unit
) {
    val app = LocalContext.current.applicationContext as KJInvoiceApp
    val productRepo = app.container.productRepository
    val products by productRepo.getAll().collectAsState(initial = emptyList())
    var query by remember { mutableStateOf("") }
    
    val filtered = products.filter { it.name.contains(query, ignoreCase = true) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)) {
            Text("Select Product", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))
            SearchBar(query = query, onQueryChange = { query = it })
            Spacer(modifier = Modifier.height(8.dp))
            
            LazyColumn {
                items(filtered) { product ->
                    ListItem(
                        headlineContent = { Text(product.name) },
                        supportingContent = { Text(CurrencyFormatter.format(product.sellingPrice)) },
                        modifier = Modifier.clickable { onProductSelect(product) }
                    )
                }
                if (filtered.isEmpty()) {
                    item {
                        EmptyState(title = "No products found")
                    }
                }
            }
        }
    }
}
