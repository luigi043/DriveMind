package com.drivemind.domain.model

import android.os.Parcelable
import com.drivemind.R
import kotlinx.parcelize.Parcelize

/**
 * Core domain model for a captured voice note.
 * Pure Kotlin - no Android/framework dependencies at this layer.
 */
@Parcelize
data class VoiceNote(
    val id: Long = 0,
    val rawContent: String,
    val processedContent: String = rawContent,
    val type: NoteType = NoteType.NOTE,
    val category: NoteCategory = NoteCategory.GENERAL,
    val timestamp: Long = System.currentTimeMillis(),
    val isCompleted: Boolean = false,
    val reminderTime: Long? = null,
    val durationSeconds: Int = 0
) : Parcelable

enum class NoteType(val labelResId: Int) {
    NOTE(R.string.type_note),
    TASK(R.string.type_task),
    REMINDER(R.string.type_reminder),
    IDEA(R.string.type_idea)
}

enum class NoteCategory(val labelResId: Int, val emoji: String) {
    GENERAL(R.string.category_general, "#"),
    WORK(R.string.category_work, "W"),
    PERSONAL(R.string.category_personal, "P"),
    SHOPPING(R.string.category_shopping, "S"),
    HEALTH(R.string.category_health, "H"),
    TRAVEL(R.string.category_travel, "T")
}
