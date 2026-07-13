package com.ekainano.translation.data

import kotlinx.coroutines.flow.Flow

class TranslationRepository(private val translationDao: TranslationDao) {

    val allTranslations: Flow<List<TranslationEntity>> = translationDao.getAllTranslations()
    
    val unsyncedCount: Flow<Int> = translationDao.getUnsyncedCount()

    suspend fun insert(translation: TranslationEntity) {
        translationDao.insertTranslation(translation)
    }

    suspend fun delete(id: Int) {
        translationDao.deleteTranslationById(id)
    }

    suspend fun clear() {
        translationDao.clearLedger()
    }

    suspend fun markAllAsSynced() {
        translationDao.markAllAsSynced()
    }
}
