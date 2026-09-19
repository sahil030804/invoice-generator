package com.kjbilling.app.domain.model

enum class TaxType {
    CGST_SGST,
    IGST,
    NO_GST
}

enum class InvoiceStatus {
    DRAFT,
    GENERATED,
    SENT,
    PAID,
    PARTIALLY_PAID,
    CANCELLED
}

enum class PaymentStatus {
    PAID,
    UNPAID,
    PARTIAL
}

enum class PaymentMethod {
    CASH,
    UPI,
    BANK_TRANSFER,
    CARD,
    CHEQUE,
    OTHER
}
