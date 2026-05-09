package com.suyash.mockcivilaviationexam.data.local.repository

import com.suyash.mockcivilaviationexam.data.local.dao.StudyMaterialDao
import com.suyash.mockcivilaviationexam.domain.model.StudyMaterial
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class StudyMaterialRepository(private val studyMaterialDao: StudyMaterialDao) {
    fun getStudyMaterialsBySection(sectionId: String): Flow<List<StudyMaterial>> {
        return studyMaterialDao.getStudyMaterialsBySection(sectionId)
            .map { entities -> entities.map { it.toDomain() } }
    }
    
    suspend fun getStudyMaterialById(id: String): StudyMaterial? {
        return studyMaterialDao.getStudyMaterialById(id)?.toDomain()
    }
    
    suspend fun insertStudyMaterial(material: StudyMaterial) {
        studyMaterialDao.insertStudyMaterial(
            com.suyash.mockcivilaviationexam.data.local.entities.StudyMaterialEntity.fromDomain(material)
        )
    }
    
    suspend fun insertStudyMaterials(materials: List<StudyMaterial>) {
        studyMaterialDao.insertStudyMaterials(
            materials.map { com.suyash.mockcivilaviationexam.data.local.entities.StudyMaterialEntity.fromDomain(it) }
        )
    }
}

