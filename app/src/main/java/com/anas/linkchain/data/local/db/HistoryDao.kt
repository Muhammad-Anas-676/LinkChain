package com.anas.linkchain.data.local.db

import androidx.room.*
import com.anas.linkchain.domain.model.HistoryItem
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {
    @Query("SELECT * FROM history_items ORDER BY completedAt DESC")
    fun getAllHistory(): Flow<List<HistoryItem>>

    @Query("SELECT * FROM history_items WHERE url = :url LIMIT 1")
    suspend fun findByUrl(url: String): HistoryItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: HistoryItem): Long

    @Delete
    suspend fun delete(item: HistoryItem)

    @Query("DELETE FROM history_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM history_items")
    suspend fun clearHistory()
}