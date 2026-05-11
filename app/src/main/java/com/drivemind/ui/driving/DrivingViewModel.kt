package com.drivemind.ui.driving

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.drivemind.domain.model.VoiceNote
import com.drivemind.domain.usecase.SaveNoteUseCase
import com.drivemind.speech.SpeechManager
import com.drivemind.speech.TtsManager
import com.drivemind.utils.DrivingDetector
import com.drivemind.utils.IntentProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DrivingViewModel @Inject constructor(
    private val speechManager: SpeechManager,
    private val ttsManager: TtsManager,
    private val drivingDetector: DrivingDetector,
    private val intentProcessor: IntentProcessor,
    private val saveNoteUseCase: SaveNoteUseCase
) : ViewModel() {

    // ---- UI State ----
    sealed class UiState {
        object Idle : UiState()
        object Listening : UiState()
        data class Processing(val partialText: String) : UiState()
        data class Saved(val note: VoiceNote) : UiState()
        data class Error(val message: String) : UiState()
    }

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    val drivingState: StateFlow<DrivingDetector.DrivingState> = drivingDetector.drivingState
    val speedKmh: StateFlow<Float> = drivingDetector.speedKmh
    val ttsState: StateFlow<TtsManager.TtsState> = ttsManager.state

    private var recordingStartTime = 0L

    init {
        observeSpeech()
        ttsManager.initialize()
    }

    fun startListening() {
        if (_uiState.value is UiState.Listening) return
        recordingStartTime = System.currentTimeMillis()
        speechManager.startListening()
    }

    fun stopListening() {
        speechManager.stopListening()
    }

    fun startDrivingDetection() {
        drivingDetector.startDetection()
    }

    fun stopDrivingDetection() {
        drivingDetector.stopDetection()
    }

    private fun observeSpeech() {
        speechManager.state
            .onEach { speechState ->
                when (speechState) {
                    is SpeechManager.SpeechState.Idle -> {
                        if (_uiState.value is UiState.Listening) {
                            _uiState.value = UiState.Idle
                        }
                    }
                    is SpeechManager.SpeechState.Listening -> {
                        _uiState.value = UiState.Listening
                    }
                    is SpeechManager.SpeechState.PartialResult -> {
                        _uiState.value = UiState.Processing(speechState.text)
                    }
                    is SpeechManager.SpeechState.Result -> {
                        processAndSave(speechState.text)
                    }
                    is SpeechManager.SpeechState.NoSpeech -> {
                        _uiState.value = UiState.Idle
                        ttsManager.speak(TtsManager.Phrases.ERROR)
                    }
                    is SpeechManager.SpeechState.Error -> {
                        _uiState.value = UiState.Error(speechState.message)
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun processAndSave(rawText: String) {
        viewModelScope.launch {
            val durationSeconds = ((System.currentTimeMillis() - recordingStartTime) / 1000).toInt()
            val result = intentProcessor.process(rawText, durationSeconds)

            saveNoteUseCase(result.note).fold(
                onSuccess = { savedId ->
                    val savedNote = result.note.copy(id = savedId)
                    _uiState.value = UiState.Saved(savedNote)
                    ttsManager.speak(intentProcessor.getConfirmationMessage(savedNote))

                    // Return to idle after brief success period
                    kotlinx.coroutines.delay(2000)
                    _uiState.value = UiState.Idle
                },
                onFailure = {
                    _uiState.value = UiState.Error("Erro ao guardar nota")
                    ttsManager.speak("Erro ao guardar. Tente novamente.")
                }
            )
        }
    }

    fun resetError() {
        _uiState.value = UiState.Idle
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
        ttsManager.shutdown()
        drivingDetector.stopDetection()
    }
}
