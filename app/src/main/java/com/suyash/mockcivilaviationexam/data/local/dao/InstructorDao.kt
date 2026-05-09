package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.*
import com.suyash.mockcivilaviationexam.data.local.entities.InstructorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface InstructorDao {
    @Query("SELECT * FROM instructors WHERE userId = :userId ORDER BY name")
    fun getAllInstructors(userId: String): Flow<List<InstructorEntity>>

    @Query("SELECT * FROM instructors WHERE userId = :userId AND isActive = 1 ORDER BY name")
    fun getActiveInstructors(userId: String): Flow<List<InstructorEntity>>

    @Query("SELECT * FROM instructors WHERE id = :id")
    suspend fun getInstructorById(id: Long): InstructorEntity?

    @Query("SELECT * FROM instructors WHERE licenseNumber = :licenseNumber AND userId = :userId")
    suspend fun getInstructorByLicense(licenseNumber: String, userId: String): InstructorEntity?

    @Query("SELECT * FROM instructors WHERE name LIKE '%' || :name || '%' AND userId = :userId")
    suspend fun searchInstructorsByName(name: String, userId: String): List<InstructorEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstructor(instructor: InstructorEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInstructors(instructors: List<InstructorEntity>)

    @Update
    suspend fun updateInstructor(instructor: InstructorEntity)

    @Delete
    suspend fun deleteInstructor(instructor: InstructorEntity)

    @Query("DELETE FROM instructors WHERE id = :id")
    suspend fun deleteInstructorById(id: Long)

    @Query("SELECT COUNT(*) FROM instructors WHERE userId = :userId AND isActive = 1")
    suspend fun getActiveInstructorCount(userId: String): Int
}