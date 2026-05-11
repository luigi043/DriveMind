package com.drivemind.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.drivemind.domain.model.NoteCategory
import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote

@Entity(
    tableName = "voice_notes",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["type"]),
        Index(value = ["is_completed"])
    ]
)
data class VoiceNoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "raw_content")
    val rawContent: String,

    @ColumnInfo(name = "processed_content")
    val processedContent: String,

    @ColumnInfo(name = "type")
    val type: String,

    @ColumnInfo(name = "category")
    val category: String,

    @ColumnInfo(name = "timestamp")
    val timestamp: Long,

    @ColumnInfo(name = "is_completed")
    val isCompleted: Boolean = false,

    @ColumnInfo(name = "reminder_time")
    val reminderTime: Long? = null,

    @ColumnInfo(name = "duration_seconds")
    val durationSeconds: Int = 0
) {
    fun toDomain(): VoiceNote = VoiceNote(
        id = id,
        rawContent = rawContent,
        processedContent = processedContent,
        type = NoteType.valueOf(type),
        category = NoteCategory.valueOf(category),
        timestamp = timestamp,
        isCompleted = isCompleted,
        reminderTime = reminderTime,
        durationSeconds = durationSeconds
    )

    companion object {
        fun fromDomain(note: VoiceNote): VoiceNoteEntity = VoiceNoteEntity(
            id = note.id,
            rawContent = note.rawContent,
            processedContent = note.processedContent,
            type = note.type.name,
            category = note.category.name,
            timestamp = note.timestamp,
            isCompleted = note.isCompleted,
            reminderTime = note.reminderTime,
            durationSeconds = note.durationSeconds
        )
    }
}
