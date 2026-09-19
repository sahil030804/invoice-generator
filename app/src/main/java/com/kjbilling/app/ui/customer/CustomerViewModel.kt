package com.kjbilling.app.ui.customer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.KJInvoiceApp
import com.kjbilling.app.data.repository.CustomerRepository
import com.kjbilling.app.domain.model.Customer
import com.kjbilling.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CustomerViewModel(private val repository: CustomerRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val customers: StateFlow<List<Customer>> = combine(
        repository.getAll(),
        _searchQuery
    ) { customers, query ->
        if (query.isBlank()) {
            customers
        } else {
            customers.filter {
                it.name.contains(query, ignoreCase = true) ||
                (it.businessName?.contains(query, ignoreCase = true) == true) ||
                (it.mobile?.contains(query, ignoreCase = true) == true)
            }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

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
                return CustomerViewModel(container.customerRepository) as T
            }
        }
    }
}
