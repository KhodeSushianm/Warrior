package com.warrior.tracker.core.validation

import com.warrior.tracker.core.common.WarriorError
import com.warrior.tracker.core.common.WarriorResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** ARCHITECTURE.md sec.15 — input range table is enforced exactly here. */
class InputValidatorTest {

    private fun failureReason(r: WarriorResult<Unit>): WarriorError.Reason {
        r as WarriorResult.Failure
        val e = r.error as WarriorError.Validation
        return e.reason
    }

    @Test
    fun repsMustBeInRange() {
        assertTrue(InputValidator.validateReps(5) is WarriorResult.Success)
        assertEquals(WarriorError.Reason.REQUIRED, failureReason(InputValidator.validateReps(null)))
        assertEquals(WarriorError.Reason.TOO_LARGE, failureReason(InputValidator.validateReps(0)))
        assertEquals(WarriorError.Reason.TOO_LARGE, failureReason(InputValidator.validateReps(1000)))
    }

    @Test
    fun externalLoadRangeAndPrecision() {
        assertTrue(InputValidator.validateExternalLoadKg(0.0) is WarriorResult.Success) // dead hang / BW+ plate 0 ok
        assertTrue(InputValidator.validateExternalLoadKg(2.5) is WarriorResult.Success)
        assertEquals(WarriorError.Reason.INVALID_FORMAT, failureReason(InputValidator.validateExternalLoadKg(2.505)))
        assertEquals(WarriorError.Reason.TOO_LARGE, failureReason(InputValidator.validateExternalLoadKg(-0.5)))
        assertEquals(WarriorError.Reason.TOO_LARGE, failureReason(InputValidator.validateExternalLoadKg(1001.0)))
    }

    @Test
    fun holdDurationRange() {
        assertTrue(InputValidator.validateHoldDurationSec(45) is WarriorResult.Success)
        assertEquals(WarriorError.Reason.TOO_LARGE, failureReason(InputValidator.validateHoldDurationSec(0)))
        assertEquals(WarriorError.Reason.TOO_LARGE, failureReason(InputValidator.validateHoldDurationSec(3601)))
    }

    @Test
    fun rpeOptionalHalfStep() {
        assertTrue(InputValidator.validateRpe(null) is WarriorResult.Success)
        assertTrue(InputValidator.validateRpe(7.5) is WarriorResult.Success)
        assertEquals(WarriorError.Reason.INVALID_FORMAT, failureReason(InputValidator.validateRpe(7.3)))
        assertEquals(WarriorError.Reason.TOO_LARGE, failureReason(InputValidator.validateRpe(10.5)))
    }

    @Test
    fun roundIntensityOptionalRange() {
        assertTrue(InputValidator.validateRoundIntensity(null) is WarriorResult.Success)
        assertTrue(InputValidator.validateRoundIntensity(10) is WarriorResult.Success)
        assertEquals(WarriorError.Reason.TOO_LARGE, failureReason(InputValidator.validateRoundIntensity(11)))
    }

    @Test
    fun bodyWeightRange() {
        assertTrue(InputValidator.validateBodyWeightKg(80.5) is WarriorResult.Success)
        assertEquals(WarriorError.Reason.TOO_LARGE, failureReason(InputValidator.validateBodyWeightKg(19.0)))
        assertEquals(WarriorError.Reason.REQUIRED, failureReason(InputValidator.validateBodyWeightKg(null)))
    }
}
