package com.warrior.tracker.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset

/**
 * Deterministic clock for tests (sec.16: "TimerEngine با Clock جعلی").
 * Lives in the test source set so it never ships in the release APK.
 */
class FixedClock(
    private var millis: Long,
    private val zone: ZoneId = ZoneOffset.UTC,
) : Clock {

    constructor(instant: Instant, zone: ZoneId = ZoneOffset.UTC) : this(instant.toEpochMilli(), zone)

    override fun nowMillis(): Long = millis

    override fun zoneId(): ZoneId = zone

    /** Advance the clock; returns this so assertions can chain. */
    fun advanceBy(deltaMillis: Long): FixedClock = apply { millis += deltaMillis }

    fun advanceSeconds(seconds: Long): FixedClock = advanceBy(seconds * 1000L)

    fun setTo(newMillis: Long): FixedClock = apply { millis = newMillis }
}
