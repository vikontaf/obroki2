package ru.obroki.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import ru.obroki.app.domain.AppThemeMode
import ru.obroki.app.domain.IncomeReceipt

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class ThemePreferences(private val context: Context) {
    private val themeKey = stringPreferencesKey("theme_mode")
    private val tutorialShownKey = booleanPreferencesKey("tutorialShown")
    private val balanceKey = doublePreferencesKey("balance")
    private val incomeScheduleKey = stringPreferencesKey("income_schedule")

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

    val balance: Flow<Double> = context.dataStore.data.map { prefs ->
        prefs[balanceKey] ?: 0.0
    }

    val incomeSchedule: Flow<List<IncomeReceipt>> = context.dataStore.data.map { prefs ->
        decodeIncomeSchedule(prefs[incomeScheduleKey])
    }

    suspend fun setThemeMode(mode: AppThemeMode) {
        context.dataStore.edit { it[themeKey] = mode.name }
    }

    suspend fun setTutorialShown() {
        context.dataStore.edit { it[tutorialShownKey] = true }
    }

    suspend fun setBalance(value: Double) {
        context.dataStore.edit { it[balanceKey] = value }
    }

    suspend fun setIncomeSchedule(receipts: List<IncomeReceipt>) {
        context.dataStore.edit {
            it[incomeScheduleKey] = encodeIncomeSchedule(receipts)
        }
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

    companion object {
        fun encodeIncomeSchedule(receipts: List<IncomeReceipt>): String {
            val arr = JSONArray()
            for (r in receipts) {
                arr.put(
                    JSONObject()
                        .put("day", r.dayOfMonth)
                        .put("amount", r.amount),
                )
            }
            return arr.toString()
        }

        fun decodeIncomeSchedule(raw: String?): List<IncomeReceipt> {
            if (raw.isNullOrBlank()) return emptyList()
            return try {
                val arr = JSONArray(raw)
                buildList {
                    for (i in 0 until arr.length()) {
                        val obj = arr.getJSONObject(i)
                        val day = obj.getInt("day")
                        val amount = obj.getDouble("amount")
                        if (day in 1..31 && amount > 0.0) {
                            add(IncomeReceipt(dayOfMonth = day, amount = amount))
                        }
                    }
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
    }
}
