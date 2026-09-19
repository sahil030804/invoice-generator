package com.kjbilling.app.data.db.converter

import android.util.Log
import androidx.room.TypeConverter
import com.kjbilling.app.domain.model.InvoiceStatus
import com.kjbilling.app.domain.model.PaymentMethod
import com.kjbilling.app.domain.model.PaymentStatus
import com.kjbilling.app.domain.model.TaxType
import java.math.BigDecimal

class Converters {

    @TypeConverter
    fun fromBigDecimal(value: BigDecimal?): String? {
        return value?.toString()
    }

    @TypeConverter
    fun toBigDecimal(value: String?): BigDecimal? {
        if (value == null) return null
        return try {
            BigDecimal(value)
        } catch (e: NumberFormatException) {
            Log.e("Converters", "Invalid BigDecimal string: $value", e)
            BigDecimal.ZERO
        }
    }

    @TypeConverter
    fun fromTaxType(value: TaxType?): String? {
        return value?.name
    }

    @TypeConverter
    fun toTaxType(value: String?): TaxType? {
        return value?.let { TaxType.valueOf(it) }
    }

    @TypeConverter
    fun fromInvoiceStatus(value: InvoiceStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toInvoiceStatus(value: String?): InvoiceStatus? {
        return value?.let { InvoiceStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromPaymentStatus(value: PaymentStatus?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPaymentStatus(value: String?): PaymentStatus? {
        return value?.let { PaymentStatus.valueOf(it) }
    }

    @TypeConverter
    fun fromPaymentMethod(value: PaymentMethod?): String? {
        return value?.name
    }

    @TypeConverter
    fun toPaymentMethod(value: String?): PaymentMethod? {
        return value?.let { PaymentMethod.valueOf(it) }
    }
}
