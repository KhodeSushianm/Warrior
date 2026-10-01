package com.warrior.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.warrior.tracker.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

/** Main methods per ARCHITECTURE.md sec.10.16. current/period bounds are derived, never stored (sec.7.5). */
@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGoal(goal: GoalEntity)

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("SELECT * FROM goals WHERE status = 'ACTIVE' ORDER BY created_at ASC")
    fun getActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY updated_at DESC")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: String): GoalEntity?

    @Query(
        """
        UPDATE goals SET status = 'ACHIEVED', achieved_at = :achievedAt, updated_at = :updatedAt
        WHERE id = :id
        """
    )
    suspend fun markAchieved(id: String, achievedAt: Long, updatedAt: Long)

    @Query("UPDATE goals SET status = 'ARCHIVED', updated_at = :updatedAt WHERE id = :id")
    suspend fun archiveGoal(id: String, updatedAt: Long)
}
