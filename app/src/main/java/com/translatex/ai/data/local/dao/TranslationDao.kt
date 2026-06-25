package com.translatex.ai.data.local.dao

import androidx.room.*
import com.translatex.ai.data.local.entity.TranslationEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data-access object for translation history and favourites.
 */
@Dao
interface TranslationDao {

    // ── Insert / Update ────────────────────────────────────────────────────

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(translation: TranslationEntity): Long

    @Update
    suspend fun update(translation: TranslationEntity)

    // ── Queries ───────────────────────────────────────────────────────────

    @Query("SELECT * FROM translation_history ORDER BY timestamp DESC")
    fun getAllHistory(): Flow<List<TranslationEntity>>

    @Query("""
        SELECT * FROM translation_history
        WHERE sourceText LIKE '%' || :query || '%'
           OR translatedText LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchHistory(query: String): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translation_history WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavorites(): Flow<List<TranslationEntity>>

    @Query("SELECT * FROM translation_history WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): TranslationEntity?

    // ── Delete ────────────────────────────────────────────────────────────

    @Delete
    suspend fun delete(translation: TranslationEntity)

    @Query("DELETE FROM translation_history")
    suspend fun clearAll()

    @Query("DELETE FROM translation_history WHERE isFavorite = 0")
    suspend fun clearNonFavorites()

    // ── Favourite toggle ──────────────────────────────────────────────────

    @Query("UPDATE translation_history SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)
}
