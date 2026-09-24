package com.suyash.mockcivilaviationexam.ui.screens.logbook

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suyash.mockcivilaviationexam.R
import com.suyash.mockcivilaviationexam.domain.logbook.FlightPhase
import com.suyash.mockcivilaviationexam.domain.logbook.RecorderState
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import com.suyash.mockcivilaviationexam.ui.viewmodel.LogbookViewModel
import com.suyash.mockcivilaviationexam.ui.screens.logbook.components.FlightEntryCard
import com.suyash.mockcivilaviationexam.ui.screens.logbook.components.LogbookSummaryCard
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogbookScreen(
    onNavigateToFlightEntry: () -> Unit,
    onNavigateToFlightDetail: (Long) -> Unit,
    onNavigateToExport: () -> Unit = {},
    onNavigateToRecorder: () -> Unit = {},
    onNavigateToAircraft: () -> Unit = {},
    onNavigateBack: (() -> Unit)? = null,
    recorderState: RecorderState? = null,
    modifier: Modifier = Modifier,
    viewModel: LogbookViewModel? = null
) {
    // For now, we'll just show the UI structure
    // The viewModel will be properly initialized when dependency injection is set up
    if (viewModel == null) {
        // Show loading or placeholder
        EmptyLogbookPlaceholder(onNavigateToFlightEntry)
        return
    }
    
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadFlights()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (onNavigateBack != null) {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
                Text(
                    text = "Flight Logbook",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateToAircraft) {
                    Icon(
                        imageVector = Icons.Default.AirplanemodeActive,
                        contentDescription = "My aircraft",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onNavigateToExport) {
                    Icon(
                        imageVector = Icons.Default.PictureAsPdf,
                        contentDescription = "Export logbook as PDF",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                IconButton(onClick = onNavigateToFlightEntry) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add flight by hand",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))

        FlyNowCard(
            recorderState = recorderState,
            onClick = onNavigateToRecorder
        )

        Spacer(modifier = Modifier.height(12.dp))
        
        LogbookSummaryCard(
            summary = uiState.summary,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        SearchBar(
            searchQuery = uiState.searchQuery,
            onSearchQueryChange = viewModel::updateSearchQuery,
            modifier = Modifier.fillMaxWidth()
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.flights.isEmpty() -> {
                EmptyLogbookState(
                    onAddFlightClick = onNavigateToFlightEntry,
                    modifier = Modifier.fillMaxSize()
                )
            }
            
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.flights) { flight ->
                        FlightEntryCard(
                            flight = flight,
                            onClick = { onNavigateToFlightDetail(flight.id) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}

/**
 * The primary action on the logbook: start recording a flight, or return to
 * the one in progress. Shown above the summary so a student walking to the
 * aircraft finds it in one tap.
 */
@Composable
private fun FlyNowCard(
    recorderState: RecorderState?,
    onClick: () -> Unit
) {
    val inProgress = recorderState != null && recorderState.sessionId.isNotEmpty()
    val phaseText = when (recorderState?.phase) {
        FlightPhase.PREFLIGHT -> "Flight started — waiting for off blocks"
        FlightPhase.TAXI_OUT -> "Taxiing out"
        FlightPhase.AIRBORNE -> "Airborne now"
        FlightPhase.TAXI_IN -> "Taxiing in"
        FlightPhase.COMPLETE -> "On blocks — tap to save this flight"
        null -> ""
    }
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (inProgress) MaterialTheme.colorScheme.tertiaryContainer
                             else MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (inProgress) Icons.Default.Timer else Icons.Default.FlightTakeoff,
                contentDescription = null,
                tint = if (inProgress) MaterialTheme.colorScheme.onTertiaryContainer
                       else MaterialTheme.colorScheme.onPrimary
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    text = if (inProgress) "Flight in progress" else "Fly now",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (inProgress) MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.onPrimary
                )
                Text(
                    text = if (inProgress) phaseText
                           else "Time your flight from off blocks to on blocks, with GPS takeoff and landing detection",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (inProgress) MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = if (inProgress) MaterialTheme.colorScheme.onTertiaryContainer
                       else MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = { Text("Search flights") },
        placeholder = { Text("Aircraft registration, departure, arrival...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search"
            )
        },
        modifier = modifier
    )
}

@Composable
private fun EmptyLogbookState(
    onAddFlightClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "No flights recorded yet",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Text(
            text = "Start building your logbook by adding your first flight",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(
            onClick = onAddFlightClick
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add First Flight")
        }
    }
}

@Composable
private fun EmptyLogbookPlaceholder(
    onAddFlightClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.Send,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Text(
            text = "Pilot Logbook",
            style = MaterialTheme.typography.headlineSmall
        )
        
        Text(
            text = "Database is being initialized...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Button(onClick = onAddFlightClick) {
            Text("Continue")
        }
    }
}