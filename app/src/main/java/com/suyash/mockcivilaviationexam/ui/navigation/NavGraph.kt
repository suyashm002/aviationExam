package com.suyash.mockcivilaviationexam.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.suyash.mockcivilaviationexam.CivilAviationApp
import com.suyash.mockcivilaviationexam.ui.screens.exam.ExamScreen
import com.suyash.mockcivilaviationexam.ui.screens.home.HomeScreen
import com.suyash.mockcivilaviationexam.ui.screens.login.LoginScreen
import com.suyash.mockcivilaviationexam.ui.screens.profile.ProfileScreen
import com.suyash.mockcivilaviationexam.ui.screens.register.RegisterScreen
import com.suyash.mockcivilaviationexam.ui.screens.study.StudyScreen
import com.suyash.mockcivilaviationexam.ui.screens.logbook.LogbookScreen
import com.suyash.mockcivilaviationexam.ui.screens.flight.FlightEntryScreen
import com.suyash.mockcivilaviationexam.ui.screens.export.ExportScreen
import com.suyash.mockcivilaviationexam.ui.screens.history.HistoryScreen
import com.suyash.mockcivilaviationexam.ui.screens.results.ResultsScreen
import com.suyash.mockcivilaviationexam.ui.screens.subscription.SubscriptionScreen
import com.suyash.mockcivilaviationexam.ui.viewmodel.FlightEntryViewModel
import com.suyash.mockcivilaviationexam.ui.viewmodel.LogbookViewModel
import com.suyash.mockcivilaviationexam.ui.viewmodel.SubscriptionViewModel

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = Screen.Home.route
) {
    val app = LocalContext.current.applicationContext as CivilAviationApp
    val subscriptionViewModel: SubscriptionViewModel = viewModel {
        SubscriptionViewModel(app.billingManager, app.subscriptionRepository)
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Home.route) {
            HomeScreen(
                onNavigateToExam = { sectionId, questionCount ->
                    if (!app.featureFlags.subscriptionEnabled) {
                        navController.navigate(Screen.Exam.createRoute(sectionId, questionCount))
                    } else {
                        val canTake = subscriptionViewModel.canTakeFreeExam(sectionId)
                        if (canTake) {
                            subscriptionViewModel.recordExamTaken(sectionId)
                            navController.navigate(Screen.Exam.createRoute(sectionId, questionCount))
                        } else {
                            navController.navigate(Screen.Subscription.createRoute(sectionId))
                        }
                    }
                },
                onNavigateToProfile = {
                    navController.navigate(Screen.Profile.route)
                },
                onNavigateToHistory = {
                    navController.navigate("history")
                },
                onNavigateToLogbook = {
                    navController.navigate(Screen.Logbook.route)
                }
            )
        }
        
        composable(Screen.Exam.route) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getString("sectionId") ?: ""
            val questionCount = backStackEntry.arguments?.getString("questionCount")?.toIntOrNull() ?: 16
            ExamScreen(
                sectionId = sectionId,
                questionCount = questionCount,
                onNavigateBack = { navController.popBackStack() },
                onExamComplete = { examSessionId ->
                    navController.navigate("results/$examSessionId") {
                        popUpTo(Screen.Exam.createRoute(sectionId, questionCount)) { inclusive = true }
                    }
                }
            )
        }
        
        composable("results/{examId}") { backStackEntry ->
            val examId = backStackEntry.arguments?.getString("examId") ?: ""
            ResultsScreen(
                examSessionId = examId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToHome = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable("history") {
            HistoryScreen(
                onNavigateBack = { navController.popBackStack() },
                onViewResults = { examSessionId ->
                    navController.navigate("results/$examSessionId")
                }
            )
        }
        
        composable(Screen.Study.route) {
            StudyScreen(
                onNavigateBack = { navController.popBackStack() }
            )
        }
        
        composable(Screen.Profile.route) {
            ProfileScreen(
                onNavigateBack = { navController.popBackStack() },
                onNavigateToLogin = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Home.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Login.route) {
            LoginScreen(
                onNavigateToRegister = {
                    navController.navigate(Screen.Register.route)
                },
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Register.route) {
            RegisterScreen(
                onNavigateBack = { navController.popBackStack() },
                onRegisterSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Register.route) { inclusive = true }
                    }
                }
            )
        }
        
        composable(Screen.Logbook.route) {
            val app = LocalContext.current.applicationContext as CivilAviationApp
            val viewModel: LogbookViewModel = viewModel {
                LogbookViewModel(app.flightOperationsUseCase)
            }
            
            LogbookScreen(
                onNavigateToFlightEntry = {
                    navController.navigate(Screen.FlightEntry.route)
                },
                onNavigateToFlightDetail = { flightId ->
                    navController.navigate(Screen.FlightEntry.createRoute(flightId))
                },
                onNavigateToExport = {
                    navController.navigate("export")
                },
                viewModel = viewModel
            )
        }
        
        composable(Screen.FlightEntry.route) {
            val app = LocalContext.current.applicationContext as CivilAviationApp
            val viewModel: FlightEntryViewModel = viewModel {
                FlightEntryViewModel(app.flightOperationsUseCase)
            }

            FlightEntryScreen(
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }

        // Editing an existing flight. Without this destination, tapping a
        // logbook entry navigates to a route that is not in the graph and the
        // app crashes.
        composable(Screen.FlightEntry.editRoute) { backStackEntry ->
            val app = LocalContext.current.applicationContext as CivilAviationApp
            val flightId = backStackEntry.arguments?.getString("flightId")?.toLongOrNull()
            val viewModel: FlightEntryViewModel = viewModel {
                FlightEntryViewModel(app.flightOperationsUseCase)
            }

            FlightEntryScreen(
                flightId = flightId,
                onNavigateBack = { navController.popBackStack() },
                viewModel = viewModel
            )
        }
        
        composable(Screen.Aircraft.route) {
            // Placeholder screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aircraft Management\n(Coming Soon)",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        composable(Screen.Instructors.route) {
            // Placeholder screen
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Instructors Management\n(Coming Soon)",
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        composable("export") {
            val app = LocalContext.current.applicationContext as CivilAviationApp
            
            ExportScreen(
                onNavigateBack = { navController.popBackStack() },
                pdfExportUseCase = app.pdfExportUseCase
            )
        }
        
        composable(Screen.StudyMaterial.createRoute("{sectionId}")) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getString("sectionId") ?: ""
            StudyScreen(
                sectionId = sectionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Subscription.route) { backStackEntry ->
            val sectionId = backStackEntry.arguments?.getString("sectionId") ?: ""
            val sectionName = app.examRepository.getSectionName(sectionId)
            SubscriptionScreen(
                sectionName = sectionName,
                onNavigateBack = { navController.popBackStack() },
                onSubscribed = {
                    navController.popBackStack()
                    navController.navigate("exam/$sectionId")
                },
                viewModel = subscriptionViewModel
            )
        }
    }
}

