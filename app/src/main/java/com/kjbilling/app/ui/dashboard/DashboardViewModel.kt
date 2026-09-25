package com.kjbilling.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.data.backup.BackupManager
import com.kjbilling.app.data.repository.CustomerRepository
import com.kjbilling.app.data.repository.InvoiceRepository
import com.kjbilling.app.di.AppContainer
import com.kjbilling.app.domain.insights.BackupStatus
import com.kjbilling.app.domain.insights.BackupStatusText
import com.kjbilling.app.domain.insights.DashboardInsights
import com.kjbilling.app.domain.insights.InsightsResult
import com.kjbilling.app.domain.khata.KhataCalculator
import com.kjbilling.app.domain.model.Invoice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal
import java.time.ZoneId

private const val RECENT_COUNT = 10
private const val STOP_TIMEOUT_MS = 5000L
private const val WEEK_DAYS = 6L

data class DashboardState(
    val recentInvoices: List<Invoice> = emptyList(),
    val todaysSales: BigDecimal = BigDecimal.ZERO,
    val pendingAmount: BigDecimal = BigDecimal.ZERO,
    val insights: InsightsResult? = null,
    val customersOwing: Int = 0,
    val backup: BackupStatusText = BackupStatusText("", false),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    invoiceRepository: InvoiceRepository,
    customerRepository: CustomerRepository,
    private val backupManager: BackupManager
) : ViewModel() {

    // Bumped when a backup is made so the tile text refreshes (last backup lives in prefs, not a Flow).
    private val backupTick = MutableStateFlow(0)

    val state: StateFlow<DashboardState> = combine(
        invoiceRepository.getAll(),
        invoiceRepository.observeSoldItemsSince(weekStart()),
        customerRepository.getAll(),
        backupTick
    ) { invoices, soldItems, customers, _ ->
        val zone = ZoneId.systemDefault()
        val now = System.currentTimeMillis()
        val insights = DashboardInsights.compute(invoices, soldItems, now, zone)
        DashboardState(
            recentInvoices = invoices.sortedByDescending { it.createdAt }.take(RECENT_COUNT),
            todaysSales = insights.todaysSales,
            pendingAmount = insights.pending,
            insights = insights,
            customersOwing = KhataCalculator.summarize(invoices, customers).customers.size,
            backup = BackupStatus.describe(backupManager.lastBackupAt(), now, hasData = invoices.isNotEmpty(), zone = zone),
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS),
        initialValue = DashboardState(isLoading = true)
    )

    /** Re-reads the backup status (e.g. after returning from Settings). */
    fun refreshBackupStatus() {
        backupTick.value += 1
    }

    /** Creates a backup zip and hands it to [onReady] for sharing. */
    fun backupNow(onReady: (File) -> Unit) {
        viewModelScope.launch {
            backupManager.createBackup().onSuccess {
                refreshBackupStatus()
                onReady(it)
            }
        }
    }

    private fun weekStart(): Long {
        val zone = ZoneId.systemDefault()
        return java.time.LocalDate.now(zone).minusDays(WEEK_DAYS).atStartOfDay(zone).toInstant().toEpochMilli()
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return DashboardViewModel(container.invoiceRepository, container.customerRepository, container.backupManager) as T
            }
        }
    }
}
