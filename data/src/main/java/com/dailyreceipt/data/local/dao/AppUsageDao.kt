package com.dailyreceipt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailyreceipt.data.local.entity.AppUsageEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AppUsageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(usages: List<AppUsageEntity>)

    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY usageTimeMillis DESC")
    fun getByDate(date: String): Flow<List<AppUsageEntity>>

    @Query("SELECT * FROM app_usage WHERE date = :date ORDER BY usageTimeMillis DESC")
    suspend fun getByDateSync(date: String): List<AppUsageEntity>

    @Query("SELECT * FROM app_usage WHERE packageName = :packageName AND date = :date LIMIT 1")
    suspend fun getByPackageAndDate(packageName: String, date: String): AppUsageEntity?

    @Query("DELETE FROM app_usage WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM app_usage WHERE date < :date")
    suspend fun deleteOlderThan(date: String)
}
