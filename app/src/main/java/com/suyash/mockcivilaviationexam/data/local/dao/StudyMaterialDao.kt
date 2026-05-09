package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suyash.mockcivilaviationexam.data.local.entities.StudyMaterialEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyMaterialDao {
    @Query("SELECT * FROM study_materials WHERE sectionId = :sectionId ORDER BY title")
    fun getStudyMaterialsBySection(sectionId: String): Flow<List<StudyMaterialEntity>>
    
    @Query("SELECT * FROM study_materials WHERE id = :id")
    suspend fun getStudyMaterialById(id: String): StudyMaterialEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterial(material: StudyMaterialEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudyMaterials(materials: List<StudyMaterialEntity>)
}

