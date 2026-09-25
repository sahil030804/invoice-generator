package com.kjbilling.app.di

import android.content.Context
import com.kjbilling.app.data.db.AppDatabase
import com.kjbilling.app.data.repository.AppSettingsRepository
import com.kjbilling.app.data.repository.BusinessProfileRepository
import com.kjbilling.app.data.repository.CustomerRepository
import com.kjbilling.app.data.repository.InvoiceRepository
import com.kjbilling.app.data.repository.ProductRepository
import com.kjbilling.app.domain.calculator.InvoiceCalculator

class AppContainer(context: Context) {
    private val database: AppDatabase by lazy { AppDatabase.getInstance(context) }
    
    val businessProfileRepository by lazy { BusinessProfileRepository(database.businessProfileDao()) }
    val customerRepository by lazy { CustomerRepository(database.customerDao()) }
    val productRepository by lazy { ProductRepository(database.productDao()) }
    val invoiceRepository by lazy { InvoiceRepository(database.invoiceDao(), database.appSettingsDao(), database) }
    val appSettingsRepository by lazy { AppSettingsRepository(database.appSettingsDao()) }
    
    val invoiceCalculator by lazy { InvoiceCalculator() }
    val logoStorage by lazy { com.kjbilling.app.data.storage.LogoStorage(context) }
    val backupManager by lazy { com.kjbilling.app.data.backup.BackupManager(context, logoStorage) }
    val invoicePdfGenerator by lazy { com.kjbilling.app.pdf.InvoicePdfGenerator(context, logoStorage) }
    val invoiceShareHelper = com.kjbilling.app.pdf.InvoiceShareHelper
}
