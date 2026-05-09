package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suyash.mockcivilaviationexam.data.local.entities.ExamSectionEntity

@Dao
interface ExamSectionDao {
    @Query("SELECT * FROM exam_sections ORDER BY name")
    suspend fun getAllSections(): List<ExamSectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<ExamSectionEntity>)

    @Query("DELETE FROM exam_sections")
    suspend fun deleteAll()
}
