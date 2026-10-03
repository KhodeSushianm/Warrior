package com.warrior.tracker.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.warrior.tracker.core.common.GoalPeriod
import com.warrior.tracker.core.common.GoalStatus
import com.warrior.tracker.core.common.GoalType

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
    val type: GoalType,
    @ColumnInfo(name = "exercise_id") val exerciseId: String? = null,
    @ColumnInfo(name = "target_value") val targetValue: Double,
    @ColumnInfo(name = "start_value") val startValue: Double? = null,
    val period: GoalPeriod = GoalPeriod.NONE,
    val deadline: Long? = null,
    val status: GoalStatus = GoalStatus.ACTIVE,
    @ColumnInfo(name = "achieved_at") val achievedAt: Long? = null,
    val note: String? = null,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
)
