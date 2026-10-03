package com.warrior.tracker.core.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

/**
 * Regression suite for the Jalali engine (sec.13).
 *
 * The Phase-0 implementation had four defects, all reproduced here:
 *  1. The out-of-table leap-year fallback returned `true` for every year, so dates outside
 *     1961-2071 converted to nonsense (and 2072-03-22 converted *backwards* to 1450/11/14).
 *  2. An off-by-one table boundary produced the impossible `JalaliDate(1450, 13, 1)` for
 *     2072-03-21.
 *  3. `format()` used the default locale, so `%d` emitted Arabic-Indic digits (U+0660) on an
 *     `ar` device and ignored `persianDigits = false` on an `fa` device.
 *  4. Every call re-parsed up to 110 date strings (~48 us/call).
 */
class JalaliTest {

    // ---- 1. Published Nowruz dates (official Iranian calendar) ----

    @Test
    fun `known nowruz dates convert to 1 farvardin`() {
        val nowruz = mapOf(
            "1996-03-20" to 1375, "2000-03-20" to 1379, "2004-03-20" to 1383,
            "2008-03-20" to 1387, "2012-03-20" to 1391, "2016-03-20" to 1395,
            "2020-03-20" to 1399, "2024-03-20" to 1403, "2025-03-21" to 1404,
            "2026-03-21" to 1405, "2027-03-21" to 1406, "2028-03-20" to 1407,
        )
        for ((iso, jy) in nowruz) {
            val j = LocalDate.parse(iso).toJalali()
            assertEquals("Nowruz $jy AP", JalaliDate(jy, 1, 1), j)
        }
    }

    @Test
    fun `today converts correctly`() {
        // 2026-10-01 == 1405/07/09 (verified against the reference algorithm).
        assertEquals(JalaliDate(1405, 7, 9), LocalDate.of(2026, 10, 1).toJalali())
    }

    @Test
    fun `last day of each month rolls over correctly`() {
        assertEquals(JalaliDate(1405, 6, 31), LocalDate.of(2026, 9, 22).toJalali())
        // 1405 is not a leap year -> Esfand has 29 days.
        assertFalse(isJalaliLeapYear(1405))
        assertEquals(29, jalaliMonthLength(1405, 12))
        assertEquals(JalaliDate(1405, 12, 29), LocalDate.of(2027, 3, 20).toJalali())
        assertEquals(JalaliDate(1406, 1, 1), LocalDate.of(2027, 3, 21).toJalali())
    }

    // ---- 2. Leap years ----

    /**
     * Every leap year from AP 1330..1459, taken from the reference algorithm. Spans both sides of
     * the anchor table (1340..1450) so the exact path AND the arithmetic fallback are both
     * exercised. The Phase-0 fallback returned `true` for all of these plus all the common years.
     */
    private val referenceLeapYears = setOf(
        1333, 1337, 1342, 1346, 1350, 1354, 1358, 1362, 1366, 1370,
        1375, 1379, 1383, 1387, 1391, 1395, 1399, 1403, 1408, 1412,
        1416, 1420, 1424, 1428, 1432, 1436, 1441, 1445, 1449, 1453, 1457,
    )

    @Test
    fun `leap years match the reference algorithm`() {
        for (jy in 1330..1459) {
            val expected = jy in referenceLeapYears
            assertEquals(
                "$jy AP leap mismatch (reference=${expected})",
                expected,
                isJalaliLeapYear(jy),
            )
            assertEquals(if (expected) 366 else 365, jalaliYearLength(jy))
        }
    }

    @Test
    fun `leap years are consistent with anchor gaps inside the table`() {
        // 366-day spacing between consecutive Nowruz dates <=> the earlier year is leap.
        for (jy in ANCHORED_FIRST_JY until ANCHORED_LAST_JY) {
            val start = JalaliDate(jy, 1, 1).toLocalDate()
            val next = JalaliDate(jy + 1, 1, 1).toLocalDate()
            val gap = next.toEpochDay() - start.toEpochDay()
            assertEquals("year length mismatch for $jy AP", if (isJalaliLeapYear(jy)) 366L else 365L, gap)
            assertEquals(jalaliYearLength(jy), gap.toInt())
        }
    }

    @Test
    fun `leap fallback works for years outside the anchor table`() {
        // The Phase-0 fallback returned true unconditionally; these are the counter-examples.
        // Below the table (< 1340):
        assertFalse("1339 AP is not leap", isJalaliLeapYear(1339))
        assertTrue("1337 AP is leap", isJalaliLeapYear(1337))
        assertTrue("1333 AP is leap", isJalaliLeapYear(1333))
        // The last table row has no following anchor, so it also goes through the fallback:
        assertFalse("1450 AP is not leap", isJalaliLeapYear(1450))
        // Above the table (> 1450):
        assertFalse("1451 AP is not leap", isJalaliLeapYear(1451))
        assertFalse("1452 AP is not leap", isJalaliLeapYear(1452))
        assertTrue("1453 AP is leap", isJalaliLeapYear(1453))
        assertTrue("1457 AP is leap", isJalaliLeapYear(1457))
    }

