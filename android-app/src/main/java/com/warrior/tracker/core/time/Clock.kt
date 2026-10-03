package com.warrior.tracker.core.time

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Injectable clock — domain code never calls System.currentTimeMillis directly,
 * so calculators and UseCases stay deterministic under test (sec.16).
 */
interface Clock {
    /** Epoch millis (UTC). */
    fun nowMillis(): Long

    /** The current instant. Zone-independent by definition; use [localDateToday] for wall-clock. */
    fun now(): Instant = Instant.ofEpochMilli(nowMillis())

    /**
     * Today's date in [zone] — the value persisted as `workouts.local_date` (sec.10.1).
     *
     * Deliberately `Instant.atZone(zone).toLocalDate()` and NOT `LocalDate.ofInstant(now(), zone)`:
     * `ofInstant` only exists on Android from **API 34**, so with minSdk 26 and no core library
     * desugaring it threw NoSuchMethodError on every device below Android 14 — and this is the
     * function that stamps every workout, body-weight entry and stats-cache row. `atZone` and
     * `toLocalDate` are both API 26.
     */
    fun localDateToday(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
        now().atZone(zone).toLocalDate()

    /** Current zone id, persisted as `workouts.timezone_id` (sec.10.1). */
    fun zoneId(): ZoneId = ZoneId.systemDefault()
}

/**
 * Production clock: epoch millis + elapsedRealtime anchors for timers (sec.8.2).
 *
 * The `@Inject constructor` is required: `BindsModule.bindClock` asks Hilt to provide this type,
 * and without it the graph only compiles by accident — Dagger validates a binding lazily, so the
 * failure surfaces on the first Phase-1 UseCase that actually injects [Clock].
 */
class SystemClockImpl @Inject constructor() : Clock {
    override fun nowMillis(): Long = System.currentTimeMillis()
}
