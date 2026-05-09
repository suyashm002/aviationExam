package com.suyash.mockcivilaviationexam.data.local.repository

import com.suyash.mockcivilaviationexam.data.local.dao.InstructorDao
import com.suyash.mockcivilaviationexam.data.local.entities.toDomainModel
import com.suyash.mockcivilaviationexam.data.local.entities.toEntity
import com.suyash.mockcivilaviationexam.domain.model.Instructor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class InstructorRepository(
    private val instructorDao: InstructorDao
) {
    fun getAllInstructors(userId: String): Flow<List<Instructor>> =
        instructorDao.getAllInstructors(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    fun getActiveInstructors(userId: String): Flow<List<Instructor>> =
        instructorDao.getActiveInstructors(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    suspend fun getInstructorById(id: Long): Instructor? =
        instructorDao.getInstructorById(id)?.toDomainModel()

    suspend fun getInstructorByLicense(licenseNumber: String, userId: String): Instructor? =
        instructorDao.getInstructorByLicense(licenseNumber, userId)?.toDomainModel()

    suspend fun searchInstructorsByName(name: String, userId: String): List<Instructor> =
        instructorDao.searchInstructorsByName(name, userId).map { it.toDomainModel() }

    suspend fun insertInstructor(instructor: Instructor): Long =
        instructorDao.insertInstructor(instructor.toEntity())

    suspend fun insertInstructors(instructors: List<Instructor>) =
        instructorDao.insertInstructors(instructors.map { it.toEntity() })

    suspend fun updateInstructor(instructor: Instructor) =
        instructorDao.updateInstructor(instructor.toEntity())

    suspend fun deleteInstructor(instructor: Instructor) =
        instructorDao.deleteInstructor(instructor.toEntity())

    suspend fun deleteInstructorById(id: Long) =
        instructorDao.deleteInstructorById(id)

    suspend fun getActiveInstructorCount(userId: String): Int =
        instructorDao.getActiveInstructorCount(userId)
}