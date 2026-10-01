package com.warrior.tracker.core.validation

import com.warrior.tracker.core.common.WarriorError
import com.warrior.tracker.core.common.WarriorResult

/**
 * Input range rules — ARCHITECTURE.md sec.15 table. Single source of truth used by
 * every UseCase before touching Room; UI shows the returned reason as a localized message.
 */
object InputValidator {

    fun validateReps(reps: Int?): WarriorResult<Unit> = when {
        reps == null -> fail("reps", WarriorError.Reason.REQUIRED)
        reps < 1 || reps > 999 -> fail("reps", WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    fun validateExternalLoadKg(kg: Double?): WarriorResult<Unit> = when {
        kg == null -> fail("external_load_kg", WarriorError.Reason.REQUIRED)
        kg < 0.0 || kg > 1000.0 -> fail("external_load_kg", WarriorError.Reason.TOO_LARGE)
        !isTwoDecimalPrecision(kg) -> fail("external_load_kg", WarriorError.Reason.INVALID_FORMAT)
        else -> ok
    }

    fun validateHoldDurationSec(sec: Int?): WarriorResult<Unit> = when {
        sec == null -> fail("duration_sec", WarriorError.Reason.REQUIRED)
        sec < 1 || sec > 3600 -> fail("duration_sec", WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    /** RPE and session_rpe share the same scale: 1..10 step 0.5 (sec.15). Null = not provided (valid). */
    fun validateRpe(value: Double?): WarriorResult<Unit> = when {
        value == null -> ok
        value < 1.0 || value > 10.0 -> fail("rpe", WarriorError.Reason.TOO_LARGE)
        !isHalfStep(value) -> fail("rpe", WarriorError.Reason.INVALID_FORMAT)
        else -> ok
    }

    fun validateRoundIntensity(value: Int?): WarriorResult<Unit> = when {
        value == null -> ok // optional
        value < 1 || value > 10 -> fail("intensity", WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    fun validateRoundDurationSec(sec: Int?): WarriorResult<Unit> = when {
        sec == null -> ok // planned-only rounds allowed
        sec < 10 || sec > 3600 -> fail("round_duration", WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    fun validateRestSec(sec: Int?): WarriorResult<Unit> = when {
        sec == null -> ok
        sec < 0 || sec > 3600 -> fail("rest", WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    fun validateBodyWeightKg(kg: Double?): WarriorResult<Unit> = when {
        kg == null -> fail("body_weight_kg", WarriorError.Reason.REQUIRED)
        kg < 20.0 || kg > 350.0 -> fail("body_weight_kg", WarriorError.Reason.TOO_LARGE)
        else -> ok
    }

    private fun isHalfStep(v: Double): Boolean =
        kotlin.math.abs((v * 2) - Math.round(v * 2)) < 1e-9

    private fun isTwoDecimalPrecision(v: Double): Boolean =
        kotlin.math.abs((v * 100) - Math.round(v * 100)) < 1e-6

    private val ok: WarriorResult<Unit> = WarriorResult.Success(Unit)
    private fun fail(field: String, reason: WarriorError.Reason) =
        WarriorResult.Failure(WarriorError.Validation(field, reason))
}
