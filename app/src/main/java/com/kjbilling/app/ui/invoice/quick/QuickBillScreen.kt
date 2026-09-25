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
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Senior Friendly • 1-Tap Billing",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
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
                tonalElevation = 8.dp,
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
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
                                    fontWeight = FontWeight.Black,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }

                        val haptic = LocalHapticFeedback.current
                        Button(
                            onClick = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                viewModel.generateBill()
                            },
                            enabled = cartSummary.totalCount > 0 && !isGenerating,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(52.dp)
                                .widthIn(min = 160.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF10B981), // Fresh green
                                contentColor = Color.White
                            )
                        ) {
                            if (isGenerating) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(24.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Generate Bill", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            }
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
                shape = RoundedCornerShape(12.dp)
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

            // Products Grid
            if (products.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (searchQuery.isBlank()) "No products added yet" else "No matching products found",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 12.dp),
                    contentPadding = PaddingValues(top = 6.dp, bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(products, key = { it.id }) { product ->
                        val qty = itemQuantities[product.id] ?: 0
                        ProductCard(
                            product = product,
                            quantity = qty,
                            onIncrement = { viewModel.incrementProduct(product.id) },
                            onDecrement = { viewModel.decrementProduct(product.id) }
                        )
                    }
                }
            }
        }
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // Success Checkmark Icon with Spring Pop
                    Box(
                        modifier = Modifier
                            .size(90.dp)
                            .scale(checkmarkScale)
                            .background(Color(0xFF10B981).copy(alpha = 0.15f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = "Success",
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(64.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Bill ${invoice.invoiceNumber} Ready!",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
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
                        text = "${CurrencyFormatter.format(invoice.grandTotal)} (Paid in Cash)",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.primary
                    )

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
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF25D366), // WhatsApp Green
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Filled.Share, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("SEND ON WHATSAPP", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Icon(Icons.Filled.Print, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("PRINT RECEIPT", fontWeight = FontWeight.Bold, fontSize = 16.sp)
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
                            .height(54.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Filled.ArrowDropDown, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("DOWNLOAD PDF", fontWeight = FontWeight.Bold, fontSize = 15.sp)
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
                            .height(56.dp),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("⚡ START NEXT BILL", fontWeight = FontWeight.Black, fontSize = 16.sp)
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
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
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
                            text = if (isSelected) "✓ Walk-in (Cash)" else "Walk-in (Cash)",
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = Color(0xFF10B981).copy(alpha = 0.15f),
                        selectedLabelColor = Color(0xFF047857)
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = if (isSelected) Color(0xFF10B981) else MaterialTheme.colorScheme.outline
                    )
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
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
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
            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.25f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "card_container"
    )
    val animatedElevation by animateDpAsState(
        targetValue = if (isSelected) 4.dp else 1.dp,
        animationSpec = tween(220, easing = FastOutSlowInEasing),
        label = "card_elevation"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = animatedContainerColor),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = animatedBorderColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = animatedElevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = CurrencyFormatter.format(product.sellingPrice),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold,
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
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = IconButtonDefaults.filledTonalIconButtonColors(
                        containerColor = if (quantity > 0) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (quantity > 0) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Text(
                        text = "−",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
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
                    modifier = Modifier.size(42.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Text(
                        text = "+",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
