package com.dailyreceipt.data

import com.dailyreceipt.presentation.ui.model.*
import java.time.LocalDate

/**
 * 샘플 데이터 — 앱 테스트용
 * TODO: 실제 데이터 수집 구현 후 이 파일은 제거하거나 테스트 전용으로 변경
 */
object SampleData {
    val sampleDailyReceipt: DailyReceipt
        get() = DailyReceipt(
            date = LocalDate.now(),
            appUsage = AppUsage(
                totalMinutes = 175,
                topApps = listOf(
                    AppUsageItem("com.instagram.android", "Instagram", 83, "📷"),
                    AppUsageItem("com.kakao.talk", "카카오톡", 45, "💬"),
                    AppUsageItem("com.google.android.youtube", "YouTube", 32, "🎬"),
                    AppUsageItem("com.google.android.apps.nexuslauncher", "뉴스", 15, "📰")
                )
            ),
            health = Health(
                steps = 8423,
                calories = 1842,
                sleepMinutes = 450,  // 7시간 30분
                heartRate = 72
            ),
            notifications = NotificationSummary(
                totalCount = 12,
                messenger = 5,
                banking = 3,
                shopping = 4
            ),
            finance = Finance(
                totalSpent = 45000,
                transactions = listOf(
                    Transaction("09:00", "커피", 4500, "카드"),
                    Transaction("12:30", "점심", 12000, "카드"),
                    Transaction("14:00", "교통비", 3500, "카드"),
                    Transaction("18:30", "장보기", 25000, "현금")
                )
            ),
            schedules = listOf(
                Schedule("08:00", "기상 및 아침 운동", "공원"),
                Schedule("09:00", "아침 식사", ""),
                Schedule("09:30", "출근", "지하철"),
                Schedule("10:00", "팀 회의", "회의실 A"),
                Schedule("12:00", "점심 식사", "사내 식당"),
                Schedule("14:00", "프로젝트 작업", ""),
                Schedule("18:00", "퇴근", ""),
                Schedule("19:00", "헬스장", "지역 체육관"),
                Schedule("21:00", "저녁 식사", ""),
                Schedule("23:00", "취침", "")
            )
        )
}
