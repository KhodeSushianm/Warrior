package com.warrior.tracker.di

import android.content.Context
import androidx.room.Room
import com.warrior.tracker.core.time.Clock
import com.warrior.tracker.core.time.SystemClockImpl
import com.warrior.tracker.data.local.database.AppDatabase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** App-wide DI (sec.5: Hilt). Layer rule sec.4.1: only data/core wired here in Phase 0. */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    /**
     * sec.12: schemas live in Git and every version change gets an explicit Migration;
     * `fallbackToDestructiveMigration` is forbidden. No destructive downgrade fallback either —
     * for a local-first app whose data has no server copy, silently wiping the database is the
     * exact failure mode sec.12 exists to prevent.
     */
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            // Enum <-> TEXT converters are declared on the database via @TypeConverters; Room
            // instantiates them itself, so no addTypeConverter() call is needed.
            .build()

    @Provides fun provideWorkoutDao(db: AppDatabase) = db.workoutDao()
    @Provides fun provideActivityDao(db: AppDatabase) = db.activityDao()
    @Provides fun provideSetDao(db: AppDatabase) = db.setDao()
    @Provides fun provideRoundDao(db: AppDatabase) = db.roundDao()
    @Provides fun provideExerciseDao(db: AppDatabase) = db.exerciseDao()
    @Provides fun provideGoalDao(db: AppDatabase) = db.goalDao()
    @Provides fun provideBodyWeightDao(db: AppDatabase) = db.bodyWeightDao()
    @Provides fun provideStatsDao(db: AppDatabase) = db.statsDao()

    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    /**
     * Application-lifetime scope for work that must outlive a ViewModel (stats rebuild after a
     * delete, seeding). MUST be a singleton: an unscoped @Provides handed every injection point a
     * fresh CoroutineScope with its own SupervisorJob that nothing ever cancelled, leaking a job
     * and its dispatcher slot per injection.
     */
    @Provides
    @Singleton
    fun provideAppScope(@IoDispatcher io: CoroutineDispatcher): CoroutineScope =
        CoroutineScope(SupervisorJob() + io)
}

@Module
@InstallIn(SingletonComponent::class)
abstract class BindsModule {
    @Binds
    @Singleton
    abstract fun bindClock(impl: SystemClockImpl): Clock
}
