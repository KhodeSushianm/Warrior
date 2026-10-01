package com.warrior.tracker.di

import android.content.Context
import androidx.room.Room
import com.warrior.tracker.core.time.Clock
import com.warrior.tracker.core.time.SystemClockImpl
import com.warrior.tracker.data.local.converter.DatabaseConverters
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

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, AppDatabase.NAME)
            .addTypeConverter(DatabaseConverters()) // same instance used by schema (Room 2.6 construction)
            .fallbackToDestructiveMigrationOnDowngrade()
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
    fun provideIoDispatcher() = Dispatchers.IO

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
