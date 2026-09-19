package com.kjbilling.app.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.data.repository.AppSettingsRepository
import com.kjbilling.app.data.repository.BusinessProfileRepository
import com.kjbilling.app.di.AppContainer
import com.kjbilling.app.domain.model.AppSettings
import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.domain.model.TaxType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class SettingsState(
    val profile: BusinessProfile? = null,
    val settings: AppSettings? = null,
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val businessProfileRepository: BusinessProfileRepository,
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        businessProfileRepository.getProfile(),
        appSettingsRepository.getSettings()
    ) { profile, settings ->
        SettingsState(
            profile = profile,
            settings = settings,
            isLoading = false
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState(isLoading = true)
    )

    fun updateInvoicePrefix(prefix: String) {
        viewModelScope.launch {
            state.value.settings?.let {
                appSettingsRepository.saveSettings(it.copy(invoicePrefix = prefix))
            }
        }
    }

    fun updateStartingNumber(number: Long) {
        viewModelScope.launch {
            state.value.settings?.let {
                appSettingsRepository.saveSettings(it.copy(nextInvoiceNumber = number))
            }
        }
    }

    fun updateGstEnabled(enabled: Boolean) {
        viewModelScope.launch {
            state.value.settings?.let {
                appSettingsRepository.saveSettings(it.copy(gstEnabled = enabled))
            }
        }
    }

    fun updateDefaultGstRate(rate: BigDecimal) {
        viewModelScope.launch {
            state.value.settings?.let {
                appSettingsRepository.saveSettings(it.copy(defaultGstRate = rate))
            }
        }
    }

    fun updateDefaultTaxType(type: TaxType) {
        viewModelScope.launch {
            state.value.settings?.let {
                appSettingsRepository.saveSettings(it.copy(defaultTaxType = type))
            }
        }
    }

    fun saveBusinessProfile(profile: BusinessProfile) {
        viewModelScope.launch {
            businessProfileRepository.saveProfile(profile)
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    container.businessProfileRepository,
                    container.appSettingsRepository
                ) as T
            }
        }
    }
}
