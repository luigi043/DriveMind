package com.drivemind.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote
import com.drivemind.domain.usecase.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getAllNotesUseCase: GetAllNotesUseCase,
    private val searchNotesUseCase: SearchNotesUseCase,
    private val saveNoteUseCase: SaveNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val toggleTaskCompletionUseCase: ToggleTaskCompletionUseCase
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow<NoteType?>(null)
    val selectedFilter = _selectedFilter.asStateFlow()

    private val _deletedNote = MutableSharedFlow<VoiceNote>()
    val deletedNote = _deletedNote.asSharedFlow()

    @OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<VoiceNote>> = combine(
        _searchQuery.debounce(300),
        _selectedFilter
    ) { query, filter ->
        Pair(query, filter)
    }.flatMapLatest { (query, filter) ->
        val matchingNotes = if (query.isBlank()) getAllNotesUseCase() else searchNotesUseCase(query.trim())
        if (filter == null) matchingNotes else matchingNotes.map { notes ->
            notes.filter { it.type == filter }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(type: NoteType?) {
        _selectedFilter.value = type
    }

    fun deleteNote(note: VoiceNote) {
        viewModelScope.launch {
            deleteNoteUseCase(note).onSuccess {
                _deletedNote.emit(note)
            }
        }
    }

    fun undoDelete(note: VoiceNote) {
        viewModelScope.launch {
            saveNoteUseCase(note)
        }
    }

    fun toggleCompletion(note: VoiceNote) {
        viewModelScope.launch {
            toggleTaskCompletionUseCase(note.id, !note.isCompleted)
        }
    }
}
