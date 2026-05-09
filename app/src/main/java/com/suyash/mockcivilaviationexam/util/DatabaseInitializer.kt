package com.suyash.mockcivilaviationexam.util

import android.content.Context
import com.suyash.mockcivilaviationexam.data.local.repository.SectionRepository
import com.suyash.mockcivilaviationexam.data.local.repository.StudyMaterialRepository
import com.suyash.mockcivilaviationexam.domain.model.Section
import com.suyash.mockcivilaviationexam.domain.model.StudyMaterial
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object DatabaseInitializer {
    suspend fun initializeDatabase(
        context: Context,
        sectionRepository: SectionRepository,
        studyMaterialRepository: StudyMaterialRepository
    ) {
        // Initialize sections (keeping for backward compatibility)
        val sections = getDefaultSections()
        sectionRepository.insertSections(sections)
        
        // Initialize study materials
        sections.forEach { section ->
            val studyMaterials = getSampleStudyMaterials(section.id)
            studyMaterialRepository.insertStudyMaterials(studyMaterials)
        }
        
        // Note: Question loading removed as this is now a pilot logbook app
        // The pilot logbook system doesn't use questions/exams
    }
    
    private fun getDefaultSections(): List<Section> {
        return listOf(
            Section(
                id = "air_law",
                name = "Air Law",
                description = "Regulations, rules, and legal aspects of aviation"
            ),
            Section(
                id = "meteorology",
                name = "Meteorology",
                description = "Weather patterns, atmospheric conditions, and forecasting"
            ),
            Section(
                id = "principles_of_flight",
                name = "Principles of Flight",
                description = "Aerodynamics, flight mechanics, and aircraft performance"
            ),
            Section(
                id = "aircraft_general",
                name = "Aircraft General",
                description = "Aircraft systems, structures, and general knowledge"
            ),
            Section(
                id = "human_performance",
                name = "Human Performance and Limitations",
                description = "Human factors, physiology, and psychological aspects"
            ),
            Section(
                id = "operational_procedures",
                name = "Operational Procedures",
                description = "Standard operating procedures and best practices"
            ),
            Section(
                id = "navigation",
                name = "Navigation",
                description = "Navigation systems, charts, and flight planning"
            ),
            Section(
                id = "communication",
                name = "Communication",
                description = "Radio procedures, phraseology, and communication protocols"
            )
        )
    }
    
    
    private fun getSampleStudyMaterials(sectionId: String): List<StudyMaterial> {
        return listOf(
            StudyMaterial(
                id = "${sectionId}_study_001",
                sectionId = sectionId,
                title = "Introduction to ${getSectionName(sectionId)}",
                content = "This section covers the fundamental concepts of ${getSectionName(sectionId)}. Study the key principles and regulations.",
                externalReferences = getExternalReferences(sectionId)
            )
        )
    }
    
    private fun getSectionName(sectionId: String): String {
        return when (sectionId) {
            "air_law" -> "Air Law"
            "meteorology" -> "Meteorology"
            "principles_of_flight" -> "Principles of Flight"
            "aircraft_general" -> "Aircraft General"
            "human_performance" -> "Human Performance and Limitations"
            "operational_procedures" -> "Operational Procedures"
            "navigation" -> "Navigation"
            "communication" -> "Communication"
            else -> sectionId
        }
    }
    
    private fun getExternalReferences(sectionId: String): List<String> {
        return when (sectionId) {
            "air_law" -> listOf("ICAO Annex 2", "EASA Part-FCL", "CASA Regulations")
            "meteorology" -> listOf("Aviation Weather Handbook", "Manual of Aviation Meteorology")
            "principles_of_flight" -> listOf("Mechanics of Flight by A.C. Kermode", "Flight Theory for Pilots")
            "aircraft_general" -> listOf("Aircraft Systems Manual", "Aircraft General Knowledge")
            "human_performance" -> listOf("Human Being Pilot - Human Factors for Aviation Professionals")
            "operational_procedures" -> listOf("ICAO Annex 6", "Standard Operating Procedures")
            "navigation" -> listOf("Navigation Aids Manual", "CASA Operational Notes")
            "communication" -> listOf("ICAO Phraseology Manual", "Radio Communication Procedures")
            else -> emptyList()
        }
    }
}

