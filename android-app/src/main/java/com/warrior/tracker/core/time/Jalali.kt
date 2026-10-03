package com.warrior.tracker.core.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Locale

/**
 * Jalali (Persian solar) calendar support — display layer only.
 * Storage stays ISO Gregorian `local_date` + UTC epoch millis (ARCHITECTURE.md sec.6.4, sec.13).
 *
 * Two-strategy engine:
 *
 *  1. **Anchored (exact).** A table of 1 Farvardin (Nowruz) Gregorian dates for AP 1340..1450
 *     (1961-2071 CE), generated from the reference jdatetime algorithm and cross-checked against
 *     published Nowruz dates (1403→2024-03-20, 1404→2025-03-21, 1405→2026-03-21). Inside this
 *     window the conversion is exact by definition: day counting between anchors.
 *  2. **Arithmetic (extended).** Outside the window the official Iranian 33-year sub-cycle
 *     algorithm (jalaali-js `jalCal`, valid for AP -61..3177) is used, so the engine never
 *     silently produces an out-of-range date for any realistic device clock.
 *
 * Pure Kotlin/JVM → unit-testable and byte-identical on every device (no `android.icu`), which is
 * the deliberate deviation from sec.5/sec.13's "android.icu PersianCalendar" note: ICU's calendar
 * is OEM/locale dependent, and a fitness tracker must render the same date on every phone.
 *
 * All hot paths work on pre-computed `Long` epoch-days; no `String` parsing per call.
 */
data class JalaliDate(val year: Int, val month: Int, val dayOfMonth: Int) : Comparable<JalaliDate> {

    init {
        require(month in 1..12) { "Jalali month out of range: $month" }
        require(dayOfMonth in 1..jalaliMonthLength(year, month)) {
            "Jalali day out of range: $year/$month/$dayOfMonth"
        }
    }

    /** e.g. `"1405/07/09"`, or Persian digits when [persianDigits] is true. Locale-independent. */
    fun format(persianDigits: Boolean = true): String {
        // Locale.ROOT is mandatory: %d is localised by default, so an `ar` device would emit
        // Arabic-Indic digits (U+0660) and an `fa` device would ignore persianDigits = false.
        val s = String.format(Locale.ROOT, "%04d/%02d/%02d", year, month, dayOfMonth)
        return if (persianDigits) s.toPersianDigits() else s
    }

    /**
     * The equivalent Gregorian date in ISO `yyyy-MM-dd` form — i.e. the value persisted in
     * `workouts.local_date` / `body_weights.local_date` (sec.10.1: storage is always Gregorian).
     */
    fun formatIso(): String = toLocalDate().toString()

    /** Number of days in this month (29/30 for Esfand depending on leap). */
    fun lengthOfMonth(): Int = jalaliMonthLength(year, month)

    /** 0-based day of year. */
    fun dayOfYear(): Int {
        var d = dayOfMonth - 1
        for (m in 1 until month) d += jalaliMonthLength(year, m)
        return d
    }

    /** The Gregorian date this Jalali date denotes. */
    fun toLocalDate(): LocalDate = LocalDate.ofEpochDay(jalaliYearStartEpochDay(year) + dayOfYear())

    /** Persian weekday name (display only; the Iranian week starts on Saturday). */
    fun dayOfWeekFa(): String = toLocalDate().dayOfWeekFa()

    fun plusDays(days: Long): JalaliDate = toLocalDate().plusDays(days).toJalali()

    override fun compareTo(other: JalaliDate): Int =
        toLocalDate().toEpochDay().compareTo(other.toLocalDate().toEpochDay())

    override fun toString(): String = format(persianDigits = false)

    companion object {
        /** Convert a Gregorian date to Jalali. */
        fun from(date: LocalDate): JalaliDate = date.toJalali()
    }
}

// ----------------------------------------------------------------------------------------------
// Anchor table — Gregorian date of 1 Farvardin for AP 1340..1450 (1961-2071 CE).
// Verified exact against the reference algorithm for every date in the window.
// ----------------------------------------------------------------------------------------------

private const val TABLE_FIRST_JY = 1340

