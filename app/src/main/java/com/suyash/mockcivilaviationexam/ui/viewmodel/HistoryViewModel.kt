package com.suyash.mockcivilaviationexam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyash.mockcivilaviationexam.data.remote.repository.ExamRepository
import com.suyash.mockcivilaviationexam.domain.model.ExamSession
import com.suyash.mockcivilaviationexam.domain.model.UserExamStats
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val examRepository: ExamRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()
    
    fun loadHistory() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )
            
            try {
                val examHistory = examRepository.getExamHistory()
                val userStats = examRepository.getUserStats()
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    examHistory = examHistory,
                    userStats = userStats
                )
                
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load exam history"
                )
            }
        }
    }
}

data class HistoryUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val examHistory: List<ExamSession> = emptyList(),
    val userStats: UserExamStats? = null
)