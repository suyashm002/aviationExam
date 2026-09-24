package com.suyash.mockcivilaviationexam.ui.viewmodel

import com.suyash.mockcivilaviationexam.domain.logbook.LogbookUser
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.suyash.mockcivilaviationexam.data.cache.LogbookPreferences
import com.suyash.mockcivilaviationexam.domain.logbook.FlightRecorder
import com.suyash.mockcivilaviationexam.domain.model.Aircraft
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import com.suyash.mockcivilaviationexam.domain.usecase.AircraftDefaultsUseCase
import com.suyash.mockcivilaviationexam.domain.usecase.FlightOperationsUseCase
import com.suyash.mockcivilaviationexam.domain.usecase.ValidationError
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import com.suyash.mockcivilaviationexam.domain.logbook.FlightTimeCalculator
import java.time.LocalDate

class FlightEntryViewModel(
    private val flightOperationsUseCase: FlightOperationsUseCase,
    private val aircraftDefaults: AircraftDefaultsUseCase,
    private val prefs: LogbookPreferences,
    private val recorder: FlightRecorder
) : ViewModel() {

    private val _uiState = MutableStateFlow(FlightEntryUiState())
    val uiState: StateFlow<FlightEntryUiState> = _uiState.asStateFlow()

    private val userId: String get() = LogbookUser.id()
    private var currentFlightId: Long? = null
    private var initialised = false

    init {
        viewModelScope.launch {
            aircraftDefaults.fleet(userId).collect { fleet ->
                _uiState.update { it.copy(fleet = fleet) }
            }
        }
    }

    /**
     * A brand-new entry: pre-fill the remembered aircraft, the last aerodromes
     * and the last instructor, because a student's next lesson looks like the
     * previous one.
     */
    fun initNewEntry() {
        if (initialised) return
        initialised = true
        viewModelScope.launch {
            val aircraft = aircraftDefaults.resolveDefault(userId)
            _uiState.update {
                it.copy(
                    aircraftId = aircraft.id,
                    aircraftType = aircraft.type,
                    aircraftModel = aircraft.model,
                    aircraftRegistration = aircraft.registration,
                    cruiseAltitudeFt = it.cruiseAltitudeFt ?: aircraft.cruiseAltitudeFt,
                    cruiseSpeedKt = it.cruiseSpeedKt ?: aircraft.cruiseSpeedKt,
                    departureAerodrome = it.departureAerodrome.ifBlank { prefs.lastDeparture },
                    arrivalAerodrome = it.arrivalAerodrome.ifBlank { prefs.lastArrival.ifBlank { prefs.lastDeparture } },
                    instructorName = it.instructorName ?: prefs.lastInstructorName.ifBlank { null },
                    instructorLicenseNumber = it.instructorLicenseNumber ?: prefs.lastInstructorLicense.ifBlank { null }
                )
            }
            validateForm()
        }
    }

    /** An entry created from the Fly Now recorder: everything it measured goes in first. */
    fun initFromRecorder() {
        if (initialised) return
        initialised = true
        val recorded = recorder.summary()
        viewModelScope.launch {
            val fleet = aircraftDefaults.fleet(userId).first()
            val aircraft = recorded.aircraft?.id?.let { id -> fleet.firstOrNull { it.id == id } }
                ?: aircraftDefaults.resolveDefault(userId)
            _uiState.update {
                it.copy(
                    fromRecorder = true,
                    recorderSessionId = recorded.sessionId,
                    date = recorded.date,
                    departureAerodrome = recorded.departure.ifBlank { prefs.lastDeparture },
                    arrivalAerodrome = recorded.arrival.ifBlank { recorded.departure },
                    aircraftId = aircraft.id,
                    aircraftType = recorded.aircraft?.type ?: aircraft.type,
                    aircraftModel = recorded.aircraft?.model ?: aircraft.model,
                    aircraftRegistration = (recorded.aircraft?.registration ?: "").ifBlank { aircraft.registration },
                    offBlockText = FlightTimeCalculator.format(recorded.offBlock),
                    takeoffText = FlightTimeCalculator.format(recorded.takeoff),
                    landingText = FlightTimeCalculator.format(recorded.landing),
                    onBlockText = FlightTimeCalculator.format(recorded.onBlock),
                    blockTime = recorded.blockHours,
                    airTime = recorded.airHours,
                    totalFlightTime = if (recorded.blockHours > 0.0) recorded.blockHours else recorded.airHours,
                    dayLandings = recorded.landings,
                    maxAltitudeFt = recorded.maxAltitudeFt,
                    maxGroundSpeedKt = recorded.maxGroundSpeedKt,
                    distanceNm = recorded.distanceNm,
                    hasTrack = recorded.hasTrack,
                    cruiseAltitudeFt = aircraft.cruiseAltitudeFt,
                    cruiseSpeedKt = aircraft.cruiseSpeedKt,
                    instructorName = prefs.lastInstructorName.ifBlank { null },
                    instructorLicenseNumber = prefs.lastInstructorLicense.ifBlank { null }
                )
            }
            // Circuits: several airborne segments. The clock-time fields only
            // describe first takeoff to last landing, so keep the measured air
            // time rather than letting recalculateClockTimes() overwrite it.
            autoCalculateTimes()
            validateForm()
        }
    }

    fun selectAircraft(aircraft: Aircraft) {
        _uiState.update {
            it.copy(
                aircraftId = aircraft.id,
                aircraftType = aircraft.type,
                aircraftModel = aircraft.model,
                aircraftRegistration = aircraft.registration.ifBlank { it.aircraftRegistration },
                cruiseAltitudeFt = it.cruiseAltitudeFt ?: aircraft.cruiseAltitudeFt,
                cruiseSpeedKt = it.cruiseSpeedKt ?: aircraft.cruiseSpeedKt
            )
        }
        aircraftDefaults.setDefault(aircraft.id)
        validateForm()
    }

    fun updateRouteVia(text: String) {
        _uiState.update { it.copy(routeVia = text) }
    }

    fun updateCruiseAltitude(value: Int?) {
        _uiState.update { it.copy(cruiseAltitudeFt = value) }
    }

    fun updateCruiseSpeed(value: Int?) {
        _uiState.update { it.copy(cruiseSpeedKt = value) }
    }

    fun updateMaxAltitude(value: Int?) {
        _uiState.update { it.copy(maxAltitudeFt = value) }
    }

    fun updateMaxGroundSpeed(value: Int?) {
        _uiState.update { it.copy(maxGroundSpeedKt = value) }
    }

    fun updateDistance(value: Double?) {
        _uiState.update { it.copy(distanceNm = value) }
    }

    fun updateHobbsStart(value: Double?) {
        _uiState.update { it.copy(hobbsStart = value) }
    }

    fun updateHobbsEnd(value: Double?) {
        _uiState.update { it.copy(hobbsEnd = value) }
    }

    fun loadFlight(flightId: Long) {
        currentFlightId = flightId
        initialised = true
        viewModelScope.launch {
            val flight = flightOperationsUseCase.getFlightById(flightId) ?: return@launch
            _uiState.update {
                it.copy(
                    aircraftId = flight.aircraftId,
                    routeVia = flight.routeVia ?: "",
                    cruiseAltitudeFt = flight.cruiseAltitudeFt,
                    cruiseSpeedKt = flight.cruiseSpeedKt,
                    maxAltitudeFt = flight.maxAltitudeFt,
                    maxGroundSpeedKt = flight.maxGroundSpeedKt,
                    distanceNm = flight.distanceNm,
                    hobbsStart = flight.hobbsStart,
                    hobbsEnd = flight.hobbsEnd,
                    hasTrack = flight.hasTrack,
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
                // A recorded flight with circuits has more air time than
                // first-takeoff-to-last-landing; keep the measured figure when
                // it is the larger of the two.
                airTime = if (it.fromRecorder && it.airTime > air) it.airTime else air,
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

                val savedId = if (currentFlightId != null) {
                    flightOperationsUseCase.updateFlight(flight.copy(id = currentFlightId!!))
                    currentFlightId!!
                } else {
                    flightOperationsUseCase.addFlight(flight)
                }

                rememberDefaults(state)
                if (state.fromRecorder) recorder.consume(savedId)

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
            userId = userId,
            aircraftId = state.aircraftId,
            routeVia = state.routeVia.trim().ifBlank { null },
            cruiseAltitudeFt = state.cruiseAltitudeFt,
            cruiseSpeedKt = state.cruiseSpeedKt,
            maxAltitudeFt = state.maxAltitudeFt,
            maxGroundSpeedKt = state.maxGroundSpeedKt,
            distanceNm = state.distanceNm,
            hobbsStart = state.hobbsStart,
            hobbsEnd = state.hobbsEnd,
            hasTrack = state.hasTrack
        )
    }

    /**
     * Next lesson will look like this one: remember the aerodromes and the
     * instructor, and if the chosen aircraft profile had no registration yet,
     * adopt the one just typed so it never has to be typed again.
     */
    private suspend fun rememberDefaults(state: FlightEntryUiState) {
        prefs.lastDeparture = state.departureAerodrome
        prefs.lastArrival = state.arrivalAerodrome
        prefs.lastInstructorName = state.instructorName ?: ""
        prefs.lastInstructorLicense = state.instructorLicenseNumber ?: ""

        val chosen = state.fleet.firstOrNull { it.id == state.aircraftId } ?: return
        aircraftDefaults.setDefault(chosen.id)
        val typed = state.aircraftRegistration.trim().uppercase()
        if (chosen.registration.isBlank() && typed.isNotBlank()) {
            aircraftDefaults.save(chosen.copy(registration = typed))
        }
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
    // ---- Route, performance and recorder data ----
    val fleet: List<Aircraft> = emptyList(),
    val aircraftId: Long? = null,
    val routeVia: String = "",
    val cruiseAltitudeFt: Int? = null,
    val cruiseSpeedKt: Int? = null,
    val maxAltitudeFt: Int? = null,
    val maxGroundSpeedKt: Int? = null,
    val distanceNm: Double? = null,
    val hobbsStart: Double? = null,
    val hobbsEnd: Double? = null,
    val hasTrack: Boolean = false,
    val fromRecorder: Boolean = false,
    val recorderSessionId: String? = null,
    val validationErrors: List<String> = emptyList(),
    val isValid: Boolean = false,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)