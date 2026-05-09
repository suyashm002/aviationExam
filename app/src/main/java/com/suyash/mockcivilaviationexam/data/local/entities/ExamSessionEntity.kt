package com.suyash.mockcivilaviationexam.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "exam_sessions_local")
data class ExamSessionEntity(
    @PrimaryKey val id: String,
    val sectionId: String,
    val userEmail: String,
    val totalQuestions: Int,
    val score: Double = 0.0,
    val correctAnswers: Int = 0,
    val completed: Boolean = false,
    val timeTaken: Int? = null,
    val startedAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
)
