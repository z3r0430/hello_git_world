package com.example.notilog.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface NotificationDao {

    /** Returns -1 when the row was a duplicate and therefore ignored. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entry: NotificationEntry): Long

    @Query("SELECT * FROM notifications ORDER BY timestampMillis DESC")
    fun observeAll(): Flow<List<NotificationEntry>>

    @Query("SELECT DISTINCT conversation FROM notifications ORDER BY conversation COLLATE NOCASE ASC")
    fun observeConversations(): Flow<List<String>>

    @Query("DELETE FROM notifications")
    suspend fun clear()

    @Query("DELETE FROM notifications WHERE id = :id")
    suspend fun delete(id: Long)
}
