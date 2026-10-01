package com.warrior.tracker.core.validation

import com.warrior.tracker.core.common.WarriorError
import com.warrior.tracker.core.common.WarriorResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * ARCHITECTURE.md sec.15 — the input range table is enforced exactly here.
 *
 * Every bound is asserted from BOTH sides. The Phase-0 suite only asserted `TOO_LARGE` for
 * below-minimum inputs, which locked in the defect instead of catching it: a user typing
 * `reps = 0` was told the value was "too large".
 */
class InputValidatorTest {

    private fun reason(r: WarriorResult<Unit>): WarriorError.Reason {
        r as WarriorResult.Failure
        val e = r.error as WarriorError.Validation
        return e.reason
    }

    private fun field(r: WarriorResult<Unit>): String {
        r as WarriorResult.Failure
        return (r.error as WarriorError.Validation).field
    }

    private fun assertOk(r: WarriorResult<Unit>) {
        assertTrue("expected Success but was $r", r is WarriorResult.Success)
    }

    // ---- Reps: integer 1..999 ----

    @Test
    fun repsMustBeInRange() {
        assertOk(InputValidator.validateReps(1))
        assertOk(InputValidator.validateReps(5))
        assertOk(InputValidator.validateReps(999))
        assertEquals(WarriorError.Reason.REQUIRED, reason(InputValidator.validateReps(null)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateReps(0)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateReps(-5)))
        assertEquals(WarriorError.Reason.TOO_LARGE, reason(InputValidator.validateReps(1000)))
        assertEquals("reps", field(InputValidator.validateReps(0)))
    }

    // ---- external_load_kg: 0..1000, max two decimals ----

    @Test
    fun externalLoadRangeAndPrecision() {
        assertOk(InputValidator.validateExternalLoadKg(0.0))  // dead hang / BW+ with no plate
        assertOk(InputValidator.validateExternalLoadKg(2.5))
        assertOk(InputValidator.validateExternalLoadKg(1000.0))
        assertOk(InputValidator.validateExternalLoadKg(100.25))
        assertEquals(WarriorError.Reason.REQUIRED, reason(InputValidator.validateExternalLoadKg(null)))
        assertEquals(WarriorError.Reason.INVALID_FORMAT, reason(InputValidator.validateExternalLoadKg(2.505)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateExternalLoadKg(-0.5)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateExternalLoadKg(-100.0)))
        assertEquals(WarriorError.Reason.TOO_LARGE, reason(InputValidator.validateExternalLoadKg(1000.5)))
        assertEquals("external_load_kg", field(InputValidator.validateExternalLoadKg(-1.0)))
    }

    /**
     * Every ordered comparison against NaN is false, so `NaN < 0 || NaN > 1000` passes. Without an
     * explicit finiteness guard a NaN weight would be written straight into the database.
     */
    @Test
    fun nonFiniteValuesAreRejectedAsInvalidFormat() {
        assertEquals(WarriorError.Reason.INVALID_FORMAT, reason(InputValidator.validateExternalLoadKg(Double.NaN)))
        assertEquals(WarriorError.Reason.INVALID_FORMAT, reason(InputValidator.validateBodyWeightKg(Double.NaN)))
        assertEquals(WarriorError.Reason.INVALID_FORMAT, reason(InputValidator.validateRpe(Double.NaN)))
        assertEquals(WarriorError.Reason.TOO_LARGE, reason(InputValidator.validateExternalLoadKg(Double.POSITIVE_INFINITY)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateExternalLoadKg(Double.NEGATIVE_INFINITY)))
    }

    // ---- Hold duration: 1..3600 s ----

    @Test
    fun holdDurationRange() {
        assertOk(InputValidator.validateHoldDurationSec(1))
        assertOk(InputValidator.validateHoldDurationSec(45))
        assertOk(InputValidator.validateHoldDurationSec(3600))
        assertEquals(WarriorError.Reason.REQUIRED, reason(InputValidator.validateHoldDurationSec(null)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateHoldDurationSec(0)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateHoldDurationSec(-1)))
        assertEquals(WarriorError.Reason.TOO_LARGE, reason(InputValidator.validateHoldDurationSec(3601)))
    }

