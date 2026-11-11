package com.drivemind.ui.history

import androidx.lifecycle.viewModelScope
import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote
import com.drivemind.domain.usecase.GetAllNotesUseCase
import com.drivemind.domain.usecase.SearchNotesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModelTest {
    @Test
    fun searchStillRespectsSelectedType() = runTest {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScheduler))
        lateinit var model: HistoryViewModel
        try {
            val task = VoiceNote(id = 1, rawContent = "Call the supplier", type = NoteType.TASK)
            val note = VoiceNote(id = 2, rawContent = "Supplier address", type = NoteType.NOTE)
            val allNotes: GetAllNotesUseCase = mock()
            val searchNotes: SearchNotesUseCase = mock()
            whenever(allNotes()).thenReturn(flowOf(listOf(task, note)))
            whenever(searchNotes("supplier")).thenReturn(flowOf(listOf(task, note)))
            model = HistoryViewModel(allNotes, searchNotes, mock(), mock(), mock())
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
                model.notes.collect {}
            }

            model.onSearchQueryChanged(" supplier ")
            model.setFilter(NoteType.TASK)
            advanceUntilIdle()

            assertEquals(listOf(task), model.notes.value)
        } finally {
            model.viewModelScope.cancel()
            Dispatchers.resetMain()
        }
    }
}
