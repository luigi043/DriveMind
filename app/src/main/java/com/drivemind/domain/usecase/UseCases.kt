package com.drivemind.domain.usecase

import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote
import com.drivemind.domain.repository.VoiceNoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Save a new voice note or update an existing one.
 */
class SaveNoteUseCase @Inject constructor(
    private val repository: VoiceNoteRepository
) {
    suspend operator fun invoke(note: VoiceNote): Result<Long> = runCatching {
        require(note.rawContent.isNotBlank()) { "Note content cannot be empty" }
        repository.saveNote(note)
    }
}

/**
 * Get all notes as a reactive stream.
 */
class GetAllNotesUseCase @Inject constructor(
    private val repository: VoiceNoteRepository
) {
    operator fun invoke(): Flow<List<VoiceNote>> = repository.getAllNotes()
}

/**
 * Get notes filtered by type.
 */
class GetNotesByTypeUseCase @Inject constructor(
    private val repository: VoiceNoteRepository
) {
    operator fun invoke(type: NoteType): Flow<List<VoiceNote>> =
        repository.getNotesByType(type)
}

/**
 * Delete a voice note permanently.
 */
class DeleteNoteUseCase @Inject constructor(
    private val repository: VoiceNoteRepository
) {
    suspend operator fun invoke(note: VoiceNote): Result<Unit> = runCatching {
        repository.deleteNote(note)
    }

    suspend operator fun invoke(id: Long): Result<Unit> = runCatching {
        repository.deleteNoteById(id)
    }
}

/**
 * Search notes by content.
 */
class SearchNotesUseCase @Inject constructor(
    private val repository: VoiceNoteRepository
) {
    operator fun invoke(query: String): Flow<List<VoiceNote>> =
        repository.searchNotes(query.trim())
}

/**
 * Mark a task as completed/incomplete.
 */
class ToggleTaskCompletionUseCase @Inject constructor(
    private val repository: VoiceNoteRepository
) {
    suspend operator fun invoke(id: Long, completed: Boolean): Result<Unit> = runCatching {
        repository.markTaskCompleted(id, completed)
    }
}

/**
 * Get note by ID.
 */
class GetNoteByIdUseCase @Inject constructor(
    private val repository: VoiceNoteRepository
) {
    suspend operator fun invoke(id: Long): VoiceNote? = repository.getNoteById(id)
}

/**
 * Update an existing note.
 */
class UpdateNoteUseCase @Inject constructor(
    private val repository: VoiceNoteRepository
) {
    suspend operator fun invoke(note: VoiceNote): Result<Unit> = runCatching {
        require(note.id > 0) { "Cannot update a note without a valid ID" }
        repository.updateNote(note)
    }
}
