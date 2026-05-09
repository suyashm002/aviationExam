package com.suyash.mockcivilaviationexam.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suyash.mockcivilaviationexam.domain.model.Question

@Entity(tableName = "exam_questions")
data class ExamQuestionEntity(
    @PrimaryKey
    val id: String,
    val sectionId: String,
    val questionText: String,
    val optionA: String,
    val optionB: String,
    val optionC: String,
    val optionD: String?,
    val correctAnswer: String,
    val explanation: String?,
    val difficulty: String,
    val source: String,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): Question {
        return Question(
            id = id,
            sectionId = sectionId,
            questionText = questionText,
            optionA = optionA,
            optionB = optionB,
            optionC = optionC,
            optionD = optionD,
            correctAnswer = correctAnswer,
            explanation = explanation,
            difficulty = difficulty,
            source = source
        )
    }

    companion object {
        fun fromDomain(question: Question): ExamQuestionEntity {
            return ExamQuestionEntity(
                id = question.id,
                sectionId = question.sectionId,
                questionText = question.questionText,
                optionA = question.optionA,
                optionB = question.optionB,
                optionC = question.optionC,
                optionD = question.optionD,
                correctAnswer = question.correctAnswer,
                explanation = question.explanation,
                difficulty = question.difficulty,
                source = question.source
            )
        }
    }
}
