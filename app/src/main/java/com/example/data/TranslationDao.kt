package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translations ORDER BY timestamp DESC")
    fun getAllTranslations(): Flow<List<TranslationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: TranslationEntity)

    @Query("DELETE FROM translations WHERE id = :id")
    suspend fun deleteTranslation(id: Int)

    @Query("SELECT * FROM translations WHERE direction = :direction AND sourceText = :sourceText LIMIT 1")
    suspend fun findTranslation(direction: String, sourceText: String): TranslationEntity?

    @Query("DELETE FROM translations")
    suspend fun clearAll()
}
