package com.kjbilling.app.domain.validator

data class GstinValidationResult(val isValid: Boolean, val error: String? = null)

object GstinValidator {
    private val GSTIN_REGEX = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$".toRegex()

    fun validate(gstin: String): GstinValidationResult {
        if (gstin.isBlank()) {
            return GstinValidationResult(false, "GSTIN cannot be empty")
        }
        
        if (gstin.length != 15) {
            return GstinValidationResult(false, "GSTIN must be 15 characters long")
        }

        val stateCode = gstin.take(2).toIntOrNull()
        if (stateCode == null || stateCode !in 1..37) {
            return GstinValidationResult(false, "Invalid state code")
        }

        if (!gstin.matches(GSTIN_REGEX)) {
            return GstinValidationResult(false, "Invalid GSTIN format")
        }
        
        return GstinValidationResult(true)
    }
}
