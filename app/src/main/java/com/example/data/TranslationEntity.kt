package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translations")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val direction: String,
    val sourceText: String,
    val rawBaseline: String,
    val editedTranslation: String,
    val structuralBreakdown: String,
    val culturalNotes: String,
    val contributorEmail: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)
