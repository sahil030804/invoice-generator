package com.kjbilling.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.data.repository.AppSettingsRepository
import com.kjbilling.app.data.repository.BusinessProfileRepository
import com.kjbilling.app.di.AppContainer
import com.kjbilling.app.domain.model.AppSettings
import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.domain.model.TaxType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.math.BigDecimal

data class OnboardingState(
    val currentStep: Int = 1,
    val businessName: String = "",
    val businessNameError: String? = null,
    val ownerName: String = "",
    val ownerNameError: String? = null,
    val mobile: String = "",
    val mobileError: String? = null,
    val address: String = "",
    val state: String = "",
    val isGstRegistered: Boolean = false,
    val gstin: String = "",
    val defaultGstRate: BigDecimal = BigDecimal.ZERO,
    val isComplete: Boolean = false
)

class OnboardingViewModel(
    private val businessProfileRepository: BusinessProfileRepository,
    private val appSettingsRepository: AppSettingsRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun updateBusinessName(name: String) {
        _state.update { it.copy(businessName = name, businessNameError = null) }
    }

    fun updateOwnerName(name: String) {
        _state.update { it.copy(ownerName = name, ownerNameError = null) }
    }

    fun updateMobile(mobile: String) {
        if (mobile.all { it.isDigit() } && mobile.length <= 10) {
            _state.update { it.copy(mobile = mobile, mobileError = null) }
        }
    }

    fun updateAddress(address: String) {
        _state.update { it.copy(address = address) }
    }

    fun updateState(state: String) {
        _state.update { it.copy(state = state) }
    }

    fun updateGstRegistered(isRegistered: Boolean) {
        _state.update { it.copy(isGstRegistered = isRegistered) }
    }

    fun updateGstin(gstin: String) {
        _state.update { it.copy(gstin = gstin) }
    }

    fun updateDefaultGstRate(rate: BigDecimal) {
        _state.update { it.copy(defaultGstRate = rate) }
    }

    fun previousStep() {
        if (_state.value.currentStep > 1) {
            _state.update { it.copy(currentStep = it.currentStep - 1) }
        }
    }

    fun nextStep() {
        val currentState = _state.value
        when (currentState.currentStep) {
            1 -> {
                val isBusinessNameValid = currentState.businessName.isNotBlank()
                val isOwnerNameValid = currentState.ownerName.isNotBlank()
                val isMobileValid = currentState.mobile.length == 10
                
                _state.update {
                    it.copy(
                        businessNameError = if (isBusinessNameValid) null else "Business Name is required",
                        ownerNameError = if (isOwnerNameValid) null else "Owner Name is required",
                        mobileError = if (isMobileValid) null else "Valid 10-digit mobile required",
                    )
                }

                if (isBusinessNameValid && isOwnerNameValid && isMobileValid) {
                    _state.update { it.copy(currentStep = 2) }
                }
            }
            2 -> {
                _state.update { it.copy(currentStep = 3) }
            }
        }
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            val currentState = _state.value
            val profile = BusinessProfile(
                businessName = currentState.businessName,
                ownerName = currentState.ownerName,
                mobile = currentState.mobile,
                address = currentState.address,
                state = currentState.state,
                gstin = if (currentState.isGstRegistered) currentState.gstin.takeIf { it.isNotBlank() } else null,
                email = "",
                city = "",
                pincode = ""
            )
            businessProfileRepository.saveProfile(profile)

            val settings = AppSettings(
                invoicePrefix = "INV",
                nextInvoiceNumber = 1,
                gstEnabled = currentState.isGstRegistered,
                defaultGstRate = currentState.defaultGstRate,
                defaultTaxType = if (currentState.isGstRegistered) TaxType.CGST_SGST else TaxType.NO_GST,
                onboardingCompleted = true
            )
            appSettingsRepository.saveSettings(settings)
            
            _state.update { it.copy(isComplete = true) }
        }
    }

    companion object {
        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OnboardingViewModel(
                    container.businessProfileRepository,
                    container.appSettingsRepository
                ) as T
            }
        }
    }
}
