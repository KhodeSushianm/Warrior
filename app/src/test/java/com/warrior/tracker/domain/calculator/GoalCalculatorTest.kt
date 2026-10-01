package com.warrior.tracker.domain.calculator

import com.warrior.tracker.core.common.GoalPeriod
import com.warrior.tracker.core.common.GoalType
import com.warrior.tracker.core.common.GoalUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.DayOfWeek
import java.time.LocalDate

/** ARCHITECTURE.md sec.7.5 — goal progress clamping, direction & period windows. */
class GoalCalculatorTest {

    @Test
    fun `progress clamps to zero and one`() {
        assertEquals(0.5, GoalCalculator.progress(current = 50.0, start = null, target = 100.0), 1e-9)
        assertEquals(0.0, GoalCalculator.progress(current = -10.0, start = null, target = 100.0), 1e-9)
        assertEquals(1.0, GoalCalculator.progress(current = 120.0, start = null, target = 100.0), 1e-9)
    }

    @Test
    fun `start offset is honored`() {
        // (60-40)/(80-40) = 0.5
        assertEquals(0.5, GoalCalculator.progress(current = 60.0, start = 40.0, target = 80.0), 1e-9)
    }

    @Test
    fun `decreasing goals (body weight loss) detect achievement correctly`() {
        assertTrue(GoalCalculator.isAchieved(current = 79.0, start = 85.0, target = 80.0))
        assertFalse(GoalCalculator.isAchieved(current = 81.0, start = 85.0, target = 80.0))
        // progress for decreasing goal: (82-85)/(80-85) = 0.6
        assertEquals(0.6, GoalCalculator.progress(current = 82.0, start = 85.0, target = 80.0), 1e-9)
    }

    @Test
    fun `weekly window starts on configured weekday`() {
        val thursday = LocalDate.of(2026, 10, 1) // Thursday
        val (satStart, satEnd) = GoalCalculator.periodWindow(thursday, GoalPeriod.WEEKLY, DayOfWeek.SATURDAY)
        assertEquals(LocalDate.of(2026, 9, 26), satStart) // previous Saturday
        assertEquals(LocalDate.of(2026, 10, 2), satEnd)

        val (monStart, monEnd) = GoalCalculator.periodWindow(thursday, GoalPeriod.WEEKLY, DayOfWeek.MONDAY)
        assertEquals(LocalDate.of(2026, 9, 28), monStart)
        assertEquals(LocalDate.of(2026, 10, 4), monEnd)
    }

    /**
     * A zero-span goal (start == target) cannot be interpolated. Phase 0 returned 1.0
     * unconditionally, which contradicted `isAchieved` for the same inputs and showed a full
     * progress bar on a goal the user had not reached.
     */
    @Test
    fun `degenerate goal agrees with isAchieved instead of always reporting full progress`() {
        assertFalse(GoalCalculator.isAchieved(current = 50.0, start = 80.0, target = 80.0))
        assertEquals(0.0, GoalCalculator.progress(current = 50.0, start = 80.0, target = 80.0), 1e-9)
        assertTrue(GoalCalculator.isAchieved(current = 80.0, start = 80.0, target = 80.0))
        assertEquals(1.0, GoalCalculator.progress(current = 80.0, start = 80.0, target = 80.0), 1e-9)
        assertTrue(GoalCalculator.isAchieved(current = 90.0, start = 80.0, target = 80.0))
        assertEquals(1.0, GoalCalculator.progress(current = 90.0, start = 80.0, target = 80.0), 1e-9)
    }

    /** NaN must not escape into the UI: every comparison against NaN is false, so coerceIn passes it through. */
    @Test
    fun `non-finite current value yields zero progress not NaN`() {
        assertEquals(0.0, GoalCalculator.progress(current = Double.NaN, start = 0.0, target = 100.0), 1e-9)
        assertEquals(0.0, GoalCalculator.progress(current = Double.POSITIVE_INFINITY, start = 0.0, target = 100.0), 0.0)
    }

    /** A deadline in the past must clamp the window, never invert it (end < start). */
    @Test
    fun `passed deadline clamps the window without inverting it`() {
        val today = LocalDate.of(2026, 10, 1)
        val (s1, e1) = GoalCalculator.periodWindow(
            today = today,
            period = GoalPeriod.MONTHLY,
            weekStart = DayOfWeek.SATURDAY,
            deadline = LocalDate.of(2026, 1, 15),
        )
        assertEquals(LocalDate.of(2026, 10, 1), s1)
        assertTrue("window must not be inverted: $s1..$e1", !e1.isBefore(s1))
        assertEquals(s1, e1)

        val (s2, e2) = GoalCalculator.periodWindow(
            today = today,
            period = GoalPeriod.WEEKLY,
            weekStart = DayOfWeek.SATURDAY,
            deadline = LocalDate.of(2020, 5, 5),
        )
        assertEquals(LocalDate.of(2026, 9, 26), s2)
        assertTrue("weekly window must not be inverted: $s2..$e2", !e2.isBefore(s2))
    }

    /** A deadline inside the period caps the natural end. */
    @Test
    fun `deadline inside the period caps the natural end`() {
        val (s, e) = GoalCalculator.periodWindow(
            today = LocalDate.of(2026, 10, 15),
            period = GoalPeriod.MONTHLY,
            weekStart = DayOfWeek.SATURDAY,
            deadline = LocalDate.of(2026, 10, 20),
        )
        assertEquals(LocalDate.of(2026, 10, 1), s)
        assertEquals(LocalDate.of(2026, 10, 20), e)
    }

    @Test
    fun `NONE period spans from the epoch sentinel to today`() {
        val today = LocalDate.of(2026, 10, 1)
        val (s, e) = GoalCalculator.periodWindow(today, GoalPeriod.NONE, DayOfWeek.SATURDAY)
        assertEquals(LocalDate.of(1970, 1, 1), s)
        assertEquals(today, e)
    }

    @Test
    fun `unit is derived from goal type and never stored`() {
        assertEquals(GoalUnit.KG, GoalCalculator.unitOf(GoalType.STRENGTH_WEIGHT))
        assertEquals(GoalUnit.KG, GoalCalculator.unitOf(GoalType.BODY_WEIGHT))
        assertEquals(GoalUnit.REPS, GoalCalculator.unitOf(GoalType.STRENGTH_REPS))
        assertEquals(GoalUnit.SESSIONS, GoalCalculator.unitOf(GoalType.SESSIONS))
        assertEquals(GoalUnit.ROUNDS, GoalCalculator.unitOf(GoalType.ROUNDS))
        assertEquals(GoalUnit.MINUTES, GoalCalculator.unitOf(GoalType.TRAINING_TIME))
    }

    @Test
    fun `monthly window is the calendar month`() {
        val (s, e) = GoalCalculator.periodWindow(LocalDate.of(2026, 10, 15), GoalPeriod.MONTHLY, DayOfWeek.SATURDAY)
        assertEquals(LocalDate.of(2026, 10, 1), s)
        assertEquals(LocalDate.of(2026, 10, 31), e)
    }
}
