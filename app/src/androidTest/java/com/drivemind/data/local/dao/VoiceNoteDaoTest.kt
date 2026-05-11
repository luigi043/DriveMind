package com.drivemind.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.drivemind.data.local.AppDatabase
import com.drivemind.data.local.entity.VoiceNoteEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class VoiceNoteDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: VoiceNoteDao

    @Before
    fun createDb() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.voiceNoteDao()
    }

    @After
    fun closeDb() = db.close()

    private fun buildEntity(
        id: Long = 0,
        content: String = "test note",
        type: String = "NOTE"
    ) = VoiceNoteEntity(
        id = id,
        rawContent = content,
        processedContent = content,
        type = type,
        category = "GENERAL",
        timestamp = System.currentTimeMillis()
    )

    @Test
    fun insertAndRetrieve() = runBlocking {
        val entity = buildEntity(content = "buy milk")
        val id = dao.insertNote(entity)
        assertTrue(id > 0)

        val fetched = dao.getNoteById(id)
        assertNotNull(fetched)
        assertEquals("buy milk", fetched?.rawContent)
    }

    @Test
    fun getAllNotesReturnsInserted() = runBlocking {
        dao.insertNote(buildEntity(content = "note one"))
        dao.insertNote(buildEntity(content = "note two"))

        val notes = dao.getAllNotes().first()
        assertEquals(2, notes.size)
    }

    @Test
    fun deleteRemovesNote() = runBlocking {
        val entity = buildEntity(content = "to delete")
        val id = dao.insertNote(entity)
        val inserted = dao.getNoteById(id)!!
        dao.deleteNote(inserted)

        assertNull(dao.getNoteById(id))
    }

    @Test
    fun searchFindsMatchingNotes() = runBlocking {
        dao.insertNote(buildEntity(content = "buy groceries"))
        dao.insertNote(buildEntity(content = "call dentist"))

        val results = dao.searchNotes("groceries").first()
        assertEquals(1, results.size)
        assertEquals("buy groceries", results.first().rawContent)
    }

    @Test
    fun filterByTypeReturnsCorrect() = runBlocking {
        dao.insertNote(buildEntity(content = "a note", type = "NOTE"))
        dao.insertNote(buildEntity(content = "a task", type = "TASK"))
        dao.insertNote(buildEntity(content = "another task", type = "TASK"))

        val tasks = dao.getNotesByType("TASK").first()
        assertEquals(2, tasks.size)
        assertTrue(tasks.all { it.type == "TASK" })
    }

    @Test
    fun markCompletedUpdatesFlag() = runBlocking {
        val id = dao.insertNote(buildEntity(type = "TASK"))
        dao.markCompleted(id, true)

        val note = dao.getNoteById(id)
        assertTrue(note?.isCompleted == true)
    }

    @Test
    fun countReflectsInserts() = runBlocking {
        assertEquals(0, dao.getCount().first())
        dao.insertNote(buildEntity())
        dao.insertNote(buildEntity())
        assertEquals(2, dao.getCount().first())
    }

    @Test
    fun deleteAllClearsTable() = runBlocking {
        dao.insertNote(buildEntity())
        dao.insertNote(buildEntity())
        dao.deleteAll()
        assertEquals(0, dao.getCount().first())
    }
}
