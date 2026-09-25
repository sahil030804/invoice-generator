package com.kjbilling.app.ui.settings

import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.kjbilling.app.data.backup.BackupManager
import com.kjbilling.app.data.prefs.ThemePreferences
import com.kjbilling.app.data.repository.AppSettingsRepository
import com.kjbilling.app.data.repository.BusinessProfileRepository
import com.kjbilling.app.data.storage.LogoStorage
import com.kjbilling.app.di.AppContainer
import com.kjbilling.app.domain.model.AppSettings
import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.domain.model.TaxType
import com.kjbilling.app.domain.model.ThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.io.File
import java.math.BigDecimal

data class SettingsState(
    val profile: BusinessProfile? = null,
    val settings: AppSettings? = null,
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val businessProfileRepository: BusinessProfileRepository,
    private val appSettingsRepository: AppSettingsRepository,
    private val logoStorage: LogoStorage,
    private val backupManager: BackupManager,
    private val themePreferences: ThemePreferences
) : ViewModel() {

    val themeMode: StateFlow<ThemeMode> = themePreferences.mode

    fun setThemeMode(mode: ThemeMode) {
        themePreferences.setMode(mode)
    }

    private val _lastBackupAt = MutableStateFlow(backupManager.lastBackupAt())
    val lastBackupAt: StateFlow<Long?> = _lastBackupAt.asStateFlow()

    private val _isBackupBusy = MutableStateFlow(false)
    val isBackupBusy: StateFlow<Boolean> = _isBackupBusy.asStateFlow()

    private val _backupMessage = MutableStateFlow<String?>(null)
    val backupMessage: StateFlow<String?> = _backupMessage.asStateFlow()

    fun clearBackupMessage() {
        _backupMessage.value = null
    }

    /** Creates a backup zip and hands it to [onReady] (the screen opens the share sheet). */
    fun backupNow(onReady: (File) -> Unit) {
        if (_isBackupBusy.value) {
            return
        }

        viewModelScope.launch {
            _isBackupBusy.value = true
            backupManager.createBackup()
                .onSuccess {
                    _lastBackupAt.value = backupManager.lastBackupAt()
                    onReady(it)
                }
                .onFailure {
                    Log.e(TAG, "Backup failed", it)
                    _backupMessage.value = "Backup failed: ${it.message ?: "unknown error"}"
                }
            _isBackupBusy.value = false
        }
    }

    /** Restores from [uri]; [onRestored] must restart the app. */
    fun restoreFrom(uri: Uri, onRestored: () -> Unit) {
        if (_isBackupBusy.value) {
            return
        }

        viewModelScope.launch {
            _isBackupBusy.value = true
            backupManager.restore(uri)
                .onSuccess { onRestored() }
                .onFailure {
                    Log.e(TAG, "Restore failed", it)
                    _backupMessage.value = "Restore failed: ${it.message ?: "unknown error"}. Your data was not changed."
                }
            _isBackupBusy.value = false
        }
    }

    /** Bumped whenever the logo changes; read [logoFile] for the current file. */
    val logoVersion: StateFlow<Long> = logoStorage.version

    private val _isLogoBusy = MutableStateFlow(false)
    val isLogoBusy: StateFlow<Boolean> = _isLogoBusy.asStateFlow()

    private val _logoError = MutableStateFlow<String?>(null)
    val logoError: StateFlow<String?> = _logoError.asStateFlow()

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

    fun logoFile() = logoStorage.logoFile()

    fun uploadLogo(uri: Uri) {
        if (_isLogoBusy.value) {
            return
        }

        viewModelScope.launch {
            _isLogoBusy.value = true
            _logoError.value = null
            logoStorage.saveLogo(uri).onFailure {
                Log.e(TAG, "Logo upload failed", it)
                _logoError.value = "Could not use this image. Please pick a PNG or JPG."
            }
            _isLogoBusy.value = false
        }
    }

    fun removeLogo() {
        if (_isLogoBusy.value) {
            return
        }

        viewModelScope.launch {
            _isLogoBusy.value = true
            _logoError.value = null
            logoStorage.removeLogo()
            _isLogoBusy.value = false
        }
    }

    fun clearLogoError() {
        _logoError.value = null
    }

    companion object {
        private const val TAG = "SettingsViewModel"

        fun factory(container: AppContainer) = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SettingsViewModel(
                    container.businessProfileRepository,
                    container.appSettingsRepository,
                    container.logoStorage,
                    container.backupManager,
                    container.themePreferences
                ) as T
            }
        }
    }
}
