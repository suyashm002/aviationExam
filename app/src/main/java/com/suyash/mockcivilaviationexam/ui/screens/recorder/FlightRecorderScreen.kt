package com.suyash.mockcivilaviationexam.ui.screens.recorder

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suyash.mockcivilaviationexam.domain.logbook.FlightPhase
import com.suyash.mockcivilaviationexam.domain.logbook.RecorderState
import com.suyash.mockcivilaviationexam.domain.model.Aircraft
import com.suyash.mockcivilaviationexam.service.FlightRecorderService
import com.suyash.mockcivilaviationexam.ui.theme.AviationSpacing
import com.suyash.mockcivilaviationexam.ui.theme.AvionicsBlue
import com.suyash.mockcivilaviationexam.ui.theme.HUDGreen
import com.suyash.mockcivilaviationexam.ui.viewmodel.FlightRecorderViewModel
import kotlinx.coroutines.delay
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * "Fly Now": a stopwatch shaped like a flight. The pilot taps Off blocks,
 * Takeoff, Landing and On blocks as they happen (or lets GPS do it), and the
 * screen shows block, air and ground time counting up. On blocks hands the
 * numbers to the entry form for review and saving.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlightRecorderScreen(
    onNavigateBack: () -> Unit,
    onSaveFlight: () -> Unit,
    onManageAircraft: () -> Unit,
    viewModel: FlightRecorderViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val form by viewModel.form.collectAsStateWithLifecycle()
    val state by viewModel.recorderState.collectAsStateWithLifecycle()
    var showDiscard by remember { mutableStateOf(false) }

    // One-second tick so the clocks move.
    var now by remember { mutableLongStateOf(System.currentTimeMillis()) }
    LaunchedEffect(state.isActive) {
        while (state.isActive) {
            now = System.currentTimeMillis()
            delay(1_000)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        val fineGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true
        viewModel.setPermissionDenied(!fineGranted)
        if (fineGranted && viewModel.recorder.state.value.isActive) {
            viewModel.recorder.setGpsEnabled(true)
            FlightRecorderService.start(context)
        }
    }

    fun hasLocationPermission() = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    fun requestPermissions() {
        val wanted = mutableListOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) wanted += Manifest.permission.POST_NOTIFICATIONS
        permissionLauncher.launch(wanted.toTypedArray())
    }

    // When a session is active with GPS wanted and permission held, make sure the
    // service is running (covers a restart of the app mid-flight).
    LaunchedEffect(state.isActive, state.gpsEnabled) {
        if (state.isActive && state.gpsEnabled && hasLocationPermission()) {
            FlightRecorderService.start(context)
        }
    }

    if (showDiscard) {
        AlertDialog(
            onDismissRequest = { showDiscard = false },
            title = { Text("Discard this flight?") },
            text = { Text("The times and any GPS track recorded so far will be deleted.") },
            confirmButton = {
                TextButton(onClick = {
                    showDiscard = false
                    FlightRecorderService.stop(context)
                    viewModel.discard()
                    onNavigateBack()
                }) { Text("Discard") }
            },
            dismissButton = { TextButton(onClick = { showDiscard = false }) { Text("Keep") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fly Now", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.isActive || state.phase == FlightPhase.COMPLETE && state.sessionId.isNotEmpty()) {
                        IconButton(onClick = { showDiscard = true }) {
                            Icon(Icons.Default.DeleteOutline, contentDescription = "Discard flight")
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
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(AviationSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AviationSpacing.md)
        ) {
            val sessionExists = state.sessionId.isNotEmpty()

            AircraftCard(
                fleet = form.fleet,
                selected = form.selectedAircraft,
                locked = sessionExists,
                onSelect = viewModel::selectAircraft,
                onManage = onManageAircraft
            )

            RouteCard(
                departure = form.departure,
                arrival = form.arrival,
                routeVia = form.routeVia,
                isLocal = form.isLocal,
                onDeparture = viewModel::updateDeparture,
                onArrival = viewModel::updateArrival,
                onRouteVia = viewModel::updateRouteVia,
                onLocal = viewModel::setLocal
            )

            GpsCard(
                requested = form.gpsRequested,
                permissionDenied = form.permissionDenied,
                fixAvailable = state.gpsFixAvailable,
                active = sessionExists && state.isActive,
                state = state,
                onToggle = { on ->
                    viewModel.setGpsRequested(on)
                    if (on) {
                        if (hasLocationPermission()) {
                            viewModel.setPermissionDenied(false)
                            if (viewModel.recorder.state.value.isActive) FlightRecorderService.start(context)
                        } else {
                            requestPermissions()
                        }
                    } else {
                        FlightRecorderService.stop(context)
                    }
                }
            )

            if (!sessionExists) {
                Button(
                    onClick = {
                        viewModel.startFlight()
                        if (form.gpsRequested && hasLocationPermission()) FlightRecorderService.start(context)
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = form.selectedAircraft != null
                ) {
                    Icon(Icons.Default.FlightTakeoff, contentDescription = null)
                    Spacer(Modifier.width(AviationSpacing.sm))
                    Text("Start flight", fontWeight = FontWeight.SemiBold)
                }
                Text(
                    "Then tap each event as it happens: off blocks, takeoff, landing, on blocks. " +
                        "With GPS on, takeoff and landing are detected for you.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                PhaseCard(state = state, now = now)

                ClocksCard(state = state, now = now)

                if (state.gpsEnabled) LiveStatsCard(state = state)

                LandingsRow(
                    landings = state.landings,
                    auto = state.autoDetectedLandings,
                    onAdd = viewModel::addLanding,
                    onRemove = viewModel::removeLanding
                )

                PhaseButtons(
                    state = state,
                    onOffBlocks = viewModel::offBlocks,
                    onTakeoff = viewModel::takeoff,
                    onLanding = viewModel::landing,
                    onOnBlocks = {
                        viewModel.onBlocks()
                        FlightRecorderService.stop(context)
                    },
                    onSave = onSaveFlight
                )
            }

            Spacer(Modifier.height(AviationSpacing.xl))
        }
    }
}

// ---------------------------------------------------------------------------

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AircraftCard(
    fleet: List<Aircraft>,
    selected: Aircraft?,
    locked: Boolean,
    onSelect: (Aircraft) -> Unit,
    onManage: () -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(AviationSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.AirplanemodeActive, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.width(AviationSpacing.sm))
                Text("Aircraft", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.weight(1f))
                TextButton(onClick = onManage) { Text("Manage") }
            }
            Spacer(Modifier.height(AviationSpacing.sm))
            ExposedDropdownMenuBox(
                expanded = expanded && !locked,
                onExpandedChange = { if (!locked) expanded = it }
            ) {
                OutlinedTextField(
                    value = selected?.displayName ?: "Choose an aircraft",
                    onValueChange = {},
                    readOnly = true,
                    enabled = !locked,
                    label = { Text("Flying today") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = expanded && !locked, onDismissRequest = { expanded = false }) {
                    fleet.forEach { aircraft ->
                        DropdownMenuItem(
                            text = { Text(aircraft.displayName) },
                            onClick = { onSelect(aircraft); expanded = false }
                        )
                    }
                }
            }
            if (selected != null && selected.registration.isBlank()) {
                Text(
                    "Add this aircraft's registration under Manage so it appears in your logbook.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            } else {
                Text(
                    "Remembered as your default until you change it.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun RouteCard(
    departure: String,
    arrival: String,
    routeVia: String,
    isLocal: Boolean,
    onDeparture: (String) -> Unit,
    onArrival: (String) -> Unit,
    onRouteVia: (String) -> Unit,
    onLocal: (Boolean) -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Column(Modifier.padding(AviationSpacing.lg)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Where", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.weight(1f))
                Text("Local flight", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(AviationSpacing.xs))
                Switch(checked = isLocal, onCheckedChange = onLocal)
            }
            Spacer(Modifier.height(AviationSpacing.sm))
            Row(horizontalArrangement = Arrangement.spacedBy(AviationSpacing.sm), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = departure, onValueChange = onDeparture,
                    label = { Text(if (isLocal) "Aerodrome" else "From") },
                    placeholder = { Text("HKNW") }, singleLine = true, modifier = Modifier.weight(1f),
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                )
                if (!isLocal) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "to", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = arrival, onValueChange = onArrival, label = { Text("To") },
                        placeholder = { Text("HKKR") }, singleLine = true, modifier = Modifier.weight(1f),
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
                    )
                }
            }
            Spacer(Modifier.height(AviationSpacing.sm))
            OutlinedTextField(
                value = routeVia, onValueChange = onRouteVia,
                label = { Text(if (isLocal) "Training area / exercise" else "Route via") },
                placeholder = { Text(if (isLocal) "Ngong Hills area, Ex 12" else "Naivasha - Nakuru") },
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GpsCard(
    requested: Boolean,
    permissionDenied: Boolean,
    fixAvailable: Boolean,
    active: Boolean,
    state: RecorderState,
    onToggle: (Boolean) -> Unit
) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            Modifier.padding(horizontal = AviationSpacing.lg, vertical = AviationSpacing.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.GpsFixed, contentDescription = null,
                tint = if (requested && fixAvailable) HUDGreen else MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(Modifier.width(AviationSpacing.md))
            Column(Modifier.weight(1f)) {
                Text("GPS auto-detect", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                val status = when {
                    !requested -> "Off — tap the buttons yourself"
                    permissionDenied -> "Location permission needed"
                    !active -> "Detects takeoff at 40 kt and landing below 30 kt; records the track"
                    fixAvailable -> "Fix OK · ${state.pointCount} points"
                    else -> "Waiting for a GPS fix…"
                }
                Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = requested, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun PhaseCard(state: RecorderState, now: Long) {
    val (label, colour) = when (state.phase) {
        FlightPhase.PREFLIGHT -> "Pre-flight" to MaterialTheme.colorScheme.onSurfaceVariant
        FlightPhase.TAXI_OUT -> "Taxiing out" to AvionicsBlue
        FlightPhase.AIRBORNE -> "Airborne" to HUDGreen
        FlightPhase.TAXI_IN -> "Taxiing in" to AvionicsBlue
        FlightPhase.COMPLETE -> "On blocks — ready to save" to MaterialTheme.colorScheme.primary
    }
    Surface(
        color = colour.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(AviationSpacing.lg), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = colour)
            Spacer(Modifier.height(AviationSpacing.xs))
            Row(horizontalArrangement = Arrangement.spacedBy(AviationSpacing.lg)) {
                TimeStamp("Off", state.offBlockAt)
                TimeStamp("T/O", state.takeoffAt)
                TimeStamp("Ldg", state.landingAt)
                TimeStamp("On", state.onBlockAt)
            }
        }
    }
}

@Composable
private fun TimeStamp(label: String, at: Long?) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(at?.let { clockTime(it) } ?: "--:--", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun ClocksCard(state: RecorderState, now: Long) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(AviationSpacing.lg),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            BigClock("In the air", state.airMillis(now), HUDGreen)
            BigClock("On the ground", state.groundMillis(now), AvionicsBlue)
            BigClock("Block", state.blockMillis(now), MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
private fun BigClock(label: String, millis: Long, colour: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(stopwatch(millis), fontSize = 26.sp, fontWeight = FontWeight.Bold, color = colour)
        Text(label, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text("${millis / 60_000} min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LiveStatsCard(state: RecorderState) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(AviationSpacing.md),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Stat("Alt", state.currentAltitudeFt?.let { "$it ft" } ?: "—")
            Stat("GS", state.currentGroundSpeedKt?.let { "$it kt" } ?: "—")
            Stat("Max alt", if (state.maxAltitudeFt > 0) "${state.maxAltitudeFt} ft" else "—")
            Stat("Max GS", if (state.maxGroundSpeedKt > 0) "${state.maxGroundSpeedKt} kt" else "—")
            Stat("Dist", if (state.distanceNm > 0) "%.1f NM".format(state.distanceNm) else "—")
        }
    }
}

@Composable
private fun Stat(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LandingsRow(landings: Int, auto: Int, onAdd: () -> Unit, onRemove: () -> Unit) {
    Card(elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = AviationSpacing.lg, vertical = AviationSpacing.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Landings", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                Text(
                    if (auto > 0) "$auto detected by GPS · add touch-and-goes with +" else "Add each touch-and-go with +",
                    style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = onRemove, enabled = landings > 0) { Icon(Icons.Default.Remove, contentDescription = "Remove landing") }
            Text("$landings", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            IconButton(onClick = onAdd) { Icon(Icons.Default.Add, contentDescription = "Add landing") }
        }
    }
}

@Composable
private fun PhaseButtons(
    state: RecorderState,
    onOffBlocks: () -> Unit,
    onTakeoff: () -> Unit,
    onLanding: () -> Unit,
    onOnBlocks: () -> Unit,
    onSave: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(AviationSpacing.sm)) {
        when (state.phase) {
            FlightPhase.PREFLIGHT -> {
                BigButton("Off blocks", Icons.Default.PlayArrow, onOffBlocks)
                OutlinedButton(onClick = onTakeoff, modifier = Modifier.fillMaxWidth()) { Text("Already rolling — mark takeoff") }
            }
            FlightPhase.TAXI_OUT -> {
                BigButton("Takeoff", Icons.Default.FlightTakeoff, onTakeoff)
                OutlinedButton(onClick = onOnBlocks, modifier = Modifier.fillMaxWidth()) { Text("Back on blocks (no flight)") }
            }
            FlightPhase.AIRBORNE -> {
                BigButton("Landing", Icons.Default.FlightLand, onLanding)
            }
            FlightPhase.TAXI_IN -> {
                BigButton("On blocks", Icons.Default.Stop, onOnBlocks)
                OutlinedButton(onClick = onTakeoff, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(AviationSpacing.xs))
                    Text("Takeoff again (another circuit)")
                }
            }
            FlightPhase.COMPLETE -> {
                BigButton("Save to logbook", Icons.Default.Check, onSave)
                Text(
                    "Review the times, add your instructor and exercise, then save.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun BigButton(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Button(onClick = onClick, modifier = Modifier.fillMaxWidth().height(60.dp)) {
        Icon(icon, contentDescription = null)
        Spacer(Modifier.width(AviationSpacing.sm))
        Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
    }
}

private val clockFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

private fun clockTime(epochMillis: Long): String =
    Instant.ofEpochMilli(epochMillis).atZone(ZoneId.systemDefault()).toLocalTime().format(clockFormat)

private fun stopwatch(millis: Long): String {
    val totalSeconds = millis / 1000
    val h = totalSeconds / 3600
    val m = (totalSeconds % 3600) / 60
    val s = totalSeconds % 60
    return if (h > 0) "%d:%02d:%02d".format(h, m, s) else "%02d:%02d".format(m, s)
}
