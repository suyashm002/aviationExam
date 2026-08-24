package com.suyash.mockcivilaviationexam.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "question_feedback")
data class QuestionFeedbackEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val questionId: String,
    val sectionId: String,
    val questionText: String,
    val feedbackType: String, // wrong_answer, unclear, typo, outdated, other
    val comment: String = "",
    val userEmail: String,
    val createdAt: Long = System.currentTimeMillis(),
    val synced: Boolean = false
)