    // ---- RPE: 1..10 step 0.5, optional ----

    @Test
    fun rpeOptionalHalfStep() {
        assertOk(InputValidator.validateRpe(null))
        assertOk(InputValidator.validateRpe(1.0))
        assertOk(InputValidator.validateRpe(7.5))
        assertOk(InputValidator.validateRpe(10.0))
        assertEquals(WarriorError.Reason.INVALID_FORMAT, reason(InputValidator.validateRpe(7.3)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateRpe(0.5)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateRpe(-1.0)))
        assertEquals(WarriorError.Reason.TOO_LARGE, reason(InputValidator.validateRpe(10.5)))
    }

    // ---- Round intensity: integer 1..10, optional ----

    @Test
    fun roundIntensityOptionalRange() {
        assertOk(InputValidator.validateRoundIntensity(null))
        assertOk(InputValidator.validateRoundIntensity(1))
        assertOk(InputValidator.validateRoundIntensity(10))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateRoundIntensity(0)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateRoundIntensity(-3)))
        assertEquals(WarriorError.Reason.TOO_LARGE, reason(InputValidator.validateRoundIntensity(11)))
    }

    // ---- Round duration: 10 s .. 60 min, optional ----

    @Test
    fun roundDurationOptionalRange() {
        assertOk(InputValidator.validateRoundDurationSec(null))  // planned-only round
        assertOk(InputValidator.validateRoundDurationSec(10))
        assertOk(InputValidator.validateRoundDurationSec(3600))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateRoundDurationSec(9)))
        assertEquals(WarriorError.Reason.TOO_LARGE, reason(InputValidator.validateRoundDurationSec(3601)))
    }

    // ---- Rest: 0 s .. 60 min, optional ----

    @Test
    fun restOptionalRange() {
        assertOk(InputValidator.validateRestSec(null))
        assertOk(InputValidator.validateRestSec(0))
        assertOk(InputValidator.validateRestSec(3600))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateRestSec(-1)))
        assertEquals(WarriorError.Reason.TOO_LARGE, reason(InputValidator.validateRestSec(3601)))
    }

    // ---- Body weight: 20..350 kg ----

    @Test
    fun bodyWeightRange() {
        assertOk(InputValidator.validateBodyWeightKg(20.0))
        assertOk(InputValidator.validateBodyWeightKg(80.5))
        assertOk(InputValidator.validateBodyWeightKg(350.0))
        assertEquals(WarriorError.Reason.REQUIRED, reason(InputValidator.validateBodyWeightKg(null)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateBodyWeightKg(19.0)))
        assertEquals(WarriorError.Reason.TOO_SMALL, reason(InputValidator.validateBodyWeightKg(0.0)))
        assertEquals(WarriorError.Reason.TOO_LARGE, reason(InputValidator.validateBodyWeightKg(351.0)))
    }

    // ---- Bounds are declared once, next to the sec.15 table ----

    @Test
    fun declaredBoundsMatchTheSpecTable() {
        assertEquals(1, InputValidator.MIN_REPS)
        assertEquals(999, InputValidator.MAX_REPS)
        assertEquals(0.0, InputValidator.MIN_LOAD_KG, 0.0)
        assertEquals(1000.0, InputValidator.MAX_LOAD_KG, 0.0)
        assertEquals(1, InputValidator.MIN_HOLD_SEC)
        assertEquals(3600, InputValidator.MAX_HOLD_SEC)
        assertEquals(1.0, InputValidator.MIN_RPE, 0.0)
        assertEquals(10.0, InputValidator.MAX_RPE, 0.0)
        assertEquals(1, InputValidator.MIN_INTENSITY)
        assertEquals(10, InputValidator.MAX_INTENSITY)
        assertEquals(10, InputValidator.MIN_ROUND_SEC)
        assertEquals(3600, InputValidator.MAX_ROUND_SEC)
        assertEquals(0, InputValidator.MIN_REST_SEC)
        assertEquals(3600, InputValidator.MAX_REST_SEC)
        assertEquals(20.0, InputValidator.MIN_BODY_WEIGHT_KG, 0.0)
        assertEquals(350.0, InputValidator.MAX_BODY_WEIGHT_KG, 0.0)
    }
}
