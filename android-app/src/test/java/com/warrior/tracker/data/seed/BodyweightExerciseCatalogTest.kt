package com.warrior.tracker.data.seed

import com.warrior.tracker.core.common.MeasureType
import com.warrior.tracker.core.common.Muscle
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BodyweightExerciseCatalogTest {

    @Test
    fun `catalog has stable unique keys and bilingual names`() {
        assertEquals(19, BODYWEIGHT_EXERCISES.size)
        assertEquals(BODYWEIGHT_EXERCISES.size, BODYWEIGHT_EXERCISES.map { it.key }.toSet().size)
        BODYWEIGHT_EXERCISES.forEach { exercise ->
            assertTrue(exercise.key.matches(Regex("[a-z0-9_]+")))
            assertTrue(exercise.nameEn.isNotBlank())
            assertTrue(exercise.nameFa.isNotBlank())
            assertTrue(exercise.primaryMuscle !in exercise.secondaryMuscles)
            assertEquals(exercise.secondaryMuscles.size, exercise.secondaryMuscles.distinct().size)
        }
    }

    @Test
    fun `catalog covers repetitions and timed holds`() {
        assertTrue(BODYWEIGHT_EXERCISES.count { it.measureType == MeasureType.REPS } >= 10)
        assertTrue(BODYWEIGHT_EXERCISES.count { it.measureType == MeasureType.DURATION } >= 5)
    }

    @Test
    fun `catalog covers all major bodyweight training regions`() {
        val primary = BODYWEIGHT_EXERCISES.map { it.primaryMuscle }.toSet()
        assertTrue(Muscle.CHEST in primary)
        assertTrue(Muscle.BACK in primary)
        assertTrue(Muscle.SHOULDERS in primary)
        assertTrue(Muscle.QUADRICEPS in primary)
        assertTrue(Muscle.GLUTES in primary)
        assertTrue(Muscle.ABS in primary)
        assertTrue(Muscle.OBLIQUES in primary)
    }
}
