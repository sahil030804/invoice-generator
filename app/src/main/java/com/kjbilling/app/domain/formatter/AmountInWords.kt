package com.kjbilling.app.domain.formatter

import java.math.BigDecimal
import java.math.RoundingMode

object AmountInWords {
    private val ones = arrayOf("", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen")
    private val tens = arrayOf("", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety")

    fun convert(amount: BigDecimal): String {
        if (amount.compareTo(BigDecimal.ZERO) == 0) return "Zero Rupees Only"
        
        val rupees = amount.setScale(0, RoundingMode.DOWN).toLong()
        val paise = amount.remainder(BigDecimal.ONE).multiply(BigDecimal("100")).setScale(0, RoundingMode.HALF_UP).toInt()

        val rupeesText = if (rupees == 0L) {
            "Zero Rupees"
        } else if (rupees == 1L) {
            "One Rupee"
        } else {
            "${convertToWords(rupees)} Rupees"
        }

        return buildString {
            append(rupeesText)
            if (paise > 0) {
                if (rupees == 0L) {
                    clear()
                    append("${convertToWords(paise.toLong())} Paise")
                } else {
                    append(" and ${convertToWords(paise.toLong())} Paise")
                }
            }
            append(" Only")
        }
    }

    private fun convertToWords(num: Long): String {
        if (num == 0L) return "Zero"

        return buildString {
            var n = num
            
            if (n >= 10000000) {
                val crores = (n / 10000000).toInt()
                append(convertToWords(crores.toLong())).append(" Crore ")
                n %= 10000000
            }
            
            if (n >= 100000) {
                val lakhs = (n / 100000).toInt()
                append(convertLessThanOneThousand(lakhs)).append(" Lakh ")
                n %= 100000
            }
            
            if (n >= 1000) {
                val thousands = (n / 1000).toInt()
                append(convertLessThanOneThousand(thousands)).append(" Thousand ")
                n %= 1000
            }
            
            if (n > 0) {
                append(convertLessThanOneThousand(n.toInt()))
            }
        }.trim()
    }

    private fun convertLessThanOneThousand(num: Int): String {
        return buildString {
            var n = num
            if (n >= 100) {
                append(ones[n / 100]).append(" Hundred ")
                n %= 100
            }
            if (n >= 20) {
                append(tens[n / 10]).append(" ")
                n %= 10
            }
            if (n > 0) {
                append(ones[n]).append(" ")
            }
        }.trim()
    }
}
