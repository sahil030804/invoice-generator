package com.kjbilling.app.domain.formatter

import com.kjbilling.app.domain.model.PaymentMethod

/** Human-friendly payment method names ("Bank transfer", not "BANK_TRANSFER"). */
fun PaymentMethod.label(): String = when (this) {
    PaymentMethod.CASH -> "Cash"
    PaymentMethod.UPI -> "UPI"
    PaymentMethod.BANK_TRANSFER -> "Bank transfer"
    PaymentMethod.CARD -> "Card"
    PaymentMethod.CHEQUE -> "Cheque"
    PaymentMethod.OTHER -> "Other"
}
