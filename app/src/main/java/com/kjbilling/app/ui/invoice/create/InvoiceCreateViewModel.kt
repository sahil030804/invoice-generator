package com.kjbilling.app.ui.invoice.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.data.repository.AppSettingsRepository
import com.kjbilling.app.data.repository.BusinessProfileRepository
import com.kjbilling.app.data.repository.CustomerRepository
import com.kjbilling.app.data.repository.InvoiceRepository
import com.kjbilling.app.data.repository.ProductRepository
import com.kjbilling.app.di.AppContainer
import com.kjbilling.app.domain.calculator.InvoiceCalculator
import com.kjbilling.app.domain.calculator.InvoiceTotals
import com.kjbilling.app.domain.model.*
import com.kjbilling.app.domain.validator.InvoiceValidator
import com.kjbilling.app.pdf.InvoicePdfGenerator
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal
import java.util.UUID

enum class DiscountType {
    PERCENT,
    AMOUNT
}

data class InvoiceItemUiState(
    val id: String = UUID.randomUUID().toString(),
    val productId: Long? = null,
    val name: String = "",
    val hsnCode: String = "",
    val quantity: String = "1",
    val unit: String = "PCS",
    val unitPrice: String = "",
    val discountPercent: String = "0",
    val discountAmount: String = "",
    val discountType: DiscountType = DiscountType.PERCENT,
    val gstRate: String = "" // empty means use default
)

class InvoiceCreateViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val businessProfileRepository: BusinessProfileRepository,
    private val invoiceCalculator: InvoiceCalculator,
    private val pdfGenerator: InvoicePdfGenerator
) : ViewModel() {

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer

    private val _items = MutableStateFlow<List<InvoiceItemUiState>>(emptyList())
    val items: StateFlow<List<InvoiceItemUiState>> = _items

    private val _taxType = MutableStateFlow(TaxType.CGST_SGST)
    val taxType: StateFlow<TaxType> = _taxType

    private val _notes = MutableStateFlow("")
    val notes: StateFlow<String> = _notes

    private val _invoiceDiscountValue = MutableStateFlow("0")
    val invoiceDiscountValue: StateFlow<String> = _invoiceDiscountValue

    private val _invoiceDiscountType = MutableStateFlow(DiscountType.PERCENT)
    val invoiceDiscountType: StateFlow<DiscountType> = _invoiceDiscountType

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _generatedFile = MutableStateFlow<File?>(null)
    val generatedFile: StateFlow<File?> = _generatedFile

    private val _generatedInvoiceId = MutableStateFlow<Long?>(null)
    val generatedInvoiceId: StateFlow<Long?> = _generatedInvoiceId

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _editingInvoice = MutableStateFlow<Invoice?>(null)
    val isEditMode: StateFlow<Boolean> = _editingInvoice.map { it != null }
        .stateIn(viewModelScope, SharingStarted.Eagerly, false)
    val editingInvoiceNumber: StateFlow<String?> = _editingInvoice.map { it?.invoiceNumber }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess

    private val defaultGstRate = appSettingsRepository.getSettings().map { settings ->
        if (settings.gstEnabled) settings.defaultGstRate else BigDecimal.ZERO
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BigDecimal.ZERO)

    private val gstEnabled = appSettingsRepository.getSettings().map { it.gstEnabled }.stateIn(
        viewModelScope, SharingStarted.Eagerly, true
    )
    
    private val businessProfile = businessProfileRepository.getProfile().stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    val invoiceTotals = combine(
        _items,
        _taxType,
        defaultGstRate,
        _invoiceDiscountValue,
        _invoiceDiscountType
    ) { itemsList, tax, defaultGst, discVal, discType ->
        val mappedItems = itemsList.mapNotNull { uiItem ->
            val qty = uiItem.quantity.toBigDecimalOrNull() ?: BigDecimal.ZERO
            val price = uiItem.unitPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
            if (qty <= BigDecimal.ZERO || price <= BigDecimal.ZERO) return@mapNotNull null

            val discPercent = if (uiItem.discountType == DiscountType.PERCENT) {
                uiItem.discountPercent.toBigDecimalOrNull() ?: BigDecimal.ZERO
            } else {
                BigDecimal.ZERO
            }
            val discAmount = if (uiItem.discountType == DiscountType.AMOUNT) {
                uiItem.discountAmount.toBigDecimalOrNull()
            } else {
                null
            }

            invoiceCalculator.calculateItem(
                quantity = qty,
                unitPrice = price,
                discountPercent = discPercent,
                gstRate = uiItem.gstRate.toBigDecimalOrNull() ?: defaultGst,
                taxType = tax,
                discountAmount = discAmount
            )
        }

        val overallDiscPercent = if (discType == DiscountType.PERCENT) {
            discVal.toBigDecimalOrNull() ?: BigDecimal.ZERO
        } else {
            BigDecimal.ZERO
        }
        val overallDiscAmount = if (discType == DiscountType.AMOUNT) {
            discVal.toBigDecimalOrNull() ?: BigDecimal.ZERO
        } else {
            BigDecimal.ZERO
        }

        invoiceCalculator.calculateInvoice(
            items = mappedItems,
            overallDiscountPercent = overallDiscPercent,
            overallDiscountAmount = overallDiscAmount
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), InvoiceTotals(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, emptyList()))

    fun updateInvoiceDiscountValue(value: String) {
        _invoiceDiscountValue.value = value
        if (_error.value != null) _error.value = null
    }

    fun updateInvoiceDiscountType(type: DiscountType) {
        _invoiceDiscountType.value = type
        if (_error.value != null) _error.value = null
    }

    fun setCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
        updateTaxType(customer)
    }

    private fun updateTaxType(customer: Customer?) {
        if (!gstEnabled.value) {
            _taxType.value = TaxType.NO_GST
            return
        }
        val bProfile = businessProfile.value
        val bState = bProfile?.state
        val cState = customer?.state
        _taxType.value = invoiceCalculator.determineTaxType(bState, cState, customer?.gstin)
    }

    fun addItem() {
        _items.value = _items.value + InvoiceItemUiState()
    }

    fun addProductAsItem(product: Product) {
        val gstRate = if (gstEnabled.value) {
            product.gstRate?.toPlainString() ?: ""
        } else {
            BigDecimal.ZERO.toPlainString()
        }
        val newItem = InvoiceItemUiState(
            productId = product.id,
            name = product.name,
            hsnCode = product.hsnCode ?: "",
            unitPrice = product.sellingPrice.toPlainString(),
            unit = product.unit,
            gstRate = gstRate
        )
        _items.value = _items.value + newItem
    }

    fun updateItem(id: String, updatedItem: InvoiceItemUiState) {
        _items.value = _items.value.map { if (it.id == id) updatedItem else it }
        if (_error.value != null) {
            _error.value = null
        }
    }

    fun removeItem(id: String) {
        _items.value = _items.value.filter { it.id != id }
    }

    fun updateNotes(newNotes: String) {
        _notes.value = newNotes
    }

    fun clearError() {
        _error.value = null
    }

    fun loadInvoice(id: Long) {
        viewModelScope.launch {
            val invoice = invoiceRepository.getById(id) ?: return@launch
            _editingInvoice.value = invoice
            _notes.value = invoice.notes ?: ""
            _taxType.value = invoice.taxType
            if (invoice.customerId != null) {
                val customer = customerRepository.getById(invoice.customerId)
                _selectedCustomer.value = customer ?: Customer(
                    id = invoice.customerId,
                    name = invoice.customerName,
                    billingAddress = invoice.customerAddress,
                    state = invoice.customerState,
                    gstin = invoice.customerGstin
                )
            } else if (invoice.customerName.isNotBlank() && invoice.customerName != "Walk-in Customer") {
                _selectedCustomer.value = Customer(
                    id = 0L,
                    name = invoice.customerName,
                    billingAddress = invoice.customerAddress,
                    state = invoice.customerState,
                    gstin = invoice.customerGstin
                )
            } else {
                _selectedCustomer.value = null
            }

            val existingItemDiscountTotal = invoice.items.fold(BigDecimal.ZERO) { acc, it -> acc.add(it.discountAmount) }
            val overallDiscountOnInvoice = invoice.totalDiscount.subtract(existingItemDiscountTotal).coerceAtLeast(BigDecimal.ZERO)
            if (overallDiscountOnInvoice > BigDecimal.ZERO) {
                _invoiceDiscountValue.value = overallDiscountOnInvoice.stripTrailingZeros().toPlainString()
                _invoiceDiscountType.value = DiscountType.AMOUNT
            } else {
                _invoiceDiscountValue.value = "0"
                _invoiceDiscountType.value = DiscountType.PERCENT
            }

            _items.value = invoice.items.map { item ->
                val hasPercent = item.discountPercent > BigDecimal.ZERO
                val hasAmount = item.discountAmount > BigDecimal.ZERO
                InvoiceItemUiState(
                    id = UUID.randomUUID().toString(),
                    productId = item.productId,
                    name = item.itemName,
                    hsnCode = item.hsnCode ?: "",
                    quantity = item.quantity.stripTrailingZeros().toPlainString(),
                    unit = item.unit ?: "PCS",
                    unitPrice = item.unitPrice.stripTrailingZeros().toPlainString(),
                    discountPercent = item.discountPercent.stripTrailingZeros().toPlainString(),
                    discountAmount = item.discountAmount.stripTrailingZeros().toPlainString(),
                    discountType = if (hasPercent) DiscountType.PERCENT else if (hasAmount) DiscountType.AMOUNT else DiscountType.PERCENT,
                    gstRate = item.gstRate.stripTrailingZeros().toPlainString()
                )
            }
        }
    }

    fun consumeGenerated() {
        _generatedFile.value = null
        _generatedInvoiceId.value = null
        _updateSuccess.value = false
    }

    fun generateInvoice() {
        viewModelScope.launch {
            if (_isGenerating.value) {
                return@launch
            }
            if (_items.value.isEmpty()) {
                _error.value = "Cannot generate invoice with no items"
                return@launch
            }
            if (_items.value.any { it.name.isBlank() || it.unitPrice.isBlank() }) {
                _error.value = "Please fill name and price for all items"
                return@launch
            }
            val discVal = _invoiceDiscountValue.value.toBigDecimalOrNull()
            if (discVal == null || discVal < BigDecimal.ZERO || (_invoiceDiscountType.value == DiscountType.PERCENT && discVal > BigDecimal("100"))) {
                _error.value = "Invoice discount must be between 0 and 100% (or a valid amount)"
                return@launch
            }
            val invalidItem = _items.value.firstOrNull { uiItem ->
                val qty = uiItem.quantity.toBigDecimalOrNull()
                val price = uiItem.unitPrice.toBigDecimalOrNull()
                val disc = if (uiItem.discountType == DiscountType.PERCENT) {
                    uiItem.discountPercent.toBigDecimalOrNull()
                } else {
                    uiItem.discountAmount.toBigDecimalOrNull()
                }
                val gst = uiItem.gstRate.toBigDecimalOrNull() ?: defaultGstRate.value
                qty == null || qty <= BigDecimal.ZERO ||
                    price == null || price <= BigDecimal.ZERO ||
                    disc == null || disc < BigDecimal.ZERO || (uiItem.discountType == DiscountType.PERCENT && disc > BigDecimal("100")) ||
                    gst < BigDecimal.ZERO ||
                    !InvoiceValidator.isWithinLimits(qty, price, gst)
            }
            if (invalidItem != null) {
                _error.value = "Check qty (>0), price (>0), discount (0-100% or ≥0 amount), GST (0-40%) for all items, and keep values realistic"
                return@launch
            }

            _isGenerating.value = true
            _error.value = null
            
            try {
                val bProfile = businessProfile.value ?: throw IllegalStateException("Business Profile not setup")
                val totals = invoiceTotals.value

                val existing = _editingInvoice.value
                if (existing != null) {
                    if (existing.status == InvoiceStatus.CANCELLED) {
                        _error.value = "Cancelled invoices cannot be modified"
                        return@launch
                    }

                    val domainItems = _items.value.mapIndexed { index, uiItem ->
                        val qty = uiItem.quantity.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val price = uiItem.unitPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val discPercent = if (uiItem.discountType == DiscountType.PERCENT) {
                            uiItem.discountPercent.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        } else {
                            BigDecimal.ZERO
                        }
                        val discAmount = if (uiItem.discountType == DiscountType.AMOUNT) {
                            uiItem.discountAmount.toBigDecimalOrNull()
                        } else {
                            null
                        }
                        val calc = invoiceCalculator.calculateItem(
                            quantity = qty,
                            unitPrice = price,
                            discountPercent = discPercent,
                            gstRate = uiItem.gstRate.toBigDecimalOrNull() ?: defaultGstRate.value,
                            taxType = _taxType.value,
                            discountAmount = discAmount
                        )
                        InvoiceItem(
                            id = 0L,
                            invoiceId = existing.id,
                            productId = uiItem.productId,
                            itemName = uiItem.name,
                            hsnCode = uiItem.hsnCode.takeIf { it.isNotBlank() },
                            quantity = qty,
                            unit = uiItem.unit,
                            unitPrice = price,
                            discountPercent = if (uiItem.discountType == DiscountType.PERCENT) discPercent else if (calc.itemAmount > BigDecimal.ZERO) calc.discountAmount.multiply(BigDecimal("100")).divide(calc.itemAmount, 2, java.math.RoundingMode.HALF_UP) else BigDecimal.ZERO,
                            discountAmount = calc.discountAmount,
                            gstRate = calc.gstRate,
                            taxableAmount = calc.taxableAmount,
                            cgstAmount = calc.cgstAmount,
                            sgstAmount = calc.sgstAmount,
                            igstAmount = calc.igstAmount,
                            taxAmount = calc.taxAmount,
                            total = calc.total,
                            sortOrder = index
                        )
                    }

                    val oldPaid = existing.amountPaid
                    val (newPaid, newPaymentStatus, newInvoiceStatus) = when {
                        oldPaid >= totals.grandTotal -> Triple(
                            totals.grandTotal,
                            PaymentStatus.PAID,
                            if (existing.status == InvoiceStatus.CANCELLED) InvoiceStatus.CANCELLED else InvoiceStatus.PAID
                        )
                        oldPaid > BigDecimal.ZERO -> Triple(
                            oldPaid,
                            PaymentStatus.PARTIAL,
                            if (existing.status == InvoiceStatus.PAID) InvoiceStatus.GENERATED else existing.status
                        )
                        else -> Triple(
                            BigDecimal.ZERO,
                            PaymentStatus.UNPAID,
                            if (existing.status == InvoiceStatus.PAID) InvoiceStatus.GENERATED else existing.status
                        )
                    }

                    val updatedInvoice = existing.copy(
                        customerId = _selectedCustomer.value?.id?.takeIf { it != 0L },
                        customerName = _selectedCustomer.value?.name ?: "Walk-in Customer",
                        customerAddress = _selectedCustomer.value?.billingAddress,
                        customerState = _selectedCustomer.value?.state,
                        customerGstin = _selectedCustomer.value?.gstin,
                        items = domainItems,
                        subtotal = totals.subtotal,
                        totalDiscount = totals.totalDiscount,
                        totalTax = totals.totalTax,
                        grandTotal = totals.grandTotal,
                        taxType = _taxType.value,
                        status = newInvoiceStatus,
                        paymentStatus = newPaymentStatus,
                        amountPaid = newPaid,
                        notes = _notes.value.takeIf { it.isNotBlank() },
                        seller = existing.seller ?: BusinessSnapshot.from(bProfile),
                        updatedAt = System.currentTimeMillis()
                    )

                    invoiceRepository.save(updatedInvoice)
                    val savedInvoice = invoiceRepository.getById(existing.id) ?: throw IllegalStateException("Failed to load saved invoice")
                    _generatedInvoiceId.value = savedInvoice.id

                    val pdfResult = pdfGenerator.generate(savedInvoice, bProfile, totals.taxBreakdown)
                    if (pdfResult.isSuccess) {
                        _generatedFile.value = pdfResult.getOrNull()
                    }
                    _updateSuccess.value = true
                } else {
                    val domainItems = _items.value.mapIndexed { index, uiItem ->
                        val qty = uiItem.quantity.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val price = uiItem.unitPrice.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        val discPercent = if (uiItem.discountType == DiscountType.PERCENT) {
                            uiItem.discountPercent.toBigDecimalOrNull() ?: BigDecimal.ZERO
                        } else {
                            BigDecimal.ZERO
                        }
                        val discAmount = if (uiItem.discountType == DiscountType.AMOUNT) {
                            uiItem.discountAmount.toBigDecimalOrNull()
                        } else {
                            null
                        }
                        val calc = invoiceCalculator.calculateItem(
                            quantity = qty,
                            unitPrice = price,
                            discountPercent = discPercent,
                            gstRate = uiItem.gstRate.toBigDecimalOrNull() ?: defaultGstRate.value,
                            taxType = _taxType.value,
                            discountAmount = discAmount
                        )
                        InvoiceItem(
                            id = 0L,
                            invoiceId = 0L,
                            productId = uiItem.productId,
                            itemName = uiItem.name,
                            hsnCode = uiItem.hsnCode.takeIf { it.isNotBlank() },
                            quantity = qty,
                            unit = uiItem.unit,
                            unitPrice = price,
                            discountPercent = if (uiItem.discountType == DiscountType.PERCENT) discPercent else if (calc.itemAmount > BigDecimal.ZERO) calc.discountAmount.multiply(BigDecimal("100")).divide(calc.itemAmount, 2, java.math.RoundingMode.HALF_UP) else BigDecimal.ZERO,
                            discountAmount = calc.discountAmount,
                            gstRate = calc.gstRate,
                            taxableAmount = calc.taxableAmount,
                            cgstAmount = calc.cgstAmount,
                            sgstAmount = calc.sgstAmount,
                            igstAmount = calc.igstAmount,
                            taxAmount = calc.taxAmount,
                            total = calc.total,
                            sortOrder = index
                        )
                    }

                    val invoice = Invoice(
                        id = 0L,
                        invoiceNumber = "", // assigned atomically by saveNew
                        invoiceDate = System.currentTimeMillis(),
                        dueDate = System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L, // 7 days
                        customerId = _selectedCustomer.value?.id,
                        customerName = _selectedCustomer.value?.name ?: "Walk-in Customer",
                        customerAddress = _selectedCustomer.value?.billingAddress,
                        customerState = _selectedCustomer.value?.state,
                        customerGstin = _selectedCustomer.value?.gstin,
                        items = domainItems,
                        subtotal = totals.subtotal,
                        totalDiscount = totals.totalDiscount,
                        totalTax = totals.totalTax,
                        grandTotal = totals.grandTotal,
                        taxType = _taxType.value,
                        status = InvoiceStatus.DRAFT,
                        paymentStatus = PaymentStatus.UNPAID,
                        notes = _notes.value.takeIf { it.isNotBlank() },
                        seller = BusinessSnapshot.from(bProfile)
                    )

                    val savedInvoiceId = invoiceRepository.saveNew(invoice)
                    val savedInvoice = invoiceRepository.getById(savedInvoiceId) ?: throw IllegalStateException("Failed to load saved invoice")
                    _generatedInvoiceId.value = savedInvoice.id

                    val pdfResult = pdfGenerator.generate(savedInvoice, bProfile, totals.taxBreakdown)
                    if (pdfResult.isSuccess) {
                        _generatedFile.value = pdfResult.getOrNull()
                        // Invoice is now a real generated document, not a draft.
                        invoiceRepository.updateStatus(savedInvoiceId, InvoiceStatus.GENERATED)
                    } else {
                        _error.value = "Invoice saved, but PDF generation failed"
                    }
                }

            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to generate invoice"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InvoiceCreateViewModel(
                    container.invoiceRepository,
                    container.customerRepository,
                    container.productRepository,
                    container.appSettingsRepository,
                    container.businessProfileRepository,
                    container.invoiceCalculator,
                    container.invoicePdfGenerator
                ) as T
            }
        }
    }
}
