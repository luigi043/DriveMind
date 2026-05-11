package com.drivemind.domain.repository

import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface defined at domain layer.
 * Implementation lives in data layer.
 */
interface VoiceNoteRepository {

    fun getAllNotes(): Flow<List<VoiceNote>>

    fun getNotesByType(type: NoteType): Flow<List<VoiceNote>>

    fun searchNotes(query: String): Flow<List<VoiceNote>>

    suspend fun getNoteById(id: Long): VoiceNote?

    suspend fun saveNote(note: VoiceNote): Long

    suspend fun updateNote(note: VoiceNote)

    suspend fun deleteNote(note: VoiceNote)

    suspend fun deleteNoteById(id: Long)

    suspend fun markTaskCompleted(id: Long, completed: Boolean)

    fun getNotesCount(): Flow<Int>
}
