package com.warrior.tracker.core.time

import org.junit.Assert.assertEquals
import org.junit.Test

/** sec.13/sec.15 — Persian & Arabic-Indic keyboard input must parse like Latin digits. */
class NumberInputParserTest {

    @Test
    fun `persian digits normalize to latin`() {
        assertEquals("123", NumberInputParser.normalizeDigits("۱۲۳"))
        assertEquals(123, NumberInputParser.toIntOrNull("۱۲۳"))
    }

    @Test
    fun `arabic-indic digits normalize to latin`() {
        assertEquals(456, NumberInputParser.toIntOrNull("٤٥٦"))
    }

    @Test
    fun `arabic decimal separator becomes dot`() {
        assertEquals(2.5, NumberInputParser.toDoubleOrNull("۲٫۵")!!, 1e-9)
    }

    @Test
    fun `invalid input returns null not exception`() {
        assertEquals(null, NumberInputParser.toIntOrNull("abc"))
        assertEquals(null, NumberInputParser.toDoubleOrNull(""))
        assertEquals(null, NumberInputParser.toDoubleOrNull(null))
    }
}
