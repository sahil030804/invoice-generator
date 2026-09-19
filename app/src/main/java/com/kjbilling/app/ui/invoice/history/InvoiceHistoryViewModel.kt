package com.kjbilling.app.ui.invoice.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.data.repository.InvoiceRepository
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.di.AppContainer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class InvoiceHistoryViewModel(private val repository: InvoiceRepository) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    val invoices: StateFlow<List<Invoice>> = combine(
        repository.getAll(),
        _searchQuery
    ) { invoices, query ->
        if (query.isBlank()) {
            invoices
        } else {
            invoices.filter {
                it.invoiceNumber.contains(query, ignoreCase = true) ||
                (it.customerName.contains(query, ignoreCase = true))
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

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return InvoiceHistoryViewModel(container.invoiceRepository) as T
            }
        }
    }
}
