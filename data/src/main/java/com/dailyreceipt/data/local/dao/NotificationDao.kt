package com.dailyreceipt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailyreceipt.data.local.entity.NotificationEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notifications: List<NotificationEntity>)

    @Query("SELECT * FROM notification WHERE date = :date ORDER BY postedTime DESC")
    fun getByDate(date: String): Flow<List<NotificationEntity>>

    @Query("SELECT * FROM notification WHERE date = :date ORDER BY postedTime DESC")
    suspend fun getByDateSync(date: String): List<NotificationEntity>

    @Query("SELECT * FROM notification WHERE packageName = :packageName AND date = :date")
    suspend fun getByPackageAndDate(packageName: String, date: String): List<NotificationEntity>

    @Query("DELETE FROM notification WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM notification WHERE date < :date")
    suspend fun deleteOlderThan(date: String)

    @Query("SELECT COUNT(*) FROM notification WHERE date = :date")
    suspend fun getCountByDate(date: String): Int
}
