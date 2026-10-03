package com.warrior.tracker.domain.model

import com.warrior.tracker.core.common.Equipment
import com.warrior.tracker.core.common.ExerciseDifficulty
import com.warrior.tracker.core.common.LoadType
import com.warrior.tracker.core.common.MeasureType

/** A selectable Strength exercise; [loadType] identifies bodyweight, weighted, or assisted input. */
data class StrengthExercise(
    val id: String,
    val nameEn: String,
    val nameFa: String,
    val loadType: LoadType,
    val measureType: MeasureType,
    val equipment: Equipment,
    val difficulty: ExerciseDifficulty,
)

/** A completed set inside an in-progress Strength workout. */
data class LoggedSet(
    val id: String,
    val setNumber: Int,
    val reps: Int?,
    val durationSec: Int?,
    val completedAt: Long?,
)

/** One exercise/activity and all sets currently logged for it. */
data class StrengthActivity(
    val id: String,
    val exerciseId: String,
    val nameEn: String,
    val nameFa: String,
    val loadType: LoadType,
    val measureType: MeasureType,
    val sets: List<LoggedSet>,
)

/** The single resumable workout draft supported by the application policy. */
data class WorkoutDraft(
    val id: String,
    val startedAt: Long,
    val localDate: String,
    val activities: List<StrengthActivity>,
) {
    val completedSetCount: Int get() = activities.sumOf { activity -> activity.sets.count { it.completedAt != null } }
}

/** Compact history row for a completed workout. */
data class WorkoutSummary(
    val id: String,
    val startedAt: Long,
    val localDate: String,
    val activeDurationSec: Long,
    val exerciseCount: Int,
    val setCount: Int,
    val totalReps: Int,
    val totalDurationSec: Int,
)