private val FARVARDIN_1 = listOf(
    "1961-03-21", "1962-03-21", "1963-03-21", "1964-03-21", "1965-03-21",
    "1966-03-21", "1967-03-21", "1968-03-21", "1969-03-21", "1970-03-21",
    "1971-03-21", "1972-03-21", "1973-03-21", "1974-03-21", "1975-03-21",
    "1976-03-21", "1977-03-21", "1978-03-21", "1979-03-21", "1980-03-21",
    "1981-03-21", "1982-03-21", "1983-03-21", "1984-03-21", "1985-03-21",
    "1986-03-21", "1987-03-21", "1988-03-21", "1989-03-21", "1990-03-21",
    "1991-03-21", "1992-03-21", "1993-03-21", "1994-03-21", "1995-03-21",
    "1996-03-20", "1997-03-21", "1998-03-21", "1999-03-21", "2000-03-20",
    "2001-03-21", "2002-03-21", "2003-03-21", "2004-03-20", "2005-03-21",
    "2006-03-21", "2007-03-21", "2008-03-20", "2009-03-21", "2010-03-21",
    "2011-03-21", "2012-03-20", "2013-03-21", "2014-03-21", "2015-03-21",
    "2016-03-20", "2017-03-21", "2018-03-21", "2019-03-21", "2020-03-20",
    "2021-03-21", "2022-03-21", "2023-03-21", "2024-03-20", "2025-03-21",
    "2026-03-21", "2027-03-21", "2028-03-20", "2029-03-20", "2030-03-21",
    "2031-03-21", "2032-03-20", "2033-03-20", "2034-03-21", "2035-03-21",
    "2036-03-20", "2037-03-20", "2038-03-21", "2039-03-21", "2040-03-20",
    "2041-03-20", "2042-03-21", "2043-03-21", "2044-03-20", "2045-03-20",
    "2046-03-21", "2047-03-21", "2048-03-20", "2049-03-20", "2050-03-21",
    "2051-03-21", "2052-03-20", "2053-03-20", "2054-03-21", "2055-03-21",
    "2056-03-20", "2057-03-20", "2058-03-21", "2059-03-21", "2060-03-20",
    "2061-03-20", "2062-03-20", "2063-03-21", "2064-03-20", "2065-03-20",
    "2066-03-20", "2067-03-21", "2068-03-20", "2069-03-20", "2070-03-20",
    "2071-03-21",
)

/** Parsed exactly once (was previously re-parsed ~110× per conversion — ~48 µs/call). */
private val ANCHOR_EPOCH_DAYS: LongArray by lazy(LazyThreadSafetyMode.PUBLICATION) {
    LongArray(FARVARDIN_1.size) { LocalDate.parse(FARVARDIN_1[it]).toEpochDay() }
}

/** First/last AP year covered exactly by the anchor table (exposed for tests/diagnostics). */
internal val ANCHORED_FIRST_JY: Int get() = TABLE_FIRST_JY
internal val ANCHORED_LAST_JY: Int get() = TABLE_FIRST_JY + FARVARDIN_1.size - 1

// ----------------------------------------------------------------------------------------------
// Arithmetic engine (official Iranian 33-year sub-cycle — jalaali-js `jalCal`).
// Kotlin's Int `/` and `%` truncate toward zero, matching the reference's `~~(a/b)` exactly.
// ----------------------------------------------------------------------------------------------

private val BREAKS = intArrayOf(
    -61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181,
    1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178,
)

/** AP years supported by the arithmetic engine: -61 .. 3177 (560 BCE .. 2599 CE). */
val JALALI_MIN_YEAR: Int get() = BREAKS.first()
val JALALI_MAX_YEAR: Int get() = BREAKS.last() - 1

private class JalCal(val leap: Int, val gregorianYear: Int, val march: Int)

private fun jalCal(jy: Int): JalCal {
    require(jy >= JALALI_MIN_YEAR && jy <= JALALI_MAX_YEAR) {
        "Jalali year $jy outside supported range $JALALI_MIN_YEAR..$JALALI_MAX_YEAR"
    }
    val gy = jy + 621
    var leapJ = -14
    var jp = BREAKS[0]
    var jump = 0
    var i = 1
    while (i < BREAKS.size) {
        val jm = BREAKS[i]
        jump = jm - jp
        if (jy < jm) break
        leapJ += (jump / 33) * 8 + (jump % 33) / 4
        jp = jm
        i++
    }
    var n = jy - jp
    leapJ += (n / 33) * 8 + (n % 33 + 3) / 4
    if (jump % 33 == 4 && jump - n == 4) leapJ++

    val leapG = gy / 4 - ((gy / 100 + 1) * 3) / 4 - 150
    val march = 20 + leapJ - leapG

    if (jump - n < 6) n = n - jump + ((jump + 4) / 33) * 33
    var leap = ((n + 1) % 33 - 1) % 4
    if (leap == -1) leap = 4
    return JalCal(leap, gy, march)
}

/**
 * True when AP year [jy] has 30 days in Esfand.
 * Inside the anchor window the answer is read from the (exact) anchor gaps; outside it, from the
 * arithmetic engine. Previously this fell back to a 128-based cycle that returned `true` for
 * every year, which made out-of-table dates convert to nonsense.
 */
fun isJalaliLeapYear(jy: Int): Boolean {
    val idx = jy - TABLE_FIRST_JY
    val a = ANCHOR_EPOCH_DAYS
    if (idx in 0 until a.size - 1) return (a[idx + 1] - a[idx]) == 366L
    return jalCal(jy).leap == 0
}

/** Days in AP year [jy]: 366 when leap, else 365. */
fun jalaliYearLength(jy: Int): Int = if (isJalaliLeapYear(jy)) 366 else 365

/** Days in month [jm] of AP year [jy]. */
fun jalaliMonthLength(jy: Int, jm: Int): Int = when {
    jm in 1..6 -> 31
    jm in 7..11 -> 30
    jm == 12 -> if (isJalaliLeapYear(jy)) 30 else 29
    else -> throw IllegalArgumentException("Jalali month out of range: $jm")
}

