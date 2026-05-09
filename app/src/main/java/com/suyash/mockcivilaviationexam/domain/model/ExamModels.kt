package com.suyash.mockcivilaviationexam.domain.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class ExamSection(
    val id: String = "", // Auto-generated Firestore document ID
    val sectionId: String = "", // Friendly ID like "air_law", "aircraft_general", etc.
    val name: String = "",
    val description: String? = null,
    val icon: String? = null,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class Question(
    val id: String = "", // Document ID
    val sectionId: String = "",
    val questionText: String = "",
    val optionA: String = "",
    val optionB: String = "",
    val optionC: String = "",
    val optionD: String? = null, // Nullable
    val correctAnswer: String = "",
    val explanation: String? = null, // Nullable
    val difficulty: String = "medium",
    val source: String = "manual",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)


data class ExamSession(
    val id: String = "", // Document ID
    val sectionId: String = "",
    val userEmail: String? = null, // Optional (can be null for anonymous)
    val totalQuestions: Int = 0,
    val score: Double = 0.0, // Percentage score (0-100)
    val correctAnswers: Int = 0,
    val completed: Boolean = false,
    val timeTaken: Int? = null, // Optional: seconds taken (can be null)
    val startedAt: Timestamp? = null, // Optional: when exam started
    val completedAt: Timestamp? = null, // Optional: when exam completed
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class ExamAnswer(
    val id: String = "", // Document ID
    val sessionId: String = "", // References exam_sessions.id
    val questionId: String = "", // References questions.id
    val selectedAnswer: String = "", // User's selected answer
    val isCorrect: Boolean = false, // Boolean: was answer correct
    @ServerTimestamp val createdAt: Timestamp? = null
)

data class ExamResults(
    val sessionId: String = "",
    val sectionName: String = "",
    val totalQuestions: Int = 0,
    val correctAnswers: Int = 0,
    val score: Int = 0,
    val isPassed: Boolean = false,
    val timeSpent: Int = 0,
    val completedAt: Timestamp = Timestamp.now(),
    val questionResults: List<QuestionResult> = emptyList()
)

data class QuestionResult(
    val questionId: String = "",
    val questionText: String = "",
    val selectedAnswer: String = "",
    val correctAnswer: String = "",
    val isCorrect: Boolean = false,
    val explanation: String? = null
)

data class UserExamStats(
    val userEmail: String = "",
    val totalExamsTaken: Int = 0,
    val averageScore: Float = 0f,
    val bestScore: Int = 0,
    val totalTimeSpent: Int = 0, // in minutes
    val sectionStats: Map<String, SectionStats> = emptyMap(),
    val lastExamDate: Timestamp? = null
)

data class SectionStats(
    val sectionId: String = "",
    val examsTaken: Int = 0,
    val averageScore: Float = 0f,
    val bestScore: Int = 0,
    val lastAttempt: Timestamp? = null
)