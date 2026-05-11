package com.drivemind.speech

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages text-to-speech output for eyes-free confirmation.
 * Critical for driving mode - user must never need to look at the screen.
 */
@Singleton
class TtsManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    sealed class TtsState {
        object Uninitialized : TtsState()
        object Ready : TtsState()
        object Speaking : TtsState()
        data class Error(val message: String) : TtsState()
    }

    private val _state = MutableStateFlow<TtsState>(TtsState.Uninitialized)
    val state: StateFlow<TtsState> = _state.asStateFlow()

    private var tts: TextToSpeech? = null

    // Predefined phrases for driving mode (avoid generating text at runtime)
    object Phrases {
        const val NOTE_SAVED = "Note saved successfully"
        const val TASK_SAVED = "Task added"
        const val REMINDER_SAVED = "Reminder created"
        const val LISTENING = "Speak now"
        const val ERROR = "I couldn't understand. Try again."
        const val DRIVING_DETECTED = "Driving mode active"
        const val DRIVING_STOPPED = "Driving mode inactive"
        const val READY = "DriveMind ready"
    }

    fun initialize(onReady: (() -> Unit)? = null) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val langResult = tts?.setLanguage(Locale.US)
                if (langResult == TextToSpeech.LANG_MISSING_DATA ||
                    langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                    // Fallback to default
                    tts?.setLanguage(Locale.getDefault())
                }
                tts?.setSpeechRate(0.95f)
                tts?.setPitch(1.0f)
                tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        _state.value = TtsState.Speaking
                    }

                    override fun onDone(utteranceId: String?) {
                        _state.value = TtsState.Ready
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        _state.value = TtsState.Ready
                    }
                })
                _state.value = TtsState.Ready
                onReady?.invoke()
            } else {
                _state.value = TtsState.Error("TTS initialization failed")
            }
        }
    }

    fun speak(resId: Int, utteranceId: String = "drivemind_tts") {
        val text = context.getString(resId)
        speak(text, utteranceId)
    }

    fun speak(text: String, utteranceId: String = "drivemind_tts") {
        if (_state.value !is TtsState.Ready && _state.value !is TtsState.Speaking) return
        tts?.speak(text, TextToSpeech.QUEUE_ADD, null, utteranceId)
    }

    fun speakInterrupt(text: String) {
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "interrupt")
    }

    fun stop() {
        tts?.stop()
        _state.value = TtsState.Ready
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _state.value = TtsState.Uninitialized
    }

    val isReady: Boolean
        get() = _state.value is TtsState.Ready
}