    // ---- 3. Never emit an impossible date ----

    @Test
    fun `no date converts to an out-of-range month or day`() {
        var d = LocalDate.of(1880, 1, 1)
        val end = LocalDate.of(2130, 12, 31)
        while (!d.isAfter(end)) {
            val j = d.toJalali()
            assertTrue("$d -> $j has month out of range", j.month in 1..12)
            assertTrue("$d -> $j has day out of range", j.dayOfMonth in 1..j.lengthOfMonth())
            d = d.plusDays(1)
        }
    }

    @Test
    fun `exact table boundaries are handled without off-by-one`() {
        // First and last dates the anchor table covers exactly.
        assertEquals(JalaliDate(ANCHORED_FIRST_JY, 1, 1), LocalDate.parse("1961-03-21").toJalali())
        assertEquals(JalaliDate(ANCHORED_FIRST_JY - 1, 12, 29), LocalDate.parse("1961-03-20").toJalali())
        // 1450 AP is not leap, so its last day is Esfand 29 == 2072-03-19; the next day must
        // advance to 1451/1/1 rather than running off the end of the table. Phase 0 returned the
        // impossible JalaliDate(1450, 13, 1) for 2072-03-21 and then went BACKWARDS in time
        // (2072-03-22 -> 1450/11/14) because the boundary test was off by one day.
        assertEquals(JalaliDate(1450, 12, 28), LocalDate.parse("2072-03-18").toJalali())
        assertEquals(JalaliDate(1450, 12, 29), LocalDate.parse("2072-03-19").toJalali())
        assertEquals(JalaliDate(1451, 1, 1), LocalDate.parse("2072-03-20").toJalali())
        assertEquals(JalaliDate(1451, 1, 2), LocalDate.parse("2072-03-21").toJalali())
        assertEquals(JalaliDate(1451, 1, 3), LocalDate.parse("2072-03-22").toJalali())
    }

    @Test
    fun `arithmetic engine covers dates far outside the anchor table`() {
        assertEquals(JalaliDate(1278, 10, 11), LocalDate.parse("1900-01-01").toJalali())
        assertEquals(JalaliDate(1879, 3, 25), LocalDate.parse("2500-06-15").toJalali())
    }

    @Test
    fun `constructing an impossible Jalali date fails loudly`() {
        // Silent corruption (month 13) is worse than a clear exception.
        assertThrowsIae { JalaliDate(1405, 13, 1) }
        assertThrowsIae { JalaliDate(1405, 0, 1) }
        assertThrowsIae { JalaliDate(1405, 7, 0) }
        assertThrowsIae { JalaliDate(1405, 7, 32) }
        // Esfand 30 only exists in a leap year.
        assertThrowsIae { JalaliDate(1405, 12, 30) }
        assertEquals(JalaliDate(1403, 12, 30), JalaliDate(1403, 12, 30)) // 1403 IS leap
    }

    // ---- 4. Round-trip ----

    @Test
    fun `gregorian to jalali and back is lossless`() {
        var d = LocalDate.of(1900, 1, 1)
        val end = LocalDate.of(2100, 12, 31)
        while (!d.isAfter(end)) {
            assertEquals("$d did not round-trip", d, d.toJalali().toLocalDate())
            d = d.plusDays(1)
        }
    }

    @Test
    fun `every valid jalali date in the anchor window round-trips`() {
        for (jy in ANCHORED_FIRST_JY..ANCHORED_LAST_JY) {
            for (jm in 1..12) {
                for (jd in 1..jalaliMonthLength(jy, jm)) {
                    val j = JalaliDate(jy, jm, jd)
                    assertEquals(j, j.toLocalDate().toJalali())
                }
            }
        }
    }

    @Test
    fun `dayOfYear and plusDays agree with LocalDate arithmetic`() {
        val j = JalaliDate(1405, 1, 1)
        assertEquals(0, j.dayOfYear())
        assertEquals(JalaliDate(1405, 1, 2), j.plusDays(1))
        assertEquals(JalaliDate(1405, 2, 1), j.plusDays(31))
        assertEquals(JalaliDate(1406, 1, 1), j.plusDays(365))
        assertEquals(JalaliDate(1404, 12, 29), j.plusDays(-1))
    }

    @Test
    fun `comparison follows chronological order`() {
        assertTrue(JalaliDate(1405, 7, 9) > JalaliDate(1405, 7, 8))
        assertTrue(JalaliDate(1405, 7, 9) < JalaliDate(1406, 1, 1))
        // 1403 AP is leap, so Esfand has 30 days; 1404 is not, so its Esfand ends at 29.
        assertTrue(JalaliDate(1403, 12, 30) < JalaliDate(1404, 1, 1))
        assertTrue(JalaliDate(1404, 12, 29) < JalaliDate(1405, 1, 1))
        assertEquals(0, JalaliDate(1405, 7, 9).compareTo(JalaliDate(1405, 7, 9)))
    }

