package com.warrior.tracker.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.warrior.tracker.data.local.entity.BodyWeightEntity
import kotlinx.coroutines.flow.Flow

/** Main methods per ARCHITECTURE.md sec.10.16. Always kg; conversion is display-only (sec.13). */
@Dao
interface BodyWeightDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertWeight(weight: BodyWeightEntity)

    @Update
    suspend fun updateWeight(weight: BodyWeightEntity)

    @Delete
    suspend fun deleteWeight(weight: BodyWeightEntity)

    @Query("SELECT * FROM body_weights ORDER BY measured_at DESC")
    fun getWeightHistory(): Flow<List<BodyWeightEntity>>

    @Query("SELECT * FROM body_weights ORDER BY measured_at DESC LIMIT 1")
    suspend fun getLatestWeight(): BodyWeightEntity?

    @Query("SELECT * FROM body_weights ORDER BY measured_at DESC LIMIT 1")
    fun observeLatestWeight(): Flow<BodyWeightEntity?>

    /** Last measurement on or before [localDate] — snapshot for workouts (sec.6.5 note). */
    @Query(
        """
        SELECT * FROM body_weights
        WHERE local_date <= :localDate
        ORDER BY local_date DESC, measured_at DESC LIMIT 1
        """
    )
    suspend fun getWeightAt(localDate: String): BodyWeightEntity?

    @Query("SELECT * FROM body_weights WHERE local_date BETWEEN :from AND :to ORDER BY measured_at ASC")
    suspend fun getWeightsBetween(from: String, to: String): List<BodyWeightEntity>
}
