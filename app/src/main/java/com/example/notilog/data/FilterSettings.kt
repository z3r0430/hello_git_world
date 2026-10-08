package com.example.notilog.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "filter_settings")

enum class FilterMode { ALL, SELECTED }

data class FilterSettings(
    val recordingEnabled: Boolean = true,
    val mode: FilterMode = FilterMode.ALL,
    /** Conversation / sender names to record when [mode] is SELECTED. */
    val selected: Set<String> = emptySet(),
) {
    /** Decides whether a message from [conversation]/[sender] should be stored. */
    fun shouldRecord(conversation: String, sender: String): Boolean {
        if (!recordingEnabled) return false
        return when (mode) {
            FilterMode.ALL -> true
            FilterMode.SELECTED -> selected.any {
                it.equals(conversation, ignoreCase = true) ||
                    it.equals(sender, ignoreCase = true)
            }
        }
    }
}

class FilterSettingsStore(private val context: Context) {

    private object Keys {
        val ENABLED = booleanPreferencesKey("recording_enabled")
        val MODE = stringPreferencesKey("filter_mode")
        val SELECTED = stringSetPreferencesKey("selected")
    }

    val settings: Flow<FilterSettings> = context.dataStore.data.map { prefs ->
        FilterSettings(
            recordingEnabled = prefs[Keys.ENABLED] ?: true,
            mode = runCatching { FilterMode.valueOf(prefs[Keys.MODE] ?: FilterMode.ALL.name) }
                .getOrDefault(FilterMode.ALL),
            selected = prefs[Keys.SELECTED] ?: emptySet(),
        )
    }

    suspend fun setRecordingEnabled(enabled: Boolean) {
        context.dataStore.edit { it[Keys.ENABLED] = enabled }
    }

    suspend fun setMode(mode: FilterMode) {
        context.dataStore.edit { it[Keys.MODE] = mode.name }
    }

    suspend fun addSelected(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED] = (prefs[Keys.SELECTED] ?: emptySet()) + trimmed
        }
    }

    suspend fun removeSelected(name: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED] = (prefs[Keys.SELECTED] ?: emptySet()) - name
        }
    }
}
