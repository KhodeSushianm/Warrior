package com.warrior.tracker.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.warrior.tracker.data.local.converter.DatabaseConverters
import com.warrior.tracker.data.local.dao.ActivityDao
import com.warrior.tracker.data.local.dao.BodyWeightDao
import com.warrior.tracker.data.local.dao.ExerciseDao
import com.warrior.tracker.data.local.dao.GoalDao
import com.warrior.tracker.data.local.dao.RoundDao
import com.warrior.tracker.data.local.dao.SetDao
import com.warrior.tracker.data.local.dao.StatsDao
import com.warrior.tracker.data.local.dao.WorkoutDao
import com.warrior.tracker.data.local.entity.ActivityEntity
import com.warrior.tracker.data.local.entity.BodyWeightEntity
import com.warrior.tracker.data.local.entity.ExerciseDailyStatsEntity
import com.warrior.tracker.data.local.entity.ExerciseEntity
import com.warrior.tracker.data.local.entity.ExerciseMuscleEntity
import com.warrior.tracker.data.local.entity.GoalEntity
import com.warrior.tracker.data.local.entity.PrEventEntity
import com.warrior.tracker.data.local.entity.RoundEntity
import com.warrior.tracker.data.local.entity.SetEntity
import com.warrior.tracker.data.local.entity.WorkoutEntity

/**
 * V1 schema — 10 tables (sec.10.3). exportSchema = true; JSONs land in docs/schemas (sec.5, sec.12).
 * Version starts at 1; every future change gets an explicit Migration (sec.10.16).
 */
@Database(
    entities = [
        WorkoutEntity::class,
        ActivityEntity::class,
        SetEntity::class,
        RoundEntity::class,
        ExerciseEntity::class,
        ExerciseMuscleEntity::class,
        GoalEntity::class,
        BodyWeightEntity::class,
        ExerciseDailyStatsEntity::class,
        PrEventEntity::class,
    ],
    version = 1,
    exportSchema = true,
)
@TypeConverters(DatabaseConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun workoutDao(): WorkoutDao
    abstract fun activityDao(): ActivityDao
    abstract fun setDao(): SetDao
    abstract fun roundDao(): RoundDao
    abstract fun exerciseDao(): ExerciseDao
    abstract fun goalDao(): GoalDao
    abstract fun bodyWeightDao(): BodyWeightDao
    abstract fun statsDao(): StatsDao

    companion object {
        const val NAME = "warrior.db"
    }
}
