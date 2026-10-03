package com.warrior.tracker.domain.calculator

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.WeekFields

/**
 * Weekly summary for Home (sec.4.2) — pure aggregation over completed workouts.
 * Week window respects the user's week-start setting (sec.13: Saturday fa / Monday en).
 */
data class WeeklySummary(
    val sessionCount: Int,
    val totalActiveSeconds: Long,
    val strengthSets: Int,
    val boxingRounds: Int,
)

object WeeklySummaryCalculator {

    fun weekWindow(today: LocalDate, weekStart: DayOfWeek): Pair<LocalDate, LocalDate> {
        val diff = (today.dayOfWeek.value - weekStart.value + 7) % 7
        val start = today.minusDays(diff.toLong())
        return start to start.plusDays(6)
    }

    /** Convenience overload using ISO weeks (Monday) — tests & default en locale. */
    fun isoWeekWindow(today: LocalDate): Pair<LocalDate, LocalDate> =
        weekWindow(today, WeekFields.ISO.firstDayOfWeek)

    fun summarize(
        workoutsThisWeek: List<WorkoutLike>,
        setsThisWeek: Int,
        roundsThisWeek: Int,
    ): WeeklySummary = WeeklySummary(
        sessionCount = workoutsThisWeek.size,
        totalActiveSeconds = workoutsThisWeek.sumOf { it.activeDurationSec ?: 0L },
        strengthSets = setsThisWeek,
        boxingRounds = roundsThisWeek,
    )

    data class WorkoutLike(val activeDurationSec: Long?)
}
