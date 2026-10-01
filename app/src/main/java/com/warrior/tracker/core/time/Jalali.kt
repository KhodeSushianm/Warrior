package com.warrior.tracker.core.time

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Jalali (Persian solar) calendar support — display layer only.
 * Storage stays ISO Gregorian `local_date` + UTC epoch millis (ARCHITECTURE.md sec.6.4, sec.13).
 *
 * Implementation: official Iranian arithmetic calendar, anchored by a table of
 * 1 Farvardin (Nowruz) Gregorian dates for AP years 1340..1450 (1961-2071 CE),
 * generated from the reference jdatetime algorithm and verified against known
 * Nowruz dates (1403→2024-03-20, 1404→2025-03-21, 1405→2026-03-21).
 * Pure Kotlin/JVM → unit-testable and identical on every device (no android.icu).
 */
data class JalaliDate(val year: Int, val month: Int, val dayOfMonth: Int) {
    /** e.g. "1404/07/09" or with Persian digits when [persianDigits] is true. */
    fun format(persianDigits: Boolean = true): String {
        val s = "%04d/%02d/%02d".format(year, month, dayOfMonth)
        return if (persianDigits) s.toPersianDigits() else s
    }
}

private val FARVARDIN_1 = listOf(
"1961-03-21",
        "1962-03-21",
        "1963-03-21",
        "1964-03-21",
        "1965-03-21",
        "1966-03-21",
        "1967-03-21",
        "1968-03-21",
        "1969-03-21",
        "1970-03-21",
        "1971-03-21",
        "1972-03-21",
        "1973-03-21",
        "1974-03-21",
        "1975-03-21",
        "1976-03-21",
        "1977-03-21",
        "1978-03-21",
        "1979-03-21",
        "1980-03-21",
        "1981-03-21",
        "1982-03-21",
        "1983-03-21",
        "1984-03-21",
        "1985-03-21",
        "1986-03-21",
        "1987-03-21",
        "1988-03-21",
        "1989-03-21",
        "1990-03-21",
        "1991-03-21",
        "1992-03-21",
        "1993-03-21",
        "1994-03-21",
        "1995-03-21",
        "1996-03-20",
        "1997-03-21",
        "1998-03-21",
        "1999-03-21",
        "2000-03-20",
        "2001-03-21",
        "2002-03-21",
        "2003-03-21",
        "2004-03-20",
        "2005-03-21",
        "2006-03-21",
        "2007-03-21",
        "2008-03-20",
        "2009-03-21",
        "2010-03-21",
        "2011-03-21",
        "2012-03-20",
        "2013-03-21",
        "2014-03-21",
        "2015-03-21",
        "2016-03-20",
        "2017-03-21",
        "2018-03-21",
        "2019-03-21",
        "2020-03-20",
        "2021-03-21",
        "2022-03-21",
        "2023-03-21",
        "2024-03-20",
        "2025-03-21",
        "2026-03-21",
        "2027-03-21",
        "2028-03-20",
        "2029-03-20",
        "2030-03-21",
        "2031-03-21",
        "2032-03-20",
        "2033-03-20",
        "2034-03-21",
        "2035-03-21",
        "2036-03-20",
        "2037-03-20",
        "2038-03-21",
        "2039-03-21",
        "2040-03-20",
        "2041-03-20",
        "2042-03-21",
        "2043-03-21",
        "2044-03-20",
        "2045-03-20",
        "2046-03-21",
        "2047-03-21",
        "2048-03-20",
        "2049-03-20",
        "2050-03-21",
        "2051-03-21",
        "2052-03-20",
        "2053-03-20",
        "2054-03-21",
        "2055-03-21",
        "2056-03-20",
        "2057-03-20",
        "2058-03-21",
        "2059-03-21",
        "2060-03-20",
        "2061-03-20",
        "2062-03-20",
        "2063-03-21",
        "2064-03-20",
        "2065-03-20",
        "2066-03-20",
        "2067-03-21",
        "2068-03-20",
        "2069-03-20",
        "2070-03-20",
        "2071-03-21")
private const val FARVARDIN_FIRST_YEAR = 1340

private val MONTH_LENGTHS_NORMAL = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 29)
private val MONTH_LENGTHS_LEAP = intArrayOf(31, 31, 31, 31, 31, 31, 30, 30, 30, 30, 30, 30)

/** True when AP year [jy] has 30 days in Esfand (leap). Derived from anchor gaps. */
internal fun isJalaliLeapYear(jy: Int): Boolean {
    val idx = jy - FARVARDIN_FIRST_YEAR
    if (idx < 0 || idx + 1 >= FARVARDIN_1.size) return jalaliLeapByCycle(jy)
    val start = LocalDate.parse(FARVARDIN_1[idx])
    val next = LocalDate.parse(FARVARDIN_1[idx + 1])
    return ChronoUnit.DAYS.between(start, next) == 366L
}

