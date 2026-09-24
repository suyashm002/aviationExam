package com.suyash.mockcivilaviationexam.ui.screens.aircraft

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.suyash.mockcivilaviationexam.domain.model.Aircraft
import com.suyash.mockcivilaviationexam.domain.model.AircraftCategory
import com.suyash.mockcivilaviationexam.domain.model.EngineType
import com.suyash.mockcivilaviationexam.ui.theme.AviationSpacing
import com.suyash.mockcivilaviationexam.ui.viewmodel.AircraftDraft
import com.suyash.mockcivilaviationexam.ui.viewmodel.AircraftViewModel

/**
 * The pilot's fleet. One aircraft is starred as the default and is pre-filled
 * on every new flight until the pilot changes it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AircraftScreen(
    onNavigateBack: () -> Unit,
    viewModel: AircraftViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    var pendingDelete by remember { mutableStateOf<Aircraft?>(null) }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("My Aircraft") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = viewModel::startAdd) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add aircraft")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = AviationSpacing.lg)
        ) {
            Text(
                text = "Your default aircraft is pre-filled on every new flight.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = AviationSpacing.md)
            )

            when {
                uiState.isLoading -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) { CircularProgressIndicator() }

                uiState.fleet.isEmpty() -> EmptyFleet(onAddCessna = viewModel::addCessna172N)

                else -> LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(AviationSpacing.sm),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 88.dp)
                ) {
                    items(uiState.fleet, key = { it.id }) { aircraft ->
                        AircraftCard(
                            aircraft = aircraft,
                            isDefault = aircraft.id == uiState.defaultAircraftId,
                            onSetDefault = { viewModel.setDefault(aircraft.id) },
                            onEdit = { viewModel.startEdit(aircraft) },
                            onDelete = { pendingDelete = aircraft }
                        )
                    }
                }
            }
        }
    }

    uiState.draft?.let { draft ->
        AircraftEditDialog(
            draft = draft,
            isSaving = uiState.isSaving,
            onChange = viewModel::updateDraft,
            onSave = viewModel::saveDraft,
            onDismiss = viewModel::cancelEdit
        )
    }

    pendingDelete?.let { aircraft ->
        AlertDialog(
            onDismissRequest = { pendingDelete = null },
            title = { Text("Remove ${aircraft.displayName}?") },
            text = { Text("Flights already logged against it keep their aircraft details.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.delete(aircraft)
                    pendingDelete = null
                }) { Text("Remove") }
            },
            dismissButton = {
                TextButton(onClick = { pendingDelete = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AircraftCard(
    aircraft: Aircraft,
    isDefault: Boolean,
    onSetDefault: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(AviationSpacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Flight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(AviationSpacing.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = aircraft.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "${aircraft.type} · ${aircraft.category.label()} · ${aircraft.engineType.label()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (isDefault) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Default aircraft",
                        tint = MaterialTheme.colorScheme.tertiary
                    )
                } else {
                    IconButton(onClick = onSetDefault) {
                        Icon(
                            imageVector = Icons.Outlined.StarOutline,
                            contentDescription = "Set as default",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            val performance = listOfNotNull(
                aircraft.cruiseSpeedKt?.let { "Cruise $it kt" },
                aircraft.cruiseAltitudeFt?.let { "Altitude $it ft" }
            )
            if (performance.isNotEmpty()) {
                Spacer(modifier = Modifier.height(AviationSpacing.xs))
                Text(
                    text = performance.joinToString(" · "),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                if (isDefault) {
                    Text(
                        text = "Default",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically)
                    )
                }
                IconButton(onClick = onEdit) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Remove",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyFleet(onAddCessna: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Flight,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(AviationSpacing.lg))
        Text(
            text = "No aircraft yet",
            style = MaterialTheme.typography.headlineSmall
        )
        Text(
            text = "Add the aircraft you train on. It will be selected on every new flight.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = AviationSpacing.xl)
        )
        Spacer(modifier = Modifier.height(AviationSpacing.xl))
        Button(onClick = onAddCessna) { Text("Add Cessna 172N") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AircraftEditDialog(
    draft: AircraftDraft,
    isSaving: Boolean,
    onChange: ((AircraftDraft) -> AircraftDraft) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (draft.isEditing) "Edit aircraft" else "Add aircraft") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(AviationSpacing.sm)
            ) {
                OutlinedTextField(
                    value = draft.registration,
                    onValueChange = { value -> onChange { it.copy(registration = value) } },
                    label = { Text("Registration") },
                    placeholder = { Text("5Y-ABC") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AviationSpacing.sm)) {
                    OutlinedTextField(
                        value = draft.type,
                        onValueChange = { value -> onChange { it.copy(type = value) } },
                        label = { Text("Type") },
                        placeholder = { Text("C172") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Characters),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = draft.model,
                        onValueChange = { value -> onChange { it.copy(model = value) } },
                        label = { Text("Model") },
                        placeholder = { Text("172N") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = draft.manufacturer,
                    onValueChange = { value -> onChange { it.copy(manufacturer = value) } },
                    label = { Text("Manufacturer") },
                    placeholder = { Text("Cessna") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                EnumDropdown(
                    label = "Category",
                    options = AircraftCategory.values().toList(),
                    selected = draft.category,
                    labelOf = { it.label() },
                    onSelect = { value -> onChange { it.copy(category = value) } }
                )
                EnumDropdown(
                    label = "Engine",
                    options = EngineType.values().toList(),
                    selected = draft.engineType,
                    labelOf = { it.label() },
                    onSelect = { value -> onChange { it.copy(engineType = value) } }
                )
                Row(horizontalArrangement = Arrangement.spacedBy(AviationSpacing.sm)) {
                    OutlinedTextField(
                        value = draft.cruiseSpeedText,
                        onValueChange = { value -> onChange { it.copy(cruiseSpeedText = value) } },
                        label = { Text("Cruise speed (kt)") },
                        placeholder = { Text("105") },
                        singleLine = true,
                        isError = draft.cruiseSpeedError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = draft.cruiseAltitudeText,
                        onValueChange = { value -> onChange { it.copy(cruiseAltitudeText = value) } },
                        label = { Text("Cruise altitude (ft)") },
                        placeholder = { Text("3500") },
                        singleLine = true,
                        isError = draft.cruiseAltitudeError,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave, enabled = draft.isValid && !isSaving) {
                Text(if (isSaving) "Saving…" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> EnumDropdown(
    label: String,
    options: List<T>,
    selected: T,
    labelOf: (T) -> String,
    onSelect: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = labelOf(selected),
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(labelOf(option)) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun AircraftCategory.label(): String = when (this) {
    AircraftCategory.AIRPLANE_SINGLE_ENGINE_LAND -> "Single-engine land"
    AircraftCategory.AIRPLANE_MULTI_ENGINE_LAND -> "Multi-engine land"
    AircraftCategory.AIRPLANE_SINGLE_ENGINE_SEA -> "Single-engine sea"
    AircraftCategory.AIRPLANE_MULTI_ENGINE_SEA -> "Multi-engine sea"
    AircraftCategory.HELICOPTER -> "Helicopter"
    AircraftCategory.GLIDER -> "Glider"
    AircraftCategory.BALLOON -> "Balloon"
    AircraftCategory.AIRSHIP -> "Airship"
}

private fun EngineType.label(): String = when (this) {
    EngineType.PISTON -> "Piston"
    EngineType.TURBOPROP -> "Turboprop"
    EngineType.TURBOJET -> "Turbojet"
    EngineType.TURBOFAN -> "Turbofan"
    EngineType.ELECTRIC -> "Electric"
}
