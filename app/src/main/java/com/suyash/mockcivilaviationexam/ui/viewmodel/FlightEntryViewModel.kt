package com.suyash.mockcivilaviationexam.ui.viewmodel

import com.suyash.mockcivilaviationexam.domain.logbook.LogbookUser
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import com.suyash.mockcivilaviationexam.domain.usecase.FlightOperationsUseCase
import com.suyash.mockcivilaviationexam.domain.usecase.ValidationError
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.suyash.mockcivilaviationexam.domain.logbook.FlightTimeCalculator
import java.time.LocalDate

class FlightEntryViewModel(
    private val flightOperationsUseCase: FlightOperationsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlightEntryUiState())
    val uiState: StateFlow<FlightEntryUiState> = _uiState.asStateFlow()

    private val userId: String get() = LogbookUser.id()
    private var currentFlightId: Long? = null

    fun loadFlight(flightId: Long) {
        currentFlightId = flightId
        viewModelScope.launch {
            val flight = flightOperationsUseCase.getFlightById(flightId) ?: return@launch
            _uiState.update {
                it.copy(
                    date = flight.date,
                    departureAerodrome = flight.departureAerodrome,
                    arrivalAerodrome = flight.arrivalAerodrome,
                    aircraftType = flight.aircraftType,
                    aircraftModel = flight.aircraftModel,
                    aircraftRegistration = flight.aircraftRegistration,
                    totalFlightTime = flight.totalFlightTime,
                    offBlockText = FlightTimeCalculator.format(flight.offBlockTime),
                    takeoffText = FlightTimeCalculator.format(flight.takeoffTime),
                    landingText = FlightTimeCalculator.format(flight.landingTime),
                    onBlockText = FlightTimeCalculator.format(flight.onBlockTime),
                    blockTime = flight.blockTime,
                    airTime = flight.airTime,
                    dayLandings = flight.dayLandings,
                    nightLandings = flight.nightLandings,
                    instrumentApproaches = flight.instrumentApproaches,
                    dayTime = flight.dayTime,
                    nightTime = flight.nightTime,
                    picTime = flight.picTime,
                    dualTime = flight.dualTime,
                    coPilotTime = flight.coPilotTime,
                    instructorTime = flight.instructorTime,
                    ifrTime = flight.ifrTime,
                    vfrTime = flight.vfrTime,
                    crossCountryTime = flight.crossCountryTime,
                    simulatorTime = flight.simulatorTime,
                    isSimulator = flight.isSimulator,
                    exerciseNumber = flight.exerciseNumber,
                    lessonNumber = flight.lessonNumber,
                    remarks = flight.remarks,
                    instructorName = flight.instructorName,
                    instructorLicenseNumber = flight.instructorLicenseNumber
                )
            }
            validateForm()
        }
    }

    fun updateDate(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
        validateForm()
    }

    fun updateDeparture(departure: String) {
        _uiState.update { it.copy(departureAerodrome = departure) }
        validateForm()
    }

    fun updateArrival(arrival: String) {
        _uiState.update { it.copy(arrivalAerodrome = arrival) }
        validateForm()
    }

    fun updateAircraftRegistration(registration: String) {
        _uiState.update { it.copy(aircraftRegistration = registration) }
        validateForm()
    }

    fun updateAircraftType(type: String) {
        _uiState.update { it.copy(aircraftType = type) }
        validateForm()
    }

    fun updateAircraftModel(model: String) {
        _uiState.update { it.copy(aircraftModel = model) }
        validateForm()
    }

    fun updateTotalFlightTime(time: Double) {
        _uiState.update { it.copy(totalFlightTime = time) }
        autoCalculateTimes()
        validateForm()
    }

    fun updateOffBlockTime(text: String) {
        _uiState.update { it.copy(offBlockText = text) }
        recalculateClockTimes()
    }

    fun updateTakeoffTime(text: String) {
        _uiState.update { it.copy(takeoffText = text) }
        recalculateClockTimes()
    }

    fun updateLandingTime(text: String) {
        _uiState.update { it.copy(landingText = text) }
        recalculateClockTimes()
    }

    fun updateOnBlockTime(text: String) {
        _uiState.update { it.copy(onBlockText = text) }
        recalculateClockTimes()
    }

    fun updateDayLandings(count: Int) {
        _uiState.update { it.copy(dayLandings = count.coerceAtLeast(0)) }
    }

    fun updateNightLandings(count: Int) {
        _uiState.update { it.copy(nightLandings = count.coerceAtLeast(0)) }
    }

    fun updateInstrumentApproaches(count: Int) {
        _uiState.update { it.copy(instrumentApproaches = count.coerceAtLeast(0)) }
    }

    /**
     * Derives block and air time from the clock times, and keeps total flight
     * time in step with block time — under ICAO rules the loggable flight time
     * IS the block time. The pilot can still override the total afterwards for
     * entries where no clock times were recorded.
     */
    private fun recalculateClockTimes() {
        val state = _uiState.value
        val block = FlightTimeCalculator.durationHours(
            FlightTimeCalculator.parse(state.offBlockText),
            FlightTimeCalculator.parse(state.onBlockText)
        )
        val air = FlightTimeCalculator.durationHours(
            FlightTimeCalculator.parse(state.takeoffText),
            FlightTimeCalculator.parse(state.landingText)
        )

        _uiState.update {
            it.copy(
                blockTime = block,
                airTime = air,
                totalFlightTime = if (block > 0.0) block else it.totalFlightTime
            )
        }
        if (block > 0.0) autoCalculateTimes()
        validateForm()
    }

    fun updateDayTime(time: Double) {
        _uiState.update { it.copy(dayTime = time) }
        validateForm()
    }

    fun updateNightTime(time: Double) {
        _uiState.update { it.copy(nightTime = time) }
        validateForm()
    }

    fun updatePicTime(time: Double) {
        _uiState.update { it.copy(picTime = time) }
        validateForm()
    }

    fun updateDualTime(time: Double) {
        _uiState.update { it.copy(dualTime = time) }
        validateForm()
    }

    fun updateCoPilotTime(time: Double) {
        _uiState.update { it.copy(coPilotTime = time) }
        validateForm()
    }

    fun updateInstructorTime(time: Double) {
        _uiState.update { it.copy(instructorTime = time) }
        validateForm()
    }

    fun updateIfrTime(time: Double) {
        _uiState.update { it.copy(ifrTime = time) }
        autoCalculateVfrTime()
        validateForm()
    }

    fun updateVfrTime(time: Double) {
        _uiState.update { it.copy(vfrTime = time) }
        validateForm()
    }

    fun updateCrossCountryTime(time: Double) {
        _uiState.update { it.copy(crossCountryTime = time) }
        validateForm()
    }

    fun updateIsSimulator(isSimulator: Boolean) {
        _uiState.update { 
            it.copy(
                isSimulator = isSimulator,
                simulatorTime = if (isSimulator) it.totalFlightTime else 0.0
            )
        }
        validateForm()
    }

    fun updateSimulatorTime(time: Double) {
        _uiState.update { it.copy(simulatorTime = time) }
        validateForm()
    }

    fun updateExerciseNumber(number: String) {
        _uiState.update { 
            it.copy(exerciseNumber = number.ifBlank { null })
        }
    }

    fun updateLessonNumber(number: String) {
        _uiState.update { 
            it.copy(lessonNumber = number.ifBlank { null })
        }
    }

    fun updateRemarks(remarks: String) {
        _uiState.update { it.copy(remarks = remarks) }
    }

    fun updateInstructorName(name: String) {
        _uiState.update { 
            it.copy(instructorName = name.ifBlank { null })
        }
        validateForm()
    }

    fun updateInstructorLicense(license: String) {
        _uiState.update { 
            it.copy(instructorLicenseNumber = license.ifBlank { null })
        }
        validateForm()
    }

    private fun autoCalculateTimes() {
        val state = _uiState.value
        val totalTime = state.totalFlightTime

        // Auto-calculate day time if night time is set
        if (state.nightTime > 0 && state.dayTime == 0.0) {
            val calculatedDayTime = (totalTime - state.nightTime).coerceAtLeast(0.0)
            _uiState.update { it.copy(dayTime = calculatedDayTime) }
        }

        // Auto-set VFR time if no IFR time and no VFR time set
        if (state.ifrTime == 0.0 && state.vfrTime == 0.0) {
            _uiState.update { it.copy(vfrTime = totalTime) }
        }
    }

    private fun autoCalculateVfrTime() {
        val state = _uiState.value
        val remainingTime = (state.totalFlightTime - state.ifrTime).coerceAtLeast(0.0)
        _uiState.update { it.copy(vfrTime = remainingTime) }
    }

    private fun validateForm() {
        val state = _uiState.value
        val flight = createFlightFromState(state)
        
        viewModelScope.launch {
            val validationErrors = flightOperationsUseCase.validateFlightEntry(flight)
            val errorMessages = validationErrors.map { error ->
                when (error) {
                    ValidationError.DEPARTURE_REQUIRED -> "Departure aerodrome is required"
                    ValidationError.ARRIVAL_REQUIRED -> "Arrival aerodrome is required"
                    ValidationError.AIRCRAFT_REGISTRATION_REQUIRED -> "Aircraft registration is required"
                    ValidationError.TOTAL_TIME_REQUIRED -> "Total flight time must be greater than 0"
                    ValidationError.DAY_NIGHT_EXCEEDS_TOTAL -> "Day and night time cannot exceed total flight time"
                    ValidationError.ROLE_TIME_EXCEEDS_TOTAL -> "Role times cannot exceed total flight time"
                    ValidationError.IFR_VFR_EXCEEDS_TOTAL -> "IFR and VFR time cannot exceed total flight time"
                    ValidationError.CROSS_COUNTRY_EXCEEDS_TOTAL -> "Cross country time cannot exceed total flight time"
                    ValidationError.SIMULATOR_TIME_REQUIRED -> "Simulator time is required when flight is marked as simulator"
                    ValidationError.INSTRUCTOR_LICENSE_REQUIRED -> "Instructor license number is required when instructor name is provided"
                }
            }
            
            _uiState.update { 
                it.copy(
                    validationErrors = errorMessages,
                    isValid = errorMessages.isEmpty()
                )
            }
        }
    }

    fun saveFlight() {
        val state = _uiState.value
        if (!state.isValid || state.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            
            try {
                val flight = createFlightFromState(state)
                
                if (currentFlightId != null) {
                    flightOperationsUseCase.updateFlight(flight.copy(id = currentFlightId!!))
                } else {
                    flightOperationsUseCase.addFlight(flight)
                }
                
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        isSaved = true
                    )
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isSaving = false,
                        error = e.message
                    )
                }
            }
        }
    }

    private fun createFlightFromState(state: FlightEntryUiState): FlightEntry {
        return FlightEntry(
            date = state.date,
            departureAerodrome = state.departureAerodrome,
            arrivalAerodrome = state.arrivalAerodrome,
            aircraftType = state.aircraftType,
            aircraftModel = state.aircraftModel,
            aircraftRegistration = state.aircraftRegistration,
            totalFlightTime = state.totalFlightTime,
            offBlockTime = FlightTimeCalculator.parse(state.offBlockText),
            takeoffTime = FlightTimeCalculator.parse(state.takeoffText),
            landingTime = FlightTimeCalculator.parse(state.landingText),
            onBlockTime = FlightTimeCalculator.parse(state.onBlockText),
            blockTime = state.blockTime,
            airTime = state.airTime,
            dayLandings = state.dayLandings,
            nightLandings = state.nightLandings,
            instrumentApproaches = state.instrumentApproaches,
            dayTime = state.dayTime,
            nightTime = state.nightTime,
            picTime = state.picTime,
            dualTime = state.dualTime,
            coPilotTime = state.coPilotTime,
            instructorTime = state.instructorTime,
            ifrTime = state.ifrTime,
            vfrTime = state.vfrTime,
            crossCountryTime = state.crossCountryTime,
            simulatorTime = state.simulatorTime,
            isSimulator = state.isSimulator,
            exerciseNumber = state.exerciseNumber,
            lessonNumber = state.lessonNumber,
            remarks = state.remarks,
            instructorName = state.instructorName,
            instructorLicenseNumber = state.instructorLicenseNumber,
            userId = userId
        )
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

data class FlightEntryUiState(
    val date: LocalDate = LocalDate.now(),
    val departureAerodrome: String = "",
    val arrivalAerodrome: String = "",
    val aircraftType: String = "",
    val aircraftModel: String = "",
    val aircraftRegistration: String = "",
    val totalFlightTime: Double = 0.0,
    // Raw text as typed, so a half-entered time like "09" is not thrown away
    // on every keystroke.
    val offBlockText: String = "",
    val takeoffText: String = "",
    val landingText: String = "",
    val onBlockText: String = "",
    val blockTime: Double = 0.0,
    val airTime: Double = 0.0,
    val dayLandings: Int = 0,
    val nightLandings: Int = 0,
    val instrumentApproaches: Int = 0,
    val dayTime: Double = 0.0,
    val nightTime: Double = 0.0,
    val picTime: Double = 0.0,
    val dualTime: Double = 0.0,
    val coPilotTime: Double = 0.0,
    val instructorTime: Double = 0.0,
    val ifrTime: Double = 0.0,
    val vfrTime: Double = 0.0,
    val crossCountryTime: Double = 0.0,
    val simulatorTime: Double = 0.0,
    val isSimulator: Boolean = false,
    val exerciseNumber: String? = null,
    val lessonNumber: String? = null,
    val remarks: String = "",
    val instructorName: String? = null,
    val instructorLicenseNumber: String? = null,
    val validationErrors: List<String> = emptyList(),
    val isValid: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)