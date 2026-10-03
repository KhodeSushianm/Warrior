package com.warrior.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.warrior.tracker.data.local.entity.ExerciseDailyStatsEntity
import com.warrior.tracker.data.local.entity.PrEventEntity
import kotlinx.coroutines.flow.Flow

/**
 * Cache tables only (sec.10.12, sec.10.13, sec.10.18). Everything here is rebuildable from
 * sets/rounds; a full `rebuildAll` must always equal incremental updates (tested in Phase 5).
 */
@Dao
interface StatsDao {

    // ---- exercise_daily_stats ----

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyStats(stats: ExerciseDailyStatsEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertDailyStatsBatch(stats: List<ExerciseDailyStatsEntity>)

    @Query("DELETE FROM exercise_daily_stats WHERE exercise_id = :exerciseId AND local_date = :localDate")
    suspend fun deleteStats(exerciseId: String, localDate: String)

    @Query("DELETE FROM exercise_daily_stats")
    suspend fun clearAllStats()

    @Query(
        """
        SELECT * FROM exercise_daily_stats
        WHERE exercise_id = :exerciseId AND local_date BETWEEN :from AND :to
        ORDER BY local_date ASC
        """
    )
    fun getStatsRange(exerciseId: String, from: String, to: String): Flow<List<ExerciseDailyStatsEntity>>

    @Query(
        """
        SELECT * FROM exercise_daily_stats
        WHERE exercise_id = :exerciseId AND local_date BETWEEN :from AND :to
        ORDER BY local_date ASC
        """
    )
    suspend fun getStatsRangeOnce(exerciseId: String, from: String, to: String): List<ExerciseDailyStatsEntity>

    // ---- pr_events ----

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertPrEvent(event: PrEventEntity)

    @Query("SELECT * FROM pr_events WHERE exercise_id = :exerciseId AND is_current = 1")
    fun getCurrentPrs(exerciseId: String): Flow<List<PrEventEntity>>

    @Query("SELECT * FROM pr_events WHERE exercise_id = :exerciseId AND is_current = 1")
    suspend fun getCurrentPrsOnce(exerciseId: String): List<PrEventEntity>

    /** Invalidate before recomputation of an exercise's records (sec.11.4). */
    @Query("DELETE FROM pr_events WHERE exercise_id = :exerciseId")
    suspend fun invalidatePrs(exerciseId: String)

    @Query("DELETE FROM pr_events")
    suspend fun clearAllPrs()

    @Query("SELECT * FROM pr_events ORDER BY achieved_at DESC")
    fun observeAllPrEvents(): Flow<List<PrEventEntity>>
}
