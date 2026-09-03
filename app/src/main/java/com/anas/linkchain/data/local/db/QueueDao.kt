package com.anas.linkchain.data.local.db

import androidx.room.*
import com.anas.linkchain.domain.model.ItemStatus
import com.anas.linkchain.domain.model.QueueItem
import kotlinx.coroutines.flow.Flow

@Dao
interface QueueDao {
    @Query("SELECT * FROM queue_items ORDER BY position ASC, createdAt ASC")
    fun getAllItems(): Flow<List<QueueItem>>

    @Query("SELECT * FROM queue_items WHERE status = 'PENDING' ORDER BY position ASC, createdAt ASC LIMIT 1")
    suspend fun getNextPending(): QueueItem?

    @Query("SELECT * FROM queue_items WHERE status = 'FAILED'")
    suspend fun getAllFailed(): List<QueueItem>

    @Query("SELECT * FROM queue_items WHERE url = :url LIMIT 1")
    suspend fun findByUrl(url: String): QueueItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: QueueItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<QueueItem>)

    @Update
    suspend fun update(item: QueueItem)

    @Query("UPDATE queue_items SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: ItemStatus)

    @Delete
    suspend fun delete(item: QueueItem)

    @Query("DELETE FROM queue_items WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM queue_items")
    suspend fun clearQueue()
}