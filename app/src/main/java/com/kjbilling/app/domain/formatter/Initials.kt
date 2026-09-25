package com.kjbilling.app.domain.formatter

/** Up to two initials for avatars and the PDF logo badge: "Acme Global" → "AG", "Rakesh" → "RA". */
object Initials {

    fun of(name: String, fallback: String = "?"): String {
        val words = name.trim().split(Regex("\\s+")).filter { it.isNotBlank() }
        return when {
            words.isEmpty() -> fallback
            words.size == 1 -> words[0].take(2).uppercase()
            else -> "${words[0].first()}${words[1].first()}".uppercase()
        }
    }
}
