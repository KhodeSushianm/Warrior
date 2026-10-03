package com.warrior.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.warrior.tracker.core.common.ActivityType
import com.warrior.tracker.data.local.entity.WorkoutEntity
import kotlinx.coroutines.flow.Flow

/** Main methods per ARCHITECTURE.md sec.10.16. */
@Dao
interface WorkoutDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWorkout(workout: WorkoutEntity)

    @Update
    suspend fun updateWorkout(workout: WorkoutEntity)

    @Delete
    suspend fun deleteWorkout(workout: WorkoutEntity)

    @Query("SELECT * FROM workouts WHERE id = :id")
    suspend fun getWorkoutById(id: String): WorkoutEntity?

    @Query("SELECT * FROM workouts WHERE local_date = :localDate ORDER BY started_at DESC")
    fun getWorkoutsByDate(localDate: String): Flow<List<WorkoutEntity>>

    /** Completed workouts on a date — what History/Calendar actually render (sec.10.19). */
    @Query(
        """
        SELECT * FROM workouts
        WHERE local_date = :localDate AND status = 'COMPLETED'
        ORDER BY started_at DESC
        """
    )
    fun getCompletedWorkoutsByDate(localDate: String): Flow<List<WorkoutEntity>>

    @Query(
        """
        SELECT * FROM workouts
        WHERE local_date BETWEEN :from AND :to
        ORDER BY started_at DESC
        """
    )
    fun getWorkoutsBetweenDates(from: String, to: String): Flow<List<WorkoutEntity>>

    @Query("SELECT * FROM workouts ORDER BY started_at DESC LIMIT :limit")
    fun getRecentWorkouts(limit: Int): Flow<List<WorkoutEntity>>

    /** Draft recovery — at most one IN_PROGRESS workout by app policy (sec.8.1). */
    @Query("SELECT * FROM workouts WHERE status = 'IN_PROGRESS' ORDER BY started_at DESC LIMIT 1")
    fun getInProgressWorkout(): Flow<WorkoutEntity?>

    @Query("SELECT * FROM workouts WHERE status = 'IN_PROGRESS' ORDER BY started_at DESC LIMIT 1")
    suspend fun getInProgressWorkoutOnce(): WorkoutEntity?

    @Query(
        """
        UPDATE workouts
        SET status = 'COMPLETED', ended_at = :endedAt, active_duration_sec = :activeDurationSec,
            session_rpe = :sessionRpe, updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun markCompleted(id: String, endedAt: Long, activeDurationSec: Long?, sessionRpe: Double?, updatedAt: Long)

    /** Weekly summary source (sec.4.2): completed workouts within weekStart..weekEnd ISO dates. */
    @Query(
        """
        SELECT * FROM workouts
        WHERE status = 'COMPLETED' AND local_date BETWEEN :weekStart AND :weekEnd
        ORDER BY started_at ASC
        """
    )
    fun getWeeklySummary(weekStart: String, weekEnd: String): Flow<List<WorkoutEntity>>

    /** Distinct dates that have completed workouts — Calendar dots (sec.10.19). */
    @Query(
        """
        SELECT DISTINCT local_date FROM workouts
        WHERE status = 'COMPLETED' AND local_date BETWEEN :from AND :to
        """
    )
    fun getWorkoutDatesBetween(from: String, to: String): Flow<List<String>>

    /** Count of completed workouts with at least one activity of the given type (sec.7.2). */
    @Query(
        """
        SELECT COUNT(DISTINCT w.id) FROM workouts w
        JOIN activities a ON a.workout_id = w.id
        WHERE w.status = 'COMPLETED' AND a.type = :type AND w.local_date BETWEEN :from AND :to
        """
    )
    fun countSessionsWithType(from: String, to: String, type: ActivityType): Flow<Int>
}
