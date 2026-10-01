package com.warrior.tracker.core.time

/**
 * Accepts Latin, Arabic-Indic and Persian digits in numeric input (sec.13, sec.15)
 * so a user with a Persian keyboard never gets "invalid number".
 * Returns null when the text cannot be interpreted as a number.
 */
object NumberInputParser {

    private const val PERSIAN_ZERO = '\u06F0'   // ۰
    private const val ARABIC_ZERO = '\u0660'    // ٠

    fun normalizeDigits(input: String): String = buildString(input.length) {
        for (ch in input) {
            when {
                ch in '۰'..'۹' -> append('0' + (ch - PERSIAN_ZERO))
                ch in '٠'..'٩' -> append('0' + (ch - ARABIC_ZERO))
                ch == '٫' -> append('.')          // Arabic decimal separator
                ch == '،' -> append('.')          // sometimes used as decimal sep
                else -> append(ch)
            }
        }
    }

    fun toIntOrNull(input: String?): Int? {
        val s = normalizeDigits(input?.trim().orEmpty())
        return s.toIntOrNull()
    }

    fun toDoubleOrNull(input: String?): Double? {
        val s = normalizeDigits(input?.trim().orEmpty())
        return s.toDoubleOrNull()
    }
}