/** Epoch-day of 1 Farvardin of AP [jy]; exact anchor when available, else arithmetic. */
private fun jalaliYearStartEpochDay(jy: Int): Long {
    val idx = jy - TABLE_FIRST_JY
    val a = ANCHOR_EPOCH_DAYS
    if (idx in a.indices) return a[idx]
    val c = jalCal(jy)
    return LocalDate.of(c.gregorianYear, 3, c.march).toEpochDay()
}

/** Largest index `i` with `a[i] <= value`, or -1 when `value` precedes the whole table. */
private fun LongArray.floorIndex(value: Long): Int {
    var lo = 0
    var hi = size - 1
    var result = -1
    while (lo <= hi) {
        val mid = (lo + hi) ushr 1
        if (this[mid] <= value) {
            result = mid
            lo = mid + 1
        } else {
            hi = mid - 1
        }
    }
    return result
}

/**
 * Convert a Gregorian date to its official Iranian (Jalali) equivalent.
 *
 * Exact inside the anchor window (1961-03-21 .. 2072-03-20, i.e. AP 1340..1450 inclusive);
 * arithmetic (AP -61..3177) outside it. Never returns an out-of-range month or day.
 */
fun LocalDate.toJalali(): JalaliDate {
    val epochDay = toEpochDay()
    val anchors = ANCHOR_EPOCH_DAYS
    val idx = anchors.floorIndex(epochDay)
    if (idx >= 0) {
        val jy = TABLE_FIRST_JY + idx
        val dayOfYear = (epochDay - anchors[idx]).toInt()
        // Guard the last table row: there is no anchor[idx+1] to bound it, so use the year length.
        if (dayOfYear < jalaliYearLength(jy)) return dayOfYearToJalali(jy, dayOfYear)
    }
    return toJalaliArithmetic(epochDay)
}

private fun dayOfYearToJalali(jy: Int, dayOfYear: Int): JalaliDate {
    var rem = dayOfYear
    var m = 1
    while (m < 12) {
        val len = jalaliMonthLength(jy, m)
        if (rem < len) break
        rem -= len
        m++
    }
    return JalaliDate(jy, m, rem + 1)
}

/** Arithmetic conversion for dates outside the anchor window. */
private fun toJalaliArithmetic(epochDay: Long): JalaliDate {
    // 1 Farvardin falls in March of jy+621, so this estimate is within ±1 year.
    var jy = LocalDate.ofEpochDay(epochDay).year - 621
    while (jy > JALALI_MIN_YEAR && jalaliYearStartEpochDay(jy) > epochDay) jy--
    while (jy < JALALI_MAX_YEAR && jalaliYearStartEpochDay(jy + 1) <= epochDay) jy++
    val dayOfYear = (epochDay - jalaliYearStartEpochDay(jy)).toInt()
    return dayOfYearToJalali(jy, dayOfYear.coerceAtLeast(0))
}

// ----------------------------------------------------------------------------------------------
// Display helpers
// ----------------------------------------------------------------------------------------------

private val WEEKDAY_NAMES_FA = arrayOf(
    "شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه",
)

/** Weekday name in Persian for this date (display only; the Iranian week starts Saturday). */
fun LocalDate.dayOfWeekFa(): String = when (dayOfWeek) {
    DayOfWeek.SATURDAY -> WEEKDAY_NAMES_FA[0]
    DayOfWeek.SUNDAY -> WEEKDAY_NAMES_FA[1]
    DayOfWeek.MONDAY -> WEEKDAY_NAMES_FA[2]
    DayOfWeek.TUESDAY -> WEEKDAY_NAMES_FA[3]
    DayOfWeek.WEDNESDAY -> WEEKDAY_NAMES_FA[4]
    DayOfWeek.THURSDAY -> WEEKDAY_NAMES_FA[5]
    DayOfWeek.FRIDAY -> WEEKDAY_NAMES_FA[6]
}

/** Persian month names (display only). */
val JALALI_MONTH_NAMES_FA = listOf(
    "فروردین", "اردیبهشت", "خرداد", "تیر", "مرداد", "شهریور",
    "مهر", "آبان", "آذر", "دی", "بهمن", "اسفند",
)

fun JalaliDate.monthNameFa(): String = JALALI_MONTH_NAMES_FA[month - 1]

private const val PERSIAN_ZERO = '\u06F0'

/** Map ASCII digits to Persian digits (U+06F0..U+06F9); leave everything else untouched. */
fun String.toPersianDigits(): String = buildString(length) {
    for (ch in this@toPersianDigits) {
        append(if (ch in '0'..'9') PERSIAN_ZERO + (ch - '0') else ch)
    }
}

/** Map Persian (U+06F0) and Arabic-Indic (U+0660) digits to ASCII; used for display round-trips. */
fun String.toLatinDigits(): String = buildString(length) {
    for (ch in this@toLatinDigits) {
        append(
            when (ch) {
                in '\u06F0'..'\u06F9' -> '0' + (ch - '\u06F0')
                in '\u0660'..'\u0669' -> '0' + (ch - '\u0660')
                else -> ch
            }
        )
    }
}

