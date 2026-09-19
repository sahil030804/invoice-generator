package com.kjbilling.app.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.kjbilling.app.domain.model.AppSettings
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.domain.model.TaxType
import java.math.BigDecimal

@Entity(tableName = "app_settings")
data class AppSettingsEntity(
    @PrimaryKey
    val id: Long = 1,
    val gstEnabled: Boolean,
    val defaultGstRate: BigDecimal,
    val defaultTaxType: TaxType,
    val invoicePrefix: String,
    val nextInvoiceNumber: Long,
    val onboardingCompleted: Boolean,
    val defaultPaymentStatus: PaymentStatus
) {
    fun toDomain(): AppSettings {
        return AppSettings(
            id = id,
            gstEnabled = gstEnabled,
            defaultGstRate = defaultGstRate,
            defaultTaxType = defaultTaxType,
            invoicePrefix = invoicePrefix,
            nextInvoiceNumber = nextInvoiceNumber,
            onboardingCompleted = onboardingCompleted,
            defaultPaymentStatus = defaultPaymentStatus
        )
    }

    companion object {
        fun fromDomain(domain: AppSettings): AppSettingsEntity {
            return AppSettingsEntity(
                id = domain.id.takeIf { it != 0L } ?: 1L,
                gstEnabled = domain.gstEnabled,
                defaultGstRate = domain.defaultGstRate,
                defaultTaxType = domain.defaultTaxType,
                invoicePrefix = domain.invoicePrefix,
                nextInvoiceNumber = domain.nextInvoiceNumber,
                onboardingCompleted = domain.onboardingCompleted,
                defaultPaymentStatus = domain.defaultPaymentStatus
            )
        }
    }
}
