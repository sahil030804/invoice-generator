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
import com.kjbilling.app.domain.calculator.InvoiceItemCalculation
import com.kjbilling.app.domain.model.BusinessSnapshot
import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceItem
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.Product
import com.kjbilling.app.domain.model.TaxType
import com.kjbilling.app.domain.quickbill.CustomCartItem
import com.kjbilling.app.domain.quickbill.QuickBill
import com.kjbilling.app.domain.quickbill.QuickPaymentMode
import com.kjbilling.app.domain.upi.UpiPayment
import com.kjbilling.app.domain.upi.UpiQrRequest
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

    // The seeded walk-in record has its own chip, so it's left out of the customer list.
    val customers: StateFlow<List<Customer>> = customerRepository.getAll()
        .map { list -> list.filterNot { it.isWalkIn } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val products: StateFlow<List<Product>> = productRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedCustomer = MutableStateFlow<Customer?>(null)
    val selectedCustomer: StateFlow<Customer?> = _selectedCustomer

    private val _itemQuantities = MutableStateFlow<Map<Long, Int>>(emptyMap())
    val itemQuantities: StateFlow<Map<Long, Int>> = _itemQuantities

    private val _customItems = MutableStateFlow<List<CustomCartItem>>(emptyList())
    val customItems: StateFlow<List<CustomCartItem>> = _customItems

    private val _paymentMode = MutableStateFlow(QuickPaymentMode.CASH)
    val paymentMode: StateFlow<QuickPaymentMode> = _paymentMode

    private var nextCustomItemId = 1L

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
        _customItems,
        products,
        _selectedCustomer,
        defaultGstRate
    ) { quantities, custom, allProducts, customer, defaultGst ->
        val taxType = taxTypeFor(businessProfile.value?.state, customer)
        val lines = buildLines(quantities, custom, allProducts.associateBy { it.id }, taxType, defaultGst)
        val totals = invoiceCalculator.calculateInvoice(lines.map { it.calc })
        QuickCartSummary(
            totalCount = QuickBill.billableCount(quantities, custom.size, allProducts.mapTo(HashSet()) { it.id }),
            subtotal = totals.subtotal,
            totalTax = totals.totalTax,
            grandTotal = totals.grandTotal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), QuickCartSummary())

    /** Generate is allowed once something is in the cart; Udhaar also needs a real customer. */
    val canGenerate: StateFlow<Boolean> = combine(cartSummary, _paymentMode, _selectedCustomer) { summary, mode, customer ->
        QuickBill.canGenerate(summary.totalCount, mode, customer)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    /** "Scan to pay" QR on the success screen when the bill was settled by UPI. */
    val successQr: StateFlow<UpiQrRequest?> = combine(_generatedInvoice, businessProfile) { invoice, profile ->
        invoice?.takeIf { it.paymentMethod == PaymentMethod.UPI }
            ?.let { UpiPayment.forAmount(it.grandTotal, profile, note = it.invoiceNumber) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

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

    fun selectPaymentMode(mode: QuickPaymentMode) {
        _paymentMode.value = mode
    }

    fun addCustomItem(name: String, amount: BigDecimal) {
        if (amount <= BigDecimal.ZERO) {
            return
        }
        _customItems.value = _customItems.value + CustomCartItem(nextCustomItemId++, QuickBill.customItemName(name), amount)
    }

    fun removeCustomItem(id: Long) {
        _customItems.value = _customItems.value.filterNot { it.id == id }
    }

    fun clearCart() {
        _itemQuantities.value = emptyMap()
        _customItems.value = emptyList()
        _error.value = null
    }

    fun resetForNextBill() {
        _selectedCustomer.value = null
        _itemQuantities.value = emptyMap()
        _customItems.value = emptyList()
        _paymentMode.value = QuickPaymentMode.CASH
        _searchQuery.value = ""
        _isGenerating.value = false
        _generatedInvoice.value = null
        _generatedFile.value = null
        _error.value = null
    }

    fun generateBill() {
        viewModelScope.launch {
            if (_isGenerating.value) return@launch

            val currentQuantities = _itemQuantities.value.filter { it.value > 0 }
            val custom = _customItems.value
            val mode = _paymentMode.value
            val customer = _selectedCustomer.value
            val billable = QuickBill.billableCount(currentQuantities, custom.size, products.value.mapTo(HashSet()) { it.id })
            if (!QuickBill.canGenerate(billable, mode, customer)) {
                _error.value = if (billable == 0) {
                    "Please select at least one product"
                } else {
                    "Select a customer for Udhaar"
                }
                return@launch
            }

            _isGenerating.value = true
            _error.value = null

            try {
                val bProfile = businessProfile.value ?: throw IllegalStateException("Business Profile not setup")
                val taxType = taxTypeFor(bProfile.state, customer)
                val lines = buildLines(currentQuantities, custom, products.value.associateBy { it.id }, taxType, defaultGstRate.value)
                val totals = invoiceCalculator.calculateInvoice(lines.map { it.calc })
                val payment = QuickBill.paymentFields(mode, totals.grandTotal)

                val invoice = Invoice(
                    id = 0L,
                    invoiceNumber = "", // assigned atomically by saveNew
                    customerId = customer?.id,
                    customerName = customer?.name ?: "Walk-in Customer",
                    customerAddress = customer?.billingAddress,
                    customerState = customer?.state,
                    customerGstin = customer?.gstin,
                    invoiceDate = System.currentTimeMillis(),
                    dueDate = null,
                    items = lines.map { it.item },
                    subtotal = totals.subtotal,
                    totalDiscount = totals.totalDiscount,
                    totalTax = totals.totalTax,
                    grandTotal = totals.grandTotal,
                    taxType = taxType,
                    status = payment.status,
                    paymentStatus = payment.paymentStatus,
                    paymentMethod = payment.method,
                    amountPaid = payment.amountPaid,
                    notes = "Counter Sale (Quick Bill)",
                    seller = BusinessSnapshot.from(bProfile),
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                val invoiceId = invoiceRepository.saveNew(invoice)
                val savedInvoice = invoiceRepository.getById(invoiceId)
                    ?: throw IllegalStateException("Failed to load saved invoice")

                // Usage stats only for catalog products; custom amounts have no product.
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

    private fun taxTypeFor(businessState: String?, customer: Customer?): TaxType {
        if (!gstEnabled.value) {
            return TaxType.NO_GST
        }
        return invoiceCalculator.determineTaxType(businessState, customer?.state, customer?.gstin)
    }

    private data class BillLine(val item: InvoiceItem, val calc: InvoiceItemCalculation)

    /** Cart → invoice lines. Catalog items add GST on top; custom amounts already include it. */
    private fun buildLines(
        quantities: Map<Long, Int>,
        custom: List<CustomCartItem>,
        productMap: Map<Long, Product>,
        taxType: TaxType,
        defaultGst: BigDecimal
    ): List<BillLine> {
        val productLines = quantities.entries
            .filter { it.value > 0 }
            .mapNotNull { (productId, qty) ->
                val product = productMap[productId] ?: return@mapNotNull null
                val calc = invoiceCalculator.calculateItem(
                    quantity = BigDecimal(qty),
                    unitPrice = product.sellingPrice,
                    discountPercent = BigDecimal.ZERO,
                    gstRate = product.gstRate ?: defaultGst,
                    taxType = taxType
                )
                BillLine(
                    InvoiceItem(
                        productId = product.id,
                        itemName = product.name,
                        hsnCode = product.hsnCode,
                        quantity = BigDecimal(qty),
                        unit = product.unit,
                        unitPrice = product.sellingPrice,
                        discountAmount = calc.discountAmount,
                        gstRate = calc.gstRate,
                        taxableAmount = calc.taxableAmount,
                        cgstAmount = calc.cgstAmount,
                        sgstAmount = calc.sgstAmount,
                        igstAmount = calc.igstAmount,
                        taxAmount = calc.taxAmount,
                        total = calc.total
                    ),
                    calc
                )
            }
        val customLines = custom.map { entry ->
            val calc = invoiceCalculator.calculateInclusiveItem(entry.amount, defaultGst, taxType)
            BillLine(
                InvoiceItem(
                    productId = null,
                    itemName = entry.name,
                    quantity = BigDecimal.ONE,
                    unit = null,
                    unitPrice = calc.taxableAmount,
                    gstRate = calc.gstRate,
                    taxableAmount = calc.taxableAmount,
                    cgstAmount = calc.cgstAmount,
                    sgstAmount = calc.sgstAmount,
                    igstAmount = calc.igstAmount,
                    taxAmount = calc.taxAmount,
                    total = calc.total,
                    priceIncludesTax = true
                ),
                calc
            )
        }
        return (productLines + customLines).mapIndexed { index, line ->
            line.copy(item = line.item.copy(sortOrder = index))
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
