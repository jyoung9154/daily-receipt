package com.dailyreceipt.domain.usecase

import com.dailyreceipt.domain.model.DailySummary
import com.dailyreceipt.domain.repository.DailySummaryRepository
import java.time.LocalDate
import javax.inject.Inject

class CollectDailyDataUseCase @Inject constructor(
    private val repository: DailySummaryRepository
) {
    suspend operator fun invoke(date: LocalDate = LocalDate.now()): DailySummary {
        return repository.collectDailyData(date)
    }
}
