package com.example.data

import kotlinx.coroutines.flow.Flow

class TranslationRepository(private val translationDao: TranslationDao) {
    val allTranslations: Flow<List<TranslationEntity>> = translationDao.getAllTranslations()

    suspend fun insert(translation: TranslationEntity) {
        translationDao.insertTranslation(translation)
    }

    suspend fun delete(id: Int) {
        translationDao.deleteTranslation(id)
    }

    suspend fun find(direction: String, sourceText: String): TranslationEntity? {
        return translationDao.findTranslation(direction, sourceText)
    }

    suspend fun clear() {
        translationDao.clearAll()
    }
}
