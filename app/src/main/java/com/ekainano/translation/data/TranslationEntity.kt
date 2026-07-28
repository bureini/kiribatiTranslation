package com.ekainano.translation.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Local offline ledger database entity supporting custom Kiribati-English translation alignments.
 */
@Entity(tableName = "translations")
data class TranslationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    @ColumnInfo(name = "direction")
    val direction: String,

    @ColumnInfo(name = "source_text")
    val sourceText: String,

    @ColumnInfo(name = "raw_baseline")
    val rawBaseline: String,

    @ColumnInfo(name = "edited_translation")
    val editedTranslation: String,

    @ColumnInfo(name = "structural_breakdown")
    val structuralBreakdown: String,

    @ColumnInfo(name = "cultural_notes")
    val culturalNotes: String,

    @ColumnInfo(name = "contributor_email")
    val contributorEmail: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "is_synced")
    val isSynced: Boolean = false
)
