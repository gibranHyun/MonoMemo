package com.monomemo.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsDataStore(private val context: Context) {

    private object Keys {
        val LAST_OPENED_NOTE_ID = longPreferencesKey("lastOpenedNoteId")
        val THEME_MODE = stringPreferencesKey("themeMode")
        val FONT_SIZE_SP = intPreferencesKey("fontSizeSp")
        val WRAP_ENABLED = booleanPreferencesKey("wrapEnabled")
        val LINE_NUMBERS_ENABLED = booleanPreferencesKey("lineNumbersEnabled")
    }

    val lastOpenedNoteId: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LAST_OPENED_NOTE_ID]
    }

    suspend fun setLastOpenedNoteId(id: Long) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LAST_OPENED_NOTE_ID] = id
        }
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[Keys.THEME_MODE] ?: "system"
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode
        }
    }

    val fontSizeSp: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.FONT_SIZE_SP] ?: 16
    }

    suspend fun setFontSizeSp(size: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FONT_SIZE_SP] = size
        }
    }

    val wrapEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.WRAP_ENABLED] ?: true
    }

    suspend fun setWrapEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WRAP_ENABLED] = enabled
        }
    }

    val lineNumbersEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.LINE_NUMBERS_ENABLED] ?: false
    }

    suspend fun setLineNumbersEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.LINE_NUMBERS_ENABLED] = enabled
        }
    }
}
