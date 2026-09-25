package com.kjbilling.app.ui.upi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.data.repository.BusinessProfileRepository
import com.kjbilling.app.di.AppContainer
import com.kjbilling.app.domain.keypad.AmountInput
import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.domain.upi.UpiPayment
import com.kjbilling.app.domain.upi.UpiQrRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class UpiQrUiState(
    val isLoading: Boolean = true,
    val hasUpiId: Boolean = false,
    val amountText: String = "",
    val request: UpiQrRequest? = null
)

/** Standalone "type any amount → show QR" screen. */
class UpiQrViewModel(businessProfileRepository: BusinessProfileRepository) : ViewModel() {

    private val amountText = MutableStateFlow("")
    private val showQr = MutableStateFlow(false)
    private val profile = businessProfileRepository.getProfile()

    val state: StateFlow<UpiQrUiState> = combine(profile, amountText, showQr) { profile, text, show ->
        UpiQrUiState(
            isLoading = false,
            hasUpiId = hasValidUpi(profile),
            amountText = text,
            request = if (show) AmountInput.toAmount(text)?.let { UpiPayment.forAmount(it, profile) } else null
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), UpiQrUiState())

    val canShow: StateFlow<Boolean> = amountText.map { AmountInput.toAmount(it) != null }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(STOP_TIMEOUT_MS), false)

    fun onAmountChange(text: String) {
        amountText.value = text
    }

    fun showQr() {
        showQr.value = true
    }

    fun newAmount() {
        showQr.value = false
        amountText.value = ""
    }

    private fun hasValidUpi(profile: BusinessProfile?): Boolean {
        return UpiPayment.isValidVpa(UpiPayment.normalizeVpa(profile?.upiId))
    }

    companion object {
        private const val STOP_TIMEOUT_MS = 5000L

        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return UpiQrViewModel(container.businessProfileRepository) as T
            }
        }
    }
}
