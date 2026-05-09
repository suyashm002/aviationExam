package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.suyash.mockcivilaviationexam.data.local.entities.SectionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SectionDao {
    @Query("SELECT * FROM sections ORDER BY id")
    fun getAllSections(): Flow<List<SectionEntity>>
    
    @Query("SELECT * FROM sections ORDER BY id")
    suspend fun getAllSectionsSuspend(): List<SectionEntity>
    
    @Query("SELECT * FROM sections WHERE id = :id")
    suspend fun getSectionById(id: String): SectionEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: SectionEntity)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSections(sections: List<SectionEntity>)
}

