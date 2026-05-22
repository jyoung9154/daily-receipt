package com.dailyreceipt.presentation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dailyreceipt.domain.usecase.CollectDailyDataUseCase
import com.dailyreceipt.domain.usecase.GetDailySummaryUseCase
import com.dailyreceipt.presentation.ui.mapper.toUiModel
import com.dailyreceipt.presentation.ui.model.DailyReceipt
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

/**
 * 오늘의 영수증 메인 ViewModel
 * 
 * Data Flow:
 * 1. ViewModel이 UseCase를 호출하여 Domain 데이터 수집
 * 2. Domain 데이터를 Mapper로 UI 모델로 변환
 * 3. UI에 StateFlow로 전달
 */
@HiltViewModel
class MainViewModel @Inject constructor(
    private val collectDailyDataUseCase: CollectDailyDataUseCase,
    private val getDailySummaryUseCase: GetDailySummaryUseCase
) : ViewModel() {

    // UI State
    private val _uiState = MutableStateFlow(DailyReceiptUiState())
    val uiState: StateFlow<DailyReceiptUiState> = _uiState.asStateFlow()

    // 현재 표시할 날짜
    private val _currentDate = MutableStateFlow(LocalDate.now())
    
    init {
        // 앱 시작 시 오늘 데이터 로드
        loadTodayData()
    }

    /**
     * 오늘의 데이터 로드
     */
    fun loadTodayData() {
        val today = LocalDate.now()
        _currentDate.value = today
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                // 1. 오늘 데이터 수집 (UsageStats, Notifications, etc.)
                collectDailyDataUseCase(today)
                
                // 2. 수집된 데이터를 Flow로 관찰
                getDailySummaryUseCase.observe(today)
                    .catch { e ->
                        // 에러 시에도 빈 영수증은 표시 (샘플 데이터로 폴백)
                        _uiState.update { 
                            it.copy(
                                isLoading = false,
                                error = null, // 에러를 UI에 직접 표시하지 않고 로그만
                                receipt = DailyReceipt(date = today) // 폴백
                            ) 
                        }
                    }
                    .collect { summary ->
                        if (summary != null) {
                            // Domain → UI 모델 변환
                            val receipt = summary.toUiModel()
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    receipt = receipt,
                                    error = null
                                )
                            }
                        } else {
                            // 데이터가 없으면 빈 영수증 표시
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    receipt = DailyReceipt(date = today),
                                    error = null
                                )
                            }
                        }
                    }
            } catch (e: Exception) {
                // 수집 실패 시에도 빈 영수증은 표시
                _uiState.update { 
                    it.copy(
                        isLoading = false,
                        error = null,
                        receipt = DailyReceipt(date = today)
                    ) 
                }
            }
        }
    }

    /**
     * 수동 데이터 새로고침
     */
    fun refresh() {
        loadTodayData()
    }

    /**
     * PDF 생성 요청
     */
    fun onGeneratePdf() {
        _uiState.update { it.copy(isGeneratingPdf = true) }
        // TODO: PdfGenerator 호출
        // 완료 후: _uiState.update { it.copy(isGeneratingPdf = false, pdfPath = path) }
    }
}

/**
 * UI State — 화면에 표시할 상태
 */
data class DailyReceiptUiState(
    val isLoading: Boolean = false,
    val isGeneratingPdf: Boolean = false,
    val receipt: DailyReceipt? = null,
    val error: String? = null,
    val pdfPath: String? = null
)
