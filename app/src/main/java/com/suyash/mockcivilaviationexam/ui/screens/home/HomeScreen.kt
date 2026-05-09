package com.suyash.mockcivilaviationexam.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.suyash.mockcivilaviationexam.ui.theme.*
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.suyash.mockcivilaviationexam.CivilAviationApp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suyash.mockcivilaviationexam.data.billing.BillingManager
import com.suyash.mockcivilaviationexam.ui.viewmodel.ExamHomeViewModel
import com.suyash.mockcivilaviationexam.ui.viewmodel.SubscriptionViewModel
import com.suyash.mockcivilaviationexam.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToExam: (String) -> Unit,
    onNavigateToProfile: () -> Unit,
    onNavigateToHistory: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExamHomeViewModel = run {
        val context = LocalContext.current
        val app = context.applicationContext as CivilAviationApp
        viewModel { ExamHomeViewModel(app.examRepository) }
    }
) {
    val examSectionsData = getExamSectionsData()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.loadUserStats()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Aviation Exams",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                ),
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Exam History",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNavigateToProfile) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Welcome Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                AvionicsBlueDark.copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.FlightTakeoff,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = AvionicsBlueDark
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "KCAA Mock Exams",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Master your aviation knowledge",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Exam Sections",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "Select a section to start practicing",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(12.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 4.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(examSectionsData) { sectionData ->
                    ExamSectionCard(
                        sectionData = sectionData,
                        onClick = {
                            if (sectionData.isAvailable) {
                                onNavigateToExam(sectionData.id)
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        title = "Exams",
                        value = "${uiState.totalExamsTaken}",
                        icon = Icons.Default.Check
                    )

                    StatItem(
                        title = "Avg Score",
                        value = "${uiState.averageScore.toInt()}%",
                        icon = Icons.Default.KeyboardArrowUp
                    )

                    StatItem(
                        title = "Best",
                        value = "${uiState.bestScore.toInt()}%",
                        icon = Icons.Default.Star
                    )
                }
            }
        }
    }
}

@Composable
private fun ExamSectionCard(
    sectionData: ExamSectionData,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 0.dp,
            pressedElevation = 2.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (sectionData.isAvailable)
                MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
            else
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Left accent stripe
            if (sectionData.isAvailable) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(56.dp)
                        .align(Alignment.CenterStart)
                        .background(AvionicsBlue, RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (sectionData.isAvailable)
                                AvionicsBlueSubtle
                            else
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.08f),
                            shape = RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = ImageVector.vectorResource(sectionData.iconRes),
                        contentDescription = sectionData.name,
                        modifier = Modifier.size(24.dp),
                        tint = if (sectionData.isAvailable)
                            AvionicsBlue
                        else
                            MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = sectionData.name,
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = if (sectionData.isAvailable)
                        MaterialTheme.colorScheme.onSurface
                    else
                        MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = if (sectionData.isAvailable) sectionData.questionCount else "Coming Soon",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (sectionData.isAvailable)
                        MaterialTheme.colorScheme.onSurfaceVariant
                    else
                        MaterialTheme.colorScheme.outline,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )

                if (sectionData.isAvailable && !sectionData.hasFreeExam) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = AviationGold.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "PRO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = AviationGold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    title: String,
    value: String,
    icon: ImageVector
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier.size(18.dp),
            tint = AviationGold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Bold
            ),
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private data class ExamSectionData(
    val id: String,
    val name: String,
    val description: String,
    val questionCount: String,
    val iconRes: Int,
    val isAvailable: Boolean = true,
    val hasFreeExam: Boolean = true
)

@Composable
private fun getExamSectionsData(): List<ExamSectionData> {
    val context = LocalContext.current
    val app = context.applicationContext as CivilAviationApp
    val isSubscribed = app.billingManager.subscriptionState.collectAsStateWithLifecycle()

    fun hasFree(sectionId: String): Boolean {
        if (!app.featureFlags.subscriptionEnabled) return true
        return isSubscribed.value.isSubscribed || app.subscriptionRepository.canTakeFreeExam(sectionId)
    }

    return listOf(
        ExamSectionData(
            id = "air_law",
            name = "Air Law",
            description = "Aviation regulations and legal requirements",
            questionCount = "16 Questions",
            iconRes = R.drawable.ic_air_law,
            isAvailable = app.examRepository.isDataAvailable("air_law"),
            hasFreeExam = hasFree("air_law")
        ),
        ExamSectionData(
            id = "aircraft_general",
            name = "Aircraft General",
            description = "Aircraft systems and general knowledge",
            questionCount = "16 Questions",
            iconRes = R.drawable.ic_aircraft_general,
            isAvailable = app.examRepository.isDataAvailable("aircraft_general"),
            hasFreeExam = hasFree("aircraft_general")
        ),
        ExamSectionData(
            id = "flight_performance",
            name = "Flight Performance",
            description = "Aircraft performance and planning",
            questionCount = "16 Questions",
            iconRes = R.drawable.ic_flight_performance,
            isAvailable = app.examRepository.isDataAvailable("flight_performance"),
            hasFreeExam = hasFree("flight_performance")
        ),
        ExamSectionData(
            id = "human_performance",
            name = "Human Performance",
            description = "Human factors and limitations",
            questionCount = "16 Questions",
            iconRes = R.drawable.ic_human_performance,
            isAvailable = app.examRepository.isDataAvailable("human_performance"),
            hasFreeExam = hasFree("human_performance")
        ),
        ExamSectionData(
            id = "meteorology",
            name = "Meteorology",
            description = "Weather and atmospheric conditions",
            questionCount = "16 Questions",
            iconRes = R.drawable.ic_meteorology,
            isAvailable = app.examRepository.isDataAvailable("meteorology"),
            hasFreeExam = hasFree("meteorology")
        ),
        ExamSectionData(
            id = "navigation",
            name = "Navigation",
            description = "Flight planning and navigation systems",
            questionCount = "16 Questions",
            iconRes = R.drawable.ic_navigation,
            isAvailable = app.examRepository.isDataAvailable("navigation"),
            hasFreeExam = hasFree("navigation")
        ),
        ExamSectionData(
            id = "operational_procedures",
            name = "Operational Procedures",
            description = "Standard operating procedures",
            questionCount = "16 Questions",
            iconRes = R.drawable.ic_operational_procedures,
            isAvailable = app.examRepository.isDataAvailable("operational_procedures"),
            hasFreeExam = hasFree("operational_procedures")
        ),
        ExamSectionData(
            id = "principles_of_flight",
            name = "Principles of Flight",
            description = "Aerodynamics and flight theory",
            questionCount = "16 Questions",
            iconRes = R.drawable.ic_principles_flight,
            isAvailable = app.examRepository.isDataAvailable("principles_of_flight"),
            hasFreeExam = hasFree("principles_of_flight")
        )
    )
}
