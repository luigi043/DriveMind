package com.drivemind.utils

import android.content.Context
import com.drivemind.R
import com.drivemind.domain.model.NoteCategory
import com.drivemind.domain.model.NoteType
import com.drivemind.domain.model.VoiceNote
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Processes raw speech text and classifies it into structured VoiceNote objects.
 * Uses keyword-based NLP rules — no network required. Works 100% offline.
 */
@Singleton
class IntentProcessor @Inject constructor(
    @ApplicationContext private val context: Context
) {

    data class ProcessingResult(
        val note: VoiceNote,
        val confidence: Float,
        val originalText: String
    )

    private val taskKeywords = setOf(
        "need to", "have to", "must", "buy", "call", "send", "book", "schedule", "get"
    )

    private val reminderKeywords = setOf(
        "tomorrow", "today", "at", "o'clock", "remind me"
    )

    private val ideaKeywords = setOf(
        "idea", "what if", "maybe", "could", "should we", "thought about"
    )

    private val workKeywords = setOf(
        "work", "project", "client", "report", "boss", "office", "deadline", "meeting"
    )

    private val shoppingKeywords = setOf(
        "buy", "store", "shop", "supermarket", "order", "milk", "bread"
    )

    private val healthKeywords = setOf(
        "doctor", "pharmacy", "medicine", "health", "appointment"
    )

    fun process(rawText: String, durationSeconds: Int = 0): ProcessingResult {
        val lower = rawText.lowercase().trim()
        val type = classifyType(lower)
        val category = classifyCategory(lower)
        val processed = processText(rawText, type)

        return ProcessingResult(
            note = VoiceNote(
                rawContent = rawText,
                processedContent = processed,
                type = type,
                category = category,
                timestamp = System.currentTimeMillis(),
                durationSeconds = durationSeconds
            ),
            confidence = calculateConfidence(lower, type, category),
            originalText = rawText
        )
    }

    private fun classifyType(text: String): NoteType {
        val taskScore = taskKeywords.count { text.contains(it) }
        val reminderScore = reminderKeywords.count { text.contains(it) }
        val ideaScore = ideaKeywords.count { text.contains(it) }
        val hasReminderIntent = text.contains("remind me")
        val hasSpecificTime =
                Regex("""\bat\s+\d{1,2}\b""").containsMatchIn(text) ||
                Regex("""\b\d{1,2}(:\d{2})?\s*(am|pm)\b""").containsMatchIn(text)

        return when {
            reminderScore > 0 && (hasReminderIntent || hasSpecificTime) -> NoteType.REMINDER
            taskScore >= reminderScore && taskScore >= ideaScore && taskScore > 0 -> NoteType.TASK
            ideaScore > taskScore && ideaScore > 0 -> NoteType.IDEA
            else -> NoteType.NOTE
        }
    }

    private fun classifyCategory(text: String): NoteCategory {
        val scores = mapOf(
            NoteCategory.WORK to workKeywords.count { text.contains(it) },
            NoteCategory.SHOPPING to shoppingKeywords.count { text.contains(it) },
            NoteCategory.HEALTH to healthKeywords.count { text.contains(it) }
        )

        val best = scores.maxByOrNull { it.value }
        return if ((best?.value ?: 0) > 0) best!!.key else NoteCategory.GENERAL
    }

    private fun processText(raw: String, type: NoteType): String {
        // Capitalize first letter, ensure proper punctuation
        val trimmed = raw.trim()
        val capitalized = trimmed.replaceFirstChar { it.uppercase() }
        return if (capitalized.endsWith(".") || capitalized.endsWith("!") ||
            capitalized.endsWith("?")) capitalized else "$capitalized."
    }

    private fun calculateConfidence(text: String, type: NoteType, category: NoteCategory): Float {
        var score = 0.5f // Base confidence
        if (type != NoteType.NOTE) score += 0.2f
        if (category != NoteCategory.GENERAL) score += 0.15f
        if (text.length > 20) score += 0.1f
        if (text.length > 50) score += 0.05f
        return score.coerceIn(0f, 1f)
    }

    /**
     * Generate a spoken confirmation message for TTS.
     */
    fun getConfirmationMessage(note: VoiceNote): String = when (note.type) {
        NoteType.TASK -> context.getString(R.string.task_saved_format, note.processedContent)
        NoteType.REMINDER -> context.getString(R.string.reminder_saved)
        NoteType.IDEA -> context.getString(R.string.idea_saved)
        NoteType.NOTE -> context.getString(R.string.note_saved)
    }
}
