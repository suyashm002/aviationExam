package com.suyash.mockcivilaviationexam.ui.screens.logbook.components

import com.suyash.mockcivilaviationexam.domain.logbook.FlightTimeCalculator
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.suyash.mockcivilaviationexam.domain.model.LogbookSummary

@Composable
fun LogbookSummaryCard(
    summary: LogbookSummary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Logbook Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryColumn(
                    title = "Flight Time",
                    items = listOf(
                        "Total" to hours(summary.totalTime),
                        "Block" to hours(summary.blockTime),
                        "Air" to hours(summary.airTime),
                        "Ground" to hours(
                            FlightTimeCalculator.groundTime(summary.blockTime, summary.airTime)
                        )
                    ),
                    modifier = Modifier.weight(1f)
                )

                SummaryColumn(
                    title = "Conditions",
                    items = listOf(
                        "Cross Country" to hours(summary.crossCountryTime),
                        "Night" to hours(summary.nightTime),
                        "IFR" to hours(summary.ifrTime),
                        "VFR" to hours(summary.vfrTime)
                    ),
                    modifier = Modifier.weight(1f)
                )

                SummaryColumn(
                    title = "Roles & Landings",
                    items = listOf(
                        "PIC" to hours(summary.picTime),
                        "Dual" to hours(summary.dualTime),
                        "Landings" to "${summary.totalLandings}",
                        "Night Ldg" to "${summary.nightLandings}"
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun SummaryColumn(
    title: String,
    items: List<Pair<String, String>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        items.forEach { (label, value) ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
        }
    }
}

/** Logbook-style duration, e.g. "12:25" rather than a raw 12.4166666. */
private fun hours(value: Double): String = FlightTimeCalculator.formatHoursMinutes(value)
