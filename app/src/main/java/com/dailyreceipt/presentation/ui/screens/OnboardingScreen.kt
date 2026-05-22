package com.dailyreceipt.presentation.ui.screens

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dailyreceipt.presentation.theme.AccentBrown
import com.dailyreceipt.presentation.theme.CreamBackground
import com.dailyreceipt.presentation.util.PermissionManager
import kotlinx.coroutines.launch

/**
 * 온보딩 화면 — 권한 요청 4 페이지
 * 
 * 페이지 구성:
 * 1. 환영 페이지 — 앱 소개
 * 2. 데이터 수집 설명 — 어떤 데이터를 어떻게 수집하는지
 * 3. 권한 요청 — 개별 권한별 설명
 * 4. 완료 — 시작 버튼
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onRequestPermission: (PermissionManager.PermissionType) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 4 })
    val scope = rememberCoroutineScope()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Skip 버튼
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            TextButton(onClick = onComplete) {
                Text("건너뛰기", color = AccentBrown)
            }
        }
        
        // Page Content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { page ->
            when (page) {
                0 -> WelcomePage()
                1 -> DataCollectionPage()
                2 -> PermissionPage(onRequestPermission)
                3 -> CompletePage()
            }
        }
        
        // Page Indicator
        Row(
            modifier = Modifier.padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(4) { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(if (pagerState.currentPage == index) 10.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) AccentBrown 
                            else AccentBrown.copy(alpha = 0.3f)
                        )
                )
            }
        }
        
        // Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (pagerState.currentPage > 0) {
                OutlinedButton(
                    onClick = { 
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage - 1) }
                    }
                ) {
                    Text("이전")
                }
            } else {
                Spacer(modifier = Modifier.width(1.dp))
            }
            
            if (pagerState.currentPage < 3) {
                Button(
                    onClick = { 
                        scope.launch { pagerState.animateScrollToPage(pagerState.currentPage + 1) }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBrown)
                ) {
                    Text("다음")
                }
            } else {
                Button(
                    onClick = onComplete,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBrown)
                ) {
                    Text("시작하기")
                }
            }
        }
    }
}

@Composable
private fun WelcomePage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Receipt,
            contentDescription = null,
            modifier = Modifier.size(120.dp),
            tint = AccentBrown
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "하루의 영수증",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBrown
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "내 하루의 모든 기록,\n영수증 한 장으로",
            fontSize = 18.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(48.dp))
        Text(
            text = "스마트폰 사용, 건강, 소비, 일정\n자동으로 기록하고 한눈에 확인하세요",
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DataCollectionPage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "어떤 데이터를 수집하나요?",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBrown
        )
        Spacer(modifier = Modifier.height(32.dp))
        
        DataItem(Icons.Default.PhoneAndroid, "📱 앱 사용 시간", "카카오톡, 인스타그램 등\n하루 동안 앱을 얼마나 사용했는지")
        Spacer(modifier = Modifier.height(16.dp))
        DataItem(Icons.Default.Favorite, "❤️ 건강 데이터", "걸음 수, 수면 시간,\n칼로리 소모량")
        Spacer(modifier = Modifier.height(16.dp))
        DataItem(Icons.Default.Notifications, "🔔 알림 내역", "받았던 알림을 정리해서\n어떤的消息을 많이 받았는지")
        Spacer(modifier = Modifier.height(16.dp))
        DataItem(Icons.Default.CalendarMonth, "📅 일정", "캘린더에 등록된\n내일 일정 미리보기")
        Spacer(modifier = Modifier.height(16.dp))
        DataItem(Icons.Default.AccountBalance, "💰 소비 내역", "결제 알림에서\n지출 내역 자동 수집")
    }
}

@Composable
private fun DataItem(icon: ImageVector, title: String, description: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(40.dp),
            tint = AccentBrown
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Text(text = description, fontSize = 12.sp, color = Color.Gray)
        }
    }
}

@Composable
private fun PermissionPage(onRequestPermission: (PermissionManager.PermissionType) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "권한 요청",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBrown
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "아래 권한이 필요합니다",
            fontSize = 14.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(24.dp))
        
        PermissionItem(
            icon = Icons.Default.BarChart,
            title = "앱 사용량 액세스",
            description = "하루 동안 앱을 얼마나 사용했는지",
            required = true,
            onClick = { onRequestPermission(PermissionManager.PermissionType.USAGE_STATS) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        PermissionItem(
            icon = Icons.Default.Notifications,
            title = "알림 접근",
            description = "수신한 알림을 읽어서 정리",
            required = true,
            onClick = { onRequestPermission(PermissionManager.PermissionType.NOTIFICATION) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        PermissionItem(
            icon = Icons.Default.DirectionsRun,
            title = "신체 활동",
            description = "걸음 수, 운동 데이터 (Google Fit)",
            required = false,
            onClick = { onRequestPermission(PermissionManager.PermissionType.ACTIVITY) }
        )
        Spacer(modifier = Modifier.height(12.dp))
        PermissionItem(
            icon = Icons.Default.CalendarMonth,
            title = "캘린더",
            description = "일정 정보 조회",
            required = false,
            onClick = { onRequestPermission(PermissionManager.PermissionType.CALENDAR) }
        )
    }
}

@Composable
private fun PermissionItem(
    icon: ImageVector,
    title: String,
    description: String,
    required: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(36.dp),
            tint = AccentBrown
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                if (required) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
            Text(text = description, fontSize = 12.sp, color = Color.Gray)
        }
        Button(
            onClick = onClick,
            colors = ButtonDefaults.buttonColors(containerColor = AccentBrown),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text("설정", fontSize = 12.sp)
        }
    }
}

@Composable
private fun CompletePage() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            modifier = Modifier.size(100.dp),
            tint = AccentBrown
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "준비 완료!",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = AccentBrown
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "이제 매일 밤 10시에\n오늘의 영수증이 도착합니다",
            fontSize = 16.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(32.dp))
        Text(
            text = "첫 영수증 만들기를 시작하세요",
            fontSize = 14.sp,
            color = AccentBrown
        )
    }
}
