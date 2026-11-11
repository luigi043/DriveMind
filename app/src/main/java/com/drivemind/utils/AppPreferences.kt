package com.drivemind.utils

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "drivemind_prefs")

/**
 * Persistent preferences for user settings.
 */
@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    // Keys
    private object Keys {
        val AUTO_DRIVING_MODE = booleanPreferencesKey("auto_driving_mode")
        val TTS_ENABLED = booleanPreferencesKey("tts_enabled")
        val PREFERRED_LANGUAGE = stringPreferencesKey("preferred_language")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }

    // Flows
    val autoDrivingMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.AUTO_DRIVING_MODE] ?: true
    }

    val ttsEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.TTS_ENABLED] ?: true
    }

    val preferredLanguage: Flow<String> = dataStore.data.map { prefs ->
        prefs[Keys.PREFERRED_LANGUAGE] ?: "en-US"
    }

    val onboardingComplete: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETE] ?: false
    }

    // Setters
    suspend fun setAutoDrivingMode(enabled: Boolean) {
        dataStore.edit { it[Keys.AUTO_DRIVING_MODE] = enabled }
    }

    suspend fun setTtsEnabled(enabled: Boolean) {
        dataStore.edit { it[Keys.TTS_ENABLED] = enabled }
    }

    suspend fun setPreferredLanguage(lang: String) {
        dataStore.edit { it[Keys.PREFERRED_LANGUAGE] = lang }
    }

    suspend fun setOnboardingComplete() {
        dataStore.edit { it[Keys.ONBOARDING_COMPLETE] = true }
    }
}
