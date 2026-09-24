package com.suyash.mockcivilaviationexam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyash.mockcivilaviationexam.data.local.repository.FlightTrackRepository
import com.suyash.mockcivilaviationexam.domain.logbook.TrackPoint
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import com.suyash.mockcivilaviationexam.domain.usecase.FlightOperationsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * One logged flight, with its GPS track when the recorder captured one.
 */
class FlightDetailViewModel(
    private val flightOperationsUseCase: FlightOperationsUseCase,
    private val tracks: FlightTrackRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlightDetailUiState())
    val uiState: StateFlow<FlightDetailUiState> = _uiState.asStateFlow()

    fun load(flightId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val flight = flightOperationsUseCase.getFlightById(flightId)
                val track = if (flight?.hasTrack == true) tracks.getTrack(flightId) else emptyList()
                _uiState.update {
                    it.copy(flight = flight, track = track, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun delete() {
        val flight = _uiState.value.flight ?: return
        viewModelScope.launch {
            try {
                tracks.deleteForFlight(flight.id)
                flightOperationsUseCase.deleteFlight(flight)
                _uiState.update { it.copy(isDeleted = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class FlightDetailUiState(
    val flight: FlightEntry? = null,
    val track: List<TrackPoint> = emptyList(),
    val isLoading: Boolean = true,
    val isDeleted: Boolean = false,
    val error: String? = null
)
