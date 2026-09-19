package com.kjbilling.app

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.kjbilling.app.data.db.AppDatabase
import com.kjbilling.app.data.db.entity.AppSettingsEntity
import com.kjbilling.app.data.db.entity.BusinessProfileEntity
import com.kjbilling.app.data.repository.InvoiceRepository
import com.kjbilling.app.domain.calculator.InvoiceCalculator
import com.kjbilling.app.domain.model.*
import com.kjbilling.app.pdf.InvoicePdfGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.math.BigDecimal

@RunWith(AndroidJUnit4::class)
class ElectricShopSeedAndGenerateTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val app: KJInvoiceApp get() = context as KJInvoiceApp

    @Test
    fun seedElectricShopDataAndGenerateInvoices() = runBlocking {
        val container = app.container
        val db = AppDatabase.getInstance(context)

        withContext(Dispatchers.IO) {
            // Clean slate for fresh seeding
            db.clearAllTables()
            File(context.filesDir, "invoices").deleteRecursively()

            // 1. Setup Business Profile & Settings
            val now = System.currentTimeMillis()
            db.businessProfileDao().upsert(
                BusinessProfileEntity(
                    id = 1L,
                    businessName = "Prime Electrical & Electronics",
                    ownerName = "Sahil",
                    mobile = "9876543210",
                    address = "Shop 12, Electric Market, MG Road",
                    city = "Pune",
                    state = "Maharashtra",
                    pincode = "411001",
                    gstin = "27AAPFU0939F1ZV",
                    email = "contact@primeelectrical.com",
                    createdAt = now,
                    updatedAt = now
                )
            )

            db.appSettingsDao().upsert(
                AppSettingsEntity(
                    id = 1L,
                    gstEnabled = true,
                    defaultGstRate = BigDecimal("18.00"),
                    defaultTaxType = TaxType.CGST_SGST,
                    invoicePrefix = "INV-",
                    nextInvoiceNumber = 4L,
                    onboardingCompleted = true,
                    defaultPaymentStatus = PaymentStatus.UNPAID
                )
            )

            val bProfile = container.businessProfileRepository.getProfileOnce() ?: BusinessProfile(
                id = 1L,
                businessName = "Prime Electrical & Electronics",
                ownerName = "Sahil",
                mobile = "9876543210",
                address = "Shop 12, Electric Market, MG Road",
                city = "Pune",
                state = "Maharashtra",
                pincode = "411001",
                gstin = "27AAPFU0939F1ZV"
            )

            // 2. Add 10 Electric Shop Products
            val productRepo = container.productRepository
            val products = listOf(
                Product(name = "Havells 9W LED Bulb B22 Cool Day Light", sellingPrice = BigDecimal("110.00"), unit = "PCS", gstRate = BigDecimal("18.00"), hsnCode = "8539"),
                Product(name = "Polycab 1.5 sq mm Single Core Copper Wire 90m", sellingPrice = BigDecimal("1850.00"), unit = "ROLL", gstRate = BigDecimal("18.00"), hsnCode = "8544"),
                Product(name = "Anchor Roma 6A 1-Way Modular Switch", sellingPrice = BigDecimal("32.00"), unit = "PCS", gstRate = BigDecimal("18.00"), hsnCode = "8536"),
                Product(name = "Anchor Roma 16A 3-Pin Modular Socket", sellingPrice = BigDecimal("85.00"), unit = "PCS", gstRate = BigDecimal("18.00"), hsnCode = "8536"),
                Product(name = "Crompton Aura 1200mm High-Speed Ceiling Fan White", sellingPrice = BigDecimal("2350.00"), unit = "PCS", gstRate = BigDecimal("18.00"), hsnCode = "8414"),
                Product(name = "Schneider Acti9 16A Single Pole MCB C-Curve", sellingPrice = BigDecimal("195.00"), unit = "PCS", gstRate = BigDecimal("18.00"), hsnCode = "8536"),
                Product(name = "Philips 20W LED Batten Tube Light 4ft", sellingPrice = BigDecimal("340.00"), unit = "PCS", gstRate = BigDecimal("18.00"), hsnCode = "8539"),
                Product(name = "Goldmedal 4-Way Modular Gang Box Surface", sellingPrice = BigDecimal("125.00"), unit = "PCS", gstRate = BigDecimal("18.00"), hsnCode = "8538"),
                Product(name = "Finolex 2.5 sq mm Flame Retardant Wire 90m", sellingPrice = BigDecimal("2950.00"), unit = "ROLL", gstRate = BigDecimal("18.00"), hsnCode = "8544"),
                Product(name = "Syska 15W Round LED Concealed Downlight 6500K", sellingPrice = BigDecimal("275.00"), unit = "PCS", gstRate = BigDecimal("18.00"), hsnCode = "8539")
            )

            val savedProductIds = products.map { productRepo.save(it) }
            assertEquals("Expected 10 products to be saved", 10, savedProductIds.size)

            // 3. Add 3 B2B Customers
            val customerRepo = container.customerRepository
            val customers = listOf(
                Customer(
                    name = "Rakesh Sharma",
                    businessName = "Sharma Electricals & Hardware",
                    mobile = "9822011223",
                    billingAddress = "Shop 14, Market Yard, Pune",
                    state = "Maharashtra",
                    gstin = "27AABCS1429B1ZX"
                ),
                Customer(
                    name = "Vikram Mehta",
                    businessName = "Apex Infra Projects Pvt Ltd",
                    mobile = "9898012345",
                    billingAddress = "102 Crystal Tower, Ring Road, Surat",
                    state = "Gujarat",
                    gstin = "24AAACA9876C1Z5"
                ),
                Customer(
                    name = "Priya Nair",
                    businessName = "Elite Interior & Lighting Studio",
                    mobile = "9765432190",
                    billingAddress = "Plot 88, Baner Road, Pune",
                    state = "Maharashtra",
                    gstin = "27AACCE5544D1Z2"
                )
            )

            val savedCustomerIds = customers.map { customerRepo.save(it) }
            assertEquals("Expected 3 customers to be saved", 3, savedCustomerIds.size)

            // 4. Create 3 Invoices
            val invoiceRepo = container.invoiceRepository
            val calc = InvoiceCalculator()
            val pdfGen = container.invoicePdfGenerator

            // --- Invoice 1: 3 products for Sharma Electricals (Intra-state: CGST + SGST) ---
            val c1 = customerRepo.getById(savedCustomerIds[0])!!
            val inv1ItemsSpec = listOf(
                Triple(products[0], "20", "5"),  // Havells 9W LED, Qty 20, 5% disc
                Triple(products[2], "50", "0"),  // Anchor 6A Switch, Qty 50
                Triple(products[3], "25", "2")   // Anchor 16A Socket, Qty 25, 2% disc
            )
            val inv1File = createAndSaveInvoice(
                invoiceRepo = invoiceRepo,
                calc = calc,
                pdfGen = pdfGen,
                bProfile = bProfile,
                invoiceNumber = "INV-0001",
                customer = c1,
                taxType = TaxType.CGST_SGST,
                paymentStatus = PaymentStatus.PAID,
                paymentMethod = PaymentMethod.UPI,
                amountPaid = null,
                itemsSpec = inv1ItemsSpec,
                notes = "Thank you for your business! Standard 1-year replacement warranty on all LED items."
            )
            copyToPublicDownloads(inv1File, "INV-0001_Sharma_Electricals.pdf")

            // --- Invoice 2: 7 products for Apex Infra Projects (Inter-state: IGST) ---
            val c2 = customerRepo.getById(savedCustomerIds[1])!!
            val inv2ItemsSpec = listOf(
                Triple(products[1], "6", "5"),   // Polycab 1.5mm wire, Qty 6, 5% disc
                Triple(products[8], "4", "5"),   // Finolex 2.5mm wire, Qty 4, 5% disc
                Triple(products[4], "8", "8"),   // Crompton 1200mm fan, Qty 8, 8% disc
                Triple(products[5], "15", "0"),  // Schneider 16A MCB, Qty 15
                Triple(products[6], "12", "5"),  // Philips 20W LED batten, Qty 12, 5% disc
                Triple(products[9], "24", "10"), // Syska 15W downlight, Qty 24, 10% disc
                Triple(products[7], "10", "0")   // Goldmedal gang box, Qty 10
            )
            val inv2File = createAndSaveInvoice(
                invoiceRepo = invoiceRepo,
                calc = calc,
                pdfGen = pdfGen,
                bProfile = bProfile,
                invoiceNumber = "INV-0002",
                customer = c2,
                taxType = TaxType.IGST,
                paymentStatus = PaymentStatus.PARTIAL,
                paymentMethod = PaymentMethod.BANK_TRANSFER,
                amountPaid = BigDecimal("30000.00"),
                itemsSpec = inv2ItemsSpec,
                notes = "Inter-state supply for project site. 50% advance received, balance on site inspection."
            )
            copyToPublicDownloads(inv2File, "INV-0002_Apex_Infra_Projects.pdf")

            // --- Invoice 3: 5 products for Elite Interior & Lighting Studio (Intra-state: CGST + SGST) ---
            val c3 = customerRepo.getById(savedCustomerIds[2])!!
            val inv3ItemsSpec = listOf(
                Triple(products[9], "16", "5"),  // Syska 15W downlight, Qty 16, 5% disc
                Triple(products[6], "8", "0"),   // Philips 20W batten, Qty 8
                Triple(products[1], "2", "0"),   // Polycab 1.5mm wire, Qty 2
                Triple(products[2], "30", "0"),  // Anchor 6A Switch, Qty 30
                Triple(products[3], "12", "0")   // Anchor 16A Socket, Qty 12
            )
            val inv3File = createAndSaveInvoice(
                invoiceRepo = invoiceRepo,
                calc = calc,
                pdfGen = pdfGen,
                bProfile = bProfile,
                invoiceNumber = "INV-0003",
                customer = c3,
                taxType = TaxType.CGST_SGST,
                paymentStatus = PaymentStatus.UNPAID,
                paymentMethod = null,
                amountPaid = BigDecimal.ZERO,
                itemsSpec = inv3ItemsSpec,
                notes = "Supply for Baner Penthouse project. Payment due within 15 days."
            )
            copyToPublicDownloads(inv3File, "INV-0003_Elite_Interior_Studio.pdf")

            // Verify all 3 invoices exist on disk
            val invoiceFiles = File(context.filesDir, "invoices").listFiles()?.filter { it.isFile && it.name.endsWith(".pdf") } ?: emptyList()
            assertEquals("Expected 3 invoice PDFs on disk", 3, invoiceFiles.size)
        }
    }

    private suspend fun createAndSaveInvoice(
        invoiceRepo: InvoiceRepository,
        calc: InvoiceCalculator,
        pdfGen: InvoicePdfGenerator,
        bProfile: BusinessProfile,
        invoiceNumber: String,
        customer: Customer,
        taxType: TaxType,
        paymentStatus: PaymentStatus,
        paymentMethod: PaymentMethod?,
        amountPaid: BigDecimal?,
        itemsSpec: List<Triple<Product, String, String>>,
        notes: String?
    ): File {
        val calculatedItems = itemsSpec.map { (product, qtyStr, discStr) ->
            calc.calculateItem(
                quantity = BigDecimal(qtyStr),
                unitPrice = product.sellingPrice,
                discountPercent = BigDecimal(discStr),
                gstRate = product.gstRate ?: BigDecimal("18.00"),
                taxType = taxType
            )
        }

        val totals = calc.calculateInvoice(calculatedItems)

        val domainItems = itemsSpec.mapIndexed { index, (product, qtyStr, discStr) ->
            val c = calculatedItems[index]
            InvoiceItem(
                id = 0L,
                invoiceId = 0L,
                productId = product.id,
                itemName = product.name,
                hsnCode = product.hsnCode,
                quantity = BigDecimal(qtyStr),
                unit = product.unit,
                unitPrice = product.sellingPrice,
                discountPercent = BigDecimal(discStr),
                discountAmount = c.discountAmount,
                gstRate = c.gstRate,
                taxableAmount = c.taxableAmount,
                cgstAmount = c.cgstAmount,
                sgstAmount = c.sgstAmount,
                igstAmount = c.igstAmount,
                taxAmount = c.taxAmount,
                total = c.total,
                sortOrder = index
            )
        }

        val resolvedAmountPaid = amountPaid ?: if (paymentStatus == PaymentStatus.PAID) totals.grandTotal else BigDecimal.ZERO

        val invoice = Invoice(
            id = 0L,
            invoiceNumber = invoiceNumber,
            invoiceDate = System.currentTimeMillis(),
            dueDate = System.currentTimeMillis() + 15 * 24 * 60 * 60 * 1000L,
            customerId = customer.id,
            customerName = customer.businessName ?: customer.name,
            customerAddress = customer.billingAddress,
            customerState = customer.state,
            customerGstin = customer.gstin,
            items = domainItems,
            subtotal = totals.subtotal,
            totalDiscount = totals.totalDiscount,
            totalTax = totals.totalTax,
            grandTotal = totals.grandTotal,
            taxType = taxType,
            status = InvoiceStatus.GENERATED,
            paymentStatus = paymentStatus,
            paymentMethod = paymentMethod,
            amountPaid = resolvedAmountPaid,
            notes = notes
        )

        val savedId = invoiceRepo.save(invoice)
        val savedInvoice = invoiceRepo.getById(savedId)!!

        val pdfResult = pdfGen.generate(savedInvoice, bProfile, totals.taxBreakdown)
        assertTrue("PDF generation failed for $invoiceNumber", pdfResult.isSuccess)
        return pdfResult.getOrThrow()
    }

    private fun copyToPublicDownloads(src: File, downloadName: String) {
        try {
            val destDir = File("/sdcard/Download")
            if (!destDir.exists()) destDir.mkdirs()
            val destFile = File(destDir, downloadName)
            src.copyTo(destFile, overwrite = true)
        } catch (e: Exception) {
            android.util.Log.e("ElectricShopTest", "Failed to copy $downloadName to Downloads", e)
        }
    }
}