/** Fallback 2820-cycle rule for years outside the table range. */
private fun jalaliLeapByCycle(jy: Int): Boolean {
    val breaks = intArrayOf(-61, 9, 38, 199, 426, 686, 756, 818, 1111, 1181, 1210, 1635, 2060, 2097, 2192, 2262, 2324, 2394, 2456, 3178)
    var y = jy; var leapJ = -14; var gp = 0
    for (b in breaks) {
        if (y < b) break
        gp = b; y -= b; leapJ += y / 128; y %= 128
    }
    if (gp > 0 && y >= 0) y--
    return leapJ + y / 4 - y % 128 / 32 >= 0
}

/**
 * Convert a Gregorian date to its official Iranian (Jalali) equivalent.
 * Inside the table window (1961–2071 CE) the conversion is exact by definition
 * (anchor-driven day counting). Outside it, a 2820-year arithmetic extension is
 * used — acceptable for a personal tracker whose data lives in the table window.
 */
fun LocalDate.toJalali(): JalaliDate {
    val firstAnchor = LocalDate.parse(FARVARDIN_1.first())
    val lastAnchor = LocalDate.parse(FARVARDIN_1.last())
    if (this.isBefore(firstAnchor) || this.isAfter(lastAnchor.plusDays(366))) {
        return toJalaliExtended(this)
    }
    var idx = 0
    while (idx + 1 < FARVARDIN_1.size && !this.isBefore(LocalDate.parse(FARVARDIN_1[idx + 1]))) idx++
    val jy = FARVARDIN_FIRST_YEAR + idx
    val doy = ChronoUnit.DAYS.between(LocalDate.parse(FARVARDIN_1[idx]), this).toInt()
    return dayOfYearToJalali(jy, doy)
}

private fun dayOfYearToJalali(jy: Int, doy: Int): JalaliDate {
    val lens = if (isJalaliLeapYear(jy)) MONTH_LENGTHS_LEAP else MONTH_LENGTHS_NORMAL
    var rem = doy
    var m = 0
    while (m < 12 && rem >= lens[m]) { rem -= lens[m]; m++ }
    return JalaliDate(jy, m + 1, rem + 1)
}

/** Arithmetic 2820-cycle conversion for dates outside the anchored table window. */
private fun toJalaliExtended(g: LocalDate): JalaliDate {
    // Estimate year from mean tropical year relative to a known anchor (1340 AP → 1961-03-21).
    val anchorG = LocalDate.parse(FARVARDIN_1.first())
    val days = ChronoUnit.DAYS.between(anchorG, g)
    var jy = FARVARDIN_FIRST_YEAR + (days / 365.242188).toInt()
    // Correct ±1 year using leap-aware year lengths.
    while (jalaliYearStart(jy).isAfter(g)) jy--
    while (!jalaliYearStart(jy + 1).isAfter(g)) jy++
    val doy = ChronoUnit.DAYS.between(jalaliYearStart(jy), g).toInt()
    return dayOfYearToJalali(jy, maxOf(doy, 0))
}

/**
 * Gregorian date of 1 Farvardin of AP year [jy] under the 2820-cycle extension.
 * Days offset = 365·Δyears + (#leap years between jy and the anchor year), signed.
 */
private fun jalaliYearStart(jy: Int): LocalDate {
    val anchorG = LocalDate.parse(FARVARDIN_1.first())
    val anchorY = FARVARDIN_FIRST_YEAR
    val leapsBetween = when {
        jy > anchorY -> countLeapsRange(anchorY, jy)          // leap years in [anchorY, jy)
        jy < anchorY -> -countLeapsRange(jy, anchorY)         // leap years in [jy, anchorY), negated
        else -> 0
    }
    val days = (jy - anchorY) * 365L + leapsBetween
    return anchorG.plusDays(days)
}

private fun countLeapsRange(fromInclusive: Int, toExclusive: Int): Int {
    var c = 0
    var y = fromInclusive
    while (y < toExclusive) {
        if (jalaliLeapByCycle(y)) c++
        y++
    }
    return c
}

/** Weekday name in Persian for this date (display only; Iranian week starts Saturday). */
fun LocalDate.dayOfWeekFa(): String {
    val names = arrayOf("شنبه", "یک‌شنبه", "دوشنبه", "سه‌شنبه", "چهارشنبه", "پنج‌شنبه", "جمعه")
    return when (dayOfWeek) {
        DayOfWeek.SATURDAY -> names[0]
        DayOfWeek.SUNDAY -> names[1]
        DayOfWeek.MONDAY -> names[2]
        DayOfWeek.TUESDAY -> names[3]
        DayOfWeek.WEDNESDAY -> names[4]
        DayOfWeek.THURSDAY -> names[5]
        DayOfWeek.FRIDAY -> names[6]
    }
}

private const val PERSIAN_ZERO = '\u06F0'

fun String.toPersianDigits(): String =
    map { if (it in '0'..'9') PERSIAN_ZERO + (it - '0') else it }.joinToString("")
