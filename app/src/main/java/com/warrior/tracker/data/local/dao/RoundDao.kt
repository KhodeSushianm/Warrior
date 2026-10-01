package com.warrior.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.warrior.tracker.data.local.entity.RoundEntity
import kotlinx.coroutines.flow.Flow

/** Main methods per ARCHITECTURE.md sec.10.16. */
@Dao
interface RoundDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRound(round: RoundEntity)

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertRounds(rounds: List<RoundEntity>)

    @Update
    suspend fun updateRound(round: RoundEntity)

    @Delete
    suspend fun deleteRound(round: RoundEntity)

    @Query("SELECT * FROM rounds WHERE activity_id = :activityId ORDER BY round_number ASC")
    fun getRoundsForActivity(activityId: String): Flow<List<RoundEntity>>

    @Query("SELECT * FROM rounds WHERE activity_id = :activityId ORDER BY round_number ASC")
    suspend fun getRoundsForActivityOnce(activityId: String): List<RoundEntity>

    /** Rounds whose workout falls in from..to by local_date (Boxing progress, sec.7.2). */
    @Query(
        """
        SELECT r.* FROM rounds r
        JOIN activities a ON a.id = r.activity_id
        JOIN workouts w ON w.id = a.workout_id
        WHERE w.status = 'COMPLETED' AND w.local_date BETWEEN :from AND :to
        """
    )
    suspend fun getRoundsBetweenDates(from: String, to: String): List<RoundEntity>

    @Query(
        """
        SELECT r.* FROM rounds r
        JOIN activities a ON a.id = r.activity_id
        JOIN workouts w ON w.id = a.workout_id
        WHERE w.status = 'COMPLETED' AND w.local_date BETWEEN :from AND :to
        """
    )
    fun observeRoundsBetweenDates(from: String, to: String): Flow<List<RoundEntity>>
}
