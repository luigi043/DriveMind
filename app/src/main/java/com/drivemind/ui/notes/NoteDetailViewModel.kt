package com.drivemind.ui.notes

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drivemind.domain.model.NoteCategory
import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote
import com.drivemind.domain.usecase.DeleteNoteUseCase
import com.drivemind.domain.usecase.UpdateNoteUseCase
import com.drivemind.speech.TtsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NoteDetailViewModel @Inject constructor(
    private val updateNoteUseCase: UpdateNoteUseCase,
    private val deleteNoteUseCase: DeleteNoteUseCase,
    private val ttsManager: TtsManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    sealed class UiState {
        object Loading : UiState()
        data class Viewing(val note: VoiceNote) : UiState()
        data class Editing(val note: VoiceNote) : UiState()
        object Deleted : UiState()
        data class Error(val msg: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val ttsReady = MutableStateFlow(false)

    init {
        val note: VoiceNote? = savedStateHandle["note"]
        if (note != null) {
            _uiState.value = UiState.Viewing(note)
        } else {
            _uiState.value = UiState.Error("Nota não encontrada")
        }
        ttsManager.initialize { ttsReady.value = true }
    }

    fun startEditing() {
        val current = (_uiState.value as? UiState.Viewing)?.note ?: return
        _uiState.value = UiState.Editing(current)
    }

    fun saveEdits(newContent: String, newType: NoteType, newCategory: NoteCategory) {
        val current = (_uiState.value as? UiState.Editing)?.note ?: return
        viewModelScope.launch {
            val updated = current.copy(
                processedContent = newContent,
                type = newType,
                category = newCategory
            )
            updateNoteUseCase(updated).onSuccess {
                _uiState.value = UiState.Viewing(updated)
            }.onFailure {
                _uiState.value = UiState.Error("Falha ao guardar alterações")
            }
        }
    }

    fun cancelEditing() {
        val current = (_uiState.value as? UiState.Editing)?.note ?: return
        _uiState.value = UiState.Viewing(current)
    }

    fun deleteNote() {
        val current = when (val s = _uiState.value) {
            is UiState.Viewing -> s.note
            is UiState.Editing -> s.note
            else -> return
        }
        viewModelScope.launch {
            deleteNoteUseCase(current).onSuccess {
                _uiState.value = UiState.Deleted
            }
        }
    }

    fun readNoteAloud() {
        val note = (_uiState.value as? UiState.Viewing)?.note ?: return
        ttsManager.speakInterrupt(note.processedContent)
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
