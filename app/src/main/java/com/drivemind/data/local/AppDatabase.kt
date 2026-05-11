package com.drivemind.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.drivemind.data.local.dao.VoiceNoteDao
import com.drivemind.data.local.entity.VoiceNoteEntity

@Database(
    entities = [VoiceNoteEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun voiceNoteDao(): VoiceNoteDao

    companion object {
        const val DATABASE_NAME = "drivemind_db"
    }
}
