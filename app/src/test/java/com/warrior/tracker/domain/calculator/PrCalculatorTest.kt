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

    @Test
    fun `max duration only for duration measures`() {
        assertEquals(45.0, PrCalculator.prValue(PRType.MAX_DURATION, false, true, 0.0, null, 45)!!, 1e-9)
        assertNull(PrCalculator.prValue(PRType.MAX_DURATION, true, false, 0.0, 10, null))
    }
}
