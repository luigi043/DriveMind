package com.drivemind.data.repository

import com.drivemind.data.local.dao.VoiceNoteDao
import com.drivemind.data.local.entity.VoiceNoteEntity
import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote
import com.drivemind.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoiceNoteRepositoryImpl @Inject constructor(
    private val dao: VoiceNoteDao
) : VoiceNoteRepository {

    override fun getAllNotes(): Flow<List<VoiceNote>> =
        dao.getAllNotes().map { entities -> entities.map { it.toDomain() } }

    override fun getNotesByType(type: NoteType): Flow<List<VoiceNote>> =
        dao.getNotesByType(type.name).map { entities -> entities.map { it.toDomain() } }

    override fun searchNotes(query: String): Flow<List<VoiceNote>> =
        dao.searchNotes(query).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getNoteById(id: Long): VoiceNote? =
        dao.getNoteById(id)?.toDomain()

    override suspend fun saveNote(note: VoiceNote): Long =
        dao.insertNote(VoiceNoteEntity.fromDomain(note))

    override suspend fun updateNote(note: VoiceNote) =
        dao.updateNote(VoiceNoteEntity.fromDomain(note))

    override suspend fun deleteNote(note: VoiceNote) =
        dao.deleteNote(VoiceNoteEntity.fromDomain(note))

    override suspend fun deleteNoteById(id: Long) =
        dao.deleteNoteById(id)

    override suspend fun markTaskCompleted(id: Long, completed: Boolean) =
        dao.markCompleted(id, completed)

    override fun getNotesCount(): Flow<Int> = dao.getCount()
}
