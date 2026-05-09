package com.suyash.mockcivilaviationexam.ui.screens.register

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.suyash.mockcivilaviationexam.CivilAviationApp
import com.suyash.mockcivilaviationexam.ui.theme.*
import com.suyash.mockcivilaviationexam.ui.viewmodel.AuthViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateBack: () -> Unit,
    onRegisterSuccess: () -> Unit,
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
    var confirmPassword by remember { mutableStateOf("") }
    var showPassword by remember { mutableStateOf(false) }
    var showConfirmPassword by remember { mutableStateOf(false) }

    LaunchedEffect(authState) {
        if (authState != null) {
            try { app.examRepository.preloadQuestionsForUser() }
            catch (e: Exception) { android.util.Log.e("RegisterScreen", "Failed to preload: ${e.message}") }
            onRegisterSuccess()
        }
    }

    // Blue gradient background (matches login)
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
        // Back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(8.dp),
            horizontalArrangement = Arrangement.Start
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .statusBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(Color.White.copy(alpha = 0.15f), RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp),
                        tint = Color.White
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Create Account",
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )

                Text(
                    text = "Start your aviation journey today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // White form card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp)
                ) {
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
                                    contentDescription = if (showPassword) "Hide" else "Show",
                                    tint = TextDarkTertiary
                                )
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val passwordMismatch = confirmPassword.isNotEmpty() && password != confirmPassword

                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text("Confirm Password") },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Lock,
                                contentDescription = null,
                                tint = if (passwordMismatch) AviationError else AvionicsBlueDark
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = if (passwordMismatch) AviationError else AvionicsBlueDark,
                            unfocusedBorderColor = if (passwordMismatch) AviationError.copy(alpha = 0.5f) else SkyBorder,
                            focusedLabelColor = if (passwordMismatch) AviationError else AvionicsBlueDark,
                            unfocusedLabelColor = TextDarkTertiary,
                            focusedTextColor = TextDark,
                            unfocusedTextColor = TextDark,
                            cursorColor = AvionicsBlueDark,
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFF9FAFB),
                            errorBorderColor = AviationError,
                            errorLabelColor = AviationError
                        ),
                        visualTransformation = if (showConfirmPassword) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { showConfirmPassword = !showConfirmPassword }) {
                                Icon(
                                    imageVector = if (showConfirmPassword) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                    contentDescription = if (showConfirmPassword) "Hide" else "Show",
                                    tint = TextDarkTertiary
                                )
                            }
                        },
                        isError = passwordMismatch
                    )

                    if (passwordMismatch) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Passwords do not match", color = AviationError, style = MaterialTheme.typography.bodySmall)
                    }

                    uiState.error?.let { error ->
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(shape = RoundedCornerShape(8.dp), color = ErrorContainerLight) {
                            Text(error, color = AviationErrorDark, style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(12.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = {
                            if (password == confirmPassword) viewModel.signUp(email.trim(), password)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !uiState.isLoading &&
                                email.isNotBlank() &&
                                password.isNotBlank() &&
                                confirmPassword.isNotBlank() &&
                                password == confirmPassword &&
                                password.length >= 6,
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = AvionicsBlueDark,
                            contentColor = Color.White,
                            disabledContainerColor = AvionicsBlueDark.copy(alpha = 0.4f),
                            disabledContentColor = Color.White.copy(alpha = 0.6f)
                        )
                    ) {
                        if (uiState.isLoading) {
                            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Creating Account...", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold))
                        } else {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Create Account", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Password must be at least 6 characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextDarkTertiary,
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SkyBorder)
                        Text("OR", modifier = Modifier.padding(horizontal = 16.dp), style = MaterialTheme.typography.bodySmall, color = TextDarkTertiary)
                        HorizontalDivider(modifier = Modifier.weight(1f), color = SkyBorder)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedButton(
                        onClick = { viewModel.signInWithGoogle(context) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        enabled = !uiState.isLoading,
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SkyBorder),
                        colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFFF9FAFB))
                    ) {
                        Text("G", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = GoogleBlue)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Google", style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold), color = TextDark)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    TextButton(onClick = onNavigateBack, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                        Text("Already have an account? Sign In", color = AvionicsBlueDark, style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium))
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
