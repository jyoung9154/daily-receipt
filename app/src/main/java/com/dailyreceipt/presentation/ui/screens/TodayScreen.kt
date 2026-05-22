package com.dailyreceipt.presentation.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyreceipt.presentation.ui.model.*
import com.dailyreceipt.presentation.ui.components.ReceiptCard
import com.dailyreceipt.presentation.theme.*
import java.text.NumberFormat
import java.time.format.DateTimeFormatter
import java.util.*

@Composable
fun TodayScreen(
    receipt: DailyReceipt,
    onGeneratePdf: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("yyyy년 MM월 dd일")
    val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.KOREA)
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header with dotted border
        ReceiptHeader(title = "하루의 영수증", date = receipt.date.format(dateFormatter))

        Spacer(modifier = Modifier.height(20.dp))

        // App Usage Card
        AppUsageCard(receipt.appUsage)

        Spacer(modifier = Modifier.height(16.dp))

        // Health Card
        HealthCard(receipt.health)

        Spacer(modifier = Modifier.height(16.dp))

        // Notifications Card
        NotificationsCard(receipt.notifications)

        Spacer(modifier = Modifier.height(16.dp))

        // Finance Card
        FinanceCard(receipt.finance, currencyFormatter)

        Spacer(modifier = Modifier.height(16.dp))

        // Schedule Timeline Card
        ScheduleTimelineCard(receipt.schedules)

        Spacer(modifier = Modifier.height(24.dp))

        // PDF Generate Button
        Button(
            onClick = onGeneratePdf,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBrown),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.PictureAsPdf, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("PDF로 저장하기", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ReceiptHeader(title: String, date: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(
                width = 2.dp,
                color = DottedBorder,
                shape = RoundedCornerShape(12.dp)
            )
            .background(CardBackground)
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = title,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = AccentBrown
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = date,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
fun AppUsageCard(appUsage: AppUsage) {
    ReceiptCard(title = "📱 앱 사용") {
        appUsage.topApps.take(4).forEach { app ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${app.icon} ${app.appName}", fontSize = 14.sp, color = TextPrimary)
                Text(text = "${app.minutes}분", fontSize = 14.sp, color = TextSecondary)
            }
        }
        if (appUsage.totalMinutes > 0) {
            Text(
                text = "총 사용: ${appUsage.totalMinutes}분",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun HealthCard(health: Health) {
    ReceiptCard(title = "❤️ 건강") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HealthItem("🚶 걸음수", "${health.steps}")
            HealthItem("🔥 칼로리", "${health.calories}kcal")
            HealthItem("😴 수면", "${health.sleepMinutes / 60}h")
            HealthItem("💓 심박수", "${health.heartRate}")
        }
    }
}

@Composable
fun HealthItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
        Text(text = value, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
    }
}

@Composable
fun NotificationsCard(notifications: NotificationSummary) {
    ReceiptCard(title = "🔔 알림") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            NotificationItem("메신저", "${notifications.messenger}")
            NotificationItem("금융", "${notifications.banking}")
            NotificationItem("쇼핑", "${notifications.shopping}")
        }
        if (notifications.totalCount > 0) {
            Text(
                text = "총 ${notifications.totalCount}개의 알림",
                fontSize = 12.sp,
                color = TextSecondary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun NotificationItem(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = count, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
        Text(text = label, fontSize = 12.sp, color = TextSecondary)
    }
}

@Composable
fun FinanceCard(finance: Finance, formatter: NumberFormat) {
    ReceiptCard(title = "💰 금융") {
        Text(
            text = "총 지출: ${formatter.format(finance.totalSpent)}",
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBrown,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        finance.transactions.take(4).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = "${item.time} ${item.description}", fontSize = 13.sp, color = TextPrimary)
                Text(text = formatter.format(item.amount), fontSize = 13.sp, color = TextSecondary)
            }
        }
    }
}

@Composable
fun ScheduleTimelineCard(schedules: List<Schedule>) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")

    ReceiptCard(title = "📅 오늘의 일정") {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp)
        ) {
            // Timeline vertical line
            Canvas(
                modifier = Modifier
                    .width(2.dp)
                    .fillMaxHeight()
            ) {
                drawLine(
                    color = TimelineLine,
                    start = Offset(0f, 0f),
                    end = Offset(0f, size.height),
                    strokeWidth = 2.dp.toPx()
                )
            }

            Column {
                schedules.take(8).forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Timeline dot
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .offset(x = (-4).dp)
                                .clip(CircleShape)
                                .background(AccentBrown)
                        )

                        Spacer(modifier = Modifier.width(12.dp))

                        // Time
                        Text(
                            text = item.time,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            modifier = Modifier.width(50.dp)
                        )

                        // Title
                        Text(
                            text = item.title,
                            fontSize = 14.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        // Location if exists
                        if (item.location.isNotEmpty()) {
                            Text(
                                text = item.location,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }
    }
}
