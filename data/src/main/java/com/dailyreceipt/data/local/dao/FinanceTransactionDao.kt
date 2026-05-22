package com.dailyreceipt.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dailyreceipt.data.local.entity.FinanceTransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FinanceTransactionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(transactions: List<FinanceTransactionEntity>)

    @Query("SELECT * FROM finance_transaction WHERE date = :date ORDER BY timestamp DESC")
    fun getByDate(date: String): Flow<List<FinanceTransactionEntity>>

    @Query("SELECT * FROM finance_transaction WHERE date = :date ORDER BY timestamp DESC")
    suspend fun getByDateSync(date: String): List<FinanceTransactionEntity>

    @Query("SELECT * FROM finance_transaction WHERE source = :source AND date = :date")
    suspend fun getBySourceAndDate(source: String, date: String): List<FinanceTransactionEntity>

    @Query("DELETE FROM finance_transaction WHERE date = :date")
    suspend fun deleteByDate(date: String)

    @Query("DELETE FROM finance_transaction WHERE date < :date")
    suspend fun deleteOlderThan(date: String)

    @Query("SELECT SUM(amount) FROM finance_transaction WHERE date = :date AND transactionType = :type")
    suspend fun getTotalAmountByType(date: String, type: String): Long?

    @Query("SELECT COUNT(*) FROM finance_transaction WHERE date = :date")
    suspend fun getCountByDate(date: String): Int
}
