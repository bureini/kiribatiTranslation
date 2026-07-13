package com.ekainano.translation.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationDao {
    @Query("SELECT * FROM translations ORDER BY timestamp DESC")
    fun getAllTranslations(): Flow<List<TranslationEntity>>

    @Query("SELECT COUNT(*) FROM translations WHERE is_synced = 0")
    fun getUnsyncedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranslation(translation: TranslationEntity)

    @Query("DELETE FROM translations WHERE id = :id")
    suspend fun deleteTranslationById(id: Int)

    @Query("DELETE FROM translations")
    suspend fun clearLedger()

    @Query("UPDATE translations SET is_synced = 1 WHERE is_synced = 0")
    suspend fun markAllAsSynced()
}

@Database(entities = [TranslationEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun translationDao(): TranslationDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "ekainano_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
