package com.suyash.mockcivilaviationexam.ui.screens.flight

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.text.input.KeyboardCapitalization
import com.suyash.mockcivilaviationexam.domain.logbook.FlightTimeCalculator
import com.suyash.mockcivilaviationexam.domain.model.Aircraft
import com.suyash.mockcivilaviationexam.domain.model.FlightRole
import com.suyash.mockcivilaviationexam.ui.viewmodel.FlightEntryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightEntryScreen(
    onNavigateBack: () -> Unit,
    flightId: Long? = null,
    fromRecorder: Boolean = false,
    onManageAircraft: () -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: FlightEntryViewModel? = null
) {
    if (viewModel == null) {
        // Show placeholder while dependency injection is set up
        FlightEntryPlaceholder(onNavigateBack)
        return
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()

    LaunchedEffect(flightId, fromRecorder) {
        when {
            flightId != null -> viewModel.loadFlight(flightId)
            fromRecorder -> viewModel.initFromRecorder()
            else -> viewModel.initNewEntry()
        }
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onNavigateBack()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(scrollState)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = when {
                    flightId != null -> "Edit Flight"
                    fromRecorder -> "Review Flight"
                    else -> "Add Flight"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            TextButton(
                onClick = { viewModel.saveFlight() },
                enabled = uiState.isValid && !uiState.isSaving
            ) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Save")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))

        if (uiState.fromRecorder) {
            RecorderBanner(hasTrack = uiState.hasTrack)
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        FlightBasicInfo(
            date = uiState.date,
            onDateChange = viewModel::updateDate,
            departure = uiState.departureAerodrome,
            onDepartureChange = viewModel::updateDeparture,
            arrival = uiState.arrivalAerodrome,
            onArrivalChange = viewModel::updateArrival,
            isLocal = uiState.isLocalFlight,
            onLocalChange = viewModel::setLocalFlight,
            routeVia = uiState.routeVia,
            onRouteViaChange = viewModel::updateRouteVia
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AircraftSection(
            fleet = uiState.fleet,
            selectedId = uiState.aircraftId,
            onSelect = viewModel::selectAircraft,
            onManageAircraft = onManageAircraft,
            registration = uiState.aircraftRegistration,
            onRegistrationChange = viewModel::updateAircraftRegistration,
            type = uiState.aircraftType,
            onTypeChange = viewModel::updateAircraftType,
            model = uiState.aircraftModel,
            onModelChange = viewModel::updateAircraftModel
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        BlockAndAirTimeSection(
            offBlockText = uiState.offBlockText,
            onOffBlockChange = viewModel::updateOffBlockTime,
            takeoffText = uiState.takeoffText,
            onTakeoffChange = viewModel::updateTakeoffTime,
            landingText = uiState.landingText,
            onLandingChange = viewModel::updateLandingTime,
            onBlockText = uiState.onBlockText,
            onOnBlockChange = viewModel::updateOnBlockTime,
            blockTime = uiState.blockTime,
            airTime = uiState.airTime
        )

        Spacer(modifier = Modifier.height(16.dp))

        LandingsSection(
            dayLandings = uiState.dayLandings,
            onDayLandingsChange = viewModel::updateDayLandings,
            nightLandings = uiState.nightLandings,
            onNightLandingsChange = viewModel::updateNightLandings,
            instrumentApproaches = uiState.instrumentApproaches,
            onInstrumentApproachesChange = viewModel::updateInstrumentApproaches,
            showIfr = uiState.showIfrFields
        )

        Spacer(modifier = Modifier.height(16.dp))

        PerformanceSection(
            cruiseAltitudeFt = uiState.cruiseAltitudeFt,
            onCruiseAltitudeChange = viewModel::updateCruiseAltitude,
            cruiseSpeedKt = uiState.cruiseSpeedKt,
            onCruiseSpeedChange = viewModel::updateCruiseSpeed,
            maxAltitudeFt = uiState.maxAltitudeFt,
            onMaxAltitudeChange = viewModel::updateMaxAltitude,
            maxGroundSpeedKt = uiState.maxGroundSpeedKt,
            onMaxGroundSpeedChange = viewModel::updateMaxGroundSpeed,
            distanceNm = uiState.distanceNm,
            onDistanceChange = viewModel::updateDistance,
            hobbsStart = uiState.hobbsStart,
            onHobbsStartChange = viewModel::updateHobbsStart,
            hobbsEnd = uiState.hobbsEnd,
            onHobbsEndChange = viewModel::updateHobbsEnd,
            measured = uiState.hasTrack
        )

        Spacer(modifier = Modifier.height(16.dp))

        FlightTimeSection(
            totalTime = uiState.totalFlightTime,
            onTotalTimeChange = viewModel::updateTotalFlightTime,
            dayTime = uiState.dayTime,
            onDayTimeChange = viewModel::updateDayTime,
            nightTime = uiState.nightTime,
            onNightTimeChange = viewModel::updateNightTime
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        PilotRoleSection(
            role = uiState.flightRole,
            onRoleChange = viewModel::setRole,
            totalTime = uiState.totalFlightTime,
            picTime = uiState.picTime,
            onPicTimeChange = viewModel::updatePicTime,
            dualTime = uiState.dualTime,
            onDualTimeChange = viewModel::updateDualTime,
            coPilotTime = uiState.coPilotTime,
            onCoPilotTimeChange = viewModel::updateCoPilotTime,
            instructorTime = uiState.instructorTime,
            onInstructorTimeChange = viewModel::updateInstructorTime
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        FlightRulesSection(
            showIfr = uiState.showIfrFields,
            onShowIfrChange = viewModel::setShowIfrFields,
            isLocal = uiState.isLocalFlight,
            ifrTime = uiState.ifrTime,
            onIfrTimeChange = viewModel::updateIfrTime,
            vfrTime = uiState.vfrTime,
            crossCountryTime = uiState.crossCountryTime,
            onCrossCountryTimeChange = viewModel::updateCrossCountryTime
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SimulatorSection(
            isSimulator = uiState.isSimulator,
            onIsSimulatorChange = viewModel::updateIsSimulator,
            simulatorTime = uiState.simulatorTime,
            onSimulatorTimeChange = viewModel::updateSimulatorTime
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        TrainingSection(
            exerciseNumber = uiState.exerciseNumber,
            onExerciseNumberChange = viewModel::updateExerciseNumber,
            lessonNumber = uiState.lessonNumber,
            onLessonNumberChange = viewModel::updateLessonNumber,
            remarks = uiState.remarks,
            onRemarksChange = viewModel::updateRemarks
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        InstructorSection(
            instructorName = uiState.instructorName,
            onInstructorNameChange = viewModel::updateInstructorName,
            instructorLicense = uiState.instructorLicenseNumber,
            onInstructorLicenseChange = viewModel::updateInstructorLicense
        )
        
        if (uiState.validationErrors.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            ValidationErrorSection(errors = uiState.validationErrors)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun RecorderBanner(hasTrack: Boolean) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Timer, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
            Spacer(Modifier.width(10.dp))
            Text(
                text = if (hasTrack)
                    "Times, landings, altitude and distance were filled from your recorded flight. The GPS track will be saved with it."
                else
                    "Times and landings were filled from your recorded flight. Check them, add the details, and save.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun FlightBasicInfo(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    departure: String,
    onDepartureChange: (String) -> Unit,
    arrival: String,
    onArrivalChange: (String) -> Unit,
    isLocal: Boolean,
    onLocalChange: (Boolean) -> Unit,
    routeVia: String,
    onRouteViaChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Flight Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                onValueChange = { 
                    try {
                        onDateChange(LocalDate.parse(it))
                    } catch (e: Exception) {
                        // Invalid date, ignore
                    }
                },
                label = { Text("Date") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        text = "Local flight",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (isLocal) "Training in the local area, back to the same aerodrome"
                               else "Cross-country: lands somewhere else",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(checked = isLocal, onCheckedChange = onLocalChange)
            }

            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = departure,
                    onValueChange = onDepartureChange,
                    label = { Text(if (isLocal) "Aerodrome" else "Departure") },
                    placeholder = { Text("HKNW") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.weight(1f)
                )
                
                if (!isLocal) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "to",
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .size(20.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    OutlinedTextField(
                        value = arrival,
                        onValueChange = onArrivalChange,
                        label = { Text("Arrival") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = routeVia,
                onValueChange = onRouteViaChange,
                label = { Text(if (isLocal) "Training area / what you flew" else "Route via") },
                placeholder = { Text(if (isLocal) "e.g. Ngong Hills area, Ex 12–13, or Circuits RWY 07" else "e.g. Naivasha - Nakuru") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AircraftSection(
    fleet: List<Aircraft>,
    selectedId: Long?,
    onSelect: (Aircraft) -> Unit,
    onManageAircraft: () -> Unit,
    registration: String,
    onRegistrationChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    model: String,
    onModelChange: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = fleet.firstOrNull { it.id == selectedId }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Aircraft",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onManageAircraft) { Text("Manage") }
            }

            if (fleet.isNotEmpty()) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = selected?.displayName ?: "Choose from my aircraft",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("My aircraft") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        fleet.forEach { aircraft ->
                            DropdownMenuItem(
                                text = { Text(aircraft.displayName) },
                                onClick = { onSelect(aircraft); expanded = false }
                            )
                        }
                    }
                }
                Text(
                    text = "Your choice is remembered for the next flight.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            } else {
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            OutlinedTextField(
                value = registration,
                onValueChange = onRegistrationChange,
                label = { Text("Registration") },
                placeholder = { Text("e.g., 5Y-ABC") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                supportingText = if (selected != null && selected.registration.isBlank())
                    ({ Text("Saved to the ${selected.displayName} profile when you save this flight") }) else null,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = type,
                    onValueChange = onTypeChange,
                    label = { Text("Type") },
                    placeholder = { Text("e.g., C172") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = model,
                    onValueChange = onModelChange,
                    label = { Text("Model") },
                    placeholder = { Text("e.g., 172N") },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun BlockAndAirTimeSection(
    offBlockText: String,
    onOffBlockChange: (String) -> Unit,
    takeoffText: String,
    onTakeoffChange: (String) -> Unit,
    landingText: String,
    onLandingChange: (String) -> Unit,
    onBlockText: String,
    onOnBlockChange: (String) -> Unit,
    blockTime: Double,
    airTime: Double
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Block & Air Times",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "24-hour clock, e.g. 0930",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = offBlockText,
                    onValueChange = onOffBlockChange,
                    label = { Text("Off blocks") },
                    placeholder = { Text("0930") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = takeoffText,
                    onValueChange = onTakeoffChange,
                    label = { Text("Takeoff") },
                    placeholder = { Text("0945") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = landingText,
                    onValueChange = onLandingChange,
                    label = { Text("Landing") },
                    placeholder = { Text("1105") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                OutlinedTextField(
                    value = onBlockText,
                    onValueChange = onOnBlockChange,
                    label = { Text("On blocks") },
                    placeholder = { Text("1115") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ComputedTime("Block", blockTime)
                    ComputedTime("Air", airTime)
                    ComputedTime(
                        "Ground",
                        FlightTimeCalculator.groundTime(blockTime, airTime)
                    )
                }
            }

            if (blockTime > 0.0 || airTime > 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                val flying = Math.round(airTime * 60)
                val ground = Math.round(FlightTimeCalculator.groundTime(blockTime, airTime) * 60)
                Text(
                    text = "In the air $flying min · on the ground (taxi, run-up, holding) $ground min",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PerformanceSection(
    cruiseAltitudeFt: Int?,
    onCruiseAltitudeChange: (Int?) -> Unit,
    cruiseSpeedKt: Int?,
    onCruiseSpeedChange: (Int?) -> Unit,
    maxAltitudeFt: Int?,
    onMaxAltitudeChange: (Int?) -> Unit,
    maxGroundSpeedKt: Int?,
    onMaxGroundSpeedChange: (Int?) -> Unit,
    distanceNm: Double?,
    onDistanceChange: (Double?) -> Unit,
    hobbsStart: Double?,
    onHobbsStartChange: (Double?) -> Unit,
    hobbsEnd: Double?,
    onHobbsEndChange: (Double?) -> Unit,
    measured: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Altitude, speed & distance",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = if (measured) "Maximums and distance came from the GPS track"
                       else "Optional — what you flew, for your own record",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IntField("Cruise alt (ft)", cruiseAltitudeFt, onCruiseAltitudeChange, Modifier.weight(1f))
                IntField("Cruise speed (kt)", cruiseSpeedKt, onCruiseSpeedChange, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IntField("Max alt (ft)", maxAltitudeFt, onMaxAltitudeChange, Modifier.weight(1f))
                IntField("Max GS (kt)", maxGroundSpeedKt, onMaxGroundSpeedChange, Modifier.weight(1f))
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                DecimalField("Distance (NM)", distanceNm, onDistanceChange, Modifier.weight(1f))
                DecimalField("Hobbs start", hobbsStart, onHobbsStartChange, Modifier.weight(1f))
                DecimalField("Hobbs end", hobbsEnd, onHobbsEndChange, Modifier.weight(1f))
            }
            if (hobbsStart != null && hobbsEnd != null && hobbsEnd >= hobbsStart) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Hobbs %.1f h".format(hobbsEnd - hobbsStart),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun IntField(label: String, value: Int?, onChange: (Int?) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(value) { mutableStateOf(value?.toString() ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = { new ->
            text = new.filter { it.isDigit() }.take(6)
            onChange(text.toIntOrNull())
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = modifier
    )
}

@Composable
private fun DecimalField(label: String, value: Double?, onChange: (Double?) -> Unit, modifier: Modifier = Modifier) {
    var text by remember(value) { mutableStateOf(value?.let { if (it % 1.0 == 0.0) "%.0f".format(it) else it.toString() } ?: "") }
    OutlinedTextField(
        value = text,
        onValueChange = { new ->
            text = new.filter { it.isDigit() || it == '.' }.take(8)
            onChange(text.toDoubleOrNull())
        },
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        modifier = modifier
    )
}

@Composable
private fun ComputedTime(label: String, hours: Double) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = FlightTimeCalculator.formatHoursMinutes(hours),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun LandingsSection(
    dayLandings: Int,
    onDayLandingsChange: (Int) -> Unit,
    nightLandings: Int,
    onNightLandingsChange: (Int) -> Unit,
    instrumentApproaches: Int,
    onInstrumentApproachesChange: (Int) -> Unit,
    showIfr: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (showIfr) "Landings & Approaches" else "Landings",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(12.dp))

            CounterRow("Day landings", dayLandings, onDayLandingsChange)
            Spacer(modifier = Modifier.height(8.dp))
            CounterRow("Night landings", nightLandings, onNightLandingsChange)
            if (showIfr) {
                Spacer(modifier = Modifier.height(8.dp))
                CounterRow("Instrument approaches", instrumentApproaches, onInstrumentApproachesChange)
            }
        }
    }
}

@Composable
private fun CounterRow(label: String, value: Int, onChange: (Int) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = { onChange(value - 1) },
                enabled = value > 0
            ) {
                Icon(Icons.Default.Remove, contentDescription = "Decrease $label")
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 28.dp),
                textAlign = TextAlign.Center
            )
            IconButton(onClick = { onChange(value + 1) }) {
                Icon(Icons.Default.Add, contentDescription = "Increase $label")
            }
        }
    }
}

@Composable
private fun FlightTimeSection(
    totalTime: Double,
    onTotalTimeChange: (Double) -> Unit,
    dayTime: Double,
    onDayTimeChange: (Double) -> Unit,
    nightTime: Double,
    onNightTimeChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Flight Time",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = if (totalTime == 0.0) "" else totalTime.toString(),
                onValueChange = { value ->
                    onTotalTimeChange(value.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Total Flight Time (hours)") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = if (dayTime == 0.0) "" else dayTime.toString(),
                    onValueChange = { value ->
                        onDayTimeChange(value.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Day Time") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                OutlinedTextField(
                    value = if (nightTime == 0.0) "" else nightTime.toString(),
                    onValueChange = { value ->
                        onNightTimeChange(value.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Night Time") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PilotRoleSection(
    role: FlightRole,
    onRoleChange: (FlightRole) -> Unit,
    totalTime: Double,
    picTime: Double,
    onPicTimeChange: (Double) -> Unit,
    dualTime: Double,
    onDualTimeChange: (Double) -> Unit,
    coPilotTime: Double,
    onCoPilotTimeChange: (Double) -> Unit,
    instructorTime: Double,
    onInstructorTimeChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Dual or solo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                val options = listOf(
                    FlightRole.DUAL to "Dual",
                    FlightRole.SOLO to "Solo",
                    FlightRole.OTHER to "Other"
                )
                options.forEachIndexed { index, (value, label) ->
                    SegmentedButton(
                        selected = role == value,
                        onClick = { onRoleChange(value) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = options.size)
                    ) { Text(label) }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (role) {
                FlightRole.DUAL -> Text(
                    text = "Logged as dual: ${FlightTimeCalculator.formatHoursMinutes(totalTime)} with an instructor.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlightRole.SOLO -> Text(
                    text = "Logged as PIC: ${FlightTimeCalculator.formatHoursMinutes(totalTime)} solo.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                FlightRole.OTHER -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = if (picTime == 0.0) "" else picTime.toString(),
                            onValueChange = { value -> onPicTimeChange(value.toDoubleOrNull() ?: 0.0) },
                            label = { Text("PIC") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = if (dualTime == 0.0) "" else dualTime.toString(),
                            onValueChange = { value -> onDualTimeChange(value.toDoubleOrNull() ?: 0.0) },
                            label = { Text("Dual") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = if (coPilotTime == 0.0) "" else coPilotTime.toString(),
                            onValueChange = { value -> onCoPilotTimeChange(value.toDoubleOrNull() ?: 0.0) },
                            label = { Text("Co-Pilot") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                        OutlinedTextField(
                            value = if (instructorTime == 0.0) "" else instructorTime.toString(),
                            onValueChange = { value -> onInstructorTimeChange(value.toDoubleOrNull() ?: 0.0) },
                            label = { Text("Instructor") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FlightRulesSection(
    showIfr: Boolean,
    onShowIfrChange: (Boolean) -> Unit,
    isLocal: Boolean,
    ifrTime: Double,
    onIfrTimeChange: (Double) -> Unit,
    vfrTime: Double,
    crossCountryTime: Double,
    onCrossCountryTimeChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (showIfr) "Flight rules & cross-country" else "Cross-country",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                TextButton(onClick = { onShowIfrChange(!showIfr) }) {
                    Text(if (showIfr) "Hide IFR" else "Show IFR")
                }
            }

            if (showIfr) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = if (ifrTime == 0.0) "" else ifrTime.toString(),
                        onValueChange = { value -> onIfrTimeChange(value.toDoubleOrNull() ?: 0.0) },
                        label = { Text("IFR Time") },
                        modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value = FlightTimeCalculator.formatDecimal(vfrTime),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("VFR Time") },
                        supportingText = { Text("Total minus IFR") },
                        modifier = Modifier.weight(1f)
                    )
                }
            } else {
                Text(
                    text = "VFR flight — all of the time is logged as VFR.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (!isLocal || crossCountryTime > 0.0) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = if (crossCountryTime == 0.0) "" else crossCountryTime.toString(),
                    onValueChange = { value -> onCrossCountryTimeChange(value.toDoubleOrNull() ?: 0.0) },
                    label = { Text("Cross-country time (hours)") },
                    supportingText = { Text("Counts towards the PPL qualifying cross-country") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            } else {
                Text(
                    text = "Local flight — no cross-country time. Turn off \"Local flight\" above for a navigation exercise.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun SimulatorSection(
    isSimulator: Boolean,
    onIsSimulatorChange: (Boolean) -> Unit,
    simulatorTime: Double,
    onSimulatorTimeChange: (Double) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Simulator/Synthetic Flight",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Switch(
                    checked = isSimulator,
                    onCheckedChange = onIsSimulatorChange
                )
            }
            
            if (isSimulator) {
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = if (simulatorTime == 0.0) "" else simulatorTime.toString(),
                    onValueChange = { value ->
                        onSimulatorTimeChange(value.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Simulator Time") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
    }
}

@Composable
private fun TrainingSection(
    exerciseNumber: String?,
    onExerciseNumberChange: (String) -> Unit,
    lessonNumber: String?,
    onLessonNumberChange: (String) -> Unit,
    remarks: String,
    onRemarksChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Training & Remarks",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = exerciseNumber ?: "",
                    onValueChange = onExerciseNumberChange,
                    label = { Text("Exercise Number") },
                    placeholder = { Text("Optional") },
                    modifier = Modifier.weight(1f)
                )
                
                OutlinedTextField(
                    value = lessonNumber ?: "",
                    onValueChange = onLessonNumberChange,
                    label = { Text("Lesson Number") },
                    placeholder = { Text("Optional") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = remarks,
                onValueChange = onRemarksChange,
                label = { Text("Remarks") },
                placeholder = { Text("Training progress, weather conditions, etc.") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6
            )
        }
    }
}

@Composable
private fun InstructorSection(
    instructorName: String?,
    onInstructorNameChange: (String) -> Unit,
    instructorLicense: String?,
    onInstructorLicenseChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Instructor Information",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = instructorName ?: "",
                onValueChange = onInstructorNameChange,
                label = { Text("Instructor Name") },
                placeholder = { Text("Required for dual time") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = instructorLicense ?: "",
                onValueChange = onInstructorLicenseChange,
                label = { Text("Instructor License Number") },
                placeholder = { Text("License number") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ValidationErrorSection(
    errors: List<String>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = "Error",
                    tint = MaterialTheme.colorScheme.onErrorContainer
                )
                Text(
                    text = "Please fix the following errors:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            errors.forEach { error ->
                Text(
                    text = "• $error",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun FlightEntryPlaceholder(
    onNavigateBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Flight Entry",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = "Setting up flight logging...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onNavigateBack) {
            Text("Back")
        }
    }
}