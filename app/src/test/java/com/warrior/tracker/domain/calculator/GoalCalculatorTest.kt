package com.warrior.tracker.domain.calculator

import com.warrior.tracker.core.common.GoalPeriod
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

    @Test
    fun `monthly window is the calendar month`() {
        val (s, e) = GoalCalculator.periodWindow(LocalDate.of(2026, 10, 15), GoalPeriod.MONTHLY, DayOfWeek.SATURDAY)
        assertEquals(LocalDate.of(2026, 10, 1), s)
        assertEquals(LocalDate.of(2026, 10, 31), e)
    }
}
