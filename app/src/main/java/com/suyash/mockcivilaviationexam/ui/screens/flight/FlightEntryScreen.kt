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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suyash.mockcivilaviationexam.ui.viewmodel.FlightEntryViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightEntryScreen(
    onNavigateBack: () -> Unit,
    flightId: Long? = null,
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

    LaunchedEffect(flightId) {
        if (flightId != null) {
            viewModel.loadFlight(flightId)
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
                text = if (flightId == null) "Add Flight" else "Edit Flight",
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
        
        FlightBasicInfo(
            date = uiState.date,
            onDateChange = viewModel::updateDate,
            departure = uiState.departureAerodrome,
            onDepartureChange = viewModel::updateDeparture,
            arrival = uiState.arrivalAerodrome,
            onArrivalChange = viewModel::updateArrival
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        AircraftSection(
            registration = uiState.aircraftRegistration,
            onRegistrationChange = viewModel::updateAircraftRegistration,
            type = uiState.aircraftType,
            onTypeChange = viewModel::updateAircraftType,
            model = uiState.aircraftModel,
            onModelChange = viewModel::updateAircraftModel
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
            ifrTime = uiState.ifrTime,
            onIfrTimeChange = viewModel::updateIfrTime,
            vfrTime = uiState.vfrTime,
            onVfrTimeChange = viewModel::updateVfrTime,
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
private fun FlightBasicInfo(
    date: LocalDate,
    onDateChange: (LocalDate) -> Unit,
    departure: String,
    onDepartureChange: (String) -> Unit,
    arrival: String,
    onArrivalChange: (String) -> Unit
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
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = departure,
                    onValueChange = onDepartureChange,
                    label = { Text("Departure") },
                    modifier = Modifier.weight(1f)
                )
                
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
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun AircraftSection(
    registration: String,
    onRegistrationChange: (String) -> Unit,
    type: String,
    onTypeChange: (String) -> Unit,
    model: String,
    onModelChange: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = "Aircraft",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = registration,
                onValueChange = onRegistrationChange,
                label = { Text("Registration") },
                placeholder = { Text("e.g., 5Y-ABC") },
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
                    placeholder = { Text("e.g., Skyhawk") },
                    modifier = Modifier.weight(1f)
                )
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

@Composable
private fun PilotRoleSection(
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
                text = "Pilot Role",
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
                    value = if (picTime == 0.0) "" else picTime.toString(),
                    onValueChange = { value ->
                        onPicTimeChange(value.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("PIC") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                OutlinedTextField(
                    value = if (dualTime == 0.0) "" else dualTime.toString(),
                    onValueChange = { value ->
                        onDualTimeChange(value.toDoubleOrNull() ?: 0.0)
                    },
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
                    onValueChange = { value ->
                        onCoPilotTimeChange(value.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Co-Pilot") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                OutlinedTextField(
                    value = if (instructorTime == 0.0) "" else instructorTime.toString(),
                    onValueChange = { value ->
                        onInstructorTimeChange(value.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("Instructor") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
        }
    }
}

@Composable
private fun FlightRulesSection(
    ifrTime: Double,
    onIfrTimeChange: (Double) -> Unit,
    vfrTime: Double,
    onVfrTimeChange: (Double) -> Unit,
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
            Text(
                text = "Flight Rules & Cross Country",
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
                    value = if (ifrTime == 0.0) "" else ifrTime.toString(),
                    onValueChange = { value ->
                        onIfrTimeChange(value.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("IFR Time") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                
                OutlinedTextField(
                    value = if (vfrTime == 0.0) "" else vfrTime.toString(),
                    onValueChange = { value ->
                        onVfrTimeChange(value.toDoubleOrNull() ?: 0.0)
                    },
                    label = { Text("VFR Time") },
                    modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = if (crossCountryTime == 0.0) "" else crossCountryTime.toString(),
                onValueChange = { value ->
                    onCrossCountryTimeChange(value.toDoubleOrNull() ?: 0.0)
                },
                label = { Text("Cross Country Time") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
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
                placeholder = { Text("KCAA license number") },
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