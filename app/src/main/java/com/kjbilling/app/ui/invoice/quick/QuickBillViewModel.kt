package com.kjbilling.app.ui.invoice.quick

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
import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceItem
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.domain.model.Product
import com.kjbilling.app.domain.model.TaxType
import com.kjbilling.app.pdf.InvoicePdfGenerator
import java.io.File
import java.math.BigDecimal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class QuickCartSummary(
    val totalCount: Int = 0,
    val subtotal: BigDecimal = BigDecimal.ZERO,
    val totalTax: BigDecimal = BigDecimal.ZERO,
    val grandTotal: BigDecimal = BigDecimal.ZERO
)

class QuickBillViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val customerRepository: CustomerRepository,
    private val productRepository: ProductRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val businessProfileRepository: BusinessProfileRepository,
    private val invoiceCalculator: InvoiceCalculator,
    private val pdfGenerator: InvoicePdfGenerator
) : ViewModel() {

    val customers: StateFlow<List<Customer>> = customerRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = productRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer

    private val _itemQuantities = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val itemQuantities: StateFlow<Map<Long, Int>> = _itemQuantities

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating

    private val _generatedInvoice = MutableStateFlow<Invoice?>(null)
    val generatedInvoice: StateFlow<Invoice?> = _generatedInvoice

    private val _generatedFile = MutableStateFlow<File?>(null)
    val generatedFile: StateFlow<File?> = _generatedFile

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val defaultGstRate = appSettingsRepository.getSettings().map { settings ->
        if (settings.gstEnabled) settings.defaultGstRate else BigDecimal.ZERO
    }.stateIn(viewModelScope, SharingStarted.Eagerly, BigDecimal.ZERO)

    private val gstEnabled = appSettingsRepository.getSettings().map { it.gstEnabled }.stateIn(
        viewModelScope, SharingStarted.Eagerly, true
    )

    private val businessProfile = businessProfileRepository.getProfile().stateIn(
        viewModelScope, SharingStarted.Eagerly, null
    )

    val cartSummary: StateFlow<QuickCartSummary> = combine(
        _itemQuantities,
        products,
        _selectedCustomer,
        defaultGstRate
    ) { quantities, allProducts, customer, defaultGst ->
        val productMap = allProducts.associateBy { it.id }
        var totalCount = 0
        val taxType = if (!gstEnabled.value) {
            TaxType.NO_GST
        } else {
            invoiceCalculator.determineTaxType(businessProfile.value?.state, customer?.state)
        }

        val calculatedItems = quantities.mapNotNull { (productId, qty) ->
            if (qty <= 0) return@mapNotNull null
            val product = productMap[productId] ?: return@mapNotNull null
            totalCount += qty

            val itemGst = product.gstRate ?: defaultGst
            invoiceCalculator.calculateItem(
                quantity = BigDecimal(qty),
                unitPrice = product.sellingPrice,
                discountPercent = BigDecimal.ZERO,
                gstRate = itemGst,
                taxType = taxType
            )
        }

        val totals = invoiceCalculator.calculateInvoice(calculatedItems)
        QuickCartSummary(
            totalCount = totalCount,
            subtotal = totals.subtotal,
            totalTax = totals.totalTax,
            grandTotal = totals.grandTotal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuickCartSummary())

    val filteredProducts: StateFlow<List<Product>> = combine(
        products,
        _searchQuery
    ) { allProducts, query ->
        if (query.isBlank()) {
            allProducts
        } else {
            allProducts.filter {
                it.name.contains(query, ignoreCase = true) ||
                    (it.sku?.contains(query, ignoreCase = true) == true)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectCustomer(customer: Customer?) {
        _selectedCustomer.value = customer
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun incrementProduct(productId: Long) {
        val current = _itemQuantities.value.toMutableMap()
        val currentQty = current[productId] ?: 0
        current[productId] = currentQty + 1
        _itemQuantities.value = current
    }

    fun decrementProduct(productId: Long) {
        val current = _itemQuantities.value.toMutableMap()
        val currentQty = current[productId] ?: 0
        if (currentQty <= 1) {
            current.remove(productId)
        } else {
            current[productId] = currentQty - 1
        }
        _itemQuantities.value = current
    }

    fun clearCart() {
        _itemQuantities.value = emptyMap()
        _error.value = null
    }

    fun resetForNextBill() {
        _selectedCustomer.value = null
        _itemQuantities.value = emptyMap()
        _searchQuery.value = ""
        _isGenerating.value = false
        _generatedInvoice.value = null
        _generatedFile.value = null
        _error.value = null
    }

    fun generateBill(paymentMethod: PaymentMethod = PaymentMethod.CASH) {
        viewModelScope.launch {
            if (_isGenerating.value) return@launch

            val currentQuantities = _itemQuantities.value.filter { it.value > 0 }
            if (currentQuantities.isEmpty()) {
                _error.value = "Please select at least one product"
                return@launch
            }

            _isGenerating.value = true
            _error.value = null

            try {
                val bProfile = businessProfile.value ?: throw IllegalStateException("Business Profile not setup")
                val productMap = products.value.associateBy { it.id }
                val customer = _selectedCustomer.value

                val taxType = if (!gstEnabled.value) {
                    TaxType.NO_GST
                } else {
                    invoiceCalculator.determineTaxType(bProfile.state, customer?.state)
                }

                val domainItems = mutableListOf<InvoiceItem>()
                val calculations = mutableListOf<com.kjbilling.app.domain.calculator.InvoiceItemCalculation>()

                currentQuantities.entries.forEachIndexed { index, (productId, qty) ->
                    val product = productMap[productId] ?: return@forEachIndexed
                    val itemGst = product.gstRate ?: defaultGstRate.value
                    val calc = invoiceCalculator.calculateItem(
                        quantity = BigDecimal(qty),
                        unitPrice = product.sellingPrice,
                        discountPercent = BigDecimal.ZERO,
                        gstRate = itemGst,
                        taxType = taxType
                    )
                    calculations.add(calc)

                    domainItems.add(
                        InvoiceItem(
                            id = 0L,
                            invoiceId = 0L,
                            productId = product.id,
                            itemName = product.name,
                            hsnCode = product.hsnCode,
                            quantity = BigDecimal(qty),
                            unit = product.unit,
                            unitPrice = product.sellingPrice,
                            discountPercent = BigDecimal.ZERO,
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
                    )
                }

                val totals = invoiceCalculator.calculateInvoice(calculations)
                val invoiceNumber = invoiceRepository.generateNextInvoiceNumber()

                val invoice = Invoice(
                    id = 0L,
                    invoiceNumber = invoiceNumber,
                    customerId = customer?.id,
                    customerName = customer?.name ?: "Walk-in Customer",
                    customerAddress = customer?.billingAddress,
                    customerState = customer?.state,
                    customerGstin = customer?.gstin,
                    invoiceDate = System.currentTimeMillis(),
                    dueDate = null,
                    items = domainItems,
                    subtotal = totals.subtotal,
                    totalDiscount = totals.totalDiscount,
                    totalTax = totals.totalTax,
                    grandTotal = totals.grandTotal,
                    taxType = taxType,
                    status = InvoiceStatus.PAID,
                    paymentStatus = PaymentStatus.PAID,
                    paymentMethod = paymentMethod,
                    amountPaid = totals.grandTotal,
                    notes = "Counter Sale (Quick Bill)",
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val invoiceId = invoiceRepository.save(invoice)
                val savedInvoice = invoiceRepository.getById(invoiceId)
                    ?: throw IllegalStateException("Failed to load saved invoice")

                // Update product usage stats in background
                currentQuantities.keys.forEach { pId ->
                    productRepository.incrementUseCount(pId)
                }
                customer?.id?.let { cId ->
                    customerRepository.markUsed(cId)
                }

                val pdfResult = pdfGenerator.generate(savedInvoice, bProfile, totals.taxBreakdown)
                if (pdfResult.isSuccess) {
                    _generatedFile.value = pdfResult.getOrNull()
                }

                _generatedInvoice.value = savedInvoice
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to generate quick bill"
            } finally {
                _isGenerating.value = false
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return QuickBillViewModel(
                    invoiceRepository = container.invoiceRepository,
                    customerRepository = container.customerRepository,
                    productRepository = container.productRepository,
                    appSettingsRepository = container.appSettingsRepository,
                    businessProfileRepository = container.businessProfileRepository,
                    invoiceCalculator = container.invoiceCalculator,
                    pdfGenerator = container.invoicePdfGenerator
                ) as T
            }
        }
    }
}
