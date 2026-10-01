package com.warrior.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * `exercise_muscles` — sec.10.9. Composite PK (exercise_id, muscle).
 * The PRIMARY muscle row also derives the display category (sec.10.8 note).
 */
@Entity(
    tableName = "exercise_muscles",
    primaryKeys = ["exercise_id", "muscle"],
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index(value = ["muscle"])],
)
data class ExerciseMuscleEntity(
    @ColumnInfo(name = "exercise_id") val exerciseId: String,
    val muscle: String, // Muscle.name
    val role: String,   // MuscleRole.name
)
