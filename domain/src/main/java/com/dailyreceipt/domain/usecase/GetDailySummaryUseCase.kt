package com.dailyreceipt.domain.usecase

import com.dailyreceipt.domain.model.DailySummary
import com.dailyreceipt.domain.repository.DailySummaryRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDailySummaryUseCase @Inject constructor(
    private val repository: DailySummaryRepository
) {
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): DailySummary? {
        return repository.getDailySummary(date)
    }

    fun observe(date: LocalDate = LocalDate.now()): Flow<DailySummary?> {
        return repository.observeDailySummary(date)
    }

    fun getRecentSummaries(limit: Int = 7): Flow<List<DailySummary>> {
        return repository.getRecentSummaries(limit)
    }
}
