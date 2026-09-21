package com.suyash.mockcivilaviationexam.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyash.mockcivilaviationexam.data.remote.repository.ExamRepository
import com.suyash.mockcivilaviationexam.domain.model.Question
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ExamViewModel(
    private val examRepository: ExamRepository
) : ViewModel() {

    companion object {
        private const val TAG = "ExamViewModel"
        private const val QUESTIONS_PER_EXAM = 16
        private const val TIME_LIMIT_MINUTES = 30
    }

    private val _uiState = MutableStateFlow(ExamUiState())
    val uiState: StateFlow<ExamUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var questionStartTime: Long = 0

    fun startExam(sectionId: String, questionCount: Int = QUESTIONS_PER_EXAM) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                error = null
            )

            try {
                Log.d(TAG, "Starting exam for section: $sectionId with $questionCount questions")

                val sectionName = examRepository.getSectionName(sectionId)

                // Get questions from local storage (no Firebase call)
                val questions = examRepository.getQuestionsForSection(sectionId, questionCount)
                Log.d(TAG, "Got ${questions.size} questions for section $sectionId")

                if (questions.isEmpty()) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        questions = emptyList()
                    )
                    return@launch
                }

                // Start exam session in Firebase
                val examSession = examRepository.startExam(sectionId, questions.size)

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    examSessionId = examSession.id,
                    sectionId = sectionId,
                    sectionName = sectionName,
                    questions = questions,
                    currentQuestionIndex = 0,
                    timeRemainingSeconds = TIME_LIMIT_MINUTES * 60,
                    startTime = System.currentTimeMillis()
                )

                startTimer()
                trackQuestionTime()

            } catch (e: Exception) {
                Log.e(TAG, "Error starting exam: ${e.message}", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to start exam"
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeRemainingSeconds > 0 && !_uiState.value.isCompleted) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    timeRemainingSeconds = _uiState.value.timeRemainingSeconds - 1
                )
            }

            // Time's up - auto submit
            if (!_uiState.value.isCompleted) {
                submitExam()
            }
        }
    }

    private fun trackQuestionTime() {
        questionStartTime = System.currentTimeMillis()
    }

    /**
     * Records answer locally only — no Firebase call per answer.
     * All answers are batch-submitted during exam completion.
     */
    fun selectAnswer(answer: String) {
        val currentState = _uiState.value
        val currentQuestion = currentState.questions[currentState.currentQuestionIndex]

        _uiState.value = currentState.copy(
            answers = currentState.answers + (currentQuestion.id to answer)
        )
    }

    fun nextQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex < currentState.questions.size - 1) {
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex + 1
            )
            trackQuestionTime()
        }
    }

    fun previousQuestion() {
        val currentState = _uiState.value
        if (currentState.currentQuestionIndex > 0) {
            _uiState.value = currentState.copy(
                currentQuestionIndex = currentState.currentQuestionIndex - 1
            )
            trackQuestionTime()
        }
    }

    /**
     * Submits the exam. All answers + results + session update are
     * batch-written to Firebase in a single call inside completeExam().
     */
    fun submitExam() {
        val currentState = _uiState.value
        if (currentState.isCompleted || currentState.examSessionId.isEmpty()) return

        viewModelScope.launch {
            _uiState.value = currentState.copy(isSubmitting = true)

            try {
                val totalTimeSpent = ((System.currentTimeMillis() - currentState.startTime) / 1000).toInt()

                val results = examRepository.completeExam(
                    sessionId = currentState.examSessionId,
                    answers = currentState.answers,
                    timeSpent = totalTimeSpent,
                    questions = currentState.questions
                )

                _uiState.value = currentState.copy(
                    isCompleted = true,
                    isSubmitting = false,
                    examResults = results
                )

                timerJob?.cancel()

            } catch (e: Exception) {
                _uiState.value = currentState.copy(
                    isSubmitting = false,
                    error = e.message ?: "Failed to submit exam"
                )
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

data class ExamUiState(
    val isLoading: Boolean = false,
    val isSubmitting: Boolean = false,
    val isCompleted: Boolean = false,
    val error: String? = null,
    val examSessionId: String = "",
    val sectionId: String = "",
    val sectionName: String = "",
    val questions: List<Question> = emptyList(),
    val currentQuestionIndex: Int = 0,
    val answers: Map<String, String> = emptyMap(),
    val timeRemainingSeconds: Int = 3600,
    val startTime: Long = 0,
    val examResults: com.suyash.mockcivilaviationexam.domain.model.ExamResults? = null
)
