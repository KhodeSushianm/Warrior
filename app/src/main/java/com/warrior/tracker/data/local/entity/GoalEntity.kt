package com.warrior.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * `goals` — sec.10.10. current/unit/period_start/period_end are NOT stored: derived (sec.7.5).
 */
@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = ExerciseEntity::class,
            parentColumns = ["id"],
            childColumns = ["exercise_id"],
            onDelete = ForeignKey.RESTRICT,
        ),
    ],
    indices = [
        Index(value = ["status"]),
        Index(value = ["exercise_id"]),
    ],
)
data class GoalEntity(
    @PrimaryKey val id: String,
    val type: String, // GoalType.name
    @ColumnInfo(name = "exercise_id") val exerciseId: String? = null,
    @ColumnInfo(name = "target_value") val targetValue: Double,
    @ColumnInfo(name = "start_value") val startValue: Double? = null,
    val period: String = "NONE", // GoalPeriod.name
    val deadline: Long? = null,
    val status: String = "ACTIVE", // GoalStatus.name
    @ColumnInfo(name = "achieved_at") val achievedAt: Long? = null,
    val note: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
