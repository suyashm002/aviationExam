package com.suyash.mockcivilaviationexam.data.local.repository

import com.suyash.mockcivilaviationexam.data.local.dao.SectionDao
import com.suyash.mockcivilaviationexam.domain.model.Section
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class SectionRepository(private val sectionDao: SectionDao) {
    fun getAllSections(): Flow<List<Section>> {
        return sectionDao.getAllSections().map { entities -> entities.map { it.toDomain() } }
    }
    
    suspend fun getAllSectionsSuspend(): List<Section> {
        return sectionDao.getAllSectionsSuspend().map { it.toDomain() }
    }
    
    suspend fun getSectionById(id: String): Section? {
        return sectionDao.getSectionById(id)?.toDomain()
    }
    
    suspend fun insertSection(section: Section) {
        sectionDao.insertSection(com.suyash.mockcivilaviationexam.data.local.entities.SectionEntity.fromDomain(section))
    }
    
    suspend fun insertSections(sections: List<Section>) {
        sectionDao.insertSections(sections.map { com.suyash.mockcivilaviationexam.data.local.entities.SectionEntity.fromDomain(it) })
    }
}

