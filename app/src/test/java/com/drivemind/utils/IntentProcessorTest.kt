package com.drivemind.utils

import android.content.Context
import com.drivemind.R
import com.drivemind.domain.model.NoteCategory
import com.drivemind.domain.model.NoteType
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class IntentProcessorTest {

    private lateinit var processor: IntentProcessor
    private lateinit var context: Context

    @Before
    fun setup() {
        context = mock(Context::class.java)
        processor = IntentProcessor(context)
    }

    @Test
    fun `task keywords classify as TASK`() {
        val result = processor.process("need to buy milk")
        assertEquals(NoteType.TASK, result.note.type)
    }

    @Test
    fun `reminder keywords with time classify as REMINDER`() {
        val result = processor.process("remind me to call doctor tomorrow at 10")
        assertEquals(NoteType.REMINDER, result.note.type)
    }

    @Test
    fun `idea keywords classify as IDEA`() {
        val result = processor.process("idea for project: use machine learning")
        assertEquals(NoteType.IDEA, result.note.type)
    }

    @Test
    fun `plain sentence classifies as NOTE`() {
        val result = processor.process("today the weather was very good")
        assertEquals(NoteType.NOTE, result.note.type)
    }

    @Test
    fun `shopping keywords set SHOPPING category`() {
        val result = processor.process("buy bread at the supermarket")
        assertEquals(NoteCategory.SHOPPING, result.note.category)
    }

    @Test
    fun `work keywords set WORK category`() {
        val result = processor.process("meeting with client tomorrow")
        assertEquals(NoteCategory.WORK, result.note.category)
    }

    @Test
    fun `health keywords set HEALTH category`() {
        val result = processor.process("book an appointment with doctor")
        assertEquals(NoteCategory.HEALTH, result.note.category)
    }

    @Test
    fun `processed content is capitalized`() {
        val result = processor.process("buy milk")
        assertEquals('B', result.note.processedContent.first())
    }

    @Test
    fun `processed content ends with period`() {
        val result = processor.process("buy milk")
        assert(result.note.processedContent.endsWith("."))
    }

    @Test
    fun `processed content does not double punctuate`() {
        val result = processor.process("buy milk!")
        assert(!result.note.processedContent.endsWith("!."))
    }

    @Test
    fun `empty-ish content still processes without crash`() {
        val result = processor.process("ok")
        assertEquals(NoteType.NOTE, result.note.type)
    }

    @Test
    fun `confirmation message includes content for tasks`() {
        val result = processor.process("need to call John")
        `when`(context.getString(R.string.task_saved_format, result.note.processedContent))
            .thenReturn("Task saved: Need to call John.")
        
        val confirmation = processor.getConfirmationMessage(result.note)
        assert(confirmation.contains("Task"))
    }
}
