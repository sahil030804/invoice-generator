package com.kjbilling.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
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
import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.domain.model.BusinessSnapshot
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
    version = AppDatabase.DB_VERSION,
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

        /**
         * Adds the seller snapshot columns to invoices and backfills every existing invoice
         * from the current business profile (best available data for pre-snapshot invoices).
         */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                SELLER_COLUMNS.forEach { db.execSQL("ALTER TABLE invoices ADD COLUMN $it TEXT") }

                val profile = db.query(
                    "SELECT businessName, ownerName, mobile, address, state, gstin, email, city, pincode " +
                        "FROM business_profiles WHERE id = 1"
                ).use { c ->
                    if (!c.moveToFirst()) {
                        return
                    }
                    BusinessProfile(
                        businessName = c.getString(0),
                        ownerName = c.getString(1),
                        mobile = c.getString(2),
                        address = c.getString(3),
                        state = c.getString(4),
                        gstin = c.getString(5),
                        email = c.getString(6),
                        city = c.getString(7),
                        pincode = c.getString(8)
                    )
                }

                val seller = BusinessSnapshot.from(profile)
                db.execSQL(
                    "UPDATE invoices SET sellerName = ?, sellerAddress = ?, sellerPhone = ?, " +
                        "sellerEmail = ?, sellerGstin = ?, sellerOwner = ?",
                    arrayOf<Any?>(seller.name, seller.address, seller.phone, seller.email, seller.gstin, seller.ownerName)
                )
            }
        }

        private val SELLER_COLUMNS = listOf(
            "sellerName", "sellerAddress", "sellerPhone", "sellerEmail", "sellerGstin", "sellerOwner"
        )

        const val DB_NAME = "kj_invoice_database"
        const val DB_VERSION = 2

        /** Closes and forgets the singleton (used by restore, right before the app restarts). */
        fun closeInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
            }
        }

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DB_NAME
                )
                    .addMigrations(MIGRATION_1_2)
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
