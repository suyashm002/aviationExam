package com.suyash.mockcivilaviationexam.ui.screens.logbook.components

import com.suyash.mockcivilaviationexam.domain.logbook.FlightTimeCalculator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.suyash.mockcivilaviationexam.domain.model.FlightEntry
import java.time.format.DateTimeFormatter

@Composable
fun FlightEntryCard(
    flight: FlightEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = flight.date.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    
                    Text(
                        text = flight.routeSummary,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (flight.isSimulator) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Simulator",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                        if (flight.isEndorsed) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Endorsed",
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Text(
                        text = FlightTimeCalculator.formatHoursMinutes(flight.totalFlightTime),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )

                    if (flight.airTime > 0.0) {
                        Text(
                            text = "air ${FlightTimeCalculator.formatHoursMinutes(flight.airTime)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (flight.totalLandings > 0) {
                        Text(
                            text = "${flight.totalLandings} ldg",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AircraftInfo(
                    registration = flight.aircraftRegistration,
                    type = flight.aircraftType,
                    modifier = Modifier.weight(1f)
                )
                
                FlightTimeBreakdown(
                    picTime = flight.picTime,
                    dualTime = flight.dualTime,
                    nightTime = flight.nightTime,
                    ifrTime = flight.ifrTime,
                    crossCountryTime = flight.crossCountryTime
                )
            }
            
            if (flight.remarks.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = flight.remarks,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (flight.instructorName != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Instructor",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.secondary
                    )
                    Text(
                        text = flight.instructorName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    if (flight.instructorLicenseNumber != null) {
                        Text(
                            text = "(${flight.instructorLicenseNumber})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AircraftInfo(
    registration: String,
    type: String,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = registration,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = type,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FlightTimeBreakdown(
    picTime: Double,
    dualTime: Double,
    nightTime: Double,
    ifrTime: Double,
    crossCountryTime: Double
) {
    Column(
        horizontalAlignment = Alignment.End
    ) {
        val timeItems = listOfNotNull(
            if (picTime > 0) "PIC: ${picTime}h" else null,
            if (dualTime > 0) "Dual: ${dualTime}h" else null,
            if (nightTime > 0) "Night: ${nightTime}h" else null,
            if (ifrTime > 0) "IFR: ${ifrTime}h" else null,
            if (crossCountryTime > 0) "XC: ${crossCountryTime}h" else null
        ).take(3)
        
        timeItems.forEach { timeText ->
            Text(
                text = timeText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}