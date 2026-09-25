package com.kjbilling.app.domain.validator

/**
 * Resolves Indian state names and GSTIN state codes so tax type never depends on exact spelling.
 *
 *   "TamilNadu" vs "Tamil Nadu"  → same state
 *   GSTIN "27AAB..."             → "Maharashtra"
 */
object StateResolver {

    private const val GSTIN_STATE_CODE_LENGTH = 2

    // GST state codes (code 25 is the old Daman & Diu, merged into 26).
    private val CODE_TO_STATE = mapOf(
        "01" to "Jammu and Kashmir", "02" to "Himachal Pradesh", "03" to "Punjab",
        "04" to "Chandigarh", "05" to "Uttarakhand", "06" to "Haryana", "07" to "Delhi",
        "08" to "Rajasthan", "09" to "Uttar Pradesh", "10" to "Bihar", "11" to "Sikkim",
        "12" to "Arunachal Pradesh", "13" to "Nagaland", "14" to "Manipur", "15" to "Mizoram",
        "16" to "Tripura", "17" to "Meghalaya", "18" to "Assam", "19" to "West Bengal",
        "20" to "Jharkhand", "21" to "Odisha", "22" to "Chhattisgarh", "23" to "Madhya Pradesh",
        "24" to "Gujarat", "25" to "Dadra and Nagar Haveli and Daman and Diu",
        "26" to "Dadra and Nagar Haveli and Daman and Diu", "27" to "Maharashtra",
        "28" to "Andhra Pradesh", "29" to "Karnataka", "30" to "Goa", "31" to "Lakshadweep",
        "32" to "Kerala", "33" to "Tamil Nadu", "34" to "Puducherry",
        "35" to "Andaman and Nicobar Islands", "36" to "Telangana", "37" to "Andhra Pradesh",
        "38" to "Ladakh"
    )

    // Old or short names → the key of the current name (letters only, lowercase).
    private val ALIASES = mapOf(
        "orissa" to "odisha",
        "pondicherry" to "puducherry",
        "uttaranchal" to "uttarakhand",
        "nctofdelhi" to "delhi",
        "newdelhi" to "delhi",
        "jk" to "jammuandkashmir",
        "andamanandnicobar" to "andamanandnicobarislands",
        "damananddiu" to "dadraandnagarhavelianddamananddiu",
        "dadraandnagarhaveli" to "dadraandnagarhavelianddamananddiu"
    )

    private fun key(name: String): String {
        val letters = name.lowercase().replace("&", "and").filter { it.isLetter() }
        return ALIASES[letters] ?: letters
    }

    /** True when both names are non-blank and refer to the same state, ignoring case, spacing and old names. */
    fun isSameState(a: String?, b: String?): Boolean {
        if (a.isNullOrBlank() || b.isNullOrBlank()) {
            return false
        }
        return key(a) == key(b)
    }

    /** State name for the first two digits of [gstin], or null when missing/unknown. */
    fun fromGstin(gstin: String?): String? {
        val code = gstin?.trim()?.take(GSTIN_STATE_CODE_LENGTH) ?: return null
        if (code.length < GSTIN_STATE_CODE_LENGTH) {
            return null
        }
        return CODE_TO_STATE[code]
    }
}
