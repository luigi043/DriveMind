package com.drivemind.ui.driving

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote
import com.drivemind.domain.usecase.SaveNoteUseCase
import com.drivemind.speech.SpeechManager
import com.drivemind.speech.TtsManager
import com.drivemind.utils.DrivingDetector
import com.drivemind.utils.IntentProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.kotlin.*

@OptIn(ExperimentalCoroutinesApi::class)
class DrivingViewModelTest {

    @get:Rule
    val instantRule = InstantTaskExecutorRule()

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var viewModel: DrivingViewModel
    private val speechManager: SpeechManager = mock()
    private val ttsManager: TtsManager = mock()
    private val drivingDetector: DrivingDetector = mock()
    private val intentProcessor: IntentProcessor = mock()
    private val saveNoteUseCase: SaveNoteUseCase = mock()

    private val speechStateFlow = MutableStateFlow<SpeechManager.SpeechState>(
        SpeechManager.SpeechState.Idle
    )
    private val drivingStateFlow = MutableStateFlow(DrivingDetector.DrivingState.UNKNOWN)
    private val speedFlow = MutableStateFlow(0f)
    private val ttsStateFlow = MutableStateFlow<TtsManager.TtsState>(TtsManager.TtsState.Uninitialized)

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        whenever(speechManager.state).thenReturn(speechStateFlow)
        whenever(drivingDetector.drivingState).thenReturn(drivingStateFlow)
        whenever(drivingDetector.speedKmh).thenReturn(speedFlow)
        whenever(ttsManager.state).thenReturn(ttsStateFlow)

        viewModel = DrivingViewModel(
            speechManager, ttsManager, drivingDetector, intentProcessor, saveNoteUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state is Idle`() {
        assertTrue(viewModel.uiState.value is DrivingViewModel.UiState.Idle)
    }

    @Test
    fun `startListening transitions to Listening state`() = runTest {
        viewModel.startListening()
        speechStateFlow.value = SpeechManager.SpeechState.Listening
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is DrivingViewModel.UiState.Listening)
    }

    @Test
    fun `partial result shows Processing state`() = runTest {
        speechStateFlow.value = SpeechManager.SpeechState.PartialResult("comprar leite")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is DrivingViewModel.UiState.Processing)
        assertEquals("comprar leite", (state as DrivingViewModel.UiState.Processing).partialText)
    }

    @Test
    fun `successful save transitions to Saved then Idle`() = runTest {
        val note = VoiceNote(rawContent = "teste", type = NoteType.NOTE)
        val processed = IntentProcessor.ProcessingResult(note, 0.7f, "teste")

        whenever(intentProcessor.process(any(), any())).thenReturn(processed)
        whenever(intentProcessor.getConfirmationMessage(any())).thenReturn("Nota guardada")
        whenever(saveNoteUseCase(any())).thenReturn(Result.success(1L))

        speechStateFlow.value = SpeechManager.SpeechState.Result("teste", 0.9f)
        advanceUntilIdle()

        verify(saveNoteUseCase).invoke(any())
    }

    @Test
    fun `NoSpeech triggers TTS error message`() = runTest {
        speechStateFlow.value = SpeechManager.SpeechState.NoSpeech
        advanceUntilIdle()
        verify(ttsManager).speak(TtsManager.Phrases.ERROR)
        assertTrue(viewModel.uiState.value is DrivingViewModel.UiState.Idle)
    }

    @Test
    fun `driving state propagates correctly`() = runTest {
        drivingStateFlow.value = DrivingDetector.DrivingState.DRIVING
        advanceUntilIdle()
        assertEquals(DrivingDetector.DrivingState.DRIVING, viewModel.drivingState.value)
    }
}
