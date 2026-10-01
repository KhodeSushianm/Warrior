package com.warrior.tracker.data.local.converter

import androidx.room.TypeConverter
import com.warrior.tracker.core.common.ActivityType
import com.warrior.tracker.core.common.BoxingType
import com.warrior.tracker.core.common.Equipment
import com.warrior.tracker.core.common.ExerciseDifficulty
import com.warrior.tracker.core.common.GoalPeriod
import com.warrior.tracker.core.common.GoalStatus
import com.warrior.tracker.core.common.GoalType
import com.warrior.tracker.core.common.LoadType
import com.warrior.tracker.core.common.MeasureType
import com.warrior.tracker.core.common.Muscle
import com.warrior.tracker.core.common.MuscleRole
import com.warrior.tracker.core.common.PRType
import com.warrior.tracker.core.common.SetType
import com.warrior.tracker.core.common.WorkoutStatus

/**
 * Enums ↔ TEXT (sec.10.16). Stored as enum.name — stable, human-readable in DB browser,
 * never ordinal-based (ordinal changes would corrupt data).
 */
class DatabaseConverters {

    @TypeConverter fun fromWorkoutStatus(v: WorkoutStatus): String = v.name
    @TypeConverter fun toWorkoutStatus(v: String): WorkoutStatus = WorkoutStatus.valueOf(v)

    @TypeConverter fun fromActivityType(v: ActivityType): String = v.name
    @TypeConverter fun toActivityType(v: String): ActivityType = ActivityType.valueOf(v)

    @TypeConverter fun fromBoxingType(v: BoxingType): String = v.name
    @TypeConverter fun toBoxingType(v: String): BoxingType = BoxingType.valueOf(v)

    @TypeConverter fun fromSetType(v: SetType): String = v.name
    @TypeConverter fun toSetType(v: String): SetType = SetType.valueOf(v)

    @TypeConverter fun fromLoadType(v: LoadType): String = v.name
    @TypeConverter fun toLoadType(v: String): LoadType = LoadType.valueOf(v)

    @TypeConverter fun fromMeasureType(v: MeasureType): String = v.name
    @TypeConverter fun toMeasureType(v: String): MeasureType = MeasureType.valueOf(v)

    @TypeConverter fun fromPrType(v: PRType): String = v.name
    @TypeConverter fun toPrType(v: String): PRType = PRType.valueOf(v)

    @TypeConverter fun fromDifficulty(v: ExerciseDifficulty): String = v.name
    @TypeConverter fun toDifficulty(v: String): ExerciseDifficulty = ExerciseDifficulty.valueOf(v)

    @TypeConverter fun fromEquipment(v: Equipment): String = v.name
    @TypeConverter fun toEquipment(v: String): Equipment = Equipment.valueOf(v)

    @TypeConverter fun fromMuscle(v: Muscle): String = v.name
    @TypeConverter fun toMuscle(v: String): Muscle = Muscle.valueOf(v)

    @TypeConverter fun fromMuscleRole(v: MuscleRole): String = v.name
    @TypeConverter fun toMuscleRole(v: String): MuscleRole = MuscleRole.valueOf(v)

    @TypeConverter fun fromGoalType(v: GoalType): String = v.name
    @TypeConverter fun toGoalType(v: String): GoalType = GoalType.valueOf(v)

    @TypeConverter fun fromGoalPeriod(v: GoalPeriod): String = v.name
    @TypeConverter fun toGoalPeriod(v: String): GoalPeriod = GoalPeriod.valueOf(v)

    @TypeConverter fun fromGoalStatus(v: GoalStatus): String = v.name
    @TypeConverter fun toGoalStatus(v: String): GoalStatus = GoalStatus.valueOf(v)
}
