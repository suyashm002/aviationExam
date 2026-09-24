package com.suyash.mockcivilaviationexam.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyash.mockcivilaviationexam.data.cache.LogbookPreferences
import com.suyash.mockcivilaviationexam.domain.logbook.FlightRecorder
import com.suyash.mockcivilaviationexam.domain.logbook.LogbookUser
import com.suyash.mockcivilaviationexam.domain.logbook.RecorderAircraft
import com.suyash.mockcivilaviationexam.domain.logbook.RecorderState
import com.suyash.mockcivilaviationexam.domain.model.Aircraft
import com.suyash.mockcivilaviationexam.domain.usecase.AircraftDefaultsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Drives the Fly Now screen. All flight state lives in [FlightRecorder] (a
 * process singleton) so it survives navigation and process death; this
 * ViewModel only holds the pre-flight form and the fleet list.
 */
class FlightRecorderViewModel(
    val recorder: FlightRecorder,
    private val aircraftDefaults: AircraftDefaultsUseCase,
    private val prefs: LogbookPreferences
) : ViewModel() {

    private val _form = MutableStateFlow(RecorderFormState())
    val form: StateFlow<RecorderFormState> = _form.asStateFlow()

    val recorderState: StateFlow<RecorderState> get() = recorder.state

    private val userId get() = LogbookUser.id()

    init {
        viewModelScope.launch {
            val default = aircraftDefaults.resolveDefault(userId)
            _form.update {
                it.copy(
                    selectedAircraft = it.selectedAircraft ?: default,
                    departure = it.departure.ifBlank { prefs.lastDeparture },
                    arrival = it.arrival.ifBlank { prefs.lastArrival.ifBlank { prefs.lastDeparture } },
                    routeVia = it.routeVia.ifBlank { prefs.lastRouteVia },
                    gpsRequested = prefs.gpsAutoDetect
                )
            }
        }
        viewModelScope.launch {
            aircraftDefaults.fleet(userId).collect { fleet ->
                _form.update { it.copy(fleet = fleet) }
            }
        }
        // Resuming an in-progress flight: mirror its route into the form.
        val active = recorder.state.value
        if (active.isActive) {
            _form.update {
                it.copy(
                    departure = active.departure, arrival = active.arrival, routeVia = active.routeVia,
                    isLocal = active.arrival.isBlank() || active.arrival.equals(active.departure, true),
                    gpsRequested = active.gpsEnabled
                )
            }
        }
    }

    fun selectAircraft(aircraft: Aircraft) {
        _form.update { it.copy(selectedAircraft = aircraft) }
        aircraftDefaults.setDefault(aircraft.id)
    }

    fun updateDeparture(text: String) {
        _form.update { it.copy(departure = text.uppercase(), arrival = if (it.isLocal) text.uppercase() else it.arrival) }
        syncRoute()
    }

    fun updateArrival(text: String) {
        _form.update { it.copy(arrival = text.uppercase()) }
        syncRoute()
    }

    fun updateRouteVia(text: String) {
        _form.update { it.copy(routeVia = text) }
        syncRoute()
    }

    fun setLocal(local: Boolean) {
        _form.update { it.copy(isLocal = local, arrival = if (local) it.departure else it.arrival) }
        syncRoute()
    }

    private fun syncRoute() {
        val f = _form.value
        if (recorder.state.value.isActive) recorder.setRoute(f.departure, f.arrival, f.routeVia)
    }

    /** The switch was toggled. The screen handles permissions and the service. */
    fun setGpsRequested(enabled: Boolean) {
        _form.update { it.copy(gpsRequested = enabled) }
        prefs.gpsAutoDetect = enabled
        if (recorder.state.value.isActive) recorder.setGpsEnabled(enabled)
    }

    fun setPermissionDenied(denied: Boolean) {
        _form.update { it.copy(permissionDenied = denied) }
    }

    fun startFlight() {
        val f = _form.value
        val aircraft = f.selectedAircraft?.let {
            RecorderAircraft(id = it.id, type = it.type, model = it.model, registration = it.registration)
        }
        prefs.lastDeparture = f.departure
        prefs.lastArrival = f.arrival
        if (f.isLocal) prefs.lastRouteVia = f.routeVia
        recorder.start(aircraft, f.departure, f.arrival, gps = f.gpsRequested && !f.permissionDenied, routeVia = f.routeVia)
    }

    fun offBlocks() = recorder.markOffBlock()
    fun takeoff() = recorder.markTakeoff()
    fun landing() = recorder.markLanding()
    fun onBlocks() = recorder.markOnBlock()
    fun addLanding() = recorder.adjustLandings(+1)
    fun removeLanding() = recorder.adjustLandings(-1)
    fun discard() = recorder.discard()
}

data class RecorderFormState(
    val fleet: List<Aircraft> = emptyList(),
    val selectedAircraft: Aircraft? = null,
    val departure: String = "",
    val arrival: String = "",
    /** Training area or route line. */
    val routeVia: String = "",
    /** Back to the same aerodrome — the PPL default. */
    val isLocal: Boolean = true,
    val gpsRequested: Boolean = true,
    val permissionDenied: Boolean = false
)
