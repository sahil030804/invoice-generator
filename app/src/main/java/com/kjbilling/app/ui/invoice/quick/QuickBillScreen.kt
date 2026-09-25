package com.kjbilling.app.ui.invoice.quick

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import com.kjbilling.app.domain.quickbill.QuickBill
import com.kjbilling.app.ui.components.UpiQrCard
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.domain.formatter.CurrencyFormatter
import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.domain.model.Product
import com.kjbilling.app.pdf.InvoicePrintHelper
import com.kjbilling.app.pdf.InvoiceShareHelper
import com.kjbilling.app.pdf.PdfDownloadHelper
import com.kjbilling.app.ui.components.appTextFieldColors
import com.kjbilling.app.ui.theme.Dimens
import com.kjbilling.app.ui.theme.ext
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickBillScreen(
    onNavigateBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val context = LocalContext.current
    val app = context.applicationContext as KJInvoiceApp
    val viewModel: QuickBillViewModel = viewModel(factory = QuickBillViewModel.factory(app.container))

    val customers by viewModel.customers.collectAsState()
    val products by viewModel.filteredProducts.collectAsState()
    val selectedCustomer by viewModel.selectedCustomer.collectAsState()
    val itemQuantities by viewModel.itemQuantities.collectAsState()
    val cartSummary by viewModel.cartSummary.collectAsState()
    val canGenerateBill by viewModel.canGenerate.collectAsState()
    val paymentMode by viewModel.paymentMode.collectAsState()
    val customItems by viewModel.customItems.collectAsState()
    val successQr by viewModel.successQr.collectAsState()
    var showCustomSheet by remember { mutableStateOf(false) }
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val generatedInvoice by viewModel.generatedInvoice.collectAsState()
    val generatedFile by viewModel.generatedFile.collectAsState()
    val error by viewModel.error.collectAsState()

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "⚡ Quick Counter Bill",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Senior Friendly • 1-Tap Billing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (cartSummary.totalCount > 0) {
                        TextButton(onClick = { viewModel.clearCart() }) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            Surface(
                tonalElevation = 0.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                val haptic = LocalHapticFeedback.current
                val onGenerate = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    viewModel.generateBill()
                }
                val canGenerate = canGenerateBill && !isGenerating
                // Very large font sizes: stack total above a full-width button so neither gets squeezed.
                val stacked = LocalDensity.current.fontScale >= STACKED_BAR_FONT_SCALE

                Column {
                    PaymentModeRow(
                        selected = paymentMode,
                        udhaarAllowed = QuickBill.canUseUdhaar(selectedCustomer),
                        onSelect = viewModel::selectPaymentMode,
                        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 8.dp)
                    )
                    if (stacked) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(Dimens.Sm)
                        ) {
                            CartSummaryText(cartSummary)
                            GenerateBillButton(canGenerate, isGenerating, onGenerate, Modifier.fillMaxWidth())
                        }
                    } else {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CartSummaryText(cartSummary, Modifier.weight(1f).padding(end = Dimens.Sm))
                            GenerateBillButton(canGenerate, isGenerating, onGenerate)
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Customer Selection Row
            CustomerSelectionSection(
                customers = customers,
                selectedCustomer = selectedCustomer,
                onSelectCustomer = { viewModel.selectCustomer(it) }
            )

            // Search Bar for products
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                placeholder = { Text("Search product name...") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotBlank()) {
                        IconButton(onClick = { viewModel.onSearchQueryChanged("") }) {
                            Icon(Icons.Filled.Close, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = MaterialTheme.shapes.medium,
                colors = appTextFieldColors()
            )

            if (error != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = error ?: "",
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            if (customItems.isNotEmpty()) {
                CustomItemChips(
                    items = customItems,
                    onRemove = viewModel::removeCustomItem,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }

            // Products grid; the first tile adds a custom (non-catalog) amount.
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp),
                contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item(key = "custom-amount") {
                    CustomAmountTile(onClick = { showCustomSheet = true })
                }
                items(products, key = { it.id }) { product ->
                    val qty = itemQuantities[product.id] ?: 0
                    ProductCard(
                        product = product,
                        quantity = qty,
                        onIncrement = { viewModel.incrementProduct(product.id) },
                        onDecrement = { viewModel.decrementProduct(product.id) }
                    )
                }
                if (products.isEmpty()) {
                    item(key = "empty-hint", span = { GridItemSpan(maxLineSpan) }) {
                        Text(
                            text = if (searchQuery.isBlank()) "No products added yet" else "No matching products found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
            }
        }
    }

    if (showCustomSheet) {
        CustomAmountSheet(
            onAdd = { name, amount ->
                viewModel.addCustomItem(name, amount)
                showCustomSheet = false
            },
            onDismiss = { showCustomSheet = false }
        )
    }

    // Bill is already saved but the PDF failed: say so, never leave the cashier guessing (a second
    // tap would create a duplicate invoice). PDF can be regenerated from the invoice detail screen.
    if (generatedInvoice != null && generatedFile == null) {
        val savedInvoice = generatedInvoice!!
        AlertDialog(
            onDismissRequest = { /* Force explicit user action */ },
            title = { Text("Bill saved: ${savedInvoice.invoiceNumber}") },
            text = { Text("The bill was saved, but the PDF could not be created. Open the invoice to download or share it.") },
            confirmButton = {
                TextButton(onClick = {
                    val invoiceId = savedInvoice.id
                    viewModel.resetForNextBill()
                    onNavigateToDetail(invoiceId)
                }) {
                    Text("View invoice")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.resetForNextBill() }) {
                    Text("Start next bill")
                }
            }
        )
    }

    // Success Screen Overlay Dialog
    if (generatedInvoice != null && generatedFile != null) {
        val invoice = generatedInvoice!!
        val file = generatedFile!!
        val haptic = LocalHapticFeedback.current

        var checkmarkVisible by remember { mutableStateOf(false) }
        LaunchedEffect(Unit) {
            checkmarkVisible = true
        }
        val checkmarkScale by animateFloatAsState(
            targetValue = if (checkmarkVisible) 1f else 0.2f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            ),
            label = "checkmark_scale"
        )

        Dialog(
            onDismissRequest = { /* Force explicit user action */ },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                color = MaterialTheme.colorScheme.background
            ) {
                // Scrolls when the UPI QR (or large text) makes it taller than the screen; centred otherwise.
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val viewportHeight = maxHeight
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .heightIn(min = viewportHeight)
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        // Success Checkmark Icon with Spring Pop
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .scale(checkmarkScale)
                                .background(MaterialTheme.ext.paid.container, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Success",
                                tint = MaterialTheme.ext.paid.text,
                                modifier = Modifier.size(64.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        Text(
                            text = "Bill ${invoice.invoiceNumber} Ready!",
                            style = MaterialTheme.typography.headlineMedium,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = invoice.customerName,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${CurrencyFormatter.format(invoice.grandTotal)} ${QuickBill.successLabel(invoice)}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )

                        successQr?.let { request ->
                            Spacer(modifier = Modifier.height(20.dp))
                            UpiQrCard(request)
                        }

                        Spacer(modifier = Modifier.height(32.dp))

                        // 1. WhatsApp Action Button
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                InvoiceShareHelper.shareFile(
                                    context = context,
                                    file = file,
                                    invoiceNumber = invoice.invoiceNumber,
                                    customerName = invoice.customerName,
                                    grandTotal = CurrencyFormatter.format(invoice.grandTotal),
                                    targetPackage = "com.whatsapp"
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.ext.whatsApp,
                                contentColor = MaterialTheme.ext.onWhatsApp
                            )
                        ) {
                            Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(Dimens.Sm))
                            Text("SEND ON WHATSAPP", style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 2. Print Receipt Button
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                InvoicePrintHelper.printPdf(context, file, "Invoice-${invoice.invoiceNumber}")
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) {
                            Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(Dimens.Sm))
                            Text("PRINT RECEIPT", style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // 3. Download PDF Button
                        OutlinedButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                scope.launch {
                                    val result = PdfDownloadHelper.downloadPdf(context, file)
                                    when (result) {
                                        is PdfDownloadHelper.DownloadResult.Success -> {
                                            Toast.makeText(context, "Saved to Downloads folder!", Toast.LENGTH_SHORT).show()
                                        }
                                        is PdfDownloadHelper.DownloadResult.Failure -> {
                                            Toast.makeText(context, "Failed: ${result.reason}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.ButtonHeight),
                            shape = MaterialTheme.shapes.medium,
                            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(Dimens.Sm))
                            Text("DOWNLOAD PDF", style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(modifier = Modifier.height(28.dp))

                        // 4. Start Next Bill Button (Large & Prominent)
                        FilledTonalButton(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.resetForNextBill()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(Dimens.ButtonHeight),
                            shape = MaterialTheme.shapes.medium,
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = MaterialTheme.ext.action,
                                contentColor = MaterialTheme.ext.onAction
                            )
                        ) {
                            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                            Spacer(modifier = Modifier.width(Dimens.Sm))
                            Text("⚡ START NEXT BILL", style = MaterialTheme.typography.labelLarge)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        TextButton(onClick = {
                            val invoiceId = invoice.id
                            viewModel.resetForNextBill()
                            onNavigateToDetail(invoiceId)
                        }) {
                            Text("View Full Invoice Details", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CustomerSelectionSection(
    customers: List<Customer>,
    selectedCustomer: Customer?,
    onSelectCustomer: (Customer?) -> Unit
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(
            text = "CUSTOMER (1-TAP SELECT)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(6.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(end = 16.dp)
        ) {
            // Default Walk-in Customer Chip
            item {
                val isSelected = selectedCustomer == null
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectCustomer(null)
                    },
                    label = {
                        Text(
                            text = if (isSelected) "✓ Walk-in" else "Walk-in",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = customerChipColors(),
                    border = customerChipBorder(isSelected)
                )
            }

            // Top frequent customers
            items(customers.take(5), key = { it.id }) { customer ->
                val isSelected = selectedCustomer?.id == customer.id
                FilterChip(
                    selected = isSelected,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelectCustomer(customer)
                    },
                    label = {
                        Text(
                            text = if (isSelected) "✓ ${customer.name}" else customer.name,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = customerChipColors(),
                    border = customerChipBorder(isSelected)
                )
            }
        }
    }
}

@Composable
private fun ProductCard(
    product: Product,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    val isSelected = quantity > 0

    val animatedBorderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "card_border"
    )
    val animatedContainerColor by animateColorAsState(
        targetValue = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "card_container"
    )
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = animatedBorderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.Md)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = CurrencyFormatter.format(product.sellingPrice),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Stepper controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Minus Button
                FilledTonalIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onDecrement()
                    },
                    enabled = quantity > 0,
                    modifier = Modifier.size(Dimens.Stepper),
                    shape = MaterialTheme.shapes.small,
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                ) {
                    Text(
                        text = "−",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                AnimatedContent(
                    targetState = quantity,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { height -> height / 2 } + fadeIn()) togetherWith
                                (slideOutVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { height -> -height / 2 } + fadeOut())
                        } else {
                            (slideInVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { height -> -height / 2 } + fadeIn()) togetherWith
                                (slideOutVertically(animationSpec = tween(180, easing = FastOutSlowInEasing)) { height -> height / 2 } + fadeOut())
                        }.using(SizeTransform(clip = false))
                    },
                    label = "product_qty"
                ) { qty ->
                    Text(
                        text = "$qty",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp)
                    )
                }

                // Plus Button
                FilledIconButton(
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onIncrement()
                    },
                    modifier = Modifier.size(Dimens.Stepper),
                    shape = MaterialTheme.shapes.small,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun customerChipColors() = FilterChipDefaults.filterChipColors(
    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
)

@Composable
private fun customerChipBorder(selected: Boolean) = FilterChipDefaults.filterChipBorder(
    enabled = true,
    selected = selected,
    borderColor = MaterialTheme.colorScheme.outline,
    selectedBorderColor = MaterialTheme.colorScheme.primary,
    selectedBorderWidth = 1.5.dp
)

private const val STACKED_BAR_FONT_SCALE = 1.5f

@Composable
private fun CartSummaryText(cartSummary: QuickCartSummary, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        AnimatedContent(
            targetState = cartSummary.totalCount,
            transitionSpec = {
                (slideInVertically { height -> height / 2 } + fadeIn()) togetherWith
                    (slideOutVertically { height -> -height / 2 } + fadeOut())
            },
            label = "items_count"
        ) { count ->
            Text(
                text = "$count ${if (count == 1) "item" else "items"} selected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        AnimatedContent(
            targetState = cartSummary.grandTotal,
            transitionSpec = {
                if (targetState > initialState) {
                    (slideInVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { height -> height } + fadeIn()) togetherWith
                        (slideOutVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { height -> -height } + fadeOut())
                } else {
                    (slideInVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { height -> -height } + fadeIn()) togetherWith
                        (slideOutVertically(animationSpec = tween(220, easing = FastOutSlowInEasing)) { height -> height } + fadeOut())
                }.using(SizeTransform(clip = false))
            },
            label = "grand_total"
        ) { total ->
            Text(
                text = CurrencyFormatter.format(total),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun GenerateBillButton(
    enabled: Boolean,
    isGenerating: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = MaterialTheme.shapes.medium,
        modifier = modifier.heightIn(min = Dimens.ButtonHeight),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.ext.action,
            contentColor = MaterialTheme.ext.onAction
        )
    ) {
        if (isGenerating) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = MaterialTheme.ext.onAction,
                strokeWidth = 2.dp
            )
        } else {
            Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(22.dp))
            Spacer(modifier = Modifier.width(Dimens.Sm))
            Text(
                "Generate Bill",
                style = MaterialTheme.typography.labelLarge,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
