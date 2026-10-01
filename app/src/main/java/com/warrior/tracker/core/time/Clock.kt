package com.warrior.tracker.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Injectable clock — domain code never calls System.currentTimeMillis directly,
 * so calculators and UseCases stay deterministic under test (sec.16).
 */
interface Clock {
    /** Epoch millis (UTC). */
    fun nowMillis(): Long

    /** Zone-aware wall-clock helpers use the device default zone by default. */
    fun now(zone: ZoneId = ZoneId.systemDefault()): Instant =
        Instant.ofEpochMilli(nowMillis())

    fun localDateToday(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        LocalDate.ofInstant(now(zone), zone)
}

/** Production clock: epoch millis + elapsedRealtime anchors for timers (sec.8.2). */
class SystemClockImpl : Clock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
