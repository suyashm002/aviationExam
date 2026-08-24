package com.suyash.mockcivilaviationexam.ui.screens.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlightTakeoff
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suyash.mockcivilaviationexam.CivilAviationApp
import com.suyash.mockcivilaviationexam.ui.theme.*
import com.suyash.mockcivilaviationexam.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val app = context.applicationContext as CivilAviationApp
    val viewModel: AuthViewModel = viewModel {
        AuthViewModel(app.authService)
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }

    // Floating plane animation
    val infiniteTransition = rememberInfiniteTransition(label = "plane")
    val planeOffsetY by infiniteTransition.animateFloat(
        initialValue = -6f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2500, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "planeFloat"
    )

    // Entry animations
    val headerAlpha = remember { Animatable(0f) }
    val headerOffsetY = remember { Animatable(30f) }
    val formAlpha = remember { Animatable(0f) }
    val formOffsetY = remember { Animatable(40f) }

    LaunchedEffect(Unit) { headerAlpha.animateTo(1f, tween(600)) }
    LaunchedEffect(Unit) { headerOffsetY.animateTo(0f, tween(600, easing = EaseOutCubic)) }
    LaunchedEffect(Unit) { formAlpha.animateTo(1f, tween(600, delayMillis = 200)) }
    LaunchedEffect(Unit) { formOffsetY.animateTo(0f, tween(600, delayMillis = 200, easing = EaseOutCubic)) }

    // Navigate on success
    LaunchedEffect(authState) {
        if (authState != null) {
            try { app.examRepository.preloadQuestionsForUser() }
            catch (e: Exception) { android.util.Log.e("LoginScreen", "Failed to preload: ${e.message}") }
            onLoginSuccess()
        }
    }

    // Blue gradient background
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        AvionicsBlueDark,
                        Color(0xFF1E3A5F)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(56.dp))

            // Header on gradient
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = headerAlpha.value
                        translationY = headerOffsetY.value
                    },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.15f))
                ) {
                    Icon(
                        imageVector = Icons.Default.FlightTakeoff,
                        contentDescription = null,
                        modifier = Modifier
                            .size(40.dp)
                            .graphicsLayer { translationY = planeOffsetY },
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Aviation Exams",
                    style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "AVIATION MOCK EXAM PREPARATION",
                    style = MaterialTheme.typography.labelMedium.copy(
                        letterSpacing = 2.5.sp,
                        fontWeight = FontWeight.Medium
                    ),
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // White form card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = formAlpha.value
                        translationY = formOffsetY.value
                    },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
                    Text(
                        text = "Welcome Back",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = TextDark
                    )

                    Text(
                        text = "Sign in to continue your preparation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextDarkSecondary,
                        modifier = Modifier.padding(top = 4.dp, bottom = 24.dp)
                    )

                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = {
                            Icon(Icons.Default.Email, contentDescription = null, tint = AvionicsBlueDark)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AvionicsBlueDark,
                            unfocusedBorderColor = SkyBorder,
                            focusedLabelColor = AvionicsBlueDark,
                            unfocusedLabelColor = TextDarkTertiary,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            cursorColor = AvionicsBlueDark,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = {
                            Icon(Icons.Default.Lock, contentDescription = null, tint = AvionicsBlueDark)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = AvionicsBlueDark,
                            unfocusedBorderColor = SkyBorder,
                            focusedLabelColor = AvionicsBlueDark,
                            unfocusedLabelColor = TextDarkTertiary,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            cursorColor = AvionicsBlueDark,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF9FAFB)
                        ),
                        visualTransformation = if (showPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showPassword = !showPassword }) {
                                Icon(
                                    imageVector = if (showPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showPassword) "Hide password" else "Show password",
                                    tint = TextDarkTertiary
                                )
                            }
                        }
                    )

                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                        TextButton(onClick = {
                            if (email.isNotBlank()) viewModel.resetPassword(email.trim())
                        }) {
                            Text(
                                "Forgot Password?",
                                color = AvionicsBlueDark,
                                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }

                    uiState.error?.let { error ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = ErrorContainerLight,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = error,
                                color = AviationErrorDark,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    if (uiState.passwordResetSent) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = SuccessContainerLight,
                            modifier = Modifier.padding(bottom = 12.dp)
                        ) {
                            Text(
                                text = "Password reset email sent! Check your inbox.",
                                color = HUDGreenDark,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.signIn(email.trim(), password) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !uiState.isLoading && email.isNotBlank() && password.isNotBlank(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AvionicsBlueDark,
                            contentColor = Color.White,
                            disabledContainerColor = AvionicsBlueDark.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Signing In...", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                        } else {
                            Icon(Icons.Default.FlightTakeoff, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Sign In",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Divider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SkyBorder)
                        Text(
                            text = "OR",
                            modifier = Modifier.padding(horizontal = 16.dp),
                            style = MaterialTheme.typography.bodySmall,
                            color = TextDarkTertiary
                        )
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SkyBorder)
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Google Sign-In
                    OutlinedButton(
                        onClick = { viewModel.signInWithGoogle(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SkyBorder),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color(0xFFF9FAFB)
                        )
                    ) {
                        Text(
                            text = "G",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = GoogleBlue
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Continue with Google",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            color = TextDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.graphicsLayer { alpha = formAlpha.value },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Don't have an account?",
                    color = Color.White.copy(alpha = 0.7f),
                    style = MaterialTheme.typography.bodyMedium
                )
                TextButton(onClick = onNavigateToRegister) {
                    Text(
                        "Register",
                        color = AviationGoldLight,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
