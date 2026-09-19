package com.kjbilling.app.data.repository

import com.kjbilling.app.data.db.dao.AppSettingsDao
import com.kjbilling.app.data.db.entity.AppSettingsEntity
import com.kjbilling.app.domain.model.AppSettings
import com.kjbilling.app.domain.model.TaxType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.math.BigDecimal

class AppSettingsRepository(private val dao: AppSettingsDao) {

    fun getSettings(): Flow<AppSettings> {
        return dao.getSettings().map { 
            it?.toDomain() ?: AppSettings(
                id = 1,
                gstEnabled = true,
                defaultGstRate = BigDecimal("18"),
                defaultTaxType = TaxType.CGST_SGST,
                invoicePrefix = "INV-",
                nextInvoiceNumber = 1,
                onboardingCompleted = false,
                defaultPaymentStatus = com.kjbilling.app.domain.model.PaymentStatus.UNPAID
            )
        }
    }

    suspend fun getSettingsOnce(): AppSettings {
        return dao.getSettingsOnce()?.toDomain() ?: AppSettings(
            id = 1,
            gstEnabled = true,
            defaultGstRate = BigDecimal("18"),
            defaultTaxType = TaxType.CGST_SGST,
            invoicePrefix = "INV-",
            nextInvoiceNumber = 1,
            onboardingCompleted = false,
            defaultPaymentStatus = com.kjbilling.app.domain.model.PaymentStatus.UNPAID
        )
    }

    suspend fun saveSettings(settings: AppSettings) {
        dao.upsert(AppSettingsEntity.fromDomain(settings))
    }

    suspend fun completeOnboarding() {
        dao.updateOnboardingCompleted(true)
    }

    suspend fun updateGstSettings(enabled: Boolean, rate: BigDecimal, taxType: TaxType) {
        dao.updateGstSettings(enabled, rate.toString(), taxType)
    }
}
