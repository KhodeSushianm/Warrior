package com.warrior.tracker.data.local.model

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Relation
import com.warrior.tracker.data.local.entity.ActivityEntity
import com.warrior.tracker.data.local.entity.ExerciseEntity
import com.warrior.tracker.data.local.entity.SetEntity
import com.warrior.tracker.data.local.entity.WorkoutEntity

/** Nested Room aggregate used to render and resume the active workout with one observed query. */
data class ActivityWithSetsAndExercise(
    @Embedded val activity: ActivityEntity,
    @Relation(parentColumn = "id", entityColumn = "activity_id")
    val sets: List<SetEntity>,
    @Relation(parentColumn = "exercise_id", entityColumn = "id")
    val exercise: ExerciseEntity?,
)

data class WorkoutWithBodyweightDetails(
    @Embedded val workout: WorkoutEntity,
    @Relation(
        entity = ActivityEntity::class,
        parentColumn = "id",
        entityColumn = "workout_id",
    )
    val activities: List<ActivityWithSetsAndExercise>,
)

/** Projection returned by the completed-workout summary query. */
data class BodyweightWorkoutSummaryRow(
    val id: String,
    @ColumnInfo(name = "started_at") val startedAt: Long,
    @ColumnInfo(name = "local_date") val localDate: String,
    @ColumnInfo(name = "active_duration_sec") val activeDurationSec: Long?,
    @ColumnInfo(name = "exercise_count") val exerciseCount: Int,
    @ColumnInfo(name = "set_count") val setCount: Int,
    @ColumnInfo(name = "total_reps") val totalReps: Int,
    @ColumnInfo(name = "total_duration_sec") val totalDurationSec: Int,
)
