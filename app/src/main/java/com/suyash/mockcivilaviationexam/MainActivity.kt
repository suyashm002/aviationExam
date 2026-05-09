package com.suyash.mockcivilaviationexam

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth
import com.suyash.mockcivilaviationexam.ui.navigation.AppNavGraph
import com.suyash.mockcivilaviationexam.ui.navigation.Screen
import com.suyash.mockcivilaviationexam.ui.theme.MockcivilAviationExamTheme
import com.suyash.mockcivilaviationexam.ui.viewmodel.AuthViewModel
import androidx.lifecycle.lifecycleScope
import com.suyash.mockcivilaviationexam.util.DatabaseInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        // Initialize Firebase
        FirebaseAuth.getInstance()
        
        val app = applicationContext as CivilAviationApp
        
        // Initialize billing only when subscription feature is enabled
        if (app.featureFlags.subscriptionEnabled) {
            app.billingManager.initialize()
        }

        // Initialize database and preload bundled questions
        lifecycleScope.launch(Dispatchers.IO) {
            DatabaseInitializer.initializeDatabase(
                this@MainActivity,
                app.sectionRepository,
                app.studyMaterialRepository
            )
            // Load all 9,076 bundled MCQ questions into Room DB (from TSV asset)
            // This is idempotent — skips if already loaded
            app.examRepository.preloadQuestionsForUser()
        }
        
        setContent {
            MockcivilAviationExamTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = viewModel {
                        AuthViewModel(app.authService)
                    }
                    val authState by authViewModel.authState.collectAsStateWithLifecycle()
                    
                    val startDestination = if (authState != null) {
                        Screen.Home.route
                    } else {
                        Screen.Login.route
                    }
                    
                    AppNavGraph(
                        startDestination = startDestination
                    )
                }
            }
        }
    }
}