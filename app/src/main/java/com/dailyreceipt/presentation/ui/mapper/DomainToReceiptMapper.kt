package com.dailyreceipt.presentation.ui.mapper

import com.dailyreceipt.domain.model.*
import com.dailyreceipt.domain.model.HealthData
import com.dailyreceipt.domain.model.FinanceTransaction
import com.dailyreceipt.domain.model.CalendarEvent
import com.dailyreceipt.domain.model.Notification
import com.dailyreceipt.domain.model.TransactionType
import com.dailyreceipt.domain.model.AppUsage as DomainAppUsage
import com.dailyreceipt.presentation.ui.model.*

/**
 * Domain Model → UI Model Mapper
 * 
 * Domain 모델 (Clean Architecture / Repository Layer)과 
 * UI 모델 (Presentation Layer)을 분리하여 의존성 정리
 */

/**
 * DailySummary → DailyReceipt 변환
 */
fun DailySummary.toUiModel(): DailyReceipt {
    return DailyReceipt(
        date = this.date,
        appUsage = this.usageStats.toUiModel(),
        health = this.healthData.toUiModel(),
        notifications = this.notifications.toNotificationSummary(),
        finance = this.financeTransactions.toFinanceUiModel(),
        schedules = this.calendarEvents.toScheduleList()
    )
}

// ===================== AppUsage =====================

fun List<DomainAppUsage>.toUiModel(): AppUsage {
    val sorted = this.sortedByDescending { it.usageTimeMillis }
    val totalMinutes = this.sumOf { it.usageTimeMinutes }.toInt()
    val topApps = sorted.take(5).map { it.toAppUsageItem() }
    return AppUsage(totalMinutes = totalMinutes, topApps = topApps)
}

fun DomainAppUsage.toAppUsageItem(): AppUsageItem {
    val icon = getAppIcon(this.packageName)
    return AppUsageItem(
        packageName = this.packageName,
        appName = this.appName,
        minutes = this.usageTimeMinutes.toInt(),
        icon = icon
    )
}

private fun getAppIcon(packageName: String): String {
    return when {
        packageName.contains("kakao") -> "💬"
        packageName.contains("instagram") -> "📷"
        packageName.contains("youtube") -> "🎬"
        packageName.contains("twitter") || packageName.contains("x.") -> "🐦"
        packageName.contains("telegram") -> "✈️"
        packageName.contains("netflix") -> "🎥"
        packageName.contains("music") || packageName.contains("spotify") -> "🎵"
        packageName.contains("game") -> "🎮"
        packageName.contains("browser") || packageName.contains("chrome") -> "🌐"
        packageName.contains("news") -> "📰"
        packageName.contains("mail") || packageName.contains("email") -> "📧"
        else -> "📱"
    }
}

// ===================== Health =====================

fun HealthData?.toUiModel(): Health {
    if (this == null) return Health()
    return Health(
        steps = this.steps,
        calories = this.caloriesBurned.toInt(),
        sleepMinutes = this.activeMinutes,
        heartRate = this.heartRateSamples.lastOrNull()?.bpm ?: 0
    )
}

// ===================== Notifications =====================

fun List<Notification>.toNotificationSummary(): NotificationSummary {
    val messenger = this.count { it.category == "messenger" || isMessengerApp(it.packageName) }
    val banking = this.count { it.category == "banking" || isBankingApp(it.packageName) }
    val shopping = this.count { it.category == "shopping" || isShoppingApp(it.packageName) }
    return NotificationSummary(
        totalCount = this.size,
        messenger = messenger,
        banking = banking,
        shopping = shopping
    )
}

private fun isMessengerApp(pkg: String) = 
    pkg.contains("kakao") || pkg.contains("telegram") || pkg.contains("messenger") || pkg.contains("discord")

private fun isBankingApp(pkg: String) = 
    pkg.contains("bank") || pkg.contains("pay") || pkg.contains("card") || pkg.contains("finance")

private fun isShoppingApp(pkg: String) = 
    pkg.contains("shop") || pkg.contains("market") || pkg.contains("delivery") || pkg.contains("coupang")

// ===================== Finance =====================

fun List<FinanceTransaction>.toFinanceUiModel(): Finance {
    val totalSpent = this.filter { it.transactionType == TransactionType.PAYMENT }.sumOf { it.amount }.toInt()
    val transactions = this.take(5).map { it.toUiModel() }
    return Finance(totalSpent = totalSpent, transactions = transactions)
}

fun FinanceTransaction.toUiModel(): Transaction {
    return Transaction(
        time = this.timestamp.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
        description = this.merchantName ?: this.source,
        amount = this.amount.toInt(),
        method = this.source
    )
}

// ===================== Schedule =====================

fun List<CalendarEvent>.toScheduleList(): List<Schedule> {
    return this.filter { !it.isAllDay }.sortedBy { it.startTime }.take(10).map { it.toUiModel() }
}

fun CalendarEvent.toUiModel(): Schedule {
    return Schedule(
        time = this.startTime.format(java.time.format.DateTimeFormatter.ofPattern("HH:mm")),
        title = this.title,
        location = this.location ?: ""
    )
}
