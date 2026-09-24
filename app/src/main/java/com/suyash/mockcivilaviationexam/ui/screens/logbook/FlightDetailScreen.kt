package com.suyash.mockcivilaviationexam.ui.screens.logbook

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suyash.mockcivilaviationexam.domain.logbook.FlightTimeCalculator
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import com.suyash.mockcivilaviationexam.ui.screens.logbook.components.AltitudeProfileChart
import com.suyash.mockcivilaviationexam.ui.screens.logbook.components.SpeedProfileChart
import com.suyash.mockcivilaviationexam.ui.screens.logbook.components.TrackOutline
import com.suyash.mockcivilaviationexam.ui.theme.AviationElevation
import com.suyash.mockcivilaviationexam.ui.theme.AviationSpacing
import com.suyash.mockcivilaviationexam.ui.theme.AvionicsBlue
import com.suyash.mockcivilaviationexam.ui.theme.AviationGold
import com.suyash.mockcivilaviationexam.ui.viewmodel.FlightDetailViewModel
import java.time.format.DateTimeFormatter
import java.util.Locale

private val TITLE_DATE: DateTimeFormatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightDetailScreen(
    flightId: Long,
    onNavigateBack: () -> Unit,
    onEdit: (Long) -> Unit,
    onDeleted: () -> Unit,
    viewModel: FlightDetailViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var confirmDelete by remember { mutableStateOf(false) }

    LaunchedEffect(flightId) { viewModel.load(flightId) }
    LaunchedEffect(uiState.isDeleted) { if (uiState.isDeleted) onDeleted() }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Delete this flight?") },
            text = { Text("The entry and its recorded track will be removed from your logbook. This cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    confirmDelete = false
                    viewModel.delete()
                }) { Text("Delete", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) { Text("Cancel") }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = uiState.flight?.date?.format(TITLE_DATE) ?: "Flight",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (uiState.flight != null) {
                        IconButton(onClick = { onEdit(flightId) }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit flight")
                        }
                        IconButton(onClick = { confirmDelete = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete flight")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->
        val flight = uiState.flight
        when {
            uiState.isLoading -> Box(
                modifier = modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            flight == null -> Box(
                modifier = modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "Flight not found",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            else -> Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(AviationSpacing.lg),
                verticalArrangement = Arrangement.spacedBy(AviationSpacing.lg)
            ) {
                RouteHeader(flight)
                HeroCard(flight)
                TimesCard(flight)
                PerformanceCard(flight)
                LandingsCard(flight)
                if (uiState.track.size > 1) {
                    TrackCard(uiState.track)
                }
                TrainingCard(flight)
                BreakdownCard(flight)
                Spacer(Modifier.height(AviationSpacing.xl))
            }
        }
    }
}

// ---- sections ------------------------------------------------------------------

@Composable
private fun RouteHeader(flight: FlightEntry) {
    val aircraftLine = listOf(flight.aircraftModel, flight.aircraftType)
        .filter { it.isNotBlank() }
        .distinct()
        .joinToString(" ")
        .let { name -> if (flight.aircraftRegistration.isBlank()) name else "$name · ${flight.aircraftRegistration}" }
    Column {
        Text(
            text = flight.routeSummary.ifBlank { "Local flight" },
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (aircraftLine.isNotBlank()) {
            Text(
                text = aircraftLine,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HeroCard(flight: FlightEntry) {
    SectionCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BigNumber(label = "In the air", value = "${flight.flightMinutes}", unit = "min")
            BigNumber(label = "On the ground", value = "${flight.groundMinutes}", unit = "min")
            BigNumber(
                label = "Block",
                value = FlightTimeCalculator.formatHoursMinutes(flight.blockTime),
                unit = "h:mm"
            )
        }
        Spacer(Modifier.height(AviationSpacing.md))
        val total = (flight.flightMinutes + flight.groundMinutes).coerceAtLeast(1)
        val airFraction = flight.flightMinutes.toFloat() / total
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
        ) {
            if (airFraction > 0f) {
                Box(
                    Modifier
                        .weight(airFraction)
                        .fillMaxSize()
                        .background(AvionicsBlue)
                )
            }
            if (airFraction < 1f) {
                Box(
                    Modifier
                        .weight(1f - airFraction)
                        .fillMaxSize()
                        .background(AviationGold)
                )
            }
        }
        Spacer(Modifier.height(AviationSpacing.xs))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Legend(color = AvionicsBlue, text = "Airborne")
            Legend(color = AviationGold, text = "Taxi / run-up")
        }
    }
}

@Composable
private fun TimesCard(flight: FlightEntry) {
    SectionCard(title = "Times") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TimeStamp("Off blocks", flight.offBlockTime?.let(FlightTimeCalculator::format))
            TimeStamp("Takeoff", flight.takeoffTime?.let(FlightTimeCalculator::format))
            TimeStamp("Landing", flight.landingTime?.let(FlightTimeCalculator::format))
            TimeStamp("On blocks", flight.onBlockTime?.let(FlightTimeCalculator::format))
        }
    }
}

@Composable
private fun PerformanceCard(flight: FlightEntry) {
    val rows = buildList {
        flight.maxAltitudeFt?.let { add("Max altitude" to "%,d ft".format(it)) }
        flight.maxGroundSpeedKt?.let { add("Max groundspeed" to "$it kt") }
        flight.cruiseAltitudeFt?.let { add("Cruise altitude" to "%,d ft".format(it)) }
        flight.cruiseSpeedKt?.let { add("Cruise speed" to "$it kt") }
        flight.distanceNm?.let { add("Distance flown" to "%.1f NM".format(it)) }
        flight.hobbsStart?.let { add("Hobbs start" to "%.1f".format(it)) }
        flight.hobbsEnd?.let { add("Hobbs end" to "%.1f".format(it)) }
        flight.hobbsTime?.let { add("Hobbs time" to "%.1f h".format(it)) }
    }
    if (rows.isEmpty()) return
    SectionCard(title = "Performance") {
        rows.forEach { (label, value) -> KeyValueRow(label, value) }
    }
}

@Composable
private fun LandingsCard(flight: FlightEntry) {
    SectionCard(title = "Landings & approaches") {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            BigNumber(label = "Day", value = "${flight.dayLandings}", unit = "ldg")
            BigNumber(label = "Night", value = "${flight.nightLandings}", unit = "ldg")
            BigNumber(label = "Instrument", value = "${flight.instrumentApproaches}", unit = "app")
        }
    }
}

@Composable
private fun TrackCard(track: List<com.suyash.mockcivilaviationexam.domain.logbook.TrackPoint>) {
    SectionCard(title = "Recorded track") {
        Caption("Ground track, north up")
        TrackOutline(points = track)
        Spacer(Modifier.height(AviationSpacing.md))
        Caption("Altitude")
        AltitudeProfileChart(points = track)
        Spacer(Modifier.height(AviationSpacing.md))
        Caption("Groundspeed")
        SpeedProfileChart(points = track)
    }
}

@Composable
private fun TrainingCard(flight: FlightEntry) {
    val rows = buildList {
        flight.exerciseNumber?.takeIf { it.isNotBlank() }?.let { add("Exercise" to it) }
        flight.lessonNumber?.takeIf { it.isNotBlank() }?.let { add("Lesson" to it) }
        flight.instructorName?.takeIf { it.isNotBlank() }?.let { add("Instructor" to it) }
        flight.instructorLicenseNumber?.takeIf { it.isNotBlank() }?.let { add("Instructor licence" to it) }
    }
    val remarks = flight.remarks.trim()
    if (rows.isEmpty() && remarks.isEmpty()) return
    SectionCard(title = "Training") {
        rows.forEach { (label, value) -> KeyValueRow(label, value) }
        if (remarks.isNotEmpty()) {
            if (rows.isNotEmpty()) Spacer(Modifier.height(AviationSpacing.sm))
            Text(
                text = "Remarks",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(text = remarks, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun BreakdownCard(flight: FlightEntry) {
    SectionCard(title = "Time breakdown") {
        KeyValueRow("PIC", FlightTimeCalculator.formatHoursMinutes(flight.picTime))
        KeyValueRow("Dual", FlightTimeCalculator.formatHoursMinutes(flight.dualTime))
        KeyValueRow("Night", FlightTimeCalculator.formatHoursMinutes(flight.nightTime))
        KeyValueRow("IFR", FlightTimeCalculator.formatHoursMinutes(flight.ifrTime))
        KeyValueRow("Cross country", FlightTimeCalculator.formatHoursMinutes(flight.crossCountryTime))
        KeyValueRow("Simulator", FlightTimeCalculator.formatHoursMinutes(flight.simulatorTime))
    }
}

// ---- building blocks --------------------------------------------------------------

@Composable
private fun SectionCard(
    title: String? = null,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = AviationElevation.Level2)
    ) {
        Column(modifier = Modifier.padding(AviationSpacing.lg)) {
            if (title != null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.height(AviationSpacing.md))
            }
            content()
        }
    }
}

@Composable
private fun BigNumber(label: String, value: String, unit: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = unit,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun TimeStamp(label: String, value: String?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value?.takeIf { it.isNotBlank() } ?: "—",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun KeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AviationSpacing.xs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.End
        )
    }
}

@Composable
private fun Caption(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(bottom = AviationSpacing.xs)
    )
}

@Composable
private fun Legend(color: androidx.compose.ui.graphics.Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            Modifier
                .height(8.dp)
                .padding(end = AviationSpacing.xs)
                .clip(RoundedCornerShape(2.dp))
                .background(color)
                .padding(horizontal = 6.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
