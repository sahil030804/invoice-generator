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
    val invoiceRepository by lazy { InvoiceRepository(database.invoiceDao(), database.appSettingsDao()) }
    val appSettingsRepository by lazy { AppSettingsRepository(database.appSettingsDao()) }
    
    val invoiceCalculator by lazy { InvoiceCalculator() }
    val invoicePdfGenerator by lazy { com.kjbilling.app.pdf.InvoicePdfGenerator(context) }
    val invoiceShareHelper = com.kjbilling.app.pdf.InvoiceShareHelper
}
