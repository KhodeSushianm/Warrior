package com.warrior.tracker.core.time

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * sec.16 mandatory edge case: "جابه‌جایی Timezone".
 *
 * `localDateToday` produces the value persisted as `workouts.local_date` (sec.10.1), so a
 * timezone mistake silently files workouts under the wrong calendar day and corrupts History,
 * the Calendar and every daily stats-cache row.
 *
 * It also pins the API-level fix: the Phase-0 implementation used `LocalDate.ofInstant`, which
 * only exists on Android from API 34 and therefore crashed with NoSuchMethodError on every device
 * below Android 14 (minSdk is 26). `Instant.atZone(...).toLocalDate()` is API 26. The crash itself
 * can only be observed on a device, so `NewApi` is promoted to a lint *error* and lint runs in CI.
 */
class ClockTest {

    private val tehran: ZoneId = ZoneId.of("Asia/Tehran")   // UTC+03:30, no DST since 2022
    private val utc: ZoneId = ZoneOffset.UTC

    @Test
    fun `the same instant is a different local date in different zones`() {
        // 2026-10-01T21:00Z is already 2026-10-02T00:30 in Tehran.
        val instant = Instant.parse("2026-10-01T21:00:00Z")
        val clock = FixedClock(instant)
        assertEquals(LocalDate.of(2026, 10, 1), clock.localDateToday(utc))
        assertEquals(LocalDate.of(2026, 10, 2), clock.localDateToday(tehran))
        assertNotEquals(clock.localDateToday(utc), clock.localDateToday(tehran))
    }

    @Test
    fun `localDateToday agrees with java time for a full day across zones`() {
        // Walk a whole day hour by hour in several zones and compare against an independent
        // computation, so an off-by-one at midnight cannot hide.
        val zones = listOf(utc, tehran, ZoneId.of("America/New_York"), ZoneId.of("Pacific/Kiritimati"))
        var instant = Instant.parse("2026-03-19T00:00:00Z")
        repeat(48) {
            val clock = FixedClock(instant)
            for (z in zones) {
                assertEquals(
                    "zone=$z instant=$instant",
                    instant.atZone(z).toLocalDate(),
                    clock.localDateToday(z),
                )
            }
            instant = instant.plusSeconds(3600)
        }
    }

    @Test
    fun `nowMillis and now stay in sync and follow the injected clock`() {
        val clock = FixedClock(0L)
        assertEquals(0L, clock.nowMillis())
        assertEquals(Instant.EPOCH, clock.now())
        clock.advanceSeconds(90)
        assertEquals(90_000L, clock.nowMillis())
        assertEquals(Instant.ofEpochSecond(90), clock.now())
        clock.setTo(1_000L)
        assertEquals(1_000L, clock.nowMillis())
    }

    @Test
    fun `a workout started just before midnight is filed on the previous day`() {
        // The exact scenario sec.8.1's draft persistence has to get right.
        val beforeMidnight = Instant.parse("2026-09-30T20:29:59Z") // 23:59:59 in Tehran
        val afterMidnight = Instant.parse("2026-09-30T20:30:00Z")  // 00:00:00 Oct 1 in Tehran
        assertEquals(LocalDate.of(2026, 9, 30), FixedClock(beforeMidnight).localDateToday(tehran))
        assertEquals(LocalDate.of(2026, 10, 1), FixedClock(afterMidnight).localDateToday(tehran))
    }

    @Test
    fun `local_date round-trips through the Jalali display layer`() {
        // Storage is Gregorian (sec.10.1); display is Jalali (sec.13). The pair must agree.
        val clock = FixedClock(Instant.parse("2026-10-01T05:00:00Z"), tehran)
        val stored = clock.localDateToday(tehran)
        assertEquals(LocalDate.of(2026, 10, 1), stored)
        assertEquals(JalaliDate(1405, 7, 9), stored.toJalali())
        assertEquals(stored, stored.toJalali().toLocalDate())
        assertTrue(clock.zoneId().id.isNotBlank())
    }
}
