package com.suyash.mockcivilaviationexam.data.local.dao

import androidx.room.*
import com.suyash.mockcivilaviationexam.data.local.entities.AircraftEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AircraftDao {
    @Query("SELECT * FROM aircraft WHERE userId = :userId ORDER BY registration")
    fun getAllAircraft(userId: String): Flow<List<AircraftEntity>>

    @Query("SELECT * FROM aircraft WHERE id = :id")
    suspend fun getAircraftById(id: Long): AircraftEntity?

    @Query("SELECT * FROM aircraft WHERE registration = :registration AND userId = :userId")
    suspend fun getAircraftByRegistration(registration: String, userId: String): AircraftEntity?

    @Query("SELECT * FROM aircraft WHERE type = :type AND userId = :userId")
    suspend fun getAircraftByType(type: String, userId: String): List<AircraftEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAircraft(aircraft: AircraftEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAircraft(aircraft: List<AircraftEntity>)

    @Update
    suspend fun updateAircraft(aircraft: AircraftEntity)

    @Delete
    suspend fun deleteAircraft(aircraft: AircraftEntity)

    @Query("DELETE FROM aircraft WHERE id = :id")
    suspend fun deleteAircraftById(id: Long)

    @Query("SELECT COUNT(*) FROM aircraft WHERE userId = :userId")
    suspend fun getAircraftCount(userId: String): Int
}