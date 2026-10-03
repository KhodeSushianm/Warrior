package com.warrior.tracker.domain.calculator

import com.warrior.tracker.core.common.LoadType
import com.warrior.tracker.core.common.MeasureType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

/** ARCHITECTURE.md sec.7.1 / sec.16 — volume & tonnage rules. */
class VolumeCalculatorTest {

    private fun setOf(
        measure: MeasureType = MeasureType.REPS,
        load: Double = 100.0,
        reps: Int? = 5,
        dur: Int? = null,
    ) = VolumeCalculator.SetLike(measure, load, reps, dur)

    @Test
    fun `weighted set volume is load times reps`() {
        assertEquals(
            500.0,
            VolumeCalculator.setVolume(LoadType.WEIGHTED, MeasureType.REPS, 100.0, 5, null),
            1e-9,
        )
    }

    @Test
    fun `bodyweight and assisted contribute zero tonnage`() {
        assertEquals(0.0, VolumeCalculator.setVolume(LoadType.BODYWEIGHT, MeasureType.REPS, 0.0, 12, null), 1e-9)
        assertEquals(0.0, VolumeCalculator.setVolume(LoadType.ASSISTED, MeasureType.REPS, 0.0, 8, null), 1e-9)
    }

    @Test
    fun `bodyweight plus contributes only added external load`() {
        assertEquals(30.0, VolumeCalculator.setVolume(LoadType.BODYWEIGHT_PLUS, MeasureType.REPS, 10.0, 3, null), 1e-9)
    }

    @Test
    fun `duration sets have zero volume but count in time`() {
        val sets = listOf(setOf(MeasureType.DURATION, 0.0, null, 45))
        assertEquals(0.0, VolumeCalculator.tonnage(sets, LoadType.WEIGHTED), 1e-9)
        assertEquals(45, VolumeCalculator.totalTimeSec(sets))
        assertEquals(0, VolumeCalculator.totalReps(sets))
    }

    @Test
    fun `tonnage sums across sets`() {
        val sets = listOf(setOf(load = 80.0, reps = 8), setOf(load = 100.0, reps = 5))
        assertEquals(1140.0, VolumeCalculator.tonnage(sets, LoadType.WEIGHTED), 1e-9)
    }

    @Test
    fun `effective volume for bodyweight plus needs known body weight else null`() {
        val sets = listOf(setOf(load = 10.0, reps = 5))
        assertNull(VolumeCalculator.effectiveVolume(sets, LoadType.BODYWEIGHT_PLUS, null))
        // (80 + 10) * 5
        assertEquals(450.0, VolumeCalculator.effectiveVolume(sets, LoadType.BODYWEIGHT_PLUS, 80.0)!!, 1e-9)
        // non-BW+ exercises return null (not applicable), never a guess
        assertNull(VolumeCalculator.effectiveVolume(sets, LoadType.WEIGHTED, 80.0))
    }
}
