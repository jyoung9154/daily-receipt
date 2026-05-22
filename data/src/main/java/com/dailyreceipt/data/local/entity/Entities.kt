package com.dailyreceipt.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDateTime

@Entity(tableName = "daily_summary")
data class DailySummaryEntity(
    @PrimaryKey
    val date: String, // Format: yyyy-MM-dd
    val totalScreenTimeMinutes: Long = 0,
    val totalNotifications: Int = 0,
    val totalSteps: Int = 0,
    val totalDistanceMeters: Float = 0f,
    val totalCaloriesBurned: Float = 0f,
    val totalActiveMinutes: Int = 0,
    val totalTransactions: Int = 0,
    val totalTransactionAmount: Long = 0,
    val totalCalendarEvents: Int = 0,
    val createdAt: String = LocalDateTime.now().toString(),
    val updatedAt: String = LocalDateTime.now().toString()
)

@Entity(tableName = "app_usage")
data class AppUsageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val packageName: String,
    val appName: String,
    val usageTimeMillis: Long,
    val lastUsed: String?, // ISO datetime
    val category: String,
    val date: String // Format: yyyy-MM-dd
)

@Entity(tableName = "notification")
data class NotificationEntity(
    @PrimaryKey
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String?,
    val content: String?,
    val postedTime: String, // ISO datetime
    val category: String?,
    val date: String // Format: yyyy-MM-dd
)

@Entity(tableName = "calendar_event")
data class CalendarEventEntity(
    @PrimaryKey
    val id: Long,
    val title: String,
    val description: String?,
    val location: String?,
    val startTime: String, // ISO datetime
    val endTime: String?, // ISO datetime
    val isAllDay: Boolean,
    val calendarId: Long,
    val date: String // Format: yyyy-MM-dd
)

@Entity(tableName = "finance_transaction")
data class FinanceTransactionEntity(
    @PrimaryKey
    val id: String,
    val source: String,
    val amount: Long,
    val merchantName: String?,
    val transactionType: String, // PAYMENT, TRANSFER, DEPOSIT, WITHDRAWAL
    val timestamp: String, // ISO datetime
    val date: String // Format: yyyy-MM-dd
)
