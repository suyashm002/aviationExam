package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.suyash.mockcivilaviationexam.data.local.entities.QuestionFeedbackEntity

@Dao
interface QuestionFeedbackDao {

    @Insert
    suspend fun insert(feedback: QuestionFeedbackEntity)

    @Query("SELECT * FROM question_feedback WHERE synced = 0")
    suspend fun getUnsyncedFeedback(): List<QuestionFeedbackEntity>

    @Query("UPDATE question_feedback SET synced = 1 WHERE id IN (:ids)")
    suspend fun markAsSynced(ids: List<Long>)

    @Query("DELETE FROM question_feedback WHERE synced = 1 AND createdAt < :before")
    suspend fun deleteOldSynced(before: Long)
}
