package com.suyash.mockcivilaviationexam.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "study_materials")
data class StudyMaterialEntity(
    @PrimaryKey
    val id: String,
    val sectionId: String,
    val title: String,
    val content: String,
    val externalReferencesJson: String = "[]" // JSON array of references
) {
    fun toDomain(): com.suyash.mockcivilaviationexam.domain.model.StudyMaterial {
        val gson = Gson()
        val referencesType = object : TypeToken<List<String>>() {}.type
        
        return com.suyash.mockcivilaviationexam.domain.model.StudyMaterial(
            id = id,
            sectionId = sectionId,
            title = title,
            content = content,
            externalReferences = gson.fromJson(externalReferencesJson, referencesType)
        )
    }
    
    companion object {
        fun fromDomain(material: com.suyash.mockcivilaviationexam.domain.model.StudyMaterial): StudyMaterialEntity {
            val gson = Gson()
            return StudyMaterialEntity(
                id = material.id,
                sectionId = material.sectionId,
                title = material.title,
                content = material.content,
                externalReferencesJson = gson.toJson(material.externalReferences)
            )
        }
    }
}

