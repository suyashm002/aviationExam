package com.suyash.mockcivilaviationexam.ui.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object Study : Screen("study")
    object Profile : Screen("profile")
    object Login : Screen("login")
    object Register : Screen("register")
    
    object Exam : Screen("exam/{sectionId}") {
        fun createRoute(sectionId: String) = "exam/$sectionId"
    }
    
    object Results : Screen("results/{examId}") {
        fun createRoute(examId: String) = "results/$examId"
    }
    
    object History : Screen("history")
    
    // Hidden logbook routes (keep for future use)
    object Logbook : Screen("logbook")
    
    object FlightEntry : Screen("flight_entry") {
        fun createRoute(flightId: Long) = "flight_entry/$flightId"
    }
    
    object Aircraft : Screen("aircraft")
    
    object Instructors : Screen("instructors")
    
    object StudyMaterial : Screen("study_material/{sectionId}") {
        fun createRoute(sectionId: String) = "study_material/$sectionId"
    }

    object Subscription : Screen("subscription/{sectionId}") {
        fun createRoute(sectionId: String) = "subscription/$sectionId"
    }
}

