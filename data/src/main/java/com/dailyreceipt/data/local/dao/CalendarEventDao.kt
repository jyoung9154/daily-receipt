package com.dailyreceipt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailyreceipt.data.local.entity.CalendarEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarEventDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(events: List<CalendarEventEntity>)

    @Query("SELECT * FROM calendar_event WHERE date = :date ORDER BY startTime ASC")
    fun getByDate(date: String): Flow<List<CalendarEventEntity>>

    @Query("SELECT * FROM calendar_event WHERE date = :date ORDER BY startTime ASC")
    suspend fun getByDateSync(date: String): List<CalendarEventEntity>

    @Query("DELETE FROM calendar_event WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM calendar_event WHERE date < :date")
    suspend fun deleteOlderThan(date: String)

    @Query("SELECT COUNT(*) FROM calendar_event WHERE date = :date")
    suspend fun getCountByDate(date: String): Int
}
