package com.dailyreceipt.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.dailyreceipt.data.local.entity.DailySummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailySummaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(summary: DailySummaryEntity)

    @Update
    suspend fun update(summary: DailySummaryEntity)

    @Delete
    suspend fun delete(summary: DailySummaryEntity)

    @Query("SELECT * FROM daily_summary WHERE date = :date LIMIT 1")
    suspend fun getByDate(date: String): DailySummaryEntity?

    @Query("SELECT * FROM daily_summary WHERE date = :date LIMIT 1")
    fun observeByDate(date: String): Flow<DailySummaryEntity?>

    @Query("SELECT * FROM daily_summary ORDER BY date DESC LIMIT :limit")
    fun getRecentSummaries(limit: Int): Flow<List<DailySummaryEntity>>

    @Query("SELECT * FROM daily_summary WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getSummariesInRange(startDate: String, endDate: String): Flow<List<DailySummaryEntity>>

    @Query("DELETE FROM daily_summary WHERE date < :date")
    suspend fun deleteOlderThan(date: String)
}
