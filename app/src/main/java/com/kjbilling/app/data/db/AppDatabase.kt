package com.kjbilling.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.kjbilling.app.data.db.converter.Converters
import com.kjbilling.app.data.db.dao.AppSettingsDao
import com.kjbilling.app.data.db.dao.BusinessProfileDao
import com.kjbilling.app.data.db.dao.CustomerDao
import com.kjbilling.app.data.db.dao.InvoiceDao
import com.kjbilling.app.data.db.dao.ProductDao
import com.kjbilling.app.data.db.entity.AppSettingsEntity
import com.kjbilling.app.data.db.entity.BusinessProfileEntity
import com.kjbilling.app.data.db.entity.CustomerEntity
import com.kjbilling.app.data.db.entity.InvoiceEntity
import com.kjbilling.app.data.db.entity.InvoiceItemEntity
import com.kjbilling.app.data.db.entity.ProductEntity
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.domain.model.TaxType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Database(
    entities = [
        BusinessProfileEntity::class,
        CustomerEntity::class,
        ProductEntity::class,
        InvoiceEntity::class,
        InvoiceItemEntity::class,
        AppSettingsEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun businessProfileDao(): BusinessProfileDao
    abstract fun customerDao(): CustomerDao
    abstract fun productDao(): ProductDao
    abstract fun invoiceDao(): InvoiceDao
    abstract fun appSettingsDao(): AppSettingsDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "kj_invoice_database"
                )
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class DatabaseCallback : RoomDatabase.Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                CoroutineScope(Dispatchers.IO).launch {
                    val settingsDao = database.appSettingsDao()
                    settingsDao.upsert(
                        AppSettingsEntity(
                            id = 1,
                            gstEnabled = true,
                            defaultGstRate = BigDecimal("18.00"),
                            defaultTaxType = TaxType.CGST_SGST,
                            invoicePrefix = "INV-",
                            nextInvoiceNumber = 1,
                            onboardingCompleted = false,
                            defaultPaymentStatus = PaymentStatus.UNPAID
                        )
                    )

                    val customerDao = database.customerDao()
                    val timestamp = System.currentTimeMillis()
                    customerDao.insert(
                        CustomerEntity(
                            name = "Walk-in Customer",
                            mobile = null,
                            email = null,
                            billingAddress = null,
                            state = null,
                            pincode = null,
                            gstin = null,
                            businessName = null,
                            notes = null,
                            isWalkIn = true,
                            lastUsedAt = timestamp,
                            createdAt = timestamp,
                            updatedAt = timestamp
                        )
                    )
                }
            }
        }
    }
}
