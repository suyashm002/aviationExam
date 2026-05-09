package com.suyash.mockcivilaviationexam.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.suyash.mockcivilaviationexam.domain.model.ExamSection

@Entity(tableName = "exam_sections")
data class ExamSectionEntity(
    @PrimaryKey
    val id: String,
    val sectionId: String,
    val name: String,
    val description: String?,
    val icon: String?,
    val cachedAt: Long = System.currentTimeMillis()
) {
    fun toDomain(): ExamSection {
        return ExamSection(
            id = id,
            sectionId = sectionId,
            name = name,
            description = description,
            icon = icon
        )
    }

    companion object {
        fun fromDomain(section: ExamSection): ExamSectionEntity {
            return ExamSectionEntity(
                id = section.id,
                sectionId = section.sectionId,
                name = section.name,
                description = section.description,
                icon = section.icon
            )
        }
    }
}
