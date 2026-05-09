package com.suyash.mockcivilaviationexam.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.Timestamp
import com.suyash.mockcivilaviationexam.data.remote.repository.ExamRepository
import com.suyash.mockcivilaviationexam.domain.model.ExamResults
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ResultsViewModel(
    private val examRepository: ExamRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ResultsViewModel"
        private const val PASSING_SCORE = 75
    }

    private val _uiState = MutableStateFlow(ResultsUiState())
    val uiState: StateFlow<ResultsUiState> = _uiState.asStateFlow()

    private var _pendingResults: ExamResults? = null

    /**
     * Set results directly from in-memory exam completion (no Firestore fetch needed).
     */
    fun setPendingResults(results: ExamResults) {
        _pendingResults = results
    }

    fun loadResults(examSessionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                Log.d(TAG, "Loading results for exam session: $examSessionId")

                // Check if we have in-memory results first (from just-completed exam)
                if (_pendingResults != null && _pendingResults!!.sessionId == examSessionId) {
                    Log.d(TAG, "Using in-memory results for session: $examSessionId")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        results = _pendingResults
                    )
                    _pendingResults = null
                    return@launch
                }

                // Fetch single session document instead of entire history
                val examSession = examRepository.getExamSession(examSessionId)

                if (examSession == null || !examSession.completed) {
                    throw Exception("Exam session not found or not completed")
                }

                Log.d(TAG, "Found exam session: ${examSession.sectionId}, score: ${examSession.score}")

                val questionResults = examRepository.getStoredQuestionResults(examSessionId)
                val sectionName = examRepository.getSectionName(examSession.sectionId)

                val results = ExamResults(
                    sessionId = examSession.id,
                    sectionName = sectionName,
                    totalQuestions = examSession.totalQuestions,
                    correctAnswers = examSession.correctAnswers,
                    score = examSession.score.toInt(),
                    isPassed = examSession.score >= PASSING_SCORE,
                    timeSpent = examSession.timeTaken ?: 0,
                    completedAt = examSession.completedAt ?: Timestamp.now(),
                    questionResults = questionResults
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    results = results
                )

            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load results"
                )
            }
        }
    }
}

data class ResultsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val results: ExamResults? = null
)
