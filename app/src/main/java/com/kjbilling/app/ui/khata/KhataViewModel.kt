package com.kjbilling.app.ui.khata

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.data.repository.BusinessProfileRepository
import com.kjbilling.app.data.repository.CustomerRepository
import com.kjbilling.app.data.repository.InvoiceRepository
import com.kjbilling.app.di.AppContainer
import com.kjbilling.app.domain.khata.KhataCalculator
import com.kjbilling.app.domain.khata.KhataSummary
import com.kjbilling.app.domain.khata.ReminderMessage
import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.upi.UpiPayment
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

private const val STOP_TIMEOUT_MS = 5000L

/** Khata overview: who owes how much. */
class KhataViewModel(
    invoiceRepository: InvoiceRepository,
    customerRepository: CustomerRepository
) : ViewModel() {

    val summary: StateFlow<KhataSummary?> = combine(invoiceRepository.getAll(), customerRepository.getAll()) { invoices, customers ->
        KhataCalculator.summarize(invoices, customers)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), null)

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return KhataViewModel(container.invoiceRepository, container.customerRepository) as T
            }
        }
    }
}

data class CustomerKhataState(
    val isLoading: Boolean = true,
    val customer: Customer? = null,
    val customerName: String = "",
    val openBills: List<Invoice> = emptyList(),
    val totalDue: BigDecimal = BigDecimal.ZERO,
    val reminderText: String = ""
)

/** One customer's Khata: unpaid bills, money received, WhatsApp reminder. */
class CustomerKhataViewModel(
    private val customerId: Long,
    private val invoiceRepository: InvoiceRepository,
    customerRepository: CustomerRepository,
    businessProfileRepository: BusinessProfileRepository
) : ViewModel() {

    private val _message = MutableStateFlow<String?>(null)
    val message: StateFlow<String?> = _message.asStateFlow()

    val state: StateFlow<CustomerKhataState> = combine(
        invoiceRepository.observeByCustomer(customerId),
        customerRepository.getAll(),
        businessProfileRepository.getProfile()
    ) { invoices, customers, profile ->
        val customer = customers.firstOrNull { it.id == customerId }
        val open = KhataCalculator.openInvoices(invoices).sortedBy { it.invoiceDate }
        val due = KhataCalculator.dueFor(customerId, invoices)
        val name = customer?.name ?: invoices.firstOrNull()?.customerName.orEmpty()
        val upi = UpiPayment.normalizeVpa(profile?.upiId)?.takeIf { UpiPayment.isValidVpa(it) }
        CustomerKhataState(
            isLoading = false,
            customer = customer,
            customerName = name,
            openBills = open,
            totalDue = due,
            reminderText = ReminderMessage.build(name, due, open.size, profile?.businessName.orEmpty(), upi)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), CustomerKhataState())

    fun receive(amount: BigDecimal, method: PaymentMethod) {
        viewModelScope.launch {
            val result = invoiceRepository.receiveCustomerPayment(customerId, amount, method)
            if (result.unapplied.signum() > 0) {
                _message.value = "Extra ₹${result.unapplied.toPlainString()} was not applied (more than total due)."
            }
        }
    }

    fun clearMessage() {
        _message.value = null
    }

    companion object {
        fun factory(container: AppContainer, customerId: Long) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CustomerKhataViewModel(
                    customerId,
                    container.invoiceRepository,
                    container.customerRepository,
                    container.businessProfileRepository
                ) as T
            }
        }
    }
}