    // ---- 5. Locale-independent formatting ----

    @Test
    fun `format is independent of the default locale`() {
        val original = Locale.getDefault()
        try {
            for (tag in listOf("en", "fa", "fa-IR", "ar", "ar-EG", "fr", "de", "ur", "ps")) {
                Locale.setDefault(Locale.forLanguageTag(tag))
                val d = JalaliDate(1405, 7, 9)
                assertEquals(
                    "locale $tag changed latin output",
                    "1405/07/09",
                    d.format(persianDigits = false),
                )
                assertEquals(
                    "locale $tag changed persian output (must be U+06F0 block, not U+0660)",
                    "\u06F1\u06F4\u06F0\u06F5/\u06F0\u06F7/\u06F0\u06F9",
                    d.format(persianDigits = true),
                )
                // formatIso() is the persisted `local_date` form: ISO Gregorian, not Jalali.
                assertEquals("2026-10-01", d.formatIso())
            }
        } finally {
            Locale.setDefault(original)
        }
    }

    @Test
    fun `digit conversion round-trips and only touches digits`() {
        assertEquals("\u06F1\u06F2\u06F3", "123".toPersianDigits())
        assertEquals("123", "\u06F1\u06F2\u06F3".toLatinDigits())
        assertEquals("123", "\u0661\u0662\u0663".toLatinDigits())   // Arabic-Indic
        assertEquals("1405/07/09", "1405/07/09".toPersianDigits().toLatinDigits())
        assertEquals("a1b", "a1b".toPersianDigits().let { it.replace('\u06F1', '1') })
        assertEquals("", "".toPersianDigits())
    }

    // ---- 6. Display helpers ----

    @Test
    fun `persian weekday names start at shanbe for saturday`() {
        assertEquals("شنبه", LocalDate.of(2026, 10, 3).dayOfWeekFa())      // Saturday
        assertEquals("یک‌شنبه", LocalDate.of(2026, 10, 4).dayOfWeekFa())   // Sunday
        assertEquals("دوشنبه", LocalDate.of(2026, 10, 5).dayOfWeekFa())
        assertEquals("سه‌شنبه", LocalDate.of(2026, 10, 6).dayOfWeekFa())
        assertEquals("چهارشنبه", LocalDate.of(2026, 10, 7).dayOfWeekFa())
        assertEquals("پنج‌شنبه", LocalDate.of(2026, 10, 1).dayOfWeekFa())  // Thursday
        assertEquals("جمعه", LocalDate.of(2026, 10, 2).dayOfWeekFa())      // Friday
        // Every DayOfWeek value must be covered.
        for (dow in DayOfWeek.values()) {
            val date = LocalDate.of(2026, 10, 5).with(dow)
            assertTrue(date.dayOfWeekFa().isNotBlank())
            assertEquals(dow.name, date.dayOfWeek.name)
        }
    }

    @Test
    fun `month names are the twelve persian months in order`() {
        val expected = listOf(
            "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
            "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
        )
        assertEquals(expected, JALALI_MONTH_NAMES_FA)
        for (m in 1..12) assertEquals(expected[m - 1], JalaliDate(1405, m, 1).monthNameFa())
    }

    @Test
    fun `month lengths follow the jalali pattern`() {
        for (jy in listOf(1404, 1405)) {
            for (m in 1..6) assertEquals(31, jalaliMonthLength(jy, m))
            for (m in 7..11) assertEquals(30, jalaliMonthLength(jy, m))
        }
        assertEquals(30, jalaliMonthLength(1403, 12)) // leap
        assertEquals(29, jalaliMonthLength(1405, 12)) // common
    }

    // ---- 7. Performance regression ----

    @Test
    fun `conversion is fast enough for a calendar grid`() {
        val dates = (0 until 42).map { LocalDate.of(2026, 10, 1).plusDays(it.toLong()) }
        repeat(200) { dates.forEach { it.toJalali() } }              // warm up JIT
        val iterations = 2_000
        val start = System.nanoTime()
        repeat(iterations) { dates.forEach { it.toJalali() } }
        val perCallNanos = (System.nanoTime() - start).toDouble() / (iterations * dates.size)
        // Phase 0 measured ~48,100 ns/call because every call re-parsed up to 110 date strings.
        // The pre-computed epoch-day table plus binary search should stay far below 5 us.
        assertTrue(
            "toJalali() regressed: ${perCallNanos}ns per call (Phase-0 baseline was ~48100ns)",
            perCallNanos < 5_000.0,
        )
    }

    private fun assertThrowsIae(block: () -> Unit) {
        try {
            block()
            fail("expected IllegalArgumentException")
        } catch (expected: IllegalArgumentException) {
            // ok
        }
    }
}
