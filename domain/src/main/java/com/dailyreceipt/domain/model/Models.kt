package com.dailyreceipt.domain.model

import java.time.LocalDate
import java.time.LocalDateTime

/**
 * Represents a single day's summary of all collected data.
 */
data class DailySummary(
    val date: LocalDate,
    val usageStats: List<AppUsage> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val healthData: HealthData? = null,
    val calendarEvents: List<CalendarEvent> = emptyList(),
    val financeTransactions: List<FinanceTransaction> = emptyList(),
    val totalScreenTimeMinutes: Long = 0,
    val createdAt: LocalDateTime = LocalDateTime.now()
)

/**
 * App usage statistics from UsageStatsManager.
 */
data class AppUsage(
    val packageName: String,
    val appName: String,
    val usageTimeMillis: Long,
    val lastUsed: LocalDateTime?,
    val category: String
) {
    val usageTimeMinutes: Long get() = usageTimeMillis / 60_000
}

/**
 * Captured notification data.
 */
data class Notification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val content: String?,
    val postedTime: LocalDateTime,
    val category: String? = null
)

/**
 * Health data from Google Fit.
 */
data class HealthData(
    val steps: Int = 0,
    val distanceMeters: Float = 0f,
    val activeMinutes: Int = 0,
    val caloriesBurned: Float = 0f,
    val heartRateSamples: List<HeartRateSample> = emptyList(),
    val recordedAt: LocalDateTime = LocalDateTime.now()
)

data class HeartRateSample(
    val bpm: Int,
    val recordedAt: LocalDateTime
)

/**
 * Calendar event from CalendarContract.
 */
data class CalendarEvent(
    val id: Long,
    val title: String,
    val description: String?,
    val location: String?,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime?,
    val isAllDay: Boolean,
    val calendarId: Long
)

/**
 * Finance transaction parsed from notifications.
 */
data class FinanceTransaction(
    val id: String,
    val source: String, // e.g., "KakaoPay", "Samsung Pay"
    val amount: Long,   // in won (KRW)
    val merchantName: String?,
    val transactionType: TransactionType,
    val timestamp: LocalDateTime
)

enum class TransactionType {
    PAYMENT,
    TRANSFER,
    DEPOSIT,
    WITHDRAWAL
}
