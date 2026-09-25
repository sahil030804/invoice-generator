package com.kjbilling.app.pdf

import com.kjbilling.app.domain.model.BusinessProfile
import com.kjbilling.app.domain.model.BusinessSnapshot
import com.kjbilling.app.domain.model.Invoice
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class InvoiceSellerSnapshotTest {

    private val profileAtIssue = BusinessProfile(
        businessName = "KJ Plastic",
        ownerName = "Sahil",
        mobile = "9876543210",
        address = "Shop 12, MG Road",
        state = "Maharashtra",
        gstin = "27AABCS1429B1ZX",
        email = "old@shop.com",
        city = "Pune",
        pincode = "411001"
    )

    private val profileLater = profileAtIssue.copy(
        businessName = "New Name Traders",
        address = "Other Road",
        gstin = null,
        email = null
    )

    private fun invoice(seller: BusinessSnapshot?) = Invoice(
        invoiceNumber = "INV-0001",
        invoiceDate = 0L,
        customerName = "Walk-in Customer",
        seller = seller
    )

    @Test
    fun snapshot_joinsAddressParts_skippingBlanks() {
        val snapshot = BusinessSnapshot.from(profileAtIssue.copy(city = " ", pincode = null))

        assertEquals("Shop 12, MG Road, Maharashtra", snapshot.address)
    }

    @Test
    fun document_usesSnapshot_notCurrentProfile() {
        val invoice = invoice(BusinessSnapshot.from(profileAtIssue))

        val doc = InvoiceDocumentModel.fromInvoice(invoice, profileLater, emptyList())

        assertEquals("KJ Plastic", doc.businessName)
        assertEquals("Shop 12, MG Road, Pune, Maharashtra, 411001", doc.businessAddress)
        assertEquals("27AABCS1429B1ZX", doc.businessGstin)
        assertEquals("old@shop.com", doc.businessEmail)
        assertEquals("Sahil", doc.signatureName)
    }

    @Test
    fun document_withoutSnapshot_fallsBackToCurrentProfile() {
        val doc = InvoiceDocumentModel.fromInvoice(invoice(null), profileLater, emptyList())

        assertEquals("New Name Traders", doc.businessName)
        assertNull(doc.businessGstin)
    }
}
