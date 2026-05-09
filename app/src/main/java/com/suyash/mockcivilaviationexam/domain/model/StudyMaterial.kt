package com.suyash.mockcivilaviationexam.domain.model

data class StudyMaterial(
    val id: String,
    val sectionId: String,
    val title: String,
    val content: String,
    val externalReferences: List<String> = emptyList()
)

