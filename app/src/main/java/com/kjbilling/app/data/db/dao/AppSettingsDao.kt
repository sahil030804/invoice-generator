package com.kjbilling.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.kjbilling.app.data.db.entity.AppSettingsEntity
import com.kjbilling.app.domain.model.TaxType
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(settings: AppSettingsEntity)

    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettings(): Flow<AppSettingsEntity?>

    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettingsOnce(): AppSettingsEntity?

    @Query("UPDATE app_settings SET onboardingCompleted = :completed WHERE id = 1")
    suspend fun updateOnboardingCompleted(completed: Boolean)

    @Query("UPDATE app_settings SET gstEnabled = :enabled, defaultGstRate = :rate, defaultTaxType = :taxType WHERE id = 1")
    suspend fun updateGstSettings(enabled: Boolean, rate: String, taxType: TaxType)

    @Query("UPDATE app_settings SET invoicePrefix = :prefix, nextInvoiceNumber = :nextNumber WHERE id = 1")
    suspend fun updateInvoicePrefixAndNumber(prefix: String, nextNumber: Long)

    @Transaction
    suspend fun generateNextInvoiceNumber(prefix: String): String {
        val settings = getSettingsOnce()
        val nextNumber = settings?.nextInvoiceNumber ?: 1L
        
        val formattedNumber = "$prefix${nextNumber.toString().padStart(4, '0')}"
        
        updateInvoicePrefixAndNumber(prefix, nextNumber + 1)
        
        return formattedNumber
    }
}
