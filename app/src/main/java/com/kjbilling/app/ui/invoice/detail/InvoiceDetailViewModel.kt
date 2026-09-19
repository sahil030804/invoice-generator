package com.kjbilling.app.ui.invoice.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.data.repository.BusinessProfileRepository
import com.kjbilling.app.data.repository.InvoiceRepository
import com.kjbilling.app.di.AppContainer
import com.kjbilling.app.domain.calculator.InvoiceCalculator
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.pdf.InvoicePdfGenerator
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal

class InvoiceDetailViewModel(
    private val invoiceRepository: InvoiceRepository,
    private val businessProfileRepository: BusinessProfileRepository,
    private val invoiceCalculator: InvoiceCalculator,
    private val pdfGenerator: InvoicePdfGenerator
) : ViewModel() {

    private val _invoice = MutableStateFlow<Invoice?>(null)
    val invoice: StateFlow<Invoice?> = _invoice

    private val _generatedFile = MutableStateFlow<File?>(null)
    val generatedFile: StateFlow<File?> = _generatedFile

    private val _downloadEvent = MutableSharedFlow<File>()
    val downloadEvent: SharedFlow<File> = _downloadEvent

    private val _isDownloading = MutableStateFlow(false)
    val isDownloading: StateFlow<Boolean> = _isDownloading

    fun loadInvoice(invoiceId: Long) {
        viewModelScope.launch {
            _invoice.value = invoiceRepository.getById(invoiceId)
        }
    }

    fun markAsPaid() {
        viewModelScope.launch {
            val current = _invoice.value ?: return@launch
            if (current.status == InvoiceStatus.CANCELLED) {
                return@launch
            }
            recordPayment(current.grandTotal, current.paymentMethod ?: PaymentMethod.CASH)
        }
    }

    fun recordPayment(amount: BigDecimal, method: PaymentMethod?) {
        viewModelScope.launch {
            val current = _invoice.value ?: return@launch
            if (current.status == InvoiceStatus.CANCELLED) {
                return@launch
            }
            if (amount <= BigDecimal.ZERO || amount > current.balanceDue) {
                return@launch
            }
            // Payments accumulate; never exceed the grand total.
            val newAmountPaid = (current.amountPaid + amount).min(current.grandTotal)
            val newStatus = if (newAmountPaid >= current.grandTotal) {
                PaymentStatus.PAID
            } else {
                PaymentStatus.PARTIAL
            }
            val newInvoiceStatus = if (newStatus == PaymentStatus.PAID) {
                InvoiceStatus.PAID
            } else if (current.status == InvoiceStatus.PAID) {
                InvoiceStatus.GENERATED
            } else {
                current.status
            }
            invoiceRepository.updatePayment(current.id, newStatus, method, newAmountPaid)
            if (newInvoiceStatus != current.status) {
                invoiceRepository.updateStatus(current.id, newInvoiceStatus)
            }
            _invoice.value = current.copy(
                paymentStatus = newStatus,
                paymentMethod = method,
                amountPaid = newAmountPaid,
                status = newInvoiceStatus
            )
        }
    }

    fun cancelInvoice() {
        viewModelScope.launch {
            val current = _invoice.value ?: return@launch
            if (current.status == InvoiceStatus.CANCELLED || current.status == InvoiceStatus.PAID) {
                return@launch
            }
            invoiceRepository.updateStatus(current.id, InvoiceStatus.CANCELLED)
            _invoice.value = current.copy(status = InvoiceStatus.CANCELLED)
        }
    }

    fun generatePdfForSharing() {
        viewModelScope.launch {
            val currentInvoice = _invoice.value ?: return@launch
            val bProfile = businessProfileRepository.getProfileOnce() ?: return@launch
            
            // Recalculate just for tax breakdown
            val mappedItems = currentInvoice.items.map { item ->
                com.kjbilling.app.domain.calculator.InvoiceItemCalculation(
                    itemAmount = item.quantity.multiply(item.unitPrice),
                    discountAmount = item.discountAmount,
                    taxableAmount = item.taxableAmount,
                    cgstAmount = item.cgstAmount ?: java.math.BigDecimal.ZERO,
                    sgstAmount = item.sgstAmount ?: java.math.BigDecimal.ZERO,
                    igstAmount = item.igstAmount ?: java.math.BigDecimal.ZERO,
                    taxAmount = item.taxAmount,
                    total = item.total,
                    gstRate = item.gstRate,
                    taxType = currentInvoice.taxType
                )
            }
            val totals = invoiceCalculator.calculateInvoice(mappedItems)
            
            val result = pdfGenerator.generate(currentInvoice, bProfile, totals.taxBreakdown)
            if (result.isSuccess) {
                _generatedFile.value = result.getOrNull()
            }
        }
    }
    
    fun clearGeneratedFile() {
        _generatedFile.value = null
    }

    fun downloadPdf() {
        viewModelScope.launch {
            if (_isDownloading.value) return@launch
            val currentInvoice = _invoice.value ?: return@launch
            val bProfile = businessProfileRepository.getProfileOnce() ?: return@launch

            _isDownloading.value = true
            try {
                val mappedItems = currentInvoice.items.map { item ->
                    com.kjbilling.app.domain.calculator.InvoiceItemCalculation(
                        itemAmount = item.quantity.multiply(item.unitPrice),
                        discountAmount = item.discountAmount,
                        taxableAmount = item.taxableAmount,
                        cgstAmount = item.cgstAmount ?: java.math.BigDecimal.ZERO,
                        sgstAmount = item.sgstAmount ?: java.math.BigDecimal.ZERO,
                        igstAmount = item.igstAmount ?: java.math.BigDecimal.ZERO,
                        taxAmount = item.taxAmount,
                        total = item.total,
                        gstRate = item.gstRate,
                        taxType = currentInvoice.taxType
                    )
                }
                val totals = invoiceCalculator.calculateInvoice(mappedItems)

                val result = pdfGenerator.generate(currentInvoice, bProfile, totals.taxBreakdown)
                if (result.isSuccess) {
                    val file = result.getOrNull()
                    if (file != null) {
                        _downloadEvent.emit(file)
                    }
                }
            } finally {
                _isDownloading.value = false
            }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InvoiceDetailViewModel(
                    container.invoiceRepository,
                    container.businessProfileRepository,
                    container.invoiceCalculator,
                    container.invoicePdfGenerator
                ) as T
            }
        }
    }
}
