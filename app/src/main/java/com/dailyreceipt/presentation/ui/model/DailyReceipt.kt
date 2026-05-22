package com.dailyreceipt.presentation.ui.model

import java.time.LocalDate

/**
 * UI 전용 모델 — 화면 표시용으로 단순화된 데이터 구조
 * 
 * Domain 모델 (복잡한 원본 데이터)과 분리하여 사용
 * 화면 표시, PDF 생성 등에서 사용
 */

/**
 * 하루의 영수증 — 메인 UI 모델
 */
data class DailyReceipt(
    val date: LocalDate = LocalDate.now(),
    val appUsage: AppUsage = AppUsage(),
    val health: Health = Health(),
    val notifications: NotificationSummary = NotificationSummary(),
    val finance: Finance = Finance(),
    val schedules: List<Schedule> = emptyList()
)

/**
 * 앱 사용 정보
 */
data class AppUsage(
    val totalMinutes: Int = 0,
    val topApps: List<AppUsageItem> = emptyList()
)

/**
 * 개별 앱 사용 정보
 */
data class AppUsageItem(
    val packageName: String = "",
    val appName: String = "",
    val minutes: Int = 0,
    val icon: String = "📱"
)

/**
 * 건강 데이터
 */
data class Health(
    val steps: Int = 0,
    val calories: Int = 0,
    val sleepMinutes: Int = 0,
    val heartRate: Int = 0
)

/**
 * 알림 요약
 */
data class NotificationSummary(
    val totalCount: Int = 0,
    val messenger: Int = 0,
    val banking: Int = 0,
    val shopping: Int = 0
)

/**
 * 금융 정보
 */
data class Finance(
    val totalSpent: Int = 0,
    val transactions: List<Transaction> = emptyList()
)

/**
 * 거래 내역
 */
data class Transaction(
    val time: String = "",
    val description: String = "",
    val amount: Int = 0,
    val method: String = ""
)

/**
 * 일정 정보
 */
data class Schedule(
    val time: String = "",
    val title: String = "",
    val location: String = ""
)
