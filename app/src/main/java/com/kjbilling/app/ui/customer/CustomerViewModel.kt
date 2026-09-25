package com.kjbilling.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.data.repository.CustomerRepository
import com.kjbilling.app.data.repository.InvoiceRepository
import com.kjbilling.app.domain.khata.KhataCalculator
import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

enum class CustomerFilter { ALL, WITH_DUES }

class CustomerViewModel(
    private val repository: CustomerRepository,
    invoiceRepository: InvoiceRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _filter = MutableStateFlow(CustomerFilter.ALL)
    val filter: StateFlow<CustomerFilter> = _filter

    /** Udhaar balance per customer id (only customers who owe something). */
    val dues: StateFlow<Map<Long, BigDecimal>> = combine(invoiceRepository.getAll(), repository.getAll()) { invoices, customers ->
        KhataCalculator.summarize(invoices, customers).customers.associate { it.customerId to it.due }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val customers: StateFlow<List<Customer>> = combine(
        repository.getAll(),
        _searchQuery,
        _filter,
        dues
    ) { customers, query, filter, dues ->
        customers
            .filter { filter == CustomerFilter.ALL || dues.containsKey(it.id) }
            .filter {
                query.isBlank() ||
                    it.name.contains(query, ignoreCase = true) ||
                    (it.businessName?.contains(query, ignoreCase = true) == true) ||
                    (it.mobile?.contains(query, ignoreCase = true) == true)
            }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun setFilter(filter: CustomerFilter) {
        _filter.value = filter
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun saveCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.save(customer)
        }
    }

    fun deleteCustomer(customer: Customer) {
        viewModelScope.launch {
            repository.delete(customer)
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return CustomerViewModel(container.customerRepository, container.invoiceRepository) as T
            }
        }
    }
}
