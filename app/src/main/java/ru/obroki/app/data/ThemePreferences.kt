package ru.obroki.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.obroki.app.domain.AppThemeMode

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val tutorialShownKey = booleanPreferencesKey("tutorialShown")

    val themeMode: Flow<AppThemeMode> = context.dataStore.data.map { prefs ->
        when (prefs[themeKey]) {
            AppThemeMode.DARK.name -> AppThemeMode.DARK
            AppThemeMode.LIGHT.name -> AppThemeMode.LIGHT
            else -> AppThemeMode.SYSTEM
        }
    }

    /** False until Instructions has been shown once (first launch). */
    val tutorialShown: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[tutorialShownKey] == true
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[themeKey] = mode.name }
    }

    suspend fun setTutorialShown() {
        context.dataStore.edit { it[tutorialShownKey] = true }
    }

    suspend fun cycleTheme(current: AppThemeMode): AppThemeMode {
        val next = when (current) {
            AppThemeMode.DARK -> AppThemeMode.LIGHT
            AppThemeMode.LIGHT -> AppThemeMode.SYSTEM
            AppThemeMode.SYSTEM -> AppThemeMode.DARK
        }
        setThemeMode(next)
        return next
    }
}
