package com.suyash.mockcivilaviationexam.ui.screens.exam

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.activity.compose.BackHandler
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.suyash.mockcivilaviationexam.ui.theme.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.suyash.mockcivilaviationexam.CivilAviationApp
import com.suyash.mockcivilaviationexam.data.local.entities.QuestionFeedbackEntity
import com.suyash.mockcivilaviationexam.ui.viewmodel.ExamViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamScreen(
    sectionId: String,
    questionCount: Int = 16,
    onNavigateBack: () -> Unit,
    onExamComplete: (String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ExamViewModel = run {
        val context = LocalContext.current
        val app = context.applicationContext as CivilAviationApp
        viewModel { ExamViewModel(app.examRepository) }
    }
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(sectionId, questionCount) {
        viewModel.startExam(sectionId, questionCount)
    }

    LaunchedEffect(uiState.isCompleted) {
        if (uiState.isCompleted && uiState.examSessionId.isNotEmpty()) {
            onExamComplete(uiState.examSessionId)
        }
    }

    when {
        uiState.isLoading -> {
            LoadingScreen()
        }
        uiState.error != null -> {
            ErrorScreen(
                error = uiState.error!!,
                onRetry = { viewModel.startExam(sectionId) },
                onBack = onNavigateBack
            )
        }
        uiState.questions.isEmpty() -> {
            EmptyQuestionsScreen(onBack = onNavigateBack)
        }
        else -> {
            ExamContent(
                uiState = uiState,
                onAnswerSelected = viewModel::selectAnswer,
                onNextQuestion = viewModel::nextQuestion,
                onPreviousQuestion = viewModel::previousQuestion,
                onSubmitExam = viewModel::submitExam,
                onNavigateBack = onNavigateBack
            )
        }
    }
}

@Composable
private fun LoadingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator(
                color = AviationGold,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Loading exam questions...",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun ErrorScreen(
    error: String,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "Error",
                modifier = Modifier.size(56.dp),
                tint = AviationError
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Failed to load exam",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = error,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onBack) {
                    Text("Back")
                }
                Button(
                    onClick = onRetry,
                    colors = ButtonDefaults.buttonColors(containerColor = AvionicsBlue)
                ) {
                    Text("Retry")
                }
            }
        }
    }
}

