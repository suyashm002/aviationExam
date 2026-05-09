package com.suyash.mockcivilaviationexam.data.remote.repository

import android.content.Context
import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.suyash.mockcivilaviationexam.data.cache.FeatureFlags
import com.suyash.mockcivilaviationexam.data.cache.QuestionCacheManager
import com.suyash.mockcivilaviationexam.data.local.dao.CacheMetadataDao
import com.suyash.mockcivilaviationexam.data.local.dao.ExamSectionDao
import com.suyash.mockcivilaviationexam.data.local.dao.ExamSessionDao
import com.suyash.mockcivilaviationexam.data.local.entities.ExamQuestionResultEntity
import com.suyash.mockcivilaviationexam.data.local.entities.ExamSessionEntity
import com.suyash.mockcivilaviationexam.domain.model.*
import java.util.UUID

class ExamRepository(
    private val context: Context,
    private val cacheManager: QuestionCacheManager,
    private val examSectionDao: ExamSectionDao? = null,
    private val cacheMetadataDao: CacheMetadataDao? = null,
    private val featureFlags: FeatureFlags? = null,
    private val examSessionDao: ExamSessionDao? = null
) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "ExamRepository"

        private const val QUESTIONS_PER_EXAM = 16
        private const val PASSING_SCORE = 75
        private const val TIME_LIMIT_MINUTES = 30

        private const val SECTIONS_CACHE_KEY = "sections_all"
        private const val CACHE_VALIDITY_HOURS = 24
    }

    fun getSectionName(sectionId: String): String {
        return when (sectionId) {
            "air_law" -> "Air Law"
            "aircraft_general" -> "Aircraft General"
            "human_performance" -> "Human Performance"
            "meteorology" -> "Meteorology"
            "navigation" -> "Navigation"
            "operational_procedures" -> "Operational Procedures"
            "principles_of_flight" -> "Principles of Flight"
            else -> "Unknown Section"
        }
    }

    private val currentUserEmail: String?
        get() = auth.currentUser?.email

    private val useRoomSections: Boolean
        get() = featureFlags?.useLocalFirstCaching == true && examSectionDao != null && cacheMetadataDao != null

    private fun isCacheValid(timestamp: Long): Boolean {
        val maxAge = CACHE_VALIDITY_HOURS.toLong() * 60L * 60L * 1000L
        return (System.currentTimeMillis() - timestamp) < maxAge
    }

    suspend fun getSections(): List<ExamSection> {
        val sections = cacheManager.getAvailableSections().map { sectionId ->
            ExamSection(
                id = sectionId,
                sectionId = sectionId,
                name = getSectionName(sectionId),
                description = getSectionDescription(sectionId),
                icon = getSectionIcon(sectionId)
            )
        }.sortedBy { it.name }

        Log.d(TAG, "Returning ${sections.size} sections (local)")
        return sections
    }

    private fun getSectionDescription(sectionId: String): String {
        return when (sectionId) {
            "air_law" -> "Aviation regulations, ICAO standards, and legal frameworks"
            "aircraft_general" -> "Aircraft systems, structures, and general knowledge"
            "human_performance" -> "Human factors, limitations, and performance in aviation"
            "meteorology" -> "Weather phenomena, forecasting, and atmospheric science"
            "navigation" -> "Navigation techniques, instruments, and procedures"
            "operational_procedures" -> "Standard operating procedures and flight operations"
            "principles_of_flight" -> "Aerodynamics, flight mechanics, and aircraft performance"
            else -> ""
        }
    }

    private fun getSectionIcon(sectionId: String): String {
        return when (sectionId) {
            "air_law" -> "gavel"
            "aircraft_general" -> "build"
            "human_performance" -> "person"
            "meteorology" -> "cloud"
            "navigation" -> "explore"
            "operational_procedures" -> "assignment"
            "principles_of_flight" -> "flight"
            else -> "quiz"
        }
    }

    fun isDataAvailable(sectionId: String): Boolean {
        return cacheManager.isDataAvailable(sectionId)
    }

    fun getAvailableSections(): Set<String> {
        return cacheManager.getAvailableSections()
    }

    suspend fun preloadQuestionsForUser() {
        try {
            Log.d(TAG, "Pre-loading bundled questions...")
            cacheManager.ensureQuestionsLoaded()
            Log.d(TAG, "Pre-load complete")
        } catch (e: Exception) {
            Log.e(TAG, "Pre-load failed: ${e.message}", e)
        }
    }

    suspend fun getQuestionsForSection(sectionId: String, limit: Int = 30): List<Question> {
        return try {
            Log.d(TAG, "Getting questions for '$sectionId' (limit: $limit)")

            if (!cacheManager.isDataAvailable(sectionId)) {
                Log.d(TAG, "Section '$sectionId' not available")
                return emptyList()
            }

            val allQuestions = cacheManager.getQuestionsFromCache(sectionId)

            if (allQuestions == null || allQuestions.isEmpty()) {
                Log.w(TAG, "No questions found for '$sectionId'")
                return emptyList()
            }

            val questions = allQuestions.shuffled().take(limit)
            Log.d(TAG, "Returning ${questions.size} questions for '$sectionId' (from ${allQuestions.size} total)")
            questions
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get questions for '$sectionId': ${e.message}", e)
            throw e
        }
    }

    /**
     * Starts an exam — creates session in local Room DB only. Zero Firebase calls.
     */
    suspend fun startExam(sectionId: String, questionCount: Int): ExamSession {
        val email = currentUserEmail
            ?: throw IllegalStateException("User not authenticated")
        Log.d(TAG, "Starting exam for '$sectionId' ($questionCount questions) - user: $email")

        val sessionId = UUID.randomUUID().toString()

        val entity = ExamSessionEntity(
            id = sessionId,
            sectionId = sectionId,
            userEmail = email,
            totalQuestions = questionCount,
            startedAt = System.currentTimeMillis()
        )
        examSessionDao?.insertSession(entity)
        Log.d(TAG, "Exam session created locally: $sessionId")

        return ExamSession(
            id = sessionId,
            sectionId = sectionId,
            userEmail = email,
            totalQuestions = questionCount,
            score = 0.0,
            correctAnswers = 0,
            completed = false,
            startedAt = Timestamp.now(),
            createdAt = Timestamp.now()
        )
    }

    /**
     * Completes an exam — computes results locally and stores in Room DB.
     * Zero Firebase calls.
     */
    suspend fun completeExam(
        sessionId: String,
        answers: Map<String, String>,
        timeSpent: Int,
        questions: List<Question>
    ): ExamResults {
        try {
            val email = currentUserEmail
                ?: throw IllegalStateException("User not authenticated")

            if (questions.isEmpty()) {
                throw IllegalStateException("Cannot complete exam with no questions")
            }

            Log.d(TAG, "Completing exam '$sessionId' - ${answers.size} answers, ${timeSpent}s")

            val sectionId = questions.first().sectionId

            // Calculate results locally
            var correctAnswers = 0
            val questionResults = questions.map { question ->
                val selectedAnswer = answers[question.id] ?: ""
                val isCorrect = selectedAnswer == question.correctAnswer
                if (isCorrect) correctAnswers++

                QuestionResult(
                    questionId = question.id,
                    questionText = question.questionText,
                    selectedAnswer = selectedAnswer,
                    correctAnswer = question.correctAnswer,
                    isCorrect = isCorrect,
                    explanation = question.explanation
                )
            }

            val totalQuestions = questions.size
            val score = (correctAnswers * 100.0) / totalQuestions
            val isPassed = score >= PASSING_SCORE
            val now = System.currentTimeMillis()

            // Store in Room DB
            examSessionDao?.completeSession(
                sessionId = sessionId,
                score = score,
                correctAnswers = correctAnswers,
                timeTaken = timeSpent,
                completedAt = now
            )

            val resultEntities = questionResults.map { qr ->
                ExamQuestionResultEntity(
                    sessionId = sessionId,
                    questionId = qr.questionId,
                    questionText = qr.questionText,
                    selectedAnswer = qr.selectedAnswer,
                    correctAnswer = qr.correctAnswer,
                    isCorrect = qr.isCorrect,
                    explanation = qr.explanation
                )
            }
            examSessionDao?.insertQuestionResults(resultEntities)

            Log.d(TAG, "Exam completed locally - Score: $score% ($correctAnswers/$totalQuestions) - ${if(isPassed) "PASSED" else "FAILED"}")

            return ExamResults(
                sessionId = sessionId,
                sectionName = getSectionName(sectionId),
                totalQuestions = totalQuestions,
                correctAnswers = correctAnswers,
                score = score.toInt(),
                isPassed = isPassed,
                timeSpent = timeSpent,
                questionResults = questionResults
            )

        } catch (e: Exception) {
            Log.e(TAG, "Failed to complete exam: ${e.message}", e)
            throw e
        }
    }

    /**
     * Gets an exam session from local Room DB.
     */
    suspend fun getExamSession(sessionId: String): ExamSession? {
        if (sessionId.isBlank()) {
            Log.e(TAG, "getExamSession called with blank sessionId")
            return null
        }
        return try {
            val entity = examSessionDao?.getSession(sessionId) ?: return null
            ExamSession(
                id = entity.id,
                sectionId = entity.sectionId,
                userEmail = entity.userEmail,
                totalQuestions = entity.totalQuestions,
                score = entity.score,
                correctAnswers = entity.correctAnswers,
                completed = entity.completed,
                timeTaken = entity.timeTaken,
                startedAt = Timestamp(entity.startedAt / 1000, 0),
                completedAt = entity.completedAt?.let { Timestamp(it / 1000, 0) }
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch exam session: ${e.message}", e)
            null
        }
    }

    /**
     * Gets stored question results from local Room DB.
     */
    suspend fun getStoredQuestionResults(sessionId: String): List<QuestionResult> {
        if (sessionId.isBlank()) {
            Log.e(TAG, "getStoredQuestionResults called with blank sessionId")
            return emptyList()
        }
        return try {
            val entities = examSessionDao?.getQuestionResults(sessionId) ?: return emptyList()
            entities.map { e ->
                QuestionResult(
                    questionId = e.questionId,
                    questionText = e.questionText,
                    selectedAnswer = e.selectedAnswer,
                    correctAnswer = e.correctAnswer,
                    isCorrect = e.isCorrect,
                    explanation = e.explanation?.ifEmpty { null }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch stored question results: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Gets exam history from local Room DB.
     */
    suspend fun getExamHistory(): List<ExamSession> {
        return try {
            val email = currentUserEmail
                ?: throw IllegalStateException("User not authenticated")

            val entities = examSessionDao?.getCompletedSessions(email) ?: return emptyList()
            entities.map { entity ->
                ExamSession(
                    id = entity.id,
                    sectionId = entity.sectionId,
                    userEmail = entity.userEmail,
                    totalQuestions = entity.totalQuestions,
                    score = entity.score,
                    correctAnswers = entity.correctAnswers,
                    completed = entity.completed,
                    timeTaken = entity.timeTaken,
                    startedAt = Timestamp(entity.startedAt / 1000, 0),
                    completedAt = entity.completedAt?.let { Timestamp(it / 1000, 0) }
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch exam history: ${e.message}", e)
            emptyList()
        }
    }

    /**
     * Gets user stats computed from local Room DB. Zero Firebase calls.
     */
    suspend fun getUserStats(): UserExamStats? {
        return try {
            val email = currentUserEmail
                ?: throw IllegalStateException("User not authenticated")
            val dao = examSessionDao ?: return null

            val totalExams = dao.getTotalExamsTaken(email)
            if (totalExams == 0) return null

            val avgScore = dao.getAverageScore(email)
            val bestScore = dao.getBestScore(email)
            val totalTime = dao.getTotalTimeSpent(email)
            val sectionRows = dao.getSectionStats(email)

            val sectionStats = sectionRows.associate { row ->
                row.sectionId to SectionStats(
                    sectionId = row.sectionId,
                    examsTaken = row.examsTaken,
                    averageScore = row.avgScore.toFloat(),
                    bestScore = row.bestScore.toInt()
                )
            }

            UserExamStats(
                userEmail = email,
                totalExamsTaken = totalExams,
                averageScore = avgScore.toFloat(),
                bestScore = bestScore.toInt(),
                totalTimeSpent = totalTime / 60,
                sectionStats = sectionStats
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get user stats: ${e.message}", e)
            null
        }
    }
}
