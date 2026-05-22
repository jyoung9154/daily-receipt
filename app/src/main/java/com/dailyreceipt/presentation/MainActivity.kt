package com.dailyreceipt.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dailyreceipt.presentation.theme.DailyReceiptTheme
import com.dailyreceipt.presentation.ui.MainViewModel
import com.dailyreceipt.presentation.ui.screens.OnboardingScreen
import com.dailyreceipt.presentation.ui.screens.TodayScreen
import com.dailyreceipt.presentation.util.PermissionManager
import dagger.hilt.android.AndroidEntryPoint

/**
 * MainActivity — 하루의 영수증 앱 진입점
 * 
 * 화면 흐름:
 * 1. 온보딩 (최초 1회) — 권한 요청
 * 2. 메인 화면 — 오늘의 영수증
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DailyReceiptTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainContent(
                        onRequestPermission = { type -> 
                            PermissionManager.openPermissionSettings(this, type)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun MainContent(
    onRequestPermission: (PermissionManager.PermissionType) -> Unit
) {
    val viewModel: MainViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()
    
    // 온보딩 완료 여부 (SharedPreferences에 저장하면 최초 1회만)
    var hasCompletedOnboarding by remember { mutableStateOf(false) }
    
    if (!hasCompletedOnboarding) {
        // 온보딩 화면
        OnboardingScreen(
            onComplete = {
                hasCompletedOnboarding = true
                viewModel.loadTodayData()
            },
            onRequestPermission = { permissionType ->
                onRequestPermission(permissionType)
            }
        )
    } else {
        // 메인 화면
        when {
            uiState.isLoading -> {
                CircularProgressIndicator()
            }
            uiState.error != null -> {
                Text(text = uiState.error ?: "알 수 없는 오류")
            }
            uiState.receipt != null -> {
                TodayScreen(
                    receipt = uiState.receipt!!,
                    onGeneratePdf = { viewModel.onGeneratePdf() }
                )
            }
            else -> {
                Text("표시할 데이터가 없습니다.")
            }
        }
    }
}
