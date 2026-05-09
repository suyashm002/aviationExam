package com.suyash.mockcivilaviationexam.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyash.mockcivilaviationexam.data.remote.repository.ExamRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExamHomeViewModel(
    private val examRepository: ExamRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ExamHomeViewModel"
    }

    private val _uiState = MutableStateFlow(ExamHomeUiState())
    val uiState: StateFlow<ExamHomeUiState> = _uiState.asStateFlow()

    fun loadUserStats() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val userStats = examRepository.getUserStats()

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    totalExamsTaken = userStats?.totalExamsTaken ?: 0,
                    averageScore = userStats?.averageScore ?: 0f,
                    bestScore = userStats?.bestScore?.toFloat() ?: 0f
                )

                Log.d(TAG, "Stats loaded: exams=${userStats?.totalExamsTaken}, avg=${userStats?.averageScore}, best=${userStats?.bestScore}")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load user stats: ${e.message}", e)
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }
}

data class ExamHomeUiState(
    val totalExamsTaken: Int = 0,
    val averageScore: Float = 0f,
    val bestScore: Float = 0f,
    val isLoading: Boolean = false
)
