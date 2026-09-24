package com.suyash.mockcivilaviationexam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyash.mockcivilaviationexam.data.cache.LogbookPreferences
import com.suyash.mockcivilaviationexam.domain.logbook.LogbookUser
import com.suyash.mockcivilaviationexam.domain.model.Aircraft
import com.suyash.mockcivilaviationexam.domain.model.AircraftCategory
import com.suyash.mockcivilaviationexam.domain.model.EngineType
import com.suyash.mockcivilaviationexam.domain.usecase.AircraftDefaultsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * The pilot's fleet and which aircraft is remembered as the default. The draft
 * holds raw text while a profile is being added or edited so partially typed
 * numbers are not thrown away on every keystroke.
 */
class AircraftViewModel(
    private val aircraftDefaults: AircraftDefaultsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AircraftUiState())
    val uiState: StateFlow<AircraftUiState> = _uiState.asStateFlow()

    private val userId: String get() = LogbookUser.id()

    init {
        viewModelScope.launch {
            try {
                // Seeds the Cessna 172N for a pilot with no fleet yet and
                // ensures a default is remembered.
                val default = aircraftDefaults.resolveDefault(userId)
                _uiState.update { it.copy(defaultAircraftId = default.id) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
            aircraftDefaults.fleet(userId).collect { fleet ->
                _uiState.update { it.copy(fleet = fleet, isLoading = false) }
            }
        }
    }

    fun setDefault(aircraftId: Long) {
        aircraftDefaults.setDefault(aircraftId)
        _uiState.update { it.copy(defaultAircraftId = aircraftId) }
    }

    fun startAdd() {
        _uiState.update { it.copy(draft = AircraftDraft()) }
    }

    fun startEdit(aircraft: Aircraft) {
        _uiState.update {
            it.copy(
                draft = AircraftDraft(
                    id = aircraft.id,
                    registration = aircraft.registration,
                    type = aircraft.type,
                    model = aircraft.model,
                    manufacturer = aircraft.manufacturer ?: "",
                    category = aircraft.category,
                    engineType = aircraft.engineType,
                    cruiseSpeedText = aircraft.cruiseSpeedKt?.toString() ?: "",
                    cruiseAltitudeText = aircraft.cruiseAltitudeFt?.toString() ?: "",
                    createdAt = aircraft.createdAt
                )
            )
        }
    }

    fun cancelEdit() {
        _uiState.update { it.copy(draft = null) }
    }

    fun updateDraft(transform: (AircraftDraft) -> AircraftDraft) {
        _uiState.update { state ->
            state.draft?.let { state.copy(draft = transform(it)) } ?: state
        }
    }

    fun saveDraft() {
        val state = _uiState.value
        val draft = state.draft ?: return
        if (!draft.isValid || state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val wasEmpty = state.fleet.isEmpty()
                val id = aircraftDefaults.save(draft.toAircraft(userId))
                if (wasEmpty || state.defaultAircraftId == LogbookPreferences.NO_AIRCRAFT) {
                    aircraftDefaults.setDefault(id)
                    _uiState.update { it.copy(defaultAircraftId = id) }
                }
                _uiState.update { it.copy(draft = null, isSaving = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, error = e.message) }
            }
        }
    }

    /** Empty-state shortcut: create the trainer profile in one tap. */
    fun addCessna172N() {
        viewModelScope.launch {
            try {
                val id = aircraftDefaults.save(Aircraft.cessna172N(userId))
                aircraftDefaults.setDefault(id)
                _uiState.update { it.copy(defaultAircraftId = id) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun delete(aircraft: Aircraft) {
        viewModelScope.launch {
            try {
                aircraftDefaults.delete(aircraft)
                _uiState.update {
                    if (it.defaultAircraftId == aircraft.id)
                        it.copy(defaultAircraftId = LogbookPreferences.NO_AIRCRAFT)
                    else it
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class AircraftUiState(
    val fleet: List<Aircraft> = emptyList(),
    val defaultAircraftId: Long = LogbookPreferences.NO_AIRCRAFT,
    val draft: AircraftDraft? = null,
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null
)

/** Raw form text for an aircraft being added ([id] == 0) or edited. */
data class AircraftDraft(
    val id: Long = 0,
    val registration: String = "",
    val type: String = "",
    val model: String = "",
    val manufacturer: String = "",
    val category: AircraftCategory = AircraftCategory.AIRPLANE_SINGLE_ENGINE_LAND,
    val engineType: EngineType = EngineType.PISTON,
    val cruiseSpeedText: String = "",
    val cruiseAltitudeText: String = "",
    val createdAt: Long = System.currentTimeMillis()
) {
    val isEditing: Boolean get() = id != 0L

    val cruiseSpeedError: Boolean
        get() = cruiseSpeedText.isNotBlank() && cruiseSpeedText.trim().toIntOrNull() == null

    val cruiseAltitudeError: Boolean
        get() = cruiseAltitudeText.isNotBlank() && cruiseAltitudeText.trim().toIntOrNull() == null

    val isValid: Boolean
        get() = registration.isNotBlank() && type.isNotBlank() &&
            !cruiseSpeedError && !cruiseAltitudeError

    fun toAircraft(userId: String): Aircraft = Aircraft(
        id = id,
        type = type.trim().uppercase(),
        model = model.trim(),
        registration = registration.trim().uppercase(),
        manufacturer = manufacturer.trim().ifBlank { null },
        category = category,
        engineType = engineType,
        userId = userId,
        createdAt = createdAt,
        cruiseSpeedKt = cruiseSpeedText.trim().toIntOrNull(),
        cruiseAltitudeFt = cruiseAltitudeText.trim().toIntOrNull()
    )
}
