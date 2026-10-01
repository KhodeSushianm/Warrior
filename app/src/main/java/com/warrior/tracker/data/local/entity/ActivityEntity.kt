package com.warrior.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * `activities` — sec.10.5. The single middle layer between Workout and Set/Round.
 * duration/intensity are NOT stored here (derived from sets/rounds).
 */
@Entity(
    tableName = "activities",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutEntity::class,
            parentColumns = ["id"],
            childColumns = ["workout_id"],
            onDelete = ForeignKey.CASCADE,
        ),
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["workout_id", "order_index"], unique = true),
        Index(value = ["exercise_id"]),
        Index(value = ["type"]),
    ],
)
data class ActivityEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "workout_id") val workoutId: String,
    @ColumnInfo(name = "order_index") val orderIndex: Int,
    val type: String, // ActivityType.name
    @ColumnInfo(name = "exercise_id") val exerciseId: String? = null, // STRENGTH only
    @ColumnInfo(name = "boxing_type") val boxingType: String? = null, // BOXING only
    @ColumnInfo(name = "custom_name") val customName: String? = null,
    val notes: String? = null,
)
