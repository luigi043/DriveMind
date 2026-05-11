package com.drivemind.data.local.dao

import androidx.room.*
import com.drivemind.data.local.entity.VoiceNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface VoiceNoteDao {

    @Query("SELECT * FROM voice_notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<VoiceNoteEntity>>

    @Query("SELECT * FROM voice_notes WHERE type = :type ORDER BY timestamp DESC")
    fun getNotesByType(type: String): Flow<List<VoiceNoteEntity>>

    @Query("""
        SELECT * FROM voice_notes 
        WHERE raw_content LIKE '%' || :query || '%' 
        OR processed_content LIKE '%' || :query || '%'
        ORDER BY timestamp DESC
    """)
    fun searchNotes(query: String): Flow<List<VoiceNoteEntity>>

    @Query("SELECT * FROM voice_notes WHERE id = :id LIMIT 1")
    suspend fun getNoteById(id: Long): VoiceNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: VoiceNoteEntity): Long

    @Update
    suspend fun updateNote(note: VoiceNoteEntity)

    @Delete
    suspend fun deleteNote(note: VoiceNoteEntity)

    @Query("DELETE FROM voice_notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    @Query("UPDATE voice_notes SET is_completed = :completed WHERE id = :id")
    suspend fun markCompleted(id: Long, completed: Boolean)

    @Query("SELECT COUNT(*) FROM voice_notes")
    fun getCount(): Flow<Int>

    @Query("DELETE FROM voice_notes")
    suspend fun deleteAll()

    @Query("""
        SELECT * FROM voice_notes 
        WHERE reminder_time IS NOT NULL AND reminder_time > :currentTime
        ORDER BY reminder_time ASC
    """)
    fun getUpcomingReminders(currentTime: Long): Flow<List<VoiceNoteEntity>>
}
