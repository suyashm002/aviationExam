package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suyash.mockcivilaviationexam.data.local.entities.ExamQuestionEntity

@Dao
interface ExamQuestionDao {
    @Query("SELECT * FROM exam_questions WHERE sectionId = :sectionId")
    suspend fun getQuestionsBySection(sectionId: String): List<ExamQuestionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<ExamQuestionEntity>)

    @Query("DELETE FROM exam_questions WHERE sectionId = :sectionId")
    suspend fun deleteQuestionsBySection(sectionId: String)

    @Query("DELETE FROM exam_questions")
    suspend fun deleteAll()
}
