package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suyash.mockcivilaviationexam.data.local.entities.ExamQuestionResultEntity
import com.suyash.mockcivilaviationexam.data.local.entities.ExamSessionEntity

@Dao
interface ExamSessionDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ExamSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestionResults(results: List<ExamQuestionResultEntity>)

    @Query("SELECT * FROM exam_sessions_local WHERE id = :sessionId")
    suspend fun getSession(sessionId: String): ExamSessionEntity?

    @Query("""
        UPDATE exam_sessions_local
        SET completed = 1, score = :score, correctAnswers = :correctAnswers,
            timeTaken = :timeTaken, completedAt = :completedAt
        WHERE id = :sessionId
    """)
    suspend fun completeSession(
        sessionId: String,
        score: Double,
        correctAnswers: Int,
        timeTaken: Int,
        completedAt: Long
    )

    @Query("""
        SELECT * FROM exam_sessions_local
        WHERE userEmail = :email AND completed = 1
        ORDER BY completedAt DESC
    """)
    suspend fun getCompletedSessions(email: String): List<ExamSessionEntity>

    @Query("SELECT * FROM exam_question_results WHERE sessionId = :sessionId")
    suspend fun getQuestionResults(sessionId: String): List<ExamQuestionResultEntity>

    @Query("SELECT COUNT(*) FROM exam_sessions_local WHERE userEmail = :email AND completed = 1")
    suspend fun getTotalExamsTaken(email: String): Int

    @Query("SELECT COALESCE(AVG(score), 0.0) FROM exam_sessions_local WHERE userEmail = :email AND completed = 1")
    suspend fun getAverageScore(email: String): Double

    @Query("SELECT COALESCE(MAX(score), 0.0) FROM exam_sessions_local WHERE userEmail = :email AND completed = 1")
    suspend fun getBestScore(email: String): Double

    @Query("SELECT COALESCE(SUM(timeTaken), 0) FROM exam_sessions_local WHERE userEmail = :email AND completed = 1")
    suspend fun getTotalTimeSpent(email: String): Int

    @Query("""
        SELECT sectionId, COUNT(*) as examsTaken, AVG(score) as avgScore, MAX(score) as bestScore
        FROM exam_sessions_local
        WHERE userEmail = :email AND completed = 1
        GROUP BY sectionId
    """)
    suspend fun getSectionStats(email: String): List<SectionStatRow>
}

data class SectionStatRow(
    val sectionId: String,
    val examsTaken: Int,
    val avgScore: Double,
    val bestScore: Double
)
