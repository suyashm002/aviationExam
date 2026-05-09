package com.suyash.mockcivilaviationexam.data.local.repository

import com.suyash.mockcivilaviationexam.data.local.dao.AircraftDao
import com.suyash.mockcivilaviationexam.data.local.entities.toDomainModel
import com.suyash.mockcivilaviationexam.data.local.entities.toEntity
import com.suyash.mockcivilaviationexam.domain.model.Aircraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
class AircraftRepository(
    private val aircraftDao: AircraftDao
) {
    fun getAllAircraft(userId: String): Flow<List<Aircraft>> =
        aircraftDao.getAllAircraft(userId).map { entities ->
            entities.map { it.toDomainModel() }
        }

    suspend fun getAircraftById(id: Long): Aircraft? =
        aircraftDao.getAircraftById(id)?.toDomainModel()

    suspend fun getAircraftByRegistration(registration: String, userId: String): Aircraft? =
        aircraftDao.getAircraftByRegistration(registration, userId)?.toDomainModel()

    suspend fun getAircraftByType(type: String, userId: String): List<Aircraft> =
        aircraftDao.getAircraftByType(type, userId).map { it.toDomainModel() }

    suspend fun insertAircraft(aircraft: Aircraft): Long =
        aircraftDao.insertAircraft(aircraft.toEntity())

    suspend fun insertAircraft(aircraft: List<Aircraft>) =
        aircraftDao.insertAircraft(aircraft.map { it.toEntity() })

    suspend fun updateAircraft(aircraft: Aircraft) =
        aircraftDao.updateAircraft(aircraft.toEntity())

    suspend fun deleteAircraft(aircraft: Aircraft) =
        aircraftDao.deleteAircraft(aircraft.toEntity())

    suspend fun deleteAircraftById(id: Long) =
        aircraftDao.deleteAircraftById(id)

    suspend fun getAircraftCount(userId: String): Int =
        aircraftDao.getAircraftCount(userId)
}