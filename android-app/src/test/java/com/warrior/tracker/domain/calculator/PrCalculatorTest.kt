package com.warrior.tracker.domain.calculator

import com.warrior.tracker.core.common.PRType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** ARCHITECTURE.md sec.7.3 — Epley e1RM and strict PR detection. */
class PrCalculatorTest {

    @Test
    fun `epley single rep equals lifted weight`() {
        assertEquals(100.0, Epley.e1rm(100.0, 1), 1e-9)
    }

    @Test
    fun `epley ten reps scales by one third`() {
        // 100 * (1 + 10/30) = 133.333...
        assertEquals(133.33333333333334, Epley.e1rm(100.0, 10), 1e-6)
    }

    @Test(expected = IllegalArgumentException::class)
    fun `epley rejects zero reps`() {
        Epley.e1rm(100.0, 0)
    }

    @Test
    fun `ties are not new records`() {
        assertTrue(PrCalculator.isNewRecord(101.0, 100.0))
        assertFalse(PrCalculator.isNewRecord(100.0, 100.0))
        assertFalse(PrCalculator.isNewRecord(99.0, 100.0))
        assertTrue(PrCalculator.isNewRecord(50.0, null)) // first-ever record
    }

    @Test
    fun bestE1rmOnlyAppliesToLowReps() {
        assertEquals(100.0, PrCalculator.prValue(PRType.BEST_E1RM, true, false, 100.0, 1, null)!!, 1e-9)
        assertEquals(140.0, PrCalculator.prValue(PRType.BEST_E1RM, true, false, 100.0, 12, null)!!, 1e-9)
        assertNull(PrCalculator.prValue(PRType.BEST_E1RM, true, false, 100.0, 15, null))
        assertNull(PrCalculator.prValue(PRType.BEST_E1RM, true, true, 100.0, 5, 30))
    }

    @Test
    fun `max weight requires weighted load type and at least one rep`() {
        assertEquals(120.0, PrCalculator.prValue(PRType.MAX_WEIGHT, true, false, 120.0, 3, null)!!, 1e-9)
        assertNull(PrCalculator.prValue(PRType.MAX_WEIGHT, false, false, 120.0, 3, null))
        assertNull(PrCalculator.prValue(PRType.MAX_WEIGHT, true, false, 120.0, null, null))
    }

    /**
     * e1RM is a load-based metric (the Epley `w` IS the external load). Without the load-type
     * guard, every BODYWEIGHT/ASSISTED exercise produced value-0 PR rows that can never be beaten
     * (ties are not records), permanently polluting the pr_events cache (sec.10.13).
     */
    @Test
    fun `bestE1rm requires an applicable load type`() {
        assertNull(PrCalculator.prValue(PRType.BEST_E1RM, loadTypeApplies = false, measureTypeIsDuration = false, externalLoadKg = 0.0, reps = 8, durationSec = null))
        assertEquals(126.66666666666667, PrCalculator.prValue(PRType.BEST_E1RM, true, false, 100.0, 8, null)!!, 1e-9)
    }

    /** sec.7.3 restricts BEST_SET_VOLUME to WEIGHTED / BODYWEIGHT_PLUS reps-based sets. */
    @Test
    fun `bestSetVolume excludes duration sets and non-applicable load types`() {
        assertEquals(500.0, PrCalculator.prValue(PRType.BEST_SET_VOLUME, true, false, 100.0, 5, null)!!, 1e-9)
        assertNull(PrCalculator.prValue(PRType.BEST_SET_VOLUME, false, false, 100.0, 5, null))
        assertNull(PrCalculator.prValue(PRType.BEST_SET_VOLUME, true, true, 0.0, 5, 45))
        assertNull(PrCalculator.prValue(PRType.BEST_SET_VOLUME, true, false, 100.0, null, null))
    }

    /** sec.7.3: REPS_AT_WEIGHT applies to every reps-based exercise, bodyweight included. */
    @Test
    fun `repsAtWeightAppliesToBodyweightButNotToDurationSets`() {
        assertEquals(15.0, PrCalculator.prValue(PRType.REPS_AT_WEIGHT, false, false, 0.0, 15, null)!!, 1e-9)
        assertNull(PrCalculator.prValue(PRType.REPS_AT_WEIGHT, true, true, 0.0, 15, 30))
        assertNull(PrCalculator.prValue(PRType.REPS_AT_WEIGHT, true, false, 0.0, null, 30))
    }

    /** Epley and the BEST_E1RM PR type must never disagree (sec.7.3 note: reps = 1 -> w). */
    @Test
    fun `bestE1rm agrees with the Epley formula for every rep count in range`() {
        for (reps in 1..12) {
            val weight = 80.0 + reps
            assertEquals(
                "reps=$reps",
                Epley.e1rm(weight, reps),
                PrCalculator.prValue(PRType.BEST_E1RM, true, false, weight, reps, null)!!,
                1e-9,
            )
        }
        assertNull(PrCalculator.prValue(PRType.BEST_E1RM, true, false, 80.0, 13, null))
        assertNull(PrCalculator.prValue(PRType.BEST_E1RM, true, false, 80.0, 0, null))
    }

    @Test
    fun `max duration only for duration measures`() {
        assertEquals(45.0, PrCalculator.prValue(PRType.MAX_DURATION, false, true, 0.0, null, 45)!!, 1e-9)
        assertNull(PrCalculator.prValue(PRType.MAX_DURATION, true, false, 0.0, 10, null))
    }
}
