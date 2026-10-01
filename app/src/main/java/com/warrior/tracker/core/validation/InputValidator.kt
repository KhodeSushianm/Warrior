package com.warrior.tracker.core.validation

import com.warrior.tracker.core.common.WarriorError
import com.warrior.tracker.core.common.WarriorResult

/**
 * Input range rules — ARCHITECTURE.md sec.15 table. Single source of truth used by
 * every UseCase before touching Room; UI shows the returned reason as a localized message.
 *
 * `Reason` must match the *direction* of the violation: below the lower bound is `TOO_SMALL`,
 * above the upper bound is `TOO_LARGE`. The UI renders these as different sentences, so reporting
 * "too large" for `reps = 0` would show the user a factually wrong message (sec.15: "پیام قابل‌فهم").
 *
 * NaN is rejected as `INVALID_FORMAT` before the range checks: every ordered comparison against
 * NaN is false, so `NaN < 0 || NaN > 1000` is `false` and a NaN would otherwise be persisted.
 * Infinities are left to the range checks, which classify them correctly as TOO_SMALL/TOO_LARGE.
 */
object InputValidator {

    /** Reps: integer 1..999 (sec.15). */
    fun validateReps(reps: Int?): WarriorResult<Unit> = when {
        reps == null -> fail(FIELD_REPS, WarriorError.Reason.REQUIRED)
        reps < MIN_REPS -> fail(FIELD_REPS, WarriorError.Reason.TOO_SMALL)
        reps > MAX_REPS -> fail(FIELD_REPS, WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    /** external_load_kg: 0..1000, at most two decimals (sec.15). */
    fun validateExternalLoadKg(kg: Double?): WarriorResult<Unit> = when {
        kg == null -> fail(FIELD_LOAD, WarriorError.Reason.REQUIRED)
        kg.isNaN() -> fail(FIELD_LOAD, WarriorError.Reason.INVALID_FORMAT)
        kg < MIN_LOAD_KG -> fail(FIELD_LOAD, WarriorError.Reason.TOO_SMALL)
        kg > MAX_LOAD_KG -> fail(FIELD_LOAD, WarriorError.Reason.TOO_LARGE)
        !isTwoDecimalPrecision(kg) -> fail(FIELD_LOAD, WarriorError.Reason.INVALID_FORMAT)
        else -> ok
    }

    /** Hold duration: 1..3600 s (sec.15). */
    fun validateHoldDurationSec(sec: Int?): WarriorResult<Unit> = when {
        sec == null -> fail(FIELD_DURATION, WarriorError.Reason.REQUIRED)
        sec < MIN_HOLD_SEC -> fail(FIELD_DURATION, WarriorError.Reason.TOO_SMALL)
        sec > MAX_HOLD_SEC -> fail(FIELD_DURATION, WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    /** RPE and session_rpe share one scale: 1..10, step 0.5 (sec.15). Null = not provided (valid). */
    fun validateRpe(value: Double?): WarriorResult<Unit> = when {
        value == null -> ok
        value.isNaN() -> fail(FIELD_RPE, WarriorError.Reason.INVALID_FORMAT)
        value < MIN_RPE -> fail(FIELD_RPE, WarriorError.Reason.TOO_SMALL)
        value > MAX_RPE -> fail(FIELD_RPE, WarriorError.Reason.TOO_LARGE)
        !isHalfStep(value) -> fail(FIELD_RPE, WarriorError.Reason.INVALID_FORMAT)
        else -> ok
    }

    /** Round intensity: integer 1..10, optional (sec.7.4, sec.15). */
    fun validateRoundIntensity(value: Int?): WarriorResult<Unit> = when {
        value == null -> ok
        value < MIN_INTENSITY -> fail(FIELD_INTENSITY, WarriorError.Reason.TOO_SMALL)
        value > MAX_INTENSITY -> fail(FIELD_INTENSITY, WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    /** Round duration: 10 s .. 60 min, optional (planned-only rounds allowed) (sec.15). */
    fun validateRoundDurationSec(sec: Int?): WarriorResult<Unit> = when {
        sec == null -> ok
        sec < MIN_ROUND_SEC -> fail(FIELD_ROUND_DURATION, WarriorError.Reason.TOO_SMALL)
        sec > MAX_ROUND_SEC -> fail(FIELD_ROUND_DURATION, WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    /** Rest: 0 s .. 60 min, optional (sec.15). */
    fun validateRestSec(sec: Int?): WarriorResult<Unit> = when {
        sec == null -> ok
        sec < MIN_REST_SEC -> fail(FIELD_REST, WarriorError.Reason.TOO_SMALL)
        sec > MAX_REST_SEC -> fail(FIELD_REST, WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    /** Body weight: 20..350 kg (sec.15). */
    fun validateBodyWeightKg(kg: Double?): WarriorResult<Unit> = when {
        kg == null -> fail(FIELD_BODY_WEIGHT, WarriorError.Reason.REQUIRED)
        // Guards NaN: every ordered comparison against NaN is false, so without this a NaN body
        // weight passed the range check and was persisted (sec.15).
        kg.isNaN() -> fail(FIELD_BODY_WEIGHT, WarriorError.Reason.INVALID_FORMAT)
        kg < MIN_BODY_WEIGHT_KG -> fail(FIELD_BODY_WEIGHT, WarriorError.Reason.TOO_SMALL)
        kg > MAX_BODY_WEIGHT_KG -> fail(FIELD_BODY_WEIGHT, WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    // ---- bounds, exactly as tabulated in sec.15 ----

    const val MIN_REPS = 1
    const val MAX_REPS = 999

    const val MIN_LOAD_KG = 0.0
    const val MAX_LOAD_KG = 1000.0

    const val MIN_HOLD_SEC = 1
    const val MAX_HOLD_SEC = 3600

    const val MIN_RPE = 1.0
    const val MAX_RPE = 10.0

    const val MIN_INTENSITY = 1
    const val MAX_INTENSITY = 10

    const val MIN_ROUND_SEC = 10
    const val MAX_ROUND_SEC = 3600

    const val MIN_REST_SEC = 0
    const val MAX_REST_SEC = 3600

    const val MIN_BODY_WEIGHT_KG = 20.0
    const val MAX_BODY_WEIGHT_KG = 350.0

    private const val FIELD_REPS = "reps"
    private const val FIELD_LOAD = "external_load_kg"
    private const val FIELD_DURATION = "duration_sec"
    private const val FIELD_RPE = "rpe"
    private const val FIELD_INTENSITY = "intensity"
    private const val FIELD_ROUND_DURATION = "round_duration"
    private const val FIELD_REST = "rest"
    private const val FIELD_BODY_WEIGHT = "body_weight_kg"

    private fun isHalfStep(v: Double): Boolean =
        kotlin.math.abs((v * 2) - Math.round(v * 2)) < 1e-9

    private fun isTwoDecimalPrecision(v: Double): Boolean =
        kotlin.math.abs((v * 100) - Math.round(v * 100)) < 1e-6

    private val ok: WarriorResult<Unit> = WarriorResult.Success(Unit)

    private fun fail(field: String, reason: WarriorError.Reason) =
        WarriorResult.Failure(WarriorError.Validation(field, reason))
}
