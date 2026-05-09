package com.suyash.mockcivilaviationexam.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sections")
data class SectionEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String,
    val icon: String = ""
) {
    fun toDomain(): com.suyash.mockcivilaviationexam.domain.model.Section {
        return com.suyash.mockcivilaviationexam.domain.model.Section(
            id = id,
            name = name,
            description = description,
            icon = icon
        )
    }
    
    companion object {
        fun fromDomain(section: com.suyash.mockcivilaviationexam.domain.model.Section): SectionEntity {
            return SectionEntity(
                id = section.id,
                name = section.name,
                description = section.description,
                icon = section.icon
            )
        }
    }
}