@Composable
private fun EmptyQuestionsScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Quiz,
                contentDescription = "No questions",
                modifier = Modifier.size(56.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "No questions available",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "This section doesn't have any questions yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = AvionicsBlue)
            ) {
                Text("Back")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExamContent(
    uiState: com.suyash.mockcivilaviationexam.ui.viewmodel.ExamUiState,
    onAnswerSelected: (String) -> Unit,
    onNextQuestion: () -> Unit,
    onPreviousQuestion: () -> Unit,
    onSubmitExam: () -> Unit,
    onNavigateBack: () -> Unit
) {
    var showExitDialog by remember { mutableStateOf(false) }

    // Block back press during submission; otherwise show confirmation
    BackHandler(enabled = !uiState.isCompleted) {
        if (!uiState.isSubmitting) {
            showExitDialog = true
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text("Exit Exam?", fontWeight = FontWeight.Bold) },
            text = { Text("Your progress will be lost. Are you sure you want to exit?") },
            confirmButton = {
                Button(
                    onClick = {
                        showExitDialog = false
                        onNavigateBack()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AviationError)
                ) {
                    Text("Exit")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showExitDialog = false }) {
                    Text("Continue Exam")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Question ${uiState.currentQuestionIndex + 1} of ${uiState.questions.size}",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (uiState.currentQuestionIndex + 1).toFloat() / uiState.questions.size },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = AviationGold,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        )
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { if (!uiState.isSubmitting) showExitDialog = true },
                        enabled = !uiState.isSubmitting
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                actions = {
                    ExamTimer(
                        timeRemainingSeconds = uiState.timeRemainingSeconds,
                        isWarning = uiState.timeRemainingSeconds <= 300
                    )
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            if (uiState.questions.isNotEmpty()) {
                val currentQuestion = uiState.questions[uiState.currentQuestionIndex]

                QuestionCard(
                    question = currentQuestion,
                    selectedAnswer = uiState.answers[currentQuestion.id],
                    onAnswerSelected = onAnswerSelected,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                )

                // Feedback button
                val context = LocalContext.current
                var showFeedbackDialog by remember { mutableStateOf(false) }
                var feedbackSubmitted by remember(currentQuestion.id) { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(
                        onClick = { showFeedbackDialog = true },
                        enabled = !feedbackSubmitted
                    ) {
                        Icon(
                            imageVector = if (feedbackSubmitted) Icons.Default.CheckCircle else Icons.Default.Flag,
                            contentDescription = "Report issue",
                            modifier = Modifier.size(16.dp),
                            tint = if (feedbackSubmitted) HUDGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (feedbackSubmitted) "Reported" else "Report Issue",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (feedbackSubmitted) HUDGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (showFeedbackDialog) {
                    QuestionFeedbackDialog(
                        onDismiss = { showFeedbackDialog = false },
                        onSubmit = { feedbackType, comment ->
                            val app = context.applicationContext as CivilAviationApp
                            val email = FirebaseAuth.getInstance().currentUser?.email ?: ""
                            CoroutineScope(Dispatchers.IO).launch {
                                app.database.questionFeedbackDao().insert(
                                    QuestionFeedbackEntity(
                                        questionId = currentQuestion.id,
                                        sectionId = currentQuestion.sectionId,
                                        questionText = currentQuestion.questionText,
                                        feedbackType = feedbackType,
                                        comment = comment,
                                        userEmail = email
                                    )
                                )
                            }
                            feedbackSubmitted = true
                            showFeedbackDialog = false
                        }
                    )
                }

                NavigationButtons(
                    canGoBack = uiState.currentQuestionIndex > 0,
                    canGoNext = uiState.currentQuestionIndex < uiState.questions.size - 1,
                    isLastQuestion = uiState.currentQuestionIndex == uiState.questions.size - 1,
                    hasAnsweredCurrent = uiState.answers.containsKey(currentQuestion.id),
                    answeredCount = uiState.answers.size,
                    totalCount = uiState.questions.size,
                    isSubmitting = uiState.isSubmitting,
                    onPrevious = onPreviousQuestion,
                    onNext = onNextQuestion,
                    onSubmit = onSubmitExam,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ExamTimer(
    timeRemainingSeconds: Int,
    isWarning: Boolean
) {
    val minutes = timeRemainingSeconds / 60
    val seconds = timeRemainingSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)
    val timerColor = if (isWarning) AviationError else AviationGold

    Surface(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isWarning) AviationErrorSubtle else AviationGoldSubtle,
        border = androidx.compose.foundation.BorderStroke(1.dp, timerColor.copy(alpha = 0.4f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = "Time remaining",
                modifier = Modifier.size(16.dp),
                tint = timerColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = timerColor
            )
        }
    }
}

@Composable
private fun QuestionCard(
    question: com.suyash.mockcivilaviationexam.domain.model.Question,
    selectedAnswer: String?,
    onAnswerSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            // Question text with left accent
            Row {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .heightIn(min = 24.dp)
                        .background(AviationGold, RoundedCornerShape(2.dp))
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = question.questionText,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.2
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            val options = listOf(
                "A" to question.optionA,
                "B" to question.optionB,
                "C" to question.optionC,
                question.optionD?.let { "D" to it }
            ).filterNotNull()

            options.forEachIndexed { index, (key, value) ->
                AnswerOption(
                    optionKey = key,
                    optionText = value,
                    isSelected = selectedAnswer == key,
                    onSelected = { onAnswerSelected(key) },
                    modifier = Modifier.fillMaxWidth()
                )
                if (index < options.size - 1) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
private fun AnswerOption(
    optionKey: String,
    optionText: String,
    isSelected: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) AvionicsBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
    val bgColor = if (isSelected) AvionicsBlueSubtle else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)

    Surface(
        modifier = modifier
            .selectable(
                selected = isSelected,
                onClick = onSelected,
                role = Role.RadioButton
            ),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = androidx.compose.foundation.BorderStroke(
            if (isSelected) 1.5.dp else 1.dp,
            borderColor
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Option letter badge
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(
                        color = if (isSelected) AvionicsBlue else MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = optionKey,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = optionText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun NavigationButtons(
    canGoBack: Boolean,
    canGoNext: Boolean,
    isLastQuestion: Boolean,
    hasAnsweredCurrent: Boolean,
    answeredCount: Int,
    totalCount: Int,
    isSubmitting: Boolean,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onSubmit: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Progress indicator
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Progress",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "$answeredCount / $totalCount",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = AviationGold
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { answeredCount.toFloat() / totalCount },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = AviationGold,
                    trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Navigation buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPrevious,
                enabled = canGoBack,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (canGoBack) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                ),
                modifier = Modifier
                    .height(46.dp)
                    .weight(1f)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Previous", style = MaterialTheme.typography.labelLarge)
            }

            if (isLastQuestion) {
                val allAnswered = answeredCount == totalCount
                Button(
                    onClick = onSubmit,
                    enabled = !isSubmitting,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (allAnswered) HUDGreen else AviationGold,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.outline,
                        disabledContentColor = Color.White.copy(alpha = 0.7f)
                    ),
                    modifier = Modifier
                        .height(46.dp)
                        .weight(1f)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Submitting...", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                    } else {
                        Icon(
                            imageVector = if (allAnswered) Icons.Default.CheckCircle else Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (allAnswered) "Complete" else "Submit",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                        )
                    }
                }
            } else {
                Button(
                    onClick = onNext,
                    enabled = canGoNext,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AvionicsBlue,
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .height(46.dp)
                        .weight(1f)
                ) {
                    Text("Next", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                    Spacer(modifier = Modifier.width(6.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        if (isLastQuestion && answeredCount < totalCount) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "You have ${totalCount - answeredCount} unanswered questions.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun QuestionFeedbackDialog(
    onDismiss: () -> Unit,
    onSubmit: (feedbackType: String, comment: String) -> Unit
) {
    val feedbackOptions = listOf(
        "wrong_answer" to "Wrong/Incorrect Answer",
        "unclear" to "Unclear Question",
        "typo" to "Typo or Grammar Error",
        "outdated" to "Outdated Information",
        "other" to "Other Issue"
    )

    var selectedType by remember { mutableStateOf("") }
    var comment by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Default.Flag,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = AviationGold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Report Issue", fontWeight = FontWeight.Bold)
            }
        },
        text = {
            Column {
                Text(
                    "What's wrong with this question?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                feedbackOptions.forEach { (type, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .selectable(
                                selected = selectedType == type,
                                onClick = { selectedType = type },
                                role = Role.RadioButton
                            )
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedType == type,
                            onClick = { selectedType = type },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = AviationGold
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = comment,
                    onValueChange = { comment = it },
                    label = { Text("Additional details (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    maxLines = 3,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AviationGold,
                        focusedLabelColor = AviationGold
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSubmit(selectedType, comment) },
                enabled = selectedType.isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = AviationGold)
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
