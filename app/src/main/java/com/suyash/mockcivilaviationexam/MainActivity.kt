package com.suyash.mockcivilaviationexam

import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Box
import com.suyash.mockcivilaviationexam.domain.logbook.LogbookUser
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
import androidx.work.*
import com.suyash.mockcivilaviationexam.data.sync.FeedbackSyncWorker
import com.suyash.mockcivilaviationexam.util.DatabaseInitializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

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

        // Schedule weekly feedback sync
        val feedbackSync = PeriodicWorkRequestBuilder<FeedbackSyncWorker>(
            7, TimeUnit.DAYS
        ).setConstraints(
            Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()
        ).build()

        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
            FeedbackSyncWorker.WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            feedbackSync
        )

        // Initialize database and preload bundled questions
        lifecycleScope.launch(Dispatchers.IO) {
            DatabaseInitializer.initializeDatabase(
                this@MainActivity,
                app.sectionRepository,
                app.studyMaterialRepository
            )
            // Load all 7,948 bundled MCQ questions into Room DB (from TSV asset)
            // This is idempotent — skips if already loaded
            app.examRepository.preloadQuestionsForUser()

            // Entries made before per-account scoping were stored under a single
            // literal id. Hand them to the signed-in pilot so an upgrade does not
            // appear to empty their logbook.
            val userId = LogbookUser.id()
            if (userId != LogbookUser.LEGACY_USER_ID) {
                app.flightEntryRepository.claimLegacyEntries(userId)
            }
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
                    val authResolved by authViewModel.authResolved.collectAsStateWithLifecycle()

                    if (!authResolved) {
                        // NavHost fixes its start destination when the graph is
                        // built, so the graph must not be built until we know
                        // whether a session was restored.
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    } else {
                        val startDestination = remember {
                            if (authState != null) Screen.Home.route else Screen.Login.route
                        }

                        AppNavGraph(
                            startDestination = startDestination
                        )
                    }
                }
            }
        }
    }
}