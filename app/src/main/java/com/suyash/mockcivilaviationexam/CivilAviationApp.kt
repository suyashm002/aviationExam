package com.suyash.mockcivilaviationexam

import android.app.Application
import com.suyash.mockcivilaviationexam.data.billing.BillingManager
import com.suyash.mockcivilaviationexam.data.billing.SubscriptionRepository
import com.suyash.mockcivilaviationexam.data.cache.FeatureFlags
import com.suyash.mockcivilaviationexam.data.cache.QuestionCacheManager
import com.suyash.mockcivilaviationexam.data.local.database.AppDatabase
import com.suyash.mockcivilaviationexam.data.local.repository.*
import com.suyash.mockcivilaviationexam.data.remote.repository.ExamRepository
import com.suyash.mockcivilaviationexam.data.remote.service.AuthService
import com.suyash.mockcivilaviationexam.domain.usecase.FlightOperationsUseCase
import com.suyash.mockcivilaviationexam.domain.usecase.PdfExportUseCase

class CivilAviationApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }

    // Feature flags
    val featureFlags by lazy { FeatureFlags(this) }

    // Question cache manager (Application-scoped)
    val questionCacheManager by lazy {
        QuestionCacheManager(
            context = this,
            examQuestionDao = database.examQuestionDao(),
            cacheMetadataDao = database.cacheMetadataDao(),
            featureFlags = featureFlags
        )
    }

    // Pilot Logbook Repositories
    val flightEntryRepository by lazy {
        FlightEntryRepository(database.flightEntryDao())
    }

    val aircraftRepository by lazy {
        AircraftRepository(database.aircraftDao())
    }

    val instructorRepository by lazy {
        InstructorRepository(database.instructorDao())
    }

    val userProfileRepository by lazy {
        UserProfileRepository(database.userProfileDao())
    }

    // Keep existing repositories for backward compatibility
    val sectionRepository by lazy {
        SectionRepository(database.sectionDao())
    }

    val studyMaterialRepository by lazy {
        StudyMaterialRepository(database.studyMaterialDao())
    }

    // Pilot Logbook Use Cases
    val flightOperationsUseCase by lazy {
        FlightOperationsUseCase(flightEntryRepository, userProfileRepository)
    }

    val pdfExportUseCase by lazy {
        PdfExportUseCase(flightEntryRepository, userProfileRepository)
    }

    // Billing & Subscription
    val billingManager by lazy { BillingManager(this) }
    val subscriptionRepository by lazy { SubscriptionRepository(this) }

    // Authentication Service
    val authService by lazy {
        AuthService()
    }

    // Exam Repository
    val examRepository by lazy {
        ExamRepository(
            context = this,
            cacheManager = questionCacheManager,
            examSectionDao = database.examSectionDao(),
            cacheMetadataDao = database.cacheMetadataDao(),
            featureFlags = featureFlags,
            examSessionDao = database.examSessionDao()
        )
    }
}
