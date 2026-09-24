package com.suyash.mockcivilaviationexam.domain.usecase

import com.suyash.mockcivilaviationexam.data.cache.LogbookPreferences
import com.suyash.mockcivilaviationexam.data.local.repository.AircraftRepository
import com.suyash.mockcivilaviationexam.domain.model.Aircraft
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

/**
 * Which aircraft a new entry should assume.
 *
 * A student at an ATO flies the same aircraft for the whole PPL, so the choice
 * is remembered until changed. A pilot with no fleet yet gets a Cessna 172N
 * profile created for them; they only have to fill in the registration.
 */
class AircraftDefaultsUseCase(
    private val aircraft: AircraftRepository,
    private val prefs: LogbookPreferences
) {
    fun fleet(userId: String): Flow<List<Aircraft>> = aircraft.getAllAircraft(userId)

    /** Returns the remembered aircraft, seeding the 172N on first use. */
    suspend fun resolveDefault(userId: String): Aircraft {
        val fleet = aircraft.getAllAircraft(userId).first()
        if (fleet.isEmpty()) {
            val id = aircraft.insertAircraft(Aircraft.cessna172N(userId))
            prefs.defaultAircraftId = id
            return aircraft.getAircraftById(id) ?: Aircraft.cessna172N(userId).copy(id = id)
        }
        val remembered = fleet.firstOrNull { it.id == prefs.defaultAircraftId }
        if (remembered != null) return remembered
        // The remembered profile was deleted; fall back to the first one and remember that.
        prefs.defaultAircraftId = fleet.first().id
        return fleet.first()
    }

    fun setDefault(aircraftId: Long) {
        prefs.defaultAircraftId = aircraftId
    }

    fun isDefault(aircraftId: Long): Boolean = prefs.defaultAircraftId == aircraftId

    suspend fun save(aircraftModel: Aircraft): Long =
        if (aircraftModel.id == 0L) aircraft.insertAircraft(aircraftModel)
        else { aircraft.updateAircraft(aircraftModel); aircraftModel.id }

    suspend fun delete(aircraftModel: Aircraft) {
        aircraft.deleteAircraft(aircraftModel)
        if (prefs.defaultAircraftId == aircraftModel.id) {
            prefs.defaultAircraftId = LogbookPreferences.NO_AIRCRAFT
        }
    }
}
