package com.kjbilling.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.data.repository.InvoiceRepository
import com.kjbilling.app.di.AppContainer
import com.kjbilling.app.domain.model.Invoice
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentStatus
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.math.BigDecimal
import java.time.LocalDate

data class DashboardState(
    val recentInvoices: List<Invoice> = emptyList(),
    val todaysSales: BigDecimal = BigDecimal.ZERO,
    val pendingAmount: BigDecimal = BigDecimal.ZERO,
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val invoiceRepository: InvoiceRepository
) : ViewModel() {

    val state: StateFlow<DashboardState> = combine(
        invoiceRepository.getAll(),
        invoiceRepository.getAll(),
        invoiceRepository.getAll()
    ) { recent, todays, pending ->
        val today = LocalDate.now()

        val todaysSales = todays
            .filter {
                it.status != InvoiceStatus.CANCELLED &&
                    java.time.Instant.ofEpochMilli(it.invoiceDate)
                        .atZone(java.time.ZoneId.systemDefault()).toLocalDate() == today
            }
            .fold(BigDecimal.ZERO) { acc, it -> acc.add(it.grandTotal) }

        val pendingAmount = pending
            .filter { it.paymentStatus != PaymentStatus.PAID && it.status != InvoiceStatus.CANCELLED }
            .fold(BigDecimal.ZERO) { acc, it -> acc.add(it.balanceDue) }
            
        val recentTop10 = recent.sortedByDescending { it.createdAt }.take(10)

        DashboardState(
            recentInvoices = recentTop10,
            todaysSales = todaysSales,
            pendingAmount = pendingAmount,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardState(isLoading = true)
    )

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(container.invoiceRepository) as T
            }
        }
    }
}
