package com.dailyreceipt.domain.repository

import com.dailyreceipt.domain.model.DailySummary
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface DailySummaryRepository {
    suspend fun collectDailyData(date: LocalDate): DailySummary
    suspend fun getDailySummary(date: LocalDate): DailySummary?
    fun observeDailySummary(date: LocalDate): Flow<DailySummary?>
    fun getRecentSummaries(limit: Int): Flow<List<DailySummary>>
    suspend fun deleteOldData(olderThan: LocalDate)
}
